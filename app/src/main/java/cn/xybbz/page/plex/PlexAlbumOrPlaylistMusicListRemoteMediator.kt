package cn.xybbz.page.plex

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import cn.xybbz.api.client.plex.PlexDatasourceServer
import cn.xybbz.api.enums.plex.PlexSortOrder
import cn.xybbz.api.enums.plex.PlexSortType
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.enums.SortTypeEnum
import cn.xybbz.entity.data.PlexOrder
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.data.remote.RemoteCurrent
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalPagingApi::class)
class PlexAlbumOrPlaylistMusicListRemoteMediator(
    private val itemId: String,
    private val sortType: SortTypeEnum?,
    private val ifFavorite: Boolean?,
    private val years: String?,
    private val plexDatasourceServer: PlexDatasourceServer,
    private val db: DatabaseClient,
    private val dataType: MusicDataTypeEnum,
    private val connectionId: Long
) : RemoteMediator<Int, XyMusic>() {

    private val remoteKeyDao = db.remoteCurrentDao
    private val remoteId = Constants.ALBUM_MUSIC + itemId + connectionId

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
            val sortType: PlexOrder = when (sortType) {
                SortTypeEnum.CREATE_TIME_ASC -> {
                    PlexOrder(PlexSortType.ADDED_AT)
                }

                SortTypeEnum.CREATE_TIME_DESC -> {
                    PlexOrder(PlexSortType.ADDED_AT, PlexSortOrder.DESCENDING)
                }

                SortTypeEnum.MUSIC_NAME_ASC -> {
                    PlexOrder()
                }

                SortTypeEnum.MUSIC_NAME_DESC -> {
                    PlexOrder(order = PlexSortOrder.DESCENDING)
                }

                SortTypeEnum.ALBUM_NAME_ASC -> {
                    PlexOrder(PlexSortType.ALBUM_TITLE_SORT)
                }

                SortTypeEnum.ALBUM_NAME_DESC -> {
                    PlexOrder(PlexSortType.ALBUM_TITLE_SORT, PlexSortOrder.DESCENDING)
                }

                SortTypeEnum.ARTIST_NAME_ASC -> {
                    PlexOrder(PlexSortType.ARTIST_TITLE_SORT)
                }

                SortTypeEnum.ARTIST_NAME_DESC -> {
                    PlexOrder(PlexSortType.ARTIST_TITLE_SORT, PlexSortOrder.DESCENDING)
                }

                null -> {
                    PlexOrder()
                }

                SortTypeEnum.PREMIERE_DATE_ASC -> PlexOrder(PlexSortType.YEAR)
                SortTypeEnum.PREMIERE_DATE_DESC -> PlexOrder(
                    PlexSortType.YEAR,
                    PlexSortOrder.DESCENDING
                )
            }
            val response = plexDatasourceServer.getMusicListByAlbumOrPlaylist(
                startIndex = loadKey * state.config.pageSize,
                pageSize = state.config.pageSize,
                ifFavorite = ifFavorite,
                sortBy = sortType.sortType,
                sortOrder = sortType.order,
                params = if (yearsInt.isNullOrEmpty()) null else mapOf(
                    pairs = arrayOf(
                        Pair(
                            "year>=${yearsInt[0]}",
                            ""
                        ), Pair("year<=${yearsInt[yearsInt.size - 1]}", "")
                    )
                ),
                itemId = itemId,
                dataType = dataType
            )

            db.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    remoteKeyDao.deleteById(remoteId)
                    db.musicDao.removeByType(
                        dataType = dataType,
                        playlistId = itemId,
                        albumId = itemId
                    )
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
                    plexDatasourceServer.saveBatchMusic(
                        items = it,
                        dataType = dataType,
                        playlistId = if (dataType == MusicDataTypeEnum.PLAYLIST) itemId else null
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