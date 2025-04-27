package io.github.toyota32k.utils

import android.app.Activity
import android.os.Build
import android.window.OnBackInvokedCallback
import androidx.activity.ComponentActivity
import androidx.lifecycle.LifecycleOwner

/**
 * onBackInvokedDispatcherに、ライフサイクルにる自動的な登録解除機能を付与するクラス
 */
class AutoDisposalOnBackInvokedDispatcher {
    private var activityRef : LifecycleReference<Activity>? = null
    private var disposer: (() -> Unit)? = null

    /**
     * Back Key 押下時のコールバックを登録
     * @param priority activity.onBackInvokedDispatcher.registerOnBackInvokedCallback() に渡す priority
     * @param activity キーイベントをハンドルするActivity
     * @param lifecycleOwner callbackの生存期間を規定するlifecycleOwner
     * @param callback Back Key 押下時のコールバック
     */
    fun register(priority:Int, activity: Activity, lifecycleOwner:LifecycleOwner, callback: () -> Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        activityRef = LifecycleReference(activity, lifecycleOwner) {
            unregister()
        }
        val invoker = object: OnBackInvokedCallback {
            override fun onBackInvoked() {
                callback()
            }
        }
        activity.onBackInvokedDispatcher.registerOnBackInvokedCallback(priority, invoker)
        disposer = {
            activity.onBackInvokedDispatcher.unregisterOnBackInvokedCallback(invoker)
        }
    }
    /**
     * Back Key 押下時のコールバックを登録
     * activity と lifecycleOwner が一致する場合に lifecycleOwnerを省略する版
     */
    fun register(priority:Int, activity: ComponentActivity, callback: () -> Unit) {
        register(priority, activity, activity, callback)
    }

    /**
     * 明示的な登録解除
     * 通常は lifecycleに任せておｋ
     */
    fun unregister() {
        disposer?.invoke()
        disposer = null
        activityRef?.reset()
    }
}