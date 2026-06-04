package cn.xybbz.api

import cn.xybbz.api.constants.ApiConstants
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TokenServerTest {

    @AfterTest
    fun tearDown() {
        TokenServer.clearAllData()
        TokenServer.updateTokenHeaderName(ApiConstants.AUTHORIZATION)
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
}
