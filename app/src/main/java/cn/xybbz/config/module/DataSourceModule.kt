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
import cn.xybbz.config.connection.ConnectionConfigServer
import cn.xybbz.config.alarm.AlarmConfig
import cn.xybbz.config.favorite.FavoriteRepository
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


    @Singleton
    @Provides
    fun dataSourceManager(
        @ApplicationContext application: Context,
        db: DatabaseClient,
        dataSources: Map<DataSourceType, @JvmSuppressWildcards Provider<IDataSourceParentServer>>,
        connectionConfigServer: ConnectionConfigServer,
        alarmConfig: AlarmConfig,
        favoriteRepository: FavoriteRepository,
        versionApiClient: VersionApiClient
    ): DataSourceManager {
        val dataSourceManager = DataSourceManager(
            application = application,
            db,
            dataSources,
            connectionConfigServer,
            alarmConfig,
            favoriteRepository,
            versionApiClient
        )
        return dataSourceManager
    }

}