package cn.xybbz.api.client.plex

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.paging.PagingData
import androidx.room.withTransaction
import cn.xybbz.api.TokenServer
import cn.xybbz.api.client.IDataSourceParentServer
import cn.xybbz.api.client.data.AllResponse
import cn.xybbz.api.client.jellyfin.data.ClientLoginInfoReq
import cn.xybbz.api.client.plex.data.ImageType
import cn.xybbz.api.client.plex.data.Metadatum
import cn.xybbz.api.client.plex.data.PlaylistMetadatum
import cn.xybbz.api.client.plex.data.toPlexLogin
import cn.xybbz.api.enums.jellyfin.CollectionType
import cn.xybbz.api.enums.plex.PlexListType
import cn.xybbz.api.enums.plex.PlexSortOrder
import cn.xybbz.api.enums.plex.PlexSortType
import cn.xybbz.api.exception.ConnectionException
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.common.enums.SortTypeEnum
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
import cn.xybbz.ui.components.LrcEntry
import kotlinx.coroutines.flow.Flow
import okhttp3.OkHttpClient

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
        //todo 在这里调用postPingSystem 因为plex的ping服务器需要token
        //todo login改成只ping
        postPingSystem()
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
        val responseData =
            plexApiClient.userApi().authenticateByName(
                "https://plex.tv/api/v2/users/signin",
                clientLoginInfoReq.toPlexLogin()
            )
        Log.i("=====", "返回响应值: $responseData")
        plexApiClient.updateAccessToken(responseData.authToken)
        plexApiClient.updateServerInfo(userId = responseData.id)
        setToken()
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
                    "http://${connection.address}:${connection.port}"
                )
                tmpResourceData.add(ipv4Data)

                if (connection.iPv6 != null) {
                    val ipv6Data = ResourceData(
                        infoResponse.name,
                        infoResponse.product,
                        "http://[${connection.address}]:${connection.port}"
                    )
                    tmpResourceData.add(ipv6Data)
                }

                val resourceData = ResourceData(
                    name = infoResponse.name,
                    infoResponse.product,
                    connection.uri
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
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }

    /**
     * 根据艺术家id获得专辑列表
     */
    override suspend fun selectArtistsByIds(artistIds: List<String>): List<XyArtist> {
        TODO("Not yet implemented")
    }

    /**
     * 根据艺术家获得音乐列表
     */
    override suspend fun selectMusicListByArtistServer(
        artistId: String,
        pageSize: Int,
        startIndex: Int
    ): AllResponse<XyMusic> {
        TODO("Not yet implemented")
    }

    /**
     * 获得专辑列表数据
     */
    override fun selectAlbumFlowList(
        sortType: SortTypeEnum?,
        ifFavorite: Boolean?,
        years: String?
    ): Flow<PagingData<XyAlbum>> {
        TODO("Not yet implemented")
    }

    /**
     * 获得音乐列表数据
     */
    override fun selectMusicFlowList(
        sortType: SortTypeEnum?,
        ifFavorite: Boolean?,
        years: String?
    ): Flow<PagingData<XyMusic>> {
        TODO("Not yet implemented")
    }

    /**
     * 获得艺术家
     */
    override fun selectArtistFlowList(
        ifFavorite: Boolean?,
        selectChat: String?
    ): Flow<PagingData<XyArtist>> {
        TODO("Not yet implemented")
    }

    /**
     * 搜索音乐,艺术家,专辑
     */
    override suspend fun searchAll(search: String): SearchData {
        TODO("Not yet implemented")
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
    override fun selectMusicListByParentId(
        sortType: SortTypeEnum?,
        ifFavorite: Boolean?,
        years: String?,
        itemId: String,
        dataType: MusicDataTypeEnum
    ): Flow<PagingData<XyMusic>> {
        TODO("Not yet implemented")
    }

    /**
     * 将项目标记为收藏
     * @param [itemId] 专辑/音乐id
     */
    override suspend fun markFavoriteItem(
        itemId: String,
        dataType: MusicTypeEnum
    ): Boolean {
        TODO("Not yet implemented")
    }

    /**
     * 取消项目收藏
     * @param [itemId] 专辑/音乐id
     */
    override suspend fun unmarkFavoriteItem(
        itemId: String,
        dataType: MusicTypeEnum
    ): Boolean {
        TODO("Not yet implemented")
    }

    /**
     * 获得专辑,艺术家,音频,歌单数量
     */
    override suspend fun getDataInfoCount(connectionId: Long) {
        TODO("Not yet implemented")
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
                viewLibrary.mediaContainer?.directory?.filter { it.type == CollectionType.MUSIC }
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
        TODO("Not yet implemented")
    }

    /**
     * 获得最多播放
     */
    override suspend fun getMostPlayerMusicList() {
        val musicList = getServerMusicList(
            plexListType = PlexListType.all,
            startIndex = 0,
            pageSize = Constants.MIN_PAGE,
            sortBy = PlexSortType.VIEWCOUNT,
            sortOrder = PlexSortOrder.DESCENDING,
            params = mapOf(Pair("viewCount>>0", ""))
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
        TODO("Not yet implemented")
    }

    /**
     * 获得收藏歌曲列表
     */
    override fun selectFavoriteMusicFlowList(): Flow<PagingData<XyMusic>> {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
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
        plexListType: PlexListType,
        startIndex: Int,
        pageSize: Int,
        search: String? = null,
        sortBy: PlexSortType,
        sortOrder: PlexSortOrder = PlexSortOrder.ASCENDING,
        params: Map<String, String>? = null
    ): AllResponse<XyMusic> {
        val response =
            plexApiClient.itemApi().getSongs(
                sectionKey = connectionConfigServer.libraryId!!,
                selectType = plexListType.toString(),
                start = startIndex,
                pageSize = pageSize,
                sort = "$sortBy:$sortOrder",
                title = search,
                params = params
            )
        return AllResponse(
            items = response.mediaContainer?.metadata?.let { convertToMusicList(it) },
            totalRecordCount = response.mediaContainer?.totalSize ?: 0,
            startIndex = startIndex
        )
    }


    /**
     * 将ItemResponse转换成XyAlbum
     */
    /*fun convertToAlbumList(item: List<ItemResponse>, ifPlaylist: Boolean = false): List<XyAlbum> {
        return item.map { album ->
            convertToAlbum(album, ifPlaylist)
        }
    }*/

    /**
     * 将ItemResponse转换成XyAlbum
     */
    /*fun convertToAlbum(album: ItemResponse, ifPlaylist: Boolean = false): XyAlbum {
        val itemImageUrl =
            if (!album.imageTags.isNullOrEmpty()) plexApiClient.createImageUrl(
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
                ?: if (ifPlaylist) Constants.UNKNOWN_PLAYLIST else Constants.UNKNOWN_ALBUM,
            connectionId = connectionConfigServer.getConnectionId(),
            artistIds = album.albumArtists?.joinToString(Constants.ARTIST_DELIMITER) { it.id },
            artists = album.albumArtists?.joinToString(Constants.ARTIST_DELIMITER) { it.name.toString() }
                ?: Constants.UNKNOWN_ARTIST,
            year = album.productionYear,
            premiereDate = album.premiereDate?.atZone(ZoneOffset.ofHours(8))?.toInstant()
                ?.toEpochMilli(),
            genreIds = album.genreItems?.joinToString(Constants.ARTIST_DELIMITER) { it.id },
            ifFavorite = album.userData?.isFavorite == true,
            ifPlaylist = ifPlaylist,
            createTime = album.dateCreated?.atZone(ZoneId.systemDefault())?.toEpochSecond() ?: 0L,
            musicCount = album.songCount?.toLong() ?: 0L
        )
    }*/


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


}