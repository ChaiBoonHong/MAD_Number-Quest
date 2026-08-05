package com.uccd3223.p1_chai_boon_hong_2206806;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
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
import androidx.activity.OnBackPressedCallback;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;

import java.util.Arrays;
import java.util.List;

public class BaseGameActivity extends AppCompatActivity {
    protected interface AnswerSelectionListener { void onSelected(AnswerChoice choice, MaterialCardView card); }

    private static final String STATE_SCORE = "state_score";
    private static final String STATE_ROUND = "state_round";
    private static final String STATE_ROUND_SCORE = "state_round_score";
    private static final String STATE_TIME_REMAINING = "state_time_remaining";
    private static final String STATE_HISTORY_SAVED = "state_history_saved";
    private static final String STATE_GAME_OVER = "state_game_over";
    private static final String STATE_RESULT_MESSAGE = "state_result_message";
    private static final String STATE_RESULT_RECORD = "state_result_record";
    private static final String STATE_PAUSED = "state_paused";
    private static final String STATE_PENDING_DELAY = "state_pending_delay";
    private static final String STATE_RECORD_CELEBRATED = "state_record_celebrated";

    protected String gameMode = "FUN";
    protected int score;
    protected boolean isGameOver;

    private TextView tvScore;
    private TextView tvTimer;
    private CountDownTimer countDownTimer;
    private long timeLimitMs = 60_000L;
    private long remainingTimeMs;
    private long timerDeadline;
    private int currentRound = 1;
    private int targetQuestionsForRound = 10;
    private int timeLimitForRoundSeconds = 30;
    private int roundScore;
    private boolean historySaved;
    private boolean isGamePaused;
    private boolean recordCelebrated;
    private String pendingResultMessage;
    private String pendingRecordText;
    private Dialog resultDialog;
    private Dialog pauseDialog;
    private Runnable pendingTransition;
    private View pendingHost;
    private long pendingTransitionDue;
    private long restoredPendingDelay;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String requestedMode = getIntent().getStringExtra("GAME_MODE");
        gameMode = requestedMode == null ? "FUN" : requestedMode;
        timeLimitMs = getIntent().getLongExtra("TIME_LIMIT", 60_000L);
        if (savedInstanceState != null) {
            score = savedInstanceState.getInt(STATE_SCORE);
            currentRound = savedInstanceState.getInt(STATE_ROUND, 1);
            roundScore = savedInstanceState.getInt(STATE_ROUND_SCORE);
            remainingTimeMs = savedInstanceState.getLong(STATE_TIME_REMAINING);
            historySaved = savedInstanceState.getBoolean(STATE_HISTORY_SAVED);
            isGameOver = savedInstanceState.getBoolean(STATE_GAME_OVER);
            pendingResultMessage = savedInstanceState.getString(STATE_RESULT_MESSAGE);
            pendingRecordText = savedInstanceState.getString(STATE_RESULT_RECORD);
            isGamePaused = savedInstanceState.getBoolean(STATE_PAUSED);
            restoredPendingDelay = savedInstanceState.getLong(STATE_PENDING_DELAY);
            recordCelebrated = savedInstanceState.getBoolean(STATE_RECORD_CELEBRATED);
        }
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                if (shouldConfirmExit(score)) pauseGame(); else finish();
            }
        });
    }

    protected void setupGameModeUI() {
        tvScore = findViewById(R.id.tvScore);
        tvTimer = findViewById(R.id.tvTimer);
        findViewById(R.id.btnPause).setOnClickListener(view -> pauseGame());
        targetQuestionsForRound = 10 + (currentRound - 1) * 5;
        timeLimitForRoundSeconds = 30 + (currentRound - 1) * 10;
        tvScore.setVisibility(View.VISIBLE);
        tvTimer.setVisibility("FUN".equals(gameMode) ? View.GONE : View.VISIBLE);
        updateScoreDisplay();

        if (isGameOver) {
            updateTimerText(0L);
            tvScore.post(() -> showResultDialog(pendingResultMessage, pendingRecordText));
            return;
        }
        if ("TIME_ATTACK".equals(gameMode)) {
            long duration = remainingTimeMs > 0 ? remainingTimeMs : timeLimitMs;
            remainingTimeMs = duration;
            updateTimerText((duration + 999L) / 1_000L);
            if (!isGamePaused) startGameTimer(duration);
        } else if ("SCORE_ATTACK".equals(gameMode)) {
            long duration = remainingTimeMs > 0 ? remainingTimeMs : timeLimitForRoundSeconds * 1_000L;
            remainingTimeMs = duration;
            updateTimerText((duration + 999L) / 1_000L);
            if (!isGamePaused) startGameTimer(duration);
        }
    }

    protected boolean canAcceptInput() { return !isGamePaused && !isGameOver; }

    protected boolean onQuestionCompleted() {
        if (!canAcceptInput()) return false;
        boolean playedRecord = false;
        score++;
        if ("SCORE_ATTACK".equals(gameMode)) {
            roundScore++;
            if (roundScore >= targetQuestionsForRound) {
                cancelTimerPreservingTime();
                currentRound++;
                roundScore = 0;
                int seconds = 30 + (currentRound - 1) * 10;
                Toast.makeText(this, getResources().getQuantityString(R.plurals.next_round, seconds, currentRound, seconds), Toast.LENGTH_SHORT).show();
                targetQuestionsForRound = 10 + (currentRound - 1) * 5;
                timeLimitForRoundSeconds = seconds;
                startGameTimer(seconds * 1_000L);
            }
        } else if ("FUN".equals(gameMode)) {
            boolean newRecord = ProgressManager.updateBest(this, getClass().getSimpleName(), gameMode, timeLimitMs, score);
            if (newRecord && !recordCelebrated) {
                recordCelebrated = true;
                GameAudioManager.get(this).playRecord();
                playedRecord = true;
            }
        }
        updateScoreDisplay();
        return playedRecord;
    }

    private int currentBestDisplay() {
        int saved = ProgressManager.getBest(this, getClass().getSimpleName(), gameMode, timeLimitMs);
        int active = "SCORE_ATTACK".equals(gameMode) ? currentRound : score;
        return Math.max(saved, active);
    }

    private void updateScoreDisplay() {
        int saved = ProgressManager.getBest(this, getClass().getSimpleName(), gameMode, timeLimitMs);
        int shownBest = currentBestDisplay();
        String best = saved == ProgressManager.NO_SCORE && shownBest == 0
                ? getString(R.string.best_none)
                : ("SCORE_ATTACK".equals(gameMode) ? getString(R.string.best_round, shownBest) : String.valueOf(shownBest));
        if ("SCORE_ATTACK".equals(gameMode)) {
            tvScore.setText(getString(R.string.round_with_best, currentRound, roundScore, targetQuestionsForRound, best));
        } else {
            tvScore.setText(getString(R.string.score_with_best, score, best));
        }
    }

    protected void renderAnswerChoices(GridLayout container, List<AnswerChoice> choices, AnswerSelectionListener listener) {
        container.removeAllViews();
        int columns = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? choices.size() : 2;
        container.setColumnCount(columns);
        container.setRowCount((choices.size() + columns - 1) / columns);
        List<Integer> colors = Arrays.asList(R.color.answer_purple, R.color.answer_blue, R.color.answer_gold, R.color.answer_teal);
        int gap = getResources().getDimensionPixelSize(R.dimen.answer_option_gap);
        for (int index = 0; index < choices.size(); index++) {
            AnswerChoice choice = choices.get(index);
            MaterialCardView card = (MaterialCardView) getLayoutInflater().inflate(R.layout.item_answer_choicer, container, false);
            card.setCardBackgroundColor(ContextCompat.getColor(this, colors.get(index % colors.size())));
            card.setTag(choice.getValue());
            card.setContentDescription(choice.getContentDescription());
            TextView label = card.findViewById(R.id.answerChoicerText);
            label.setText(choice.getLabel());
            boolean hasLetter = choice.getLabel().chars().anyMatch(Character::isLetter);
            label.setTextSize(TypedValue.COMPLEX_UNIT_SP, hasLetter && choice.getLabel().length() >= 11 ? 16f : hasLetter ? 18f : 28f);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams(GridLayout.spec(index / columns), GridLayout.spec(index % columns, 1f));
            params.width = 0;
            params.height = getResources().getDimensionPixelSize(R.dimen.answer_option_min_height);
            params.setMargins(gap / 2, gap / 2, gap / 2, gap / 2);
            card.setLayoutParams(params);
            card.setOnClickListener(view -> { if (canAcceptInput()) listener.onSelected(choice, card); });
            container.addView(card);
        }
    }

    protected void completeAnswerChoice(GridLayout container, MaterialCardView selectedCard, TextView feedback, String message, Runnable nextQuestion) {
        if (!canAcceptInput()) return;
        for (int i = 0; i < container.getChildCount(); i++) container.getChildAt(i).setEnabled(false);
        selectedCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.answer_correct));
        selectedCard.setStrokeColor(ContextCompat.getColor(this, R.color.game_star));
        selectedCard.<TextView>findViewById(R.id.answerStateIcon).setVisibility(View.VISIBLE);
        celebrateQuestion(selectedCard, feedback, message, nextQuestion);
    }

    protected void retryAnswerChoice(MaterialCardView card, TextView feedback, String message) {
        if (!canAcceptInput()) return;
        int originalColor = card.getCardBackgroundColor().getDefaultColor();
        card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.answer_retry));
        showRetryFeedback(card, feedback, message);
        card.postDelayed(() -> { if (card.isEnabled() && !isFinishing()) card.setCardBackgroundColor(originalColor); }, 550L);
    }

    protected void restoreCompletedAnswerChoice(GridLayout container, int correctValue, TextView feedback, String message, Runnable nextQuestion) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof MaterialCardView) {
                MaterialCardView card = (MaterialCardView) child;
                card.setEnabled(false);
                if (Integer.valueOf(correctValue).equals(card.getTag())) {
                    card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.answer_correct));
                    card.setStrokeColor(ContextCompat.getColor(this, R.color.game_star));
                    card.<TextView>findViewById(R.id.answerStateIcon).setVisibility(View.VISIBLE);
                }
            }
        }
        restoreCompletedQuestion(feedback, message, nextQuestion);
    }

    protected void restoreCompletedQuestion(TextView feedback, String message, Runnable nextQuestion) {
        showPositiveFeedback(feedback, message);
        if (!isGameOver) scheduleTransition(feedback, nextQuestion, restoredPendingDelay > 0 ? restoredPendingDelay : 500L);
        restoredPendingDelay = 0L;
    }

    protected void celebrateQuestion(View anchor, TextView feedback, String message, Runnable nextQuestion) {
        if (!canAcceptInput()) return;
        showPositiveFeedback(feedback, message);
        feedback.setAlpha(0f); feedback.setScaleX(.82f); feedback.setScaleY(.82f);
        feedback.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(220L).start();
        if (GameSettingsManager.isVibrationEnabled(this)) anchor.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        anchor.animate().scaleX(1.06f).scaleY(1.06f).setDuration(140L).withEndAction(() -> anchor.animate().scaleX(1f).scaleY(1f).setDuration(140L).start()).start();
        if (!onQuestionCompleted()) GameAudioManager.get(this).playCorrect();
        scheduleTransition(feedback, nextQuestion, "FUN".equals(gameMode) ? 900L : 650L);
    }

    protected void showRetryFeedback(View anchor, TextView feedback, String message) {
        feedback.setText(message);
        feedback.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        feedback.setTextColor(ContextCompat.getColor(this, R.color.answer_retry));
        feedback.setVisibility(View.VISIBLE);
        if (GameSettingsManager.isVibrationEnabled(this)) anchor.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
        GameAudioManager.get(this).playIncorrect();
        anchor.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
    }

    private void showPositiveFeedback(TextView feedback, String message) {
        feedback.setText(message);
        feedback.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        feedback.setTextColor(ContextCompat.getColor(this, R.color.answer_correct));
        feedback.setVisibility(View.VISIBLE);
    }

    protected void resetFeedback(TextView feedback) {
        feedback.clearAnimation(); feedback.setVisibility(View.INVISIBLE); feedback.setAlpha(1f); feedback.setScaleX(1f); feedback.setScaleY(1f);
    }

    private void scheduleTransition(View host, Runnable runnable, long delay) {
        clearPendingTransition();
        pendingHost = host;
        pendingTransition = () -> {
            Runnable action = pendingTransition;
            clearPendingTransition();
            if (!isGameOver && !isFinishing() && !isGamePaused && action != null) runnable.run();
        };
        pendingTransitionDue = isGamePaused ? delay : SystemClock.elapsedRealtime() + delay;
        if (!isGamePaused) host.postDelayed(pendingTransition, delay);
    }

    private void clearPendingTransition() {
        if (pendingHost != null && pendingTransition != null) pendingHost.removeCallbacks(pendingTransition);
        pendingHost = null; pendingTransition = null; pendingTransitionDue = 0L;
    }

    private long pendingDelay() {
        if (pendingTransition == null) return 0L;
        return isGamePaused ? Math.max(1L, pendingTransitionDue)
                : Math.max(1L, pendingTransitionDue - SystemClock.elapsedRealtime());
    }

    private void updateTimerText(long seconds) { if (tvTimer != null) tvTimer.setText(getString(R.string.time_format, seconds)); }

    private void startGameTimer(long durationMs) {
        if (isGamePaused || isGameOver) return;
        if (countDownTimer != null) countDownTimer.cancel();
        remainingTimeMs = durationMs;
        timerDeadline = SystemClock.elapsedRealtime() + durationMs;
        countDownTimer = new CountDownTimer(durationMs, 250L) {
            @Override public void onTick(long millis) {
                remainingTimeMs = Math.max(0L, timerDeadline - SystemClock.elapsedRealtime());
                updateTimerText((remainingTimeMs + 999L) / 1_000L);
            }
            @Override public void onFinish() { remainingTimeMs = 0L; updateTimerText(0L); endGame(); }
        }.start();
    }

    private void cancelTimerPreservingTime() {
        if (countDownTimer != null) {
            remainingTimeMs = Math.max(0L, timerDeadline - SystemClock.elapsedRealtime());
            countDownTimer.cancel(); countDownTimer = null;
        }
    }

    protected void pauseGame() {
        if (isGameOver || isFinishing()) return;
        if (!isGamePaused) {
            long transitionDelay = pendingTransition == null ? 0L
                    : Math.max(1L, pendingTransitionDue - SystemClock.elapsedRealtime());
            isGamePaused = true;
            cancelTimerPreservingTime();
            if (pendingHost != null && pendingTransition != null) {
                pendingHost.removeCallbacks(pendingTransition);
                pendingTransitionDue = transitionDelay;
            }
        }
        if (hasWindowFocus()) showPauseDialog();
    }

    private void showPauseDialog() {
        if (pauseDialog != null && pauseDialog.isShowing() || isGameOver || isFinishing()) return;
        Dialog dialog = new Dialog(this, R.style.FullScreenDialogTheme);
        pauseDialog = dialog;
        dialog.setContentView(R.layout.dialog_game_pause);
        dialog.setCancelable(false);
        dialog.findViewById(R.id.btnResume).setOnClickListener(view -> resumeGame());
        dialog.findViewById(R.id.btnPauseSettings).setOnClickListener(view -> startActivity(new Intent(this, SettingsActivity.class)));
        dialog.findViewById(R.id.btnPauseHome).setOnClickListener(view -> showHomeConfirmation());
        showFullScreen(dialog);
        dialog.findViewById(R.id.btnResume).sendAccessibilityEvent(android.view.accessibility.AccessibilityEvent.TYPE_VIEW_FOCUSED);
    }

    private void resumeGame() {
        if (pauseDialog != null) pauseDialog.dismiss();
        pauseDialog = null;
        isGamePaused = false;
        if (!"FUN".equals(gameMode) && remainingTimeMs > 0) startGameTimer(remainingTimeMs);
        if (pendingHost != null && pendingTransition != null) {
            long delay = pendingTransitionDue;
            pendingTransitionDue = SystemClock.elapsedRealtime() + delay;
            pendingHost.postDelayed(pendingTransition, delay);
        }
    }

    private void showHomeConfirmation() {
        if (!shouldConfirmExit(score)) {
            finish();
            return;
        }
        Dialog dialog = new Dialog(this, R.style.FullScreenDialogTheme);
        dialog.setContentView(R.layout.dialog_confirmation);
        dialog.<TextView>findViewById(R.id.tvConfirmTitle).setText(R.string.leave_game_title);
        dialog.<TextView>findViewById(R.id.tvConfirmMessage).setText(
                "FUN".equals(gameMode) ? R.string.leave_fun_message : R.string.leave_game_message);
        dialog.<Button>findViewById(R.id.btnConfirmPositive).setText(R.string.go_home);
        dialog.<Button>findViewById(R.id.btnConfirmNegative).setText(R.string.keep_playing);
        dialog.findViewById(R.id.btnConfirmPositive).setOnClickListener(view -> { dialog.dismiss(); finish(); });
        dialog.findViewById(R.id.btnConfirmNegative).setOnClickListener(view -> dialog.dismiss());
        showFullScreen(dialog);
    }

    static boolean shouldConfirmExit(int currentScore) {
        return currentScore > 0;
    }

    private void endGame() {
        if (isGameOver || isGamePaused) return;
        isGameOver = true;
        cancelTimerPreservingTime(); clearPendingTransition();
        int finalScore = "SCORE_ATTACK".equals(gameMode) ? currentRound : score;
        int previous = ProgressManager.getBest(this, getClass().getSimpleName(), gameMode, timeLimitMs);
        boolean newHigh = ProgressManager.updateBest(this, getClass().getSimpleName(), gameMode, timeLimitMs, finalScore);
        saveHistoryRecord(finalScore);
        if (newHigh && !recordCelebrated) { recordCelebrated = true; GameAudioManager.get(this).playRecord(); }
        pendingRecordText = newHigh ? getString(R.string.new_high_score)
                : getString(R.string.best_score, previous == ProgressManager.NO_SCORE ? finalScore : previous);
        if ("TIME_ATTACK".equals(gameMode)) pendingResultMessage = getResources().getQuantityString(R.plurals.time_attack_result, score, score);
        else pendingResultMessage = getString(R.string.round_rush_result, currentRound);
        showResultDialog(pendingResultMessage, pendingRecordText);
    }

    private void showResultDialog(String message, String recordText) {
        if (message == null || recordText == null || isFinishing() || resultDialog != null && resultDialog.isShowing()) return;
        Dialog dialog = new Dialog(this, R.style.FullScreenDialogTheme);
        resultDialog = dialog; dialog.setContentView(R.layout.dialog_game_result); dialog.setCancelable(false);
        dialog.<TextView>findViewById(R.id.tvResultMessage).setText(message);
        dialog.<TextView>findViewById(R.id.tvResultRecord).setText(recordText);
        dialog.<Button>findViewById(R.id.btnMainMenu).setOnClickListener(view -> { dialog.dismiss(); finish(); });
        dialog.<Button>findViewById(R.id.btnPlayAgain).setOnClickListener(view -> { dialog.dismiss(); Intent replay = new Intent(getIntent()); finish(); startActivity(replay); });
        showFullScreen(dialog);
    }

    private void showFullScreen(Dialog dialog) {
        dialog.show(); Window window = dialog.getWindow();
        if (window != null) { window.setBackgroundDrawableResource(android.R.color.transparent); window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT); }
    }

    private void saveHistoryRecord(int finalScore) {
        if (historySaved) return;
        HistoryManager.saveRecord(this, new HistoryRecord(getClass().getSimpleName().replace("Activity", ""), gameMode, finalScore, System.currentTimeMillis(), "TIME_ATTACK".equals(gameMode) ? timeLimitMs : 0L));
        historySaved = true;
    }

    @Override protected void onResume() {
        super.onResume();
        if (isGamePaused && !isGameOver) getWindow().getDecorView().post(this::showPauseDialog);
    }

    @Override protected void onStop() {
        if (!isChangingConfigurations() && !isGameOver) pauseGame();
        super.onStop();
    }

    @Override protected void onDestroy() {
        cancelTimerPreservingTime(); clearPendingTransition();
        if (isFinishing() && "FUN".equals(gameMode) && score > 0) saveHistoryRecord(score);
        super.onDestroy();
    }

    @Override protected void onSaveInstanceState(Bundle out) {
        cancelTimerPreservingTime();
        out.putInt(STATE_SCORE, score); out.putInt(STATE_ROUND, currentRound); out.putInt(STATE_ROUND_SCORE, roundScore);
        out.putLong(STATE_TIME_REMAINING, remainingTimeMs); out.putBoolean(STATE_HISTORY_SAVED, historySaved);
        out.putBoolean(STATE_GAME_OVER, isGameOver); out.putString(STATE_RESULT_MESSAGE, pendingResultMessage); out.putString(STATE_RESULT_RECORD, pendingRecordText);
        out.putBoolean(STATE_PAUSED, isGamePaused); out.putLong(STATE_PENDING_DELAY, pendingDelay()); out.putBoolean(STATE_RECORD_CELEBRATED, recordCelebrated);
        super.onSaveInstanceState(out);
    }
}
