package io.github.toyota32k.utils

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.MutableLiveData
import io.github.toyota32k.utils.lifecycle.closableObserve
import io.github.toyota32k.utils.lifecycle.closableObserveForever
import io.github.toyota32k.utils.lifecycle.disposableObserve
import io.github.toyota32k.utils.lifecycle.disposableObserveForever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`

class DisposableObserverTest {

    // LiveDataをテストで使用するために必要なルール
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

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
    fun testDisposableObserver() {
        // 準備
        val data = MutableLiveData<String>()
        val owner = createLifecycleOwner(Lifecycle.State.RESUMED)
        val values = mutableListOf<String>()

        // 実行
        val disposable = data.disposableObserve(owner) { values.add(it) }

        // 初期状態の検証
        assertFalse(disposable.disposed)
        assertEquals(0, values.size)

        // データ変更を通知
        data.value = "テスト1"
        assertEquals(listOf("テスト1"), values)

        // 破棄後のテスト
        disposable.dispose()
        assertTrue(disposable.disposed)

        // 破棄後はデータが変更されても通知されない
        data.value = "テスト2"
        assertEquals(listOf("テスト1"), values)
    }

    @Test
    fun testDisposableForeverObserver() {
        // 準備
        val data = MutableLiveData<Int>()
        val values = mutableListOf<Int>()

        // 実行
        val disposable = data.disposableObserveForever { values.add(it) }

        // 初期状態の検証
        assertFalse(disposable.disposed)
        assertEquals(0, values.size)

        // データ変更を通知
        data.value = 1
        assertEquals(listOf(1), values)

        data.value = 2
        assertEquals(listOf(1, 2), values)

        // 破棄後のテスト
        disposable.dispose()
        assertTrue(disposable.disposed)

        // 破棄後はデータが変更されても通知されない
        data.value = 3
        assertEquals(listOf(1, 2), values)
    }

    @Test
    fun testClosableObserve() {
        // 準備
        val data = MutableLiveData<String>()
        val owner = createLifecycleOwner(Lifecycle.State.RESUMED)
        val values = mutableListOf<String>()

        // 実行
        val closable = data.closableObserve(owner) { values.add(it) }

        // データ変更を通知
        data.value = "Hello"
        assertEquals(listOf("Hello"), values)

        // closableインターフェースを使用して解除
        closable.close()
        data.value = "World"

        // 破棄後はデータが変更されても通知されない
        assertEquals(listOf("Hello"), values)
    }

    @Test
    fun testClosableObserveForever() {
        // 準備
        val data = MutableLiveData<Double>()
        val values = mutableListOf<Double>()

        // 実行
        val closable = data.closableObserveForever { values.add(it) }

        // データ変更を通知
        data.value = 1.0
        data.value = 2.5
        assertEquals(listOf(1.0, 2.5), values)

        // closableインターフェースを使用して解除
        closable.close()
        data.value = 3.0

        // 破棄後はデータが変更されても通知されない
        assertEquals(listOf(1.0, 2.5), values)
    }

    @Test
    fun testLifecycleAwareness() {
        // 準備
        val data = MutableLiveData<String>()
        val owner = createLifecycleOwner(Lifecycle.State.RESUMED)
        val lifecycle = owner.lifecycle as LifecycleRegistry
        val values = mutableListOf<String>()

        // 実行
        data.disposableObserve(owner) { values.add(it) }

        // RESUMED状態での通知
        data.value = "アクティブ"
        assertEquals(listOf("アクティブ"), values)

        // STOPPED状態への変更
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        data.value = "停止中"

        // STOPPED状態では通知されない
        assertEquals(listOf("アクティブ"), values)

        // RESUMED状態への復帰
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        // 再開時に最新の値が通知される
        assertEquals(listOf("アクティブ", "停止中"), values)

        // ライフサイクル破棄
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        data.value = "破棄後"

        // DESTROYED状態では通知されない
        assertEquals(listOf("アクティブ", "停止中"), values)
    }

    @Test
    fun testMultipleObservers() {
        // 準備
        val data = MutableLiveData<String>()
        val owner = createLifecycleOwner(Lifecycle.State.RESUMED)
        val values1 = mutableListOf<String>()
        val values2 = mutableListOf<String>()

        // 実行: 2つのオブザーバーを登録
        val disposable1 = data.disposableObserve(owner) { values1.add(it) }
        val disposable2 = data.disposableObserveForever { values2.add(it) }

        // データ変更を通知
        data.value = "両方"
        assertEquals(listOf("両方"), values1)
        assertEquals(listOf("両方"), values2)

        // 1つ目のオブザーバーを解除
        disposable1.dispose()
        data.value = "1つ目解除後"

        // 1つ目は通知されず、2つ目は通知される
        assertEquals(listOf("両方"), values1)
        assertEquals(listOf("両方", "1つ目解除後"), values2)

        // 2つ目のオブザーバーも解除
        disposable2.dispose()
        data.value = "両方解除後"

        // どちらも通知されない
        assertEquals(listOf("両方"), values1)
        assertEquals(listOf("両方", "1つ目解除後"), values2)
    }
}