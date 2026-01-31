/*
 *   XyMusic
 *   Copyright (C) 2023 xianyvbang
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package cn.xybbz.api.client.jellyfin

import XyArtistInfo
import android.content.Context
import android.icu.math.BigDecimal
import android.os.Build
import android.util.Log
import androidx.room.withTransaction
import cn.xybbz.R
import cn.xybbz.api.client.IDataSourceParentServer
import cn.xybbz.api.client.data.XyResponse
import cn.xybbz.api.client.jellyfin.data.CreatePlaylistRequest
import cn.xybbz.api.client.jellyfin.data.ItemRequest
import cn.xybbz.api.client.jellyfin.data.ItemResponse
import cn.xybbz.api.client.jellyfin.data.PlaybackStartInfo
import cn.xybbz.api.client.jellyfin.data.PlaylistUserPermissions
import cn.xybbz.api.client.jellyfin.data.ViewRequest
import cn.xybbz.api.dispatchs.MediaLibraryAndFavoriteSyncScheduler
import cn.xybbz.api.enums.AudioCodecEnum
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
import cn.xybbz.common.constants.Constants.LYRICS_AMPLIFICATION
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.common.enums.SortTypeEnum
import cn.xybbz.common.utils.CharUtils
import cn.xybbz.common.utils.DateUtil.toSecondMs
import cn.xybbz.common.utils.PlaylistParser
import cn.xybbz.config.download.DownLoadManager
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.entity.data.LrcEntryData
import cn.xybbz.entity.data.SearchAndOrder
import cn.xybbz.entity.data.SearchData
import cn.xybbz.entity.data.ext.joinToString
import cn.xybbz.entity.data.toSearchAndOrder
import cn.xybbz.localdata.common.LocalConstants
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.data.artist.XyArtist
import cn.xybbz.localdata.data.genre.XyGenre
import cn.xybbz.localdata.data.library.XyLibrary
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.data.music.XyMusicExtend
import cn.xybbz.localdata.data.music.XyPlayMusic
import cn.xybbz.localdata.enums.DataSourceType
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import okhttp3.OkHttpClient
import java.net.SocketTimeoutException

/**
 * Jellyfin api客户端管理
 * @author xybbz
 * @date 2024/06/12
 */
class JellyfinDatasourceServer(
    private val db: DatabaseClient,
    private val application: Context,
    settingsManager: SettingsManager,
    private val jellyfinApiClient: JellyfinApiClient,
    mediaLibraryAndFavoriteSyncScheduler: MediaLibraryAndFavoriteSyncScheduler,
    downloadManager: DownLoadManager
) : IDataSourceParentServer(
    db,
    settingsManager,
    application,
    jellyfinApiClient,
    mediaLibraryAndFavoriteSyncScheduler,
    downloadManager
) {

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
        jellyfinApiClient.createApiClient(
            appName, "$versionName", deviceId, deviceName
        )
        //提前写入没有sessionToken的Authenticate请求头,不然登录请求都会报错
        setToken()
        jellyfinApiClient.setRetrofitData(address, ifTmpObject())
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
        val favorite = jellyfinApiClient.userLibraryApi().markFavoriteItem(itemId).isFavorite
        return favorite
    }

    /**
     * 取消项目收藏
     * @param [itemId] 专辑/音乐id
     */
    override suspend fun unmarkFavoriteItem(itemId: String, dataType: MusicTypeEnum): Boolean {
        val favorite = jellyfinApiClient.userLibraryApi().unmarkFavoriteItem(itemId).isFavorite
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
        updateOrSaveDataInfoCount(music, album, artist, playlist, genres, favorite, connectionId)
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
            .deleteItems(ids = musicIds.joinToString())
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
        val queryResult = jellyfinApiClient.userLibraryApi().getItem(albumId)
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
     * @param [itemId] 音乐id
     * @return 返回歌词列表
     */
    override suspend fun getMusicLyricList(itemId: String): List<LrcEntryData>? {
        val music = db.musicDao.selectById(itemId)
        return if (music?.ifLyric == true) {
            val lyrics = jellyfinApiClient.lyricsApi().getLyrics(itemId)
            lyrics.lyrics.map {
                LrcEntryData(startTime = it.start!! / LYRICS_AMPLIFICATION, text = it.text)
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
    ): XyResponse<XyMusic> {
        val response =
            getServerMusicList(
                pageSize = pageSize,
                startIndex = startIndex,
                artistIds = listOf(artistId)
            )
        return response
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
    ): List<XyPlayMusic>? {
        var selectMusicList =
            super.getMusicList(pageSize = pageSize, pageNum = pageNum)

        if (selectMusicList.isNullOrEmpty()) {
            val homeMusicList = getServerMusicList(
                pageSize,
                pageNum * pageSize
            )
            selectMusicList = transitionPlayMusic(homeMusicList.items)
        }
        return selectMusicList
    }

    /**
     * 根据专辑获得歌曲列表
     */
    override suspend fun getMusicListByAlbumId(
        albumId: String, pageSize: Int,
        pageNum: Int
    ): List<XyPlayMusic>? {
        var selectMusicList =
            super.getMusicListByAlbumId(albumId = albumId, pageSize = pageSize, pageNum = pageNum)

        if (selectMusicList.isNullOrEmpty()) {
            val homeMusicList = getServerMusicList(
                pageSize,
                pageNum * pageSize,
                parentId = albumId
            )
            selectMusicList = transitionPlayMusic(homeMusicList.items)
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
    ): List<XyPlayMusic>? {
        var selectMusicList =
            super.getMusicListByArtistId(
                artistId = artistId,
                pageSize = pageSize,
                pageNum = pageNum
            )
        if (selectMusicList.isNullOrEmpty()) {
            val homeMusicList = getServerMusicList(
                pageSize,
                pageNum * pageSize,
                artistIds = listOf(artistId)
            )
            selectMusicList = transitionPlayMusic(homeMusicList.items)
        }
        return selectMusicList
    }

    /**
     * 根据艺术家列表获得歌曲列表
     */
    override suspend fun getMusicListByArtistIds(
        artistIds: List<String>,
        pageSize: Int
    ): List<XyMusic>? {
        return getServerMusicList(
            startIndex = 0,
            pageSize = pageSize,
            artistIds = artistIds
        ).items
    }


    /**
     * 获得收藏歌曲列表
     */
    override suspend fun getMusicListByFavorite(
        pageSize: Int,
        pageNum: Int
    ): List<XyPlayMusic>? {
        var selectMusicList =
            super.getMusicListByFavorite(pageSize = pageSize, pageNum = pageNum)
        if (selectMusicList.isNullOrEmpty()) {
            val homeMusicList = getServerMusicList(
                pageSize,
                pageNum * pageSize,
                isFavorite = true
            )
            selectMusicList = transitionPlayMusic(homeMusicList.items)
        }
        return selectMusicList
    }

    /**
     * 创建下载链接
     */
    override fun createDownloadUrl(musicId: String): String {
        return jellyfinApiClient.createDownloadUrl(musicId)
    }

    /**
     * 获得随机音乐
     */
    override suspend fun getRandomMusicList(pageSize: Int, pageNum: Int): List<XyMusic>? {
        return getServerMusicList(
            startIndex = pageNum * pageSize,
            pageSize = pageSize,
            sortBy = listOf(ItemSortBy.RANDOM)
        ).items
    }

    /**
     * 获得媒体库列表
     */
    override suspend fun selectMediaLibrary(connectionId: Long) {
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
                    connectionId = connectionId
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
    override suspend fun playRecordMusicOrAlbumList(pageSize: Int) {
        val musicList = getServerMusicList(
            startIndex = 0,
            pageSize = pageSize,
            filters = listOf(ItemFilter.IS_PLAYED),
            sortBy = listOf(ItemSortBy.DATE_PLAYED),
            sortOrder = listOf(SortOrder.DESCENDING)
        ).items
        if (!musicList.isNullOrEmpty())
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

        val response = getPlaylistsServer(0, 10000)
        return db.withTransaction {
            db.albumDao.removePlaylist()
            response.items?.let {
                saveBatchAlbum(it, MusicDataTypeEnum.PLAYLIST, true)
            }
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
                        userId = getUserId(), canEdit = true
                    )
                ), isPublic = false
            )
        ).id
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
                val items =
                    getServerMusicList(startIndex = 0, pageSize = 1, path = it.path).items
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
                    musicIds = removeDuplicatesMusicList.map { music -> music.itemId }
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
        playlistId: String, musicIds: List<String>
    ): Boolean {
        jellyfinApiClient.playlistsApi().addItemToPlaylist(
            playlistId = playlistId,
            ids = musicIds.joinToString()
        )
        return super.saveMusicPlaylist(playlistId, musicIds)
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
            entryIds = musicIds.joinToString()
        )
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
                ids = artistIds, parentId = libraryId
            ).toMap()
        ).items
        return convertToArtistList(items)
    }

    /**
     * 从远程获得艺术家描述
     */
    override suspend fun selectArtistDescribe(artistId: String): XyArtist? {
        val item = jellyfinApiClient.userLibraryApi()
            .getItem(itemId = artistId)
        return XyArtistInfo(convertToArtistList(listOf(item))[0], null)
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
        val albumList = jellyfinApiClient.userLibraryApi().getLatestMedia(
            ItemRequest(
                fields = listOf(ItemFields.PRIMARY_IMAGE_ASPECT_RATIO),
                imageTypeLimit = 1,
                enableImageTypes = listOf(
                    ImageType.PRIMARY, ImageType.BACKDROP, ImageType.BANNER, ImageType.THUMB
                ),
                limit = Constants.MIN_PAGE,
                parentId = libraryId
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
     * 获得Okhttp客户端
     */
    override fun getOkhttpClient(): OkHttpClient {
        return jellyfinApiClient.apiOkHttpClient
    }

    /**
     * 获得token
     */
    override fun setToken() {
        jellyfinApiClient.updateTokenOrHeadersOrQuery()
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
    override fun getMusicPlayUrl(
        musicId: String,
        static: Boolean,
        audioCodec: AudioCodecEnum?,
        audioBitRate: Int?,
        session: String?
    ): String {
        return jellyfinApiClient.createAudioUrl(
            musicId,
            audioCodec,
            static,
            audioBitRate
        )
    }

    /**
     * 获得相似歌曲列表
     */
    override suspend fun getSimilarMusicList(musicId: String): List<XyMusicExtend>? {
        val response = jellyfinApiClient.itemApi().getSimilarItems(
            itemId = musicId,
            userId = getUserId(),
            limit = Constants.SIMILAR_MUSIC_LIST_PAGE,
            fields = listOf(
                ItemFields.PRIMARY_IMAGE_ASPECT_RATIO,
                ItemFields.SORT_NAME,
                ItemFields.MEDIA_SOURCES,
                ItemFields.DATE_CREATED,
                ItemFields.GENRES
            ).joinToString(LocalConstants.ARTIST_DELIMITER)
        )

        return transitionMusicExtend(convertToMusicList(response.items))
    }

    /**
     * 获得歌手热门歌曲列表
     */
    override suspend fun getArtistPopularMusicList(
        artistId: String?,
        artistName: String?
    ): List<XyMusicExtend>? {
        val items = getServerMusicList(
            startIndex = 0,
            pageSize = Constants.ARTIST_HOT_MUSIC_LIST_PAGE,
            filters = listOf(ItemFilter.IS_PLAYED),
            sortBy = listOf(ItemSortBy.PLAY_COUNT),
            sortOrder = listOf(SortOrder.DESCENDING),
            artistIds = artistId?.let { listOf(artistId) }
        ).items
        return transitionMusicExtend(items)
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
     * 获得流派内音乐列表/或者专辑
     * @param [genreIds] 流派id
     */
    override suspend fun selectMusicListByGenreIds(
        genreIds: List<String>,
        pageSize: Int
    ): List<XyMusic>? {
        return getServerMusicList(
            startIndex = 0,
            pageSize = pageSize,
            genreIds = genreIds
        ).items
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
    ): XyResponse<XyMusic> {
        val sortType: SearchAndOrder = sortType.toSearchAndOrder()
        val response = getServerMusicList(
            startIndex = startIndex,
            pageSize = pageSize,

            isFavorite = isFavorite,
            sortBy = sortType.sortType?.let { listOf(sortType.sortType) },
            sortOrder = sortType.order?.let { listOf(sortType.order) },
            parentId = parentId,
            years = years
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
    ): XyResponse<XyAlbum> {
        val sortType: SearchAndOrder = sortType.toSearchAndOrder()
        val response = getAlbumList(
            startIndex = startIndex,
            pageSize = pageSize,
            sortBy = sortType.sortType?.let { listOf(sortType.sortType) },
            sortOrder = sortType.order?.let { listOf(sortType.order) },
            isFavorite = isFavorite,
            years = years,
            artistIds = artistId?.let { listOf(artistId) },
            genreIds = genreId?.let { listOf(genreId) }
        )
        return response
    }

    /**
     * 远程获得相似艺术家
     */
    override suspend fun getSimilarArtistsRemotely(
        artistId: String,
        startIndex: Int,
        pageSize: Int
    ): List<XyArtist>? {
        val response = jellyfinApiClient.artistsApi().getSimilarArtists(
            artistId = artistId,
            ItemRequest(
                limit = Constants.UI_LIST_PAGE,
                userId = getUserId()
            ).toMap()
        )
        return XyArtistInfo(null, convertToArtistList(response.items))
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
    ): XyResponse<XyMusic> {
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
    ): XyResponse<XyGenre> {
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
    ): XyResponse<XyMusic> {
        val sortType: SearchAndOrder = sortType.toSearchAndOrder()
        val response = getServerMusicList(
            startIndex = startIndex,
            pageSize = pageSize,
            sortBy = sortType.sortType?.let { listOf(sortType.sortType) },
            sortOrder = sortType.order?.let { listOf(sortType.order) },
            isFavorite = isFavorite,
            years = years
        )
        return response
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
    ): XyResponse<XyAlbum> {
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
                parentId = libraryId
            ).toMap()
        )

        return XyResponse(
            items = albumResponse.items.map { album ->
                convertToAlbum(
                    album,
                    isFavorite ?: false
                )
            },
            totalRecordCount = albumResponse.totalRecordCount,
            startIndex = albumResponse.startIndex ?: 0
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
        artistIds: List<String>? = null,
        isFavorite: Boolean? = null,
        search: String? = null,
        sortBy: List<ItemSortBy>? = null,
        sortOrder: List<SortOrder>? = null,
        filters: List<ItemFilter>? = null,
        years: List<Int>? = null,
        genreIds: List<String>? = null,
        parentId: String? = null,
        path: String? = null
    ): XyResponse<XyMusic> {
        val response = jellyfinApiClient.itemApi().getItems(
            itemRequest = ItemRequest(
                artistIds = artistIds,
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
                    ItemFields.DATE_CREATED,
                    ItemFields.GENRES
                ),
                searchTerm = search,
                imageTypeLimit = 1,
                genreIds = genreIds,
                years = years,
                albumIds = albumId?.let { listOf(albumId) },
                parentId = if (parentId.isNullOrBlank()) libraryId else parentId,
                path = path
            ).toMap()
        )
        val items = response.items.map {
            convertToMusic(it)
        }

        return XyResponse<XyMusic>(
            items = items,
            totalRecordCount = response.totalRecordCount,
            startIndex = response.startIndex ?: 0
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
    ): XyResponse<XyArtist> {
        val response = jellyfinApiClient.artistsApi().getArtists(
            ItemRequest(
                limit = pageSize,
                startIndex = startIndex,
                sortBy = listOf(ItemSortBy.SORT_NAME),
                sortOrder = listOf(SortOrder.ASCENDING),
                fields = listOf(
                    ItemFields.SORT_NAME,
                ),
                imageTypeLimit = 1,
                enableImageTypes = listOf(
                    ImageType.PRIMARY, ImageType.BACKDROP
                ),
                searchTerm = search,
                isFavorite = isFavorite,
                parentId = libraryId
            ).toMap()
        )
        val artistList = convertToArtistList(response.items)
        return XyResponse(
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
    ): XyResponse<XyGenre> {
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
                parentId = libraryId
            ).toMap()
        )
        return XyResponse<XyGenre>(
            items = convertToGenreList(genres.items),
            totalRecordCount = genres.totalRecordCount,
            startIndex = genres.startIndex ?: 0
        )
    }

    /**
     * 获取歌单列表
     */
    suspend fun getPlaylistsServer(startIndex: Int, pageSize: Int): XyResponse<XyAlbum> {
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
                        userId = getUserId()
                    ).toMap()
                )
            val xyResponse = XyResponse(
                items = convertToAlbumList(playlists.items, true),
                totalRecordCount = playlists.totalRecordCount,
                startIndex = startIndex
            )
            xyResponse
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "获取歌单失败", e)
            XyResponse<XyAlbum>(items = emptyList(), 0, 0)
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
                ?: if (ifPlaylist) application.getString(Constants.UNKNOWN_PLAYLIST) else application.getString(
                    Constants.UNKNOWN_ALBUM
                ),
            connectionId = getConnectionId(),
            artistIds = item.albumArtists?.joinToString() { it.id },
            artists = item.albumArtists?.mapNotNull { it.name }?.joinToString()
                ?: application.getString(Constants.UNKNOWN_ARTIST),
            year = item.productionYear,
            premiereDate = item.productionYear?.toLong(),
            genreIds = item.genreItems?.joinToString() { it.id },
            ifFavorite = item.userData?.isFavorite == true,
            ifPlaylist = ifPlaylist,
            createTime = item.dateCreated?.toSecondMs() ?: 0L,
            musicCount = if (ifPlaylist) (item.childCount?.toLong()
                ?: 0L) else (item.songCount?.toLong() ?: 0L)
        )
    }

    fun convertToMusicList(item: List<ItemResponse>): List<XyMusic> {
        return item.map { music ->
            convertToMusic(
                music
            )
        }
    }

    //MusicResponseVo转换XyItem
    fun convertToMusic(item: ItemResponse): XyMusic {

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

        return XyMusic(
            itemId = item.id,
            pic = itemImageUrl,
            name = item.name ?: application.getString(Constants.UNKNOWN_MUSIC),
            downloadUrl = createDownloadUrl(item.id),
            album = item.albumId.toString(),
            albumName = item.album,
            connectionId = getConnectionId(),
            artists = item.artistItems?.mapNotNull { artist -> artist.name },
            artistIds = item.artistItems?.map { artist -> artist.id },
            albumArtist = item.albumArtists?.map { artist -> artist.name.toString() }
                ?: listOf(application.getString(Constants.UNKNOWN_ARTIST)),
            albumArtistIds = item.albumArtists?.map { artist -> artist.id },
            createTime = item.dateCreated?.toSecondMs() ?: 0L,
            year = item.productionYear,
            genreIds = item.genreItems?.map { it.id },
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
            playlistItemId = item.id,
            lastPlayedDate = item.userData?.lastPlayedDate?.toSecondMs() ?: 0L
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
                if (!item.name.isNullOrBlank() && !item.imageTags.isNullOrEmpty() && item.imageTags?.containsKey(
                        ImageType.PRIMARY
                    ) == true
                ) item.name?.let {
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

            val backdropImageUrl =
                if (!item.backdropImageTags.isNullOrEmpty()) item.name?.let {
                    jellyfinApiClient.createArtistImageUrl(
                        name = it,
                        imageType = ImageType.BACKDROP,
                        imageIndex = 0,
                        fillHeight = 1080,
                        fillWidth = 1920,
                        quality = 100,
                        tag = item.backdropImageTags?.get(0)
                    )
                }
                else null

            val sortName = item.sortName
            val shortNameStart = if (!sortName.isNullOrBlank()) sortName[0] else '#'
            val selectChat =
                if (!CharUtils.isEnglishLetter(shortNameStart)) "#" else shortNameStart.toString()
            XyArtist(
                artistId = item.id,
                name = item.name ?: application.getString(R.string.unknown_artist),
                pic = artistImageUrl,
                backdrop = backdropImageUrl,
                connectionId = getConnectionId(),
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
            name = item.name ?: application.getString(Constants.UNKNOWN_ALBUM),
            connectionId = getConnectionId(),
            createTime = item.dateCreated?.toSecondMs() ?: 0L,
        )
    }
}