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

package cn.xybbz.database.converter

import androidx.room.TypeConverter
import cn.xybbz.database.common.Constants
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

/**
 * 使用 JSON 保存字符串列表的 Room 转换器。
 *
 * 主要用于艺术家名称这类可能包含逗号、斜杠等字符的字段，读取时兼容旧的逗号分隔数据。
 */
class JsonStringListTypeConverter {

    /**
     * 字符串列表使用的 JSON 编码器。
     */
    private val json = Json

    /**
     * 字符串列表的 kotlinx.serialization 序列化器。
     */
    private val listSerializer = ListSerializer(String.serializer())

    /**
     * 将字符串列表编码为 JSON 数组字符串。
     */
    @TypeConverter
    fun listToString(list: List<String>?): String? {
        return list?.let { json.encodeToString(listSerializer, it) }
    }

    /**
     * 将数据库文本还原为字符串列表，优先解析 JSON，失败时兼容旧逗号格式。
     */
    @TypeConverter
    fun stringToList(value: String?): List<String>? {
        val text = value ?: return null
        if (text.isBlank()) {
            return emptyList()
        }
        return runCatching {
            json.decodeFromString(listSerializer, text)
        }.getOrElse {
            text.split(Constants.ARTIST_DELIMITER)
        }
    }
}
