package cn.xybbz.ui.popup

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class XyPopTipManagerTest {

    @AfterTest
    fun tearDown() {
        XyPopTipManager.dismissCurrent()
    }

    @Test
    fun negativeOneDurationStaysVisibleUntilReplaced() = runBlocking {
        XyPopTipManager.show(
            text = "persistent",
            durationMillis = -1L
        )

        delay(100)

        val persistentTip = XyPopTipManager.currentTip.value
        assertNotNull(persistentTip)
        assertEquals("persistent", persistentTip.text)

        XyPopTipManager.show(
            text = "replacement",
            durationMillis = -1L
        )

        val replacementTip = XyPopTipManager.currentTip.value
        assertNotNull(replacementTip)
        assertEquals("replacement", replacementTip.text)
    }
}
