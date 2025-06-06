package io.github.toyota32k.utils

import io.github.toyota32k.logger.UtLog
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import java.lang.Long.max
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration
import kotlin.time.DurationUnit

/**
 * タイムアウトを監視するクラス
 *
 * val timeKeeper = TimeKeeper(lifecycleScope, "sample")
 * timeKeeper.start(1000, pause=false, repeat=true) {
 *    doSomething()
 * }
 */
class TimeKeeper(
    ownerContext: CoroutineContext,
    private val nameForDebug:String,
    private val timeProvider: ()->Long = { System.currentTimeMillis() } // for unit test
    ) {
    constructor(
        ownerScope: CoroutineScope,
        nameForDebug: String,
        timeProvider: ()->Long = { System.currentTimeMillis() }
    ) : this(ownerScope.coroutineContext, nameForDebug,timeProvider)

    private var startTick:Long = 0L
    private val scope = CoroutineScope(ownerContext)
    private var paused = MutableStateFlow(0)
    private var job: Job? = null
    private var timeout:Long = -1

    private val logger by lazy { UtLog("TimeKeeper($nameForDebug)", UtLib.logger) }

    /**
     * 監視を開始する
     * @param pause true: pause状態で監視を開始 (withTimeout()とともに使うことを想定）
     * @param repeat true: 監視を繰り返す
     * @param onTimeout 監視がタイムアウトしたときのコールバック falseを返すと、repeat=trueでも監視を終了する
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun start(timeoutInMS:Long, pause:Boolean=false, repeat:Boolean=false, onTimeout:(()->Boolean)) {
        timeout = timeoutInMS
        if(timeout<0) return
        if(pause) {
            paused.value = 1
        }
        startTick = timeProvider()
        job = scope.launch {
            logger.debug("started")
            while(isActive) {
                paused.first { it==0 }
                val remain = timeout - (timeProvider() - startTick)
                if(remain<=0) {
//                    logger.debug("timeout")
                    if(!onTimeout() || !repeat) {
                        break
                    }
                    touch()
                }
                delay(max(remain,100))
            }
            logger.debug("finished")
        }
    }

    /**
     * かっちょいい start （timeoutをDurationで与える)
     * 使用例）
     *  start(3.seconds, ...)   // かっちょよすぎる。。。渡すとき使うとき計２回変換されるんだが、かっちょよさのためには、そのくらい気にならない。
     */
    fun start(timeout: Duration, pause:Boolean=false, repeat:Boolean=false, onTimeout:(()->Boolean))
            = start(timeout.toLong(DurationUnit.MILLISECONDS), pause, repeat, onTimeout)

    /**
     * 監視を停止する
     */
    fun pause() {
        logger.debug()
        paused.value++
    }

    /**
     * 停止した監視を再開する
     */
    fun resume() {
        logger.debug()
        touch()
        paused.value--
        logger.assert(paused.value>=0, "pause/resume mismatch.")
    }

    /**
     * 監視を終了する
     */
    fun stop() {
        logger.debug()
        job?.cancel()
        job = null
    }

    /**
     * 延命する
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun touch() {
        if(timeout<0) return
        startTick = timeProvider()
    }

    /**
     * タイムアウト監視付きで処理を実行する
     * start(pause=true)して使う。
     */
    inline fun <T> withTimeout(fn:()->T):T {
        resume()
        return try {
            fn()
        } finally {
            pause()
        }
    }
}