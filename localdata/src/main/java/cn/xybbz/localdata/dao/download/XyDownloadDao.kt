package cn.xybbz.localdata.dao.download

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cn.xybbz.localdata.data.download.XyDownload
import cn.xybbz.localdata.enums.DownloadStatus

@Dao
interface XyDownloadDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg xyDownload: XyDownload): List<Long>

    @Query("select * from xy_download where id = :id")
    suspend fun selectById(id: Long): XyDownload?

    @Query("update xy_download set status = :status where id = :id")
    suspend fun updateStatus(id: Long, status: DownloadStatus)

    @Query("update xy_download set progress = :progress, downloadedBytes = :downloadedBytes, totalBytes = :totalBytes, updateTime = :updateTime where id = :id")
    suspend fun updateProgress(
        id: Long,
        progress: Int,
        downloadedBytes: Long,
        totalBytes: Long,
        updateTime: Long
    )

    @Query("SELECT status FROM xy_download WHERE id = :id")
    suspend fun getStatusById(id: Long): DownloadStatus?

    @Query("UPDATE xy_download SET status = :status, error = :error, updateTime = :updateTime WHERE id = :id")
    suspend fun updateOnError(
        id: Long,
        status: DownloadStatus,
        error: String?,
        updateTime: Long
    )

    @Query("update xy_download set status = :status, progress = 100, downloadedBytes = totalBytes, updateTime = :updateTime, filePath = :finalPath WHERE id = :id")
    suspend fun updateOnSuccess(
        id: Long,
        status: DownloadStatus = DownloadStatus.COMPLETED,
        finalPath: String,
        updateTime: Long
    )

    @Query("UPDATE xy_download SET status = :status, updateTime = :updateTime WHERE id IN (:ids)")
    suspend fun updateStatuses(ids: List<Long>, status: DownloadStatus, updateTime: Long)


    @Query("DELETE FROM xy_download WHERE id IN (:id)")
    suspend fun deleteById(vararg id: Long)


    @Query("SELECT * FROM xy_download WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Long>): List<XyDownload>

    @Query("SELECT * FROM xy_download where connectionId = :connectionId")
    suspend fun getAllTasksSuspend(connectionId: Long): List<XyDownload>


}