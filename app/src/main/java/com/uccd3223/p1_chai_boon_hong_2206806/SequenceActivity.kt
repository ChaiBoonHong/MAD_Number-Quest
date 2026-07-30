package com.uccd3223.p1_chai_boon_hong_2206806

import android.graphics.Color
import androidx.core.graphics.toColorInt
import android.os.Bundle
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
import kotlin.random.Random

class SequenceActivity : AppCompatActivity() {

    private lateinit var sequenceContainerLayout: FlexboxLayout
    private lateinit var optionsContainerLayout: GridLayout
    private lateinit var currentQuestion: ExerciseGeneratorUtil.SequenceData
    private var lastStart: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sequence)

        initializeUI()
        loadNextQuestion()
    }

    private fun initializeUI() {
        sequenceContainerLayout = findViewById(R.id.sequenceContainerLayout)
        optionsContainerLayout = findViewById(R.id.optionsContainerLayout)
        
        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    private fun loadNextQuestion() {
        sequenceContainerLayout.removeAllViews()
        optionsContainerLayout.removeAllViews()

        var start: Int
        do {
            start = Random.nextInt(1, 25)
        } while (start == lastStart)
        lastStart = start
        
        val step = Random.nextInt(1, 5)
        currentQuestion = ExerciseGeneratorUtil.generateSequence(start, step, 5)

        renderSequenceUI()
        renderOptionsUI()
    }

    private fun renderSequenceUI() {
        val dpScale = resources.displayMetrics.density
        val margin = (8 * dpScale).toInt()

        for (i in currentQuestion.sequence.indices) {
            val itemView = layoutInflater.inflate(R.layout.item_train_carriage, sequenceContainerLayout, false) as android.widget.FrameLayout
            val tv = itemView.findViewById<TextView>(R.id.trainText)
            
            val layoutParams = FlexboxLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(margin, margin, margin, margin)
            }
            itemView.layoutParams = layoutParams

            if (i == currentQuestion.missingIndex) {
                tv.text = "?"
                tv.setTextColor(Color.DKGRAY)
            } else {
                tv.text = currentQuestion.sequence[i].toString()
                tv.setTextColor(Color.WHITE)
            }
            sequenceContainerLayout.addView(itemView)
        }
    }

    private fun renderOptionsUI() {
        val correctAnswer = currentQuestion.missingValue
        val optionsData = ExerciseGeneratorUtil.generateRecognitionOptions(correctAnswer, 15, 4)

        val colors = listOf("#9C27B0", "#1368CE", "#D89E00", "#00BCD4")

        for ((index, option) in optionsData.options.withIndex()) {
            val optionBtn = layoutInflater.inflate(R.layout.item_answer_choicer, optionsContainerLayout, false) as MaterialCardView
            optionBtn.setCardBackgroundColor(colors[index].toColorInt())
            
            val tv = optionBtn.findViewById<TextView>(R.id.answerChoicerText)
            tv.text = option.toString()
            
            val layoutParams = GridLayout.LayoutParams(
                GridLayout.spec(GridLayout.UNDEFINED, 1f),
                GridLayout.spec(GridLayout.UNDEFINED, 1f)
            ).apply {
                width = 0
                height = ViewGroup.LayoutParams.WRAP_CONTENT
            }
            optionBtn.layoutParams = layoutParams

            optionBtn.setOnClickListener { checkAnswer(option, optionBtn) }
            optionsContainerLayout.addView(optionBtn)
        }
    }

    private fun checkAnswer(selectedOption: Int, card: MaterialCardView) {
        if (selectedOption == currentQuestion.missingValue) {
            card.setCardBackgroundColor("#66BB6A".toColorInt())
            Toast.makeText(this, "Great Job!", Toast.LENGTH_SHORT).show()
            
            for (i in 0 until optionsContainerLayout.childCount) {
                optionsContainerLayout.getChildAt(i).isEnabled = false
            }

            card.postDelayed({ loadNextQuestion() }, 1000)
        } else {
            card.setCardBackgroundColor("#EF5350".toColorInt())
            val shake = AnimationUtils.loadAnimation(this, R.anim.shake)
            card.startAnimation(shake)
            Toast.makeText(this, "Oops, try again!", Toast.LENGTH_SHORT).show()
        }
    }
}
