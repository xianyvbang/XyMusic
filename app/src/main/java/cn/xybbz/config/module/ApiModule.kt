package cn.xybbz.config.module

import cn.xybbz.api.client.CacheApiClient
import cn.xybbz.api.client.ImageApiClient
import cn.xybbz.api.client.emby.EmbyApiClient
import cn.xybbz.api.client.jellyfin.JellyfinApiClient
import cn.xybbz.api.client.navidrome.NavidromeApiClient
import cn.xybbz.api.client.subsonic.SubsonicApiClient
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
    fun jellyfinApiClient(): JellyfinApiClient {
        val defaultApiConfig = JellyfinApiClient()
        return defaultApiConfig
    }

    @Singleton
    @Provides
    fun subsonicApiClient():SubsonicApiClient{
        val subsonicApiClient = SubsonicApiClient()
        return subsonicApiClient
    }

    @Singleton
    @Provides
    fun navidromeApiClient(): NavidromeApiClient {
        val navidromeApiClient = NavidromeApiClient()
        return navidromeApiClient
    }

    @Singleton
    @Provides
    fun embyApiClient(): EmbyApiClient {
        val embyApiClient = EmbyApiClient()
        return embyApiClient
    }

    @Singleton
    @Provides
    fun cacheApiClient():CacheApiClient{
        val cacheApiClient = CacheApiClient()
        return cacheApiClient
    }

    @Singleton
    @Provides
    fun imageApiClient(): ImageApiClient {
        val imageApiClient = ImageApiClient()
        return imageApiClient
    }
}