package com.uccd3223.p1_chai_boon_hong_2206806

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<MaterialCardView>(R.id.cardSequence).setOnClickListener {
            startActivity(Intent(this, SequenceActivity::class.java))
        }
        
        findViewById<MaterialCardView>(R.id.cardAssociation).setOnClickListener {
            startActivity(Intent(this, AssociationActivity::class.java))
        }
        
        findViewById<MaterialCardView>(R.id.cardPlaceValue).setOnClickListener {
            startActivity(Intent(this, PlaceValueActivity::class.java))
        }
        
        findViewById<MaterialCardView>(R.id.cardRecognition).setOnClickListener {
            startActivity(Intent(this, RecognitionActivity::class.java))
        }
    }
}