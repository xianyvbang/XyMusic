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

package cn.xybbz.common.enums

import org.jetbrains.compose.resources.StringResource
import xymusic.composeapp.generated.resources.Res
import xymusic.composeapp.generated.resources.transcode_audio_bit_rate_type_text_01

enum class TranscodeAudioBitRateType(
    /** 码率的固定展示文案，数字码率直接使用该字段。 */
    val audioBitRateStr: String,
    val audioBitRate: Int,
    /** 码率的资源展示文案，需要本地化的档位使用该字段。 */
    val audioBitRateResource: StringResource? = null,
) {
    LOSSLESS("", 0, Res.string.transcode_audio_bit_rate_type_text_01),
    HIGHEST("320K", 320000),
    HIGH("256K", 256000),
    MEDIUM("192K", 192000),
    LOW("128K", 128000);

    companion object {
        fun getTranscodeAudioBitRate(audioBitRate: Int): TranscodeAudioBitRateType {
            return when (audioBitRate) {
                0 -> LOSSLESS
                128000 -> LOW
                192000 -> MEDIUM
                256000 -> HIGH
                320000 -> HIGHEST
                else -> LOW
            }
        }
    }
}
