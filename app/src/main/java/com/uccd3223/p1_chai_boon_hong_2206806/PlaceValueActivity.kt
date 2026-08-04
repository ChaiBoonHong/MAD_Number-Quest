package com.uccd3223.p1_chai_boon_hong_2206806

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.card.MaterialCardView
import com.uccd3223.p1_chai_boon_hong_2206806.util.ExerciseGeneratorUtil
import java.util.Locale

class PlaceValueActivity : BaseGameActivity() {

    private lateinit var tensContainerLayout: FlexboxLayout
    private lateinit var onesContainerLayout: FlexboxLayout
    private lateinit var optionsContainerLayout: GridLayout
    private lateinit var tvTensCount: TextView
    private lateinit var tvOnesCount: TextView
    private lateinit var tvFeedback: TextView
    private lateinit var currentData: ExerciseGeneratorUtil.PlaceValueData
    private var currentOptions = emptyList<Int>()
    private var lastTotal = -1
    private var questionLocked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_value)
        setupGameModeUI()
        initializeUI()

        if (savedInstanceState?.containsKey(STATE_TENS) == true) {
            restoreQuestion(savedInstanceState)
        } else {
            loadNextQuestion()
        }
    }

    private fun initializeUI() {
        tensContainerLayout = findViewById(R.id.tensContainerLayout)
        onesContainerLayout = findViewById(R.id.onesContainerLayout)
        optionsContainerLayout = findViewById(R.id.optionsContainerLayout)
        tvTensCount = findViewById(R.id.tvTensCount)
        tvOnesCount = findViewById(R.id.tvOnesCount)
        tvFeedback = findViewById(R.id.tvFeedback)
        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun loadNextQuestion() {
        questionLocked = false
        resetFeedback(tvFeedback)
        do {
            currentData = ExerciseGeneratorUtil.generatePlaceValue(9)
        } while (currentData.total == lastTotal)
        lastTotal = currentData.total
        currentOptions = ExerciseGeneratorUtil
            .generateRecognitionOptions(currentData.total, 99, 4)
            .options
        renderQuestion()
    }

    private fun restoreQuestion(state: Bundle) {
        currentData = ExerciseGeneratorUtil.PlaceValueData(
            tens = state.getInt(STATE_TENS),
            ones = state.getInt(STATE_ONES)
        )
        lastTotal = state.getInt(STATE_LAST_TOTAL, currentData.total)
        questionLocked = state.getBoolean(STATE_QUESTION_LOCKED)
        currentOptions = state.getIntegerArrayList(STATE_OPTIONS)?.toList().orEmpty()
        if (currentOptions.size != 4) {
            currentOptions = ExerciseGeneratorUtil
                .generateRecognitionOptions(currentData.total, 99, 4)
                .options
        }
        renderQuestion()
        if (questionLocked) {
            restoreCompletedAnswerChoice(
                optionsContainerLayout,
                currentData.total,
                tvFeedback,
                successMessage(),
                ::loadNextQuestion
            )
        }
    }

    private fun renderQuestion() {
        renderVisualUI()
        renderOptionsUI()
    }

    private fun renderVisualUI() {
        tensContainerLayout.removeAllViews()
        onesContainerLayout.removeAllViews()
        tvTensCount.text = resources.getQuantityString(
            R.plurals.tens_count,
            currentData.tens,
            currentData.tens
        )
        tvOnesCount.text = resources.getQuantityString(
            R.plurals.ones_count,
            currentData.ones,
            currentData.ones
        )

        val density = resources.displayMetrics.density
        val margin = (3 * density).toInt()
        val tensWidth = (18 * density).toInt()
        val tensHeight = (118 * density).toInt()
        val oneSize = (28 * density).toInt()

        repeat(currentData.tens) {
            val view = ImageView(this).apply {
                setImageResource(R.drawable.ic_tens_block)
                scaleType = ImageView.ScaleType.FIT_XY
                contentDescription = getString(R.string.ten_block_description)
                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
                layoutParams = FlexboxLayout.LayoutParams(tensWidth, tensHeight).apply {
                    setMargins(margin, margin, margin, margin)
                }
            }
            tensContainerLayout.addView(view)
        }

        repeat(currentData.ones) {
            val view = ImageView(this).apply {
                setImageResource(R.drawable.ic_ones_block)
                scaleType = ImageView.ScaleType.FIT_XY
                contentDescription = getString(R.string.one_block_description)
                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
                layoutParams = FlexboxLayout.LayoutParams(oneSize, oneSize).apply {
                    setMargins(margin, margin, margin, margin)
                }
            }
            onesContainerLayout.addView(view)
        }
    }

    private fun renderOptionsUI() {
        val choices = currentOptions.map { option ->
            AnswerChoice(
                value = option,
                label = getString(R.string.number_value, option),
                contentDescription = getString(R.string.answer_number, option)
            )
        }
        renderAnswerChoices(optionsContainerLayout, choices) { choice, card ->
            checkAnswer(choice.value, card)
        }
    }

    private fun checkAnswer(selectedOption: Int, card: MaterialCardView) {
        if (questionLocked || isGameOver) return
        if (selectedOption == currentData.total) {
            questionLocked = true
            completeAnswerChoice(
                optionsContainerLayout,
                card,
                tvFeedback,
                successMessage(),
                ::loadNextQuestion
            )
        } else {
            retryAnswerChoice(card, tvFeedback, getString(R.string.place_value_retry))
        }
    }

    private fun successMessage(): String {
        val placeParts = getString(
            R.string.number_words_pair,
            resources.getQuantityString(R.plurals.tens_count, currentData.tens, currentData.tens),
            resources.getQuantityString(R.plurals.ones_count, currentData.ones, currentData.ones)
        )
        val numberWords = ExerciseGeneratorUtil
            .numberToWords(currentData.total)
            .lowercase(Locale.getDefault())
        return getString(R.string.place_value_success, placeParts, numberWords)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (::currentData.isInitialized) {
            outState.putInt(STATE_TENS, currentData.tens)
            outState.putInt(STATE_ONES, currentData.ones)
        }
        outState.putInt(STATE_LAST_TOTAL, lastTotal)
        outState.putIntegerArrayList(STATE_OPTIONS, ArrayList(currentOptions))
        outState.putBoolean(STATE_QUESTION_LOCKED, questionLocked)
        super.onSaveInstanceState(outState)
    }

    companion object {
        private const val STATE_TENS = "place_value_tens"
        private const val STATE_ONES = "place_value_ones"
        private const val STATE_LAST_TOTAL = "place_value_last_total"
        private const val STATE_OPTIONS = "place_value_options"
        private const val STATE_QUESTION_LOCKED = "place_value_question_locked"
    }
}
