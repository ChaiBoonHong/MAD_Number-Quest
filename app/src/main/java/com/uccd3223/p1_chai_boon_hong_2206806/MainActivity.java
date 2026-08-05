package com.uccd3223.p1_chai_boon_hong_2206806;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class MainActivity extends AppCompatActivity {
    private MaterialButton btnFunMode;
    private MaterialButton btnChallengeMode;
    private TextView tvModeHint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnFunMode = findViewById(R.id.btnFunMode);
        btnChallengeMode = findViewById(R.id.btnChallengeMode);
        tvModeHint = findViewById(R.id.tvModeHint);
        findViewById(R.id.btnHistory).setOnClickListener(view ->
                startActivity(new Intent(this, HistoryActivity.class))
        );
        findViewById(R.id.btnSettings).setOnClickListener(view ->
                startActivity(new Intent(this, SettingsActivity.class))
        );

        btnFunMode.setOnClickListener(view -> selectMode(false));
        btnChallengeMode.setOnClickListener(view -> selectMode(true));
        findViewById(R.id.cardSequence).setOnClickListener(view ->
                handleGameSelection(SequenceActivity.class));
        findViewById(R.id.cardAssociation).setOnClickListener(view ->
                handleGameSelection(AssociationActivity.class));
        findViewById(R.id.cardPlaceValue).setOnClickListener(view ->
                handleGameSelection(PlaceValueActivity.class));
        findViewById(R.id.cardRecognition).setOnClickListener(view ->
                handleGameSelection(RecognitionActivity.class));
    }

    private void selectMode(boolean challenge) {
        btnFunMode.setChecked(!challenge);
        btnChallengeMode.setChecked(challenge);
        tvModeHint.setText(challenge ? R.string.challenge_mode_hint : R.string.fun_mode_hint);
    }

    private String getSelectedMode() {
        return btnChallengeMode.isChecked() ? "CHALLENGE" : "FUN";
    }

    private void handleGameSelection(Class<?> activityClass) {
        if ("FUN".equals(getSelectedMode())) {
            Intent intent = new Intent(this, activityClass);
            intent.putExtra("GAME_MODE", "FUN");
            startActivity(intent);
        } else {
            showChallengeSelectionDialog(activityClass);
        }
    }

    private void showChallengeSelectionDialog(Class<?> activityClass) {
        Dialog dialog = new Dialog(this, R.style.FullScreenDialogTheme);
        dialog.setContentView(R.layout.dialog_challenge_selection);
        dialog.<Button>findViewById(R.id.btnTimeAttack).setOnClickListener(view -> {
            dialog.dismiss();
            showTimeSelectionDialog(activityClass);
        });
        dialog.<Button>findViewById(R.id.btnScoreAttack).setOnClickListener(view -> {
            dialog.dismiss();
            Intent intent = new Intent(this, activityClass);
            intent.putExtra("GAME_MODE", "SCORE_ATTACK");
            startActivity(intent);
        });
        dialog.<ImageButton>findViewById(R.id.btnClose).setOnClickListener(view -> dialog.dismiss());
        showFullScreenDialog(dialog);
    }

    private void showTimeSelectionDialog(Class<?> activityClass) {
        Dialog dialog = new Dialog(this, R.style.FullScreenDialogTheme);
        dialog.setContentView(R.layout.dialog_time_selection);
        dialog.<Button>findViewById(R.id.btn60s).setOnClickListener(view ->
                startTimeAttack(dialog, activityClass, 60_000L));
        dialog.<Button>findViewById(R.id.btn90s).setOnClickListener(view ->
                startTimeAttack(dialog, activityClass, 90_000L));
        dialog.<Button>findViewById(R.id.btn120s).setOnClickListener(view ->
                startTimeAttack(dialog, activityClass, 120_000L));
        dialog.<ImageButton>findViewById(R.id.btnClose).setOnClickListener(view -> dialog.dismiss());
        showFullScreenDialog(dialog);
    }

    private void startTimeAttack(Dialog dialog, Class<?> activityClass, long timeLimit) {
        dialog.dismiss();
        Intent intent = new Intent(this, activityClass);
        intent.putExtra("GAME_MODE", "TIME_ATTACK");
        intent.putExtra("TIME_LIMIT", timeLimit);
        startActivity(intent);
    }

    private void showFullScreenDialog(Dialog dialog) {
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }
}
