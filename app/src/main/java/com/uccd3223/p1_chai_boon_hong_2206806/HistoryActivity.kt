package com.uccd3223.p1_chai_boon_hong_2206806

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.android.material.tabs.TabLayout

class HistoryActivity : AppCompatActivity() {

    private lateinit var rvHistory: RecyclerView
    private lateinit var historyList: List<HistoryRecord>
    private lateinit var tvEmptyHistory: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
        }

        rvHistory = findViewById(R.id.rvHistory)
        tvEmptyHistory = findViewById(R.id.tvEmptyHistory)
        rvHistory.layoutManager = LinearLayoutManager(this)
        
        historyList = HistoryManager.getHistory(this)
        
        val tabLayout = findViewById<TabLayout>(R.id.tabLayoutMode)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                updateList(tab?.position ?: 0)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Initial load
        updateList(0)
    }
    
    private fun updateList(tabPosition: Int) {
        val filteredList = if (tabPosition == 0) {
            historyList.filter { it.mode == "FUN" }
        } else {
            historyList.filter { it.mode != "FUN" }
        }
        rvHistory.adapter = HistoryAdapter(filteredList)
        tvEmptyHistory.visibility = if (filteredList.isEmpty()) View.VISIBLE else View.GONE
    }
}
