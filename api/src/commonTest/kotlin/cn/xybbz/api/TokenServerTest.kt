package cn.xybbz.api

import cn.xybbz.api.constants.ApiConstants
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TokenServerTest {

    @BeforeTest
    fun setUp() {
        resetTokenServer()
    }

    @AfterTest
    fun tearDown() {
        resetTokenServer()
    }

    /**
     * 重置 TokenServer，避免单例状态污染其他测试。
     */
    private fun resetTokenServer() {
        TokenServer.clearAllData()
        TokenServer.updateTokenHeaderName(ApiConstants.AUTHORIZATION)
        TokenServer.updateBaseUrl("")
    }

    @Test
    fun navidromeEmptyAuthParamsDoNotMarkRequestsReady() {
        TokenServer.setAuthenticatedRequestData(
            token = "Bearer null",
            queryMap = mapOf(
                "u" to "",
                "t" to "",
                "s" to "",
                "v" to "",
                "c" to "",
                "f" to "json"
            ),
            headerMap = mapOf(ApiConstants.NAVIDROME_HEADER to "")
        )

        assertFalse(TokenServer.authenticatedRequestStateFlow.value.ready)
        assertEquals(mapOf("f" to "json"), TokenServer.queryMap)
        assertEquals("", TokenServer.token)
        assertEquals(emptyMap(), TokenServer.headerMap)
    }

    @Test
    fun completeSubsonicQueryMarksRequestsReady() {
        val queryMap = mapOf(
            "u" to "demo",
            "t" to "token",
            "s" to "salt",
            "v" to "1.16.1",
            "c" to "XyMusic",
            "f" to "json"
        )

        TokenServer.setAuthenticatedRequestData(
            token = "",
            queryMap = queryMap,
            headerMap = emptyMap()
        )

        assertTrue(TokenServer.authenticatedRequestStateFlow.value.ready)
        assertEquals(queryMap, TokenServer.queryMap)
    }

    @Test
    fun mediaBrowserDeviceHeaderIsNotReadyUntilTokenIsPresent() {
        val deviceOnlyHeader = """MediaBrowser Client="XyMusic", Version="1", DeviceId="device""""
        val authenticatedHeader = """MediaBrowser Client="XyMusic", Version="1", Token="token""""

        TokenServer.setAuthenticatedRequestData(
            token = deviceOnlyHeader,
            queryMap = emptyMap(),
            headerMap = emptyMap()
        )
        assertFalse(TokenServer.authenticatedRequestStateFlow.value.ready)
        assertEquals("", TokenServer.token)

        TokenServer.setAuthenticatedRequestData(
            token = authenticatedHeader,
            queryMap = emptyMap(),
            headerMap = emptyMap()
        )
        assertTrue(TokenServer.authenticatedRequestStateFlow.value.ready)
        assertEquals(authenticatedHeader, TokenServer.token)
    }

    /**
     * 批量写入认证参数时会发布一份完整快照。
     */
    @Test
    fun authenticatedRequestDataPublishesTokenQueryAndHeaderAsSingleSnapshot() {
        val queryMap = completeSubsonicQueryMap()
        val headerMap = mapOf(ApiConstants.AUTHORIZATION to "Bearer header-token")

        TokenServer.setAuthenticatedRequestData(
            token = "Bearer token",
            queryMap = queryMap,
            headerMap = headerMap
        )

        val snapshot = TokenServer.authenticatedRequestData
        assertEquals("Bearer token", snapshot.token)
        assertEquals(queryMap, snapshot.queryMap)
        assertEquals(headerMap, snapshot.headerMap)
        assertEquals(ApiConstants.AUTHORIZATION, snapshot.tokenHeaderName)
        assertEquals(TokenServer.token, snapshot.token)
        assertEquals(TokenServer.queryMap, snapshot.queryMap)
        assertEquals(TokenServer.headerMap, snapshot.headerMap)
    }

    /**
     * 单字段更新只替换目标字段，其余认证快照字段保持不变。
     */
    @Test
    fun individualUpdatesPreserveOtherSnapshotFields() {
        val queryMap = completeSubsonicQueryMap()
        val headerMap = mapOf(ApiConstants.AUTHORIZATION to "Bearer header-token")
        TokenServer.setAuthenticatedRequestData(
            token = "Bearer first-token",
            queryMap = queryMap,
            headerMap = headerMap
        )
        TokenServer.updateTokenHeaderName(ApiConstants.EMBY_AUTHORIZATION)
        TokenServer.updateBaseUrl("http://demo.server")

        TokenServer.setTokenData("Bearer second-token")

        val snapshot = TokenServer.authenticatedRequestData
        assertEquals("Bearer second-token", snapshot.token)
        assertEquals(queryMap, snapshot.queryMap)
        assertEquals(headerMap, snapshot.headerMap)
        assertEquals(ApiConstants.EMBY_AUTHORIZATION, snapshot.tokenHeaderName)
        assertEquals("http://demo.server", snapshot.baseUrl)
    }

    /**
     * 清空连接态只清理认证值，保留请求元信息。
     */
    @Test
    fun clearAllDataClearsAuthValuesButKeepsRequestMetadata() {
        TokenServer.setAuthenticatedRequestData(
            token = "Bearer token",
            queryMap = completeSubsonicQueryMap(),
            headerMap = mapOf(ApiConstants.AUTHORIZATION to "Bearer header-token")
        )
        TokenServer.updateTokenHeaderName(ApiConstants.EMBY_AUTHORIZATION)
        TokenServer.updateBaseUrl("http://demo.server")

        TokenServer.clearAllData()

        val snapshot = TokenServer.authenticatedRequestData
        assertFalse(TokenServer.authenticatedRequestStateFlow.value.ready)
        assertEquals("", snapshot.token)
        assertEquals(emptyMap(), snapshot.queryMap)
        assertEquals(emptyMap(), snapshot.headerMap)
        assertEquals(ApiConstants.EMBY_AUTHORIZATION, snapshot.tokenHeaderName)
        assertEquals("http://demo.server", snapshot.baseUrl)
    }

    /**
     * 服务地址变化会通过认证状态版本号通知订阅方刷新。
     */
    @Test
    fun updateBaseUrlIncrementsAuthStateVersion() {
        val startVersion = TokenServer.authenticatedRequestStateFlow.value.version

        TokenServer.updateBaseUrl("http://demo.server")

        assertEquals("http://demo.server", TokenServer.authenticatedRequestData.baseUrl)
        assertEquals(startVersion + 1, TokenServer.authenticatedRequestStateFlow.value.version)
    }

    /**
     * 构造完整的 Subsonic 公共查询参数。
     */
    private fun completeSubsonicQueryMap(): Map<String, String> {
        return mapOf(
            "u" to "demo",
            "t" to "token",
            "s" to "salt",
            "v" to "1.16.1",
            "c" to "XyMusic",
            "f" to "json"
        )
    }
}
