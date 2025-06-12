package io.github.toyota32k.utils

/**
 * rxjava の Disposable を使おうかとも思ったけど、rxjavaを導入するほどでもなさそうなので、独自定義にしておく。
 */
interface IDisposable {
    fun dispose()
}

interface IDisposableEx : IDisposable {
    val disposed:Boolean
}

inline fun <T> IDisposable.use(fn:()->T):T {
    return try {
        fn()
    } finally {
        dispose()
    }
}
