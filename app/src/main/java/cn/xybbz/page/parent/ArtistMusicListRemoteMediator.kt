package cn.xybbz.page.parent

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import cn.xybbz.api.client.IDataSourceParentServer
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.constants.RemoteIdConstants
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.data.remote.RemoteCurrent
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalPagingApi::class)
class ArtistMusicListRemoteMediator(
    private val artistId: String,
    private val datasourceServer: IDataSourceParentServer,
    private val db: DatabaseClient,
    private val connectionId: Long
) : RemoteMediator<Int, XyMusic>() {

    private val remoteKeyDao = db.remoteCurrentDao
    private val remoteId = RemoteIdConstants.ARTIST + RemoteIdConstants.MUSIC + artistId

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

            val response =
                datasourceServer.selectMusicListByArtistServer(
                    pageSize = state.config.pageSize,
                    startIndex = loadKey * state.config.pageSize,
                    artistId = artistId
                )
            db.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    remoteKeyDao.deleteById(remoteId)
                    db.musicDao.removeByType(MusicDataTypeEnum.ARTIST, artistId)
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
                    datasourceServer.saveBatchMusic(
                        it,
                        MusicDataTypeEnum.ARTIST,
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