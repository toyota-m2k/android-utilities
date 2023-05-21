package io.github.toyota32k.utils

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

interface IUtAwaitable<T> {
    suspend fun await():T
    fun await(fn:(T)->Unit) {
        MainScope().launch {
            val r = await()
            fn(r)
        }
    }
    fun await(fn:()->Unit) {
        MainScope().launch {
            await()
            fn()
        }
    }
}
open class UtAwaitable<T>:IUtAwaitable<T> {
    val completion = MutableStateFlow(false)
    var result:T? = null
        private set

    fun complete(result:T) {
        this.result = result
        completion.value = true
    }

    override suspend fun await(): T {
        completion.filter { it }.first()
        return result!!
    }
}

fun UtAwaitable<Unit>.complete() {
    complete(Unit)
}
