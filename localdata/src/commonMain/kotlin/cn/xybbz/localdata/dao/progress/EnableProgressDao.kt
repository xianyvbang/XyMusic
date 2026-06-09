package cn.xybbz.localdata.dao.progress

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import cn.xybbz.localdata.data.progress.EnableProgress
import kotlinx.coroutines.flow.Flow

@Dao
interface EnableProgressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(data: EnableProgress)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveBatch(data: List<EnableProgress>)

    @Update
    suspend fun updateById(data: EnableProgress)

    @Query("select albumId,ifEnableAlbumHistory from xy_enable_progress")
    fun getAlbumEnableProgressMap(): Flow<Map<@MapColumn(columnName = "albumId") String, @MapColumn(
        columnName = "ifEnableAlbumHistory"
    ) Boolean>>

    /**
     * 查询该专辑是否开启播放历史记录
     */
    @Query("select ifEnableAlbumHistory from xy_enable_progress where albumId = :albumId")
    fun getAlbumEnableProgressByAlbumId(albumId: String): Flow<Boolean?>

    @Query("select * from xy_enable_progress")
    suspend fun selectAllData(): List<EnableProgress>

    @Query("delete from xy_enable_progress")
    suspend fun removeAll();

    @Query("delete from xy_enable_progress where connectionId = :connectionId")
    suspend fun removeByConnectionId(connectionId:Long)

}