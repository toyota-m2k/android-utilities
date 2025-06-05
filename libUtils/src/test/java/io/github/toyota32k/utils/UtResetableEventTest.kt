package io.github.toyota32k.utils

import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class UtResetableEventTest {

    @Test
    fun testInitialState() {
        // シグナル状態で初期化
        val signaledEvent = UtResetableEvent(true, false)
        assertTrue(signaledEvent.waitOne(0))

        // 非シグナル状態で初期化
        val nonSignaledEvent = UtResetableEvent(false, false)
        assertFalse(nonSignaledEvent.waitOne(0))
    }

    @Test
    fun testManualResetEvent() {
        val event = UtResetableEvent(false, false)  // 手動リセット
        assertFalse(event.waitOne(0))  // 初期状態は非シグナル

        // シグナル状態に設定
        event.set()
        assertTrue(event.waitOne(0))

        // 手動リセットの場合はwaitOne後もシグナル状態が維持される
        assertTrue(event.waitOne(0))

        // リセットするとシグナル状態が解除される
        event.reset()
        assertFalse(event.waitOne(0))
    }

    @Test
    fun testAutoResetEvent() {
        val event = UtResetableEvent(false, true)  // 自動リセット
        assertFalse(event.waitOne(0))  // 初期状態は非シグナル

        // シグナル状態に設定
        event.set()
        assertTrue(event.waitOne(0))

        // 自動リセットの場合はwaitOne後に自動的にシグナル状態が解除される
        assertFalse(event.waitOne(0))

        // 再度シグナル状態に設定
        event.set()
        assertTrue(event.waitOne(0))
        assertFalse(event.waitOne(0))
    }

    @Test
    fun testWaitOneBlocking() {
        val event = UtResetableEvent(false, true)
        val threadFinished = AtomicBoolean(false)
        val thread = Thread {
            event.waitOne()  // ブロッキング待機
            threadFinished.set(true)
        }

        thread.start()
        Thread.sleep(100)  // スレッドが確実にwaitOneに到達するのを待つ

        // スレッドはまだブロックされているはず
        assertFalse(threadFinished.get())

        // シグナルを設定してスレッドを解放
        event.set()
        thread.join(1000)

        // スレッドが完了したことを確認
        assertTrue(threadFinished.get())
    }

    @Test
    fun testWaitOneTimeout() {
        val event = UtResetableEvent(false, false)

        // タイムアウト
        val start = System.currentTimeMillis()
        assertFalse(event.waitOne(500))
        val duration = System.currentTimeMillis() - start

        // タイムアウトが約500msであることを確認（多少の誤差を許容）
        assertTrue(duration >= 490 && duration < 1000)
    }

    @Test
    fun testMultipleThreadsManualReset() {
        val threadCount = 5
        val event = UtResetableEvent(false, false)  // 手動リセット
        val latch = CountDownLatch(1)
        val counter = AtomicInteger(0)

        // 複数のスレッドがイベントを待機
        val threads = List(threadCount) {
            Thread {
                try {
                    latch.await()  // 全スレッドが同時にwaitOneを呼び出せるように同期
                    event.waitOne(5000)
                    counter.incrementAndGet()
                } catch (e: InterruptedException) {
                    // 無視
                }
            }.apply { start() }
        }

        // スレッドをスタート
        latch.countDown()
        Thread.sleep(100)  // スレッドがwaitOneに到達するのを待つ

        // イベントをシグナル状態にする
        event.set()

        // 全スレッドの終了を待つ
        threads.forEach { it.join(1000) }

        // 全スレッドがシグナルを受け取ったことを確認
        assertEquals(threadCount, counter.get())
    }

    @Test
    fun testWithLock() {
        val event = UtResetableEvent(true, false)
        val result = event.withLock {
            42
        }
        assertEquals(42, result)

        // 非シグナル状態では実行されない（ブロックされる）
        val nonSignaledEvent = UtResetableEvent(false, false)
        val executed = AtomicBoolean(false)

        val thread = Thread {
            nonSignaledEvent.withLock {
                executed.set(true)
            }
        }

        thread.start()
        Thread.sleep(100)
        assertFalse(executed.get())  // まだブロックされているはず

        // シグナルを設定して実行させる
        nonSignaledEvent.set()
        thread.join(1000)
        assertTrue(executed.get())  // 実行された
    }
}