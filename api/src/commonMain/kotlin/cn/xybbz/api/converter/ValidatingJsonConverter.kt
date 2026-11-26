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

import cn.xybbz.api.client.subsonic.data.SubsonicResponse
import cn.xybbz.api.client.subsonic.data.throwIfFailed
import io.ktor.http.ContentType
import io.ktor.http.content.OutgoingContent
import io.ktor.serialization.ContentConverter
import io.ktor.serialization.kotlinx.KotlinxSerializationConverter
import io.ktor.util.reflect.TypeInfo
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.charsets.Charset

/**
 * 带业务状态校验的 JSON 转换器。
 * 说明：响应体在反序列化时只读取一次，读取完成后立即校验 Subsonic 业务状态，
 * 避免在 HttpResponseValidator 中二次读取响应体导致 DoubleReceiveException。
 * @param delegate 实际执行 JSON 编解码的转换器
 */
class ValidatingJsonConverter(
    private val delegate: ContentConverter = KotlinxSerializationConverter(jsonSerializer)
) : ContentConverter {

    /**
     * 序列化请求体，直接委托给实际转换器。
     */
    override suspend fun serialize(
        contentType: ContentType,
        charset: Charset,
        typeInfo: TypeInfo,
        value: Any?
    ): OutgoingContent? = delegate.serialize(contentType, charset, typeInfo, value)

    /**
     * 反序列化响应体，并对 Subsonic 响应做业务状态校验。
     * 只有 Subsonic 协议响应才校验，Emby/Jellyfin/Plex 等接口不受影响。
     */
    override suspend fun deserialize(
        charset: Charset,
        typeInfo: TypeInfo,
        content: ByteReadChannel
    ): Any? {
        val body = delegate.deserialize(charset, typeInfo, content)
        if (body is SubsonicResponse<*>) {
            body.throwIfFailed()
        }
        return body
    }
}
