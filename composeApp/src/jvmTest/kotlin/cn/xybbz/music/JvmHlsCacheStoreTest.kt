package cn.xybbz.music

import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * JVM HLS 分片缓存存储层测试。
 *
 * 重点验证“临时文件完整提交后才算缓存命中”的核心约束。
 */
class JvmHlsCacheStoreTest {

    @Test
    fun committedResourceCanBeReadAsCached() {
        // 每个用例使用独立临时目录，避免缓存索引互相影响。
        val root = createTempDirectory()
        val store = JvmHlsCacheStore(root, "application/octet-stream")
        val entry = store.openEntry(
            cacheKey = "hls:song-1",
            safeCacheKey = "hls-song-1",
            playlistUrl = "https://example.test/master.m3u8",
        )

        // 先把 playlist 中发现的媒体分片登记到索引，此时资源还没有缓存完成。
        store.updateResources(
            entry = entry,
            playlistUrl = "https://example.test/master.m3u8",
            resources = listOf(
                HlsPlaylistResource(
                    url = "https://example.test/seg-1.ts",
                    kind = HlsResourceKind.SEGMENT,
                    cacheable = true,
                )
            ),
        )
        val tempFile = store.createTempResourceFile(entry, "https://example.test/seg-1.ts")
        // 模拟上游分片已经完整下载到临时文件。
        tempFile.writeBytes(byteArrayOf(1, 2, 3))

        // 提交后临时文件会变成正式缓存文件，并更新 index.json。
        store.commitResource(
            entry = entry,
            resourceUrl = "https://example.test/seg-1.ts",
            kind = HlsResourceKind.SEGMENT,
            cacheable = true,
            contentType = "video/mp2t",
            tempFile = tempFile,
        )

        // 缓存命中必须能读到内容长度、响应类型和进度状态。
        val resource = store.cachedResource(entry, "https://example.test/seg-1.ts")
        assertNotNull(resource)
        assertEquals(3, resource.contentLength)
        assertEquals("video/mp2t", resource.contentType)
        assertEquals(HlsCacheProgress(cached = 1, total = 1), store.progress(entry))
    }

    @Test
    fun tempResourceDoesNotBecomeCacheHitBeforeCommit() {
        // 临时文件存在不等于缓存完成，只有 commitResource 后才能进入缓存索引。
        val root = createTempDirectory()
        val store = JvmHlsCacheStore(root, "application/octet-stream")
        val entry = store.openEntry(
            cacheKey = "hls:song-2",
            safeCacheKey = "hls-song-2",
            playlistUrl = "https://example.test/master.m3u8",
        )

        val tempFile = store.createTempResourceFile(entry, "https://example.test/seg-1.ts")
        tempFile.writeBytes(byteArrayOf(1, 2, 3))

        assertNull(store.cachedResource(entry, "https://example.test/seg-1.ts"))
    }

    @Test
    fun progressCountsOnlyCacheableNonPlaylistResources() {
        // HLS 缓存进度只统计可缓存媒体分片，子 playlist 和不可缓存分片不计入 total。
        val root = createTempDirectory()
        val store = JvmHlsCacheStore(root, "application/octet-stream")
        val entry = store.openEntry(
            cacheKey = "hls:song-3",
            safeCacheKey = "hls-song-3",
            playlistUrl = "https://example.test/master.m3u8",
        )

        store.updateResources(
            entry = entry,
            playlistUrl = "https://example.test/master.m3u8",
            resources = listOf(
                HlsPlaylistResource(
                    url = "https://example.test/child.m3u8",
                    kind = HlsResourceKind.PLAYLIST,
                    cacheable = false,
                ),
                HlsPlaylistResource(
                    url = "https://example.test/seg-1.ts",
                    kind = HlsResourceKind.SEGMENT,
                    cacheable = true,
                ),
                HlsPlaylistResource(
                    url = "https://example.test/seg-2.ts",
                    kind = HlsResourceKind.SEGMENT,
                    cacheable = false,
                ),
            ),
        )

        assertEquals(HlsCacheProgress(cached = 0, total = 1), store.progress(entry))
    }

    /**
     * 创建测试专用临时缓存目录。
     */
    private fun createTempDirectory(): File {
        return Files.createTempDirectory("xy-hls-cache-test").toFile()
    }
}
