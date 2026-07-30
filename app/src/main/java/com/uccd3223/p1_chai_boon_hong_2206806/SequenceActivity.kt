package com.uccd3223.p1_chai_boon_hong_2206806

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.uccd3223.p1_chai_boon_hong_2206806.util.ExerciseGeneratorUtil
import kotlin.random.Random

class SequenceActivity : AppCompatActivity() {

    private lateinit var sequenceContainerLayout: LinearLayout
    private lateinit var optionsContainerLayout: LinearLayout
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
        val padding = (16 * dpScale).toInt()
        val margin = (8 * dpScale).toInt()

        for (i in currentQuestion.sequence.indices) {
            val itemText = TextView(this).apply {
                textSize = 32f
                setPadding(padding, padding, padding, padding)
                setTextColor(Color.WHITE)
                
                val layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(margin, margin, margin, margin)
                }
                this.layoutParams = layoutParams

                if (i == currentQuestion.missingIndex) {
                    text = "?"
                    setBackgroundColor(Color.LTGRAY)
                } else {
                    text = currentQuestion.sequence[i].toString()
                    setBackgroundColor(Color.parseColor("#4CAF50"))
                }
            }
            sequenceContainerLayout.addView(itemText)
        }
    }

    private fun renderOptionsUI() {
        val dpScale = resources.displayMetrics.density
        val margin = (8 * dpScale).toInt()
        
        val correctAnswer = currentQuestion.missingValue
        val optionsData = ExerciseGeneratorUtil.generateRecognitionOptions(correctAnswer, 15, 3)

        for (option in optionsData.options) {
            val optionBtn = Button(this).apply {
                text = option.toString()
                textSize = 28f
                
                val layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(margin, margin, margin, margin)
                }
                this.layoutParams = layoutParams

                setOnClickListener { checkAnswer(option) }
            }
            optionsContainerLayout.addView(optionBtn)
        }
    }

    private fun checkAnswer(selectedOption: Int) {
        if (selectedOption == currentQuestion.missingValue) {
            loadNextQuestion() // correct
        } else {
            // incorrect animation would go here
        }
    }
}
