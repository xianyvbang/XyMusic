package cn.xybbz.api.client.subsonic

import android.content.Context
import android.icu.text.Transliterator
import android.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.room.withTransaction
import cn.xybbz.api.TokenServer
import cn.xybbz.api.client.IDataSourceParentServer
import cn.xybbz.api.client.data.AllResponse
import cn.xybbz.api.client.jellyfin.data.ClientLoginInfoReq
import cn.xybbz.api.client.subsonic.data.AlbumID3
import cn.xybbz.api.client.subsonic.data.ArtistID3
import cn.xybbz.api.client.subsonic.data.GenreID3
import cn.xybbz.api.client.subsonic.data.PlaylistID3
import cn.xybbz.api.client.subsonic.data.ScrobbleRequest
import cn.xybbz.api.client.subsonic.data.SongID3
import cn.xybbz.api.client.subsonic.data.SubsonicArtistsResponse
import cn.xybbz.api.client.subsonic.data.SubsonicResponse
import cn.xybbz.api.enums.jellyfin.CollectionType
import cn.xybbz.api.enums.subsonic.AlbumType
import cn.xybbz.api.enums.subsonic.Status
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.constants.Constants.ALBUM_MUSIC_LIST_PAGE_SIZE
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.common.enums.SortTypeEnum
import cn.xybbz.common.utils.CharUtils
import cn.xybbz.common.utils.PasswordUtils
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
import cn.xybbz.page.subsonic.AlbumRemoteMediator
import cn.xybbz.page.subsonic.FavoriteMusicRemoteMediator
import cn.xybbz.page.subsonic.SubsonicAlbumOrPlaylistMusicListRemoteMediator
import cn.xybbz.page.subsonic.SubsonicGenreAlbumListRemoteMediator
import cn.xybbz.page.subsonic.SubsonicGenresRemoteMediator
import cn.xybbz.ui.components.LrcEntry
import kotlinx.coroutines.flow.Flow
import okhttp3.OkHttpClient

@androidx.annotation.OptIn(UnstableApi::class)
class SubsonicDatasourceServer(
    private val db: DatabaseClient,
    private val application: Context,
    private val connectionConfigServer: ConnectionConfigServer,
    private val subsonicApiClient: SubsonicApiClient
) : IDataSourceParentServer(
    db,
    connectionConfigServer,
    application
) {
    /**
     * 获得当前数据源类型
     */
    override fun getDataSourceType(): DataSourceType {
        return DataSourceType.SUBSONIC
    }

    /**
     * 登录功能
     */
    override suspend fun login(clientLoginInfoReq: ClientLoginInfoReq): LoginSuccessData {
        return LoginSuccessData(
            userId = clientLoginInfoReq.username,
            accessToken = "",
            serverId = "",
            serverName = getDataSourceType().title,
            version = subsonicApiClient.protocolVersion
        )
    }

    /**
     * 连通性检测
     */
    override suspend fun postPingSystem(): Boolean {
        return try {
            val pingData = subsonicApiClient.userApi().postPingSystem()
            Log.i("=====", "ping数据返回: $pingData")
            pingData.subsonicResponse.status == Status.Ok
        } catch (e: Exception) {
            throw e
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
        val packageManager = application.packageManager
        val packageName = application.packageName
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
        val appName = packageManager.getApplicationLabel(applicationInfo).toString()
        val versionName = packageInfo.versionName
        val versionCode = packageInfo.longVersionCode

        val encryptMd5 = PasswordUtils.encryptMd5(password)

        subsonicApiClient.createApiClient(
            username, encryptMd5.passwordMd5, encryptMd5.encryptedSalt, getDataSourceType().version,
            "${appName}:${versionName}.${versionCode}"
        )
        setToken()
        subsonicApiClient.setRetrofitData(address)
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
            subsonicApiClient.artistsApi().getArtists(connectionConfigServer.libraryId)
        val artists = convertIndexToArtistList(response, false)
        return AllResponse(
            items = artists,
            totalRecordCount = artists.size,
            startIndex = 0
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
                initialLoadSize = Constants.UI_INIT_LIST_PAGE,
                prefetchDistance = Constants.UI_PREFETCH_DISTANCE,
            ),
            remoteMediator = AlbumRemoteMediator(
                sortType = sortType,
                ifFavorite = ifFavorite,
                years = years,
                dataSource = getDataSourceType(),
                db = db,
                subsonicDatasourceServer = this,
                connectionId = connectionConfigServer.getConnectionId()
            )
        ) {
            db.albumDao.selectHomeAlbumListPage()
        }.flow
    }

    /**
     * 获得音乐列表数据 Subsonic没办法一次性获得所有音乐
     */
    override fun selectMusicFlowList(
        sortType: SortTypeEnum?,
        ifFavorite: Boolean?,
        years: String?
    ): Flow<PagingData<XyMusic>> {
        return Pager(
            config = PagingConfig(
                pageSize = Constants.UI_LIST_PAGE,  // 每一页个数
                initialLoadSize = Constants.UI_INIT_LIST_PAGE,
                prefetchDistance = Constants.UI_PREFETCH_DISTANCE,
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
        val searchData = SearchData()
        try {
            val search3 =
                subsonicApiClient.itemApi().search3(
                    query = search,
                    musicFolderId = connectionConfigServer.libraryId
                )
            if (search3.subsonicResponse.status == Status.Ok) {
                search3.subsonicResponse.searchResult3?.let { search ->
                    searchData.artists = search.artist?.let {
                        val artistList = convertToArtistList(it, false)
                        saveBatchArtist(artistList)
                        artistList
                    }
                    searchData.albums = search.album?.let {
                        val albumList = convertToAlbumList(it)
                        db.albumDao.saveDataBatch(albumList)
                        albumList
                    }
                    searchData.musics = search.song?.let {
                        val musicList = convertToMusicList(it)
                        db.musicDao.saveDataBatch(musicList)
                        musicList
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "搜索失败", e)
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
                initialLoadSize = Constants.UI_INIT_LIST_PAGE,
                prefetchDistance = Constants.UI_PREFETCH_DISTANCE,
            ),
            remoteMediator = SubsonicAlbumOrPlaylistMusicListRemoteMediator(
                itemId = itemId,
                subsonicDatasourceServer = this,
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
     * 获得专辑,艺术家,音频,歌单数量
     */
    override suspend fun getDataInfoCount(connectionId: Long) {

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
    override suspend fun selectAlbumInfoById(
        albumId: String,
        dataType: MusicDataTypeEnum
    ): XyAlbum? {
        var artistExtend = db.albumDao.selectById(albumId)
        if (artistExtend == null) {
            if (dataType == MusicDataTypeEnum.ALBUM) {
                val album = subsonicApiClient.itemApi().getAlbum(albumId)
                //存储歌曲数据
                album.subsonicResponse.album?.song?.let {

                    val albumMusicList = convertToMusicList(it)
                    db.musicDao.saveBatch(
                        albumMusicList, dataType,
                        connectionId = connectionConfigServer.getConnectionId(),
                    )
                }
                artistExtend = album.subsonicResponse.album?.let { convertToAlbum(it) }
            } else {
                val playlist = subsonicApiClient.playlistsApi()
                    .getPlaylistById(albumId.replace(Constants.SUBSONIC_PLAYLIST_SUFFIX, ""))
                //存储歌曲数据
                playlist.subsonicResponse.playlist?.entry?.let {
                    val albumMusicList = convertToMusicList(it)
                    db.musicDao.saveBatch(
                        albumMusicList, dataType,
                        connectionId = connectionConfigServer.getConnectionId(),
                        playlistId = albumId
                    )
                }

                artistExtend =
                    playlist.subsonicResponse.playlist?.let { convertToPlaylist(it) }
            }
        }
        return artistExtend
    }

    /**
     * 获得专辑或歌单的音乐列表
     */
    suspend fun getMusicListByAlbumOrPlaylist(
        itemId: String,
        dataType: MusicDataTypeEnum
    ): List<XyMusic>? {
        if (dataType == MusicDataTypeEnum.ALBUM) {
            val album = subsonicApiClient.itemApi().getAlbum(itemId)
            //存储歌曲数据
            return album.subsonicResponse.album?.song?.let {

                convertToMusicList(it)
            }
        } else {
            val playlist = subsonicApiClient.playlistsApi()
                .getPlaylistById(itemId.replace(Constants.SUBSONIC_PLAYLIST_SUFFIX, ""))
            //存储歌曲数据
            return playlist.subsonicResponse.playlist?.entry?.let {
                convertToMusicList(it)
            }
        }
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
        return null
    }


    /**
     * 根据艺术家获得专辑列表
     */
    override fun selectAlbumListByArtistId(artistId: String): Flow<PagingData<XyAlbum>> {
        return Pager(
            config = PagingConfig(
                pageSize = ALBUM_MUSIC_LIST_PAGE_SIZE,  // 每一页个数
                initialLoadSize = Constants.UI_INIT_LIST_PAGE,
                prefetchDistance = Constants.UI_PREFETCH_DISTANCE,
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
            config = PagingConfig(
                pageSize = ALBUM_MUSIC_LIST_PAGE_SIZE,  // 每一页个数
                initialLoadSize = Constants.UI_INIT_LIST_PAGE,
                prefetchDistance = Constants.UI_PREFETCH_DISTANCE,
            ),
            remoteMediator = ArtistMusicListRemoteMediator(
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
        //获得艺术家专辑列表
        val albumIds = db.albumDao.selectListByArtistId(artistId)
        val musicList = mutableListOf<XyMusic>()
        if (albumIds.isNotEmpty()) {
            albumIds.forEach { albumId ->
                val album = subsonicApiClient.itemApi().getAlbum(albumId)
                album.subsonicResponse.album?.song?.let { musics ->
                    val musicInfos = convertToMusicList(musics)
                    musicList.addAll(musicInfos)
                }

            }
        }
        return AllResponse<XyMusic>(
            items = musicList,
            totalRecordCount = musicList.size,
            startIndex = 0
        )
    }

    /**
     * 获得随机音乐
     */
    override suspend fun getRandomMusicList(pageSize: Int, pageNum: Int): List<XyMusic>? {
        val randomSongs = subsonicApiClient.itemApi().getRandomSongs(
            size = pageSize,
            musicFolderId = connectionConfigServer.libraryId
        )
        return randomSongs.subsonicResponse.randomSongs?.song?.let {
            convertToMusicList(
                it
            )
        }
    }

    /**
     * 获取歌单列表
     */
    override suspend fun getPlaylists(): List<XyAlbum>? {
        db.albumDao.removePlaylist()
        return connectionConfigServer.connectionConfig?.username?.let { username ->
            val playlists = subsonicApiClient.playlistsApi().getPlaylists(username)
            playlists.subsonicResponse.playlists?.playlist?.let { playlist ->
                saveBatchAlbum(convertToPlaylists(playlist), MusicDataTypeEnum.PLAYLIST, true)
            }
        }
    }

    /**
     * 增加歌单
     * @param [name] 名称
     * @return [String?] 歌单id
     */
    override suspend fun createPlaylist(name: String): String? {
        val id = subsonicApiClient.playlistsApi()
            .createPlaylist(name = name).subsonicResponse.playlist?.id
        return id?.let { it + Constants.SUBSONIC_PLAYLIST_SUFFIX }
    }

    /**
     * 新增或修改歌单
     */
    override suspend fun importPlaylist(playlistData: ExportPlaylistData): Boolean {
        val playlists = playlistData.playlist
        val playlistMusicMap = playlistData.playlistMusic.groupBy { it.playlistId }
        if (playlists.isNotEmpty()) {

            connectionConfigServer.connectionConfig?.username?.let { username ->
                val playlistServerList = subsonicApiClient.playlistsApi().getPlaylists(username)
                //获得新建的表单
                val itemDataList = playlistServerList.subsonicResponse.playlists?.playlist
                if (!itemDataList.isNullOrEmpty()) {
                    val playlistMap = itemDataList.associateBy { it.id }
                    playlists.forEach {
                        if (playlistMap.containsKey(it.itemId)) {
                            val playlistMusics = playlistMusicMap[it.itemId]
                            if (!playlistMusics.isNullOrEmpty()) {
                                //更新音乐数据
                                saveMusicPlaylist(
                                    playlistId = it.itemId + Constants.SUBSONIC_PLAYLIST_SUFFIX,
                                    musicIds = playlistMusics.map { it.musicId },
                                    pic = it.pic
                                )
                            }
                        } else {
                            playlists.forEach {
                                val playlistId =
                                    subsonicApiClient.playlistsApi().createPlaylist(
                                        name = it.name
                                    ).subsonicResponse.playlist?.id
                                val playlistMusics = playlistMusicMap[it.itemId]
                                if (!playlistMusics.isNullOrEmpty() && !playlistId.isNullOrBlank()) {
                                    saveMusicPlaylist(
                                        playlistId = playlistId + Constants.SUBSONIC_PLAYLIST_SUFFIX,
                                        musicIds = playlistMusics.map { it.musicId },
                                        pic = it.pic
                                    )
                                }
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
        subsonicApiClient.playlistsApi().updatePlaylist(
            playlistId = id.replace(Constants.SUBSONIC_PLAYLIST_SUFFIX, ""),
            name = name
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
        subsonicApiClient.playlistsApi().deletePlaylist(
            id = id.replace(Constants.SUBSONIC_PLAYLIST_SUFFIX, "")
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
        subsonicApiClient.playlistsApi().updatePlaylist(
            playlistId = playlistId.replace(Constants.SUBSONIC_PLAYLIST_SUFFIX, ""),
            songIdToAdd = musicIds
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
        val musicIndexList =
            db.musicDao.selectMusicIndexByPlaylistId(playlistId, musicIds).map { it.toString() }

        subsonicApiClient.playlistsApi().updatePlaylist(
            playlistId = playlistId.replace(Constants.SUBSONIC_PLAYLIST_SUFFIX, ""),
            songIndexToRemove = musicIndexList
        )
        db.musicDao.removeByPlaylistMusicByIndex(
            playlistId = playlistId,
            musicIndex = musicIndexList
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
        val artist = subsonicApiClient.artistsApi().getArtist(artistIds[0])
        return artist.subsonicResponse.artist?.let { convertToArtistList(listOf(it), false) }
            ?: emptyList()
    }

    /**
     * 根据id获得艺术家信息
     * @param [artistId] 艺术家id
     * @return [List<ArtistItem>?] 艺术家信息
     */
    override suspend fun selectArtistInfoById(artistId: String): XyArtist? {
        var artistInfo: XyArtist? = db.artistDao.selectById(artistId)
        if (artistInfo == null) {
            val artist = subsonicApiClient.artistsApi().getArtist(artistId)
            //专辑转换
            artistInfo = artist.subsonicResponse.artist?.album?.let { albums ->
                val albumList = convertToAlbumList(albums)
                //存储到数据库中
                if (albumList.isNotEmpty())
                    db.albumDao.saveBatch(
                        albumList,
                        dataType = MusicDataTypeEnum.ARTIST,
                        connectionId = connectionConfigServer.getConnectionId(),
                        artistId
                    )

                artist.subsonicResponse.artist?.let {
                    convertToArtist(
                        it,
                        false,
                        indexNumber = 0
                    )
                }
            }
        }

        return artistInfo

    }

    /**
     * 获得媒体库列表
     */
    override suspend fun selectMediaLibrary() {
        try {
            db.withTransaction {
                db.libraryDao.remove()
                val musicFolders = subsonicApiClient.userViewsApi().getMusicFolders()
                //存储历史记录
                val libraries =
                    musicFolders.subsonicResponse.musicFolders?.musicFolders?.map {
                        XyLibrary(
                            id = it.id,
                            collectionType = CollectionType.MUSIC.serialName,
                            name = it.name.toString(),
                            connectionId = connectionConfigServer.getConnectionId()
                        )
                    }
                if (!libraries.isNullOrEmpty()) {
                    db.libraryDao.saveBatch(libraries)
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 获得最近播放音乐或专辑
     */
    override suspend fun playRecordMusicOrAlbumList() {
        //subsonic只有最近播放专辑
        //插入最新播放专辑
        val albumList = subsonicApiClient.itemApi().getAlbumList2(
            type = AlbumType.RECENT,
            size = Constants.MIN_PAGE,
            offset = 0,
            musicFolderId = connectionConfigServer.libraryId
        ).subsonicResponse.albumList2?.album
        if (!albumList.isNullOrEmpty()) {
            db.withTransaction {
                db.albumDao.removeByType(MusicDataTypeEnum.PLAY_HISTORY)
                val toAlbumList = convertToAlbumList(albumList)
                saveBatchAlbum(toAlbumList, MusicDataTypeEnum.PLAY_HISTORY)
            }
        }
    }

    /**
     * 获得最多播放
     */
    override suspend fun getMostPlayerMusicList() {
        val albumList = subsonicApiClient.itemApi().getAlbumList2(
            type = AlbumType.FREQUENT,
            size = Constants.MIN_PAGE,
            offset = 0,
            musicFolderId = connectionConfigServer.libraryId
        ).subsonicResponse.albumList2?.album
        if (!albumList.isNullOrEmpty()) {
            db.withTransaction {
                db.albumDao.removeByType(MusicDataTypeEnum.MAXIMUM_PLAY)
                val toAlbumList = convertToAlbumList(albumList)
                saveBatchAlbum(toAlbumList, MusicDataTypeEnum.MAXIMUM_PLAY)
            }
        }
    }

    /**
     * 获得最新专辑
     */
    override suspend fun getNewestAlbumList() {
        val albumList = subsonicApiClient.itemApi().getAlbumList2(
            type = AlbumType.NEWEST,
            size = Constants.MIN_PAGE,
            offset = 0,
            musicFolderId = connectionConfigServer.libraryId
        ).subsonicResponse.albumList2?.album
        if (!albumList.isNullOrEmpty()) {
            db.withTransaction {
                db.albumDao.removeByType(MusicDataTypeEnum.NEWEST)
                val toAlbumList = convertToAlbumList(albumList)
                saveBatchAlbum(toAlbumList, MusicDataTypeEnum.NEWEST)
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
                initialLoadSize = Constants.UI_INIT_LIST_PAGE,
                prefetchDistance = Constants.UI_PREFETCH_DISTANCE,
            ),
            remoteMediator = FavoriteMusicRemoteMediator(
                subsonicDatasourceServer = this,
                db = db,
                connectionId = connectionConfigServer.getConnectionId()
            )
        ) {
            db.musicDao.selectFavoriteMusicListPage()
        }.flow
    }

    /**
     * 获得所有收藏数据
     */
    override suspend fun initFavoriteData() {
        try {
            val starred2 = subsonicApiClient.itemApi().getStarred2(
                musicFolderId = connectionConfigServer.libraryId
            )
            starred2.subsonicResponse.starred2?.album?.let { albums ->
                val albumList = convertToAlbumList(albums)
                saveBatchAlbum(albumList, MusicDataTypeEnum.FAVORITE)
            }
            starred2.subsonicResponse.starred2?.artist?.let { artists ->
                val artistList = convertToArtistList(artists, true)
                artistList.forEach {
                    val artist = db.artistDao.selectById(it.artistId)
                    if (artist == null) {
                        db.artistDao.save(it)
                    } else {
                        db.artistDao.update(
                            artist.copy(
                                name = it.name,
                                pic = it.pic,
                                describe = it.describe,
                                sortName = it.sortName,
                                musicCount = it.musicCount,
                                albumCount = it.albumCount,
                                ifFavorite = true
                            )
                        )
                    }
                }
            }
            starred2.subsonicResponse.starred2?.song?.let { songs ->
                val musicList = convertToMusicList(songs)
                saveBatchMusic(musicList, dataType = MusicDataTypeEnum.FAVORITE)
            }
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "获得收藏数据失败",e)
        }
    }

    /**
     * 获得收藏的音乐
     */
    suspend fun getMusicFavoriteData(): List<XyMusic>? {
        val starred2 = subsonicApiClient.itemApi().getStarred2(
            musicFolderId = connectionConfigServer.libraryId
        )
        return starred2.subsonicResponse.starred2?.song?.let { songs ->
            convertToMusicList(songs)
        }
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
            ), remoteMediator = SubsonicGenresRemoteMediator(
                subsonicDatasourceServer = this,
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
                initialLoadSize = Constants.UI_INIT_LIST_PAGE,
                prefetchDistance = Constants.UI_PREFETCH_DISTANCE,
            ),
            remoteMediator = SubsonicGenreAlbumListRemoteMediator(
                genreId = genreId,
                subsonicDatasourceServer = this,
                db = db,
                connectionId = connectionConfigServer.getConnectionId()
            )
        ) {
            db.albumDao.selectGenreAlbumListPage(genreId)
        }.flow
    }

    /**
     * 获得OkHttpClient
     */
    override fun getOkhttpClient(): OkHttpClient {
        return subsonicApiClient.apiOkHttpClient
    }

    /**
     * 设置token
     */
    override fun setToken() {
        TokenServer.setQueryMapData(subsonicApiClient.getQueryMapData())
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
        subsonicApiClient.userApi().scrobble(
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
        return subsonicApiClient.createAudioUrl(musicId)
    }

    /**
     * 释放
     */
    override suspend fun release() {
        super.release()
        subsonicApiClient.release()
    }

    fun convertIndexToArtistList(
        response: SubsonicResponse<SubsonicArtistsResponse>,
        ifFavorite: Boolean,
    ): List<XyArtist> {
        return if (response.subsonicResponse.status == Status.Ok) {
            response.subsonicResponse.artists?.index?.flatMap { index ->
                convertToArtistList(index.artist, ifFavorite, index.name)
            }?.sortedBy { it.indexNumber }
                ?.mapIndexed { indexNumber, artist -> artist.copy(indexNumber = indexNumber) }
                ?: emptyList()
        } else {
            emptyList()
        }
    }

    /**
     * 将ArtistID3转换成XyArtist
     */
    fun convertToArtistList(
        item: List<ArtistID3>,
        ifFavorite: Boolean,
        index: String? = null
    ): List<XyArtist> {
        val transliterator = Transliterator.getInstance("Han-Latin")
        val artistList = item.map { artist ->
            val result = if (index.isNullOrBlank()) null else transliterator.transliterate(index)
            val shortNameStart = if (!result.isNullOrBlank()) result[0] else '#'
            val selectChat =
                if (!CharUtils.isEnglishLetter(shortNameStart)) "#" else shortNameStart.toString()
                    .lowercase()

            convertToArtist(artist, ifFavorite, selectChat, 0)
        }

        return artistList
    }


    /**
     * 将ArtistID3转换成XyArtist
     */
    fun convertToArtist(
        artistId3: ArtistID3,
        ifFavorite: Boolean,
        index: String? = null,
        indexNumber: Int,
    ): XyArtist {


        return XyArtist(
            artistId = artistId3.id,
            pic = if (artistId3.coverArt.isNullOrBlank()) null else artistId3.coverArt?.let { coverArt ->
                subsonicApiClient.getImageUrl(
                    coverArt
                )
            },
            name = artistId3.name,
            connectionId = connectionConfigServer.getConnectionId(),
            selectChat = index ?: "",
            ifFavorite = ifFavorite,
            indexNumber = indexNumber
        )
    }

    /**
     * 将AlbumID3转换成XyAlbum
     */
    fun convertToAlbumList(item: List<AlbumID3>, ifPlaylist: Boolean = false): List<XyAlbum> {
        return item.map { album ->
            convertToAlbum(album, ifPlaylist)
        }
    }

    /**
     * 将AlbumID3转换成XyAlbum
     */
    fun convertToAlbum(album: AlbumID3, ifPlaylist: Boolean = false): XyAlbum {
        return XyAlbum(
            itemId = album.id,
            pic = if (album.coverArt.isNullOrBlank()) null else album.coverArt?.let {
                subsonicApiClient.getImageUrl(
                    it
                )
            },
            name = album.name,
            connectionId = connectionConfigServer.getConnectionId(),
            ifFavorite = false,
            artists = album.artist,
            artistIds = album.artistId,
            ifPlaylist = ifPlaylist,
            musicCount = album.songCount
        )
    }

    /**
     * 将PlaylistID3转换成XyAlbum
     */
    fun convertToPlaylists(playlists: List<PlaylistID3>): List<XyAlbum> {
        return playlists.map { playlist ->
            convertToPlaylist(playlist)
        }
    }

    /**
     * 将PlaylistID3转换成XyAlbum
     */
    fun convertToPlaylist(playlist: PlaylistID3): XyAlbum {
        return XyAlbum(
            itemId = playlist.id + Constants.SUBSONIC_PLAYLIST_SUFFIX,
            pic = if (playlist.coverArt.isNotBlank()) subsonicApiClient.getImageUrl(playlist.coverArt) else null,
            name = playlist.name,
            connectionId = connectionConfigServer.getConnectionId(),
            ifFavorite = false,
            ifPlaylist = true,
            musicCount = playlist.songCount
        )
    }

    /**
     * 将SongID3转换成XyMusic
     */
    fun convertToMusicList(item: List<SongID3>): List<XyMusic> {
        return item.map { music ->
            convertToMusic(
                music,
                music.starred != null
            )
        }
    }

    /**
     * 将SongID3转换成XyMusic
     */
    fun convertToMusic(music: SongID3, ifFavorite: Boolean): XyMusic {
        return XyMusic(
            itemId = music.id,
            pic = if (music.coverArt.isNullOrBlank()) null else music.coverArt?.let {
                subsonicApiClient.getImageUrl(
                    it
                )
            },
            name = music.title,
            musicUrl = subsonicApiClient.createAudioUrl(music.id),
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
            ifFavoriteStatus = ifFavorite,
            path = music.path,
            bitRate = music.bitRate,
            sampleRate = 0,
            bitDepth = 0,
            size = music.size,
            runTimeTicks = music.duration,
            container = music.suffix,
            codec = music.suffix,
            ifLyric = false,
            lyric = ""
        )
    }

    /**
     * 将Genre转换成XyGenre
     */
    fun convertToGenreList(genres: List<GenreID3>): List<XyGenre> {
        return genres.map {
            convertToGenre(it)
        }
    }

    /**
     * 将Genre转换成XyGenre
     */
    fun convertToGenre(genre: GenreID3): XyGenre {
        return XyGenre(
            itemId = genre.value,
            pic = "",
            name = genre.value,
            connectionId = connectionConfigServer.getConnectionId()
        )
    }

    /**
     * 获得专辑列表
     */
    suspend fun getAlbumList(
        pageSize: Int,
        startIndex: Int,
        type: AlbumType = AlbumType.NEWEST,
        isFavorite: Boolean? = null,
        years: List<Int>? = null,
        genreId: String? = null
    ): SubsonicResponse<List<XyAlbum>?> {

        var alphabeticalByName = type
        if (isFavorite == true) {
            alphabeticalByName = AlbumType.STARRED
        } else if (!years.isNullOrEmpty()) {
            alphabeticalByName = AlbumType.BY_YEAR
        } else if (!genreId.isNullOrBlank()) {
            alphabeticalByName = AlbumType.BY_GENRE
        }

        val albumList = subsonicApiClient.itemApi().getAlbumList2(
            type = alphabeticalByName,
            size = pageSize,
            offset = startIndex,
            fromYear = years?.get(0),
            toYear = years?.get(years.size - 1),
            genre = genreId,
            musicFolderId = connectionConfigServer.libraryId
        )
        return SubsonicResponse<List<XyAlbum>?>(albumList.subsonicResponse.albumList2?.album?.let {
            convertToAlbumList(it)
        })
    }


    /**
     * 获得流派列表
     * @return [Response<ItemResponse>]
     */
    suspend fun getGenreList(): List<XyGenre>? {
        val genres = subsonicApiClient.genreApi().getGenres()
        return genres.subsonicResponse.genres?.genre?.let { convertToGenreList(it) }
    }


    /**
     * 将项目标记为收藏
     * @param [itemId] 专辑/音乐id
     */
    override suspend fun markFavoriteItem(itemId: String, dataType: MusicTypeEnum): Boolean {
        val favorite = subsonicApiClient.userLibraryApi()
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
        val favorite = subsonicApiClient.userLibraryApi().unmarkFavoriteItem(
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
     * 获得歌曲列表
     */
    override suspend fun getMusicList(
        pageSize: Int,
        pageNum: Int
    ): List<XyMusic>? {
        return null
    }

    /**
     * 根据专辑获得歌曲列表
     */
    override suspend fun getMusicListByAlbumId(
        albumId: String,
        pageSize: Int,
        pageNum: Int
    ): List<XyMusic>? {
        return db.musicDao.selectMusicListByAlbumId(albumId, pageSize, pageNum * pageSize)
    }

    /**
     * 根据艺术家获得歌曲列表
     */
    override suspend fun getMusicListByArtistId(
        artistId: String,
        pageSize: Int,
        pageNum: Int
    ): List<XyMusic>? {
        return db.musicDao.selectMusicListByArtistId(artistId, pageSize, pageNum * pageSize)
    }

    /**
     * 获得收藏歌曲列表
     */
    override suspend fun getMusicListByFavorite(
        pageSize: Int,
        pageNum: Int
    ): List<XyMusic>? {
        return db.musicDao.selectMusicListByFavorite(pageSize, pageNum * pageSize)
    }
}