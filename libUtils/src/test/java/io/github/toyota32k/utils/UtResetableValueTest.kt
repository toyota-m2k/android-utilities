package io.github.toyota32k.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class UtResetableValueTest {

    @Test
    fun testBasicResetableValue() {
        val value = UtResetableValue<String>()

        // 初期状態では値を持たない
        assertFalse(value.hasValue)

        // 値をセット
        value.value = "テスト"
        assertTrue(value.hasValue)
        assertEquals("テスト", value.value)

        // リセット
        value.reset()
        assertFalse(value.hasValue)

        // リセット後に値を取得するとNullPointerException
        try {
            value.value
            fail("リセット後にvalueアクセスでNullPointerExceptionが発生すべき")
        } catch (e: NullPointerException) {
            // 期待通り
        }

        // リセット前の処理
        value.value = "テスト2"
        var resetCalled = false
        value.reset { v ->
            assertEquals("テスト2", v)
            resetCalled = true
        }
        assertTrue(resetCalled)

        // setIfNeed
        value.setIfNeed { "新しい値" }
        assertTrue(value.hasValue)
        assertEquals("新しい値", value.value)

        // 値があるときはsetIfNeedは何もしない
        value.setIfNeed { "変化なし" }
        assertEquals("新しい値", value.value)
    }

    @Test
    fun testLazyResetableValue() {
        var initializerCalled = 0
        val lazyValue = UtLazyResetableValue<String> {
            initializerCalled++
            "遅延初期化"
        }

        // 初期状態では初期化されていない
        assertFalse(lazyValue.hasValue)
        assertEquals(0, initializerCalled)

        // 値にアクセスすると初期化される
        assertEquals("遅延初期化", lazyValue.value)
        assertTrue(lazyValue.hasValue)
        assertEquals(1, initializerCalled)

        // 再度アクセスしても初期化関数は呼ばれない
        assertEquals("遅延初期化", lazyValue.value)
        assertEquals(1, initializerCalled)

        // 明示的に値を設定
        lazyValue.value = "明示的な値"
        assertEquals("明示的な値", lazyValue.value)

        // リセット後は再度初期化される
        lazyValue.reset()
        assertFalse(lazyValue.hasValue)
        assertEquals("遅延初期化", lazyValue.value)
        assertEquals(2, initializerCalled)

        // setIfNeedは既に値があるので呼ばれない
        lazyValue.setIfNeed { "使われない値" }
        assertEquals("遅延初期化", lazyValue.value)
    }

    @Test
    fun testResetableFlowValue() {
        val flowValue = UtResetableFlowValue<Int>()

        // 初期状態では値を持たない
        assertFalse(flowValue.hasValue)

        // 値を設定
        flowValue.value = 42
        assertTrue(flowValue.hasValue)
        assertEquals(42, flowValue.value)

        // Flow経由でも値を取得できる
        runBlocking {
            assertEquals(42, flowValue.first())
        }

        // リセット
        flowValue.reset()
        assertFalse(flowValue.hasValue)

        // 既存のMutableStateFlowを使用
        val flow = MutableStateFlow<String?>("初期値")
        val customFlowValue = UtResetableFlowValue(flow)

        assertTrue(customFlowValue.hasValue)
        assertEquals("初期値", customFlowValue.value)

        // 値の変更がFlowに反映される
        customFlowValue.value = "新しい値"
        assertEquals("新しい値", flow.value)

        // リセットするとnullになる
        customFlowValue.reset { value ->
            assertEquals("新しい値", value)
        }
        assertNull(flow.value)
        assertFalse(customFlowValue.hasValue)
    }

    @Test
    fun testNullableResetableValue() {
        // 通常のケース
        val nullableValue = UtNullableResetableValue<String>()

        assertFalse(nullableValue.hasValue)
        assertNull(nullableValue.value)

        // 値をセット
        nullableValue.setIfNeed { "テスト値" }
        assertTrue(nullableValue.hasValue)
        assertEquals("テスト値", nullableValue.value)

        // 既に値があるときは変化しない
        nullableValue.setIfNeed { "変化なし" }
        assertEquals("テスト値", nullableValue.value)

        // リセット
        nullableValue.reset(null)
        assertFalse(nullableValue.hasValue)
        assertNull(nullableValue.value)

        // nullをセットしてもhasValueはtrueにならない
        nullableValue.setIfNeed { null }
        assertFalse(nullableValue.hasValue)

        // nullの保持を許可するケース
        val keepNullValue = UtNullableResetableValue<String>(allowKeepNull = true)

        keepNullValue.setIfNeed { null }
        assertTrue(keepNullValue.hasValue)  // nullでもhasValueはtrue
        assertNull(keepNullValue.value)

        // 非同期setIfNeed
        runBlocking {
            val asyncValue = UtNullableResetableValue<Int>()
            val result = asyncValue.setIfNeedAsync { 123 }
            assertEquals(123, result)
            assertTrue(asyncValue.hasValue)
        }

        // 遅延初期化
        var lazyInitCalled = 0
        val lazyValue = UtNullableResetableValue<String>(lazy = {
            lazyInitCalled++
            "遅延値"
        })

        assertEquals(0, lazyInitCalled)
        assertEquals("遅延値", lazyValue.value)
        assertEquals(1, lazyInitCalled)
        assertTrue(lazyValue.hasValue)

        // 2回目以降は初期化関数は呼ばれない
        assertEquals("遅延値", lazyValue.value)
        assertEquals(1, lazyInitCalled)
    }

    @Test
    fun testManualIncarnateResetableValue() {
        var incarnateCount = 0
        var resetCount = 0

        val manualValue = UtManualIncarnateResetableValue<String>(
            onIncarnate = {
                incarnateCount++
                "初期値"
            },
            onReset = { value ->
                resetCount++
                assertEquals("初期値", value)
            }
        )

        // 構築時に初期化される
        assertEquals(1, incarnateCount)
        assertTrue(manualValue.hasValue)
        assertEquals("初期値", manualValue.value)

        // リセット
        manualValue.reset()
        assertEquals(1, resetCount)
        assertFalse(manualValue.hasValue)

        try {
            manualValue.value
            fail("リセット後にvalueアクセスでNullPointerExceptionが発生すべき")
        } catch (e: NullPointerException) {
            // 期待通り
        }

        // incarnate()で再初期化
        assertTrue(manualValue.incarnate())
        assertEquals(2, incarnateCount)
        assertTrue(manualValue.hasValue)
        assertEquals("初期値", manualValue.value)

        // 既に値があるときはincarnate()はfalseを返す
        assertFalse(manualValue.incarnate())
        assertEquals(2, incarnateCount)

        // 明示的に値を設定
        manualValue.value = "新しい値"
        assertEquals("新しい値", manualValue.value)

        // カスタムリセット処理
        var customResetCalled = false
        manualValue.reset { value ->
            assertEquals("新しい値", value)
            customResetCalled = true
        }
        assertTrue(customResetCalled)
        assertEquals(1, resetCount) // onResetは呼ばれない
    }
}