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

package cn.xybbz.di

import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.api.client.IDataSourceParentServer
import cn.xybbz.api.client.custom.CustomMediaApiClient
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
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.enums.DataSourceType
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
@Configuration
class DataSourceModule {

    @Factory
    @DataSourceKey(DataSourceType.JELLYFIN)
    fun jellyfinApiServer(
        db: DatabaseClient,
        settingsManager: SettingsManager,
        jellyfinApiClient: JellyfinApiClient,
        customMediaApiClient: CustomMediaApiClient,
        contextWrapper: ContextWrapper
    ): IDataSourceParentServer {
        return JellyfinDatasourceServer(
            db,
            settingsManager,
            jellyfinApiClient,
            customMediaApiClient,
            contextWrapper
        )
    }

    @Factory
    @DataSourceKey(DataSourceType.SUBSONIC)
    fun subsonicDataSourceServer(
        db: DatabaseClient,
        settingsManager: SettingsManager,
        subsonicApiClient: SubsonicApiClient,
        customMediaApiClient: CustomMediaApiClient,
        contextWrapper: ContextWrapper
    ): IDataSourceParentServer {
        return SubsonicDatasourceServer(
            db,
            settingsManager,
            subsonicApiClient,
            customMediaApiClient,
            contextWrapper
        )
    }

    @Factory
    @DataSourceKey(DataSourceType.NAVIDROME)
    fun navidromeDatasourceServer(
        db: DatabaseClient,
        settingsManager: SettingsManager,
        navidromeApiClient: NavidromeApiClient,
        customMediaApiClient: CustomMediaApiClient,
        contextWrapper: ContextWrapper
    ): IDataSourceParentServer {
        return NavidromeDatasourceServer(
            db,
            settingsManager,
            navidromeApiClient,
            customMediaApiClient,
            contextWrapper
        )
    }

    @Factory
    @DataSourceKey(DataSourceType.EMBY)
    fun embyDatasourceServer(
        db: DatabaseClient,
        settingsManager: SettingsManager,
        embyApiClient: EmbyApiClient,
        customMediaApiClient: CustomMediaApiClient,
        contextWrapper: ContextWrapper
    ): IDataSourceParentServer {
        return EmbyDatasourceServer(
            db,
            settingsManager,
            embyApiClient,
            customMediaApiClient,
            contextWrapper
        )
    }


    @Factory
    @DataSourceKey(DataSourceType.PLEX)
    fun plexDatasourceServer(
        db: DatabaseClient,
        settingsManager: SettingsManager,
        plexApiClient: PlexApiClient,
        customMediaApiClient: CustomMediaApiClient,
        contextWrapper: ContextWrapper
    ): IDataSourceParentServer {
        return PlexDatasourceServer(
            db,
            settingsManager,
            plexApiClient,
            customMediaApiClient,
            contextWrapper
        )
    }


    @Singleton
    fun dataSourceManager(
        db: DatabaseClient,
//        alarmConfig: AlarmConfig,
        versionApiClient: VersionApiClient,
    ): DataSourceManager {
        val dataSourceManager = DataSourceManager(
            db,
//            alarmConfig,
            versionApiClient
        )
        return dataSourceManager
    }

}
