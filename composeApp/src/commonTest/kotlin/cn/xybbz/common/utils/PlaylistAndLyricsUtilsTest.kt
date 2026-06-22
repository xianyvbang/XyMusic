package cn.xybbz.common.utils

import cn.xybbz.common.utils.LrcUtils.getIndex
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * 播放列表与歌词工具测试。
 *
 * 覆盖导入导出、歌词时间轴、双语歌词合并和版本比较等播放器周边业务路径。
 */
class PlaylistAndLyricsUtilsTest {

    /**
     * M3U 歌单解析应读取 EXTINF 时长、标题信息和紧随其后的资源路径。
     */
    @Test
    fun parseM3UReadsMetadataAndPath() {
        val tracks = PlaylistParser.parseM3U(
            listOf(
                "#EXTM3U",
                "#EXTINF:215,Artist - Song",
                "https://demo.test/song.flac",
            )
        )

        assertEquals(1, tracks.size)
        assertEquals(215, tracks.single().duration)
        assertEquals("Artist", tracks.single().title)
        assertEquals("Artist", tracks.single().artist)
        assertEquals("https://demo.test/song.flac", tracks.single().path)
    }

    /**
     * TXT 歌单解析应忽略空行，并按“歌手-标题”拆分显示信息。
     */
    @Test
    fun parseTxtIgnoresBlankLinesAndSplitsArtistTitle() {
        val tracks = PlaylistParser.parseTxt(
            listOf(
                "Artist - Song",
                "   ",
            )
        )

        assertEquals(1, tracks.size)
        assertEquals("Artist ", tracks.single().artist)
        assertEquals(" Song", tracks.single().title)
    }

    /**
     * M3U 和 TXT 导出应保持固定格式，方便导入导出回归验证。
     */
    @Test
    fun exportPlaylistsUsesExpectedFormat() {
        val track = PlaylistParser.Track(
            title = "Song",
            duration = 120,
            path = "/music/song.mp3",
        )

        assertEquals(
            "#EXTM3U\n#EXTINF:120,Song\n/music/song.mp3\n",
            PlaylistParser.exportM3U(listOf(track)),
        )
        assertEquals("Song", PlaylistParser.exportTxt(listOf(track)))
    }

    /**
     * 歌词解析应支持多时间标签排序，并自动设置每行结束时间。
     */
    @Test
    fun parseLrcSortsEntriesAndComputesEndTimes() {
        val entries = LrcUtils.parseLrc(
            """
            [00:20.00]second
            [00:10.50][00:30.000]first
            """.trimIndent()
        )

        assertEquals(listOf(10_500L, 20_000L, 30_000L), entries.map { it.startTime })
        assertEquals(20_000L, entries[0].endTime)
        assertEquals(Long.MAX_VALUE, entries.last().endTime)
        assertEquals("00:10", entries.first().startTimeFormat)
    }

    /**
     * 双语歌词应按开始时间合并翻译文本，并让显示文本包含两行。
     */
    @Test
    fun parseBilingualLrcMergesSecondTextByStartTime() {
        val entries = LrcUtils.parseLrc(
            arrayOf(
                "[00:01.00]hello",
                "[00:01.00]你好",
            )
        )

        assertEquals("你好", entries?.single()?.secondText)
        assertEquals("hello\n你好", entries?.single()?.displayText)
        assertNull(LrcUtils.parseLrc(arrayOf("", "[00:01.00]empty")))
    }

    /**
     * 歌词索引应按当前播放进度和偏移量定位当前行。
     */
    @Test
    fun getIndexUsesProgressAndOffset() {
        val entries = LrcUtils.parseLrc(
            """
            [00:01.00]one
            [00:03.00]two
            """.trimIndent()
        )

        assertEquals(0, entries.getIndex(progress = 1_500L, offsetMs = 0L))
        assertEquals(1, entries.getIndex(progress = 2_500L, offsetMs = -500L))
        assertEquals(-1, entries.getIndex(progress = 500L, offsetMs = 0L))
    }

    /**
     * 版本比较应支持 v 前缀、缺失段补 0 和当前版本高于远端版本的情况。
     */
    @Test
    fun isLatestVersionComparesSemanticParts() {
        assertTrue(GitHubVersionVersionUtils.isLatestVersion("v1.2", "1.2.0"))
        assertTrue(GitHubVersionVersionUtils.isLatestVersion("1.3.0", "1.2.9"))
        assertEquals(false, GitHubVersionVersionUtils.isLatestVersion("1.2.0", "1.2.1"))
    }

    /**
     * 英文字母判断应只接受 ASCII 大小写字母。
     */
    @Test
    fun isEnglishLetterAcceptsOnlyAsciiLetters() {
        assertTrue(CharUtils.isEnglishLetter('A'))
        assertTrue(CharUtils.isEnglishLetter('z'))
        assertEquals(false, CharUtils.isEnglishLetter('中'))
        assertEquals(false, CharUtils.isEnglishLetter('1'))
    }
}
