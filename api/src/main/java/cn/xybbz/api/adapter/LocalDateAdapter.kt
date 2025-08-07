package cn.xybbz.api.adapter

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.LocalDate

class LocalDateAdapter {
    @FromJson
    fun fromJson(json: String): LocalDate = LocalDate.parse(json)

    @ToJson
    fun toJson(value: LocalDate): String = value.toString()
}
