package cn.xybbz.api.serializers

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * API 自定义序列化器测试。
 *
 * 覆盖服务端返回布尔字符串、布尔字面量、空时间和 ISO 时间字符串等兼容路径。
 */
class ApiSerializersTest {

    /**
     * 测试 BooleanStringSerializer 的宿主数据结构。
     */
    @Serializable
    private data class BooleanPayload(
        // 服务端可能返回 true/false 或字符串形式的 true/false。
        @Serializable(BooleanStringSerializer::class)
        val enabled: Boolean,
    )

    /**
     * 测试 LocalDateTimeTimestampSerializer 的宿主数据结构。
     */
    @Serializable
    private data class TimestampPayload(
        // 服务端 ISO 时间字符串在业务内统一转成毫秒时间戳。
        @Serializable(LocalDateTimeTimestampSerializer::class)
        val createdAt: Long,
    )

    /**
     * 布尔序列化器应兼容布尔字面量和大小写不同的字符串。
     */
    @Test
    fun booleanStringSerializerReadsBooleanAndStringValues() {
        assertEquals(true, Json.decodeFromString<BooleanPayload>("""{"enabled":true}""").enabled)
        assertEquals(false, Json.decodeFromString<BooleanPayload>("""{"enabled":" false "}""").enabled)
        assertEquals("""{"enabled":true}""", Json.encodeToString(BooleanPayload(enabled = true)))
    }

    /**
     * 非布尔文本应直接失败，避免脏数据被静默解析成错误权限。
     */
    @Test
    fun booleanStringSerializerRejectsInvalidText() {
        assertFailsWith<IllegalStateException> {
            Json.decodeFromString<BooleanPayload>("""{"enabled":"yes"}""")
        }
    }

    /**
     * 时间戳序列化器应把 ISO 时间字符串转成毫秒，并把空值兜底为 0。
     */
    @Test
    fun timestampSerializerReadsIsoTextAndNullValue() {
        assertEquals(
            1_609_459_200_000L,
            Json.decodeFromString<TimestampPayload>("""{"createdAt":"2021-01-01T00:00:00Z"}""").createdAt,
        )
        assertEquals(
            0L,
            Json.decodeFromString<TimestampPayload>("""{"createdAt":null}""").createdAt,
        )
    }

    /**
     * 时间戳序列化器输出时应保留毫秒数，方便本地缓存再序列化。
     */
    @Test
    fun timestampSerializerWritesMilliseconds() {
        assertEquals(
            """{"createdAt":1234}""",
            Json.encodeToString(TimestampPayload(createdAt = 1234L)),
        )
    }
}
