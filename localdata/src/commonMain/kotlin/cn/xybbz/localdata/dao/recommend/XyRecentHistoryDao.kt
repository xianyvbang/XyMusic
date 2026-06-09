package cn.xybbz.localdata.dao.recommend

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cn.xybbz.localdata.data.recommend.XyDailyRecommendHistory

@Dao
interface XyRecentHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<XyDailyRecommendHistory>)

    @Query(
        """
        SELECT songId FROM xy_daily_recommend_history
        where connectionId = (select connectionId from xy_settings)
        and (
            (mediaLibraryId is null and (select libraryIds from xy_connection_config where id = (select connectionId from xy_settings)) is null)
            or mediaLibraryId = (select libraryIds from xy_connection_config where id = (select connectionId from xy_settings))
        )
    """
    )
    suspend fun getAllIds(): List<String>

    @Query(
        """
        DELETE FROM xy_daily_recommend_history
        WHERE timestamp < :expireBefore
          and connectionId = (select connectionId from xy_settings)
          and (
              (mediaLibraryId is null and (select libraryIds from xy_connection_config where id = (select connectionId from xy_settings)) is null)
              or mediaLibraryId = (select libraryIds from xy_connection_config where id = (select connectionId from xy_settings))
          )
    """
    )
    suspend fun deleteExpired(expireBefore: Long)

    @Query(
        """
        SELECT songId
        FROM xy_daily_recommend_history
        WHERE connectionId = (SELECT connectionId FROM xy_settings)
          and (
              (mediaLibraryId is null and (select libraryIds from xy_connection_config where id = (select connectionId from xy_settings)) is null)
              or mediaLibraryId = (select libraryIds from xy_connection_config where id = (select connectionId from xy_settings))
          )
          AND songId NOT IN (
              SELECT songId
              FROM xy_daily_recommend_history
              WHERE connectionId = (SELECT connectionId FROM xy_settings)
                and (
                    (mediaLibraryId is null and (select libraryIds from xy_connection_config where id = (select connectionId from xy_settings)) is null)
                    or mediaLibraryId = (select libraryIds from xy_connection_config where id = (select connectionId from xy_settings))
                )
              ORDER BY timestamp DESC, recommendIndex DESC
              LIMIT :maxSize
          )
    """
    )
    suspend fun maxSizeSongIds(maxSize: Int): List<String>


    @Query(
        """
        DELETE FROM xy_daily_recommend_history
        WHERE connectionId = (SELECT connectionId FROM xy_settings)
          and (
              (mediaLibraryId is null and (select libraryIds from xy_connection_config where id = (select connectionId from xy_settings)) is null)
              or mediaLibraryId = (select libraryIds from xy_connection_config where id = (select connectionId from xy_settings))
          )
          AND songId NOT IN (
              SELECT songId
              FROM xy_daily_recommend_history
              WHERE connectionId = (SELECT connectionId FROM xy_settings)
                and (
                    (mediaLibraryId is null and (select libraryIds from xy_connection_config where id = (select connectionId from xy_settings)) is null)
                    or mediaLibraryId = (select libraryIds from xy_connection_config where id = (select connectionId from xy_settings))
                )
              ORDER BY timestamp DESC, recommendIndex DESC
              LIMIT :maxSize
          )
    """
    )
    suspend fun trimToMaxSize(maxSize: Int)

    @Query("delete from xy_daily_recommend_history where connectionId = :connectionId")
    suspend fun removeByConnectionId(connectionId: Long)

    @Query("delete from xy_daily_recommend_history")
    suspend fun removeAll()
}
