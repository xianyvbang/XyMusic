package cn.xybbz.music

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JvmHlsPlaylistRewriterTest {

    @Test
    fun rewritesSegmentsKeysMapsAndChildPlaylists() {
        val playlist = """
            #EXTM3U
            #EXT-X-KEY:METHOD=AES-128,URI="keys/key.bin"
            #EXT-X-MAP:URI="init.mp4"
            #EXT-X-STREAM-INF:BANDWIDTH=128000
            child/audio.m3u8
            #EXTINF:10,
            seg-1.ts
            #EXTINF:10,
            https://cdn.example.test/seg-2.ts
        """.trimIndent()

        val result = JvmHlsPlaylistRewriter.rewrite(
            playlistText = playlist,
            playlistUrl = "/Audio/song/master.m3u8?token=abc",
        ) { resource ->
            "local://${resource.kind.routeValue}?cache=${resource.cacheable}&url=${resource.url}"
        }

        assertTrue("local://key?cache=true&url=/Audio/song/keys/key.bin" in result.text)
        assertTrue("local://map?cache=true&url=/Audio/song/init.mp4" in result.text)
        assertTrue("local://playlist?cache=false&url=/Audio/song/child/audio.m3u8" in result.text)
        assertTrue("local://segment?cache=true&url=/Audio/song/seg-1.ts" in result.text)
        assertTrue("local://segment?cache=true&url=https://cdn.example.test/seg-2.ts" in result.text)

        assertEquals(
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
        val playlist = """
            #EXTM3U
            #EXT-X-BYTERANGE:100@0
            file.ts
        """.trimIndent()

        val result = JvmHlsPlaylistRewriter.rewrite(
            playlistText = playlist,
            playlistUrl = "/hls/playlist.m3u8",
        ) { resource ->
            "local://${resource.kind.routeValue}?cache=${resource.cacheable}&url=${resource.url}"
        }

        assertTrue("local://segment?cache=false&url=/hls/file.ts" in result.text)
        assertFalse(result.resources.single().cacheable)
    }
}
