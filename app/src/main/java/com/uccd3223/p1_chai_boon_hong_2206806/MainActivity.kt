package com.uccd3223.p1_chai_boon_hong_2206806

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnSequence).setOnClickListener {
            startActivity(Intent(this, SequenceActivity::class.java))
        }
        
        // Other buttons
        findViewById<Button>(R.id.btnAssociation).setOnClickListener {
            startActivity(Intent(this, AssociationActivity::class.java))
        }
        findViewById<Button>(R.id.btnPlaceValue).setOnClickListener {
            startActivity(Intent(this, PlaceValueActivity::class.java))
        }
        findViewById<Button>(R.id.btnRecognition).setOnClickListener {
            startActivity(Intent(this, RecognitionActivity::class.java))
        }
    }
}