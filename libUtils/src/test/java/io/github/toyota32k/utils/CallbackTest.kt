package io.github.toyota32k.utils

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import io.github.toyota32k.utils.lifecycle.Callback
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`

class CallbackTest {

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
    fun testCallbackWithLifecycleOwner() {
        // 準備
        val owner = createLifecycleOwner(Lifecycle.State.RESUMED)
        val callCount = mutableListOf<String>()

        // 実行
        val callback = Callback<String, Boolean>(owner) { param ->
            callCount.add(param)
            true
        }

        // 初期状態での呼び出し
        val result1 = callback.invoke("テスト1")
        assertEquals(true, result1)
        assertEquals(listOf("テスト1"), callCount)

        // 再度呼び出し
        val result2 = callback.invoke("テスト2")
        assertEquals(true, result2)
        assertEquals(listOf("テスト1", "テスト2"), callCount)

        // 破棄してからの呼び出し
        callback.dispose()
        val result3 = callback.invoke("テスト3")
        assertNull(result3)
        assertEquals(listOf("テスト1", "テスト2"), callCount) // 追加されていないことを確認
    }

    @Test
    fun testEmptyCallback() {
        // 空のコンストラクタで初期化
        val emptyCallback = Callback<Int, String>()

        // invokeしても何も起きないことを確認
        val result = emptyCallback.invoke(42)
        assertNull(result)
    }

    @Test
    fun testSetCallback() {
        // 準備
        val owner1 = createLifecycleOwner(Lifecycle.State.RESUMED)
        val owner2 = createLifecycleOwner(Lifecycle.State.RESUMED)
        val callList = mutableListOf<Pair<String, Int>>()

        // 最初のコールバック
        val callback = Callback<Int, String>(owner1) { param ->
            callList.add(Pair("callback1", param))
            "結果1"
        }

        // 一度呼び出し
        val result1 = callback.invoke(10)
        assertEquals("結果1", result1)
        assertEquals(listOf(Pair("callback1", 10)), callList)

        // 新しいコールバックに設定
        callback.set(owner2) { param ->
            callList.add(Pair("callback2", param))
            "結果2"
        }

        // 再度呼び出し
        val result2 = callback.invoke(20)
        assertEquals("結果2", result2)
        assertEquals(listOf(Pair("callback1", 10), Pair("callback2", 20)), callList)
    }

    @Test
    fun testLifecycleDestruction() {
        // 準備
        val owner = createLifecycleOwner(Lifecycle.State.RESUMED)
        val lifecycle = owner.lifecycle as LifecycleRegistry
        val callList = mutableListOf<String>()

        // コールバック作成
        val callback = Callback<String, Unit>(owner) { param ->
            callList.add(param)
        }

        // 通常の呼び出し
        callback.invoke("アクティブ")
        assertEquals(listOf("アクティブ"), callList)

        // ライフサイクル破棄
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)

        // 破棄後の呼び出しは無効になる
        val result = callback.invoke("破棄後")
        assertNull(result)
        assertEquals(listOf("アクティブ"), callList)
    }

    @Test
    fun testMultipleDisposes() {
        // 準備
        val owner = createLifecycleOwner(Lifecycle.State.RESUMED)
        val callCount = mutableListOf<Int>()

        // コールバック作成
        val callback = Callback<Int, Boolean>(owner) { param ->
            callCount.add(param)
            true
        }

        // 初期呼び出し
        callback.invoke(1)
        assertEquals(listOf(1), callCount)

        // 一度目の破棄
        callback.dispose()
        val result1 = callback.invoke(2)
        assertNull(result1)
        assertEquals(listOf(1), callCount)

        // 二度目の破棄も安全に実行できる
        callback.dispose()
        val result2 = callback.invoke(3)
        assertNull(result2)
        assertEquals(listOf(1), callCount)
    }

    @Test
    fun testGenericTypesWithComplexReturnType() {
        // 準備
        val owner = createLifecycleOwner(Lifecycle.State.RESUMED)

        // 複雑な型のコールバック
        val callback = Callback<List<String>, Map<String, Int>>(owner) { strings ->
            strings.associateWith { it.length }
        }

        // 呼び出しと結果確認
        val input = listOf("あいうえお", "かきくけこ", "さしすせそ")
        val result = callback.invoke(input)

        // 期待される結果
        val expected = mapOf(
            "あいうえお" to 5,
            "かきくけこ" to 5,
            "さしすせそ" to 5
        )

        assertEquals(expected, result)
    }
}