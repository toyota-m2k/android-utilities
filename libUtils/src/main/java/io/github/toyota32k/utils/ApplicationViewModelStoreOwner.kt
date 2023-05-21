package io.github.toyota32k.utils

import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

object ApplicationViewModelStoreOwner : ViewModelStoreOwner {
    private val mViewModelStore:ViewModelStore by lazy { ViewModelStore() }

    override val viewModelStore: ViewModelStore
        get() = io.github.toyota32k.utils.ApplicationViewModelStoreOwner.mViewModelStore

    // to be called from Application.onTerminate()
    fun releaseViewModelStore() {
        io.github.toyota32k.utils.ApplicationViewModelStoreOwner.mViewModelStore.clear()
    }

}