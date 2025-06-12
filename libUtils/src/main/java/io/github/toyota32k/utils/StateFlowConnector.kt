package io.github.toyota32k.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach

/**
 * フローの出力を、他のMutableStateFlow の入力に接続する。
 */
class StateFlowConnector<T>(source: Flow<T>, private val destination: MutableStateFlow<T>, parentScope:CoroutineScope?=null):IDisposableEx {
    private var job: Job?

    init {
        UtLib.logger.verbose { "flow-connector started" }
        UtLib.logger.debug()
        job = source.onEach {
            destination.value = it
        }.onCompletion {
            UtLib.logger.debug { "flow-connector disposed" }
        }.launchIn(CoroutineScope( parentScope?.coroutineContext ?: Dispatchers.IO))
    }

    override fun dispose() {
        UtLib.logger.debug()
        job?.cancel()
        job = null
    }

    override val disposed: Boolean
        get() = job == null

    companion object {
        fun <T> Flow<T>.connectTo(destination:MutableStateFlow<T>, parentScope:CoroutineScope?=null):StateFlowConnector<T> =
                StateFlowConnector(this, destination, parentScope)
    }
}