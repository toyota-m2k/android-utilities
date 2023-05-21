package io.github.toyota32k.utils

import androidx.appcompat.app.AppCompatActivity
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController

@RunWith(RobolectricTestRunner::class)
class SingleLiveEventTest {
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
    fun singleEventTest() {
        var activity = createActivity()
        val event = SingleLiveEvent<Int>()
        var value = 0
        val observer = object: Observer<Int> {
            override fun onChanged(t: Int) {
                value = t
            }
        }

        event.observe(activity,observer)
        assertEquals(0, value,)

        // Activityが生きている間は、fire()した値が、普通にイベントとして受け取れる
        event.fire(2)
        assertEquals(2, value,)
        event.fire(4)
        assertEquals(4, value,)

        // Activity が死んだら、イベントは発行されない
        finish()
        event.fire(6)
        assertEquals(4, value,)
        event.removeObserver(observer)

        // 新しいActivityでobserverし直したら（）、死んでいる間に発行されたイベントが受け取れる
        activity = createActivity()
        event.observe(activity,observer)
        assertEquals(6, value,)

        // removeObserverしたら、当然、イベントは受け取らない
        event.removeObserver(observer)
        event.fire(8)
        assertEquals(6, value,)

        // observeし直したら、イベントが来る
        event.observe(activity,observer)
        assertEquals(8, value,)

        finish()
        event.removeObserver(observer)
    }
}