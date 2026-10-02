package io.github.tavim26.benchmark.model

/** A group of related tests that run together and receive a combined score. */
enum class Category(val displayName: String) {
    CPU("CPU"),
    RENDERING("Rendering (software, on CPU)"),
    MEMORY("Memory & storage")
}

/**
 * Every individual test in the app.
 *
 * [referenceMs] is the estimated execution time on a reference mid-range device.
 * A device that matches it scores 1000 points on that test; twice as fast scores 2000.
 */
enum class BenchmarkTest(
    val category: Category,
    val displayName: String,
    val referenceMs: Double
) {
    FACTORIAL(Category.CPU, "Factorial (BigInteger)", 60.0),
    FIBONACCI(Category.CPU, "Fibonacci (BigInteger)", 200.0),
    BUBBLE_SORT(Category.CPU, "Bubble sort", 400.0),
    QUICK_SORT(Category.CPU, "Quick sort", 150.0),

    GRADIENT_FILL(Category.RENDERING, "Gradient fill", 300.0),
    MANDELBROT(Category.RENDERING, "Mandelbrot set", 150.0),

    MATRIX_ALLOCATION(Category.MEMORY, "Matrix allocation", 300.0),
    MEMORY_COPY(Category.MEMORY, "RAM copy", 80.0),
    STORAGE_WRITE(Category.MEMORY, "Storage write", 60.0),
    STORAGE_READ(Category.MEMORY, "Storage read", 10.0)
}

/** The outcome of one test: total execution time plus a human-readable description. */
data class TestResult(
    val test: BenchmarkTest,
    val timeMs: Double,
    val detail: String
)