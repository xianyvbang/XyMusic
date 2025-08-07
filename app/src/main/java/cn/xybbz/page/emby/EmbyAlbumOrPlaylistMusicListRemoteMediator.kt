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
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalPagingApi::class)
class EmbyAlbumOrPlaylistMusicListRemoteMediator(
    private val itemId: String,
    private val sortType: SortTypeEnum?,
    private val ifFavorite: Boolean?,
    private val years: String?,
    private val embyDatasourceServer: EmbyDatasourceServer,
    private val db: DatabaseClient,
    private val dataType: MusicDataTypeEnum,
    private val connectionId: Long
) : RemoteMediator<Int, XyMusic>() {

    private val remoteKeyDao = db.remoteCurrentDao
    private val remoteId = Constants.ALBUM_MUSIC + itemId+connectionId

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

                SortTypeEnum.PREMIERE_DATE_ASC -> SearchAndOrder()
                SortTypeEnum.PREMIERE_DATE_DESC -> SearchAndOrder()
            }
            val response = embyDatasourceServer.getServerMusicList(
                startIndex = loadKey * state.config.pageSize,
                pageSize = state.config.pageSize,
                isFavorite = ifFavorite,
                sortBy = sortType.sortType?.let { listOf(sortType.sortType) },
                sortOrder = sortType.order?.let { listOf(sortType.order) },
                years = yearsInt,
                parentId = itemId
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
                embyDatasourceServer.saveBatchMusic(
                    items = response.items,
                    dataType = dataType,
                    playlistId = if (dataType == MusicDataTypeEnum.PLAYLIST) itemId else null
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