package io.github.toyota32k.utils
import kotlinx.coroutines.*
import org.junit.Assert
import org.junit.Test
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class FlowableEventTest {
    @Test
    fun singleTest() {
        val ev1 = FlowableEvent()
        var v = false
        CoroutineScope(Dispatchers.Default).launch {
            delay(500)
            v = true
            ev1.set()
        }
        runBlocking {
            ev1.waitOne()
            ev1.waitOne()   // autoReset==falseだから、何回waitしてもok
        }
        Assert.assertTrue(v)
    }
    @Test
    fun initialTest() {
        val ev1 = FlowableEvent(initial = true)
        runBlocking {
            ev1.waitOne()
            ev1.waitOne()   // autoReset==falseだから、何回waitしてもok
        }
        Assert.assertTrue(true)
    }
    @Test
    fun autoResetTest() {
        val ev1 = FlowableEvent(autoReset = true)
        val v = AtomicInteger(0)
        CoroutineScope(Dispatchers.Default).launch {
            ev1.waitOne()
            v.incrementAndGet()
        }
        CoroutineScope(Dispatchers.Default).launch {
            ev1.waitOne()
            v.incrementAndGet()
        }
        runBlocking {
            ev1.set()
            delay(100)
        }
        Assert.assertEquals(1, v.get())
        runBlocking {
            ev1.set()
            delay(100)
        }
        Assert.assertEquals(2, v.get())
    }
    @Test
    fun multiWaitTest() {
        val ev1 = FlowableEvent(autoReset = false)
        val v = AtomicInteger(0)
        CoroutineScope(Dispatchers.Default).launch {
            ev1.waitOne()
            v.incrementAndGet()
        }
        CoroutineScope(Dispatchers.Default).launch {
            ev1.waitOne()
            v.incrementAndGet()
        }
        runBlocking {
            ev1.set()
            delay(100)
        }
        Assert.assertEquals(2, v.get())
    }
    @Test
    fun resetTest() {
        val ev1 = FlowableEvent(autoReset = false)
        val ev2 = FlowableEvent(autoReset = false)
        val x = AtomicBoolean(true)
        val v = AtomicInteger(0)
        CoroutineScope(Dispatchers.Default).launch {
            while(x.get()) {
                ev1.waitOne()
                ev1.reset()
                delay(100)
                v.incrementAndGet()
                ev2.set()
            }
        }
        runBlocking {
            for(i in 0..2) {
                delay(100)
                ev1.set()
                ev2.waitOne()
                ev2.reset()
            }
        }
        Assert.assertEquals(3, v.get())

    }
}