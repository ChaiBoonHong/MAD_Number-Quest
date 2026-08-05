package com.uccd3223.p1_chai_boon_hong_2206806;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;

public final class GameAudioManager implements AudioManager.OnAudioFocusChangeListener {
    private static GameAudioManager instance;
    private final Context context;
    private final AudioManager audioManager;
    private final SoundPool soundPool;
    private final int correctSound;
    private final int incorrectSound;
    private final int recordSound;
    private MediaPlayer musicPlayer;
    private AudioFocusRequest focusRequest;
    private boolean appForeground;

    private GameAudioManager(Context context) {
        this.context = context.getApplicationContext();
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        soundPool = new SoundPool.Builder().setMaxStreams(2).setAudioAttributes(attributes).build();
        correctSound = soundPool.load(context, R.raw.confirmation_003, 1);
        incorrectSound = soundPool.load(context, R.raw.error_003, 1);
        recordSound = soundPool.load(context, R.raw.power_up_sound_v1, 1);
    }

    public static synchronized GameAudioManager get(Context context) {
        if (instance == null) instance = new GameAudioManager(context);
        return instance;
    }

    public void setAppForeground(boolean foreground) {
        appForeground = foreground;
        if (foreground) refreshMusic(); else pauseMusic();
    }

    public void refreshMusic() {
        if (!appForeground || !GameSettingsManager.isMusicEnabled(context)) {
            pauseMusic();
            return;
        }
        float volume = GameSettingsManager.getMusicVolume(context) / 100f;
        if (musicPlayer == null) {
            musicPlayer = MediaPlayer.create(context, R.raw.title_in_game);
            if (musicPlayer == null) return;
            musicPlayer.setLooping(true);
        }
        musicPlayer.setVolume(volume, volume);
        if (!musicPlayer.isPlaying() && requestAudioFocus()) musicPlayer.start();
    }

    public void playCorrect() { play(correctSound); }
    public void playIncorrect() { play(incorrectSound); }
    public void playRecord() { play(recordSound); }

    private void play(int soundId) {
        if (!GameSettingsManager.isSoundEnabled(context)) return;
        float volume = GameSettingsManager.getSoundVolume(context) / 100f;
        soundPool.play(soundId, volume, volume, 1, 0, 1f);
    }

    private boolean requestAudioFocus() {
        AudioAttributes attributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).build();
        focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(attributes).setOnAudioFocusChangeListener(this).build();
        return audioManager.requestAudioFocus(focusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private void pauseMusic() {
        if (musicPlayer != null && musicPlayer.isPlaying()) musicPlayer.pause();
    }

    @Override public void onAudioFocusChange(int focusChange) {
        if (focusChange == AudioManager.AUDIOFOCUS_GAIN) refreshMusic(); else pauseMusic();
    }
}
