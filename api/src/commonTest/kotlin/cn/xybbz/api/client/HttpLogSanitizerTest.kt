package cn.xybbz.api.client

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * 网络日志脱敏测试。
 */
class HttpLogSanitizerTest {
    /**
     * 认证头与查询参数都应被统一脱敏。
     */
    @Test
    fun sanitizeMasksSensitiveHeadersAndQueryParameters() {
        val rawMessage = buildString {
            append("Authorization: Bearer secret-token, ")
            append("X-Emby-Authorization=MediaBrowser Token=emby-secret, ")
            append("X-Plex-Token: plex-secret ")
            append("GET https://demo.example.com/music?id=1&token=query-secret&api_key=api-secret")
        }

        val sanitized = HttpLogSanitizer.sanitize(rawMessage)

        assertFalse(sanitized.contains("secret-token"))
        assertFalse(sanitized.contains("emby-secret"))
        assertFalse(sanitized.contains("plex-secret"))
        assertFalse(sanitized.contains("query-secret"))
        assertFalse(sanitized.contains("api-secret"))
        assertTrue(sanitized.contains("Authorization: ***"))
        assertTrue(sanitized.contains("token=***"))
        assertTrue(sanitized.contains("api_key=***"))
    }

    /**
     * URL 单独脱敏时也不能泄露常见 token 参数。
     */
    @Test
    fun sanitizeUrlMasksSensitiveQueryParameters() {
        val rawUrl = "https://demo.example.com/photo?X-Plex-Token=plex-secret&token=query-secret"

        val sanitized = HttpLogSanitizer.sanitizeUrl(rawUrl)

        assertFalse(sanitized.contains("plex-secret"))
        assertFalse(sanitized.contains("query-secret"))
        assertTrue(sanitized.contains("X-Plex-Token=***"))
        assertTrue(sanitized.contains("token=***"))
    }
}
