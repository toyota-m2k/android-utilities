package io.github.toyota32k.utils

import java.io.Closeable

/**
 * 後始末が必要な処理を Closeable i/f にラップするクラス
 */
class GenericCloseable(val fnClose:()->Unit): Closeable {
    private var disposed = false
    override fun close() {
        if(!disposed) {
            disposed = true
            fnClose()
        }
    }

    companion object {
        fun create(fn:()->(()->Unit)):GenericCloseable {
            return GenericCloseable(fn())
        }
    }
}

/**
 * IDisposable --> Closeable 変換
 */
fun IDisposable.asCloseable() : Closeable {
    return GenericCloseable.create {
        { this.dispose() }
    }
}
