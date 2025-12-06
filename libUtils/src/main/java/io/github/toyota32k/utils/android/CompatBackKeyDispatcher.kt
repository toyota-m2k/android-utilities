package io.github.toyota32k.utils.android

import android.app.Activity
import android.os.Build
import android.window.OnBackInvokedCallback
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LifecycleOwner
import io.github.toyota32k.utils.lifecycle.LifecycleReference

/**
 * onBackInvokedCallback と onBackPressedCallback の違いを隠蔽するクラス
 * 両方の機能性のANDになるので、当然できることは減るが「とりあえず移行したい」用途には便利なのでは？
 */
class CompatBackKeyDispatcher(val doNotCallOnIme:Boolean=true) {
    companion object {
        // onBackInvokedDispatcher を使うと、IMEを表示した状態で Backキーを押下すると、
        // IMEがBackを処理する前に、アプリ側のonBackInvokedDispatcherがイベントを受け取ってしまうため、IMEが閉じなくなる。
        // ダイアログで使っていると、IMEを閉じようとして「戻る」操作をすると、ダイアログの方が閉じてしまう。
        // onBackInvokedDispatcherPriority をいろいろ調整しても効果なし。
        // 当面は onBackInvokedDispatcherは使わず、onBackPressedDispatcher でやりくりする。
        var ENABLE_INVOKED_DISPATCHER = false
    }

    private var onBackInvokedDispatcherPriority:Int = 0
    private var activityRef : LifecycleReference<Activity>? = null
    private val activity: Activity?
        get() = activityRef?.value
    private var backKeyHandler: BackKeyHandler? = null

    private abstract class BackKeyHandler: OnBackInvokedCallback, OnBackPressedCallback(true)

    fun isImeVisible():Boolean {
        val activity = this.activity ?: return false
        val rootView = activity.window.decorView
        return ViewCompat.getRootWindowInsets(rootView)?.isVisible(WindowInsetsCompat.Type.ime()) ?: false
    }


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
    fun register(activity: ComponentActivity, lifecycleOwner: LifecycleOwner, callback: () -> Unit) {
        activityRef = LifecycleReference(activity, lifecycleOwner) {
            unregister()
        }
        val handler = object : BackKeyHandler() {
            // 共通
            private fun handleEvent() {
                if (!doNotCallOnIme ||!isImeVisible()) {
                    callback()
                }
            }
            // OnBackPressedCallback
            override fun handleOnBackPressed() {
                handleEvent()
//                isEnabled = false
                // isEnabled = false にすると、handleOnBackPressed()が呼ばれなくなる。
                // API的には、例えば、callback()が false を返したら isEnable = false にする、というのが普通だが、
                // onBackInvokedDispatcherの callback()とシグネチャを共通化する必要があるので、isEnabled は使用しないことにする。
                // １回限りのコールバックにしたい場合は、unregister() で代用する。
            }

            // OnBackInvokedCallback
            override fun onBackInvoked() {
                handleEvent()
            }
        }
        backKeyHandler = handler    // 解放用に保存

        if (ENABLE_INVOKED_DISPATCHER && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU/*34*/) {
            // Android 14以降: onBackInvokedDispatcherを使用
            activity.onBackInvokedDispatcher.registerOnBackInvokedCallback(onBackInvokedDispatcherPriority, handler)
        }
        // Android 13以前、または enableOnBackInvokedCallback="false" の場合は onBackPressedDispatcherを使用
        // どちらのコールバックが呼ばれるかは OSに任せる
        activity.onBackPressedDispatcher.addCallback(lifecycleOwner, handler)
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
        backKeyHandler?.apply {
            remove()
            if (ENABLE_INVOKED_DISPATCHER && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU/*34*/) {
                // Android 14以降: onBackInvokedDispatcherを使用
                activity?.onBackInvokedDispatcher?.unregisterOnBackInvokedCallback(this)
            }
        }
    }
}