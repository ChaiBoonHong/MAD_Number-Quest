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
    private var lastTarget: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recognition)

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
                setTextColor(android.graphics.Color.WHITE)
                setBackgroundResource(R.drawable.btn_rounded_primary)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(margin, margin, margin, margin)
                }
                setOnClickListener { checkAnswer(option, this) }
            }
            optionsContainerLayout.addView(optionBtn)
        }
    }

    private fun checkAnswer(selectedOption: Int, button: Button) {
        if (selectedOption == currentData.targetNumber) {
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
