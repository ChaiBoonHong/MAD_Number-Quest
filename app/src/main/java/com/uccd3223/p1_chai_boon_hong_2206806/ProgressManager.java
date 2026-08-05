package com.uccd3223.p1_chai_boon_hong_2206806;

import android.content.Context;
import android.content.SharedPreferences;

public final class ProgressManager {
    private static final String PREFS = "GameHighScores";
    public static final int NO_SCORE = -1;

    private ProgressManager() { }

    public static String category(String mode, long timeLimitMs) {
        if ("FUN".equals(mode)) return "FUN";
        if ("TIME_ATTACK".equals(mode)) return timeLimitMs > 0 ? "TA_" + timeLimitMs : "TA_LEGACY";
        if ("SCORE_ATTACK".equals(mode)) return "ROUND_RUSH";
        return "UNKNOWN";
    }

    public static String key(String gameName, String mode, long timeLimitMs) {
        return gameName + "_" + category(mode, timeLimitMs);
    }

    public static int getBest(Context context, String gameName, String mode, long timeLimitMs) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String scoreKey = key(gameName, mode, timeLimitMs);
        if (prefs.contains(scoreKey)) return prefs.getInt(scoreKey, NO_SCORE);
        if ("SCORE_ATTACK".equals(mode)) return prefs.getInt(gameName + "_SA", NO_SCORE);
        return NO_SCORE;
    }

    public static boolean updateBest(Context context, String gameName, String mode, long timeLimitMs, int score) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String key = key(gameName, mode, timeLimitMs);
        int previous = getBest(context, gameName, mode, timeLimitMs);
        if (score <= previous) return false;
        prefs.edit().putInt(key, score).apply();
        return true;
    }

    public static void reset(Context context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply();
    }
}
