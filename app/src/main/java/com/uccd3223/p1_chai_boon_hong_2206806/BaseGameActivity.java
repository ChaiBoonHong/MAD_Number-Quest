package com.uccd3223.p1_chai_boon_hong_2206806;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;

import java.util.Arrays;
import java.util.List;

public class BaseGameActivity extends AppCompatActivity {
    protected interface AnswerSelectionListener {
        void onSelected(AnswerChoice choice, MaterialCardView card);
    }

    private static final String STATE_SCORE = "state_score";
    private static final String STATE_ROUND = "state_round";
    private static final String STATE_ROUND_SCORE = "state_round_score";
    private static final String STATE_TIME_REMAINING = "state_time_remaining";
    private static final String STATE_HISTORY_SAVED = "state_history_saved";
    private static final String STATE_GAME_OVER = "state_game_over";
    private static final String STATE_RESULT_MESSAGE = "state_result_message";
    private static final String STATE_RESULT_RECORD = "state_result_record";

    private boolean historySaved;
    private String pendingResultMessage;
    private String pendingRecordText;
    private Dialog resultDialog;

    protected String gameMode = "FUN";
    protected int score;
    protected boolean isGameOver;

    private TextView tvScore;
    private TextView tvTimer;
    private CountDownTimer countDownTimer;
    private long timeLimitMs = 60_000L;
    private long remainingTimeMs;
    private int currentRound = 1;
    private int targetQuestionsForRound = 10;
    private int timeLimitForRoundSeconds = 30;
    private int roundScore;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String requestedMode = getIntent().getStringExtra("GAME_MODE");
        gameMode = requestedMode == null ? "FUN" : requestedMode;
        timeLimitMs = getIntent().getLongExtra("TIME_LIMIT", 60_000L);
        prefs = getSharedPreferences("GameHighScores", Context.MODE_PRIVATE);
        if (savedInstanceState != null) {
            score = savedInstanceState.getInt(STATE_SCORE, 0);
            currentRound = savedInstanceState.getInt(STATE_ROUND, 1);
            roundScore = savedInstanceState.getInt(STATE_ROUND_SCORE, 0);
            remainingTimeMs = savedInstanceState.getLong(STATE_TIME_REMAINING, 0L);
            historySaved = savedInstanceState.getBoolean(STATE_HISTORY_SAVED, false);
            isGameOver = savedInstanceState.getBoolean(STATE_GAME_OVER, false);
            pendingResultMessage = savedInstanceState.getString(STATE_RESULT_MESSAGE);
            pendingRecordText = savedInstanceState.getString(STATE_RESULT_RECORD);
        }
    }

    protected void setupGameModeUI() {
        tvScore = findViewById(R.id.tvScore);
        tvTimer = findViewById(R.id.tvTimer);

        if (isGameOver) {
            tvScore.setVisibility(View.VISIBLE);
            tvTimer.setVisibility("FUN".equals(gameMode) ? View.GONE : View.VISIBLE);
            if ("SCORE_ATTACK".equals(gameMode)) updateScoreAttackUI();
            else updateScoreText();
            updateTimerText(0L);
            tvScore.post(() -> {
                if (pendingResultMessage != null && pendingRecordText != null) {
                    showResultDialog(pendingResultMessage, pendingRecordText);
                }
            });
            return;
        }

        switch (gameMode) {
            case "TIME_ATTACK":
                tvScore.setVisibility(View.VISIBLE);
                tvTimer.setVisibility(View.VISIBLE);
                updateScoreText();
                long timeAttackDuration = remainingTimeMs > 0L ? remainingTimeMs : timeLimitMs;
                updateTimerText(timeAttackDuration / 1_000L);
                startGameTimer(timeAttackDuration);
                break;
            case "SCORE_ATTACK":
                tvScore.setVisibility(View.VISIBLE);
                tvTimer.setVisibility(View.VISIBLE);
                targetQuestionsForRound = 10 + (currentRound - 1) * 5;
                timeLimitForRoundSeconds = 30 + (currentRound - 1) * 10;
                updateScoreAttackUI();
                long roundDuration = remainingTimeMs > 0L
                        ? remainingTimeMs
                        : timeLimitForRoundSeconds * 1_000L;
                startGameTimer(roundDuration);
                break;
            default:
                tvScore.setVisibility(View.VISIBLE);
                tvTimer.setVisibility(View.GONE);
                updateScoreText();
                break;
        }
    }

    private void startScoreAttackRound() {
        targetQuestionsForRound = 10 + (currentRound - 1) * 5;
        timeLimitForRoundSeconds = 30 + (currentRound - 1) * 10;
        updateScoreAttackUI();
        startGameTimer(timeLimitForRoundSeconds * 1_000L);
    }

    private void updateScoreAttackUI() {
        tvScore.setText(getString(
                R.string.round_format,
                currentRound,
                roundScore,
                targetQuestionsForRound
        ));
    }

    protected void onQuestionCompleted() {
        if (isGameOver) return;
        score++;

        if ("SCORE_ATTACK".equals(gameMode)) {
            roundScore++;
            updateScoreAttackUI();
            if (roundScore >= targetQuestionsForRound) {
                if (countDownTimer != null) countDownTimer.cancel();
                currentRound++;
                roundScore = 0;
                int nextRoundSeconds = 30 + (currentRound - 1) * 10;
                Toast.makeText(
                        this,
                        getResources().getQuantityString(
                                R.plurals.next_round,
                                nextRoundSeconds,
                                currentRound,
                                nextRoundSeconds
                        ),
                        Toast.LENGTH_SHORT
                ).show();
                startScoreAttackRound();
            }
        } else {
            updateScoreText();
        }
    }

    protected void renderAnswerChoices(
            GridLayout container,
            List<AnswerChoice> choices,
            AnswerSelectionListener listener
    ) {
        container.removeAllViews();
        int columns = getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE ? choices.size() : 2;
        container.setColumnCount(columns);
        container.setRowCount((choices.size() + columns - 1) / columns);
        List<Integer> colors = Arrays.asList(
                R.color.answer_purple,
                R.color.answer_blue,
                R.color.answer_gold,
                R.color.answer_teal
        );
        int gap = getResources().getDimensionPixelSize(R.dimen.answer_option_gap);

        for (int index = 0; index < choices.size(); index++) {
            AnswerChoice choice = choices.get(index);
            MaterialCardView card = (MaterialCardView) getLayoutInflater().inflate(
                    R.layout.item_answer_choicer,
                    container,
                    false
            );
            card.setCardBackgroundColor(ContextCompat.getColor(this, colors.get(index % colors.size())));
            card.setTag(choice.getValue());
            card.setContentDescription(choice.getContentDescription());
            TextView label = card.findViewById(R.id.answerChoicerText);
            label.setText(choice.getLabel());
            boolean hasLetter = choice.getLabel().chars().anyMatch(Character::isLetter);
            float textSize = hasLetter && choice.getLabel().length() >= 11
                    ? 16f
                    : hasLetter ? 18f : 28f;
            label.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                    GridLayout.spec(index / columns),
                    GridLayout.spec(index % columns, 1f)
            );
            params.width = 0;
            params.height = getResources().getDimensionPixelSize(R.dimen.answer_option_min_height);
            params.setMargins(gap / 2, gap / 2, gap / 2, gap / 2);
            card.setLayoutParams(params);
            card.setOnClickListener(view -> listener.onSelected(choice, card));
            container.addView(card);
        }
    }

    protected void completeAnswerChoice(
            GridLayout container,
            MaterialCardView selectedCard,
            TextView feedback,
            String message,
            Runnable nextQuestion
    ) {
        for (int index = 0; index < container.getChildCount(); index++) {
            container.getChildAt(index).setEnabled(false);
        }
        selectedCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.answer_correct));
        selectedCard.setStrokeColor(ContextCompat.getColor(this, R.color.game_star));
        selectedCard.<TextView>findViewById(R.id.answerStateIcon).setVisibility(View.VISIBLE);
        celebrateQuestion(selectedCard, feedback, message, nextQuestion);
    }

    protected void retryAnswerChoice(MaterialCardView card, TextView feedback, String message) {
        int originalColor = card.getCardBackgroundColor().getDefaultColor();
        card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.answer_retry));
        showRetryFeedback(card, feedback, message);
        card.postDelayed(() -> {
            if (card.isEnabled() && !isFinishing()) card.setCardBackgroundColor(originalColor);
        }, 550L);
    }

    protected void restoreCompletedAnswerChoice(
            GridLayout container,
            int correctValue,
            TextView feedback,
            String message,
            Runnable nextQuestion
    ) {
        for (int index = 0; index < container.getChildCount(); index++) {
            View child = container.getChildAt(index);
            if (!(child instanceof MaterialCardView)) continue;
            MaterialCardView card = (MaterialCardView) child;
            card.setEnabled(false);
            if (Integer.valueOf(correctValue).equals(card.getTag())) {
                card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.answer_correct));
                card.setStrokeColor(ContextCompat.getColor(this, R.color.game_star));
                card.<TextView>findViewById(R.id.answerStateIcon).setVisibility(View.VISIBLE);
            }
        }
        restoreCompletedQuestion(feedback, message, nextQuestion);
    }

    protected void restoreCompletedQuestion(
            TextView feedback,
            String message,
            Runnable nextQuestion
    ) {
        feedback.setText(message);
        feedback.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        feedback.setTextColor(ContextCompat.getColor(this, R.color.answer_correct));
        feedback.setVisibility(View.VISIBLE);
        if (!isGameOver) {
            feedback.postDelayed(() -> {
                if (!isGameOver && !isFinishing()) nextQuestion.run();
            }, 500L);
        }
    }

    protected void celebrateQuestion(
            View anchor,
            TextView feedback,
            String message,
            Runnable nextQuestion
    ) {
        feedback.setText(message);
        feedback.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        feedback.setTextColor(ContextCompat.getColor(this, R.color.answer_correct));
        feedback.setVisibility(View.VISIBLE);
        feedback.setAlpha(0f);
        feedback.setScaleX(0.82f);
        feedback.setScaleY(0.82f);
        feedback.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(220L).start();
        anchor.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        anchor.animate().scaleX(1.06f).scaleY(1.06f).setDuration(140L).withEndAction(() ->
                anchor.animate().scaleX(1f).scaleY(1f).setDuration(140L).start()
        ).start();
        onQuestionCompleted();

        long delay = "FUN".equals(gameMode) ? 900L : 650L;
        feedback.postDelayed(() -> {
            if (!isGameOver && !isFinishing()) nextQuestion.run();
        }, delay);
    }

    protected void showRetryFeedback(View anchor, TextView feedback, String message) {
        feedback.setText(message);
        feedback.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        feedback.setTextColor(ContextCompat.getColor(this, R.color.answer_retry));
        feedback.setVisibility(View.VISIBLE);
        anchor.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
        anchor.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
    }

    protected void resetFeedback(TextView feedback) {
        feedback.clearAnimation();
        feedback.setVisibility(View.INVISIBLE);
        feedback.setAlpha(1f);
        feedback.setScaleX(1f);
        feedback.setScaleY(1f);
    }

    private void updateScoreText() {
        tvScore.setText(getString(R.string.score_format, score));
    }

    private void updateTimerText(long seconds) {
        tvTimer.setText(getString(R.string.time_format, seconds));
    }

    private void startGameTimer(long durationMs) {
        if (countDownTimer != null) countDownTimer.cancel();
        remainingTimeMs = durationMs;
        countDownTimer = new CountDownTimer(durationMs, 1_000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingTimeMs = millisUntilFinished;
                updateTimerText(millisUntilFinished / 1_000L);
            }

            @Override
            public void onFinish() {
                remainingTimeMs = 0L;
                updateTimerText(0L);
                endGame();
            }
        }.start();
    }

    private String getHighScoreKey() {
        String gameName = getClass().getSimpleName();
        return "TIME_ATTACK".equals(gameMode)
                ? gameName + "_TA_" + timeLimitMs
                : gameName + "_SA";
    }

    private void endGame() {
        if (isGameOver) return;
        isGameOver = true;
        if (countDownTimer != null) countDownTimer.cancel();

        String key = getHighScoreKey();
        int highScore = prefs.getInt(key, 0);
        int finalScore = "SCORE_ATTACK".equals(gameMode) ? currentRound : score;
        boolean newHighScore = finalScore > highScore;
        saveHistoryRecord(finalScore);
        if (newHighScore) prefs.edit().putInt(key, finalScore).apply();

        String recordText = newHighScore
                ? getString(R.string.new_high_score)
                : getString(R.string.best_score, highScore);
        String message;
        switch (gameMode) {
            case "TIME_ATTACK":
                message = getResources().getQuantityString(R.plurals.time_attack_result, score, score);
                break;
            case "SCORE_ATTACK":
                message = getString(R.string.round_rush_result, currentRound);
                break;
            default:
                message = getString(R.string.great_job);
                break;
        }
        pendingResultMessage = message;
        pendingRecordText = recordText;
        showResultDialog(message, recordText);
    }

    private void showResultDialog(String message, String recordText) {
        if (isFinishing() || resultDialog != null && resultDialog.isShowing()) return;
        Dialog dialog = new Dialog(this, R.style.FullScreenDialogTheme);
        resultDialog = dialog;
        dialog.setContentView(R.layout.dialog_game_result);
        dialog.setCancelable(false);
        dialog.<TextView>findViewById(R.id.tvResultMessage).setText(message);
        dialog.<TextView>findViewById(R.id.tvResultRecord).setText(recordText);
        dialog.<Button>findViewById(R.id.btnMainMenu).setOnClickListener(view -> {
            dialog.dismiss();
            finish();
        });
        dialog.<Button>findViewById(R.id.btnPlayAgain).setOnClickListener(view -> {
            dialog.dismiss();
            Intent replayIntent = new Intent(getIntent());
            finish();
            startActivity(replayIntent);
        });
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    private void saveHistoryRecord(int finalScore) {
        if (historySaved) return;
        HistoryRecord record = new HistoryRecord(
                getClass().getSimpleName().replace("Activity", ""),
                gameMode,
                finalScore,
                System.currentTimeMillis()
        );
        HistoryManager.saveRecord(this, record);
        historySaved = true;
    }

    @Override
    protected void onDestroy() {
        if (countDownTimer != null) countDownTimer.cancel();
        if (isFinishing() && "FUN".equals(gameMode) && score > 0) saveHistoryRecord(score);
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_SCORE, score);
        outState.putInt(STATE_ROUND, currentRound);
        outState.putInt(STATE_ROUND_SCORE, roundScore);
        outState.putLong(STATE_TIME_REMAINING, remainingTimeMs);
        outState.putBoolean(STATE_HISTORY_SAVED, historySaved);
        outState.putBoolean(STATE_GAME_OVER, isGameOver);
        outState.putString(STATE_RESULT_MESSAGE, pendingResultMessage);
        outState.putString(STATE_RESULT_RECORD, pendingRecordText);
        super.onSaveInstanceState(outState);
    }
}
