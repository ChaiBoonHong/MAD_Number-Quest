package com.uccd3223.p1_chai_boon_hong_2206806

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.uccd3223.p1_chai_boon_hong_2206806.util.ExerciseGeneratorUtil

class AssociationActivity : AppCompatActivity() {

    private lateinit var objectsGridLayout: GridLayout
    private lateinit var optionsContainerLayout: LinearLayout
    private var targetCount: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_association)

        initializeUI()
        loadNextQuestion()
    }

    private fun initializeUI() {
        objectsGridLayout = findViewById(R.id.objectsGridLayout)
        optionsContainerLayout = findViewById(R.id.optionsContainerLayout)
    }

    private fun loadNextQuestion() {
        objectsGridLayout.removeAllViews()
        optionsContainerLayout.removeAllViews()

        targetCount = ExerciseGeneratorUtil.generateObjectCount(9)

        renderObjectsUI()
        renderOptionsUI()
    }

    private fun renderObjectsUI() {
        val dpScale = resources.displayMetrics.density
        val size = (64 * dpScale).toInt()
        val margin = (8 * dpScale).toInt()

        for (i in 0 until targetCount) {
            val itemView = ImageView(this).apply {
                // In a real app we'd load a drawable like an apple or star.
                // Here we use a colored box as a placeholder visually representing an object.
                setBackgroundColor(Color.parseColor("#FF9800"))
                val layoutParams = GridLayout.LayoutParams().apply {
                    width = size
                    height = size
                    setMargins(margin, margin, margin, margin)
                }
                this.layoutParams = layoutParams
            }
            objectsGridLayout.addView(itemView)
        }
    }

    private fun renderOptionsUI() {
        val dpScale = resources.displayMetrics.density
        val margin = (8 * dpScale).toInt()
        
        val optionsData = ExerciseGeneratorUtil.generateRecognitionOptions(targetCount, 9, 3)

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
        if (selectedOption == targetCount) {
            loadNextQuestion()
        } else {
            // Error handling (e.g. shake animation)
        }
    }
}
