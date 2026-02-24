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
import cn.xybbz.common.constants.RemoteIdConstants
import cn.xybbz.entity.data.Sort
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.enums.DataSourceType
import cn.xybbz.localdata.enums.MusicDataTypeEnum

/**
 * 专辑的网络数据加载
 * @author xybbz
 * @date 2024/06/14
 * @constructor 创建[AlbumRemoteMediator]
 * @param [database] 本地缓存管理类
 * @param [dataSource] 数据源来兴
 */
@OptIn(ExperimentalPagingApi::class)
class AlbumRemoteMediator(
    private val dataSource: DataSourceType,
    private val db: DatabaseClient,
    private val datasourceServer: IDataSourceParentServer,
    private val connectionId: Long,
    private val sort: Sort
) : DefaultRemoteMediator<XyAlbum,XyAlbum>(
    db,
    RemoteIdConstants.ALBUM + dataSource + connectionId,
    connectionId
) {

    /**
     * 获得远程服务对象列表
     * @param [loadKey] 页码 从0开始
     * @param [pageSize] 页面大小
     */
    override suspend fun getRemoteServerObjectList(
        loadKey: Int,
        pageSize: Int
    ): XyResponse<XyAlbum> {
        val sort = sort.value
        return datasourceServer.getRemoteServerAlbumList(
            startIndex = loadKey * pageSize,
            pageSize = pageSize,
            sortType = sort.sortType,
            isFavorite = sort.isFavorite,
            years = sort.yearList,
            genreId = null
        )
    }

    /**
     * 删除本地数据库对象列表
     */
    override suspend fun removeLocalObjectList() {
        db.albumDao.removeByType(MusicDataTypeEnum.HOME)
    }

    /**
     * 存储对象列表到本地数据库
     */
    override suspend fun saveBatchLocalObjectList(items: List<XyAlbum>) {
        //存储专辑
        datasourceServer.saveBatchAlbum(
            items,
            MusicDataTypeEnum.HOME
        )
    }

    override suspend fun initialize(): InitializeAction {
        //判断是否
        return super.initialize()
    }
}