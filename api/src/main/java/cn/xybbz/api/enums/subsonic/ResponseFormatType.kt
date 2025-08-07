package cn.xybbz.api.enums.subsonic

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
enum class ResponseFormatType (val serialName: String){
    XML("xml"),
    JSON("json"),
    JSONP("jsonp");

    override fun toString(): String = serialName
}