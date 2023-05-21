package io.github.toyota32k.utils

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

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
            mutex.tryLock(owner)
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

    /**
     * 名前でミューテックスをクリア
     * もし待っている人がいると具合が悪い。
     * 多分使わない。
     */
    fun removeMutex(name:String) {
        synchronized(mutexMap) {
            mutexMap.remove(name)
        }
    }
}