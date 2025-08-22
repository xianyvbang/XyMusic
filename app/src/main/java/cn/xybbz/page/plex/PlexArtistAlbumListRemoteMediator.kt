package cn.xybbz.page.plex

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import cn.xybbz.api.client.plex.PlexDatasourceServer
import cn.xybbz.common.constants.Constants
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.data.remote.RemoteCurrent
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalPagingApi::class)
class PlexArtistAlbumListRemoteMediator(
    private val artistId: String,
    private val plexDatasourceServer: PlexDatasourceServer,
    private val db: DatabaseClient,
    private val connectionId: Long
) : RemoteMediator<Int, XyAlbum>() {

    private val remoteKeyDao = db.remoteCurrentDao
    private val remoteId: String = Constants.ARTIST + Constants.ALBUM + artistId + connectionId
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, XyAlbum>
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

            val response = plexDatasourceServer.getAlbumList(
                artistId = artistId,
                pageSize = state.config.pageSize,
                startIndex = loadKey * state.config.pageSize,
            )

            db.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    remoteKeyDao.deleteById(remoteId)
                    db.albumDao.removeByType(MusicDataTypeEnum.ARTIST, artistId)
                }

                remoteKeyDao.insertOrReplace(
                    RemoteCurrent(
                        id = remoteId,
                        nextKey = loadKey,
                        total = response.totalRecordCount,
                        connectionId = connectionId
                    )
                )

                response.items?.let {
                    plexDatasourceServer.saveBatchAlbum(
                        baseItemList = it,
                        dataType = MusicDataTypeEnum.ARTIST,
                        artistId = artistId
                    )
                }
            }

            MediatorResult.Success(
                endOfPaginationReached = response.items.isNullOrEmpty()
                        || (loadKey * state.config.pageSize) >= response.totalRecordCount
            )
        } catch (e: Exception) {
            e.printStackTrace()
            MediatorResult.Error(e)
        }
    }

    override suspend fun initialize(): InitializeAction {
        val cacheTimeout =
            TimeUnit.MILLISECONDS.convert(Constants.PAGE_TIME_FAILURE, TimeUnit.MINUTES)
        return if (System.currentTimeMillis() - (remoteKeyDao.remoteKeyById(remoteId)?.createTime
                ?: 0) <= cacheTimeout
        ) {
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }
}