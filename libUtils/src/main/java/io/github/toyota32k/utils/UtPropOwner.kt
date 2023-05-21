package io.github.toyota32k.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 外向きには、StatusFlow としてプロパティを公開しつつ、内部的には、MutableStateFlowとして扱うことを実現する、世紀の大発明ｗ
 * - この仕組みを使いたいクラス（ViewModel派生クラスなど）を、IUtPropOwner派生にする。
 * - プロパティは、val prop:StateFlow<T> = MutableStateFlow<T>() のように実装する。
 * - プロパティの値を変更するときは、そのクラス内から、prop.mutable.value に値をセットする。
 *
 * 使用例）
 * class HogeViewModel: ViewModel(), IFlowPropertyHost {
 *    val isBusy:StateFlow<Boolean> = MutableStateFlow(false)
 *
 *    fun setBusy(busy:Boolean) {
 *        isBusy.mutable.value = busy
 *    }
 * }
 *
 * class HogeActivity {
 *     lateinit var viewModel:HogeViewModel
 *
 *     fun doSomething() {
 *        if(viewModel.isBusy.value) return
 *        // viewModel.isBusy.value = true     // error (the property of "isBusy" is immutable.)
 *        viewModel.setBusy(true) {
 *            try {
 *                // do something
 *            } finally {
 *                viewModel.setBusy(false)
 *            }
 *        }
 *     }
 * }
 *
 * ちなみに、mutable プロパティは、単に StateFlowの拡張プロパティなので、グローバルに宣言してしまうことも可能なのだが、
 * さすがにそれは気が引けるので、IUtPropOwner i/f 内に隠蔽し、これを使いたいクラスで、このi/f を継承するルールにしてみた。
 * 当然、IUtPropOwnerを継承するクラスからは、(Mutableでない) StateFlow にも mutable プロパティが生えてしまい、
 * アクセスすると、IllegalCast違反で死ぬだろう（あえて型チェックなんかせず、死ぬようにしている）が、そのあたりは、プログラマの責任で。
 */
interface IUtPropOwner {
    val <T> Flow<T>.mutable: MutableStateFlow<T>
        get() = this as MutableStateFlow<T>
    val <T> LiveData<T>.mutable: MutableLiveData<T>
        get() = this as MutableLiveData<T>
}

/**
 * IUtPropOwner と同じコンセプトの作品
 * こちらは、MutableStateFlowをFlowProp内に隠蔽して、StateFlowに見せかける、というアプローチ。
 * こちらの場合は、常に、mutable プロパティが見えてしまうので、IUtPropOwner よりは安全に、よそさまのStateを変更できてしまうが、
 * 値を変更するとき、明示的に .mutable を介することで、プログラマの意識をちょっとだけ高める程度の効果はあるんじゃないかと。
 * 当然のことながら、mutable の定義が衝突するので、IUtPropOwner と混ぜるのはNG。
 */
class FlowProp<T>(val mutable:MutableStateFlow<T>) : StateFlow<T> by mutable {
    constructor(initialValue:T) : this (MutableStateFlow(initialValue))
}

