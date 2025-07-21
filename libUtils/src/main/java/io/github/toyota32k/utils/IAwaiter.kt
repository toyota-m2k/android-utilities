package io.github.toyota32k.utils

/**
 * キャンセル可能な待ち合わせ用 i/f
 */
interface IAwaiter<T> {
    suspend fun await():T
    fun cancel()
}
