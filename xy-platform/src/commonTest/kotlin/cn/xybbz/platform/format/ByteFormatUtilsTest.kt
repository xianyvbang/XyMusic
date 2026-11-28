package cn.xybbz.platform.format

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 容量格式化工具测试。
 *
 * 覆盖零值、Byte、KB 和 MB 等换算分支。
 */
class ByteFormatUtilsTest {

    /**
     * 字节格式化应正确处理零值、Byte、KB 和 MB 分支。
     */
    @Test
    fun formatBytesUsesExpectedUnitAndPrecision() {
        assertEquals("0B", formatBytes(0))
        assertEquals("0 B", formatBytes(0, withSpace = true))
        assertEquals("512B", formatBytes(512))
        assertEquals("1.5 KB", formatBytes(1536, withSpace = true))
        assertEquals("1.0MB", formatBytes(1024L * 1024L))
    }
}
