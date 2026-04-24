package cn.xybbz.di

import cn.xybbz.StartKoinApp
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.enums.PlayStateEnum
import cn.xybbz.common.utils.CoroutineScopeUtils
import cn.xybbz.common.utils.PlayerListRestoreUtils
import cn.xybbz.config.HomeDataRepository
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.config.music.PlayerEventCoordinator
import cn.xybbz.config.proxy.ProxyConfigServer
import cn.xybbz.config.setting.OnSettingsChangeListener
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.config.volume.VolumeServer
import cn.xybbz.localdata.enums.CacheUpperLimitEnum
import kotlinx.coroutines.launch
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
    appScope.launch {
        val koinTmp = koin.koin
        koinTmp.get<HomeDataRepository>().initData()
        koinTmp.get<ProxyConfigServer>().initConfig()
        val settingsManager = koinTmp.get<SettingsManager>()
        val settings = settingsManager.setSettingsData()
        // 先启动播放器事件协调器，再初始化控制器，确保后续播放器事件有统一接收方。
        koinTmp.get<PlayerEventCoordinator>().start()
        koinTmp.get<MusicCommonController>().initController {

            appScope.launch {
                PlayerListRestoreUtils.restoreCurrentDataSourcePlayerList(
                    koinTmp.get(),
                    koinTmp.get(),
                    koinTmp.get(),
                    settings.connectionId.toString()
                )
            }
            appScope.launch {
                koinTmp.get<VolumeServer>().updateVolume(settings.jvmVolume ?: 60)
            }

            settingsManager.setOnListener(object : OnSettingsChangeListener {
                override fun onCacheMaxBytesChanged(
                    cacheUpperLimit: CacheUpperLimitEnum,
                    oldCacheUpperLimit: CacheUpperLimitEnum
                ) {
                    if (oldCacheUpperLimit == CacheUpperLimitEnum.No && cacheUpperLimit != CacheUpperLimitEnum.No && state == PlayStateEnum.Playing) {
                        musicInfo?.let {
                            startCache(it, settingsManager.getStatic())
                        }
                    }
                }

                override suspend fun onMusicResourceConfigChanged() {
                    refreshPlaylistCoverMetadata()
                }
            })
        }
        val dataSourceManager = koinTmp.get<DataSourceManager>()
        dataSourceManager.initDataSource(settings.dataSourceType, settings.connectionId)
    }
    return koin
}

