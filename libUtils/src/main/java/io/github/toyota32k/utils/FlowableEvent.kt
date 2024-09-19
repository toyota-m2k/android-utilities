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
    private val flow = MutableStateFlow(initial)

    suspend fun set() {
        flow.value = true
    }
    suspend fun reset() {
        flow.value = false
    }
    suspend fun waitOne() {
        flow.filter { it }.first()
        if (autoReset) {
            if (flow.value) {
                flow.value = false
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
    suspend fun <T> withLock(timeout:Long, defOnTimeot:T, fn:()->T):T {
        if(waitOne(timeout)) {
            return fn()
        } else {
            return defOnTimeot
        }
    }
    suspend fun <T> withLock(timeout:Duration, defOnTimeot:T, fn:()->T):T {
        return withLock(timeout.inWholeMilliseconds.coerceAtLeast(1), defOnTimeot, fn)
    }
}