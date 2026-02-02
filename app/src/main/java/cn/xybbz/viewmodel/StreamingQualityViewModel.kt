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

package cn.xybbz.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.api.client.navidrome.data.TranscodingInfo
import cn.xybbz.api.enums.AudioCodecEnum
import cn.xybbz.common.enums.TranscodeAudioBitRateType
import cn.xybbz.config.BackgroundConfig
import cn.xybbz.config.setting.SettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Locale.getDefault
import javax.inject.Inject

@HiltViewModel
class StreamingQualityViewModel @Inject constructor(
    val backgroundConfig: BackgroundConfig,
    private val settingsManager: SettingsManager,
    private val dataSourceManager: DataSourceManager
) : ViewModel() {

    /**
     * 是否转码
     */
    var ifTranscoding by mutableStateOf(false)
        private set

    /**
     * 转码格式
     */
    var transcodeFormat by mutableStateOf(AudioCodecEnum.ROW.audioCodec)
        private set

    /**
     * 移动网络转码比特率
     */
    var mobileNetworkAudioBitRate by mutableStateOf(TranscodeAudioBitRateType.MEDIUM)
        private set

    /**
     * wifi网络转码比特率
     */
    var wifiNetworkAudioBitRate by mutableStateOf(TranscodeAudioBitRateType.LOSSLESS)
        private set

    var transcodeAudioBitRateType by mutableStateOf<List<TranscodingInfo>>(emptyList())
        private set

    init {
        getTranscodeConfig()
        //获得服务端支持的转码类型
        getServerTranscodeFormat()
    }

    private fun getTranscodeConfig() {
        val settings = settingsManager.get()
        ifTranscoding = settings.ifTranscoding
        transcodeFormat = settings.transcodeFormat
        mobileNetworkAudioBitRate =
            TranscodeAudioBitRateType.getTranscodeAudioBitRate(settings.mobileNetworkAudioBitRate)
        wifiNetworkAudioBitRate =
            TranscodeAudioBitRateType.getTranscodeAudioBitRate(settings.wifiNetworkAudioBitRate)
    }

    private fun getServerTranscodeFormat() {
        viewModelScope.launch {
            val transcodingType = dataSourceManager.getTranscodingType()
            val transcodingInfoList = mutableListOf<TranscodingInfo>()
            if (transcodingType.isNotEmpty()) {
                transcodingInfoList.addAll(transcodingType)
            }
            if (transcodingInfoList.isNotEmpty()) {
                transcodingInfoList.addAll(
                    AudioCodecEnum.entries.filter { codec -> !transcodingInfoList.any { it.targetFormat == codec.audioCodec } && codec.ifShow }
                        .map {
                            TranscodingInfo("", it.defaultBitRate, "", "", it.audioCodec)
                        }
                )
            } else {
                transcodingInfoList.addAll(
                    AudioCodecEnum.entries.filter { codec -> codec.ifShow }.map {
                        TranscodingInfo("", it.defaultBitRate, "", "", it.audioCodec)
                    }
                )

            }
            transcodeAudioBitRateType = transcodingInfoList.map {
                it.copy(name = it.targetFormat.uppercase(getDefault()))
            }
        }
    }

    suspend fun updateIfTranscoding(ifTranscoding: Boolean) {
        this.ifTranscoding = ifTranscoding
        settingsManager.setIfTranscoding(ifTranscoding)
    }

    suspend fun updateMobileNetworkAudioBitRate(mobileNetworkAudioBitRate: TranscodeAudioBitRateType) {
        this.mobileNetworkAudioBitRate = mobileNetworkAudioBitRate
        settingsManager.setMobileNetworkAudioBitRate(mobileNetworkAudioBitRate.audioBitRate)
    }

    suspend fun updateWifiNetworkAudioBitRate(wifiNetworkAudioBitRate: TranscodeAudioBitRateType) {
        this.wifiNetworkAudioBitRate = wifiNetworkAudioBitRate
        settingsManager.setWifiNetworkAudioBitRate(wifiNetworkAudioBitRate.audioBitRate)
    }

    suspend fun updateTranscodeFormat(transcodeFormat: String) {
        this.transcodeFormat = transcodeFormat
        settingsManager.setTranscodeFormat(transcodeFormat)
    }
}