package com.uccd3223.p1_chai_boon_hong_2206806

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
        }

        val rvHistory = findViewById<RecyclerView>(R.id.rvHistory)
        rvHistory.layoutManager = LinearLayoutManager(this)
        
        val historyList = HistoryManager.getHistory(this)
        rvHistory.adapter = HistoryAdapter(historyList)
    }
}
