package com.uccd3223.p1_chai_boon_hong_2206806

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.os.CountDownTimer
import android.util.TypedValue
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView

data class AnswerChoice(
    val value: Int,
    val label: String,
    val contentDescription: String
)

open class BaseGameActivity : AppCompatActivity() {

    private var isHistorySaved = false
    private var pendingResultMessage: String? = null
    private var pendingRecordText: String? = null
    private var resultDialog: Dialog? = null

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
        isGameOver = savedInstanceState?.getBoolean(STATE_GAME_OVER) ?: false
        pendingResultMessage = savedInstanceState?.getString(STATE_RESULT_MESSAGE)
        pendingRecordText = savedInstanceState?.getString(STATE_RESULT_RECORD)
    }

    protected fun setupGameModeUI() {
        tvScore = findViewById(R.id.tvScore)
        tvTimer = findViewById(R.id.tvTimer)

        if (isGameOver) {
            tvScore?.visibility = View.VISIBLE
            tvTimer?.visibility = if (gameMode == "FUN") View.GONE else View.VISIBLE
            if (gameMode == "SCORE_ATTACK") updateScoreAttackUI() else updateScoreText()
            updateTimerText(0)
            tvScore?.post {
                val message = pendingResultMessage
                val record = pendingRecordText
                if (message != null && record != null) showResultDialog(message, record)
            }
            return
        }
        
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
                tvScore?.visibility = View.VISIBLE
                tvTimer?.visibility = View.GONE
                updateScoreText()
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

        when (gameMode) {
            "SCORE_ATTACK" -> {
                roundScore++
                updateScoreAttackUI()

                if (roundScore >= targetQuestionsForRound) {
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
            else -> updateScoreText()
        }
    }

    protected fun renderAnswerChoices(
        container: GridLayout,
        choices: List<AnswerChoice>,
        onSelected: (AnswerChoice, MaterialCardView) -> Unit
    ) {
        container.removeAllViews()
        val columns = if (
            resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        ) {
            choices.size
        } else {
            2
        }
        container.columnCount = columns
        container.rowCount = (choices.size + columns - 1) / columns
        val colors = listOf(
            R.color.answer_purple,
            R.color.answer_blue,
            R.color.answer_gold,
            R.color.answer_teal
        )
        val gap = resources.getDimensionPixelSize(R.dimen.answer_option_gap)

        choices.forEachIndexed { index, choice ->
            val card = layoutInflater.inflate(
                R.layout.item_answer_choicer,
                container,
                false
            ) as MaterialCardView
            card.setCardBackgroundColor(ContextCompat.getColor(this, colors[index % colors.size]))
            card.tag = choice.value
            card.contentDescription = choice.contentDescription
            card.findViewById<TextView>(R.id.answerChoicerText).apply {
                text = choice.label
                val size = when {
                    choice.label.any(Char::isLetter) && choice.label.length >= 11 -> 16f
                    choice.label.any(Char::isLetter) -> 18f
                    else -> 28f
                }
                setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
            }
            card.layoutParams = GridLayout.LayoutParams(
                GridLayout.spec(index / columns),
                GridLayout.spec(index % columns, 1f)
            ).apply {
                width = 0
                height = resources.getDimensionPixelSize(R.dimen.answer_option_min_height)
                setMargins(gap / 2, gap / 2, gap / 2, gap / 2)
            }
            card.setOnClickListener { onSelected(choice, card) }
            container.addView(card)
        }
    }

    protected fun completeAnswerChoice(
        container: GridLayout,
        selectedCard: MaterialCardView,
        feedback: TextView,
        message: String,
        onNextQuestion: () -> Unit
    ) {
        for (index in 0 until container.childCount) {
            container.getChildAt(index).isEnabled = false
        }
        selectedCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.answer_correct))
        selectedCard.strokeColor = ContextCompat.getColor(this, R.color.game_star)
        selectedCard.findViewById<TextView>(R.id.answerStateIcon).visibility = View.VISIBLE
        celebrateQuestion(selectedCard, feedback, message, onNextQuestion)
    }

    protected fun retryAnswerChoice(
        card: MaterialCardView,
        feedback: TextView,
        message: String
    ) {
        val originalColor = card.cardBackgroundColor.defaultColor
        card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.answer_retry))
        showRetryFeedback(card, feedback, message)
        card.postDelayed({
            if (card.isEnabled && !isFinishing) card.setCardBackgroundColor(originalColor)
        }, 550L)
    }

    protected fun restoreCompletedAnswerChoice(
        container: GridLayout,
        correctValue: Int,
        feedback: TextView,
        message: String,
        onNextQuestion: () -> Unit
    ) {
        for (index in 0 until container.childCount) {
            val card = container.getChildAt(index) as? MaterialCardView ?: continue
            card.isEnabled = false
            if (card.tag == correctValue) {
                card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.answer_correct))
                card.strokeColor = ContextCompat.getColor(this, R.color.game_star)
                card.findViewById<TextView>(R.id.answerStateIcon).visibility = View.VISIBLE
            }
        }
        restoreCompletedQuestion(feedback, message, onNextQuestion)
    }

    protected fun restoreCompletedQuestion(
        feedback: TextView,
        message: String,
        onNextQuestion: () -> Unit
    ) {
        feedback.text = message
        feedback.accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
        feedback.setTextColor(ContextCompat.getColor(this, R.color.answer_correct))
        feedback.visibility = View.VISIBLE
        if (!isGameOver) {
            feedback.postDelayed({
                if (!isGameOver && !isFinishing) onNextQuestion()
            }, 500L)
        }
    }

    protected fun celebrateQuestion(
        anchor: View,
        feedback: TextView,
        message: String,
        onNextQuestion: () -> Unit
    ) {
        feedback.text = message
        feedback.accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
        feedback.setTextColor(ContextCompat.getColor(this, R.color.answer_correct))
        feedback.visibility = View.VISIBLE
        feedback.alpha = 0f
        feedback.scaleX = 0.82f
        feedback.scaleY = 0.82f
        feedback.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(220L).start()
        anchor.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        anchor.animate().scaleX(1.06f).scaleY(1.06f).setDuration(140L).withEndAction {
            anchor.animate().scaleX(1f).scaleY(1f).setDuration(140L).start()
        }.start()
        onQuestionCompleted()

        val delay = if (gameMode == "FUN") 900L else 650L
        feedback.postDelayed({
            if (!isGameOver && !isFinishing) onNextQuestion()
        }, delay)
    }

    protected fun showRetryFeedback(anchor: View, feedback: TextView, message: String) {
        feedback.text = message
        feedback.accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
        feedback.setTextColor(ContextCompat.getColor(this, R.color.answer_retry))
        feedback.visibility = View.VISIBLE
        anchor.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        anchor.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake))
    }

    protected fun resetFeedback(feedback: TextView) {
        feedback.clearAnimation()
        feedback.visibility = View.INVISIBLE
        feedback.alpha = 1f
        feedback.scaleX = 1f
        feedback.scaleY = 1f
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
        
        pendingResultMessage = message
        pendingRecordText = recordText
        showResultDialog(message, recordText)
    }

    private fun showResultDialog(message: String, recordText: String) {
        if (isFinishing || resultDialog?.isShowing == true) return
        resultDialog = Dialog(this, R.style.FullScreenDialogTheme).apply {
            setContentView(R.layout.dialog_game_result)
            setCancelable(false)
            findViewById<TextView>(R.id.tvResultMessage).text = message
            findViewById<TextView>(R.id.tvResultRecord).text = recordText
            findViewById<Button>(R.id.btnMainMenu).setOnClickListener {
                dismiss()
                finish()
            }
            findViewById<Button>(R.id.btnPlayAgain).setOnClickListener {
                dismiss()
                val replayIntent = Intent(intent)
                finish()
                startActivity(replayIntent)
            }
            show()
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
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
        outState.putBoolean(STATE_GAME_OVER, isGameOver)
        outState.putString(STATE_RESULT_MESSAGE, pendingResultMessage)
        outState.putString(STATE_RESULT_RECORD, pendingRecordText)
        super.onSaveInstanceState(outState)
    }

    companion object {
        private const val STATE_SCORE = "state_score"
        private const val STATE_ROUND = "state_round"
        private const val STATE_ROUND_SCORE = "state_round_score"
        private const val STATE_TIME_REMAINING = "state_time_remaining"
        private const val STATE_HISTORY_SAVED = "state_history_saved"
        private const val STATE_GAME_OVER = "state_game_over"
        private const val STATE_RESULT_MESSAGE = "state_result_message"
        private const val STATE_RESULT_RECORD = "state_result_record"
    }
}
