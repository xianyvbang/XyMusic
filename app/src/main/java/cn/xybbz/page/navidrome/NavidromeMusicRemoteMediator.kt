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
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.data.remote.RemoteCurrent
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import coil.network.HttpException
import java.io.IOException
import java.util.concurrent.TimeUnit


@OptIn(ExperimentalPagingApi::class)
class NavidromeMusicRemoteMediator(
    private val sortType: SortTypeEnum?,
    private val ifFavorite: Boolean?,
    private val db: DatabaseClient,
    private val navidromeDatasourceServer: NavidromeDatasourceServer,
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
            val sortType: NavidromeOrder = when (sortType) {
                SortTypeEnum.CREATE_TIME_ASC -> {
                    NavidromeOrder(SortType.CREATED_AT)
                }

                SortTypeEnum.CREATE_TIME_DESC -> {
                    NavidromeOrder(SortType.CREATED_AT, OrderType.DESC)
                }

                SortTypeEnum.MUSIC_NAME_ASC -> {
                    NavidromeOrder(sortType = SortType.TITLE)
                }

                SortTypeEnum.MUSIC_NAME_DESC -> {
                    NavidromeOrder(sortType = SortType.TITLE, order = OrderType.DESC)
                }

                SortTypeEnum.ALBUM_NAME_ASC -> {
                    NavidromeOrder(SortType.ALBUM)
                }

                SortTypeEnum.ALBUM_NAME_DESC -> {
                    NavidromeOrder(SortType.ALBUM, OrderType.DESC)
                }

                SortTypeEnum.ARTIST_NAME_ASC -> {
                    NavidromeOrder(SortType.ARTIST)
                }

                SortTypeEnum.ARTIST_NAME_DESC -> {
                    NavidromeOrder(SortType.ARTIST, OrderType.DESC)
                }

                null -> {
                    NavidromeOrder(sortType = SortType.TITLE)
                }

                SortTypeEnum.PREMIERE_DATE_ASC -> NavidromeOrder(SortType.YEAR)
                SortTypeEnum.PREMIERE_DATE_DESC -> NavidromeOrder(SortType.YEAR, OrderType.DESC)
            }

            val response = navidromeDatasourceServer.getServerMusicList(
                startIndex = loadKey * state.config.pageSize,
                pageSize = state.config.pageSize,
                sortBy = sortType.sortType,
                sortOrder = sortType.order,
                isFavorite = ifFavorite,
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

                response.items?.let {
                    navidromeDatasourceServer.saveBatchMusic(
                        it,
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