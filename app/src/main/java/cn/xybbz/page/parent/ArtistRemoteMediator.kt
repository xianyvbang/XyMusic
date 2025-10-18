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
import cn.xybbz.localdata.data.artist.XyArtistExt
import cn.xybbz.localdata.data.remote.RemoteCurrent
import cn.xybbz.localdata.enums.DataSourceType
import coil.network.HttpException
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * jellyfin艺术家的网络数据加载
 * @author xybbz
 * @date 2024/06/14
 * @constructor 创建[ArtistRemoteMediator]
 * @param [userId] 用户id
 * @param [database] 本地缓存管理类
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
                    if (remoteKey == null || (remoteKey.nextKey * state.config.pageSize) >= remoteKey.total
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
                        connectionId = connectionId
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
        } catch (e: HttpException) {
            e.printStackTrace()
            MediatorResult.Error(e)
        } catch (e: Exception) {
            e.printStackTrace()
            MediatorResult.Error(e)
        }
    }

    override suspend fun initialize(): InitializeAction {
        val cacheTimeout =
            TimeUnit.MILLISECONDS.convert(Constants.ARTIST_PAGE_TIME_FAILURE, TimeUnit.MINUTES)
        return if (System.currentTimeMillis() - (remoteKeyDao.remoteKeyById(remoteId)?.createTime
                ?: 0) <= cacheTimeout
        ) {
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }


    /**
     * 根据数据源删除艺术家
     * @param [dataSource] 数据源
     */
    suspend fun removeArtists() {
        db.artistDao.removeByDataSource()
    }
}