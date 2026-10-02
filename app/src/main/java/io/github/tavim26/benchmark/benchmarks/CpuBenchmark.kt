package io.github.tavim26.benchmark.benchmarks

import io.github.tavim26.benchmark.model.BenchmarkTest
import io.github.tavim26.benchmark.model.Category
import io.github.tavim26.benchmark.model.TestResult
import kotlin.random.Random

/**
 * Algorithmic CPU workloads. Input arrays are generated with a fixed seed before
 * timing starts, so every run measures the same work and only the algorithm is timed.
 */
class CpuBenchmark : Benchmark {

    override val category = Category.CPU

    override fun run(): List<TestResult> = listOf(
        factorialTest(),
        fibonacciTest(),
        bubbleSortTest(),
        quickSortTest()
    )

    private fun factorialTest(): TestResult {
        val time = measureMillis {
            repeat(FACTORIAL_RUNS) {
                Blackhole.consume(Algorithms.factorial(FACTORIAL_N).bitLength().toLong())
            }
        }
        return TestResult(BenchmarkTest.FACTORIAL, time, "n = $FACTORIAL_N, $FACTORIAL_RUNS runs")
    }

    private fun fibonacciTest(): TestResult {
        val time = measureMillis {
            repeat(FIBONACCI_RUNS) {
                Blackhole.consume(Algorithms.fibonacci(FIBONACCI_N).bitLength().toLong())
            }
        }
        return TestResult(BenchmarkTest.FIBONACCI, time, "n = $FIBONACCI_N, $FIBONACCI_RUNS runs")
    }

    private fun bubbleSortTest(): TestResult {
        val inputs = randomArrays(BUBBLE_SORT_RUNS, BUBBLE_SORT_SIZE)
        val time = measureMillis {
            for (array in inputs) {
                Algorithms.bubbleSort(array)
                Blackhole.consume(array[0].toLong())
            }
        }
        return TestResult(
            BenchmarkTest.BUBBLE_SORT,
            time,
            "$BUBBLE_SORT_SIZE integers, $BUBBLE_SORT_RUNS runs"
        )
    }

    private fun quickSortTest(): TestResult {
        val inputs = randomArrays(QUICK_SORT_RUNS, QUICK_SORT_SIZE)
        val time = measureMillis {
            for (array in inputs) {
                Algorithms.quickSort(array)
                Blackhole.consume(array[0].toLong())
            }
        }
        return TestResult(
            BenchmarkTest.QUICK_SORT,
            time,
            "$QUICK_SORT_SIZE integers, $QUICK_SORT_RUNS runs"
        )
    }

    private fun randomArrays(count: Int, size: Int): List<IntArray> {
        val random = Random(SEED)
        return List(count) { IntArray(size) { random.nextInt() } }
    }

    private companion object {
        const val FACTORIAL_N = 3_000
        const val FACTORIAL_RUNS = 20
        const val FIBONACCI_N = 10_000
        const val FIBONACCI_RUNS = 20
        const val BUBBLE_SORT_SIZE = 5_000
        const val BUBBLE_SORT_RUNS = 10
        const val QUICK_SORT_SIZE = 200_000
        const val QUICK_SORT_RUNS = 10
        const val SEED = 42
    }
}