package io.github.toyota32k.utils.lifecycle

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import io.github.toyota32k.utils.IDisposableEx
import java.io.Closeable

/**
 * LiveDataのobserverをIDisposable#dispose()やCloseable#close() によって登録解除できるようにするクラス
 */
abstract class DisposableObserverBase<T>(private var data: LiveData<T>?, private val callback: (v: T) -> Unit) : Observer<T>, IDisposableEx, Closeable {
    override fun onChanged(value: T) {
        callback(value)
    }
    override fun dispose() {
        data?.removeObserver(this)
        data = null
    }

    override val disposed: Boolean
        get() = data==null

    override fun close() =dispose()
}

class DisposableObserver<T>(data:LiveData<T>, owner:LifecycleOwner, callback: (v: T) -> Unit):DisposableObserverBase<T>(data, callback) {
    init {
        data.observe(owner, this)
    }
}
class DisposableForeverObserver<T>(data:LiveData<T>, callback: (v: T) -> Unit):DisposableObserverBase<T>(data, callback) {
    init {
        data.observeForever(this)
    }
}

/**
 * LiveDataにオブザーバーを登録し、登録解除用の IDisposable または、Closeable を返す。
 */
fun <T> LiveData<T>.disposableObserve(owner: LifecycleOwner, fn:(value:T)->Unit) : IDisposableEx
        = DisposableObserver(this,owner,fn)
fun <T> LiveData<T>.disposableObserveForever(fn:(value:T)->Unit) : IDisposableEx
        = DisposableForeverObserver(this,fn)

fun <T> LiveData<T>.closableObserve(owner: LifecycleOwner, fn:(value:T)->Unit) : Closeable
        = DisposableObserver(this,owner,fn)
fun <T> LiveData<T>.closableObserveForever(fn:(value:T)->Unit) : Closeable
        = DisposableForeverObserver(this,fn)

