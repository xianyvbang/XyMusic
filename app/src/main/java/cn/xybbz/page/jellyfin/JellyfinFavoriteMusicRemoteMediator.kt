package cn.xybbz.page.jellyfin

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import cn.xybbz.api.client.jellyfin.JellyfinDatasourceServer
import cn.xybbz.common.constants.Constants
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.data.remote.RemoteCurrent
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalPagingApi::class)
class JellyfinFavoriteMusicRemoteMediator(
    private val jellyfinDatasourceServer: JellyfinDatasourceServer,
    private val db: DatabaseClient,
    private val connectionId: Long
) : RemoteMediator<Int, XyMusic>() {

    private val remoteKeyDao = db.remoteCurrentDao
    private val remoteId = Constants.FAVORITE + connectionId
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, XyMusic>,
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
                    if (remoteKey == null || (remoteKey.nextKey * state.config.pageSize) >= remoteKey.total
                    ) {
                        return MediatorResult.Success(
                            endOfPaginationReached = true
                        )
                    }
                    remoteKey.nextKey.plus(1)
                }
            }

            val response = jellyfinDatasourceServer.getServerMusicList(
                pageSize = state.config.pageSize,
                startIndex = loadKey * state.config.pageSize,
                isFavorite = true
            )
            db.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    remoteKeyDao.deleteById(remoteId)
                    db.musicDao.removeByType(MusicDataTypeEnum.FAVORITE)
                }

                remoteKeyDao.insertOrReplace(
                    RemoteCurrent(
                        id = remoteId,
                        nextKey = loadKey,
                        total = response.totalRecordCount,
                        connectionId = connectionId
                    )
                )

                jellyfinDatasourceServer.saveBatchMusic(
                    response.items,
                    MusicDataTypeEnum.FAVORITE
                )
            }

            MediatorResult.Success(
                endOfPaginationReached = response.items.isEmpty()
                        || (loadKey * state.config.pageSize) >= response.totalRecordCount
            )

        } catch (e: Exception) {
            e.printStackTrace()
            MediatorResult.Error(e)
        }
    }

    override suspend fun initialize(): InitializeAction {
        val cacheTimeout = TimeUnit.MILLISECONDS.convert(Constants.PAGE_TIME_FAILURE, TimeUnit.MINUTES)
        return if (System.currentTimeMillis() - (remoteKeyDao.remoteKeyById(remoteId)?.createTime
                ?: 0) <= cacheTimeout
        ) {
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }
}