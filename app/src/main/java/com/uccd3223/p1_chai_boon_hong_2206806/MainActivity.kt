package com.uccd3223.p1_chai_boon_hong_2206806

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.google.android.material.materialswitch.MaterialSwitch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val modeSwitch = findViewById<com.google.android.material.materialswitch.MaterialSwitch>(R.id.modeSwitch)

        fun getSelectedMode(): String {
            return if (modeSwitch.isChecked) "CHALLENGE" else "FUN"
        }
        
        fun handleGameSelection(activityClass: Class<*>) {
            val mode = getSelectedMode()
            if (mode == "FUN") {
                val intent = Intent(this, activityClass)
                intent.putExtra("GAME_MODE", "FUN")
                startActivity(intent)
            } else {
                showChallengeSelectionDialog(activityClass)
            }
        }

        findViewById<MaterialCardView>(R.id.cardSequence).setOnClickListener {
            handleGameSelection(SequenceActivity::class.java)
        }
        
        findViewById<MaterialCardView>(R.id.cardAssociation).setOnClickListener {
            handleGameSelection(AssociationActivity::class.java)
        }
        
        findViewById<MaterialCardView>(R.id.cardPlaceValue).setOnClickListener {
            handleGameSelection(PlaceValueActivity::class.java)
        }
        
        findViewById<MaterialCardView>(R.id.cardRecognition).setOnClickListener {
            handleGameSelection(RecognitionActivity::class.java)
        }
    }
    
    private fun showChallengeSelectionDialog(activityClass: Class<*>) {
        val dialog = android.app.Dialog(this, R.style.FullScreenDialogTheme)
        dialog.setContentView(R.layout.dialog_challenge_selection)
            
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT)
        
        dialog.findViewById<android.widget.Button>(R.id.btnTimeAttack).setOnClickListener {
            dialog.dismiss()
            showTimeSelectionDialog(activityClass)
        }
        
        dialog.findViewById<android.widget.Button>(R.id.btnScoreAttack).setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, activityClass)
            intent.putExtra("GAME_MODE", "SCORE_ATTACK")
            startActivity(intent)
        }
        
        dialog.findViewById<android.widget.ImageButton>(R.id.btnClose).setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun showTimeSelectionDialog(activityClass: Class<*>) {
        val dialog = android.app.Dialog(this, R.style.FullScreenDialogTheme)
        dialog.setContentView(R.layout.dialog_time_selection)
            
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT)
        
        val startTimeAttack = { timeLimit: Long ->
            dialog.dismiss()
            val intent = Intent(this, activityClass)
            intent.putExtra("GAME_MODE", "TIME_ATTACK")
            intent.putExtra("TIME_LIMIT", timeLimit)
            startActivity(intent)
        }
        
        dialog.findViewById<android.widget.Button>(R.id.btn60s).setOnClickListener { startTimeAttack(60000L) }
        dialog.findViewById<android.widget.Button>(R.id.btn90s).setOnClickListener { startTimeAttack(90000L) }
        dialog.findViewById<android.widget.Button>(R.id.btn120s).setOnClickListener { startTimeAttack(120000L) }
        
        dialog.findViewById<android.widget.ImageButton>(R.id.btnClose).setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }
}