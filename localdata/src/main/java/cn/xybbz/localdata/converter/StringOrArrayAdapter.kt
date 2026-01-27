package cn.xybbz.localdata.converter

import cn.xybbz.localdata.common.LocalConstants
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

class StringOrArrayAdapter {

    @FromJson
    fun fromJson(str: String?): List<String>? {
        return str?.split(LocalConstants.ARTIST_DELIMITER)
    }

    @ToJson
    fun toJson(value: List<String>?): String? {
        return value?.joinToString(LocalConstants.ARTIST_DELIMITER)
    }
}
