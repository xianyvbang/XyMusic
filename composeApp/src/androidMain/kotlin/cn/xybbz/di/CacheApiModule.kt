package cn.xybbz.di

import cn.xybbz.api.client.CacheApiClient
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
class CacheApiModule {

    @Single
    fun cacheApiClient(): CacheApiClient {
        return CacheApiClient()
    }
}