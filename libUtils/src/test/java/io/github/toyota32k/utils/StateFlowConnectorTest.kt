package io.github.toyota32k.utils

import io.github.toyota32k.utils.StateFlowConnector.Companion.connectTo
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class StateFlowConnectorTest {
    private lateinit var testScope: CoroutineScope

    @Before
    fun setup() {
        testScope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
    }

    @After
    fun tearDown() {
        testScope.cancel()
    }

    @Test
    fun testBasicConnection() = runBlocking {
        val source = MutableStateFlow(0)
        val destination = MutableStateFlow(0)
        val connector = StateFlowConnector(source, destination, testScope)

        // sourceの変更がdestinationに反映されることを確認
        source.value = 1
        delay(100) // 値の伝播を待つ
        Assert.assertEquals(1, destination.value)

        source.value = 2
        delay(100)
        Assert.assertEquals(2, destination.value)

        // コネクタを破棄しても正常終了することを確認
        connector.dispose()
    }

    @Test
    fun testDispose() = runBlocking {
        val source = MutableStateFlow(0)
        val destination = MutableStateFlow(0)
        val connector = StateFlowConnector(source, destination, testScope)

        // 最初の接続確認
        source.value = 1
        delay(100)
        Assert.assertEquals(1, destination.value)

        // コネクタを破棄
        connector.dispose()

        // 破棄後は値が伝播しないことを確認
        source.value = 2
        delay(100)
        Assert.assertEquals(1, destination.value) // 2ではなく1のまま
    }

    @Test
    fun testExtensionFunction() = runBlocking {
        val source = MutableStateFlow(0)
        val destination = MutableStateFlow(0)

        // 拡張関数を使った接続
        val connector = source.connectTo(destination, testScope)

        // 正常に接続されていることを確認
        source.value = 1
        delay(100)
        Assert.assertEquals(1, destination.value)

        connector.dispose()

        source.value = 2
        delay(100)
        Assert.assertEquals(1, destination.value)
    }

    @Test
    fun testWithFlowSource() = runBlocking {
        val counter = AtomicInteger(0)
        val source = flow {
            emit(counter.incrementAndGet()) // 1
            delay(100)
            emit(counter.incrementAndGet()) // 2
            delay(100)
            emit(counter.incrementAndGet()) // 3
        }

        val destination = MutableStateFlow(0)
        val connector = StateFlowConnector(source, destination, testScope)

        // 連続して値が変化することを確認
        // 微妙な「タイミングずらし」でいけるんか？ --> 案外いけたｗ
        delay(50)
        Assert.assertEquals(1, destination.value)

        delay(100)
        Assert.assertEquals(2, destination.value)

        delay(100)
        Assert.assertEquals(3, destination.value)

        connector.dispose()
    }

    @Test
    fun testParentScopeCancel() = runBlocking {
        val parentScope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val source = MutableStateFlow(0)
        val destination = MutableStateFlow(0)

        // 親スコープを使って接続
        StateFlowConnector(source, destination, parentScope)

        // 正常に接続されていることを確認
        source.value = 1
        delay(100)
        Assert.assertEquals(1, destination.value)

        // 親スコープをキャンセル
        parentScope.cancel()
        delay(100)

        // キャンセル後は値が伝播しないことを確認
        source.value = 2
        delay(100)
        Assert.assertEquals(1, destination.value)
    }

    @Test
    fun testFlowCompletion() = runBlocking {
        val source = flow {
            emit(1)
            emit(2)
            // flowが完了する
        }

        val destination = MutableStateFlow(0)
        val connector = StateFlowConnector(source, destination, testScope)

        delay(100) // 処理完了を待つ
        Assert.assertEquals(2, destination.value)

        // flowは完了しているが、connectorは明示的に破棄する必要がある
        connector.dispose()
    }
}