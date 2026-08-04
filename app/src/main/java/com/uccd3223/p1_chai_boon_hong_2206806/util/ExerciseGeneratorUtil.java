package com.uccd3223.p1_chai_boon_hong_2206806.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public final class ExerciseGeneratorUtil {
    public enum SortDirection { ASCENDING, DESCENDING }

    public enum RecognitionPromptMode { WORD_TO_NUMBER, NUMBER_TO_WORD }

    public static final class PlaceValueData {
        private final int tens;
        private final int ones;

        public PlaceValueData(int tens, int ones) {
            this.tens = tens;
            this.ones = ones;
        }

        public int getTens() {
            return tens;
        }

        public int getOnes() {
            return ones;
        }

        public int getTotal() {
            return tens * 10 + ones;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof PlaceValueData)) return false;
            PlaceValueData data = (PlaceValueData) other;
            return tens == data.tens && ones == data.ones;
        }

        @Override
        public int hashCode() {
            return Objects.hash(tens, ones);
        }
    }

    public static final class RecognitionData {
        private final int targetNumber;
        private final List<Integer> options;

        public RecognitionData(int targetNumber, List<Integer> options) {
            this.targetNumber = targetNumber;
            this.options = new ArrayList<>(options);
        }

        public int getTargetNumber() {
            return targetNumber;
        }

        public List<Integer> getOptions() {
            return new ArrayList<>(options);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof RecognitionData)) return false;
            RecognitionData data = (RecognitionData) other;
            return targetNumber == data.targetNumber && options.equals(data.options);
        }

        @Override
        public int hashCode() {
            return Objects.hash(targetNumber, options);
        }
    }

    public static final class RecognitionExercise {
        private final int targetNumber;
        private final List<Integer> options;
        private final RecognitionPromptMode promptMode;

        public RecognitionExercise(
                int targetNumber,
                List<Integer> options,
                RecognitionPromptMode promptMode
        ) {
            this.targetNumber = targetNumber;
            this.options = new ArrayList<>(options);
            this.promptMode = Objects.requireNonNull(promptMode);
        }

        public int getTargetNumber() {
            return targetNumber;
        }

        public List<Integer> getOptions() {
            return new ArrayList<>(options);
        }

        public RecognitionPromptMode getPromptMode() {
            return promptMode;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof RecognitionExercise)) return false;
            RecognitionExercise exercise = (RecognitionExercise) other;
            return targetNumber == exercise.targetNumber
                    && options.equals(exercise.options)
                    && promptMode == exercise.promptMode;
        }

        @Override
        public int hashCode() {
            return Objects.hash(targetNumber, options, promptMode);
        }
    }

    public static final class SortExercise {
        private final List<Integer> options;
        private final SortDirection direction;

        public SortExercise(List<Integer> options, SortDirection direction) {
            this.options = new ArrayList<>(options);
            this.direction = Objects.requireNonNull(direction);
        }

        public List<Integer> getOptions() {
            return new ArrayList<>(options);
        }

        public SortDirection getDirection() {
            return direction;
        }

        public List<Integer> getOrderedNumbers() {
            List<Integer> ordered = new ArrayList<>(options);
            ordered.sort(direction == SortDirection.ASCENDING
                    ? Integer::compareTo
                    : Collections.reverseOrder());
            return ordered;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof SortExercise)) return false;
            SortExercise exercise = (SortExercise) other;
            return options.equals(exercise.options) && direction == exercise.direction;
        }

        @Override
        public int hashCode() {
            return Objects.hash(options, direction);
        }
    }

    public static final class SequenceData {
        private final int[] sequence;
        private final int missingIndex;

        public SequenceData(int[] sequence, int missingIndex) {
            this.sequence = sequence.clone();
            this.missingIndex = missingIndex;
        }

        public int[] getSequence() {
            return sequence.clone();
        }

        public int getMissingIndex() {
            return missingIndex;
        }

        public int getMissingValue() {
            return sequence[missingIndex];
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof SequenceData)) return false;
            SequenceData data = (SequenceData) other;
            return missingIndex == data.missingIndex && Arrays.equals(sequence, data.sequence);
        }

        @Override
        public int hashCode() {
            return 31 * Arrays.hashCode(sequence) + missingIndex;
        }
    }

    private ExerciseGeneratorUtil() {
    }

    public static int generateObjectCount(int maxObjects) {
        if (maxObjects < 1) throw new IllegalArgumentException("maxObjects must be at least 1");
        return ThreadLocalRandom.current().nextInt(1, maxObjects + 1);
    }

    public static PlaceValueData generatePlaceValue(int maxTens) {
        if (maxTens < 1) throw new IllegalArgumentException("maxTens must be at least 1");
        return new PlaceValueData(
                ThreadLocalRandom.current().nextInt(1, maxTens + 1),
                ThreadLocalRandom.current().nextInt(0, 10)
        );
    }

    public static RecognitionData generateRecognitionOptions(
            int targetNumber,
            int maxRange,
            int numOptions
    ) {
        if (maxRange < 0) throw new IllegalArgumentException("maxRange must not be negative");
        if (targetNumber < 0 || targetNumber > maxRange) {
            throw new IllegalArgumentException("targetNumber must be inside maxRange");
        }
        if (numOptions < 1 || numOptions > maxRange + 1) {
            throw new IllegalArgumentException("numOptions must fit inside the learning range");
        }

        Set<Integer> preferred = new LinkedHashSet<>();
        if (targetNumber >= 10) preferred.add((targetNumber % 10) * 10 + targetNumber / 10);
        preferred.add(targetNumber - 10);
        preferred.add(targetNumber + 10);
        preferred.add(targetNumber - 2);
        preferred.add(targetNumber + 2);
        preferred.add(targetNumber - 1);
        preferred.add(targetNumber + 1);

        Set<Integer> options = new LinkedHashSet<>();
        options.add(targetNumber);
        List<Integer> preferredList = new ArrayList<>(preferred);
        Collections.shuffle(preferredList);
        for (int candidate : preferredList) {
            if (candidate != targetNumber && candidate >= 0 && candidate <= maxRange) {
                if (options.size() < numOptions) options.add(candidate);
            }
        }

        List<Integer> remaining = new ArrayList<>();
        for (int value = 0; value <= maxRange; value++) {
            if (!options.contains(value)) remaining.add(value);
        }
        Collections.shuffle(remaining);
        for (int candidate : remaining) {
            if (options.size() < numOptions) options.add(candidate);
        }

        List<Integer> shuffled = new ArrayList<>(options);
        Collections.shuffle(shuffled);
        return new RecognitionData(targetNumber, shuffled);
    }

    public static RecognitionExercise generateRecognitionExercise() {
        int target = ThreadLocalRandom.current().nextInt(0, 100);
        RecognitionPromptMode mode = ThreadLocalRandom.current().nextBoolean()
                ? RecognitionPromptMode.WORD_TO_NUMBER
                : RecognitionPromptMode.NUMBER_TO_WORD;
        return generateRecognitionExercise(99, 4, target, mode);
    }

    public static RecognitionExercise generateRecognitionExercise(
            int maxRange,
            int numOptions,
            int targetNumber,
            RecognitionPromptMode promptMode
    ) {
        RecognitionData data = generateRecognitionOptions(targetNumber, maxRange, numOptions);
        return new RecognitionExercise(data.getTargetNumber(), data.getOptions(), promptMode);
    }

    public static SequenceData generateSequence(int start, int step, int length) {
        if (length < 1) throw new IllegalArgumentException("length must be at least 1");
        int[] sequence = new int[length];
        for (int index = 0; index < length; index++) sequence[index] = start + index * step;
        return new SequenceData(sequence, ThreadLocalRandom.current().nextInt(length));
    }

    public static String numberToWords(int number) {
        if (number == 0) return "Zero";
        if (number < 0 || number > 99) return Integer.toString(number);
        String[] ones = {
                "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
                "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen",
                "Seventeen", "Eighteen", "Nineteen"
        };
        String[] tens = {
                "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
        };
        if (number < 20) return ones[number];
        String tensWord = tens[number / 10];
        String onesWord = ones[number % 10];
        return onesWord.isEmpty() ? tensWord : tensWord + "-" + onesWord;
    }

    public static List<Integer> generateSortSequence(int length) {
        return generateSortSequence(length, 99);
    }

    public static List<Integer> generateSortSequence(int length, int maxNumber) {
        if (maxNumber < 1) throw new IllegalArgumentException("maxNumber must be at least 1");
        if (length < 1 || length > maxNumber) {
            throw new IllegalArgumentException("length must be between 1 and maxNumber");
        }
        Set<Integer> numbers = new LinkedHashSet<>();
        while (numbers.size() < length) {
            numbers.add(ThreadLocalRandom.current().nextInt(1, maxNumber + 1));
        }
        return new ArrayList<>(numbers);
    }

    public static SortExercise generateSortExercise(int length) {
        SortDirection direction = ThreadLocalRandom.current().nextBoolean()
                ? SortDirection.ASCENDING
                : SortDirection.DESCENDING;
        return generateSortExercise(length, 0, 99, direction);
    }

    public static SortExercise generateSortExercise(
            int length,
            int minNumber,
            int maxNumber,
            SortDirection direction
    ) {
        if (minNumber < 0) throw new IllegalArgumentException("minNumber must not be negative");
        if (maxNumber > 99) throw new IllegalArgumentException("maxNumber must be no more than 99");
        if (minNumber > maxNumber) {
            throw new IllegalArgumentException("minNumber must not exceed maxNumber");
        }
        if (length < 1 || length > maxNumber - minNumber + 1) {
            throw new IllegalArgumentException("length must fit inside the learning range");
        }

        Set<Integer> numbers = new LinkedHashSet<>();
        while (numbers.size() < length) {
            numbers.add(ThreadLocalRandom.current().nextInt(minNumber, maxNumber + 1));
        }
        List<Integer> options = new ArrayList<>(numbers);
        Collections.shuffle(options);
        return new SortExercise(options, direction);
    }

    public static List<Integer> balloonRowPattern(int count) {
        switch (count) {
            case 3:
                return Collections.singletonList(3);
            case 4:
                return Arrays.asList(2, 2);
            case 5:
                return Arrays.asList(3, 2);
            case 6:
                return Arrays.asList(3, 3);
            default:
                throw new IllegalArgumentException("Balloon count must be between 3 and 6");
        }
    }
}
