package com.uccd3223.p1_chai_boon_hong_2206806

import androidx.core.graphics.toColorInt
import android.os.Bundle
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.Toast
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.uccd3223.p1_chai_boon_hong_2206806.util.ExerciseGeneratorUtil

class AssociationActivity : AppCompatActivity() {

    private lateinit var objectsGridLayout: GridLayout
    private lateinit var optionsContainerLayout: GridLayout
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
                setImageResource(R.drawable.game_apple)
                scaleType = ImageView.ScaleType.FIT_CENTER
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
        val optionsData = ExerciseGeneratorUtil.generateRecognitionOptions(targetCount, 9, 4)

        val colors = listOf("#9C27B0", "#1368CE", "#D89E00", "#00BCD4")

        for ((index, option) in optionsData.options.withIndex()) {
            val optionBtn = layoutInflater.inflate(R.layout.item_answer_choicer, optionsContainerLayout, false) as MaterialCardView
            optionBtn.setCardBackgroundColor(colors[index].toColorInt())
            
            val tv = optionBtn.findViewById<android.widget.TextView>(R.id.answerChoicerText)
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
        if (selectedOption == targetCount) {
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
