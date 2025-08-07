package cn.xybbz.localdata.dao.genre

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cn.xybbz.localdata.data.genre.XyGenre

@Dao
interface XyGenreDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveBatch(data: List<XyGenre>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(data: XyGenre)

    @Query("delete from xy_genre where connectionId = (select xs.connectionId from xy_settings xs)")
    suspend fun remove()

    @Query("select * from xy_genre where connectionId = (select xs.connectionId from xy_settings xs)")
    fun selectByDataSourceType(): PagingSource<Int, XyGenre>

    @Query("delete from xy_genre where connectionId = :connectionId")
    suspend fun removeByConnectionId(connectionId: Long)

    @Query("delete from xy_genre")
    suspend fun removeAll()

    @Query("select * from xy_genre")
    suspend fun selectAll():List<XyGenre>

    @Query("select * from xy_genre where itemId = :genreId")
    suspend fun selectById(genreId: String): XyGenre?

    @Query("select count(itemId) from xy_genre where connectionId = (select xs.connectionId from xy_settings xs)")
    suspend fun selectCount():Int


    @Query("""
        update xy_genre set pic = REPLACE(pic,:oldAddress,:newAddress)
        where pic like :oldAddress || '%'
    """)
    suspend fun updateUrlByConnectionId(oldAddress: String, newAddress: String)
}