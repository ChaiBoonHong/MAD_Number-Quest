package com.uccd3223.p1_chai_boon_hong_2206806

import android.content.ClipData
import android.content.ClipDescription
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.uccd3223.p1_chai_boon_hong_2206806.util.ExerciseGeneratorUtil
import com.uccd3223.p1_chai_boon_hong_2206806.util.ExerciseGeneratorUtil.SortDirection
import com.uccd3223.p1_chai_boon_hong_2206806.util.ExerciseGeneratorUtil.SortExercise
import kotlin.math.abs
import kotlin.random.Random

class SequenceActivity : BaseGameActivity() {

    private lateinit var sequenceContainerLayout: LinearLayout
    private lateinit var optionsContainerLayout: LinearLayout
    private lateinit var tvStartDirection: TextView
    private lateinit var tvFeedback: TextView
    private lateinit var currentExercise: SortExercise
    private var placedNumbers = mutableListOf<Int?>()
    private var lastSignature = ""
    private var questionLocked = false
    private val targetViews = mutableListOf<FrameLayout>()
    private val sourceViews = mutableMapOf<Int, FrameLayout>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sequence)
        setupGameModeUI()
        initializeUI()

        if (savedInstanceState?.containsKey(STATE_OPTIONS) == true) {
            restoreQuestion(savedInstanceState)
        } else {
            loadNextQuestion()
        }
    }

    private fun initializeUI() {
        sequenceContainerLayout = findViewById(R.id.sequenceContainerLayout)
        optionsContainerLayout = findViewById(R.id.optionsContainerLayout)
        tvStartDirection = findViewById(R.id.tvStartDirection)
        tvFeedback = findViewById(R.id.tvFeedback)
        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun loadNextQuestion() {
        questionLocked = false
        resetFeedback(tvFeedback)
        do {
            val length = Random.nextInt(3, 7)
            currentExercise = ExerciseGeneratorUtil.generateSortExercise(length)
        } while (questionSignature(currentExercise) == lastSignature)
        lastSignature = questionSignature(currentExercise)
        placedNumbers = MutableList(currentExercise.options.size) { null }
        renderQuestion()
    }

    private fun restoreQuestion(state: Bundle) {
        val options = state.getIntArray(STATE_OPTIONS)?.toList().orEmpty()
        if (options.size !in 3..6 || options.distinct().size != options.size) {
            loadNextQuestion()
            return
        }
        val direction = SortDirection.entries[
            state.getInt(STATE_DIRECTION).coerceIn(0, SortDirection.entries.lastIndex)
        ]
        currentExercise = SortExercise(options, direction)
        val restored = state.getIntArray(STATE_PLACED)
            ?.map { value -> value.takeUnless { it == EMPTY_POSITION } }
            .orEmpty()
        placedNumbers = if (restored.size == options.size) {
            restored.toMutableList()
        } else {
            MutableList(options.size) { null }
        }
        lastSignature = state.getString(STATE_LAST_SIGNATURE).orEmpty()
        questionLocked = state.getBoolean(STATE_QUESTION_LOCKED)
        renderQuestion()
        if (questionLocked) {
            restoreCompletedQuestion(tvFeedback, successMessage(), ::loadNextQuestion)
        }
    }

    private fun questionSignature(exercise: SortExercise): String =
        "${exercise.direction}:${exercise.options.sorted().joinToString(",")}"

    private fun renderQuestion() {
        tvStartDirection.setText(
            if (currentExercise.direction == SortDirection.ASCENDING) {
                R.string.start_with_smallest
            } else {
                R.string.start_with_biggest
            }
        )
        renderSequenceTargets()
        renderAnswerBalloons()
    }

    private fun renderSequenceTargets() {
        targetViews.clear()
        renderBalloonRows(sequenceContainerLayout, currentExercise.options.size) { index, row ->
            val targetView = inflateBalloon(row)
            targetViews += targetView
            val placed = placedNumbers[index]
            if (placed == null) {
                targetView.findViewById<TextView>(R.id.balloonText).text = ""
                targetView.contentDescription = getString(
                    R.string.position_empty,
                    index + 1,
                    currentExercise.options.size
                )
                attachDropTarget(targetView, index)
            } else {
                fillTargetView(targetView, index, placed)
            }
            targetView
        }
    }

    private fun renderAnswerBalloons() {
        sourceViews.clear()
        renderBalloonRows(optionsContainerLayout, currentExercise.options.size) { index, row ->
            val number = currentExercise.options[index]
            val optionView = inflateBalloon(row)
            sourceViews[number] = optionView
            optionView.findViewById<TextView>(R.id.balloonText).text =
                getString(R.string.number_value, number)
            optionView.findViewById<ImageView>(R.id.balloonBg).imageTintList =
                ColorStateList.valueOf(colorForNumber(number))
            optionView.contentDescription = getString(R.string.drag_number, number)
            optionView.visibility = if (number in placedNumbers) View.INVISIBLE else View.VISIBLE
            optionView.setOnClickListener { placeByTap(number, optionView) }
            attachImmediateDrag(optionView, number)
            optionView
        }
    }

    private fun renderBalloonRows(
        parent: LinearLayout,
        count: Int,
        createView: (Int, LinearLayout) -> FrameLayout
    ) {
        parent.removeAllViews()
        val pattern = ExerciseGeneratorUtil.balloonRowPattern(count)
        var itemIndex = 0
        pattern.forEach { rowSize ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            repeat(rowSize) {
                row.addView(createView(itemIndex++, row))
            }
            parent.addView(row)
        }
    }

    private fun inflateBalloon(parent: LinearLayout): BalloonFrameLayout {
        val size = resources.getDimensionPixelSize(R.dimen.balloon_size)
        val gap = resources.getDimensionPixelSize(R.dimen.balloon_gap)
        return (layoutInflater.inflate(
            R.layout.item_balloon,
            parent,
            false
        ) as BalloonFrameLayout).apply {
            layoutParams = LinearLayout.LayoutParams(size, size).apply {
                setMargins(gap, gap, gap, gap)
            }
        }
    }

    private fun attachDropTarget(targetView: FrameLayout, targetIndex: Int) {
        targetView.setOnDragListener { view, event ->
            val source = event.localState as? View
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED ->
                    event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) && source != null
                DragEvent.ACTION_DRAG_ENTERED -> {
                    view.alpha = 0.7f
                    true
                }
                DragEvent.ACTION_DRAG_EXITED -> {
                    view.alpha = 1f
                    true
                }
                DragEvent.ACTION_DROP -> {
                    view.alpha = 1f
                    val number = event.clipData.getItemAt(0).text.toString().toIntOrNull()
                    if (number != null && source != null &&
                        placedNumbers[targetIndex] == null &&
                        number == currentExercise.orderedNumbers[targetIndex]
                    ) {
                        placeNumber(targetIndex, number, source)
                    } else {
                        showSequenceRetry(view)
                    }
                    true
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    view.alpha = 1f
                    source?.alpha = 1f
                    true
                }
                else -> false
            }
        }
    }

    private fun attachImmediateDrag(optionView: BalloonFrameLayout, number: Int) {
        val touchSlop = ViewConfiguration.get(this).scaledTouchSlop
        var downX = 0f
        var downY = 0f
        var dragStarted = false

        optionView.setOnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.x
                    downY = event.y
                    dragStarted = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (!dragStarted &&
                        (abs(event.x - downX) > touchSlop || abs(event.y - downY) > touchSlop)
                    ) {
                        val dragData = ClipData(
                            number.toString(),
                            arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                            ClipData.Item(number.toString())
                        )
                        dragStarted = view.startDragAndDrop(
                            dragData,
                            View.DragShadowBuilder(view),
                            view,
                            0
                        )
                        if (dragStarted) {
                            view.alpha = 0.35f
                            view.parent?.requestDisallowInterceptTouchEvent(true)
                        }
                    }
                    dragStarted
                }
                MotionEvent.ACTION_UP -> {
                    view.parent?.requestDisallowInterceptTouchEvent(false)
                    if (!dragStarted) view.performClick()
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    view.parent?.requestDisallowInterceptTouchEvent(false)
                    view.alpha = 1f
                    true
                }
                else -> dragStarted
            }
        }
    }

    private fun placeByTap(number: Int, sourceView: View) {
        if (questionLocked || isGameOver || sourceView.visibility != View.VISIBLE) return
        val nextIndex = placedNumbers.indexOfFirst { it == null }
        if (nextIndex >= 0 && number == currentExercise.orderedNumbers[nextIndex]) {
            placeNumber(nextIndex, number, sourceView)
        } else {
            showSequenceRetry(sourceView)
        }
    }

    private fun placeNumber(targetIndex: Int, number: Int, sourceView: View) {
        if (questionLocked || placedNumbers[targetIndex] != null || sourceView.visibility != View.VISIBLE) return
        placedNumbers[targetIndex] = number
        fillTargetView(targetViews[targetIndex], targetIndex, number)
        sourceView.visibility = View.INVISIBLE

        if (placedNumbers.all { it != null }) {
            questionLocked = true
            celebrateQuestion(targetViews[targetIndex], tvFeedback, successMessage(), ::loadNextQuestion)
        }
    }

    private fun successMessage(): String = getString(
        if (currentExercise.direction == SortDirection.ASCENDING) {
            R.string.sequence_success_small
        } else {
            R.string.sequence_success_big
        }
    )

    private fun fillTargetView(targetView: FrameLayout, index: Int, number: Int) {
        targetView.setOnDragListener(null)
        targetView.findViewById<TextView>(R.id.balloonText).text =
            getString(R.string.number_value, number)
        targetView.findViewById<ImageView>(R.id.balloonBg).imageTintList =
            ColorStateList.valueOf(colorForNumber(number))
        targetView.contentDescription = getString(
            R.string.position_filled,
            index + 1,
            currentExercise.options.size,
            number
        )
    }

    private fun showSequenceRetry(anchor: View) {
        val message = getString(
            if (currentExercise.direction == SortDirection.ASCENDING) {
                R.string.sequence_retry_small
            } else {
                R.string.sequence_retry_big
            }
        )
        showRetryFeedback(anchor, tvFeedback, message)
    }

    private fun colorForNumber(number: Int): Int {
        val colors = listOf(
            R.color.game_blue,
            R.color.game_orange,
            R.color.game_purple,
            R.color.game_green,
            R.color.game_balloon_pink,
            R.color.game_star
        )
        val index = currentExercise.options.indexOf(number).coerceAtLeast(0)
        return ContextCompat.getColor(this, colors[index % colors.size])
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (::currentExercise.isInitialized) {
            outState.putIntArray(STATE_OPTIONS, currentExercise.options.toIntArray())
            outState.putInt(STATE_DIRECTION, currentExercise.direction.ordinal)
            outState.putIntArray(
                STATE_PLACED,
                placedNumbers.map { it ?: EMPTY_POSITION }.toIntArray()
            )
        }
        outState.putString(STATE_LAST_SIGNATURE, lastSignature)
        outState.putBoolean(STATE_QUESTION_LOCKED, questionLocked)
        super.onSaveInstanceState(outState)
    }

    companion object {
        private const val EMPTY_POSITION = -1
        private const val STATE_OPTIONS = "sequence_options"
        private const val STATE_DIRECTION = "sequence_direction"
        private const val STATE_PLACED = "sequence_placed"
        private const val STATE_LAST_SIGNATURE = "sequence_last_signature"
        private const val STATE_QUESTION_LOCKED = "sequence_question_locked"
    }
}
