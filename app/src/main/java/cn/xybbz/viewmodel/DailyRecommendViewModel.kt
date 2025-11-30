package cn.xybbz.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.xybbz.api.client.IDataSourceManager
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.music.MusicController
import cn.xybbz.config.BackgroundConfig
import cn.xybbz.config.ConnectionConfigServer
import cn.xybbz.config.download.DownloadRepository
import cn.xybbz.config.favorite.FavoriteRepository
import cn.xybbz.config.recommender.DailyRecommender
import cn.xybbz.entity.data.music.MusicPlayContext
import cn.xybbz.entity.data.music.OnMusicPlayParameter
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.data.music.XyMusicExtend
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DailyRecommendViewModel @Inject constructor(
    private val db: DatabaseClient,
    private val dataSourceManager: IDataSourceManager,
    val musicPlayContext: MusicPlayContext,
    val musicController: MusicController,
    val favoriteRepository: FavoriteRepository,
    val downloadRepository: DownloadRepository,
    val backgroundConfig: BackgroundConfig,
    private val dailyRecommender: DailyRecommender,
    private val connectionConfigServer: ConnectionConfigServer
) : ViewModel() {


    /**
     * 推荐音乐
     */
    var recommendedMusicList by mutableStateOf<List<XyMusicExtend>>(emptyList())
        private set

    init {

        getRecommendedMusicList()
    }

    /**
     * 获得推荐音乐列表
     */
    private fun getRecommendedMusicList() {
        viewModelScope.launch {
            connectionConfigServer.loginStateFlow.collect { bool ->
                if (bool) {
                    db.musicDao.selectRecommendedMusicExtendListFlow(50)
                        .distinctUntilChanged()
                        .collect {
                            recommendedMusicList = it
                        }
                }
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
                        .copy(ifFavoriteStatus = it.music.itemId in favoriteRepository.favoriteSet.value)
                }
            )
        }
    }
}