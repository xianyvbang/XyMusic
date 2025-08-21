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
import cn.xybbz.api.client.plex.data.Metadatum
import cn.xybbz.api.client.plex.data.PlaylistMetadatum
import cn.xybbz.api.client.plex.data.PlexLibraryItemResponse
import cn.xybbz.api.client.plex.data.PlexResponse
import cn.xybbz.api.client.plex.data.toPlexLogin
import cn.xybbz.api.enums.plex.ImageType
import cn.xybbz.api.enums.plex.MetadatumType
import cn.xybbz.api.enums.plex.PlexListType
import cn.xybbz.api.enums.plex.PlexSortOrder
import cn.xybbz.api.enums.plex.PlexSortType
import cn.xybbz.api.exception.ConnectionException
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.constants.Constants.ALBUM_MUSIC_LIST_PAGE_SIZE
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.common.enums.SortTypeEnum
import cn.xybbz.common.utils.CharUtils
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
import cn.xybbz.localdata.enums.DataSourceType
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import cn.xybbz.page.all.ArtistRemoteMediator
import cn.xybbz.page.plex.PlexAlbumOrPlaylistMusicListRemoteMediator
import cn.xybbz.page.plex.PlexAlbumRemoteMediator
import cn.xybbz.page.plex.PlexFavoriteMusicRemoteMediator
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

    private var musicFavoriteCollectionId: String? = null
    private var albumFavoriteCollectionId: String? = null
    private var artistFavoriteCollectionId: String? = null

    //服务器信息
    private var machineIdentifier: String? = null

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
        //todo 在这里调用postPingSystem 因为plex的ping服务器需要token
        //todo login改成只ping
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
            //获得machineIdentifier
            machineIdentifier = postPingSystem.mediaContainer?.machineIdentifier
        } catch (e: Exception) {
            Log.i(Constants.LOG_ERROR_PREFIX, "ping服务器失败", e)
            return false
        }

        return true
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
            plexApiClient.itemApi().getArtistInfo(
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
            type = null,
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
                if (musicFavoriteCollectionId == null) {
                    //查询合集
                    val musicCollection = plexApiClient.userLibraryApi()
                        .getCollection(title = Constants.PLEX_MUSIC_COLLECTION_TITLE)
                    if (musicCollection.mediaContainer?.metadata.isNullOrEmpty()) {
                        val collectionResponse = plexApiClient.userLibraryApi().addCollection(
                            sectionId = connectionConfigServer.libraryId!!
                        )
                        musicFavoriteCollectionId =
                            collectionResponse.mediaContainer?.metadata?.get(0)?.index
                    } else {
                        musicFavoriteCollectionId =
                            musicCollection.mediaContainer?.metadata?.get(0)?.index
                    }
                    if (musicFavoriteCollectionId == null) {
                        return false
                    }
                }
                collectionId = musicFavoriteCollectionId
            }

            MusicTypeEnum.ALBUM -> {
                if (albumFavoriteCollectionId == null) {
                    //查询合集
                    val albumCollection = plexApiClient.userLibraryApi()
                        .getCollection(subtype = 9, title = Constants.PLEX_ALBUM_COLLECTION_TITLE)
                    if (albumCollection.mediaContainer?.metadata.isNullOrEmpty()) {
                        val collectionResponse = plexApiClient.userLibraryApi().addCollection(
                            sectionId = connectionConfigServer.libraryId!!
                        )
                        albumFavoriteCollectionId =
                            collectionResponse.mediaContainer?.metadata?.get(0)?.index
                    } else {
                        albumFavoriteCollectionId =
                            albumCollection.mediaContainer?.metadata?.get(0)?.index
                    }
                    if (albumFavoriteCollectionId == null) {
                        return false
                    }
                }
                collectionId = albumFavoriteCollectionId
            }

            MusicTypeEnum.ARTIST -> {
                if (artistFavoriteCollectionId == null) {
                    //查询合集
                    val albumCollection = plexApiClient.userLibraryApi()
                        .getCollection(subtype = 9, title = Constants.PLEX_ARTIST_COLLECTION_TITLE)
                    if (albumCollection.mediaContainer?.metadata.isNullOrEmpty()) {
                        val collectionResponse = plexApiClient.userLibraryApi().addCollection(
                            sectionId = connectionConfigServer.libraryId!!
                        )
                        artistFavoriteCollectionId =
                            collectionResponse.mediaContainer?.metadata?.get(0)?.index
                    } else {
                        artistFavoriteCollectionId =
                            albumCollection.mediaContainer?.metadata?.get(0)?.index
                    }
                    if (artistFavoriteCollectionId == null) {
                        return false
                    }
                }

                collectionId = artistFavoriteCollectionId
            }
        }

        if (collectionId == null) {
            return false
        }

        plexApiClient.userLibraryApi()
            .markFavoriteItem(
                collectionId = collectionId,
                uri = "server://${machineIdentifier}/com.plexapp.plugins.library/library/metadata/${itemId}"
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
                if (musicFavoriteCollectionId == null) {
                    //查询合集
                    val musicCollection = plexApiClient.userLibraryApi()
                        .getCollection(title = Constants.PLEX_MUSIC_COLLECTION_TITLE)
                    musicFavoriteCollectionId =
                        musicCollection.mediaContainer?.metadata?.get(0)?.index
                    if (musicFavoriteCollectionId == null) {
                        return true
                    }
                }
                collectionId = musicFavoriteCollectionId
            }

            MusicTypeEnum.ALBUM -> {
                if (albumFavoriteCollectionId == null) {
                    //查询合集
                    val albumCollection = plexApiClient.userLibraryApi()
                        .getCollection(subtype = 9, title = Constants.PLEX_ALBUM_COLLECTION_TITLE)
                    albumFavoriteCollectionId =
                        albumCollection.mediaContainer?.metadata?.get(0)?.index
                    if (albumFavoriteCollectionId == null) {
                        return true
                    }
                }
                collectionId = albumFavoriteCollectionId
            }

            MusicTypeEnum.ARTIST -> {
                if (artistFavoriteCollectionId == null) {
                    //查询合集
                    val albumCollection = plexApiClient.userLibraryApi()
                        .getCollection(subtype = 9, title = Constants.PLEX_ARTIST_COLLECTION_TITLE)
                    artistFavoriteCollectionId =
                        albumCollection.mediaContainer?.metadata?.get(0)?.index
                    if (artistFavoriteCollectionId == null) {
                        return true
                    }
                }

                collectionId = artistFavoriteCollectionId
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
                    ).mediaContainer?.totalSize
                } catch (e: Exception) {
                    Log.e(Constants.LOG_ERROR_PREFIX, "加载流派数量报错", e)
                    null
                }
            }

            val favorite = async {
                favorite = try {
                    getFavoriteMusicList(
                        startIndex = 0, pageSize = 0,
                    ).mediaContainer?.totalSize
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
        TODO("Not yet implemented")
    }

    /**
     * 批量删除数据
     * 按 ID 删除
     * @param [musicIds] 需要删除数据的
     * @return [Boolean?]
     */
    override suspend fun removeByIds(musicIds: List<String>): Boolean {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }

    /**
     * 按 ID 选择音乐信息
     * @param [itemId] 音乐唯一标识
     * @return [XyMusic?]
     */
    override suspend fun selectMusicInfoById(itemId: String): XyMusic? {
        TODO("Not yet implemented")
    }

    /**
     * 根据音乐获得歌词信息
     * @param [music] 音乐id
     * @return 返回歌词列表
     */
    override suspend fun getMusicLyricList(music: XyMusic): List<LrcEntry>? {
        TODO("Not yet implemented")
    }

    /**
     * 根据艺术家获得专辑列表
     */
    override fun selectAlbumListByArtistId(artistId: String): Flow<PagingData<XyAlbum>> {
        TODO("Not yet implemented")
    }

    /**
     * 根据艺术家获得音乐列表
     */
    override fun selectMusicListByArtistId(artistId: String): Flow<PagingData<XyMusic>> {
        TODO("Not yet implemented")
    }

    /**
     * 获得随机音乐
     */
    override suspend fun getRandomMusicList(
        pageSize: Int,
        pageNum: Int
    ): List<XyMusic>? {
        TODO("Not yet implemented")
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
     * 新增或修改歌单
     */
    override suspend fun importPlaylist(playlistData: ExportPlaylistData): Boolean {
        TODO("Not yet implemented")
    }

    /**
     * 编辑歌单名称
     * @param [id] ID
     * @param [name] 姓名
     */
    override suspend fun editPlaylistName(id: String, name: String): Boolean {
        TODO("Not yet implemented")
    }

    /**
     * 删除歌单
     * @param [id] ID
     */
    override suspend fun removePlaylist(id: String): Boolean {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }

    /**
     * 根据id获得艺术家信息
     * @param [artistId] 艺术家id
     * @return [List<ArtistItem>?] 艺术家信息
     */
    override suspend fun selectArtistInfoById(artistId: String): XyArtist? {
        TODO("Not yet implemented")
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
                db.albumDao.removeByType(MusicDataTypeEnum.NEWEST)
                saveBatchAlbum(
                    albums, MusicDataTypeEnum.NEWEST
                )

            }
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
    override suspend fun selectGenresPage(): Flow<PagingData<XyGenre>> {
        TODO("Not yet implemented")
    }

    /**
     * 获得流派详情
     */
    override suspend fun getGenreById(genreId: String): XyGenre? {
        TODO("Not yet implemented")
    }

    /**
     * 获得流派内音乐列表/或者专辑
     * todo 试一下能不能查询到专辑
     * @param [genreId] 流派id
     */
    override fun selectAlbumListByGenreId(genreId: String): Flow<PagingData<XyAlbum>> {
        TODO("Not yet implemented")
    }

    /**
     * 获得歌曲列表
     */
    override suspend fun getMusicList(
        pageSize: Int,
        pageNum: Int
    ): List<XyMusic>? {
        TODO("Not yet implemented")
    }

    /**
     * 根据专辑获得歌曲列表
     */
    override suspend fun getMusicListByAlbumId(
        albumId: String,
        pageSize: Int,
        pageNum: Int
    ): List<XyMusic>? {
        TODO("Not yet implemented")
    }

    /**
     * 根据艺术家获得歌曲列表
     */
    override suspend fun getMusicListByArtistId(
        artistId: String,
        pageSize: Int,
        pageNum: Int
    ): List<XyMusic>? {
        TODO("Not yet implemented")
    }

    /**
     * 获得收藏歌曲列表
     */
    override suspend fun getMusicListByFavorite(
        pageSize: Int,
        pageNum: Int
    ): List<XyMusic>? {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }

    /**
     * 上报播放进度
     */
    override suspend fun reportProgress(
        musicId: String,
        playSessionId: String,
        positionTicks: Long?
    ) {
        TODO("Not yet implemented")
    }

    /**
     * 获得播放连接
     */
    override suspend fun getMusicPlayUrl(musicId: String): String {
        //todo 这里要接收key地址
        return plexApiClient.createAudioUrl(musicId)

    }

    /**
     * 获取歌单列表
     */
    suspend fun getPlaylistsServer(startIndex: Int, pageSize: Int): AllResponse<XyAlbum> {
        return try {
            val playlists =
                plexApiClient.playlistsApi()
                    .getPlaylists()
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
     * 首页音乐数据接口
     * 获取音乐列表
     * @param [pageSize] 页面大小
     * @param [startIndex] 页码
     * @param [albumId] 专辑 ID
     * @return [Response<ItemResponse>]
     */
    /*suspend fun getServerMusicList(
        startIndex: Int,
        pageSize: Int,
        albumId: String? = null,
        artistId: String? = null,
        genreId: String? = null,
        isFavorite: Boolean? = null,
        search: String? = null,
        sortBy: SortType = SortType.TITLE,
        sortOrder: PlexSortOrder = PlexSortOrder.ASCENDING
    ): AllResponse<XyMusic> {
        val response =
            plexApiClient.itemApi().getSongs(
                start = startIndex,
                pageSize = pageSize,
                order = sortOrder,
                sort = sortBy,
                title = search,
                starred = isFavorite,
                genreId = genreId,
                albumId = albumId,
                artistId = artistId
            )
        return AllResponse(
            items = response.data?.let { convertToMusicList(it, false) },
            totalRecordCount = response.totalCount ?: 0,
            startIndex = startIndex
        )
    }*/

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
                trackCollection = if (ifFavorite == true) musicFavoriteCollectionId else null,
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
            albumCollection = if (ifFavorite == true) albumFavoriteCollectionId else null,
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
     * @param [filters] 过滤器
     * @param [search] 搜索
     * @param [sortBy] 排序方式
     * @param [sortOrder] 排序订单
     * @return [Response<ItemResponse>]
     */
    suspend fun getGenreList(
        plexListType: PlexListType = PlexListType.genre,
        type: Int = 8,
        startIndex: Int,
        pageSize: Int,
        search: String? = null,
        sortBy: PlexSortType = PlexSortType.TITLE_SORT,
        sortOrder: PlexSortOrder = PlexSortOrder.ASCENDING,
        params: Map<String, String>? = null
    ): PlexResponse<PlexLibraryItemResponse> {
        val genreResponse = plexApiClient.itemApi().getSongs(
            sectionKey = connectionConfigServer.libraryId!!,
            type = type,
            selectType = plexListType.toString(),
            start = startIndex,
            pageSize = pageSize,
            sort = "$sortBy:$sortOrder",
            title = search,
            params = params ?: mapOf(Pair("1", "1"))
        )

        return genreResponse
    }


    suspend fun getFavoriteMusicList(
        plexListType: PlexListType = PlexListType.collections,
        startIndex: Int,
        pageSize: Int,
        params: Map<String, String>? = null
    ): PlexResponse<PlexLibraryItemResponse> {

        val favoriteCollections = plexApiClient.itemApi().getSongs(
            sectionKey = connectionConfigServer.libraryId!!,
            selectType = plexListType.toString(),
            start = 0,
            pageSize = 1000,
            params = params ?: mapOf(Pair("1", "1"))
        )

        val collectionIds =
            favoriteCollections.mediaContainer?.metadata?.find { it.subtype == MetadatumType.Track.value }?.ratingKey

        val response =
            plexApiClient.itemApi().getFavoriteSongs(
                sectionKey = collectionIds!!,
                start = startIndex,
                pageSize = pageSize,
            )
        return response
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
                artistId = artistId
            )
        } else {
            //存储歌曲数据
            val playlistMusicList =
                plexApiClient.playlistsApi().getPlaylistMusicList(
                    playlistId = itemId,
                    start = startIndex,
                    pageSize = pageSize,
                    sort = "$sortBy:$sortOrder",
                    trackCollection = if (ifFavorite == true) musicFavoriteCollectionId else null,
                    params = params ?: mapOf(Pair("1", "1")),
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
            artists = album.parentRatingKey.toString(),
            year = album.year,
            premiereDate = album.originallyAvailableAt?.atStartOfDay(ZoneOffset.ofHours(8))
                ?.toInstant()
                ?.toEpochMilli(),
            genreIds = album.genre?.joinToString(Constants.ARTIST_DELIMITER) { it.tag },
            ifFavorite = album.collection?.any { it.tag == "收藏" } == true,
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
        val part = item.media?.get(0)?.part?.get(0)
        val mediaSourceInfo =
            part?.stream?.find { it.streamType == 2L }

        val mediaStreamLyric =
            part?.stream?.find { it.streamType == 4L }


        val audioUrl = getMusicPlayUrl(item.ratingKey)

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
            ifFavoriteStatus = item.collection?.any { it.tag == "收藏" } == true,
            ifLyric = mediaStreamLyric != null,
            path = part?.file,
            bitRate = mediaSourceInfo?.bitrate ?: 0,
            sampleRate = mediaSourceInfo?.samplingRate,
            bitDepth = mediaSourceInfo?.bitDepth,
            size = part?.size,
            runTimeTicks = part?.duration,
            container = part?.container,
            codec = mediaSourceInfo?.codec,
            lyric = "",
            genreIds = ""
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
            ifFavorite = artist.collection?.any { it.tag == "收藏" } == true,
            indexNumber = indexNumber
        )
    }

}