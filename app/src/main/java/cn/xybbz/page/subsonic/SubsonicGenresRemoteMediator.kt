package cn.xybbz.page.subsonic

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import cn.xybbz.api.client.subsonic.SubsonicDatasourceServer
import cn.xybbz.common.constants.Constants
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.genre.XyGenre
import cn.xybbz.localdata.data.remote.RemoteCurrent
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalPagingApi::class)
class SubsonicGenresRemoteMediator(
    private val subsonicDatasourceServer: SubsonicDatasourceServer,
    private val db: DatabaseClient,
    private val connectionId: Long
) : RemoteMediator<Int, XyGenre>() {
    private val remoteKeyDao = db.remoteCurrentDao
    private val remoteId = Constants.GENRE + connectionId
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, XyGenre>
    ): MediatorResult {
        return try {
            val loadKey: Int = when (loadType) {
                LoadType.REFRESH -> 0
                LoadType.PREPEND -> return MediatorResult.Success(
                    endOfPaginationReached = true
                )

                LoadType.APPEND -> {
                    return MediatorResult.Success(
                        endOfPaginationReached = true
                    )
                }
            }

            val response = subsonicDatasourceServer.getGenreList()

            db.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    remoteKeyDao.deleteById(remoteId)
                    db.genreDao.remove()
                }

                remoteKeyDao.insertOrReplace(
                    RemoteCurrent(
                        id = remoteId,
                        nextKey = loadKey,
                        total = response?.size?:0,
                        connectionId = connectionId
                    )
                )

                response?.let {
                    subsonicDatasourceServer.saveBatchGenre(
                        items = it
                    )
                }

            }

            MediatorResult.Success(
                endOfPaginationReached = true
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