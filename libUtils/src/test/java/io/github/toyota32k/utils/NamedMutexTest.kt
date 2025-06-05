package io.github.toyota32k.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NamedMutexTest {
    private val testScope = CoroutineScope(Dispatchers.Default)

    @Before
    fun setUp() {
        // テスト前に念のためミューテックスマップをクリア
        synchronized(NamedMutex.mutexMap) {
            NamedMutex.mutexMap.clear()
        }
    }

    @After
    fun tearDown() {
        // テスト後にミューテックスマップをクリア
        synchronized(NamedMutex.mutexMap) {
            NamedMutex.mutexMap.clear()
        }
    }

    @Test
    fun testTryLock() {
        val name = "testMutex"

        // 初期状態ではロックされていない
        assertFalse(NamedMutex.isLocked(name))

        // ロックの取得
        assertTrue(NamedMutex.tryLock(name, this))

        // ロックされていることを確認
        assertTrue(NamedMutex.isLocked(name))

        // 同じオーナーでのロック再取得は失敗する
        assertFalse(NamedMutex.tryLock(name, this))

        // 別のオーナーでのロック取得も失敗する
        val otherOwner = Any()
        assertFalse(NamedMutex.tryLock(name, otherOwner))

        // ロック解除
        NamedMutex.unlock(name, this)

        // ロックが解除されていることを確認
        assertFalse(NamedMutex.isLocked(name))

        // 別のオーナーがロックできることを確認
        assertTrue(NamedMutex.tryLock(name, otherOwner))

        // 別のオーナーでロックを解除
        NamedMutex.unlock(name, otherOwner)
        assertFalse(NamedMutex.isLocked(name))
    }

    @Test
    fun testHoldsLock() {
        val name = "testHoldsLock"
        val owner1 = Any()
        val owner2 = Any()

        // 初期状態では誰もロックを保持していない
        assertFalse(NamedMutex.holdsLock(name, owner1))
        assertFalse(NamedMutex.holdsLock(name, owner2))

        // owner1でロック
        assertTrue(NamedMutex.tryLock(name, owner1))

        // ロック状態の確認
        assertTrue(NamedMutex.holdsLock(name, owner1))
        assertFalse(NamedMutex.holdsLock(name, owner2))

        // 解除
        NamedMutex.unlock(name, owner1)
        assertFalse(NamedMutex.holdsLock(name, owner1))
    }

    @Test
    fun testMutexCreation() {
        val name = "testCreation"

        // ミューテックスが作成される前は存在しない
        assertFalse(name in NamedMutex.mutexMap)

        // ロック試行でミューテックスが作成される
        NamedMutex.tryLock(name)

        // ミューテックスが作成されたことを確認
        assertTrue(name in NamedMutex.mutexMap)
        assertNotNull(NamedMutex.mutexMap[name])

        // ロック解除後もミューテックスは残る
        NamedMutex.unlock(name)
        assertTrue(name in NamedMutex.mutexMap)

        // ミューテックス削除
        NamedMutex.removeMutex(name)
        assertFalse(name in NamedMutex.mutexMap)
    }

    @Test
    fun testWithLockBasic() = runBlocking {
        val name = "testWithLock"
        var counter = 0

        // withLockを使ったロック内の処理実行
        val result = NamedMutex.withLock(name) {
            counter++
            "Result"
        }

        // 結果と副作用の確認
        assertEquals("Result", result)
        assertEquals(1, counter)
        assertFalse(NamedMutex.isLocked(name))  // withLock終了後はロック解除されている
    }

    @Test
    fun testWithLockTimeout() = runBlocking {
        val name = "testWithLockTimeout"
        val owner = Any()

        // 先にロックを取得
        assertTrue(NamedMutex.tryLock(name, owner))

        // タイムアウト付きのwithLockを試行
        try {
            NamedMutex.withLock(name, 100L) {
                fail("ここは実行されないはず")
                "unreachable"
            }
            fail("例外が発生するはず")
        } catch (e: TimeoutCancellationException) {
            // タイムアウト例外が正しく発生
        }

        // デフォルト値を指定したタイムアウト付きのwithLock
        val result = NamedMutex.withLockOrDefault(name, 100L, "Default") {
            fail("ここは実行されないはず")
            "unreachable"
        }

        // タイムアウトによりデフォルト値が返されることを確認
        assertEquals("Default", result)

        // 後始末
        NamedMutex.unlock(name, owner)
    }

    @Test
    fun testConcurrentAccess() = runBlocking {
        val name = "testConcurrent"
        val sharedCounter = intArrayOf(0)  // 複数スレッドから安全に操作するためintArrayを使用
        val jobs = mutableListOf<Job>()
        val concurrentTasks = 5
        val incrementsPerTask = 100

        // 複数のコルーチンから同時にインクリメントする
        for (i in 0 until concurrentTasks) {
            val job = launch {
                repeat(incrementsPerTask) {
                    NamedMutex.withLock(name) {
                        val current = sharedCounter[0]
                        delay(1)  // 競合を発生させるための小さな遅延
                        sharedCounter[0] = current + 1
                    }
                }
            }
            jobs.add(job)
        }

        // 全てのジョブが完了するのを待つ
        jobs.forEach { it.join() }

        // 競合がなければカウンタの値は正確であるはず
        assertEquals(concurrentTasks * incrementsPerTask, sharedCounter[0])

        // ミューテックスは解放されているはず
        assertFalse(NamedMutex.isLocked(name))
    }

    @Test
    fun testMultipleMutexes() = runBlocking {
        val names = listOf("mutex1", "mutex2", "mutex3")
        val results = mutableMapOf<String, String>()

        // 複数の異なる名前のミューテックスを同時に使用
        val deferred = names.map { name ->
            async {
                NamedMutex.withLock(name) {
                    delay(100)  // 少し時間のかかる処理
                    results[name] = "Processed $name"
                    name
                }
            }
        }

        // 全ての処理が完了するのを待つ
        deferred.forEach { it.await() }

        // 各ミューテックスで処理が行われたことを確認
        names.forEach { name ->
            assertEquals("Processed $name", results[name])
            assertFalse(NamedMutex.isLocked(name))
        }
    }

    @Test
    fun testNestedLocks() = runBlocking {
        // 異なる名前のミューテックスはネストして取得できる
        val result = NamedMutex.withLock("outer") {
            assertEquals(true, NamedMutex.isLocked("outer"))
            assertEquals(false, NamedMutex.isLocked("inner"))

            NamedMutex.withLock("inner") {
                assertEquals(true, NamedMutex.isLocked("outer"))
                assertEquals(true, NamedMutex.isLocked("inner"))
                "Nested result"
            }
        }

        assertEquals("Nested result", result)
        assertFalse(NamedMutex.isLocked("outer"))
        assertFalse(NamedMutex.isLocked("inner"))
    }

    private fun fail(message: String) {
        throw AssertionError(message)
    }
}