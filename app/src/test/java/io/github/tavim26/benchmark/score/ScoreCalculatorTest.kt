package io.github.tavim26.benchmark.score

import io.github.tavim26.benchmark.model.BenchmarkTest
import io.github.tavim26.benchmark.model.TestResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScoreCalculatorTest {

    private val cpuTest = BenchmarkTest.QUICK_SORT
    private val memoryTest = BenchmarkTest.MEMORY_COPY

    private fun result(test: BenchmarkTest, timeMs: Double) = TestResult(test, timeMs, "")

    @Test
    fun referenceTime_scores1000() {
        val score = ScoreCalculator.testScore(result(cpuTest, cpuTest.referenceMs))
        assertEquals(1000.0, score, 1e-9)
    }

    @Test
    fun halfTheReferenceTime_doublesTheScore() {
        val score = ScoreCalculator.testScore(result(cpuTest, cpuTest.referenceMs / 2))
        assertEquals(2000.0, score, 1e-9)
    }

    @Test
    fun slowerThanReference_scoresLower() {
        val score = ScoreCalculator.testScore(result(cpuTest, cpuTest.referenceMs * 4))
        assertEquals(250.0, score, 1e-9)
    }

    @Test
    fun categoryScore_isGeometricMeanOfTestScores() {
        val results = listOf(
            result(cpuTest, cpuTest.referenceMs),      // 1000
            result(cpuTest, cpuTest.referenceMs / 4)   // 4000
        )
        assertEquals(2000, ScoreCalculator.categoryScore(results))
    }

    @Test
    fun overallScore_weighsCategoriesEqually() {
        val cpuResults = List(4) { result(cpuTest, cpuTest.referenceMs) }  // category score 1000
        val memoryResults = listOf(result(memoryTest, memoryTest.referenceMs / 4))  // category score 4000

        assertEquals(2000, ScoreCalculator.overallScore(listOf(cpuResults, memoryResults)))
    }

    @Test
    fun emptyInput_hasNoScore() {
        assertNull(ScoreCalculator.categoryScore(emptyList()))
        assertNull(ScoreCalculator.overallScore(emptyList()))
    }

    @Test
    fun zeroTime_doesNotProduceInfinity() {
        val score = ScoreCalculator.testScore(result(cpuTest, 0.0))
        assertTrue(score.isFinite())
    }
}