package io.github.toyota32k.utils

import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.CoroutineContext

/**
 * Flowに (LiveDataっぽい) observe メソッドを付与するクラス
 * LiveData # observeForever に相当するメソッドはないが、coroutineContext に Dispatchers.IO などを渡せば、forever的に使えるはず。
 * Observerの登録解除は、observe()の戻り値に対する dispose() でよいが、disposerにゴミが残るので、頻繁に解除するなら、clean()も呼んだ方がよいと思う。
 * removeObserver()を使えば、ゴミが残らず衛生的。
 */
class ObservableFlow<T>(val flow: Flow<T>):IDisposable, Flow<T> by flow {
    private val disposer = Disposer()
    fun observe(owner: LifecycleOwner, fn:(T)->Unit):IDisposable {
        assert(!disposer.disposed)
        return flow.disposableObserve(owner, fn).apply {
            disposer.register(this)
        }
    }
    fun observe(coroutineContext: CoroutineContext, fn:(T)->Unit):IDisposable {
        assert(!disposer.disposed)
        return flow.disposableObserve(coroutineContext, fn).apply {
            disposer.register(this)
        }
    }

    fun removeObserver(observer: IDisposable) {
        disposer.unregister(observer)
    }

    fun clean() {
        disposer.clean()
    }

    override fun dispose() {
        disposer.dispose()
    }
}
