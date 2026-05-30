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

    val lrcEntryListFlow: StateFlow<List<LrcEntryData>> = lrcServer.lcrEntryListFlow

    val lrcStateFlow: StateFlow<LrcState> = lrcServer.lrcStateFlow

    var lyricsPreviewOffsetMs by mutableLongStateOf(0L)
        private set

    fun getProgressStateFlow(): Flow<Long> {
        return musicController.progressStateFlow
    }

    fun seekTo(millSeconds: Long) {
        musicController.seekTo(millSeconds)
    }

    fun getLyricsOffsetMs(itemId: String): Long {
        return lrcServer.lrcConfig
            ?.takeIf { it.itemId == itemId }
            ?.lrcOffsetMs
            ?: 0L
    }

    fun resetLyricsPreviewOffset(itemId: String) {
        lyricsPreviewOffsetMs = getLyricsOffsetMs(itemId)
    }

    fun adjustLyricsPreviewOffset(deltaMs: Long) {
        lyricsPreviewOffsetMs += deltaMs
    }

    fun resetLyricsPreviewOffsetToZero() {
        lyricsPreviewOffsetMs = 0L
    }

    fun confirmLyricsPreviewOffset(itemId: String = lrcServer.itemId) {
        updateLyricsOffset(lyricsPreviewOffsetMs, itemId)
    }

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
