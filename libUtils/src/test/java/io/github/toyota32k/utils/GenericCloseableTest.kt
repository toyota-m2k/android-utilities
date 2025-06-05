package io.github.toyota32k.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.io.Closeable

class GenericCloseableTest {

    @Test
    fun testBasicCloseFunctionality() {
        // 準備
        var closeCount = 0
        val closeable = GenericCloseable { closeCount++ }

        // 初期状態の確認
        assertEquals(0, closeCount)

        // close実行
        closeable.close()
        assertEquals(1, closeCount)

        // 2回目のcloseは実行されないことを確認
        closeable.close()
        assertEquals(1, closeCount)
    }

    @Test
    fun testCompanionCreate() {
        // 準備
        var setupCalled = false
        var closeCalled = false

        // createメソッドでセットアップと後始末の関数を設定
        val closeable = GenericCloseable.create {
            setupCalled = true
            // 戻り値として後始末関数を返す
            { closeCalled = true }
        }

        // createメソッド呼び出し時点でセットアップ関数が実行される
        assertTrue(setupCalled)
        assertFalse(closeCalled)

        // close実行で後始末関数が実行される
        closeable.close()
        assertTrue(closeCalled)
    }

    @Test
    fun testAsCloseableExtension() {
        // 準備
        var disposeCalled = false

        // テスト用のIDisposable実装
        val disposable = object : IDisposable {
            override fun dispose() {
                disposeCalled = true
            }
        }

        // IDisposableをCloseableに変換
        val closeable = disposable.asCloseable()

        // 初期状態
        assertFalse(disposeCalled)

        // close呼び出しがdisposeに転送される
        closeable.close()
        assertTrue(disposeCalled)
    }

    @Test
    fun testMultipleCloses() {
        // 準備
        var closeCount = 0
        val closeable = GenericCloseable { closeCount++ }

        // 複数回closeしても1回しか実行されない
        for (i in 1..5) {
            closeable.close()
        }
        assertEquals(1, closeCount)
    }

    @Test
    fun testWithResources() {
        // 準備
        var resourceClosed = false
        val resource = GenericCloseable { resourceClosed = true }

        // try-with-resourcesパターンでの使用
        try {
            resource.use {
                // リソースを使用
                assertFalse(resourceClosed)
            }
            // ブロック終了時に自動的にclose呼び出し
            assertTrue(resourceClosed)
        } catch (e: Exception) {
            fail("例外が発生しました: ${e.message}")
        }
    }

    @Test
    fun testChainOfCloseables() {
        // 準備
        val closeOrder = mutableListOf<String>()

        // 複数のCloseableを作成
        val closeable1 = GenericCloseable { closeOrder.add("closeable1") }
        val closeable2 = GenericCloseable { closeOrder.add("closeable2") }
        val closeable3 = GenericCloseable { closeOrder.add("closeable3") }

        // 連鎖したリソース使用パターン
        closeable1.use { _ ->
            closeable2.use { _ ->
                closeable3.use { _ ->
                    // すべてのリソースを使用
                    assertEquals(0, closeOrder.size)
                }
                // closeable3が閉じられる
                assertEquals(listOf("closeable3"), closeOrder)
            }
            // closeable2が閉じられる
            assertEquals(listOf("closeable3", "closeable2"), closeOrder)
        }
        // closeable1が閉じられる
        assertEquals(listOf("closeable3", "closeable2", "closeable1"), closeOrder)
    }

    @Test
    fun testDisposableToCloseableAndBack() {
        // 準備
        var operationCount = 0

        // IDisposable作成
        val disposable = GenericDisposable { operationCount++ }

        // IDisposable -> Closeable -> IDisposable の変換
        val closeable = disposable.asCloseable()
        val reconvertedDisposable = closeable.asDisposable()

        // 初期状態
        assertEquals(0, operationCount)

        // 変換後のIDisposableを使用
        reconvertedDisposable.dispose()
        assertEquals(1, operationCount)

        // 元のDisposableは既に処理済みになる
        disposable.dispose()
        assertEquals(1, operationCount)
    }

    private fun assertTrue(condition: Boolean) {
        org.junit.Assert.assertTrue(condition)
    }

    private fun assertFalse(condition: Boolean) {
        org.junit.Assert.assertFalse(condition)
    }

    private fun fail(message: String) {
        org.junit.Assert.fail(message)
    }
}