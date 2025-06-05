package io.github.toyota32k.utils

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout

/**
 * 名前付きミューテックス
 */
object NamedMutex {
    val mutexMap = mutableMapOf<String, Mutex>()

    /**
     * ミューテックスをロックする。
     * ロックできなければfalseを返す。
     * @param name ミューテックスの名前
     * @param owner ミューテックスのオーナー（無指定なら null）
     */
    fun tryLock(name:String, owner:Any?=null):Boolean {
        return synchronized(mutexMap) {
            val mutex = mutexMap[name] ?: Mutex().apply { mutexMap[name] = this }
            try {
                mutex.tryLock(owner)
            } catch(_: IllegalStateException) {
                // すでにロックされていて、tryLock が false を返すのは、owner == null の場合だけ。
                // ownerを指定していると、同じ owner に対する tryLock に対して IllegalStateExceptionをスローする。
                // ちょっとおもてたんとちがう
                false
            }
        }
    }

    /**
     * ミューテックスはロックされているか？
     * @param name ミューテックスの名前
     */
    fun isLocked(name:String):Boolean {
        return synchronized(mutexMap) {
            mutexMap[name]?.isLocked ?: false
        }
    }

    /**
     * オーナーはミューテックスを保持しているか？
     * @param name ミューテックスの名前
     * @param owner ミューテックスのオーナー
     */
    fun holdsLock(name:String, owner:Any) : Boolean {
        return synchronized(mutexMap) {
            mutexMap[name]?.holdsLock(owner) ?: false
        }
    }

    /**
     * tryLock==trueの場合に、ロックを解除する
     */
    fun unlock(name:String, owner:Any?=null) {
        synchronized(mutexMap) {
            mutexMap[name]?.unlock(owner)
        }
    }

    /**
     * ロックしてごにょごにょする
     */
    suspend inline fun <R> withLock(name:String, owner:Any?=null, action:()->R):R {
        val mutex = synchronized(mutexMap) {
            mutexMap[name] ?: Mutex().apply { mutexMap[name] = this }
        }
        return mutex.withLock(owner, action)
    }

    @Throws(TimeoutCancellationException::class)
    suspend inline fun <R> withLock(name:String, timeout:Long, owner:Any?=null, crossinline action:()->R):R {
        val mutex = synchronized(mutexMap) {
            mutexMap[name] ?: Mutex().apply { mutexMap[name] = this }
        }
        return withTimeout(timeout) {
            mutex.withLock(owner, action)
        }
    }

    suspend inline fun <R> withLockOrDefault(name:String, timeout:Long, def:R, owner:Any?=null, crossinline action:()->R):R {
        val mutex = synchronized(mutexMap) {
            mutexMap[name] ?: Mutex().apply { mutexMap[name] = this }
        }
        return try {
            withTimeout(timeout) {
                mutex.withLock(owner, action)
            }
        } catch(e: TimeoutCancellationException) {
            def
        }
    }

    /**
     * 名前でミューテックスをクリア
     * もし待っている人がいると具合が悪い。
     * 待っている人がまったくいないことが明らかな場合以外は使ってはいけない。
     */
    fun removeMutex(name:String) {
        synchronized(mutexMap) {
            mutexMap.remove(name)
        }
    }
}