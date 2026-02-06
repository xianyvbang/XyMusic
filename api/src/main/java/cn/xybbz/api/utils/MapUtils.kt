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

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonPrimitive

/**
 * 把任意 @Serializable 对象转换成 Map<String, String>
 * 适用于 Retrofit @QueryMap
 */
inline fun <reified T> T.toQueryMap(
    isConvertList: Boolean = true
): Map<String, String> where T : Any {
    // 1. 把对象序列化为 JsonElement
    val jsonElement = Json.encodeToJsonElement<T>(this)

    if (jsonElement !is JsonObject) return emptyMap()

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
    return mapNotNull.toMap()
}