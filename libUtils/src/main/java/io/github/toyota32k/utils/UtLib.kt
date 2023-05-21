@file:Suppress("unused")

package io.github.toyota32k.utils

object UtLib {
    var DEBUG = false
    @JvmStatic
    fun initialize(externalLogger:IUtExternalLogger, debug:Boolean=false) {
        UtLoggerInstance.externalLogger = externalLogger
        DEBUG = debug
    }
}

/**
 * 例外を投げるAssert
 */
fun utAssert(check:Boolean,msg:(()->String)?=null) {
    if(!check) {
        if (UtLib.DEBUG) {
            error(msg?.invoke() ?: "Assertion failed")
        } else {
            UtLog.libLogger.stackTrace(AssertionError(msg?.invoke() ?: "Assertion failed"))
        }
    }
}

/**
 * StackTraceを出力するだけのやさしいAssert
 */
fun utTenderAssert(check:Boolean,msg:(()->String)?=null) {
    if (UtLib.DEBUG && !check) {
        UtLog.libLogger.assert(check, msg?.invoke() ?: "Assertion failed")
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