package cn.xybbz.localdata.dao.music

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import cn.xybbz.localdata.data.artist.XyArtist
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtistDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun save(value: XyArtist)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun saveBatch(value: List<XyArtist>)

    @Update
    suspend fun update(data: XyArtist)

    @Query("select * from xy_artist where artistId = :id and connectionId = (select connectionId from xy_settings )")
    suspend fun selectById(id: String): XyArtist?

    @Query("delete from xy_artist where connectionId = (select connectionId from xy_settings )")
    suspend fun removeByDataSource()

    @Query("delete from xy_artist")
    suspend fun removeAll()

    @Query("delete from xy_artist where connectionId = :connectionId")
    suspend fun removeByConnectionId(connectionId: Long)

    @Query(
        """
        select xa.* from xy_artist xa
        where xa.connectionId = (select connectionId from xy_settings)
        and (:selectChat IS NULL OR selectChat = :selectChat)
    """
    )
    fun selectListPagingSource(selectChat: String? = null): PagingSource<Int, XyArtist>

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

    /**
     * 查询分页数据
     */
    fun selectListPage(
        ifFavorite: Boolean?,
        selectLetter: String?
    ): PagingSource<Int, XyArtist> {
        val stringBuilder = StringBuilder(
            """
            select xa.* from xy_artist xa
             inner join xy_favorite xf on xf.itemId = xa.artistId
        where xa.dataSource = (select dataSourceType from xy_settings)
        """.trimIndent()
        )

        if (ifFavorite == true) {
            stringBuilder.append(" and xf.ifFavorite = 1")
        }
        if (!selectLetter.isNullOrBlank()) {
            stringBuilder.append(" and xf.selectChat = '$selectLetter'")
        }

        return selectListPage(SimpleSQLiteQuery(stringBuilder.toString()))
    }

    @RawQuery(observedEntities = [XyArtist::class])
    fun selectListPage(sql: SupportSQLiteQuery): PagingSource<Int, XyArtist>

    @Query("select * from xy_artist where artistId in (:artistIds) and connectionId = (select connectionId from xy_settings) ")
    suspend fun selectByIds(artistIds: List<String>): List<XyArtist>


    @Query(
        """
        update xy_artist set pic = REPLACE(pic,:oldAddress,:newAddress)
        where pic like :oldAddress || '%'
    """
    )
    suspend fun updateUrlByConnectionId(oldAddress: String, newAddress: String)

    /**
     * 根据selectChat获得所属位置索引
     */
    @Query("select indexNumber from xy_artist where selectChat = :selectChat and connectionId = (select connectionId from xy_settings) order by indexNumber limit 1")
    suspend fun selectIndexBySelectChat(selectChat: String): Int

    /**
     * 获得艺术家表中selectChar字段
     */
    @Query("select selectChat from xy_artist where connectionId = (select connectionId from xy_settings) group by selectChat order by selectChat")
    fun getSelectCharList(): Flow<List<String>>


}