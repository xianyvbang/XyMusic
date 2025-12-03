package cn.xybbz.api.adapter

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.LocalDate

/**
 * LocalDate适配器
 * 将字符串转换成LocalDate/将LocalDate转换成字符串
 * @author xybbz
 * @date 2025/12/02
 * @constructor 创建[LocalDateAdapter]
 */
class LocalDateAdapter {
    @FromJson
    fun fromJson(json: String): LocalDate = LocalDate.parse(json)

    @ToJson
    fun toJson(value: LocalDate): String = value.toString()
}
