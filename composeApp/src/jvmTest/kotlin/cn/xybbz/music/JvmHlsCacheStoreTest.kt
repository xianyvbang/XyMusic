package cn.xybbz.music

import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class JvmHlsCacheStoreTest {

    @Test
    fun committedResourceCanBeReadAsCached() {
        val root = createTempDirectory()
        val store = JvmHlsCacheStore(root, "application/octet-stream")
        val entry = store.openEntry(
            cacheKey = "hls:song-1",
            safeCacheKey = "hls-song-1",
            playlistUrl = "https://example.test/master.m3u8",
        )

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
        tempFile.writeBytes(byteArrayOf(1, 2, 3))

        store.commitResource(
            entry = entry,
            resourceUrl = "https://example.test/seg-1.ts",
            kind = HlsResourceKind.SEGMENT,
            cacheable = true,
            contentType = "video/mp2t",
            tempFile = tempFile,
        )

        val resource = store.cachedResource(entry, "https://example.test/seg-1.ts")
        assertNotNull(resource)
        assertEquals(3, resource.contentLength)
        assertEquals("video/mp2t", resource.contentType)
        assertEquals(HlsCacheProgress(cached = 1, total = 1), store.progress(entry))
    }

    @Test
    fun tempResourceDoesNotBecomeCacheHitBeforeCommit() {
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

    private fun createTempDirectory(): File {
        return Files.createTempDirectory("xy-hls-cache-test").toFile()
    }
}
