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

import androidx.paging.PagingData
import cn.xybbz.api.client.data.ClientLoginInfoReq
import cn.xybbz.api.client.data.XyResponse
import cn.xybbz.api.enums.AudioCodecEnum
import cn.xybbz.api.state.ClientLoginInfoState
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.common.enums.SortTypeEnum
import cn.xybbz.common.utils.PlaylistParser
import cn.xybbz.entity.data.LrcEntryData
import cn.xybbz.entity.data.ResourceData
import cn.xybbz.entity.data.SearchData
import cn.xybbz.entity.data.Sort
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.data.artist.XyArtist
import cn.xybbz.localdata.data.artist.XyArtistExt
import cn.xybbz.localdata.data.genre.XyGenre
import cn.xybbz.localdata.data.music.HomeMusic
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.data.music.XyMusicExtend
import cn.xybbz.localdata.data.music.XyPlayMusic
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.OkHttpClient

/**
 * 本地用户接口类
 * @author xybbz
 * @date 2024/06/12
 * @constructor 创建[IDataSourceServer]
 */
interface IDataSourceServer {


    fun ifTmpObject(): Boolean

    fun updateIfTmpObject(ifTmp: Boolean)

    /**
     * 用户登录逻辑
     */
    suspend fun addClientAndLogin(clientLoginInfoReq: ClientLoginInfoReq): Flow<ClientLoginInfoState>?

    /**
     * 自动登录
     * @return [Flow<ClientLoginInfoState>?]
     */
    suspend fun autoLogin(ifLogin: Boolean = false): Flow<ClientLoginInfoState>?


    /**
     * 获得资源地址
     */
    suspend fun getResources(clientLoginInfoReq: ClientLoginInfoReq): List<ResourceData> {
        return emptyList()
    }

    /**
     * 获得专辑列表数据
     */
    fun selectAlbumFlowList(
        sort: Sort
    ): Flow<PagingData<XyAlbum>>

    /**
     * 获得音乐列表数据
     */
    fun selectMusicFlowList(
        sort: Sort
    ): Flow<PagingData<HomeMusic>>

    /**
     * 获得艺术家
     */
    fun selectArtistFlowList(): Flow<PagingData<XyArtistExt>>

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
        itemId: String,
        dataType: MusicDataTypeEnum,
        sort: StateFlow<Sort>
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
     * @param [itemId] 音乐id
     * @return 返回歌词列表
     */
    suspend fun getMusicLyricList(itemId: String): List<LrcEntryData>?

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
     * 获得随机音乐
     */
    suspend fun getRandomMusicExtendList(pageSize: Int, pageNum: Int): List<XyPlayMusic>?

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
     * 导入歌单
     */
    suspend fun importPlaylist(playlistData: PlaylistParser.Playlist, playlistId: String): Boolean

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
        musicIds: List<String>
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
     * 从远程获得艺术家信息
     */
    suspend fun selectArtistInfoByRemotely(artistId: String): XyArtist?

    /**
     * 获得媒体库列表
     */
    suspend fun selectMediaLibrary()

    /**
     * 获得最近播放音乐或专辑
     */
    suspend fun playRecordMusicOrAlbumList(pageSize: Int = Constants.ALBUM_MUSIC_LIST_PAGE)

    /**
     * 获得最近播放音乐列表
     */
    suspend fun getPlayRecordMusicList(pageSize: Int = Constants.MIN_PAGE): List<XyMusic>

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
     * @param [genreId] 流派id
     */
    fun selectAlbumListByGenreId(genreId: String): Flow<PagingData<XyAlbum>>

    /**
     * 获得流派内音乐列表/或者专辑
     * @param [genreIds] 流派id
     */
    suspend fun selectMusicListByGenreIds(
        genreIds: List<String>,
        pageSize: Int
    ): List<XyMusic>?

    /**
     * 获得歌曲列表
     */
    suspend fun getMusicList(
        pageSize: Int,
        pageNum: Int
    ): List<XyPlayMusic>?

    /**
     * 根据专辑获得歌曲列表
     */
    suspend fun getMusicListByAlbumId(
        albumId: String,
        pageSize: Int,
        pageNum: Int
    ): List<XyPlayMusic>?

    /**
     * 根据艺术家获得歌曲列表
     */
    suspend fun getMusicListByArtistId(
        artistId: String,
        pageSize: Int,
        pageNum: Int
    ): List<XyPlayMusic>?

    /**
     * 根据艺术家列表获得歌曲列表
     */
    suspend fun getMusicListByArtistIds(
        artistIds: List<String>,
        pageSize: Int
    ): List<XyMusic>?

    /**
     * 获得收藏歌曲列表
     */
    suspend fun getMusicListByFavorite(
        pageSize: Int,
        pageNum: Int
    ): List<XyPlayMusic>?


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
    suspend fun getRemoteServerMusicListByAlbumOrPlaylist(
        startIndex: Int,
        pageSize: Int,
        isFavorite: Boolean? = null,
        sortType: SortTypeEnum? = null,
        years: List<Int>? = null,
        parentId: String,
        dataType: MusicDataTypeEnum
    ): XyResponse<XyMusic>

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
    fun getMusicPlayUrl(
        musicId: String,
        static: Boolean = true,
        audioCodec: AudioCodecEnum? = null,
        audioBitRate: Int? = null,
        playSessionId: String
    ): String

    /**
     * 获得相似歌曲列表
     */
    suspend fun getSimilarMusicList(musicId: String): List<XyMusicExtend>?

    /**
     * 获得歌手热门歌曲列表
     */
    suspend fun getArtistPopularMusicList(
        artistId: String?,
        artistName: String? = null
    ): List<XyMusicExtend>?

    /**
     * 获得相似歌手列表
     */
    fun getResemblanceArtist(artistId: String): Flow<PagingData<XyArtist>>

    /**
     * 释放
     */
    suspend fun release()


}