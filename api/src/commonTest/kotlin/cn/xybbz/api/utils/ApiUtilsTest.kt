package cn.xybbz.api.utils

import cn.xybbz.api.constants.ApiConstants
import io.ktor.http.Headers
import io.ktor.http.HeadersBuilder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * API 通用工具测试。
 *
 * 覆盖认证参数构建、请求头透传、URL 协议补齐和 JSON 参数扁平化等高频业务路径。
 */
class ApiUtilsTest {

    /**
     * 鉴权参数值应清理首尾空白和换行，保持服务端 Authorization 参数格式稳定。
     */
    @Test
    fun buildParameterTrimsAndNormalizesValue() {
        val parameter = ParameterUtils.buildParameter("Device", "  mobile\nclient  ")

        assertEquals("""Device="mobile client"""", parameter)
    }

    /**
     * 鉴权参数 key 包含分隔符或引号边界时应直接拒绝，避免拼出无法解析的认证头。
     */
    @Test
    fun buildParameterRejectsInvalidKeys() {
        assertFailsWith<IllegalArgumentException> {
            ParameterUtils.buildParameter("Bad=Key", "value")
        }
        assertFailsWith<IllegalArgumentException> {
            ParameterUtils.buildParameter("Bad,Key", "value")
        }
        assertFailsWith<IllegalArgumentException> {
            ParameterUtils.buildParameter("\"Bad", "value")
        }
        assertFailsWith<IllegalArgumentException> {
            ParameterUtils.buildParameter("Bad\"", "value")
        }
    }

    /**
     * 普通请求应追加当前 token，空 token 不应写入空鉴权头。
     */
    @Test
    fun appendCustomRequestHeadersOnlyAddsNonBlankToken() {
        val headersWithToken = HeadersBuilder().apply {
            appendCustomRequestHeaders(
                sourceHeaders = Headers.Empty,
                token = "Bearer demo-token",
                tokenHeaderName = ApiConstants.AUTHORIZATION,
            )
        }.build()
        val headersWithoutToken = HeadersBuilder().apply {
            appendCustomRequestHeaders(
                sourceHeaders = Headers.Empty,
                token = "",
                tokenHeaderName = ApiConstants.AUTHORIZATION,
            )
        }.build()

        assertEquals("Bearer demo-token", headersWithToken[ApiConstants.AUTHORIZATION])
        assertFalse(headersWithoutToken.contains(ApiConstants.AUTHORIZATION))
    }

    /**
     * 自定义图片请求应透传原始 Authorization，保证受保护封面图仍能被加载。
     */
    @Test
    fun appendCustomRequestHeadersForwardsAuthorizationForCustomImages() {
        val sourceHeaders = Headers.build {
            append(ApiConstants.CUSTOM_IMAGE_HEADER_NAME, "1")
            append(ApiConstants.AUTHORIZATION, "Bearer image-token")
        }

        val result = HeadersBuilder().apply {
            appendCustomRequestHeaders(
                sourceHeaders = sourceHeaders,
                token = "",
                tokenHeaderName = ApiConstants.AUTHORIZATION,
            )
        }.build()

        assertEquals("Bearer image-token", result[ApiConstants.AUTHORIZATION])
    }

    /**
     * 缺少协议头的地址应补齐 HTTP，已有协议的地址应保持不变。
     */
    @Test
    fun withDefaultHttpSchemeKeepsExistingSchemeAndAddsMissingScheme() {
        assertEquals("http://demo.test", "demo.test".withDefaultHttpScheme())
        assertEquals("https://demo.test", "https://demo.test".withDefaultHttpScheme())
    }

    /**
     * JSON 转查询参数时应过滤空值，并按配置决定数组是展开还是合并。
     */
    @Test
    fun convertToPairsFiltersEmptyValuesAndSupportsJoinedLists() {
        val jsonObject = JsonObject(
            mapOf(
                "name" to JsonPrimitive("demo"),
                "empty" to JsonPrimitive(""),
                "ids" to JsonArray(
                    listOf(
                        JsonPrimitive("1"),
                        JsonPrimitive(""),
                        JsonPrimitive("2"),
                    )
                ),
                "metadata" to JsonObject(mapOf("library" to JsonPrimitive("main"))),
            )
        )

        val expandedPairs = jsonObject.convertToPairs(isConvertList = false)
        val joinedPairs = jsonObject.convertToPairs(isConvertList = true)

        assertTrue("name" to "demo" in expandedPairs)
        assertTrue("ids" to "1" in expandedPairs)
        assertTrue("ids" to "2" in expandedPairs)
        assertFalse(expandedPairs.any { it.first == "empty" })
        assertTrue("ids" to "1,2" in joinedPairs)
        assertTrue(joinedPairs.any { it.first == "metadata" && it.second.contains("library") })
    }
}
