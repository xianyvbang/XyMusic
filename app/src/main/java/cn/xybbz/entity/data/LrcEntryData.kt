package cn.xybbz.entity.data

import androidx.compose.runtime.Stable
import cn.xybbz.common.utils.LrcUtils


/**
 * 歌词数据对象
 * @author 刘梦龙
 * @date 2025/04/27
 * @constructor 创建[LrcEntryData]
 * @param [startTime] 歌词开始时间
 * @param [text] 歌词主文本
 * @param [secondText] 歌词翻译文本
 * @param [wordTimings] 逐字时间
 */
@Stable
data class LrcEntryData(
    val startTime: Long,
    val text: String,
    var secondText: String = "",
    //todo 这个字段没有赋值
    val wordTimings: List<Long> = emptyList(),
) : Comparable<LrcEntryData> {
    var endTime: Long = 0

    //分秒 mm:ss
    val startTimeFormat: String get() = LrcUtils.formatTime(startTime)

    //该行显示的歌词
    val displayText: String get() = if (secondText.isNotBlank()) "${text}\n$secondText" else text

    override fun compareTo(other: LrcEntryData): Int {
        val l = startTime - other.startTime
        return if (l > 0) 1 else if (l == 0L) 0 else -1
    }
}