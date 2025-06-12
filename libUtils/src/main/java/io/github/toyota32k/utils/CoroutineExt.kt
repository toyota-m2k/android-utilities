package io.github.toyota32k.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

/**
 * 親をキャンセルすると子もキャンセルされるが、
 * 子をキャンセルしても親はキャンセルされない CoroutineContext を作成
 */
fun CoroutineContext.childContext(): CoroutineContext
    = this + Job(this[Job])

/**
 * 親をキャンセルすると子もキャンセルされるが、
 * 子をキャンセルしても親はキャンセルされない CoroutineScope を作成
 */
fun CoroutineScope.childScope(): CoroutineScope
    = CoroutineScope(coroutineContext.childContext())