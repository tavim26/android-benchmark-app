package io.github.tavim26.benchmark.benchmarks

import java.math.BigInteger

/** Pure algorithm implementations used by the CPU benchmark. Kept separate so they can be unit tested. */
object Algorithms {

    fun factorial(n: Int): BigInteger {
        require(n >= 0) { "n must be non-negative" }
        var result = BigInteger.ONE
        for (i in 2..n) {
            result = result.multiply(BigInteger.valueOf(i.toLong()))
        }
        return result
    }

    /** Returns the n-th Fibonacci number, where fibonacci(0) = 0 and fibonacci(1) = 1. */
    fun fibonacci(n: Int): BigInteger {
        require(n >= 0) { "n must be non-negative" }
        var current = BigInteger.ZERO
        var next = BigInteger.ONE
        repeat(n) {
            val sum = current.add(next)
            current = next
            next = sum
        }
        return current
    }

    /** Sorts [array] in place. Stops early if a pass makes no swaps. */
    fun bubbleSort(array: IntArray) {
        val n = array.size
        for (i in 0 until n - 1) {
            var swapped = false
            for (j in 0 until n - i - 1) {
                if (array[j] > array[j + 1]) {
                    array.swap(j, j + 1)
                    swapped = true
                }
            }
            if (!swapped) return
        }
    }

    /** Sorts [array] in place. */
    fun quickSort(array: IntArray) {
        quickSort(array, 0, array.lastIndex)
    }

    /**
     * Recurses into the smaller partition and loops over the larger one,
     * so the recursion depth stays O(log n) even for unfavorable inputs.
     */
    private fun quickSort(array: IntArray, low: Int, high: Int) {
        var lo = low
        var hi = high
        while (lo < hi) {
            val pivotIndex = partition(array, lo, hi)
            if (pivotIndex - lo < hi - pivotIndex) {
                quickSort(array, lo, pivotIndex - 1)
                lo = pivotIndex + 1
            } else {
                quickSort(array, pivotIndex + 1, hi)
                hi = pivotIndex - 1
            }
        }
    }

    /** Lomuto partition using the middle element as pivot, which avoids the worst case on sorted input. */
    private fun partition(array: IntArray, low: Int, high: Int): Int {
        val middle = low + (high - low) / 2
        array.swap(middle, high)
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
}