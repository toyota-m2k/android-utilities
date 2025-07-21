package io.github.toyota32k.utils.android

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import io.github.toyota32k.logger.UtLog
import io.github.toyota32k.utils.IAwaiter
import io.github.toyota32k.utils.UtLib
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import java.util.UUID
import kotlin.coroutines.CoroutineContext

/**
 * Workerの完了を待ち合わせるための i/f (IAwaiter)
 * 完了時の WorkInfo を lastWorkInfoに保持する。
 * WorkInfo.outputData によって、SUCCEEDED/FAILED以外の情報を返したいときに利用することを想定。
 */
interface IWorkerAwaiter : IAwaiter<Boolean> {
    val lastWorkInfo: WorkInfo?
//    override suspend fun await():Boolean
//    override fun cancel()
}

/**
 * progressコールバック付きCoroutineWorker の実装用基底クラス
 * 派生クラスで、fun doWork() をオーバーライドし、Workerの処理を記述する。
 * doWork() 内では、CoroutineWorkerのメソッドに加えて、以下のメソッドが利用できる。
 *
 * - fun progress() 進捗をProgressAwaiterに送信する --> ProgressWorkerGeneratorに渡した onProgress ハンドラで受け取る
 * - fun customEvent() 任意のDataをProgressAwaiterに送信する --> ProgressWorkerGeneratorに渡した onCustomEvent ハンドラで受け取る
 */
abstract class ProgressWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    companion object {
        const val PROGRESS_TOTAL_LENGTH = "total_length"
        const val PROGRESS_CURRENT_BYTES = "current_bytes"
        const val PROGRESS_CUSTOM = "custom_progress_event"
    }
    protected suspend fun progress(current:Long, total:Long) {
        setProgress(
            workDataOf(
                PROGRESS_CURRENT_BYTES to current,
                PROGRESS_TOTAL_LENGTH to total
            ))
    }
    protected suspend fun customEvent(eventData:Data.Builder) {
        eventData.putBoolean(PROGRESS_CUSTOM, true)
        setProgress(eventData.build())
    }
}

/**
 * ProgressWorkerを生成・実行するためのヘルパークラス。
 */
object ProgressWorkerGenerator {
    val logger = UtLog("PWG", UtLib.logger)

    /**
     * ProgressWorkerと通信し、実行を待ち合わせて、結果を中継するための IWorkerAwaiter実装クラス。
     */
    class ProgressAwaiter(
        scope:CoroutineScope,
        val workManager:WorkManager, val id:UUID,
        val onProgress:((current:Long, total:Long)->Unit)?,
        val onCustomEvent:((Data)->Unit)?): IWorkerAwaiter {
        override var lastWorkInfo: WorkInfo? = null

        private val result:Flow<Boolean> = flow {
            workManager.getWorkInfoByIdFlow(id)
                .collect { workInfo ->
                    when (workInfo?.state) {
                        WorkInfo.State.RUNNING -> {
                            workInfo.progress.apply {
                                if (getBoolean(ProgressWorker.Companion.PROGRESS_CUSTOM, false)) {
                                    onCustomEvent?.invoke(this)
                                } else {
                                    onProgress?.invoke(
                                        getLong(ProgressWorker.Companion.PROGRESS_CURRENT_BYTES, 0L),
                                        getLong(ProgressWorker.Companion.PROGRESS_TOTAL_LENGTH, 0L)
                                    )
                                }
                            }
                        }
                        WorkInfo.State.SUCCEEDED -> {
                            logger.info("work succeeded")
                            lastWorkInfo = workInfo
                            emit(true)
                        }
                        WorkInfo.State.FAILED, WorkInfo.State.CANCELLED -> {
                            logger.info("work failed")
                            lastWorkInfo = workInfo
                            emit(false)
                        }
                        else -> {}
                    }
                }
            logger.info("Completed")
        }.shareIn(scope, started = SharingStarted.Eagerly, replay = 1)

        /**
         * 結果を待ち合わせる。
         */
        override suspend fun await(): Boolean {
            return result.first()
        }

        /**
         * Workerの処理を中止する。
         */
        override fun cancel() {
            workManager.cancelWorkById(id)
        }
    }

    /**
     * 最小限の指定で ProgressWorkerを開始する。
     */
    inline fun <reified T:ProgressWorker> process(context:Context, data:Data, noinline onProgress:((current:Long, total:Long)->Unit)?): IWorkerAwaiter {
        return builder<T>().setInputData(data).apply { if(onProgress!=null) onProgress(onProgress) }.build(context)
    }

    /**
     * ProgressWorker開始用の Builderクラス
     */
    class Builder(clazz:Class<out ListenableWorker>) {
        private val request = OneTimeWorkRequest.Builder(clazz)
        private var expedited: OutOfQuotaPolicy = OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST
        private var coroutineContext:CoroutineContext = Dispatchers.IO
        private var onProgress: ((current:Long, total:Long)->Unit)? = null
        private var onCustomEvent: ((Data)->Unit)? = null


        fun setExpedited(expedited: OutOfQuotaPolicy): Builder {
            this.expedited = expedited
            return this
        }
        fun setInputData(data:Data) : Builder {
            request.setInputData(data)
            return this
        }
        fun customize(fn:(OneTimeWorkRequest.Builder)->Unit): Builder {
            fn(request)
            return this
        }
        fun onProgress(fn:(current:Long, total:Long)->Unit): Builder {
            onProgress = fn
            return this
        }
        fun onCustomEvent(fn:(Data)->Unit): Builder {
            onCustomEvent = fn
            return this
        }
        fun build(context:Context): IWorkerAwaiter {
            val req = request.setExpedited(expedited).build()

            val workManager = WorkManager.getInstance(context)
            workManager.enqueue(req)
            return ProgressAwaiter(CoroutineScope(coroutineContext), workManager, req.id, onProgress, onCustomEvent)
        }
    }

    inline fun <reified T:ProgressWorker> builder():Builder {
        return Builder(T::class.java)
    }
    fun builder(clazz:Class<out ListenableWorker>):Builder {
        return Builder(clazz)
    }
}