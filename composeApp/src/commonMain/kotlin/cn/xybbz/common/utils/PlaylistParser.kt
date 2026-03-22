package cn.xybbz.common.utils

import java.io.File
import java.nio.charset.Charset

/**
 * 播放列表解析工具类
 * 支持 M3U8 / TXT 格式
 */
object PlaylistParser {
    data class Track(
        val title: String,   // 歌曲标题（可选）
        val artist: String? = null,  // 艺术家（可选）
        val duration: Int? = null,   // 时长（秒，可选）
        val path: String? = null             // 本地路径或 URL
    )

    data class Playlist(
        val musicList: List<Track>, val playlistName: String
    )

    /**
     * 解析 M3U8 文件
     */
    fun parseM3U(lines: List<String>): List<Track> {
        val result = mutableListOf<Track>()

        var title: String = ""
        var artist: String? = null
        var duration: Int? = null

        for (line in lines) {
            when {
                line.startsWith("#EXTINF", true) -> {
                    // 格式: #EXTINF:123,Artist - Title
                    val info = line.removePrefix("#EXTINF:").trim()
                    val parts = info.split(",", limit = 2)
                    duration = parts.getOrNull(0)?.toIntOrNull()
                    val titleAndArtist = parts.getOrNull(1)?.trim()?.split("-", limit = 2)
                    title = titleAndArtist?.getOrNull(0)?.trim() ?: ""
                    artist = titleAndArtist?.getOrNull(0)?.trim()
                }

                line.isNotEmpty() && !line.startsWith("#") -> {
                    // 歌曲路径
                    result.add(Track(title, artist, duration, line))
                    title = ""
                    duration = null
                    artist = null
                }
            }
        }
        return result
    }

    /**
     * 导出 M3U8 文件
     */
    fun exportM3U(tracks: List<Track>): String {
        val builder = StringBuilder()
        builder.appendLine("#EXTM3U")
        for (track in tracks) {
            builder.appendLine("#EXTINF:${track.duration ?: -1},${track.title}")
            builder.appendLine(track.path)
        }
        return builder.toString()
    }

    /**
     * 解析 TXT 文件
     * 每行一个路径/URL
     */
    fun parseTxt(lines: List<String>): List<Track> {
        return lines.mapNotNull { line ->
            val path = line.trim()
            val titleAndArtist = line.split("-", limit = 2)
            val title = titleAndArtist.getOrNull(1)
            val artist = titleAndArtist.getOrNull(0)
            if (path.isNotEmpty()) Track(title = title ?: "", artist = artist) else null
        }
    }

    /**
     * 导出 TXT 文件
     */
    fun exportTxt(tracks: List<Track>):String {
        return tracks.joinToString("\n") { it.title }
    }
}
