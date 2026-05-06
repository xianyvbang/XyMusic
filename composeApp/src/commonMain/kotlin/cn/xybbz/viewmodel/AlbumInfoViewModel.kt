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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.room.Transaction
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.constants.RemoteIdConstants
import cn.xybbz.common.enums.DownloadTypes
import cn.xybbz.common.enums.SortTypeEnum
import cn.xybbz.common.utils.Log
import cn.xybbz.common.utils.PlaylistFileUtils
import cn.xybbz.common.utils.PlaylistParser
import cn.xybbz.config.download.enqueueMusicDownload
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.config.music.MusicPlayContext
import cn.xybbz.config.select.SelectControl
import cn.xybbz.download.DownloaderManager
import cn.xybbz.download.database.DownloadDatabaseClient
import cn.xybbz.entity.data.Sort
import cn.xybbz.localdata.config.LocalDatabaseClient
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.data.progress.EnableProgress
import cn.xybbz.localdata.data.progress.Progress
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class AlbumInfoViewModel(
    @InjectedParam private val itemId: String,
    @InjectedParam private val dataType: MusicDataTypeEnum,
    val dataSourceManager: DataSourceManager,
    val db: LocalDatabaseClient,
    val downloadDb: DownloadDatabaseClient,
    val musicPlayContext: MusicPlayContext,
    val musicController: MusicCommonController,
    val selectControl: SelectControl,
    private val downloaderManager: DownloaderManager,
) : PageListViewModel<XyMusic>(dataSourceManager, SortTypeEnum.MUSIC_NAME_ASC) {

    val downloadMusicIdsFlow =
        downloadDb.downloadDao.getAllMusicTaskUidsFlow(
            notTypeData = DownloadTypes.APK.toString(),
            mediaLibraryId = dataSourceManager.getConnectionId().toString()
        )
    val favoriteSet = db.musicDao.selectFavoriteListFlow()

    /**
     * 播放进度
     */
    var albumPlayerHistoryProgress by mutableStateOf<Progress?>(null)
        private set

    /**
     * 进度信息map
     */
    var albumPlayerHistoryProgressMap = mutableStateMapOf<String, Int>()
        private set

    /**
     * 专辑信息
     */
    var xyAlbumInfoData by mutableStateOf<XyAlbum?>(null)
        private set


    /**
     * 封面图
     */
    var albumPic: String? by mutableStateOf(null)
        private set

    /**
     * 收藏信息
     */
    var ifFavorite by mutableStateOf(false)
        private set

    /**
     * 是否开启记录播放历史
     */
    var ifSavePlaybackHistory by mutableStateOf(false)

    init {
        getPlayerHistoryProgressList()
        getAlbumInfoData()
    }

    /**
     * 获得专辑详情
     */
    fun getAlbumInfoData() {
        viewModelScope.launch {
            coroutineScope {
                val localAlbumInfoDeferred =
                    async { dataSourceManager.selectLocalAlbumInfoById(itemId) }
                val serverAlbumInfoDeferred =
                    async { dataSourceManager.selectServerAlbumInfoById(itemId, dataType) }

                localAlbumInfoDeferred.await()?.let { localAlbumInfo ->
                    xyAlbumInfoData = localAlbumInfo
                    albumPic = localAlbumInfo.pic
                    ifFavorite = localAlbumInfo.ifFavorite
                }

                serverAlbumInfoDeferred.await()?.let { serverAlbumInfo ->
                    xyAlbumInfoData = serverAlbumInfo
                    if (albumPic.isNullOrBlank()) {
                        albumPic = serverAlbumInfo.pic
                    }
                    ifFavorite = serverAlbumInfo.ifFavorite
                }
            }
        }

    }


    //region 播放历史进度
    fun getPlayerHistoryProgressList() {
        viewModelScope.launch {
            db.progressDao.selectByAlbumIdFlowOne(albumId = itemId)
                .distinctUntilChanged().collect { data ->
                    albumPlayerHistoryProgress = data
                }
        }


        viewModelScope.launch {
            db.progressDao.selectByAlbumIdFlowMap(itemId).distinctUntilChanged()
                .collect {
                    if (it.isNotEmpty()) {
                        Log.i("=====", "数据进度变化")
                        albumPlayerHistoryProgressMap.clear()
                        albumPlayerHistoryProgressMap.putAll(it)
                    }
                }
        }
        //获取专辑是否开启播放历史记录
        viewModelScope.launch {
            db.enableProgressDao.getAlbumEnableProgressByAlbumId(itemId)
                .distinctUntilChanged().collect {
                    ifSavePlaybackHistory = it == true
                }
        }
    }

    //endregion

    /**
     * 修改保存是否开启播放历史存储
     */
    fun setIfSavePlaybackHistoryData(albumId: String, value: Boolean) {
        this.ifSavePlaybackHistory = value
        viewModelScope.launch {
            saveIfSavePlaybackHistoryData(albumId, value)
        }
    }

    /**
     * 删除专辑播放历史
     */
    fun removeAlbumPlayerHistoryProgress(musicId: String) {
        viewModelScope.launch {
            db.progressDao.removeByMusicId(musicId = musicId)

        }
    }


    /**
     * 更新专辑的播放历史数据
     */
    @Transaction
    suspend fun saveIfSavePlaybackHistoryData(albumId: String, value: Boolean) {
        db.enableProgressDao.save(
            EnableProgress(
                albumId,
                value,
                connectionId = dataSourceManager.getConnectionId()
            )
        )
        if (!value) {
            //清空播放历史
            db.progressDao.removeByAlbumId(albumId)
        }
    }


    /**
     * 导出歌单
     */
    suspend fun exportPlaylist(): PlaylistParser.Playlist? {
        return PlaylistFileUtils.createTrackList(
            dataSourceManager = dataSourceManager,
            playlistId = itemId
        )
    }

    /**
     * 导入歌单
     */
    suspend fun importPlaylist(playlist: PlaylistParser.Playlist) {
        dataSourceManager.importPlaylist(playlist, itemId)
    }

    /**
     * 删除歌单
     * @param [id] ID
     */
    suspend fun removePlaylist(id: String) {
        dataSourceManager.removePlaylist(id)
    }

    /**
     * 修改歌单名称
     * @param [id] ID
     * @param [name] 名称
     */
    fun editPlaylistName(id: String, name: String) {
        viewModelScope.launch {
            dataSourceManager.editPlaylistName(id, name)
        }
    }

    /**
     * 更新选择功能是否是在歌单中
     */
    fun show(onOpenChange: ((Boolean) -> Unit)? = null) {
        selectControl.show(
            true,
            itemId,
            dataType == MusicDataTypeEnum.PLAYLIST,
            onOpenChange = onOpenChange
        )
    }

    /**
     * 更新收藏状态
     */
    fun updateIfFavorite(ifFavorite: Boolean) {
        this.ifFavorite = ifFavorite
    }

    fun downloadMusic(musicData: XyMusic) {
        viewModelScope.launch {
            downloaderManager.enqueueMusicDownload(musicData, dataSourceManager)
        }
    }

    /**
     * 获得数据结构
     */
    override fun getFlowPageData(sort: Sort): Flow<PagingData<XyMusic>> {
        return dataSourceManager.selectMusicListByParentId(
            itemId = itemId,
            dataType = dataType,
            sort = sort
        )
    }

    override suspend fun updateDataSourceRemoteKey() {
        val remoteId = RemoteIdConstants.ALBUM_MUSIC +
                itemId
        dataSourceManager.updateDataSourceRemoteKey(remoteId)
    }

}
