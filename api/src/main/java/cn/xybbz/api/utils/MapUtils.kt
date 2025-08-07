package cn.xybbz.api.utils

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.squareup.moshi.Types

fun <T> convertToMap(dataClass: T,isConvertList: Boolean = true): Map<String, String> {
    val moshi = Moshi.Builder().add(IntOrDoubleAdapter()).build()
    val type = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
    val adapter: JsonAdapter<Map<String, Any>> = moshi.adapter(type)
    val dataJson = moshi.adapter<T>(dataClass!!::class.java).toJson(dataClass)
    val fromJson = adapter.fromJson(dataJson)
    if (isConvertList){
        return (fromJson ?: emptyMap()).toValueString().filterValues { it.isNotBlank() }
    }else {
        return (fromJson ?: emptyMap()).mapValues { it.value.toString() }.filterValues { it.isNotBlank() }
    }
}

fun Map<String, Any>.toValueString(): Map<String, String> {
    return this.mapValues {
        when (it.value) {
            is List<*> -> (it.value as List<*>).joinToString(",")  // 转换为逗号分隔的字符串
            else -> it.value.toString()
        }
    }
}

class IntOrDoubleAdapter : JsonAdapter<Int>() {
    @FromJson
    override fun fromJson(reader: JsonReader): Int {
        val value = reader.nextString()
        return value.toInt()
    }

    @ToJson
    override fun toJson(writer: JsonWriter, value: Int?) {
        writer.value(value?.toString())
    }
}