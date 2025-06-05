package io.github.toyota32k.utils.lifecycle

import androidx.annotation.MainThread
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import io.github.toyota32k.utils.IDisposable

class Callback<T,R> private constructor(private var lifecycle: Lifecycle?, private var callback:((T)->R)?)
 : LifecycleEventObserver, IDisposable {
    constructor(owner: LifecycleOwner, fn:((T)->R)) : this(owner.lifecycle,fn)
    constructor(): this(null,null)

    init {
        lifecycle?.addObserver(this)
    }

    @MainThread
    fun set(owner: LifecycleOwner, fn:((T)->R)) {
        dispose()
        callback = fn
        lifecycle = owner.lifecycle
        lifecycle?.addObserver(this)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (!source.lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
            dispose()
        }
    }

    @MainThread
    override fun dispose() {
        lifecycle?.let {
            it.removeObserver(this)
            lifecycle = null
            callback = null
        }
    }

    private val alive: Boolean
        get() = lifecycle != null

    @MainThread
    fun invoke(arg: T) :R? {
        return if (alive) {
            callback?.invoke(arg)
        } else {
            null
        }
    }
}