package com.uccd3223.p1_chai_boon_hong_2206806;

import android.os.Bundle;
import android.app.Dialog;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;

public class SettingsActivity extends AppCompatActivity {
    private MaterialSwitch musicSwitch;
    private MaterialSwitch soundSwitch;
    private MaterialSwitch vibrationSwitch;
    private Slider musicSlider;
    private Slider soundSlider;
    private boolean binding;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        findViewById(R.id.btnBack).setOnClickListener(view -> finish());
        musicSwitch = findViewById(R.id.switchMusic);
        soundSwitch = findViewById(R.id.switchSound);
        vibrationSwitch = findViewById(R.id.switchVibration);
        musicSlider = findViewById(R.id.sliderMusic);
        soundSlider = findViewById(R.id.sliderSound);
        bindValues();

        musicSwitch.setOnCheckedChangeListener((button, checked) -> {
            if (binding) return;
            GameSettingsManager.setMusicEnabled(this, checked);
            GameAudioManager.get(this).refreshMusic();
        });
        soundSwitch.setOnCheckedChangeListener((button, checked) -> {
            if (!binding) GameSettingsManager.setSoundEnabled(this, checked);
        });
        vibrationSwitch.setOnCheckedChangeListener((button, checked) -> {
            if (!binding) GameSettingsManager.setVibrationEnabled(this, checked);
        });
        musicSlider.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                GameSettingsManager.setMusicVolume(this, Math.round(value));
                GameAudioManager.get(this).refreshMusic();
            }
        });
        soundSlider.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) GameSettingsManager.setSoundVolume(this, Math.round(value));
        });
        findViewById(R.id.btnReset).setOnClickListener(view -> showResetConfirmation());
    }

    private void bindValues() {
        binding = true;
        musicSwitch.setChecked(GameSettingsManager.isMusicEnabled(this));
        soundSwitch.setChecked(GameSettingsManager.isSoundEnabled(this));
        vibrationSwitch.setChecked(GameSettingsManager.isVibrationEnabled(this));
        musicSlider.setValue(GameSettingsManager.getMusicVolume(this));
        soundSlider.setValue(GameSettingsManager.getSoundVolume(this));
        binding = false;
    }

    private void showResetConfirmation() {
        Dialog dialog = new Dialog(this, R.style.FullScreenDialogTheme);
        dialog.setContentView(R.layout.dialog_confirmation);
        dialog.<TextView>findViewById(R.id.tvConfirmTitle).setText(R.string.reset_title);
        dialog.<TextView>findViewById(R.id.tvConfirmMessage).setText(R.string.reset_message);
        dialog.<Button>findViewById(R.id.btnConfirmPositive).setText(R.string.reset);
        dialog.<Button>findViewById(R.id.btnConfirmNegative).setText(R.string.cancel);
        dialog.findViewById(R.id.btnConfirmNegative).setOnClickListener(view -> dialog.dismiss());
        dialog.findViewById(R.id.btnConfirmPositive).setOnClickListener(view -> {
                    HistoryManager.reset(this);
                    ProgressManager.reset(this);
                    GameSettingsManager.reset(this);
                    bindValues();
                    GameAudioManager.get(this).refreshMusic();
                    Toast.makeText(this, R.string.reset_complete, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                });
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }
}
