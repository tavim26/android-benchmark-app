package io.github.tavim26.benchmark.benchmarks

import io.github.tavim26.benchmark.model.BenchmarkTest
import io.github.tavim26.benchmark.model.Category
import io.github.tavim26.benchmark.model.TestResult
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.random.Random

/**
 * RAM tests (allocation, copy throughput) and storage tests (file write and read).
 * Test data is prepared before timing starts.
 *
 * @param workDir directory for the temporary storage test file (the app's cache directory).
 */
class MemoryBenchmark(private val workDir: File) : Benchmark {

    override val category = Category.MEMORY

    override fun run(): List<TestResult> =
        listOf(matrixAllocationTest(), memoryCopyTest()) + storageTests()

    private fun matrixAllocationTest(): TestResult {
        val time = measureMillis {
            repeat(MATRIX_COUNT) { index ->
                val matrix = Array(MATRIX_SIZE) { row -> IntArray(MATRIX_SIZE) { column -> row + column + index } }
                Blackhole.consume(matrix[MATRIX_SIZE - 1][MATRIX_SIZE - 1].toLong())
            }
        }
        return TestResult(
            BenchmarkTest.MATRIX_ALLOCATION,
            time,
            "$MATRIX_COUNT matrices of ${MATRIX_SIZE}×$MATRIX_SIZE integers"
        )
    }

    private fun memoryCopyTest(): TestResult {
        val source = IntArray(COPY_ELEMENTS) { it }
        val destination = IntArray(COPY_ELEMENTS)
        val time = measureMillis {
            repeat(COPY_RUNS) {
                System.arraycopy(source, 0, destination, 0, COPY_ELEMENTS)
            }
        }
        Blackhole.consume(destination[COPY_ELEMENTS - 1].toLong())
        val megabytes = COPY_ELEMENTS * BYTES_PER_INT * COPY_RUNS / BYTES_PER_MB
        return TestResult(
            BenchmarkTest.MEMORY_COPY,
            time,
            "${megabytes.toInt()} MB copied, ${throughput(megabytes, time)} MB/s"
        )
    }

    private fun storageTests(): List<TestResult> {
        val data = ByteArray(FILE_SIZE_MB * BYTES_PER_MB.toInt())
        Random(SEED).nextBytes(data)
        val file = File(workDir, "storage_benchmark.tmp")

        try {
            val writeTime = measureMillis {
                FileOutputStream(file).use { output ->
                    output.write(data)
                    // Forces the data to physical storage instead of only the OS write cache.
                    output.fd.sync()
                }
            }

            val buffer = ByteArray(data.size)
            val readTime = measureMillis {
                FileInputStream(file).use { input ->
                    var offset = 0
                    while (offset < buffer.size) {
                        val bytesRead = input.read(buffer, offset, buffer.size - offset)
                        if (bytesRead < 0) break
                        offset += bytesRead
                    }
                }
            }
            Blackhole.consume(buffer[buffer.size - 1].toLong())

            val megabytes = FILE_SIZE_MB.toDouble()
            return listOf(
                TestResult(
                    BenchmarkTest.STORAGE_WRITE,
                    writeTime,
                    "$FILE_SIZE_MB MB, ${throughput(megabytes, writeTime)} MB/s"
                ),
                TestResult(
                    BenchmarkTest.STORAGE_READ,
                    readTime,
                    "$FILE_SIZE_MB MB, ${throughput(megabytes, readTime)} MB/s (may be served from the OS cache)"
                )
            )
        } finally {
            file.delete()
        }
    }

    private fun throughput(megabytes: Double, timeMs: Double): String =
        (megabytes * 1000.0 / timeMs.coerceAtLeast(0.001)).formatOneDecimal()

    private companion object {
        const val MATRIX_COUNT = 10
        const val MATRIX_SIZE = 2_000
        const val COPY_ELEMENTS = 4 * 1024 * 1024 // 16 MB of integers
        const val COPY_RUNS = 20
        const val FILE_SIZE_MB = 8
        const val BYTES_PER_INT = 4.0
        const val BYTES_PER_MB = 1024.0 * 1024.0
        const val SEED = 42
    }
}