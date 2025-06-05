package io.github.toyota32k.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.Closeable

class GenericDisposableTest {

    @Test
    fun testBasicDisposeFunctionality() {
        // 準備
        var disposeCount = 0
        val disposable = GenericDisposable { disposeCount++ }

        // 初期状態の確認
        assertEquals(0, disposeCount)

        // dispose実行
        disposable.dispose()
        assertEquals(1, disposeCount)

        // 2回目のdisposeは実行されないことを確認
        disposable.dispose()
        assertEquals(1, disposeCount)
    }

    @Test
    fun testCompanionCreate() {
        // 準備
        var setupCalled = false
        var disposeCalled = false

        // createメソッドでセットアップと後始末の関数を設定
        val disposable = GenericDisposable.create {
            setupCalled = true
            // 戻り値として後始末関数を返す
            { disposeCalled = true }
        }

        // createメソッド呼び出し時点でセットアップ関数が実行される
        assertTrue(setupCalled)
        assertFalse(disposeCalled)

        // dispose実行で後始末関数が実行される
        disposable.dispose()
        assertTrue(disposeCalled)
    }

    @Test
    fun testAsDisposableExtension() {
        // 準備
        var closeCalled = false

        // テスト用のCloseable実装
        val closeable = object : Closeable {
            override fun close() {
                closeCalled = true
            }
        }

        // CloseableをIDisposableに変換
        val disposable = closeable.asDisposable()

        // 初期状態
        assertFalse(closeCalled)

        // dispose呼び出しがcloseに転送される
        disposable.dispose()
        assertTrue(closeCalled)
    }

    @Test
    fun testMultipleDisposes() {
        // 準備
        var disposeCount = 0
        val disposable = GenericDisposable { disposeCount++ }

        // 複数回disposeしても1回しか実行されない
        for (i in 1..5) {
            disposable.dispose()
        }
        assertEquals(1, disposeCount)
    }

    @Test
    fun testCloseableToDisposableAndBack() {
        // 準備
        var operationCount = 0

        // Closeable作成
        val closeable = GenericCloseable { operationCount++ }

        // Closeable -> IDisposable -> Closeable の変換
        val disposable = closeable.asDisposable()
        val reconvertedCloseable = disposable.asCloseable()

        // 初期状態
        assertEquals(0, operationCount)

        // 変換後のCloseableを使用
        reconvertedCloseable.close()
        assertEquals(1, operationCount)

        // 元のCloseableは既に処理済みになる
        closeable.close()
        assertEquals(1, operationCount)
    }

    @Test
    fun testCompositeDisposables() {
        // 準備
        val disposeOrder = mutableListOf<String>()

        // 複数のDisposableを作成
        val disposable1 = GenericDisposable { disposeOrder.add("disposable1") }
        val disposable2 = GenericDisposable { disposeOrder.add("disposable2") }
        val disposable3 = GenericDisposable { disposeOrder.add("disposable3") }

        // 全て正常に動作することを確認
        assertFalse(disposeOrder.contains("disposable1"))
        assertFalse(disposeOrder.contains("disposable2"))
        assertFalse(disposeOrder.contains("disposable3"))

        // 順に破棄
        disposable1.dispose()
        assertEquals(listOf("disposable1"), disposeOrder)

        disposable2.dispose()
        assertEquals(listOf("disposable1", "disposable2"), disposeOrder)

        disposable3.dispose()
        assertEquals(listOf("disposable1", "disposable2", "disposable3"), disposeOrder)
    }

    @Test
    fun testGenericDisposableWithLambda() {
        // ラムダ式を利用した場合
        var result = ""
        val disposable = GenericDisposable {
            result = "処理完了"
        }

        // 初期状態
        assertEquals("", result)

        // 実行
        disposable.dispose()
        assertEquals("処理完了", result)
    }
}