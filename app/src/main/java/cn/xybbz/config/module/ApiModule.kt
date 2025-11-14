package cn.xybbz.config.module

import cn.xybbz.api.client.CacheApiClient
import cn.xybbz.api.client.ImageApiClient
import cn.xybbz.api.client.emby.EmbyApiClient
import cn.xybbz.api.client.jellyfin.JellyfinApiClient
import cn.xybbz.api.client.navidrome.NavidromeApiClient
import cn.xybbz.api.client.plex.PlexApiClient
import cn.xybbz.api.client.subsonic.SubsonicApiClient
import cn.xybbz.api.client.version.VersionApiClient
import cn.xybbz.config.module.annotations.ApiTypeKey
import cn.xybbz.localdata.enums.DownloadTypes
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ApiModule {

    @Provides
    @IntoMap
    @ApiTypeKey(DownloadTypes.JELLYFIN)
    fun jellyfinApiClient(): JellyfinApiClient {
        val defaultApiConfig = JellyfinApiClient()
        return defaultApiConfig
    }

    @Provides
    @IntoMap
    @ApiTypeKey(DownloadTypes.SUBSONIC)
    fun subsonicApiClient(): SubsonicApiClient {
        val subsonicApiClient = SubsonicApiClient()
        return subsonicApiClient
    }

    @Provides
    @IntoMap
    @ApiTypeKey(DownloadTypes.NAVIDROME)
    fun navidromeApiClient(): NavidromeApiClient {
        val navidromeApiClient = NavidromeApiClient()
        return navidromeApiClient
    }

    @Provides
    @IntoMap
    @ApiTypeKey(DownloadTypes.EMBY)
    fun embyApiClient(): EmbyApiClient {
        val embyApiClient = EmbyApiClient()
        return embyApiClient
    }

    @Provides
    @IntoMap
    @ApiTypeKey(DownloadTypes.PLEX)
    fun plexApiClient(): PlexApiClient {
        val plexApiClient = PlexApiClient()
        return plexApiClient
    }

    @Singleton
    @Provides
    fun cacheApiClient(): CacheApiClient {
        val cacheApiClient = CacheApiClient()
        return cacheApiClient
    }

    @Singleton
    @Provides
    fun imageApiClient(): ImageApiClient {
        val imageApiClient = ImageApiClient()
        return imageApiClient
    }

    @Singleton
    @Provides
    @IntoMap
    @ApiTypeKey(DownloadTypes.APK)
    fun versionApiClient(): VersionApiClient {
        val versionApiClient = VersionApiClient()
        return versionApiClient
    }
}