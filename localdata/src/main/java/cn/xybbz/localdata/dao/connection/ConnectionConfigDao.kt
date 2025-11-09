package cn.xybbz.localdata.dao.connection

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import cn.xybbz.localdata.data.connection.ConnectionConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface ConnectionConfigDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(data: ConnectionConfig): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveBatch(data: List<ConnectionConfig>)

    @Update
    suspend fun update(data: ConnectionConfig)
    @Update
    suspend fun updateBatch(datas : List<ConnectionConfig>)

    @Query("select * from xy_connection_config where id = (select connectionId from xy_settings) ")
    suspend fun selectConnectionConfig(): ConnectionConfig?

    @Query("select * from xy_connection_config")
    suspend fun selectAllData(): List<ConnectionConfig>

    @Query("delete from xy_connection_config")
    suspend fun removeAll()

    @Query("select * from xy_connection_config")
    fun selectAllDataFlow(): Flow<List<ConnectionConfig>>

    @Query("select * from xy_connection_config where id = :id")
    suspend fun selectById(id:Long): ConnectionConfig?

    @Query("select * from xy_connection_config where id = :id")
    fun selectByIdFlow(id:Long): Flow<ConnectionConfig?>


    @Query("select count(id) from xy_connection_config")
    suspend fun selectCount():Int

    @Query("update xy_connection_config set currentPassword = :currentPassword, iv = :iv, `key` = :key where id = :connectionId")
    suspend fun updatePassword(currentPassword: String, iv: String, key: String,connectionId: Long)

    @Query("update xy_connection_config set libraryId = :libraryId where id = :connectionId")
    suspend fun updateLibraryId(libraryId: String?, connectionId: Long)

    @Query("update xy_connection_config set address = :address where id = :connectionId")
    suspend fun updateAddress(address: String, connectionId: Long)

    @Query("delete from xy_connection_config where id = :connectionId")
    suspend fun removeById(connectionId: Long)

    @Query("update xy_connection_config set name = :name where id = :connectionId")
    suspend fun updateName(name: String, connectionId: Long)
}