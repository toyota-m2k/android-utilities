package io.github.toyota32k.utils.lifecycle

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// io.github.toyota32k.utils.IUtPropOwner の LiveData版
interface IUtLiveDataPropOwner {
    val <T> LiveData<T>.mutable: MutableLiveData<T>
        get() = this as MutableLiveData<T>
}

