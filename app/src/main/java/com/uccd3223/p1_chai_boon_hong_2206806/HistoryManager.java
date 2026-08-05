package com.uccd3223.p1_chai_boon_hong_2206806;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class HistoryManager {
    private static final String PREFS_NAME = "GameHistoryPrefs";
    private static final String KEY_HISTORY = "history_list";
    private static final int MAX_HISTORY_RECORDS = 100;

    private HistoryManager() {
    }

    public static void saveRecord(Context context, HistoryRecord record) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        List<HistoryRecord> history = new ArrayList<>(getHistory(context));
        history.add(0, record);
        if (history.size() > MAX_HISTORY_RECORDS) {
            history.subList(MAX_HISTORY_RECORDS, history.size()).clear();
        }
        prefs.edit().putString(KEY_HISTORY, new Gson().toJson(history)).apply();
    }

    public static List<HistoryRecord> getHistory(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_HISTORY, null);
        if (json == null) return Collections.emptyList();

        Type type = new TypeToken<List<HistoryRecord>>() { }.getType();
        try {
            List<HistoryRecord> history = new Gson().fromJson(json, type);
            return history == null ? Collections.emptyList() : history;
        } catch (RuntimeException exception) {
            return Collections.emptyList();
        }
    }

    public static void reset(Context context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply();
    }
}
