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

import cn.xybbz.api.converter.jsonSerializer
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonPrimitive

/**
 * 到字符串映射
 * @param [isConvertList] 是转换列表 true 将列表使用逗号(,)分隔并转换成字符串,false的时候多个相同的key对应多个不同的值
 * @return [List<Pair<String, String>>]
 */
inline fun <reified T> T.toStringMap(
    isConvertList: Boolean = false
): List<Pair<String, String>> {
    val jsonElement = jsonSerializer.encodeToJsonElement(this)
    if (jsonElement !is JsonObject) return emptyList()
    //改为Pair
    return jsonElement.convertToPairs(isConvertList)
}


inline fun <reified T> T.toListMap(
    isConvertList: Boolean = false
): Array<Pair<String, Iterable<String>>> {
    val jsonElement = jsonSerializer.encodeToJsonElement(this)
    if (jsonElement !is JsonObject) return emptyArray()
    //改为Pair
    return jsonElement.convertToListPairs(isConvertList).toTypedArray()
}
private val logger = KotlinLogging.logger("JsonObject")

fun JsonObject.convertToListPairs(isConvertList: Boolean): List<Pair<String, Iterable<String>>> {
    val toMap = this.toMap()

    val flatMap = toMap.flatMap { (key, value) ->
        when (value) {
            is JsonPrimitive -> {
                val content = value.content
                if (content.isNotEmpty()) listOf(key to listOf(content)) else emptyList()
            }
            is JsonArray -> {
                if (isConvertList) {
                    listOf(key to listOf(value.filter { !it.jsonPrimitive.contentOrNull.isNullOrEmpty() }
                        .joinToString(",") { it.jsonPrimitive.content }))
                } else {
                    logger.error {  value.map { it.toString() }.toList().joinToString(" || ") }
                    listOf(key to value.map { it.toString() }.toList())
                }
            }
            else -> {
                val str = value.toString()
                if (str.isNotEmpty()) listOf(key to listOf(str)) else emptyList()
            }
        }
    }
    return flatMap
}


fun JsonObject.convertToPairs(isConvertList: Boolean): List<Pair<String, String>> {
    val toMap = this.toMap()

    return toMap.flatMap { (key, value) ->
        when (value) {
            is JsonPrimitive -> {
                val content = value.content
                if (content.isNotEmpty()) listOf(key to content) else emptyList()
            }

            is JsonArray -> {
                if (isConvertList) {
                    listOf(key to value.filter { !it.jsonPrimitive.contentOrNull.isNullOrEmpty() }
                        .joinToString(",") { it.jsonPrimitive.content })
                } else {
                    value.mapNotNull {
                        val content = it.jsonPrimitive.contentOrNull
                        if (!content.isNullOrEmpty()) key to content else null
                    }
                }
            }

            else -> {
                val str = value.toString()
                if (str.isNotEmpty()) listOf(key to str) else emptyList()
            }
        }
    }
}
