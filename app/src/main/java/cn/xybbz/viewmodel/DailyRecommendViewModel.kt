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

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.music.MusicController
import cn.xybbz.config.BackgroundConfig
import cn.xybbz.config.recommender.DailyRecommender
import cn.xybbz.entity.data.music.MusicPlayContext
import cn.xybbz.entity.data.music.OnMusicPlayParameter
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.data.music.XyMusicExtend
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DailyRecommendViewModel @Inject constructor(
    private val db: DatabaseClient,
    private val dataSourceManager: DataSourceManager,
    val musicPlayContext: MusicPlayContext,
    val musicController: MusicController,
    val backgroundConfig: BackgroundConfig,
    private val dailyRecommender: DailyRecommender,
) : ViewModel() {

    val downloadMusicIdsFlow =
        db.downloadDao.getAllMusicTaskUidsFlow()
    val favoriteSet = db.musicDao.selectFavoriteListFlow()

    /**
     * 推荐音乐
     */
    var recommendedMusicList by mutableStateOf<List<XyMusicExtend>>(emptyList())
        private set

    private var recommendedMusicJob: Job? = null

    init {
        observeLoginSuccessForRecommendedMusic()
    }

    /**
     * 获得推荐音乐列表
     */
    private fun observeLoginSuccessForRecommendedMusic() {
        viewModelScope.launch {
            dataSourceManager.mergeFlow.collect {
                startRecommendedMusicObserver()
            }
        }
    }

    private fun startRecommendedMusicObserver() {
        // 取消旧 Job 避免重复订阅
        recommendedMusicJob?.cancel()

        recommendedMusicJob = viewModelScope.launch {
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
            musicPlayContext.musicList(
                onMusicPlayParameter,
                recommendedMusicList.map {
                    it.toPlayMusic()
                        .copy(ifFavoriteStatus = db.musicDao.selectIfFavoriteByMusic(it.music.itemId))
                }
            )
        }
    }
}