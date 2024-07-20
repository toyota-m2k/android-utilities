# android-utilities

さまざまなアプリで利用できる便利な機能を実装したライブラリです。
Android 5 くらいの時代から少しずつ作っていたものなので、SDK がサポートして不要になったり、時代遅れになったAPIもあります。
そのうち整理整頓したい。。。

## Install

```gradle
implementation("com.github.toyota-m2k:android-utility:Tag")
```
https://jitpack.io/#toyota-m2k/android-utilities

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
Activityなどのライフサイクルに依存しない ViewModel を作成する場合に使用する。
ただし、安易に使うと、リソースリークの原因になるので要注意。

## Callback

コールバック関数を１つだけ保持できるハンドラコンテナ。
ライフサイクルオーナーが破棄され（DESTROYED）るときに、自動的にハンドラの登録が解除される。 

## Chronos

時間計測クラス。関数の呼び出しからリターンするまでの時間を計測したり、個々の関数呼び出しの前後でラップ時間を記録したりできる。
計測結果は [UtLog](#utlog) に出力する。ラップタイムを記録するような場合は、Chronos クラスを単体で使うが、１つのブロックの時間を計るだけなら、UtLog#chronos() を使うのが簡単。

#### 使用例
```Kotlin
val logger = UtLog("Sample")
fun takePicture(): Bitmap? {
  logger.chronos {
    return try {
        imageCapture.take()
    } catch (e: Throwable) {
        TcLib.logger.error(e)
        null
    }
}
```

## CollectionExt

コレクション（Map, List）用の拡張関数。
kotlinが標準でサポートするようになったので、もう使わない。　

## ConstantLiveData

値が変化しないLiveDataクラス。
LiveDataを要求するAPI ([android-binder](https://github.com/toyota-m2k/android-binding) など)を統一的に扱えるようにする。

## ConvertLiveData

文字列 - 数値、などの双方向の型変換をサポートするLiveData型。
bool値をView.visibility に変換して表示を切り替えるような単方向の変換であれば、map などのフィルターオペレータで十分だが、EditTextのtextプロパティと数値をバインドするような双方向変換で必要となる。

## DisposableObserver

LiveDataのデータ変化を監視可能なオブザーバークラス。LiveDataの拡張関数、disposableObserve()で生成する。
LiveData の observe() は、Observerクラスを作らないといけないし、removeObserverするには、LiveDataとObserveの両方のインスタンスが必要で、ちょっと使いにくい。
DisposableObserverは、IDisposable（≒Closeable）を継承しているので、dispose()を呼び出すことで、オブザーバーの登録解除が可能。
LifecycleOwner（Activityなど）のライフサイクルに従って動作するので、次の例では、onDestroyで disposable.dispose() を呼ぶ必要はない。
ただし、LiveData#disposableObserveForever() で作成したIDisposableは、onDestroyなどで解放しなければならない。

```kotlin

private lateinit var liveData:LiveData<Boolean>
private var disposable:IDisposable? = null

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    disposable = liveData.diposableObserve(this/*LifecycleOwner*/) {
        if(it) {
            // removeObserverするには、disposeを呼ぶだけで ok
            disposable?.dispose()
            disposable = null
        }
    }
}

```


## DisposableFlowObserver

DisposableObserverのFlow版。
Flowのデータ変化を監視可能なオブザーバークラス。Flowの拡張関数、disposeObserve() で生成する。
IDisposable（≒Closeable）を継承しており、dispose()を呼び出すことで、オブザーバーの登録解除が可能。
LifecycleOwner（Activityなど）のライフサイクルまたは、CoroutineScope内で動作するので、次の例では、onDestroyで disposable.dispose() を呼ぶ必要はない。

```kotlin

private lateinit var flow:Flow<Boolean>
private var disposable:IDisposable? = null

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    disposable = flow.diposableObserve(this/*LifecycleOwner*/) {
        if(it) {
          // removeObserverするには、disposeを呼ぶだけで ok
          disposable?.dispose()
          disposable = null
        }
    }
}

```

## Disposer

IDisposableのコレクション。
Disposerインスタンスを作って、IDisposableを登録(register または、`+` operator)しておけば、
Disposer#dispose() メソッドで、まとめてdisposeできる。

```kotlin

private lateinit var dataA:LiveData<Boolean>
private lateinit var dataB:LiveData<Int>
private lateinit var dataC:LiveData<String>

private val disposer = Disposer()

override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  setContentView(R.layout.activity_main)
  disposer
    .register(dataA.disposableObserveForever { updateA(it) })
    .register(dataB.disposableObserveForever { updateB(it) })
    .register(dataC.disposableObserveForever { updateC(it) })
}
override fun onDestroy() {
    disposer.dispose()
}
```


## FlowableEvent

.NETの ManualResetEvent/AutoResetEvent に相当する、イベントの発生を待ち合わせるための同期オブジェクト。
内部的に Flowを使っており、waitOne()を呼び出すと、外部から、set() が呼ばれるまでサスペンドする。
Flow登場以前には、Channelベースの SuspendableEvent を使っていたが、現在は、FlowableEventの使用を推奨。

## FlowExt

Flowの拡張関数。
- fun MutableStateFlow&lt;Boolean>.toggle()<br>`viewModel.someBooleanProperty.value = !viewModel.someBooleanProperty.value` と書く代わりに、`viewModel.someBooleanProperty.toggle()` と書ける！ ... 作ったことさえ忘れてしまっていたほどのトリビアル。

## GenericCloseable

後始末が必要な処理を Closeable i/f にラップするクラス。
try/finally の代わりに、use() が使えるので後始末の漏れを防止できる。

- fun IDisposable.asCloseable() : Closeable <br>
IDisposable --> Closeable変換用拡張関数

## GenericDisposable

GenericCloseableのIDisposable版。
後始末が必要な処理を IDisposable i/f にラップするクラス。

- fun Closeable.asDisposable() : IDisposable<br>
Closeable-->IDisposable変換用拡張関数

## IDisposable

.NET方面からの移住者にはおなじみのi/f。Java方面では、CloseableとかAutoCloseableとかが一般的だが、FlowやLiveDataは、Rx的な流儀で書くほうがしっくりくる（個人の感想です）。
ただ、rxjava は導入したくないので、独自に定義した。
asCloseable() / asDisposable() 拡張関数で、Closeable i/f  と相互変換可能。

## LifecycleDisposer

ライフサイクルオーナーが破棄されるときに、自動的に dispose()を呼び出す、[Disposer](#disposer)派生クラス。

## LifecycleOwnerHolder

Activityなどライフサイクルによって破棄・再構築されるオブジェクトを、staticな変数や、アプリケーションスコープの変数として持たせると、リソースが解放されずリークしてしまう。
Android OS的には、このようなオブジェクトの参照は好ましくないとされるが、どうしても必要なケースには、このクラスを利用する。保持するライフサイクルオーナーが破棄されるときに、参照を手放すのでリークを回避できる。

## LifecycleReference

ライフサイクルオーナーの生存期間だけ参照可能なオブジェクトを保持するクラス。
WeakReference的な手法で利用する。

## Listeners

複数のコールバック関数を保持できるハンドラコンテナ。
ライフサイクルオーナーが破棄されるとき（DESTROYEDになったとき）に、自動的にハンドラの登録が解除される。


## ListSorter

※UtSorter と全く同じ内容。。。どういう経緯でこうなったか不明。ToDo: そのうち整理する。

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
名前をキーにグローバルに生成・参照できるMutex。

## ObservableFlow

Flowに (LiveDataっぽい) observe メソッドを付与するクラス。
LiveData#observeForever に相当するメソッドはないが、coroutineContext に Dispatchers.IO などを渡せば、forever的に使えるはず。 
Observerの登録解除は、observe()の戻り値に対する dispose() でよいが、disposerにゴミが残るので、頻繁に解除するなら、clean()も呼んだ方がよいと思う。 
removeObserver()を使えば、ゴミが残らず衛生的。

ところで、Flow#disposableObserve()を直接呼ぶのと何が違うのか？
[android-dialog](https://github.com/toyota-m2k/android-dialog)で、サブタスクの状態を流すflowに、ObservableFlowを使っている。
disposableObserve() は、observeした側が責任をもってdisposeすることを期待しているのに対し、ObservableFlow は、observeされた側から一括強制解除できる点が異なる。
タスク管理での利用シーンでは、タスク外からadhocにobserveしてきた（それぞれがちゃんとdisposeしてくれる保証がない）リスナーたちを、タスクが終了するときに確実に解除したいので、これを利用している。

## PackageUtil

パッケージを扱う機能のユーティリティ。
現在は、パッケージからバージョン番号を取得する機能だけ実装。

## SharedPreferenceDelegate

Android の SharedPreference の読み書きに、プロパティ委譲を利用できるようにするための、ReadWriteProperty i/f の実装を提供するクラス。

```kotlin
object Settings {
    private lateinit var spd :SharedPreferenceDelegate

    fun initialize(application: Application) {
        if(this::spd.isInitialized) return
        spd = SharedPreferenceDelegate(application)
    }
    
    var userName by spd.pref("")
    var deviceName by spd.pref(Build.MODEL)
    var activated by spd.pref(false)
}
```

## SingleLiveEvent

[LiveDataを生でイベントとして使うのはよくない](
https://medium.com/androiddevelopers/lvedata-with-snackbar-navigation-and-other-events-the-singleliveevent-case-ac2622673150)
で紹介されている SingleLiveEvent を参考に使いやすく再構成。
ただし、利用実績はほとんどない。現在は、Listener / Callback クラスか、
[android-binder](https://github.com/toyota-m2k/android-binding) の Command系クラスを使っている。

## StateFlowConnector

フローの出力を、他のMutableStateFlow の入力に接続する。
通常はFlowを直接参照して、Rxオペレーターで操作すればよいのだが、２つの独立したViewModelで、
それぞれ別々にMutableStateFlowがインスタンス化される場合に、片方の変化を、もう片方にコピーする、という用途で使用する。

次の例では、ModelB#outputをModelA#inputに接続している。
ModelAを、コンストラクタなどで、外から input:StateFlow&lt;Int> を渡すように修正できれば問題ないのだが、それぞれが別のライブラリで定義されていたりして、修正できない事案が発生したとき、こんな無理やりなクラスを作ったんだった。

```kotlin
    class ModelA {
        val input = MutableStateFlow<Int>(0)
    }
    class ModelB {
        val output = MutableStateFlow<Int>(0)
    }
    val modelA = ModelA()
    val modelB = ModelB()

    fun foo() {
      modelB.output.connectTo(modelA.input).asCloseable().use {
        for(i in 1..100) {
          modelB.output.value = i
          delay(1000)
        }
      }
    }
```

## SuspendableEvent

Kotlin Channel を使ったイベントクラス。
今後は、Flowベースの FlowableEvent を使う。

## TimeKeeper

定期的な処理実行を汎用化したクラス。
動画プレーヤーで、「再生を開始して、３秒間何も操作されなければ、コントロールパネルを非表示にする」という処理を書きたくて作ったクラス。
単純な遅延実行やリピート実行などに適用可能。

```kotlin
val timeKeeper = TimeKeeper(lifecycleScope, "sample")
var count = 0
// 1秒に１回のペースで、doSomething()を100回呼び出します。
timeKeeper.start(1000, pause=false, repeat=true) {
    doSomething()
    count++
    if (count == 100)
        timeKeeper.stop()
    }
}
```
## TimeSpan

ミリ秒で与えられた時間を、時・分・秒に分解、書式化するクラス。
.NETのTimeSpanと名前は同じだが、こちらは、かなり低機能で、まったく別物。

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

## UtMutableStateFlowLiveData

MutableStateFlow を MutableLiveData として利用するための変換クラス

- fun &lt;T> MutableStateFlow&lt;T>.asMutableLiveData(lifecycleOwner: LifecycleOwner): MutableLiveData&lt;T>


## UtObservableCounter

Flowによる参照数の監視が可能な参照カウンタクラス


## UtObservableFlag

Flow による監視可能なフラグクラス。
内部的にフラグ状態はカウンタとして保持しており、ネストした呼び出しが可能。
trySetIfNot(), withFlagIfNot()を使うことで、普通のフラグ的にも使える。

## UtPropOwner

ViewModelにおいて、監視可能なプロパティの実装にMutableStateFlowを使うが、Viewからは値を参照（画面に表示）するだけで、値の変更はViewModel内の処理でのみ行う、というケースも多い。
このような場合、MutableStateFlowを public に晒すのは気持ち悪くないですか？

そこで、外向きには、StateFlow としてプロパティを公開しつつ、内部的には、MutableStateFlowとして扱うことを実現する。
- この仕組みを使いたいクラス（ViewModel派生クラスなど）を、IUtPropOwner派生にする。
- プロパティは、val prop:StateFlow&lt;T> = MutableStateFlow&lt;T>() のように実装する。
- プロパティの値を変更するときは、そのクラス内から、prop.mutable.value に値をセットする。

```kotlin
class Some : IUtPropOwner {
    // 外部から、someProperty は、リードオンリーな StateFlow<String> に見える
    public val someProperty:StateFlow<String> = MutableStateFlow("initial")
  
    fun update(msg: String) {
        // 内部からは、someProperty.mutable で、MutableStateFlowとして値を変更できる
        someProperty.mutable.value = "${Date()} $msg"
    }
  
}
```

ちなみに、mutable プロパティは、単に StateFlowの拡張プロパティなので、グローバルに宣言してしまうことも可能なのだが、
さすがにそれは気が引けるので、IUtPropOwner i/f 内に隠蔽し、これを使いたいクラスで、このi/f を継承するルールにしてみた。
当然、IUtPropOwnerを継承するクラスからは、(Mutableでない) StateFlow にも mutable プロパティが生えてしまい、
アクセスすると、IllegalCast違反で死ぬだろう（あえて型チェックなんかせず、死ぬようにしている）が、そのあたりは、プログラマの責任で。


## UtResetableValue

```
lateinit var some:SomeClass
```
とすれば、someは、NonNullとしてチェックなしに使えて便利なのだが、一旦、値をセットすると未初期化状態に戻せない（と思う）。
だから open --> close --> open のように破棄後に再利用されるクラスを作る場合など、無効状態を表すために、やむを得ず nullableにすることがある。
しかし、値の有効期間を別の方法で正しく管理しているなら、いちいち null チェックするのが煩わしい。そこで、
lateinit的に（nullチェックなしに）使え、かつ、未初期化状態にリセット可能なクラスを作ってみた。

- interface IUtResetableValue&lt;T><br>リセット可能なlateinit的クラスのi/f定義
- class UtResetableValue&lt;T><br>最も基本的なクラス。単純にset/get/resetする。
- class UtLazyResetableValue&lt;T>(val fn:()->T)<br>値が要求されたときに初期化するlazy的動作の本命クラス。
- class UtResetableFlowValue&lt;T>(private val flow: MutableStateFlow&lt;T?><br>値を保持するためにMutableStateFlowを使い、Flow&lt;T?> として扱える値クラス。Flow i/f 経由で値を監視するときは、resetされた状態をnullで表すため、Flow&lt;T?> (nullable)となるのは致し方なし。
- class UtNullableResetableValue&lt;T>(private val allowKeepNull:Boolean=false, private val lazy:(()->T?)?=null) <br>nullチェックなしで使え、かつ再初期化可能な値を保持する、というIUtResetableValueの本来の目的を完全に見失い、nullも保持できるようにしてしまったクラス。なぜ作ったか忘れてしまったが、無駄なインスタンス化を避け、lazy的に必要なときにだけインスタンスを作りたい、という場合に使ったんじゃないかと思う。
- class UtManualIncarnateResetableValue&lt;T>(private val onIncarnate:()->T, private val onReset:((T)->Unit)?)<br>UtLazyResetableValueの亜種だが、一旦リセットされると、incarnate() を呼ばない限り、valueを参照で、勝手に蘇生しない、こじらせ度maxなクラス。何に使ったんだっけ？？？

## UtSortedList

常にソートされた状態を維持するリストクラス。

## UtSorter

MutableList を内包し、ソートされた状態を維持して、add (insert) できるようにする。
特に、[ObservableList](https://github.com/toyota-m2k/android-binding/blob/main/libBinder/src/main/java/io/github/toyota32k/binder/list/ObservableList.kt) をソートした状態で使用するとき、
アイテム挿入で全リスト作り直しになって表示の全更新が発生するのを回避できる。

## ViewExt

Viewのサイズやマージン操作、サイズ計算(dp/px変換など）を拡張関数として定義。

- Context
  - fun Context.activity(): Activity?<br>Context が所属する Activity を取得する。
  - fun Context.lifecycleOwner() : LifecycleOwner?<br>Context が所属する LifecycleOwner を取得する。
  - fun Context.viewModelStorageOwner(): ViewModelStoreOwner?<br>Context が所属する ViewModelStoreOwner を取得する。
  - fun Context.dpToPx(dp:Float): Int<br>dp を px に変換する。
  - fun Context.pxToDp(px:Float): Int<br>px を dp に変換する。
- View
  - fun View.activity(): Activity?<br>View が所属する Activity を取得する。
  - fun View.lifecycleOwner() : LifecycleOwner?<br>View が所属する LifecycleOwner を取得する。
  - fun View.viewModelStorageOwner(): ViewModelStoreOwner?<br>View が所属する ViewModelStoreOwner を取得する。
  - fun View.setLayoutWidth(width:Int)<br>View の LayoutParams の width を設定する。
  - fun View.getLayoutWidth() : Int<br>View の LayoutParams の width を取得する。layoutParams が取得できなければ、width プロパティを返す。
  - fun View.setLayoutHeight(height:Int)<br>View の LayoutParams の height を設定する。
  - fun View.getLayoutHeight() : Int<br>View の LayoutParams の height を取得する。layoutParams が取得できなければ、height プロパティを返す。
  - fun View.setLayoutSize(width:Int, height:Int)<br>View の LayoutParams の width, height を設定する。
  - fun View.measureAndGetSize() : Size<br>View のサイズを計測して返す。
  - fun View.setMargin(left:Int, top:Int, right:Int, bottom:Int)<br>View の LayoutParams の margin を設定する。
- ListView
  - fun ListView.calcContentHeight():Int<br>ListView のコンテントの高さ（各アイテムの高さの合計）を計算する（可変アイテムサイズ用） 
  - fun ListView.calcFixedContentHeight():Int<br>ListView のコンテントの高さ（各アイテムの高さの合計）を計算する（固定アイテムサイズ用） 

## WeakReferenceDelegate

WeakReference をプロパティ委譲するためのクラス

#### 使用例
```Kotlin
class CameraManipulator {
  private var camera:Camera? by WeakReferenceDelegate()
  fun attachCamera(camera: Camera) {
    this.camera = camera    // create a WeakReference to `camera` instance.
  }
  fun zoom() {
    this.camera?.zoom()     // use the WeakReference as a nullable field.
  }
}
```

## gesture

タッチイベントの処理、ジェスチャー操作に関するユーティリティクラス群

### UtGestureInterpreter
Android の低レベルで複雑なタッチイベントを総合的に評価して、
- タップ   --> tapListener
- ロングタップ --> longTapListener
- ダブルタップ --> doubleTapListener
- スクロール --> scrollListener
- 縦フリック --> flickVerticalListener
- 横フリック --> flickHorizontalListener
- ピンチ --> scaleListener
のイベントに振り分ける。利用者は、リスナーにハンドラを登録することで、各イベントを受け取って処理できる。
```kotlin
class MainActivity : AppCompatActivity() {
    // UtGestureInterpreter を作成
    // この例では、
    // - スケールイベントは処理しない
    // - ダブルタップは処理しない（-->その分、シングルタップの判定が少し速い）
    private val gestureInterpreter: UtGestureInterpreter by lazy { UtGestureInterpreter(applicationContext, enableScaleEvent = false, rapidTap = true) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val contentView = findViewById<TextView>(R.id.content_view)
        gestureInterpreter.setup(this, contentView) {
            onTap { 
                edit()      // タップイベントで、テキストの編集モードを開始する
            }
            onLongTap { 
                showProperty()    //ロングタップで、プロパティ表示
            }
            onFlickHorizontal { e->
                when (e.direction) {
                    Direction.Start -> previousPage()
                    Direction.End -> nextPage()
                }
            }
        }
    }
}
```

### UtManipulationAgent / IUtManipulationTarget
UtManipulationAgentは、UtGestureInterpreter のスクロール/スケールイベントを利用して、ビューの拡大、移動、ページ切り替えなどを実現するクラス。
IUtManipulationTargetは、UtManipulationAgent が操作するビューの情報を提供するためのインターフェース。

```kotlin
class MainActivity : AppCompatActivity(), IUtManipulationTarget {
    // UtGestureInterpreter を作成
    // この例では、
    // - ダブルタップは処理しない（-->その分、シングルタップの判定が少し速い）
    // - UtManipulationAgentで使うので、scaleイベントも扱う
    private val gestureInterpreter: UtGestureInterpreter by lazy { UtGestureInterpreter(applicationContext, enableScaleEvent = true, rapidTap = true) }
    // UtManipulationAgent を作成
    // この例では、MainActivity自体がIUtManipulationTargetを実装しているので、thisを引数に渡して構築。
    private val manipulationAgent: UtManipulationAgent by lazy { UtManipulationAgent(this/*IUtManipulationTargetインスタンス*/) }

    // region IUtManipulationTarget i/f
    override val parentView: View get() = findViewById<FrameLayout>(R.id.parent_view)
    override val contentView: View get() = findViewById<TextView>(R.id.content_view)
    // 横幅の40%オーバースクロールしたら、changePage
    override val overScrollX: Float = 0.4f
    // 縦方向はオーバースクロールしない
    override val overScrollY: Float = 0f
    // 横方向オーバースクロールでページ切り替え
    override val pageOrientation: EnumSet<Orientation> = EnumSet.of(Orientation.Horizontal)
    override fun changePage(orientation: Orientation, dir: Direction): Boolean {
      // ページ切り替えの実行
      // return true  移動した --> 続くページ切り替えアニメーションを実行。scale/translation を元に戻す
      //        false 移動しなかった --> びよーんと戻す
      return true
    }
  
    override fun hasNextPage(orientation: Orientation, dir: Direction): Boolean {
      // 次/前のページはあるか？
      return true
    }
    // endregion
  
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val containerView = findViewById<FrameLayout>(R.id.parent_view)
        val targetView = findViewById<TextView>(R.id.content_view)
        gestureInterpreter.setup(this, containerView) {
            onTap {
                edit()      // タップイベントで、テキストの編集モードを開始する
            }
            onLongTap {
                showProperty()    //ロングタップで、プロパティ表示
            }
            // onScroll / onScale を manipulationAgentに接続
            onScroll(manipulationAgent::onScroll)
            onScale(manipulationAgent::onScale)
        }
    }
}
```
上の例では、説明のため MainActivity が IUtManipulationTarget を実装したが、UtSimpleManipulationTarget を使うと、次のように実装を簡素化できる。
サンプルプログラム app の、MainActivityでは、UtSimpleManipulationTargetを使って簡単なページングを実装している。
ただし、アプリの構成により、ズーム操作の対象となるビューが動的に変わる場合、
例えば、画像・ビデオ・テキストなどのコンテンツによって、contentViewが切り替わる場合は、UtSimpleManipulationTargetは使えないので、その場合は、IUtManipulationTargetを実装する必要がある。

```kotlin
class MainActivity : AppCompatActivity() {
    // UtGestureInterpreter を作成
    // この例では、
    // - ダブルタップは処理しない（-->その分、シングルタップの判定が少し速い）
    // - UtManipulationAgentで使うので、scaleイベントも扱う
    private val gestureInterpreter: UtGestureInterpreter by lazy { UtGestureInterpreter(applicationContext,enableScaleEvent = true,rapidTap = true) }

    // UtSimpleManipulationTarget を使って、IUtManipulationTargetを実装
    private val manipulationTarget: IUtManipulationTarget by lazy {
        UtSimpleManipulationTarget(findViewById<FrameLayout>(R.id.parent_view),findViewById<TextView>(R.id.content_view),0.4f,0f,EnumSet.of(Orientation.Horizontal))
            .callbacks {
                changePage { orientation, dir ->
                    // change page
                    true
                }
                hasNextPage { orientation, dir ->
                    true
                }
            }
    }

    // UtManipulationAgent を作成
    // UtSimpleManipulationTargetで作ったIUtManipulationTargetインスタンスを使ってUtManipulationAgentを構築
    private val manipulationAgent: UtManipulationAgent by lazy { UtManipulationAgent(manipulationTarget) }

    override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      setContentView(R.layout.activity_main)
  
      val containerView = findViewById<FrameLayout>(R.id.parent_view)
      val targetView = findViewById<TextView>(R.id.content_view)
      gestureInterpreter.setup(this, containerView) {
        onTap {
          edit()      // タップイベントで、テキストの編集モードを開始する
        }
        onLongTap {
          showProperty()    //ロングタップで、プロパティ表示
        }
        // onScroll / onScale を manipulationAgentに接続
        onScroll(manipulationAgent::onScroll)
        onScale(manipulationAgent::onScale)
      }
    }
}
```

### UtClickRepeater

ボタンを押し続けたときに、Clickイベントを連続して発行できるようにするクラス。
view.setOnTouchListener を使うので、これを使う仕掛け（UtGestureInterpreterなど）とは共存できない。

```kotlin
val clickRepeater = UtClickRepeater()
clickRepeater.attachView(findById(R.id.button))
  ...
clickRepeater.dispose()
```