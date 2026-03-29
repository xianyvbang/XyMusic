package cn.xybbz.di

import cn.xybbz.StartKoinApp
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.utils.Log
import cn.xybbz.config.HomeDataRepository
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.config.proxy.ProxyConfigServer
import cn.xybbz.config.setting.SettingsManager
import kotlinx.coroutines.runBlocking
import org.koin.core.KoinApplication
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.includes
import org.koin.plugin.module.dsl.startKoin

fun initKoin(config: KoinAppDeclaration? = null): KoinApplication {
    val koin = startKoin<StartKoinApp> {
        includes(config)
        modules(
        )
    }
    runBlocking {
        val koinTmp = koin.koin
        koinTmp.get<ProxyConfigServer>().initConfig()
        val settings = koinTmp.get<SettingsManager>().setSettingsData()
        koinTmp.get<DataSourceManager>().initDataSource(settings.dataSourceType)
        koinTmp.get<HomeDataRepository>().initData()
        Log.i("init", "musicController加载")
        koinTmp.get<MusicCommonController>().initController()
    }
    return koin
}

