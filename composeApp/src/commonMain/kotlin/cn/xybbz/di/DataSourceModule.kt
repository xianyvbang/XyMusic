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
import cn.xybbz.common.enums.DataSourceQualifiers
import cn.xybbz.localdata.config.LocalDatabaseClient
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton

@Module
@Configuration
class DataSourceModule {

    @Factory
    @Named(DataSourceQualifiers.JELLYFIN)
    fun jellyfinApiServer(
        jellyfinApiClient: JellyfinApiClient,
    ): IDataSourceParentServer {
        return JellyfinDatasourceServer(
            jellyfinApiClient,
        )
    }

    @Factory
    @Named(DataSourceQualifiers.SUBSONIC)
    fun subsonicDataSourceServer(
        subsonicApiClient: SubsonicApiClient,
    ): IDataSourceParentServer {
        return SubsonicDatasourceServer(
            subsonicApiClient,
        )
    }

    @Factory
    @Named(DataSourceQualifiers.NAVIDROME)
    fun navidromeDatasourceServer(
        navidromeApiClient: NavidromeApiClient,
    ): IDataSourceParentServer {
        return NavidromeDatasourceServer(
            navidromeApiClient,
        )
    }

    @Factory
    @Named(DataSourceQualifiers.EMBY)
    fun embyDatasourceServer(
        embyApiClient: EmbyApiClient,
    ): IDataSourceParentServer {
        return EmbyDatasourceServer(
            embyApiClient,
        )
    }


    @Factory
    @Named(DataSourceQualifiers.PLEX)
    fun plexDatasourceServer(
        plexApiClient: PlexApiClient,
    ): IDataSourceParentServer {
        return PlexDatasourceServer(
            plexApiClient,
        )
    }


    @Singleton
    fun dataSourceManager(
        db: LocalDatabaseClient,
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
