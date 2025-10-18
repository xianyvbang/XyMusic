package cn.xybbz.localdata.dao.player

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import cn.xybbz.localdata.data.player.XyPlayer
import cn.xybbz.localdata.enums.PlayerTypeEnum

@Dao
interface XyPlayerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(xyPlayer: XyPlayer)

    @Update
    suspend fun updateById(data: XyPlayer)

    @Delete
    suspend fun remove(xyPlayer: XyPlayer)

    @Query("delete from xy_player where connectionId = (select connectionId from xy_settings)")
    suspend fun removeByDatasource()


    @Query("select * from xy_player where connectionId = (select connectionId from xy_settings) LIMIT 1")
    suspend fun selectPlayerByDataSource(): XyPlayer?

    @Query("update xy_player set playerType = :code where connectionId = (select connectionId from xy_settings)")
    suspend fun updatePlayType(code: PlayerTypeEnum)

    @Query("update xy_player set musicId = :musicId where connectionId = (select connectionId from xy_settings) ")
    suspend fun updateIndex(musicId: String)

    @Query("delete from xy_player where connectionId = :connectionId")
    suspend fun removeByConnectionId(connectionId:Long)

    @Query("delete from xy_player")
    suspend fun removeAll()
}