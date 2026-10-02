package io.github.tavim26.benchmark.benchmarks

import io.github.tavim26.benchmark.model.Category
import io.github.tavim26.benchmark.model.TestResult
import java.util.Locale

/** A set of tests for one [Category]. */
interface Benchmark {

    val category: Category

    /** Runs all tests sequentially. This call is blocking: invoke it from a background thread. */
    fun run(): List<TestResult>
}

/** Measures the execution time of [block] in milliseconds, with nanosecond precision. */
internal inline fun measureMillis(block: () -> Unit): Double {
    val start = System.nanoTime()
    block()
    return (System.nanoTime() - start) / 1_000_000.0
}

/**
 * Consumes computed values so the runtime cannot treat the benchmarked work
 * as dead code and optimize it away.
 */
internal object Blackhole {

    @Volatile
    private var sink: Long = 0

    fun consume(value: Long) {
        sink = sink xor value
    }
}

internal fun Double.formatOneDecimal(): String = String.format(Locale.US, "%.1f", this)