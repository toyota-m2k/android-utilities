package io.github.toyota32k.utils

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.Observer
import io.github.toyota32k.utils.lifecycle.UtMutableStateFlowLiveData
import io.github.toyota32k.utils.lifecycle.asMutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@ExperimentalCoroutinesApi
class UtMutableStateFlowLiveDataTest {

    // LiveDataのテスト用ルール
    @get:Rule
    val rule: TestRule = InstantTaskExecutorRule()

    // コルーチンテスト用のディスパッチャー
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()

    @Captor
    private lateinit var stringCaptor: ArgumentCaptor<String>

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        // テスト用のメインディスパッチャーを設定
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        // テスト後にディスパッチャーをリセット
        Dispatchers.resetMain()
    }

    @Test
    fun initialValueTest() {
        val initialValue = "initial value"
        val stateFlow = MutableStateFlow(initialValue)
        val liveData = UtMutableStateFlowLiveData(stateFlow)

        assertEquals(initialValue, liveData.value)
    }

    @Test
    fun observerTest() = runTest {
        // StateFlowの値変更がLiveDataに反映されることを確認
        val initialValue = "initial value"
        val modifiedValue = "modified value"
        val stateFlow = MutableStateFlow(initialValue)
        val lifecycleOwner = mockLifecycleOwner()

        // LifecycleOwnerなしで初期化
        val liveData = UtMutableStateFlowLiveData(stateFlow)

        // Observerの設定
        val observer: Observer<String> = mock()
        liveData.observeForever(observer)

        // 手動でライフサイクルを接続
        liveData.attachToLifecycle(lifecycleOwner)

        // 値の変更とディスパッチャーの進行
        stateFlow.value = modifiedValue
        testDispatcher.scheduler.advanceUntilIdle()

        // 検証
        verify(observer).onChanged(initialValue)
        verify(observer).onChanged(modifiedValue)
    }

    @Test
    fun liveDataToStateFlowTest() = runTest {
        // LiveDataの値変更がStateFlowに反映されることを確認
        val initialValue = "initial value"
        val secondValue = "second value"
        val thirdValue = "third value"
        val stateFlow = MutableStateFlow(initialValue)
        val liveData = UtMutableStateFlowLiveData(stateFlow, mockLifecycleOwner())

        assertEquals(initialValue, stateFlow.value)
        assertEquals(initialValue, liveData.value)

        // LiveDataの値を更新
        liveData.value = secondValue

        // StateFlowの値も更新されていることを確認
        assertEquals(secondValue, stateFlow.value)

        // StateFlowの値を更新
        stateFlow.value = thirdValue
        // テスト用ディスパッチャー（TestDispatcher）のスケジューラーに対して「キューに入っているすべてのタスクが完了するまで時間を進める」ように指示
        // これをやらないと、非同期に実行されている flow.collect が処理される保証がない。
        testDispatcher.scheduler.advanceUntilIdle()

        // LiveDataの値も更新されていることを確認
        assertEquals(thirdValue, liveData.value)
    }

    @Test
    fun extensionFunctionTest() = runTest {
        val initialValue = "initial value"
        val modifiedValue = "modified value"
        val stateFlow = MutableStateFlow(initialValue)
        val lifecycleOwner = mockLifecycleOwner()

        val liveData = stateFlow.asMutableLiveData(lifecycleOwner)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(initialValue, liveData.value)

        // StateFlowの更新
        stateFlow.value = modifiedValue
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(modifiedValue, liveData.value)
    }

    private fun mockLifecycleOwner(): LifecycleOwner {
        val lifecycleOwner = mock<LifecycleOwner>()
        val lifecycle = LifecycleRegistry(lifecycleOwner)
        Mockito.`when`(lifecycleOwner.lifecycle).thenReturn(lifecycle)
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        return lifecycleOwner
    }
}