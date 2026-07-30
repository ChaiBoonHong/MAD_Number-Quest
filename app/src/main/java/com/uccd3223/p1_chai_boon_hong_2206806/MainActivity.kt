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

        val modeSwitch = findViewById<MaterialSwitch>(R.id.modeSwitch)

        fun getSelectedMode(): String {
            return if (modeSwitch.isChecked) "PK" else "FUN"
        }

        findViewById<MaterialCardView>(R.id.cardSequence).setOnClickListener {
            val intent = Intent(this, SequenceActivity::class.java)
            intent.putExtra("GAME_MODE", getSelectedMode())
            startActivity(intent)
        }
        
        findViewById<MaterialCardView>(R.id.cardAssociation).setOnClickListener {
            val intent = Intent(this, AssociationActivity::class.java)
            intent.putExtra("GAME_MODE", getSelectedMode())
            startActivity(intent)
        }
        
        findViewById<MaterialCardView>(R.id.cardPlaceValue).setOnClickListener {
            val intent = Intent(this, PlaceValueActivity::class.java)
            intent.putExtra("GAME_MODE", getSelectedMode())
            startActivity(intent)
        }
        
        findViewById<MaterialCardView>(R.id.cardRecognition).setOnClickListener {
            val intent = Intent(this, RecognitionActivity::class.java)
            intent.putExtra("GAME_MODE", getSelectedMode())
            startActivity(intent)
        }
    }
}