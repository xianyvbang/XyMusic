package cn.xybbz.localdata.dao.setting

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import cn.xybbz.localdata.data.setting.XySettings
import cn.xybbz.localdata.enums.CacheUpperLimitEnum
import cn.xybbz.localdata.enums.LanguageType
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(data: XySettings): Long

    @Query("delete from xy_settings")
    suspend fun remove()

    /**
     * 更新
     */
    @Update
    suspend fun update(vararg data: XySettings): Int

    @Query("select * from xy_settings limit 1")
    fun selectOne(): Flow<XySettings?>

    @Query("select * from xy_settings limit 1")
    suspend fun selectOneData(): XySettings?

    @Query("select doubleSpeed from xy_settings limit 1")
    fun selectDoubleSpeed(): Flow<Float?>

    @Query("update xy_settings set doubleSpeed = :doubleSpeed where id = :id")
    suspend fun updateDoubleSpeed(doubleSpeed: Float, id: Long)

    @Query("update xy_settings set cacheUpperLimit = :cacheUpperLimit where id = :id")
    suspend fun updateCacheUpperLimit(cacheUpperLimit: CacheUpperLimitEnum, id: Long)

    @Query("update xy_settings set ifDesktopLyrics = :ifDesktopLyrics where id = :id")
    suspend fun updateIfDesktopLyrics(ifDesktopLyrics: Int, id: Long)

    @Query("update xy_settings set ifEnableAlbumHistory = :ifEnableAlbumHistory where id = :id")
    suspend fun updateIfEnableAlbumHistory(ifEnableAlbumHistory: Boolean, id: Long)

    @Query("update xy_settings set connectionId = :connectionId where id = :id")
    suspend fun updateConnectionId(connectionId: Long?, id: Long)

    @Query("update xy_settings set ifHandleAudioFocus = :ifHandleAudioFocus where id = :id")
    suspend fun updateIfHandleAudioFocus(ifHandleAudioFocus: Boolean, id: Long)

    @Query("update xy_settings set languageType = :languageType where id = :id")
    suspend fun updateLanguageType(languageType: LanguageType, id: Long)

    @Query("update xy_settings set latestVersion = :latestVersion where id = :id")
    suspend fun updateLatestVersion(latestVersion: String, id: Long)

    @Query("update xy_settings set lasestApkUrl = :lasestApkUrl where id = :id")
    suspend fun updateLatestApkUrl(lasestApkUrl: String, id: Long)

    @Query("update xy_settings set latestVersionTime = :latestVersionTime where id = :id")
    suspend fun updateLatestVersionTime(latestVersionTime: Long, id: Long)

    @Query("update xy_settings set maxConcurrentDownloads = :maxConcurrentDownloads where id = :id")
    suspend fun updateMaxConcurrentDownloads(maxConcurrentDownloads: Int, id: Long)
}