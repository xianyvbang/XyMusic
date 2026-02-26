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

package cn.xybbz.localdata.dao.music

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import cn.xybbz.localdata.data.artist.FavoriteArtist
import cn.xybbz.localdata.data.artist.XyArtist
import cn.xybbz.localdata.data.artist.XyArtistExt
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(value: XyArtist)

    /**
     * 批量写入数据并且写入收藏信息
     */
    suspend fun saveArtistBatch(items: List<XyArtist>,connectionId: Long){
        saveBatch(items)
        //写入FavoriteArtist
        val favoriteArtists = items.filter { it.ifFavorite }.map {
            FavoriteArtist(
                it.artistId,
                connectionId
            )
        }
        if (favoriteArtists.isNotEmpty())
            saveBatchFavoriteArtist(favoriteArtists)
    }


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun saveBatch(value: List<XyArtist>)

    @Update
    suspend fun update(data: XyArtist)

    @Query("select * from xy_artist where artistId = :id and connectionId = (select connectionId from xy_settings )")
    suspend fun selectById(id: String): XyArtist?


    /**
     * 根据id获得艺术家收藏信息
     */
    @Query(
        """
        select ifFavorite from favoriteartist
        where artistId = :itemId and connectionId = (select connectionId from xy_settings) 
    """
    )
    suspend fun selectFavoriteById(itemId: String): Boolean?

    @Query("delete from xy_artist where connectionId = (select connectionId from xy_settings )")
    suspend fun removeByDataSource()

    @Query("delete from xy_artist")
    suspend fun removeAll()

    @Query("delete from xy_artist where connectionId = :connectionId")
    suspend fun removeByConnectionId(connectionId: Long)

    @Query(
        """
        select xa.*,row_number() over (order by xa.selectChat) as indexNumber,fa.ifFavorite as favorite from xy_artist xa
        left join favoriteartist fa on fa.artistId = xa.artistId
        where xa.connectionId = (select connectionId from xy_settings)
        order by xa.selectChat
    """
    )
    fun selectListPagingSource(): PagingSource<Int, XyArtistExt>

    @Query("select * from xy_artist where artistId = :id")
    suspend fun selectExtendById(id: String): XyArtist?

    @Query("select name from xy_artist where name like '%' || :search || '%' limit 3")
    suspend fun searchHitList(search: String): List<String>

    /**
     * 搜索
     * @param [searchQuery] 搜索查询
     * @return [PagingSource<Int, ArtistItem>]
     */
    @Query(
        """
        select xa.* from xy_artist xa
        inner join xy_settings xs on xs.connectionId = xa.connectionId
        where  name like '%' || :searchQuery || '%'
        limit 50
    """
    )
    fun searchPagingSource(
        searchQuery: String,
    ): List<XyArtist>


    @Query("select * from xy_artist where connectionId = (select connectionId from xy_settings )")
    suspend fun selectAllData(): List<XyArtist>

    @Query(
        """
        select count(xa.artistId) from xy_artist xa
            where xa.connectionId = (select connectionId from xy_settings)
    """
    )
    suspend fun selectCount(): Int

    @Query("select * from xy_artist where artistId in (:artistIds) and connectionId = (select connectionId from xy_settings) ")
    suspend fun selectByIds(artistIds: List<String>): List<XyArtist>

    /**
     * 根据selectChat获得所属位置索引
     */
    @Query("""
        select t.indexNumber from (
        select xa.selectChat,row_number() over (order by xa.selectChat) as indexNumber,fa.ifFavorite as favorite from xy_artist xa
        left join favoriteartist fa on fa.artistId = xa.artistId
        where xa.connectionId = (select connectionId from xy_settings)
        order by xa.selectChat
        )t where t.selectChat = :selectChat
        
    """)
    suspend fun selectIndexBySelectChat(selectChat: String): Int

    /**
     * 获得艺术家表中selectChar字段
     */
    @Query("select selectChat from xy_artist where connectionId = (select connectionId from xy_settings) group by selectChat order by selectChat")
    fun getSelectCharList(): Flow<List<String>>


    @Query(
        """
        update favoriteartist set ifFavorite = :ifFavorite where artistId = :itemId and connectionId = (select connectionId from xy_settings)
    """
    )
    suspend fun updateFavoriteByItemId(ifFavorite: Boolean, itemId: String)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveBatchFavoriteArtist(data: List<FavoriteArtist>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFavoriteArtist(data: FavoriteArtist)


    @Query("""
        select count(artistId) from favoriteartist where artistId = :itemId and connectionId = (select connectionId from xy_settings) 
    """)
    suspend fun selectFavoriteCount(itemId: String): Int
}