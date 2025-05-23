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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU/*34*/) {
            // Android 14以降: onBackInvokedDispatcherを使用
            onBackInvokedCallback = AutoDisposalOnBackInvokedDispatcher().apply {
                register(onBackInvokedDispatcherPriority, activity, lifecycleOwner) {
                    callback()
                }
            }
        }
        // Android 13以前、または enableOnBackInvokedCallback="false" の場合は onBackPressedDispatcherを使用
        // どちらのコールバックが呼ばれるかは OSに任せる
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                callback()
//                isEnabled = false
                // isEnabled = false にすると、handleOnBackPressed()が呼ばれなくなる。
                // API的には、例えば、callback()が false を返したら isEnable = false にする、というのが普通だが、
                // onBackInvokedDispatcherの callback()とシグネチャを共通化する必要があるので、isEnabled は使用しないことにする。
                // １回限りのコールバックにしたい場合は、unregister() で代用する。
            }
        }.apply {
            onBackPressedCallback = this
            activity.onBackPressedDispatcher.addCallback(lifecycleOwner, this)
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