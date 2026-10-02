package io.github.tavim26.benchmark.benchmarks

import io.github.tavim26.benchmark.model.BenchmarkTest
import io.github.tavim26.benchmark.model.Category
import io.github.tavim26.benchmark.model.TestResult

/**
 * Software rendering into an ARGB framebuffer. Runs entirely on the CPU:
 * it measures per-pixel computation and memory bandwidth, not the GPU.
 */
class RenderingBenchmark : Benchmark {

    override val category = Category.RENDERING

    override fun run(): List<TestResult> = listOf(
        gradientFillTest(),
        mandelbrotTest()
    )

    private fun gradientFillTest(): TestResult {
        val framebuffer = IntArray(FULL_HD_WIDTH * FULL_HD_HEIGHT)
        val time = measureMillis {
            for (frame in 0 until GRADIENT_FRAMES) {
                renderGradient(framebuffer, FULL_HD_WIDTH, FULL_HD_HEIGHT, frame)
            }
        }
        Blackhole.consume(framebuffer[framebuffer.size / 2].toLong())
        return TestResult(
            BenchmarkTest.GRADIENT_FILL,
            time,
            "$GRADIENT_FRAMES frames at ${FULL_HD_WIDTH}×$FULL_HD_HEIGHT, ${fps(GRADIENT_FRAMES, time)} FPS"
        )
    }

    private fun mandelbrotTest(): TestResult {
        val framebuffer = IntArray(HD_WIDTH * HD_HEIGHT)
        val time = measureMillis {
            repeat(MANDELBROT_FRAMES) {
                renderMandelbrot(framebuffer, HD_WIDTH, HD_HEIGHT)
            }
        }
        Blackhole.consume(framebuffer[framebuffer.size / 2].toLong())
        return TestResult(
            BenchmarkTest.MANDELBROT,
            time,
            "$MANDELBROT_FRAMES frames at ${HD_WIDTH}×$HD_HEIGHT, " +
                "$MANDELBROT_MAX_ITERATIONS max iterations, ${fps(MANDELBROT_FRAMES, time)} FPS"
        )
    }

    private fun renderGradient(framebuffer: IntArray, width: Int, height: Int, frame: Int) {
        for (y in 0 until height) {
            val rowOffset = y * width
            val green = y * 255 / height
            for (x in 0 until width) {
                val red = (x + frame * 8) and 0xFF
                val blue = (x xor y) and 0xFF
                framebuffer[rowOffset + x] = argb(red, green, blue)
            }
        }
    }

    private fun renderMandelbrot(framebuffer: IntArray, width: Int, height: Int) {
        for (py in 0 until height) {
            val y0 = py.toDouble() / height * 2.4 - 1.2
            for (px in 0 until width) {
                val x0 = px.toDouble() / width * 3.5 - 2.5
                var x = 0.0
                var y = 0.0
                var iteration = 0
                while (x * x + y * y <= 4.0 && iteration < MANDELBROT_MAX_ITERATIONS) {
                    val nextX = x * x - y * y + x0
                    y = 2.0 * x * y + y0
                    x = nextX
                    iteration++
                }
                val shade = iteration * 255 / MANDELBROT_MAX_ITERATIONS
                framebuffer[py * width + px] = argb(shade, shade, shade)
            }
        }
    }

    private fun argb(red: Int, green: Int, blue: Int): Int =
        (0xFF shl 24) or (red shl 16) or (green shl 8) or blue

    private fun fps(frames: Int, timeMs: Double): String =
        (frames * 1000.0 / timeMs.coerceAtLeast(0.001)).formatOneDecimal()

    private companion object {
        const val FULL_HD_WIDTH = 1920
        const val FULL_HD_HEIGHT = 1080
        const val GRADIENT_FRAMES = 30
        const val HD_WIDTH = 1280
        const val HD_HEIGHT = 720
        const val MANDELBROT_FRAMES = 3
        const val MANDELBROT_MAX_ITERATIONS = 100
    }
}