package cn.xybbz.proxy

import cn.xybbz.music.CacheSessionSnapshot
import cn.xybbz.music.CacheStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * JVM 本地代理播放缓存响应测试。
 */
class JvmReverseProxyServerTest {

    @Test
    fun wrappedProxyUrlUsesLoopbackHostAndAccessToken() {
        val proxyUrl = JvmReverseProxyServer.wrapTargetUrl("https://example.test/song.mp3")
        val accessToken = extractAccessToken(proxyUrl)

        assertTrue(proxyUrl.startsWith("http://127.0.0.1:19180/proxy?"))
        assertTrue(accessToken.isNotBlank())
        assertTrue(JvmReverseProxyServer.isValidAccessToken(accessToken))
    }

    @Test
    fun cacheAndHlsPlaybackUrlsCarryAccessToken() {
        val cacheUrl = JvmReverseProxyServer.wrapCachePlaybackUrl(sessionId = 123L)
        val hlsUrl = JvmReverseProxyServer.wrapHlsResourceUrl(
            sessionId = 123L,
            resourceUrl = "https://example.test/segment.ts",
            type = "segment",
            cacheable = true,
        )

        assertTrue(JvmReverseProxyServer.isValidAccessToken(extractAccessToken(cacheUrl)))
        assertTrue(JvmReverseProxyServer.isValidAccessToken(extractAccessToken(hlsUrl)))
    }

    @Test
    fun rejectsMissingOrInvalidAccessToken() {
        assertFalse(JvmReverseProxyServer.isValidAccessToken(null))
        assertFalse(JvmReverseProxyServer.isValidAccessToken(""))
        assertFalse(JvmReverseProxyServer.isValidAccessToken("invalid-token"))
    }

    @Test
    fun incompleteCacheLimitsResponseRangeToWarmWindow() {
        val responseRange = JvmReverseProxyServer.resolveCacheResponseRange(
            requestedRange = JvmReverseProxyServer.PlaybackRange(
                start = 0L,
                endInclusive = 10 * 1024 * 1024L - 1L,
                isPartial = false,
            ),
            snapshot = snapshot(
                totalBytes = 10 * 1024 * 1024L,
                status = CacheStatus.DOWNLOADING,
            ),
        )

        assertEquals(0L, responseRange.start)
        assertEquals(1024 * 1024L - 1L, responseRange.endInclusive)
        assertTrue(responseRange.isPartial)
    }

    @Test
    fun completedCacheKeepsRequestedRange() {
        val requestedRange = JvmReverseProxyServer.PlaybackRange(
            start = 2 * 1024 * 1024L,
            endInclusive = 4 * 1024 * 1024L - 1L,
            isPartial = true,
        )

        val responseRange = JvmReverseProxyServer.resolveCacheResponseRange(
            requestedRange = requestedRange,
            snapshot = snapshot(
                totalBytes = 10 * 1024 * 1024L,
                status = CacheStatus.COMPLETED,
            ),
        )

        assertEquals(requestedRange, responseRange)
    }

    private fun snapshot(
        totalBytes: Long,
        status: CacheStatus,
    ): CacheSessionSnapshot {
        return CacheSessionSnapshot(
            id = 1L,
            sourceUrl = "https://example.test/song.mp3",
            totalBytes = totalBytes,
            downloadedBytes = 0L,
            status = status,
            contentType = "audio/mpeg",
            rangeRequestsSupported = true,
        )
    }

    private fun extractAccessToken(url: String): String {
        return Regex("[?&]xy_proxy_token=([^&]+)")
            .find(url)
            ?.groupValues
            ?.get(1)
            .orEmpty()
    }
}
