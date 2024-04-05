# android-utilities

さまざまなアプリで利用できる便利な機能を実装したライブラリです。

## ActivityExt

ステータスバー、アクションバーの表示・非表示を切り替えるための拡張関数を提供します。

- fun FragmentActivity.hideStatusBar()
- fun FragmentActivity.showStatusBar()
- fun FragmentActivity.showStatusBar(flag:Boolean)

- fun AppCompatActivity.hideActionBar()
- fun AppCompatActivity.showActionBar()
- fun AppCompatActivity.showActionBar(flag:Boolean)

## ApplicationViewModelStoreOwner

アプリケーションスコープでViewModelの生存を保証する ViewModelStore の実装クラス。　

## Callback

コールバック関数を１つだけ保持できるハンドラコンテナ。
ライフサイクルオーナーが破棄され（DESTROYED）るときに、自動的にハンドラの登録が解除される。 

## Chronos

時間計測クラス。関数の呼び出しからリターンするまでの時間を計測したり、個々の関数呼び出しの前後でラップ時間を記録したりできる。
計測結果は UtLog に出力する。

## CollectionExt

コレクション（Map, List）用の拡張関数。
kotlinが標準でサポートするようになってきたので、もう使わない。　

## ConstantLiveData

値が変化しないLiveDataクラス。
LiveDataを要求するAPI ([android-binder](https://github.com/toyota-m2k/android-binding) など)を統一的に扱えるようにする。

## ConvertLiveData

文字列 - 数値、などの双方向の型変換をサポートするLiveData型。
bool値をView.visibility に変換して表示を切り替えるような単方向の変換であれば、map などのフィルターオペレータで十分だが、EditTextのtextプロパティと数値をバインドするような双方向変換で必要となる。

## DisposableFlowObserver

DisposableObserverのFlow版。
Flowのデータ変化を監視可能なオブザーバークラス。
IDisposable（≒Closeable）を継承しており、dispose()を呼び出すことで、オブザーバーの登録解除が可能。

## DisposableObserver

DisposableFlowObserverの LiveData版。
LiveDataのデータ変化を監視可能なオブザーバークラス。
IDisposable（≒Closeable）を継承しており、dispose()を呼び出すことで、オブザーバーの登録解除が可能。

## Disposer

IDisposableのコレクション。
Disposerインスタンスを作って、IDisposableを登録(register または、`+` operator)しておけば、
Disposer#dispose() メソッドで、まとめてdisposeできる。

## FlowableEvent

.NETの ManualResetEvent/AutoResetEvent に相当する、イベントの発生を待ち合わせるための同期オブジェクト。
内部的に Flowを使っており、waitOne()を呼び出すと、外部から、set() が呼ばれるまでサスペンドする。
Flow登場以前には、Channelベースの SuspendableEvent を使っていたが、今後は、FlowableEventの使用を推奨。

## FlowExt

Flowの拡張関数。
- fun MutableStateFlow&lt;Boolean>.toggle()<br>`viewModel.someBooleanProperty.value = !viewModel.someBooleanProperty.value` と書く代わりに、`viewModel.someBooleanProperty.toggle()` と書ける！ ... 作ったことさえ忘れてしまっていたほどのトリビアル。

## GenericCloseable

後始末が必要な処理を Closeable i/f にラップするクラス。
try/finally の代わりに、use() が使えるので後始末の漏れを防止できる。

- fun IDisposable.asCloseable() : Closeable <br>
IDisposable --> Closeable変換用拡張関数

GenericDisposable.kt

GenericCloseableのIDisposable版。
後始末が必要な処理を IDisposable i/f にラップするクラス。

- fun Closeable.asDisposable() : IDisposable<br>
Closeable-->IDisposable変換用拡張関数

## IDisposable

.NET方面からの移住者にはおなじみのi/f。Java方面では、CloseableとかAutoCloseableとかが一般的だが、FlowやLiveDataは、Rx的な流儀で書くほうがしっくりくる（個人の感想です）。
ただ、rxjava は導入したくないので、独自に定義した。


## LifecycleDisposer

ライフサイクルオーナーが破棄されるときに、自動的に dispose()を呼び出す、Disposer派生クラス。

## LifecycleOwnerHolder

Activityなどライフサイクルによって破棄・再構築されるオブジェクトを、staticな変数や、アプリケーションスコープの変数として持たせると、リソースが解放されずリークしてしまう。
Android OS的には、このようなオブジェクトの参照は好ましくないとされるが、どうしても必要なケースには、このクラスを利用する。保持するライフサイクルオーナーが破棄されるときに、参照を手放すのでリークを回避できる。

## LifecycleReference

ライフサイクルオーナーの生存期間だけ参照可能なオブジェクトを保持するクラス。
WeakReference的な手法で利用する。

## Listeners

複数のコールバック関数を保持できるハンドラコンテナ。
ライフサイクルオーナーが破棄され（DESTROYED）るときに、自動的にハンドラの登録が解除される。


## ListSorter

MutableList を内包し、ソートされた状態を維持して、add (insert) できるようにする。
特に、[ObservableList](https://github.com/toyota-m2k/android-binding/blob/main/libBinder/src/main/java/io/github/toyota32k/binder/list/ObservableList.kt) をソートした状態で使用するとき、
アイテム挿入で全リスト作り直しになって表示の全更新が発生するのを回避できる。

## LiveDataExt

LiveData に、Rx的なオペレータを追加する拡張関数です。

- fun &lt;T,R> LiveData&lt;T>.mapEx(fn:(T?)->R): LiveData&lt;R> <br>
  LiveData.map だと、初期値が反映されないので、初期値の反映が必要なときはこちらを使う。
- fun &lt;T> LiveData&lt;T>.filter(predicate:(T?)->Boolean): LiveData&lt;T>
- fun &lt;T> LiveData&lt;T>.notNull(): LiveData&lt;T>
- fun &lt;T1,T2,R> combineLatest(src1:LiveData&lt;T1>,src2: LiveData&lt;T2>, fn:(T1?,T2?)->R?):LiveData&lt;R>
- fun or(vararg args:LiveData&lt;Boolean>):LiveData&lt;Boolean>
- fun and(vararg args:LiveData&lt;Boolean>):LiveData&lt;Boolean>
- fun LiveData&lt;Boolean>.not():LiveData&lt;Boolean>

## Logger

UtLogで使用する、低レベルログAPI (IUtVaLogger）の基本実装
デフォルトで、LogCatにログを出力する。ファイルに保存する、など、追加の動作が必要なら、UtLoggerInstance.externalLogger に、
IUtExternalLoggerを実装したオブジェクトをセットする。

## MuSize

Mutableな Sizeクラス。
インスタンス生成後、width, height を変更できる。

## NamedMutex

名前付きMutexクラス。
名前をキーにグローバルにMutexを生成・参照できる。

## ObservableFlow

Flowに (LiveDataっぽい) observe メソッドを付与するクラス。
LiveData#observeForever に相当するメソッドはないが、coroutineContext に Dispatchers.IO などを渡せば、forever的に使えるはず。 
Observerの登録解除は、observe()の戻り値に対する dispose() でよいが、disposerにゴミが残るので、頻繁に解除するなら、clean()も呼んだ方がよいと思う。 
removeObserver()を使えば、ゴミが残らず衛生的。

## PackageUtil

パッケージを扱う機能のユーティリティ。
現在は、パッケージからバージョン番号を取得する機能だけ実装。

## SharedPreferenceDelegate

Android の SharedPreference の読み書きに、プロパティ委譲を利用できるようにするための、ReadWriteProperty i/f の実装を提供するクラス。

## SingleLiveEvent

[LiveDataを生でイベントとして使うのはよくない](
https://medium.com/androiddevelopers/lvedata-with-snackbar-navigation-and-other-events-the-singleliveevent-case-ac2622673150)
で紹介されている SingleLiveEvent を参考に使いやすく再構成。
ただし、利用実績はほとんどない。現在は、Listener / Callback クラスを使うか、
[android-binder](https://github.com/toyota-m2k/android-binding) の Commandクラスを使っている。

## StateFlowConnector

フローの出力を、他のMutableStateFlow の入力に接続する。
通常はFlowを直接参照して、Rxオペレーターで操作すればよいのだが、２つの独立したViewModelで、
それぞれ別々にMutableStateFlowがインスタンス化される場合に、片方の変化を、もう片方にコピーする、という用途で使用する。

## SuspendableEvent

Kotlin Channel を使ったイベントクラス。
今後は、Flowベースの FlowableEvent を使う。

## TimeKeeper

定期的な処理実行を汎用化したクラス。

## TimeSpan

ミリ秒で与えられた時間を、時・分・秒に分解、書式化するクラス。

## TintDrawable

Android のアイコン(Vector PathベースのDrawable)の色（Tint）をプログラム的に変更するヘルパークラス。

## UtAwaitable

awaitすると、complete が呼ばれるまでサスペンドする。
普通はFlowableEvent で間に合うのだが、kotlinではなく、java から呼び出す必要があったときに作ったような気がする。

## UtFitter

子ビューを、親ビューの領域に合わせて配置するときのサイズを計算するためのクラス。

## UtLib

整理されていない、ニッチな拡張関数とか。

## UtLog

ログ出力用ユーティリティ。
出力する文字列にタグ、クラス名、関数名などを自動的に付加する。

## UtManualIncarnateResetableValue

UtMutableStateFlowLiveData.kt
UtObservableCounter.kt
UtObservableFlag.kt
UtPropOwner.kt

## UtResetableValue



UtSortedList.kt
UtSorter.kt
ViewExt.kt
WeakReferenceDelegate.kt