package cn.xybbz.api.enums.navidrome

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
enum class OrderType(private val orderName:String) {

    ASC("ASC"),
    DESC("DESC");

    override fun toString(): String {
        return orderName
    }
}