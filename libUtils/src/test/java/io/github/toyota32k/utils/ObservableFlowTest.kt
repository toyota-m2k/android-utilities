package io.github.toyota32k.utils

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import io.github.toyota32k.utils.lifecycle.ObservableFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`

@ExperimentalCoroutinesApi
class ObservableFlowTest {

    // LiveData関連のテストのためのルール
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Dispatchersを制御するためのルール
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        // テスト開始時にMain Dispatcherをセット
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        // テスト終了後にMain Dispatcherをリセット
        Dispatchers.resetMain()
    }

    // テスト用のLifecycleOwner
    private fun createLifecycleOwner(state: Lifecycle.State): LifecycleOwner {
        val owner = Mockito.mock(LifecycleOwner::class.java)
        val lifecycle = LifecycleRegistry(owner)
        lifecycle.handleLifecycleEvent(when(state) {
            Lifecycle.State.CREATED -> Lifecycle.Event.ON_CREATE
            Lifecycle.State.STARTED -> Lifecycle.Event.ON_START
            Lifecycle.State.RESUMED -> Lifecycle.Event.ON_RESUME
            Lifecycle.State.DESTROYED -> Lifecycle.Event.ON_DESTROY
            else -> throw IllegalArgumentException("Unsupported state: $state")
        })
        `when`(owner.lifecycle).thenReturn(lifecycle)
        return owner
    }

    @Test
    fun testObserveWithLifecycleOwner() = runTest {
        val flow = MutableStateFlow(0)
        val observableFlow = ObservableFlow(flow)

        // アクティブなLifecycleOwner
        val activeOwner = createLifecycleOwner(Lifecycle.State.RESUMED)

        val values = mutableListOf<Int>()
        val disposable = observableFlow.observe(activeOwner) { values.add(it) }

        advanceUntilIdle() // 非同期処理を完了させる

        // 初期値が取得されている
        assertEquals(listOf(0), values)

        // 値の変更が通知される
        flow.value = 1
        advanceUntilIdle() // 非同期処理を完了させる

        assertEquals(listOf(0, 1), values)

        // 解除後は値が変わっても通知されない
        disposable.dispose()
        flow.value = 2
        advanceUntilIdle() // 非同期処理を完了させる

        assertEquals(listOf(0, 1), values)
    }

    @Test
    fun testObserveWithCoroutineContext() = runTest {
        val flow = MutableStateFlow("初期値")
        val observableFlow = ObservableFlow(flow)

        val values = mutableListOf<String>()
        val disposable = observableFlow.observe(testDispatcher) { values.add(it) }

        advanceUntilIdle() // 非同期処理を完了させる

        // 初期値が取得されている
        assertEquals(listOf("初期値"), values)

        // 値の変更が通知される
        flow.value = "変更値"
        advanceUntilIdle() // 非同期処理を完了させる

        assertEquals(listOf("初期値", "変更値"), values)

        // 解除後は値が変わっても通知されない
        disposable.dispose()
        flow.value = "解除後"
        advanceUntilIdle() // 非同期処理を完了させる

        assertEquals(listOf("初期値", "変更値"), values)
    }

    @Test
    fun testRemoveObserver() = runTest {
        val flow = MutableStateFlow(10)
        val observableFlow = ObservableFlow(flow)

        val values1 = mutableListOf<Int>()
        val values2 = mutableListOf<Int>()

        val disposable1 = observableFlow.observe(testDispatcher) { values1.add(it) }
        val disposable2 = observableFlow.observe(testDispatcher) { values2.add(it) }

        advanceUntilIdle() // 非同期処理を完了させる

        assertEquals(listOf(10), values1)
        assertEquals(listOf(10), values2)

        // 1つのオブザーバーを削除
        observableFlow.removeObserver(disposable1)

        flow.value = 20
        advanceUntilIdle() // 非同期処理を完了させる

        // disposable1は通知されないが、disposable2は通知される
        assertEquals(listOf(10), values1)
        assertEquals(listOf(10, 20), values2)
    }

    @Test
    fun testClean() = runTest {
        val flow = MutableStateFlow(0)
        val observableFlow = ObservableFlow(flow)

        val values1 = mutableListOf<Int>()
        val values2 = mutableListOf<Int>()

        val disposable1 = observableFlow.observe(testDispatcher) { values1.add(it) }
        advanceUntilIdle() // 非同期処理を完了させる

        disposable1.dispose() // 明示的に破棄

        val disposable2 = observableFlow.observe(testDispatcher) { values2.add(it) }
        advanceUntilIdle() // 非同期処理を完了させる

        // クリーンアップ前はdisposable1がDisposerに残っている可能性がある
        observableFlow.clean()

        // 新しい値を発行
        flow.value = 1
        advanceUntilIdle() // 非同期処理を完了させる

        // disposable2だけが通知される
        assertEquals(listOf(0), values1)
        assertEquals(listOf(0, 1), values2)
    }

    @Test
    fun testDispose() = runTest {
        val flow = MutableStateFlow("テスト")
        val observableFlow = ObservableFlow(flow)

        val values1 = mutableListOf<String>()
        val values2 = mutableListOf<String>()

        observableFlow.observe(testDispatcher) { values1.add(it) }
        observableFlow.observe(testDispatcher) { values2.add(it) }
        advanceUntilIdle() // 非同期処理を完了させる

        assertEquals(listOf("テスト"), values1)
        assertEquals(listOf("テスト"), values2)

        // ObservableFlow自体を破棄
        observableFlow.dispose()

        // 値の変更は通知されない
        flow.value = "更新後"
        advanceUntilIdle() // 非同期処理を完了させる

        assertEquals(listOf("テスト"), values1)
        assertEquals(listOf("テスト"), values2)
    }

    @Test
    fun testFlowDelegation() = runTest {
        val testFlow = flow {
            emit(1)
            delay(10)
            emit(2)
        }

        val observableFlow = ObservableFlow(testFlow)

        // Flowインターフェースのメソッドが正しく委譲されていることを確認
        assertEquals(1, observableFlow.first())
    }

    @Test
    fun testLifecycleAwareness() = runTest {
        val flow = MutableStateFlow(0)
        val observableFlow = ObservableFlow(flow)

        // アクティブなLifecycleOwner
        val owner = createLifecycleOwner(Lifecycle.State.RESUMED)
        val lifecycle = owner.lifecycle as LifecycleRegistry

        val values = mutableListOf<Int>()
        observableFlow.observe(owner) { values.add(it) }
        advanceUntilIdle() // 非同期処理を完了させる

        assertEquals(listOf(0), values)

        flow.value = 1
        advanceUntilIdle() // 非同期処理を完了させる

        assertEquals(listOf(0, 1), values)

        // ライフサイクル状態の変更
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        advanceUntilIdle() // 非同期処理を完了させる

        flow.value = 2
        advanceUntilIdle() // 非同期処理を完了させる

        // DESTROYED状態になると値は収集されない
        assertEquals(listOf(0, 1), values)
    }
}