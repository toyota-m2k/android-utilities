package io.github.toyota32k.utils

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class UtObservableFlagTest {
    private lateinit var flag: UtObservableFlag

    @Before
    fun setUp() {
        flag = UtObservableFlag()
    }

    @Test
    fun initialValueIsFalse() {
        assertFalse(flag.flagged)
    }

    @Test
    fun setMakesFlagTrue() {
        flag.set()
        assertTrue(flag.flagged)
    }

    @Test
    fun resetMakesFlagFalse() {
        flag.set()
        assertTrue(flag.flagged)

        flag.reset()
        assertFalse(flag.flagged)
    }

    @Test
    fun multipleSetIncreasesCounter() {
        flag.set()
        flag.set()
        assertTrue(flag.flagged)

        // 1回のresetではフラグはまだtrueのはず
        flag.reset()
        assertTrue(flag.flagged)

        // 2回目のresetでフラグがfalseになる
        flag.reset()
        assertFalse(flag.flagged)
    }

    @Test
    fun trySetIfNotReturnsTrueWhenNotFlagged() {
        assertTrue(flag.trySetIfNot())
        assertTrue(flag.flagged)
    }

    @Test
    fun trySetIfNotReturnsFalseWhenFlagged() {
        flag.set()
        assertFalse(flag.trySetIfNot())
        assertTrue(flag.flagged)
    }

    @Test
    fun withFlagExecutesAndResets() {
        val result = flag.withFlag {
            assertTrue(flag.flagged)
            "test result"
        }

        assertEquals("test result", result)
        assertFalse(flag.flagged)
    }

    @Test
    fun withFlagIfNotExecutesWhenNotFlagged() {
        val result = flag.withFlagIfNot {
            assertTrue(flag.flagged)
            "test result"
        }

        assertEquals("test result", result)
        assertFalse(flag.flagged)
    }

    @Test
    fun withFlagIfNotReturnsNullWhenFlagged() {
        flag.set()

        val result = flag.withFlagIfNot {
            fail("This should not be executed")
            "unreachable"
        }

        assertNull(result)
        assertTrue(flag.flagged)

        flag.reset()
    }

    @Test
    fun closeableSetClosesCorrectly() {
        val closeable = flag.closeableSet()
        assertTrue(flag.flagged)

        closeable.close()
        assertFalse(flag.flagged)
    }

    @Test
    fun closeableTrySetIfNotReturnsCloseableWhenNotFlagged() {
        val closeable = flag.closeableTrySetIfNot()
        assertNotNull(closeable)
        assertTrue(flag.flagged)

        closeable?.close()
        assertFalse(flag.flagged)
    }

    @Test
    fun closeableTrySetIfNotReturnsNullWhenFlagged() {
        flag.set()

        val closeable = flag.closeableTrySetIfNot()
        assertNull(closeable)
        assertTrue(flag.flagged)

        flag.reset()
    }

    @Test
    fun withFlagHandlesExceptions() {
        try {
            flag.withFlag {
                assertTrue(flag.flagged)
                throw RuntimeException("Test exception")
            }
            fail("Exception should be propagated")
        } catch (e: RuntimeException) {
            assertEquals("Test exception", e.message)
        }

        assertFalse(flag.flagged)
    }

    @Test
    fun flowEmitsCurrentValue() = runBlocking {
        assertFalse(flag.first())

        flag.set()
        assertTrue(flag.first())

        flag.reset()
        assertFalse(flag.first())
    }
}