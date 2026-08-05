package com.uccd3223.p1_chai_boon_hong_2206806;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.uccd3223.p1_chai_boon_hong_2206806.util.ExerciseGeneratorUtil;
import com.uccd3223.p1_chai_boon_hong_2206806.util.ExerciseGeneratorUtil.SortDirection;
import com.uccd3223.p1_chai_boon_hong_2206806.util.ExerciseGeneratorUtil.SortExercise;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class SequenceActivity extends BaseGameActivity {
    private interface BalloonViewFactory {
        FrameLayout create(int index, LinearLayout row);
    }

    private static final int EMPTY_POSITION = -1;
    private static final String STATE_OPTIONS = "sequence_options";
    private static final String STATE_DIRECTION = "sequence_direction";
    private static final String STATE_PLACED = "sequence_placed";
    private static final String STATE_LAST_SIGNATURE = "sequence_last_signature";
    private static final String STATE_QUESTION_LOCKED = "sequence_question_locked";

    private LinearLayout sequenceContainerLayout;
    private LinearLayout optionsContainerLayout;
    private TextView tvStartDirection;
    private TextView tvFeedback;
    private SortExercise currentExercise;
    private List<Integer> placedNumbers = new ArrayList<>();
    private String lastSignature = "";
    private boolean questionLocked;
    private final List<FrameLayout> targetViews = new ArrayList<>();
    private final Map<Integer, FrameLayout> sourceViews = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sequence);
        setupGameModeUI();
        initializeUI();
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_OPTIONS)) {
            restoreQuestion(savedInstanceState);
        } else {
            loadNextQuestion();
        }
    }

    private void initializeUI() {
        sequenceContainerLayout = findViewById(R.id.sequenceContainerLayout);
        optionsContainerLayout = findViewById(R.id.optionsContainerLayout);
        tvStartDirection = findViewById(R.id.tvStartDirection);
        tvFeedback = findViewById(R.id.tvFeedback);
    }

    private void loadNextQuestion() {
        questionLocked = false;
        resetFeedback(tvFeedback);
        do {
            int length = ThreadLocalRandom.current().nextInt(3, 7);
            currentExercise = ExerciseGeneratorUtil.generateSortExercise(length);
        } while (questionSignature(currentExercise).equals(lastSignature));
        lastSignature = questionSignature(currentExercise);
        placedNumbers = new ArrayList<>(Collections.nCopies(currentExercise.getOptions().size(), null));
        renderQuestion();
    }

    private void restoreQuestion(Bundle state) {
        int[] optionArray = state.getIntArray(STATE_OPTIONS);
        List<Integer> options = new ArrayList<>();
        if (optionArray != null) {
            for (int option : optionArray) options.add(option);
        }
        if (options.size() < 3 || options.size() > 6
                || new HashSet<>(options).size() != options.size()) {
            loadNextQuestion();
            return;
        }

        int directionIndex = Math.max(
                0,
                Math.min(SortDirection.values().length - 1, state.getInt(STATE_DIRECTION))
        );
        currentExercise = new SortExercise(options, SortDirection.values()[directionIndex]);
        int[] restoredArray = state.getIntArray(STATE_PLACED);
        List<Integer> restored = new ArrayList<>();
        if (restoredArray != null) {
            for (int value : restoredArray) restored.add(value == EMPTY_POSITION ? null : value);
        }
        placedNumbers = restored.size() == options.size()
                ? restored
                : new ArrayList<>(Collections.nCopies(options.size(), null));
        String restoredSignature = state.getString(STATE_LAST_SIGNATURE);
        lastSignature = restoredSignature == null ? "" : restoredSignature;
        questionLocked = state.getBoolean(STATE_QUESTION_LOCKED);
        renderQuestion();
        if (questionLocked) {
            restoreCompletedQuestion(tvFeedback, successMessage(), this::loadNextQuestion);
        }
    }

    private String questionSignature(SortExercise exercise) {
        List<Integer> sorted = exercise.getOptions();
        Collections.sort(sorted);
        StringBuilder signature = new StringBuilder(exercise.getDirection().name()).append(':');
        for (int index = 0; index < sorted.size(); index++) {
            if (index > 0) signature.append(',');
            signature.append(sorted.get(index));
        }
        return signature.toString();
    }

    private void renderQuestion() {
        tvStartDirection.setText(currentExercise.getDirection() == SortDirection.ASCENDING
                ? R.string.start_with_smallest
                : R.string.start_with_biggest);
        renderSequenceTargets();
        renderAnswerBalloons();
    }

    private void renderSequenceTargets() {
        targetViews.clear();
        renderBalloonRows(
                sequenceContainerLayout,
                currentExercise.getOptions().size(),
                (index, row) -> {
                    BalloonFrameLayout targetView = inflateBalloon(row);
                    targetViews.add(targetView);
                    Integer placed = placedNumbers.get(index);
                    if (placed == null) {
                        targetView.<TextView>findViewById(R.id.balloonText).setText("");
                        targetView.setContentDescription(getString(
                                R.string.position_empty,
                                index + 1,
                                currentExercise.getOptions().size()
                        ));
                        attachDropTarget(targetView, index);
                    } else {
                        fillTargetView(targetView, index, placed);
                    }
                    return targetView;
                }
        );
    }

    private void renderAnswerBalloons() {
        sourceViews.clear();
        List<Integer> options = currentExercise.getOptions();
        renderBalloonRows(
                optionsContainerLayout,
                options.size(),
                (index, row) -> {
                    int number = options.get(index);
                    BalloonFrameLayout optionView = inflateBalloon(row);
                    sourceViews.put(number, optionView);
                    optionView.<TextView>findViewById(R.id.balloonText).setText(
                            getString(R.string.number_value, number)
                    );
                    optionView.<ImageView>findViewById(R.id.balloonBg).setImageTintList(
                            ColorStateList.valueOf(colorForNumber(number))
                    );
                    optionView.setContentDescription(getString(R.string.drag_number, number));
                    optionView.setVisibility(placedNumbers.contains(number) ? View.INVISIBLE : View.VISIBLE);
                    optionView.setOnClickListener(view -> placeByTap(number, optionView));
                    attachImmediateDrag(optionView, number);
                    return optionView;
                }
        );
    }

    private void renderBalloonRows(
            LinearLayout parent,
            int count,
            BalloonViewFactory factory
    ) {
        parent.removeAllViews();
        List<Integer> pattern = ExerciseGeneratorUtil.balloonRowPattern(count);
        int itemIndex = 0;
        for (int rowSize : pattern) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER);
            row.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            for (int item = 0; item < rowSize; item++) {
                row.addView(factory.create(itemIndex++, row));
            }
            parent.addView(row);
        }
    }

    private BalloonFrameLayout inflateBalloon(LinearLayout parent) {
        int size = getResources().getDimensionPixelSize(R.dimen.balloon_size);
        int gap = getResources().getDimensionPixelSize(R.dimen.balloon_gap);
        BalloonFrameLayout balloon = (BalloonFrameLayout) getLayoutInflater().inflate(
                R.layout.item_balloon,
                parent,
                false
        );
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        params.setMargins(gap, gap, gap, gap);
        balloon.setLayoutParams(params);
        return balloon;
    }

    private void attachDropTarget(FrameLayout targetView, int targetIndex) {
        targetView.setOnDragListener((view, event) -> {
            View source = event.getLocalState() instanceof View
                    ? (View) event.getLocalState()
                    : null;
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return event.getClipDescription() != null
                            && event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
                            && source != null;
                case DragEvent.ACTION_DRAG_ENTERED:
                    view.setAlpha(0.7f);
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    view.setAlpha(1f);
                    return true;
                case DragEvent.ACTION_DROP:
                    view.setAlpha(1f);
                    Integer number = parseDraggedNumber(event);
                    if (number != null
                            && source != null
                            && placedNumbers.get(targetIndex) == null
                            && number.equals(currentExercise.getOrderedNumbers().get(targetIndex))) {
                        placeNumber(targetIndex, number, source);
                    } else {
                        showSequenceRetry(view);
                    }
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    view.setAlpha(1f);
                    if (source != null) source.setAlpha(1f);
                    return true;
                default:
                    return false;
            }
        });
    }

    private Integer parseDraggedNumber(DragEvent event) {
        if (event.getClipData() == null || event.getClipData().getItemCount() == 0) return null;
        try {
            return Integer.valueOf(event.getClipData().getItemAt(0).getText().toString());
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private void attachImmediateDrag(BalloonFrameLayout optionView, int number) {
        int touchSlop = ViewConfiguration.get(this).getScaledTouchSlop();
        float[] down = new float[2];
        boolean[] dragStarted = new boolean[1];

        optionView.setOnTouchListener((view, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    down[0] = event.getX();
                    down[1] = event.getY();
                    dragStarted[0] = false;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (!dragStarted[0]
                            && (Math.abs(event.getX() - down[0]) > touchSlop
                            || Math.abs(event.getY() - down[1]) > touchSlop)) {
                        ClipData dragData = new ClipData(
                                Integer.toString(number),
                                new String[] { ClipDescription.MIMETYPE_TEXT_PLAIN },
                                new ClipData.Item(Integer.toString(number))
                        );
                        dragStarted[0] = view.startDragAndDrop(
                                dragData,
                                new View.DragShadowBuilder(view),
                                view,
                                0
                        );
                        if (dragStarted[0]) {
                            view.setAlpha(0.35f);
                            view.getParent().requestDisallowInterceptTouchEvent(true);
                        }
                    }
                    return dragStarted[0];
                case MotionEvent.ACTION_UP:
                    view.getParent().requestDisallowInterceptTouchEvent(false);
                    if (!dragStarted[0]) view.performClick();
                    return true;
                case MotionEvent.ACTION_CANCEL:
                    view.getParent().requestDisallowInterceptTouchEvent(false);
                    view.setAlpha(1f);
                    return true;
                default:
                    return dragStarted[0];
            }
        });
    }

    private void placeByTap(int number, View sourceView) {
        if (questionLocked || !canAcceptInput() || sourceView.getVisibility() != View.VISIBLE) return;
        int nextIndex = -1;
        for (int index = 0; index < placedNumbers.size(); index++) {
            if (placedNumbers.get(index) == null) {
                nextIndex = index;
                break;
            }
        }
        if (nextIndex >= 0 && number == currentExercise.getOrderedNumbers().get(nextIndex)) {
            placeNumber(nextIndex, number, sourceView);
        } else {
            showSequenceRetry(sourceView);
        }
    }

    private void placeNumber(int targetIndex, int number, View sourceView) {
        if (questionLocked || !canAcceptInput()
                || placedNumbers.get(targetIndex) != null
                || sourceView.getVisibility() != View.VISIBLE) return;
        placedNumbers.set(targetIndex, number);
        fillTargetView(targetViews.get(targetIndex), targetIndex, number);
        sourceView.setVisibility(View.INVISIBLE);

        boolean complete = true;
        for (Integer placed : placedNumbers) {
            if (placed == null) {
                complete = false;
                break;
            }
        }
        if (complete) {
            questionLocked = true;
            celebrateQuestion(
                    targetViews.get(targetIndex),
                    tvFeedback,
                    successMessage(),
                    this::loadNextQuestion
            );
        }
    }

    private String successMessage() {
        return getString(currentExercise.getDirection() == SortDirection.ASCENDING
                ? R.string.sequence_success_small
                : R.string.sequence_success_big);
    }

    private void fillTargetView(FrameLayout targetView, int index, int number) {
        targetView.setOnDragListener(null);
        targetView.<TextView>findViewById(R.id.balloonText).setText(
                getString(R.string.number_value, number)
        );
        targetView.<ImageView>findViewById(R.id.balloonBg).setImageTintList(
                ColorStateList.valueOf(colorForNumber(number))
        );
        targetView.setContentDescription(getString(
                R.string.position_filled,
                index + 1,
                currentExercise.getOptions().size(),
                number
        ));
    }

    private void showSequenceRetry(View anchor) {
        int message = currentExercise.getDirection() == SortDirection.ASCENDING
                ? R.string.sequence_retry_small
                : R.string.sequence_retry_big;
        showRetryFeedback(anchor, tvFeedback, getString(message));
    }

    private int colorForNumber(int number) {
        int[] colors = {
                R.color.game_blue,
                R.color.game_orange,
                R.color.game_purple,
                R.color.game_green,
                R.color.game_balloon_pink,
                R.color.game_star
        };
        int index = Math.max(0, currentExercise.getOptions().indexOf(number));
        return ContextCompat.getColor(this, colors[index % colors.length]);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (currentExercise != null) {
            List<Integer> options = currentExercise.getOptions();
            int[] optionArray = new int[options.size()];
            int[] placedArray = new int[placedNumbers.size()];
            for (int index = 0; index < options.size(); index++) {
                optionArray[index] = options.get(index);
                Integer placed = placedNumbers.get(index);
                placedArray[index] = placed == null ? EMPTY_POSITION : placed;
            }
            outState.putIntArray(STATE_OPTIONS, optionArray);
            outState.putInt(STATE_DIRECTION, currentExercise.getDirection().ordinal());
            outState.putIntArray(STATE_PLACED, placedArray);
        }
        outState.putString(STATE_LAST_SIGNATURE, lastSignature);
        outState.putBoolean(STATE_QUESTION_LOCKED, questionLocked);
        super.onSaveInstanceState(outState);
    }
}
