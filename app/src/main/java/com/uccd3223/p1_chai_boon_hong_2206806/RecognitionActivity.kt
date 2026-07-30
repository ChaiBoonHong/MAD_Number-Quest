package com.uccd3223.p1_chai_boon_hong_2206806

import android.os.Bundle
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.graphics.toColorInt
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.uccd3223.p1_chai_boon_hong_2206806.util.ExerciseGeneratorUtil
import kotlin.random.Random

class RecognitionActivity : BaseGameActivity() {

    private lateinit var tvTarget: TextView
    private lateinit var optionsContainerLayout: GridLayout
    private lateinit var currentData: ExerciseGeneratorUtil.RecognitionData
    private var lastTarget: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recognition)
        
        setupGameModeUI()

        initializeUI()
        loadNextQuestion()
    }

    private fun initializeUI() {
        tvTarget = findViewById(R.id.tvTarget)
        optionsContainerLayout = findViewById(R.id.optionsContainerLayout)
        
        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    private fun loadNextQuestion() {
        optionsContainerLayout.removeAllViews()

        var target: Int
        do {
            target = Random.nextInt(1, 50)
        } while (target == lastTarget)
        lastTarget = target
        
        currentData = ExerciseGeneratorUtil.generateRecognitionOptions(target, 50, 4)

        tvTarget.text = ExerciseGeneratorUtil.numberToWords(currentData.targetNumber)
        renderOptionsUI()
    }

    private fun renderOptionsUI() {
        val colors = listOf("#9C27B0", "#1368CE", "#D89E00", "#00BCD4")

        for ((index, option) in currentData.options.withIndex()) {
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
        if (selectedOption == currentData.targetNumber) {
            card.setCardBackgroundColor("#66BB6A".toColorInt())
            Toast.makeText(this, "Great Job!", Toast.LENGTH_SHORT).show()
            onQuestionCompleted()
            
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
