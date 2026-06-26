package cn.xybbz.database.converter

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * JSON 字符串列表转换器测试。
 */
class JsonStringListTypeConverterTest {

    /**
     * 转换器测试对象。
     */
    private val converter = JsonStringListTypeConverter()

    /**
     * 含逗号的艺术家名称写入后应作为单个列表项读回。
     */
    @Test
    fun roundTripArtistNameWithComma() {
        val artists = listOf("Earth, Wind & Fire")

        val stored = converter.listToString(artists)

        assertEquals("""["Earth, Wind & Fire"]""", stored)
        assertEquals(artists, converter.stringToList(stored))
    }

    /**
     * 普通多艺术家名称应保持列表顺序和数量。
     */
    @Test
    fun roundTripMultipleArtistNames() {
        val artists = listOf("A", "B")

        val stored = converter.listToString(artists)

        assertEquals("""["A","B"]""", stored)
        assertEquals(artists, converter.stringToList(stored))
    }

    /**
     * 旧版逗号分隔文本应继续按历史格式读取。
     */
    @Test
    fun readLegacyCommaSeparatedText() {
        assertEquals(listOf("A", "B"), converter.stringToList("A,B"))
    }

    /**
     * 空值和空白文本应按计划返回空结果。
     */
    @Test
    fun readNullAndBlankText() {
        assertNull(converter.stringToList(null))
        assertEquals(emptyList(), converter.stringToList(""))
        assertEquals(emptyList(), converter.stringToList("   "))
    }
}
