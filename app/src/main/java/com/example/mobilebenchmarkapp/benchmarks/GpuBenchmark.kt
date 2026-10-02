package com.example.mobilebenchmarkapp.benchmarks

import android.content.Context
import java.io.File
import java.util.concurrent.Executors
import kotlin.random.Random
import kotlin.system.measureTimeMillis

class GpuBenchmark(
    private val context: Context,
    private val onResult: (String) -> Unit
) {

    private val executor = Executors.newFixedThreadPool(4)
    private val resultsFile = File(context.filesDir, "gpu_results.txt")

    data class Color(val r: Byte, val g: Byte, val b: Byte, val a: Byte = 255.toByte())

    private val width = 1920
    private val height = 1080

    fun run()
    {
        executor.submit { runTest(5, "GPU Test 1 (5 frames)") }
        executor.submit { runTest(10, "GPU Test 2 (10 frames)") }
        executor.submit { runTest(15, "GPU Test 3 (15 frames)") }
        executor.submit { runTest(20, "GPU Test 4 (20 frames)") }
    }

    private fun runTest(testDurationFrames: Int, testName: String)
    {
        val totalTime = measureTotalTime {
            for (frame in 1 until testDurationFrames)
            {
                val framebuffer = Array(width) { Array(height) { Color(1, 3, 4) } }

                applyEffect(framebuffer)
            }
        }

        val averageFrameTime = totalTime
        logResult("$testName: Average Frame Time = $averageFrameTime ms")
    }

    private fun applyEffect(framebuffer: Array<Array<Color>>)
    {
        for (x in framebuffer.indices)
        {
            for (y in framebuffer[x].indices)
            {
                framebuffer[x][y] = calculateColor(x, y)
            }
        }
    }

    private fun calculateColor(x: Int, y: Int): Color
    {
        val r = ((x * y) % 256).toByte()
        val g = ((x + y) % 256).toByte()
        val b = (Random.nextInt(0, 256)).toByte()
        return Color(r, g, b)
    }


    private inline fun measureTotalTime(action: () -> Unit): Long
    {
        return measureTimeMillis { action() }
    }


    private fun logResult(result: String)
    {
        onResult(result)
        resultsFile.appendText("$result\n")
    }
}
