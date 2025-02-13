# リファレンス

## [ActivityExt](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/ActivityExt.kt)

Activityの状態を設定するための拡張関数を定義しています。

#### ステータスバー表示・非表示を切り替える。

```kotlin
fun FragmentActivity.hideStatusBar()
fun FragmentActivity.showStatusBar()
fun FragmentActivity.showStatusBar(flag:Boolean)
```

#### アクションバーの表示・非表示を切り替える。

```kotlin
fun AppCompatActivity.hideActionBar()
fun AppCompatActivity.showActionBar()
fun AppCompatActivity.showActionBar(flag:Boolean)
```

#### ActivityのOrientationを設定する。

```kotlin
fun FragmentActivity.setOrientation(orientation:ActivityOrientation)
```

#### プリセットした状態をまとめて設定

```kotlin
  val normalOptions = ActivityOptions.actionAndStatusBar(showActionBar=true, showStatusBar=true, orientation=ActivityOrientation.AUTO)
  val editingOptions = ActivityOptions.actionAndStatusBar(showActionBar=false, showStatusBar=false, orientation=ActivityOrientation.LANDSCAPE)

  fun onEditingModeChanged(editing:Boolean) {
    val options = if(editing) editingOptions else normalOptions
    options.apply(this)
  }
```

さらに、[android-binding の activityOptionsBinding](https://github.com/toyota-m2k/android-binding/blob/main/libBinder/src/main/java/io/github/toyota32k/binder/ActivityBinding.kt)を使えば、
ビューモデル(Flow<ActivityOptions>)にバインドしてリアクティブにこれらの状態を更新することができます。

## [ApplicationViewModelStoreOwner](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/ApplicationViewModelStoreOwner.kt)

アプリケーションスコープでViewModelの生存を保証する ViewModelStore の実装クラス。　<br>
Activityなどのライフサイクルに依存しない ViewModel を作成する場合に使用します。当然のことながら、安易に使うとリソースリークの原因になるので要注意。

## [Callback](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/Callback.kt)

コールバック関数を１つだけ保持できるハンドラコンテナ。<br>
ライフサイクルオーナーが破棄される（Lifecycle.State.DESTROYEDになる）ときに、自動的にハンドラの登録が解除されます。 複数のハンドラを登録する場合は、[Listeners](#listeners) クラスを使用してください。

## [Chronos](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/Chronos.kt)

デバッグ用時間計測クラス。<br>
関数の呼び出しからリターンするまでの時間を計測したり、個々の関数呼び出しの前後でラップ時間を記録できます。計測結果は [UtLog](#utlog) に出力します。１つのブロックの時間を計るだけなら、UtLog#chronos() を使うのが便利です。

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

## [ConstantLiveData](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/ConstantLiveData.kt)

値が変化しないLiveDataクラス。<br>
LiveDataを要求するAPI ([android-binding](https://github.com/toyota-m2k/android-binding) など)を統一的な作法で扱うために使用できます。

## [ConvertLiveData](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/ConvertLiveData.kt)

文字列 - 数値、などの双方向の型変換をサポートするLiveData型。<br>
bool値をView.visibility に変換して表示を切り替えるような単方向の変換であれば、map などのフィルターオペレータで十分ですが、EditTextのtextプロパティ(String型)を数値型に双方向変換してバインドするような場合に利用します。

## [DisposableObserver](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/DisposableObserver.kt)

LiveDataのobserve/removeObserverを[IDisposable](#idisposable)i/fとして扱うクラス<br>
LiveDataの拡張関数 `disposableObserve()` または `disposableObserveForever()` で生成します。
LiveData の observe() は、Observerクラスを作る必要があり、removeObserverするには、LiveDataとObserverの両方のインスタンスを保持しておかなければなりませんが、`DisposableObserver` を使うと、dispose()を呼び出すことで、オブザーバーの登録解除が可能となります。
 `disposableObserve()`で生成した `DisposableObserver` は、 LifecycleOwner（Activityなど）のライフサイクルに従って動作するので、次の例では、onDestroyで disposable.dispose() を呼ぶ必要がありません。
`disposableObserveForever()` で作成した `DisposableObserver` は、明示的に `dispose()`を呼んで解放する必要があります。

#### 使用例
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


## [DisposableFlowObserver](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/DisposableFlowObserver.kt)

[DisposableObserver](#disposableobserver)のFlow版。<br>
Flowの拡張関数、`disposeObserve()` または、`disposableObserveForever()` で生成します。
`disposableObserve()`で生成した `DisposableFlowObserver` は、 LifecycleOwner（Activityなど）のライフサイクルまたは、CoroutineScope内で動作するので、次の例では、onDestroyで disposable.dispose() を呼ぶ必要はありません。
`disposableObserveForever()` で生成した `DisposableFlowObserver` は、明示的に `dispose()`を呼んで解放する必要があります。

#### 使用例
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

## [Disposer](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/Disposer.kt)

[IDisposable](#idisposable) のコレクション。
Disposerインスタンスを作って、IDisposable　を登録(register または、`+` operator)しておけば、
Disposer#dispose() メソッドで、まとめてdisposeできます。派生クラスの [LifecycleDisposer](#lifecycledisposer)を使えば、LifecycleOwner (Activityなど）のライフサイクルに合わせて自動的に dispose()を呼び出せます。

#### 使用例
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


## [FlowableEvent](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/FlowableEvent.kt)

イベントの発生を待ち合わせるための同期オブジェクト<br>
.NET の ManualResetEvent/AutoResetEvent に相当するクラスです。内部的に Flowを使っており、waitOne()を呼び出すと、外部から、set() が呼ばれるまでサスペンドします。

#### 使用例
```kotlin
suspend fun example() {
  val event = FlowableEvent()
  var result = false
  coroutineScope(Dispathers.IO).launch {
    ... // do something
    result = true
    event.set()
  }
  event.waitOne()   // event.set()が呼ばれるまで待つ
  assert(result == true)
}
```

## [FlowExt](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/FlowExt.kt)

Flowの拡張関数<br>
```kotlin
fun MutableStateFlow<Boolean>.toggle()
```
`viewModel.someBooleanProperty.value = !viewModel.someBooleanProperty.value` と書く代わりに、`viewModel.someBooleanProperty.toggle()` と書ける！ ... 作ったことさえ忘れてしまっていたほどトリビアルな関数。

## [GenericCloseable](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/GenericCloseable.kt)

後始末が必要な処理を Closeable i/f にラップするクラス。
try/finally の代わりに、use() が使えるので後始末の漏れを防止できます。

IDisposable --> Closeable変換用拡張関数
```kotlin
fun IDisposable.asCloseable() : Closeable
```


## [GenericDisposable](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/GenericDisposable.kt)

GenericCloseableのIDisposable版。
後始末が必要な処理を IDisposable i/f にラップするクラス。<br>
[Disposer](#disposer) や、[LifecycleDisposer](#lifecycledisposer) に登録してまとめて後始末することができます。

#### 使用例
```kotlin
val disposable = GenericDisposable.create {
  // onFocusChangedListenerをセット
  val edit = findViewById(R.id.edit_text)
  edit?.setOnFocusChangeListener { _, focus ->
      viewModel.focus = focus
  }
  ({ edit.onFocusChangeListener = null })
}
...
// 不要になれば dispose()を呼び出してリスナーを解除
disposable.dispose()
```


Closeable-->IDisposable変換用拡張関数
```kotlin
fun Closeable.asDisposable() : IDisposable
```

## [IAwaiter](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/IAwaiter.kt)

キャンセル可能な待ち合わせタスクを抽象化するインターフェース<br>

キャンセル可能な関数の戻り値として使用します。JobやCoroutineScope を直接返すのと比べて、内部実装の自由度を確保できる利点があります。

## [IDisposable](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/IDisposable.kt)

.NET方面からの移住者にはおなじみのインターフェース。<br>
Javaでは、`Closeable` や `AutoCloseable` が一般的ですが、close()よりも、dispose() の方が rxらしい感じがする（個人の感想です）ので、自家製アプリ用に定義しました。
IDisposable#asCloseable() / Closeable#asDisposable() 拡張関数で、Closeable i/f  と相互変換が可能です。

## [LifecycleDisposer](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/LifecycleDisposer.kt)

ライフサイクルオーナーが破棄されるときに、自動的に dispose()を呼び出す、[Disposer](#disposer)派生クラス。

## [LifecycleOwnerHolder](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/LifecycleOwnerHolder.kt)

ライフサイクルオーナーの参照を安全に保持するクラス。<br>
`Activity` など、ライフサイクルによって破棄・再構築されるオブジェクトを、staticな変数や、アプリケーションスコープの変数、あるいは、ViewModel のフィールドとして持たせると、リソースが解放されずリークしてしまうので、そのような実装は避けるべきです。しかし、どうしてもそれが必要なケースには、このクラスを使います。ライフサイクルオーナーが破棄されるときに、参照を手放すのでリークを回避することができます。

## [LifecycleReference](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/LifecycleReference.kt)

ライフサイクルオーナーの生存期間だけ参照可能なオブジェクトを保持するクラス。<br>
WeakReference的な手法で利用します。ライフサイクルオーナーがDestroyされると、参照しているオブジェクト（LifecycleReference#value）は `null` になります。

#### 使用例
```kotlin
class ViewModel {
  var viewRef: LifecycleReference<View>? = null
  fun doSomething {
    val view = viewRef?.value ?: return
    // viewは、MainActivityの onCreateで設定され、onDestroyまで有効。onDestroy以降は nullが返る。
  }
}
class MainActivity {
  lateinit var viewModel:ViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    ...
    viewModel.viewRef = LifecycleReference(findViewById(R.id.view))
  }
}
```

## [Listeners](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/Listeners.kt)


複数のコールバック関数を保持できるハンドラコンテナ。<br>
ライフサイクルオーナーが破棄される（Lifecycle.State.DESTROYEDになる）ときに、自動的にハンドラの登録が解除されます。 ハンドラを１つだけ登録する場合は、[Callback](#callback) クラスの利用を検討してください。

## [ListSorter](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/ListSorter.kt)

deprecated
[UtSorter](#utsorter) を使ってください。

## [LiveDataExt](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/LiveDataExt.kt)

LiveData に、Rx的なオペレータを追加する拡張関数です。

- fun &lt;T,R> LiveData&lt;T>.mapEx(fn:(T?)->R): LiveData&lt;R> <br>
  LiveData.map だと、初期値が反映されないので、初期値の反映が必要なときはこちらを使う。
- fun &lt;T> LiveData&lt;T>.filter(predicate:(T?)->Boolean): LiveData&lt;T>
- fun &lt;T> LiveData&lt;T>.notNull(): LiveData&lt;T>
- fun &lt;T1,T2,R> combineLatest(src1:LiveData&lt;T1>,src2: LiveData&lt;T2>, fn:(T1?,T2?)->R?):LiveData&lt;R>
- fun or(vararg args:LiveData&lt;Boolean>):LiveData&lt;Boolean>
- fun and(vararg args:LiveData&lt;Boolean>):LiveData&lt;Boolean>
- fun LiveData&lt;Boolean>.not():LiveData&lt;Boolean>

## [Logger](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/Logger.kt)

UtLogで使用する、低レベルログAPI (IUtVaLogger）の基本実装。<br>
デフォルトで、LogCatにログを出力します。ファイルに保存する、など、追加の動作が必要なら、UtLoggerInstance.externalLogger に、
IUtExternalLoggerを実装したオブジェクトをセットしてください。

## [MuSize](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/MuSize.kt)

Mutableな Sizeクラス。<br>
インスタンス生成後、width, height を変更できます。`Size` / *`SizeF` との相互変換も可能です。

## [NamedMutex](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/NamedMutex.kt)

名前付きMutexクラス。<br>
名前をキーにグローバルに生成・参照できるMutexクラスを作成することができます。

## [ObservableFlow](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/ObservableFlow.kt)

Flowに (LiveData 的な) observe メソッドを付与するクラス。<br>
LiveData と同様、observe() でオブザーバーを登録し、 / removeObserver() で解除します。
ただし、LiveData の observe() と異なり、ライフサイクルオーナーのライフサイクルではなく、渡す CoroutineContext の中で監視が行われます。

通常は、Flow#disposableObserve()拡張関数を使うのが便利ですが、ObservableFlow は、observeされた側から一括強制解除できる点が異なります。つまり、disposableObserve() は、observeした側が責任をもってdisposeすることを期待しているのに対し、ObservableFlow は、例えば、外部から adhoc に observe され、observeされている側の処理が終わった時に、確実にobserverを解除したい場合に利用します。

## [PackageUtil](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/PackageUtil.kt)

パッケージを扱う機能のユーティリティ。<br>
現在は、パッケージからバージョン番号を取得する機能だけ実装。

## [ProgressWorkerProcessor](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/ProgressWorkerProcessor.kt)

Android 15 以降、アプリがバックグラウンドに回る（他のアプリに切り替える）と、数秒後にネットワーク接続が切断されるようになりました。そのため、ファイルのアップロード/ダウンロード中にユーザーが他のアプリの画面を表示してしまうと、処理が失敗してしまいます。この問題を回避するには、[WorkManager](https://developer.android.com/reference/androidx/work/WorkManager)を使う必要があります。`ProgressWorkerProcessor` は、WorkManager によるバックグラウンド処理に、
- 進捗通知 : progress(current,total)コールバックを使用
- バックグラウンド処理のキャンセル : [IAwaiter](#iawaiter)を使用

を追加するためのボイラープレートクラスです。

`ProgressWorkerProcessor` を利用する場合は、まず、`ProgressWorker` を派生したクラスを作成し、`doWork()` をオーバーライドします。`doWork()` にはバックグラウンド処理の中身を実装するとともに、適宜 `progressManager#progress()` を呼び出すことで、呼び出し元に進捗を通知します。

`Worker` の起動には、`ProgressWorkerProcessor#process()` を使います。呼び出し元から　Worker には、普通の引数を渡すことができず、[androidx.work.Data](https://developer.android.com/reference/androidx/work/Data)を使ってパラメータ受け渡します。以下は、WorkManagerを使ってダウンロードする例ですが、object クラス `Downloader` に、download()メソッドを用意して、パラメータの引き渡しを含む `ProgressWorkerProcessor#process()` の呼び出しに関わる煩雑な処理を隠蔽しています。

#### 使用例
```kotlin
object Downloader {
    const val KEY_URL = "url"
    const val KEY_FILE_PATH = "path"

    val processor = ProgressWorkerProcessor()

    /**
     * ProgressWorkerを派生する、バックグラウンド処理の実装クラス
     */
    class DownloadWorker(context: Context, params: WorkerParameters) : ProgressWorker(context, params) {
        private suspend fun downloadUrlToFile(url: String, file: File) {
            var connection: HttpURLConnection? = null
            try {
                connection = (URL(url).openConnection() as HttpURLConnection).apply { connect() }
                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    throw Exception("Server returned HTTP ${connection.responseCode} ${connection.responseMessage}")
                }
                val fileLength = connection.contentLength.toLong()
                connection.inputStream.use { inputStream->
                FileOutputStream(file).use { outputStream ->
                    val buffer = ByteArray(4096)
                    var totalBytesRead = 0L
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead
                        progress(totalBytesRead, fileLength)
                        yield() // CoroutineScope のキャンセルを検出する
                    }
                }}
            } finally {
                connection?.disconnect()
            }
        }

        /**
         * バックグラウンド処理
         */
        override suspend fun doWork(): Result {
            return try {
                val url = inputData.getString(KEY_URL) ?: return Result.failure()
                val file = File(inputData.getString(KEY_FILE_PATH) ?: return Result.failure())
                downloadUrlToFile(url, file)
                Result.success()
            } catch (_: CancellationException) {
                // cancelled
                return Result.failure()
            } catch (e: Throwable) {
                // download error
                return Result.failure()
            }
        }
    }

    /**
     * WorkManagerを使ってファイルをダウンロードする
     * @param context
     * @param url   ターゲットURL
     * @param path  保存先ファイル
     * @param progress 進捗報告用コールバック関数
     * @return 待ち合わせ/キャンセル用 IAwaiter 
     */
    fun download(context:Context, url:String, path:String, progress:((current:Long, total:Long)->Unit)?):IAwaiter<Boolean> {
        // DownloadWorker の実行を要求する
        // DownloadWorker が使用するパラメーターは、workDataOb()を使って、androidx.work.Data にセットする。
        return processor.process<DownloadWorker>(context,
            workDataOf(
                KEY_URL to url,
                KEY_FILE_PATH to path,
            ), progress)
    }
}
```

## [SharedPreferenceDelegate](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/SharedPreferenceDelegate.kt)

Android の `SharedPreference` の読み書きに、プロパティ委譲を利用できるようにするための、ReadWriteProperty i/f の実装を提供するクラス。

#### 使用例
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

## [SingleLiveEvent](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/SingleLiveEvent.kt)

[LiveDataを生でイベントとして使うのはよくない](
https://medium.com/androiddevelopers/lvedata-with-snackbar-navigation-and-other-events-the-singleliveevent-case-ac2622673150)
で紹介されている `SingleLiveEvent` を参考に再構成しました。
ただし、利用実績はほとんどありません。通常は Listener / Callback クラスか、
[android-binding](https://github.com/toyota-m2k/android-binding) の Command系クラスを使っています。

## [StateFlowConnector](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/StateFlowConnector.kt)

フローの出力を、他のMutableStateFlow の入力に接続するクラス。<br>
通常はFlowを直接参照して、Rxオペレーターで操作しますが、２つの独立したViewModelで、
それぞれ別々にMutableStateFlowがインスタンス化される場合に、これらを接続する、つまり、片方の変化を、もう片方にコピーするために使用します。

次の例では、`ModelB#output`を`ModelA#input`に接続しています。
ModelAを、コンストラクタなどで、外から input:StateFlow&lt;Int> を渡すように実装を変更するのが正攻法ですが、これらが別のライブラリで定義されている場合など、モデルの構成を変更しないで、それぞれの入出力を接続する場合に使用します。

#### 使用例
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

Kotlin Channel を使ったイベントクラス。(Deprecated)
Flowベースの [FlowableEvent](#flowableevent) を使ってください。

## [TimeKeeper](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/TimeKeeper.kt)

定期的な処理実行を汎用化したクラス。<br>
単純な遅延実行やリピート実行などに利用できますが、例えば「３秒間操作が無ければ(= touch()が呼ばれなければ) パネルを閉じる」というようなタイマーベースの処理を書くことができます。

#### 使用例
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

## [TimeSpan](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/TimeSpan.kt)

ミリ秒で与えられた時間を、時・分・秒に分解、書式化するクラス。<br>
.NETのTimeSpanと同じ名前にしてしまいましたが、まったく別物です。

## [TintDrawable](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/TintDrawable)

Android のアイコン(Vector PathベースのDrawable)の色（Tint）をプログラム的に変更するヘルパークラス。

## [UtAwaitable](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/UtAwaitable.kt)

awaitすると、complete が呼ばれるまでサスペンドするクラス<br>
普通はFlowableEvent を使いますが、kotlinではなく、java から呼び出すために作りました。

## [UtFitter](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/UtFitter.kt)

子ビューを、親ビューの領域に合わせて配置するときのサイズを計算するためのクラス。

与えた `FitMode` に従ってサイズを計算します。
|FitMode|動作|
|----|----|
|Width|アスペクト比を変えず指定された幅になるように高さを調整|
|Height|アスペクト比を変えず指定された高さになるよう幅を調整|
|Inside|アスペクト比を変えず指定された幅・高さの中に収まるよう、幅・高さを調整|
|Fit|指定された幅・高さに変更|


## [UtLog](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/UtLog.kt)

ログ出力用ユーティリティ。<br>
出力する文字列にタグ、クラス名、関数名などを自動的に付加します。

## [UtMutableStateFlowLiveData](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/UtMutableStateFlowLiveData.kt)

MutableStateFlow を MutableLiveData として利用するための変換クラス

- fun &lt;T> MutableStateFlow&lt;T>.asMutableLiveData(lifecycleOwner: LifecycleOwner): MutableLiveData&lt;T>


## [UtObservableCounter](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/UtObservableCounter.kt)

Flowによる参照数の監視が可能な参照カウンタクラス


## [UtObservableFlag](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/UtObservableFlag.kt)

Flow による監視可能なフラグクラス。<br>
trySetIfNot(), withFlagIfNot()を使うことで、単純なBoolean型のフラグとして使えます。
内部的にフラグ状態はカウンタとして保持しいるので、ネストした呼び出しも可能です。

## [UtPropOwner](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/UtPropOwner.kt)


MutableStateFlow型のフィールド を immutable な StateFlow (または Flow) 型のフィールドとして公開するユーティリティ<br>

ViewModelにおいて、監視可能なフィールドの実装にMutableStateFlowを使いますが、外部（View）からは値を参照（画面に表示）するだけで、値の変更はViewModel内の処理でのみ行うケースがよくあります。このようなフィールドは StateFlow （またはFlow）型で外部に公開すべきです。

このように、「外向きには StateFlow としてフィールドを公開しつつ、内部的には MutableStateFlow として扱う」ことを実現するための定義を用意しました。次のようにして利用します。

1. この仕組みを使いたいクラス（ViewModel派生クラスなど）を、`IUtPropOwner`から派生する。
2. プロパティは、`val prop:StateFlow<T> = MutableStateFlow<T>()` のように実装する。
3. プロパティの値を変更するときは、そのクラス内から、`prop.mutable.value` に値をセットする。

#### 使用例
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

尚、`mutable` は、IUtPropOwner i/f 内に隠蔽した Flow の拡張プロパティです。従って、IUtPropOwner を継承するクラスからは、Mutableでない StateFlow に対しても mutable プロパティが使えるようになってしまいますが、当然、これにアクセスすると、IllegalCastException がスローされます。

## [UtResetableValue](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/UtResetableValue.kt)

nullチェックなしに使えるリセット可能な変数を定義するクラス<br>

```
lateinit var some:SomeClass
```
とすれば、someは NonNull型なので null チェックなしに使えて便利ですが、一旦、値をセットすると未初期化状態に戻せません。
例えば、
```kotlin
class SomeFile {
  lateinit var file:File
  fun open(target:File) {
    file = target
  }
  fun doSomething {
    file.doSomething()  // nullチェック不要
  }
  fun close() {
    // fileフィールドを「未設定」状態に戻せない
    // file = null
  }
}
```
このような場合は、fileフィールドを nullableにして未設定状態を表現することになり、それを使用する場合は nullチェックが必要になります。
```kotlin
class SomeFile {
  var file:File? = null
  fun open(target:File) {
    file = target
  }
  fun doSomething {
    file?.doSomething()  // nullチェックが必要
  }
  fun close() {
    // 初期状態に戻す
    file = null
  }
}
```
値の有効・無効を別の方法で正しく管理している場合は、本来必要のない null チェックを毎回行っていることになります。そこで、lateinit的に（nullチェックなしに）使え、かつ、未初期化状態にリセット可能なクラスを作ってみました。

```kotlin
interface IUtResetableValue<T>
```
リセット可能なlateinit的クラスのi/f定義


```kotlin
class UtResetableValue<T>
```
最も基本的なクラス。単純にset/get/resetする。

```kotlin
class UtLazyResetableValue<T>(val fn:()->T)
```
value が要求されたときに初期化するlazy的動作を行うクラス。
value初期化関数をコンストラクタに渡します。
reset()しても、valueが参照されると、自動的にvalueが再初期化されます。

```kotlin
class UtResetableFlowValue<T>(private val flow: MutableStateFlow<T?>
```
UtResetableValue&lt;T>と同等に扱え、且つ、Flow&lt;T?> として値を監視できるクラス。
ただし、Flowとして操作するときは、resetされた状態をnullで表すため、Flow&lt;T?> (nullable)となります。

```kotlin
class UtNullableResetableValue<T>(
  private val allowKeepNull:Boolean=false, 
  private val lazy:(()->T?)?=null)
```
UtLazyResetableValue を nullも保持できるようにしたクラス。allowKeepNull = true とすると、null をセットしても　hasValue == true となります。
nullチェックなしで使え、かつ再初期化可能な値を保持する、というIUtResetableValueの本来の目的から逸脱してしまいました。

```kotlin
class UtManualIncarnateResetableValue<T>(
  private val onIncarnate:()->T, 
  private val onReset:((T)->Unit)?)
```
UtLazyResetableValue とほぼ同じですが、一旦リセットされると、incarnate() を呼ばない限り、valueを参照しても、自動的に再初期化されることはなく、例外（NRE）をスローします。

## [UtSortedList](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/UtSortedList.kt)

常にソートされた状態を維持するリストクラス。MutableList を置き換えることができます。

## [UtSorter](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/UtSorter.kt)

MutableList を内包し、ソートされた状態を維持して、add (insert) できるようにします。
MutableList クラスを置き換えることが可能なら、UtSortedList が使えますが、単純に置き換えられない場合、例えば、[ObservableList](https://github.com/toyota-m2k/android-binding/blob/main/libBinder/src/main/java/io/github/toyota32k/binder/list/ObservableList.kt) をソートした状態で使いたい場合には、この UtSorter を使うことで既存の MutableList 派生クラスにソート機能を追加できます。

#### 使用例
```kotlin
val list = mutableListOf<Int>()
val sorter = UtSorter(list, allowDuplication=false, comparator={ a, b -> a - b })
sorter.add(35)
sorter.add(7)
sorter.add(29)
// list上では、7, 29, 35 の順にソートされている
```

## [ViewExt](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/ViewExt.kt)

Viewのサイズやマージン操作、サイズ計算(dp/px変換など）を拡張関数として定義。

#### Context の拡張関数
```kotlin
fun Context.activity() : Activity?
fun Context.lifecycleOwner(): LifecycleOwner?
fun Context.viewModelStorageOwner(): ViewModelStoreOwner?
```
  それぞれ、Context が所属する Activity, LifecycleOwner, ViewModelStoreOwner を取得します。ApplicationContext の場合は nullを返します。

```kotlin
fun Context.dpToPx(dp:Float): Int
fun Context.pxToDp(px:Float): Int
```
DP と Pixel を相互変換します。

#### View の拡張関数
```kotlin
fun View.activity(): Activity?
fun View.lifecycleOwner() : LifecycleOwner?
fun View.viewModelStorageOwner(): ViewModelStoreOwner?
```
  それぞれ、View が所属する Activity, LifecycleOwner, ViewModelStoreOwner を取得します。
```kotlin
fun View.getLayoutWidth() : Int
fun View.setLayoutWidth(width:Int)
fun View.getLayoutHeight() : Int
fun View.setLayoutHeight(height:Int)
fun View.setLayoutSize(width:Int, height:Int)
```  
Viewの LayoutParams の width/height を取得/設定します。

```kotlin
fun View.setMargin(left:Int, top:Int, right:Int, bottom:Int)
```
View の MarginLayoutParams にマージンを設定します。

```kotlin
fun View.measureAndGetSize() : Size
```
View のサイズを返します。
View#measure() を呼んだあと、measuredWidth, measuredHeight を取得しています。


#### ListView の拡張関数
```kotlin
fun ListView.calcContentHeight():Int
fun ListView.calcFixedContentHeight():Int
```
ListView のコンテントの高さ（各アイテムの高さの合計）を計算します。それぞれ、可変アイテムサイズ用と、固定アイテムサイズ用です。

## [StyledAttrRetriever](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/StyledAttrRetriever.kt)

`StyledAttribute` のラッパークラス<br>

StyledAttribute から、色やサイズを取得するための複雑な処理を隠蔽します。また、AutoCloseable i/f を継承しており、use() によって recycle() の呼び出しを自動化します。

```kotlin
@ColorInt fun getColor(@StyleableRes attrId: Int, @AttrRes themeAttrId: Int, @AttrRes fallbackThemeAttrRes: Int, @ColorInt def: Int): Int
@ColorInt fun getColor(@StyleableRes attrId: Int, @AttrRes themeAttrId: Int, @ColorInt def: Int): Int
```
StyledAttribute から、カスタム属性 (`attrId`: `attrs.xml` で、`declare-styleable` によって定義されたID)、
テーマ色 (themeAttr`Id: attrId の色が定義されていない場合に使用する、テーマ色のID)、
フォールバックテーマ色 (`fallbackThemeAttrRes`: themeAttrIdのテーマ色が未定義の場合に使用するID)
デフォルト色 (`def`: 上記のどれも取得できない場合に使用される色) の順に利用可能な色を取得して返します。

```kotlin
@ColorInt fun getColorWithAlphaOnFallback(@StyleableRes attrId: Int, @AttrRes themeAttrId: Int, @AttrRes fallbackThemeAttrRes: Int, @ColorInt def: Int, alpha: Int): Int
@ColorInt fun getColorWithAlphaOnFallback(@StyleableRes attrId: Int, @AttrRes themeAttrId: Int, @AttrRes fallbackThemeAttrRes: Int, @ColorInt def: Int, alpha: Float): Int
@ColorInt fun getColorWithAlphaOnFallback(@StyleableRes attrId: Int, @AttrRes themeAttrId: Int, @ColorInt def: Int, alpha: Int): Int
@ColorInt fun getColorWithAlphaOnFallback(@StyleableRes attrId: Int, @AttrRes themeAttrId: Int, @ColorInt def: Int, alpha: Float): Int
```
StyledAttribute から、透過色を取得します。

getColor() と同じ優先順序で色を取得しますが、色をattrIdから取得した場合は、その色を、それ以外（テーマ色)から取得した場合は、`alpha`引数で与えたalpha値が付与されます。
alpha引数は Float 型なら、0f..1f の値、Int 型なら、0..0xFF の値を指定します。

```kotlin
fun getDrawable(@StyleableRes attrId: Int, @AttrRes themeAttrId: Int, @AttrRes fallbackThemeAttrRes: Int, @ColorInt def: Int): Drawable {
fun getDrawable(@StyleableRes attrId: Int, @AttrRes themeAttrId: Int, @ColorInt def: Int): Drawable {
fun getDrawable(@StyleableRes attrId:Int): Drawable?
```
StyledAttributes から、Drawable を取得します。
themeAttrId(, fallbackThemeAttrRes), def を引数にとる getDrawableメソッドは、カスタム属性が Drawableなら、それを返しますが、それ以外の場合は、getColor() で取得される色を ColorDrawable として返します。Viewの background のように、color または、Drawable を受け取ることのできるプロパティに使用します。

```kotlin
fun getDrawableWithAlphaOnFallback(@StyleableRes attrId: Int, @AttrRes themeAttrId: Int, @AttrRes fallbackThemeAttrRes: Int, @ColorInt def: Int, alpha: Int): Drawable
fun getDrawableWithAlphaOnFallback(@StyleableRes attrId: Int, @AttrRes themeAttrId: Int, @AttrRes fallbackThemeAttrRes: Int, @ColorInt def: Int, alpha: Float): Drawable
fun getDrawableWithAlphaOnFallback(@StyleableRes attrId: Int, @AttrRes themeAttrId: Int, @ColorInt def: Int, alpha: Int): Drawable
fun getDrawableWithAlphaOnFallback(@StyleableRes attrId: Int, @AttrRes themeAttrId: Int, @ColorInt def: Int, alpha: Float): Drawable
```
getDrawable()と同じですが、attrId 以外から色を取得した場合は、alpha値を付与します。

```kotlin
fun getDimensionPixelSize(@StyleableRes attrId: Int, def: IDimension): Int
```

StyledAttributesからサイズ情報をピクセル単位で取得します。<br>
デフォルト値を渡す `def` 引数には IDimension型を使用します。
IDimension型を使うことにより、その値が、DP 単位か、Pixel 単位かを明確に区別することが可能です。
使い方は、kotlin.time.Duration に似ており、Int型の値に、`.px` を付けると Pixel単位として、`.dp` を指定すると DP単位として扱われます。
IDimension型は、DP/Pixelの相互変換もサポートしており、例えば、100ピクセルをDPとして扱う場合は、
```
val dp = 100.px.dp(context)
```
と書くことができます。

#### 使用例
```kotlin
class MyCustomView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : View(context, attrs, defStyleAttr) {
    companion object {
      val DEF_VIEW_HEIGHT = 36.dp
      val DEF_VIEW_WIDTH = 120.dp
    }
    @ColorInt val textColor:Int
    val backgroundDrawable:Drawable
    val height:Int
    val width:Int
    init {
        StyledAttrRetriever(context, attrs, R.styleable.MyCustomView, defStyleAttr, 0).use { sar ->
          textColor = sar.getColor(R.styleable.MyCustomView_textColor, com.google.android.material.R.attr.colorOnPrimary, 0xFF000000.toInt())
          backgroundDrawable = sar.getDrawable(R.styleable.MyCustomView_backgroundDrawable, com.google.android.material.R.attr.colorPrimary, 0xFFFFFFFF.toInt())
          height = sar.getDimensionPixelSize(R.styleable.MyCustomView_height, DEF_VIEW_HEIGHT)
          width = sar.getDimensionPixelSize(R.styleable.MyCustomView_width, DEF_VIEW_WIDTH)
        }
    }
}

```

## [WeakReferenceDelegate](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/WeakReferenceDelegate.kt)

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
----
# gesture

タッチイベントの処理、ジェスチャー操作に関するユーティリティクラス群

### [UtGestureInterpreter](https://github.com/toyota-m2k/android-utilities/tree/main/libUtils/src/main/java/io/github/toyota32k/utils/gesture/UtGestureInterpreter.kt)

タッチイベントを解釈するクラス。<br>

Android の低レベルで複雑なタッチイベントを総合的に評価して、
- タップ   --> tapListener
- ロングタップ --> longTapListener
- ダブルタップ --> doubleTapListener
- スクロール --> scrollListener
- 縦フリック --> flickVerticalListener
- 横フリック --> flickHorizontalListener
- ピンチ --> scaleListener
のイベントに振り分けます。このインスタンスにリスナーハンドラを登録することで、各イベントを受け取って処理することができます

#### 使用例
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

### [UtManipulationAgent](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/gesture/UtManipulationAgent.kt)

ビューの拡大、移動、ページ切り替えなどを実現するクラス。<br>

スクロール、スケーリングイベントの解釈には、[UtGestureInterpreter](#utgestureinterpreter) を使用しています。
次の [IUtManipulationTarget](#iutmanipulationtarget) と組み合わせて使います。

### [IUtManipulationTarget](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/gesture/IUtManipulationTarget.kt)

ズーム・スクロール、スワイプによるページめくりをサポートする View に関する情報を表現するインターフェース。<br>

[UtGestureInterpreter](#utgestureinterpreter), [UtManipulationAgent](#utmanipulationagent) に対して、対象となるビュー情報、ページング情報を供給するために使用されます。

#### 使用例
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

### [UtSimpleManipulationTarget](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/gesture/UtSimpleManipulationTarget.kt)

単純な（ズーム操作の対象となるビューが動的変わらない） IUtManipulationTarget の実装クラス<br>

これを使うと、[IUtManipulationTarget](#iutmanipulationtarget) の実装例は、次のように簡略化できます。

#### 使用例
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

### [UtScaleGestureManager](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/gesture/UtScaleGestureManager.kt)

ピンチ操作によるビューのズーム操作をサポートするクラス。<br>

UtGestureInterpreter と UtManipulationAgent を組み合わせた簡単なボイラープレートです。
次の例は、ピンチによるズームをサポートする動画プレーヤーをイメージしています。タップで再生・停止をトグルし、ダブルタップで、ズーム率をリセットします。

#### 使用例
```kotlin
  gestureManager = UtScaleGestureManager(
        applicationContext, 
        enableDoubleTap:true, 
        SimpleManipulationTarget(findViewById(R.id.container), findViewById(R.id.player)))
      .setup(this) {
          onTap {
            // タップで再生・停止をトグル
            togglePlay()
          }
          onDoubleTap {
            // ダブルタップでズームをリセット
            gestureManager.agent.resetScrollAndScale()
          }
      }

```

### [UtClickRepeater](https://github.com/toyota-m2k/android-utilities/blob/main/libUtils/src/main/java/io/github/toyota32k/utils/gesture/UtClickRepeater.kt)

ボタンを押し続けたときに、Clickイベントを連続して発行できるようにするクラス。<br>

view.setOnTouchListener を使うので、これを他の用途に使う実装、例えば、UtGestureInterpreter などとは共存できません。

#### 使用例
```kotlin
val clickRepeater = UtClickRepeater()
clickRepeater.attachView(findById(R.id.button))
  ...
clickRepeater.dispose()
```