package com.uccd3223.p1_chai_boon_hong_2206806

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit

open class BaseGameActivity : AppCompatActivity() {

    private var isHistorySaved = false

    protected var gameMode: String = "FUN"
    protected var score: Int = 0 // Also acts as questions answered for Score Attack
    protected var isGameOver: Boolean = false
    
    private var tvScore: TextView? = null
    private var tvTimer: TextView? = null
    
    private var countDownTimer: CountDownTimer? = null
    // Time Attack
    private var timeLimitMs: Long = 60000L
    private var remainingTimeMs: Long = 0L
    
    // Score Attack Rounds
    private var currentRound: Int = 1
    private var targetQuestionsForRound: Int = 10
    private var timeLimitForRoundSeconds: Int = 30
    
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gameMode = intent.getStringExtra("GAME_MODE") ?: "FUN"
        timeLimitMs = intent.getLongExtra("TIME_LIMIT", 60000L)
        prefs = getSharedPreferences("GameHighScores", Context.MODE_PRIVATE)
        score = savedInstanceState?.getInt(STATE_SCORE) ?: 0
        currentRound = savedInstanceState?.getInt(STATE_ROUND) ?: 1
        roundScore = savedInstanceState?.getInt(STATE_ROUND_SCORE) ?: 0
        remainingTimeMs = savedInstanceState?.getLong(STATE_TIME_REMAINING) ?: 0L
        isHistorySaved = savedInstanceState?.getBoolean(STATE_HISTORY_SAVED) ?: false
    }

    protected fun setupGameModeUI() {
        tvScore = findViewById(R.id.tvScore)
        tvTimer = findViewById(R.id.tvTimer)
        
        when (gameMode) {
            "TIME_ATTACK" -> {
                tvScore?.visibility = View.VISIBLE
                tvTimer?.visibility = View.VISIBLE
                updateScoreText()
                val duration = remainingTimeMs.takeIf { it > 0L } ?: timeLimitMs
                updateTimerText(duration / 1000)
                startGameTimer(duration)
            }
            "SCORE_ATTACK" -> {
                tvScore?.visibility = View.VISIBLE
                tvTimer?.visibility = View.VISIBLE
                targetQuestionsForRound = 10 + (currentRound - 1) * 5
                timeLimitForRoundSeconds = 30 + (currentRound - 1) * 10
                updateScoreAttackUI()
                startGameTimer(remainingTimeMs.takeIf { it > 0L } ?: timeLimitForRoundSeconds * 1000L)
            }
            else -> {
                tvScore?.visibility = View.GONE
                tvTimer?.visibility = View.GONE
            }
        }
    }
    
    private fun startScoreAttackRound() {
        targetQuestionsForRound = 10 + (currentRound - 1) * 5
        timeLimitForRoundSeconds = 30 + (currentRound - 1) * 10
        
        // Reset score for the round, or keep cumulative? Usually cumulative is better for high score.
        // The requirement says "Round 1 - 10 questions in 30 seconds, each round add 5 questions...". 
        // We will keep score cumulative, so we need `roundScore` to track progress in the current round.
        
        updateScoreAttackUI()
        startGameTimer(timeLimitForRoundSeconds * 1000L)
    }
    
    private var roundScore: Int = 0
    
    private fun updateScoreAttackUI() {
        tvScore?.text = getString(R.string.round_format, currentRound, roundScore, targetQuestionsForRound)
    }
    
    protected fun onQuestionCompleted() {
        if (isGameOver) return
        score++
        
        if (gameMode == "FUN") return
        
        if (gameMode == "TIME_ATTACK") {
            updateScoreText()
        } else if (gameMode == "SCORE_ATTACK") {
            roundScore++
            updateScoreAttackUI()
            
            if (roundScore >= targetQuestionsForRound) {
                // Round completed!
                countDownTimer?.cancel()
                currentRound++
                roundScore = 0
                
                val nextRoundSeconds = 30 + (currentRound - 1) * 10
                android.widget.Toast.makeText(
                    this,
                    resources.getQuantityString(
                        R.plurals.next_round,
                        nextRoundSeconds,
                        currentRound,
                        nextRoundSeconds
                    ),
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                startScoreAttackRound()
            }
        }
    }
    
    private fun updateScoreText() {
        tvScore?.text = getString(R.string.score_format, score)
    }
    
    private fun updateTimerText(seconds: Long) {
        tvTimer?.text = getString(R.string.time_format, seconds)
    }
    
    private fun startGameTimer(durationMs: Long) {
        countDownTimer?.cancel()
        remainingTimeMs = durationMs
        countDownTimer = object : CountDownTimer(durationMs, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTimeMs = millisUntilFinished
                updateTimerText(millisUntilFinished / 1000)
            }
            override fun onFinish() {
                remainingTimeMs = 0L
                updateTimerText(0)
                endGame()
            }
        }.start()
    }
    
    private fun getHighScoreKey(): String {
        val gameName = this.javaClass.simpleName
        return if (gameMode == "TIME_ATTACK") {
            "${gameName}_TA_${timeLimitMs}"
        } else {
            "${gameName}_SA"
        }
    }
    
    private fun endGame() {
        if (isGameOver) return
        isGameOver = true
        countDownTimer?.cancel()
        
        val key = getHighScoreKey()
        val highScore = prefs.getInt(key, 0)
        
        val finalScore = if (gameMode == "SCORE_ATTACK") currentRound else score
        val isNewHighScore = finalScore > highScore
        
        saveHistoryRecord(finalScore)
        
        if (isNewHighScore) {
            prefs.edit { putInt(key, finalScore) }
        }
        
        val recordText = if (isNewHighScore) {
            getString(R.string.new_high_score)
        } else {
            getString(R.string.best_score, highScore)
        }
        
        val message = when (gameMode) {
            "TIME_ATTACK" -> resources.getQuantityString(
                R.plurals.time_attack_result,
                score,
                score
            )
            "SCORE_ATTACK" -> getString(R.string.round_rush_result, currentRound)
            else -> getString(R.string.great_job)
        }
        
        AlertDialog.Builder(this)
            .setTitle(R.string.challenge_complete)
            .setMessage("$message\n\n$recordText")
            .setCancelable(false)
            .setPositiveButton(R.string.main_menu) { _, _ ->
                finish()
            }
            .setNegativeButton(R.string.play_again) { _, _ ->
                recreate()
            }
            .show()
    }

    private fun saveHistoryRecord(finalScore: Int) {
        if (isHistorySaved) return
        val record = HistoryRecord(
            gameName = this.javaClass.simpleName.replace("Activity", ""),
            mode = gameMode,
            score = finalScore,
            timestamp = System.currentTimeMillis()
        )
        HistoryManager.saveRecord(this, record)
        isHistorySaved = true
    }

    override fun onDestroy() {
        countDownTimer?.cancel()
        if (isFinishing && gameMode == "FUN" && score > 0) {
            saveHistoryRecord(score)
        }
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(STATE_SCORE, score)
        outState.putInt(STATE_ROUND, currentRound)
        outState.putInt(STATE_ROUND_SCORE, roundScore)
        outState.putLong(STATE_TIME_REMAINING, remainingTimeMs)
        outState.putBoolean(STATE_HISTORY_SAVED, isHistorySaved)
        super.onSaveInstanceState(outState)
    }

    companion object {
        private const val STATE_SCORE = "state_score"
        private const val STATE_ROUND = "state_round"
        private const val STATE_ROUND_SCORE = "state_round_score"
        private const val STATE_TIME_REMAINING = "state_time_remaining"
        private const val STATE_HISTORY_SAVED = "state_history_saved"
    }
}
