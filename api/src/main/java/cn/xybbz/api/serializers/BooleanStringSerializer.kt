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

package cn.xybbz.api.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull

object BooleanStringSerializer : KSerializer<Boolean> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("BooleanString", PrimitiveKind.BOOLEAN)

    override fun serialize(encoder: Encoder, value: Boolean) {
        // 序列化时正常输出 true/false
        encoder.encodeBoolean(value)
    }

    override fun deserialize(decoder: Decoder): Boolean {
        // 必须是 JsonDecoder 才能拿到 JsonElement
        val jsonDecoder = decoder as? JsonDecoder
            ?: return decoder.decodeBoolean()

        val element = jsonDecoder.decodeJsonElement()
        val primitive = element as? JsonPrimitive
            ?: error("Expected JsonPrimitive, but got: $element")

        // 1) 支持 true/false
        primitive.booleanOrNull?.let { return it }

        // 2) 支持 "true"/"false"
        return when (val text = primitive.contentOrNull?.trim()?.lowercase()) {
            "true" -> true
            "false" -> false
            else -> error("Invalid boolean value: $text")
        }
    }
}
