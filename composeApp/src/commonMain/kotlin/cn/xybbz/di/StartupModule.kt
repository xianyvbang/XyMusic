package cn.xybbz.di

import cn.xybbz.config.HomeDataRepository
import cn.xybbz.config.proxy.ProxyConfigServer
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.startup.DataSourceBootstrapper
import cn.xybbz.startup.StartupInitializer
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
@Configuration
class StartupModule {

    /**
     * 提供轻量启动初始化器。
     * 该初始化器不依赖 DataSourceManager，避免应用启动阶段提前创建数据源链路。
     */
    @Singleton
    fun startupInitializer(
        homeDataRepository: HomeDataRepository,
        proxyConfigServer: ProxyConfigServer,
        settingsManager: SettingsManager,
    ): StartupInitializer {
        return StartupInitializer(
            homeDataRepository = homeDataRepository,
            proxyConfigServer = proxyConfigServer,
            settingsManager = settingsManager
        )
    }

    /**
     * 提供首页首帧后的数据源启动协调器。
     * 具体依赖由协调器内部按需解析，防止 Koin 创建单例时拉起重依赖。
     */
    @Singleton
    fun dataSourceBootstrapper(): DataSourceBootstrapper {
        return DataSourceBootstrapper()
    }
}
