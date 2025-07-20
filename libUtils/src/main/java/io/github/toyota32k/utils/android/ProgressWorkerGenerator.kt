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
 * キャンセル可能な待ち合わせ用 i/f
 */
interface IAwaiter<T> {
    suspend fun await():T
    fun cancel()
}

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

object ProgressWorkerGenerator {
    val logger = UtLog("PWG", UtLib.logger)

    class ProgressAwaiter(
        scope:CoroutineScope,
        val workManager:WorkManager, val id:UUID,
        val onProgress:((current:Long, total:Long)->Unit)?,
        val onCustomEvent:((Data)->Unit)?): IAwaiter<Boolean> {
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
                            emit(true)
                        }
                        WorkInfo.State.FAILED, WorkInfo.State.CANCELLED -> {
                            logger.info("work failed")
                            emit(false)
                        }
                        else -> {}
                    }
                }
            logger.info("Completed")
        }.shareIn(scope, started = SharingStarted.Eagerly, replay = 1)

        override suspend fun await(): Boolean {
            return result.first()
        }

        override fun cancel() {
            workManager.cancelWorkById(id)
        }
    }

    inline fun <reified T:ProgressWorker> process(context:Context, data:Data, noinline onProgress:((current:Long, total:Long)->Unit)?): IAwaiter<Boolean> {
        return builder<T>().setInputData(data).apply { if(onProgress!=null) onProgress(onProgress) }.build(context)
    }

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
        fun build(context:Context): IAwaiter<Boolean> {
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