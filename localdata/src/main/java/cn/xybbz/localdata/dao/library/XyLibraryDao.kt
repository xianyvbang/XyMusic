package cn.xybbz.localdata.dao.library

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cn.xybbz.localdata.data.library.XyLibrary

@Dao
interface XyLibraryDao {

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun saveBatch(data: List<XyLibrary>)

    @Query("delete from xy_library where connectionId = (select connectionId from xy_settings)")
    suspend fun remove()

    @Query("select * from xy_library where connectionId = (select connectionId from xy_settings)")
    suspend fun selectListByDataSourceType(): List<XyLibrary>

    @Query("delete from xy_library where connectionId = :connectionId")
    suspend fun removeByConnectionId(connectionId: Long)

    @Query("delete from xy_library")
    suspend fun removeAll()

}