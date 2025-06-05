package io.github.toyota32k.utils

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration

/**
 * Flow (StateFlow) ベースのResetableEventクラス
 *
 * @param initial   false: 非シグナル状態で開始（デフォルト） / true: シグナル状態で開始
 * @param autoReset false: 手動リセット（デフォルト）/ true: 自動リセット
 */
class FlowableEvent(initial:Boolean=false, val autoReset:Boolean=false) {
    private val flag = java.util.concurrent.atomic.AtomicBoolean(initial)
    private val flow = MutableStateFlow(initial)
    private val mutex = Mutex()

    fun set() {
        if( flag.compareAndSet(false, true) ) {
            flow.value = true
        }
    }
    fun reset() {
        if( flag.compareAndSet(true, false) ) {
            flow.value = false
        }
    }
    suspend fun waitOne() {
        if(!autoReset) {
            // manual reset の場合は、単に flowに trueが入るのを待つだけで ok
            flow.filter { it }.first()
        } else {
            // auto reset の場合は、first()を待つ呼び出しを１つに限定するため、mutexで待たせる。
            mutex.withLock {
                flow.filter { it }.first()
                reset()
            }
        }
    }

    suspend fun waitOne(timeout:Long):Boolean {
        return try {
            withTimeout(timeout) {
                waitOne()
                true
            }
        } catch(e: TimeoutCancellationException) {
            false
        }
    }
    suspend fun waitOne(timeout:Duration):Boolean {
        return waitOne(timeout.inWholeMilliseconds.coerceAtLeast(1))
    }
    suspend fun <T> withLock(fn:()->T):T {
        waitOne()
        return fn()
    }
    suspend fun <T> withLock(timeout:Long, defOnTimeout:T, fn:()->T):T {
        if(waitOne(timeout)) {
            return fn()
        } else {
            return defOnTimeout
        }
    }
    suspend fun <T> withLock(timeout:Duration, defOnTimeout:T, fn:()->T):T {
        return withLock(timeout.inWholeMilliseconds.coerceAtLeast(1), defOnTimeout, fn)
    }
}