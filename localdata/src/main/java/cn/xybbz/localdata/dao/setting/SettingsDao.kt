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

package cn.xybbz.localdata.dao.setting

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import cn.xybbz.localdata.data.setting.XySettings
import cn.xybbz.localdata.enums.CacheUpperLimitEnum
import cn.xybbz.localdata.enums.DataSourceType
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

    @Query("update xy_settings set connectionId = :connectionId, dataSourceType = :dataSourceType where id = :id")
    suspend fun updateConnectionId(connectionId: Long?, dataSourceType: DataSourceType?, id: Long)

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

    @Query("update xy_settings set ifEnableSyncPlayProgress = :ifEnableSyncPlayProgress where id = :id")
    suspend fun updateIfEnableSyncPlayProgress(ifEnableSyncPlayProgress: Boolean, id: Long)

    @Query("update xy_settings set fadeDurationMs = :fadeDurationMs where id = :id")
    suspend fun updateFadeDurationMs(fadeDurationMs: Long, id: Long)
}