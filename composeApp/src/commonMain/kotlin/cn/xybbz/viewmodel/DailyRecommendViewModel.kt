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
import cn.xybbz.assembler.MusicPlayAssembler
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.enums.DownloadTypes
import cn.xybbz.common.utils.Log
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.config.music.MusicPlayContext
import cn.xybbz.config.recommender.DailyRecommender
import cn.xybbz.download.database.DownloadDatabaseClient
import cn.xybbz.entity.data.music.OnMusicPlayParameter
import cn.xybbz.localdata.config.LocalDatabaseClient
import cn.xybbz.localdata.data.music.XyMusic
import kotlinx.coroutines.flow.any
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class DailyRecommendViewModel(
    private val db: LocalDatabaseClient,
    private val downloadDb: DownloadDatabaseClient,
    private val dataSourceManager: DataSourceManager,
    val musicPlayContext: MusicPlayContext,
    val musicController: MusicCommonController,
    private val dailyRecommender: DailyRecommender,
) : ViewModel() {

    val downloadMusicIdsFlow =
        downloadDb.downloadDao.getAllMusicTaskUidsFlow(
            notTypeData = DownloadTypes.APK.toString(),
            mediaLibraryId = dataSourceManager.getConnectionId().toString()
        )
    val favoriteSet = db.musicDao.selectFavoriteListFlow()

    /**
     * 推荐音乐
     */
    var recommendedMusicList by mutableStateOf<List<XyMusic>>(emptyList())
        private set


    init {
        startRecommendedMusicObserver()
        observeLoginSuccessForRecommendedMusic()
    }

    /**
     * 获得推荐音乐列表
     */
    private fun observeLoginSuccessForRecommendedMusic() {
        viewModelScope.launch {
            dataSourceManager.mergeFlow.collect {
                generateRecommendedMusicList()
            }
        }
    }

    private fun startRecommendedMusicObserver() {
        viewModelScope.launch {
            db.musicDao
                .selectRecommendedMusicExtendListFlow(50)
                .distinctUntilChanged()
                .collect { list ->
                    recommendedMusicList = list
                }
        }
    }


    /**
     * 获得音乐数据
     * @param [musicId] 音乐id
     */
    suspend fun getMusicInfo(
        musicId: String,
    ): XyMusic? {
        return dataSourceManager.selectMusicInfoById(musicId)
    }

    suspend fun generateRecommendedMusicList() {
        try {
            dailyRecommender.generate()
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "生成每日推荐错误", e)
        }
    }

    fun musicList(
        onMusicPlayParameter: OnMusicPlayParameter
    ) {
        viewModelScope.launch {
            val playMusicList = MusicPlayAssembler.toPlayMusicList(
                musicList = recommendedMusicList,
                downloadDb = downloadDb,
                mediaLibraryId = dataSourceManager.getConnectionId().toString()
            ) ?: emptyList()
            musicPlayContext.musicList(
                onMusicPlayParameter,
                playMusicList.map {
                    it.copy(ifFavoriteStatus = favoriteSet.any { favoriteId -> it.itemId in favoriteId })
                }
            )
        }
    }
}
