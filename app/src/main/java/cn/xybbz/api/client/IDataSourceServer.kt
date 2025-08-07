package cn.xybbz.api.client

import androidx.paging.PagingData
import cn.xybbz.api.client.jellyfin.data.ClientLoginInfoReq
import cn.xybbz.api.state.ClientLoginInfoState
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.common.enums.SortTypeEnum
import cn.xybbz.entity.data.SearchData
import cn.xybbz.entity.data.file.backup.ExportPlaylistData
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.data.artist.XyArtist
import cn.xybbz.localdata.data.genre.XyGenre
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import cn.xybbz.ui.components.LrcEntry
import kotlinx.coroutines.flow.Flow
import okhttp3.OkHttpClient

/**
 * 本地用户接口类
 * @author xybbz
 * @date 2024/06/12
 * @constructor 创建[IDataSourceServer]
 */
interface IDataSourceServer {


    /**
     * 用户登录逻辑
     */
    suspend fun addClientAndLogin(clientLoginInfoReq: ClientLoginInfoReq): Flow<ClientLoginInfoState>?

    /**
     * 自动登录
     * @return [Flow<ClientLoginInfoState>?]
     */
    suspend fun autoLogin(): Flow<ClientLoginInfoState>?

    /**
     * 获得专辑列表数据
     */
    fun selectAlbumFlowList(
        sortType: SortTypeEnum?,
        ifFavorite: Boolean?,
        years: String?
    ): Flow<PagingData<XyAlbum>>

    /**
     * 获得音乐列表数据
     */
    fun selectMusicFlowList(
        sortType: SortTypeEnum? = null,
        ifFavorite: Boolean? = null,
        years: String? = null
    ): Flow<PagingData<XyMusic>>

    /**
     * 获得艺术家
     */
    fun selectArtistFlowList(
        ifFavorite: Boolean?,
        selectChat: String?
    ): Flow<PagingData<XyArtist>>

    /**
     * 搜索音乐,艺术家,专辑
     */
    suspend fun searchAll(search: String): SearchData

    /**
     * 获得专辑或歌单内音乐列表
     * @param [sortType] 排序类型
     * @param [ifFavorite] 是否收藏筛选
     * @param [years] 筛选年代数据
     * @param [itemId] 专辑id
     * @param [dataType] 数据类型
     * @return [Flow<PagingData<XyMusic>>]
     */
    fun selectMusicListByParentId(
        sortType: SortTypeEnum?,
        ifFavorite: Boolean?,
        years: String?,
        itemId: String,
        dataType: MusicDataTypeEnum
    ): Flow<PagingData<XyMusic>>

    /**
     * 将项目标记为收藏
     * @param [itemId] 专辑/音乐id
     */
    suspend fun markFavoriteItem(itemId: String, dataType: MusicTypeEnum): Boolean

    /**
     * 取消项目收藏
     * @param [itemId] 专辑/音乐id
     */
    suspend fun unmarkFavoriteItem(itemId: String, dataType: MusicTypeEnum): Boolean

    /**
     * 获得专辑,艺术家,音频,歌单数量
     */
    suspend fun getDataInfoCount(connectionId: Long)

    /**
     * 删除数据
     * @param [musicId] 需要删除数据的id
     * @return true->删除成功,false->删除失败
     */
    suspend fun removeById(musicId: String): Boolean

    /**
     * 批量删除数据
     * 按 ID 删除
     * @param [musicIds] 需要删除数据的
     * @return [Boolean?]
     */
    suspend fun removeByIds(musicIds: List<String>): Boolean

    /**
     * 获得专辑信息
     * @param [albumId] 专辑id
     * @return 专辑+艺术家信息
     */
    suspend fun selectAlbumInfoById(albumId: String, dataType: MusicDataTypeEnum): XyAlbum?

    /**
     * 按 ID 选择音乐信息
     * @param [itemId] 音乐唯一标识
     * @return [XyMusic?]
     */
    suspend fun selectMusicInfoById(itemId: String): XyMusic?

    /**
     * 根据音乐获得歌词信息
     * @param [music] 音乐id
     * @return 返回歌词列表
     */
    suspend fun getMusicLyricList(music: XyMusic): List<LrcEntry>?

    /**
     * 根据艺术家获得专辑列表
     */
    fun selectAlbumListByArtistId(artistId: String): Flow<PagingData<XyAlbum>>

    /**
     * 根据艺术家获得音乐列表
     */
    fun selectMusicListByArtistId(artistId: String): Flow<PagingData<XyMusic>>


    /**
     * 获得随机音乐
     */
    suspend fun getRandomMusicList(pageSize: Int, pageNum: Int): List<XyMusic>?

    /**
     * 获取歌单列表
     */
    suspend fun getPlaylists(): List<XyAlbum>?

    /**
     * 增加歌单
     * @param [name] 名称
     * @return [String?] 歌单id
     */
    suspend fun addPlaylist(name: String): Boolean

    /**
     * 新增或修改歌单
     */
    suspend fun importPlaylist(playlistData: ExportPlaylistData): Boolean

    /**
     * 编辑歌单名称
     * @param [id] ID
     * @param [name] 姓名
     */
    suspend fun editPlaylistName(id: String, name: String): Boolean

    /**
     * 删除歌单
     * @param [id] ID
     */
    suspend fun removePlaylist(id: String): Boolean

    /**
     * 保存自建歌单中的音乐
     * @param [playlistId] 歌单id
     * @param [musicIds] 音乐id集合
     * @param [pic] 自建歌单图片
     */
    suspend fun saveMusicPlaylist(
        playlistId: String,
        musicIds: List<String>,
        pic: String? = ""
    ): Boolean

    /**
     * 删除自建歌单中的音乐
     * @param [playlistId] 歌单id
     * @param [musicIds] 音乐id集合
     */
    suspend fun removeMusicPlaylist(playlistId: String, musicIds: List<String>): Boolean


    /**
     * 根据id集合获得艺术家信息集合
     * @param [artistIds] 艺术家id
     * @return [List<ArtistItem>?] 艺术家信息
     */
    suspend fun selectArtistInfoByIds(artistIds: List<String>): List<XyArtist>?

    /**
     * 根据id获得艺术家信息
     * @param [artistId] 艺术家id
     * @return [List<ArtistItem>?] 艺术家信息
     */
    suspend fun selectArtistInfoById(artistId: String): XyArtist?

    /**
     * 获得媒体库列表
     */
    suspend fun selectMediaLibrary()

    /**
     * 获得最近播放音乐或专辑
     */
    suspend fun playRecordMusicOrAlbumList()

    /**
     * 获得最多播放
     */
    suspend fun getMostPlayerMusicList()

    /**
     * 获得最新专辑
     */
    suspend fun getNewestAlbumList()

    /**
     * 获得收藏歌曲列表
     */
    fun selectFavoriteMusicFlowList(): Flow<PagingData<XyMusic>>

    /**
     * 获得流派列表
     */
    suspend fun selectGenresPage(): Flow<PagingData<XyGenre>>

    /**
     * 获得流派详情
     */
    suspend fun getGenreById(genreId: String): XyGenre?

    /**
     * 获得流派内音乐列表/或者专辑
     * todo 试一下能不能查询到专辑
     * @param [genreId] 流派id
     */
    fun selectAlbumListByGenreId(genreId: String): Flow<PagingData<XyAlbum>>

    /**
     * 获得歌曲列表
     */
    suspend fun getMusicList(
        pageSize: Int,
        pageNum: Int
    ): List<XyMusic>?

    /**
     * 根据专辑获得歌曲列表
     */
    suspend fun getMusicListByAlbumId(
        albumId: String,
        pageSize: Int,
        pageNum: Int
    ): List<XyMusic>?

    /**
     * 根据艺术家获得歌曲列表
     */
    suspend fun getMusicListByArtistId(
        artistId: String,
        pageSize: Int,
        pageNum: Int
    ): List<XyMusic>?

    /**
     * 获得收藏歌曲列表
     */
    suspend fun getMusicListByFavorite(
        pageSize: Int,
        pageNum: Int
    ): List<XyMusic>?


    /**
     * 获得OkHttpClient
     */
    fun getOkhttpClient(): OkHttpClient

    /**
     * 设置token
     */
    fun setToken()

    /**
     * 上报播放状态
     */
    suspend fun reportPlaying(
        musicId: String,
        playSessionId: String,
        isPaused: Boolean = false,
        positionTicks: Long? = null
    )

    /**
     * 上报播放进度
     */
    suspend fun reportProgress(musicId: String, playSessionId: String, positionTicks: Long?)


    /**
     * 获得播放连接
     */
    suspend fun getMusicPlayUrl(musicId: String): String

    /**
     * 释放
     */
    suspend fun release()


}