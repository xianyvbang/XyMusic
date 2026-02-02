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
import cn.xybbz.config.BackgroundConfig
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.localdata.enums.CacheUpperLimitEnum
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class CacheLimitViewModel @Inject constructor(
    private val settingsManager: SettingsManager,
    val backgroundConfig: BackgroundConfig
) : ViewModel() {

    /**
     * 缓存上限
     */
    var cacheUpperLimit by mutableStateOf(settingsManager.cacheUpperLimit)
        private set

    /**
     * 缓存大小
     */
    var cacheSizeInfo by mutableStateOf("")
        private set


    fun setCacheUpperLimitData(data: CacheUpperLimitEnum) {
        cacheUpperLimit = data
        viewModelScope.launch {
            settingsManager.setCacheUpperLimit(
                cacheUpperLimit
            )

        }
    }

    fun getAutomaticCacheSize() {
        cacheSizeInfo = formatSize(settingsManager.maxBytes) ?:""

    }

    fun formatSize(size: Long): String? {
        var suffix = "B"
        var value = size.toDouble()

        if (value > 1024) {
            suffix = "KB"
            value /= 1024.0
        }
        if (value > 1024) {
            suffix = "MB"
            value /= 1024.0
        }
        if (value > 1024) {
            suffix = "GB"
            value /= 1024.0
        }

        return String.format(Locale.getDefault(), "%.0f%s", value, suffix)
    }
}