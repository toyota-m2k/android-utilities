@file:Suppress("unused")

package io.github.toyota32k.utils

import io.github.toyota32k.logger.UtLog
import io.github.toyota32k.logger.UtLogConfig

object UtLib {
    var DEBUG:Boolean
        get() = UtLogConfig.debug
        set(v) { UtLogConfig.debug = v }
    var logger = UtLog("UtLib")
}


/**
 * 例外を投げるAssert
 */
fun utAssert(check:Boolean,msg:(()->String)?=null) {
    if(!check) {
        if (UtLib.DEBUG) {
            error(msg?.invoke() ?: "Assertion failed")
        } else {
            UtLib.logger.stackTrace(AssertionError(msg?.invoke() ?: "Assertion failed"))
        }
    }
}

/**
 * StackTraceを出力するだけのやさしいAssert
 */
fun utTenderAssert(check:Boolean,msg:(()->String)?=null) {
    if (UtLib.DEBUG && !check) {
        UtLib.logger.assert(check, msg?.invoke() ?: "Assertion failed")
    }
}

inline fun Boolean.onTrue(fn:()->Unit):Boolean {
    if(this) {
        fn()
    }
    return this
}
inline fun Boolean.onFalse(fn:()->Unit):Boolean {
    if(!this) {
        fn()
    }
    return this
}

inline fun <R> Boolean.letOnTrue(fn:()->R) : R? {
    return if(this) {
        fn()
    } else {
        null
    }
}

inline fun <R> Boolean.letOnFalse(fn:()->R) : R? {
    return if(!this) {
        fn()
    } else {
        null
    }
}

fun String?.contentOrDefault(fn:()->String) : String {
    return if(this.isNullOrEmpty()) fn() else this
}
fun String?.contentOrDefault(def:String) : String {
    return if(this.isNullOrEmpty()) def else this
}

/**
 * Builder のチェーンの中に条件分岐を入れたいとき用
 * Some.Builder()
 *  .foo()
 *  .bar()
 *  .conditional(flag) { baz() }
 *  .apply( if(flag) baz() }
 *  .qux()
 */
fun <T> T.conditional(condition:Boolean, fn:T.()->Unit):T {
    if(condition) {
        fn()
    }
    return this
}