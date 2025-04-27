package io.github.toyota32k.utils

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.LifecycleOwner

/**
 * onBackInvokedCallback と onBackPressedCallback の違いを隠蔽するクラス
 * 両方の機能性のANDになるので、当然できることは減るが「とりあえず移行したい」用途には便利なのでは？
 */
class CompatBackKeyDispatcher(
    private val useInvokedDispatcher:Boolean=true,
    private var onBackInvokedDispatcherPriority:Int = 0) {
    private var onBackInvokedCallback: AutoDisposalOnBackInvokedDispatcher? = null
    private var onBackPressedCallback: OnBackPressedCallback? = null

    /**
     * activity.onBackInvokedDispatcher.registerOnBackInvokedCallback() に渡す priority を設定
     */
    fun setInvokedDispatcherPriority(priority:Int):CompatBackKeyDispatcher {
        onBackInvokedDispatcherPriority = priority
        return this
    }

    /**
     * Back Key 押下時のコールバックを登録
     * @param activity キーイベントをハンドルするActivity
     * @param lifecycleOwner callbackの生存期間を規定するlifecycleOwner
     * @param callback Back Key 押下時のコールバック
     */
    fun register(activity: ComponentActivity, lifecycleOwner:LifecycleOwner, callback: () -> Unit) {
        if (useInvokedDispatcher && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU/*34*/) {
            onBackInvokedCallback = AutoDisposalOnBackInvokedDispatcher().apply {
                register(onBackInvokedDispatcherPriority, activity, lifecycleOwner) {
                    callback()
                }
            }
        } else {
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    callback()
                    isEnabled = false
                }
            }.apply {
                onBackPressedCallback = this
                activity.onBackPressedDispatcher.addCallback(lifecycleOwner, this)
            }
        }
    }

    /**
     * Back Key 押下時のコールバックを登録
     * activity と lifecycleOwner が一致する場合に lifecycleOwnerを省略する版
     */
    fun register(activity: ComponentActivity, callback: ()->Unit) {
        register(activity, activity, callback)
    }

    /**
     * 明示的な登録解除
     * 通常は lifecycleに任せておｋ
     */
    fun unregister() {
        onBackInvokedCallback?.unregister()
        onBackInvokedCallback = null
        onBackPressedCallback?.remove()
        onBackPressedCallback = null
    }
}