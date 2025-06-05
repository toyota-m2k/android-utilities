package io.github.toyota32k.utils

import androidx.appcompat.app.AppCompatActivity
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import io.github.toyota32k.utils.lifecycle.LifecycleReference
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController

@RunWith(RobolectricTestRunner::class)
class LifecycleReferenceTest {
    @get:Rule
    val instantExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var activityController: ActivityController<JustTestActivity>

    private fun createActivity(): AppCompatActivity {
        activityController = Robolectric.buildActivity(JustTestActivity::class.java)
        return activityController.create().start().get() as AppCompatActivity
    }

    private fun finish() {
        activityController.pause().destroy()
    }

    // テスト用のライフサイクル所有者を作成
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
    fun testBasicLifecycleReference() {
        // 準備
        val activity = createActivity()
        val testObject = "テストオブジェクト"
        var destroyedObject: String? = null

        // LifecycleReferenceの作成
        val reference = LifecycleReference(
            testObject,
            activity
        ) { destroyedObject = it }

        // 初期状態の確認
        assertEquals(testObject, reference.value)
        assertNull(destroyedObject)

        // アクティビティ破棄時の動作確認
        finish()
        assertNull(reference.value)
        assertEquals(testObject, destroyedObject)
    }

    @Test
    fun testManualReset() {
        // 準備
        val owner = createLifecycleOwner(Lifecycle.State.RESUMED)
        val testObject = "テストオブジェクト"
        var destroyedObject: String? = null

        // LifecycleReferenceの作成
        val reference = LifecycleReference(
            testObject,
            owner
        ) { destroyedObject = it }

        // 初期状態の確認
        assertEquals(testObject, reference.value)
        assertNull(destroyedObject)

        // 手動リセット
        reference.reset()
        assertNull(reference.value)
        assertNull(destroyedObject)      // reset()では、destroyedCallback()は呼ばれない。

        // ライフサイクル破棄の影響を受けないことを確認（既にresetされているため）
        val lifecycle = owner.lifecycle as LifecycleRegistry
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        assertNull(reference.value)
        assertNull(destroyedObject)      // reset()では、destroyedCallback()は呼ばれない。
    }

    @Test
    fun testWithNullValue() {
        // 準備
        val owner = createLifecycleOwner(Lifecycle.State.RESUMED)
        var callbackInvoked = false

        // null値でLifecycleReferenceを作成
        val reference = LifecycleReference<String>(
            null,
            owner
        ) {
            callbackInvoked = true
        }

        // 初期状態の確認
        assertNull(reference.value)
        assertFalse(callbackInvoked)

        // ライフサイクル破棄
        val lifecycle = owner.lifecycle as LifecycleRegistry
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)

        // コールバックは呼ばれていないこと（値がnullだったため）
        assertFalse(callbackInvoked)
    }

    @Test
    fun testWithoutDestroyCallback() {
        // 準備
        val owner = createLifecycleOwner(Lifecycle.State.RESUMED)
        val testObject = "テストデータ"

        // コールバックなしでLifecycleReferenceを作成
        val reference = LifecycleReference(testObject, owner)

        // 初期状態の確認
        assertEquals(testObject, reference.value)

        // ライフサイクル破棄（例外が発生しないことを確認）
        val lifecycle = owner.lifecycle as LifecycleRegistry
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)

        // 値がクリアされていること
        assertNull(reference.value)
    }

    @Test
    fun testWithComplexObject() {
        // 準備
        data class TestData(val id: Int, val name: String)

        val activity = createActivity()
        val testData = TestData(1, "テストデータ")
        var destroyedData: TestData? = null

        // 複雑なオブジェクトでLifecycleReferenceを作成
        val reference = LifecycleReference(
            testData,
            activity
        ) { destroyedData = it }

        // 初期状態の確認
        assertEquals(testData, reference.value)
        assertNull(destroyedData)

        // アクティビティ破棄
        finish()
        assertNull(reference.value)
        assertEquals(testData, destroyedData)
        assertEquals(1, destroyedData?.id)
        assertEquals("テストデータ", destroyedData?.name)
    }
}