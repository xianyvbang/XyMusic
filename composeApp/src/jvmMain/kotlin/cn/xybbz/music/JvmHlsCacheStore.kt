package cn.xybbz.music

import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.writeFully
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.RandomAccessFile
import java.security.MessageDigest

/**
 * JVM HLS 播放缓存存储层。
 *
 * HLS 缓存以分片为最小提交单位：只有完整写入的分片才会进入 index.json，
 * 避免播放器后续请求命中半文件。
 */
internal class JvmHlsCacheStore(
    private val rootDirectory: File,
    private val defaultContentType: String,
) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    fun openEntry(
        cacheKey: String,
        safeCacheKey: String,
        playlistUrl: String,
    ): JvmHlsCacheEntry {
        rootDirectory.mkdirs()
        val directory = File(rootDirectory, "hls-$safeCacheKey").normalizedDirectory()
        directory.mkdirs()
        val indexFile = File(directory, INDEX_FILE_NAME)
        val loadedIndex = loadIndexOrNull(indexFile)

        val indexMatchesRequest = loadedIndex != null &&
                loadedIndex.cacheKey == cacheKey &&
                loadedIndex.playlistUrl == playlistUrl

        val index = if (indexMatchesRequest) {
            sanitizeIndex(directory, requireNotNull(loadedIndex))
        } else {
            if (indexFile.exists()) {
                deleteDirectoryChildren(directory)
            }
            HlsCacheIndex(
                cacheKey = cacheKey,
                playlistUrl = playlistUrl,
                playlistContentType = DEFAULT_PLAYLIST_CONTENT_TYPE,
                resources = emptyList(),
                lastAccessTime = System.currentTimeMillis(),
            )
        }

        val entry = JvmHlsCacheEntry(
            cacheKey = cacheKey,
            directory = directory,
            indexFile = indexFile,
            index = index,
        )
        persistIndex(entry)
        return entry
    }

    fun updatePlaylistContentType(
        entry: JvmHlsCacheEntry,
        contentType: String?,
    ) = synchronized(entry.lock) {
        if (contentType.isNullOrBlank() || contentType == entry.index.playlistContentType) {
            return@synchronized
        }
        entry.index = entry.index.copy(
            playlistContentType = contentType,
            lastAccessTime = System.currentTimeMillis(),
        )
        persistIndex(entry)
    }

    fun updateResources(
        entry: JvmHlsCacheEntry,
        playlistUrl: String,
        resources: List<HlsPlaylistResource>,
    ) = synchronized(entry.lock) {
        if (resources.isEmpty()) {
            entry.index = entry.index.copy(lastAccessTime = System.currentTimeMillis())
            persistIndex(entry)
            return@synchronized
        }

        val existingByUrl = entry.index.resources.associateBy { resource -> resource.url }.toMutableMap()
        var nextOrder = (entry.index.resources.maxOfOrNull { resource -> resource.order } ?: -1) + 1

        resources.forEach { resource ->
            val existing = existingByUrl[resource.url]
            existingByUrl[resource.url] = if (existing == null) {
                HlsCachedResource(
                    url = resource.url,
                    kind = resource.kind.routeValue,
                    cacheable = resource.cacheable && resource.kind != HlsResourceKind.PLAYLIST,
                    fileName = resourceFileName(resource.url),
                    contentType = defaultContentType,
                    contentLength = 0L,
                    cached = false,
                    order = nextOrder++,
                )
            } else {
                existing.copy(
                    kind = resource.kind.routeValue,
                    cacheable = existing.cacheable ||
                            (resource.cacheable && resource.kind != HlsResourceKind.PLAYLIST),
                )
            }
        }

        val sortedResources = existingByUrl.values.sortedBy { resource -> resource.order }
        entry.index = entry.index.copy(
            lastPlaylistUrl = playlistUrl,
            resources = sortedResources,
            completed = isComplete(sortedResources),
            lastAccessTime = System.currentTimeMillis(),
        )
        persistIndex(entry)
    }

    fun cachedResource(
        entry: JvmHlsCacheEntry,
        resourceUrl: String,
    ): HlsCachedResource? = synchronized(entry.lock) {
        entry.index.resources.firstOrNull { resource ->
            resource.url == resourceUrl &&
                    resource.cacheable &&
                    resource.cached &&
                    File(entry.directory, resource.fileName).exists()
        }
    }

    fun nextPrefetchResource(entry: JvmHlsCacheEntry): HlsCachedResource? = synchronized(entry.lock) {
        entry.index.resources.firstOrNull { resource ->
            resource.cacheable && !resource.cached && resource.kind != HlsResourceKind.PLAYLIST.routeValue
        }
    }

    fun progress(entry: JvmHlsCacheEntry): HlsCacheProgress = synchronized(entry.lock) {
        val cacheableResources = entry.index.resources.filter { resource ->
            resource.cacheable && resource.kind != HlsResourceKind.PLAYLIST.routeValue
        }
        HlsCacheProgress(
            cached = cacheableResources.count { resource -> resource.cached },
            total = cacheableResources.size,
        )
    }

    fun isComplete(entry: JvmHlsCacheEntry): Boolean = synchronized(entry.lock) {
        entry.index.completed || isComplete(entry.index.resources)
    }

    fun markAccessed(entry: JvmHlsCacheEntry) = synchronized(entry.lock) {
        entry.index = entry.index.copy(lastAccessTime = System.currentTimeMillis())
        persistIndex(entry)
    }

    fun createTempResourceFile(
        entry: JvmHlsCacheEntry,
        resourceUrl: String,
    ): File {
        entry.directory.mkdirs()
        return File(
            entry.directory,
            "${resourceFileName(resourceUrl)}-${System.nanoTime()}.tmp",
        )
    }

    fun commitResource(
        entry: JvmHlsCacheEntry,
        resourceUrl: String,
        kind: HlsResourceKind,
        cacheable: Boolean,
        contentType: String?,
        tempFile: File,
    ) = synchronized(entry.lock) {
        if (!cacheable || kind == HlsResourceKind.PLAYLIST || !tempFile.exists() || tempFile.length() <= 0L) {
            tempFile.delete()
            return@synchronized
        }

        val finalFileName = resourceFileName(resourceUrl)
        val finalFile = File(entry.directory, finalFileName)
        if (finalFile.exists()) {
            finalFile.delete()
        }
        moveFile(tempFile, finalFile)

        val resources = entry.index.resources.toMutableList()
        val existingIndex = resources.indexOfFirst { resource -> resource.url == resourceUrl }
        val nextOrder = (resources.maxOfOrNull { resource -> resource.order } ?: -1) + 1
        val committed = HlsCachedResource(
            url = resourceUrl,
            kind = kind.routeValue,
            cacheable = true,
            fileName = finalFileName,
            contentType = contentType?.takeIf { it.isNotBlank() } ?: defaultContentType,
            contentLength = finalFile.length(),
            cached = true,
            order = if (existingIndex >= 0) resources[existingIndex].order else nextOrder,
        )
        if (existingIndex >= 0) {
            resources[existingIndex] = committed
        } else {
            resources += committed
        }

        val sortedResources = resources.sortedBy { resource -> resource.order }
        entry.index = entry.index.copy(
            resources = sortedResources,
            completed = isComplete(sortedResources),
            lastAccessTime = System.currentTimeMillis(),
        )
        persistIndex(entry)
    }

    suspend fun readResource(
        entry: JvmHlsCacheEntry,
        resource: HlsCachedResource,
        output: ByteWriteChannel,
        buffer: ByteArray,
    ) = withContext(Dispatchers.IO) {
        val resourceFile = File(entry.directory, resource.fileName)
        RandomAccessFile(resourceFile, "r").use { input ->
            while (true) {
                val bytesRead = input.read(buffer)
                if (bytesRead <= 0) {
                    break
                }
                output.writeFully(buffer, 0, bytesRead)
                output.flush()
            }
        }
        markAccessed(entry)
    }

    fun sizeBytes(): Long {
        return if (rootDirectory.exists()) {
            rootDirectory.walkTopDown().filter { file -> file.isFile }.sumOf { file -> file.length() }
        } else {
            0L
        }
    }

    fun deleteAll() {
        if (rootDirectory.exists()) {
            rootDirectory.deleteRecursively()
        }
        rootDirectory.mkdirs()
    }

    fun enforceLimit(
        limitBytes: Long,
        protectedCacheKeys: Set<String>,
    ) {
        if (limitBytes <= 0L) {
            return
        }

        var currentSize = sizeBytes()
        if (currentSize <= limitBytes) {
            return
        }

        val candidates = rootDirectory
            .listFiles()
            .orEmpty()
            .filter { file -> file.isDirectory }
            .mapNotNull { directory ->
                val index = loadIndexOrNull(File(directory, INDEX_FILE_NAME)) ?: return@mapNotNull null
                if (index.cacheKey in protectedCacheKeys) {
                    return@mapNotNull null
                }
                HlsEvictionCandidate(
                    directory = directory,
                    lastAccessTime = index.lastAccessTime,
                )
            }
            .sortedBy { candidate -> candidate.lastAccessTime }

        for (candidate in candidates) {
            if (currentSize <= limitBytes) {
                break
            }
            val deletedBytes = candidate.directory.walkTopDown()
                .filter { file -> file.isFile }
                .sumOf { file -> file.length() }
            candidate.directory.deleteRecursively()
            currentSize -= deletedBytes
        }
    }

    private fun loadIndexOrNull(indexFile: File): HlsCacheIndex? {
        if (!indexFile.exists()) {
            return null
        }
        return runCatching {
            json.decodeFromString<HlsCacheIndex>(indexFile.readText())
        }.getOrNull()
    }

    private fun sanitizeIndex(
        directory: File,
        index: HlsCacheIndex,
    ): HlsCacheIndex {
        val resources = index.resources.map { resource ->
            if (!resource.cached) {
                return@map resource
            }
            val file = File(directory, resource.fileName)
            if (file.exists() && file.length() >= resource.contentLength) {
                resource
            } else {
                resource.copy(cached = false, contentLength = 0L, contentType = defaultContentType)
            }
        }
        return index.copy(
            resources = resources,
            completed = isComplete(resources),
        )
    }

    private fun persistIndex(entry: JvmHlsCacheEntry) {
        entry.directory.mkdirs()
        entry.indexFile.writeText(json.encodeToString(entry.index))
    }

    private fun isComplete(resources: List<HlsCachedResource>): Boolean {
        val cacheableResources = resources.filter { resource ->
            resource.cacheable && resource.kind != HlsResourceKind.PLAYLIST.routeValue
        }
        return cacheableResources.isNotEmpty() && cacheableResources.all { resource -> resource.cached }
    }

    private fun resourceFileName(resourceUrl: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(resourceUrl.encodeToByteArray())
            .joinToString("") { byte -> "%02x".format(byte) }
        return "hls-$digest.cache"
    }

    private fun moveFile(
        source: File,
        target: File,
    ) {
        target.parentFile?.mkdirs()
        if (!source.renameTo(target)) {
            source.copyTo(target, overwrite = true)
            source.delete()
        }
    }

    private fun deleteDirectoryChildren(directory: File) {
        directory.listFiles().orEmpty().forEach { file ->
            if (file.isDirectory) {
                file.deleteRecursively()
            } else {
                file.delete()
            }
        }
    }

    private fun File.normalizedDirectory(): File {
        return runCatching {
            canonicalFile
        }.getOrDefault(absoluteFile)
    }

    private data class HlsEvictionCandidate(
        val directory: File,
        val lastAccessTime: Long,
    )

    private companion object {
        private const val INDEX_FILE_NAME = "index.json"
        private const val DEFAULT_PLAYLIST_CONTENT_TYPE = "application/vnd.apple.mpegurl"
    }
}

internal class JvmHlsCacheEntry(
    val cacheKey: String,
    val directory: File,
    val indexFile: File,
    @Volatile var index: HlsCacheIndex,
) {
    val lock: Any = Any()
}

@Serializable
internal data class HlsCacheIndex(
    val cacheKey: String,
    val playlistUrl: String,
    val playlistContentType: String,
    val lastPlaylistUrl: String = playlistUrl,
    val resources: List<HlsCachedResource> = emptyList(),
    val completed: Boolean = false,
    val lastAccessTime: Long = System.currentTimeMillis(),
)

@Serializable
internal data class HlsCachedResource(
    val url: String,
    val kind: String,
    val cacheable: Boolean,
    val fileName: String,
    val contentType: String,
    val contentLength: Long,
    val cached: Boolean,
    val order: Int,
)

internal data class HlsCacheProgress(
    val cached: Int,
    val total: Int,
)
