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

package cn.xybbz.localdata.dao.album

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import cn.xybbz.localdata.data.album.ArtistAlbum
import cn.xybbz.localdata.data.album.FavoriteAlbum
import cn.xybbz.localdata.data.album.GenreAlbum
import cn.xybbz.localdata.data.album.HomeAlbum
import cn.xybbz.localdata.data.album.MaximumPlayAlbum
import cn.xybbz.localdata.data.album.NewestAlbum
import cn.xybbz.localdata.data.album.PlayHistoryAlbum
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDataBatch(data: List<XyAlbum>)

    @Transaction
    suspend fun saveBatch(
        data: List<XyAlbum>,
        dataType: MusicDataTypeEnum,
        connectionId: Long,
        artistId: String? = null,
        genreId: String? = null
    ) {
        saveDataBatch(data)
        //写入FavoriteAlbum
        val favoriteAlbums = data.filter { it.ifFavorite }.map {
            FavoriteAlbum(
                it.itemId,
                connectionId
            )
        }
        if (favoriteAlbums.isNotEmpty())
            saveBatchFavoriteAlbum(favoriteAlbums)
        when (dataType) {
            MusicDataTypeEnum.HOME -> {
                var index = selectHomeIndex() ?: -1
                saveHomeAlbum(data.map {
                    index += 1
                    HomeAlbum(
                        albumId = it.itemId,
                        index = index,
                        connectionId = connectionId
                    )
                })
            }

            MusicDataTypeEnum.FAVORITE -> {}
            MusicDataTypeEnum.ALBUM -> {}
            MusicDataTypeEnum.ARTIST -> {
                artistId?.let { artist ->
                    var index = selectArtistIndex() ?: -1
                    saveArtistAlbum(data.map {
                        index += 1
                        ArtistAlbum(
                            artistId = artistId,
                            albumId = it.itemId,
                            index = index,
                            connectionId = connectionId
                        )
                    })
                }
            }

            MusicDataTypeEnum.PLAY_HISTORY -> {
                var index = selectPlayHistoryIndex() ?: -1
                savePlayHistoryAlbum(
                    data.map {
                        index += 1
                        PlayHistoryAlbum(
                            albumId = it.itemId,
                            index = index,
                            connectionId = connectionId
                        )
                    }
                )
            }

            MusicDataTypeEnum.PLAY_QUEUE -> {}
            MusicDataTypeEnum.MAXIMUM_PLAY -> {

                var index = selectPlayHistoryIndex() ?: -1
                saveMaximumPlayAlbum(
                    data.map {
                        index += 1
                        MaximumPlayAlbum(
                            albumId = it.itemId,
                            index = index,
                            connectionId = connectionId
                        )
                    }
                )

            }

            MusicDataTypeEnum.NEWEST -> {
                var index = selectNewestIndex() ?: -1
                saveNewestAlbum(data.map {
                    index += 1
                    NewestAlbum(
                        albumId = it.itemId,
                        index = index,
                        connectionId = connectionId
                    )
                })
            }

            MusicDataTypeEnum.GENRE -> {
                genreId?.let { genreId ->
                    var index = selectNewestIndex() ?: -1
                    saveGenrePlayAlbum(data.map {
                        index += 1
                        GenreAlbum(
                            albumId = it.itemId,
                            genreId = genreId,
                            index = index,
                            connectionId = connectionId
                        )
                    })
                }

            }

            MusicDataTypeEnum.PLAYLIST -> {}
            MusicDataTypeEnum.RECOMMEND -> {}
        }

    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveHomeAlbum(data: List<HomeAlbum>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveArtistAlbum(data: List<ArtistAlbum>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveNewestAlbum(data: List<NewestAlbum>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePlayHistoryAlbum(data: List<PlayHistoryAlbum>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMaximumPlayAlbum(data: List<MaximumPlayAlbum>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGenrePlayAlbum(data: List<GenreAlbum>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveBatchFavoriteAlbum(data: List<FavoriteAlbum>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFavoriteAlbum(data: FavoriteAlbum)

    @Query("select `index` from homealbum where connectionId = (select connectionId from xy_settings) order by `index` desc limit 1")
    suspend fun selectHomeIndex(): Int?

    @Query("select `index` from homealbum where connectionId = (select connectionId from xy_settings) order by `index` desc limit 1")
    suspend fun selectArtistIndex(): Int?

    @Query("select `index` from newestalbum where connectionId = (select connectionId from xy_settings) order by `index` desc limit 1")
    suspend fun selectNewestIndex(): Int?

    @Query("select `index` from playhistoryalbum where connectionId = (select connectionId from xy_settings) order by `index` desc limit 1")
    suspend fun selectPlayHistoryIndex(): Int?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun save(data: XyAlbum): Long

    @Update
    suspend fun update(data: XyAlbum)

    @Query("update xy_album set name = :name where itemId = :itemId and connectionId = (select connectionId from xy_settings)")
    suspend fun updateName(itemId: String, name: String)

    @Query("update xy_album set pic = (select pic from xy_music where itemId = (select musicId from playlistmusic where playlistId = :itemId) and connectionId = (select connectionId from xy_settings)) where itemId = :itemId and connectionId = (select connectionId from xy_settings)")
    suspend fun updatePic(itemId: String)

    @Query("update xy_album set pic = :pic,musicCount = (select count(musicId) from playlistmusic where playlistId = :itemId) where itemId = :itemId and connectionId = (select connectionId from xy_settings)")
    suspend fun updatePicAndCount(itemId: String, pic: String)

    /**
     * 按艺术家id获得音乐分页列表
     * @param [artistId] 艺术家id
     * @return [PagingSource<Int, MusicArtistExtend>]
     */
    @Transaction
    @Query(
        """
        select xa.* from artistalbum aa
        inner join xy_album xa on aa.albumId = xa.itemId
        inner join xy_settings xs on xa.connectionId = xs.connectionId and aa.connectionId = xs.connectionId
        where  aa.artistId = :artistId
    """
    )
    fun selectArtistAlbumListPage(artistId: String): PagingSource<Int, XyAlbum>

    /**
     * 获得流派专辑分页信息
     */
    @Transaction
    @Query(
        """
        select mi.* from genrealbum ga
        inner join xy_album mi on ga.albumId = mi.itemId
        inner join xy_settings xs on mi.connectionId = xs.connectionId and ga.connectionId = xs.connectionId
        where ga.genreId = :genreId
        order by `index`
    """
    )
    fun selectGenreAlbumListPage(genreId: String): PagingSource<Int, XyAlbum>

    /**
     * 获得专辑分页信息
     */
    @Transaction
    @Query(
        """
        select mi.* from homealbum ha
        inner join xy_album mi on ha.albumId = mi.itemId
        inner join xy_settings xs on mi.connectionId = xs.connectionId and ha.connectionId = xs.connectionId
        order by `index`
    """
    )
    fun selectHomeAlbumListPage(): PagingSource<Int, XyAlbum>

    /**
     * 根据数据类型删除音乐
     */
    @Transaction
    suspend fun removeByType(
        dataType: MusicDataTypeEnum,
        artistId: String? = null,
        genreId: String? = null
    ) {
        when (dataType) {
            MusicDataTypeEnum.HOME -> {
                removeHomeAlbum()
            }

            MusicDataTypeEnum.FAVORITE -> {}
            MusicDataTypeEnum.ALBUM -> {}
            MusicDataTypeEnum.ARTIST -> {
                artistId?.let {
                    removeArtistAlbum(artistId)
                }
            }

            MusicDataTypeEnum.PLAY_HISTORY -> {
                removePlayHistoryAlbum()
            }

            MusicDataTypeEnum.PLAY_QUEUE -> {}
            MusicDataTypeEnum.MAXIMUM_PLAY -> {
                removeMaximumPlayAlbum()
            }

            MusicDataTypeEnum.NEWEST -> {
                removeNewestAlbum()
            }

            MusicDataTypeEnum.GENRE -> {
                genreId?.let {
                    removeGenreAlbum(genreId)
                }
            }
            MusicDataTypeEnum.PLAYLIST -> {}
            MusicDataTypeEnum.RECOMMEND -> {}
        }
        removeByNotQuote()
    }

    @Query(
        """
        delete from homealbum where connectionId = (select connectionId from xy_settings)
    """
    )
    suspend fun removeHomeAlbum()

    @Query(
        """
        delete from ArtistAlbum where artistId = :artistId and connectionId = (select connectionId from xy_settings)
    """
    )
    suspend fun removeArtistAlbum(artistId: String)

    @Query(
        """
        delete from NewestAlbum where connectionId = (select connectionId from xy_settings)
    """
    )
    suspend fun removeNewestAlbum()


    @Query(
        """
        delete from playhistoryalbum where connectionId = (select connectionId from xy_settings)
    """
    )
    suspend fun removePlayHistoryAlbum()

    @Query(
        """
        delete from maximumplayalbum where connectionId = (select connectionId from xy_settings)
    """
    )
    suspend fun removeMaximumPlayAlbum()

    @Query(
        """
        delete from genrealbum where genreId = :genreId and connectionId = (select connectionId from xy_settings)
    """
    )
    suspend fun removeGenreAlbum(genreId: String)

    /**
     *  删除没有被引用的数据
     */
    @Query(
        """
        DELETE FROM xy_album 
        WHERE itemId NOT IN (SELECT albumId FROM homealbum)
          AND itemId NOT IN (SELECT albumId FROM genrealbum)
          AND itemId NOT IN (SELECT albumId FROM newestalbum)
          and itemId not in (select albumId from playhistoryalbum)
          and itemId not in (select albumId from maximumplayalbum)
          and itemId not in (select albumId from artistalbum)
          and ifPlaylist = 0
    """
    )
    suspend fun removeByNotQuote()


    @Query("delete from xy_album")
    suspend fun removeAll()

    /**
     * 根据链接id删除专辑
     */
    @Query("delete from xy_album where connectionId = :connectionId")
    suspend fun removeByConnectionId(connectionId: Long)

    @Query("delete from xy_album where itemId = :itemId and connectionId = (select connectionId from xy_settings)")
    suspend fun removeById(itemId: String)

    /**
     * 删除全部歌单
     */
    @Query("delete from xy_album where ifPlaylist = 1")
    suspend fun removePlaylist()

    /**
     * 根据id获得专辑信息数据
     */
    @Query(
        """
        select itemId,pic,name,artistIds,artists,genreIds,connectionId,year,
        premiereDate,ifPlaylist,musicCount,createTime from xy_album
        where itemId = :itemId and connectionId = (select connectionId from xy_settings) 
    """
    )
    suspend fun selectById(itemId: String): XyAlbum?

    /**
     * 根据id获得专辑收藏信息
     */
    @Query(
        """
        select ifFavorite from favoritealbum
        where albumId = :itemId and connectionId = (select connectionId from xy_settings) 
    """
    )
    suspend fun selectFavoriteById(itemId: String): Boolean?

    /**
     * 根据数据源获得最新专辑列表数据
     */
    @Query(
        """
        select xa.itemId,xa.pic,xa.name,xa.artistIds,xa.artists,xa.genreIds,xa.connectionId,xa.year,
        xa.premiereDate,xa.ifPlaylist,xa.musicCount,xa.createTime from newestalbum na
        inner join xy_album xa on na.albumId = xa.itemId
        inner join xy_settings xs on xa.connectionId = xs.connectionId and na.connectionId = xs.connectionId
        order by `index`
        limit :limit
    """
    )
    fun selectNewestListFlow(limit: Int): Flow<List<XyAlbum>>

    @Query(
        """
        select xa.itemId,xa.pic,xa.name,xa.artistIds,xa.artists,xa.genreIds,xa.connectionId,xa.year,
        xa.premiereDate,xa.ifPlaylist,xa.musicCount,xa.createTime from newestalbum na
        inner join xy_album xa on na.albumId = xa.itemId
        inner join xy_settings xs on xa.connectionId = xs.connectionId and na.connectionId = xs.connectionId
        order by `index`
        limit :limit
    """
    )
    suspend fun selectNewestList(limit: Int): List<XyAlbum>

    /**
     * 根据数据源获得歌单列表数据
     */
    @Query(
        """
        select xa.itemId,xa.pic,xa.name,xa.artistIds,xa.artists,xa.genreIds,xa.connectionId,xa.year,
        xa.premiereDate,xa.ifPlaylist,xa.musicCount,xa.createTime,
        (select count(musicId) from playlistmusic where connectionId = (select connectionId from xy_settings) and playlistId = xa.itemId) as musicCount 
        from xy_album xa 
        inner join xy_settings xs on xa.connectionId = xs.connectionId
        where ifPlaylist = 1
        order by xa.createTime
    """
    )
    fun selectPlaylistFlow(): Flow<List<XyAlbum>>

    /**
     * 根据数据源获得歌单列表数据
     */
    @Query(
        """
         select xa.itemId,xa.pic,xa.name,xa.artistIds,xa.artists,xa.genreIds,xa.connectionId,xa.year,
        xa.premiereDate,xa.ifPlaylist,xa.musicCount,xa.createTime,
        (select count(musicId) from playlistmusic where connectionId = (select connectionId from xy_settings) and playlistId = xa.itemId) as musicCount 
        from xy_album xa 
        inner join xy_settings xs on xa.connectionId = xs.connectionId
        where ifPlaylist = 1
        order by xa.createTime
    """
    )
    suspend fun selectPlaylist(): List<XyAlbum>


    /**
     * 查询所有数据
     */
    @Query("""
        select itemId,pic,name,artistIds,artists,genreIds,connectionId,year,
        premiereDate,ifPlaylist,musicCount,createTime from xy_album
    """)
    suspend fun selectAllData(): List<XyAlbum>

    /**
     * 根据艺术家id获得专辑列表
     */
    @Query(
        """
        select albumId from ArtistAlbum where artistId = :artistId
    """
    )
    suspend fun selectListByArtistId(artistId: String): List<String>


    /**
     * 获得最多播放的limit条数据
     */
    @Query(
        """
        select xa.itemId,xa.pic,xa.name,xa.artistIds,xa.artists,xa.genreIds,xa.connectionId,xa.year,
        xa.premiereDate,xa.ifPlaylist,xa.musicCount,xa.createTime from maximumplayalbum mpm
        inner join xy_album xa on mpm.albumId = xa.itemId
        inner join xy_settings xs on xa.connectionId = xs.connectionId and mpm.connectionId = xs.connectionId
        order by `index`
        limit :limit
    """
    )
    fun selectMaximumPlayAlbumListFlow(
        limit: Int
    ): Flow<List<XyAlbum>>

    @Query(
        """
        select xa.itemId,xa.pic,xa.name,xa.artistIds,xa.artists,xa.genreIds,xa.connectionId,xa.year,
        xa.premiereDate,xa.ifPlaylist,xa.musicCount,xa.createTime from maximumplayalbum mpm
        inner join xy_album xa on mpm.albumId = xa.itemId
        inner join xy_settings xs on xa.connectionId = xs.connectionId and mpm.connectionId = xs.connectionId
        order by `index`
        limit :limit
    """
    )
    suspend fun selectMaximumPlayAlbumList(
        limit: Int
    ): List<XyAlbum>

    /**
     * 获得播放历史的limit条数据
     */
    @Query(
        """
        select xa.itemId,xa.pic,xa.name,xa.artistIds,xa.artists,xa.genreIds,xa.connectionId,xa.year,
        xa.premiereDate,xa.ifPlaylist,xa.musicCount,xa.createTime from playhistoryalbum phm
        inner join xy_album xa on phm.albumId = xa.itemId
        inner join xy_settings xs on xa.connectionId = xs.connectionId and phm.connectionId = xs.connectionId
        order by `index`
        limit :limit
    """
    )
    fun selectPlayHistoryAlbumListFlow(
        limit: Int
    ): Flow<List<XyAlbum>>

    @Query(
        """
        select xa.itemId,xa.pic,xa.name,xa.artistIds,xa.artists,xa.genreIds,xa.connectionId,xa.year,
        xa.premiereDate,xa.ifPlaylist,xa.musicCount,xa.createTime from playhistoryalbum phm
        inner join xy_album xa on phm.albumId = xa.itemId
        inner join xy_settings xs on xa.connectionId = xs.connectionId and phm.connectionId = xs.connectionId
        order by `index`
        limit :limit
    """
    )
    suspend fun selectPlayHistoryAlbumList(
        limit: Int
    ): List<XyAlbum>


    @Query(
        """
        update favoritealbum set ifFavorite = :ifFavorite where albumId = :itemId and connectionId = (select connectionId from xy_settings)
    """
    )
    suspend fun updateFavoriteByItemId(ifFavorite: Boolean, itemId: String)

    @Query("""
        select count(albumId) from favoritealbum where albumId = :itemId and connectionId = (select connectionId from xy_settings) 
    """)
    suspend fun selectFavoriteCount(itemId: String): Int
}