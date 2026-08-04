package com.uccd3223.p1_chai_boon_hong_2206806;

import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.card.MaterialCardView;
import com.uccd3223.p1_chai_boon_hong_2206806.util.ExerciseGeneratorUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PlaceValueActivity extends BaseGameActivity {
    private static final String STATE_TENS = "place_value_tens";
    private static final String STATE_ONES = "place_value_ones";
    private static final String STATE_LAST_TOTAL = "place_value_last_total";
    private static final String STATE_OPTIONS = "place_value_options";
    private static final String STATE_QUESTION_LOCKED = "place_value_question_locked";

    private FlexboxLayout tensContainerLayout;
    private FlexboxLayout onesContainerLayout;
    private GridLayout optionsContainerLayout;
    private TextView tvTensCount;
    private TextView tvOnesCount;
    private TextView tvFeedback;
    private ExerciseGeneratorUtil.PlaceValueData currentData;
    private List<Integer> currentOptions = Collections.emptyList();
    private int lastTotal = -1;
    private boolean questionLocked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_value);
        setupGameModeUI();
        initializeUI();
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_TENS)) {
            restoreQuestion(savedInstanceState);
        } else {
            loadNextQuestion();
        }
    }

    private void initializeUI() {
        tensContainerLayout = findViewById(R.id.tensContainerLayout);
        onesContainerLayout = findViewById(R.id.onesContainerLayout);
        optionsContainerLayout = findViewById(R.id.optionsContainerLayout);
        tvTensCount = findViewById(R.id.tvTensCount);
        tvOnesCount = findViewById(R.id.tvOnesCount);
        tvFeedback = findViewById(R.id.tvFeedback);
        findViewById(R.id.btnBack).setOnClickListener(view -> finish());
    }

    private void loadNextQuestion() {
        questionLocked = false;
        resetFeedback(tvFeedback);
        do {
            currentData = ExerciseGeneratorUtil.generatePlaceValue(9);
        } while (currentData.getTotal() == lastTotal);
        lastTotal = currentData.getTotal();
        currentOptions = ExerciseGeneratorUtil
                .generateRecognitionOptions(currentData.getTotal(), 99, 4)
                .getOptions();
        renderQuestion();
    }

    private void restoreQuestion(Bundle state) {
        currentData = new ExerciseGeneratorUtil.PlaceValueData(
                state.getInt(STATE_TENS),
                state.getInt(STATE_ONES)
        );
        lastTotal = state.getInt(STATE_LAST_TOTAL, currentData.getTotal());
        questionLocked = state.getBoolean(STATE_QUESTION_LOCKED);
        ArrayList<Integer> restoredOptions = state.getIntegerArrayList(STATE_OPTIONS);
        currentOptions = restoredOptions == null ? Collections.emptyList() : restoredOptions;
        if (currentOptions.size() != 4) {
            currentOptions = ExerciseGeneratorUtil
                    .generateRecognitionOptions(currentData.getTotal(), 99, 4)
                    .getOptions();
        }
        renderQuestion();
        if (questionLocked) {
            restoreCompletedAnswerChoice(
                    optionsContainerLayout,
                    currentData.getTotal(),
                    tvFeedback,
                    successMessage(),
                    this::loadNextQuestion
            );
        }
    }

    private void renderQuestion() {
        renderVisualUI();
        renderOptionsUI();
    }

    private void renderVisualUI() {
        tensContainerLayout.removeAllViews();
        onesContainerLayout.removeAllViews();
        tvTensCount.setText(getResources().getQuantityString(
                R.plurals.tens_count,
                currentData.getTens(),
                currentData.getTens()
        ));
        tvOnesCount.setText(getResources().getQuantityString(
                R.plurals.ones_count,
                currentData.getOnes(),
                currentData.getOnes()
        ));

        float density = getResources().getDisplayMetrics().density;
        int margin = (int) (3 * density);
        int tensWidth = (int) (18 * density);
        int tensHeight = (int) (118 * density);
        int oneSize = (int) (28 * density);

        for (int index = 0; index < currentData.getTens(); index++) {
            ImageView view = new ImageView(this);
            view.setImageResource(R.drawable.ic_tens_block);
            view.setScaleType(ImageView.ScaleType.FIT_XY);
            view.setContentDescription(getString(R.string.ten_block_description));
            view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
            FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(tensWidth, tensHeight);
            params.setMargins(margin, margin, margin, margin);
            view.setLayoutParams(params);
            tensContainerLayout.addView(view);
        }

        for (int index = 0; index < currentData.getOnes(); index++) {
            ImageView view = new ImageView(this);
            view.setImageResource(R.drawable.ic_ones_block);
            view.setScaleType(ImageView.ScaleType.FIT_XY);
            view.setContentDescription(getString(R.string.one_block_description));
            view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
            FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(oneSize, oneSize);
            params.setMargins(margin, margin, margin, margin);
            view.setLayoutParams(params);
            onesContainerLayout.addView(view);
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
        if (selectedOption == currentData.getTotal()) {
            questionLocked = true;
            completeAnswerChoice(
                    optionsContainerLayout,
                    card,
                    tvFeedback,
                    successMessage(),
                    this::loadNextQuestion
            );
        } else {
            retryAnswerChoice(card, tvFeedback, getString(R.string.place_value_retry));
        }
    }

    private String successMessage() {
        String placeParts = getString(
                R.string.number_words_pair,
                getResources().getQuantityString(
                        R.plurals.tens_count,
                        currentData.getTens(),
                        currentData.getTens()
                ),
                getResources().getQuantityString(
                        R.plurals.ones_count,
                        currentData.getOnes(),
                        currentData.getOnes()
                )
        );
        String numberWords = ExerciseGeneratorUtil.numberToWords(currentData.getTotal())
                .toLowerCase(Locale.getDefault());
        return getString(R.string.place_value_success, placeParts, numberWords);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (currentData != null) {
            outState.putInt(STATE_TENS, currentData.getTens());
            outState.putInt(STATE_ONES, currentData.getOnes());
        }
        outState.putInt(STATE_LAST_TOTAL, lastTotal);
        outState.putIntegerArrayList(STATE_OPTIONS, new ArrayList<>(currentOptions));
        outState.putBoolean(STATE_QUESTION_LOCKED, questionLocked);
        super.onSaveInstanceState(outState);
    }
}
