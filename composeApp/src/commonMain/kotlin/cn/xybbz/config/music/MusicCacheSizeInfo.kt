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

package cn.xybbz.config.music

import cn.xybbz.common.utils.formatBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * 音乐播放缓存占用信息。
 *
 * @property bytes 原始字节数，供进度、汇总等数值场景复用。
 * @property label 格式化后的容量文案，供设置页面直接展示。
 */
data class MusicCacheSizeInfo(
    val bytes: Long,
    val label: String,
)

/**
 * 音乐播放缓存占用流。
 *
 * 存储管理页和缓存上限页都通过这里读取同一份缓存大小，避免各页面重复拼接格式化逻辑。
 */
fun DownloadCacheCommonController.musicCacheSizeInfoFlow(): Flow<MusicCacheSizeInfo> {
    return allCacheSizeFlow.map { bytes ->
        bytes.toMusicCacheSizeInfo()
    }
}

/**
 * 刷新音乐播放缓存占用。
 */
suspend fun DownloadCacheCommonController.refreshMusicCacheSize() {
    withContext(Dispatchers.IO) {
        getCacheSize()
    }
}

/**
 * 将原始字节数转换为缓存占用展示信息。
 */
fun Long.toMusicCacheSizeInfo(): MusicCacheSizeInfo {
    return MusicCacheSizeInfo(
        bytes = this,
        label = formatBytes(this),
    )
}
