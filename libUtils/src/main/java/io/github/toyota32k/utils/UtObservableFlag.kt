package io.github.toyota32k.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.io.Closeable

/**
 * Flow による監視可能なフラグクラス。
 * 内部的にフラグ状態はカウンタとして保持しており、ネストした呼び出しが可能。
 * trySetIfNot(), withFlagIfNot()を使うことで、普通のフラグ的にも使える。
 */
class UtObservableFlag private constructor(val flag:MutableStateFlow<Int>, val boolFlag: Flow<Boolean>) : Flow<Boolean> by boolFlag {
    private constructor(flag:MutableStateFlow<Int>) : this(flag, flag.map { it!=0 })
    constructor():this(MutableStateFlow(0))

    /**
     * 現在、フラグは立っているか？
     */
    val flagged:Boolean get() = synchronized(this) {
        flag.value!=0
    }

    /**
     * カウンタをインクリメントする。
     * 必ず、reset()を呼び出すこと。
     */
    fun set() = synchronized(this) {
        flag.value++
    }

    /**
     * フラグが立っていなければ、フラグを立ててtrueを返す。
     * もし、フラグが立っていれば、何もしないで、falseを返す。
     * このメソッドがtrueを返した場合に限り、必ずreset()を呼び出すこと。
     */
    fun trySetIfNot():Boolean = synchronized(this) {
        if(flag.value==0) {
            flag.value++
            true
        } else {
            false
        }
    }

    /**
     * カウンタをデクリメントする。
     */
    fun reset() = synchronized(this) {
        flag.value--
    }

    /**
     * カウンタをインクリメントして、fnを実行。
     * fnが終わったらカウンタを元に戻す。
     */
    inline fun <T> withFlag(fn:()->T):T {
        set()
        try {
            return fn()
        } finally {
            reset()
        }
    }

    /**
     * フラグが立っていなければ、カウンタをインクリメントして、fnを実行。
     * fnが終わったらカウンタを元に戻す。
     * フラグが立っていれば 何もしないで　null を返す。
     */
    inline fun <T> withFlagIfNot(fn:()->T):T? {
        if(!trySetIfNot()) return null
        try {
            return fn()
        } finally {
            reset()
        }
    }

    private inner class Closer : Closeable {
        override fun close() {
            reset()
        }
    }

    fun closeableTrySetIfNot():Closeable? {
        return if(trySetIfNot()) {
            Closer()
        } else null
    }

    fun closeableSet():Closeable {
        set()
        return Closer()
    }
}