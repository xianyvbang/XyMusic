package cn.xybbz.localdata.dao.count

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import cn.xybbz.localdata.data.count.XyDataCount
import kotlinx.coroutines.flow.Flow

@Dao
interface XyDataCountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(data: XyDataCount)

    @Update
    suspend fun update(data: XyDataCount)

    @Query("select * from xy_data_count where connectionId = :connectionId")
    suspend fun selectOne(connectionId: Long): XyDataCount?

    @Query("select * from xy_data_count where connectionId = (select connectionId from xy_settings)")
    fun selectOneFlow(): Flow<XyDataCount?>

    @Query("select * from xy_data_count where connectionId = :connectionId")
    fun selectOneByConnectionId(connectionId: Long): Flow<XyDataCount?>
}