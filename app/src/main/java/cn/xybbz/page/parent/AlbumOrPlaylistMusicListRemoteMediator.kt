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

package cn.xybbz.page.parent

import androidx.paging.ExperimentalPagingApi
import cn.xybbz.api.client.IDataSourceParentServer
import cn.xybbz.api.client.data.XyResponse
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.constants.RemoteIdConstants
import cn.xybbz.entity.data.Sort
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalPagingApi::class)
class AlbumOrPlaylistMusicListRemoteMediator(
    private val itemId: String,
    private val datasourceServer: IDataSourceParentServer,
    private val db: DatabaseClient,
    private val dataType: MusicDataTypeEnum,
    private val connectionId: Long,
    private val sortFlow: StateFlow<Sort>
) : DefaultRemoteMediator<XyMusic, XyMusic>(
    db,
    RemoteIdConstants.ALBUM_MUSIC + itemId + connectionId,
    connectionId
) {

    /**
     * 获得远程服务对象列表
     */
    override suspend fun getRemoteServerObjectList(
        loadKey: Int,
        pageSize: Int
    ): XyResponse<XyMusic> {
        val sort = sortFlow.value
        return datasourceServer.getRemoteServerMusicListByAlbumOrPlaylist(
            startIndex = loadKey * pageSize,
            pageSize = pageSize,
            isFavorite = sort.isFavorite,
            sortType = sort.sortType,
            years = sort.yearList,
            parentId = itemId,
            dataType = dataType
        )
    }

    /**
     * 删除本地数据库对象列表
     */
    override suspend fun removeLocalObjectList() {
        db.musicDao.removeByType(
            dataType = dataType,
            playlistId = itemId,
            albumId = itemId
        )
    }

    /**
     * 存储对象列表到本地数据库
     */
    override suspend fun saveBatchLocalObjectList(items: List<XyMusic>) {
        datasourceServer.saveBatchMusic(
            items = items,
            dataType = dataType,
            playlistId = if (dataType == MusicDataTypeEnum.PLAYLIST) itemId else null
        )
    }

    override suspend fun getInitializeAction(): InitializeAction {
        //判断itemId的createTime数据是否大于列表的远程键创建时间,如果是,则刷新数据
        val cacheTimeout =
            TimeUnit.MILLISECONDS.convert(Constants.PAGE_TIME_FAILURE, TimeUnit.MINUTES)
        val remoteCreateTime = (remoteKeyDao.remoteKeyById(remoteId)?.createTime
            ?: 0)
        val bool = System.currentTimeMillis() - remoteCreateTime <= cacheTimeout
        val album = db.albumDao.selectById(itemId)
        return if (bool && (album?.createTime ?: 0) <= remoteCreateTime) {
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }
}