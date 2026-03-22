package cn.xybbz.localdata.dao.setting

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import cn.xybbz.localdata.data.setting.SkipTime

@Dao
interface SkipTimeDao {

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun save(data: SkipTime)

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun saveBatch(data: List<SkipTime>)

    @Query("select * from skip_time where albumId = :albumId limit 1")
    suspend fun selectByAlbumId(albumId: String): SkipTime?

    @Query("select * from skip_time where id = :id")
    suspend fun selectById(id: Long): SkipTime

    @Update
    suspend fun updateByID(skipTime: SkipTime)

    @Query("select * from skip_time")
    suspend fun selectAllData(): List<SkipTime>

    @Query("delete from skip_time")
    suspend fun removeAll()

    @Query("delete from skip_time where connectionId = :connectionId")
    suspend fun removeByConnectionId(connectionId: Long)


}