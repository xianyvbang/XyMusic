/*
 *   XyMusic
 *   Copyright (C) 2023 xianyvbang
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package cn.xybbz.common.utils

import cn.xybbz.entity.data.LrcEntryData

object LrcUtils {
    private const val SECOND_IN_MILLIS = 1_000L
    private const val MINUTE_IN_MILLIS = 60_000L

    /**
     * 从文本解析歌词
     */
    fun parseLrc(lrcText: String): List<LrcEntryData> {
        if (lrcText.isEmpty()) {
            return emptyList()
        }
        val entryList: MutableList<LrcEntryData> = mutableListOf()
        val array = lrcText.lines()
        for (line in array) {
            val list: List<LrcEntryData> = parseLine(line)
            if (list.isNotEmpty()) {
                entryList.addAll(list)
            }
        }
        //排序
        val list = entryList.sortedBy { it.startTime }
        for (i in list.indices) {
            if (i == list.size - 1) {
                list[i].endTime = Long.MAX_VALUE
            } else {
                list[i].endTime = list[i + 1].startTime
            }

        }
        return list
    }

    /**
     * 解析一行歌词
     */
    fun parseLine(lineText: String): List<LrcEntryData> {
        if (lineText.isEmpty()) {
            return listOf()
        }
        val line = lineText.trim()
        // [00:07]
        val lineMatch = PATTERN_LINE.matchEntire(line) ?: return listOf()
        val times = lineMatch.groupValues[1]
        val text = lineMatch.groupValues[3]
        if (times.isEmpty()) {
            return listOf()
        }
        val entryList: MutableList<LrcEntryData> = ArrayList()

        // [00:17]
        PATTERN_TIME.findAll(times).forEach { matchResult ->
            val min = matchResult.groupValues[1].toLongOrNull() ?: 0
            val sec = matchResult.groupValues[2].toLongOrNull() ?: 0
            val milString = matchResult.groupValues[3]
            var mil = milString.toLong()
            // 如果毫秒是两位数，需要乘以10
            if (milString.length == 2) {
                mil *= 10
            }
            val time = min * MINUTE_IN_MILLIS + sec * SECOND_IN_MILLIS + mil
            entryList.add(LrcEntryData(time, text))
        }
        return entryList
    }

    /**
     * 转为[分:秒]
     */
    fun formatTime(milli: Long): String {
        val m = (milli / MINUTE_IN_MILLIS).toInt()
        val s = (milli / SECOND_IN_MILLIS % 60).toInt()
        val mm = m.toString().padStart(2, '0')
        val ss = s.toString().padStart(2, '0')
        return "$mm:$ss"
    }


    private val PATTERN_LINE = Regex("((\\[\\d\\d:\\d\\d\\.\\d{2,3}])+)(.+)")
    private val PATTERN_TIME = Regex("\\[(\\d\\d):(\\d\\d)\\.(\\d{2,3})]")

    /**
     * 从文本解析双语歌词
     */
    fun parseLrc(lrcTexts: Array<String>?): List<LrcEntryData>? {
        if (lrcTexts == null || lrcTexts.size != 2 || lrcTexts[0].isEmpty()) {
            return null
        }
        val mainLrcText = lrcTexts[0]
        val secondLrcText = lrcTexts[1]
        val mainEntryList: List<LrcEntryData>? = parseLrc(mainLrcText)
        val secondEntryList: List<LrcEntryData>? = parseLrc(secondLrcText)
        if (mainEntryList != null && secondEntryList != null) {
            for (mainEntry in mainEntryList) {
                for (secondEntry in secondEntryList) {
                    if (mainEntry.startTime == secondEntry.startTime) {
                        mainEntry.secondText = secondEntry.text
                    }
                }
            }
        }
        return mainEntryList
    }


    fun List<LrcEntryData>.getIndex(progress: Long, offsetMs: Long): Int {
        val index =
            this.indexOfFirst { item -> (item.startTime + offsetMs) <= progress && progress < (item.endTime + offsetMs) }
        return index
    }

}
