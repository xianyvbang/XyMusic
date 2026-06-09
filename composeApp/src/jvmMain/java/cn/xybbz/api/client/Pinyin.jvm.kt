package cn.xybbz.api.client

import com.github.promeg.pinyinhelper.Pinyin

actual fun toLatinCompat(text: String): String? {
    if (text.isBlank()) return null
    return Pinyin.toPinyin(text[0])
}