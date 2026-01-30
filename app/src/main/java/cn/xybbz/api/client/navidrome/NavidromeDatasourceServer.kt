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

package cn.xybbz.api.client.navidrome

import XyArtistInfo
import android.content.Context
import android.util.Log
import androidx.room.withTransaction
import cn.xybbz.api.client.IDataSourceParentServer
import cn.xybbz.api.client.data.XyResponse
import cn.xybbz.api.client.navidrome.data.AlbumItem
import cn.xybbz.api.client.navidrome.data.ArtistItem
import cn.xybbz.api.client.navidrome.data.Genre
import cn.xybbz.api.client.navidrome.data.PlaylistAddMusicsUpdateRequest
import cn.xybbz.api.client.navidrome.data.PlaylistItemData
import cn.xybbz.api.client.navidrome.data.PlaylistUpdateRequest
import cn.xybbz.api.client.navidrome.data.SongItem
import cn.xybbz.api.client.navidrome.data.getWithTotalCount
import cn.xybbz.api.client.subsonic.data.ArtistID3
import cn.xybbz.api.client.subsonic.data.ScrobbleRequest
import cn.xybbz.api.constants.ApiConstants
import cn.xybbz.api.dispatchs.MediaLibraryAndFavoriteSyncScheduler
import cn.xybbz.api.enums.AudioCodecEnum
import cn.xybbz.api.enums.navidrome.OrderType
import cn.xybbz.api.enums.navidrome.SortType
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.common.enums.SortTypeEnum
import cn.xybbz.common.utils.CharUtils
import cn.xybbz.common.utils.DateUtil.toSecondMs
import cn.xybbz.common.utils.LrcUtils
import cn.xybbz.common.utils.PlaylistParser
import cn.xybbz.config.download.DownLoadManager
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.entity.data.LrcEntryData
import cn.xybbz.entity.data.NavidromeOrder
import cn.xybbz.entity.data.SearchData
import cn.xybbz.entity.data.ext.joinToString
import cn.xybbz.entity.data.ext.toXyMusic
import cn.xybbz.entity.data.toNavidromeOrder
import cn.xybbz.entity.data.toNavidromeOrder2
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.data.artist.XyArtist
import cn.xybbz.localdata.data.genre.XyGenre
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.data.music.XyMusicExtend
import cn.xybbz.localdata.data.music.XyPlayMusic
import cn.xybbz.localdata.enums.DataSourceType
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import convertToArtist
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import okhttp3.OkHttpClient
import java.net.SocketTimeoutException

class NavidromeDatasourceServer constructor(
    private val db: DatabaseClient,
    application: Context,
    settingsManager: SettingsManager,
    private val navidromeApiClient: NavidromeApiClient,
    mediaLibraryAndFavoriteSyncScheduler: MediaLibraryAndFavoriteSyncScheduler,
    downloadManager: DownLoadManager
) : IDataSourceParentServer(
    db,
    settingsManager,
    application,
    navidromeApiClient,
    mediaLibraryAndFavoriteSyncScheduler,
    downloadManager
) {
    /**
     * 获得当前数据源类型
     */
    override fun getDataSourceType(): DataSourceType {
        return DataSourceType.NAVIDROME
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
    ): XyResponse<XyArtist> {
        val response =
            getWithTotalCount {
                navidromeApiClient.artistsApi().getArtists(
                    start = startIndex,
                    end = startIndex + pageSize,
                    name = search,
                    starred = isFavorite
                )
            }
        val artists = response.data?.let { convertToArtistList(it) } ?: emptyList()
        return XyResponse(
            items = artists,
            totalRecordCount = response.totalCount ?: 0,
            startIndex = startIndex
        )
    }

    /**
     * 根据艺术家id获得艺术家列表
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
        val favorite = try {
            navidromeApiClient.userLibraryApi()
                .markFavoriteItem(
                    id = if (dataType == MusicTypeEnum.MUSIC) listOf(itemId) else null,
                    albumId = if (dataType == MusicTypeEnum.ALBUM) listOf(itemId) else null,
                    artistId = if (dataType == MusicTypeEnum.ARTIST) listOf(itemId) else null
                )
            true
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "收藏失败", e)
            false
        }
        return favorite
    }

    /**
     * 取消项目收藏
     * @param [itemId] 专辑/音乐id
     */
    override suspend fun unmarkFavoriteItem(itemId: String, dataType: MusicTypeEnum): Boolean {
        val favorite = try {
            navidromeApiClient.userLibraryApi().unmarkFavoriteItem(
                id = if (dataType == MusicTypeEnum.MUSIC) listOf(itemId) else null,
                albumId = if (dataType == MusicTypeEnum.ALBUM) listOf(itemId) else null,
                artistId = if (dataType == MusicTypeEnum.ARTIST) listOf(itemId) else null
            )
            true
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "取消收藏失败", e)
            false
        }
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
                    getPlaylistsServer(0, 1).totalRecordCount
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
                        pageSize = 1, startIndex = 0, isFavorite = true
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
    ): XyAlbum {
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
     * @param [itemId] 音乐id
     * @return 返回歌词列表
     */
    override suspend fun getMusicLyricList(itemId: String): List<LrcEntryData>? {
        val music = db.musicDao.selectById(itemId = itemId)
        return if (music?.ifLyric == true) {
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
    ): XyResponse<XyMusic> {
        return getServerMusicList(
            startIndex = startIndex,
            pageSize = pageSize,
            artistIds = listOf(artistId)
        )
    }

    /**
     * 获得随机音乐
     */
    override suspend fun getRandomMusicList(pageSize: Int, pageNum: Int): List<XyMusic>? {
        return getServerMusicList(
            startIndex = pageSize * pageNum,
            pageSize = pageSize,
            sortBy = SortType.RANDOM,
        ).items
    }

    /**
     * 获取歌单列表
     */
    override suspend fun getPlaylists(): List<XyAlbum>? {
        val allResponse = getPlaylistsServer(0, 0)
        db.withTransaction {
            db.albumDao.removePlaylist()
            if (!allResponse.items.isNullOrEmpty()) {
                allResponse.items?.let {
                    saveBatchAlbum(it, MusicDataTypeEnum.PLAYLIST, true)
                }
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
     */
    override suspend fun saveMusicPlaylist(
        playlistId: String,
        musicIds: List<String>
    ): Boolean {
        navidromeApiClient.playlistsApi().addPlaylistMusics(
            playlistId = playlistId, PlaylistAddMusicsUpdateRequest(musicIds)
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
    override suspend fun selectArtistInfoById(artistId: String): XyArtist? {
        var artistInfo: XyArtist? = db.artistDao.selectById(artistId)
        if (artistInfo != null) {
            artistInfo =
                artistInfo.copy(ifFavorite = db.artistDao.selectFavoriteById(artistId) ?: false)
        }
        return artistInfo
    }

    /**
     * 获得媒体库列表
     */
    override suspend fun selectMediaLibrary(connectionId: Long) {

    }

    /**
     * 获得最近播放音乐或专辑
     */
    override suspend fun playRecordMusicOrAlbumList(pageSize: Int) {
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
     * 获得歌曲列表
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
        albumId: String,
        pageSize: Int,
        pageNum: Int
    ): List<XyPlayMusic>? {
        var selectMusicList =
            super.getMusicListByAlbumId(albumId = albumId, pageSize = pageSize, pageNum = pageNum)

        if (selectMusicList.isNullOrEmpty()) {
            val homeMusicList = getServerMusicList(
                startIndex = pageNum * pageSize,
                pageSize = pageSize,
                albumId = albumId
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
                startIndex = pageNum * pageSize,
                pageSize = pageSize,
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
                startIndex = pageNum * pageSize,
                pageSize = pageSize,
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
        return navidromeApiClient.createDownloadUrl(musicId)
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
    override fun getMusicPlayUrl(
        musicId: String,
        static: Boolean,
        audioCodec: AudioCodecEnum?,
        audioBitRate: Int?,
        session: String?
    ): String {
        var audioCodec = audioCodec ?: AudioCodecEnum.ROW
        if (static) {
            audioCodec = AudioCodecEnum.ROW
        }
        return navidromeApiClient.createAudioUrl(musicId, audioCodec, audioBitRate)
    }

    /**
     * 获得相似歌曲列表
     */
    override suspend fun getSimilarMusicList(musicId: String): List<XyMusicExtend>? {
        val items =
            navidromeApiClient.itemApi().getSimilarSongs(
                songId = musicId,
                count = Constants.SIMILAR_MUSIC_LIST_PAGE
            ).subsonicResponse.songs.toXyMusic(
                getConnectionId(),
                createDownloadUrl = { createDownloadUrl(it) },
                getImageUrl = {
                    navidromeApiClient.getImageUrl(
                        it
                    )
                }
            )
        return transitionMusicExtend(items)
    }

    /**
     * 获得歌手热门歌曲列表
     */
    override suspend fun getArtistPopularMusicList(
        artistId: String?,
        artistName: String?
    ): List<XyMusicExtend>? {
        val items =
            navidromeApiClient.itemApi().getTopSongs(
                artistName = artistName ?: "",
                count = Constants.ARTIST_HOT_MUSIC_LIST_PAGE
            ).subsonicResponse.topSongs.toXyMusic(
                getConnectionId(),
                createDownloadUrl = { createDownloadUrl(it) },
                getImageUrl = { navidromeApiClient.getImageUrl(it) }
            )
        return transitionMusicExtend(items)
    }

    /**
     * 释放
     */
    override fun close() {
        super.close()
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
    ): XyResponse<XyMusic> {
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
    ): XyResponse<XyAlbum> {
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
     * 远程获得相似艺术家
     */
    override suspend fun getSimilarArtistsRemotely(
        artistId: String,
        startIndex: Int,
        pageSize: Int
    ): XyArtistInfo {
        val response =
            navidromeApiClient.artistsApi().getArtistInfo(id = artistId, count = pageSize)
        return XyArtistInfo(null, response.subsonicResponse.artistInfo?.similarArtist?.map {
            convertToArtist(
                artistId3 = it,
                indexNumber = 0
            )
        })
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
    ): XyResponse<XyAlbum> {

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

        return XyResponse(
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
        artistIds: List<String>? = null,
        genreIds: List<String>? = null,
        isFavorite: Boolean? = null,
        search: String? = null,
        year: Int? = null,
        sortBy: SortType = SortType.TITLE,
        sortOrder: OrderType = OrderType.ASC,
    ): XyResponse<XyMusic> {
        val response =
            getWithTotalCount {
                navidromeApiClient.itemApi().getSong(
                    start = startIndex,
                    end = startIndex + pageSize,
                    order = sortOrder,
                    sort = sortBy,
                    title = search,
                    starred = isFavorite,
                    genreIds = genreIds,
                    albumId = albumId,
                    artistIds = artistIds,
                    year = year,
                )
            }
        return XyResponse(
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
    ): XyResponse<XyMusic> {
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

            return XyResponse(
                items = playlistMusicList.data?.let { convertToMusicList(it, true) },
                totalRecordCount = playlistMusicList.totalCount ?: 0,
                startIndex = startIndex
            )
        }
    }

    /**
     * 获取歌单列表
     */
    suspend fun getPlaylistsServer(startIndex: Int, pageSize: Int): XyResponse<XyAlbum> {
        return try {
            val playlists =
                getWithTotalCount {
                    navidromeApiClient.playlistsApi()
                        .getPlaylists(start = startIndex, end = startIndex + pageSize)
                }
            val xyResponse = XyResponse(
                items = playlists.data?.let { convertToPlaylists(it) },
                totalRecordCount = playlists.totalCount ?: 0,
                startIndex = startIndex
            )
            xyResponse
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "获取歌单失败", e)
            XyResponse(items = emptyList(), 0, 0)
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
    ): XyResponse<XyGenre> {
        val genreResponse = getWithTotalCount {
            navidromeApiClient.genreApi().getGenres(
                start = startIndex,
                end = startIndex + pageSize,
                name = search,
                order = sortOrder,
                sort = sortBy
            )
        }

        return XyResponse(
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
            connectionId = getConnectionId(),
            ifFavorite = false,
            ifPlaylist = true,
            musicCount = playlist.songCount,
            createTime = playlist.createdAt.toSecondMs()
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
        val orderArtistName = artist.orderArtistName
        val result =
            if (orderArtistName.isNullOrBlank()) null else toLatinCompat(
                orderArtistName
            )
        val shortNameStart = if (!result.isNullOrBlank()) result[0] else '#'
        val selectChat =
            if (!CharUtils.isEnglishLetter(shortNameStart)) "#" else shortNameStart.toString()
                .lowercase()
        return XyArtist(
            artistId = artist.id,
            pic = artist.smallImageUrl,
            backdrop = artist.largeImageUrl,
            name = artist.name,
            connectionId = getConnectionId(),
            selectChat = selectChat,
            ifFavorite = artist.starred ?: false,
            indexNumber = indexNumber
        )
    }


    /**
     * 将ArtistID3转换成XyArtist
     */
    fun convertToArtist(
        artistId3: ArtistID3,
        index: String? = null,
        indexNumber: Int,
    ): XyArtist {

        return artistId3.convertToArtist(
            pic = if (artistId3.coverArt.isNullOrBlank()) null else artistId3.coverArt?.let { coverArt ->
                navidromeApiClient.getImageUrl(
                    coverArt
                )
            },
            backdrop = if (artistId3.coverArt.isNullOrBlank()) null else artistId3.coverArt?.let { coverArt ->
                navidromeApiClient.getImageUrl(
                    coverArt
                )
            },
            index = index,
            indexNumber = indexNumber,
            connectionId = getConnectionId()
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
            connectionId = getConnectionId(),
            artists = album.albumArtist,
            artistIds = album.albumArtistId,
            ifPlaylist = ifPlaylist,
            musicCount = album.songCount,
            premiereDate = album.maxYear.toLong(),
            year = album.maxYear,
            ifFavorite = album.starred ?: false,
            genreIds = album.genres?.joinToString { it.id },
            createTime = album.createdAt.toSecondMs()
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
            itemId = if (isPlaylistMusic) music.mediaFileId ?: "" else music.id,
            pic = navidromeApiClient.getImageUrl(
                ApiConstants.NAVIDROME_IMAGE_PREFIX_MUSIC + music.id
            ),
            name = music.title,
            downloadUrl = if (isPlaylistMusic) music.mediaFileId?.let {
                navidromeApiClient.createDownloadUrl(
                    it
                )
            } ?: "" else navidromeApiClient.createDownloadUrl(music.id),
            album = music.albumId,
            albumName = music.album,
            genreIds = music.genres?.map { it.id },
            connectionId = getConnectionId(),
            artists = listOf(music.artist),
            artistIds = listOf(music.artistId),
            albumArtist = listOf(music.artist),
            albumArtistIds = listOf(music.artistId),
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
            playlistItemId = music.id,
            lastPlayedDate = music.playDate?.toSecondMs() ?: 0L,
            createTime = music.createdAt.toSecondMs()
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
            connectionId = getConnectionId()
        )
    }

}