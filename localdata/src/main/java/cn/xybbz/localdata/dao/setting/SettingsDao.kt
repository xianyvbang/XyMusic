package cn.xybbz.localdata.dao.setting

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import cn.xybbz.localdata.data.setting.Settings
import cn.xybbz.localdata.enums.CacheUpperLimitEnum
import cn.xybbz.localdata.enums.LanguageType
import cn.xybbz.localdata.enums.ThemeTypeEnum
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(data: Settings): Long

    @Query("delete from xy_settings")
    suspend fun remove()

    /**
     * 更新
     */
    @Update
    suspend fun update(vararg data: Settings): Int

    @Query("select * from xy_settings limit 1")
    fun selectOne(): Flow<Settings?>

    @Query("select * from xy_settings limit 1")
    suspend fun selectOneData(): Settings?

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

    @Query("update xy_settings set autoBackups = :autoBackups where id = :id")
    suspend fun updateAutoBackups(autoBackups: Boolean, id: Long)

    @Query("update xy_settings set ifHandleAudioFocus = :ifHandleAudioFocus where id = :id")
    suspend fun updateIfHandleAudioFocus(ifHandleAudioFocus: Boolean, id: Long)

    @Query("update xy_settings set themeType = :themeType where id = :id")
    suspend fun updateThemeType(themeType: ThemeTypeEnum, id: Long)

    @Query("update xy_settings set isDynamic = :isDynamic where id = :id")
    suspend fun updateIsDynamic(isDynamic: Boolean, id: Long)

    @Query("update xy_settings set languageType = :languageType where id = :id")
    suspend fun updateLanguageType(languageType: LanguageType, id: Long)
}