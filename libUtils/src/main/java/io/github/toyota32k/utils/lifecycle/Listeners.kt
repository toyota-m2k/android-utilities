package io.github.toyota32k.utils.lifecycle

import androidx.annotation.MainThread
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import io.github.toyota32k.utils.IDisposable
import io.github.toyota32k.utils.UtLib

interface ListenerKey<T>: IDisposable {
    fun invoke(arg:T)
}

class Listeners<T> : IDisposable {
    fun interface IListener<T> {
        fun onChanged(value:T)
    }

    private val functions = mutableListOf<ListenerKey<T>>()
    private val tobeDeleted = mutableSetOf<ListenerKey<T>>()
    private var busy:Boolean = false    // invoke中にセットされる
    val count:Int get() = functions.size

    @MainThread
    override fun dispose() {
        clear()
    }

    open inner class IndependentInvoker(callback:(T)->Unit):ListenerKey<T> {
        var fn:((T)->Unit)? = callback

        @MainThread
        override fun invoke(arg:T) {
            fn?.invoke(arg)
        }

        @MainThread
        override fun dispose() {
            if(!busy) {
                functions.remove(this)
            } else {
                tobeDeleted.add(this)
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
                    tobeDeleted.add(this)
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
    fun add(owner: LifecycleOwner, listener:IListener<T>): IDisposable {
        return add(owner, listener::onChanged)
    }

    @MainThread
    fun addForever(fn:(T)->Unit): IDisposable {
        return IndependentInvoker(fn).apply {
            functions.add(this)
        }
    }

    @MainThread
    fun addForever(listener:IListener<T>): IDisposable {
        return IndependentInvoker(listener::onChanged).apply {
            functions.add(this)
        }
    }

    @MainThread
    fun remove(key: IDisposable) {
        key.dispose()
    }

    @MainThread
    fun clear() {
        if (!busy) {
            while (functions.isNotEmpty()) {
                functions.last().dispose()
            }
        } else {
            // invoke中にdeleteが要求された場合はここに入る
            tobeDeleted.addAll(functions)
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
            UtLib.logger.stackTrace(e)
        }
        busy = false

        if(tobeDeleted.isNotEmpty()) {
            tobeDeleted.forEach {
                it.dispose()
            }
            tobeDeleted.clear()
        }
    }
}

class UnitListeners : IDisposable {
    private val listeners = Listeners<Unit>()
    @MainThread
    fun add(owner:LifecycleOwner, fn:()->Unit): IDisposable {
        return listeners.add(owner) { fn() }
    }


    @MainThread
    fun addForever(fn:()->Unit): IDisposable {
        return listeners.addForever { fn() }
    }

    @MainThread
    fun clear() {
        listeners.clear()
    }

    @MainThread
    override fun dispose() {
        listeners.dispose()
    }

    @MainThread
    fun invoke() {
        listeners.invoke(Unit)
    }
}
