package cn.xybbz.di

import cn.xybbz.api.client.custom.CustomMediaApiClient
import cn.xybbz.api.client.emby.EmbyApiClient
import cn.xybbz.api.client.jellyfin.JellyfinApiClient
import cn.xybbz.api.client.navidrome.NavidromeApiClient
import cn.xybbz.api.client.plex.PlexApiClient
import cn.xybbz.api.client.subsonic.SubsonicApiClient
import cn.xybbz.api.client.version.VersionApiClient
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
@Configuration
class ApiModule {

    @Factory
    fun jellyfinApiClient(): JellyfinApiClient {
        val defaultApiConfig = JellyfinApiClient()
        return defaultApiConfig
    }

    @Factory
    fun subsonicApiClient(): SubsonicApiClient {
        val subsonicApiClient = SubsonicApiClient()
        return subsonicApiClient
    }

    @Factory
    fun navidromeApiClient(): NavidromeApiClient {
        val navidromeApiClient = NavidromeApiClient()
        return navidromeApiClient
    }

    @Factory
    fun embyApiClient(): EmbyApiClient {
        val embyApiClient = EmbyApiClient()
        return embyApiClient
    }

    @Factory
    fun plexApiClient(): PlexApiClient {
        val plexApiClient = PlexApiClient()
        return plexApiClient
    }

 /*   @Singleton
    fun cacheApiClient(): CacheApiClient {
        val cacheApiClient = CacheApiClient()
        return cacheApiClient
    }*/


    @Singleton
    fun versionApiClient(): VersionApiClient {
        val versionApiClient = VersionApiClient()
        return versionApiClient
    }

    @Singleton
    fun customMediaApiClient(): CustomMediaApiClient {
        return CustomMediaApiClient()
    }
}
