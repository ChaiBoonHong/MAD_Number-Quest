package com.uccd3223.p1_chai_boon_hong_2206806

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.uccd3223.p1_chai_boon_hong_2206806.util.ExerciseGeneratorUtil
import kotlin.random.Random

class RecognitionActivity : AppCompatActivity() {

    private lateinit var tvTarget: TextView
    private lateinit var optionsContainerLayout: LinearLayout
    private lateinit var currentData: ExerciseGeneratorUtil.RecognitionData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recognition)

        initializeUI()
        loadNextQuestion()
    }

    private fun initializeUI() {
        tvTarget = findViewById(R.id.tvTarget)
        optionsContainerLayout = findViewById(R.id.optionsContainerLayout)
    }

    private fun loadNextQuestion() {
        optionsContainerLayout.removeAllViews()

        val target = Random.nextInt(1, 20)
        currentData = ExerciseGeneratorUtil.generateRecognitionOptions(target, 20, 4)

        tvTarget.text = "Find the number: ${currentData.targetNumber}"
        renderOptionsUI()
    }

    private fun renderOptionsUI() {
        val dpScale = resources.displayMetrics.density
        val margin = (8 * dpScale).toInt()

        for (option in currentData.options) {
            val optionBtn = Button(this).apply {
                text = option.toString()
                textSize = 36f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(margin, margin, margin, margin)
                }
                setOnClickListener { checkAnswer(option) }
            }
            optionsContainerLayout.addView(optionBtn)
        }
    }

    private fun checkAnswer(selectedOption: Int) {
        if (selectedOption == currentData.targetNumber) {
            loadNextQuestion()
        } else {
            // Error handling
        }
    }
}
