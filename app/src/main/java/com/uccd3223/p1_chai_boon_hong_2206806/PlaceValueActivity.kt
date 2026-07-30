package com.uccd3223.p1_chai_boon_hong_2206806

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.uccd3223.p1_chai_boon_hong_2206806.util.ExerciseGeneratorUtil

class PlaceValueActivity : AppCompatActivity() {

    private lateinit var visualContainerLayout: LinearLayout
    private lateinit var optionsContainerLayout: LinearLayout
    private lateinit var currentData: ExerciseGeneratorUtil.PlaceValueData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_value)

        initializeUI()
        loadNextQuestion()
    }

    private fun initializeUI() {
        visualContainerLayout = findViewById(R.id.visualContainerLayout)
        optionsContainerLayout = findViewById(R.id.optionsContainerLayout)
    }

    private fun loadNextQuestion() {
        visualContainerLayout.removeAllViews()
        optionsContainerLayout.removeAllViews()

        currentData = ExerciseGeneratorUtil.generatePlaceValue(9)

        renderVisualUI()
        renderOptionsUI()
    }

    private fun renderVisualUI() {
        val dpScale = resources.displayMetrics.density
        val margin = (8 * dpScale).toInt()
        val padding = (16 * dpScale).toInt()

        // Tens
        for (i in 0 until currentData.tens) {
            val tensView = TextView(this).apply {
                text = "10"
                textSize = 24f
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.parseColor("#E91E63"))
                setPadding(padding, padding, padding, padding)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(margin, margin, margin, margin)
                }
            }
            visualContainerLayout.addView(tensView)
        }

        // Ones
        for (i in 0 until currentData.ones) {
            val onesView = TextView(this).apply {
                text = "1"
                textSize = 18f
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.parseColor("#2196F3"))
                setPadding(padding, padding, padding, padding)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(margin, margin, margin, margin)
                }
            }
            visualContainerLayout.addView(onesView)
        }
    }

    private fun renderOptionsUI() {
        val dpScale = resources.displayMetrics.density
        val margin = (8 * dpScale).toInt()
        
        val optionsData = ExerciseGeneratorUtil.generateRecognitionOptions(currentData.total, 99, 3)

        for (option in optionsData.options) {
            val optionBtn = Button(this).apply {
                text = option.toString()
                textSize = 28f
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
        if (selectedOption == currentData.total) {
            loadNextQuestion()
        } else {
            // Error handling
        }
    }
}
