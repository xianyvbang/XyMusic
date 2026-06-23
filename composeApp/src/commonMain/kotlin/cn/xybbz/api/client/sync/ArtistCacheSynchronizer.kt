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

package cn.xybbz.api.client.sync

import cn.xybbz.api.client.IDataSourceParentServer
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.constants.RemoteIdConstants
import cn.xybbz.database.withTransaction
import cn.xybbz.localdata.config.LocalDatabaseClient
import cn.xybbz.localdata.data.artist.XyArtist
import cn.xybbz.localdata.data.remote.RemoteCurrent
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

/**
 * 艺术家缓存同步器。
 * 负责把远端艺术家分批同步到本地 Room，UI 层只从本地分页读取。
 */
class ArtistCacheSynchronizer(
    private val db: LocalDatabaseClient,
    private val datasourceServer: IDataSourceParentServer
) {

    /**
     * 艺术家缓存刷新锁，避免艺术家页和字母索引重复触发全量同步。
     */
    private val refreshMutex = Mutex()

    /**
     * 按需刷新本地艺术家缓存。
     * 远端按普通批次拉完后再替换本地表，避免同步失败时清空已有艺术家列表。
     */
    suspend fun refreshIfNeeded(force: Boolean) {
        refreshMutex.withLock {
            // 艺术家缓存按连接和数据源隔离，避免切换服务器后复用旧缓存标记。
            val connectionId = datasourceServer.getConnectionId()
            val remoteId = RemoteIdConstants.ARTIST + datasourceServer.getDataSourceType() + connectionId
            val remoteCurrent = db.remoteCurrentDao.remoteKeyById(remoteId)
            val localArtistCount = db.artistDao.selectCount()

            // 只有本地数量覆盖远端记录总数时，才认为缓存完整。
            // 这能避开旧艺术家分页同步只落一页却留下刷新标记的问题。
            val hasCompleteLocalCache = localArtistCount > 0 &&
                    localArtistCount >= (remoteCurrent?.total ?: 0)
            val now = Clock.System.now().toEpochMilliseconds()
            val cacheTimeout = Constants.ARTIST_PAGE_TIME_FAILURE.minutes.inWholeMilliseconds

            // 未强制刷新、缓存完整、且未过期时直接复用本地 Room 数据。
            if (hasCompleteLocalCache &&
                !force &&
                remoteCurrent?.refresh != true &&
                now - (remoteCurrent?.createTime ?: 0L) <= cacheTimeout
            ) {
                return@withLock
            }

            val artistList = mutableListOf<XyArtist>()
            var startIndex = 0
            var totalRecordCount = 0

            // 使用正常 UI 分页大小分批拉取，避免一次性超大 pageSize 触发服务端限制或超时。
            while (true) {
                val response = datasourceServer.getArtistList(
                    startIndex = startIndex,
                    pageSize = Constants.UI_LIST_PAGE
                )
                val items = response.items.orEmpty()

                // 第一次响应记录远端总数，最终写入 RemoteCurrent 作为缓存完整性依据。
                if (totalRecordCount <= 0) {
                    totalRecordCount = response.totalRecordCount
                }

                // 空批次说明服务端已经没有更多数据，防止异常 total 导致死循环。
                if (items.isEmpty()) {
                    break
                }

                artistList.addAll(items)
                startIndex += items.size

                // 已拉取数量达到远端总数时停止，Room 展示仍然走本地 PagingSource。
                if (startIndex >= response.totalRecordCount) {
                    totalRecordCount = response.totalRecordCount
                    break
                }
            }

            db.withTransaction {
                // 远端全量拉取成功后再替换本地缓存，失败时不会清空旧艺术家列表。
                db.artistDao.removeByDataSource()
                datasourceServer.saveBatchArtist(artistList)

                // 写入成功刷新标记，供下次进入艺术家页判断是否需要重新同步。
                db.remoteCurrentDao.insertOrReplace(
                    RemoteCurrent(
                        id = remoteId,
                        nextKey = startIndex,
                        total = totalRecordCount,
                        connectionId = connectionId,
                        refresh = false,
                        createTime = now
                    )
                )
            }
        }
    }
}
