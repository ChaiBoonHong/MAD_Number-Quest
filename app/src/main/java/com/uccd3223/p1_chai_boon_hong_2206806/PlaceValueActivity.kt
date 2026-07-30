package com.uccd3223.p1_chai_boon_hong_2206806

import androidx.core.graphics.toColorInt
import android.os.Bundle
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.card.MaterialCardView
import com.uccd3223.p1_chai_boon_hong_2206806.util.ExerciseGeneratorUtil

class PlaceValueActivity : AppCompatActivity() {

    private lateinit var visualContainerLayout: FlexboxLayout
    private lateinit var optionsContainerLayout: GridLayout
    private lateinit var currentData: ExerciseGeneratorUtil.PlaceValueData
    private var lastTotal: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_value)

        initializeUI()
        loadNextQuestion()
    }

    private fun initializeUI() {
        visualContainerLayout = findViewById(R.id.visualContainerLayout)
        optionsContainerLayout = findViewById(R.id.optionsContainerLayout)
        
        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    private fun loadNextQuestion() {
        visualContainerLayout.removeAllViews()
        optionsContainerLayout.removeAllViews()

        do {
            currentData = ExerciseGeneratorUtil.generatePlaceValue(9)
        } while (currentData.total == lastTotal)
        lastTotal = currentData.total

        renderVisualUI()
        renderOptionsUI()
    }

    private fun renderVisualUI() {
        val dpScale = resources.displayMetrics.density
        
        // Dynamically calculate unit size to fit within the 1:1 square border
        // A Tens block is 1 unit wide, 10 units tall. A Ones block is 1 unit wide, 1 unit tall.
        val totalBlocks = currentData.tens + currentData.ones
        val unitSizeDp = when {
            totalBlocks <= 6 -> 24
            totalBlocks <= 12 -> 20
            else -> 18
        }
        val marginDp = 4
        
        val margin = (marginDp * dpScale).toInt()
        val unitSize = (unitSizeDp * dpScale).toInt()
        val tensHeight = unitSize * 10

        // Tens
        for (i in 0 until currentData.tens) {
            val tensView = ImageView(this).apply {
                setImageResource(R.drawable.ic_tens_block)
                scaleType = ImageView.ScaleType.FIT_XY
                val newLayoutParams = FlexboxLayout.LayoutParams(unitSize, tensHeight).apply {
                    setMargins(margin, margin, margin, margin)
                }
                this.layoutParams = newLayoutParams
            }
            visualContainerLayout.addView(tensView)
        }

        // Ones
        for (i in 0 until currentData.ones) {
            val onesView = ImageView(this).apply {
                setImageResource(R.drawable.ic_ones_block)
                scaleType = ImageView.ScaleType.FIT_XY
                val newLayoutParams = FlexboxLayout.LayoutParams(unitSize, unitSize).apply {
                    setMargins(margin, margin, margin, margin)
                }
                this.layoutParams = newLayoutParams
            }
            visualContainerLayout.addView(onesView)
        }
    }

    private fun renderOptionsUI() {
        val optionsData = ExerciseGeneratorUtil.generateRecognitionOptions(currentData.total, 99, 4)

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
        if (selectedOption == currentData.total) {
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
