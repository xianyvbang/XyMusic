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

enum class TranscodeAudioBitRateType(val audioBitRate: Int) {
    LOSSLESS(0),
    LOW(128),
    MEDIUM(192),
    HIGH(256),
    HIGHEST(320);

    companion object {
        fun getTranscodeAudioBitRate(audioBitRate: Int): TranscodeAudioBitRateType {
            return when (audioBitRate) {
                0 -> LOSSLESS
                128 -> LOW
                192 -> MEDIUM
                256 -> HIGH
                320 -> HIGHEST
                else -> LOW
            }
        }
    }
}