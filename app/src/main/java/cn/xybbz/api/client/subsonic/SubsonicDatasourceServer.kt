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

package cn.xybbz.api.client.subsonic

import XyArtistInfo
import android.content.Context
import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import cn.xybbz.api.client.IDataSourceParentServer
import cn.xybbz.api.client.data.XyResponse
import cn.xybbz.api.client.subsonic.data.AlbumID3
import cn.xybbz.api.client.subsonic.data.ArtistID3
import cn.xybbz.api.client.subsonic.data.GenreID3
import cn.xybbz.api.client.subsonic.data.PlaylistID3
import cn.xybbz.api.client.subsonic.data.ScrobbleRequest
import cn.xybbz.api.client.subsonic.data.SongID3
import cn.xybbz.api.client.subsonic.data.SubsonicArtistsResponse
import cn.xybbz.api.client.subsonic.data.SubsonicResponse
import cn.xybbz.api.dispatchs.MediaLibraryAndFavoriteSyncScheduler
import cn.xybbz.api.enums.AudioCodecEnum
import cn.xybbz.api.enums.jellyfin.CollectionType
import cn.xybbz.api.enums.subsonic.AlbumType
import cn.xybbz.api.enums.subsonic.Status
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.common.enums.SortTypeEnum
import cn.xybbz.common.utils.CharUtils
import cn.xybbz.common.utils.DateUtil.toSecondMs
import cn.xybbz.common.utils.LrcUtils
import cn.xybbz.common.utils.PasswordUtils
import cn.xybbz.common.utils.PlaylistParser
import cn.xybbz.config.download.DownLoadManager
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.entity.data.LrcEntryData
import cn.xybbz.entity.data.SearchData
import cn.xybbz.entity.data.Sort
import cn.xybbz.entity.data.ext.toXyMusic
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.data.artist.XyArtist
import cn.xybbz.localdata.data.genre.XyGenre
import cn.xybbz.localdata.data.library.XyLibrary
import cn.xybbz.localdata.data.music.HomeMusic
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.data.music.XyMusicExtend
import cn.xybbz.localdata.data.music.XyPlayMusic
import cn.xybbz.localdata.enums.DataSourceType
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import cn.xybbz.page.defaultLocalPager
import convertToArtist
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.OkHttpClient

class SubsonicDatasourceServer constructor(
    private val db: DatabaseClient,
    private val application: Context,
    settingsManager: SettingsManager,
    private val subsonicApiClient: SubsonicApiClient,
    mediaLibraryAndFavoriteSyncScheduler: MediaLibraryAndFavoriteSyncScheduler,
    downloadManager: DownLoadManager
) : IDataSourceParentServer(
    db,
    settingsManager,
    application,
    subsonicApiClient,
    mediaLibraryAndFavoriteSyncScheduler,
    downloadManager
) {
    /**
     * 获得当前数据源类型
     */
    override fun getDataSourceType(): DataSourceType {
        return DataSourceType.SUBSONIC
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

        val encryptMd5 = PasswordUtils.encryptMd5(password)

        subsonicApiClient.createApiClient(
            username, encryptMd5.passwordMd5, encryptMd5.encryptedSalt, getDataSourceType().version,
            "${appName}:${versionName}"
        )
        setToken()
        subsonicApiClient.setRetrofitData(address, ifTmpObject())
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
            subsonicApiClient.artistsApi().getArtists(libraryId)
        val artists = convertIndexToArtistList(response)
        return XyResponse(
            items = artists,
            totalRecordCount = artists.size,
            startIndex = 0
        )
    }

    /**
     * 获得音乐列表数据 Subsonic没办法一次性获得所有音乐
     */
    override fun selectMusicFlowList(
        sortFlow: StateFlow<Sort>
    ): Flow<PagingData<HomeMusic>> {
        return defaultLocalPager {
            val sort = sortFlow.value
            val yearList = sort.yearList
            db.musicDao.selectHomeMusicListPageByYear(
                ifFavorite = sort.isFavorite,
                if (yearList.isNullOrEmpty()) null else yearList[0],
                if (yearList.isNullOrEmpty()) null else yearList[yearList.size - 1]
            )
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
                    musicFolderId = libraryId
                )
            if (search3.subsonicResponse.status == Status.Ok) {
                search3.subsonicResponse.searchResult3?.let { search ->
                    searchData.artists = search.artist?.let {
                        val artistList = convertToArtistList(it)
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
    override suspend fun selectAlbumInfoByRemotely(
        albumId: String,
        dataType: MusicDataTypeEnum
    ): XyAlbum? {
        var artistExtend: XyAlbum?
        if (dataType == MusicDataTypeEnum.ALBUM) {
            val album = subsonicApiClient.itemApi().getAlbum(albumId)
            //存储歌曲数据
            album.subsonicResponse.album?.song?.let {

                val albumMusicList = convertToMusicList(it)
                saveBatchMusic(
                    albumMusicList, dataType
                )
            }
            artistExtend = album.subsonicResponse.album?.let { convertToAlbum(it) }
        } else {
            val playlist = subsonicApiClient.playlistsApi()
                .getPlaylistById(albumId.replace(Constants.SUBSONIC_PLAYLIST_SUFFIX, ""))
            //存储歌曲数据
            playlist.subsonicResponse.playlist?.entry?.let {
                val albumMusicList = convertToMusicList(it)
                saveBatchMusic(
                    items = albumMusicList, dataType,
                    playlistId = albumId
                )
            }

            artistExtend =
                playlist.subsonicResponse.playlist?.let { convertToPlaylist(it) }
        }
        return artistExtend
    }

    /**
     * 获得专辑或歌单的音乐列表
     */
    suspend fun getMusicListByAlbumOrPlaylist(
        itemId: String,
        dataType: MusicDataTypeEnum
    ): XyResponse<XyMusic> {
        if (dataType == MusicDataTypeEnum.ALBUM) {
            val album = subsonicApiClient.itemApi().getAlbum(itemId)
            //存储歌曲数据
            val xyMusics = album.subsonicResponse.album?.song?.let {
                convertToMusicList(it)
            }
            return XyResponse(
                items = xyMusics,
                totalRecordCount = xyMusics?.size ?: 0,
                startIndex = 0
            )
        } else {
            val playlist = subsonicApiClient.playlistsApi()
                .getPlaylistById(itemId.replace(Constants.SUBSONIC_PLAYLIST_SUFFIX, ""))
            //存储歌曲数据
            val response = playlist.subsonicResponse.playlist?.entry?.let {
                convertToMusicList(it)
            }
            return XyResponse(
                items = response,
                totalRecordCount = response?.size ?: 0,
                startIndex = 0
            )
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
     * @param [itemId] 音乐id
     * @return 返回歌词列表
     */
    override suspend fun getMusicLyricList(itemId: String): List<LrcEntryData>? {
        val music = db.musicDao.selectById(itemId)
        return if (music?.ifLyric == true) {
            val lyrics = subsonicApiClient.lyricsApi().getLyrics(music.artists?.get(0), music.name)
            lyrics.subsonicResponse.lyrics?.value?.let {
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
        return XyResponse(
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
            musicFolderId = libraryId
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
        val username = subsonicApiClient.username
        return if (username.isNotBlank()) {
            val playlists = subsonicApiClient.playlistsApi().getPlaylists(username)
            db.withTransaction {
                db.albumDao.removePlaylist()
                playlists.subsonicResponse.playlists?.playlist?.let { playlist ->
                    saveBatchAlbum(convertToPlaylists(playlist), MusicDataTypeEnum.PLAYLIST, true)
                }
            }


        } else null

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
     * 导入歌单
     */
    override suspend fun importPlaylist(
        playlistData: PlaylistParser.Playlist,
        playlistId: String
    ): Boolean {

        val musicList = playlistData.musicList.mapNotNull {
            try {
                val items =
                    subsonicApiClient.itemApi().search3(
                        query = it.title + " " + it.artist,
                        artistCount = 0,
                        albumCount = 0,
                        songCount = 1,
                        musicFolderId = libraryId
                    ).subsonicResponse.searchResult3?.song
                if (items.isNullOrEmpty()) null else {
                    val music = items[0]
                    convertToMusic(music)
                }
            } catch (e: Exception) {
                Log.e(Constants.LOG_ERROR_PREFIX, "获取歌曲: ${it.title} 信息失败", e)
                null
            }
        }


        val serverMusicList = getMusicListByAlbumOrPlaylist(
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
     */
    override suspend fun saveMusicPlaylist(
        playlistId: String,
        musicIds: List<String>
    ): Boolean {
        subsonicApiClient.playlistsApi().updatePlaylist(
            playlistId = playlistId.replace(Constants.SUBSONIC_PLAYLIST_SUFFIX, ""),
            songIdToAdd = musicIds
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
        return artist.subsonicResponse.artist?.let { convertToArtistList(listOf(it)) }
            ?: emptyList()
    }

    /**
     * 根据id获得艺术家信息
     * @param [artistId] 艺术家id
     * @return [List<ArtistItem>?] 艺术家信息
     */
    override suspend fun selectArtistInfoByRemotely(artistId: String): XyArtistInfo? {
        val artist = subsonicApiClient.artistsApi().getArtist(artistId)
        //专辑转换
        artist.subsonicResponse.artist?.album?.let { albums ->
            val albumList = convertToAlbumList(albums)
            //存储到数据库中
            if (albumList.isNotEmpty())
                db.albumDao.saveBatch(
                    albumList,
                    dataType = MusicDataTypeEnum.ARTIST,
                    connectionId = getConnectionId(),
                    artistId
                )
        }
        val artistList = artist.subsonicResponse.artist?.let {
            convertToArtist(
                it,
                indexNumber = 0
            )
        }

        val similarArtists = getSimilarArtistsRemotely(artistId, 0, 12)
        return XyArtistInfo(artistList, similarArtists)

    }

    /**
     * 获得媒体库列表
     */
    override suspend fun selectMediaLibrary(connectionId: Long) {
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
                            name = it.name,
                            connectionId = connectionId
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
    override suspend fun playRecordMusicOrAlbumList(pageSize: Int) {
        //subsonic只有最近播放专辑
        //插入最新播放专辑
        val albumList = subsonicApiClient.itemApi().getAlbumList2(
            type = AlbumType.RECENT,
            size = pageSize,
            offset = 0,
            musicFolderId = libraryId
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
            musicFolderId = libraryId
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
            musicFolderId = libraryId
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
     * 获得收藏的音乐
     */
    suspend fun getMusicFavoriteData(): List<XyMusic>? {
        val starred2 = subsonicApiClient.itemApi().getStarred2(
            musicFolderId = libraryId
        )
        return starred2.subsonicResponse.starred2?.song?.let { songs ->
            convertToMusicList(songs)
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
    ): List<XyMusic> {

        val map = genreIds.mapNotNull {
            try {
                val randomSongs = subsonicApiClient.itemApi().getSongsByGenre(
                    genre = it,
                    size = pageSize,
                    musicFolderId = libraryId
                )
                randomSongs.subsonicResponse.songsByGenre?.song?.let { song ->
                    convertToMusicList(
                        song
                    )
                }
            } catch (e: Exception) {
                Log.e(Constants.LOG_ERROR_PREFIX, "获取流派内音乐失败", e)
                null
            }
        }.flatten()
        return map
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
        subsonicApiClient.updateTokenOrHeadersOrQuery()
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
        return subsonicApiClient.createAudioUrl(musicId, audioCodec, audioBitRate)
    }

    /**
     * 获得相似歌曲列表
     */
    override suspend fun getSimilarMusicList(musicId: String): List<XyMusicExtend>? {
        val items =
            subsonicApiClient.itemApi().getSimilarSongs(
                songId = musicId,
                count = Constants.SIMILAR_MUSIC_LIST_PAGE
            ).subsonicResponse.songs?.toXyMusic(
                getConnectionId(),
                { createDownloadUrl(it) },
                {
                    subsonicApiClient.getImageUrl(
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
            subsonicApiClient.itemApi().getTopSongs(
                artistName = artistName ?: "",
                count = Constants.ARTIST_HOT_MUSIC_LIST_PAGE
            ).subsonicResponse.topSongs.toXyMusic(
                getConnectionId(),
                createDownloadUrl = { createDownloadUrl(it) },
                getImageUrl = { subsonicApiClient.getImageUrl(it) }
            )
        return transitionMusicExtend(items)
    }


    /**
     * 释放
     */
    override fun close() {
        super.close()
        subsonicApiClient.release()
    }


    /**
     * 获取远程服务器的专辑和歌单音乐列表
     * @param [startIndex] 开始索引
     * @param [pageSize] 页面大小
     * @param [isFavorite] 是否收藏
     * @param [sortType] 排序类型
     * @param [years] 年列表
     * @param [parentId] 上级id
     * @param [dataType] 数据类型
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
        return getMusicListByAlbumOrPlaylist(
            itemId = parentId, dataType = dataType
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
        val response = getAlbumList(
            startIndex = startIndex,
            pageSize = pageSize,
            isFavorite = isFavorite,
            years = years,
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
    ): List<XyArtist>? {
        val response =
            subsonicApiClient.artistsApi().getArtistInfo(id = artistId, count = pageSize)
        return response.subsonicResponse.artistInfo?.similarArtist?.map {
            convertToArtist(
                artistId3 = it,
                indexNumber = 0
            )
        }
    }

    /**
     * 获得专辑列表的RemoteMediator
     */
    @OptIn(ExperimentalPagingApi::class)
    override fun getAlbumListRemoteMediator(artistId: String): RemoteMediator<Int, XyAlbum>? {
        return null
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
        val items = getMusicFavoriteData()
        return XyResponse(
            items = items,
            totalRecordCount = items?.size ?: 0
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
        val items = getGenreList()
        return XyResponse(
            items = items,
            totalRecordCount = items?.size ?: 0
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
        return XyResponse(
            items = emptyList()
        )
    }

    fun convertIndexToArtistList(
        response: SubsonicResponse<SubsonicArtistsResponse>,
    ): List<XyArtist> {
        return if (response.subsonicResponse.status == Status.Ok) {
            response.subsonicResponse.artists?.index?.flatMap { index ->
                convertToArtistList(index.artist, index.name)
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
        index: String? = null
    ): List<XyArtist> {
        val artistList = item.map { artist ->
            val result = if (index.isNullOrBlank()) null else toLatinCompat(index)
            val shortNameStart = if (!result.isNullOrBlank()) result[0] else '#'
            val selectChat =
                if (!CharUtils.isEnglishLetter(shortNameStart)) "#" else shortNameStart.toString()
                    .lowercase()

            convertToArtist(artist, selectChat, 0)
        }

        return artistList
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
                subsonicApiClient.getImageUrl(
                    coverArt
                )
            },
            backdrop = if (artistId3.coverArt.isNullOrBlank()) null else artistId3.coverArt?.let { coverArt ->
                subsonicApiClient.getImageUrl(
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
            connectionId = getConnectionId(),
            ifFavorite = !album.starred.isNullOrBlank(),
            artists = album.artist,
            artistIds = album.artistId,
            ifPlaylist = ifPlaylist,
            musicCount = album.songCount,
            createTime = album.created.toSecondMs(),
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
            connectionId = getConnectionId(),
            ifFavorite = false,
            ifPlaylist = true,
            musicCount = playlist.songCount,
            createTime = playlist.created.toSecondMs()
        )
    }

    /**
     * 将SongID3转换成XyMusic
     */
    fun convertToMusicList(item: List<SongID3>): List<XyMusic> {
        return item.map { music ->
            convertToMusic(
                music
            )
        }
    }

    /**
     * 将SongID3转换成XyMusic
     */
    fun convertToMusic(music: SongID3): XyMusic {
        return music.toXyMusic(
            pic = if (music.coverArt.isNullOrBlank()) null else music.coverArt?.let {
                subsonicApiClient.getImageUrl(
                    it
                )
            },
            downloadUrl = createDownloadUrl(music.id),
            connectionId = getConnectionId()
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
            connectionId = getConnectionId()
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
    ): XyResponse<XyAlbum> {

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
            musicFolderId = libraryId
        )

        val size = albumList.subsonicResponse.albumList2?.album?.size ?: 0
        return XyResponse(
            items = albumList.subsonicResponse.albumList2?.album?.let {
                convertToAlbumList(it)
            },
            totalRecordCount = if (albumList.subsonicResponse.albumList2?.album.isNullOrEmpty())
                startIndex
            else if (size < pageSize)
                startIndex + size
            else
                startIndex + size + 1,
            startIndex = startIndex
        )
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
        val favorite = try {
            subsonicApiClient.userLibraryApi()
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
            subsonicApiClient.userLibraryApi().unmarkFavoriteItem(
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
     * 获得歌曲列表
     */
    override suspend fun getMusicList(
        pageSize: Int,
        pageNum: Int
    ): List<XyPlayMusic>? {
        return null
    }

    /**
     * 根据专辑获得歌曲列表
     */
    override suspend fun getMusicListByAlbumId(
        albumId: String,
        pageSize: Int,
        pageNum: Int
    ): List<XyPlayMusic>? {
        return super.getMusicListByAlbumId(
            albumId = albumId,
            pageSize = pageSize,
            pageNum = pageNum
        )
    }

    /**
     * 根据艺术家获得歌曲列表
     */
    override suspend fun getMusicListByArtistId(
        artistId: String,
        pageSize: Int,
        pageNum: Int
    ): List<XyPlayMusic>? {
        return super.getMusicListByArtistId(
            artistId = artistId,
            pageSize = pageSize,
            pageNum = pageNum
        )
    }

    /**
     * 根据艺术家列表获得歌曲列表
     */
    override suspend fun getMusicListByArtistIds(
        artistIds: List<String>,
        pageSize: Int
    ): List<XyMusic>? {
        return null
    }

    /**
     * 获得收藏歌曲列表
     */
    override suspend fun getMusicListByFavorite(
        pageSize: Int,
        pageNum: Int
    ): List<XyPlayMusic>? {
        return super.getMusicListByFavorite(pageSize = pageSize, pageNum = pageNum)
    }

    /**
     * 创建下载链接
     */
    override fun createDownloadUrl(musicId: String): String {
        return subsonicApiClient.createDownloadUrl(musicId)
    }
}