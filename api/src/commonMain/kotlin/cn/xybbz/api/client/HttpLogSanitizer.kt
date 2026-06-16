package cn.xybbz.api.client

import cn.xybbz.api.constants.ApiConstants

/**
 * 网络日志脱敏工具，统一屏蔽认证头和常见 token 查询参数。
 */
object HttpLogSanitizer {
    private const val REDACTED = "***"

    private val sensitiveHeaders = setOf(
        ApiConstants.AUTHORIZATION.lowercase(),
        "x-emby-authorization",
        ApiConstants.EMBY_AUTHORIZATION.lowercase(),
        ApiConstants.PLEX_AUTHORIZATION.lowercase(),
        ApiConstants.PROXY_AUTHORIZATION.lowercase()
    )

    private val sensitiveQueryNames = setOf(
        "token",
        "api_key",
        "x-plex-token"
    )

    /**
     * 对 Ktor 原始日志文本做脱敏。
     */
    fun sanitize(message: String): String {
        return message
            .replaceHeaderTokens()
            .replaceInlineSensitiveAssignments()
            .replaceQueryTokens()
    }

    private fun String.replaceHeaderTokens(): String {
        var next = this
        sensitiveHeaders.forEach { headerName ->
            next = next.replace(
                Regex(
                    "(?i)($headerName\\s*[:=]\\s*)(.*?)(?=(,\\s*[A-Za-z-]+\\s*[:=])|\\s+[A-Z]+\\s+https?://|\\r?\\n|$|\\])"
                ),
                "$1$REDACTED"
            )
        }
        return next
    }

    private fun String.replaceQueryTokens(): String {
        var next = this
        sensitiveQueryNames.forEach { queryName ->
            next = next.replace(
                Regex("(?i)([?&]$queryName=)([^&\\s]+)"),
                "$1$REDACTED"
            )
        }
        return next
    }

    /**
     * 对 header 值中的内联 token 片段做补充脱敏。
     */
    private fun String.replaceInlineSensitiveAssignments(): String {
        return this
            .replace(
                Regex("(?i)(\\btoken\\s*=\\s*)([^,&\\s\\]]+)"),
                "$1$REDACTED"
            )
            .replace(
                Regex("(?i)(\\bapi_key\\s*=\\s*)([^,&\\s\\]]+)"),
                "$1$REDACTED"
            )
    }

    /**
     * 对 URL 查询参数做脱敏，供业务日志拼接 URL 时复用。
     */
    fun sanitizeUrl(url: String): String {
        return url.replaceQueryTokens()
    }
}
