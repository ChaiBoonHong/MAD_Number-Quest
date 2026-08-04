package com.uccd3223.p1_chai_boon_hong_2206806

import android.os.Bundle
import android.util.TypedValue
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import com.uccd3223.p1_chai_boon_hong_2206806.util.ExerciseGeneratorUtil
import com.uccd3223.p1_chai_boon_hong_2206806.util.ExerciseGeneratorUtil.RecognitionPromptMode

class RecognitionActivity : BaseGameActivity() {

    private lateinit var tvTarget: TextView
    private lateinit var tvTargetPrompt: TextView
    private lateinit var optionsContainerLayout: GridLayout
    private lateinit var tvFeedback: TextView
    private var targetNumber = 0
    private var currentOptions = emptyList<Int>()
    private var promptMode = RecognitionPromptMode.WORD_TO_NUMBER
    private var lastTarget = -1
    private var lastModeOrdinal = -1
    private var questionLocked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recognition)
        setupGameModeUI()
        initializeUI()

        if (savedInstanceState?.containsKey(STATE_TARGET) == true) {
            restoreQuestion(savedInstanceState)
        } else {
            loadNextQuestion()
        }
    }

    private fun initializeUI() {
        tvTarget = findViewById(R.id.tvTarget)
        tvTargetPrompt = findViewById(R.id.tvTargetPrompt)
        optionsContainerLayout = findViewById(R.id.optionsContainerLayout)
        tvFeedback = findViewById(R.id.tvFeedback)
        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun loadNextQuestion() {
        questionLocked = false
        resetFeedback(tvFeedback)
        var exercise: ExerciseGeneratorUtil.RecognitionExercise
        do {
            exercise = ExerciseGeneratorUtil.generateRecognitionExercise()
        } while (exercise.targetNumber == lastTarget && exercise.promptMode.ordinal == lastModeOrdinal)

        targetNumber = exercise.targetNumber
        promptMode = exercise.promptMode

        lastTarget = targetNumber
        lastModeOrdinal = promptMode.ordinal
        currentOptions = exercise.options
        renderQuestion()
    }

    private fun restoreQuestion(state: Bundle) {
        targetNumber = state.getInt(STATE_TARGET)
        lastTarget = state.getInt(STATE_LAST_TARGET, targetNumber)
        promptMode = RecognitionPromptMode.entries[
            state.getInt(STATE_PROMPT_MODE).coerceIn(0, RecognitionPromptMode.entries.lastIndex)
        ]
        lastModeOrdinal = state.getInt(STATE_LAST_MODE, promptMode.ordinal)
        questionLocked = state.getBoolean(STATE_QUESTION_LOCKED)
        currentOptions = state.getIntegerArrayList(STATE_OPTIONS)?.toList().orEmpty()
        if (currentOptions.size != 4) {
            currentOptions = ExerciseGeneratorUtil
                .generateRecognitionOptions(targetNumber, 99, 4)
                .options
        }
        renderQuestion()
        if (questionLocked) {
            restoreCompletedAnswerChoice(
                optionsContainerLayout,
                targetNumber,
                tvFeedback,
                successMessage(),
                ::loadNextQuestion
            )
        }
    }

    private fun renderQuestion() {
        val numberWords = ExerciseGeneratorUtil.numberToWords(targetNumber)
        val choices = when (promptMode) {
            RecognitionPromptMode.WORD_TO_NUMBER -> {
                tvTargetPrompt.setText(R.string.find_the_number)
                tvTarget.text = numberWords
                tvTarget.setTextSize(TypedValue.COMPLEX_UNIT_SP, 38f)
                currentOptions.map { option ->
                    AnswerChoice(
                        option,
                        getString(R.string.number_value, option),
                        getString(R.string.answer_number, option)
                    )
                }
            }
            RecognitionPromptMode.NUMBER_TO_WORD -> {
                tvTargetPrompt.setText(R.string.find_the_word)
                tvTarget.text = getString(R.string.number_value, targetNumber)
                tvTarget.setTextSize(TypedValue.COMPLEX_UNIT_SP, 56f)
                currentOptions.map { option ->
                    val words = ExerciseGeneratorUtil.numberToWords(option)
                    AnswerChoice(
                        option,
                        words,
                        getString(R.string.answer_text, words)
                    )
                }
            }
        }
        tvTarget.contentDescription = tvTarget.text
        renderAnswerChoices(optionsContainerLayout, choices) { choice, card ->
            checkAnswer(choice.value, card)
        }
    }

    private fun checkAnswer(selectedOption: Int, card: MaterialCardView) {
        if (questionLocked || isGameOver) return
        if (selectedOption == targetNumber) {
            questionLocked = true
            completeAnswerChoice(
                optionsContainerLayout,
                card,
                tvFeedback,
                successMessage(),
                ::loadNextQuestion
            )
        } else {
            val retryRes = if (promptMode == RecognitionPromptMode.WORD_TO_NUMBER) {
                R.string.recognition_retry
            } else {
                R.string.recognition_retry_number
            }
            retryAnswerChoice(card, tvFeedback, getString(retryRes))
        }
    }

    private fun successMessage(): String = getString(
        R.string.recognition_success,
        ExerciseGeneratorUtil.numberToWords(targetNumber),
        getString(R.string.number_value, targetNumber)
    )

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(STATE_TARGET, targetNumber)
        outState.putInt(STATE_LAST_TARGET, lastTarget)
        outState.putInt(STATE_PROMPT_MODE, promptMode.ordinal)
        outState.putInt(STATE_LAST_MODE, lastModeOrdinal)
        outState.putIntegerArrayList(STATE_OPTIONS, ArrayList(currentOptions))
        outState.putBoolean(STATE_QUESTION_LOCKED, questionLocked)
        super.onSaveInstanceState(outState)
    }

    companion object {
        private const val STATE_TARGET = "recognition_target"
        private const val STATE_LAST_TARGET = "recognition_last_target"
        private const val STATE_PROMPT_MODE = "recognition_prompt_mode"
        private const val STATE_LAST_MODE = "recognition_last_mode"
        private const val STATE_OPTIONS = "recognition_options"
        private const val STATE_QUESTION_LOCKED = "recognition_question_locked"
    }
}
