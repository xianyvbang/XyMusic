package cn.xybbz.music

import java.net.URI

/**
 * JVM HLS 播放列表重写器。
 *
 * 只处理播放缓存需要代理的 URI：media segment、child playlist、EXT-X-MAP 和 EXT-X-KEY。
 * 其它 HLS 标签保持原样，避免误改服务端私有扩展。
 */
internal object JvmHlsPlaylistRewriter {

    fun rewrite(
        playlistText: String,
        playlistUrl: String,
        resourceUrlBuilder: (HlsPlaylistResource) -> String,
    ): HlsPlaylistRewriteResult {
        val resources = mutableListOf<HlsPlaylistResource>()
        val rewrittenLines = mutableListOf<String>()
        var nextUriIsChildPlaylist = false
        var nextUriHasByteRange = false

        playlistText.lineSequence().forEach { line ->
            val trimmed = line.trim()
            when {
                trimmed.startsWith("#EXT-X-I-FRAME-STREAM-INF") -> {
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
                    rewrittenLines += line
                    nextUriIsChildPlaylist = true
                    nextUriHasByteRange = false
                }

                trimmed.startsWith("#EXT-X-MEDIA") -> {
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
                    rewrittenLines += line
                    nextUriHasByteRange = true
                }

                trimmed.startsWith("#") || trimmed.isBlank() -> {
                    rewrittenLines += line
                }

                else -> {
                    val kind = if (nextUriIsChildPlaylist || looksLikePlaylistUri(trimmed)) {
                        HlsResourceKind.PLAYLIST
                    } else {
                        HlsResourceKind.SEGMENT
                    }
                    val resolvedUrl = resolveHlsUri(playlistUrl, trimmed)
                    val resource = HlsPlaylistResource(
                        url = resolvedUrl,
                        kind = kind,
                        cacheable = kind == HlsResourceKind.SEGMENT && !nextUriHasByteRange,
                    )
                    resources += resource
                    rewrittenLines += resourceUrlBuilder(resource)
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

    fun resolveHlsUri(
        playlistUrl: String,
        uri: String,
    ): String {
        val trimmedUri = uri.trim()
        if (trimmedUri.hasScheme()) {
            return trimmedUri
        }
        if (trimmedUri.startsWith("//")) {
            val baseScheme = runCatching { URI(playlistUrl).scheme }.getOrNull()
            return if (baseScheme.isNullOrBlank()) trimmedUri else "$baseScheme:$trimmedUri"
        }
        return runCatching {
            URI(playlistUrl).resolve(trimmedUri).normalize().toString()
        }.getOrElse {
            resolveRelativePath(playlistUrl, trimmedUri)
        }
    }

    private fun rewriteUriAttribute(
        line: String,
        playlistUrl: String,
        kind: HlsResourceKind,
        cacheable: Boolean,
        resources: MutableList<HlsPlaylistResource>,
        resourceUrlBuilder: (HlsPlaylistResource) -> String,
    ): String {
        val location = findUriAttributeValue(line) ?: return line
        val rawUri = line.substring(location.first, location.last + 1)
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
            val start = valueStart + 1
            val end = line.indexOf('"', start)
            if (end <= start) null else start until end
        } else {
            val comma = line.indexOf(',', valueStart)
            val endExclusive = if (comma < 0) line.length else comma
            if (endExclusive <= valueStart) null else valueStart until endExclusive
        }
    }

    private fun String.shouldProxy(): Boolean {
        if (isBlank()) {
            return false
        }
        val normalized = trim().lowercase()
        return !normalized.startsWith("data:") &&
                !normalized.startsWith("skd:") &&
                !normalized.startsWith("urn:")
    }

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

    private fun looksLikePlaylistUri(uri: String): Boolean {
        val path = uri.substringBefore('?').substringBefore('#').lowercase()
        return path.endsWith(".m3u8") || path.endsWith(".m3u")
    }

    private fun resolveRelativePath(
        playlistUrl: String,
        uri: String,
    ): String {
        val baseWithoutQuery = playlistUrl.substringBefore('?').substringBefore('#')
        val baseDirectory = baseWithoutQuery.substringBeforeLast('/', missingDelimiterValue = "")
        val combined = if (baseDirectory.isBlank()) uri else "$baseDirectory/$uri"
        val hasLeadingSlash = combined.startsWith("/")
        val normalizedParts = ArrayDeque<String>()
        combined.split('/').forEach { part ->
            when (part) {
                "", "." -> Unit
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

internal data class HlsPlaylistRewriteResult(
    val text: String,
    val resources: List<HlsPlaylistResource>,
)

internal data class HlsPlaylistResource(
    val url: String,
    val kind: HlsResourceKind,
    val cacheable: Boolean,
)

internal enum class HlsResourceKind(val routeValue: String) {
    PLAYLIST("playlist"),
    SEGMENT("segment"),
    KEY("key"),
    MAP("map");

    companion object {
        fun fromRouteValue(value: String?): HlsResourceKind? {
            return entries.firstOrNull { kind -> kind.routeValue == value }
        }
    }
}
