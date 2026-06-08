package cn.xybbz.music

import cn.xybbz.api.constants.ApiConstants
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * HLS playlist 重写器测试。
 *
 * 重点覆盖需要代理的资源类型，以及不适合完整缓存的 BYTERANGE 分片。
 */
class JvmHlsPlaylistRewriterTest {

    @Test
    fun rewritesSegmentsKeysMapsAndChildPlaylists() {
        // 构造同时包含 key、map、子 playlist、相对分片和绝对分片的 playlist。
        val playlist = """
            #EXTM3U
            #EXT-X-KEY:METHOD=AES-128,URI="keys/key.bin"
            #EXT-X-MAP:URI="init.mp4"
            #EXT-X-STREAM-INF:BANDWIDTH=128000
            child/audio.m3u8
            #EXTINF:10,
            seg-1.ts
            #EXTINF:10,
            ${ApiConstants.HTTPS}cdn.example.test/seg-2.ts
        """.trimIndent()

        val result = JvmHlsPlaylistRewriter.rewrite(
            playlistText = playlist,
            playlistUrl = "/Audio/song/master.m3u8?token=abc",
        ) { resource ->
            // 测试中用 local:// 模拟真实本地代理地址，方便断言类型和缓存标记。
            "local://${resource.kind.routeValue}?cache=${resource.cacheable}&url=${resource.url}"
        }

        // 确认所有可代理资源都被替换成本地地址，并且相对路径已经正确解析。
        assertTrue("local://key?cache=true&url=/Audio/song/keys/key.bin" in result.text)
        assertTrue("local://map?cache=true&url=/Audio/song/init.mp4" in result.text)
        assertTrue("local://playlist?cache=false&url=/Audio/song/child/audio.m3u8" in result.text)
        assertTrue("local://segment?cache=true&url=/Audio/song/seg-1.ts" in result.text)
        assertTrue("local://segment?cache=true&url=${ApiConstants.HTTPS}cdn.example.test/seg-2.ts" in result.text)

        assertEquals(
            // 资源清单顺序要和 playlist 中出现的顺序一致，后台预取依赖这个顺序。
            listOf(
                HlsResourceKind.KEY,
                HlsResourceKind.MAP,
                HlsResourceKind.PLAYLIST,
                HlsResourceKind.SEGMENT,
                HlsResourceKind.SEGMENT,
            ),
            result.resources.map { resource -> resource.kind },
        )
    }

    @Test
    fun byteRangeSegmentIsRewrittenButNotCacheable() {
        // BYTERANGE 表示只请求文件的一段，代理可以保留，但不能按完整文件缓存。
        val playlist = """
            #EXTM3U
            #EXT-X-BYTERANGE:100@0
            file.ts
        """.trimIndent()

        val result = JvmHlsPlaylistRewriter.rewrite(
            playlistText = playlist,
            playlistUrl = "/hls/playlist.m3u8",
        ) { resource ->
            // cache=false 是本用例的核心断言。
            "local://${resource.kind.routeValue}?cache=${resource.cacheable}&url=${resource.url}"
        }

        assertTrue("local://segment?cache=false&url=/hls/file.ts" in result.text)
        assertFalse(result.resources.single().cacheable)
    }
}
