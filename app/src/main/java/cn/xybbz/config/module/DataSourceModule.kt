package cn.xybbz.config.module

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
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
import cn.xybbz.config.ConnectionConfigServer
import cn.xybbz.config.IDataSourceManager
import cn.xybbz.config.alarm.AlarmConfig
import cn.xybbz.config.favorite.FavoriteRepository
import cn.xybbz.localdata.config.DatabaseClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataSourceModule {

    @Singleton
    @Provides
    fun jellyfinApiServer(
        db: DatabaseClient,
        connectionConfigServer: ConnectionConfigServer,
        jellyfinApiClient: JellyfinApiClient,
        @ApplicationContext application: Context
    ): JellyfinDatasourceServer {
        return JellyfinDatasourceServer(
            db,
            application,
            connectionConfigServer,
            jellyfinApiClient
        )
    }

    @Singleton
    @Provides
    fun subsonicDataSourceServer(
        db: DatabaseClient,
        connectionConfigServer: ConnectionConfigServer,
        subsonicApiClient: SubsonicApiClient,
        @ApplicationContext application: Context
    ): SubsonicDatasourceServer {
        return SubsonicDatasourceServer(
            db,
            application,
            connectionConfigServer,
            subsonicApiClient
        )
    }

    @Singleton
    @Provides
    fun navidromeDatasourceServer(
        db: DatabaseClient,
        connectionConfigServer: ConnectionConfigServer,
        navidromeApiClient: NavidromeApiClient,
        @ApplicationContext application: Context
    ): NavidromeDatasourceServer {
        return NavidromeDatasourceServer(
            db,
            application,
            connectionConfigServer,
            navidromeApiClient
        )
    }

    @Singleton
    @Provides
    fun embyDatasourceServer(
        db: DatabaseClient,
        connectionConfigServer: ConnectionConfigServer,
        embyApiClient: EmbyApiClient,
        @ApplicationContext application: Context
    ): EmbyDatasourceServer {
        return EmbyDatasourceServer(
            db,
            application,
            connectionConfigServer,
            embyApiClient
        )
    }


    @Singleton
    @Provides
    fun plexDatasourceServer(
        db: DatabaseClient,
        connectionConfigServer: ConnectionConfigServer,
        plexApiClient: PlexApiClient,
        @ApplicationContext application: Context
    ): PlexDatasourceServer {
        return PlexDatasourceServer(
            db,
            application,
            connectionConfigServer,
            plexApiClient
        )
    }


    @OptIn(UnstableApi::class)
    @Singleton
    @Provides
    fun dataSourceManager(
        @ApplicationContext application: Context,
        db: DatabaseClient,
        jellyfinDatasourceServer: JellyfinDatasourceServer,
        subsonicDataSourceServer: SubsonicDatasourceServer,
        navidromeDatasourceServer: NavidromeDatasourceServer,
        embyDatasourceServer: EmbyDatasourceServer,
        plexDatasourceServer: PlexDatasourceServer,
        connectionConfigServer: ConnectionConfigServer,
        alarmConfig: AlarmConfig,
        favoriteRepository: FavoriteRepository
    ): IDataSourceManager {
        val dataSourceManager = IDataSourceManager(
            application,
            db,
            jellyfinDatasourceServer,
            subsonicDataSourceServer,
            navidromeDatasourceServer,
            embyDatasourceServer,
            plexDatasourceServer,
            connectionConfigServer,
            alarmConfig,
            favoriteRepository
        )
        dataSourceManager.initDataSource()
        return dataSourceManager
    }

}