package cn.xybbz.localdata.dao.remote

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cn.xybbz.localdata.data.remote.RemoteCurrent

@Dao
interface RemoteCurrentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(remoteKey: RemoteCurrent)

    @Query("SELECT * FROM remote_current WHERE id = :query and connectionId = (select connectionId from xy_settings)")
    suspend fun remoteKeyById(query: String): RemoteCurrent?

    @Query("update remote_current set nextKey =:nextKey ,total = :total where id = :id and connectionId = (select connectionId from xy_settings)")
    suspend fun updateNextKey(id:String,nextKey:Int,total:Int)

    @Query("update remote_current set prevKey =:prevKey,total = :total where id = :id and connectionId = (select connectionId from xy_settings)")
    suspend fun updatePrevKey(id:String,prevKey:Int,total:Int)

    @Query("DELETE FROM remote_current WHERE id = :query and connectionId = (select connectionId from xy_settings)")
    suspend fun deleteById(query: String)

    @Query("delete from remote_current where connectionId = :connectionId")
    suspend fun removeByConnectionId(connectionId:Long)

    @Query("delete from remote_current")
    suspend fun removeAll()
}