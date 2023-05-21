package io.github.toyota32k.sample

import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

object ApplicationViewModelStoreOwner : ViewModelStoreOwner {
    private val mViewModelStore:ViewModelStore by lazy { ViewModelStore() }

    override val viewModelStore: ViewModelStore
        get() = mViewModelStore

    // to be called from Application.onTerminate()
    fun releaseViewModelStore() {
        mViewModelStore.clear()
    }

}