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

package cn.xybbz.api.client

import android.content.Context
import android.icu.text.Transliterator
import android.os.Build
import android.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.RemoteMediator
import androidx.room.Transaction
import androidx.room.withTransaction
import cn.xybbz.R
import cn.xybbz.api.TokenServer
import cn.xybbz.api.client.data.ClientLoginInfoReq
import cn.xybbz.api.client.data.XyResponse
import cn.xybbz.api.exception.ConnectionException
import cn.xybbz.api.exception.ServiceException
import cn.xybbz.api.exception.UnauthorizedException
import cn.xybbz.api.state.ClientLoginInfoState
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.enums.SortTypeEnum
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.common.utils.PasswordUtils
import cn.xybbz.config.connection.ConnectionConfigServer
import cn.xybbz.entity.data.EncryptAesData
import cn.xybbz.entity.data.Sort
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.data.artist.XyArtist
import cn.xybbz.localdata.data.artist.XyArtistExt
import cn.xybbz.localdata.data.connection.ConnectionConfig
import cn.xybbz.localdata.data.count.XyDataCount
import cn.xybbz.localdata.data.genre.XyGenre
import cn.xybbz.localdata.data.music.HomeMusic
import cn.xybbz.localdata.data.music.PlaylistMusic
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.data.music.XyMusicExtend
import cn.xybbz.localdata.data.music.XyPlayMusic
import cn.xybbz.localdata.enums.DataSourceType
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import cn.xybbz.page.bigPager
import cn.xybbz.page.defaultPager
import cn.xybbz.page.parent.AlbumOrPlaylistMusicListRemoteMediator
import cn.xybbz.page.parent.AlbumRemoteMediator
import cn.xybbz.page.parent.ArtistAlbumListRemoteMediator
import cn.xybbz.page.parent.ArtistMusicListRemoteMediator
import cn.xybbz.page.parent.ArtistRemoteMediator
import cn.xybbz.page.parent.FavoriteMusicRemoteMediator
import cn.xybbz.page.parent.GenreAlbumListRemoteMediator
import cn.xybbz.page.parent.GenresRemoteMediator
import cn.xybbz.page.parent.MusicRemoteMediator
import cn.xybbz.page.parent.ResemblanceArtistRemoteMediator
import coil.Coil
import coil.ImageLoader
import com.github.promeg.pinyinhelper.Pinyin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import java.net.SocketTimeoutException
import java.util.UUID


abstract class IDataSourceParentServer(
    private val db: DatabaseClient,
    private val connectionConfigServer: ConnectionConfigServer,
    private val application: Context,
    val defaultParentApiClient: DefaultParentApiClient
) : IDataSourceServer {

    private var ifTmpObject = false

    override fun ifTmpObject(): Boolean {
        return ifTmpObject
    }

    override fun updateIfTmpObject(ifTmp: Boolean) {
        ifTmpObject = ifTmp
    }

    /**
     * 获得当前数据源类型
     */
    abstract fun getDataSourceType(): DataSourceType

    /**
     * 根据输入地址获取服务器信息
     * @param [clientLoginInfoReq] 输入信息
     */
    override suspend fun addClientAndLogin(clientLoginInfoReq: ClientLoginInfoReq): Flow<ClientLoginInfoState> {

        return flow {
            Log.i("=====", "输入的地址: ${clientLoginInfoReq.address}")
            emit(ClientLoginInfoState.Connected(clientLoginInfoReq.address))
            var deviceId = getDeviceId()
            var connectionConfig: ConnectionConfig? = null
            clientLoginInfoReq.connectionId?.let {
                connectionConfig = db.connectionConfigDao.selectById(it)
                connectionConfig?.deviceId?.let { device ->
                    if (device.isNotBlank())
                        deviceId = device
                }

            }

            //保存客户端数据
            createApiClient(
                clientLoginInfoReq.address,
                deviceId,
                username = clientLoginInfoReq.username,
                password = clientLoginInfoReq.password
            )

            //获得服务端信息
            val responseData =
                defaultParentApiClient.login(clientLoginInfoReq)
            Log.i("=====", "返回响应值: $responseData")

            //开始校验版本
            val version = responseData.version
            if (!version.isNullOrBlank()) {
                val versionAtLeast = isVersionAtLeast(version)
                if (!versionAtLeast) {
                    throw ServiceException(
                        application.getString(
                            R.string.server_version_too_low,
                            version,
                            getDataSourceType().version
                        )
                    )
                }
            } else {
                throw ServiceException(application.getString(R.string.server_version_cannot_be_obtained))
            }
            val accessToken =
                responseData.accessToken
            val userId =
                responseData.userId

            val encryptAES = PasswordUtils.encryptAES(clientLoginInfoReq.password)

            connectionConfig = connectionConfig?.let {
                connectionConfig.copy(
                    serverId = responseData.serverId ?: "",
                    name = responseData.serverName ?: getDataSourceType().title,
                    serverName = responseData.serverName ?: "",
                    address = clientLoginInfoReq.address,
                    type = getDataSourceType(),
                    userId = userId.toString(),
                    username = clientLoginInfoReq.username,
                    accessToken = accessToken,
                    currentPassword = encryptAES.aesData,
                    iv = encryptAES.aesIv,
                    key = encryptAES.aesKey,
                    serverVersion = version,
                    updateTime = System.currentTimeMillis(),
                    lastLoginTime = System.currentTimeMillis(),
                    deviceId = deviceId,
                    navidromeExtendToken = responseData.navidromeExtendToken,
                    navidromeExtendSalt = responseData.navidromeExtendSalt,
                    machineIdentifier = responseData.machineIdentifier
                )
            } ?: ConnectionConfig(
                serverId = responseData.serverId ?: "",
                serverName = responseData.serverName ?: "",
                name = responseData.serverName ?: getDataSourceType().title,
                address = clientLoginInfoReq.address,
                type = getDataSourceType(),
                userId = userId.toString(),
                username = clientLoginInfoReq.username,
                accessToken = accessToken,
                currentPassword = encryptAES.aesData,
                iv = encryptAES.aesIv,
                key = encryptAES.aesKey,
                serverVersion = version,
                deviceId = deviceId,
                navidromeExtendToken = responseData.navidromeExtendToken,
                navidromeExtendSalt = responseData.navidromeExtendSalt,
                machineIdentifier = responseData.machineIdentifier
            )
            emitAll(loginAfter(connectionConfig))
        }.flowOn(Dispatchers.IO).catch {
            it.printStackTrace()
            connectionConfigServer.updateLoginStates(true)
            when (it) {
                is SocketTimeoutException -> {
                    emit(ClientLoginInfoState.ServiceTimeOutState)
                }

                is ConnectionException -> {
                    emit(ClientLoginInfoState.ConnectError)
                }

                is UnauthorizedException -> {
                    emit(ClientLoginInfoState.UnauthorizedErrorState)
                }

                else -> {
                    emit(ClientLoginInfoState.ErrorState(it))
                }
            }
        }
    }

    /**
     * 登录后的数据
     */
    private fun loginAfter(connectionConfig: ConnectionConfig): Flow<ClientLoginInfoState> {
        return flow {
            db.withTransaction {
                val connectionId = if (connectionConfig.id != 0L) {
                    db.connectionConfigDao.update(connectionConfig)
                    connectionConfig.id
                } else {
                    db.connectionConfigDao.save(connectionConfig)
                }
                if (!ifTmpObject()) {
                    connectionConfigServer.setConnectionConfigData(connectionConfig.copy(id = connectionId))
//                    selectMediaLibrary()
                    MessageUtils.sendDismiss()
                    setCoilImageOkHttpClient()
                    connectionConfigServer.updateLoginStates(true)
//                    initFavoriteData()
                    /* try {
                         getDataInfoCount(connectionId)
                     } catch (e: Exception) {
                         Log.i(
                             Constants.LOG_ERROR_PREFIX,
                             "获取音乐/专辑/艺术家/收藏/流派数量异常",
                             e
                         )
                     }*/
                }
            }

            emit(ClientLoginInfoState.UserLoginSuccess)
        }

    }

    /**
     * 获得设备id
     */
    open fun getDeviceId(): String {
        return UUID.randomUUID().toString()
    }

    /**
     * 创建连接客户端
     * @param [address] 地址
     */
    abstract suspend fun createApiClient(
        address: String,
        deviceId: String,
        username: String,
        password: String
    )

    /**
     * 设置okhttp到数据源
     */
    @androidx.annotation.OptIn(UnstableApi::class)
    open suspend fun setCoilImageOkHttpClient() {
        val imageLoader = ImageLoader.Builder(application)
            .okHttpClient(getOkhttpClient())
            .build()
        Coil.setImageLoader(imageLoader)
    }

    /**
     * 自动登录
     */
    override suspend fun autoLogin(ifLogin: Boolean): Flow<ClientLoginInfoState> {


        //获得启用的连接信息
        val connectionConfig = db.connectionConfigDao.selectConnectionConfig() ?: return flowOf(
            ClientLoginInfoState.SelectServer
        )

        val address = connectionConfig.address

        val packageManager = application.packageManager
        val packageName = application.packageName
        val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
        val appName = packageManager.getApplicationLabel(applicationInfo).toString()

        //判断是否能连接
        return flow {

            var password = connectionConfig.currentPassword
            if (connectionConfig.key.isNotBlank() && connectionConfig.iv.isNotBlank() && connectionConfig.currentPassword.isNotBlank()) {
                password = PasswordUtils.decryptAES(
                    EncryptAesData(
                        aesKey = connectionConfig.key,
                        aesIv = connectionConfig.iv,
                        aesData = connectionConfig.currentPassword
                    )
                )
            }
            val clientLoginInfoReq = ClientLoginInfoReq(
                username = connectionConfig.username,
                password = password,
                address = address,
                appName = appName,
                clientVersion = getDataSourceType().version,
                connectionId = connectionConfig.id,
                serverVersion = connectionConfig.serverVersion,
                serverName = connectionConfig.serverName,
                serverId = connectionConfig.serverId,
            )
            if (ifLogin || connectionConfig.accessToken.isNullOrBlank()) {

                MessageUtils.sendPopTipHint(
                    R.string.logging_in,
                    delay = 5000
                )
                emitAll(
                    addClientAndLogin(
                        clientLoginInfoReq = clientLoginInfoReq
                    )
                )
            } else {
                //保存客户端数据
                createApiClient(
                    address,
                    connectionConfig.deviceId,
                    username = connectionConfig.username,
                    password = password
                )
                defaultParentApiClient.loginAfter(
                    connectionConfig.accessToken,
                    connectionConfig.userId,
                    connectionConfig.navidromeExtendToken,
                    connectionConfig.navidromeExtendSalt,
                    clientLoginInfoReq = clientLoginInfoReq
                )
                defaultParentApiClient.pingAfter(connectionConfig.machineIdentifier)
                emitAll(loginAfter(connectionConfig))
            }


        }.flowOn(Dispatchers.IO).catch {
            Log.e(Constants.LOG_ERROR_PREFIX, "自动登录异常 ${it.message}", it)
            connectionConfigServer.updateLoginStates(true)
            when (it) {
                is SocketTimeoutException -> {
                    emit(ClientLoginInfoState.ServiceTimeOutState)
                }

                is UnauthorizedException -> {
                    emit(ClientLoginInfoState.UnauthorizedErrorState)
                }

                else -> {
                    emit(ClientLoginInfoState.ErrorState(it))
                }
            }
        }
    }

    /**
     * 检验链接版本是否大于等于支持最小版本
     */
    fun isVersionAtLeast(currentVersion: String): Boolean {

        val version = currentVersion
            .lowercase()                 // 转小写，避免 "V1.2.3" 这种
            .replace(Regex("[^0-9.]"), ".") // 把非数字和点替换成点
            .replace(Regex("\\.+"), ".")    // 合并多余的点
            .trim('.')

        val curParts = version.split(".").map { it.toIntOrNull() ?: 0 }
        val tarParts = getDataSourceType().version.split(".").map { it.toIntOrNull() ?: 0 }
        val maxLength = maxOf(curParts.size, tarParts.size)

        for (i in 0 until maxLength) {
            val c = curParts.getOrElse(i) { 0 }
            val t = tarParts.getOrElse(i) { 0 }
            if (c > t) {
                return true
            } else if (c < t) {
                return false
            }
        }
        return true // 全部相等
    }


    /**
     * 获得艺术家
     */
    @OptIn(ExperimentalPagingApi::class)
    override fun selectArtistFlowList(): Flow<PagingData<XyArtistExt>> {
        return bigPager(
            remoteMediator = ArtistRemoteMediator(
                db = db,
                datasourceServer = this,
                dataSource = getDataSourceType(),
                connectionId = connectionConfigServer.getConnectionId()
            )
        ) {
            db.artistDao.selectListPagingSource()
        }.flow
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
        itemId: String,
        dataType: MusicDataTypeEnum,
        sortFlow: StateFlow<Sort>
    ): Flow<PagingData<XyMusic>> {
        return defaultPager(
            remoteMediator = AlbumOrPlaylistMusicListRemoteMediator(
                itemId = itemId,
                datasourceServer = this,
                db = db,
                dataType = dataType,
                connectionId = connectionConfigServer.getConnectionId(),
                sortFlow = sortFlow
            )
        ) {
            if (dataType == MusicDataTypeEnum.ALBUM)
                db.musicDao.selectAlbumMusicListPage(albumId = itemId)
            else
                db.musicDao.selectPlaylistMusicListPage(playlistId = itemId)
        }.flow
    }

    /**
     * 根据艺术家获得音乐列表
     */
    @OptIn(ExperimentalPagingApi::class)
    override fun selectMusicListByArtistId(artistId: String): Flow<PagingData<XyMusic>> {
        return defaultPager(
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
     * 获得随机音乐
     */
    override suspend fun getRandomMusicExtendList(
        pageSize: Int,
        pageNum: Int
    ): List<XyPlayMusic>? {
        return transitionPlayMusic(
            getRandomMusicList(pageSize, pageNum)
        )
    }

    /**
     * 获得专辑列表数据
     */
    @OptIn(ExperimentalPagingApi::class)
    override fun selectAlbumFlowList(
        sortFlow: StateFlow<Sort>
    ): Flow<PagingData<XyAlbum>> {
        return defaultPager(
            pageSize = Constants.UI_LIST_PAGE,
            remoteMediator = AlbumRemoteMediator(
                dataSource = getDataSourceType(),
                db = db,
                datasourceServer = this,
                connectionId = connectionConfigServer.getConnectionId(),
                sortFlow = sortFlow
            )
        ) {
            db.albumDao.selectHomeAlbumListPage()
        }.flow
    }

    /**
     * 根据艺术家获得专辑列表
     */
    @OptIn(ExperimentalPagingApi::class)
    override fun selectAlbumListByArtistId(artistId: String): Flow<PagingData<XyAlbum>> {
        return defaultPager(
            remoteMediator = getAlbumListRemoteMediator(artistId = artistId)
        ) {
            db.albumDao.selectArtistAlbumListPage(
                artistId
            )
        }.flow
    }

    /**
     * 获得收藏歌曲列表
     */
    @OptIn(ExperimentalPagingApi::class)
    override fun selectFavoriteMusicFlowList(): Flow<PagingData<XyMusic>> {
        return defaultPager(
            remoteMediator = FavoriteMusicRemoteMediator(
                datasourceServer = this,
                db = db,
                connectionId = connectionConfigServer.getConnectionId()
            )
        ) {
            db.musicDao.selectFavoriteMusicListPage()
        }.flow
    }


    /**
     * 获得流派内音乐列表/或者专辑
     * @param [genreId] 流派id
     */
    @OptIn(ExperimentalPagingApi::class)
    override fun selectAlbumListByGenreId(genreId: String): Flow<PagingData<XyAlbum>> {
        return defaultPager(
            remoteMediator = GenreAlbumListRemoteMediator(
                genreId = genreId,
                datasourceServer = this,
                db = db,
                connectionId = connectionConfigServer.getConnectionId()
            )
        ) {
            db.albumDao.selectGenreAlbumListPage(genreId)
        }.flow
    }


    /**
     * 获得流派列表
     */
    @OptIn(ExperimentalPagingApi::class)
    override suspend fun selectGenresPage(): Flow<PagingData<XyGenre>> {
        return defaultPager(
            remoteMediator = GenresRemoteMediator(
                datasourceServer = this,
                db = db,
                connectionId = connectionConfigServer.getConnectionId()
            )
        ) {
            db.genreDao.selectByDataSourceType()
        }.flow
    }


    /**
     * 获得音乐列表数据
     */
    @OptIn(ExperimentalPagingApi::class)
    override fun selectMusicFlowList(
        sortFlow: StateFlow<Sort>
    ): Flow<PagingData<HomeMusic>> {
        return defaultPager(
            remoteMediator = MusicRemoteMediator(
                db = db,
                datasourceServer = this,
                connectionId = connectionConfigServer.getConnectionId(),
                sort = sortFlow
            )
        ) {
            db.musicDao.selectHomeMusicListPage()
        }.flow
    }


    /**
     * 增加歌单
     * @param [name] 名称
     * @return [String?] 歌单id
     */
    override suspend fun addPlaylist(name: String): Boolean {
        return try {
            val playlistId = createPlaylist(name)
            if (!playlistId.isNullOrBlank()) {

                val album = XyAlbum(
                    itemId = playlistId,
                    name = name,
                    connectionId = connectionConfigServer.getConnectionId(),
                    ifPlaylist = true,
                    musicCount = 0
                )
                db.albumDao.save(
                    album
                )
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }

    }

    /**
     * 保存自建歌单中的音乐
     * @param [playlistId] 歌单id
     * @param [musicIds] 音乐id集合
     * @param [pic] 自建歌单图片
     */
    override suspend fun saveMusicPlaylist(
        playlistId: String,
        musicIds: List<String>
    ): Boolean {
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
        db.albumDao.updatePic(playlistId)
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
        var xyAlbum = db.albumDao.selectById(albumId)

        if (xyAlbum == null) {
            xyAlbum = selectAlbumInfoByRemotely(albumId, dataType)
        } else {
            val ifFavorite = db.albumDao.selectFavoriteById(albumId) == true
            xyAlbum = xyAlbum.copy(ifFavorite = ifFavorite)
        }
        return xyAlbum
    }

    /**
     * 根据id获得艺术家信息
     * @param [artistId] 艺术家id
     * @return [List<ArtistItem>?] 艺术家信息
     */
    override suspend fun selectArtistInfoById(artistId: String): XyArtist? {
        var artistInfo: XyArtist? = db.artistDao.selectById(artistId)
        if (artistInfo == null) {
            artistInfo = selectArtistInfoByRemotely(artistId)
        } else {
            val ifFavorite = db.artistDao.selectFavoriteById(artistId) == true
            artistInfo = artistInfo.copy(ifFavorite = ifFavorite)
        }

        return artistInfo
    }

    /**
     * 获得最近播放音乐列表
     */
    override suspend fun getPlayRecordMusicList(pageSize: Int): List<XyMusic> {
        return db.musicDao.selectPlayHistoryMusicList(pageSize)
    }


    /**
     * 批量写入艺术家
     * @param [items] 艺术家信息
     */
    @Transaction
    open suspend fun saveBatchArtist(items: List<XyArtist>) {
        if (items.isNotEmpty()) {
            db.artistDao.saveArtistBatch(
                items,
                connectionConfigServer.getConnectionId()
            )
        }
    }

    /**
     * 存储专辑和音乐中的艺术家
     */
    suspend fun saveBatchAlbum(
        baseItemList: List<XyAlbum>,
        dataType: MusicDataTypeEnum,
        ifPlaylist: Boolean = false,
        artistId: String? = null,
        genreId: String? = null
    ): List<XyAlbum> {
        val albumList = baseItemList.map { it.copy(ifPlaylist = ifPlaylist) }
        if (albumList.isNotEmpty()) {
            db.albumDao.saveBatch(
                data = albumList,
                dataType = dataType,
                connectionId = connectionConfigServer.getConnectionId(),
                artistId = artistId,
                genreId = genreId
            )
        }
        return albumList
    }

    /**
     * 批量写入音乐
     * @param [items] 艺术家信息
     */
    @Transaction
    open suspend fun saveBatchMusic(
        items: List<XyMusic>,
        dataType: MusicDataTypeEnum,
        artistId: String? = null,
        playlistId: String? = null
    ) {
        if (items.isNotEmpty()) {
            db.musicDao.saveBatch(
                data = items,
                dataType = dataType,
                connectionId = connectionConfigServer.getConnectionId(),
                artistId = artistId,
                playlistId = playlistId
            )
        }
    }

    /**
     * 批量写入流派
     * @param [items] 艺术家信息
     */
    @Transaction
    open suspend fun saveBatchGenre(
        items: List<XyGenre>
    ) {
        if (items.isNotEmpty()) {
            db.genreDao.saveBatch(
                data = items
            )
        }
    }

    /**
     * 根据id集合获得艺术家信息集合
     * @param [artistIds] 艺术家id
     * @return [List<ArtistItem>?] 艺术家信息
     */
    override suspend fun selectArtistInfoByIds(artistIds: List<String>): List<XyArtist> {
        return try {
            val tmpXyArtists = mutableListOf<XyArtist>()
            val artistItems = db.artistDao.selectByIds(artistIds)
            if (artistItems.isEmpty()) {
                val items = selectArtistsByIds(artistIds)
                tmpXyArtists.addAll(items)
            } else if (artistItems.size < artistIds.size) {
                tmpXyArtists.addAll(artistItems)
                val newArtistIds = artistItems.map { it.artistId }
                val noRepeated = newArtistIds.subtract(artistIds.toSet())
                if (noRepeated.isNotEmpty()) {
                    val items = selectArtistsByIds(noRepeated.map { it })
                    tmpXyArtists.addAll(items)
                }
            } else {
                tmpXyArtists.addAll(artistItems)
            }
            return tmpXyArtists
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "根据id集合获得艺术家信息集合失败", e)
            emptyList()
        }

    }

    /**
     * 获得所有收藏数据
     */
    open suspend fun initFavoriteData() {

    }


    /**
     * 获得专辑,艺术家,音频,歌单数量
     */
    suspend fun updateOrSaveDataInfoCount(
        music: Int?,
        album: Int?,
        artist: Int?,
        playlist: Int?,
        genres: Int?,
        favorite: Int?,
        connectionId: Long
    ) {
        val dataCount = db.dataCountDao.selectOne(connectionId)
        if (dataCount != null) {
            db.dataCountDao.update(
                XyDataCount(
                    connectionId = connectionId,
                    musicCount = music ?: dataCount.musicCount,
                    albumCount = album ?: dataCount.albumCount,
                    artistCount = artist ?: dataCount.artistCount,
                    playlistCount = playlist ?: dataCount.playlistCount,
                    genreCount = genres ?: dataCount.genreCount,
                    favoriteCount = favorite ?: dataCount.favoriteCount
                )
            )
        } else {
            db.dataCountDao.save(
                XyDataCount(
                    connectionId = connectionId,
                    musicCount = music ?: 0,
                    albumCount = album ?: 0,
                    artistCount = artist ?: 0,
                    playlistCount = playlist ?: 0,
                    genreCount = genres ?: 0,
                    favoriteCount = favorite ?: 0
                )
            )
        }
    }

    /**
     * 释放
     */
    override suspend fun release() {
        TokenServer.clearAllData()
    }


    /**
     * 创建歌单
     */
    abstract suspend fun createPlaylist(name: String): String?

    /**
     * 获得艺术家列表
     */
    abstract suspend fun getArtistList(
        startIndex: Int,
        pageSize: Int,
        isFavorite: Boolean? = null,
        search: String? = null,
    ): XyResponse<XyArtist>

    /**
     * 从远程获得专辑信息
     */
    abstract suspend fun selectAlbumInfoByRemotely(
        albumId: String,
        dataType: MusicDataTypeEnum
    ): XyAlbum?

    /**
     * 从远程获得艺术家信息
     */
    abstract override suspend fun selectArtistInfoByRemotely(artistId: String): XyArtist?


    /**
     * 根据艺术家id获得艺术家列表
     */
    abstract suspend fun selectArtistsByIds(artistIds: List<String>): List<XyArtist>


    /**
     * 根据艺术家获得音乐列表
     */
    abstract suspend fun selectMusicListByArtistServer(
        artistId: String,
        pageSize: Int,
        startIndex: Int
    ): XyResponse<XyMusic>

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
    abstract override suspend fun getRemoteServerMusicListByAlbumOrPlaylist(
        startIndex: Int,
        pageSize: Int,
        isFavorite: Boolean?,
        sortType: SortTypeEnum?,
        years: List<Int>?,
        parentId: String,
        dataType: MusicDataTypeEnum
    ): XyResponse<XyMusic>

    /**
     * 获得相似歌手列表
     */
    @OptIn(ExperimentalPagingApi::class)
    override fun getResemblanceArtist(artistId: String): Flow<PagingData<XyArtist>> {
        return defaultPager(
            pageSize = Constants.MIN_PAGE
        ) {
            ResemblanceArtistRemoteMediator(
                artistId = artistId,
                datasourceServer = this
            )
        }.flow
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
    abstract suspend fun getRemoteServerAlbumList(
        startIndex: Int,
        pageSize: Int,
        sortType: SortTypeEnum? = null,
        isFavorite: Boolean? = null,
        years: List<Int>? = null,
        artistId: String? = null,
        genreId: String? = null,
    ): XyResponse<XyAlbum>

    /**
     * 远程获得相似艺术家
     */
    abstract suspend fun getSimilarArtistsRemotely(
        artistId: String,
        startIndex: Int,
        pageSize: Int
    ): XyResponse<XyArtist>

    /**
     * 获得专辑列表的RemoteMediator
     */
    @OptIn(ExperimentalPagingApi::class)
    open fun getAlbumListRemoteMediator(artistId: String): RemoteMediator<Int, XyAlbum>? {
        return ArtistAlbumListRemoteMediator(
            artistId = artistId,
            datasourceServer = this,
            db = db,
            connectionId = connectionConfigServer.getConnectionId()
        )
    }

    /**
     * 获取远程服务器收藏音乐列表
     * @param [startIndex] 启动索引
     * @param [pageSize] 页面大小
     * @param [isFavorite] 是最喜欢
     * @return [AllResponse<XyMusic>]
     */
    abstract suspend fun getRemoteServerFavoriteMusicList(
        startIndex: Int,
        pageSize: Int,
        isFavorite: Boolean
    ): XyResponse<XyMusic>

    /**
     * 获取远程服务器流派列表
     * @param [startIndex] 启动索引
     * @param [pageSize] 页面大小
     * @return [AllResponse<XyGenre>]
     */
    abstract suspend fun getRemoteServerGenreList(
        startIndex: Int,
        pageSize: Int
    ): XyResponse<XyGenre>


    /**
     * 获取远程服务器音乐列表
     * @param [startIndex] 启动索引
     * @param [pageSize] 页面大小
     * @param [isFavorite] 是最喜欢
     * @param [sortType] 排序类型
     * @param [years] 年
     * @return [AllResponse<XyMusic>]
     */
    abstract suspend fun getRemoteServerMusicList(
        startIndex: Int,
        pageSize: Int,
        isFavorite: Boolean?,
        sortType: SortTypeEnum?,
        years: List<Int>?
    ): XyResponse<XyMusic>

    /**
     * getMusicListByAlbumId
     */
    override suspend fun getMusicListByAlbumId(
        albumId: String,
        pageSize: Int,
        pageNum: Int
    ): List<XyPlayMusic>? {
        return db.musicDao.selectMusicExtendListByAlbumId(albumId, pageSize, pageNum * pageSize)
    }

    /**
     * 获得歌曲列表
     */
    override suspend fun getMusicList(
        pageSize: Int,
        pageNum: Int
    ): List<XyPlayMusic>? {
        return db.musicDao.selectMusicExtendList(pageSize, pageNum * pageSize)
    }

    suspend fun transitionPlayMusic(musicList: List<XyMusic>?): List<XyPlayMusic>? {
        val downloads = musicList?.map { it.itemId }?.let {
            db.downloadDao.getMusicByMusicIds(it)
        }
        val downloadMap = downloads?.associateBy { it.uid }

        return musicList?.map { music ->
            music.toPlayMusic()
                .copy(filePath = if (downloadMap?.containsKey(music.itemId) == true) downloadMap[music.itemId]?.filePath else null)
        }
    }

    suspend fun transitionMusicExtend(musicList: List<XyMusic>?): List<XyMusicExtend>? {
        val downloads = musicList?.map { it.itemId }?.let {
            db.downloadDao.getMusicByMusicIds(it)
        }
        val downloadMap = downloads?.associateBy { it.uid }

        return musicList?.map { music ->
            XyMusicExtend(
                music = music,
                filePath = if (downloadMap?.containsKey(music.itemId) == true) downloadMap[music.itemId]?.filePath else null
            )
        }
    }

    /**
     * 根据艺术家获得歌曲列表
     */
    override suspend fun getMusicListByArtistId(
        artistId: String,
        pageSize: Int,
        pageNum: Int
    ): List<XyPlayMusic>? {
        return db.musicDao.selectMusicExtendListByArtistId(artistId, pageSize, pageNum * pageSize)
    }

    /**
     * 获得收藏歌曲列表
     */
    override suspend fun getMusicListByFavorite(
        pageSize: Int,
        pageNum: Int
    ): List<XyPlayMusic>? {
        return db.musicDao.selectMusicExtendListByFavorite(pageSize, pageNum * pageSize)
    }

    /**
     * 创建下载链接
     */
    abstract fun createDownloadUrl(musicId: String): String


    /**
     * 汉字转拼音
     */
    fun toLatinCompat(text: String): String? {
        if (text.isBlank()) return null

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Transliterator.getInstance("Han-Latin").transliterate(text)
        } else {
            Pinyin.toPinyin(text[0]) // 或其他兼容库
        }

    }

}