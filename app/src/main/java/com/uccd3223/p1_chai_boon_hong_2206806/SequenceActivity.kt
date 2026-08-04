package com.uccd3223.p1_chai_boon_hong_2206806

import android.content.ClipData
import android.content.ClipDescription
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.DragEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.toColorInt
import com.google.android.flexbox.FlexboxLayout
import com.uccd3223.p1_chai_boon_hong_2206806.util.ExerciseGeneratorUtil

class SequenceActivity : BaseGameActivity() {

    private lateinit var sequenceContainerLayout: FlexboxLayout
    private lateinit var optionsContainerLayout: FlexboxLayout
    private lateinit var tvFeedback: TextView
    private lateinit var targetSortedSequence: List<Int>
    private lateinit var shuffledOptions: List<Int>
    private var completedCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sequence)
        setupGameModeUI()
        initializeUI()
        loadNextQuestion()
    }

    private fun initializeUI() {
        sequenceContainerLayout = findViewById(R.id.sequenceContainerLayout)
        optionsContainerLayout = findViewById(R.id.optionsContainerLayout)
        tvFeedback = findViewById(R.id.tvFeedback)
        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun loadNextQuestion() {
        sequenceContainerLayout.removeAllViews()
        optionsContainerLayout.removeAllViews()
        completedCount = 0
        shuffledOptions = ExerciseGeneratorUtil.generateSortSequence(4)
        targetSortedSequence = shuffledOptions.sorted()
        renderSequenceUI()
        renderOptionsUI()
    }

    private fun renderSequenceUI() {
        val density = resources.displayMetrics.density
        val size = (92 * density).toInt()
        val margin = (3 * density).toInt()

        targetSortedSequence.indices.forEach { index ->
            val targetView = layoutInflater.inflate(
                R.layout.item_balloon,
                sequenceContainerLayout,
                false
            ) as FrameLayout
            targetView.layoutParams = FlexboxLayout.LayoutParams(size, size).apply {
                setMargins(margin, margin, margin, margin)
            }
            targetView.findViewById<TextView>(R.id.balloonText).text = ""
            targetView.contentDescription = getString(R.string.answer_number, index + 1)

            targetView.setOnDragListener { view, event ->
                when (event.action) {
                    DragEvent.ACTION_DRAG_STARTED ->
                        event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) &&
                            event.localState is View
                    DragEvent.ACTION_DRAG_ENTERED -> {
                        view.alpha = 0.72f
                        true
                    }
                    DragEvent.ACTION_DRAG_EXITED, DragEvent.ACTION_DRAG_ENDED -> {
                        view.alpha = 1f
                        true
                    }
                    DragEvent.ACTION_DROP -> {
                        view.alpha = 1f
                        val number = event.clipData.getItemAt(0).text.toString().toIntOrNull()
                        val source = event.localState as? View
                        if (number == targetSortedSequence[index] && source != null) {
                            placeNumber(index, number, source)
                        } else {
                            showIncorrect(view)
                        }
                        true
                    }
                    else -> false
                }
            }
            sequenceContainerLayout.addView(targetView)
        }
    }

    private fun renderOptionsUI() {
        val density = resources.displayMetrics.density
        val size = (92 * density).toInt()
        val margin = (3 * density).toInt()
        val colors = listOf("#4F8FF7", "#FF9F43", "#8B6BE8", "#35B77A")

        shuffledOptions.forEachIndexed { index, option ->
            val optionView = layoutInflater.inflate(
                R.layout.item_balloon,
                optionsContainerLayout,
                false
            ) as FrameLayout
            optionView.layoutParams = FlexboxLayout.LayoutParams(size, size).apply {
                setMargins(margin, margin, margin, margin)
            }
            optionView.findViewById<TextView>(R.id.balloonText).text =
                getString(R.string.number_value, option)
            optionView.findViewById<ImageView>(R.id.balloonBg).imageTintList =
                ColorStateList.valueOf(colors[index % colors.size].toColorInt())
            optionView.contentDescription = getString(R.string.drag_number, option)

            optionView.setOnClickListener {
                val nextEmptyIndex = targetSortedSequence.indices.firstOrNull { targetIndex ->
                    val target = sequenceContainerLayout.getChildAt(targetIndex)
                    target.findViewById<TextView>(R.id.balloonText).text.isEmpty()
                }
                if (nextEmptyIndex != null && option == targetSortedSequence[nextEmptyIndex]) {
                    placeNumber(nextEmptyIndex, option, optionView)
                } else {
                    showIncorrect(optionView)
                }
            }
            optionView.setOnLongClickListener { view ->
                val dragData = ClipData(
                    option.toString(),
                    arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                    ClipData.Item(option.toString())
                )
                view.startDragAndDrop(dragData, View.DragShadowBuilder(view), view, 0)
                true
            }
            optionsContainerLayout.addView(optionView)
        }
    }

    private fun placeNumber(targetIndex: Int, number: Int, sourceView: View) {
        val targetView = sequenceContainerLayout.getChildAt(targetIndex)
        val targetText = targetView.findViewById<TextView>(R.id.balloonText)
        if (targetText.text.isNotEmpty() || sourceView.visibility != View.VISIBLE) return

        targetText.text = getString(R.string.number_value, number)
        targetView.findViewById<ImageView>(R.id.balloonBg).imageTintList =
            sourceView.findViewById<ImageView>(R.id.balloonBg).imageTintList
        targetView.setOnDragListener(null)
        targetView.contentDescription = getString(R.string.answer_number, number)
        sourceView.visibility = View.INVISIBLE
        completedCount++

        if (completedCount == targetSortedSequence.size) {
            tvFeedback.setText(R.string.great_job)
            tvFeedback.setTextColor("#35B77A".toColorInt())
            tvFeedback.visibility = View.VISIBLE
            tvFeedback.postDelayed({
                if (!isGameOver) {
                    onQuestionCompleted()
                    loadNextQuestion()
                    tvFeedback.visibility = View.INVISIBLE
                }
            }, 650)
        }
    }

    private fun showIncorrect(view: View) {
        tvFeedback.setText(R.string.try_again)
        tvFeedback.setTextColor("#EB5757".toColorInt())
        tvFeedback.visibility = View.VISIBLE
        view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake))
    }
}
