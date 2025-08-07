package cn.xybbz.page.subsonic

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import cn.xybbz.api.client.subsonic.SubsonicDatasourceServer
import cn.xybbz.common.constants.Constants
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.data.remote.RemoteCurrent
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalPagingApi::class)
class SubsonicGenreAlbumListRemoteMediator(
    private val genreId: String,
    private val subsonicDatasourceServer: SubsonicDatasourceServer,
    private val db: DatabaseClient,
    private val connectionId: Long
) : RemoteMediator<Int, XyAlbum>() {
    private val remoteKeyDao = db.remoteCurrentDao
    private val remoteId: String = genreId + connectionId


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

            val response = subsonicDatasourceServer.getAlbumList(
                genreId = genreId,
                pageSize = state.config.pageSize,
                startIndex = loadKey * state.config.pageSize,
            )

            db.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    remoteKeyDao.deleteById(remoteId)
                    db.albumDao.removeByType(MusicDataTypeEnum.GENRE, genreId = genreId)
                }

                remoteKeyDao.insertOrReplace(
                    RemoteCurrent(
                        id = remoteId,
                        nextKey = loadKey,
                        total = 0,
                        connectionId = connectionId
                    )
                )
                response.subsonicResponse?.let {albumList->
                    subsonicDatasourceServer.saveBatchAlbum(
                        baseItemList = albumList,
                        dataType = MusicDataTypeEnum.GENRE,
                        genreId = genreId
                    )
                }
            }

            MediatorResult.Success(
                endOfPaginationReached = response.subsonicResponse.isNullOrEmpty()
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