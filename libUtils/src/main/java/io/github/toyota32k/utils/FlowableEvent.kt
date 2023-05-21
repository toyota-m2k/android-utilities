package io.github.toyota32k.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Flow (StateFlow) ベースのResetableEventクラス
 */
class FlowableEvent(initial:Boolean=false, val autoReset:Boolean=false) {
    private val flow = MutableStateFlow(initial)
    private val mutex = Mutex()

    suspend fun set() {
        mutex.withLock {
            flow.value = true
        }
    }
    suspend fun reset() {
        mutex.withLock {
            flow.value = false
        }
    }
    suspend fun waitOne() {
        while(true) {
            mutex.withLock {
                if (flow.value) {
                    if (autoReset) {
                        flow.value = false
                    }
                    return  // waitOne
                }
            }
            flow.filter { it }.first()
        }
    }
    suspend fun <T> withLock(fn:()->T):T {
        waitOne()
        return fn()
    }
}