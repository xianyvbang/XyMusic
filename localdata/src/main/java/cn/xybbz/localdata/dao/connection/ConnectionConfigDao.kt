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

package cn.xybbz.localdata.dao.connection

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import cn.xybbz.localdata.data.connection.ConnectionConfig
import cn.xybbz.localdata.data.connection.ConnectionConfigExt
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
    suspend fun updateBatch(datas: List<ConnectionConfig>)

    @Query("select * from xy_connection_config where id = (select connectionId from xy_settings) ")
    suspend fun selectConnectionConfig(): ConnectionConfig?

    @Query("select * from xy_connection_config order by createTime desc limit 1 ")
    suspend fun selectConnectionConfigOrderByCreateTime(): ConnectionConfig?

    @Query("select * from xy_connection_config where id = (select connectionId from xy_settings) ")
    fun selectConnectionConfigFlow(): Flow<ConnectionConfig?>

    @Query("select * from xy_connection_config")
    suspend fun selectAllData(): List<ConnectionConfig>

    @Query("delete from xy_connection_config")
    suspend fun removeAll()

    @Query(
        """
        select xcc.*,xl.name as libraryName from xy_connection_config xcc
        left join xy_library xl on xl.connectionId = xcc.id and xcc.libraryId = xl.id
    """
    )
    fun selectAllDataExtFlow(): Flow<List<ConnectionConfigExt>>

    @Query(
        """
        select xcc.* from xy_connection_config xcc
    """
    )
    fun selectAllDataFlow(): Flow<List<ConnectionConfig>>

    @Query("select * from xy_connection_config where id = :id")
    suspend fun selectById(id: Long): ConnectionConfig?

    @Query("select * from xy_connection_config where id = :id")
    fun selectByIdFlow(id: Long): Flow<ConnectionConfig?>

    @Query("select count(id) from xy_connection_config")
    suspend fun selectCount(): Int

    @Query("update xy_connection_config set libraryId = :libraryId where id = :connectionId")
    suspend fun updateLibraryId(libraryId: String?, connectionId: Long)

    @Query("update xy_connection_config set address = :address where id = :connectionId")
    suspend fun updateAddress(address: String, connectionId: Long)

    @Query("delete from xy_connection_config where id = :connectionId")
    suspend fun removeById(connectionId: Long)

    @Query("update xy_connection_config set name = :name where id = :connectionId")
    suspend fun updateName(name: String, connectionId: Long)
}