package cn.xybbz.api.client

import com.github.promeg.pinyinhelper.Pinyin

/**
 * 汉字转拼音
 */
actual fun toLatinCompat(text: String): String? {
    if (text.isBlank()) return null
    return Pinyin.toPinyin(text[0])
}