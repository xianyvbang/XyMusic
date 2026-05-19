package cn.xybbz.music

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import java.io.File
import java.nio.file.Files

/**
 * JVM span 播放缓存存储层测试。
 *
 * 这些测试只验证本地 index/span 行为，不依赖 VLC 或真实网络，方便快速发现缓存区间计算问题。
 */
class JvmPlaybackCacheStoreTest {

    /**
     * 提交相邻 span 后应当形成完整覆盖。
     */
    @Test
    fun commitAdjacentSpansMergesCoverage() {
        val root = createTempDirectory()
        val store = JvmPlaybackCacheStore(root, "application/octet-stream")
        val entry = store.openEntry(
            cacheKey = "song-1",
            safeCacheKey = "song-1",
            sourceUrl = "https://example.test/song.mp3",
            totalBytes = 10,
        )

        store.commitSpan(entry, 0, 4, tempSpan(root, 5))
        store.commitSpan(entry, 5, 9, tempSpan(root, 5))

        val coverage = store.mergedCoverageForTest(entry)
        assertEquals(1, coverage.size)
        assertEquals(0, coverage.first().start)
        assertEquals(9, coverage.first().endInclusive)
        assertEquals(10, store.cachedBytes(entry))
        assertTrue(store.isComplete(entry))
    }

    /**
     * seek 写入远端 span 后，缓存统计必须去重，且未覆盖中间缺口时不能误判完整。
     */
    @Test
    fun separatedSpansKeepGapAndCountUniqueBytes() {
        val root = createTempDirectory()
        val store = JvmPlaybackCacheStore(root, "application/octet-stream")
        val entry = store.openEntry(
            cacheKey = "song-2",
            safeCacheKey = "song-2",
            sourceUrl = "https://example.test/song.flac",
            totalBytes = 20,
        )

        store.commitSpan(entry, 0, 4, tempSpan(root, 5))
        store.commitSpan(entry, 10, 14, tempSpan(root, 5))

        val coverage = store.mergedCoverageForTest(entry)
        assertEquals(2, coverage.size)
        assertEquals(10, store.cachedBytes(entry))
        assertFalse(store.isComplete(entry))
        assertEquals(5, store.firstMissingPositionFromStart(entry))
        assertEquals(10, store.nextSpanStartAfter(entry, 5))
    }

    /**
     * 部分重叠的新 span 不能移除旧 span，否则旧 span 两侧仍有效的缓存会丢失。
     */
    @Test
    fun partialOverlapKeepsOldSpanCoverage() {
        val root = createTempDirectory()
        val store = JvmPlaybackCacheStore(root, "application/octet-stream")
        val entry = store.openEntry(
            cacheKey = "song-overlap",
            safeCacheKey = "song-overlap",
            sourceUrl = "https://example.test/song-overlap.mp3",
            totalBytes = 20,
        )

        store.commitSpan(entry, 0, 9, tempSpan(root, 10))
        store.commitSpan(entry, 5, 14, tempSpan(root, 10))

        val coverage = store.mergedCoverageForTest(entry)
        assertEquals(1, coverage.size)
        assertEquals(0, coverage.first().start)
        assertEquals(14, coverage.first().endInclusive)
        assertEquals(15, store.cachedBytes(entry))
    }

    /**
     * 损坏的 index.json 只能影响当前歌曲目录，重新打开时应创建干净索引。
     */
    @Test
    fun corruptIndexIsDiscardedForCurrentEntry() {
        val root = createTempDirectory()
        val songDirectory = File(root, "song-3").apply { mkdirs() }
        File(songDirectory, "index.json").writeText("{broken")
        File(songDirectory, "span-old.cache").writeText("stale")

        val store = JvmPlaybackCacheStore(root, "application/octet-stream")
        val entry = store.openEntry(
            cacheKey = "song-3",
            safeCacheKey = "song-3",
            sourceUrl = "https://example.test/song.aac",
            totalBytes = 8,
        )

        assertEquals(0, store.cachedBytes(entry))
        assertTrue(File(songDirectory, "index.json").exists())
        assertFalse(File(songDirectory, "span-old.cache").exists())
    }

    /**
     * 创建测试临时目录。
     *
     * @return 独立的临时目录。
     */
    private fun createTempDirectory(): File {
        return Files.createTempDirectory("xy-playback-cache-test").toFile()
    }

    /**
     * 创建指定长度的临时 span 文件。
     *
     * @param root 临时文件所在目录。
     * @param length 文件长度，单位是 byte。
     * @return 已写入测试数据的临时文件。
     */
    private fun tempSpan(
        root: File,
        length: Int,
    ): File {
        return File.createTempFile("span-test", ".tmp", root).apply {
            writeBytes(ByteArray(length) { index -> index.toByte() })
        }
    }
}
