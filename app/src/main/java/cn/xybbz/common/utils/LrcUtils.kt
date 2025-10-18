package cn.xybbz.common.utils

import android.text.TextUtils
import android.text.format.DateUtils
import cn.xybbz.entity.dto.NetworkLrcDto
import cn.xybbz.ui.components.LrcEntry
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.regex.Pattern


object LrcUtils {

    const val LRC_SUFFIX = "lrc"
    const val TXT_SUFFIX = "txt"

    /**
     * 从文本解析歌词
     */
    fun parseLrc(lrcText: String): List<LrcEntry> {
        if (TextUtils.isEmpty(lrcText)) {
            return emptyList()
        }
        val entryList: MutableList<LrcEntry> = mutableListOf()
        val array = lrcText.lines()
        for (line in array) {
            val list: List<LrcEntry> = parseLine(line)
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
    fun parseLine(lineText: String): List<LrcEntry> {
        var line = lineText
        if (TextUtils.isEmpty(line)) {
            return listOf()
        }
        line = line.trim()
        // [00:07]
        val lineMatcher = PATTERN_LINE.matcher(line)
        if (!lineMatcher.matches()) {
            return listOf()
        }
        val times = lineMatcher.group(1) ?: ""
        val text = lineMatcher.group(3)
        val entryList: MutableList<LrcEntry> = ArrayList()

        // [00:17]
        val timeMatcher = PATTERN_TIME.matcher(times)
        while (timeMatcher.find()) {
            val min = timeMatcher.group(1)?.toLong() ?: 0
            val sec = timeMatcher.group(2)?.toLong() ?: 0
            val milString = timeMatcher.group(3) ?: ""
            var mil = milString.toLong()
            // 如果毫秒是两位数，需要乘以10
            if (milString.length == 2) {
                mil *= 10
            }
            val time = min * DateUtils.MINUTE_IN_MILLIS + sec * DateUtils.SECOND_IN_MILLIS + mil
            entryList.add(LrcEntry(time, text ?: ""))
        }
        return entryList
    }

    /**
     * 转为[分:秒]
     */
    fun formatTime(milli: Long): String {
        val m = (milli / DateUtils.MINUTE_IN_MILLIS).toInt()
        val s = (milli / DateUtils.SECOND_IN_MILLIS % 60).toInt()
        val mm = String.format(Locale.getDefault(), "%02d", m)
        val ss = String.format(Locale.getDefault(), "%02d", s)
        return "$mm:$ss"
    }


    private val PATTERN_LINE = Pattern.compile("((\\[\\d\\d:\\d\\d\\.\\d{2,3}\\])+)(.+)")
    private val PATTERN_TIME = Pattern.compile("\\[(\\d\\d):(\\d\\d)\\.(\\d{2,3})\\]")

    /**
     * 从文件解析双语歌词
     */
    fun parseLrc(lrcFiles: Array<File?>?): List<LrcEntry>? {
        if (lrcFiles == null || lrcFiles.size != 2 || lrcFiles[0] == null) {
            return null
        }
        val mainLrcFile = lrcFiles[0]
        val secondLrcFile = lrcFiles[1]
        val mainEntryList = parseLrc(mainLrcFile)
        val secondEntryList = parseLrc(secondLrcFile)
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

    /**
     * 从文件解析歌词
     */
    private fun parseLrc(lrcFile: File?): List<LrcEntry>? {
        if (lrcFile == null || !lrcFile.exists()) {
            return null
        }
        val entryList: MutableList<LrcEntry> = ArrayList()
        try {
            val br = BufferedReader(InputStreamReader(FileInputStream(lrcFile), "utf-8"))
            var line: String
            while (br.readLine().also { line = it } != null) {
                val list: List<LrcEntry>? = parseLine(line)
                if (!list.isNullOrEmpty()) {
                    entryList.addAll(list)
                }
            }
            br.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        entryList.sort()
        return entryList
    }

    /**
     * 从文本解析双语歌词
     */
    fun parseLrc(lrcTexts: Array<String>?): List<LrcEntry>? {
        if (lrcTexts == null || lrcTexts.size != 2 || TextUtils.isEmpty(lrcTexts[0])) {
            return null
        }
        val mainLrcText = lrcTexts[0]
        val secondLrcText = lrcTexts[1]
        val mainEntryList: List<LrcEntry>? = parseLrc(mainLrcText)
        val secondEntryList: List<LrcEntry>? = parseLrc(secondLrcText)
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


    /**
     * 获取网络文本，需要在工作线程中执行
     */
   private fun getContentFromNetwork(url: String?, charset: String? = null): NetworkLrcDto {
        //歌词数据
        var lrcText = ""
        //是否返回数据
        var ifOk = false
        try {
            val lrcUrl = URL(url)
            val conn = lrcUrl.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            if (conn.responseCode == 200) {
                val `is` = conn.inputStream
                val bos = ByteArrayOutputStream()
                val buffer = ByteArray(1024)
                var len: Int
                while (`is`.read(buffer).also { len = it } != -1) {
                    bos.write(buffer, 0, len)
                }
                `is`.close()
                bos.close()
                lrcText = bos.toString(charset ?: "utf-8")
            }
            if (lrcText.isNotBlank()){
                ifOk = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return NetworkLrcDto(ifOk,lrcText)
    }

}