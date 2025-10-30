package cn.xybbz.localdata.dao.music

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import cn.xybbz.localdata.data.music.AlbumMusic
import cn.xybbz.localdata.data.music.ArtistMusic
import cn.xybbz.localdata.data.music.FavoriteMusic
import cn.xybbz.localdata.data.music.HomeMusic
import cn.xybbz.localdata.data.music.MaximumPlayMusic
import cn.xybbz.localdata.data.music.NewestMusic
import cn.xybbz.localdata.data.music.PlayHistoryMusic
import cn.xybbz.localdata.data.music.PlayQueueMusic
import cn.xybbz.localdata.data.music.PlaylistMusic
import cn.xybbz.localdata.data.music.RecommendedMusic
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@Dao
interface XyMusicDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDataBatch(data: List<XyMusic>): List<Long>


    @Transaction
    suspend fun saveBatch(
        data: List<XyMusic>,
        dataType: MusicDataTypeEnum,
        connectionId: Long,
        artistId: String? = null,
        playlistId: String? = null
    ) {
        saveDataBatch(data)
        when (dataType) {
            MusicDataTypeEnum.HOME -> {

                var index = selectHomeIndex() ?: -1
                saveHomeMusic(data.map {
                    index += 1
                    HomeMusic(
                        musicId = it.itemId,
                        index = index,
                        connectionId = connectionId
                    )
                })
            }

            MusicDataTypeEnum.FAVORITE -> {
                var index = selectFavoriteIndex() ?: -1
                saveFavoriteMusic(data.map {
                    index += 1
                    FavoriteMusic(
                        musicId = it.itemId,
                        index = index,
                        connectionId = connectionId
                    )
                })
            }

            MusicDataTypeEnum.ALBUM -> {
                var index = selectAlbumIndex() ?: -1
                val albumMusicList = data.map {
                    index += 1
                    AlbumMusic(
                        albumId = it.album,
                        musicId = it.itemId,
                        index = index,
                        connectionId = connectionId
                    )
                }
                saveAlbumMusic(albumMusicList)
            }

            MusicDataTypeEnum.ARTIST -> {
                var index = selectArtistIndex() ?: -1
                artistId?.let { artist ->
                    saveArtistMusic(data.map {
                        index += 1
                        ArtistMusic(
                            artistId = artistId,
                            musicId = it.itemId,
                            index = index,
                            connectionId = connectionId
                        )
                    })
                }
            }

            MusicDataTypeEnum.PLAY_HISTORY -> {
                var index = selectPlayHistoryIndex() ?: -1
                savePlayHistoryMusic(data.map {
                    index += 1
                    PlayHistoryMusic(
                        musicId = it.itemId,
                        index = index,
                        connectionId = connectionId
                    )
                })
            }

            MusicDataTypeEnum.PLAY_QUEUE -> {
                var index = selectPlayQueueIndex() ?: -1
                savePlayQueueMusic(data.map {
                    index += 1
                    PlayQueueMusic(
                        musicId = it.itemId,
                        index = index,
                        connectionId = connectionId
                    )
                })
            }

            MusicDataTypeEnum.MAXIMUM_PLAY -> {
                var index = selectMaximumPlayIndex() ?: -1
                saveMaximumPlayMusic(data.map {
                    index += 1
                    MaximumPlayMusic(
                        musicId = it.itemId,
                        index = index,
                        connectionId = connectionId
                    )
                })
            }

            MusicDataTypeEnum.NEWEST -> {
                var index = selectNewestIndex() ?: -1
                saveNewestMusic(data.map {
                    index += 1
                    NewestMusic(
                        musicId = it.itemId,
                        index = index,
                        connectionId = connectionId
                    )
                })
            }

            MusicDataTypeEnum.GENRE -> {

            }

            MusicDataTypeEnum.PLAYLIST -> {
                var index = -1
                playlistId?.let {
                    val playlistMusic = data.map {
                        index += 1
                        PlaylistMusic(
                            playlistId = playlistId,
                            musicId = it.itemId,
                            index = index,
                            connectionId = connectionId
                        )
                    }
                    savePlaylistMusic(playlistMusic)
                }
            }

            MusicDataTypeEnum.RECOMMEND -> {
                saveRecommendedMusic(data.mapIndexed { index, item ->
                    RecommendedMusic(
                        item.itemId,
                        connectionId,
                        index
                    )
                })
            }
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveHomeMusic(data: List<HomeMusic>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFavoriteMusic(data: List<FavoriteMusic>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAlbumMusic(data: List<AlbumMusic>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveArtistMusic(data: List<ArtistMusic>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePlayHistoryMusic(data: List<PlayHistoryMusic>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePlayQueueMusic(data: List<PlayQueueMusic>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMaximumPlayMusic(data: List<MaximumPlayMusic>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveNewestMusic(data: List<NewestMusic>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePlaylistMusic(data: List<PlaylistMusic>)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRecommendedMusic(data: List<RecommendedMusic>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun save(data: XyMusic): Long

    @Update
    suspend fun update(data: XyMusic)

    @Query("delete from xy_music")
    suspend fun removeAll()

    @Transaction
    suspend fun removeByType(
        dataType: MusicDataTypeEnum,
        artistId: String? = null,
        playlistId: String? = null,
        albumId: String? = null
    ) {
        when (dataType) {
            MusicDataTypeEnum.HOME -> {
                removeHomeMusic()
            }

            MusicDataTypeEnum.FAVORITE -> {
                removeFavoriteMusic()
            }

            MusicDataTypeEnum.ALBUM -> {
                albumId?.let {
                    removeAlbumMusic(albumId)
                }
            }

            MusicDataTypeEnum.ARTIST -> {
                artistId?.let { artist ->
                    removeArtistMusic(artist)
                }
            }

            MusicDataTypeEnum.PLAY_HISTORY -> {
                removePlayHistoryMusic()
            }

            MusicDataTypeEnum.PLAY_QUEUE -> {
                removePlayQueueMusic()
            }

            MusicDataTypeEnum.MAXIMUM_PLAY -> {
                removeMaximumPlayMusic()
            }

            MusicDataTypeEnum.NEWEST -> {
                removeNewestMusic()
            }

            MusicDataTypeEnum.GENRE -> {

            }

            MusicDataTypeEnum.PLAYLIST -> {
                playlistId?.let {
                    removePlaylistMusic(playlistId)
                }
            }

            MusicDataTypeEnum.RECOMMEND -> {
                removeRecommendedMusic()
            }
        }
        removeByNotQuote()
    }

    @Query(
        """
        delete from homemusic where connectionId = (select connectionId from xy_settings)
    """
    )
    suspend fun removeHomeMusic()

    @Query(
        """
        delete from favoritemusic where connectionId = (select connectionId from xy_settings)
    """
    )
    suspend fun removeFavoriteMusic()

    @Query(
        """
        delete from albumMusic where albumId = :albumId and connectionId = (select connectionId from xy_settings)
    """
    )
    suspend fun removeAlbumMusic(albumId: String)

    @Query(
        """
        delete from artistMusic where artistId = :artistId and connectionId = (select connectionId from xy_settings)
    """
    )
    suspend fun removeArtistMusic(artistId: String)

    @Query(
        """
        delete from playHistoryMusic where connectionId = (select connectionId from xy_settings)
    """
    )
    suspend fun removePlayHistoryMusic()

    @Query(
        """
        delete from playQueueMusic where connectionId = (select connectionId from xy_settings)
    """
    )
    suspend fun removePlayQueueMusic()

    @Query(
        """
        delete from maximumPlayMusic where connectionId = (select connectionId from xy_settings)
    """
    )
    suspend fun removeMaximumPlayMusic()

    @Query(
        """
        delete from newestMusic where connectionId = (select connectionId from xy_settings)
    """
    )
    suspend fun removeNewestMusic()

    @Query(
        """
        delete from playlistMusic where playlistId = :playlistId and connectionId = (select connectionId from xy_settings)
    """
    )
    suspend fun removePlaylistMusic(playlistId: String)

    @Query(
        """
        delete from RecommendedMusic where connectionId = (select connectionId from xy_settings)
    """
    )
    suspend fun removeRecommendedMusic()

    /**
     *  删除没有被引用的数据
     */
    @Query(
        """
        DELETE FROM xy_music 
        WHERE itemId NOT IN (SELECT musicId FROM HomeMusic)
          AND itemId NOT IN (SELECT musicId FROM AlbumMusic)
          AND itemId NOT IN (SELECT musicId FROM ArtistMusic)
          AND itemId NOT IN (SELECT musicId FROM PlaylistMusic)
          AND itemId NOT IN (SELECT musicId FROM favoritemusic)
          AND itemId NOT IN (SELECT musicId FROM maximumplaymusic)
          AND itemId NOT IN (SELECT musicId FROM newestmusic)
          AND itemId NOT IN (SELECT musicId FROM playhistorymusic)
          AND itemId NOT IN (SELECT musicId FROM playqueuemusic)
          AND itemId NOT IN (SELECT musicId FROM recommendedmusic)
    """
    )
    suspend fun removeByNotQuote()


    /**
     * 获得音乐分页信息
     * @return [PagingSource<Int, XyMusic>]
     */
    @Query(
        """
        select mi.* from HomeMusic hm
        inner join xy_music mi on hm.musicId = mi.itemId
        inner join xy_settings xs on mi.connectionId = xs.connectionId and hm.connectionId = xs.connectionId
        order by hm.`index`
    """
    )
    fun selectHomeMusicListPage(): PagingSource<Int, XyMusic>

    /**
     * 获得音乐分页信息
     * @return [PagingSource<Int, XyMusic>]
     */
    @Query(
        """
        select mi.* from HomeMusic hm
        inner join xy_music mi on hm.musicId = mi.itemId
        inner join xy_settings xs on mi.connectionId = xs.connectionId and hm.connectionId = xs.connectionId
        WHERE (:ifFavorite IS NULL OR mi.itemId in(select musicId from favoritemusic))
        AND (:startYear IS NULL OR mi.year between :startYear and :endYear)
        order by hm.`index`
    """
    )
    fun selectHomeMusicListPageByYear(
        ifFavorite: Boolean?,
        startYear: Int?,
        endYear: Int?
    ): PagingSource<Int, XyMusic>


    /**
     * 获得收藏分页信息
     */
    @Transaction
    @Query(
        """
        select mi.* from favoritemusic fm
        inner join xy_music mi on fm.musicId = mi.itemId
        inner join xy_settings xs on mi.connectionId = xs.connectionId and fm.connectionId = xs.connectionId
        order by `index`
    """
    )
    fun selectFavoriteMusicListPage(): PagingSource<Int, XyMusic>

    /**
     * 按艺术家id获得音乐分页列表
     * @param [artistId] 艺术家id
     * @return [PagingSource<Int, MusicArtistExtend>]
     */
    @Transaction
    @Query(
        """
        select mi.* from artistmusic am
        inner join xy_music mi on am.musicId = mi.itemId
        inner join xy_settings xs on mi.connectionId = xs.connectionId and am.connectionId = xs.connectionId
        where  am.artistId = :artistId
        order by `index`
    """
    )
    fun selectArtistMusicListPage(artistId: String): PagingSource<Int, XyMusic>

    /**
     * 按专辑id获得音乐分页列表
     * @param [albumId] 专辑id
     * @return [PagingSource<Int, MusicArtistExtend>]
     */
    @Transaction
    @Query(
        """
        select mi.* from albummusic am
        inner join xy_music mi on am.musicId = mi.itemId
        inner join xy_settings xs on mi.connectionId = xs.connectionId and am.connectionId = xs.connectionId
        where  am.albumId = :albumId
        order by `index`
    """
    )
    fun selectAlbumMusicListPage(albumId: String): PagingSource<Int, XyMusic>

    /**
     * 按歌单id获得音乐分页列表
     * @param [playlistId] 歌单id
     * @return [PagingSource<Int, MusicArtistExtend>]
     */
    @Transaction
    @Query(
        """
        select mi.* from playlistmusic pm
        inner join xy_music mi on pm.musicId = mi.itemId
        inner join xy_settings xs on mi.connectionId = xs.connectionId and pm.connectionId = xs.connectionId
        where  pm.playlistId = :playlistId
        order by `index`
    """
    )
    fun selectPlaylistMusicListPage(playlistId: String): PagingSource<Int, XyMusic>


    /**
     * 获得音乐分页信息
     * @return [List<XyMusic>]
     */
    suspend fun selectLimitMusicListFlow(
        dataType: MusicDataTypeEnum,
        limit: Int
    ): Flow<List<XyMusic>> {
        return when (dataType) {
            MusicDataTypeEnum.HOME -> {
                flow {}
            }

            MusicDataTypeEnum.FAVORITE -> {
                flow {}
            }

            MusicDataTypeEnum.ALBUM -> {
                flow {}
            }

            MusicDataTypeEnum.ARTIST -> {
                flow {}
            }

            MusicDataTypeEnum.PLAY_HISTORY -> {
                selectPlayHistoryMusicListFlow(limit)
            }

            MusicDataTypeEnum.PLAY_QUEUE -> {
                flow {}
            }

            MusicDataTypeEnum.MAXIMUM_PLAY -> {
                selectMaximumPlayMusicListFlow(limit)
            }

            MusicDataTypeEnum.NEWEST -> {
                flow {}
            }

            MusicDataTypeEnum.GENRE -> {
                flow {}
            }

            MusicDataTypeEnum.PLAYLIST -> {
                flow {}
            }

            MusicDataTypeEnum.RECOMMEND -> {
                selectRecommendedMusicListFlow(limit)
            }
        }
    }

    /**
     * 获得首页音乐分页信息
     * @return [List<XyMusic>]
     */
    @Query(
        """
         select mi.* from HomeMusic hm
        inner join xy_music mi on hm.musicId = mi.itemId
        inner join xy_settings xs on mi.connectionId = xs.connectionId and hm.connectionId = xs.connectionId
        order by `index`
        limit :limit offset :startIndex 
    """
    )
    suspend fun selectHomeMusicList(limit: Int, startIndex: Int): List<XyMusic>

    /**
     * 获得播放历史的limit条数据
     */
    @Query(
        """
        select mi.* from PlayHistoryMusic phm
        inner join xy_music mi on phm.musicId = mi.itemId
        inner join xy_settings xs on mi.connectionId = xs.connectionId and phm.connectionId = xs.connectionId
        order by `index`
        limit :limit
    """
    )
    fun selectPlayHistoryMusicListFlow(
        limit: Int
    ): Flow<List<XyMusic>>

    /**
     * 获得播放历史的limit条数据
     */
    @Query(
        """
        select mi.* from PlayHistoryMusic phm
        inner join xy_music mi on phm.musicId = mi.itemId
        inner join xy_settings xs on mi.connectionId = xs.connectionId and phm.connectionId = xs.connectionId
        order by `index`
        limit :limit
    """
    )
    suspend fun selectPlayHistoryMusicList(
        limit: Int
    ): List<XyMusic>

    /**
     * 获得播放列表的数据
     */
    @Query(
        """
        select mi.* from playqueuemusic pqm
        inner join xy_music mi on pqm.musicId = mi.itemId
        inner join xy_settings xs on mi.connectionId = xs.connectionId and pqm.connectionId = xs.connectionId
        order by `index`
    """
    )
    suspend fun selectPlayQueueMusicList(): List<XyMusic>

    /**
     * 获得歌单中音乐的数据
     */
    @Query(
        """
        select mi.* from playlistmusic pqm
        inner join xy_music mi on pqm.musicId = mi.itemId
        inner join xy_settings xs on mi.connectionId = xs.connectionId and pqm.connectionId = xs.connectionId
        order by `index`
    """
    )
    suspend fun selectPlaylistMusicList(): List<XyMusic>

    /**
     * 获得最多播放的limit条数据
     */
    @Query(
        """
        select mi.* from maximumplaymusic mpm
        inner join xy_music mi on mpm.musicId = mi.itemId
        inner join xy_settings xs on mi.connectionId = xs.connectionId and mpm.connectionId = xs.connectionId
        order by `index`
        limit :limit
    """
    )
    fun selectMaximumPlayMusicListFlow(
        limit: Int
    ): Flow<List<XyMusic>>


    @Query(
        """
        select mi.* from RecommendedMusic mpm
        inner join xy_music mi on mpm.musicId = mi.itemId
        inner join xy_settings xs on mi.connectionId = xs.connectionId and mpm.connectionId = xs.connectionId
        order by `index`
        limit :limit
    """
    )
    fun selectRecommendedMusicListFlow(
        limit: Int
    ): Flow<List<XyMusic>>


    /**
     * 根据id查询XyItem详情
     * @param [itemId] 音乐 ID
     * @return [XyItem?]
     */
    @Query("select * from xy_music where itemId = :itemId and connectionId = (select connectionId from xy_settings)limit 1")
    suspend fun selectById(
        itemId: String
    ): XyMusic?

    /**
     * 根据itemId,数据源数据类型删除数据
     * @param [itemId] 项目id
     */
    @Query(
        """
        delete from xy_music where 
        itemId = :itemId and connectionId = (select connectionId from xy_settings) 
    """
    )
    suspend fun removeByItemId(itemId: String)

    /**
     * 根据musicId删除所有数据
     */
    @Query(
        """
        delete from xy_music where itemId in (:itemIds) and connectionId = (select connectionId from xy_settings)
    """
    )
    suspend fun removeByItemIds(itemIds: List<String>)

    /**
     * 获得当前链接里的所有音乐信息
     */
    @Query("select * from xy_music where connectionId = (select connectionId from xy_settings)")
    suspend fun selectAllData(): List<XyMusic>

    /**
     * 获得播放次数最多的歌曲
     */
    @Transaction
    @Query(
        """
       select mi.* from artistmusic am
        inner join xy_music mi on am.musicId = mi.itemId
        inner join xy_settings xs on mi.connectionId = xs.connectionId and am.connectionId = xs.connectionId
        order by `index`
        limit :limit
    """
    )
    suspend fun selectMaximumPlayMusicList(
        limit: Int
    ): List<XyMusic>


    /**
     * 更新收藏状态
     */
    @Transaction
    suspend fun updateFavoriteByItemId(ifFavorite: Boolean, itemId: String, connectionId: Long) {

        val favoriteIndex = selectFavoriteIndex() ?: -1
        saveFavoriteMusic(
            FavoriteMusic(
                musicId = itemId,
                connectionId = connectionId,
                index = favoriteIndex + 1
            )
        )
        updateMusicFavorite(ifFavorite, itemId)
    }

    /**
     * 更新收藏状态
     */
    @Query("update xy_music set ifFavoriteStatus = :ifFavorite where itemId = :itemId and connectionId = (select connectionId from xy_settings) ")
    suspend fun updateMusicFavorite(ifFavorite: Boolean, itemId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFavoriteMusic(data: FavoriteMusic)


    /**
     * 根据创建时间排序,保留最新的20条
     */
    @Transaction
    suspend fun deletePlayHistory() {
        deletePlayHistoryMusic()
        removeByNotQuote()
    }

    /**
     * 根据创建时间排序,保留最新的20条
     */
    @Query(
        """
    DELETE FROM playhistorymusic
    WHERE musicId IN (
        SELECT musicId FROM playhistorymusic
         where connectionId = (select connectionId from xy_settings)
        ORDER BY `index` ASC
        LIMIT (SELECT COUNT(*) FROM playhistorymusic where connectionId = (select connectionId from xy_settings)) - 20
    )
    """
    )
    suspend fun deletePlayHistoryMusic()

    /**
     * 更新播放次数
     */
    @Query("update xy_music set playedCount = :playedCount where itemId = :itemId and connectionId = (select connectionId from xy_settings)")
    suspend fun updateByPlayedCount(playedCount: Int, itemId: String)

    /**
     * 获得播放历史数量
     */
    @Query(
        """
        select count(musicId) from playhistorymusic where connectionId = (select connectionId from xy_settings)
    """
    )
    suspend fun selectPlayHistoryCount(): Long

    @Query("select `index` from homemusic where connectionId = (select connectionId from xy_settings) order by `index` desc limit 1")
    suspend fun selectHomeIndex(): Int?

    @Query("select `index` from favoritemusic where connectionId = (select connectionId from xy_settings) order by `index` desc limit 1")
    suspend fun selectFavoriteIndex(): Int?

    @Query("select `index` from albummusic where connectionId = (select connectionId from xy_settings) order by `index` desc limit 1")
    suspend fun selectAlbumIndex(): Int?

    @Query("select `index` from artistmusic where connectionId = (select connectionId from xy_settings) order by `index` desc limit 1")
    suspend fun selectArtistIndex(): Int?

    @Query("select `index` from playlistmusic where connectionId = (select connectionId from xy_settings) order by `index` desc limit 1")
    suspend fun selectPlaylistIndex(): Int?

    @Query("select `index` from playhistorymusic where connectionId = (select connectionId from xy_settings) order by `index` desc limit 1")
    suspend fun selectPlayHistoryIndex(): Int?

    @Query("select `index` from playqueuemusic where connectionId = (select connectionId from xy_settings) order by `index` desc limit 1")
    suspend fun selectPlayQueueIndex(): Int?

    @Query("select `index` from maximumplaymusic where connectionId = (select connectionId from xy_settings) order by `index` desc limit 1")
    suspend fun selectMaximumPlayIndex(): Int?

    @Query("select `index` from newestmusic where connectionId = (select connectionId from xy_settings) order by `index` desc limit 1")
    suspend fun selectNewestIndex(): Int?

    @Query("select `index` from playlistmusic where connectionId = (select connectionId from xy_settings) and playlistId = :playlistId and musicId in (:musicIds)")
    suspend fun selectMusicIndexByPlaylistId(playlistId: String, musicIds: List<String>): List<Int>


    @Query("select `index` from playhistorymusic where connectionId = (select connectionId from xy_settings) order by `index` limit 1")
    suspend fun selectPlayHistoryIndexAsc(): Int?

    /**
     * 根据数据源获得歌单音乐关联数据
     */
    @Query(
        """
        select pm.*
        from playlistmusic pm 
        inner join xy_settings xs on pm.connectionId = xs.connectionId
        order by `index`
    """
    )
    suspend fun selectPlaylistMusic(): List<PlaylistMusic>

    /**
     * 根据数据源获得歌单音乐关联数据
     */
    @Query(
        """
        select pm.*
        from playlistmusic pm 
        inner join xy_settings xs on pm.connectionId = xs.connectionId
        where pm.playlistId = :playlistId
        order by `index`
    """
    )
    suspend fun selectPlaylistMusicById(playlistId: String): List<PlaylistMusic>

    /**
     * 根据数据源获得歌单音乐关联数据
     */
    @Query(
        """
        select xm.*
        from playlistmusic pm 
        inner join xy_music xm on xm.itemId = pm.musicId
        inner join xy_settings xs on pm.connectionId = xs.connectionId
        where pm.playlistId = :playlistId
        order by `index` limit 1
    """
    )
    suspend fun selectPlaylistMusicOneById(playlistId: String): XyMusic?

    /**
     * 根据itemIds获得音乐列表
     */
    @Query("select * from xy_music where connectionId = (select connectionId from xy_settings) and itemId in (:itemIds)")
    suspend fun selectMusicListByItemIds(itemIds: List<String>): List<XyMusic>

    /**
     * 根据专辑id获得音乐列表
     */
    @Query(
        """
        select mi.* from albummusic am
        inner join xy_music mi on am.musicId = mi.itemId
        inner join xy_settings xs on mi.connectionId = xs.connectionId and am.connectionId = xs.connectionId
        where  am.albumId = :albumId
        order by `index`
        limit :limit offset :startIndex
    """
    )
    suspend fun selectMusicListByAlbumId(
        albumId: String,
        limit: Int,
        startIndex: Int
    ): List<XyMusic>

    /**
     * 根据专辑id获得音乐列表
     */
    @Query(
        """
         select mi.* from artistmusic am
        inner join xy_music mi on am.musicId = mi.itemId
        inner join xy_settings xs on mi.connectionId = xs.connectionId and am.connectionId = xs.connectionId
        where  am.artistId = :artistId
        order by `index`
        limit :limit offset :startIndex
    """
    )
    suspend fun selectMusicListByArtistId(
        artistId: String,
        limit: Int,
        startIndex: Int
    ): List<XyMusic>

    /**
     * 获得收藏分页信息
     */
    @Transaction
    @Query(
        """
        select mi.* from favoritemusic fm
        inner join xy_music mi on fm.musicId = mi.itemId
        inner join xy_settings xs on mi.connectionId = xs.connectionId and fm.connectionId = xs.connectionId
        order by `index`
        limit :limit offset :startIndex
    """
    )
    suspend fun selectMusicListByFavorite(
        limit: Int,
        startIndex: Int
    ): List<XyMusic>

    @Query(
        """
        update xy_music set pic = REPLACE(pic,:oldAddress,:newAddress),
        musicUrl = REPLACE(pic,:oldAddress,:newAddress)
        where pic like :oldAddress || '%' or musicUrl like :oldAddress || '%'
    """
    )
    suspend fun updateUrlByConnectionId(oldAddress: String, newAddress: String)

    /**
     * 根据indexNumber删除数据
     */
    @Query("delete from playlistmusic where playlistId = :playlistId and `index` in (:musicIndex) and connectionId = (select connectionId from xy_settings)")
    suspend fun removeByPlaylistMusicByIndex(playlistId: String, musicIndex: List<String>)

    /**
     * 根据音乐id删除数据
     */
    @Query("delete from playlistmusic where playlistId = :playlistId and musicId in (:musicIds) and connectionId = (select connectionId from xy_settings)")
    suspend fun removeByPlaylistMusicByMusicId(playlistId: String, musicIds: List<String>)

    /**
     * 根据音乐id删除数据
     */
    @Query("delete from playlistmusic where playlistId = :playlistId and musicId = :musicId and connectionId = (select connectionId from xy_settings)")
    suspend fun removeByPlaylistMusicByMusicId(playlistId: String, musicId: String)
}