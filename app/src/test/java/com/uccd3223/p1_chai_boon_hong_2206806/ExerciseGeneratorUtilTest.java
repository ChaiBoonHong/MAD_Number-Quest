package com.uccd3223.p1_chai_boon_hong_2206806;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.uccd3223.p1_chai_boon_hong_2206806.util.ExerciseGeneratorUtil;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class ExerciseGeneratorUtilTest {
    @Test
    public void recognitionOptionsContainTargetAndAreUnique() {
        List<Integer> targets = Arrays.asList(0, 25, 50);
        for (int index = 0; index < 100; index++) {
            int target = targets.get(index % targets.size());
            ExerciseGeneratorUtil.RecognitionData result =
                    ExerciseGeneratorUtil.generateRecognitionOptions(target, 50, 4);
            assertEquals(4, result.getOptions().size());
            assertEquals(4, new HashSet<>(result.getOptions()).size());
            assertTrue(result.getOptions().contains(result.getTargetNumber()));
            assertEquals(1, Collections.frequency(result.getOptions(), result.getTargetNumber()));
            assertTrue(result.getOptions().stream().allMatch(value -> value >= 0 && value <= 50));
        }
    }

    @Test
    public void recognitionExerciseSupportsBothPromptModesAndBoundaries() {
        ExerciseGeneratorUtil.RecognitionPromptMode[] modes =
                ExerciseGeneratorUtil.RecognitionPromptMode.values();
        for (int index = 0; index < modes.length; index++) {
            int target = index == 0 ? 0 : 99;
            ExerciseGeneratorUtil.RecognitionExercise result =
                    ExerciseGeneratorUtil.generateRecognitionExercise(99, 4, target, modes[index]);
            assertEquals(target, result.getTargetNumber());
            assertEquals(modes[index], result.getPromptMode());
            assertEquals(4, result.getOptions().size());
            assertEquals(4, new HashSet<>(result.getOptions()).size());
            assertEquals(1, Collections.frequency(result.getOptions(), target));
        }
    }

    @Test
    public void generatedSortSequenceHasRequestedUniqueValues() {
        for (int index = 0; index < 100; index++) {
            List<Integer> result = ExerciseGeneratorUtil.generateSortSequence(4, 10);
            assertEquals(4, result.size());
            assertEquals(4, new HashSet<>(result).size());
            assertTrue(result.stream().allMatch(value -> value >= 1 && value <= 10));
        }
    }

    @Test
    public void sortExercisesCoverEveryLengthAndDirection() {
        for (int length = 3; length <= 6; length++) {
            for (ExerciseGeneratorUtil.SortDirection direction
                    : ExerciseGeneratorUtil.SortDirection.values()) {
                for (int index = 0; index < 50; index++) {
                    ExerciseGeneratorUtil.SortExercise result =
                            ExerciseGeneratorUtil.generateSortExercise(length, 0, 99, direction);
                    assertEquals(length, result.getOptions().size());
                    assertEquals(length, new HashSet<>(result.getOptions()).size());
                    assertTrue(result.getOptions().stream().allMatch(value -> value >= 0 && value <= 99));
                    List<Integer> expected = result.getOptions();
                    expected.sort(direction == ExerciseGeneratorUtil.SortDirection.ASCENDING
                            ? Integer::compareTo
                            : Collections.reverseOrder());
                    assertEquals(expected, result.getOrderedNumbers());
                }
            }
        }
    }

    @Test
    public void sortExerciseCanIncludeLearningRangeBoundaries() {
        ExerciseGeneratorUtil.SortExercise zero = ExerciseGeneratorUtil.generateSortExercise(
                1, 0, 0, ExerciseGeneratorUtil.SortDirection.ASCENDING);
        ExerciseGeneratorUtil.SortExercise ninetyNine = ExerciseGeneratorUtil.generateSortExercise(
                1, 99, 99, ExerciseGeneratorUtil.SortDirection.ASCENDING);
        assertEquals(Collections.singletonList(0), zero.getOptions());
        assertEquals(Collections.singletonList(99), ninetyNine.getOptions());
        assertNotEquals(zero.getOptions(), ninetyNine.getOptions());
    }

    @Test
    public void balloonRowsMatchTheRequiredLayouts() {
        assertEquals(Collections.singletonList(3), ExerciseGeneratorUtil.balloonRowPattern(3));
        assertEquals(Arrays.asList(2, 2), ExerciseGeneratorUtil.balloonRowPattern(4));
        assertEquals(Arrays.asList(3, 2), ExerciseGeneratorUtil.balloonRowPattern(5));
        assertEquals(Arrays.asList(3, 3), ExerciseGeneratorUtil.balloonRowPattern(6));
    }

    @Test(expected = IllegalArgumentException.class)
    public void balloonRowsRejectUnsupportedCounts() {
        ExerciseGeneratorUtil.balloonRowPattern(2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void sortSequenceRejectsImpossibleRequest() {
        ExerciseGeneratorUtil.generateSortSequence(5, 4);
    }

    @Test
    public void numberToWordsHandlesLearningRange() {
        assertEquals("Zero", ExerciseGeneratorUtil.numberToWords(0));
        assertEquals("One", ExerciseGeneratorUtil.numberToWords(1));
        assertEquals("Nineteen", ExerciseGeneratorUtil.numberToWords(19));
        assertEquals("Forty-Two", ExerciseGeneratorUtil.numberToWords(42));
        assertEquals("Ninety-Nine", ExerciseGeneratorUtil.numberToWords(99));
    }
}
