package io.github.toyota32k.utils

import androidx.lifecycle.MutableLiveData

/**
 * 値が変化しないLiveData
 * LiveData は abstract なので直接作成できないので、継承したクラスを用意してみた。
 * 値が変化しないことがわかっているけど、Bindingの仕掛けを使いたいときに、無駄なMutableLiveDataを作るのも気が引けるので。
 * ... もともとは、LiveDataを派生していたけれど、BoolBinding で、BoolConvert.Invert を指定したとき、
 * （変更する/しないにかかわらず）MutableLiveDataを要求し、LiveDataだとキャスト違反になるので、MutableLiveData派生に変更した。
 * そもそも、それなら派生は必要ない、という話なんだが。
 * MutableLiveDataのコンストラクタを見ると、LiveData()を呼んでいるだけなので、コスト的には、LiveDataを作るのと変わらないと思う。
 */
class ConstantLiveData<T>(value:T) : MutableLiveData<T>(value)

fun <T> T.asConstantLiveData() = ConstantLiveData<T>(this)
