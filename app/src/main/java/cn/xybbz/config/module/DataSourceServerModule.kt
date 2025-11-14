package cn.xybbz.config.module

import cn.xybbz.api.client.IDataSourceParentServer
import cn.xybbz.api.client.emby.EmbyDatasourceServer
import cn.xybbz.api.client.jellyfin.JellyfinDatasourceServer
import cn.xybbz.api.client.navidrome.NavidromeDatasourceServer
import cn.xybbz.api.client.plex.PlexDatasourceServer
import cn.xybbz.api.client.subsonic.SubsonicDatasourceServer
import cn.xybbz.config.module.annotations.DataSourceKey
import cn.xybbz.localdata.enums.DataSourceType
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap

@Module
@InstallIn(SingletonComponent::class)
object DataSourceServerModule {

    @Provides
    @IntoMap
    @DataSourceKey(DataSourceType.JELLYFIN)
    fun provideJellyfin(server: JellyfinDatasourceServer): IDataSourceParentServer = server

    @Provides
    @IntoMap
    @DataSourceKey(DataSourceType.SUBSONIC)
    fun provideSubsonic(server: SubsonicDatasourceServer): IDataSourceParentServer = server

    @Provides
    @IntoMap
    @DataSourceKey(DataSourceType.NAVIDROME)
    fun provideNavidrome(server: NavidromeDatasourceServer): IDataSourceParentServer = server

    @Provides
    @IntoMap
    @DataSourceKey(DataSourceType.EMBY)
    fun provideEmby(server: EmbyDatasourceServer): IDataSourceParentServer = server

    @Provides
    @IntoMap
    @DataSourceKey(DataSourceType.PLEX)
    fun providePlex(server: PlexDatasourceServer): IDataSourceParentServer = server
}