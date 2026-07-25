package cn.xybbz.config.music

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 播放进度串行 worker 的并发与生命周期测试。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PlaybackProgressWorkerTest {

    /**
     * 重复启动同一会话时只应保留一个周期定时器。
     */
    @Test
    fun repeatedStartCreatesOnlyOneTicker() = runTest {
        val reports = mutableListOf<PlaybackProgressSnapshot>()
        val worker = PlaybackProgressWorker(
            parentScope = this,
            reportIntervalMillis = REPORT_INTERVAL_MILLIS,
            canContinue = { true },
            currentPosition = { 1_000L },
            reportProgress = { reports.add(it) },
            onReportFailure = {}
        )

        try {
            repeat(20) {
                worker.start(MUSIC_ID, PLAY_SESSION_ID)
            }
            runCurrent()
            advanceTimeBy(REPORT_INTERVAL_MILLIS)
            runCurrent()

            assertEquals(1, reports.size)
            assertEquals(1_000L, reports.single().positionMs)
        } finally {
            worker.close()
            runCurrent()
        }
    }

    /**
     * 在途请求执行期间的连续即时进度只应保留最新值。
     */
    @Test
    fun reportNowKeepsOnlyLatestPendingSnapshot() = runTest {
        val firstRequestStarted = CompletableDeferred<Unit>()
        val allowFirstRequestToFinish = CompletableDeferred<Unit>()
        val reportedPositions = mutableListOf<Long>()
        var inFlightCount = 0
        var maxInFlightCount = 0
        val worker = PlaybackProgressWorker(
            parentScope = this,
            reportIntervalMillis = REPORT_INTERVAL_MILLIS,
            canContinue = { true },
            currentPosition = { 1_000L },
            reportProgress = { snapshot ->
                inFlightCount += 1
                maxInFlightCount = maxOf(maxInFlightCount, inFlightCount)
                try {
                    reportedPositions.add(snapshot.positionMs)
                    if (reportedPositions.size == 1) {
                        firstRequestStarted.complete(Unit)
                        allowFirstRequestToFinish.await()
                    }
                } finally {
                    inFlightCount -= 1
                }
            },
            onReportFailure = {}
        )

        try {
            worker.start(MUSIC_ID, PLAY_SESSION_ID)
            runCurrent()
            advanceTimeBy(REPORT_INTERVAL_MILLIS)
            runCurrent()
            assertTrue(firstRequestStarted.isCompleted)

            worker.reportNow(MUSIC_ID, 2_000L)
            worker.reportNow(MUSIC_ID, 3_000L)
            worker.reportNow(MUSIC_ID, 4_000L)
            runCurrent()

            allowFirstRequestToFinish.complete(Unit)
            runCurrent()

            assertEquals(listOf(1_000L, 4_000L), reportedPositions)
            assertEquals(1, maxInFlightCount)
        } finally {
            worker.close()
            runCurrent()
        }
    }

    /**
     * 停止屏障应取消在途请求并阻止旧会话继续上报。
     */
    @Test
    fun stopAndJoinCancelsRequestAndPreventsFurtherReports() = runTest {
        val requestStarted = CompletableDeferred<Unit>()
        var reportCount = 0
        val worker = PlaybackProgressWorker(
            parentScope = this,
            reportIntervalMillis = REPORT_INTERVAL_MILLIS,
            canContinue = { true },
            currentPosition = { 1_000L },
            reportProgress = {
                reportCount += 1
                requestStarted.complete(Unit)
                awaitCancellation()
            },
            onReportFailure = {}
        )

        try {
            worker.start(MUSIC_ID, PLAY_SESSION_ID)
            runCurrent()
            advanceTimeBy(REPORT_INTERVAL_MILLIS)
            runCurrent()
            assertTrue(requestStarted.isCompleted)

            val stopJob = launch {
                worker.stopAndJoin()
            }
            runCurrent()

            assertTrue(stopJob.isCompleted)
            worker.reportNow(MUSIC_ID, 9_000L)
            advanceTimeBy(REPORT_INTERVAL_MILLIS * 2)
            runCurrent()
            assertEquals(1, reportCount)
        } finally {
            worker.close()
            runCurrent()
        }
    }

    /**
     * 新会话启动后应拒绝旧歌曲的即时进度和旧定时器事件。
     */
    @Test
    fun newSessionRejectsOldSessionSnapshots() = runTest {
        val reports = mutableListOf<PlaybackProgressSnapshot>()
        val worker = PlaybackProgressWorker(
            parentScope = this,
            reportIntervalMillis = REPORT_INTERVAL_MILLIS,
            canContinue = { true },
            currentPosition = { 5_000L },
            reportProgress = { reports.add(it) },
            onReportFailure = {}
        )

        try {
            worker.start(MUSIC_ID, PLAY_SESSION_ID)
            runCurrent()
            worker.stopAndJoin()
            worker.start(NEW_MUSIC_ID, NEW_PLAY_SESSION_ID)
            runCurrent()

            worker.reportNow(MUSIC_ID, 6_000L)
            worker.reportNow(NEW_MUSIC_ID, 7_000L)
            runCurrent()

            assertEquals(1, reports.size)
            assertEquals(NEW_MUSIC_ID, reports.single().musicId)
            assertEquals(7_000L, reports.single().positionMs)
        } finally {
            worker.close()
            runCurrent()
        }
    }

    /**
     * 会话条件失效或 worker 关闭后不应再产生远端请求。
     */
    @Test
    fun invalidOrClosedWorkerDoesNotReport() = runTest {
        var canContinue = true
        var reportCount = 0
        val worker = PlaybackProgressWorker(
            parentScope = this,
            reportIntervalMillis = REPORT_INTERVAL_MILLIS,
            canContinue = { canContinue },
            currentPosition = { 1_000L },
            reportProgress = { reportCount += 1 },
            onReportFailure = {}
        )

        worker.start(MUSIC_ID, PLAY_SESSION_ID)
        runCurrent()
        canContinue = false
        advanceTimeBy(REPORT_INTERVAL_MILLIS)
        runCurrent()
        assertEquals(0, reportCount)

        worker.close()
        runCurrent()
        worker.start(MUSIC_ID, PLAY_SESSION_ID)
        worker.reportNow(MUSIC_ID, 2_000L)
        advanceTimeBy(REPORT_INTERVAL_MILLIS)
        runCurrent()
        assertEquals(0, reportCount)
    }

    /**
     * 协程取消不应被当作普通上报失败处理。
     */
    @Test
    fun cancellationIsNotReportedAsFailure() = runTest {
        var failureCount = 0
        val worker = PlaybackProgressWorker(
            parentScope = this,
            reportIntervalMillis = REPORT_INTERVAL_MILLIS,
            canContinue = { true },
            currentPosition = { 1_000L },
            reportProgress = { throw CancellationException("test cancellation") },
            onReportFailure = { failureCount += 1 }
        )

        try {
            worker.start(MUSIC_ID, PLAY_SESSION_ID)
            runCurrent()
            advanceTimeBy(REPORT_INTERVAL_MILLIS)
            runCurrent()

            assertEquals(0, failureCount)
        } finally {
            worker.close()
            runCurrent()
        }
    }

    private companion object {
        /** 测试使用的固定周期上报间隔。 */
        const val REPORT_INTERVAL_MILLIS = 10_000L

        /** 测试使用的第一首歌曲 ID。 */
        const val MUSIC_ID = "music-1"

        /** 测试使用的第一条播放会话 ID。 */
        const val PLAY_SESSION_ID = "session-1"

        /** 测试使用的第二首歌曲 ID。 */
        const val NEW_MUSIC_ID = "music-2"

        /** 测试使用的第二条播放会话 ID。 */
        const val NEW_PLAY_SESSION_ID = "session-2"
    }
}
