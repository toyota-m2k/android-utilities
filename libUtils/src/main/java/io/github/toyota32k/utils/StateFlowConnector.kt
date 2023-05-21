@file:Suppress("unused")

package io.github.toyota32k.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*

/**
 * フローの出力を、他のMutableStateFlow の入力に接続する。
 */
class StateFlowConnector<T>(source: Flow<T>, private val destination: MutableStateFlow<T>, parentScope:CoroutineScope?=null):IDisposable {
    private var scope :CoroutineScope?

    init {
        val scope = CoroutineScope ((parentScope?.coroutineContext ?: Dispatchers.IO) + SupervisorJob())
        source.onEach {
            destination.value = it
        }.onCompletion {
            UtLog.libLogger.debug("disposed.")
        }.launchIn(scope)
        this.scope = scope
    }

    override fun dispose() {
        UtLog.libLogger.debug()
        scope?.cancel()
        scope = null
    }

    companion object {
        fun <T> Flow<T>.connectTo(destination:MutableStateFlow<T>, parentScope:CoroutineScope?=null):StateFlowConnector<T> =
                StateFlowConnector(this, destination, parentScope)
    }
}