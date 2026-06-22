package cn.xybbz.api.events

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 重新登录事件总线测试。
 *
 * 覆盖未授权事件的发送与订阅，保证 401 触发后的业务通知链路可用。
 */
class ReLoginEventBusTest {

    /**
     * 订阅方应能收到已发送的未授权事件。
     */
    @Test
    fun notifyEmitsUnauthorizedEventToCollector() = runBlocking {
        val eventBus = ReLoginEventBus()

        val receivedEvent = coroutineScope {
            val deferredEvent = async(start = CoroutineStart.UNDISPATCHED) {
                eventBus.events.first()
            }

            eventBus.notify(ReLoginEvent.Unauthorized)

            deferredEvent.await()
        }

        assertEquals(ReLoginEvent.Unauthorized, receivedEvent)
    }
}
