package cn.xybbz.api.client.jellyfin

import android.content.Context
import android.icu.math.BigDecimal
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
import cn.xybbz.api.client.jellyfin.data.CreatePlaylistRequest
import cn.xybbz.api.client.jellyfin.data.ItemRequest
import cn.xybbz.api.client.jellyfin.data.ItemResponse
import cn.xybbz.api.client.jellyfin.data.PlaybackStartInfo
import cn.xybbz.api.client.jellyfin.data.PlaylistUserPermissions
import cn.xybbz.api.client.jellyfin.data.Response
import cn.xybbz.api.client.jellyfin.data.ViewRequest
import cn.xybbz.api.client.jellyfin.data.toAllResponse
import cn.xybbz.api.client.jellyfin.data.toLogin
import cn.xybbz.api.enums.jellyfin.BaseItemKind
import cn.xybbz.api.enums.jellyfin.CollectionType
import cn.xybbz.api.enums.jellyfin.ImageType
import cn.xybbz.api.enums.jellyfin.ItemFields
import cn.xybbz.api.enums.jellyfin.ItemFilter
import cn.xybbz.api.enums.jellyfin.ItemSortBy
import cn.xybbz.api.enums.jellyfin.MediaProtocol
import cn.xybbz.api.enums.jellyfin.MediaStreamType
import cn.xybbz.api.enums.jellyfin.MediaType
import cn.xybbz.api.enums.jellyfin.PlayMethod
import cn.xybbz.api.enums.jellyfin.SortOrder
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.constants.Constants.ALBUM_MUSIC_LIST_PAGE_SIZE
import cn.xybbz.common.constants.Constants.LYRICS_AMPLIFICATION
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.common.enums.SortTypeEnum
import cn.xybbz.common.utils.CharUtils
import cn.xybbz.config.ConnectionConfigServer
import cn.xybbz.entity.api.LoginSuccessData
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
import cn.xybbz.page.jellyfin.JellyfinAlbumOrPlaylistMusicListRemoteMediator
import cn.xybbz.page.jellyfin.JellyfinAlbumRemoteMediator
import cn.xybbz.page.jellyfin.JellyfinArtistAlbumListRemoteMediator
import cn.xybbz.page.jellyfin.JellyfinFavoriteMusicRemoteMediator
import cn.xybbz.page.jellyfin.JellyfinGenreAlbumListRemoteMediator
import cn.xybbz.page.jellyfin.JellyfinGenresRemoteMediator
import cn.xybbz.page.jellyfin.JellyfinMusicRemoteMediator
import cn.xybbz.ui.components.LrcEntry
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.supervisorScope
import okhttp3.OkHttpClient
import java.net.SocketTimeoutException
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Jellyfin api客户端管理
 * @author xybbz
 * @date 2024/06/12
 */
class JellyfinDatasourceServer(
    private val db: DatabaseClient,
    private val application: Context,
    private val connectionConfigServer: ConnectionConfigServer,
    private val jellyfinApiClient: JellyfinApiClient
) : IDataSourceParentServer(
    db,
    connectionConfigServer,
    application
) {


    /**
     * 登录功能
     */
    override suspend fun login(clientLoginInfoReq: ClientLoginInfoReq): LoginSuccessData {
        val responseData =
            jellyfinApiClient.userApi().authenticateByName(clientLoginInfoReq.toLogin())
        Log.i("=====", "返回响应值: $responseData")
        jellyfinApiClient.updateAccessToken(responseData.accessToken)
        setToken()
        val systemInfo = jellyfinApiClient.userApi().getSystemInfo()
        Log.i("=====", "服务器信息 $systemInfo")
        return LoginSuccessData(
            userId = responseData.user?.id,
            accessToken = responseData.accessToken,
            serverId = responseData.serverId,
            serverName = systemInfo.serverName,
            version = systemInfo.version
        )
    }

    /**
     * 连通性检测
     */
    override suspend fun postPingSystem(): Boolean {
        return try {
            val pingData = jellyfinApiClient.userApi().postPingSystem()
            Log.i("=====", "ping数据返回: $pingData")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 获得当前数据源类型
     */
    override fun getDataSourceType(): DataSourceType {
        return DataSourceType.JELLYFIN
    }

    /**
     * 根据选择的服务端信息生成apiClient
     */
    override suspend fun createApiClient(
        address: String,
        deviceId: String,
        username: String,
        password: String
    ) {
        val deviceName = "${Build.BRAND} ${Build.MODEL}"
        val packageManager = application.packageManager
        val packageName = application.packageName
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
        val appName = packageManager.getApplicationLabel(applicationInfo).toString()
        val versionName = packageInfo.versionName
        val versionCode = packageInfo.longVersionCode
        jellyfinApiClient.createApiClient(
            appName, "${versionName}.${versionCode}", deviceId, deviceName
        )
        //提前写入没有sessionToken的Authenticate请求头,不然登录请求都会报错
        setToken()
        jellyfinApiClient.setRetrofitData(address)
    }

    /**
     * 获得专辑列表数据
     */
    @OptIn(ExperimentalPagingApi::class)
    override fun selectAlbumFlowList(
        sortType: SortTypeEnum?,
        ifFavorite: Boolean?,
        years: String?,
    ): Flow<PagingData<XyAlbum>> {
        return Pager(
            config = PagingConfig(
                pageSize = Constants.UI_LIST_PAGE,  // 每一页个数
                initialLoadSize = Constants.UI_INIT_LIST_PAGE,
                prefetchDistance = Constants.UI_PREFETCH_DISTANCE,
            ), remoteMediator = JellyfinAlbumRemoteMediator(
                sortType = sortType,
                ifFavorite = ifFavorite,
                years = years,
                dataSource = getDataSourceType(),
                db = db,
                jellyfinDatasourceServer = this,
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
        sortType: SortTypeEnum?, ifFavorite: Boolean?, years: String?
    ): Flow<PagingData<XyMusic>> {
        return Pager(
            config = PagingConfig(
                pageSize = Constants.UI_LIST_PAGE,  // 每一页个数
                initialLoadSize = Constants.UI_INIT_LIST_PAGE,
                prefetchDistance = Constants.UI_PREFETCH_DISTANCE,
            ), remoteMediator = JellyfinMusicRemoteMediator(
                sortType = sortType,
                db = db,
                jellyfinDatasourceServer = this,
                ifFavorite = ifFavorite,
                years = years,
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
        ifFavorite: Boolean?, selectChat: String?
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
     * 搜索专辑
     * @param [search] 搜索内容
     */
    suspend fun searchAlbumFlowList(search: String): List<XyAlbum> {
        return getAlbumList(
            pageSize = Constants.UI_LIST_PAGE, startIndex = 0, search = search
        ).items
    }

    /**
     * 搜索音乐
     * @param [search] 搜索内容
     */
    suspend fun searchMusicFlowList(search: String): List<XyMusic> {
        return getServerMusicList(
            pageSize = ALBUM_MUSIC_LIST_PAGE_SIZE, startIndex = 0, search = search
        ).items
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
     * 获得专辑内音乐列表
     * @param [itemId] 专辑id
     */
    @OptIn(ExperimentalPagingApi::class)
    override fun selectMusicListByParentId(
        sortType: SortTypeEnum?,
        ifFavorite: Boolean?,
        years: String?,
        itemId: String,
        dataType: MusicDataTypeEnum,
    ): Flow<PagingData<XyMusic>> {
        return Pager(
            config = PagingConfig(
                pageSize = ALBUM_MUSIC_LIST_PAGE_SIZE,  // 每一页个数
                initialLoadSize = Constants.UI_INIT_LIST_PAGE,
                prefetchDistance = Constants.UI_PREFETCH_DISTANCE,
            ), remoteMediator = JellyfinAlbumOrPlaylistMusicListRemoteMediator(
                itemId = itemId,
                sortType = sortType,
                ifFavorite = ifFavorite,
                years = years,
                jellyfinDatasourceServer = this,
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
    override suspend fun markFavoriteItem(itemId: String, dataType: MusicTypeEnum): Boolean {
        val favorite = jellyfinApiClient.userLibraryApi().markFavoriteItem(itemId).isFavorite
        db.musicDao.updateFavoriteByItemId(
            favorite,
            itemId,
            connectionConfigServer.getConnectionId()
        )
        return favorite
    }

    /**
     * 取消项目收藏
     * @param [itemId] 专辑/音乐id
     */
    override suspend fun unmarkFavoriteItem(itemId: String, dataType: MusicTypeEnum): Boolean {
        val favorite = jellyfinApiClient.userLibraryApi().unmarkFavoriteItem(itemId).isFavorite
        db.musicDao.updateFavoriteByItemId(
            favorite,
            itemId,
            connectionConfigServer.getConnectionId()
        )
        return favorite
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
                    getServerMusicList(pageSize = 0, startIndex = 0).totalRecordCount
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
                    val response = getGenreList(
                        startIndex = 0,
                        pageSize = 0
                    )
                    response.totalRecordCount
                } catch (e: Exception) {
                    Log.e(Constants.LOG_ERROR_PREFIX, "加载流派数量报错", e)
                    null
                }
            }

            val favorite = async {
                favorite = try {
                    val response = getServerMusicList(
                        pageSize = 0, startIndex = 0, isFavorite = true
                    )
                    response.totalRecordCount
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
        jellyfinApiClient.libraryApi().deleteItem(itemId = musicId)
        return true
    }

    override suspend fun removeByIds(musicIds: List<String>): Boolean {
        jellyfinApiClient.libraryApi()
            .deleteItems(ids = musicIds.joinToString(Constants.ARTIST_DELIMITER) { it })
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
            val queryResult = jellyfinApiClient.userLibraryApi().getItem(albumId)
            artistExtend = convertToAlbum(queryResult)
        }
        return artistExtend
    }

    /**
     * 按 ID 选择音乐信息
     * @param [itemId] 音乐唯一标识
     * @return [MusicArtistExtend?]
     */
    override suspend fun selectMusicInfoById(itemId: String): XyMusic? {
        return db.musicDao.selectById(itemId)
    }

    /**
     * 根据音乐获得歌词信息
     * @param [music] 音乐id
     * @return 返回歌词列表
     */
    override suspend fun getMusicLyricList(music: XyMusic): List<LrcEntry>? {
        return if (music.ifLyric) {
            val lyrics = jellyfinApiClient.lyricsApi().getLyrics(music.itemId)
            lyrics.lyrics.map {
                LrcEntry(startTime = it.start!! / LYRICS_AMPLIFICATION, text = it.text)
            }
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
                initialLoadSize = Constants.UI_INIT_LIST_PAGE,
                prefetchDistance = Constants.UI_PREFETCH_DISTANCE,
            ), remoteMediator = JellyfinArtistAlbumListRemoteMediator(
                artistId = artistId,
                jellyfinDatasourceServer = this,
                db = db,
                connectionId = connectionConfigServer.getConnectionId()
            )
        ) {
            db.albumDao.selectArtistAlbumListPage(
                artistId,
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
                initialLoadSize = Constants.UI_INIT_LIST_PAGE,
                prefetchDistance = Constants.UI_PREFETCH_DISTANCE,
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
     * 根据艺术家获得音乐列表
     */
    override suspend fun selectMusicListByArtistServer(
        artistId: String,
        pageSize: Int,
        startIndex: Int
    ): AllResponse<XyMusic> {
        val response =
            getServerMusicList(
                pageSize = pageSize,
                startIndex = startIndex,
                artistId = artistId
            )
        return response.toAllResponse(startIndex)
    }

    /**
     * 获得音乐数据
     * @param [pageSize] 分页大小
     * @param [pageNum] 分页页码
     * @return [XyMusic] 返回数据
     */
    override suspend fun getMusicList(
        pageSize: Int,
        pageNum: Int
    ): List<XyMusic>? {
        var selectMusicList = db.musicDao.selectHomeMusicList(pageSize, pageNum * pageSize)

        if (selectMusicList.isEmpty()) {
            val homeMusicList = getServerMusicList(
                pageSize,
                pageNum * pageSize
            )
            selectMusicList = homeMusicList.items
        }
        return selectMusicList
    }

    /**
     * 根据专辑获得歌曲列表
     */
    override suspend fun getMusicListByAlbumId(
        albumId: String, pageSize: Int,
        pageNum: Int
    ): List<XyMusic>? {
        var selectMusicList =
            db.musicDao.selectMusicListByAlbumId(albumId, pageSize, pageNum * pageSize)

        if (selectMusicList.isEmpty()) {
            val homeMusicList = getServerMusicList(
                pageSize,
                pageNum * pageSize,
                parentId = albumId
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
        var selectMusicList =
            db.musicDao.selectMusicListByArtistId(artistId, pageSize, pageNum * pageSize)
        if (selectMusicList.isEmpty()) {
            val homeMusicList = getServerMusicList(
                pageSize,
                pageNum * pageSize,
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
        var selectMusicList =
            db.musicDao.selectMusicListByFavorite(pageSize, pageNum * pageSize)
        if (selectMusicList.isEmpty()) {
            val homeMusicList = getServerMusicList(
                pageSize,
                pageNum * pageSize,
                isFavorite = true
            )
            selectMusicList = homeMusicList.items
        }
        return selectMusicList
    }

    /**
     * 获得随机音乐
     */
    override suspend fun getRandomMusicList(pageSize: Int, pageNum: Int): List<XyMusic>? {
        return getServerMusicList(
            pageNum * pageSize,
            pageSize,
            sortBy = listOf(ItemSortBy.RANDOM)
        ).items
    }

    /**
     * 获得媒体库列表
     * //todo 这里需要只显示音乐媒体库
     */
    override suspend fun selectMediaLibrary() {
        val viewLibrary = jellyfinApiClient.userViewsApi().getUserViews(
            ViewRequest(
//                    includeExternalContent = false,
//                    presetViews = listOf(CollectionType.MUSIC)
            ).toMap()
        )
        //存储历史记录
        val libraries =
            viewLibrary.items.filter { it.collectionType == CollectionType.MUSIC }.map {
                XyLibrary(
                    id = it.id,
                    collectionType = it.collectionType.toString(),
                    name = it.name.toString(),
                    connectionId = connectionConfigServer.getConnectionId()
                )
            }
        if (libraries.isNotEmpty()) {
            db.withTransaction {
                db.libraryDao.remove()
                db.libraryDao.saveBatch(libraries)
            }
        }
    }

    /**
     * 获得最近播放音乐
     */
    override suspend fun playRecordMusicOrAlbumList() {
        val musicList = getServerMusicList(
            startIndex = 0,
            pageSize = Constants.MIN_PAGE,
            filters = listOf(ItemFilter.IS_PLAYED),
            sortBy = listOf(ItemSortBy.DATE_PLAYED),
            sortOrder = listOf(SortOrder.DESCENDING)
        ).items
        if (musicList.isNotEmpty())
            db.withTransaction {
                db.musicDao.removeByType(MusicDataTypeEnum.PLAY_HISTORY)

                saveBatchMusic(musicList, MusicDataTypeEnum.PLAY_HISTORY)
            }
    }

    /**
     * 获取歌单列表
     * @return [List<XyPlaylist>?]
     */
    override suspend fun getPlaylists(): List<XyAlbum>? {
        db.albumDao.removePlaylist()
        val response = getPlaylistsServer(0, 10000)
        return response.items?.let {
            saveBatchAlbum(it, MusicDataTypeEnum.PLAYLIST, true)
        }
    }

    /**
     * 增加歌单
     * @param [name] 名称
     * @return [String?] 歌单id
     */
    override suspend fun createPlaylist(name: String): String {
        return jellyfinApiClient.playlistsApi().createPlaylist(
            CreatePlaylistRequest(
                name = name, mediaType = MediaType.AUDIO, ids = emptyList(), users = listOf(
                    PlaylistUserPermissions(
                        userId = connectionConfigServer.getUserId(), canEdit = true
                    )
                ), isPublic = false
            )
        ).id
    }


    /**
     * 新增或修改歌单
     */
    override suspend fun importPlaylist(playlistData: ExportPlaylistData): Boolean {
        val playlists = playlistData.playlist
        val itemIds = playlists.map { it.itemId }
        val playlistMusicMap = playlistData.playlistMusic.groupBy { it.playlistId }
        if (playlists.isNotEmpty()) {
            val items = jellyfinApiClient.itemApi()
                .getItems(itemRequest = ItemRequest(ids = itemIds).toMap())
            //获得新建的表单
            val itemDataList = items.items
            if (itemDataList.isNotEmpty()) {
                val playlistMap = itemDataList.associateBy { it.id }
                playlists.forEach {
                    if (playlistMap.containsKey(it.itemId)) {
                        val playlistMusics = playlistMusicMap[it.itemId]
                        if (!playlistMusics.isNullOrEmpty()) {
                            //更新音乐数据
                            saveMusicPlaylist(
                                playlistId = it.itemId,
                                musicIds = playlistMusics.map { music -> music.musicId },
                                pic = it.pic
                            )
                        }
                    } else {
                        playlists.forEach { playlist ->
                            val playlistId = jellyfinApiClient.playlistsApi().createPlaylist(
                                CreatePlaylistRequest(
                                    name = playlist.name,
                                    mediaType = MediaType.AUDIO,
                                    ids = emptyList(),
                                    users = listOf(
                                        PlaylistUserPermissions(
                                            userId = connectionConfigServer.getUserId(),
                                            canEdit = true
                                        )
                                    ),
                                    isPublic = false
                                )
                            ).id
                            val playlistMusics = playlistMusicMap[playlist.itemId]
                            if (!playlistMusics.isNullOrEmpty()) {
                                saveMusicPlaylist(
                                    playlistId = playlistId,
                                    musicIds = playlistMusics.map { music -> music.musicId },
                                    pic = it.pic
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
        jellyfinApiClient.playlistsApi().updatePlaylist(
            playlistId = id, CreatePlaylistRequest(
                name = name
            )
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
        jellyfinApiClient.libraryApi().deleteItem(
            itemId = id
        )
        db.albumDao.removeById(
            id
        )
        return true
    }

    /**
     * 保存自建歌单中的音乐
     * @param [playlistId] 歌单id
     * @param [musicIds] 音乐id
     * @param [pic] 照片
     */
    override suspend fun saveMusicPlaylist(
        playlistId: String, musicIds: List<String>, pic: String?
    ): Boolean {
        jellyfinApiClient.playlistsApi().addItemToPlaylist(
            playlistId = playlistId,
            ids = musicIds.joinToString(Constants.ARTIST_DELIMITER) { it })
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
        jellyfinApiClient.playlistsApi().deletePlaylist(
            playlistId = playlistId,
            entryIds = musicIds.joinToString(Constants.ARTIST_DELIMITER) { it })
        db.musicDao.removeByPlaylistMusicByMusicId(
            playlistId = playlistId,
            musicIds = musicIds
        )
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
     * 根据艺术家id获得专辑列表
     */
    override suspend fun selectArtistsByIds(artistIds: List<String>): List<XyArtist> {
        val items = jellyfinApiClient.itemApi().getItems(
            ItemRequest(
                ids = artistIds, parentId = connectionConfigServer.libraryId
            ).toMap()
        ).items
        return convertToArtistList(items)
    }

    /**
     * 根据id获得艺术家信息
     * @param [artistId] 艺术家id
     * @return [List<ArtistItem>?] 艺术家信息
     */
    override suspend fun selectArtistInfoById(artistId: String): XyArtist {
        var artistInfo: XyArtist? = db.artistDao.selectById(artistId)
        if (artistInfo == null) {
            val item = jellyfinApiClient.userLibraryApi().getItem(itemId = artistId)
            val tmpArtistInfo = convertToArtistList(listOf(item))
            artistInfo = tmpArtistInfo[0]
        }

        return artistInfo
    }

    /**
     * 获得最多播放
     */
    override suspend fun getMostPlayerMusicList() {
        val musicList = getServerMusicList(
            startIndex = 0,
            pageSize = Constants.MIN_PAGE,
            filters = listOf(ItemFilter.IS_PLAYED),
            sortBy = listOf(ItemSortBy.PLAY_COUNT),
            sortOrder = listOf(SortOrder.DESCENDING)
        ).items
        if (musicList.isNotEmpty())
            db.withTransaction {
                db.musicDao.removeByType(MusicDataTypeEnum.MAXIMUM_PLAY)
                saveBatchMusic(musicList, dataType = MusicDataTypeEnum.MAXIMUM_PLAY)
            }
    }

    /**
     * 获得最新专辑
     */
    override suspend fun getNewestAlbumList() {
        val albumList = jellyfinApiClient.userLibraryApi().getLatestMedia(
            ItemRequest(
                fields = listOf(ItemFields.PRIMARY_IMAGE_ASPECT_RATIO),
                imageTypeLimit = 1,
                enableImageTypes = listOf(
                    ImageType.PRIMARY, ImageType.BACKDROP, ImageType.BANNER, ImageType.THUMB
                ),
                limit = Constants.MIN_PAGE,
                parentId = connectionConfigServer.libraryId
            ).toMap()
        )
        if (albumList.isNotEmpty())
            db.withTransaction {
                db.albumDao.removeByType(MusicDataTypeEnum.NEWEST)
                saveBatchAlbum(albumList.map { album ->
                    convertToAlbum(
                        album
                    )
                }, MusicDataTypeEnum.NEWEST)
            }
    }

    /**
     * 查询收藏音乐数据
     */
    @OptIn(ExperimentalPagingApi::class)
    override fun selectFavoriteMusicFlowList(): Flow<PagingData<XyMusic>> {
        return Pager(
            PagingConfig(
                pageSize = Constants.UI_LIST_PAGE,  // 每一页个数
                initialLoadSize = Constants.UI_INIT_LIST_PAGE,
                prefetchDistance = Constants.UI_PREFETCH_DISTANCE,
            ),
            remoteMediator = JellyfinFavoriteMusicRemoteMediator(
                jellyfinDatasourceServer = this,
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
                initialLoadSize = Constants.UI_INIT_LIST_PAGE,
                prefetchDistance = Constants.UI_PREFETCH_DISTANCE,
            ),
            remoteMediator = JellyfinGenresRemoteMediator(
                jellyfinDatasourceServer = this,
                db = db,
                connectionId = connectionConfigServer.getConnectionId()
            )
        ) {
            db.genreDao.selectByDataSourceType()
        }.flow
    }

    /**
     * 获得Okhttp客户端
     */
    override fun getOkhttpClient(): OkHttpClient {
        return jellyfinApiClient.apiOkHttpClient
    }

    /**
     * 获得token
     */
    override fun setToken() {
        TokenServer.setTokenData(jellyfinApiClient.getToken())
    }

    /**
     * 上报播放
     */
    override suspend fun reportPlaying(
        musicId: String,
        playSessionId: String,
        isPaused: Boolean,
        positionTicks: Long?
    ) {
        jellyfinApiClient.userApi().playing(
            PlaybackStartInfo(
                itemId = musicId,
                playSessionId = musicId,
                positionTicks = positionTicks,
                canSeek = true,
                isPaused = isPaused,
                isMuted = false,
                playMethod = PlayMethod.TRANSCODE
            )
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
        try {
            jellyfinApiClient.userApi().progress(
                PlaybackStartInfo(
                    itemId = musicId,
                    playSessionId = musicId,
                    positionTicks = positionTicks,
                    canSeek = true,
                    isPaused = false,
                    isMuted = false,
                    playMethod = PlayMethod.TRANSCODE
                )
            )
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "播放上报失败", e)
        }
    }

    /**
     * 获得播放连接
     */
    override suspend fun getMusicPlayUrl(musicId: String): String {
        return jellyfinApiClient.createAudioUrl(musicId)
    }

    /**
     * 获得流派内音乐列表/或者专辑
     * todo 试一下能不能查询到专辑
     * @param [genreId] 流派id
     */
    @OptIn(ExperimentalPagingApi::class)
    override fun selectAlbumListByGenreId(genreId: String): Flow<PagingData<XyAlbum>> {
        return Pager(
            PagingConfig(
                pageSize = Constants.UI_LIST_PAGE,  // 每一页个数
                initialLoadSize = Constants.UI_INIT_LIST_PAGE,
                prefetchDistance = Constants.UI_PREFETCH_DISTANCE,
            ),
            remoteMediator = JellyfinGenreAlbumListRemoteMediator(
                genreId = genreId,
                jellyfinDatasourceServer = this,
                db = db,
                connectionId = connectionConfigServer.getConnectionId()
            )
        ) {
            db.albumDao.selectGenreAlbumListPage(genreId)
        }.flow
    }

    /**
     * 获得流派详情
     */
    override suspend fun getGenreById(genreId: String): XyGenre? {
        var genre = db.genreDao.selectById(genreId)
        if (genre == null) {
            val queryResult = jellyfinApiClient.userLibraryApi().getItem(genreId)
            genre = convertToGenre(queryResult)
        }
        return genre
    }

    /**
     * 释放
     */
    override suspend fun release() {
        super.release()
        jellyfinApiClient.release()
    }

    /**
     * 获得专辑列表
     */
    suspend fun getAlbumList(
        pageSize: Int,
        startIndex: Int,
        albumIds: List<String>? = null,
        artistIds: List<String>? = null,
        isFavorite: Boolean? = null,
        search: String? = null,
        sortBy: List<ItemSortBy>? = null,
        sortOrder: List<SortOrder>? = null,
        filters: List<ItemFilter>? = null,
        years: List<Int>? = null,
        genreIds: List<String>? = null
    ): Response<XyAlbum> {
        val albumResponse = jellyfinApiClient.itemApi().getItems(
            ItemRequest(
                artistIds = artistIds,
                limit = pageSize,
                startIndex = startIndex,
                sortBy = sortBy ?: listOf(ItemSortBy.SORT_NAME),
                sortOrder = sortOrder ?: listOf(SortOrder.ASCENDING),
                includeItemTypes = listOf(BaseItemKind.MUSIC_ALBUM),
                recursive = true,
                isFavorite = isFavorite,
                filters = filters,
                fields = listOf(
                    ItemFields.PRIMARY_IMAGE_ASPECT_RATIO,
                    ItemFields.SORT_NAME,
                    ItemFields.EXTERNAL_URLS,
                    ItemFields.DATE_CREATED,
                    ItemFields.GENRES
                ),
                imageTypeLimit = 1,
                enableImageTypes = listOf(
                    ImageType.PRIMARY, ImageType.BACKDROP, ImageType.BANNER, ImageType.THUMB
                ),
                searchTerm = search,
                ids = albumIds,
                years = years,
                genreIds = genreIds,
                parentId = connectionConfigServer.libraryId
            ).toMap()
        )

        return Response(
            items = albumResponse.items.map { album ->
                convertToAlbum(
                    album,
                    isFavorite ?: false
                )
            },
            totalRecordCount = albumResponse.totalRecordCount,
            startIndex = albumResponse.startIndex
        )
    }

    /**
     * 首页音乐数据接口
     * 获取音乐列表
     * @param [pageSize] 页面大小
     * @param [startIndex] 页码
     * @param [albumId] 专辑 ID
     * @return [Response<ItemResponse>]
     */
    suspend fun getServerMusicList(
        pageSize: Int,
        startIndex: Int,
        albumId: String? = null,
        artistId: String? = null,
        isFavorite: Boolean? = null,
        search: String? = null,
        sortBy: List<ItemSortBy>? = null,
        sortOrder: List<SortOrder>? = null,
        filters: List<ItemFilter>? = null,
        years: List<Int>? = null,
        parentId: String? = null,
    ): Response<XyMusic> {
        val response = jellyfinApiClient.itemApi().getItems(
            itemRequest = ItemRequest(
                artistIds = artistId?.let { listOf(it) },
                limit = pageSize,
                startIndex = startIndex,
                sortBy = sortBy ?: listOf(ItemSortBy.SORT_NAME),
                sortOrder = sortOrder ?: listOf(SortOrder.ASCENDING),
                includeItemTypes = listOf(BaseItemKind.AUDIO),
                recursive = true,
                isFavorite = isFavorite,
                filters = filters,
                fields = listOf(
                    ItemFields.PRIMARY_IMAGE_ASPECT_RATIO,
                    ItemFields.SORT_NAME,
                    ItemFields.MEDIA_SOURCES,
                    ItemFields.DATE_CREATED
                ),
                searchTerm = search,
                imageTypeLimit = 1,
                years = years,
                albumIds = albumId?.let { listOf(albumId) },
                parentId = if (parentId.isNullOrBlank()) connectionConfigServer.libraryId else parentId
            ).toMap()
        )
        val items = response.items.map {
            transitionMusic(it)
        }

        return Response<XyMusic>(
            items = items,
            totalRecordCount = response.totalRecordCount,
            startIndex = response.startIndex
        )
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
        val response = jellyfinApiClient.artistsApi().getArtists(
            ItemRequest(
                limit = pageSize,
                startIndex = startIndex,
                sortBy = listOf(ItemSortBy.SORT_NAME),
                sortOrder = listOf(SortOrder.ASCENDING),
                fields = listOf(
                    ItemFields.PRIMARY_IMAGE_ASPECT_RATIO,
                    ItemFields.SORT_NAME,
                    ItemFields.EXTERNAL_URLS,
                ),
//                includeItemTypes = listOf(BaseItemKind.MUSIC_ARTIST),
                imageTypeLimit = 1,
                enableImageTypes = listOf(
                    ImageType.PRIMARY, ImageType.BACKDROP, ImageType.BANNER, ImageType.THUMB
                ),
                searchTerm = search,
                isFavorite = isFavorite,
                parentId = connectionConfigServer.libraryId
            ).toMap()
        )
        val artistList = convertToArtistList(response.items)
        return AllResponse(
            items = artistList,
            totalRecordCount = response.totalRecordCount,
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
        startIndex: Int,
        pageSize: Int,
        filters: List<ItemFilter>? = null,
        search: String? = null,
        sortBy: List<ItemSortBy>? = listOf(ItemSortBy.SORT_NAME),
        sortOrder: List<SortOrder>? = listOf(SortOrder.ASCENDING),
    ): Response<XyGenre> {
        val genres = jellyfinApiClient.genreApi().getGenres(
            itemRequest = ItemRequest(
                limit = pageSize,
                startIndex = startIndex,
                sortBy = sortBy ?: listOf(ItemSortBy.SORT_NAME),
                sortOrder = sortOrder ?: listOf(SortOrder.ASCENDING),
                recursive = true,
                filters = filters,
                fields = listOf(
                    ItemFields.PRIMARY_IMAGE_ASPECT_RATIO,
                    ItemFields.ITEM_COUNTS,
                ),
                searchTerm = search,
                imageTypeLimit = 1,
                parentId = connectionConfigServer.libraryId
            ).toMap()
        )
        return Response<XyGenre>(
            items = convertToGenreList(genres.items),
            totalRecordCount = genres.totalRecordCount,
            startIndex = genres.startIndex
        )
    }

    /**
     * 获取歌单列表
     */
    suspend fun getPlaylistsServer(startIndex: Int, pageSize: Int): AllResponse<XyAlbum> {
        return try {
            val playlists =
                jellyfinApiClient.itemApi().getItems(
                    ItemRequest(
                        sortBy = listOf(ItemSortBy.DATE_CREATED),
                        sortOrder = listOf(SortOrder.DESCENDING),
                        recursive = true,
                        includeItemTypes = listOf(BaseItemKind.PLAYLIST),
                        fields = listOf(
                            ItemFields.PRIMARY_IMAGE_ASPECT_RATIO,
                            ItemFields.SORT_NAME,
                            ItemFields.EXTERNAL_URLS,
                            ItemFields.DATE_CREATED
                        ),
                        startIndex = 0,
                        limit = pageSize,
                        userId = connectionConfigServer.getUserId()
                    ).toMap()
                )
            val allResponse = AllResponse(
                items = convertToAlbumList(playlists.items, true),
                totalRecordCount = playlists.totalRecordCount,
                startIndex = startIndex
            )
            allResponse
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "获取歌单失败", e)
            AllResponse<XyAlbum>(items = emptyList(), 0, 0)
        }
    }


    /**
     * 将ItemResponse转换成XyAlbum
     */
    fun convertToAlbumList(item: List<ItemResponse>, ifPlaylist: Boolean = false): List<XyAlbum> {
        return item.map { album ->
            convertToAlbum(album, ifPlaylist)
        }
    }


    /**
     * AlbumResponseVo转换为XyAlbum
     */
    fun convertToAlbum(
        item: ItemResponse,
        ifPlaylist: Boolean = false
    ): XyAlbum {
        val itemImageUrl =
            if (!item.imageTags.isNullOrEmpty()) jellyfinApiClient.createImageUrl(
                itemId = item.id,
                imageType = ImageType.PRIMARY,
                fillWidth = 297,
                fillHeight = 297,
                quality = 96,
                tag = item.imageTags?.get(ImageType.PRIMARY)
            )
            else null
        return XyAlbum(
            itemId = item.id,
            pic = itemImageUrl,
            name = item.name
                ?: if (ifPlaylist) Constants.UNKNOWN_PLAYLIST else Constants.UNKNOWN_ALBUM,
            connectionId = connectionConfigServer.getConnectionId(),
            artistIds = item.albumArtists?.joinToString(Constants.ARTIST_DELIMITER) { it.id },
            artists = item.albumArtists?.joinToString(Constants.ARTIST_DELIMITER) { it.name.toString() }
                ?: Constants.UNKNOWN_ARTIST,
            year = item.productionYear,
            premiereDate = item.premiereDate?.atZone(ZoneOffset.ofHours(8))?.toInstant()
                ?.toEpochMilli(),
            genreIds = item.genreItems?.joinToString(Constants.ARTIST_DELIMITER) { it.id },
            ifFavorite = item.userData?.isFavorite == true,
            ifPlaylist = ifPlaylist,
            createTime = item.dateCreated?.atZone(ZoneId.systemDefault())?.toEpochSecond() ?: 0L,
            musicCount = item.songCount?.toLong() ?: 0L
        )
    }

    //MusicResponseVo转换XyItem
    suspend fun transitionMusic(item: ItemResponse): XyMusic {

        val itemImageUrl = item.albumPrimaryImageTag?.let {
            item.albumId?.let { albumId ->
                jellyfinApiClient.createImageUrl(
                    albumId,
                    ImageType.PRIMARY,
                    fillWidth = 297,
                    fillHeight = 297,
                    quality = 96,
                    tag = item.albumPrimaryImageTag
                )
            }
        }


        //获得音乐信息
        val mediaSourceInfo = item.mediaSources?.find { it.protocol == MediaProtocol.FILE }
        val mediaStream = mediaSourceInfo?.mediaStreams?.find { it.type == MediaStreamType.AUDIO }

        val mediaStreamLyric =
            mediaSourceInfo?.mediaStreams?.find { it.type == MediaStreamType.LYRIC }

        val audioUrl = getMusicPlayUrl(item.id)

        return XyMusic(
            itemId = item.id,
            pic = itemImageUrl,
            name = item.name ?: Constants.UNKNOWN_MUSIC,
            musicUrl = audioUrl,
            album = item.albumId.toString(),
            albumName = item.album,
            connectionId = connectionConfigServer.getConnectionId(),
            artists = item.artistItems?.joinToString(Constants.ARTIST_DELIMITER) { artist -> artist.name.toString() },
            artistIds = item.artistItems?.joinToString(Constants.ARTIST_DELIMITER) { artist -> artist.id },
            albumArtist = item.albumArtists?.joinToString(Constants.ARTIST_DELIMITER) { artist -> artist.name.toString() }
                ?: Constants.UNKNOWN_ARTIST,
            albumArtistIds = item.albumArtists?.joinToString(Constants.ARTIST_DELIMITER) { artist -> artist.id },
            createTime = item.dateCreated?.atZone(ZoneId.systemDefault())?.toEpochSecond() ?: 0L,
            year = item.productionYear,
            genreIds = item.genreItems?.joinToString(Constants.ARTIST_DELIMITER) { it.id },
            playedCount = item.userData?.playCount ?: 0,
            ifFavoriteStatus = item.userData?.isFavorite == true,
            ifLyric = mediaStreamLyric != null,
            path = mediaSourceInfo?.path,
            bitRate = mediaSourceInfo?.bitrate,
            sampleRate = mediaStream?.sampleRate,
            bitDepth = mediaStream?.bitDepth,
            size = mediaSourceInfo?.size,
            runTimeTicks = BigDecimal.valueOf(mediaSourceInfo?.runTimeTicks ?: 0)
                .divide(BigDecimal(10000), BigDecimal.ROUND_UP).toLong(),
            container = mediaSourceInfo?.container,
            codec = mediaStream?.codec,
            lyric = ""
        )
    }


    /**
     * 根据BaseItemDto组装艺术家名单
     * @param [items] 项目
     * @return [List<ArtistItem>]
     */
    fun convertToArtistList(items: List<ItemResponse>): List<XyArtist> {
        val xyArtists = items.mapIndexed { index, item ->
            val artistImageUrl =
                if (!item.name.isNullOrBlank() && !item.imageTags.isNullOrEmpty()) item.name?.let {
                    jellyfinApiClient.createArtistImageUrl(
                        name = it,
                        imageType = ImageType.PRIMARY,
                        imageIndex = 0,
                        fillWidth = 600,
                        fillHeight = 600,
                        quality = 100,
                        tag = item.imageTags?.get(ImageType.PRIMARY)
                    )
                }
                else null

            val sortName = item.sortName
            val shortNameStart = if (!sortName.isNullOrBlank()) sortName[0] else '#'
            val selectChat =
                if (!CharUtils.isEnglishLetter(shortNameStart)) "#" else shortNameStart.toString()
            XyArtist(
                artistId = item.id,
                name = item.name ?: "未知艺术家",
                pic = artistImageUrl,
                connectionId = connectionConfigServer.getConnectionId(),
                sortName = sortName,
                describe = item.overview,
                musicCount = item.songCount,
                albumCount = item.albumCount,
                selectChat = selectChat,
                ifFavorite = item.userData?.isFavorite == true,
                indexNumber = index
            )
        }
        return xyArtists
    }


    /**
     * 将Genre转换成XyGenre
     */
    fun convertToGenreList(genres: List<ItemResponse>): List<XyGenre> {
        return genres.map {
            convertToGenre(it)
        }
    }

    /**
     * 将ItemResponse转换成XyGenre
     */
    fun convertToGenre(item: ItemResponse): XyGenre {
        val itemImageUrl = item.imageTags?.get(ImageType.PRIMARY)?.let {
            jellyfinApiClient.createImageUrl(
                itemId = item.id,
                imageType = ImageType.PRIMARY,
                fillWidth = 297,
                fillHeight = 297,
                quality = 96,
                tag = it
            )
        }
        return XyGenre(
            itemId = item.id,
            pic = itemImageUrl ?: "",
            name = item.name ?: Constants.UNKNOWN_ALBUM,
            connectionId = connectionConfigServer.getConnectionId(),
            createTime = item.dateCreated?.atZone(ZoneId.systemDefault())?.toEpochSecond() ?: 0L,
        )
    }
}