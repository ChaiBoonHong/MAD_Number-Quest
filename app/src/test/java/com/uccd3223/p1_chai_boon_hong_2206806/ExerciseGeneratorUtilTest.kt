package com.uccd3223.p1_chai_boon_hong_2206806

import com.uccd3223.p1_chai_boon_hong_2206806.util.ExerciseGeneratorUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExerciseGeneratorUtilTest {

    @Test
    fun recognitionOptionsContainTargetAndAreUnique() {
        repeat(100) {
            val result = ExerciseGeneratorUtil.generateRecognitionOptions(25, 50, 4)
            assertEquals(4, result.options.size)
            assertEquals(4, result.options.distinct().size)
            assertTrue(result.targetNumber in result.options)
            assertTrue(result.options.all { it in 1..50 })
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

    @Test(expected = IllegalArgumentException::class)
    fun sortSequenceRejectsImpossibleRequest() {
        ExerciseGeneratorUtil.generateSortSequence(5, 4)
    }

    @Test
    fun numberToWordsHandlesLearningRange() {
        assertEquals("One", ExerciseGeneratorUtil.numberToWords(1))
        assertEquals("Nineteen", ExerciseGeneratorUtil.numberToWords(19))
        assertEquals("Forty-Two", ExerciseGeneratorUtil.numberToWords(42))
        assertEquals("Ninety-Nine", ExerciseGeneratorUtil.numberToWords(99))
    }
}
