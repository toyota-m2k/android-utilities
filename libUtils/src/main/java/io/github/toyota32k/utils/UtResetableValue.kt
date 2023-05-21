package io.github.toyota32k.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/*
 * lateinit var some:SomeClass
 * とすれば、someは、NonNullとしてチェックなしに使えて便利なのだが、一旦、値をセットすると未初期化状態に戻せない（と思う）。
 * だから open --> close --> open のように破棄後に再利用されるクラスを作る場合など、無効状態を表すために、やむを得ず nullableにすることがある。
 * そこで、lateinit的に（nullチェックなしに）使え、かつ、未初期化状態にリセット可能なクラスを作ってみた。
 * 有効期間をプログラマの責任で管理しなければならないことは言うまでもない。
 */

/**
 * リセット可能なlateinit的クラスのi/f定義
 */
interface IUtResetableValue<T> {
    /**
     * 保持している値 (利用時にnotnullであることはプログラマが保証する）
     */
    var value:T

    /**
     * 値を保持しているかどうか？
     */
    val hasValue:Boolean

    /**
     * 未初期化状態に戻す。
     * @param preReset リセット前の処理（不要ならnull)
     */
    fun reset(preReset:((T)->Unit)?=null)

    /**
     * 未初期化状態なら（値がセットされていなれば）値をセットする。
     * @param fn 値を返すやつ
     */
    fun setIfNeed(fn:()->T)
}

/**
 * 最も基本的な値クラス
 */
class UtResetableValue<T> : IUtResetableValue<T> {
    private var rawValue:T? = null
    override var value:T
        get() = rawValue!!
        set(v) { rawValue = v }
    override val hasValue
        get() = rawValue!=null
    override fun reset(preReset:((T)->Unit)?) {
        val rv = rawValue ?: return
        preReset?.invoke(rv)
        rawValue = null
    }
    override fun setIfNeed(fn:()->T) {
        if(rawValue == null) {
            value = fn()
        }
    }
}

/**
 * valueが要求されたときに初期化する lazy的な値クラス
 * @param fn 値初期化関数
 */
class UtLazyResetableValue<T>(val fn:()->T): IUtResetableValue<T>  {
    private var rawValue:T? = null
    override var value:T
        get() = rawValue ?: fn().apply { rawValue = this }
        set(v) { rawValue = v }
    override val hasValue
        get() = rawValue!=null
    override fun reset(preReset:((T)->Unit)?) {
        val rv = rawValue ?: return
        preReset?.invoke(rv)
        rawValue = null
    }
    override fun setIfNeed(fn:()->T) {
        if(rawValue == null) {
            value = fn()
        }
    }
}

/**
 * 値を保持するためにMutableStateFlowを使い、Flow<T?> として扱える値クラス。
 * Flow i/f は、T? (nullable)となる点はご愛敬。
 */
class UtResetableFlowValue<T>(private val flow: MutableStateFlow<T?> = MutableStateFlow(null)): IUtResetableValue<T>, Flow<T?> by flow {
    override var value:T
        get() = flow.value!!
        set(v) { flow.value = v }
    override val hasValue
        get() = flow.value!=null
    override fun reset(preReset:((T)->Unit)?) {
        val v = flow.value
        if(v!=null) {
            preReset?.invoke(v)
            flow.value = null
        }
    }
    override fun setIfNeed(fn:()->T) {
        if(flow.value == null) {
            value = fn()
        }
    }
}

/**
 * @param allowKeepNull     trueにすると、nullがセットされてもそれを立派な値として hasValue = true にする。
 */
class UtNullableResetableValue<T>(private val allowKeepNull:Boolean=false, private val lazy:(()->T?)?=null){
    var value:T? = null
        get() {
            if(!hasValue && lazy!=null) {
                field = lazy.invoke()
                if(field!=null || allowKeepNull) {
                    hasValue = true
                }
            }
            return field
        }
        private set

    var hasValue:Boolean = false
        private set

    fun setIfNeed(fn:()->T?):T? {
        if(!hasValue) {
            value = fn()
            if(value!=null ||allowKeepNull) {
                hasValue = true
            }
        }
        return value
    }

    suspend fun setIfNeedAsync(fn:suspend ()->T?):T? {
        if(!hasValue) {
            value = fn()
            if(value!=null ||allowKeepNull) {
                hasValue = true
            }
        }
        return value
    }

    fun reset(preReset:((T)->Unit)?) {
        if(value!=null && preReset!=null) {
            preReset.invoke(value!!)
        }
        hasValue = false
        value = null
    }
}
