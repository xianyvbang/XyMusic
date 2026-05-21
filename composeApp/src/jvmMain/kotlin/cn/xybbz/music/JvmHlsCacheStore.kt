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
    /** HLS 缓存根目录，所有播放列表索引和分片文件都会写入这里。 */
    private val rootDirectory: File,
    /** 分片响应没有返回 Content-Type 时使用的兜底类型。 */
    private val defaultContentType: String,
) {

    /**
     * index.json 的序列化配置。
     *
     * 索引文件可能由旧版本应用写入，因此读取时忽略未知字段；
     * 写入时保留默认值，方便人工排查缓存状态。
     */
    private val json = Json {
        // 兼容旧版本或未来版本写入的索引字段。
        ignoreUnknownKeys = true
        // 默认字段也写入 index.json，避免排查时字段缺失造成歧义。
        encodeDefaults = true
        // 缓存索引是调试入口，格式化后更容易查看。
        prettyPrint = true
    }

    /**
     * 打开或创建一个 HLS 缓存条目。
     *
     * 同一个 safeCacheKey 会映射到固定目录；如果目录里的索引属于同一首歌
     * 和同一个播放列表地址，则复用旧缓存，否则清空旧目录重新建索引。
     */
    fun openEntry(
        cacheKey: String,
        safeCacheKey: String,
        playlistUrl: String,
    ): JvmHlsCacheEntry {
        // 先确保根目录存在，再为当前播放项创建独立目录。
        rootDirectory.mkdirs()
        val directory = File(rootDirectory, "hls-$safeCacheKey").normalizedDirectory()
        directory.mkdirs()
        val indexFile = File(directory, INDEX_FILE_NAME)
        // 索引读取失败时直接当作无缓存处理，避免坏索引影响播放。
        val loadedIndex = loadIndexOrNull(indexFile)

        // 只有业务 key 和播放列表地址都匹配，目录里的缓存才属于当前请求。
        val indexMatchesRequest = loadedIndex != null &&
                loadedIndex.cacheKey == cacheKey &&
                loadedIndex.playlistUrl == playlistUrl

        val index = if (indexMatchesRequest) {
            // 复用索引前校验文件是否还存在，防止索引和磁盘状态不一致。
            sanitizeIndex(directory, requireNotNull(loadedIndex))
        } else {
            // 目录被复用但索引不匹配时，清理旧文件，避免串用其他歌曲分片。
            if (indexFile.exists()) {
                deleteDirectoryChildren(directory)
            }
            // 创建空索引，后续会随着播放列表解析和分片下载逐步填充。
            HlsCacheIndex(
                cacheKey = cacheKey,
                playlistUrl = playlistUrl,
                playlistContentType = DEFAULT_PLAYLIST_CONTENT_TYPE,
                resources = emptyList(),
                lastAccessTime = System.currentTimeMillis(),
            )
        }

        // Entry 持有当前索引和锁，后续所有读写都会围绕它进行同步。
        val entry = JvmHlsCacheEntry(
            cacheKey = cacheKey,
            directory = directory,
            indexFile = indexFile,
            index = index,
        )
        // 打开时立即落盘，保证目录里始终有一个可恢复的 index.json。
        persistIndex(entry)
        return entry
    }

    /**
     * 更新主播放列表的 Content-Type。
     *
     * 播放列表请求通常先于分片请求发生，这里记录真实类型，供缓存命中时复用。
     */
    fun updatePlaylistContentType(
        entry: JvmHlsCacheEntry,
        contentType: String?,
    ) = synchronized(entry.lock) {
        // 空值或未变化时不写盘，减少无意义的 index.json 更新。
        if (contentType.isNullOrBlank() || contentType == entry.index.playlistContentType) {
            return@synchronized
        }
        entry.index = entry.index.copy(
            playlistContentType = contentType,
            lastAccessTime = System.currentTimeMillis(),
        )
        persistIndex(entry)
    }

    /**
     * 根据最新播放列表内容刷新资源清单。
     *
     * 这里只登记播放列表里出现过的资源，不直接写入分片内容；
     * 真正的分片缓存由 [commitResource] 完成。
     */
    fun updateResources(
        entry: JvmHlsCacheEntry,
        playlistUrl: String,
        resources: List<HlsPlaylistResource>,
    ) = synchronized(entry.lock) {
        // 播放列表暂时没有解析出资源时，只刷新访问时间，保留旧清单。
        if (resources.isEmpty()) {
            entry.index = entry.index.copy(lastAccessTime = System.currentTimeMillis())
            persistIndex(entry)
            return@synchronized
        }

        // 按 URL 合并新旧资源，避免同一分片重复登记。
        val existingByUrl = entry.index.resources.associateBy { resource -> resource.url }.toMutableMap()
        // order 用于保持播放列表出现顺序，预取时会按这个顺序处理。
        var nextOrder = (entry.index.resources.maxOfOrNull { resource -> resource.order } ?: -1) + 1

        resources.forEach { resource ->
            val existing = existingByUrl[resource.url]
            existingByUrl[resource.url] = if (existing == null) {
                // 新资源先登记为未缓存；播放列表本身不作为分片缓存。
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
                // 已登记资源保留原顺序；一旦被判定可缓存，就不要被后续结果降级。
                existing.copy(
                    kind = resource.kind.routeValue,
                    cacheable = existing.cacheable ||
                            (resource.cacheable && resource.kind != HlsResourceKind.PLAYLIST),
                )
            }
        }

        // 排序后写回索引，保证后续读取、预取和进度统计顺序稳定。
        val sortedResources = existingByUrl.values.sortedBy { resource -> resource.order }
        entry.index = entry.index.copy(
            lastPlaylistUrl = playlistUrl,
            resources = sortedResources,
            completed = isComplete(sortedResources),
            lastAccessTime = System.currentTimeMillis(),
        )
        persistIndex(entry)
    }

    /**
     * 查询指定资源是否已经有可直接返回的缓存文件。
     */
    fun cachedResource(
        entry: JvmHlsCacheEntry,
        resourceUrl: String,
    ): HlsCachedResource? = synchronized(entry.lock) {
        // 索引标记、可缓存标记和真实文件都满足时，才认为缓存命中。
        entry.index.resources.firstOrNull { resource ->
            resource.url == resourceUrl &&
                    resource.cacheable &&
                    resource.cached &&
                    File(entry.directory, resource.fileName).exists()
        }
    }

    /**
     * 找到下一个适合后台预取的分片。
     */
    fun nextPrefetchResource(entry: JvmHlsCacheEntry): HlsCachedResource? = synchronized(entry.lock) {
        // 只预取媒体分片，不预取播放列表，避免递归刷新列表造成状态混乱。
        entry.index.resources.firstOrNull { resource ->
            resource.cacheable && !resource.cached && resource.kind != HlsResourceKind.PLAYLIST.routeValue
        }
    }

    /**
     * 计算当前条目的 HLS 分片缓存进度。
     */
    fun progress(entry: JvmHlsCacheEntry): HlsCacheProgress = synchronized(entry.lock) {
        // 进度只统计真正需要下载的媒体分片，播放列表不计入 total。
        val cacheableResources = entry.index.resources.filter { resource ->
            resource.cacheable && resource.kind != HlsResourceKind.PLAYLIST.routeValue
        }
        HlsCacheProgress(
            cached = cacheableResources.count { resource -> resource.cached },
            total = cacheableResources.size,
        )
    }

    /**
     * 判断当前条目的可缓存分片是否已经全部完成。
     */
    fun isComplete(entry: JvmHlsCacheEntry): Boolean = synchronized(entry.lock) {
        // 优先相信索引里的完成标记；如果旧索引没写入标记，则实时重算一次。
        entry.index.completed || isComplete(entry.index.resources)
    }

    /**
     * 刷新访问时间，用于缓存淘汰时判断最近使用情况。
     */
    fun markAccessed(entry: JvmHlsCacheEntry) = synchronized(entry.lock) {
        entry.index = entry.index.copy(lastAccessTime = System.currentTimeMillis())
        persistIndex(entry)
    }

    /**
     * 为即将下载的分片创建临时文件。
     *
     * 临时文件名包含纳秒时间，避免并发请求同一资源时互相覆盖。
     */
    fun createTempResourceFile(
        entry: JvmHlsCacheEntry,
        resourceUrl: String,
    ): File {
        // 下载中的内容只能先写临时文件，完整提交后才会变成正式缓存。
        entry.directory.mkdirs()
        return File(
            entry.directory,
            "${resourceFileName(resourceUrl)}-${System.nanoTime()}.tmp",
        )
    }

    /**
     * 将已经完整下载的临时分片提交为正式缓存。
     *
     * 只有可缓存的媒体分片会进入 index.json；播放列表和空文件会被丢弃。
     */
    fun commitResource(
        entry: JvmHlsCacheEntry,
        resourceUrl: String,
        kind: HlsResourceKind,
        cacheable: Boolean,
        contentType: String?,
        tempFile: File,
    ) = synchronized(entry.lock) {
        // 不可缓存、播放列表、下载失败或空文件都不能提交，避免污染缓存索引。
        if (!cacheable || kind == HlsResourceKind.PLAYLIST || !tempFile.exists() || tempFile.length() <= 0L) {
            tempFile.delete()
            return@synchronized
        }

        // 正式文件名只由资源 URL 决定，同一分片重复下载时会覆盖旧文件。
        val finalFileName = resourceFileName(resourceUrl)
        val finalFile = File(entry.directory, finalFileName)
        if (finalFile.exists()) {
            finalFile.delete()
        }
        // 先移动文件，再更新索引，保证索引里出现的文件已经真实存在。
        moveFile(tempFile, finalFile)

        // 复用已有资源的 order，避免提交后改变播放列表顺序。
        val resources = entry.index.resources.toMutableList()
        val existingIndex = resources.indexOfFirst { resource -> resource.url == resourceUrl }
        val nextOrder = (resources.maxOfOrNull { resource -> resource.order } ?: -1) + 1
        // 提交后的资源一定是可缓存且已缓存，并记录最终文件大小和响应类型。
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
            // 已登记资源更新为已缓存状态。
            resources[existingIndex] = committed
        } else {
            // 如果分片请求先于播放列表清单更新到达，也要补登记该资源。
            resources += committed
        }

        // 每次提交后重新计算完成状态，并把最新索引落盘。
        val sortedResources = resources.sortedBy { resource -> resource.order }
        entry.index = entry.index.copy(
            resources = sortedResources,
            completed = isComplete(sortedResources),
            lastAccessTime = System.currentTimeMillis(),
        )
        persistIndex(entry)
    }

    /**
     * 读取缓存分片并写入 Ktor 响应通道。
     */
    suspend fun readResource(
        entry: JvmHlsCacheEntry,
        resource: HlsCachedResource,
        output: ByteWriteChannel,
        buffer: ByteArray,
    ) = withContext(Dispatchers.IO) {
        // 文件读取放到 IO 线程，避免阻塞调用方协程上下文。
        val resourceFile = File(entry.directory, resource.fileName)
        RandomAccessFile(resourceFile, "r").use { input ->
            while (true) {
                val bytesRead = input.read(buffer)
                if (bytesRead <= 0) {
                    break
                }
                // 每读一块就写出并 flush，降低播放器等待首包的时间。
                output.writeFully(buffer, 0, bytesRead)
                output.flush()
            }
        }
        // 读取成功后刷新访问时间，避免活跃播放的缓存被优先淘汰。
        markAccessed(entry)
    }

    /**
     * 统计 HLS 缓存根目录当前占用的总字节数。
     */
    fun sizeBytes(): Long {
        return if (rootDirectory.exists()) {
            // 只统计文件大小，目录本身不参与容量限制。
            rootDirectory.walkTopDown().filter { file -> file.isFile }.sumOf { file -> file.length() }
        } else {
            0L
        }
    }

    /**
     * 清空全部 HLS 缓存。
     */
    fun deleteAll() {
        if (rootDirectory.exists()) {
            rootDirectory.deleteRecursively()
        }
        // 删除后立即重建根目录，后续写入不用再处理根目录缺失。
        rootDirectory.mkdirs()
    }

    /**
     * 按最近访问时间执行缓存淘汰，直到总大小不超过限制。
     *
     * [protectedCacheKeys] 中的缓存正在使用或需要保留，不参与本轮淘汰。
     */
    fun enforceLimit(
        limitBytes: Long,
        protectedCacheKeys: Set<String>,
    ) {
        // 非正数表示不启用容量限制。
        if (limitBytes <= 0L) {
            return
        }

        var currentSize = sizeBytes()
        // 当前大小已经满足限制时无需扫描索引。
        if (currentSize <= limitBytes) {
            return
        }

        // 候选目录必须能读到索引；读不到索引的目录暂不处理，避免误删非缓存内容。
        val candidates = rootDirectory
            .listFiles()
            .orEmpty()
            .filter { file -> file.isDirectory }
            .mapNotNull { directory ->
                val index = loadIndexOrNull(File(directory, INDEX_FILE_NAME)) ?: return@mapNotNull null
                // 正在播放或被上层保护的条目不能淘汰。
                if (index.cacheKey in protectedCacheKeys) {
                    return@mapNotNull null
                }
                HlsEvictionCandidate(
                    directory = directory,
                    lastAccessTime = index.lastAccessTime,
                )
            }
            .sortedBy { candidate -> candidate.lastAccessTime }

        // 从最久未访问的缓存开始删除，删除后同步扣减当前大小。
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

    /**
     * 读取缓存索引；不存在或解析失败时返回 null。
     */
    private fun loadIndexOrNull(indexFile: File): HlsCacheIndex? {
        if (!indexFile.exists()) {
            return null
        }
        return runCatching {
            // 索引损坏时不向外抛异常，让调用方按无缓存继续播放。
            json.decodeFromString<HlsCacheIndex>(indexFile.readText())
        }.getOrNull()
    }

    /**
     * 清洗索引里已经标记为缓存完成的资源。
     *
     * 如果磁盘文件缺失或比记录的小，就把该资源退回未缓存状态。
     */
    private fun sanitizeIndex(
        directory: File,
        index: HlsCacheIndex,
    ): HlsCacheIndex {
        val resources = index.resources.map { resource ->
            // 未缓存资源没有对应文件要求，直接保留。
            if (!resource.cached) {
                return@map resource
            }
            val file = File(directory, resource.fileName)
            if (file.exists() && file.length() >= resource.contentLength) {
                // 文件存在且长度满足索引记录，认为该分片可继续复用。
                resource
            } else {
                // 文件状态异常时只回退该资源，不清空整个播放列表索引。
                resource.copy(cached = false, contentLength = 0L, contentType = defaultContentType)
            }
        }
        return index.copy(
            resources = resources,
            completed = isComplete(resources),
        )
    }

    /**
     * 将当前索引写入 index.json。
     */
    private fun persistIndex(entry: JvmHlsCacheEntry) {
        entry.directory.mkdirs()
        entry.indexFile.writeText(json.encodeToString(entry.index))
    }

    /**
     * 根据资源列表实时计算缓存是否完整。
     */
    private fun isComplete(resources: List<HlsCachedResource>): Boolean {
        // 只有至少存在一个可缓存媒体分片，且所有媒体分片都已缓存，才算完成。
        val cacheableResources = resources.filter { resource ->
            resource.cacheable && resource.kind != HlsResourceKind.PLAYLIST.routeValue
        }
        return cacheableResources.isNotEmpty() && cacheableResources.all { resource -> resource.cached }
    }

    /**
     * 根据资源 URL 生成稳定且安全的本地文件名。
     */
    private fun resourceFileName(resourceUrl: String): String {
        // URL 可能包含查询参数和特殊字符，使用 SHA-256 避免路径非法或过长。
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(resourceUrl.encodeToByteArray())
            .joinToString("") { byte -> "%02x".format(byte) }
        return "hls-$digest.cache"
    }

    /**
     * 将临时文件移动到最终缓存位置。
     */
    private fun moveFile(
        source: File,
        target: File,
    ) {
        target.parentFile?.mkdirs()
        // renameTo 在跨文件系统时可能失败，失败后回退为复制再删除。
        if (!source.renameTo(target)) {
            source.copyTo(target, overwrite = true)
            source.delete()
        }
    }

    /**
     * 删除目录下的所有子文件和子目录，但保留目录本身。
     */
    private fun deleteDirectoryChildren(directory: File) {
        directory.listFiles().orEmpty().forEach { file ->
            if (file.isDirectory) {
                file.deleteRecursively()
            } else {
                file.delete()
            }
        }
    }

    /**
     * 获取规范化目录路径。
     */
    private fun File.normalizedDirectory(): File {
        return runCatching {
            // canonicalFile 可消除 .. 和符号链接；失败时使用绝对路径兜底。
            canonicalFile
        }.getOrDefault(absoluteFile)
    }

    /**
     * 缓存淘汰候选项。
     */
    private data class HlsEvictionCandidate(
        /** 待删除的缓存目录。 */
        val directory: File,
        /** 最近访问时间，值越小越先被淘汰。 */
        val lastAccessTime: Long,
    )

    private companion object {
        /** 每个 HLS 缓存目录里的索引文件名。 */
        private const val INDEX_FILE_NAME = "index.json"
        /** 播放列表默认响应类型。 */
        private const val DEFAULT_PLAYLIST_CONTENT_TYPE = "application/vnd.apple.mpegurl"
    }
}

/**
 * 单个 HLS 播放缓存条目。
 *
 * 一个条目对应一个独立目录和一份 index.json，内部用 lock 保证索引读写串行。
 */
internal class JvmHlsCacheEntry(
    /** 业务缓存 key，通常对应一首歌或一次播放资源。 */
    val cacheKey: String,
    /** 当前条目的缓存目录。 */
    val directory: File,
    /** 当前条目的索引文件。 */
    val indexFile: File,
    /** 内存中的索引副本，更新后需要通过 persistIndex 落盘。 */
    @Volatile var index: HlsCacheIndex,
) {
    /** 串行化当前条目的索引和文件状态变更。 */
    val lock: Any = Any()
}

/**
 * HLS 缓存索引。
 *
 * 该结构会序列化到 index.json，用来恢复播放列表和分片缓存状态。
 */
@Serializable
internal data class HlsCacheIndex(
    /** 业务缓存 key，用来校验目录是否属于当前请求。 */
    val cacheKey: String,
    /** 初始主播放列表地址，用来判断旧索引是否可以复用。 */
    val playlistUrl: String,
    /** 主播放列表响应类型。 */
    val playlistContentType: String,
    /** 最近一次解析到资源的播放列表地址，可能是主列表或变体列表。 */
    val lastPlaylistUrl: String = playlistUrl,
    /** 播放列表中出现过的资源清单。 */
    val resources: List<HlsCachedResource> = emptyList(),
    /** 是否所有可缓存媒体分片都已经完成缓存。 */
    val completed: Boolean = false,
    /** 最近访问时间，用于容量淘汰排序。 */
    val lastAccessTime: Long = System.currentTimeMillis(),
)

/**
 * 播放列表中的单个资源缓存记录。
 */
@Serializable
internal data class HlsCachedResource(
    /** 资源原始 URL。 */
    val url: String,
    /** 资源类型，使用 HlsResourceKind 的路由值持久化。 */
    val kind: String,
    /** 当前资源是否允许缓存。 */
    val cacheable: Boolean,
    /** 资源落盘后的本地文件名。 */
    val fileName: String,
    /** 命中缓存时返回给播放器的 Content-Type。 */
    val contentType: String,
    /** 已缓存文件的字节长度。 */
    val contentLength: Long,
    /** 该资源是否已经完整写入本地文件。 */
    val cached: Boolean,
    /** 资源在播放列表中的顺序，用于预取和稳定排序。 */
    val order: Int,
)

/**
 * HLS 媒体分片缓存进度。
 */
internal data class HlsCacheProgress(
    /** 已完成缓存的媒体分片数量。 */
    val cached: Int,
    /** 需要缓存的媒体分片总数量。 */
    val total: Int,
)
