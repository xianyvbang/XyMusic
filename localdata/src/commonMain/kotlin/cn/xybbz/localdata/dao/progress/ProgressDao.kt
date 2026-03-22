package cn.xybbz.localdata.dao.progress

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import cn.xybbz.localdata.data.progress.Progress
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(data: Progress)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveBatch(data: List<Progress>)

    @Delete
    suspend fun remove(data: Progress)

    @Update
    suspend fun update(data: Progress)

    @Query("delete from progress where musicId = :musicId")
    suspend fun removeByMusicId(musicId: String)

    @Query("select * from progress where musicId =:musicId")
    suspend fun selectByMusicId(musicId: String): Progress?

    @Query("select * from progress where albumId = :albumId")
    suspend fun selectByAlbumId(albumId: String): List<Progress>

    @Query(
        """
        select pr.* from progress pr
        where pr.albumId = :albumId order by pr.createTime desc limit 1
    """
    )
    @Transaction
    fun selectByAlbumIdFlowOne(albumId: String): Flow<Progress?>

    @Query("delete from progress where albumId = :albumId")
    suspend fun removeByAlbumId(albumId: String)

    @Query("select musicId,progressPercentage from progress where albumId = :albumId order by createTime desc")
    fun selectByAlbumIdFlowMap(albumId: String): Flow<Map<@MapColumn(columnName = "musicId") String, @MapColumn(
        columnName = "progressPercentage"
    ) Int>>

    @Query("select * from progress where albumId = :albumId order by `index` desc limit 1")
    suspend fun selectByAlbumIdOne(albumId: String): Progress?

    @Query("select * from progress where albumId = :albumId order by createTime desc limit 1")
    suspend fun selectByAlbumIdOneOrderCreate(albumId: String): Progress?

    @Query("select * from progress where musicId = :musicId order by createTime desc limit 1")
    suspend fun selectByMusicIdOne(musicId: String): Progress?

    @Query("select * from progress where musicId in (:musicIds)")
    suspend fun selectByMusicIds(musicIds: List<String>): List<Progress>

    @Query("select * from progress")
    suspend fun selectAllData(): List<Progress>

    @Query("delete from progress")
    suspend fun removeAll()

    @Query("delete from progress where connectionId = :connectionId")
    suspend fun removeByConnectionId(connectionId: Long)
}