package com.uccd3223.p1_chai_boon_hong_2206806

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object HistoryManager {
    private const val PREFS_NAME = "GameHistoryPrefs"
    private const val KEY_HISTORY = "history_list"
    
    fun saveRecord(context: Context, record: HistoryRecord) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val historyList = getHistory(context).toMutableList()
        historyList.add(0, record) // Add to top
        
        val gson = Gson()
        val json = gson.toJson(historyList)
        prefs.edit().putString(KEY_HISTORY, json).apply()
    }
    
    fun getHistory(context: Context): List<HistoryRecord> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        
        val gson = Gson()
        val type = object : TypeToken<List<HistoryRecord>>() {}.type
        return gson.fromJson(json, type)
    }
}
