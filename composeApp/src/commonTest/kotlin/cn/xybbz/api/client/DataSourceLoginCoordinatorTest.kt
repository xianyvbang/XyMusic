package cn.xybbz.api.client

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * 数据源登录并发协调器测试。
 */
class DataSourceLoginCoordinatorTest {
    /**
     * 手动登录应按调用顺序串行执行。
     */
    @Test
    fun runManualLoginSerializesCalls() {
        runBlocking {
            val coordinator = DataSourceLoginCoordinator()
            val firstStarted = CompletableDeferred<Unit>()
            val allowFirstFinish = CompletableDeferred<Unit>()
            val events = mutableListOf<String>()

            val firstJob = launch(start = CoroutineStart.UNDISPATCHED) {
                coordinator.runManualLogin {
                    events.add("first-start")
                    firstStarted.complete(Unit)
                    allowFirstFinish.await()
                    events.add("first-end")
                }
            }
            firstStarted.await()

            val secondJob = launch(start = CoroutineStart.UNDISPATCHED) {
                coordinator.runManualLogin {
                    events.add("second")
                }
            }
            delay(10)
            assertEquals(listOf("first-start"), events)

            allowFirstFinish.complete(Unit)
            firstJob.join()
            secondJob.join()

            assertEquals(listOf("first-start", "first-end", "second"), events)
        }
    }

    /**
     * 自动重登录任务并发触发时只允许第一个进入。
     */
    @Test
    fun tryLaunchAutomaticReloginSkipsConcurrentAutomaticJob() {
        runBlocking {
            val coordinator = DataSourceLoginCoordinator()
            val started = CompletableDeferred<Unit>()
            val allowFinish = CompletableDeferred<Unit>()
            var loginCount = 0

            val firstJob = coordinator.tryLaunchAutomaticRelogin(this) {
                loginCount++
                started.complete(Unit)
                allowFinish.await()
            }
            started.await()

            val secondJob = coordinator.tryLaunchAutomaticRelogin(this) {
                loginCount++
            }

            assertNull(secondJob)
            assertEquals(1, loginCount)

            allowFinish.complete(Unit)
            firstJob?.join()
        }
    }

    /**
     * 手动登录执行中自动重登录应直接跳过，不排队补跑。
     */
    @Test
    fun tryLaunchAutomaticReloginSkipsWhenManualLoginRunning() {
        runBlocking {
            val coordinator = DataSourceLoginCoordinator()
            val manualStarted = CompletableDeferred<Unit>()
            val allowManualFinish = CompletableDeferred<Unit>()
            var automaticCount = 0

            val manualJob = launch(start = CoroutineStart.UNDISPATCHED) {
                coordinator.runManualLogin {
                    manualStarted.complete(Unit)
                    allowManualFinish.await()
                }
            }
            manualStarted.await()

            val automaticJob = coordinator.tryLaunchAutomaticRelogin(this) {
                automaticCount++
            }

            automaticJob?.join()
            assertEquals(0, automaticCount)

            allowManualFinish.complete(Unit)
            manualJob.join()
        }
    }
}
