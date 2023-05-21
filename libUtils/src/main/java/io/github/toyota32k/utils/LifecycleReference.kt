package io.github.toyota32k.utils

import androidx.lifecycle.LifecycleOwner

class LifecycleReference<T>(v:T?, owner:LifecycleOwner, val onDestroyed:((T)->Unit)?=null) {
    var value:T? = v
        private set
    private val holder = LifecycleOwnerHolder(owner) {
        value?.apply { onDestroyed?.invoke(this) }
        value = null
    }
}