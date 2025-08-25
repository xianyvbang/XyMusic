package cn.xybbz.api.client.plex

import android.content.Context
import android.icu.text.Transliterator
import android.os.Build
import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.room.withTransaction
import cn.xybbz.api.TokenServer
import cn.xybbz.api.client.IDataSourceParentServer
import cn.xybbz.api.client.data.AllResponse
import cn.xybbz.api.client.jellyfin.data.ClientLoginInfoReq
import cn.xybbz.api.client.plex.data.Directory
import cn.xybbz.api.client.plex.data.Metadatum
import cn.xybbz.api.client.plex.data.PlaylistMetadatum
import cn.xybbz.api.client.plex.data.toPlexLogin
import cn.xybbz.api.enums.plex.ImageType
import cn.xybbz.api.enums.plex.MetadatumType
import cn.xybbz.api.enums.plex.PlayState
import cn.xybbz.api.enums.plex.PlexListType
import cn.xybbz.api.enums.plex.PlexSortOrder
import cn.xybbz.api.enums.plex.PlexSortType
import cn.xybbz.api.exception.ConnectionException
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.constants.Constants.ALBUM_MUSIC_LIST_PAGE_SIZE
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.common.enums.SortTypeEnum
import cn.xybbz.common.utils.CharUtils
import cn.xybbz.common.utils.LrcUtils
import cn.xybbz.config.ConnectionConfigServer
import cn.xybbz.entity.api.LoginSuccessData
import cn.xybbz.entity.data.ResourceData
import cn.xybbz.entity.data.SearchData
import cn.xybbz.entity.data.file.backup.ExportPlaylistData
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.data.artist.XyArtist
import cn.xybbz.localdata.data.genre.XyGenre
import cn.xybbz.localdata.data.library.XyLibrary
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.data.type.PlaylistMusic
import cn.xybbz.localdata.enums.DataSourceType
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import cn.xybbz.page.all.ArtistMusicListRemoteMediator
import cn.xybbz.page.all.ArtistRemoteMediator
import cn.xybbz.page.plex.PlexAlbumOrPlaylistMusicListRemoteMediator
import cn.xybbz.page.plex.PlexAlbumRemoteMediator
import cn.xybbz.page.plex.PlexArtistAlbumListRemoteMediator
import cn.xybbz.page.plex.PlexFavoriteMusicRemoteMediator
import cn.xybbz.page.plex.PlexGenreAlbumListRemoteMediator
import cn.xybbz.page.plex.PlexGenresRemoteMediator
import cn.xybbz.page.plex.PlexMusicRemoteMediator
import cn.xybbz.ui.components.LrcEntry
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.supervisorScope
import okhttp3.OkHttpClient
import java.net.SocketTimeoutException
import java.time.ZoneOffset
import java.util.UUID

class PlexDatasourceServer(
    private val db: DatabaseClient,
    private val application: Context,
    private val connectionConfigServer: ConnectionConfigServer,
    private val plexApiClient: PlexApiClient
) : IDataSourceParentServer(
    db,
    connectionConfigServer,
    application
) {

    /**
     * 获得当前数据源类型
     */
    override fun getDataSourceType(): DataSourceType {
        return DataSourceType.PLEX
    }

    /**
     * 登录功能
     */
    override suspend fun login(clientLoginInfoReq: ClientLoginInfoReq): LoginSuccessData {
        plexLogin(clientLoginInfoReq)
        postPingSystem()
        return LoginSuccessData(
            userId = plexApiClient.userId,
            accessToken = plexApiClient.getToken(),
            serverId = clientLoginInfoReq.serverId,
            serverName = clientLoginInfoReq.serverName,
            version = clientLoginInfoReq.serverVersion
        )
    }

    private suspend fun plexLogin(clientLoginInfoReq: ClientLoginInfoReq): LoginSuccessData {
        val responseData =
            plexApiClient.userApi().authenticateByName(
                "https://plex.tv/api/v2/users/signin",
                clientLoginInfoReq.toPlexLogin()
            )
        Log.i("=====", "返回响应值: $responseData")
        plexApiClient.updateAccessToken(responseData.authToken)
        plexApiClient.updateServerInfo(userId = responseData.id)
        setToken()
        return LoginSuccessData(
            userId = plexApiClient.userId,
            accessToken = plexApiClient.getToken(),
            serverId = clientLoginInfoReq.serverId,
            serverName = clientLoginInfoReq.serverName,
            version = clientLoginInfoReq.serverVersion
        )
    }

    /**
     * 连通性检测
     */
    override suspend fun postPingSystem(): Boolean {
        try {
            val postPingSystem = plexApiClient.userApi().postPingSystem()
            Log.i("=====", postPingSystem.toString())
            //获得machineIdentifier
            plexApiClient.updateMachineIdentifier(postPingSystem.mediaContainer?.machineIdentifier)
        } catch (e: Exception) {
            Log.i(Constants.LOG_ERROR_PREFIX, "ping服务器失败", e)
            return false
        }

        return true
    }

    /**
     * 获得所有收藏数据
     */
    override suspend fun initFavoriteData() {
        //查询收藏合集是否存在,如果不存在则查询
        if (plexApiClient.musicFavoriteCollectionId == null) {
            //查询合集
            val musicCollection = plexApiClient.userLibraryApi()
                .getCollection(
                    connectionConfigServer.libraryId!!,
                    title = Constants.PLEX_MUSIC_COLLECTION_TITLE
                )
            if (musicCollection.mediaContainer?.metadata.isNullOrEmpty()) {
                val collectionResponse = plexApiClient.userLibraryApi().addCollection(
                    title = Constants.PLEX_MUSIC_COLLECTION_TITLE,
                    sectionId = connectionConfigServer.libraryId!!
                )
                val musicCollectionInfo = collectionResponse.mediaContainer?.metadata?.get(
                    0
                )
                plexApiClient.updateMusicFavoriteCollectionId(
                    musicCollectionInfo?.ratingKey,
                    musicCollectionInfo?.index
                )

            } else {
                val musicCollectionInfo = musicCollection.mediaContainer?.metadata?.get(0)
                plexApiClient.updateMusicFavoriteCollectionId(
                    musicCollectionInfo?.ratingKey,
                    musicCollectionInfo?.index
                )

            }
        }

        if (plexApiClient.albumFavoriteCollectionId == null) {
            //查询合集
            val albumCollection = plexApiClient.userLibraryApi()
                .getCollection(
                    connectionConfigServer.libraryId!!,
                    subtype = 9,
                    title = Constants.PLEX_ALBUM_COLLECTION_TITLE
                )
            if (albumCollection.mediaContainer?.metadata.isNullOrEmpty()) {
                val collectionResponse = plexApiClient.userLibraryApi().addCollection(
                    type = 9,
                    title = Constants.PLEX_ALBUM_COLLECTION_TITLE,
                    sectionId = connectionConfigServer.libraryId!!
                )
                val albumCollectionInfo = collectionResponse.mediaContainer?.metadata?.get(0)
                plexApiClient.updateAlbumFavoriteCollectionId(
                    albumCollectionInfo?.ratingKey,
                    albumCollectionInfo?.index
                )

            } else {
                val albumCollectionInfo = albumCollection.mediaContainer?.metadata?.get(
                    0
                )
                plexApiClient.updateAlbumFavoriteCollectionId(
                    albumCollectionInfo?.ratingKey,
                    albumCollectionInfo?.index
                )

            }
        }
        if (plexApiClient.artistFavoriteCollectionId == null) {
            //查询合集
            val albumCollection = plexApiClient.userLibraryApi()
                .getCollection(
                    connectionConfigServer.libraryId!!,
                    subtype = 8,
                    title = Constants.PLEX_ARTIST_COLLECTION_TITLE
                )
            if (albumCollection.mediaContainer?.metadata.isNullOrEmpty()) {
                val collectionResponse = plexApiClient.userLibraryApi().addCollection(
                    type = 8,
                    title = Constants.PLEX_ARTIST_COLLECTION_TITLE,
                    sectionId = connectionConfigServer.libraryId!!
                )
                val artistCollectionInfo = collectionResponse.mediaContainer?.metadata?.get(
                    0
                )
                plexApiClient.updateArtistFavoriteCollectionId(
                    artistCollectionInfo?.ratingKey,
                    artistCollectionInfo?.index
                )

            } else {
                val artistCollectionInfo = albumCollection.mediaContainer?.metadata?.get(
                    0
                )
                plexApiClient.updateArtistFavoriteCollectionId(
                    artistCollectionInfo?.ratingKey,
                    artistCollectionInfo?.index
                )

            }
        }
    }

    /**
     * 获得设备id
     */
    override fun getDeviceId(): String {
        return plexApiClient.clientId.ifBlank { super.getDeviceId() }
    }

    /**
     * 创建连接客户端
     * @param [address] 地址
     */
    override suspend fun createApiClient(
        address: String,
        deviceId: String,
        username: String,
        password: String
    ) {
        val packageManager = application.packageManager
        val packageName = application.packageName
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        val versionName = packageInfo.versionName
        val versionCode = packageInfo.longVersionCode
        plexApiClient.createApiClient(
            "XyMusic", deviceId, "${versionName}.${versionCode}", Build.BRAND, Build.MODEL
        )
        //提前写入没有sessionToken的Authenticate请求头,不然登录请求都会报错
        setToken()
        if (address.isBlank())
            plexApiClient.setRetrofitData("http://localhost")
        else
            plexApiClient.setRetrofitData(address)
    }

    /**
     * 获得资源地址
     */
    override suspend fun getResources(clientLoginInfoReq: ClientLoginInfoReq): List<ResourceData> {
        createApiClient(address = "", deviceId = getDeviceId(), username = "", "")
        plexLogin(clientLoginInfoReq)
        val systemInfo = plexApiClient.userApi()
            .getSystemInfo("https://plex.tv/api/v2/resources?includeHttps=1&includeRelay=1")
        Log.i("=====", "服务器信息 $systemInfo")

        if (systemInfo.isEmpty()) {
            throw ConnectionException()
        }

        val resourceData = systemInfo.flatMap { infoResponse ->
            infoResponse.connections.flatMap { connection ->
                val tmpResourceData = mutableListOf<ResourceData>()
                val ipv4Data = ResourceData(
                    infoResponse.name,
                    infoResponse.product,
                    "http://${connection.address}:${connection.port}",
                    serverVersion = infoResponse.platformVersion ?: "",
                    serverName = infoResponse.name,
                    serverId = UUID.randomUUID().toString()

                )
                tmpResourceData.add(ipv4Data)

                if (connection.iPv6 != null) {
                    val ipv6Data = ResourceData(
                        infoResponse.name,
                        infoResponse.product,
                        "http://[${connection.address}]:${connection.port}",
                        serverVersion = infoResponse.platformVersion ?: "",
                        serverName = infoResponse.name,
                        serverId = UUID.randomUUID().toString()
                    )
                    tmpResourceData.add(ipv6Data)
                }

                val resourceData = ResourceData(
                    name = infoResponse.name,
                    infoResponse.product,
                    connection.uri,
                    serverVersion = infoResponse.platformVersion ?: "",
                    serverName = infoResponse.name,
                    serverId = UUID.randomUUID().toString()
                )

                tmpResourceData.add(resourceData)
                tmpResourceData
            }
        }

        return resourceData

    }

    /**
     * 创建歌单
     */
    override suspend fun createPlaylist(name: String): String? {
        return plexApiClient.playlistsApi().createPlaylist(
            title = name
        ).mediaContainer?.metadata?.get(0)?.ratingKey
    }

    /**
     * 获得艺术家列表
     */
    override suspend fun getArtistList(
        startIndex: Int,
        pageSize: Int,
        isFavorite: Boolean?,
        search: String?
    ): AllResponse<XyArtist> {
        val response = plexApiClient.itemApi().getSongs(
            sectionKey = connectionConfigServer.libraryId!!,
            type = 8,
            selectType = PlexListType.all.toString(),
            start = startIndex,
            pageSize = pageSize,
            title = search,
            params = mapOf(Pair("1", "1"))
        )

        val artistList = response.mediaContainer?.metadata?.let {
            convertToArtistList(it)
        }
        return AllResponse(
            items = artistList,
            totalRecordCount = response.mediaContainer?.totalSize ?: 0,
            startIndex = startIndex
        )
    }

    /**
     * 根据艺术家id获得艺术家列表
     */
    override suspend fun selectArtistsByIds(artistIds: List<String>): List<XyArtist> {
        val items = artistIds.mapNotNull {
            plexApiClient.itemApi().getLibraryInfo(
                sectionKey = it
            ).mediaContainer?.metadata
        }.flatMap { it }
        return convertToArtistList(items)
    }

    /**
     * 根据艺术家获得音乐列表
     */
    override suspend fun selectMusicListByArtistServer(
        artistId: String,
        pageSize: Int,
        startIndex: Int
    ): AllResponse<XyMusic> {
        return getServerMusicList(
            plexListType = PlexListType.all,
            startIndex = startIndex,
            pageSize = pageSize,
            artistId = artistId,
            sortBy = PlexSortType.RATING_COUNT
        )
    }

    /**
     * 获得专辑列表数据
     */
    @OptIn(ExperimentalPagingApi::class)
    override fun selectAlbumFlowList(
        sortType: SortTypeEnum?,
        ifFavorite: Boolean?,
        years: String?
    ): Flow<PagingData<XyAlbum>> {
        return Pager(
            config = PagingConfig(
                pageSize = Constants.UI_LIST_PAGE,  // 每一页个数
                enablePlaceholders = true
            ), remoteMediator = PlexAlbumRemoteMediator(
                sortType = sortType,
                ifFavorite = ifFavorite,
                years = years,
                dataSource = getDataSourceType(),
                db = db,
                plexDatasourceServer = this,
                connectionId = connectionConfigServer.getConnectionId()
            )
        ) {
            db.albumDao.selectHomeAlbumListPage()
        }.flow
    }

    /**
     * 获得音乐列表数据
     */
    @OptIn(ExperimentalPagingApi::class)
    override fun selectMusicFlowList(
        sortType: SortTypeEnum?,
        ifFavorite: Boolean?,
        years: String?
    ): Flow<PagingData<XyMusic>> {
        return Pager(
            config = PagingConfig(
                pageSize = Constants.UI_LIST_PAGE,  // 每一页个数
                prefetchDistance = 2, // 距离下一页请求的距离
                initialLoadSize = Constants.UI_LIST_PAGE // 第一次加载数量，如果不设置的话是 pageSize * 2
            ), remoteMediator = PlexMusicRemoteMediator(
                sortType = sortType,
                db = db,
                plexDatasourceServer = this,
                ifFavorite = ifFavorite,
                connectionId = connectionConfigServer.getConnectionId()
            )
        ) {
            db.musicDao.selectHomeMusicListPage()
        }.flow
    }

    /**
     * 获得艺术家
     */
    @OptIn(ExperimentalPagingApi::class)
    override fun selectArtistFlowList(
        ifFavorite: Boolean?,
        selectChat: String?
    ): Flow<PagingData<XyArtist>> {
        return Pager(
            config = PagingConfig(
                pageSize = Constants.PAGE_SIZE_ALL,  // 每一页个数
                enablePlaceholders = true
            ), remoteMediator = ArtistRemoteMediator(
                db = db,
                datasourceServer = this,
                ifFavorite = ifFavorite,
                dataSource = getDataSourceType(),
                connectionId = connectionConfigServer.getConnectionId()
            )
        ) {
            db.artistDao.selectListPagingSource(selectChat)
        }.flow
    }

    /**
     * 搜索音乐,艺术家,专辑
     */
    override suspend fun searchAll(search: String): SearchData {
        val searchData = supervisorScope {
            val searchData = SearchData()
            val album = async {
                searchData.albums = try {
                    val albumList = searchAlbumFlowList(search)
                    db.albumDao.saveDataBatch(albumList)
                    albumList
                } catch (e: SocketTimeoutException) {
                    Log.e(Constants.LOG_ERROR_PREFIX, "加载专辑超时", e)
                    null
                } catch (e: Exception) {
                    Log.e(Constants.LOG_ERROR_PREFIX, "加载专辑报错", e)
                    null
                }

            }

            val artist = async {
                searchData.artists = try {
                    val artistList = searchArtistFlowList(search)
                    saveBatchArtist(artistList)
                    artistList
                } catch (e: SocketTimeoutException) {
                    Log.e(Constants.LOG_ERROR_PREFIX, "加载艺术家超时", e)
                    null
                } catch (e: Exception) {
                    Log.e(Constants.LOG_ERROR_PREFIX, "加载艺术家报错", e)
                    null
                }

            }

            val music = async {
                searchData.musics = try {
                    val musicList = searchMusicFlowList(search)
                    db.musicDao.saveDataBatch(musicList)
                    musicList
                } catch (e: SocketTimeoutException) {
                    Log.e(Constants.LOG_ERROR_PREFIX, "加载音乐超时", e)
                    null
                } catch (e: Exception) {
                    Log.e(Constants.LOG_ERROR_PREFIX, "加载音乐报错", e)
                    null
                }

            }

            album.await()
            artist.await()
            music.await()
            searchData
        }

        return searchData
    }

    /**
     * 获得专辑或歌单内音乐列表
     * @param [sortType] 排序类型
     * @param [ifFavorite] 是否收藏筛选
     * @param [years] 筛选年代数据
     * @param [itemId] 专辑id
     * @param [dataType] 数据类型
     * @return [Flow<PagingData<XyMusic>>]
     */
    @OptIn(ExperimentalPagingApi::class)
    override fun selectMusicListByParentId(
        sortType: SortTypeEnum?,
        ifFavorite: Boolean?,
        years: String?,
        itemId: String,
        dataType: MusicDataTypeEnum
    ): Flow<PagingData<XyMusic>> {
        return Pager(
            config = PagingConfig(
                pageSize = ALBUM_MUSIC_LIST_PAGE_SIZE,  // 每一页个数
//                    prefetchDistance = ALBUM_MUSIC_LIST_PAGE_SIZE, // 距离下一页请求的距离
                initialLoadSize = ALBUM_MUSIC_LIST_PAGE_SIZE,  // 第一次加载数量，如果不设置的话是 pageSize * 2
            ), remoteMediator = PlexAlbumOrPlaylistMusicListRemoteMediator(
                itemId = itemId,
                sortType = sortType,
                ifFavorite = ifFavorite,
                years = years,
                plexDatasourceServer = this,
                db = db,
                dataType = dataType,
                connectionId = connectionConfigServer.getConnectionId()
            )
        ) {
            if (dataType == MusicDataTypeEnum.ALBUM)
                db.musicDao.selectAlbumMusicListPage(albumId = itemId)
            else
                db.musicDao.selectPlaylistMusicListPage(playlistId = itemId)
        }.flow
    }

    /**
     * 将项目标记为收藏
     * @param [itemId] 专辑/音乐id
     */
    override suspend fun markFavoriteItem(
        itemId: String,
        dataType: MusicTypeEnum
    ): Boolean {
        //查询收藏合集是否存在,如果不存在则查询
        var collectionId: String?
        when (dataType) {
            MusicTypeEnum.MUSIC -> {
                if (plexApiClient.musicFavoriteCollectionId == null) {
                    //查询合集
                    val musicCollection = plexApiClient.userLibraryApi()
                        .getCollection(
                            connectionConfigServer.libraryId!!,
                            title = Constants.PLEX_MUSIC_COLLECTION_TITLE
                        )
                    if (musicCollection.mediaContainer?.metadata.isNullOrEmpty()) {
                        val collectionResponse = plexApiClient.userLibraryApi().addCollection(
                            title = Constants.PLEX_MUSIC_COLLECTION_TITLE,
                            sectionId = connectionConfigServer.libraryId!!
                        )
                        val musicCollectionInfo = collectionResponse.mediaContainer?.metadata?.get(
                            0
                        )
                        plexApiClient.updateMusicFavoriteCollectionId(
                            musicCollectionInfo?.ratingKey,
                            musicCollectionInfo?.index
                        )

                    } else {

                        val musicCollectionInfo = musicCollection.mediaContainer?.metadata?.get(0)
                        plexApiClient.updateMusicFavoriteCollectionId(
                            musicCollectionInfo?.ratingKey,
                            musicCollectionInfo?.index
                        )

                    }
                    if (plexApiClient.musicFavoriteCollectionId == null) {
                        return false
                    }
                }
                collectionId = plexApiClient.musicFavoriteCollectionId
            }

            MusicTypeEnum.ALBUM -> {
                if (plexApiClient.albumFavoriteCollectionId == null) {
                    //查询合集
                    val albumCollection = plexApiClient.userLibraryApi()
                        .getCollection(
                            connectionConfigServer.libraryId!!,
                            subtype = 9,
                            title = Constants.PLEX_ALBUM_COLLECTION_TITLE
                        )
                    if (albumCollection.mediaContainer?.metadata.isNullOrEmpty()) {
                        val collectionResponse = plexApiClient.userLibraryApi().addCollection(
                            type = 9,
                            title = Constants.PLEX_ALBUM_COLLECTION_TITLE,
                            sectionId = connectionConfigServer.libraryId!!
                        )
                        val albumCollectionInfo =
                            collectionResponse.mediaContainer?.metadata?.get(0)
                        plexApiClient.updateAlbumFavoriteCollectionId(
                            albumCollectionInfo?.ratingKey,
                            albumCollectionInfo?.index
                        )

                    } else {
                        val albumCollectionInfo = albumCollection.mediaContainer?.metadata?.get(
                            0
                        )
                        plexApiClient.updateAlbumFavoriteCollectionId(
                            albumCollectionInfo?.ratingKey,
                            albumCollectionInfo?.index
                        )

                    }
                    if (plexApiClient.albumFavoriteCollectionId == null) {
                        return false
                    }
                }
                collectionId = plexApiClient.albumFavoriteCollectionId
            }

            MusicTypeEnum.ARTIST -> {
                if (plexApiClient.artistFavoriteCollectionId == null) {
                    //查询合集
                    val albumCollection = plexApiClient.userLibraryApi()
                        .getCollection(
                            connectionConfigServer.libraryId!!,
                            subtype = 8,
                            title = Constants.PLEX_ARTIST_COLLECTION_TITLE
                        )
                    if (albumCollection.mediaContainer?.metadata.isNullOrEmpty()) {
                        val collectionResponse = plexApiClient.userLibraryApi().addCollection(
                            type = 8,
                            title = Constants.PLEX_ARTIST_COLLECTION_TITLE,
                            sectionId = connectionConfigServer.libraryId!!
                        )
                        val artistCollectionInfo = collectionResponse.mediaContainer?.metadata?.get(
                            0
                        )
                        plexApiClient.updateArtistFavoriteCollectionId(
                            artistCollectionInfo?.ratingKey,
                            artistCollectionInfo?.index
                        )

                    } else {
                        val artistCollectionInfo = albumCollection.mediaContainer?.metadata?.get(
                            0
                        )
                        plexApiClient.updateArtistFavoriteCollectionId(
                            artistCollectionInfo?.ratingKey,
                            artistCollectionInfo?.index
                        )

                    }
                    if (plexApiClient.artistFavoriteCollectionId == null) {
                        return false
                    }
                }

                collectionId = plexApiClient.artistFavoriteCollectionId
            }
        }

        if (collectionId == null) {
            return false
        }

        plexApiClient.userLibraryApi()
            .markFavoriteItem(
                collectionId = collectionId,
                uri = plexApiClient.createMusicUri(itemId)
            )
        db.musicDao.updateFavoriteByItemId(
            true,
            itemId,
            connectionConfigServer.getConnectionId()
        )
        return true
    }

    /**
     * 取消项目收藏
     * @param [itemId] 专辑/音乐id
     */
    override suspend fun unmarkFavoriteItem(
        itemId: String,
        dataType: MusicTypeEnum
    ): Boolean {
        var collectionId: String?
        when (dataType) {
            MusicTypeEnum.MUSIC -> {
                if (plexApiClient.musicFavoriteCollectionId == null) {
                    //查询合集
                    val musicCollection = plexApiClient.userLibraryApi()
                        .getCollection(
                            connectionConfigServer.libraryId!!,
                            title = Constants.PLEX_MUSIC_COLLECTION_TITLE
                        )
                    val musicCollectionInfo = musicCollection.mediaContainer?.metadata?.get(
                        0
                    )
                    plexApiClient.updateMusicFavoriteCollectionId(
                        musicCollectionInfo?.ratingKey,
                        musicCollectionInfo?.index
                    )

                    if (plexApiClient.musicFavoriteCollectionId == null) {
                        return true
                    }
                }
                collectionId = plexApiClient.musicFavoriteCollectionId
            }

            MusicTypeEnum.ALBUM -> {
                if (plexApiClient.albumFavoriteCollectionId == null) {
                    //查询合集
                    val albumCollection = plexApiClient.userLibraryApi()
                        .getCollection(
                            connectionConfigServer.libraryId!!,
                            subtype = 9,
                            title = Constants.PLEX_ALBUM_COLLECTION_TITLE
                        )
                    val albumCollectionInfo = albumCollection.mediaContainer?.metadata?.get(
                        0
                    )
                    plexApiClient.updateAlbumFavoriteCollectionId(
                        albumCollectionInfo?.ratingKey,
                        albumCollectionInfo?.index
                    )

                    if (plexApiClient.albumFavoriteCollectionId == null) {
                        return true
                    }
                }
                collectionId = plexApiClient.albumFavoriteCollectionId
            }

            MusicTypeEnum.ARTIST -> {
                if (plexApiClient.artistFavoriteCollectionId == null) {
                    //查询合集
                    val albumCollection = plexApiClient.userLibraryApi()
                        .getCollection(
                            connectionConfigServer.libraryId!!,
                            subtype = 8,
                            title = Constants.PLEX_ARTIST_COLLECTION_TITLE
                        )
                    val albumCollectionInfo = albumCollection.mediaContainer?.metadata?.get(
                        0
                    )
                    plexApiClient.updateArtistFavoriteCollectionId(
                        albumCollectionInfo?.ratingKey,
                        albumCollectionInfo?.index
                    )

                    if (plexApiClient.artistFavoriteCollectionId == null) {
                        return true
                    }
                }

                collectionId = plexApiClient.artistFavoriteCollectionId
            }
        }

        if (collectionId == null) {
            return true
        }

        plexApiClient.userLibraryApi().unmarkFavoriteItem(
            musicId = itemId,
            collectionId = collectionId
        )
        db.musicDao.updateFavoriteByItemId(
            false,
            itemId,
            connectionConfigServer.getConnectionId()
        )
        return false
    }

    /**
     * 获得专辑,艺术家,音频,歌单数量
     */
    override suspend fun getDataInfoCount(connectionId: Long) {
        var album: Int? = null
        var artist: Int? = null
        var music: Int? = null
        var playlist: Int? = null
        var genres: Int? = null
        var favorite: Int? = null
        supervisorScope {
            val album = async {
                album = try {
                    getAlbumList(pageSize = 0, startIndex = 0).totalRecordCount
                } catch (e: SocketTimeoutException) {
                    Log.e(Constants.LOG_ERROR_PREFIX, "加载专辑数量超时", e)
                    null
                } catch (e: Exception) {
                    Log.e(Constants.LOG_ERROR_PREFIX, "加载专辑数量报错", e)
                    null
                }

            }

            val artist = async {
                artist = try {
                    getArtistList(startIndex = 0, pageSize = 0).totalRecordCount
                } catch (e: SocketTimeoutException) {
                    Log.e(Constants.LOG_ERROR_PREFIX, "加载艺术家数量超时", e)
                    null

                } catch (e: Exception) {
                    Log.e(Constants.LOG_ERROR_PREFIX, "加载艺术家数量报错", e)
                    null
                }
            }

            val music = async {
                music = try {
                    getServerMusicList(startIndex = 0, pageSize = 0).totalRecordCount
                } catch (e: SocketTimeoutException) {
                    Log.e(Constants.LOG_ERROR_PREFIX, "加载音乐数量超时", e)
                    null

                } catch (e: Exception) {
                    Log.e(Constants.LOG_ERROR_PREFIX, "加载音乐数量报错", e)
                    null
                }
            }

            val playlist = async {
                playlist = try {
                    getPlaylistsServer(0, 0).totalRecordCount
                } catch (e: SocketTimeoutException) {
                    Log.e(Constants.LOG_ERROR_PREFIX, "加载歌单数量超时", e)
                    null

                } catch (e: Exception) {
                    Log.e(Constants.LOG_ERROR_PREFIX, "加载歌单数量报错", e)
                    null
                }

            }

            val genres = async {
                genres = try {
                    getGenreList(
                        startIndex = 0,
                        pageSize = 0,
                    ).totalRecordCount
                } catch (e: Exception) {
                    Log.e(Constants.LOG_ERROR_PREFIX, "加载流派数量报错", e)
                    null
                }
            }

            val favorite = async {
                favorite = try {
                    getFavoriteMusicList(
                        startIndex = 0, pageSize = 0,
                    ).totalRecordCount
                } catch (e: Exception) {
                    Log.e(Constants.LOG_ERROR_PREFIX, "加载收藏数量报错", e)
                    null
                }

            }

            album.await()
            artist.await()
            music.await()
            playlist.await()
            genres.await()
            favorite.await()
        }

        updateOrSaveDataInfoCount(music, album, artist, playlist, genres, favorite)
    }

    /**
     * 删除数据
     * @param [musicId] 需要删除数据的id
     * @return true->删除成功,false->删除失败
     */
    override suspend fun removeById(musicId: String): Boolean {
        plexApiClient.libraryApi().deleteItem(itemId = musicId)
        return true
    }

    /**
     * 批量删除数据
     * 按 ID 删除
     * @param [musicIds] 需要删除数据的
     * @return [Boolean?]
     */
    override suspend fun removeByIds(musicIds: List<String>): Boolean {
        musicIds.forEach { musicId ->
            try {
                removeById(musicId)
            } catch (e: Exception) {
                Log.e(Constants.LOG_ERROR_PREFIX, "删除音乐部分失败", e)
            }
        }
        return true
    }

    /**
     * 获得专辑信息
     * @param [albumId] 专辑id
     * @return 专辑+艺术家信息
     */
    override suspend fun selectAlbumInfoById(
        albumId: String,
        dataType: MusicDataTypeEnum
    ): XyAlbum? {
        var artistExtend = db.albumDao.selectById(albumId)
        if (artistExtend == null) {
            if (dataType == MusicDataTypeEnum.ALBUM) {
                val album = plexApiClient.itemApi().getLibraryInfo(albumId)
                artistExtend = album.mediaContainer?.metadata?.get(0)?.let { convertToAlbum(it) }
            } else {
                val playlist = plexApiClient.playlistsApi()
                    .getPlaylistById(albumId)
                artistExtend =
                    playlist.mediaContainer?.metadata?.get(0)?.let { convertToPlaylist(it) }
            }
        }
        return artistExtend
    }

    /**
     * 按 ID 选择音乐信息
     * @param [itemId] 音乐唯一标识
     * @return [XyMusic?]
     */
    override suspend fun selectMusicInfoById(itemId: String): XyMusic? {
        val libraryInfo = plexApiClient.itemApi().getLibraryInfo(itemId)
        //获得音乐信息
        val part = libraryInfo.mediaContainer?.metadata?.get(0)?.media?.get(0)?.part?.get(0)
        val mediaSourceInfo =
            part?.stream?.find { it.streamType == 2L }
        return db.musicDao.selectById(itemId)?.copy(
            sampleRate = mediaSourceInfo?.samplingRate,
            bitDepth = mediaSourceInfo?.bitDepth,
        )
    }

    /**
     * 根据音乐获得歌词信息
     * @param [music] 音乐id
     * @return 返回歌词列表
     */
    override suspend fun getMusicLyricList(music: XyMusic): List<LrcEntry>? {
        return if (music.ifLyric && !music.lyric.isNullOrBlank()) {
            val lyrics = music.lyric?.let { plexApiClient.lyricsApi().getLyrics(it) }
            val lrcEntries = lyrics?.let {
                LrcUtils.parseLrc(lyrics)
            }
            lrcEntries
        } else {
            null
        }
    }

    /**
     * 根据艺术家获得专辑列表
     */
    @OptIn(ExperimentalPagingApi::class)
    override fun selectAlbumListByArtistId(artistId: String): Flow<PagingData<XyAlbum>> {
        return Pager(
            config = PagingConfig(
                pageSize = ALBUM_MUSIC_LIST_PAGE_SIZE,  // 每一页个数
                prefetchDistance = 0, // 距离下一页请求的距离
                initialLoadSize = ALBUM_MUSIC_LIST_PAGE_SIZE  // 第一次加载数量，如果不设置的话是 pageSize * 2
            ), remoteMediator = PlexArtistAlbumListRemoteMediator(
                artistId = artistId,
                plexDatasourceServer = this,
                db = db,
                connectionId = connectionConfigServer.getConnectionId()
            )
        ) {
            db.albumDao.selectArtistAlbumListPage(
                artistId
            )
        }.flow
    }

    /**
     * 根据艺术家获得音乐列表
     */
    @OptIn(ExperimentalPagingApi::class)
    override fun selectMusicListByArtistId(artistId: String): Flow<PagingData<XyMusic>> {
        return Pager(
            PagingConfig(
                pageSize = Constants.UI_LIST_PAGE,  // 每一页个数
                prefetchDistance = 2, // 距离下一页请求的距离
                initialLoadSize = Constants.UI_LIST_PAGE // 第一次加载数量，如果不设置的话是 pageSize * 2
            ), remoteMediator = ArtistMusicListRemoteMediator(
                artistId = artistId,
                datasourceServer = this,
                db = db,
                connectionId = connectionConfigServer.getConnectionId()
            )
        ) {
            db.musicDao.selectArtistMusicListPage(
                artistId = artistId
            )
        }.flow
    }

    /**
     * 获得随机音乐
     */
    override suspend fun getRandomMusicList(
        pageSize: Int,
        pageNum: Int
    ): List<XyMusic>? {
        return getServerMusicList(
            startIndex = pageNum * pageSize,
            pageSize = pageSize,
            sortBy = PlexSortType.RANDOM
        ).items
    }

    /**
     * 获取歌单列表
     */
    override suspend fun getPlaylists(): List<XyAlbum>? {
        return db.withTransaction {
            val response = getPlaylistsServer(0, 10000)
            db.albumDao.removePlaylist()
            response.items?.let {
                saveBatchAlbum(it, MusicDataTypeEnum.PLAYLIST, true)
            }
        }
    }

    /**
     * 导入歌单
     */
    override suspend fun importPlaylist(playlistData: ExportPlaylistData): Boolean {
        val playlists = playlistData.playlist
        val playlistMusicMap = playlistData.playlistMusic.groupBy { it.playlistId }
        if (playlists.isNotEmpty()) {

            val playlistServerList = getPlaylistsServer(0, 1000)
            //获得新建的表单
            val itemDataList: List<XyAlbum>? = playlistServerList.items
            if (!itemDataList.isNullOrEmpty()) {
                val playlistMap = itemDataList.associateBy { it.itemId }
                playlists.forEach { playlist ->
                    if (playlistMap.containsKey(playlist.itemId)) {
                        val playlistMusics = playlistMusicMap[playlist.itemId]
                        if (!playlistMusics.isNullOrEmpty()) {
                            //更新音乐数据
                            saveMusicPlaylist(
                                playlistId = playlist.itemId,
                                musicIds = playlistMusics.map { music -> music.musicId },
                                pic = playlist.pic
                            )
                        }
                    } else {
                        playlists.forEach { playlist ->
                            val playlistId = createPlaylist(
                                name = playlist.name
                            )
                            val playlistMusics = playlistMusicMap[playlist.itemId]
                            if (!playlistMusics.isNullOrEmpty() && !playlistId.isNullOrBlank()) {
                                saveMusicPlaylist(
                                    playlistId = playlistId,
                                    musicIds = playlistMusics.map { it.musicId },
                                    pic = playlist.pic
                                )
                            }
                        }
                    }
                }
            }
        }
        return true
    }

    /**
     * 编辑歌单名称
     * @param [id] ID
     * @param [name] 姓名
     */
    override suspend fun editPlaylistName(id: String, name: String): Boolean {
        plexApiClient.playlistsApi().updatePlaylist(
            playlistId = id, title = name
        )
        db.albumDao.updateName(
            id, name
        )
        return true
    }

    /**
     * 删除歌单
     * @param [id] ID
     */
    override suspend fun removePlaylist(id: String): Boolean {
        plexApiClient.playlistsApi().deletePlaylist(
            playlistId = id
        )
        db.albumDao.removeById(
            id
        )
        return true
    }

    /**
     * 保存自建歌单中的音乐
     * @param [playlistId] 歌单id
     * @param [musicIds] 音乐id集合
     * @param [pic] 自建歌单图片
     */
    override suspend fun saveMusicPlaylist(
        playlistId: String,
        musicIds: List<String>,
        pic: String?
    ): Boolean {
        plexApiClient.playlistsApi().addPlaylistMusics(
            playlistId = playlistId,
            uri = plexApiClient.createMusicUri(musicIds)
        )
        var playlistIndex = db.musicDao.selectPlaylistIndex() ?: -1
        val playlists = musicIds.map { musicId ->
            playlistIndex += 1
            PlaylistMusic(
                playlistId = playlistId,
                musicId = musicId,
                index = playlistIndex,
                connectionId = connectionConfigServer.getConnectionId()
            )
        }
        db.musicDao.savePlaylistMusic(playlists)
        //更新歌单的封面信息
        db.albumDao.updatePic(playlistId, pic.toString())
        return true
    }

    /**
     * 删除自建歌单中的音乐
     * @param [playlistId] 歌单id
     * @param [musicIds] 音乐id集合
     */
    override suspend fun removeMusicPlaylist(
        playlistId: String,
        musicIds: List<String>
    ): Boolean {
        musicIds.forEach {
            try {
                plexApiClient.playlistsApi().removePlaylistMusics(
                    playlistId = playlistId,
                    itemId = it
                )

                db.musicDao.removeByPlaylistMusicByMusicId(
                    playlistId = playlistId,
                    musicId = it
                )
            } catch (e: Exception) {
                Log.e(Constants.LOG_ERROR_PREFIX, "从播放列表删除音乐失败", e)
            }
        }

        //获得歌单中的第一个音乐,并写入歌单封面
        val musicInfo = db.musicDao.selectPlaylistMusicOneById(playlistId)
        if (musicInfo != null && !musicInfo.pic.isNullOrBlank()) {
            musicInfo.pic?.let {
                db.albumDao.updatePicAndCount(playlistId, it)
            }
        }
        return true
    }

    /**
     * 根据id获得艺术家信息
     * @param [artistId] 艺术家id
     * @return [List<ArtistItem>?] 艺术家信息
     */
    override suspend fun selectArtistInfoById(artistId: String): XyArtist? {
        var artistInfo: XyArtist? = db.artistDao.selectById(artistId)
        if (artistInfo == null) {
            val item = plexApiClient.itemApi()
                .getLibraryInfo(sectionKey = artistId)
            artistInfo = item.mediaContainer?.metadata?.get(0)?.let {
                convertToArtist(it, 0)
            }
        }
        return artistInfo
    }

    /**
     * 获得媒体库列表
     */
    override suspend fun selectMediaLibrary() {
        db.withTransaction {
            db.libraryDao.remove()
            val viewLibrary = plexApiClient.userViewsApi().getUserViews()
            //存储历史记录
            val libraries =
                viewLibrary.mediaContainer?.directory?.filter { it.type == MetadatumType.Artist }
                    ?.map {
                        XyLibrary(
                            id = it.key,
                            collectionType = it.type.toString(),
                            name = it.title,
                            connectionId = connectionConfigServer.getConnectionId()
                        )
                    }
            if (!libraries.isNullOrEmpty()) {
                db.libraryDao.saveBatch(libraries)
                //将id写入到lib中
                val librariesList = db.libraryDao.selectListByDataSourceType()
                if (librariesList.isNotEmpty()) {
                    val library = librariesList[0]
                    db.connectionConfigDao.updateLibraryId(
                        libraryId = library.id,
                        connectionId = connectionConfigServer.getConnectionId()
                    )
                    connectionConfigServer.updateLibraryId(library.id)
                }
            }
        }

    }

    /**
     * 获得最近播放音乐或专辑
     */
    override suspend fun playRecordMusicOrAlbumList() {
        val albumList = getAlbumList(
            plexListType = PlexListType.all,
            type = 9,
            startIndex = 0,
            pageSize = Constants.MIN_PAGE,
            sortBy = PlexSortType.LAST_VIEWED_AT,
            sortOrder = PlexSortOrder.DESCENDING
        )
        albumList.items?.let { albums ->
            db.withTransaction {
                db.albumDao.removeByType(MusicDataTypeEnum.PLAY_HISTORY)
                saveBatchAlbum(
                    albums, MusicDataTypeEnum.PLAY_HISTORY
                )

            }
        }

        val musicList = getServerMusicList(
            startIndex = 0,
            pageSize = Constants.MIN_PAGE,
            sortBy = PlexSortType.LAST_VIEWED_AT,
            sortOrder = PlexSortOrder.DESCENDING
        ).items
        if (!musicList.isNullOrEmpty())
            db.withTransaction {
                db.musicDao.removeByType(MusicDataTypeEnum.PLAY_HISTORY)
                saveBatchMusic(musicList, MusicDataTypeEnum.PLAY_HISTORY)
            }
    }

    /**
     * 获得最多播放
     */
    override suspend fun getMostPlayerMusicList() {
        val response = getServerMusicList(
            plexListType = PlexListType.all,
            startIndex = 0,
            pageSize = Constants.MIN_PAGE,
            sortBy = PlexSortType.VIEWCOUNT,
            sortOrder = PlexSortOrder.DESCENDING,
            params = mapOf(Pair("viewCount>>=0", ""))
        )
        response.items?.let { musicList ->
            db.withTransaction {
                db.musicDao.removeByType(MusicDataTypeEnum.MAXIMUM_PLAY)
                saveBatchMusic(musicList, dataType = MusicDataTypeEnum.MAXIMUM_PLAY)
            }
        }

    }

    /**
     * 获得最新专辑
     */
    override suspend fun getNewestAlbumList() {
        val albumList = getAlbumList(
            plexListType = PlexListType.all,
            type = 9,
            startIndex = 0,
            pageSize = Constants.MIN_PAGE,
            sortBy = PlexSortType.ADDED_AT,
            sortOrder = PlexSortOrder.DESCENDING
        )
        albumList.items?.let { albums ->
            db.withTransaction {
                db.albumDao.removeByType(MusicDataTypeEnum.NEWEST)
                saveBatchAlbum(
                    albums, MusicDataTypeEnum.NEWEST
                )

            }
        }

    }

    /**
     * 获得收藏歌曲列表
     */
    @OptIn(ExperimentalPagingApi::class)
    override fun selectFavoriteMusicFlowList(): Flow<PagingData<XyMusic>> {
        return Pager(
            PagingConfig(
                pageSize = Constants.UI_LIST_PAGE,  // 每一页个数
                prefetchDistance = 2, // 距离下一页请求的距离
                initialLoadSize = Constants.UI_LIST_PAGE // 第一次加载数量，如果不设置的话是 pageSize * 2
            ),
            remoteMediator = PlexFavoriteMusicRemoteMediator(
                plexDatasourceServer = this,
                db = db,
                connectionId = connectionConfigServer.getConnectionId()
            )
        ) {
            db.musicDao.selectFavoriteMusicListPage()
        }.flow
    }

    /**
     * 获得流派列表
     */
    @OptIn(ExperimentalPagingApi::class)
    override suspend fun selectGenresPage(): Flow<PagingData<XyGenre>> {
        return Pager(
            PagingConfig(
                pageSize = Constants.UI_LIST_PAGE,  // 每一页个数
                prefetchDistance = 2, // 距离下一页请求的距离
                initialLoadSize = Constants.UI_LIST_PAGE // 第一次加载数量，如果不设置的话是 pageSize * 2
            ),
            remoteMediator = PlexGenresRemoteMediator(
                plexDatasourceServer = this,
                db = db,
                connectionId = connectionConfigServer.getConnectionId()
            )
        ) {
            db.genreDao.selectByDataSourceType()
        }.flow
    }

    /**
     * 获得流派详情
     */
    override suspend fun getGenreById(genreId: String): XyGenre? {
        return db.genreDao.selectById(genreId)
    }

    /**
     * 获得流派内音乐列表/或者专辑
     * @param [genreId] 流派id
     */
    @OptIn(ExperimentalPagingApi::class)
    override fun selectAlbumListByGenreId(genreId: String): Flow<PagingData<XyAlbum>> {
        return Pager(
            PagingConfig(
                pageSize = Constants.UI_LIST_PAGE,  // 每一页个数
                prefetchDistance = 2, // 距离下一页请求的距离
                initialLoadSize = Constants.UI_LIST_PAGE // 第一次加载数量，如果不设置的话是 pageSize * 2
            ),
            remoteMediator = PlexGenreAlbumListRemoteMediator(
                genreId = genreId,
                plexDatasourceServer = this,
                db = db,
                connectionId = connectionConfigServer.getConnectionId()
            )
        ) {
            db.albumDao.selectGenreAlbumListPage(genreId)
        }.flow
    }

    /**
     * 获得歌曲列表
     */
    override suspend fun getMusicList(
        pageSize: Int,
        pageNum: Int
    ): List<XyMusic>? {
        var selectMusicList: List<XyMusic>? =
            db.musicDao.selectHomeMusicList(pageSize, pageNum * pageSize)

        if (selectMusicList.isNullOrEmpty()) {
            val homeMusicList = getServerMusicList(
                pageSize = pageSize,
                startIndex = pageNum * pageSize
            )
            selectMusicList = homeMusicList.items

        }
        return selectMusicList
    }

    /**
     * 根据专辑获得歌曲列表
     */
    override suspend fun getMusicListByAlbumId(
        albumId: String,
        pageSize: Int,
        pageNum: Int
    ): List<XyMusic>? {
        var selectMusicList: List<XyMusic>? =
            db.musicDao.selectMusicListByAlbumId(albumId, pageSize, pageNum * pageSize)

        if (selectMusicList.isNullOrEmpty()) {
            val homeMusicList = getServerMusicList(
                pageSize = pageSize,
                startIndex = pageNum * pageSize,
                albumId = albumId
            )
            selectMusicList = homeMusicList.items
        }
        return selectMusicList
    }

    /**
     * 根据艺术家获得歌曲列表
     */
    override suspend fun getMusicListByArtistId(
        artistId: String,
        pageSize: Int,
        pageNum: Int
    ): List<XyMusic>? {
        var selectMusicList: List<XyMusic>? =
            db.musicDao.selectMusicListByArtistId(artistId, pageSize, pageNum * pageSize)
        if (selectMusicList.isNullOrEmpty()) {
            val homeMusicList = getServerMusicList(
                pageSize = pageSize,
                startIndex = pageNum * pageSize,
                artistId = artistId
            )
            selectMusicList = homeMusicList.items
        }
        return selectMusicList
    }

    /**
     * 获得收藏歌曲列表
     */
    override suspend fun getMusicListByFavorite(
        pageSize: Int,
        pageNum: Int
    ): List<XyMusic>? {
        var selectMusicList: List<XyMusic>? =
            db.musicDao.selectMusicListByFavorite(pageSize, pageNum * pageSize)
        if (selectMusicList.isNullOrEmpty()) {
            val homeMusicList = getServerMusicList(
                pageSize = pageSize,
                startIndex = pageNum * pageSize,
                ifFavorite = true
            )
            selectMusicList = homeMusicList.items
        }
        return selectMusicList
    }

    /**
     * 获得OkHttpClient
     */
    override fun getOkhttpClient(): OkHttpClient {
        return plexApiClient.apiOkHttpClient
    }

    /**
     * 设置token
     */
    override fun setToken() {
        TokenServer.setTokenData(plexApiClient.getToken())
        TokenServer.setHeaderMapData(plexApiClient.getHeadersMapData())
    }

    /**
     * 上报播放状态
     */
    override suspend fun reportPlaying(
        musicId: String,
        playSessionId: String,
        isPaused: Boolean,
        positionTicks: Long?
    ) {
        plexApiClient.userApi().playing(
            ratingKey = musicId,
            time = positionTicks ?: 0,
            state = if (isPaused) PlayState.PLAYING else PlayState.PAUSED
        )
    }

    /**
     * 上报播放进度
     */
    override suspend fun reportProgress(
        musicId: String,
        playSessionId: String,
        positionTicks: Long?
    ) {
        plexApiClient.userApi().playing(
            ratingKey = musicId,
            time = positionTicks ?: 0,
            state = PlayState.PLAYING
        )
    }

    /**
     * 获得播放连接
     */
    override suspend fun getMusicPlayUrl(musicId: String): String {
        return plexApiClient.createAudioUrl(musicId)
    }

    /**
     * 获取歌单列表
     */
    suspend fun getPlaylistsServer(startIndex: Int, pageSize: Int): AllResponse<XyAlbum> {
        return try {
            val playlists =
                plexApiClient.playlistsApi()
                    .getPlaylists(start = startIndex, pageSize = pageSize)
            val allResponse = AllResponse(
                items = playlists.mediaContainer?.let { response ->
                    response.metadata?.let {
                        convertToPlaylists(
                            it
                        )
                    }
                },
                totalRecordCount = playlists.mediaContainer?.metadata?.size ?: 0,
                startIndex = startIndex
            )
            allResponse
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "获取歌单失败", e)
            AllResponse(items = emptyList(), 0, 0)
        }
    }


    /**
     * 获取音乐列表
     * @param [pageSize] 页面大小
     * @param [startIndex] 页码
     * @param [albumId] 专辑 ID
     * @return [Response<ItemResponse>]
     */
    suspend fun getServerMusicList(
        plexListType: PlexListType = PlexListType.all,
        type: Int? = 10,
        startIndex: Int,
        pageSize: Int,
        search: String? = null,
        sortBy: PlexSortType? = PlexSortType.ARTIST_TITLE_SORT,
        sortOrder: PlexSortOrder? = PlexSortOrder.ASCENDING,
        artistId: String? = null,
        albumId: String? = null,
        ifFavorite: Boolean? = null,
        years: List<String>? = null,
        params: Map<String, String>? = null
    ): AllResponse<XyMusic> {
        val response =
            plexApiClient.itemApi().getSongs(
                sectionKey = connectionConfigServer.libraryId!!,
                type = type,
                selectType = plexListType.toString(),
                start = startIndex,
                pageSize = pageSize,
                sort = "$sortBy:$sortOrder",
                title = search,
                artistId = artistId,
                albumId = albumId,
                trackCollection = if (ifFavorite == true) plexApiClient.musicFavoriteCollectionIndex else null,
                year = years,
                params = params ?: mapOf(Pair("1", "1"))
            )
        return AllResponse(
            items = response.mediaContainer?.metadata?.let { convertToMusicList(it) },
            totalRecordCount = response.mediaContainer?.totalSize ?: 0,
            startIndex = startIndex
        )
    }


    /**
     * 获得专辑列表
     */
    suspend fun getAlbumList(
        plexListType: PlexListType = PlexListType.all,
        type: Int = 9,
        startIndex: Int,
        pageSize: Int,
        search: String? = null,
        sortBy: PlexSortType = PlexSortType.TITLE_SORT,
        sortOrder: PlexSortOrder = PlexSortOrder.ASCENDING,
        ifFavorite: Boolean? = null,
        artistId: String? = null,
        genreId: String? = null,
        albumYear: List<String>? = null,
        params: Map<String, String>? = null
    ): AllResponse<XyAlbum> {
        val albumResponse = plexApiClient.itemApi().getSongs(
            sectionKey = connectionConfigServer.libraryId!!,
            type = type,
            selectType = plexListType.toString(),
            start = startIndex,
            pageSize = pageSize,
            sort = "$sortBy:$sortOrder",
            title = search,
            artistId = artistId,
            genreId = genreId,
            albumCollection = if (ifFavorite == true) plexApiClient.albumFavoriteCollectionIndex else null,
            albumYear = albumYear,
            params = params ?: mapOf(Pair("1", "1"))
        )

        return AllResponse(
            items = albumResponse.mediaContainer?.metadata?.let {
                convertToAlbumList(
                    it
                )
            }, totalRecordCount = albumResponse.mediaContainer?.totalSize ?: 0,
            startIndex = startIndex
        )
    }


    /**
     * 获得流派列表
     * @param [pageSize] 限制
     * @param [startIndex] 启动索引
     * @param [search] 搜索
     * @param [sortBy] 排序方式
     * @param [sortOrder] 排序订单
     * @return [Response<ItemResponse>]
     */
    //todo getSongs这里返回值可能有问题可能有别的需要测试
    suspend fun getGenreList(
        type: Int = 9,
        startIndex: Int,
        pageSize: Int,
        search: String? = null,
        sortBy: PlexSortType = PlexSortType.TITLE_SORT,
        sortOrder: PlexSortOrder = PlexSortOrder.ASCENDING,
    ): AllResponse<XyGenre> {
        val genreResponse = plexApiClient.itemApi().getGenres(
            sectionKey = connectionConfigServer.libraryId!!,
            type = type,
            start = startIndex,
            pageSize = pageSize,
            sort = "$sortBy:$sortOrder",
            title = search,
        )
        return AllResponse(
            items = genreResponse.mediaContainer?.directory?.let { convertToGenreList(it) },
            totalRecordCount = genreResponse.mediaContainer?.totalSize ?: 0,
            startIndex = startIndex
        )
    }


    suspend fun getFavoriteMusicList(
        startIndex: Int,
        pageSize: Int,
    ): AllResponse<XyMusic> {

        val musicCollection = plexApiClient.userLibraryApi()
            .getCollection(
                connectionConfigServer.libraryId!!,
                title = Constants.PLEX_MUSIC_COLLECTION_TITLE
            )

        val collectionId =
            musicCollection.mediaContainer?.metadata?.get(0)?.ratingKey

        return if (collectionId.isNullOrBlank()) {

            AllResponse(
                items = null,
                totalRecordCount = 0,
                startIndex = startIndex
            )
        } else {
            val favoriteSongs = plexApiClient.userLibraryApi().getFavoriteSongs(
                sectionKey = collectionId,
                start = startIndex,
                pageSize = pageSize,
            )
            AllResponse(
                items = favoriteSongs.mediaContainer?.metadata?.let { convertToMusicList(it) },
                totalRecordCount = favoriteSongs.mediaContainer?.totalSize ?: 0,
                startIndex = startIndex
            )
        }
    }


    /**
     * 获得专辑或歌单的音乐列表
     */
    suspend fun getMusicListByAlbumOrPlaylist(
        startIndex: Int,
        pageSize: Int,
        itemId: String,
        dataType: MusicDataTypeEnum,
        ifFavorite: Boolean? = null,
        sortBy: PlexSortType? = PlexSortType.ARTIST_TITLE_SORT,
        sortOrder: PlexSortOrder? = PlexSortOrder.ASCENDING,
        params: Map<String, String>? = null,
        years: List<String>? = null,
        artistId: String? = null
    ): AllResponse<XyMusic> {
        if (dataType == MusicDataTypeEnum.ALBUM) {
            //存储歌曲数据
            return getServerMusicList(
                startIndex = startIndex,
                pageSize = pageSize,
                albumId = itemId,
                ifFavorite = ifFavorite,
                sortBy = sortBy,
                sortOrder = sortOrder,
                params = params,
                artistId = artistId,
                years = years
            )
        } else {
            //存储歌曲数据
            val playlistMusicList =
                plexApiClient.playlistsApi().getPlaylistMusicList(
                    playlistId = itemId,
                    start = startIndex,
                    pageSize = pageSize,
                    sort = "$sortBy:$sortOrder",
                    trackCollection = if (ifFavorite == true) plexApiClient.musicFavoriteCollectionIndex else null,
                    params = params ?: mapOf(Pair("1", "1")),
                    year = years,
                    artistId = artistId
                )

            return AllResponse(
                items = playlistMusicList.mediaContainer?.metadata?.let { convertToMusicList(it) },
                totalRecordCount = playlistMusicList.mediaContainer?.totalSize ?: 0,
                startIndex = startIndex
            )
        }
    }


    /**
     * 搜索专辑
     * @param [search] 搜索内容
     */
    suspend fun searchAlbumFlowList(search: String): List<XyAlbum> {
        return getAlbumList(
            pageSize = Constants.UI_LIST_PAGE, startIndex = 0, search = search
        ).items ?: emptyList()
    }

    /**
     * 搜索音乐
     * @param [search] 搜索内容
     */
    suspend fun searchMusicFlowList(search: String): List<XyMusic> {
        return getServerMusicList(
            pageSize = ALBUM_MUSIC_LIST_PAGE_SIZE, startIndex = 0, search = search
        ).items ?: emptyList()
    }

    /**
     * 搜索艺术家
     * @param [searchQuery] 搜索查询
     * @return [Flow<PagingData<ArtistItem>>?]
     */
    suspend fun searchArtistFlowList(searchQuery: String): List<XyArtist> {
        val response =
            getArtistList(startIndex = 0, pageSize = Constants.UI_LIST_PAGE, search = searchQuery)
        return response.items ?: emptyList()
    }

    /**
     * 将ItemResponse转换成XyAlbum
     */
    fun convertToAlbumList(item: List<Metadatum>): List<XyAlbum> {
        return item.map { album ->
            convertToAlbum(album)
        }
    }

    /**
     * 将ItemResponse转换成XyAlbum
     */
    fun convertToAlbum(album: Metadatum): XyAlbum {
        val itemImageUrl =
            album.image?.let { images ->
                val image = images.findLast { it.type == ImageType.CoverPoster }
                image?.let {
                    plexApiClient.getImageUrl(
                        image.url,
                    )
                }
            }

        return XyAlbum(
            itemId = album.ratingKey,
            pic = itemImageUrl,
            name = album.title,
            connectionId = connectionConfigServer.getConnectionId(),
            artistIds = album.parentRatingKey.toString(),
            artists = album.parentTitle.toString(),
            year = album.year,
            premiereDate = album.originallyAvailableAt?.atStartOfDay(ZoneOffset.ofHours(8))
                ?.toInstant()
                ?.toEpochMilli(),
            genreIds = album.genre?.joinToString(Constants.ARTIST_DELIMITER) { it.tag },
            ifFavorite = album.collection?.any { it.tag == Constants.PLEX_ALBUM_COLLECTION_TITLE } == true,
            ifPlaylist = false,
            createTime = album.addedAt,
            musicCount = album.childCount ?: 0
        )
    }


    /**
     * 将PlaylistItemData3转换成XyAlbum
     */
    fun convertToPlaylists(playlists: List<PlaylistMetadatum>): List<XyAlbum> {
        return playlists.map { playlist ->
            convertToPlaylist(playlist)
        }
    }

    /**
     * 将PlaylistItemData转换成XyAlbum
     */
    fun convertToPlaylist(playlist: PlaylistMetadatum): XyAlbum {
        return XyAlbum(
            itemId = playlist.ratingKey,
            pic = playlist.composite?.let { plexApiClient.getImageUrl(it) },
            name = playlist.title ?: "未知歌单",
            connectionId = connectionConfigServer.getConnectionId(),
            ifFavorite = false,
            ifPlaylist = true,
            musicCount = playlist.leafCount ?: 0
        )
    }


    /**
     * 将ItemResponse换成XyMusic
     */
    suspend fun convertToMusicList(item: List<Metadatum>): List<XyMusic> {
        return item.map { music ->
            convertToMusic(
                music
            )
        }
    }

    //ItemResponse转换XyMusic
    suspend fun convertToMusic(item: Metadatum): XyMusic {
        val itemImageUrl = item.image?.let { images ->
            val image = images.findLast { it.type == ImageType.CoverPoster }
            image?.let {
                plexApiClient.getImageUrl(
                    image.url,
                )
            }

        }


        //获得音乐信息
        val media = item.media?.get(0)
        val part = media?.part?.get(0)
        val mediaSourceInfo =
            part?.stream?.find { it.streamType == 2L }

        val mediaStreamLyric =
            part?.stream?.find { it.streamType == 4L }


        val audioUrl = getMusicPlayUrl(part?.key.toString())

        return XyMusic(
            itemId = item.ratingKey,
            pic = itemImageUrl,
            name = item.title,
            musicUrl = audioUrl,
            album = item.parentRatingKey.toString(),
            albumName = item.parentTitle,
            connectionId = connectionConfigServer.getConnectionId(),
            artists = item.grandparentTitle,
            artistIds = item.grandparentRatingKey,
            albumArtist = item.grandparentTitle ?: Constants.UNKNOWN_ARTIST,
            albumArtistIds = item.grandparentRatingKey,
            createTime = item.addedAt,
            year = item.parentYear,
            playedCount = 0,
            ifFavoriteStatus = item.collection?.any { it.tag == Constants.PLEX_MUSIC_COLLECTION_TITLE } == true,
            ifLyric = mediaStreamLyric != null,
            lyric = mediaStreamLyric?.id.toString(),
            path = part?.file,
            bitRate = media?.bitrate ?: 0,
            sampleRate = mediaSourceInfo?.samplingRate,
            bitDepth = mediaSourceInfo?.bitDepth,
            size = part?.size,
            runTimeTicks = part?.duration,
            container = part?.container,
            codec = media?.audioCodec,
            genreIds = "",
            playlistItemId = item.playlistItemID
        )
    }


    /**
     * 根据BaseItemDto组装艺术家名单
     * @param [items] 项目
     * @return [List<ArtistItem>]
     */
    fun convertToArtistList(items: List<Metadatum>): List<XyArtist> {
        val xyArtists = items.mapIndexed { index, item ->
            convertToArtist(item, index)
        }
        return xyArtists
    }

    /**
     * 将ArtistID3转换成XyArtist
     */
    fun convertToArtist(
        artist: Metadatum,
        indexNumber: Int,
    ): XyArtist {
        val artistImageUrl =
            artist.image?.let { images ->
                val image = images.findLast { it.type == ImageType.CoverPoster }
                image?.let {
                    plexApiClient.getImageUrl(
                        image.url,
                    )
                }

            }

        val transliterator = Transliterator.getInstance("Han-Latin")
        val result =
            if (artist.title.isBlank()) null else transliterator.transliterate(
                artist.title
            )
        val shortNameStart = if (!result.isNullOrBlank()) result[0] else '#'
        val selectChat =
            if (!CharUtils.isEnglishLetter(shortNameStart)) "#" else shortNameStart.toString()
                .lowercase()

        return XyArtist(
            artistId = artist.ratingKey,
            name = artist.title,
            pic = artistImageUrl,
            connectionId = connectionConfigServer.getConnectionId(),
            describe = artist.summary,
            selectChat = selectChat,
            ifFavorite = artist.collection?.any { it.tag == Constants.PLEX_ARTIST_COLLECTION_TITLE } == true,
            indexNumber = indexNumber,
        )
    }


    /**
     * 将Genre转换成XyGenre
     */
    fun convertToGenreList(genres: List<Directory>): List<XyGenre> {
        return genres.map {
            convertToGenre(it)
        }
    }

    /**
     * 将Genre转换成XyGenre
     */
    fun convertToGenre(genre: Directory): XyGenre {
        return XyGenre(
            itemId = genre.key,
            pic = "",
            name = genre.title,
            connectionId = connectionConfigServer.getConnectionId()
        )
    }


}