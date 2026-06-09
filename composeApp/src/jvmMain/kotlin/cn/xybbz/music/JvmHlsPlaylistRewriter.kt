package cn.xybbz.music

import java.net.URI

/**
 * JVM HLS 播放列表重写器。
 *
 * 只处理播放缓存需要代理的 URI：media segment、child playlist、EXT-X-MAP 和 EXT-X-KEY。
 * 其它 HLS 标签保持原样，避免误改服务端私有扩展。
 */
internal object JvmHlsPlaylistRewriter {

    /**
     * 重写 HLS playlist 文本。
     *
     * @param playlistText 上游返回的原始 m3u8 文本。
     * @param playlistUrl 当前 m3u8 的原始地址，用来解析相对 URI。
     * @param resourceUrlBuilder 将真实资源地址包装成本地代理地址的构造函数。
     * @return 重写后的 playlist 文本，以及本次解析发现的资源清单。
     */
    fun rewrite(
        playlistText: String,
        playlistUrl: String,
        resourceUrlBuilder: (HlsPlaylistResource) -> String,
    ): HlsPlaylistRewriteResult {
        // 记录所有被代理的资源，供缓存层更新 index.json 和后台预取。
        val resources = mutableListOf<HlsPlaylistResource>()
        // 逐行构造新的 m3u8，未被代理的标签会原样写回。
        val rewrittenLines = mutableListOf<String>()
        // EXT-X-STREAM-INF 后一行是子 playlist URI，而不是媒体分片。
        var nextUriIsChildPlaylist = false
        // EXT-X-BYTERANGE 后一行通常引用同一文件的部分范围，不适合按整文件缓存。
        var nextUriHasByteRange = false

        playlistText.lineSequence().forEach { line ->
            val trimmed = line.trim()
            when {
                trimmed.startsWith("#EXT-X-I-FRAME-STREAM-INF") -> {
                    // I-frame playlist 的 URI 写在标签属性中，作为子 playlist 代理但不进入分片缓存。
                    rewrittenLines += rewriteUriAttribute(
                        line = line,
                        playlistUrl = playlistUrl,
                        kind = HlsResourceKind.PLAYLIST,
                        cacheable = false,
                        resources = resources,
                        resourceUrlBuilder = resourceUrlBuilder,
                    )
                    nextUriIsChildPlaylist = false
                    nextUriHasByteRange = false
                }

                trimmed.startsWith("#EXT-X-STREAM-INF") -> {
                    // 该标签自身没有 URI，下一条非标签行才是 variant playlist。
                    rewrittenLines += line
                    nextUriIsChildPlaylist = true
                    nextUriHasByteRange = false
                }

                trimmed.startsWith("#EXT-X-MEDIA") -> {
                    // AUDIO/SUBTITLES 等媒体组 URI 也属于子 playlist，保持可播放但不缓存为分片。
                    rewrittenLines += rewriteUriAttribute(
                        line = line,
                        playlistUrl = playlistUrl,
                        kind = HlsResourceKind.PLAYLIST,
                        cacheable = false,
                        resources = resources,
                        resourceUrlBuilder = resourceUrlBuilder,
                    )
                    nextUriHasByteRange = false
                }

                trimmed.startsWith("#EXT-X-KEY") -> {
                    // METHOD=NONE 表示不需要密钥文件，其它 key URI 可以代理并缓存。
                    val cacheable = "METHOD=NONE" !in trimmed.uppercase()
                    rewrittenLines += rewriteUriAttribute(
                        line = line,
                        playlistUrl = playlistUrl,
                        kind = HlsResourceKind.KEY,
                        cacheable = cacheable,
                        resources = resources,
                        resourceUrlBuilder = resourceUrlBuilder,
                    )
                    nextUriHasByteRange = false
                }

                trimmed.startsWith("#EXT-X-MAP") -> {
                    // 初始化片段如果带 BYTERANGE，就不能安全地按完整文件缓存。
                    rewrittenLines += rewriteUriAttribute(
                        line = line,
                        playlistUrl = playlistUrl,
                        kind = HlsResourceKind.MAP,
                        cacheable = "BYTERANGE=" !in trimmed.uppercase(),
                        resources = resources,
                        resourceUrlBuilder = resourceUrlBuilder,
                    )
                    nextUriHasByteRange = false
                }

                trimmed.startsWith("#EXT-X-BYTERANGE") -> {
                    // 下一个媒体 URI 会按范围读取，代理但不作为可缓存整文件。
                    rewrittenLines += line
                    nextUriHasByteRange = true
                }

                trimmed.startsWith("#") || trimmed.isBlank() -> {
                    // 未识别标签和空行保持原样，减少对服务端扩展字段的干扰。
                    rewrittenLines += line
                }

                else -> {
                    // 普通 URI 行可能是子 playlist，也可能是媒体分片，需要结合上下文判断。
                    val kind = if (nextUriIsChildPlaylist || looksLikePlaylistUri(trimmed)) {
                        HlsResourceKind.PLAYLIST
                    } else {
                        HlsResourceKind.SEGMENT
                    }
                    // m3u8 中的相对路径必须先还原为上游真实地址，再交给本地代理包装。
                    val resolvedUrl = resolveHlsUri(playlistUrl, trimmed)
                    val resource = HlsPlaylistResource(
                        url = resolvedUrl,
                        kind = kind,
                        cacheable = kind == HlsResourceKind.SEGMENT && !nextUriHasByteRange,
                    )
                    resources += resource
                    rewrittenLines += resourceUrlBuilder(resource)
                    // 当前 URI 已经消费掉上一行标签的上下文标记。
                    nextUriIsChildPlaylist = false
                    nextUriHasByteRange = false
                }
            }
        }

        return HlsPlaylistRewriteResult(
            text = rewrittenLines.joinToString("\n"),
            resources = resources,
        )
    }

    /**
     * 将 playlist 中的 URI 解析成可直接请求的绝对或规范化路径。
     *
     * HLS 允许绝对 URL、协议相对 URL、相对路径和根路径，这里统一处理。
     */
    fun resolveHlsUri(
        playlistUrl: String,
        uri: String,
    ): String {
        val trimmedUri = uri.trim()
        // 已经带 scheme 的地址无需依赖 playlistUrl。
        if (trimmedUri.hasScheme()) {
            return trimmedUri
        }
        // //cdn.example.com/a.ts 这种地址沿用当前 playlist 的 scheme。
        if (trimmedUri.startsWith("//")) {
            val baseScheme = runCatching { URI(playlistUrl).scheme }.getOrNull()
            return if (baseScheme.isNullOrBlank()) trimmedUri else "$baseScheme:$trimmedUri"
        }
        // 优先使用 java.net.URI 处理标准 URL 和相对路径。
        return runCatching {
            URI(playlistUrl).resolve(trimmedUri).normalize().toString()
        }.getOrElse {
            // playlistUrl 可能是服务端内部路径，不一定能被 URI 正常解析，失败时走手动兜底。
            resolveRelativePath(playlistUrl, trimmedUri)
        }
    }

    /**
     * 重写标签中的 URI 属性。
     */
    private fun rewriteUriAttribute(
        line: String,
        playlistUrl: String,
        kind: HlsResourceKind,
        cacheable: Boolean,
        resources: MutableList<HlsPlaylistResource>,
        resourceUrlBuilder: (HlsPlaylistResource) -> String,
    ): String {
        // 没有 URI 属性的标签无法代理，保持原样。
        val location = findUriAttributeValue(line) ?: return line
        val rawUri = line.substring(location.first, location.last + 1)
        // data/skd/urn 等特殊 URI 不是 HTTP 资源，不能交给本地代理。
        if (!rawUri.shouldProxy()) {
            return line
        }

        val resource = HlsPlaylistResource(
            url = resolveHlsUri(playlistUrl, rawUri),
            kind = kind,
            cacheable = cacheable,
        )
        resources += resource
        return line.replaceRange(location.first, location.last + 1, resourceUrlBuilder(resource))
    }

    /**
     * 查找标签里 URI= 的属性值范围。
     *
     * 支持带双引号和不带双引号两种 HLS 写法。
     */
    private fun findUriAttributeValue(line: String): IntRange? {
        val uriStart = line.indexOf("URI=")
        if (uriStart < 0) {
            return null
        }
        val valueStart = uriStart + "URI=".length
        if (valueStart >= line.length) {
            return null
        }
        return if (line[valueStart] == '"') {
            // URI="..."：范围只覆盖引号里的内容，替换时保留原引号。
            val start = valueStart + 1
            val end = line.indexOf('"', start)
            if (end <= start) null else start until end
        } else {
            // URI=...：值到下一个逗号或行尾结束。
            val comma = line.indexOf(',', valueStart)
            val endExclusive = if (comma < 0) line.length else comma
            if (endExclusive <= valueStart) null else valueStart until endExclusive
        }
    }

    /**
     * 判断 URI 是否适合交给本地 HTTP 代理。
     */
    private fun String.shouldProxy(): Boolean {
        if (isBlank()) {
            return false
        }
        val normalized = trim().lowercase()
        // DRM、自包含数据和 URN 不是普通网络资源，代理后反而会破坏播放器识别。
        return !normalized.startsWith("data:") &&
                !normalized.startsWith("skd:") &&
                !normalized.startsWith("urn:")
    }

    /**
     * 按 RFC 3986 的 scheme 字符规则判断字符串是否已经是绝对 URI。
     */
    private fun String.hasScheme(): Boolean {
        val schemeEnd = indexOf(':')
        if (schemeEnd <= 0) {
            return false
        }
        take(schemeEnd).forEachIndexed { index, char ->
            val valid = if (index == 0) {
                char.isLetter()
            } else {
                char.isLetterOrDigit() || char == '+' || char == '-' || char == '.'
            }
            if (!valid) {
                return false
            }
        }
        return true
    }

    /**
     * 根据路径后缀粗略判断普通 URI 行是否是子 playlist。
     */
    private fun looksLikePlaylistUri(uri: String): Boolean {
        val path = uri.substringBefore('?').substringBefore('#').lowercase()
        return path.endsWith(".m3u8") || path.endsWith(".m3u")
    }

    /**
     * 手动解析相对路径。
     *
     * 该逻辑用于处理 playlistUrl 不是标准绝对 URL 的情况，比如服务端返回的内部相对路径。
     */
    private fun resolveRelativePath(
        playlistUrl: String,
        uri: String,
    ): String {
        // 先去掉 query/fragment，路径拼接只能基于目录部分。
        val baseWithoutQuery = playlistUrl.substringBefore('?').substringBefore('#')
        val baseDirectory = baseWithoutQuery.substringBeforeLast('/', missingDelimiterValue = "")
        val combined = if (baseDirectory.isBlank()) uri else "$baseDirectory/$uri"
        val hasLeadingSlash = combined.startsWith("/")
        val normalizedParts = ArrayDeque<String>()
        combined.split('/').forEach { part ->
            when (part) {
                "", "." -> Unit
                // 遇到 .. 时回退一级目录。
                ".." -> if (normalizedParts.isNotEmpty()) normalizedParts.removeLast()
                else -> normalizedParts.addLast(part)
            }
        }
        return normalizedParts.joinToString(
            separator = "/",
            prefix = if (hasLeadingSlash) "/" else "",
        )
    }
}

/**
 * HLS playlist 重写结果。
 */
internal data class HlsPlaylistRewriteResult(
    /** 重写后的 m3u8 文本。 */
    val text: String,
    /** 本次重写过程中发现并代理的资源列表。 */
    val resources: List<HlsPlaylistResource>,
)

/**
 * HLS playlist 中的一个可代理资源。
 */
internal data class HlsPlaylistResource(
    /** 解析后的真实上游资源地址。 */
    val url: String,
    /** 资源类型，用来决定代理路由和缓存策略。 */
    val kind: HlsResourceKind,
    /** 是否允许把该资源完整写入本地缓存。 */
    val cacheable: Boolean,
)

/**
 * HLS 资源类型。
 *
 * [routeValue] 会写入本地代理 URL 的 type 参数中。
 */
internal enum class HlsResourceKind(val routeValue: String) {
    /** m3u8 播放列表，包含主列表和子列表。 */
    PLAYLIST("playlist"),
    /** 媒体分片，例如 ts、aac 或 fmp4 fragment。 */
    SEGMENT("segment"),
    /** 加密密钥文件。 */
    KEY("key"),
    /** fMP4 初始化片段。 */
    MAP("map");

    companion object {
        /**
         * 从本地代理 URL 的 type 参数还原资源类型。
         */
        fun fromRouteValue(value: String?): HlsResourceKind? {
            return entries.firstOrNull { kind -> kind.routeValue == value }
        }
    }
}
