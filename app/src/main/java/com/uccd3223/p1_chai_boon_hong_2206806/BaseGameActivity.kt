package com.uccd3223.p1_chai_boon_hong_2206806

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

open class BaseGameActivity : AppCompatActivity() {

    private var isHistorySaved = false

    protected var gameMode: String = "FUN"
    protected var score: Int = 0 // Also acts as questions answered for Score Attack
    protected var timeElapsed: Int = 0
    
    private var tvScore: TextView? = null
    private var tvTimer: TextView? = null
    
    private var countDownTimer: CountDownTimer? = null
    private val stopwatchHandler = Handler(Looper.getMainLooper())
    private var stopwatchRunnable: Runnable? = null
    
    // Time Attack
    private var timeLimitMs: Long = 60000L
    
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
    }

    protected fun setupGameModeUI() {
        tvScore = findViewById(R.id.tvScore)
        tvTimer = findViewById(R.id.tvTimer)
        
        when (gameMode) {
            "TIME_ATTACK" -> {
                tvScore?.visibility = View.VISIBLE
                tvTimer?.visibility = View.VISIBLE
                updateScoreText()
                updateTimerText(timeLimitMs / 1000)
                startGameTimer(timeLimitMs)
            }
            "SCORE_ATTACK" -> {
                tvScore?.visibility = View.VISIBLE
                tvTimer?.visibility = View.VISIBLE
                startScoreAttackRound()
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
        tvScore?.text = "R$currentRound ($roundScore/$targetQuestionsForRound)"
    }
    
    protected fun onQuestionCompleted() {
        if (gameMode == "FUN") return
        
        score++
        
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
                
                android.widget.Toast.makeText(this, "Round $currentRound! +${30 + (currentRound-1)*10}s", android.widget.Toast.LENGTH_SHORT).show()
                startScoreAttackRound()
            }
        }
    }
    
    private fun updateScoreText() {
        tvScore?.text = "Score: $score"
    }
    
    private fun updateTimerText(seconds: Long) {
        tvTimer?.text = "Time: ${seconds}s"
    }
    
    private fun startGameTimer(durationMs: Long) {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(durationMs, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                updateTimerText(millisUntilFinished / 1000)
            }
            override fun onFinish() {
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
        countDownTimer?.cancel()
        
        val key = getHighScoreKey()
        val highScore = prefs.getInt(key, 0)
        
        val finalScore = if (gameMode == "SCORE_ATTACK") currentRound else score
        val isNewHighScore = finalScore > highScore
        
        saveHistoryRecord(finalScore)
        
        if (isNewHighScore) {
            prefs.edit().putInt(key, finalScore).apply()
        }
        
        val recordText = if (isNewHighScore) "\nNew High Score!" else "\nBest: $highScore"
        
        val message = when (gameMode) {
            "TIME_ATTACK" -> "Time's up! You scored $score.$recordText"
            "SCORE_ATTACK" -> "Time's up! You reached Round $currentRound.$recordText"
            else -> "Good job!"
        }
        
        AlertDialog.Builder(this)
            .setTitle("Challenge Complete")
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Main Menu") { _, _ ->
                finish()
            }
            .setNegativeButton("Play Again") { _, _ ->
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
        super.onDestroy()
        countDownTimer?.cancel()
        if (gameMode == "FUN" && score > 0) {
            saveHistoryRecord(score)
        }
    }
}
