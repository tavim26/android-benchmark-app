package com.example.mobilebenchmarkapp.benchmarks

import android.content.Context
import java.io.File
import java.util.concurrent.Executors
import kotlin.system.measureTimeMillis

class CpuBenchmark(
    private val context: Context,
    private val onResult: (String) -> Unit
) {
    private val executor = Executors.newFixedThreadPool(4)
    private val resultsFile = File(context.filesDir, "cpu_results.txt")

    fun run() {
        executor.submit { runFactorialTest() }
        executor.submit { runFibonacciTest() }
        executor.submit { runBubbleSortTest() }
        executor.submit { runQuickSortTest() }
    }

    private fun runFactorialTest() {
        val totalTime = measureTotalTime {
            repeat(10) { calculateFactorial(1000) }
        }
        logResult("CPU Factorial Test (100 runs) total time: $totalTime ms")
    }

    private fun runFibonacciTest() {
        val totalTime = measureTotalTime {
            repeat(10) { generateFibonacci(1000) }
        }
        logResult("CPU Fibonacci Test (100 runs) total time: $totalTime ms")
    }

    private fun runBubbleSortTest() {
        val totalTime = measureTotalTime {
            repeat(10) { bubbleSort(generateRandomArray(50000)) }
        }
        logResult("CPU Bubble Sort Test (10 runs) total time: $totalTime ms")
    }

    private fun runQuickSortTest() {
        val totalTime = measureTotalTime {
            repeat(10) { quickSort(generateRandomArray(50000), 0, 9999) }
        }
        logResult("CPU Quick Sort Test (10 runs) total time: $totalTime ms")
    }

    private inline fun measureTotalTime(action: () -> Unit): Long {
        return measureTimeMillis { action() }
    }

    private fun calculateFactorial(n: Int): Long {
        var result = 1L
        for (i in 2..n) {
            result *= i
        }
        return result
    }

    private fun generateFibonacci(n: Int): Long {
        var a = 0L
        var b = 1L
        for (i in 2..n) {
            val temp = a + b
            a = b
            b = temp
        }
        return b
    }

    private fun bubbleSort(array: IntArray): IntArray {
        val n = array.size
        for (i in 0 until n - 1) {
            for (j in 0 until n - i - 1) {
                if (array[j] > array[j + 1]) {
                    val temp = array[j]
                    array[j] = array[j + 1]
                    array[j + 1] = temp
                }
            }
        }
        return array
    }

    private fun quickSort(array: IntArray, low: Int, high: Int) {
        if (low < high) {
            val pi = partition(array, low, high)
            quickSort(array, low, pi - 1)
            quickSort(array, pi + 1, high)
        }
    }

    private fun partition(array: IntArray, low: Int, high: Int): Int {
        val pivot = array[high]
        var i = low - 1
        for (j in low until high) {
            if (array[j] <= pivot) {
                i++
                array.swap(i, j)
            }
        }
        array.swap(i + 1, high)
        return i + 1
    }

    private fun IntArray.swap(i: Int, j: Int) {
        val temp = this[i]
        this[i] = this[j]
        this[j] = temp
    }

    private fun generateRandomArray(size: Int): IntArray {
        return IntArray(size) { (0..9999).random() }
    }

    private fun logResult(result: String) {
        onResult(result)

        resultsFile.appendText("$result\n")
    }
}
