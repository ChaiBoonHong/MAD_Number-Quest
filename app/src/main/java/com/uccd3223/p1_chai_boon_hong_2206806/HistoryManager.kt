package com.uccd3223.p1_chai_boon_hong_2206806

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.core.content.edit

object HistoryManager {
    private const val PREFS_NAME = "GameHistoryPrefs"
    private const val KEY_HISTORY = "history_list"
    
    fun saveRecord(context: Context, record: HistoryRecord) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val historyList = getHistory(context).toMutableList()
        historyList.add(0, record) // Add to top
        if (historyList.size > MAX_HISTORY_RECORDS) {
            historyList.subList(MAX_HISTORY_RECORDS, historyList.size).clear()
        }
        
        val gson = Gson()
        val json = gson.toJson(historyList)
        prefs.edit { putString(KEY_HISTORY, json) }
    }
    
    fun getHistory(context: Context): List<HistoryRecord> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        
        val gson = Gson()
        val type = object : TypeToken<List<HistoryRecord>>() {}.type
        return runCatching { gson.fromJson<List<HistoryRecord>>(json, type) }
            .getOrNull()
            .orEmpty()
    }

    private const val MAX_HISTORY_RECORDS = 100
}
