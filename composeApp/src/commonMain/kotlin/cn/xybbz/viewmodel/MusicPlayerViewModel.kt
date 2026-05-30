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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import cn.xybbz.assembler.MusicPlayAssembler
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.config.lrc.LrcState
import cn.xybbz.common.utils.Log
import cn.xybbz.config.lrc.LrcServer
import cn.xybbz.config.music.DownloadCacheCommonController
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.config.music.MusicPlayContext
import cn.xybbz.download.database.DownloadDatabaseClient
import cn.xybbz.entity.data.LrcEntryData
import cn.xybbz.localdata.config.LocalDatabaseClient
import cn.xybbz.localdata.data.music.XyMusic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.lyrics_tab
import xymusic_kmp.composeapp.generated.resources.recommend
import xymusic_kmp.composeapp.generated.resources.song_tab

@KoinViewModel
class MusicPlayerViewModel (
    val musicController: MusicCommonController,
    private val musicPlayContext: MusicPlayContext,
    val dataSourceManager: DataSourceManager,
    val downloadCacheController: DownloadCacheCommonController,
    val lrcServer: LrcServer,
    private val downloadDb: DownloadDatabaseClient,
    private val db: LocalDatabaseClient
) : ViewModel() {


    val dataList = listOf(Res.string.song_tab, Res.string.lyrics_tab, Res.string.recommend)

    val favoriteSet = db.musicDao.selectFavoriteListFlow()

    /**
     * 播放页可观察的真实歌词列表。
     *
     * 外层播放器页面通过 ViewModel 读取歌词数据，避免 UI 直接散落访问 [LrcServer]。
     */
    val lrcEntryListFlow: StateFlow<List<LrcEntryData>> = lrcServer.lcrEntryListFlow

    /**
     * 歌词显示状态，包含当前高亮歌词、所属歌曲和偏移配置。
     */
    val lrcStateFlow: StateFlow<LrcState> = lrcServer.lrcStateFlow

    /**
     * JVM 播放页右键菜单使用的临时偏移预览值。
     *
     * 该值只影响当前歌词滚动预览，用户点击确认前不会写入数据库。
     */
    var lyricsPreviewOffsetMs by mutableLongStateOf(0L)
        private set

    /**
     * 暴露播放进度流，供歌词相关逻辑按毫秒计算当前行。
     */
    fun getProgressStateFlow(): Flow<Long> {
        return musicController.progressStateFlow
    }

    /**
     * 跳转到指定播放位置，主要用于点击歌词行后定位播放进度。
     */
    fun seekTo(millSeconds: Long) {
        musicController.seekTo(millSeconds)
    }

    /**
     * 读取指定歌曲已保存的歌词偏移。
     *
     * 只复用 itemId 匹配的缓存配置，避免切歌瞬间把上一首歌的偏移带到当前歌曲。
     */
    fun getLyricsOffsetMs(itemId: String): Long {
        return lrcServer.lrcConfig
            ?.takeIf { it.itemId == itemId }
            ?.lrcOffsetMs
            ?: 0L
    }

    /**
     * 将右键菜单的预览偏移恢复为当前歌曲已保存的偏移。
     */
    fun resetLyricsPreviewOffset(itemId: String) {
        lyricsPreviewOffsetMs = getLyricsOffsetMs(itemId)
    }

    /**
     * 按增量调整预览偏移，正数增加 offsetMs，负数减少 offsetMs。
     */
    fun adjustLyricsPreviewOffset(deltaMs: Long) {
        lyricsPreviewOffsetMs += deltaMs
    }

    /**
     * 将预览偏移归零，但不立即持久化。
     */
    fun resetLyricsPreviewOffsetToZero() {
        lyricsPreviewOffsetMs = 0L
    }

    /**
     * 确认当前预览偏移，并写入当前歌曲的歌词配置。
     */
    fun confirmLyricsPreviewOffset(itemId: String = lrcServer.itemId) {
        updateLyricsOffset(lyricsPreviewOffsetMs, itemId)
    }

    /**
     * 保存歌词偏移配置。
     *
     * 只有待保存歌曲和 [LrcServer] 当前歌词所属歌曲一致时才写入，防止异步切歌期间误写。
     */
    fun updateLyricsOffset(offsetMs: Long, itemId: String = lrcServer.itemId) {
        if (itemId.isBlank() || lrcServer.itemId != itemId) {
            return
        }
        viewModelScope.launch {
            lrcServer.updateLrcConfig(offsetMs)
        }
    }

    fun addNextPlayer(music: XyMusic) {
        viewModelScope.launch {
            Log.i("=====", "添加到列表")
            db.musicDao.save(music)
            MusicPlayAssembler.toPlayMusic(
                music = music,
                downloadDb = downloadDb,
                mediaLibraryId = dataSourceManager.getConnectionId().toString()
            )?.let {
                musicPlayContext.addNextPlayer(it)
            }
        }
    }

}
