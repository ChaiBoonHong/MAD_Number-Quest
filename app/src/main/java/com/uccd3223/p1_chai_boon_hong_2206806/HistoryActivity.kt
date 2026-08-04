package com.uccd3223.p1_chai_boon_hong_2206806

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class HistoryActivity : AppCompatActivity() {

    private lateinit var rvHistory: RecyclerView
    private lateinit var historyList: List<HistoryRecord>
    private lateinit var tvEmptyHistory: TextView
    private lateinit var btnHistoryFun: MaterialButton
    private lateinit var btnHistoryChallenge: MaterialButton
    private var selectedModeIndex = 0

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
        
        btnHistoryFun = findViewById(R.id.btnHistoryFun)
        btnHistoryChallenge = findViewById(R.id.btnHistoryChallenge)
        btnHistoryFun.setOnClickListener { selectMode(0) }
        btnHistoryChallenge.setOnClickListener { selectMode(1) }

        selectMode(savedInstanceState?.getInt(STATE_SELECTED_MODE) ?: 0)
    }

    private fun selectMode(modeIndex: Int) {
        selectedModeIndex = modeIndex.coerceIn(0, 1)
        btnHistoryFun.isChecked = selectedModeIndex == 0
        btnHistoryChallenge.isChecked = selectedModeIndex == 1
        updateList(selectedModeIndex)
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

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(STATE_SELECTED_MODE, selectedModeIndex)
        super.onSaveInstanceState(outState)
    }

    companion object {
        private const val STATE_SELECTED_MODE = "history_selected_mode"
    }
}
