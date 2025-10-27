package cn.xybbz.api.client.emby

import android.content.Context
import android.icu.math.BigDecimal
import android.os.Build
import android.util.Log
import androidx.room.withTransaction
import cn.xybbz.api.client.IDataSourceParentServer
import cn.xybbz.api.client.data.AllResponse
import cn.xybbz.api.client.jellyfin.data.ClientLoginInfoReq
import cn.xybbz.api.client.jellyfin.data.CreatePlaylistRequest
import cn.xybbz.api.client.jellyfin.data.ItemRequest
import cn.xybbz.api.client.jellyfin.data.ItemResponse
import cn.xybbz.api.client.jellyfin.data.PlaybackStartInfo
import cn.xybbz.api.client.jellyfin.data.ViewRequest
import cn.xybbz.api.client.jellyfin.data.toLogin
import cn.xybbz.api.enums.jellyfin.BaseItemKind
import cn.xybbz.api.enums.jellyfin.CollectionType
import cn.xybbz.api.enums.jellyfin.ImageType
import cn.xybbz.api.enums.jellyfin.ItemFields
import cn.xybbz.api.enums.jellyfin.ItemFilter
import cn.xybbz.api.enums.jellyfin.ItemSortBy
import cn.xybbz.api.enums.jellyfin.MediaProtocol
import cn.xybbz.api.enums.jellyfin.MediaStreamType
import cn.xybbz.api.enums.jellyfin.PlayMethod
import cn.xybbz.api.enums.jellyfin.SortOrder
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.constants.Constants.LYRICS_AMPLIFICATION
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.common.enums.SortTypeEnum
import cn.xybbz.common.utils.CharUtils
import cn.xybbz.common.utils.PlaylistParser
import cn.xybbz.config.ConnectionConfigServer
import cn.xybbz.entity.api.LoginSuccessData
import cn.xybbz.entity.data.SearchAndOrder
import cn.xybbz.entity.data.SearchData
import cn.xybbz.entity.data.toSearchAndOrder
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.data.artist.XyArtist
import cn.xybbz.localdata.data.genre.XyGenre
import cn.xybbz.localdata.data.library.XyLibrary
import cn.xybbz.localdata.data.music.PlaylistMusic
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.enums.DataSourceType
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import cn.xybbz.ui.components.LrcEntry
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import okhttp3.OkHttpClient
import java.net.SocketTimeoutException
import java.time.ZoneId
import java.time.ZoneOffset
import javax.inject.Inject

class EmbyDatasourceServer @Inject constructor(
    private val db: DatabaseClient,
    private val application: Context,
    private val connectionConfigServer: ConnectionConfigServer,
    private val embyApiClient: EmbyApiClient,
) : IDataSourceParentServer(
    db,
    connectionConfigServer,
    application
) {
    /**
     * 获得当前数据源类型
     */
    override fun getDataSourceType(): DataSourceType {
        return DataSourceType.EMBY
    }

    /**
     * 登录功能
     */
    override suspend fun login(clientLoginInfoReq: ClientLoginInfoReq): LoginSuccessData {
        val responseData =
            embyApiClient.userApi().authenticateByName(clientLoginInfoReq.toLogin())
        Log.i("=====", "返回响应值: $responseData")
        embyApiClient.updateAccessTokenAndUserId(responseData.accessToken, responseData.user?.id)
        setToken()
        val systemInfo = embyApiClient.userApi().getSystemInfo()
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
            val pingData = embyApiClient.userApi().postPingSystem()
            if (pingData.isSuccessful) {
                val raw = pingData.body()?.string()
                Log.i("=====", "ping数据返回: $raw")// "Ping"
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "连通性检测失败", e)
            false
        }
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
        val deviceName = "${Build.BRAND} ${Build.MODEL}"
        val packageManager = application.packageManager
        val packageName = application.packageName
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        val versionName = packageInfo.versionName
        val versionCode = packageInfo.longVersionCode
        embyApiClient.createApiClient(
            "Android", "${versionName}.${versionCode}", deviceId, deviceName
        )
        //提前写入没有sessionToken的Authenticate请求头,不然登录请求都会报错
        setToken()
        embyApiClient.setRetrofitData(address, ifTmpObject())
    }

    /**
     * 创建歌单
     */
    override suspend fun createPlaylist(name: String): String? {
        return embyApiClient.playlistsApi().createPlaylist(
            name = name
        ).id
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
        val response = embyApiClient.artistsApi().getArtists(
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
     * 根据艺术家id获得专辑列表
     */
    override suspend fun selectArtistsByIds(artistIds: List<String>): List<XyArtist> {
        val items = embyApiClient.itemApi().getUserItems(
            userId = connectionConfigServer.getUserId(),
            ItemRequest(
                ids = artistIds,
                parentId = connectionConfigServer.libraryId,
                userId = connectionConfigServer.getUserId()
            ).toMap()
        ).items
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
        val response =
            getServerMusicList(
                startIndex = startIndex,
                pageSize = pageSize,
                artistId = artistId
            )
        return response
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
        ).items ?: emptyList()
    }

    /**
     * 搜索音乐
     * @param [search] 搜索内容
     */
    suspend fun searchMusicFlowList(search: String): List<XyMusic> {
        return getServerMusicList(
            startIndex = 0, pageSize = Constants.UI_LIST_PAGE, search = search
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
     * 将项目标记为收藏
     * @param [itemId] 专辑/音乐id
     */
    override suspend fun markFavoriteItem(itemId: String, dataType: MusicTypeEnum): Boolean {
        val favorite = embyApiClient.userLibraryApi()
            .markFavoriteItem(connectionConfigServer.getUserId(), itemId).isFavorite
        return favorite

    }

    /**
     * 取消项目收藏
     * @param [itemId] 专辑/音乐id
     */
    override suspend fun unmarkFavoriteItem(itemId: String, dataType: MusicTypeEnum): Boolean {
        val favorite = embyApiClient.userLibraryApi()
            .unmarkFavoriteItem(connectionConfigServer.getUserId(), itemId).isFavorite
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
                    getAlbumList(pageSize = 1, startIndex = 0).totalRecordCount
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
                    val response = getGenreList(
                        startIndex = 0,
                        pageSize = 0,
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
                        startIndex = 0, pageSize = 0, isFavorite = true
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
        embyApiClient.libraryApi().deleteItem(itemId = musicId)
        return true
    }

    /**
     * 批量删除数据
     * 按 ID 删除
     * @param [musicIds] 需要删除数据的
     * @return [Boolean?]
     */
    override suspend fun removeByIds(musicIds: List<String>): Boolean {
        embyApiClient.libraryApi()
            .deleteItems(ids = musicIds.joinToString(Constants.ARTIST_DELIMITER) { it })
        return true
    }

    /**
     * 从远程获得专辑信息
     * @param [albumId] 专辑id
     * @return 专辑+艺术家信息
     */
    override suspend fun selectAlbumInfoByRemotely(
        albumId: String,
        dataType: MusicDataTypeEnum
    ): XyAlbum? {
        val queryResult = embyApiClient.userLibraryApi()
            .getItem(connectionConfigServer.getUserId(), albumId)
        return convertToAlbum(
            queryResult,
        )
    }

    /**
     * 按 ID 选择音乐信息
     * @param [itemId] 音乐唯一标识
     * @return [XyMusic?]
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
            val lyrics = embyApiClient.lyricsApi().getLyrics(music.itemId)
            lyrics.lyrics.map {
                LrcEntry(startTime = it.start!! / LYRICS_AMPLIFICATION, text = it.text)
            }
        } else {
            null
        }
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
            sortBy = listOf(ItemSortBy.RANDOM)
        ).items
    }

    /**
     * 获取歌单列表
     */
    override suspend fun getPlaylists(): List<XyAlbum>? {
        db.albumDao.removePlaylist()
        val response = getPlaylistsServer(0, 10000)
        return response.items?.let {
            saveBatchAlbum(it, MusicDataTypeEnum.PLAYLIST, true)
        }
    }

    /**
     * 导入歌单
     */
    override suspend fun importPlaylist(
        playlistData: PlaylistParser.Playlist,
        playlistId: String
    ): Boolean {

        //导入数据,并且去重
        val musicList = playlistData.musicList.mapNotNull {
            try {
                val items = getServerMusicList(startIndex = 0, pageSize = 1, path = it.path).items
                if (items.isNullOrEmpty()) null else items[0]
            } catch (e: Exception) {
                Log.e(Constants.LOG_ERROR_PREFIX, "获取歌曲: ${it.title} 信息失败", e)
                null
            }
        }

        val serverMusicList = getServerMusicList(
            startIndex = 0,
            pageSize = Constants.PAGE_SIZE_ALL,
            parentId = playlistId
        )

        val serverMusicMap = serverMusicList.items?.groupBy { it.itemId }
        if (musicList.isNotEmpty()) {
            //去重后的列表
            val removeDuplicatesMusicList = musicList.mapNotNull {
                if (serverMusicMap?.containsKey(it.itemId) == true) null else it
            }
            if (removeDuplicatesMusicList.isNotEmpty()) {
                saveBatchMusic(
                    removeDuplicatesMusicList,
                    MusicDataTypeEnum.PLAYLIST,
                    null,
                    playlistId
                )
                val pic = if (removeDuplicatesMusicList.isNotEmpty()) musicList[0].pic else null
                saveMusicPlaylist(
                    playlistId = playlistId,
                    musicIds = removeDuplicatesMusicList.map { music -> music.itemId },
                    pic = pic
                )
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
        embyApiClient.playlistsApi().updatePlaylist(
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
        embyApiClient.libraryApi().deleteItem(
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
     * @param [musicIds] 音乐id集合
     * @param [pic] 自建歌单图片
     */
    override suspend fun saveMusicPlaylist(
        playlistId: String,
        musicIds: List<String>,
        pic: String?
    ): Boolean {
        embyApiClient.playlistsApi().addItemToPlaylist(
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
        embyApiClient.playlistsApi().deletePlaylist(
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
     * 根据id获得艺术家信息
     * @param [artistId] 艺术家id
     * @return [List<ArtistItem>?] 艺术家信息
     */
    override suspend fun selectArtistInfoByRemotely(artistId: String): XyArtist? {
        val item = embyApiClient.userLibraryApi()
            .getItem(userId = connectionConfigServer.getUserId(), itemId = artistId)
        val tmpArtistInfo = convertToArtistList(listOf(item))
        return tmpArtistInfo[0]


    }

    /**
     * 获得媒体库列表
     */
    override suspend fun selectMediaLibrary() {
        db.withTransaction {
            db.libraryDao.remove()
            val viewLibrary = embyApiClient.userViewsApi().getUserViews(
                userId = connectionConfigServer.getUserId(),
                ViewRequest().toMap()
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
                db.libraryDao.saveBatch(libraries)
            }
        }

    }

    /**
     * 获得最近播放音乐或专辑
     */
    override suspend fun playRecordMusicOrAlbumList(pageSize: Int) {
        val musicList = getServerMusicList(
            startIndex = 0,
            pageSize = pageSize,
            filters = listOf(ItemFilter.IS_PLAYED),
            sortBy = listOf(ItemSortBy.DATE_PLAYED),
            sortOrder = listOf(SortOrder.DESCENDING)
        ).items
        if (!musicList.isNullOrEmpty()) {
            db.withTransaction {
                db.musicDao.removeByType(MusicDataTypeEnum.PLAY_HISTORY)
                saveBatchMusic(musicList, MusicDataTypeEnum.PLAY_HISTORY)

            }
        }
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
        if (!musicList.isNullOrEmpty())
            db.withTransaction {
                db.musicDao.removeByType(MusicDataTypeEnum.MAXIMUM_PLAY)
                saveBatchMusic(musicList, dataType = MusicDataTypeEnum.MAXIMUM_PLAY)
            }
    }

    /**
     * 获得最新专辑
     */
    override suspend fun getNewestAlbumList() {
        val albumList = embyApiClient.userLibraryApi().getLatestMedia(
            userId = connectionConfigServer.getUserId(),
            ItemRequest(
                fields = listOf(ItemFields.PRIMARY_IMAGE_ASPECT_RATIO),
                imageTypeLimit = 1,
                enableImageTypes = listOf(
                    ImageType.PRIMARY, ImageType.BACKDROP, ImageType.BANNER, ImageType.THUMB
                ),
                limit = Constants.MIN_PAGE,
                parentId = connectionConfigServer.libraryId,
                userId = connectionConfigServer.getUserId()
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
     * 获得流派详情
     */
    override suspend fun getGenreById(genreId: String): XyGenre? {
        var genre = db.genreDao.selectById(genreId)
        if (genre == null) {
            val queryResult =
                embyApiClient.genreApi().getItem(connectionConfigServer.getUserId(), genreId)
            genre = convertToGenre(queryResult)
        }
        return genre

    }

    /**
     * 获得歌曲列表
     */
    override suspend fun getMusicList(
        pageSize: Int,
        pageNum: Int
    ): List<XyMusic>? {
        var selectMusicList: List<XyMusic>? =
            super.getMusicList(pageSize = pageSize, pageNum = pageNum)

        if (selectMusicList.isNullOrEmpty()) {
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
        albumId: String,
        pageSize: Int,
        pageNum: Int
    ): List<XyMusic>? {
        var selectMusicList: List<XyMusic>? =
            super.getMusicListByAlbumId(albumId = albumId, pageSize = pageSize, pageNum = pageNum)

        if (selectMusicList.isNullOrEmpty()) {
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
        var selectMusicList: List<XyMusic>? =
            super.getMusicListByArtistId(
                artistId = artistId,
                pageSize = pageSize,
                pageNum = pageNum
            )
        if (selectMusicList.isNullOrEmpty()) {
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
        var selectMusicList: List<XyMusic>? =
            super.getMusicListByFavorite(pageSize = pageSize, pageNum = pageNum)
        if (selectMusicList.isNullOrEmpty()) {
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
     * 获得OkHttpClient
     */
    override fun getOkhttpClient(): OkHttpClient {
        return embyApiClient.apiOkHttpClient
    }

    /**
     * 设置token
     */
    override fun setToken() {
        embyApiClient.updateTokenOrHeadersOrQuery()
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
        embyApiClient.userApi().playing(
            PlaybackStartInfo(
                itemId = musicId,
                playSessionId = musicId,
                positionTicks = positionTicks,
                canSeek = true,
                isPaused = isPaused,
                isMuted = false,
                playMethod = PlayMethod.DIRECT_STREAM
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

    }

    /**
     * 获得播放连接
     */
    override suspend fun getMusicPlayUrl(musicId: String): String {
        return embyApiClient.createAudioUrl(musicId)
    }

    /**
     * 获取远程服务器音乐列表
     * @param [startIndex] 开始索引
     * @param [pageSize] 页面大小
     * @param [isFavorite] 是否收藏
     * @param [sortType] 排序类型
     * @param [years] 年列表
     * @param [parentId] 上级id
     * @return [AllResponse<XyMusic>]
     */
    override suspend fun getRemoteServerMusicListByAlbumOrPlaylist(
        startIndex: Int,
        pageSize: Int,
        isFavorite: Boolean?,
        sortType: SortTypeEnum?,
        years: List<Int>?,
        parentId: String,
        dataType: MusicDataTypeEnum
    ): AllResponse<XyMusic> {
        val sortType: SearchAndOrder = sortType.toSearchAndOrder()
        val response = getServerMusicList(
            startIndex = startIndex,
            pageSize = pageSize,
            isFavorite = isFavorite,
            sortBy = sortType.sortType?.let { listOf(sortType.sortType) },
            sortOrder = sortType.order?.let { listOf(sortType.order) },
            years = years,
            parentId = parentId
        )
        return response
    }

    /**
     * 获取远程服务器专辑列表
     * @param [startIndex] 开始索引
     * @param [pageSize] 页面大小
     * @param [sortType] 排序类型
     * @param [isFavorite] 是否收藏
     * @param [years] 年列表
     * @return [AllResponse<XyAlbum>]
     */
    override suspend fun getRemoteServerAlbumList(
        startIndex: Int,
        pageSize: Int,
        sortType: SortTypeEnum?,
        isFavorite: Boolean?,
        years: List<Int>?,
        artistId: String?,
        genreId: String?
    ): AllResponse<XyAlbum> {
        val sortType: SearchAndOrder = sortType.toSearchAndOrder()
        val response = getAlbumList(
            startIndex = startIndex,
            pageSize = pageSize,
            sortBy = sortType.sortType?.let { listOf(sortType.sortType) },
            sortOrder = sortType.order?.let { listOf(sortType.order) },
            isFavorite = isFavorite,
            years = years,
            artistIds = artistId?.let { listOf(artistId) },
            genreIds = genreId?.let { listOf(genreId) },
        )
        return response
    }

    /**
     * 获取远程服务器收藏音乐列表
     * @param [startIndex] 启动索引
     * @param [pageSize] 页面大小
     * @param [isFavorite] 是最喜欢
     * @return [AllResponse<XyMusic>]
     */
    override suspend fun getRemoteServerFavoriteMusicList(
        startIndex: Int,
        pageSize: Int,
        isFavorite: Boolean
    ): AllResponse<XyMusic> {
        return getServerMusicList(
            startIndex = startIndex,
            pageSize = pageSize,
            isFavorite = isFavorite
        )
    }

    /**
     * 获取远程服务器流派列表
     * @param [startIndex] 启动索引
     * @param [pageSize] 页面大小
     * @return [AllResponse<XyGenre>]
     */
    override suspend fun getRemoteServerGenreList(
        startIndex: Int,
        pageSize: Int
    ): AllResponse<XyGenre> {
        return getGenreList(
            startIndex = startIndex,
            pageSize = pageSize,
        )
    }

    /**
     * 获取远程服务器音乐列表
     * @param [startIndex] 启动索引
     * @param [pageSize] 页面大小
     * @param [isFavorite] 是最喜欢
     * @param [sortType] 排序类型
     * @param [years] 年
     * @return [AllResponse<XyMusic>]
     */
    override suspend fun getRemoteServerMusicList(
        startIndex: Int,
        pageSize: Int,
        isFavorite: Boolean?,
        sortType: SortTypeEnum?,
        years: List<Int>?
    ): AllResponse<XyMusic> {
        val sortType: SearchAndOrder = sortType.toSearchAndOrder()
        val response = getServerMusicList(
            startIndex = startIndex,
            pageSize = pageSize,
            isFavorite = isFavorite,
            sortBy = sortType.sortType?.let { listOf(sortType.sortType) },
            sortOrder = sortType.order?.let { listOf(sortType.order) },
            years = years
        )
        return response
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
        startIndex: Int,
        pageSize: Int,
        albumId: String? = null,
        artistId: String? = null,
        isFavorite: Boolean? = null,
        search: String? = null,
        sortBy: List<ItemSortBy>? = null,
        sortOrder: List<SortOrder>? = null,
        filters: List<ItemFilter>? = null,
        years: List<Int>? = null,
        parentId: String? = null,
        path: String? = null,
    ): AllResponse<XyMusic> {
        val response = embyApiClient.itemApi().getUserItems(
            userId = connectionConfigServer.getUserId(),
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
                parentId = if (parentId.isNullOrBlank()) connectionConfigServer.libraryId else parentId,
                userId = connectionConfigServer.getUserId(),
                path = path
            ).toMap()
        )
        return AllResponse<XyMusic>(
            items = convertToMusicList(response.items),
            totalRecordCount = response.totalRecordCount,
            startIndex = response.startIndex ?: 0
        )
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
    ): AllResponse<XyAlbum> {
        val albumResponse = embyApiClient.itemApi().getUserItems(
            userId = connectionConfigServer.getUserId(),
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
                parentId = connectionConfigServer.libraryId,
                userId = connectionConfigServer.getUserId()
            ).toMap()
        )

        return AllResponse(
            items = convertToAlbumList(albumResponse.items),
            totalRecordCount = albumResponse.totalRecordCount,
            startIndex = albumResponse.startIndex ?: 0
        )
    }


    /**
     * 获取歌单列表
     */
    suspend fun getPlaylistsServer(startIndex: Int, pageSize: Int): AllResponse<XyAlbum> {
        return try {
            val playlists =
                embyApiClient.itemApi().getUserItems(
                    userId = connectionConfigServer.getUserId(),
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
                        imageTypeLimit = 1,
                        enableImageTypes = listOf(
                            ImageType.PRIMARY
                        ),
                        startIndex = 0,
                        limit = pageSize,
                        parentId = connectionConfigServer.libraryId,
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
    ): AllResponse<XyGenre> {
        val genreResponse = embyApiClient.genreApi().getGenres(
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

        return AllResponse(
            items = convertToGenreList(genreResponse.items),
            totalRecordCount = genreResponse.totalRecordCount,
            startIndex = startIndex
        )
    }

    /**
     * 根据BaseItemDto组装艺术家名单
     * @param [items] 项目
     * @return [List<ArtistItem>]
     */
    fun convertToArtistList(items: List<ItemResponse>): List<XyArtist> {
        val xyArtists = items.mapIndexed { index, item ->
            convertToArtist(item, index)
        }
        return xyArtists
    }

    /**
     * 将ArtistID3转换成XyArtist
     */
    fun convertToArtist(
        artist: ItemResponse,
        indexNumber: Int,
    ): XyArtist {
        val artistImageUrl =
            if (!artist.name.isNullOrBlank() && !artist.imageTags.isNullOrEmpty()) artist.name?.let {
                embyApiClient.createArtistImageUrl(
                    name = it,
                    imageType = ImageType.PRIMARY,
                    imageIndex = 0,
                    fillWidth = 600,
                    fillHeight = 600,
                    quality = 100,
                    tag = artist.imageTags?.get(ImageType.PRIMARY)
                )
            }
            else null

        val sortName = artist.sortName
        val shortNameStart = if (!sortName.isNullOrBlank()) sortName[0] else '#'
        val selectChat =
            if (!CharUtils.isEnglishLetter(shortNameStart)) "#" else shortNameStart.toString()
        return XyArtist(
            artistId = artist.id,
            name = artist.name ?: application.getString(Constants.UNKNOWN_ARTIST),
            pic = artistImageUrl,
            connectionId = connectionConfigServer.getConnectionId(),
            sortName = sortName,
            describe = artist.overview,
            musicCount = artist.songCount,
            albumCount = artist.albumCount,
            selectChat = selectChat,
            ifFavorite = artist.userData?.isFavorite == true,
            indexNumber = indexNumber
        )
    }


    /**
     * 将ItemResponse换成XyMusic
     */
    suspend fun convertToMusicList(item: List<ItemResponse>): List<XyMusic> {
        return item.map { music ->
            convertToMusic(
                music
            )
        }
    }

    //ItemResponse转换XyMusic
    suspend fun convertToMusic(item: ItemResponse): XyMusic {

        val itemImageUrl = item.albumPrimaryImageTag?.let {
            item.albumId?.let { albumId ->
                embyApiClient.createImageUrl(
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
            name = item.name ?: application.getString(Constants.UNKNOWN_MUSIC),
            musicUrl = audioUrl,
            album = item.albumId.toString(),
            albumName = item.album,
            connectionId = connectionConfigServer.getConnectionId(),
            artists = item.artistItems?.joinToString(Constants.ARTIST_DELIMITER) { artist -> artist.name.toString() },
            artistIds = item.artistItems?.joinToString(Constants.ARTIST_DELIMITER) { artist -> artist.id },
            albumArtist = item.albumArtists?.joinToString(Constants.ARTIST_DELIMITER) { artist -> artist.name.toString() }
                ?: application.getString(Constants.UNKNOWN_ARTIST),
            albumArtistIds = item.albumArtists?.joinToString(Constants.ARTIST_DELIMITER) { artist -> artist.id },
            createTime = item.dateCreated?.atZone(ZoneId.systemDefault())?.toEpochSecond() ?: 0L,
            year = item.productionYear,
            genreIds = item.genreItems?.joinToString(Constants.ARTIST_DELIMITER) { it.id },
            playedCount = item.userData?.playCount ?: 0,
            ifFavoriteStatus = item.userData?.isFavorite == true,
            ifLyric = mediaStreamLyric != null,
            path = mediaSourceInfo?.path ?: "",
            bitRate = mediaSourceInfo?.bitrate,
            sampleRate = mediaStream?.sampleRate,
            bitDepth = mediaStream?.bitDepth,
            size = mediaSourceInfo?.size,
            runTimeTicks = BigDecimal.valueOf(mediaSourceInfo?.runTimeTicks ?: 0)
                .divide(BigDecimal(10000), BigDecimal.ROUND_UP).toLong(),
            container = mediaSourceInfo?.container,
            codec = mediaStream?.codec,
            lyric = "",
            playlistItemId = item.id
        )
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
     * 将ItemResponse转换成XyAlbum
     */
    fun convertToAlbum(album: ItemResponse, ifPlaylist: Boolean = false): XyAlbum {
        val itemImageUrl =
            if (!album.imageTags.isNullOrEmpty()) embyApiClient.createImageUrl(
                itemId = album.id,
                imageType = ImageType.PRIMARY,
                fillWidth = 297,
                fillHeight = 297,
                quality = 96,
                tag = album.imageTags?.get(ImageType.PRIMARY)
            )
            else null
        return XyAlbum(
            itemId = album.id,
            pic = itemImageUrl,
            name = album.name
                ?: if (ifPlaylist) application.getString(Constants.UNKNOWN_PLAYLIST) else application.getString(Constants.UNKNOWN_ALBUM),
            connectionId = connectionConfigServer.getConnectionId(),
            artistIds = album.albumArtists?.joinToString(Constants.ARTIST_DELIMITER) { it.id },
            artists = album.albumArtists?.joinToString(Constants.ARTIST_DELIMITER) { it.name.toString() }
                ?: application.getString(Constants.UNKNOWN_ARTIST),
            year = album.productionYear,
            premiereDate = album.premiereDate?.atZone(ZoneOffset.ofHours(8))?.toInstant()
                ?.toEpochMilli(),
            genreIds = album.genreItems?.joinToString(Constants.ARTIST_DELIMITER) { it.id },
            ifFavorite = album.userData?.isFavorite == true,
            ifPlaylist = ifPlaylist,
            createTime = album.dateCreated?.atZone(ZoneId.systemDefault())?.toEpochSecond() ?: 0L,
            musicCount = album.songCount?.toLong() ?: 0L
        )
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
     * 将Genre转换成XyGenre
     */
    fun convertToGenre(genre: ItemResponse): XyGenre {
        val itemImageUrl = genre.imageTags?.get(ImageType.PRIMARY)?.let {
            embyApiClient.createImageUrl(
                itemId = genre.id,
                imageType = ImageType.PRIMARY,
                fillWidth = 297,
                fillHeight = 297,
                quality = 96,
                tag = it
            )
        }

        return XyGenre(
            itemId = genre.id,
            pic = itemImageUrl ?: "",
            name = genre.name ?: application.getString(Constants.UNKNOWN_ALBUM),
            connectionId = connectionConfigServer.getConnectionId(),
            createTime = genre.dateCreated?.atZone(ZoneId.systemDefault())?.toEpochSecond() ?: 0L,
        )
    }


}