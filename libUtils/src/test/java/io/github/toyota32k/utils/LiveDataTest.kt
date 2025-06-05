package io.github.toyota32k.utils

import androidx.appcompat.app.AppCompatActivity
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import io.github.toyota32k.utils.lifecycle.and
import io.github.toyota32k.utils.lifecycle.combineLatest
import io.github.toyota32k.utils.lifecycle.not
import io.github.toyota32k.utils.lifecycle.or
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController

@RunWith(RobolectricTestRunner::class)
class LiveDataTest {
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
    fun combineTest() {
        val activity = createActivity()

        val source0 = MutableLiveData<Int>(0)
        val source1 = MutableLiveData<Int>(0)
        val source2 = MutableLiveData<Int>(0)
        val source3 = MutableLiveData<Int>(0)
        val source4 = MutableLiveData<Int>(0)

        val c2 = combineLatest(source0, source1) { a, b ->
            (a ?: 0) + (b ?: 0)
        }
        val c3 = combineLatest(source0, source1, source2) { a, b, c ->
            (a ?: 0) + (b ?: 0) + (c ?: 0)
        }
        val c4 = combineLatest(source0, source1, source2, source3) { a, b, c, d ->
            (a ?: 0) + (b ?: 0) + (c ?: 0) + (d ?: 0)
        }
        val c5 = combineLatest(source0, source1, source2, source3, source4) { a, b, c, d, e ->
            (a ?: 0) + (b ?: 0) + (c ?: 0) + (d ?: 0) + (e ?: 0)
        }
        assertEquals(0,c2.value)
        assertEquals(0,c3.value)
        assertEquals(0,c4.value)
        assertEquals(0,c5.value)

        // Observerがセットされていない状態では、MediatorLiveDataの値は変化しない
        source0.value = 1
        assertEquals(0,c2.value)
        assertEquals(0,c3.value)
        assertEquals(0,c4.value)
        assertEquals(0,c5.value)

//        c2.observe(activity) {}
//        c3.observe(activity) {}
//        c4.observe(activity) {}
//        c5.observe(activity) {}

        val m = combineLatest(c2, c3, c4, c5) { a, b, c, d ->
            (a ?: 0) + (b ?: 0) + (c ?: 0) + (d ?: 0)

        }
        m.observe(activity) {}


        assertEquals(1,c2.value)
        assertEquals(1,c3.value)
        assertEquals(1,c4.value)
        assertEquals(1,c5.value)

        source1.value = 2
        assertEquals(3,c2.value)
        assertEquals(3,c3.value)
        assertEquals(3,c4.value)
        assertEquals(3,c5.value)

        source2.value = 3
        assertEquals(3,c2.value)
        assertEquals(6,c3.value)
        assertEquals(6,c4.value)
        assertEquals(6,c5.value)

        source3.value = 4
        assertEquals(3,c2.value)
        assertEquals(6,c3.value)
        assertEquals(10,c4.value)
        assertEquals(10,c5.value)

        source4.value = 5
        assertEquals(3,c2.value)
        assertEquals(6,c3.value)
        assertEquals(10,c4.value)
        assertEquals(15,c5.value)

        finish()
    }

    @Test
    fun andOrTest() {
        val activity = createActivity()

        val source0 = MutableLiveData<Boolean>(false)
        val source1 = MutableLiveData<Boolean>(false)
        val source2 = MutableLiveData<Boolean>(false)

        val not = source0.not()
        val and = and(source0, source1, source2)
        val or = or(source0, source1, source2)

        val m = or(not, and, or)
        m.observe(activity) {}

        assertEquals(false, source0.value)
        assertEquals(false, source1.value)
        assertEquals(false, source2.value)
        assertEquals(true, not.value)
        assertEquals(false, and.value)
        assertEquals(false, or.value)

        source0.value = true
        assertEquals(true, source0.value)
        assertEquals(false, source1.value)
        assertEquals(false, source2.value)
        assertEquals(false, not.value)
        assertEquals(false, and.value)
        assertEquals(true, or.value)

        source1.value = true
        assertEquals(true, source0.value)
        assertEquals(true, source1.value)
        assertEquals(false, source2.value)
        assertEquals(false, not.value)
        assertEquals(false, and.value)
        assertEquals(true, or.value)

        source2.value = true
        assertEquals(true, source0.value)
        assertEquals(true, source1.value)
        assertEquals(true, source2.value)
        assertEquals(false, not.value)
        assertEquals(true, and.value)
        assertEquals(true, or.value)

        finish()
    }
}