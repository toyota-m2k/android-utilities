package io.github.toyota32k.utils

import androidx.appcompat.app.AppCompatActivity
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.github.toyota32k.utils.lifecycle.asMutableLiveData
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController

@RunWith(RobolectricTestRunner::class)
class MutableFlowTest {
    @Rule
    @JvmField
    val instantExecutorRule : InstantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var activityController : ActivityController<JustTestActivity>

    private fun createActivity(): AppCompatActivity {
        activityController = Robolectric.buildActivity(JustTestActivity::class.java)
        return activityController.create().start().get() as AppCompatActivity
    }
    private fun finish() {
        activityController.pause().destroy()
    }

    @Test
    fun asMutableLiveDataTest() {
        val activity = createActivity()

        val flow = MutableStateFlow(1)
        val liveData = flow.asMutableLiveData(activity)

        Assert.assertEquals(1, flow.value)
        Assert.assertEquals(1, liveData.value)
        flow.value = 2
        Assert.assertEquals(2, flow.value)
        Assert.assertEquals(2, liveData.value)
        liveData.value = 3
        Assert.assertEquals(3, flow.value)
        Assert.assertEquals(3, liveData.value)
        finish()
        liveData.value = 4
        Assert.assertEquals(3, flow.value)
        Assert.assertEquals(4, liveData.value)
        flow.value = 5
        Assert.assertEquals(5, flow.value)
        Assert.assertEquals(4, liveData.value)

    }
}