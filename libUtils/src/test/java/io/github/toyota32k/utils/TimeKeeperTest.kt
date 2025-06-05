package io.github.toyota32k.utils

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

@ExperimentalCoroutinesApi
class TimeKeeperTest {

    private lateinit var testDispatcher: TestDispatcher
    private lateinit var scheduler: TestCoroutineScheduler
    private lateinit var timeKeeper: TimeKeeper
    private var testTime: Long = 1000000L  // 初期値を設定（単純な0より分かりやすい値に）
    private lateinit var timeProvider: () -> Long

    @Before
    fun setup() {
        scheduler = TestCoroutineScheduler()
        testDispatcher = StandardTestDispatcher(scheduler)
        testTime = 1000000L // テスト前にリセット
        timeProvider = { testTime }
        timeKeeper = TimeKeeper(testDispatcher, "test", timeProvider)
    }

    @After
    fun tearDown() {
    }

    @Test
    fun testTimeout() = runTest(testDispatcher) {
        var timeoutTriggered = false

        // 1秒のタイムアウト設定
        timeKeeper.start(1000) {
            timeoutTriggered = true
            false  // repeat しない
        }

        // タイムアウト前はフラグが立っていない
        testTime += 500
        advanceTimeBy(500)
        runCurrent()
        assertFalse(timeoutTriggered)

        // タイムアウト後はフラグが立つ
        testTime += 600  // 合計1.1秒経過
        advanceTimeBy(600)
        runCurrent()
        assertTrue(timeoutTriggered)
    }

    @Test
    fun testRepeatTimeout() = runTest(testDispatcher) {
        var timeoutCount = 0

        // 500ミリ秒のタイムアウトを繰り返す
        timeKeeper.start(500, repeat = true) {
            timeoutCount++
            timeoutCount < 3  // 3回実行したら終了
        }

        // 1回目のタイムアウト
        testTime += 600
        advanceTimeBy(600)
        runCurrent()
        assertEquals(1, timeoutCount)

        // 2回目のタイムアウト
        testTime += 500
        advanceTimeBy(500)
        runCurrent()
        assertEquals(2, timeoutCount)

        // 3回目のタイムアウト（falseを返すのでこれで終了）
        testTime += 500
        advanceTimeBy(500)
        runCurrent()
        assertEquals(3, timeoutCount)

        // もう増えない
        testTime += 1000
        advanceTimeBy(1000)
        runCurrent()
        assertEquals(3, timeoutCount)
    }

    @Test
    fun testPauseResume() = runTest(testDispatcher) {
        var timeoutTriggered = false

        // pause状態で開始
        timeKeeper.start(500, pause = true) {
            timeoutTriggered = true
            false
        }

        // pause中は時間が経過してもタイムアウトしない
        testTime += 1000
        advanceTimeBy(1000)
        runCurrent()
        assertFalse(timeoutTriggered)

        // resumeするとタイマーがリセットされて、そこから500ms後にタイムアウト
        timeKeeper.resume()
        testTime += 400
        advanceTimeBy(400)
        runCurrent()
        assertFalse(timeoutTriggered)

        testTime += 200  // 合計600ms
        advanceTimeBy(200)
        runCurrent()
        assertTrue(timeoutTriggered)
    }

    @Test
    fun testStop() = runTest(testDispatcher) {
        var timeoutTriggered = false

        timeKeeper.start(500) {
            timeoutTriggered = true
            false
        }

        // 途中で停止
        testTime += 300
        advanceTimeBy(300)
        runCurrent()
        timeKeeper.stop()

        // タイムアウト時間を過ぎても実行されない
        testTime += 1000
        advanceTimeBy(1000)
        runCurrent()
        assertFalse(timeoutTriggered)
    }

    @Test
    fun testTouch() = runTest(testDispatcher) {
        var timeoutTriggered = false

        timeKeeper.start(500) {
            timeoutTriggered = true
            false
        }

        // 400ms経過時点でtouchを呼ぶと、そこからさらに500ms必要
        testTime += 400
        advanceTimeBy(400)
        runCurrent()
        timeKeeper.touch()

        // 400 + 400 = 800msでは、まだタイムアウトしない
        testTime += 400
        advanceTimeBy(400)
        runCurrent()
        assertFalse(timeoutTriggered)

        // さらに200ms進めるとタイムアウト (合計1000ms)
        testTime += 200
        advanceTimeBy(200)
        runCurrent()
        assertTrue(timeoutTriggered)
    }

    @Test
    fun testWithTimeout() = runTest(testDispatcher) {
        var executed = false
        var timeoutTriggered = false

        // pause=trueで開始
        timeKeeper.start(500, pause = true) {
            timeoutTriggered = true
            false
        }

        // withTimeoutブロックの中は実行される
        val result = timeKeeper.withTimeout {
            executed = true
            testTime += 300  // ブロック内で時間が進んでも
            advanceTimeBy(300)
            runCurrent()
            "result"
        }

        assertEquals("result", result)
        assertTrue(executed)
        assertFalse(timeoutTriggered)  // まだタイムアウトしていない

        // withTimeoutを抜けると自動的にpauseされる
        testTime += 1000
        advanceTimeBy(1000)
        runCurrent()
        assertFalse(timeoutTriggered)  // pause中なのでタイムアウトしない
    }

    @Test
    fun testDurationBasedStart() = runTest(testDispatcher) {
        var timeoutTriggered = false

        // Durationベースのstart
        timeKeeper.start(1.seconds) {
            timeoutTriggered = true
            false
        }

        testTime += 900
        advanceTimeBy(900)
        runCurrent()
        assertFalse(timeoutTriggered)

        testTime += 200
        advanceTimeBy(200)
        runCurrent()
        assertTrue(timeoutTriggered)
    }

    @Test
    fun testNegativeTimeout() = runTest(testDispatcher) {
        var timeoutTriggered = false

        // 負のタイムアウト値の場合は何も起きない
        timeKeeper.start(-1) {
            timeoutTriggered = true
            false
        }

        testTime += 5000
        advanceTimeBy(5000)
        runCurrent()
        assertFalse(timeoutTriggered)
    }
}