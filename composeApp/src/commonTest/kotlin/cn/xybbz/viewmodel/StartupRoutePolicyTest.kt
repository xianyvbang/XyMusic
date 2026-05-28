package cn.xybbz.viewmodel

import cn.xybbz.AppStartupContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StartupRoutePolicyTest {

    @Test
    fun settingsNotLoadedRoutesToStartup() {
        // 设置尚未加载时不能提前判断首开状态，保持启动页。
        val decision = resolveStartupContent(
            settingsLoaded = false,
            ifEntryPage = false,
            readyForContent = false,
            hasShownMainContent = false
        )

        assertEquals(AppStartupContent.STARTUP, decision.content)
        assertFalse(decision.hasShownMainContent)
    }

    @Test
    fun noConnectionRoutesToConnectionAndResetsMainLatch() {
        // 无连接配置时进入连接页，并重置主壳已展示标记。
        val decision = resolveStartupContent(
            settingsLoaded = true,
            ifEntryPage = false,
            readyForContent = false,
            hasShownMainContent = true
        )

        assertEquals(AppStartupContent.CONNECTION, decision.content)
        assertFalse(decision.hasShownMainContent)
    }

    @Test
    fun configuredAppWaitsOnStartupUntilStartLoadingIsComplete() {
        // 有连接配置但 start() 轻量加载未完成时，继续停留启动页。
        val decision = resolveStartupContent(
            settingsLoaded = true,
            ifEntryPage = true,
            readyForContent = false,
            hasShownMainContent = false
        )

        assertEquals(AppStartupContent.STARTUP, decision.content)
        assertFalse(decision.hasShownMainContent)
    }

    @Test
    fun completedStartupLoadingEntersMainAndLatchesIt() {
        // start() 轻量加载完成后才进入主壳，并锁存主壳已展示状态。
        val decision = resolveStartupContent(
            settingsLoaded = true,
            ifEntryPage = true,
            readyForContent = true,
            hasShownMainContent = false
        )

        assertEquals(AppStartupContent.MAIN, decision.content)
        assertTrue(decision.hasShownMainContent)
    }

    @Test
    fun refreshLoginAfterMainHasShownStaysInMain() {
        // 已进入主壳后，刷新登录的短暂 loading 不应把外层页面切回 STARTUP。
        val decision = resolveStartupContent(
            settingsLoaded = true,
            ifEntryPage = true,
            readyForContent = false,
            hasShownMainContent = true
        )

        assertEquals(AppStartupContent.MAIN, decision.content)
        assertTrue(decision.hasShownMainContent)
    }
}
