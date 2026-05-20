package cn.xybbz.api.client

import cn.xybbz.localdata.enums.DataSourceType
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlaybackHlsDetectionTest {

    @Test
    fun jellyfinStaticPlaybackIsNotHls() {
        assertFalse(
            resolvePlaybackIfHls(
                static = true,
                musicUrl = "/Audio/song-1/stream?deviceId=device&static=true",
                dataSourceType = DataSourceType.JELLYFIN,
            )
        )
    }

    @Test
    fun jellyfinTranscodingPlaybackIsHls() {
        assertTrue(
            resolvePlaybackIfHls(
                static = false,
                musicUrl = "/Audio/song-1/universal?deviceId=device&transcodingProtocol=hls",
                dataSourceType = DataSourceType.JELLYFIN,
            )
        )
    }

    @Test
    fun navidromePlaybackIsNotHlsWithoutHlsUrlSignal() {
        assertFalse(
            resolvePlaybackIfHls(
                static = false,
                musicUrl = "/rest/stream?id=song-1&format=mp3&estimateContentLength=true",
                dataSourceType = DataSourceType.NAVIDROME,
            )
        )
    }

    @Test
    fun m3u8UrlIsHlsEvenForNonHlsDatasource() {
        assertTrue(
            resolvePlaybackIfHls(
                static = false,
                musicUrl = "https://example.test/audio/master.m3u8",
                dataSourceType = DataSourceType.NAVIDROME,
            )
        )
    }
}
