package io.github.toyota32k.utils

import androidx.annotation.MainThread
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

/**
 * LifecycleOwnerを、その生存期間だけ安全に保持するクラス
 */
class LifecycleOwnerHolder(owner: LifecycleOwner?=null, onDestroyed:(()->Unit)?=null) : LifecycleEventObserver, IDisposableEx {
    private var destroyedCallback:(()->Unit)? = onDestroyed

    var lifecycleOwner:LifecycleOwner? = null
        @MainThread
        private set(owner) {
            field?.lifecycle?.apply {
                removeObserver(this@LifecycleOwnerHolder)
            }
            field = owner
            owner?.lifecycle?.addObserver(this)
        }

    /**
     * lifecycleOwnerをセットする
     * @param onDestroyed   lifecycleOwnerが死んだときのコールバック（nullなら無視：省略可能）
     */
    fun attachOwner(owner: LifecycleOwner, onDestroyed: (() -> Unit)?=null) : LifecycleOwnerHolder {
        lifecycleOwner = owner
        if(onDestroyed!=null) {
            destroyedCallback = onDestroyed
        }
        return this
    }

    /**
     * lifecycleOwnerとの関連を解除する
     */
    fun detachOwner() {
        lifecycleOwner = null
    }

    /**
     * lifecycleOwnerが死んだときのコールバックをセットする。
     */
    fun destroyed(onDestroyed: (() -> Unit)?):LifecycleOwnerHolder {
        destroyedCallback = onDestroyed
        return this
    }

    init {
        lifecycleOwner = owner
    }

    /**
     * LifecycleEventObserver.onStateChanged
     */
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if(!source.lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
            destroyedCallback?.invoke()
            detachOwner()
        }
    }

    override val disposed: Boolean
        get() = lifecycleOwner == null

    /**
     * lifecycleOwnerとの関係を清算し、連絡先も消す。
     * detachOwner().destroyed(null) と等価。
     */
    override fun dispose() {
        detachOwner()
        destroyedCallback = null
    }
}