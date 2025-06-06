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
        scope = CoroutineScope (parentScope?.coroutineContext ?: (Dispatchers.IO + SupervisorJob())).apply {
            source.onEach {
                destination.value = it
            }.onCompletion {
                UtLib.logger.debug("disposed.")
            }.launchIn(this)
        }
    }

    override fun dispose() {
        UtLib.logger.debug()
        scope?.cancel()
        scope = null
    }

    companion object {
        fun <T> Flow<T>.connectTo(destination:MutableStateFlow<T>, parentScope:CoroutineScope?=null):StateFlowConnector<T> =
                StateFlowConnector(this, destination, parentScope)
    }
}