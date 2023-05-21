@file:Suppress("unused")

package io.github.toyota32k.utils

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import java.lang.Long.max
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration
import kotlin.time.DurationUnit

/**
 * タイムアウトを監視するクラス
 */
class TimeKeeper(ownerContext: CoroutineContext, private val nameForDebug:String) {
    constructor(ownerScope: CoroutineScope, nameForDebug: String) : this(ownerScope.coroutineContext, nameForDebug)
    private var startTick:Long = 0L
    private val scope = CoroutineScope(ownerContext)
    private var paused = MutableStateFlow(0)
    private var job: Job? = null
    private var timeout:Long = -1

    private val logger by lazy { UtLog("TimeKeeper($nameForDebug)") }

    /**
     * 監視を開始する
     * @param pause true: pause状態で監視を開始 (withTimeout()とともに使うことを想定）
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun start(timeoutInMS:Long, pause:Boolean=false, repeat:Boolean=false, onTimeout:(()->Unit)) {
        timeout = timeoutInMS
        if(timeout<0) return
        if(pause) {
            paused.value = 1
        }
        startTick = System.currentTimeMillis()
        job = scope.launch {
            logger.debug("started")
            while(isActive) {
                paused.first { it==0 }
                val remain = timeout - (System.currentTimeMillis() - startTick)
                if(remain<=0) {
//                    logger.debug("timeout")
                    onTimeout()
                    if(!repeat) {
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
    @Suppress("unused")
    fun start(timeout: Duration, pause:Boolean=false, repeat:Boolean=false, onTimeout:(()->Unit))
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
        startTick = System.currentTimeMillis()
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