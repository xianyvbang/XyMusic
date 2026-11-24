package cn.xybbz.config.image

import cn.xybbz.api.TokenServer
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

/**
 * 封面地址解析测试。
 *
 * 重点验证登录态参数不会进入封面 URL：Sketch 的三层缓存 key 都从 URI 派生，
 * 一旦 URL 带上每次登录都会变化的 u/t/s/v/c/f，封面就会在每次登录后重复从远程下载。
 */
class CoverImageResolverTest {

    @AfterTest
    fun tearDown() {
        // 清理全局认证快照，避免单例状态污染其他用例。
        resetTokenServer()
    }

    /**
     * 相对封面地址只拼接服务地址，不追加当前登录态的认证参数。
     */
    @Test
    fun normalizeCoverUrlDoesNotAppendAuthenticatedQuery() {
        TokenServer.setAuthenticatedRequestData(
            token = "Bearer access-token",
            queryMap = fullSubsonicQueryMap(token = "md5-token", salt = "random-salt"),
            headerMap = emptyMap()
        )

        val url = "/rest/getCoverArt?id=al-1".normalizeCoverUrl("https://music.example.com")

        assertEquals("https://music.example.com/rest/getCoverArt?id=al-1", url)
    }

    /**
     * 两次登录拿到不同的盐与 token 时，同一张封面的地址必须保持一致，缓存 key 才不会失效。
     */
    @Test
    fun normalizeCoverUrlKeepsSameKeyAcrossLogins() {
        val pic = "/rest/getCoverArt?id=mf-2"

        TokenServer.setAuthenticatedRequestData(
            token = "Bearer access-token-1",
            queryMap = fullSubsonicQueryMap(token = "md5-token-1", salt = "salt-1"),
            headerMap = emptyMap()
        )
        val firstUrl = pic.normalizeCoverUrl("http://192.168.1.2:4533")

        TokenServer.setAuthenticatedRequestData(
            token = "Bearer access-token-2",
            queryMap = fullSubsonicQueryMap(token = "md5-token-2", salt = "salt-2"),
            headerMap = emptyMap()
        )
        val secondUrl = pic.normalizeCoverUrl("http://192.168.1.2:4533")

        assertEquals(firstUrl, secondUrl)
        assertFalse(secondUrl.orEmpty().contains("md5-token"))
        assertFalse(secondUrl.orEmpty().contains("salt-"))
    }

    /**
     * 绝对封面地址应原样返回，不会因为当前登录态被追加参数。
     */
    @Test
    fun normalizeCoverUrlKeepsAbsoluteUrlUntouched() {
        TokenServer.setAuthenticatedRequestData(
            token = "Bearer access-token",
            queryMap = fullSubsonicQueryMap(token = "md5-token", salt = "random-salt"),
            headerMap = emptyMap()
        )

        val url = "https://image.example.com/cover/1.jpg".normalizeCoverUrl("https://music.example.com")

        assertEquals("https://image.example.com/cover/1.jpg", url)
    }

    /**
     * 空白地址返回 null，保持占位图逻辑不变。
     */
    @Test
    fun normalizeCoverUrlReturnsNullForBlankValue() {
        assertNull("   ".normalizeCoverUrl("https://music.example.com"))
        assertNull(null.normalizeCoverUrl("https://music.example.com"))
    }

    /**
     * 构造一组完整的 Subsonic/Navidrome 认证参数，便于验证它们不会进入封面 URL。
     */
    private fun fullSubsonicQueryMap(token: String, salt: String): Map<String, String> {
        return mapOf(
            "u" to "demo",
            "t" to token,
            "s" to salt,
            "v" to "1.16.1",
            "c" to "XyMusic",
            "f" to "json"
        )
    }

    /**
     * 重置 TokenServer，保证用例之间互不影响。
     */
    private fun resetTokenServer() {
        TokenServer.clearAllData()
        TokenServer.updateBaseUrl("")
    }
}
