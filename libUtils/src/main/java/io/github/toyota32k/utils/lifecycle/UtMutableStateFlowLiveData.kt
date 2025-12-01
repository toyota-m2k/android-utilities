package io.github.toyota32k.utils.lifecycle

import androidx.lifecycle.*
import io.github.toyota32k.utils.FlowableEvent
import io.github.toyota32k.utils.UtLib.logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * MutableStateFlow を MutableLiveData として利用するための変換クラス
 * 通常は、MutableStateFlow.asMutableLiveData() を使って構築する。
 */
class UtMutableStateFlowLiveData<T>(val flow: MutableStateFlow<T>, lifecycleOwner: LifecycleOwner?=null): MutableLiveData<T>(), Observer<T> {
    var prevLiveDataValue:T? = null
    init {
        value = flow.value
        prevLiveDataValue = flow.value
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
                    if (value!==it) {
                        value = it
                    }
                }
            }
        }
    }

    override fun onChanged(value: T) {
        // 値が変化していない場合の無駄打ちを止める
        // 初期化(init->attachToLifecycle)直後の（MutableStateFlow側の）値変更が反映されない現象が発生。
        // LiveData#observe(lifecycleOwner, this) のあと、必ず初回の onChanged が呼ばれるが、
        // これが、flow.collect() による MutableStateFlow の値変更監視開始後に呼ばれるため、
        // flowに対する値変更が、onChangedによって、初期値（＝observe呼び出し時点のLiveData#value）で上書きされてしまう。
        // これを回避するため、liveDataの前回値と比較して、変更がない場合は flow.value への反映を行わないよう修正した。
        if (prevLiveDataValue!=value) {
            prevLiveDataValue = value
            flow.value = value
        }
    }
}

/**
 * MutableStateFlow --> MutableLiveData 変換
 */
fun <T> MutableStateFlow<T>.asMutableLiveData(lifecycleOwner: LifecycleOwner): MutableLiveData<T>
        = UtMutableStateFlowLiveData(this, lifecycleOwner)
