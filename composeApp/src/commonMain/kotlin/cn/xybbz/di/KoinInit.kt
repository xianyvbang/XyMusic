package cn.xybbz.di

import cn.xybbz.StartKoinApp
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.utils.CoroutineScopeUtils
import cn.xybbz.common.utils.Log
import cn.xybbz.common.utils.PlayerListRestoreUtils
import cn.xybbz.config.HomeDataRepository
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.config.music.PlayerEventCoordinator
import cn.xybbz.config.proxy.ProxyConfigServer
import cn.xybbz.config.setting.SettingsManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.core.KoinApplication
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.includes
import org.koin.plugin.module.dsl.startKoin

fun initKoin(config: KoinAppDeclaration? = null): KoinApplication {

    val appScope = CoroutineScopeUtils.getIo("koin-init")
    val koin = startKoin<StartKoinApp> {
        includes(config)
        modules(
        )
    }
/*
    runBlocking {
        val koinTmp = koin.koin
        koinTmp.get<HomeDataRepository>().initData()

    }*/
    appScope.launch {
        val koinTmp = koin.koin
        koinTmp.get<HomeDataRepository>().initData()
        koinTmp.get<ProxyConfigServer>().initConfig()
        val settings = koinTmp.get<SettingsManager>().setSettingsData()
        koinTmp.get<DataSourceManager>().initDataSource(settings.dataSourceType)


    }

    appScope.launch {
        val koinTmp = koin.koin
        // 先启动播放器事件协调器，再初始化控制器，确保后续播放器事件有统一接收方。
        koinTmp.get<PlayerEventCoordinator>().start()
        koinTmp.get<MusicCommonController>().initController {
            appScope.launch {
                // 只有控制器 ready 之后再恢复播放列表，才能避免恢复时机早于播放器初始化。
                PlayerListRestoreUtils.restoreCurrentDataSourcePlayerList(
                    koinTmp.get(),
                    koinTmp.get()
                )
            }
        }
        Log.i("init", "musicController加载")
    }
    return koin
}

