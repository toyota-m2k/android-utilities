package io.github.toyota32k.utils

import androidx.lifecycle.LifecycleOwner

/**
 * LifecycleOwnerと生存期間を共にするDisposerクラス
 */
open class LifecycleDisposer(owner:LifecycleOwner?=null) : Disposer() {
    private val lifecycleOwnerHolder = LifecycleOwnerHolder(owner) { reset() }

    var lifecycleOwner:LifecycleOwner?
        get() = lifecycleOwnerHolder.lifecycleOwner
        set(v) {
            if(v!=null) {
                lifecycleOwnerHolder.attachOwner(v)
            } else {
                lifecycleOwnerHolder.detachOwner()
            }
        }
}