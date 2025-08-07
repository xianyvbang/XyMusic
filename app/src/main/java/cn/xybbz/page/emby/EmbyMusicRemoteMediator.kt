package cn.xybbz.page.emby

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import cn.xybbz.api.client.emby.EmbyDatasourceServer
import cn.xybbz.api.enums.jellyfin.ItemSortBy
import cn.xybbz.api.enums.jellyfin.SortOrder
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.enums.SortTypeEnum
import cn.xybbz.entity.data.SearchAndOrder
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.data.remote.RemoteCurrent
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import coil.network.HttpException
import java.io.IOException
import java.util.concurrent.TimeUnit


@OptIn(ExperimentalPagingApi::class)
class EmbyMusicRemoteMediator(
    private val sortType: SortTypeEnum?,
    private val ifFavorite: Boolean?,
    private val years: String?,
    private val db: DatabaseClient,
    private val embyDatasourceServer: EmbyDatasourceServer,
    private val connectionId: Long
) : RemoteMediator<Int, XyMusic>() {
    private val remoteKeyDao = db.remoteCurrentDao
    private val remoteId: String = Constants.MUSIC + connectionId

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, XyMusic>
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
            val yearsInt = years?.split(',')?.map { it.toInt() }
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

                SortTypeEnum.PREMIERE_DATE_ASC -> SearchAndOrder(ItemSortBy.PREMIERE_DATE)
                SortTypeEnum.PREMIERE_DATE_DESC -> SearchAndOrder(
                    ItemSortBy.PREMIERE_DATE,
                    SortOrder.DESCENDING
                )
            }

            val response = embyDatasourceServer.getServerMusicList(
                startIndex = loadKey * state.config.pageSize,
                state.config.pageSize,
                isFavorite = ifFavorite,
                sortBy = sortType.sortType?.let { listOf(sortType.sortType) },
                sortOrder = sortType.order?.let { listOf(sortType.order) },
                years = yearsInt
            )

            db.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    remoteKeyDao.deleteById(remoteId)
                    //删除音乐
                    db.musicDao.removeByType(MusicDataTypeEnum.HOME)
                }

                remoteKeyDao.insertOrReplace(
                    RemoteCurrent(
                        id = remoteId,
                        nextKey = loadKey,
                        total = response.totalRecordCount,
                        connectionId = connectionId
                    )
                )

                response.items.let {
                    embyDatasourceServer.saveBatchMusic(
                        it,
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