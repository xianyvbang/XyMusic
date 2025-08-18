package cn.xybbz.api.client

import android.content.Context
import android.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.room.Transaction
import androidx.room.withTransaction
import cn.xybbz.api.TokenServer
import cn.xybbz.api.client.data.AllResponse
import cn.xybbz.api.client.jellyfin.data.ClientLoginInfoReq
import cn.xybbz.api.exception.ConnectionException
import cn.xybbz.api.exception.UnauthorizedException
import cn.xybbz.api.state.ClientLoginInfoState
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.common.utils.PasswordUtils
import cn.xybbz.config.ConnectionConfigServer
import cn.xybbz.entity.api.LoginSuccessData
import cn.xybbz.entity.data.EncryptAesData
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.data.artist.XyArtist
import cn.xybbz.localdata.data.connection.ConnectionConfig
import cn.xybbz.localdata.data.count.XyDataCount
import cn.xybbz.localdata.data.genre.XyGenre
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.enums.DataSourceType
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import coil.Coil
import coil.ImageLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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
) : IDataSourceServer {


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
            if (getDataSourceType().ifInputUrl)
                try {
                    val postPingSystem = postPingSystem()
                    if (postPingSystem) {
                        Log.i("=====", "是否连通: $postPingSystem")
                        emit(ClientLoginInfoState.ConnectionSuccess)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    when (e) {
                        is UnauthorizedException -> {
                            throw e
                        }

                        else -> {
                            throw ConnectionException()
                        }
                    }
                }

            //获得服务端信息
            val responseData =
                login(clientLoginInfoReq)
            Log.i("=====", "返回响应值: $responseData")
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
                    ifEnable = true,
                    serverVersion = responseData.version ?: "",
                    updateTime = System.currentTimeMillis(),
                    lastLoginTime = System.currentTimeMillis(),
                    deviceId = deviceId
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
                ifEnable = true,
                serverVersion = responseData.version ?: "",
                deviceId = deviceId
            )


            db.withTransaction {
                val connectionId = if (clientLoginInfoReq.connectionId != null) {
                    db.connectionConfigDao.update(connectionConfig)
                    clientLoginInfoReq.connectionId!!
                } else {
                    db.connectionConfigDao.save(connectionConfig)
                }
                connectionConfigServer.setConnectionConfigData(connectionConfig.copy(id = connectionId))
                setToken()
                selectMediaLibrary()
                MessageUtils.sendDismiss()
                setServerOkHttpClient()
                connectionConfigServer.updateLoginStates(true)
                initFavoriteData()
            }

            emit(ClientLoginInfoState.UserLoginSuccess)
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
     * 登录功能
     */
    abstract suspend fun login(clientLoginInfoReq: ClientLoginInfoReq): LoginSuccessData

    /**
     * 连通性检测
     */
    abstract suspend fun postPingSystem(): Boolean

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
    open suspend fun setServerOkHttpClient() {
        val imageLoader = ImageLoader.Builder(application)
            .okHttpClient(getOkhttpClient())
            .build()
        Coil.setImageLoader(imageLoader)
    }

    /**
     * 自动登录
     */
    override suspend fun autoLogin(): Flow<ClientLoginInfoState> {

        //获得启用的连接信息
        val connectionConfig = db.connectionConfigDao.selectConnectionConfig() ?: return flowOf(
            ClientLoginInfoState.SelectServer
        )

        val address = connectionConfig.address

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
            emitAll(
                addClientAndLogin(
                    clientLoginInfoReq = ClientLoginInfoReq(
                        username = connectionConfig.username,
                        password = password,
                        address = address,
                        connectionId = connectionConfig.id,
                        serverVersion = connectionConfig.serverVersion,
                        serverName = connectionConfig.serverName,
                        serverId = connectionConfig.serverId
                    )
                )
            )

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
    ): AllResponse<XyArtist>

    /**
     * 批量写入艺术家
     * @param [items] 艺术家信息
     */
    @Transaction
    open suspend fun saveBatchArtist(items: List<XyArtist>) {
        if (items.isNotEmpty()) {
            db.artistDao.saveBatch(
                items
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
    ): AllResponse<XyMusic>


    /**
     * 获得专辑,艺术家,音频,歌单数量
     */
    suspend fun updateOrSaveDataInfoCount(
        music: Int?,
        album: Int?,
        artist: Int?,
        playlist: Int?,
        genres: Int?,
        favorite: Int?
    ) {
        val dataCount = db.dataCountDao.selectOne()
        if (dataCount != null) {
            db.dataCountDao.update(
                XyDataCount(
                    connectionId = connectionConfigServer.getConnectionId(),
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
                    connectionId = connectionConfigServer.getConnectionId(),
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
}