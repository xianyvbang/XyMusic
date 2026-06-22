package cn.xybbz.common.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * 通用业务工具测试。
 *
 * 覆盖地址解析、字节展示和列表分页计算等无需平台环境的业务分支。
 */
class CommonUtilsTest {

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

    /**
     * 地址端口解析应支持普通域名、带认证信息地址和 IPv6 方括号地址。
     */
    @Test
    fun extractPortOrNullReadsSupportedAddressForms() {
        assertEquals(4533, "https://demo.test:4533/music?id=1".extractPortOrNull())
        assertEquals(8080, "http://user:pass@demo.test:8080/path".extractPortOrNull())
        assertEquals(9000, "http://[::1]:9000".extractPortOrNull())
    }

    /**
     * 地址端口解析遇到空地址、缺失端口、非法端口或越界端口时应返回空。
     */
    @Test
    fun extractPortOrNullRejectsInvalidAddressForms() {
        assertNull("   ".extractPortOrNull())
        assertNull("https://demo.test/music".extractPortOrNull())
        assertNull("https://demo.test:abc".extractPortOrNull())
        assertNull("https://demo.test:65536".extractPortOrNull())
        assertNull("http://[::1".extractPortOrNull())
    }

    /**
     * 音乐列表页码计算应把当前页内索引归到第一页，超过页大小后进入下一页。
     */
    @Test
    fun getPageNumUsesCurrentPageBoundary() {
        assertEquals(0, MusicListIndexUtils.getPageNum(index = 20, pageSize = 20))
        assertEquals(1, MusicListIndexUtils.getPageNum(index = 21, pageSize = 20))
        assertEquals(2, MusicListIndexUtils.getPageNum(index = 40, pageSize = 20))
    }
}
