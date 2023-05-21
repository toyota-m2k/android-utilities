package io.github.toyota32k.utils

import androidx.annotation.MainThread
import androidx.lifecycle.*
import java.util.concurrent.atomic.AtomicBoolean

interface ISingleLiveEvent<T> {
    @MainThread
    fun observe(owner: LifecycleOwner, observer: Observer<in T>)
    @MainThread
    fun removeObserver(observer: Observer<in T>)
}

/**
 * LiveDataベースのイベントクラス
 *
 * LiveDataを生でイベントとして使うのはよくないらしい。
 * https://medium.com/androiddevelopers/livedata-with-snackbar-navigation-and-other-events-the-singleliveevent-case-ac2622673150
 * ↑で紹介されている SingleLiveEvent を参考に使いやすく再構成。
 *
 * 制限
 *  - Observerは１つしか登録できない
 *  - observeForever()はサポートしない
 *
 */
class SingleLiveEvent<T:Any> : ISingleLiveEvent<T> {
    private val subject = SingleLiveData<T>()

    val liveData:LiveData<T>
        get() = subject

    fun fire(v:T) {
        subject.value = v
    }

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        subject.observe(owner, observer)
    }

    override fun removeObserver(observer: Observer<in T>) {
        subject.removeObserver(observer)
    }
}

class SingleLiveData<T> : MutableLiveData<T>(), Observer<T> {
    private val pending = AtomicBoolean(false)
    private var originalObserver:Observer<in T>? = null

    @MainThread
    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        if (hasActiveObservers()) {
            UtLog.libLogger.error("only one observer can be registered to SingleLiveData")
            throw IllegalStateException("SingleLiveData.observe prevent from registering multiple observers.")
        }

        // Observe the internal MutableLiveData
        originalObserver = observer
        super.observe(owner, this)
    }

    @MainThread
    override fun removeObserver(observer: Observer<in T>) {
        if(observer==originalObserver) {
            originalObserver = null
            super.removeObserver(this)
        }
    }

    override fun observeForever(observer: Observer<in T>) {
        if (hasActiveObservers()) {
            UtLog.libLogger.error("only one observer can be registered to SingleLiveData")
            throw IllegalStateException("SingleLiveData.observe prevent from registering multiple observers.")
        }

        // Observe the internal MutableLiveData
        originalObserver = observer
        super.observeForever(this)
    }

    override fun removeObservers(owner: LifecycleOwner) {
        super.removeObservers(owner)
        originalObserver = null
    }

    @MainThread
    override fun setValue(t: T?) {
        pending.set(true)
        super.setValue(t)
    }

    override fun onChanged(value: T) {
        if (pending.compareAndSet(true, false)) {
            originalObserver?.onChanged(value)
        }
    }
}