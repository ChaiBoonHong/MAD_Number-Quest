package com.uccd3223.p1_chai_boon_hong_2206806;

import android.content.Context;
import android.content.SharedPreferences;

public final class GameSettingsManager {
    private static final String PREFS = "NumberQuestSettings";
    private static final String MUSIC_ENABLED = "music_enabled";
    private static final String MUSIC_VOLUME = "music_volume";
    private static final String SOUND_ENABLED = "sound_enabled";
    private static final String SOUND_VOLUME = "sound_volume";
    private static final String VIBRATION_ENABLED = "vibration_enabled";

    public static final int DEFAULT_MUSIC_VOLUME = 30;
    public static final int DEFAULT_SOUND_VOLUME = 80;

    private GameSettingsManager() { }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static boolean isMusicEnabled(Context context) { return prefs(context).getBoolean(MUSIC_ENABLED, true); }
    public static int getMusicVolume(Context context) { return prefs(context).getInt(MUSIC_VOLUME, DEFAULT_MUSIC_VOLUME); }
    public static boolean isSoundEnabled(Context context) { return prefs(context).getBoolean(SOUND_ENABLED, true); }
    public static int getSoundVolume(Context context) { return prefs(context).getInt(SOUND_VOLUME, DEFAULT_SOUND_VOLUME); }
    public static boolean isVibrationEnabled(Context context) { return prefs(context).getBoolean(VIBRATION_ENABLED, true); }

    public static void setMusicEnabled(Context context, boolean value) { prefs(context).edit().putBoolean(MUSIC_ENABLED, value).apply(); }
    public static void setMusicVolume(Context context, int value) { prefs(context).edit().putInt(MUSIC_VOLUME, clamp(value)).apply(); }
    public static void setSoundEnabled(Context context, boolean value) { prefs(context).edit().putBoolean(SOUND_ENABLED, value).apply(); }
    public static void setSoundVolume(Context context, int value) { prefs(context).edit().putInt(SOUND_VOLUME, clamp(value)).apply(); }
    public static void setVibrationEnabled(Context context, boolean value) { prefs(context).edit().putBoolean(VIBRATION_ENABLED, value).apply(); }
    public static void reset(Context context) { prefs(context).edit().clear().apply(); }

    private static int clamp(int value) { return Math.max(0, Math.min(100, value)); }
}
