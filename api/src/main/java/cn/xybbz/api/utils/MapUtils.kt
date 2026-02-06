/*
 *   XyMusic
 *   Copyright (C) 2023 xianyvbang
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package cn.xybbz.api.utils

import android.util.Log
import cn.xybbz.api.converter.json
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive


/**
 * 把任意 @Serializable 对象转换成 Map<String, String>
 * 适用于 Retrofit @QueryMap
 */
inline fun <reified T> T.toQueryMap(
    isConvertList: Boolean = true,
    serializer: KSerializer<T>,
): Map<String, String> where T : Any {

    // 1. 把对象序列化为 JsonElement
//    val jsonElement = json.encodeToJsonElement(PolymorphicSerializer(T::class), this)
    val jsonElement = json.encodeToJsonElement(serializer, this)

    if (jsonElement !is JsonObject) return emptyMap()
    val currentTimeMillis = System.currentTimeMillis()
    Log.i("=======","转换开始时间: $currentTimeMillis")
    val toMap = jsonElement.toMap()
    val mapNotNull = toMap.mapNotNull { (key, value) ->
        val stringValue = when (value) {
            is JsonPrimitive -> value.content
            is JsonArray -> if (isConvertList) value.filter { !it.jsonPrimitive.contentOrNull.isNullOrEmpty() }
                .joinToString(",") { it.jsonPrimitive.content } else value.toString() // List -> CSV
            else -> value.toString()
        }
        key to stringValue
    }.filter { it.second.isNotEmpty() }
    val toMutableMap = mapNotNull.toMap().toMutableMap()
    toMutableMap.remove("type")
    Log.i("=======","转换结束时间: ${System.currentTimeMillis() - currentTimeMillis}")
    return toMutableMap
}



inline fun <reified T> T.toQueryMap(
    isConvertList: Boolean = true,
): Map<String, String> where T : Any {

    // 1. 把对象序列化为 JsonElement
    val jsonElement = json.encodeToJsonElement(PolymorphicSerializer(T::class), this)
//    val jsonElement = json.encodeToJsonElement(serializer, this)

    if (jsonElement !is JsonObject) return emptyMap()
    val currentTimeMillis = System.currentTimeMillis()
    Log.i("=======","转换开始时间: $currentTimeMillis")
    val toMap = jsonElement.toMap()
    val mapNotNull = toMap.mapNotNull { (key, value) ->
        val stringValue = when (value) {
            is JsonPrimitive -> value.content
            is JsonArray -> if (isConvertList) value.filter { !it.jsonPrimitive.contentOrNull.isNullOrEmpty() }
                .joinToString(",") { it.jsonPrimitive.content } else value.toString() // List -> CSV
            else -> value.toString()
        }
        key to stringValue
    }.filter { it.second.isNotEmpty() }
    val toMutableMap = mapNotNull.toMap().toMutableMap()
    toMutableMap.remove("type")
    Log.i("=======","转换结束时间: ${System.currentTimeMillis() - currentTimeMillis}")
    return toMutableMap
}