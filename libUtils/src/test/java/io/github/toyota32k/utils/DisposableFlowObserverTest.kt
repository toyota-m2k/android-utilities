package io.github.toyota32k.utils

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import io.github.toyota32k.utils.lifecycle.DisposableFlowObserver
import io.github.toyota32k.utils.lifecycle.disposableObserve
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`

@ExperimentalCoroutinesApi
class DisposableFlowObserverTest {

    // LiveData関連のテストのためのルール
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // テスト用のディスパッチャー
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

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
            else -> throw IllegalArgumentException("未対応のライフサイクル状態: $state")
        })
        `when`(owner.lifecycle).thenReturn(lifecycle)
        return owner
    }

    @Test
    fun testDisposableFlowObserverWithContext() = testScope.runTest {
        // 準備
        val flow = MutableStateFlow(0)
        val values = mutableListOf<Int>()

        // 実行
        val disposable = DisposableFlowObserver(flow, testDispatcher) { values.add(it) }

        // 初期状態の検証
        assertFalse(disposable.disposed)
        advanceUntilIdle() // 非同期処理完了を待つ
        assertEquals(listOf(0), values)

        // 値の変更が通知されること
        flow.value = 1
        advanceUntilIdle()
        assertEquals(listOf(0, 1), values)

        // 破棄後のテスト
        disposable.dispose()
        assertTrue(disposable.disposed)

        // 破棄後は値が変更されても通知されないこと
        flow.value = 2
        advanceUntilIdle()
        assertEquals(listOf(0, 1), values)
    }

    @Test
    fun testDisposableFlowObserverWithLifecycleOwner() = testScope.runTest {
        // 準備
        val flow = MutableStateFlow("テスト")
        val owner = createLifecycleOwner(Lifecycle.State.RESUMED)
        val values = mutableListOf<String>()

        // 実行
        val disposable = DisposableFlowObserver(flow, owner) { values.add(it) }

        // 初期状態の検証
        assertFalse(disposable.disposed)
        advanceUntilIdle()
        assertEquals(listOf("テスト"), values)

        // 値の変更が通知されること
        flow.value = "変更後"
        advanceUntilIdle()
        assertEquals(listOf("テスト", "変更後"), values)

        // 破棄後のテスト
        disposable.dispose()
        assertTrue(disposable.disposed)

        // 破棄後は値が変更されても通知されないこと
        flow.value = "破棄後"
        advanceUntilIdle()
        assertEquals(listOf("テスト", "変更後"), values)
    }

    @Test
    fun testFlowExtensions() = testScope.runTest {
        // 準備
        val flow = MutableStateFlow(0)
        val values1 = mutableListOf<Int>()
        val values2 = mutableListOf<Int>()

        // 拡張関数でオブザーバー登録
        val disposable1 = flow.disposableObserve(testDispatcher) { values1.add(it) }
        advanceUntilIdle()
        assertEquals(listOf(0), values1)

        // 値変更の確認
        flow.value = 1
        advanceUntilIdle()
        assertEquals(listOf(0, 1), values1)

        // LifecycleOwnerバージョンも確認
        val owner = createLifecycleOwner(Lifecycle.State.RESUMED)
        val disposable2 = flow.disposableObserve(owner) { values2.add(it) }
        advanceUntilIdle()
        assertEquals(listOf(1), values2)  // 現在値が1なので1が通知される

        // 値変更後に両方のオブザーバーが通知されること
        flow.value = 2
        advanceUntilIdle()
        assertEquals(listOf(0, 1, 2), values1)
        assertEquals(listOf(1, 2), values2)

        // 解除した場合
        disposable1.dispose()
        flow.value = 3
        advanceUntilIdle()
        assertEquals(listOf(0, 1, 2), values1)  // 通知されない
        assertEquals(listOf(1, 2, 3), values2)  // まだ通知される

        // 両方解除
        disposable2.dispose()
        flow.value = 4
        advanceUntilIdle()
        assertEquals(listOf(0, 1, 2), values1)
        assertEquals(listOf(1, 2, 3), values2)
    }

    @Test
    fun testMultipleDisposals() = testScope.runTest {
        // 準備
        val flow = MutableStateFlow("初期値")
        val values = mutableListOf<String>()

        // 実行
        val disposable = DisposableFlowObserver(flow, testDispatcher) { values.add(it) }
        advanceUntilIdle()

        // 破棄
        disposable.dispose()
        assertTrue(disposable.disposed)

        // 2回目の破棄も安全に実行できることを確認
        disposable.dispose()
        assertTrue(disposable.disposed)

        // 値は初期値のみ
        assertEquals(listOf("初期値"), values)
    }

    @Test
    fun testWithDelayedFlow() = testScope.runTest {
        // 遅延のあるフローを作成
        val delayedFlow = flow {
            emit("最初")
            delay(100)
            emit("遅延後")
        }

        val values = mutableListOf<String>()
        val disposable = DisposableFlowObserver(delayedFlow, testDispatcher) { values.add(it) }

        // 非同期処理が完了するまで時間を進める
        advanceTimeBy(50)
        assertEquals(listOf("最初"), values)

        // 遅延後の値も受信できることを確認
        advanceTimeBy(100)
        assertEquals(listOf("最初", "遅延後"), values)

        disposable.dispose()
    }
}