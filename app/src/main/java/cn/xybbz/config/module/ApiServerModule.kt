package cn.xybbz.config.module

import cn.xybbz.api.client.ApiConfig
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
import cn.xybbz.api.client.version.service.GitHubVersionApi
import cn.xybbz.config.module.annotations.DataSourceKey
import cn.xybbz.localdata.enums.DataSourceType
import cn.xybbz.localdata.enums.DownloadTypes
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap

@Module
@InstallIn(SingletonComponent::class)
class ApiServerModule {

    @Provides
    @IntoMap
    @ApiTypeKey(DownloadTypes.JELLYFIN)
    fun provideJellyfinApi(server: JellyfinApiClient): ApiConfig = server

    @Provides
    @IntoMap
    @ApiTypeKey(DownloadTypes.SUBSONIC)
    fun provideSubsonicApi(server: SubsonicApiClient): ApiConfig = server

    @Provides
    @IntoMap
    @ApiTypeKey(DownloadTypes.NAVIDROME)
    fun provideNavidromeApi(server: NavidromeApiClient): ApiConfig = server

    @Provides
    @IntoMap
    @ApiTypeKey(DownloadTypes.EMBY)
    fun provideEmbyApi(server: EmbyApiClient): ApiConfig = server

    @Provides
    @IntoMap
    @ApiTypeKey(DownloadTypes.PLEX)
    fun providePlexApi(server: PlexApiClient): ApiConfig = server


    @Provides
    @IntoMap
    @ApiTypeKey(DownloadTypes.APK)
    fun provideGithubApi(server: VersionApiClient): ApiConfig = server
}