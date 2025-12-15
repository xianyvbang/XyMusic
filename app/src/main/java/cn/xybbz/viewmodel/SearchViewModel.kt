package cn.xybbz.viewmodel

import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import androidx.room.Transaction
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.music.MusicController
import cn.xybbz.config.BackgroundConfig
import cn.xybbz.config.ConnectionConfigServer
import cn.xybbz.config.download.DownloadRepository
import cn.xybbz.config.favorite.FavoriteRepository
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.data.artist.XyArtist
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.data.search.SearchHistory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @OptIn(UnstableApi::class)
@Inject constructor(
    private val db: DatabaseClient,
    private val dataSourceManager: DataSourceManager,
    private val connectionConfigServer: ConnectionConfigServer,
    val musicController: MusicController,
    val favoriteRepository: FavoriteRepository,
    val downloadRepository: DownloadRepository,
    val backgroundConfig: BackgroundConfig
) : ViewModel() {


    /**
     * 搜索历史
     */
    val searchHistory = mutableStateListOf<SearchHistory>()

    //歌曲
    var musicList by mutableStateOf<List<XyMusic>>(emptyList())
        private set


    //专辑
    var albumList: List<XyAlbum> by mutableStateOf(emptyList())
        private set

    //艺术家
    var artistList: List<XyArtist> by mutableStateOf(emptyList())
        private set

    var isSearchLoad by mutableStateOf(false)
        private set

    /**
     * 是否显示搜索结果
     */
    var ifShowSearchResult by mutableStateOf(false)
        private set


    init {
        getSearchHistoryData()
    }

    private fun getSearchHistoryData() {
        viewModelScope.launch {
            db.searchHistoryDao.selectListAll().distinctUntilChanged().collect {
                searchHistory.clear()
                searchHistory.addAll(it)
            }
        }

    }

    /**
     * 清空搜索历史
     */
    fun clearSearchHistory() {
        viewModelScope.launch {
            db.searchHistoryDao.deleteAll()
        }
    }


    fun onSearch(
        searchQuery: String,
    ) {
        if (searchQuery.isNotBlank()) {
            ifShowSearchResult = true
            isSearchLoad = true
            viewModelScope.launch {
                try {
                    val searchData = dataSourceManager.searchAll(searchQuery)
                    musicList = searchData.musics ?: emptyList()
                    albumList = searchData.albums ?: emptyList()
                    artistList = searchData.artists ?: emptyList()
                    saveSearchHistory(
                        SearchHistory(
                            searchQuery = searchQuery,
                            connectionId = connectionConfigServer.getConnectionId()
                        )
                    )
                } catch (e: Exception) {
                    Log.e(Constants.LOG_ERROR_PREFIX, "搜索失败: ${e.message}", e)
                }
            }.invokeOnCompletion {
                isSearchLoad = false
            }
        }
    }


    /**
     * 存储搜索历史
     */
    @Transaction
    suspend fun saveSearchHistory(searchHistory: SearchHistory) {
        //判断数据是否存在
        val searchQuery =
            db.searchHistoryDao.selectOneBySearchQuery(searchHistory.searchQuery)
        if (searchQuery == null) {
            db.searchHistoryDao.save(searchHistory)
        }
    }


    fun addMusic(music: XyMusic) {
        viewModelScope.launch {
            val download = db.downloadDao.getMusicCompleteTaskByUid(music.itemId)
            val playMusic = music.toPlayMusic().copy(
                ifFavoriteStatus = music.itemId in favoriteRepository.favoriteSet.value,
                filePath = download?.filePath
            )
            musicController.addMusic(
                playMusic,
                artistId = "",
                true
            )
        }

    }

    /**
     * 更新是否显示搜索结果
     */
    fun updateIfShowSearchResult(ifShowSearchResult: Boolean) {
        Log.i("=====", "数据调用1")
        this.ifShowSearchResult = ifShowSearchResult
    }

}