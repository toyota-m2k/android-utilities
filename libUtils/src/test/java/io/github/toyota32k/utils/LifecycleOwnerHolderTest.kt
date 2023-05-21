package io.github.toyota32k.utils

import androidx.appcompat.app.AppCompatActivity
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController

@RunWith(RobolectricTestRunner::class)
class LifecycleOwnerHolderTest {
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

    @Test
    fun lifecycleOwnerHolderTest() {
        var activity = createActivity()
        var disposed = false
        val holder = LifecycleOwnerHolder(activity) {
            disposed = true
        }

        assertFalse(disposed)
        finish()
        assertTrue(disposed)

        disposed = false
        activity = createActivity()
        holder.attachOwner(activity)
        assertFalse(disposed)

        finish()
        assertTrue(disposed)
    }
}