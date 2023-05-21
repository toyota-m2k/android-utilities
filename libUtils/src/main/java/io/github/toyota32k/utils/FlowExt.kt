package io.github.toyota32k.utils

import kotlinx.coroutines.flow.MutableStateFlow

/**
 * viewModel.someBooleanProperty.value = !viewModel.someBooleanProperty.value
 * と書くのが煩わしいです。
 * --> viewModel.someBooleanProperty.toggle()
 */
fun MutableStateFlow<Boolean>.toggle() {
    value = !value
}

