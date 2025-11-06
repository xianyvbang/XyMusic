package cn.xybbz.common.utils

import android.annotation.SuppressLint
import android.icu.util.Calendar
import android.text.format.DateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtil {

    /**
     * 时间戳转换成字符窜
     * @param pattern 时间样式 yyyy-MM-dd HH:mm:ss
     * @return [String] 时间字符串
     */
    @SuppressLint("SimpleDateFormat")
    fun Long.toDateStr(pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
        val inTimeInMillis = if (this < 1_000_000_000_000L) this * 1000 else this
        return DateFormat.format(pattern, inTimeInMillis).toString()
    }

    /**
     * 将字符串转为时间戳
     * @param pattern 时间样式 yyyy-MM-dd HH:mm:ss
     * @return [String] 时间字符串
     */
    fun String.toDateLong(pattern: String = "yyyy-MM-dd HH:mm:ss"): Long {
        val formatter = DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
        val localDateTime = LocalDateTime.parse(this, formatter)
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
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
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, day, hour, minute, second)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun millisecondsToTime(milliseconds: Long): String {
        val instant = Instant.ofEpochMilli(milliseconds)
        val formatter = DateTimeFormatter.ofPattern("mm:ss").withZone(ZoneId.systemDefault())
        return formatter.format(instant)
    }

    /**
     * 获得当前年
     */
    fun thisYear(): Int {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        return year
    }

    /**
     * 获得1900到当前年的Set列表
     */
    fun getYearSet(): Set<Int> {
        val years: Set<Int> = (1900..thisYear()).toSet()
        return years
    }
}