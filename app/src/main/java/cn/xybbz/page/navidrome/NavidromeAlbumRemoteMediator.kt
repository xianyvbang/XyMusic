package cn.xybbz.page.navidrome

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import cn.xybbz.api.client.navidrome.NavidromeDatasourceServer
import cn.xybbz.api.enums.navidrome.OrderType
import cn.xybbz.api.enums.navidrome.SortType
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.enums.SortTypeEnum
import cn.xybbz.entity.data.NavidromeOrder
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.data.remote.RemoteCurrent
import cn.xybbz.localdata.enums.DataSourceType
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import coil.network.HttpException
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * navidrome专辑的网络数据加载
 * @author xybbz
 * @date 2024/06/14
 * @constructor 创建[NavidromeAlbumRemoteMediator]
 * @param [userId] 用户id
 * @param [database] 本地缓存管理类
 * @param [dataSource] 数据源来兴
 */
@OptIn(ExperimentalPagingApi::class)
class NavidromeAlbumRemoteMediator(
    private val sortType: SortTypeEnum?,
    private val ifFavorite: Boolean?,
    private val years: String?,
    private val dataSource: DataSourceType,
    private val db: DatabaseClient,
    private val navidromeDatasourceServer: NavidromeDatasourceServer,
    private val connectionId: Long
) : RemoteMediator<Int, XyAlbum>() {

    private val remoteKeyDao = db.remoteCurrentDao
    private val remoteId = Constants.ALBUM + dataSource + connectionId
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

            val sortType: NavidromeOrder = when (sortType) {
                SortTypeEnum.CREATE_TIME_ASC -> {
                    NavidromeOrder(SortType.CREATED_AT)
                }

                SortTypeEnum.CREATE_TIME_DESC -> {
                    NavidromeOrder(SortType.CREATED_AT, OrderType.DESC)
                }

                SortTypeEnum.MUSIC_NAME_ASC -> {
                    NavidromeOrder()
                }

                SortTypeEnum.MUSIC_NAME_DESC -> {
                    NavidromeOrder()
                }

                SortTypeEnum.ALBUM_NAME_ASC -> {
                    NavidromeOrder(SortType.NAME)
                }

                SortTypeEnum.ALBUM_NAME_DESC -> {
                    NavidromeOrder(SortType.NAME, OrderType.DESC)
                }

                SortTypeEnum.ARTIST_NAME_ASC -> {
                    NavidromeOrder(SortType.ALBUM_ARTIST)
                }

                SortTypeEnum.ARTIST_NAME_DESC -> {
                    NavidromeOrder(SortType.ALBUM_ARTIST, OrderType.DESC)
                }

                null -> {
                    NavidromeOrder()
                }

                SortTypeEnum.PREMIERE_DATE_ASC -> NavidromeOrder(SortType.MAX_YEAR)
                SortTypeEnum.PREMIERE_DATE_DESC -> NavidromeOrder(SortType.MAX_YEAR, OrderType.DESC)
            }

            val yearsInt = years?.split(',')?.map { it.toInt() }

            val response = navidromeDatasourceServer.getAlbumList(
                startIndex = loadKey * state.config.pageSize,
                pageSize = state.config.pageSize,
                orderType = sortType.order,
                sortBy = sortType.sortType,
                isFavorite = ifFavorite,
                years = yearsInt
            )

            db.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    remoteKeyDao.deleteById(remoteId)
                    db.albumDao.removeByType(MusicDataTypeEnum.HOME)
                }

                remoteKeyDao.insertOrReplace(
                    RemoteCurrent(
                        id = remoteId,
                        nextKey = loadKey,
                        total = response.totalRecordCount,
                        connectionId = connectionId
                    )
                )
                response.items?.let { items ->
                    navidromeDatasourceServer.saveBatchAlbum(
                        items,
                        MusicDataTypeEnum.HOME
                    )
                }
            }

            MediatorResult.Success(
                endOfPaginationReached = response.items.isNullOrEmpty()
                        || (loadKey * state.config.pageSize) >= response.totalRecordCount
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