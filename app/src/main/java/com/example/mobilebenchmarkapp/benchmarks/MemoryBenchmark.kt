package com.example.mobilebenchmarkapp.benchmarks

import android.content.Context
import java.io.File
import java.io.RandomAccessFile
import java.util.concurrent.Executors
import kotlin.random.Random
import kotlin.system.measureTimeMillis

class MemoryBenchmark(
    private val context: Context,
    private val onResult: (String) -> Unit
) {
    private val executor = Executors.newFixedThreadPool(3)
    private val resultsFile = File(context.filesDir, "memory_results.txt")

    fun run() {
        executor.submit { runWriteTest(1) }
        executor.submit { runReadTest(1) }
        executor.submit { runMatrixAllocationTest(10, 2000) }
    }

    private fun runWriteTest(mb: Int) {
        val tempFile = createTempFile()

        val writeTime = measureTimeMillis {
            val data = ByteArray(mb * 1024 * 1024) { Random.nextBytes(1)[0] }
            writeToFile(tempFile, data)
        }

        val result = "Memory Write Test ($mb MB): $writeTime ms"
        logResult(result)
        tempFile.delete()
    }

    private fun runReadTest(mb: Int) {
        val tempFile = createTempFile()

        val readTime = measureTimeMillis {

            val data = ByteArray(mb * 1024 * 1024) { Random.nextBytes(1)[0] }
            writeToFile(tempFile, data)
            readFromFile(tempFile)
        }

        val result = "Memory Read Test ($mb MB): $readTime ms"
        logResult(result)
        tempFile.delete()
    }

    private fun runMatrixAllocationTest(n: Int, m: Int) {
        val allocationTime = measureTimeMillis {
            repeat(n) {
                Array(m) { IntArray(m) { Random.nextInt(0, 100) } }
            }
        }

        val result = "Matrix Allocation Test ($n matrices of $m x $m): $allocationTime ms"
        logResult(result)
    }

    private fun createTempFile(): File {
        return File.createTempFile("benchmark_", ".tmp").apply {
            deleteOnExit()
        }
    }

    private fun writeToFile(file: File, data: ByteArray) {
        RandomAccessFile(file, "rw").use { raf ->
            raf.write(data)
        }
    }

    private fun readFromFile(file: File): ByteArray {
        return RandomAccessFile(file, "r").use { raf ->
            val fileLength = raf.length().toInt()
            val buffer = ByteArray(fileLength)
            raf.readFully(buffer)
            buffer
        }
    }

    private fun logResult(result: String) {
        onResult(result)

        resultsFile.appendText("$result\n")
    }
}
