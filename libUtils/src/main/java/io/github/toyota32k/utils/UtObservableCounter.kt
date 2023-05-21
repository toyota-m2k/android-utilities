package io.github.toyota32k.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.Closeable

/**
 * Flowによる参照数の監視が可能な参照カウンタクラス
 */
class UtObservableCounter(private val counter:MutableStateFlow<Int> = MutableStateFlow(0)) : Flow<Int> by counter {
    val count:Int get() = synchronized(this) { counter.value }

    fun set():Int = synchronized(this) {
        counter.value++
        return counter.value
    }

    fun reset():Int = synchronized(this) {
        assert(counter.value>0)
        counter.value--
        return counter.value
    }

    inline fun <T> onSet(fn:()->T):T {
        set()
        return try {
            fn()
        } finally {
            reset()
        }
    }

    inline fun <T> withSetCounter(fn:(counter:Int)->T):T {
        val c = set()
        return try {
            fn(c)
        } finally {
            reset()
        }
    }

    private inner class Closer: Closeable {
        override fun close() {
            reset()
        }
    }

    fun closeableSet():Closeable {
        set()
        return Closer()
    }
}