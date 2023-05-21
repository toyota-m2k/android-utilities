package io.github.toyota32k.utils

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.Closeable

class ObservableFlagTest {
    @Before
    fun setup() {
    }

    @Test
    fun flagTest() {
        val flag = UtObservableFlag()
        assertFalse(flag.flagged)
        assertTrue(flag.trySetIfNot())
        assertFalse(flag.trySetIfNot())
        assertTrue(flag.flagged)
        flag.reset()
        assertFalse(flag.flagged)
        assertTrue(flag.trySetIfNot())

        var b:Boolean
        b = flag.withFlagIfNot{ true } ?: false
        assertFalse(b)
        flag.reset()
        b = flag.withFlagIfNot { true } ?: false
        assertTrue(b)
        assertFalse(flag.flagged)

        b = flag.withFlag {
            assertTrue(flag.flagged)
            true
        }
        assertTrue(b)
        assertFalse(flag.flagged)

        var u:Closeable?
        u = flag.closeableSet()
        assertTrue(flag.flagged)
        u.close()
        assertFalse(flag.flagged)

        u = flag.closeableTrySetIfNot()
        assertNotNull(u)
        assertTrue(flag.flagged)

        val u2:Closeable?
        u2 = flag.closeableTrySetIfNot()
        assertNull(u2)

        u?.close()
        assertFalse(flag.flagged)

        flag.closeableSet().use {
            assertTrue(flag.flagged)
        }
        assertFalse(flag.flagged)
    }

    @Test
    fun counterTest() {
        val counter = UtObservableCounter()
        assertEquals(counter.count, 0)
        counter.set()
        assertEquals(counter.count, 1)
        counter.set()
        assertEquals(counter.count, 2)
        counter.reset()
        assertEquals(counter.count, 1)
        counter.onSet { ->
            assertEquals(counter.count, 2)
        }
        assertEquals(counter.count, 1)
        counter.withSetCounter {
            assertEquals(it, 2)
        }
        assertEquals(counter.count, 1)

        var u = counter.closeableSet()
        assertEquals(counter.count, 2)
        u.close()
        assertEquals(counter.count, 1)

        counter.closeableSet().use {
            assertEquals(counter.count, 2)
        }
        assertEquals(counter.count, 1)
    }
}