package com.uccd3223.p1_chai_boon_hong_2206806

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.uccd3223.p1_chai_boon_hong_2206806.util.ExerciseGeneratorUtil
import kotlin.random.Random

class AssociationActivity : BaseGameActivity() {

    private enum class CountTheme(
        val drawableRes: Int,
        val promptRes: Int,
        val objectNameRes: Int,
        val tintRes: Int? = null
    ) {
        APPLE(R.drawable.game_apple, R.string.count_apples_prompt, R.string.count_object_apple),
        STAR(R.drawable.ic_count_star, R.string.count_stars_prompt, R.string.count_object_star),
        BALLOON(
            R.drawable.ic_balloon,
            R.string.count_balloons_prompt,
            R.string.count_object_balloon,
            R.color.game_balloon_pink
        )
    }

    private lateinit var objectsGridLayout: GridLayout
    private lateinit var optionsContainerLayout: GridLayout
    private lateinit var tvInstruction: TextView
    private lateinit var tvFeedback: TextView
    private var targetCount = 0
    private var lastTargetCount = -1
    private var currentTheme = CountTheme.APPLE
    private var lastThemeOrdinal = -1
    private var currentOptions = emptyList<Int>()
    private var questionLocked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_association)
        setupGameModeUI()
        initializeUI()

        if (savedInstanceState?.containsKey(STATE_TARGET_COUNT) == true) {
            restoreQuestion(savedInstanceState)
        } else {
            loadNextQuestion()
        }
    }

    private fun initializeUI() {
        objectsGridLayout = findViewById(R.id.objectsGridLayout)
        optionsContainerLayout = findViewById(R.id.optionsContainerLayout)
        tvInstruction = findViewById(R.id.tvInstruction)
        tvFeedback = findViewById(R.id.tvFeedback)
        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun loadNextQuestion() {
        questionLocked = false
        resetFeedback(tvFeedback)
        objectsGridLayout.removeAllViews()

        do {
            targetCount = ExerciseGeneratorUtil.generateObjectCount(9)
            currentTheme = CountTheme.entries[Random.nextInt(CountTheme.entries.size)]
        } while (targetCount == lastTargetCount && currentTheme.ordinal == lastThemeOrdinal)

        lastTargetCount = targetCount
        lastThemeOrdinal = currentTheme.ordinal
        currentOptions = ExerciseGeneratorUtil
            .generateRecognitionOptions(targetCount, 9, 4)
            .options
        renderQuestion()
    }

    private fun restoreQuestion(state: Bundle) {
        targetCount = state.getInt(STATE_TARGET_COUNT)
        lastTargetCount = state.getInt(STATE_LAST_TARGET_COUNT, targetCount)
        currentTheme = CountTheme.entries[state.getInt(STATE_THEME).coerceIn(0, CountTheme.entries.lastIndex)]
        lastThemeOrdinal = state.getInt(STATE_LAST_THEME, currentTheme.ordinal)
        questionLocked = state.getBoolean(STATE_QUESTION_LOCKED)
        currentOptions = state.getIntegerArrayList(STATE_OPTIONS)?.toList().orEmpty()
        if (currentOptions.size != 4) {
            currentOptions = ExerciseGeneratorUtil
                .generateRecognitionOptions(targetCount, 9, 4)
                .options
        }
        renderQuestion()
        if (questionLocked) {
            restoreCompletedAnswerChoice(
                optionsContainerLayout,
                targetCount,
                tvFeedback,
                getString(R.string.count_success, targetCount),
                ::loadNextQuestion
            )
        }
    }

    private fun renderQuestion() {
        tvInstruction.setText(currentTheme.promptRes)
        renderObjectsUI()
        renderOptionsUI()
    }

    private fun renderObjectsUI() {
        objectsGridLayout.removeAllViews()
        val density = resources.displayMetrics.density
        val size = (60 * density).toInt()
        val margin = (7 * density).toInt()
        val activeIndices = getActiveIndices(targetCount)

        repeat(9) { index ->
            val isActive = index in activeIndices
            val itemView = ImageView(this).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = size
                    height = size
                    setMargins(margin, margin, margin, margin)
                }
                scaleType = ImageView.ScaleType.FIT_CENTER
                if (isActive) {
                    setImageResource(currentTheme.drawableRes)
                    currentTheme.tintRes?.let {
                        imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this@AssociationActivity, it))
                    }
                    contentDescription = getString(currentTheme.objectNameRes)
                    importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
                } else {
                    visibility = View.INVISIBLE
                    importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
                }
            }
            objectsGridLayout.addView(itemView)
        }
    }

    private fun getActiveIndices(count: Int): List<Int> = when (count) {
        1 -> listOf(4)
        2 -> listOf(3, 5)
        3 -> listOf(3, 4, 5)
        4 -> listOf(0, 2, 6, 8)
        5 -> listOf(0, 2, 4, 6, 8)
        6 -> listOf(0, 2, 3, 5, 6, 8)
        7 -> listOf(0, 2, 3, 4, 5, 6, 8)
        8 -> listOf(0, 1, 2, 3, 5, 6, 7, 8)
        9 -> (0..8).toList()
        else -> emptyList()
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
        if (selectedOption == targetCount) {
            questionLocked = true
            completeAnswerChoice(
                optionsContainerLayout,
                card,
                tvFeedback,
                getString(R.string.count_success, targetCount),
                ::loadNextQuestion
            )
        } else {
            retryAnswerChoice(card, tvFeedback, getString(R.string.count_retry))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(STATE_TARGET_COUNT, targetCount)
        outState.putInt(STATE_LAST_TARGET_COUNT, lastTargetCount)
        outState.putInt(STATE_THEME, currentTheme.ordinal)
        outState.putInt(STATE_LAST_THEME, lastThemeOrdinal)
        outState.putIntegerArrayList(STATE_OPTIONS, ArrayList(currentOptions))
        outState.putBoolean(STATE_QUESTION_LOCKED, questionLocked)
        super.onSaveInstanceState(outState)
    }

    companion object {
        private const val STATE_TARGET_COUNT = "association_target_count"
        private const val STATE_LAST_TARGET_COUNT = "association_last_target_count"
        private const val STATE_THEME = "association_theme"
        private const val STATE_LAST_THEME = "association_last_theme"
        private const val STATE_OPTIONS = "association_options"
        private const val STATE_QUESTION_LOCKED = "association_question_locked"
    }
}
