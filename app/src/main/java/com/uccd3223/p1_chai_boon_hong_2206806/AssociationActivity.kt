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
    private var lastTargetCount: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_association)

        initializeUI()
        loadNextQuestion()
    }

    private fun initializeUI() {
        objectsGridLayout = findViewById(R.id.objectsGridLayout)
        optionsContainerLayout = findViewById(R.id.optionsContainerLayout)
        
        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    private fun loadNextQuestion() {
        objectsGridLayout.removeAllViews()
        optionsContainerLayout.removeAllViews()

        do {
            targetCount = ExerciseGeneratorUtil.generateObjectCount(15)
        } while (targetCount == lastTargetCount)
        lastTargetCount = targetCount

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
                setTextColor(Color.WHITE)
                setBackgroundResource(R.drawable.btn_rounded_primary)
                val layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(margin, margin, margin, margin)
                }
                this.layoutParams = layoutParams

                setOnClickListener { checkAnswer(option, this) }
            }
            optionsContainerLayout.addView(optionBtn)
        }
    }

    private fun checkAnswer(selectedOption: Int, button: Button) {
        if (selectedOption == targetCount) {
            button.setBackgroundResource(R.drawable.btn_rounded_correct)
            android.widget.Toast.makeText(this, "Great Job!", android.widget.Toast.LENGTH_SHORT).show()
            
            for (i in 0 until optionsContainerLayout.childCount) {
                optionsContainerLayout.getChildAt(i).isEnabled = false
            }

            button.postDelayed({ loadNextQuestion() }, 1000)
        } else {
            button.setBackgroundResource(R.drawable.btn_rounded_wrong)
            val shake = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.shake)
            button.startAnimation(shake)
            android.widget.Toast.makeText(this, "Oops, try again!", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}
