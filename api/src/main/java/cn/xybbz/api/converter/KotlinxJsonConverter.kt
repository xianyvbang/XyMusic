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

package cn.xybbz.api.converter

import cn.xybbz.api.client.data.Request
import cn.xybbz.api.client.jellyfin.data.ItemRequest
import cn.xybbz.api.client.jellyfin.data.ViewRequest
import cn.xybbz.api.client.subsonic.data.ScrobbleRequest
import cn.xybbz.api.client.subsonic.data.SubsonicSearchRequest
import cn.xybbz.api.constants.ApiConstants.HEADER_ACCEPT
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.converter.kotlinx.serialization.asConverterFactory

val module = SerializersModule {
    polymorphic(Request::class) {
        subclass(ItemRequest::class, ItemRequest.serializer())
        subclass(ScrobbleRequest::class, ScrobbleRequest.serializer())
        subclass(SubsonicSearchRequest::class, SubsonicSearchRequest.serializer())
        subclass(ViewRequest::class, ViewRequest.serializer())
        // 其他子类都要注册
    }
}

val json = Json {
    serializersModule = module
    ignoreUnknownKeys = true  // JSON 多字段不报错
    encodeDefaults = true     // 默认值也写入（可选）
    explicitNulls = false     // null 不输出（可选）
}

fun kotlinxJsonConverter() = json.asConverterFactory(HEADER_ACCEPT.toMediaType())