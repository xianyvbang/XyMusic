package cn.xybbz.api.client.navidrome

import android.content.Context
import android.icu.text.Transliterator
import android.util.Log
import androidx.room.withTransaction
import cn.xybbz.api.client.IDataSourceParentServer
import cn.xybbz.api.client.data.AllResponse
import cn.xybbz.api.client.jellyfin.data.ClientLoginInfoReq
import cn.xybbz.api.client.navidrome.data.AlbumItem
import cn.xybbz.api.client.navidrome.data.ArtistItem
import cn.xybbz.api.client.navidrome.data.Genre
import cn.xybbz.api.client.navidrome.data.PlaylistAddMusicsUpdateRequest
import cn.xybbz.api.client.navidrome.data.PlaylistItemData
import cn.xybbz.api.client.navidrome.data.PlaylistUpdateRequest
import cn.xybbz.api.client.navidrome.data.SongItem
import cn.xybbz.api.client.navidrome.data.getWithTotalCount
import cn.xybbz.api.client.navidrome.data.toNavidromeLogin
import cn.xybbz.api.client.subsonic.data.ScrobbleRequest
import cn.xybbz.api.constants.ApiConstants
import cn.xybbz.api.enums.navidrome.OrderType
import cn.xybbz.api.enums.navidrome.SortType
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.common.enums.SortTypeEnum
import cn.xybbz.common.utils.CharUtils
import cn.xybbz.common.utils.LrcUtils
import cn.xybbz.common.utils.PlaylistParser
import cn.xybbz.config.ConnectionConfigServer
import cn.xybbz.entity.api.LoginSuccessData
import cn.xybbz.entity.data.NavidromeOrder
import cn.xybbz.entity.data.SearchData
import cn.xybbz.entity.data.toNavidromeOrder
import cn.xybbz.entity.data.toNavidromeOrder2
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.data.artist.XyArtist
import cn.xybbz.localdata.data.genre.XyGenre
import cn.xybbz.localdata.data.music.PlaylistMusic
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.enums.DataSourceType
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import cn.xybbz.ui.components.LrcEntry
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import okhttp3.OkHttpClient
import java.net.SocketTimeoutException
import javax.inject.Inject

class NavidromeDatasourceServer @Inject constructor(
    private val db: DatabaseClient,
    private val application: Context,
    private val connectionConfigServer: ConnectionConfigServer,
    private val navidromeApiClient: NavidromeApiClient,
) : IDataSourceParentServer(
    db,
    connectionConfigServer,
    application
) {
    /**
     * 获得当前数据源类型
     */
    override fun getDataSourceType(): DataSourceType {
        return DataSourceType.NAVIDROME
    }

    /**
     * 登录功能
     */
    override suspend fun login(clientLoginInfoReq: ClientLoginInfoReq): LoginSuccessData {
        val responseData = navidromeApiClient.userApi().login(clientLoginInfoReq.toNavidromeLogin())
        Log.i("=====", "返回响应值: $responseData")
        val packageManager = application.packageManager
        val packageName = application.packageName
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
        val appName = packageManager.getApplicationLabel(applicationInfo).toString()
        val versionName = packageInfo.versionName
        val versionCode = packageInfo.longVersionCode
        navidromeApiClient.createSubsonicApiClient(
            username = clientLoginInfoReq.username,
            passwordMd5 = responseData.subsonicToken,
            encryptedSalt = responseData.subsonicSalt,
            protocolVersion = DataSourceType.SUBSONIC.version,
            clientName = "${appName}:${versionName}.${versionCode}",
            token = responseData.token,
            id = responseData.id
        )
        setToken()
        val systemInfo = navidromeApiClient.userApi().postPingSystem()
        Log.i("=====", "服务器信息 $systemInfo")

        return LoginSuccessData(
            userId = responseData.id,
            accessToken = responseData.token,
            serverId = "",
            serverName = systemInfo.subsonicResponse.type,
            version = systemInfo.subsonicResponse.serverVersion
        )
    }

    /**
     * 连通性检测
     */
    override suspend fun postPingSystem(): Boolean {
        return true
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
        navidromeApiClient.setRetrofitData(address, ifTmpObject())
    }

    /**
     * 创建歌单
     */
    override suspend fun createPlaylist(name: String): String? {
        val id = navidromeApiClient.playlistsApi()
            .createPlaylist(name = name)?.id
        return id
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
        val response =
            getWithTotalCount {
                navidromeApiClient.artistsApi().getArtists(
                    start = 0,
                    end = 0,
                    name = search,
                    starred = isFavorite
                )
            }
        val artists = response.data?.let { convertToArtistList(it) } ?: emptyList()
        return AllResponse(
            items = artists,
            totalRecordCount = response.totalCount ?: 0,
            startIndex = startIndex
        )
    }

    /**
     * 根据艺术家id获得专辑列表
     */
    override suspend fun selectArtistsByIds(artistIds: List<String>): List<XyArtist> {
        return artistIds.mapNotNull { artistId ->
            val artist = navidromeApiClient.artistsApi().getArtist(artistId)
            artist?.let { artist -> convertToArtist(artist, 0) }
        }
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
        return getServerAlbumList(
            pageSize = Constants.UI_LIST_PAGE, startIndex = 0, search = search
        ).items ?: emptyList()
    }

    /**
     * 搜索音乐
     * @param [search] 搜索内容
     */
    suspend fun searchMusicFlowList(search: String): List<XyMusic> {
        return getServerMusicList(
            pageSize = Constants.UI_LIST_PAGE, startIndex = 0, search = search
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
        val favorite = navidromeApiClient.userLibraryApi()
            .markFavoriteItem(
                id = if (dataType == MusicTypeEnum.MUSIC) listOf(itemId) else null,
                albumId = if (dataType == MusicTypeEnum.ALBUM) listOf(itemId) else null,
                artistId = if (dataType == MusicTypeEnum.ARTIST) listOf(itemId) else null
            ).isFavorite
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
        val favorite = navidromeApiClient.userLibraryApi().unmarkFavoriteItem(
            id = if (dataType == MusicTypeEnum.MUSIC) listOf(itemId) else null,
            albumId = if (dataType == MusicTypeEnum.ALBUM) listOf(itemId) else null,
            artistId = if (dataType == MusicTypeEnum.ARTIST) listOf(itemId) else null
        ).isFavorite
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
                    getServerAlbumList(pageSize = 1, startIndex = 0).totalRecordCount
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
                    getArtistList(startIndex = 0, pageSize = 1).totalRecordCount
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
                    getServerMusicList(pageSize = 1, startIndex = 0).totalRecordCount
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
                        pageSize = 1,
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
        return true
    }

    /**
     * 批量删除数据
     * 按 ID 删除
     * @param [musicIds] 需要删除数据的
     * @return [Boolean?]
     */
    override suspend fun removeByIds(musicIds: List<String>): Boolean {
        return true
    }

    /**
     * 获得专辑信息
     * @param [albumId] 专辑id
     * @return 专辑+艺术家信息
     */
    override suspend fun selectAlbumInfoByRemotely(
        albumId: String,
        dataType: MusicDataTypeEnum
    ): XyAlbum? {
        val queryResult = navidromeApiClient.itemApi().getAlbum(albumId)
        return convertToAlbum(queryResult)
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
            music.lyric?.let {
                LrcUtils.parseLrc(it)

            }
        } else {
            null
        }
    }

    /**
     * 根据艺术家获得音乐列表
     */
    override suspend fun selectMusicListByArtistServer(
        artistId: String,
        pageSize: Int,
        startIndex: Int
    ): AllResponse<XyMusic> {
        return getServerMusicList(startIndex = startIndex, pageSize = pageSize, artistId = artistId)
    }

    /**
     * 获得随机音乐
     */
    override suspend fun getRandomMusicList(pageSize: Int, pageNum: Int): List<XyMusic>? {
        val randomSongs = getServerMusicList(
            startIndex = pageSize * pageNum,
            pageSize = pageSize,
            sortBy = SortType.RANDOM,
        )
        return randomSongs.items
    }

    /**
     * 获取歌单列表
     */
    override suspend fun getPlaylists(): List<XyAlbum>? {
        db.albumDao.removePlaylist()
        val allResponse = getPlaylistsServer(0, 0)
        if (!allResponse.items.isNullOrEmpty()) {
            allResponse.items?.let {
                saveBatchAlbum(it, MusicDataTypeEnum.PLAYLIST, true)
            }
        }
        return allResponse.items
    }

    /**
     * 导入歌单
     */
    override suspend fun importPlaylist(
        playlistData: PlaylistParser.Playlist,
        playlistId: String
    ): Boolean {

        val musicList = playlistData.musicList.mapNotNull {
            try {
                val items = getServerMusicList(
                    startIndex = 0,
                    pageSize = 1,
                    search = it.title + " " + it.artist
                ).items
                if (items.isNullOrEmpty()) null else items[0]
            } catch (e: Exception) {
                Log.e(Constants.LOG_ERROR_PREFIX, "获取歌曲: ${it.title} 信息失败", e)
                null
            }
        }

        val serverMusicList = getMusicListByAlbumOrPlaylist(
            startIndex = 0,
            pageSize = Constants.PAGE_SIZE_ALL,
            itemId = playlistId,
            dataType = MusicDataTypeEnum.PLAYLIST
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
        navidromeApiClient.playlistsApi().updatePlaylist(
            playlistId = id, PlaylistUpdateRequest(
                name = name,
                id = id
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
        navidromeApiClient.playlistsApi().deletePlaylist(
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
        navidromeApiClient.playlistsApi().addPlaylistMusics(
            playlistId = playlistId, PlaylistAddMusicsUpdateRequest(musicIds)
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
        val response = navidromeApiClient.playlistsApi().removePlaylistMusics(
            playlistId = playlistId, id = musicIds
        )
        return if (response.id != null || !response.ids.isNullOrEmpty()) {
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
            true
        } else {
            false
        }
    }

    /**
     * 根据id获得艺术家信息
     * @param [artistId] 艺术家id
     * @return [List<ArtistItem>?] 艺术家信息
     */
    override suspend fun selectArtistInfoByRemotely(artistId: String): XyArtist? {
        val artist = navidromeApiClient.artistsApi().getArtist(artistId)
        return artist?.let { convertToArtist(it, indexNumber = 0) }
    }

    /**
     * 获得媒体库列表
     */
    override suspend fun selectMediaLibrary() {

    }

    /**
     * 获得最近播放音乐或专辑
     */
    override suspend fun playRecordMusicOrAlbumList(pageSize: Int) {
        //只有最近播放专辑
        //插入最新播放专辑
        val musicList = getServerMusicList(
            startIndex = 0,
            pageSize = pageSize,
            sortOrder = OrderType.DESC,
            sortBy = SortType.PLAY_DATE
        )
        musicList.items?.let {
            db.withTransaction {
                db.musicDao.removeByType(MusicDataTypeEnum.PLAY_HISTORY)
                saveBatchMusic(it, MusicDataTypeEnum.PLAY_HISTORY)
            }
        }
    }

    /**
     * 获得最多播放
     */
    override suspend fun getMostPlayerMusicList() {
        val albumList = getServerAlbumList(
            startIndex = 0,
            pageSize = Constants.MIN_PAGE,
            orderType = OrderType.DESC,
            sortBy = SortType.PLAY_COUNT,
            recentlyPlayed = true
        )
        albumList.items?.let {
            db.withTransaction {
                db.albumDao.removeByType(MusicDataTypeEnum.MAXIMUM_PLAY)
                saveBatchAlbum(it, MusicDataTypeEnum.MAXIMUM_PLAY)
            }
        }
    }

    /**
     * 获得最新专辑
     */
    override suspend fun getNewestAlbumList() {
        val albumList = getServerAlbumList(
            startIndex = 0,
            pageSize = Constants.MIN_PAGE,
            orderType = OrderType.DESC,
            sortBy = SortType.RECENTLY_ADDED,
        )
        albumList.items?.let {
            db.withTransaction {
                db.albumDao.removeByType(MusicDataTypeEnum.NEWEST)
                saveBatchAlbum(it, MusicDataTypeEnum.NEWEST)
            }
        }
    }

    /**
     * 获得流派详情
     */
    override suspend fun getGenreById(genreId: String): XyGenre? {
        return db.genreDao.selectById(genreId)
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
                startIndex = pageNum * pageSize,
                pageSize = pageSize,
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
            super.getMusicListByArtistId(
                artistId = artistId,
                pageSize = pageSize,
                pageNum = pageNum
            )
        if (selectMusicList.isNullOrEmpty()) {
            val homeMusicList = getServerMusicList(
                startIndex = pageNum * pageSize,
                pageSize = pageSize,
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
                startIndex = pageNum * pageSize,
                pageSize = pageSize,
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
        return navidromeApiClient.apiOkHttpClient
    }

    /**
     * 设置token
     */
    override fun setToken() {
        navidromeApiClient.updateTokenOrHeadersOrQuery()
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
        navidromeApiClient.userApi().scrobble(
            ScrobbleRequest(
                id = musicId,
                submission = isPaused
            ).toMap()
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
        return navidromeApiClient.createAudioUrl(musicId)
    }

    /**
     * 释放
     */
    override suspend fun release() {
        super.release()
        navidromeApiClient.release()
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
        val sortType: NavidromeOrder = sortType.toNavidromeOrder()
        return getMusicListByAlbumOrPlaylist(
            startIndex = startIndex,
            pageSize = pageSize,
            isFavorite = isFavorite,
            itemId = parentId,
            dataType = dataType,
            sortOrder = sortType.order,
            sortBy = sortType.sortType,
            year = years?.get(0)
        )
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
        val sortType: NavidromeOrder = sortType.toNavidromeOrder()
        val response = getServerAlbumList(
            startIndex = startIndex,
            pageSize = pageSize,
            orderType = sortType.order,
            sortBy = sortType.sortType,
            isFavorite = isFavorite,
            year = years?.get(0),
            artistId = artistId,
            genreId = genreId
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
            pageSize = pageSize
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
        val sortType: NavidromeOrder = sortType.toNavidromeOrder2()
        val response = getServerMusicList(
            startIndex = startIndex,
            pageSize = pageSize,
            sortBy = sortType.sortType,
            sortOrder = sortType.order,
            isFavorite = isFavorite,
            year = years?.get(0)
        )
        return response
    }

    /**
     * 获得专辑列表
     */
    suspend fun getServerAlbumList(
        pageSize: Int,
        startIndex: Int,
        search: String? = null,
        orderType: OrderType = OrderType.ASC,
        sortBy: SortType = SortType.NAME,
        isFavorite: Boolean? = null,
        year: Int? = null,
        genreId: String? = null,
        artistId: String? = null,
        recentlyPlayed: Boolean? = null,
    ): AllResponse<XyAlbum> {

        val albumList =
            getWithTotalCount {
                navidromeApiClient.itemApi().getAlbumList(
                    start = startIndex,
                    end = startIndex + pageSize,
                    name = search,
                    order = orderType,
                    sort = sortBy,
                    starred = isFavorite,
                    year = year,
                    genreId = genreId,
                    artistId = artistId,
                    recentlyPlayed = recentlyPlayed
                )
            }

        return AllResponse(
            items = albumList.data?.let { convertToAlbumList(it) },
            totalRecordCount = albumList.totalCount ?: 0,
            startIndex = startIndex
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
        startIndex: Int,
        pageSize: Int,
        albumId: String? = null,
        artistId: String? = null,
        genreId: String? = null,
        isFavorite: Boolean? = null,
        search: String? = null,
        year: Int? = null,
        sortBy: SortType = SortType.TITLE,
        sortOrder: OrderType = OrderType.ASC,
    ): AllResponse<XyMusic> {
        val response =
            getWithTotalCount {
                navidromeApiClient.itemApi().getSong(
                    start = startIndex,
                    end = startIndex + pageSize,
                    order = sortOrder,
                    sort = sortBy,
                    title = search,
                    starred = isFavorite,
                    genreId = genreId,
                    albumId = albumId,
                    artistId = artistId,
                    year = year,
                )
            }
        return AllResponse(
            items = response.data?.let { convertToMusicList(it, false) },
            totalRecordCount = response.totalCount ?: 0,
            startIndex = startIndex
        )
    }

    /**
     * 获得专辑或歌单的音乐列表
     */
    suspend fun getMusicListByAlbumOrPlaylist(
        startIndex: Int,
        pageSize: Int,
        isFavorite: Boolean? = null,
        itemId: String,
        dataType: MusicDataTypeEnum,
        sortOrder: OrderType = OrderType.ASC,
        sortBy: SortType = SortType.NAME,
        year: Int? = null
    ): AllResponse<XyMusic> {
        if (dataType == MusicDataTypeEnum.ALBUM) {
            //存储歌曲数据
            return getServerMusicList(
                startIndex = startIndex,
                pageSize = pageSize,
                isFavorite = isFavorite,
                albumId = itemId,
                sortOrder = sortOrder,
                sortBy = sortBy,
                year = year

            )
        } else {
            //存储歌曲数据
            val playlistMusicList =
                getWithTotalCount {
                    navidromeApiClient.playlistsApi().getPlaylistMusicList(
                        playlistId = itemId, start = startIndex,
                        end = startIndex + pageSize,
                        starred = isFavorite
                    )
                }

            return AllResponse(
                items = playlistMusicList.data?.let { convertToMusicList(it, true) },
                totalRecordCount = playlistMusicList.totalCount ?: 0,
                startIndex = startIndex
            )
        }
    }

    /**
     * 获取歌单列表
     */
    suspend fun getPlaylistsServer(startIndex: Int, pageSize: Int): AllResponse<XyAlbum> {
        return try {
            val playlists =
                getWithTotalCount {
                    navidromeApiClient.playlistsApi()
                        .getPlaylists(start = startIndex, end = startIndex + pageSize)
                }
            val allResponse = AllResponse(
                items = playlists.data?.let { convertToPlaylists(it) },
                totalRecordCount = playlists.totalCount ?: 0,
                startIndex = startIndex
            )
            allResponse
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "获取歌单失败", e)
            AllResponse(items = emptyList(), 0, 0)
        }
    }


    /**
     * 获得流派列表
     * @param [startIndex] 启动索引
     * @param [pageSize] 限制
     * @param [search] 搜索
     * @param [sortBy] 排序方式
     * @param [sortOrder] 排序订单
     * @return [Response<ItemResponse>]
     */
    suspend fun getGenreList(
        startIndex: Int,
        pageSize: Int,
        search: String? = null,
        sortBy: SortType = SortType.NAME,
        sortOrder: OrderType = OrderType.ASC
    ): AllResponse<XyGenre> {
        val genreResponse = getWithTotalCount {
            navidromeApiClient.genreApi().getGenres(
                start = startIndex,
                end = startIndex + pageSize,
                name = search,
                order = sortOrder,
                sort = sortBy
            )
        }

        return AllResponse(
            items = genreResponse.data?.let { convertToGenreList(it) },
            totalRecordCount = genreResponse.totalCount ?: 0,
            startIndex = startIndex
        )
    }

    /**
     * 将PlaylistItemData3转换成XyAlbum
     */
    fun convertToPlaylists(playlists: List<PlaylistItemData>): List<XyAlbum> {
        return playlists.map { playlist ->
            convertToPlaylist(playlist)
        }
    }

    /**
     * 将PlaylistItemData转换成XyAlbum
     */
    fun convertToPlaylist(playlist: PlaylistItemData): XyAlbum {
        return XyAlbum(
            itemId = playlist.id,
            pic = navidromeApiClient.getImageUrl(ApiConstants.NAVIDROME_IMAGE_PREFIX_PLAYLIST + playlist.id),
            name = playlist.name,
            connectionId = connectionConfigServer.getConnectionId(),
            ifFavorite = false,
            ifPlaylist = true,
            musicCount = playlist.songCount
        )
    }

    /**
     * 将ArtistItem转换成XyArtist
     */
    fun convertToArtistList(
        item: List<ArtistItem>
    ): List<XyArtist> {
        val artistList = item.mapIndexed { index, artist ->
            convertToArtist(artist, index)
        }
        return artistList
    }

    /**
     * 将ArtistID3转换成XyArtist
     */
    fun convertToArtist(
        artist: ArtistItem,
        indexNumber: Int,
    ): XyArtist {
        val transliterator = Transliterator.getInstance("Han-Latin")
        val result =
            if (artist.orderArtistName.isBlank()) null else transliterator.transliterate(
                artist.orderArtistName
            )
        val shortNameStart = if (!result.isNullOrBlank()) result[0] else '#'
        val selectChat =
            if (!CharUtils.isEnglishLetter(shortNameStart)) "#" else shortNameStart.toString()
                .lowercase()
        return XyArtist(
            artistId = artist.id,
            pic = artist.smallImageUrl,
            name = artist.name,
            connectionId = connectionConfigServer.getConnectionId(),
            selectChat = selectChat,
            ifFavorite = artist.starred ?: false,
            indexNumber = indexNumber
        )
    }

    /**
     * 将AlbumID3转换成XyAlbum
     */
    fun convertToAlbumList(item: List<AlbumItem>, ifPlaylist: Boolean = false): List<XyAlbum> {
        return item.map { album ->
            convertToAlbum(album, ifPlaylist)
        }
    }

    /**
     * 将AlbumID3转换成XyAlbum
     */
    fun convertToAlbum(album: AlbumItem, ifPlaylist: Boolean = false): XyAlbum {
        return XyAlbum(
            itemId = album.id,
            pic = navidromeApiClient.getImageUrl(
                ApiConstants.NAVIDROME_IMAGE_PREFIX_ALBUM + album.id
            ),
            name = album.name,
            connectionId = connectionConfigServer.getConnectionId(),
            artists = album.albumArtist,
            artistIds = album.albumArtistId,
            ifPlaylist = ifPlaylist,
            musicCount = album.songCount,
            premiereDate = album.date?.replace("-", "")?.toLong() ?: 0,
            year = album.maxYear,
            ifFavorite = album.starred ?: false,
            genreIds = album.genres?.joinToString(Constants.ARTIST_DELIMITER) { it.id }
        )
    }


    /**
     * 将SongID3转换成XyMusic
     */
    fun convertToMusicList(item: List<SongItem>, isPlaylistMusic: Boolean): List<XyMusic> {
        return item.map { music ->
            convertToMusic(
                music,
                isPlaylistMusic
            )
        }
    }

    /**
     * 将SongID3转换成XyMusic
     */
    fun convertToMusic(music: SongItem, isPlaylistMusic: Boolean): XyMusic {
        return XyMusic(
            itemId = music.id,
            pic = navidromeApiClient.getImageUrl(
                ApiConstants.NAVIDROME_IMAGE_PREFIX_MUSIC + music.id
            ),
            name = music.title,
            musicUrl = if (isPlaylistMusic) music.mediaFileId?.let {
                navidromeApiClient.createAudioUrl(
                    it
                )
            } ?: "" else navidromeApiClient.createAudioUrl(music.id),
            album = music.albumId,
            albumName = music.album,
            genreIds = music.genre,
            connectionId = connectionConfigServer.getConnectionId(),
            artists = music.artist,
            artistIds = music.artistId,
            albumArtist = music.artist,
            albumArtistIds = music.artistId,
            year = music.year,
            playedCount = 0,
            ifFavoriteStatus = music.starred == true,
            path = music.path,
            bitRate = music.bitRate * 1000,
            sampleRate = music.sampleRate,
            bitDepth = music.bitDepth,
            size = music.size,
            runTimeTicks = music.duration.toLong() * 1000,
            container = music.suffix,
            codec = music.suffix,
            ifLyric = !music.lyrics.isNullOrBlank(),
            lyric = music.lyrics,
            playlistItemId = music.id
        )
    }

    /**
     * 将Genre转换成XyGenre
     */
    fun convertToGenreList(genres: List<Genre>): List<XyGenre> {
        return genres.map {
            convertToGenre(it)
        }
    }

    /**
     * 将Genre转换成XyGenre
     */
    fun convertToGenre(genre: Genre): XyGenre {
        return XyGenre(
            itemId = genre.id,
            pic = "",
            name = genre.name,
            connectionId = connectionConfigServer.getConnectionId()
        )
    }

}