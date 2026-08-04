package com.uccd3223.p1_chai_boon_hong_2206806;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;
import com.uccd3223.p1_chai_boon_hong_2206806.util.ExerciseGeneratorUtil;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class AssociationActivity extends BaseGameActivity {
    private enum CountTheme {
        APPLE(R.drawable.game_apple, R.string.count_apples_prompt, R.string.count_object_apple, null),
        STAR(R.drawable.ic_count_star, R.string.count_stars_prompt, R.string.count_object_star, null),
        BALLOON(
                R.drawable.ic_balloon,
                R.string.count_balloons_prompt,
                R.string.count_object_balloon,
                R.color.game_balloon_pink
        );

        final int drawableRes;
        final int promptRes;
        final int objectNameRes;
        final Integer tintRes;

        CountTheme(int drawableRes, int promptRes, int objectNameRes, Integer tintRes) {
            this.drawableRes = drawableRes;
            this.promptRes = promptRes;
            this.objectNameRes = objectNameRes;
            this.tintRes = tintRes;
        }
    }

    private static final String STATE_TARGET_COUNT = "association_target_count";
    private static final String STATE_LAST_TARGET_COUNT = "association_last_target_count";
    private static final String STATE_THEME = "association_theme";
    private static final String STATE_LAST_THEME = "association_last_theme";
    private static final String STATE_OPTIONS = "association_options";
    private static final String STATE_QUESTION_LOCKED = "association_question_locked";

    private GridLayout objectsGridLayout;
    private GridLayout optionsContainerLayout;
    private TextView tvInstruction;
    private TextView tvFeedback;
    private int targetCount;
    private int lastTargetCount = -1;
    private CountTheme currentTheme = CountTheme.APPLE;
    private int lastThemeOrdinal = -1;
    private List<Integer> currentOptions = Collections.emptyList();
    private boolean questionLocked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_association);
        setupGameModeUI();
        initializeUI();
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_TARGET_COUNT)) {
            restoreQuestion(savedInstanceState);
        } else {
            loadNextQuestion();
        }
    }

    private void initializeUI() {
        objectsGridLayout = findViewById(R.id.objectsGridLayout);
        optionsContainerLayout = findViewById(R.id.optionsContainerLayout);
        tvInstruction = findViewById(R.id.tvInstruction);
        tvFeedback = findViewById(R.id.tvFeedback);
        findViewById(R.id.btnBack).setOnClickListener(view -> finish());
    }

    private void loadNextQuestion() {
        questionLocked = false;
        resetFeedback(tvFeedback);
        objectsGridLayout.removeAllViews();
        do {
            targetCount = ExerciseGeneratorUtil.generateObjectCount(9);
            CountTheme[] themes = CountTheme.values();
            currentTheme = themes[ThreadLocalRandom.current().nextInt(themes.length)];
        } while (targetCount == lastTargetCount && currentTheme.ordinal() == lastThemeOrdinal);

        lastTargetCount = targetCount;
        lastThemeOrdinal = currentTheme.ordinal();
        currentOptions = ExerciseGeneratorUtil
                .generateRecognitionOptions(targetCount, 9, 4)
                .getOptions();
        renderQuestion();
    }

    private void restoreQuestion(Bundle state) {
        targetCount = state.getInt(STATE_TARGET_COUNT);
        lastTargetCount = state.getInt(STATE_LAST_TARGET_COUNT, targetCount);
        int themeIndex = Math.max(0, Math.min(CountTheme.values().length - 1, state.getInt(STATE_THEME)));
        currentTheme = CountTheme.values()[themeIndex];
        lastThemeOrdinal = state.getInt(STATE_LAST_THEME, currentTheme.ordinal());
        questionLocked = state.getBoolean(STATE_QUESTION_LOCKED);
        ArrayList<Integer> restoredOptions = state.getIntegerArrayList(STATE_OPTIONS);
        currentOptions = restoredOptions == null ? Collections.emptyList() : restoredOptions;
        if (currentOptions.size() != 4) {
            currentOptions = ExerciseGeneratorUtil
                    .generateRecognitionOptions(targetCount, 9, 4)
                    .getOptions();
        }
        renderQuestion();
        if (questionLocked) {
            restoreCompletedAnswerChoice(
                    optionsContainerLayout,
                    targetCount,
                    tvFeedback,
                    getString(R.string.count_success, targetCount),
                    this::loadNextQuestion
            );
        }
    }

    private void renderQuestion() {
        tvInstruction.setText(currentTheme.promptRes);
        renderObjectsUI();
        renderOptionsUI();
    }

    private void renderObjectsUI() {
        objectsGridLayout.removeAllViews();
        float density = getResources().getDisplayMetrics().density;
        int size = (int) (60 * density);
        int margin = (int) (7 * density);
        List<Integer> activeIndices = getActiveIndices(targetCount);

        for (int index = 0; index < 9; index++) {
            boolean active = activeIndices.contains(index);
            ImageView itemView = new ImageView(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = size;
            params.height = size;
            params.setMargins(margin, margin, margin, margin);
            itemView.setLayoutParams(params);
            itemView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            if (active) {
                itemView.setImageResource(currentTheme.drawableRes);
                if (currentTheme.tintRes != null) {
                    itemView.setImageTintList(ColorStateList.valueOf(
                            ContextCompat.getColor(this, currentTheme.tintRes)
                    ));
                }
                itemView.setContentDescription(getString(currentTheme.objectNameRes));
                itemView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
            } else {
                itemView.setVisibility(View.INVISIBLE);
                itemView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            }
            objectsGridLayout.addView(itemView);
        }
    }

    private List<Integer> getActiveIndices(int count) {
        switch (count) {
            case 1: return Collections.singletonList(4);
            case 2: return Arrays.asList(3, 5);
            case 3: return Arrays.asList(3, 4, 5);
            case 4: return Arrays.asList(0, 2, 6, 8);
            case 5: return Arrays.asList(0, 2, 4, 6, 8);
            case 6: return Arrays.asList(0, 2, 3, 5, 6, 8);
            case 7: return Arrays.asList(0, 2, 3, 4, 5, 6, 8);
            case 8: return Arrays.asList(0, 1, 2, 3, 5, 6, 7, 8);
            case 9: return Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8);
            default: return Collections.emptyList();
        }
    }

    private void renderOptionsUI() {
        List<AnswerChoice> choices = new ArrayList<>();
        for (int option : currentOptions) {
            choices.add(new AnswerChoice(
                    option,
                    getString(R.string.number_value, option),
                    getString(R.string.answer_number, option)
            ));
        }
        renderAnswerChoices(optionsContainerLayout, choices, (choice, card) ->
                checkAnswer(choice.getValue(), card));
    }

    private void checkAnswer(int selectedOption, MaterialCardView card) {
        if (questionLocked || isGameOver) return;
        if (selectedOption == targetCount) {
            questionLocked = true;
            completeAnswerChoice(
                    optionsContainerLayout,
                    card,
                    tvFeedback,
                    getString(R.string.count_success, targetCount),
                    this::loadNextQuestion
            );
        } else {
            retryAnswerChoice(card, tvFeedback, getString(R.string.count_retry));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_TARGET_COUNT, targetCount);
        outState.putInt(STATE_LAST_TARGET_COUNT, lastTargetCount);
        outState.putInt(STATE_THEME, currentTheme.ordinal());
        outState.putInt(STATE_LAST_THEME, lastThemeOrdinal);
        outState.putIntegerArrayList(STATE_OPTIONS, new ArrayList<>(currentOptions));
        outState.putBoolean(STATE_QUESTION_LOCKED, questionLocked);
        super.onSaveInstanceState(outState);
    }
}
