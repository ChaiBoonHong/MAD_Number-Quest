package com.uccd3223.p1_chai_boon_hong_2206806

import com.uccd3223.p1_chai_boon_hong_2206806.util.ExerciseGeneratorUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExerciseGeneratorUtilTest {

    @Test
    fun recognitionOptionsContainTargetAndAreUnique() {
        repeat(100) {
            val target = listOf(0, 25, 50)[it % 3]
            val result = ExerciseGeneratorUtil.generateRecognitionOptions(target, 50, 4)
            assertEquals(4, result.options.size)
            assertEquals(4, result.options.distinct().size)
            assertTrue(result.targetNumber in result.options)
            assertEquals(1, result.options.count { option -> option == result.targetNumber })
            assertTrue(result.options.all { option -> option in 0..50 })
        }
    }

    @Test
    fun recognitionExerciseSupportsBothPromptModesAndBoundaries() {
        ExerciseGeneratorUtil.RecognitionPromptMode.entries.forEachIndexed { index, mode ->
            val target = if (index == 0) 0 else 99
            val result = ExerciseGeneratorUtil.generateRecognitionExercise(
                targetNumber = target,
                promptMode = mode
            )
            assertEquals(target, result.targetNumber)
            assertEquals(mode, result.promptMode)
            assertEquals(4, result.options.size)
            assertEquals(4, result.options.distinct().size)
            assertEquals(1, result.options.count { it == target })
        }
    }

    @Test
    fun generatedSortSequenceHasRequestedUniqueValues() {
        repeat(100) {
            val result = ExerciseGeneratorUtil.generateSortSequence(4, 10)
            assertEquals(4, result.size)
            assertEquals(4, result.distinct().size)
            assertTrue(result.all { it in 1..10 })
        }
    }

    @Test
    fun sortExercisesCoverEveryLengthAndDirection() {
        for (length in 3..6) {
            ExerciseGeneratorUtil.SortDirection.entries.forEach { direction ->
                repeat(50) {
                    val result = ExerciseGeneratorUtil.generateSortExercise(
                        length = length,
                        direction = direction
                    )
                    assertEquals(length, result.options.size)
                    assertEquals(length, result.options.distinct().size)
                    assertTrue(result.options.all { it in 0..99 })
                    val expected = if (direction == ExerciseGeneratorUtil.SortDirection.ASCENDING) {
                        result.options.sorted()
                    } else {
                        result.options.sortedDescending()
                    }
                    assertEquals(expected, result.orderedNumbers)
                }
            }
        }
    }

    @Test
    fun sortExerciseCanIncludeLearningRangeBoundaries() {
        val zero = ExerciseGeneratorUtil.generateSortExercise(
            length = 1,
            minNumber = 0,
            maxNumber = 0
        )
        val ninetyNine = ExerciseGeneratorUtil.generateSortExercise(
            length = 1,
            minNumber = 99,
            maxNumber = 99
        )
        assertEquals(listOf(0), zero.options)
        assertEquals(listOf(99), ninetyNine.options)
        assertNotEquals(zero.options, ninetyNine.options)
    }

    @Test
    fun balloonRowsMatchTheRequiredLayouts() {
        assertEquals(listOf(3), ExerciseGeneratorUtil.balloonRowPattern(3))
        assertEquals(listOf(2, 2), ExerciseGeneratorUtil.balloonRowPattern(4))
        assertEquals(listOf(3, 2), ExerciseGeneratorUtil.balloonRowPattern(5))
        assertEquals(listOf(3, 3), ExerciseGeneratorUtil.balloonRowPattern(6))
    }

    @Test(expected = IllegalArgumentException::class)
    fun balloonRowsRejectUnsupportedCounts() {
        ExerciseGeneratorUtil.balloonRowPattern(2)
    }

    @Test(expected = IllegalArgumentException::class)
    fun sortSequenceRejectsImpossibleRequest() {
        ExerciseGeneratorUtil.generateSortSequence(5, 4)
    }

    @Test
    fun numberToWordsHandlesLearningRange() {
        assertEquals("Zero", ExerciseGeneratorUtil.numberToWords(0))
        assertEquals("One", ExerciseGeneratorUtil.numberToWords(1))
        assertEquals("Nineteen", ExerciseGeneratorUtil.numberToWords(19))
        assertEquals("Forty-Two", ExerciseGeneratorUtil.numberToWords(42))
        assertEquals("Ninety-Nine", ExerciseGeneratorUtil.numberToWords(99))
    }
}
