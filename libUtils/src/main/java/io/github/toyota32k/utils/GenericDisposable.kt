package io.github.toyota32k.utils

import java.io.Closeable

/**
 * 後始末が必要な処理を IDisposable i/f にラップするクラス
 */
class GenericDisposable(val fnDispose:()->Unit) : IDisposable {
    private var disposed = false
    override fun dispose() {
        if(!disposed) {
            disposed = true
            fnDispose()
        }
    }

    companion object {
        fun create(fn:()->(()->Unit)):GenericDisposable {
            return GenericDisposable(fn())
        }
    }
}

/**
 * Closeable --> IDisposable 変換
 */
fun Closeable.asDisposable() : IDisposable {
    return GenericDisposable.create {
        { this.close() }
    }
}
