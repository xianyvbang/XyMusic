package cn.xybbz.page.jellyfin

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import cn.xybbz.api.client.jellyfin.JellyfinDatasourceServer
import cn.xybbz.api.enums.jellyfin.ItemSortBy
import cn.xybbz.api.enums.jellyfin.SortOrder
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.enums.SortTypeEnum
import cn.xybbz.entity.data.SearchAndOrder
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.data.remote.RemoteCurrent
import cn.xybbz.localdata.enums.DataSourceType
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import coil.network.HttpException
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * jellyfin专辑的网络数据加载
 * @author xybbz
 * @date 2024/06/14
 * @constructor 创建[JellyfinAlbumRemoteMediator]
 * @param [userId] 用户id
 * @param [database] 本地缓存管理类
 * @param [dataSource] 数据源来兴
 */
@OptIn(ExperimentalPagingApi::class)
class JellyfinAlbumRemoteMediator(
    private val sortType: SortTypeEnum?,
    private val ifFavorite: Boolean?,
    private val years: String?,
    private val dataSource: DataSourceType,
    private val db: DatabaseClient,
    private val jellyfinDatasourceServer: JellyfinDatasourceServer,
    private val connectionId: Long
) : RemoteMediator<Int, XyAlbum>() {

    private val remoteKeyDao = db.remoteCurrentDao
    private val remoteId = Constants.ALBUM + dataSource
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

            val sortType: SearchAndOrder = when (sortType) {
                SortTypeEnum.CREATE_TIME_ASC -> {
                    SearchAndOrder(ItemSortBy.DATE_CREATED)
                }

                SortTypeEnum.CREATE_TIME_DESC -> {
                    SearchAndOrder(ItemSortBy.DATE_CREATED, SortOrder.DESCENDING)
                }

                SortTypeEnum.MUSIC_NAME_ASC -> {
                    SearchAndOrder()
                }

                SortTypeEnum.MUSIC_NAME_DESC -> {
                    SearchAndOrder(order = SortOrder.DESCENDING)
                }

                SortTypeEnum.ALBUM_NAME_ASC -> {
                    SearchAndOrder(ItemSortBy.ALBUM)
                }

                SortTypeEnum.ALBUM_NAME_DESC -> {
                    SearchAndOrder(ItemSortBy.ALBUM, SortOrder.DESCENDING)
                }

                SortTypeEnum.ARTIST_NAME_ASC -> {
                    SearchAndOrder(ItemSortBy.ARTIST)
                }

                SortTypeEnum.ARTIST_NAME_DESC -> {
                    SearchAndOrder(ItemSortBy.ARTIST, SortOrder.DESCENDING)
                }

                null -> {
                    SearchAndOrder()
                }

                SortTypeEnum.PREMIERE_DATE_ASC -> SearchAndOrder()
                SortTypeEnum.PREMIERE_DATE_DESC -> SearchAndOrder()
            }

            val yearsInt = years?.split(',')?.map { it.toInt() }


            val response = jellyfinDatasourceServer.getAlbumList(
                startIndex = loadKey * state.config.pageSize,
                pageSize = if (loadKey == 0) state.config.initialLoadSize else state.config.pageSize,
                sortBy = sortType.sortType?.let { listOf(sortType.sortType) },
                sortOrder = sortType.order?.let { listOf(sortType.order) },
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
                        nextKey = if (loadKey == 0) state.config.initialLoadSize / state.config.pageSize else loadKey,
                        total = response.totalRecordCount,
                        connectionId = connectionId
                    )
                )
                if (response.items.isNotEmpty()) {
                    //存储专辑
                    jellyfinDatasourceServer.saveBatchAlbum(
                        response.items,
                        MusicDataTypeEnum.HOME
                    )
                }
            }

            MediatorResult.Success(
                endOfPaginationReached = response.items.isEmpty()
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