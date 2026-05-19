package cn.xybbz.music

import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.writeFully
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile

/**
 * JVM 播放缓存的本地 span 存储层。
 *
 * 这个类只负责“缓存文件如何落盘、索引如何维护、缓存区间如何查询”，不关心播放器和上游网络。
 * 这样控制器可以像 Media3 的 CacheDataSource 一样，把一次播放读取拆成缓存命中和回源补段两种路径。
 *
 * @property rootDirectory JVM 播放缓存根目录，所有歌曲缓存都会落在这个目录下。
 * @property defaultContentType 上游还没有返回真实类型时写入索引的保守默认 Content-Type。
 */
internal class JvmPlaybackCacheStore(
    private val rootDirectory: File,
    private val defaultContentType: String,
) {

    /**
     * 缓存索引 JSON 编解码器。
     *
     * ignoreUnknownKeys 用于兼容后续索引字段扩展；encodeDefaults 让 index.json 自描述更完整。
     */
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    /**
     * 打开或创建指定歌曲的缓存条目。
     *
     * 如果 index.json 损坏，只清理当前歌曲目录并重建索引，避免单首歌的坏缓存影响整个播放缓存。
     *
     * @param cacheKey 业务缓存 key，包含歌曲 id、静态地址、转码格式和码率等影响播放内容的因素。
     * @param safeCacheKey 已经过文件名安全处理的缓存 key，用作歌曲缓存目录名。
     * @param sourceUrl 当前歌曲的真实上游播放地址，用于发现缓存是否对应同一个媒体内容。
     * @param totalBytes 当前媒体总字节数，span 完整性判断依赖它。
     * @return 可供控制器持有的缓存条目。
     */
    fun openEntry(
        cacheKey: String,
        safeCacheKey: String,
        sourceUrl: String,
        totalBytes: Long,
    ): JvmPlaybackCacheEntry {
        rootDirectory.mkdirs()
        val directory = File(rootDirectory, safeCacheKey).normalizedDirectory()
        directory.mkdirs()
        val indexFile = File(directory, INDEX_FILE_NAME)
        val loadedIndex = loadIndexOrNull(indexFile)

        val indexMatchesRequest = loadedIndex != null &&
                loadedIndex.cacheKey == cacheKey &&
                loadedIndex.sourceUrl == sourceUrl &&
                loadedIndex.totalBytes == totalBytes

        val index = if (indexMatchesRequest) {
            val matchedIndex = requireNotNull(loadedIndex)
            // 旧索引可能记录了已经被用户手动删除的 span 文件，打开时统一校验一次。
            sanitizeIndex(directory, matchedIndex)
        } else {
            if (indexFile.exists()) {
                /*
                 * index.json 不能解析、同一 cacheKey 换源、或总长度变化时，都不能继续复用旧 span。
                 * 只清理当前歌曲目录可以避免一首歌的异常影响整个缓存目录。
                 */
                deleteDirectoryChildren(directory)
            }
            PlaybackCacheIndex(
                cacheKey = cacheKey,
                sourceUrl = sourceUrl,
                totalBytes = totalBytes,
                contentType = defaultContentType,
                spans = emptyList(),
                completed = false,
                lastAccessTime = System.currentTimeMillis(),
            )
        }

        val entry = JvmPlaybackCacheEntry(
            cacheKey = cacheKey,
            directory = directory,
            indexFile = indexFile,
            index = index,
        )
        persistIndex(entry)
        return entry
    }

    /**
     * 读取条目当前的 Content-Type。
     *
     * @param entry 当前歌曲缓存条目。
     * @return 索引里保存的真实媒体类型，缺失时返回默认类型。
     */
    fun contentType(entry: JvmPlaybackCacheEntry): String = synchronized(entry.lock) {
        entry.index.contentType.takeIf { it.isNotBlank() } ?: defaultContentType
    }

    /**
     * 更新当前条目的真实 Content-Type。
     *
     * 上游可能返回转码流，因此类型必须以 HTTP 响应头为准，而不是按原始文件扩展名推断。
     *
     * @param entry 当前歌曲缓存条目。
     * @param contentType 上游响应里的 Content-Type。
     */
    fun updateContentType(
        entry: JvmPlaybackCacheEntry,
        contentType: String?,
    ) = synchronized(entry.lock) {
        if (contentType.isNullOrBlank() || contentType == entry.index.contentType) {
            return@synchronized
        }
        entry.index = entry.index.copy(contentType = contentType)
        persistIndex(entry)
    }

    /**
     * 更新上游缓存校验字段。
     *
     * 这些字段当前只持久化不参与请求校验，先保留下来，后续可以用于判断服务端资源是否变化。
     *
     * @param entry 当前歌曲缓存条目。
     * @param etag 上游 ETag 响应头。
     * @param lastModified 上游 Last-Modified 响应头。
     */
    fun updateValidators(
        entry: JvmPlaybackCacheEntry,
        etag: String?,
        lastModified: String?,
    ) = synchronized(entry.lock) {
        if (etag.isNullOrBlank() && lastModified.isNullOrBlank()) {
            return@synchronized
        }
        entry.index = entry.index.copy(
            etag = etag ?: entry.index.etag,
            lastModified = lastModified ?: entry.index.lastModified,
        )
        persistIndex(entry)
    }

    /**
     * 标记条目最近被访问。
     *
     * LRU 清理会按这个时间删除旧歌曲缓存；播放命中和创建会话时都应该刷新它。
     *
     * @param entry 当前歌曲缓存条目。
     */
    fun markAccessed(entry: JvmPlaybackCacheEntry) = synchronized(entry.lock) {
        entry.index = entry.index.copy(lastAccessTime = System.currentTimeMillis())
        persistIndex(entry)
    }

    /**
     * 统计条目已缓存的去重字节数。
     *
     * @param entry 当前歌曲缓存条目。
     * @return 已缓存区间合并后的总字节数，单位是 byte。
     */
    fun cachedBytes(entry: JvmPlaybackCacheEntry): Long = synchronized(entry.lock) {
        mergedCoverage(entry.index.spans).sumOf { span -> span.endInclusive - span.start + 1 }
    }

    /**
     * 判断条目是否已经完整覆盖整首媒体。
     *
     * @param entry 当前歌曲缓存条目。
     * @return true 表示 span 已覆盖 [0, totalBytes - 1]。
     */
    fun isComplete(entry: JvmPlaybackCacheEntry): Boolean = synchronized(entry.lock) {
        entry.index.completed || coversWholeMedia(entry.index)
    }

    /**
     * 查找包含指定位置的物理 span。
     *
     * 这里查的是实际文件片段，不是合并后的逻辑区间，因为读取时必须知道具体文件名。
     *
     * @param entry 当前歌曲缓存条目。
     * @param position 需要读取的媒体字节位置。
     * @return 命中的 span，不存在时返回 null。
     */
    fun findSpanContaining(
        entry: JvmPlaybackCacheEntry,
        position: Long,
    ): CachedSpan? = synchronized(entry.lock) {
        entry.index.spans.firstOrNull { span -> position in span.start..span.endInclusive }
    }

    /**
     * 查找指定位置之后最近的 span 起点。
     *
     * 播放请求遇到缓存缺口时，用这个值把回源范围限制在“缺失区间”内，避免重复下载已经命中的后续 span。
     *
     * @param entry 当前歌曲缓存条目。
     * @param position 当前缺失区间起点。
     * @return 后续最近的 span 起点，不存在时返回 null。
     */
    fun nextSpanStartAfter(
        entry: JvmPlaybackCacheEntry,
        position: Long,
    ): Long? = synchronized(entry.lock) {
        entry.index.spans
            .asSequence()
            .map { span -> span.start }
            .filter { start -> start > position }
            .minOrNull()
    }

    /**
     * 找到从 0 开始的连续缓存前缀之后第一个缺失位置。
     *
     * 后台预取只沿着连续前缀向后推进，避免把预取任务变成随机 Range 抢占播放器请求。
     *
     * @param entry 当前歌曲缓存条目。
     * @return 第一个缺失字节位置；如果已经完整缓存，则返回 totalBytes。
     */
    fun firstMissingPositionFromStart(entry: JvmPlaybackCacheEntry): Long = synchronized(entry.lock) {
        val totalBytes = entry.index.totalBytes
        var expectedStart = 0L
        for (span in mergedCoverage(entry.index.spans)) {
            if (span.start > expectedStart) {
                break
            }
            if (span.endInclusive >= expectedStart) {
                expectedStart = span.endInclusive + 1
            }
            if (expectedStart >= totalBytes) {
                return@synchronized totalBytes
            }
        }
        expectedStart.coerceAtMost(totalBytes)
    }

    /**
     * 创建一个临时 span 文件。
     *
     * 临时文件不会出现在 index.json 中，只有完整写入并提交后才会成为可命中的缓存。
     *
     * @param entry 当前歌曲缓存条目。
     * @param start 当前临时文件对应的媒体起始字节。
     * @param endInclusive 当前临时文件对应的媒体结束字节。
     * @return 可写入的临时文件。
     */
    fun createTempSpanFile(
        entry: JvmPlaybackCacheEntry,
        start: Long,
        endInclusive: Long,
    ): File {
        entry.directory.mkdirs()
        return File(
            entry.directory,
            "span-$start-$endInclusive-${System.nanoTime()}.tmp",
        )
    }

    /**
     * 提交一个已经完整写入的 span 临时文件。
     *
     * 提交时只登记物理文件，并在覆盖计算中合并相邻/重叠区间；这样避免为了合并大文件而频繁复制音频数据。
     *
     * @param entry 当前歌曲缓存条目。
     * @param start span 起始字节。
     * @param endInclusive span 结束字节。
     * @param tempFile 已完整写入的临时文件。
     */
    fun commitSpan(
        entry: JvmPlaybackCacheEntry,
        start: Long,
        endInclusive: Long,
        tempFile: File,
    ) = synchronized(entry.lock) {
        val expectedLength = endInclusive - start + 1
        if (expectedLength <= 0L || !tempFile.exists() || tempFile.length() < expectedLength) {
            tempFile.delete()
            return@synchronized
        }

        val finalFileName = "span-$start-$endInclusive.cache"
        val finalFile = File(entry.directory, finalFileName)
        if (finalFile.exists()) {
            finalFile.delete()
        }
        moveFile(tempFile, finalFile)

        val physicalSpans = dropFullyCoveredPhysicalSpans(
            directory = entry.directory,
            spans = entry.index.spans,
            start = start,
            endInclusive = endInclusive,
        ) + CachedSpan(
            start = start,
            endInclusive = endInclusive,
            fileName = finalFileName,
        )

        val sortedSpans = physicalSpans.sortedWith(compareBy<CachedSpan> { it.start }.thenBy { it.endInclusive })
        entry.index = entry.index.copy(
            spans = sortedSpans,
            completed = coversWholeMedia(entry.index.copy(spans = sortedSpans)),
            lastAccessTime = System.currentTimeMillis(),
        )
        persistIndex(entry)
    }

    /**
     * 从命中的 span 文件中读取指定范围并写给本地代理响应。
     *
     * @param entry 当前歌曲缓存条目。
     * @param span 命中的物理 span。
     * @param start 本次读取的媒体起始字节。
     * @param endInclusive 本次读取的媒体结束字节。
     * @param buffer 复用的读写缓冲区。
     * @param output 本地代理响应输出通道。
     */
    suspend fun readSpanRange(
        entry: JvmPlaybackCacheEntry,
        span: CachedSpan,
        start: Long,
        endInclusive: Long,
        buffer: ByteArray,
        output: ByteWriteChannel,
    ) = withContext(Dispatchers.IO) {
        val spanFile = File(entry.directory, span.fileName)
        RandomAccessFile(spanFile, "r").use { input ->
            input.seek(start - span.start)
            var remaining = endInclusive - start + 1
            while (remaining > 0L) {
                val readSize = minOf(buffer.size.toLong(), remaining).toInt()
                val bytesRead = input.read(buffer, 0, readSize)
                if (bytesRead <= 0) {
                    break
                }
                output.writeFully(buffer, 0, bytesRead)
                // VLC 对本地 HTTP 小片段比较敏感，及时 flush 可以减少播放器误判卡顿。
                output.flush()
                remaining -= bytesRead
            }
        }
        markAccessed(entry)
    }

    /**
     * 读取当前缓存根目录占用空间。
     *
     * @return 所有缓存文件大小，单位是 byte。
     */
    fun sizeBytes(): Long {
        return if (rootDirectory.exists()) {
            rootDirectory.walkTopDown().filter { file -> file.isFile }.sumOf { file -> file.length() }
        } else {
            0L
        }
    }

    /**
     * 删除整个播放缓存目录下的所有内容。
     *
     * 这里会顺带删除旧版 `.part`/最终文件，因为它们不再参与新版 span 索引。
     */
    fun deleteAll() {
        if (rootDirectory.exists()) {
            rootDirectory.deleteRecursively()
        }
        rootDirectory.mkdirs()
    }

    /**
     * 按 LRU 策略执行缓存上限清理。
     *
     * 为了避免正在播放的歌曲突然被删除，protectedCacheKeys 中的条目不会被清理。
     *
     * @param limitBytes 缓存目录允许占用的最大字节数。
     * @param protectedCacheKeys 当前播放或正在使用的缓存 key 集合。
     */
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
                CacheEvictionCandidate(
                    directory = directory,
                    cacheKey = index.cacheKey,
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
            // LRU 清理以“整首歌”为单位，避免删掉半个 span 后 index 和文件状态不一致。
            candidate.directory.deleteRecursively()
            currentSize -= deletedBytes
        }
    }

    /**
     * 返回合并后的逻辑覆盖区间。
     *
     * 该方法主要供测试验证 span 合并结果；播放读取仍使用物理 span，避免额外拷贝文件。
     *
     * @param entry 当前歌曲缓存条目。
     * @return 相邻或重叠 span 合并后的覆盖区间。
     */
    fun mergedCoverageForTest(entry: JvmPlaybackCacheEntry): List<CachedSpan> = synchronized(entry.lock) {
        mergedCoverage(entry.index.spans)
    }

    /**
     * 尝试读取 index.json。
     *
     * 解析失败返回 null，由调用方执行当前歌曲目录级别的损坏恢复。
     *
     * @param indexFile 索引文件。
     * @return 解析成功的索引，失败时为 null。
     */
    private fun loadIndexOrNull(indexFile: File): PlaybackCacheIndex? {
        if (!indexFile.exists()) {
            return null
        }
        return runCatching {
            json.decodeFromString<PlaybackCacheIndex>(indexFile.readText())
        }.getOrNull()
    }

    /**
     * 清理索引中已经失效的 span。
     *
     * 用户可能手动删除缓存文件，启动时做一次校验可以避免读取不存在的 span 文件。
     *
     * @param directory 当前歌曲缓存目录。
     * @param index 从磁盘读取到的原始索引。
     * @return 去掉无效 span 后的索引。
     */
    private fun sanitizeIndex(
        directory: File,
        index: PlaybackCacheIndex,
    ): PlaybackCacheIndex {
        val validSpans = index.spans.filter { span ->
            val file = File(directory, span.fileName)
            val expectedLength = span.endInclusive - span.start + 1
            file.exists() && file.length() >= expectedLength && span.start <= span.endInclusive
        }.sortedWith(compareBy<CachedSpan> { it.start }.thenBy { it.endInclusive })

        return index.copy(
            spans = validSpans,
            completed = coversWholeMedia(index.copy(spans = validSpans)),
        )
    }

    /**
     * 持久化索引到 index.json。
     *
     * 索引是 span 能否命中的唯一来源，因此每次状态变更都同步写盘，优先保证重启后的可恢复性。
     *
     * @param entry 当前歌曲缓存条目。
     */
    private fun persistIndex(entry: JvmPlaybackCacheEntry) {
        entry.directory.mkdirs()
        entry.indexFile.writeText(json.encodeToString(entry.index))
    }

    /**
     * 丢弃被新 span 完全覆盖的旧物理 span 索引。
     *
     * 重复 span 可能来自前台播放和后台预取同时下载同一区间；保留最新 span 可以避免索引出现重复覆盖。
     * 只有旧 span 被完整覆盖时才移除索引，部分重叠的旧 span 仍保留，否则会丢失两侧仍然有效的缓存范围。
     * 这里暂时不删除旧物理文件，因为 VLC 响应线程可能正在读取旧文件，立即删除会造成并发读失败。
     *
     * @param directory 当前歌曲缓存目录。
     * @param spans 原有物理 span 列表。
     * @param start 新 span 起始字节。
     * @param endInclusive 新 span 结束字节。
     * @return 保留的旧 span。
     */
    private fun dropFullyCoveredPhysicalSpans(
        directory: File,
        spans: List<CachedSpan>,
        start: Long,
        endInclusive: Long,
    ): List<CachedSpan> {
        // directory 当前只作为语义参数保留，后续如果加入“无活跃读取后清理孤儿文件”可以直接复用。
        directory.mkdirs()
        return spans.filterNot { span ->
            span.start >= start && span.endInclusive <= endInclusive
        }
    }

    /**
     * 计算 span 的逻辑覆盖区间。
     *
     * 这里会合并相邻和重叠区间，用于进度统计、完整性判断和测试验证。
     *
     * @param spans 物理 span 列表。
     * @return 合并后的逻辑覆盖列表。
     */
    private fun mergedCoverage(spans: List<CachedSpan>): List<CachedSpan> {
        val sortedSpans = spans
            .filter { span -> span.start <= span.endInclusive }
            .sortedWith(compareBy<CachedSpan> { it.start }.thenBy { it.endInclusive })
        if (sortedSpans.isEmpty()) {
            return emptyList()
        }

        val merged = mutableListOf<CachedSpan>()
        var current = sortedSpans.first()
        for (span in sortedSpans.drop(1)) {
            if (span.start <= current.endInclusive + 1) {
                // 只合并逻辑区间，不合并文件名；读取时仍按物理 span 精确定位文件。
                current = current.copy(
                    endInclusive = maxOf(current.endInclusive, span.endInclusive),
                    fileName = current.fileName,
                )
            } else {
                merged += current
                current = span
            }
        }
        merged += current
        return merged
    }

    /**
     * 判断索引是否完整覆盖媒体。
     *
     * @param index 当前缓存索引。
     * @return true 表示从 0 到 totalBytes - 1 都已经有缓存覆盖。
     */
    private fun coversWholeMedia(index: PlaybackCacheIndex): Boolean {
        if (index.totalBytes <= 0L) {
            return false
        }
        val coverage = mergedCoverage(index.spans)
        return coverage.firstOrNull()?.let { span ->
            span.start == 0L && span.endInclusive >= index.totalBytes - 1
        } == true
    }

    /**
     * 移动文件，跨文件系统失败时退回到复制再删除。
     *
     * @param source 源文件。
     * @param target 目标文件。
     */
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

    /**
     * 删除目录下所有子项，但保留目录本身。
     *
     * @param directory 要清空的目录。
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
     * 规范化目录路径。
     *
     * @return canonicalFile 失败时退回 absoluteFile。
     */
    private fun File.normalizedDirectory(): File {
        return runCatching {
            canonicalFile
        }.getOrDefault(absoluteFile)
    }

    /**
     * 等待清理的缓存候选项。
     *
     * @property directory 歌曲缓存目录。
     * @property cacheKey 业务缓存 key，用于避开正在播放的条目。
     * @property lastAccessTime 最近访问时间，数值越小越优先删除。
     */
    private data class CacheEvictionCandidate(
        val directory: File,
        val cacheKey: String,
        val lastAccessTime: Long,
    )

    private companion object {
        /**
         * 每首歌缓存目录中的索引文件名。
         */
        private const val INDEX_FILE_NAME = "index.json"
    }
}

/**
 * JVM 播放缓存条目。
 *
 * 一个条目对应一首歌曲的一组 span 文件和一个 index.json。
 *
 * @property cacheKey 业务缓存 key，线程间只读共享。
 * @property directory 当前歌曲缓存目录，线程间只读共享。
 * @property indexFile 当前歌曲索引文件，线程间只读共享。
 * @property index 当前内存索引，会随 span 写入和访问时间更新。
 */
internal class JvmPlaybackCacheEntry(
    val cacheKey: String,
    val directory: File,
    val indexFile: File,
    @Volatile var index: PlaybackCacheIndex,
) {
    /**
     * 条目级锁。
     *
     * 同一首歌可能同时被后台预取和前台播放回源写入，索引更新必须串行化。
     */
    val lock: Any = Any()
}

/**
 * 持久化到 index.json 的歌曲缓存索引。
 *
 * @property cacheKey 业务缓存 key，用于判断目录是否属于当前歌曲和播放参数。
 * @property sourceUrl 上游播放地址，用于避免同一歌曲换源后复用旧缓存。
 * @property totalBytes 媒体总字节数，单位是 byte。
 * @property contentType 上游返回的真实 Content-Type。
 * @property etag 上游 ETag 响应头，当前仅持久化备用。
 * @property lastModified 上游 Last-Modified 响应头，当前仅持久化备用。
 * @property spans 已完成写入的物理缓存片段列表。
 * @property completed 是否已经完整缓存整首媒体。
 * @property lastAccessTime 最近访问时间戳，单位是毫秒，用于 LRU 清理。
 */
@Serializable
internal data class PlaybackCacheIndex(
    val cacheKey: String,
    val sourceUrl: String,
    val totalBytes: Long,
    val contentType: String,
    val etag: String? = null,
    val lastModified: String? = null,
    val spans: List<CachedSpan> = emptyList(),
    val completed: Boolean = false,
    val lastAccessTime: Long = System.currentTimeMillis(),
)

/**
 * 一个已完成写入的缓存片段。
 *
 * @property start 片段在媒体中的起始字节位置，包含该位置。
 * @property endInclusive 片段在媒体中的结束字节位置，包含该位置。
 * @property fileName 片段对应的物理文件名，相对于歌曲缓存目录。
 */
@Serializable
internal data class CachedSpan(
    val start: Long,
    val endInclusive: Long,
    val fileName: String,
)
