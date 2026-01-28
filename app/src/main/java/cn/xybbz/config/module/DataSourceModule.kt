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

package cn.xybbz.config.module

import android.content.Context
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.api.client.IDataSourceParentServer
import cn.xybbz.api.client.emby.EmbyApiClient
import cn.xybbz.api.client.emby.EmbyDatasourceServer
import cn.xybbz.api.client.jellyfin.JellyfinApiClient
import cn.xybbz.api.client.jellyfin.JellyfinDatasourceServer
import cn.xybbz.api.client.navidrome.NavidromeApiClient
import cn.xybbz.api.client.navidrome.NavidromeDatasourceServer
import cn.xybbz.api.client.plex.PlexApiClient
import cn.xybbz.api.client.plex.PlexDatasourceServer
import cn.xybbz.api.client.subsonic.SubsonicApiClient
import cn.xybbz.api.client.subsonic.SubsonicDatasourceServer
import cn.xybbz.api.client.version.VersionApiClient
import cn.xybbz.api.dispatchs.MediaLibraryAndFavoriteSyncScheduler
import cn.xybbz.config.alarm.AlarmConfig
import cn.xybbz.config.download.DownLoadManager
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.enums.DataSourceType
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataSourceModule {

    @Singleton
    @Provides
    fun dataSourceManager(
        @ApplicationContext application: Context,
        db: DatabaseClient,
        alarmConfig: AlarmConfig,
        versionApiClient: VersionApiClient,
        mediaLibraryAndFavoriteSyncScheduler: MediaLibraryAndFavoriteSyncScheduler,
        downloadManager: DownLoadManager,
        settingsManager: SettingsManager
    ): DataSourceManager {
        return DataSourceManager(
            application = application,
            db,
            alarmConfig,
            versionApiClient,
            mediaLibraryAndFavoriteSyncScheduler,
            downloadManager,
            settingsManager
        )
    }

}