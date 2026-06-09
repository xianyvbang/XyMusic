package cn.xybbz.music

import cn.xybbz.common.utils.Log
import cn.xybbz.common.utils.LrcUtils
import cn.xybbz.entity.data.LrcEntryData
import cn.xybbz.localdata.data.music.XyPlayMusic
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import java.io.File

/**
 * JVM 本地音频内嵌歌词读取器。
 *
 * 只处理桌面端已经下载到本地的音频文件，远程流仍交给音乐服务或自定义歌词接口兜底。
 */
class JvmLyricsMetadataReader {

    /**
     * 从本地音频文件标签中读取 LRC 歌词。
     *
     * @return 解析后的歌词行；文件不存在、没有歌词或歌词不是可解析 LRC 时返回 null。
     */
    fun readEmbeddedLyrics(music: XyPlayMusic): List<LrcEntryData>? {
        // 只有本地文件才读取内嵌标签；远程 URL 不在这里下载或解析。
        val filePath = music.filePath?.takeIf { it.isNotBlank() } ?: return null
        val file = File(filePath)
        if (!file.isFile) {
            return null
        }

        return runCatching {
            // jaudiotagger 会按音频格式读取标准歌词字段，统一从 FieldKey.LYRICS 取文本。
            val lyrics = AudioFileIO.read(file)
                .tag
                ?.getFirst(FieldKey.LYRICS)
                ?.takeIf { it.isNotBlank() }
                ?: return null
            // 当前播放页只展示带时间戳的 LRC，纯文本歌词解析为空后继续走网络兜底。
            LrcUtils.parseLrc(lyrics).takeIf { it.isNotEmpty() }
        }.onFailure {
            Log.e("JvmLyricsMetadataReader", "读取本地内嵌歌词失败: ${music.itemId}", it)
        }.getOrNull()
    }
}
