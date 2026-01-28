package cn.xybbz.config.module

import cn.xybbz.api.client.CacheApiClient
import cn.xybbz.api.client.ImageApiClient
import cn.xybbz.api.client.emby.EmbyApiClient
import cn.xybbz.api.client.jellyfin.JellyfinApiClient
import cn.xybbz.api.client.navidrome.NavidromeApiClient
import cn.xybbz.api.client.plex.PlexApiClient
import cn.xybbz.api.client.subsonic.SubsonicApiClient
import cn.xybbz.api.client.version.VersionApiClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ApiModule {
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
    fun versionApiClient(): VersionApiClient {
        val versionApiClient = VersionApiClient()
        return versionApiClient
    }
}