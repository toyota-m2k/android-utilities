package io.github.toyota32k.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach

/**
 * フローの出力を、他のMutableStateFlow の入力に接続する。
 */
class StateFlowConnector<T>(source: Flow<T>, private val destination: MutableStateFlow<T>, parentScope:CoroutineScope?=null):IDisposable {
    private var scope :CoroutineScope?

    init {
        scope = CoroutineScope( parentScope?.run { coroutineContext + Job(coroutineContext[Job]) } ?: (Dispatchers.IO + SupervisorJob())).apply {
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