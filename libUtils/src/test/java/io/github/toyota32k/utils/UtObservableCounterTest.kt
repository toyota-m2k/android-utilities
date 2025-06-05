package io.github.toyota32k.utils

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class UtObservableCounterTest {
    private lateinit var counter: UtObservableCounter

    @Before
    fun setUp() {
        counter = UtObservableCounter()
    }

    @Test
    fun initialValueIsZero() {
        assertEquals(0, counter.count)
    }

    @Test
    fun setIncreasesCounter() {
        assertEquals(1, counter.set())
        assertEquals(1, counter.count)
    }

    @Test
    fun resetDecreasesCounter() {
        counter.set()
        assertEquals(0, counter.reset())
        assertEquals(0, counter.count)
    }

    @Test(expected = AssertionError::class)
    fun resetWithZeroCountThrowsAssertion() {
        counter.reset()
    }

    @Test
    fun onSetExecutesFunctionAndResets() {
        val result = counter.onSet {
            assertEquals(1, counter.count)
            "test result"
        }

        assertEquals("test result", result)
        assertEquals(0, counter.count)
    }

    @Test
    fun withSetCounterProvidesCounterValue() {
        counter.set() // 1回目: 1

        val result = counter.withSetCounter { counterValue ->
            assertEquals(2, counterValue)
            assertEquals(2, counter.count)
            counterValue * 10
        }

        assertEquals(20, result)
        assertEquals(1, counter.count)
    }

    @Test
    fun closeableSetClosesCorrectly() {
        val closeable = counter.closeableSet()
        assertEquals(1, counter.count)

        closeable.close()
        assertEquals(0, counter.count)
    }

    @Test
    fun flowEmitsCurrentValue() = runBlocking {
        assertEquals(0, counter.first())

        counter.set()
        assertEquals(1, counter.first())

        counter.reset()
        assertEquals(0, counter.first())
    }

    @Test
    fun multipleSetAndResetOperations() {
        repeat(5) { counter.set() }
        assertEquals(5, counter.count)

        repeat(3) { counter.reset() }
        assertEquals(2, counter.count)
    }

    @Test
    fun onSetHandlesExceptions() {
        try {
            counter.onSet {
                assertEquals(1, counter.count)
                throw RuntimeException("Test exception")
            }
            fail("Exception should be propagated")
        } catch (e: RuntimeException) {
            assertEquals("Test exception", e.message)
        }

        assertEquals(0, counter.count)
    }
}