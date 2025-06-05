package io.github.toyota32k.utils

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class DisposerTest {

    class TestDisposable(override var disposed: Boolean = false) : IDisposableEx {
        override fun dispose() {
            disposed = true
        }
    }

    private lateinit var disposer: Disposer

    @Before
    fun setup() {
        disposer = Disposer()
    }

    @Test
    fun testInitialState() {
        assertFalse(disposer.disposed)
        assertEquals(0, disposer.count)
    }

    @Test
    fun testRegisterAndCount() {
        val d1 = TestDisposable()
        val d2 = TestDisposable()

        disposer.register(d1, d2)

        assertEquals(2, disposer.count)
        assertFalse(disposer.disposed)
    }

    @Test
    fun testPlusOperator() {
        val d1 = TestDisposable()
        val d2 = TestDisposable()

        disposer + d1 + d2

        assertEquals(2, disposer.count)
        assertFalse(disposer.disposed)
    }

    @Test
    fun testDispose() {
        val d1 = TestDisposable()
        val d2 = TestDisposable()

        disposer + d1 + d2
        disposer.dispose()

        assertTrue(d1.disposed)
        assertTrue(d2.disposed)
        assertTrue(disposer.disposed)
        assertEquals(0, disposer.count)
    }

    @Test
    fun testReset() {
        val d1 = TestDisposable()
        val d2 = TestDisposable()

        disposer + d1 + d2
        disposer.reset()

        assertTrue(d1.disposed)
        assertTrue(d2.disposed)
        assertEquals(0, disposer.count)
        assertFalse(disposer.disposed)  // resetではdisposedフラグはクリアされる
    }

    @Test
    fun testUnregister() {
        val d1 = TestDisposable()
        val d2 = TestDisposable()
        val d3 = TestDisposable()

        disposer + d1 + d2 + d3
        assertEquals(3, disposer.count)

        disposer.unregister(d2)

        assertTrue(d2.disposed)
        assertFalse(d1.disposed)
        assertFalse(d3.disposed)
        assertEquals(2, disposer.count)
    }

    @Test
    fun testMinusOperator() {
        val d1 = TestDisposable()
        val d2 = TestDisposable()

        disposer + d1 + d2
        disposer - d1

        assertTrue(d1.disposed)
        assertFalse(d2.disposed)
        assertEquals(1, disposer.count)
    }

    @Test
    fun testClientData() {
        val clientData = TestDisposable()
        disposer.clientData = clientData

        disposer.reset()

        assertTrue(clientData.disposed)
        assertNull(disposer.clientData)
    }

    @Test
    fun testNonDisposableClientData() {
        val clientData = "テストデータ"
        disposer.clientData = clientData

        disposer.reset()

        // 非IDisposableなclientDataはresetで消えない
        assertEquals("テストデータ", disposer.clientData)
    }

    @Test
    fun testClean() {
        val d1 = TestDisposable(true)  // すでにdisposed
        val d2 = TestDisposable()      // まだdisposedされていない
        val d3 = TestDisposable(true)  // すでにdisposed

        disposer + d1 + d2 + d3
        assertEquals(3, disposer.count)

        disposer.clean()

        assertEquals(1, disposer.count) // d2のみ残る
    }

    @Test
    fun testReRegisterAfterDispose() {
        val d1 = TestDisposable()

        disposer + d1
        disposer.dispose()
        assertTrue(disposer.disposed)

        val d2 = TestDisposable()
        disposer + d2

        assertFalse(disposer.disposed)  // register()でdisposedフラグはクリアされる
        assertEquals(1, disposer.count)
    }
}