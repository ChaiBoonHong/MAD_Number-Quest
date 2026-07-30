package com.uccd3223.p1_chai_boon_hong_2206806

import android.content.ClipData
import android.content.ClipDescription
import android.graphics.Color
import androidx.core.graphics.toColorInt
import android.os.Bundle
import android.view.DragEvent
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.card.MaterialCardView
import com.uccd3223.p1_chai_boon_hong_2206806.util.ExerciseGeneratorUtil

class SequenceActivity : BaseGameActivity() {

    private lateinit var sequenceContainerLayout: com.google.android.flexbox.FlexboxLayout
    private lateinit var optionsContainerLayout: com.google.android.flexbox.FlexboxLayout
    private lateinit var tvFeedback: android.widget.TextView
    private lateinit var targetSortedSequence: List<Int>
    private lateinit var shuffledOptions: List<Int>
    private var completedCount: Int = 0

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
        
        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
        }
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
        val dpScale = resources.displayMetrics.density
        val margin = (8 * dpScale).toInt()

        for (i in targetSortedSequence.indices) {
            val itemView = layoutInflater.inflate(R.layout.item_balloon, sequenceContainerLayout, false) as android.widget.FrameLayout
            val tv = itemView.findViewById<TextView>(R.id.balloonText)
            val bg = itemView.findViewById<android.widget.ImageView>(R.id.balloonBg)
            
            val size = (110 * dpScale).toInt()
            val layoutParams = com.google.android.flexbox.FlexboxLayout.LayoutParams(
                size,
                size
            ).apply {
                setMargins(margin/2, margin, margin/2, margin)
            }
            itemView.layoutParams = layoutParams

            tv.text = ""
            
            // Set up Drag Listener
            itemView.setOnDragListener { v, event ->
                when (event.action) {
                    DragEvent.ACTION_DRAG_STARTED -> {
                        event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
                    }
                    DragEvent.ACTION_DRAG_ENTERED -> {
                        v.alpha = 0.7f
                        true
                    }
                    DragEvent.ACTION_DRAG_EXITED -> {
                        v.alpha = 1.0f
                        true
                    }
                    DragEvent.ACTION_DROP -> {
                        v.alpha = 1.0f
                        val item = event.clipData.getItemAt(0)
                        val draggedNumber = item.text.toString().toIntOrNull()
                        
                        if (draggedNumber != null && draggedNumber == targetSortedSequence[i]) {
                            // Correct placement
                            tv.text = draggedNumber.toString()
                            tv.setTextColor(Color.BLACK)
                            
                            val sourceView = event.localState as View
                            val sourceBg = sourceView.findViewById<android.widget.ImageView>(R.id.balloonBg)
                            bg.imageTintList = sourceBg.imageTintList
                            
                            sourceView.visibility = View.INVISIBLE
                            
                            v.setOnDragListener(null) // Disable further drops on this carriage
                            completedCount++
                            
                            if (completedCount == targetSortedSequence.size) {
                                tvFeedback.text = "Great Job!"
                                tvFeedback.setTextColor("#66BB6A".toColorInt())
                                tvFeedback.visibility = View.VISIBLE
                                
                                tvFeedback.postDelayed({
                                    if (tvFeedback.text == "Great Job!") {
                                        tvFeedback.visibility = View.INVISIBLE
                                    }
                                }, 1000)
                                
                                onQuestionCompleted()
                                loadNextQuestion()
                            }
                        } else {
                            // Incorrect placement
                            tvFeedback.text = "Oops, try again!"
                            tvFeedback.setTextColor("#EF5350".toColorInt())
                            tvFeedback.visibility = View.VISIBLE
                            
                            val shake = AnimationUtils.loadAnimation(this@SequenceActivity, R.anim.shake)
                            v.startAnimation(shake)
                        }
                        true
                    }
                    DragEvent.ACTION_DRAG_ENDED -> {
                        v.alpha = 1.0f
                        true
                    }
                    else -> false
                }
            }
            
            sequenceContainerLayout.addView(itemView)
        }
    }

    private fun renderOptionsUI() {
        val dpScale = resources.displayMetrics.density
        val margin = (8 * dpScale).toInt()
        val colors = listOf("#1368CE", "#D89E00", "#9C27B0", "#E65100") // Blue, Yellow/Orange, Purple, Dark Orange

        for ((index, option) in shuffledOptions.withIndex()) {
            val optionBtn = layoutInflater.inflate(R.layout.item_balloon, optionsContainerLayout, false) as android.widget.FrameLayout
            
            val tv = optionBtn.findViewById<TextView>(R.id.balloonText)
            tv.text = option.toString()
            tv.setTextColor(Color.BLACK)
            
            val bg = optionBtn.findViewById<android.widget.ImageView>(R.id.balloonBg)
            bg.imageTintList = android.content.res.ColorStateList.valueOf(Color.parseColor(colors[index % colors.size]))
            
            val size = (110 * dpScale).toInt()
            val layoutParams = com.google.android.flexbox.FlexboxLayout.LayoutParams(
                size,
                size
            ).apply {
                setMargins(margin, margin, margin, margin)
            }
            optionBtn.layoutParams = layoutParams

            // Set up Touch Listener for direct dragging
            @android.annotation.SuppressLint("ClickableViewAccessibility")
            optionBtn.setOnTouchListener { view, motionEvent ->
                if (motionEvent.action == android.view.MotionEvent.ACTION_DOWN) {
                    val item = ClipData.Item(option.toString())
                    val dragData = ClipData(
                        option.toString(),
                        arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                        item
                    )
                    
                    val myShadow = View.DragShadowBuilder(view)
                    
                    view.startDragAndDrop(
                        dragData,
                        myShadow,
                        view, // pass view as local state
                        0
                    )
                    true
                } else {
                    false
                }
            }
            
            optionsContainerLayout.addView(optionBtn)
        }
    }
}
