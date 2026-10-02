package io.github.tavim26.benchmark.benchmarks

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigInteger
import kotlin.random.Random

class AlgorithmsTest {

    @Test
    fun factorial_ofSmallNumbers() {
        assertEquals(BigInteger.ONE, Algorithms.factorial(0))
        assertEquals(BigInteger.ONE, Algorithms.factorial(1))
        assertEquals(BigInteger.valueOf(120), Algorithms.factorial(5))
        assertEquals(BigInteger("2432902008176640000"), Algorithms.factorial(20))
    }

    @Test
    fun factorial_beyondLongRange() {
        // 21! does not fit in a Long, which the original implementation relied on.
        assertEquals(BigInteger("51090942171709440000"), Algorithms.factorial(21))
    }

    @Test
    fun fibonacci_knownValues() {
        assertEquals(BigInteger.ZERO, Algorithms.fibonacci(0))
        assertEquals(BigInteger.ONE, Algorithms.fibonacci(1))
        assertEquals(BigInteger.ONE, Algorithms.fibonacci(2))
        assertEquals(BigInteger.valueOf(55), Algorithms.fibonacci(10))
        assertEquals(BigInteger("354224848179261915075"), Algorithms.fibonacci(100))
    }

    @Test
    fun bubbleSort_sortsRandomArray() {
        val random = Random(1)
        val array = IntArray(500) { random.nextInt() }
        val expected = array.sortedArray()

        Algorithms.bubbleSort(array)

        assertArrayEquals(expected, array)
    }

    @Test
    fun quickSort_sortsRandomArray() {
        val random = Random(2)
        val array = IntArray(10_000) { random.nextInt() }
        val expected = array.sortedArray()

        Algorithms.quickSort(array)

        assertArrayEquals(expected, array)
    }

    @Test
    fun quickSort_handlesSortedReversedAndDuplicateInput() {
        val inputs = listOf(
            IntArray(1_000) { it },
            IntArray(1_000) { 1_000 - it },
            IntArray(1_000) { it % 3 },
            IntArray(1_000) { 7 }
        )
        for (array in inputs) {
            val expected = array.sortedArray()
            Algorithms.quickSort(array)
            assertArrayEquals(expected, array)
        }
    }

    @Test
    fun sorts_handleEmptyAndSingleElementArrays() {
        val empty = IntArray(0)
        Algorithms.bubbleSort(empty)
        Algorithms.quickSort(empty)
        assertArrayEquals(IntArray(0), empty)

        val single = intArrayOf(42)
        Algorithms.bubbleSort(single)
        Algorithms.quickSort(single)
        assertArrayEquals(intArrayOf(42), single)
    }
}