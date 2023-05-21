@file:Suppress("unused")

package io.github.toyota32k.utils

import androidx.annotation.MainThread
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

interface ListenerKey<T>:IDisposable {
    fun invoke(arg:T)
}

class Listeners<T> {
    interface IListener<T> {
        fun onChanged(value:T)
    }

    private val functions = mutableListOf<ListenerKey<T>>()
    private val tobeDelete = mutableSetOf<ListenerKey<T>>()
    private var busy:Boolean = false
    val count:Int get() = functions.size

    inner open class IndependentInvoker(callback:(T)->Unit):ListenerKey<T> {
        var fn:((T)->Unit)? = callback

        @MainThread
        override fun invoke(arg:T) {
            fn?.invoke(arg)
        }

        override fun dispose() {
            if(!busy) {
                functions.remove(this)
            } else {
                tobeDelete.add(this)
            }
            fn = null
        }
    }

    inner class OwneredInvoker(owner:LifecycleOwner, val fn:(T)->Unit) : LifecycleEventObserver, ListenerKey<T> {
        var lifecycle:Lifecycle?
        init {
            lifecycle = owner.lifecycle.also {
                it.addObserver(this)
            }
        }

        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if(!source.lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
                dispose()
            }
        }

        @MainThread
        override fun dispose() {
            lifecycle?.let {
                lifecycle = null
                it.removeObserver(this)
                if(!busy) {
                    functions.remove(this)
                } else {
                    // invoke中にdeleteが要求された場合はここに入る
                    tobeDelete.add(this)
                }
            }
        }

        private val alive:Boolean
            get() = lifecycle !=null

        @MainThread
        override fun invoke(arg:T) {
            if(alive) {
                fn(arg)
            } else {
                dispose()
            }
        }
    }


    @MainThread
    fun add(owner:LifecycleOwner, fn:(T)->Unit): IDisposable {
        return OwneredInvoker(owner, fn).apply {
            functions.add(this)
        }
    }

    @MainThread
    fun add(owner: LifecycleOwner, listener:IListener<T>):IDisposable {
        return add(owner, listener::onChanged)
    }

    @MainThread
    fun addForever(fn:(T)->Unit):IDisposable {
        return IndependentInvoker(fn).apply {
            functions.add(this)
        }
    }

    @MainThread
    fun addForever(listener:IListener<T>):IDisposable {
        return IndependentInvoker(listener::onChanged).apply {
            functions.add(this)
        }
    }

    @MainThread
    fun remove(key:IDisposable) {
        key.dispose()
    }

    @MainThread
    fun clear() {
        while(functions.isNotEmpty()) {
            functions.last().dispose()
        }
    }

    @MainThread
    fun invoke(v:T) {
        busy = true
        try {
            functions.forEach {
                it.invoke(v)
            }
        } catch(e:Throwable) {
            UtLog.libLogger.stackTrace(e)
        }
        busy = false

        if(tobeDelete.size>0) {
            tobeDelete.forEach {
                it.dispose()
            }
            tobeDelete.clear()
        }
    }
}

class UnitListeners {
    private val listeners = Listeners<Unit>()
    @MainThread
    fun add(owner:LifecycleOwner, fn:()->Unit): IDisposable {
        return listeners.add(owner) { fn() }
    }


    @MainThread
    fun addForever(fn:()->Unit):IDisposable {
        return listeners.addForever { fn() }
    }

    fun clear() {
        listeners.clear()
    }

    fun invoke() {
        listeners.invoke(Unit)
    }
}
