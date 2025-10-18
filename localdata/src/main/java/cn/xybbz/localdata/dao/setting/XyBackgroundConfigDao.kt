package cn.xybbz.localdata.dao.setting

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import cn.xybbz.localdata.data.setting.XyBackgroundConfig

@Dao
interface XyBackgroundConfigDao {

    @Query("select * from xy_background_config limit 1")
    suspend fun selectOne(): XyBackgroundConfig?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(data: XyBackgroundConfig): Long

    @Update
    suspend fun updateById(data: XyBackgroundConfig)

    @Delete
    suspend fun deleteById(data: XyBackgroundConfig)


    @Query("update xy_background_config set homeBrash = :homeBrash where id = :id")
    suspend fun updateHomeBrash(homeBrash: String, id: Long)

    @Query("delete from xy_connection_config")
    suspend fun remove()
}