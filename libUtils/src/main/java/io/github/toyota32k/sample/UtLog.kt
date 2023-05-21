package io.github.toyota32k.sample

import android.util.Log
import io.github.toyota32k.bindit.BuildConfig
import java.io.Closeable
import java.lang.Exception

class UtLog @JvmOverloads constructor(val tag:String, val parent:UtLog?=null, val omissionNamespace:String?=parent?.omissionNamespace, private val outputClassName:Boolean=true, private val outputMethodName:Boolean=true) {
    constructor(tag:String, parent:UtLog?, omissionNamespaceClass:Class<*>, outputClassName:Boolean=true, outputMethodName:Boolean=true):this(tag, parent, namespaceOfClass(omissionNamespaceClass), outputClassName, outputMethodName)
    companion object {
        var logLevel = Log.DEBUG

        fun hierarchicTag(tag:String, parent:UtLog?):String {
            return if(parent!=null) {
                "${hierarchicTag(parent.tag, parent.parent)}.${tag}"
            } else {
                tag
            }
        }

        fun namespaceOfClass(clazz:Class<*>):String {
            return clazz.name.substringBeforeLast(".", "").run {
                if(isEmpty()) {
                    clazz.name
                } else {
                    "$this."
                }
            }
        }
//        fun className():String {
//            return Thread.currentThread().stackTrace[2].className
//        }
//        fun className(omissionNamespace: String?):String {
//            val cn = className()
//            if(!omissionNamespace.isNullOrBlank() && cn.startsWith(omissionNamespace)) {
//                return cn.substring(omissionNamespace.length)
//            } else {
//                return cn
//            }
//        }
//        fun methodName():String {
//            return Thread.currentThread().stackTrace[2].methodName
//        }
//        fun classAndMethodName():Pair<String,String> {
//            val e = Thread.currentThread().stackTrace[2]
//            return Pair(e.className, e.methodName)
//        }
//
//        fun assert(chk:Boolean, msg:String) {
//            if(!chk) {
//                UtLogger.stackTrace(Exception("assertion failed."), msg)
//            }
//        }

        val libLogger:UtLog by lazy { UtLog("libUtils") }
    }

    private val logger = UtLoggerInstance(hierarchicTag(tag,parent))

    private fun stripNamespace(classname:String):String {
        if(!omissionNamespace.isNullOrBlank() && classname.startsWith(omissionNamespace)) {
            return classname.substring(omissionNamespace.length)
        } else {
            return classname
        }
    }

//    var stackOffset:Int = 4

    private fun getCallerStack():StackTraceElement {
        val stack = Throwable().stackTrace  // Thread.currentThread().stackTrace  Throwable().stackTraceの方が速いらしい。
        val loggerClassName = this.javaClass.name
        val chronosClassName = Chronos::class.java.name
        var n = 0
        while(n<stack.size-1 && !stack[n].className.startsWith(loggerClassName)) { n++ }
        while(n<stack.size-1 && (stack[n].className.startsWith(loggerClassName)||stack[n].className.startsWith(chronosClassName))) { n++ }
        return stack[n]
    }

    fun compose(message:String?):String {
        return if(outputClassName||outputMethodName) {
//            val stack = Thread.currentThread().stackTrace
//            var n:Int = stackOffset
//            var e = stack[n]
//            while(e.className == this.javaClass.name) {
////            while(e.methodName.endsWith("\$default") && n<stack.size) {
//                n++
//                e = stack[n]
//            }
            val e = getCallerStack()
            if(!outputClassName) {
                if(message!=null) "${e.methodName}: $message" else e.methodName
            } else if(!outputMethodName) {
                if(message!=null) "${stripNamespace(e.className)}: ${message}" else stripNamespace(e.className)
            } else {
                if(message!=null) "${stripNamespace(e.className)}.${e.methodName}: ${message}" else "${stripNamespace(e.className)}.${e.methodName}"
            }
        } else {
            message ?: ""
        }
    }

    @JvmOverloads
    fun debug(msg: String?=null) {
        if(logLevel<=Log.DEBUG) {
            logger.debug(compose(msg))
        }
    }
    fun debug(fn:()->String?) {
        if(logLevel<=Log.DEBUG) {
            logger.debug(compose(fn()?:return))
        }
    }
    fun debug(flag:Boolean, fn:()->String) {
        if(flag && logLevel<=Log.DEBUG) {
            logger.debug(compose(fn()))
        }
    }

    @JvmOverloads
    fun warn(msg: String?=null) {
        logger.warn(compose(msg))
    }

    @JvmOverloads
    fun error(msg: String?=null) {
        logger.error(compose(msg))
    }

    @JvmOverloads
    fun error(e:Throwable, msg:String?=null) {
        logger.stackTrace(e, compose(msg))
    }

    @JvmOverloads
    fun info(msg: String?=null) {
        logger.info(compose(msg))
    }

    @JvmOverloads
    fun verbose(msg: String?=null) {
        if(logLevel<=Log.VERBOSE) {
            logger.verbose(compose(msg))
        }
    }
    fun verbose(fn: () -> String) {
        if(logLevel<=Log.VERBOSE) {
            logger.verbose(compose(fn()))
        }
    }

    @JvmOverloads
    fun stackTrace(e:Throwable, msg:String?=null) {
        logger.stackTrace(e, compose(msg))
    }

    @JvmOverloads
    fun print(level:Int, msg:String?=null) {
        when(level) {
            Log.ERROR -> ::error
            Log.WARN -> ::warn
            Log.INFO -> ::info
            Log.DEBUG -> ::debug
            else->::verbose
        }(msg)
    }

    @JvmOverloads
    fun assert(chk:Boolean, msg:String?=null) {
        if(!chk) {
            stackTrace(Exception("assertion failed."), compose(msg))
        }
    }

    @JvmOverloads
    fun assertStrongly(chk:Boolean, msg:String?=null) {
        if(!chk) {
            stackTrace(Exception("assertion failed."), msg)
            if (BuildConfig.DEBUG) {
                // デバッグ版なら例外を投げる
                error(compose(msg))
            }
        }
    }

    @JvmOverloads
    fun scopeWatch(msg:String?=null) : Closeable {
        val composed = compose(msg)
        logger.debug("$composed - enter")
        return ScopeWatcher { logger.debug("$composed - exit") }
    }

    private class ScopeWatcher(val leaving:()->Unit) : Closeable {
        override fun close() {
            leaving()
        }
    }

    inline fun <T> scopeCheck(msg:String?=null, level: Int=Log.DEBUG, fn:()->T):T {
        return try {
            print(level, "$msg - enter")
            fn()
        } finally {
            print(level, "$msg - exit")
        }
    }

    @JvmOverloads
    inline fun <T> chronos(tag:String="TIME", msg:String?=null, level: Int=Log.DEBUG,  fn:()->T):T {
        return if(when(level) {
            Log.DEBUG,Log.VERBOSE -> BuildConfig.DEBUG
            else -> true
        }) {
            Chronos(this, tag = tag, logLevel = level).measure(msg) {
                fn()
            }
        } else fn()
    }

}