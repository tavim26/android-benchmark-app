package io.github.tavim26.benchmark.score

import io.github.tavim26.benchmark.model.TestResult
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.roundToInt

/**
 * Converts execution times into scores where higher is better.
 *
 * - Test score: 1000 × reference time / measured time.
 * - Category score: geometric mean of its test scores.
 * - Overall score: geometric mean of the category scores, so every category
 *   weighs the same regardless of how many tests it contains.
 *
 * The geometric mean is the standard way to combine benchmark ratios:
 * a 2× improvement on any test changes the result by the same factor.
 */
object ScoreCalculator {

    const val REFERENCE_SCORE = 1000.0
    private const val MIN_TIME_MS = 0.001

    fun testScore(result: TestResult): Double =
        REFERENCE_SCORE * result.test.referenceMs / result.timeMs.coerceAtLeast(MIN_TIME_MS)

    fun categoryScore(results: List<TestResult>): Int? =
        categoryScoreExact(results)?.roundToInt()

    fun overallScore(resultsByCategory: Collection<List<TestResult>>): Int? =
        geometricMean(resultsByCategory.mapNotNull { categoryScoreExact(it) })?.roundToInt()

    fun geometricMean(values: List<Double>): Double? {
        if (values.isEmpty()) return null
        return exp(values.sumOf { ln(it) } / values.size)
    }

    private fun categoryScoreExact(results: List<TestResult>): Double? =
        geometricMean(results.map { testScore(it) })
}