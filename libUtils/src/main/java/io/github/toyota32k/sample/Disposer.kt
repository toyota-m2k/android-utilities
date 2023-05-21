package io.github.toyota32k.sample

open class Disposer : IDisposableEx {
    final override var disposed: Boolean = false
        private set

    open operator fun plus(disposable:IDisposable): Disposer {
        return register(disposable)
    }
    open operator fun minus(disposable:IDisposable): Disposer {
        return unregister(disposable)
    }

    protected val disposables = mutableListOf<IDisposable>()
    val count get() = disposables.size

    override fun dispose() {
        if(!disposed) {
            reset()
            disposed = true
        }
    }

    fun register(vararg disposables:IDisposable):Disposer {
        disposed = false
        for(b in disposables) {
            this.disposables.add(b)
        }
        return this
    }

    fun unregister(vararg disposables:IDisposable?):Disposer {
        for(b in disposables) {
            if(b!=null && this.disposables.contains(b)) {
                this.disposables.remove(b)
                b.dispose()
            }
        }
        return this
    }

    var clientData:Any? = null

    fun reset() {
        disposables.forEach { it.dispose() }
        disposables.clear()
        disposed = false

        // clientData が disposableならdispose()してnullにする。
        // disposableでなければ何もしない。
        val cd = clientData as? IDisposable ?: return
        cd.dispose()
        clientData = null
    }

    fun clean() {
        val itr = disposables.iterator()
        while(itr.hasNext()) {
            val v = itr.next() as? IDisposableEx ?: continue
            if(v.disposed) {
                itr.remove()
            }
        }
    }
}
