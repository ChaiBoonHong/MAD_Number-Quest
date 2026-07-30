package com.uccd3223.p1_chai_boon_hong_2206806.util

import kotlin.random.Random

object ExerciseGeneratorUtil {

    data class PlaceValueData(val tens: Int, val ones: Int) {
        val total: Int get() = (tens * 10) + ones
    }

    data class RecognitionData(val targetNumber: Int, val options: List<Int>)

    data class SequenceData(val sequence: IntArray, val missingIndex: Int) {
        val missingValue: Int get() = sequence[missingIndex]
    }

    /**
     * 1. Number to Objects Association
     * Generates a random number of objects to display.
     */
    fun generateObjectCount(maxObjects: Int): Int {
        return Random.nextInt(1, maxObjects + 1)
    }

    /**
     * 2. Place Value
     * Generates a two-digit number broken down into tens and ones.
     */
    fun generatePlaceValue(maxTens: Int): PlaceValueData {
        val tens = Random.nextInt(1, maxTens + 1)
        val ones = Random.nextInt(0, 10)
        return PlaceValueData(tens, ones)
    }

    /**
     * 3. Numbers Recognition
     * Generates a target number and a shuffled list of distractor options that are similar (close) to the target.
     */
    fun generateRecognitionOptions(targetNumber: Int, maxRange: Int, numOptions: Int): RecognitionData {
        val options = mutableSetOf(targetNumber)
        val offset = 5 // Options will be within +/- 5 of the target
        while (options.size < numOptions) {
            val minBound = maxOf(1, targetNumber - offset)
            val maxBound = minOf(maxRange, targetNumber + offset)
            val distractor = Random.nextInt(minBound, maxBound + 1)
            options.add(distractor)
        }
        return RecognitionData(targetNumber, options.toList().shuffled())
    }

    /**
     * 4. Numbers in Sequence
     * Generates an array representing a sequence and the index of the missing element.
     */
    fun generateSequence(start: Int, step: Int, length: Int): SequenceData {
        val sequence = IntArray(length) { i -> start + (i * step) }
        val missingIndex = Random.nextInt(length)
        return SequenceData(sequence, missingIndex)
    }
}
