package io.github.toyota32k.utils

import androidx.appcompat.app.AppCompatActivity
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import io.github.toyota32k.utils.lifecycle.LifecycleDisposer
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
class LifecycleDisposerTest {
    @Rule
    @JvmField
    val instantExecutorRule : InstantTaskExecutorRule = InstantTaskExecutorRule()

    lateinit var activityController : ActivityController<JustTestActivity>

    fun createActivity(): AppCompatActivity {
        activityController = Robolectric.buildActivity(JustTestActivity::class.java)
        return activityController.create().start().get() as AppCompatActivity
    }
    fun finish() {
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

    class TestDisposable(override var disposed: Boolean=false) : IDisposableEx {
        override fun dispose() {
            disposed = true
        }
    }

    @Test
    fun lifecycleDisposerTest() {
        val activity = createActivity()
        val d1 = TestDisposable()
        val d2 = TestDisposable()

        val disposer = LifecycleDisposer(activity) + d1 + d2
        assertFalse(d1.disposed)
        assertFalse(d2.disposed)
        assertEquals(2, disposer.count)

        disposer.dispose()
        assertTrue(d1.disposed)
        assertTrue(d2.disposed)
        assertTrue(disposer.disposed)
        assertEquals(0, disposer.count)

        d1.disposed = false
        d2.disposed = false
        disposer + d1 + d2
        assertFalse(disposer.disposed)  // register()で disposedフラグはクリアされる
        assertEquals(2, disposer.count)
        assertFalse(d1.disposed)
        assertFalse(d2.disposed)

        finish()
        assertTrue(d1.disposed)
        assertTrue(d2.disposed)
        assertEquals(0, disposer.count)
    }

    @Test
    fun testLifecycleOwnerSetting() {
        // 初期状態でライフサイクル所有者なしでインスタンス化
        val disposer = LifecycleDisposer()
        assertNull(disposer.lifecycleOwner)

        // ライフサイクル所有者を設定
        val owner = createLifecycleOwner(Lifecycle.State.RESUMED)
        disposer.lifecycleOwner = owner
        assertEquals(owner, disposer.lifecycleOwner)

        // ライフサイクル所有者をnullに設定
        disposer.lifecycleOwner = null
        assertNull(disposer.lifecycleOwner)
    }

    @Test
    fun testLifecycleDestruction() {
        // モックされたライフサイクル所有者を使用
        val owner = createLifecycleOwner(Lifecycle.State.RESUMED)
        val lifecycle = owner.lifecycle as LifecycleRegistry

        val d1 = TestDisposable()
        val d2 = TestDisposable()

        val disposer = LifecycleDisposer(owner) + d1 + d2

        // 初期状態の確認
        assertFalse(d1.disposed)
        assertFalse(d2.disposed)
        assertEquals(2, disposer.count)

        // ライフサイクル破棄
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)

        // 破棄されたことを確認
        assertTrue(d1.disposed)
        assertTrue(d2.disposed)
        assertEquals(0, disposer.count)
    }

    @Test
    fun testDisposerResetAfterLifecycleDestruction() {
        val owner = createLifecycleOwner(Lifecycle.State.RESUMED)
        val lifecycle = owner.lifecycle as LifecycleRegistry

        val d1 = TestDisposable()

        val disposer = LifecycleDisposer(owner) + d1

        // ライフサイクル破棄
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)

        // 破棄後に新しいリソースを登録
        val d2 = TestDisposable()
        disposer + d2

        // 新しいリソースはまだ破棄されていない
        assertFalse(d2.disposed)
        assertEquals(1, disposer.count)

        // 手動でdispose
        disposer.dispose()

        // すべて破棄されている
        assertTrue(d2.disposed)
        assertEquals(0, disposer.count)
    }

    @Test
    fun testLifecycleOwnerReattachment() {
        // 最初のライフサイクル所有者
        val owner1 = createLifecycleOwner(Lifecycle.State.RESUMED)
        val lifecycle1 = owner1.lifecycle as LifecycleRegistry

        val disposer = LifecycleDisposer(owner1)
        val d1 = TestDisposable()
        disposer + d1

        // 2つ目のライフサイクル所有者に切り替え
        val owner2 = createLifecycleOwner(Lifecycle.State.RESUMED)
        val lifecycle2 = owner2.lifecycle as LifecycleRegistry

        disposer.lifecycleOwner = owner2

        // 最初のライフサイクル所有者を破棄しても影響なし
        lifecycle1.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        assertFalse(d1.disposed)
        assertEquals(1, disposer.count)

        // 新しいライフサイクル所有者の破棄で解放される
        lifecycle2.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        assertTrue(d1.disposed)
        assertEquals(0, disposer.count)
    }
}