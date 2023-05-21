package io.github.toyota32k.utils

import androidx.appcompat.app.AppCompatActivity
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController

@RunWith(RobolectricTestRunner::class)
class ListenersTest {
    @Rule
    @JvmField
    val instantExecutorRule : InstantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var activityController :ActivityController<JustTestActivity>

    private fun createActivity():AppCompatActivity {
        activityController = Robolectric.buildActivity(JustTestActivity::class.java)
        return activityController.create().start().get() as AppCompatActivity
    }
    private fun finish() {
        activityController.pause().destroy()
    }

    @Test
    fun callbackTest() {
        var activity = createActivity()
        val callback = Callback<Int,String>()
        var text:String? = null

        callback.set(activity, {
            "v=$it".apply {
                text = this
            }
        })

        assertNull(text)
        var x = callback.invoke(1)
        assertEquals(text, x)
        assertEquals("v=1", text)
        x = callback.invoke(2)
        assertEquals(text, x)
        assertEquals("v=2", text)
        finish()
        x = callback.invoke(3)
        assertNull(x)
        assertEquals("v=2", text)

        activity = createActivity()
        callback.set(activity, {
            "x=$it".apply {
                text = this
            }
        })
        assertNull(x)
        assertEquals("v=2", text)
        x = callback.invoke(4)
        assertEquals("x=4", x)
        assertEquals("x=4", text)
    }

    @Test
    fun listenersTest() {
        var activity = createActivity()
        val listeners = Listeners<String>()
        var text1:String? = null
        var text2:String? = null

        listeners.add(activity) {
            text1 = "1:$it"
        }
        listeners.add(activity) {
            text2 = "2:$it"
        }

        assertNull(text1)
        assertNull(text2)

        listeners.invoke("A")
        assertEquals("1:A", text1)
        assertEquals("2:A", text2)
        listeners.invoke("B")
        assertEquals("1:B", text1)
        assertEquals("2:B", text2)

        finish()
        listeners.invoke("C")
        assertEquals("1:B", text1)
        assertEquals("2:B", text2)

        activity = createActivity()
        val k1 = listeners.add(activity) {
            text1 = "1-2:$it"
        }
        listeners.add(activity) {
            text2 = "2-2:$it"
        }

        assertEquals("1:B", text1)
        assertEquals("2:B", text2)
        listeners.invoke("D")
        assertEquals("1-2:D", text1)
        assertEquals("2-2:D", text2)

        k1.dispose()
        listeners.invoke("E")
        assertEquals("1-2:D", text1)
        assertEquals("2-2:E", text2)
    }
}