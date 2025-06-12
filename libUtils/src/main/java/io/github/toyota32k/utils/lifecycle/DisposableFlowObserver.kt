package io.github.toyota32k.utils.lifecycle

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import io.github.toyota32k.utils.IDisposableEx
import io.github.toyota32k.utils.UtLib
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.CoroutineContext

/**
 * Flow を監視可能な Observer クラス
 * 親のcoroutineContextをキャンセルすれば監視jobもキャンセルされるので、lifecycleScope を使用している場合には、
 * LifecycleOwnerがdestroyされるときに（たぶん）自動解除されるはずだが、それに頼らず、明示的に dispose() を呼ぶことを強く推奨。
 * 例えば、
 * ```
 * val ctx = Dispatchers.IO+SupervisorJob()
 * CoroutineScope(ctx).launch {
 *   flowSample.disposableObserve(this.coroutineContext) { logger.debug("value=$it") }
 *   ... （そこそこ長い処理）
 * }
 * ```
 * のように書くと、"そこそこ長い処理"が終わって、CoroutineScope.launch {} ブロックを抜けると ctxが終了して flowの監視も自動終了するような錯覚を覚えるが、
 * 実際には、launch 自体はすぐに制御を返すが、その中の処理が続く限り、CoroutineScope は終了しないので、このctx は永久に終了しない。
 * ちなみに、
 * ```
 * withContext { flowSample.disposableObserve(this.coroutineContext) { logger.debug("value=$it") } }
 * ```
 * と書くと、このwithContext()は永久に戻ってこない。
 * これらは、発見・調査が困難なバグの原因となるので要注意。
 */
class DisposableFlowObserver<T> constructor(flow: Flow<T>, coroutineContext: CoroutineContext, private val callback:(v:T)->Unit): IDisposableEx {
    constructor(flow: Flow<T>, callback:(v:T)->Unit) : this(flow, Dispatchers.Main + SupervisorJob(), callback)
    constructor(flow: Flow<T>, owner: LifecycleOwner, callback:(v:T)->Unit) : this(flow, owner.lifecycleScope.coroutineContext, callback)
    companion object {
        val idGenerator = AtomicInteger(0)
    }
    private var job: Job?
    private val id = idGenerator.getAndIncrement()

    init {
        UtLib.logger.verbose { "observer started:$id" }
        job = flow.onEach {
            callback(it)
        }.onCompletion {
            UtLib.logger.verbose { "observer disposed:$id" }
        }.launchIn(CoroutineScope(coroutineContext))
    }

    override fun dispose() {
        UtLib.logger.verbose("observer disposing:$id")
        job?.cancel()
        job = null
    }

    override val disposed: Boolean
        get() = job == null
}

/**
 * Flow に LifecycleOwner のスコープでオブザーバーを登録し、登録解除用の IDisposable を返す。
 */
fun <T> Flow<T>.disposableObserve(owner: LifecycleOwner, callback:(value:T)->Unit):DisposableFlowObserver<T> =
    DisposableFlowObserver(this, owner, callback)
/**
 * Flow にCoroutineContextのスコープでオブザーバーを登録し、登録解除用の IDisposable を返す。
 * 利用注意：DisposableFlowObserver のコメントを参照
 */
fun <T> Flow<T>.disposableObserve(coroutineContext: CoroutineContext, callback:(value:T)->Unit):DisposableFlowObserver<T> =
    DisposableFlowObserver(this, coroutineContext, callback)

/**
 * Flow に Dispatchers.Mainスコープでオブザーバーを登録し、登録解除用の IDisposable を返す。
 */
fun <T> Flow<T>.disposableObserve(callback:(value:T)->Unit):DisposableFlowObserver<T> =
    DisposableFlowObserver(this, callback)
