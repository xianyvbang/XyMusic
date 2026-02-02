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

package cn.xybbz.api.adapter

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId


/**
 * LocalDateTime适配器
 * 将字符串转换成LocalDateTime/将LocalDateTime转换成字符串
 * @author xybbz
 * @date 2025/12/02
 * @constructor 创建[LocalDateAdapter]
 */
class LocalDateTimeAdapter {

    private val zoneId: ZoneId = ZoneId.systemDefault()

    @FromJson
    fun fromJson(json: String): LocalDateTime {
        // 只处理 Z 结尾的 UTC 时间，避免创建 OffsetDateTime
        return if (json.endsWith("Z")) {
            val instant = Instant.parse(json) // Instant 是轻量对象
            LocalDateTime.ofInstant(instant, zoneId)
        } else {
            // 不带 Z 的情况，直接截断到毫秒解析，避免纳秒对象创建太多
            val trimmed = json.takeWhile { it != 'Z' }  // 安全截断
            LocalDateTime.parse(trimmed)
        }
    }

    @ToJson
    fun toJson(value: LocalDateTime): String {
        // 转回 UTC ISO 字符串
        val instant = value.atZone(zoneId).toInstant()
        return instant.toString() // 输出 2026-01-16T04:37:51.844Z
    }
}