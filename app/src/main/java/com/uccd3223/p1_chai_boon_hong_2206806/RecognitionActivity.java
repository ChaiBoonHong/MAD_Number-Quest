package com.uccd3223.p1_chai_boon_hong_2206806;

import android.os.Bundle;
import android.util.TypedValue;
import android.widget.GridLayout;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;
import com.uccd3223.p1_chai_boon_hong_2206806.util.ExerciseGeneratorUtil;
import com.uccd3223.p1_chai_boon_hong_2206806.util.ExerciseGeneratorUtil.RecognitionPromptMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecognitionActivity extends BaseGameActivity {
    private static final String STATE_TARGET = "recognition_target";
    private static final String STATE_LAST_TARGET = "recognition_last_target";
    private static final String STATE_PROMPT_MODE = "recognition_prompt_mode";
    private static final String STATE_LAST_MODE = "recognition_last_mode";
    private static final String STATE_OPTIONS = "recognition_options";
    private static final String STATE_QUESTION_LOCKED = "recognition_question_locked";

    private TextView tvTarget;
    private TextView tvTargetPrompt;
    private GridLayout optionsContainerLayout;
    private TextView tvFeedback;
    private int targetNumber;
    private List<Integer> currentOptions = Collections.emptyList();
    private RecognitionPromptMode promptMode = RecognitionPromptMode.WORD_TO_NUMBER;
    private int lastTarget = -1;
    private int lastModeOrdinal = -1;
    private boolean questionLocked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognition);
        setupGameModeUI();
        initializeUI();
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_TARGET)) {
            restoreQuestion(savedInstanceState);
        } else {
            loadNextQuestion();
        }
    }

    private void initializeUI() {
        tvTarget = findViewById(R.id.tvTarget);
        tvTargetPrompt = findViewById(R.id.tvTargetPrompt);
        optionsContainerLayout = findViewById(R.id.optionsContainerLayout);
        tvFeedback = findViewById(R.id.tvFeedback);
        findViewById(R.id.btnBack).setOnClickListener(view -> finish());
    }

    private void loadNextQuestion() {
        questionLocked = false;
        resetFeedback(tvFeedback);
        ExerciseGeneratorUtil.RecognitionExercise exercise;
        do {
            exercise = ExerciseGeneratorUtil.generateRecognitionExercise();
        } while (exercise.getTargetNumber() == lastTarget
                && exercise.getPromptMode().ordinal() == lastModeOrdinal);

        targetNumber = exercise.getTargetNumber();
        promptMode = exercise.getPromptMode();
        lastTarget = targetNumber;
        lastModeOrdinal = promptMode.ordinal();
        currentOptions = exercise.getOptions();
        renderQuestion();
    }

    private void restoreQuestion(Bundle state) {
        targetNumber = state.getInt(STATE_TARGET);
        lastTarget = state.getInt(STATE_LAST_TARGET, targetNumber);
        int modeIndex = Math.max(
                0,
                Math.min(RecognitionPromptMode.values().length - 1, state.getInt(STATE_PROMPT_MODE))
        );
        promptMode = RecognitionPromptMode.values()[modeIndex];
        lastModeOrdinal = state.getInt(STATE_LAST_MODE, promptMode.ordinal());
        questionLocked = state.getBoolean(STATE_QUESTION_LOCKED);
        ArrayList<Integer> restoredOptions = state.getIntegerArrayList(STATE_OPTIONS);
        currentOptions = restoredOptions == null ? Collections.emptyList() : restoredOptions;
        if (currentOptions.size() != 4) {
            currentOptions = ExerciseGeneratorUtil
                    .generateRecognitionOptions(targetNumber, 99, 4)
                    .getOptions();
        }
        renderQuestion();
        if (questionLocked) {
            restoreCompletedAnswerChoice(
                    optionsContainerLayout,
                    targetNumber,
                    tvFeedback,
                    successMessage(),
                    this::loadNextQuestion
            );
        }
    }

    private void renderQuestion() {
        String numberWords = ExerciseGeneratorUtil.numberToWords(targetNumber);
        List<AnswerChoice> choices = new ArrayList<>();
        if (promptMode == RecognitionPromptMode.WORD_TO_NUMBER) {
            tvTargetPrompt.setText(R.string.find_the_number);
            tvTarget.setText(numberWords);
            tvTarget.setTextSize(TypedValue.COMPLEX_UNIT_SP, 38f);
            for (int option : currentOptions) {
                choices.add(new AnswerChoice(
                        option,
                        getString(R.string.number_value, option),
                        getString(R.string.answer_number, option)
                ));
            }
        } else {
            tvTargetPrompt.setText(R.string.find_the_word);
            tvTarget.setText(getString(R.string.number_value, targetNumber));
            tvTarget.setTextSize(TypedValue.COMPLEX_UNIT_SP, 56f);
            for (int option : currentOptions) {
                String words = ExerciseGeneratorUtil.numberToWords(option);
                choices.add(new AnswerChoice(
                        option,
                        words,
                        getString(R.string.answer_text, words)
                ));
            }
        }
        tvTarget.setContentDescription(tvTarget.getText());
        renderAnswerChoices(optionsContainerLayout, choices, (choice, card) ->
                checkAnswer(choice.getValue(), card));
    }

    private void checkAnswer(int selectedOption, MaterialCardView card) {
        if (questionLocked || isGameOver) return;
        if (selectedOption == targetNumber) {
            questionLocked = true;
            completeAnswerChoice(
                    optionsContainerLayout,
                    card,
                    tvFeedback,
                    successMessage(),
                    this::loadNextQuestion
            );
        } else {
            int retryRes = promptMode == RecognitionPromptMode.WORD_TO_NUMBER
                    ? R.string.recognition_retry
                    : R.string.recognition_retry_number;
            retryAnswerChoice(card, tvFeedback, getString(retryRes));
        }
    }

    private String successMessage() {
        return getString(
                R.string.recognition_success,
                ExerciseGeneratorUtil.numberToWords(targetNumber),
                getString(R.string.number_value, targetNumber)
        );
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_TARGET, targetNumber);
        outState.putInt(STATE_LAST_TARGET, lastTarget);
        outState.putInt(STATE_PROMPT_MODE, promptMode.ordinal());
        outState.putInt(STATE_LAST_MODE, lastModeOrdinal);
        outState.putIntegerArrayList(STATE_OPTIONS, new ArrayList<>(currentOptions));
        outState.putBoolean(STATE_QUESTION_LOCKED, questionLocked);
        super.onSaveInstanceState(outState);
    }
}
