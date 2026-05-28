package cn.xybbz.di

import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.config.HomeDataRepository
import cn.xybbz.config.proxy.ProxyConfigServer
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.startup.StartupInitializer
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
@Configuration
class StartupModule {

    @Singleton
    fun startupInitializer(
        homeDataRepository: HomeDataRepository,
        proxyConfigServer: ProxyConfigServer,
        settingsManager: SettingsManager,
        dataSourceManager: DataSourceManager,
    ): StartupInitializer {
        return StartupInitializer(
            homeDataRepository = homeDataRepository,
            proxyConfigServer = proxyConfigServer,
            settingsManager = settingsManager,
            dataSourceManager = dataSourceManager
        )
    }
}
