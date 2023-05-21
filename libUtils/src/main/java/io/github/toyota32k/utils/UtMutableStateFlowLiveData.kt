package io.github.toyota32k.utils

import androidx.lifecycle.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * MutableStateFlow を MutableLiveData として利用するための変換クラス
 * 通常は、MutableStateFlow.asMutableLiveData() を使って構築する。
 */
class UtMutableStateFlowLiveData<T>(val flow: MutableStateFlow<T>, lifecycleOwner: LifecycleOwner?=null): MutableLiveData<T>(), Observer<T> {
    init {
        value = flow.value
        if(null!=lifecycleOwner) {
            attachToLifecycle(lifecycleOwner)
        }
    }

    fun attachToLifecycle(lifecycleOwner: LifecycleOwner) {
        observe(lifecycleOwner, this)
        // これが、今後推奨される方法だと思うが、repeatOnLifecycle を使うには、lifecycle_version = "2.4.0" が必要で、これがまだ alpha なので、当面は利用を見合わせる。
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                flow.collect {
                    postValue(it)
                }
            }
        }
//        lifecycleOwner.lifecycleScope.launchWhenStarted {
//            flow.collect {
//                if(value!==it) {
//                    value = it
//                }
//            }
//        }
    }

    override fun onChanged(value: T) {
        flow.value = value
    }
}

/**
 * MutableStateFlow --> MutableLiveData 変換
 */
fun <T> MutableStateFlow<T>.asMutableLiveData(lifecycleOwner: LifecycleOwner): MutableLiveData<T>
        = UtMutableStateFlowLiveData(this, lifecycleOwner)
