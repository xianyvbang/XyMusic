package cn.xybbz.viewmodel

import cn.xybbz.AppStartupContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StartupRoutePolicyTest {

    @Test
    fun noConnectionRoutesToConnectionAndResetsMainLatch() {
        val decision = resolveStartupContent(
            ifEntryPage = false,
            readyForContent = false,
            hasShownMainContent = true
        )

        assertEquals(AppStartupContent.CONNECTION, decision.content)
        assertFalse(decision.hasShownMainContent)
    }

    @Test
    fun configuredAppWaitsOnStartupUntilDataSourceServiceIsPublished() {
        val decision = resolveStartupContent(
            ifEntryPage = true,
            readyForContent = false,
            hasShownMainContent = false
        )

        assertEquals(AppStartupContent.STARTUP, decision.content)
        assertFalse(decision.hasShownMainContent)
    }

    @Test
    fun publishedDataSourceServiceEntersMainAndLatchesIt() {
        val decision = resolveStartupContent(
            ifEntryPage = true,
            readyForContent = true,
            hasShownMainContent = false
        )

        assertEquals(AppStartupContent.MAIN, decision.content)
        assertTrue(decision.hasShownMainContent)
    }

    @Test
    fun refreshLoginAfterMainHasShownStaysInMain() {
        val decision = resolveStartupContent(
            ifEntryPage = true,
            readyForContent = false,
            hasShownMainContent = true
        )

        assertEquals(AppStartupContent.MAIN, decision.content)
        assertTrue(decision.hasShownMainContent)
    }
}
