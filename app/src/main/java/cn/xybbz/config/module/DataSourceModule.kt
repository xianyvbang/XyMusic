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
import cn.xybbz.api.client.ImageApiClient
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

    @Provides
    fun jellyfinApiServer(
        db: DatabaseClient,
        settingsManager: SettingsManager,
        jellyfinApiClient: JellyfinApiClient,
        @ApplicationContext application: Context,
        mediaLibraryAndFavoriteSyncScheduler: MediaLibraryAndFavoriteSyncScheduler,
        downloadManager: DownLoadManager
    ): JellyfinDatasourceServer {
        return JellyfinDatasourceServer(
            db,
            application,
            settingsManager,
            jellyfinApiClient,
            mediaLibraryAndFavoriteSyncScheduler,
            downloadManager
        )
    }

    @Provides
    fun subsonicDataSourceServer(
        db: DatabaseClient,
        settingsManager: SettingsManager,
        subsonicApiClient: SubsonicApiClient,
        @ApplicationContext application: Context,
        mediaLibraryAndFavoriteSyncScheduler: MediaLibraryAndFavoriteSyncScheduler,
        downloadManager: DownLoadManager
    ): SubsonicDatasourceServer {
        return SubsonicDatasourceServer(
            db,
            application,
            settingsManager,
            subsonicApiClient,
            mediaLibraryAndFavoriteSyncScheduler,
            downloadManager
        )
    }

    @Provides
    fun navidromeDatasourceServer(
        db: DatabaseClient,
        settingsManager: SettingsManager,
        navidromeApiClient: NavidromeApiClient,
        @ApplicationContext application: Context,
        mediaLibraryAndFavoriteSyncScheduler: MediaLibraryAndFavoriteSyncScheduler,
        downloadManager: DownLoadManager
    ): NavidromeDatasourceServer {
        return NavidromeDatasourceServer(
            db,
            application,
            settingsManager,
            navidromeApiClient,
            mediaLibraryAndFavoriteSyncScheduler,
            downloadManager
        )
    }

    @Provides
    fun embyDatasourceServer(
        db: DatabaseClient,
        settingsManager: SettingsManager,
        embyApiClient: EmbyApiClient,
        @ApplicationContext application: Context,
        mediaLibraryAndFavoriteSyncScheduler: MediaLibraryAndFavoriteSyncScheduler,
        downloadManager: DownLoadManager
    ): EmbyDatasourceServer {
        return EmbyDatasourceServer(
            db,
            application,
            settingsManager,
            embyApiClient,
            mediaLibraryAndFavoriteSyncScheduler,
            downloadManager
        )
    }


    @Provides
    fun plexDatasourceServer(
        db: DatabaseClient,
        settingsManager: SettingsManager,
        plexApiClient: PlexApiClient,
        @ApplicationContext application: Context,
        mediaLibraryAndFavoriteSyncScheduler: MediaLibraryAndFavoriteSyncScheduler,
        downloadManager: DownLoadManager,
    ): PlexDatasourceServer {
        return PlexDatasourceServer(
            db,
            application,
            settingsManager,
            plexApiClient,
            mediaLibraryAndFavoriteSyncScheduler,
            downloadManager
        )
    }


    @Singleton
    @Provides
    fun dataSourceManager(
        @ApplicationContext application: Context,
        db: DatabaseClient,
        dataSources: Map<DataSourceType, @JvmSuppressWildcards Provider<IDataSourceParentServer>>,
        alarmConfig: AlarmConfig,
        versionApiClient: VersionApiClient,
        imageApiClient: ImageApiClient
    ): DataSourceManager {
        val dataSourceManager = DataSourceManager(
            application = application,
            db,
            dataSources,
            alarmConfig,
            versionApiClient,
            imageApiClient
        )
        return dataSourceManager
    }

}