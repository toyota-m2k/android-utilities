package io.github.toyota32k.utils

import androidx.appcompat.app.AppCompatActivity
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController

@RunWith(RobolectricTestRunner::class)
class LifecycleDisposerTest {
    @Rule
    @JvmField
    val instantExecutorRule : InstantTaskExecutorRule = InstantTaskExecutorRule()

    lateinit var activityController : ActivityController<JustTestActivity>

    fun createActivity(): AppCompatActivity {
        activityController = Robolectric.buildActivity(JustTestActivity::class.java)
        return activityController.create().start().get() as AppCompatActivity
    }
    fun finish() {
        activityController.pause().destroy()
    }

    class TestDisposable(override var disposed: Boolean=false) : IDisposableEx {
        override fun dispose() {
            disposed = true
        }
    }

    @Test
    fun lifecycleDisposerTest() {
        val activity = createActivity()
        val d1 = TestDisposable()
        val d2 = TestDisposable()

        val disposer = LifecycleDisposer(activity) + d1 + d2
        assertFalse(d1.disposed)
        assertFalse(d2.disposed)
        assertEquals(2, disposer.count)

        disposer.dispose()
        assertTrue(d1.disposed)
        assertTrue(d2.disposed)
        assertTrue(disposer.disposed)
        assertEquals(0, disposer.count)

        d1.disposed = false
        d2.disposed = false
        disposer + d1 + d2
        assertFalse(disposer.disposed)  // register()で disposedフラグはクリアされる
        assertEquals(2, disposer.count)
        assertFalse(d1.disposed)
        assertFalse(d2.disposed)

        finish()
        assertTrue(d1.disposed)
        assertTrue(d2.disposed)
        assertEquals(0, disposer.count)
    }
}