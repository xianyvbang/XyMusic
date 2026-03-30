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
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import cn.xybbz.api.client.IDataSourceParentServer
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.constants.RemoteIdConstants
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.config.withTransaction
import cn.xybbz.localdata.data.artist.XyArtistExt
import cn.xybbz.localdata.data.remote.RemoteCurrent
import cn.xybbz.localdata.enums.DataSourceType
import kotlinx.io.IOException
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit

/**
 * jellyfin艺术家的网络数据加载
 * @author xybbz
 * @date 2024/06/14
 * @constructor 创建[ArtistRemoteMediator]
 * @param [dataSource] 数据源来兴
 */
@OptIn(ExperimentalPagingApi::class)
class ArtistRemoteMediator(
    private val db: DatabaseClient,
    private val datasourceServer: IDataSourceParentServer,
    private val dataSource: DataSourceType,
    private val connectionId: Long
) : RemoteMediator<Int, XyArtistExt>() {

    private val remoteKeyDao = db.remoteCurrentDao
    private val remoteId = RemoteIdConstants.ARTIST + dataSource + connectionId
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, XyArtistExt>
    ): MediatorResult {
        return try {

            val loadKey: Int = when (loadType) {
                LoadType.REFRESH -> 0
                LoadType.PREPEND -> return MediatorResult.Success(
                    endOfPaginationReached = true
                )

                LoadType.APPEND -> {
                    val remoteKey = db.withTransaction {
                        remoteKeyDao.remoteKeyById(remoteId)
                    }
                    if (remoteKey == null || (remoteKey.nextKey + 1 * state.config.pageSize) >= remoteKey.total
                    ) {
                        return MediatorResult.Success(
                            endOfPaginationReached = true
                        )
                    }
                    remoteKey.nextKey.plus(1)
                }
            }

            val response = datasourceServer.getArtistList(
                startIndex = loadKey * state.config.pageSize,
                pageSize = state.config.pageSize
            )

            db.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    remoteKeyDao.deleteById(remoteId)
                    removeArtists()
                }

                remoteKeyDao.insertOrReplace(
                    RemoteCurrent(
                        id = remoteId,
                        nextKey = loadKey,
                        total = response.totalRecordCount,
                        connectionId = connectionId,
                        refresh = false
                    )
                )
                response.items?.let {
                    datasourceServer.saveBatchArtist(it)
                }
            }

            MediatorResult.Success(
                endOfPaginationReached = true
            )
        } catch (e: IOException) {
            e.printStackTrace()
            MediatorResult.Error(e)
        } catch (e: Exception) {
            e.printStackTrace()
            MediatorResult.Error(e)
        }
    }

    override suspend fun initialize(): InitializeAction {
        val cacheTimeout =
            Constants.ARTIST_PAGE_TIME_FAILURE.minutes.toLong(DurationUnit.MILLISECONDS)
        return getInitializeAction(remoteKeyDao, remoteId, cacheTimeout)
    }


    /**
     * 根据数据源删除艺术家
     * @param [dataSource] 数据源
     */
    suspend fun removeArtists() {
        db.artistDao.removeByDataSource()
    }
}