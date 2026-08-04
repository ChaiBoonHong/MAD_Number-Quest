package com.uccd3223.p1_chai_boon_hong_2206806.util

import kotlin.random.Random

object ExerciseGeneratorUtil {

    enum class SortDirection { ASCENDING, DESCENDING }

    enum class RecognitionPromptMode { WORD_TO_NUMBER, NUMBER_TO_WORD }

    data class PlaceValueData(val tens: Int, val ones: Int) {
        val total: Int get() = (tens * 10) + ones
    }

    data class RecognitionData(val targetNumber: Int, val options: List<Int>)

    data class RecognitionExercise(
        val targetNumber: Int,
        val options: List<Int>,
        val promptMode: RecognitionPromptMode
    )

    data class SortExercise(
        val options: List<Int>,
        val direction: SortDirection
    ) {
        val orderedNumbers: List<Int>
            get() = when (direction) {
                SortDirection.ASCENDING -> options.sorted()
                SortDirection.DESCENDING -> options.sortedDescending()
            }
    }

    data class SequenceData(val sequence: IntArray, val missingIndex: Int) {
        val missingValue: Int get() = sequence[missingIndex]

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is SequenceData) return false
            return sequence.contentEquals(other.sequence) && missingIndex == other.missingIndex
        }

        override fun hashCode(): Int {
            var result = sequence.contentHashCode()
            result = 31 * result + missingIndex
            return result
        }
    }

    /**
     * 1. Number to Objects Association
     * Generates a random number of objects to display.
     */
    fun generateObjectCount(maxObjects: Int): Int {
        require(maxObjects >= 1) { "maxObjects must be at least 1" }
        return Random.nextInt(1, maxObjects + 1)
    }

    /**
     * 2. Place Value
     * Generates a two-digit number broken down into tens and ones.
     */
    fun generatePlaceValue(maxTens: Int): PlaceValueData {
        require(maxTens >= 1) { "maxTens must be at least 1" }
        val tens = Random.nextInt(1, maxTens + 1)
        val ones = Random.nextInt(0, 10)
        return PlaceValueData(tens, ones)
    }

    /**
     * 3. Numbers Recognition
     * Generates a target number and a shuffled list of distractor options that are similar (close) to the target.
     */
    fun generateRecognitionOptions(targetNumber: Int, maxRange: Int, numOptions: Int): RecognitionData {
        require(maxRange >= 0) { "maxRange must not be negative" }
        require(targetNumber in 0..maxRange) { "targetNumber must be inside maxRange" }
        require(numOptions in 1..(maxRange + 1)) {
            "numOptions must fit inside the learning range"
        }

        val preferred = linkedSetOf<Int>()
        if (targetNumber >= 10) {
            preferred += (targetNumber % 10) * 10 + (targetNumber / 10)
        }
        preferred += targetNumber - 10
        preferred += targetNumber + 10
        preferred += targetNumber - 2
        preferred += targetNumber + 2
        preferred += targetNumber - 1
        preferred += targetNumber + 1

        val options = linkedSetOf(targetNumber)
        preferred
            .filter { it != targetNumber && it in 0..maxRange }
            .shuffled()
            .forEach { if (options.size < numOptions) options += it }

        (0..maxRange)
            .filter { it !in options }
            .shuffled()
            .forEach { if (options.size < numOptions) options += it }

        return RecognitionData(targetNumber, options.shuffled())
    }

    fun generateRecognitionExercise(
        maxRange: Int = 99,
        numOptions: Int = 4,
        targetNumber: Int = Random.nextInt(0, maxRange + 1),
        promptMode: RecognitionPromptMode = if (Random.nextBoolean()) {
            RecognitionPromptMode.WORD_TO_NUMBER
        } else {
            RecognitionPromptMode.NUMBER_TO_WORD
        }
    ): RecognitionExercise {
        val data = generateRecognitionOptions(targetNumber, maxRange, numOptions)
        return RecognitionExercise(data.targetNumber, data.options, promptMode)
    }

    /**
     * 4. Numbers in Sequence
     * Generates an array representing a sequence and the index of the missing element.
     */
    fun generateSequence(start: Int, step: Int, length: Int): SequenceData {
        require(length >= 1) { "length must be at least 1" }
        val sequence = IntArray(length) { i -> start + (i * step) }
        val missingIndex = Random.nextInt(length)
        return SequenceData(sequence, missingIndex)
    }

    fun numberToWords(number: Int): String {
        if (number == 0) return "Zero"
        if (number < 0 || number > 99) return number.toString()
        val ones = arrayOf("", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
            "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen")
        val tens = arrayOf("", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety")

        return if (number < 20) {
            ones[number]
        } else {
            val t = tens[number / 10]
            val o = ones[number % 10]
            if (o.isEmpty()) t else "$t-$o"
        }
    }

    fun generateSortSequence(length: Int, maxNumber: Int = 99): List<Int> {
        require(maxNumber >= 1) { "maxNumber must be at least 1" }
        require(length in 1..maxNumber) { "length must be between 1 and maxNumber" }
        val numbers = mutableSetOf<Int>()
        while (numbers.size < length) {
            numbers.add(Random.nextInt(1, maxNumber + 1))
        }
        return numbers.toList()
    }

    fun generateSortExercise(
        length: Int,
        minNumber: Int = 0,
        maxNumber: Int = 99,
        direction: SortDirection = if (Random.nextBoolean()) {
            SortDirection.ASCENDING
        } else {
            SortDirection.DESCENDING
        }
    ): SortExercise {
        require(minNumber >= 0) { "minNumber must not be negative" }
        require(maxNumber <= 99) { "maxNumber must be no more than 99" }
        require(minNumber <= maxNumber) { "minNumber must not exceed maxNumber" }
        require(length in 1..(maxNumber - minNumber + 1)) {
            "length must fit inside the learning range"
        }

        val numbers = mutableSetOf<Int>()
        while (numbers.size < length) {
            numbers += Random.nextInt(minNumber, maxNumber + 1)
        }
        return SortExercise(numbers.toList().shuffled(), direction)
    }

    fun balloonRowPattern(count: Int): List<Int> = when (count) {
        3 -> listOf(3)
        4 -> listOf(2, 2)
        5 -> listOf(3, 2)
        6 -> listOf(3, 3)
        else -> throw IllegalArgumentException("Balloon count must be between 3 and 6")
    }
}
