package com.uccd3223.p1_chai_boon_hong_2206806.util

import kotlin.random.Random

object ExerciseGeneratorUtil {

    data class PlaceValueData(val tens: Int, val ones: Int) {
        val total: Int get() = (tens * 10) + ones
    }

    data class RecognitionData(val targetNumber: Int, val options: List<Int>)

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
        val pool = mutableSetOf<Int>()
        
        if (targetNumber >= 10) {
            // Reversed digits (e.g. 45 -> 54)
            val reversed = (targetNumber % 10) * 10 + (targetNumber / 10)
            if (reversed != targetNumber && reversed <= maxRange) pool.add(reversed)
            
            // Off by 10 (e.g. 45 -> 35, 55)
            if (targetNumber - 10 > 0) pool.add(targetNumber - 10)
            if (targetNumber + 10 <= maxRange) pool.add(targetNumber + 10)
            
            // Off by 1 (e.g. 45 -> 44, 46)
            if (targetNumber - 1 > 0) pool.add(targetNumber - 1)
            if (targetNumber + 1 <= maxRange) pool.add(targetNumber + 1)
            
            // Same tens, different ones
            val tensBase = (targetNumber / 10) * 10
            pool.add(tensBase + Random.nextInt(0, 10))
            
            // Same ones, different tens
            val ones = targetNumber % 10
            val maxTensDigit = maxRange / 10
            if (maxTensDigit > 0) {
                pool.add(Random.nextInt(1, maxTensDigit + 1) * 10 + ones)
            }
        } else {
            // Target < 10
            if (targetNumber - 1 > 0) pool.add(targetNumber - 1)
            if (targetNumber + 1 <= maxRange) pool.add(targetNumber + 1)
            if (targetNumber - 2 > 0) pool.add(targetNumber - 2)
            if (targetNumber + 2 <= maxRange) pool.add(targetNumber + 2)
        }

        // Filter valid candidates and take what we need
        val validPool = pool.filter { it != targetNumber && it in 1..maxRange }.shuffled()
        options.addAll(validPool.take(numOptions - 1))
        
        // Fill remaining with random close numbers if needed
        var attempts = 0
        while (options.size < numOptions && attempts < 100) {
            val minBound = maxOf(1, targetNumber - 10)
            val maxBound = minOf(maxRange, targetNumber + 10)
            if (maxBound >= minBound) {
                options.add(Random.nextInt(minBound, maxBound + 1))
            }
            attempts++
        }
        
        // Final fallback to fill remaining
        attempts = 0
        while (options.size < numOptions && attempts < 100) {
            options.add(Random.nextInt(1, maxRange + 1))
            attempts++
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
        val numbers = mutableSetOf<Int>()
        while (numbers.size < length) {
            numbers.add(Random.nextInt(1, maxNumber + 1))
        }
        return numbers.toList()
    }
}
