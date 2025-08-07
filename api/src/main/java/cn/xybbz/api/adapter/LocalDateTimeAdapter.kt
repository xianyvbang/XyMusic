package cn.xybbz.api.adapter

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class LocalDateTimeAdapter : JsonAdapter<LocalDateTime>() {

    private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    @FromJson
    override fun fromJson(reader: JsonReader): LocalDateTime? {
        return if (reader.peek() == JsonReader.Token.NULL) {
            reader.nextNull()
        } else {
            val dateTimeString = reader.nextString()
            // 将带时区的字符串解析为 OffsetDateTime，然后转为 LocalDateTime
            OffsetDateTime.parse(dateTimeString, formatter).toLocalDateTime()
        }
    }

    @ToJson
    override fun toJson(writer: JsonWriter, value: LocalDateTime?) {
        if (value == null) {
            writer.nullValue()
        } else {
            // 如果需要，可以选择将 LocalDateTime 转换为 ISO 8601 格式的字符串
            writer.value(value.toString())
        }
    }
}