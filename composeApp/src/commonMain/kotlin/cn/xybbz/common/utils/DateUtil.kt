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

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.math.roundToInt
import kotlin.time.Clock
import kotlin.time.Instant

object DateUtil {
    private val systemTimeZone: TimeZone
        get() = TimeZone.currentSystemDefault()

    @OptIn(FormatStringsInDatetimeFormats::class)
    private fun dateTimeFormat(pattern: String) = LocalDateTime.Format {
        byUnicodePattern(pattern)
    }

    private fun Long.toEpochMilliseconds(): Long {
        return if (this < 1_000_000_000_000L) this * 1000 else this
    }

    /**
     * 时间戳转换成字符窜
     * @param pattern 时间样式 yyyy-MM-dd HH:mm:ss
     * @return [String] 时间字符串
     */
    fun Long.toDateStr(pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
        val localDateTime = Instant
            .fromEpochMilliseconds(toEpochMilliseconds())
            .toLocalDateTime(systemTimeZone)
        return dateTimeFormat(pattern).format(localDateTime)
    }

    /**
     * 将字符串转为时间戳
     * @param pattern 时间样式 yyyy-MM-dd HH:mm:ss
     * @return [String] 时间字符串
     */
    fun String.toDateLong(pattern: String = "yyyy-MM-dd HH:mm:ss"): Long {
        val localDateTime = LocalDateTime.parse(this, dateTimeFormat(pattern))
        return localDateTime.toInstant(systemTimeZone).toEpochMilliseconds()
    }

    /**
     * 根据年月日获取时间戳
     * @param year 年
     * @param month 月
     * @param day 日
     * @return [Long] 时间戳
     */
    fun getDateFromYMD(year: Int, month: Int, day: Int): Long {
        return getDateFromYMDHMS(year, month, day, 0, 0, 0)
    }

    /**
     * 根据年月日时分秒获取时间戳
     * @param year Int 年
     * @param month Int 月
     * @param day Int 日
     * @param hour Int 时
     * @param minute Int 分
     * @param second Int 秒
     * @return [Long] 时间戳
     */
    fun getDateFromYMDHMS(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        second: Int
    ): Long {
        return LocalDateTime(
            year = year,
            month = month,
            day = day,
            hour = hour,
            minute = minute,
            second = second
        ).toInstant(systemTimeZone).toEpochMilliseconds()
    }

    fun millisecondsToTime(milliseconds: Long): String {
        val totalSeconds = (milliseconds / 1000).coerceAtLeast(0)
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    }

    /**
     * 获得当前年
     */
    fun thisYear(): Int {
        return Clock.System.now().toLocalDateTime(systemTimeZone).year
    }

    /**
     * 获得1900到当前年的Set列表
     */
    fun getYearSet(): Set<Int> {
        val years: Set<Int> = (1900..thisYear()).toSet()
        return years
    }

    /**
     * 将毫秒转换成类似1s300ms的形式
     */
    fun Long.toSecondMsString(): String {
        val seconds = this / 1000
        val ms = this % 1000
        return when {
            seconds > 0 && ms > 0 -> "${seconds}s${ms}ms"
            seconds > 0 -> "${seconds}s"
            else -> "${ms}ms"
        }
    }

    fun Long.toSecondMs(): Float {
        return ((this.toDouble() / 1000.0) * 100).roundToInt() / 100f
    }

}
