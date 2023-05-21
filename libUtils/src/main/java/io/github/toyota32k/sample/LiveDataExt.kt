@file:Suppress("unused")

package io.github.toyota32k.sample

import androidx.lifecycle.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.Closeable

/**
 * LiveData.map だと、初期値が反映されないので、初期値の反映が必要なときはこちらを使う。
 */
fun <T,R> LiveData<T>.mapEx(fn:(T?)->R): LiveData<R> {
    return MediatorLiveData<R>().also { med ->
        med.addSource(this) { med.value = fn(it) }
        med.value = fn(this.value)
    }
}

//
//fun <T,R> LiveData<T>.flatMap(fn:(T)-> LiveData<R>): LiveData<R>
//        = Transformations.switchMap(this) { x->fn(x)}
//
//fun <T> LiveData<T>.distinctUntilChanged(): LiveData<T> {
//    var first = true
//    return MediatorLiveData<T>().also { med->
//        med.addSource(this ) { current ->
//            val prev = med.value
//            if (first || (prev == null && current != null) || (prev != null && prev != current)) {
//                first = false
//                med.value = current
//            }
//        }
//    }
//}

fun <T> LiveData<T>.filter(predicate:(T?)->Boolean): LiveData<T> {
    return MediatorLiveData<T>().also { med->
        med.addSource(this) { current ->
            if (predicate(current)) {
                med.value = current
            }
        }
    }
}

fun <T> LiveData<T>.notNull(): LiveData<T> {
    return MediatorLiveData<T>().also { med ->
        med.addSource(this) { current ->
            if (current != null) {
                med.value = current
            }
        }
    }
}

fun <T1,T2,R> combineLatest(src1:LiveData<T1>,src2: LiveData<T2>, fn:(T1?,T2?)->R?):LiveData<R> {
    return MediatorLiveData<R>().also { med ->
        fun combine() {
            med.value = fn(src1.value, src2.value)
        }
        med.addSource(src1) { combine() }
        med.addSource(src2) { combine() }
        combine()
    }
}

//fun <T,T1,R> LiveData<T>.combineLatest(src2: LiveData<T1>, fn:(T?, T1?)->R?): LiveData<R> {
//    val src = this
//    return MediatorLiveData<R>().also { med ->
//        fun combine() {
//            med.value = fn(src.value, src2.value)
//        }
//        med.addSource(src) { combine() }
//        med.addSource(src2) { combine() }
//        combine()
//    }
//}

fun <T1,T2,T3,R> combineLatest(src1:LiveData<T1>, src2: LiveData<T2>, src3:LiveData<T3>, fn:(T1?, T2?, T3?)->R?): LiveData<R> {
    return MediatorLiveData<R>().also { med ->
        fun combine() {
            med.value = fn(src1.value, src2.value,src3.value)
        }
        med.addSource(src1) { combine() }
        med.addSource(src2) { combine() }
        med.addSource(src3) { combine() }
        combine()
    }
}

fun <T1,T2,T3,T4,R> combineLatest(src1:LiveData<T1>, src2: LiveData<T2>, src3:LiveData<T3>, src4:LiveData<T4>, fn:(T1?, T2?, T3?, T4?)->R?): LiveData<R> {
    return MediatorLiveData<R>().also { med ->
        fun combine() {
            med.value = fn(src1.value, src2.value,src3.value,src4.value)
        }
        med.addSource(src1) { combine() }
        med.addSource(src2) { combine() }
        med.addSource(src3) { combine() }
        med.addSource(src4) { combine() }
        combine()
    }
}

fun <T1,T2,T3,T4,T5,R> combineLatest(src1:LiveData<T1>, src2: LiveData<T2>, src3:LiveData<T3>, src4:LiveData<T4>, src5:LiveData<T5>, fn:(T1?, T2?, T3?, T4?, T5?)->R?): LiveData<R> {
    return MediatorLiveData<R>().also { med ->
        fun combine() {
            med.value = fn(src1.value, src2.value,src3.value,src4.value,src5.value)
        }
        med.addSource(src1) { combine() }
        med.addSource(src2) { combine() }
        med.addSource(src3) { combine() }
        med.addSource(src4) { combine() }
        med.addSource(src5) { combine() }
        combine()
    }
}

///**
// * LiveData.observe()で、ラムダ式を使えるようにする拡張メソッド。
// * （今は、直接ラムダ式を渡せない。そのうち、Kotlinがサポートするようになるかもしれないが。）
// *
// * @param owner Activity or Fragment
// * @param fn: リスナー
// * @return 内部で生成した Observer （removeObserverするなら、どこかに覚えておく。不要なら無視してOK）
// */
//fun <T> LiveData<T>.observe(owner: LifecycleOwner, fn:(v:T?)->Unit) : Observer<T?> {
//    return Observer(fn).also {
//        this.observe(owner, it)
//    }
//}
//
//fun <T> LiveData<T>.observe(view: View, fn:(v:T?)->Unit) : Observer<T?>? {
//    val owner = view.lifecycleOwner()?:return null
//    return this.observe(owner, fn)
//}

//class CloseableObserver<T>(private val data: LiveData<T>, owner: LifecycleOwner, private val callback:(v:T?)->Unit): Observer<T?>, Closeable {
//    init {
//        data.observe(owner, this)
//    }
//    override fun onChanged(t: T?) {
//        callback(t)
//    }
//    override fun close() {
//        data.removeObserver(this)
//    }
//    // alias
//    fun dispose() = close()
//}

//fun <T> LiveData<T>.closableObserve(owner: LifecycleOwner, fn:(v:T?)->Unit) : Closeable
//        = CloseableObserver(this,owner,fn)

//fun <T> LiveData<T>.observe(view: View, fn:(v:T?)->Unit) : Closeable? {
//    val owner = view.lifecycleOwner()?:return null
//    return CloseableObserver(this, owner, fn)
//}

class Closeables : Closeable, IDisposable {
    private val list = mutableListOf<Closeable>()

    operator fun plusAssign(c: Closeable?) {
        if(c!=null) {
            list.add(c)
        }
    }

    operator fun minusAssign(c: Closeable?) {
        if(c!=null && list.remove(c)) {
            c.close()
        }
    }

    override fun close() {
        list.forEach {
            it.close()
        }
        list.clear()
    }

    // alias
    override fun dispose() = close()
}



fun or(vararg args:LiveData<Boolean>):LiveData<Boolean> {
    return MediatorLiveData<Boolean>().also { med ->
        fun check(@Suppress("UNUSED_PARAMETER") v:Boolean) {
            med.value = (null!=args.find {it.value==true})
        }
        for(v in args) {
            med.addSource(v, ::check)
        }
        check(false)
    }
}

fun and(vararg args:LiveData<Boolean>):LiveData<Boolean> {
    return MediatorLiveData<Boolean>().also { med ->
        fun check(@Suppress("UNUSED_PARAMETER") v:Boolean) {
            med.value = (null==args.find {it.value!=true})
        }
        for(v in args) {
            med.addSource(v, ::check)
        }
        check(false)
    }
}

fun LiveData<Boolean>.not():LiveData<Boolean> {
    return this.mapEx { !(it==true) }
}

//@ExperimentalCoroutinesApi
//fun <T> LiveData<T>.toFlow(): Flow<T?> = callbackFlow {
//    offer(value)
//    val observer = Observer<T> {
//        offer(it)
//    }
//    observeForever(observer)
//    awaitClose { removeObserver(observer) }
//}

//fun <T> LiveData<T>.toPublisher(owner: LifecycleOwner) =
//        LiveDataReactiveStreams.toPublisher(owner,this)
//
//fun <T> Publisher<T>.toLiveData() =
//        LiveDataReactiveStreams.fromPublisher(this)




