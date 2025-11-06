package cn.xybbz.localdata.dao.recommend

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cn.xybbz.localdata.data.recommend.XyRecentHistory

@Dao
interface XyRecentHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<XyRecentHistory>)

    @Query("SELECT songId FROM xy_recent_recommend_history where connectionId = (select connectionId from xy_settings)")
    suspend fun getAllIds(): List<String>

    @Query("DELETE FROM xy_recent_recommend_history WHERE timestamp < :expireBefore and connectionId = (select connectionId from xy_settings)")
    suspend fun deleteExpired(expireBefore: Long)

    @Query("select songId from xy_recent_recommend_history where songId not in (SELECT songId FROM xy_recent_recommend_history ORDER BY timestamp DESC LIMIT :maxSize) and connectionId = (select connectionId from xy_settings)")
    suspend fun maxSizeSongIds(maxSize: Int): List<String>


    @Query("DELETE FROM xy_recent_recommend_history WHERE songId NOT IN (SELECT songId FROM xy_recent_recommend_history ORDER BY timestamp DESC LIMIT :maxSize) and connectionId = (select connectionId from xy_settings)")
    suspend fun trimToMaxSize(maxSize: Int)
}