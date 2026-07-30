package cn.xybbz.startup

import cn.xybbz.config.HomeDataRepository
import cn.xybbz.config.proxy.ProxyConfigServer
import cn.xybbz.config.setting.SettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

/**
 * 应用启动阶段的轻量初始化器。
 * 这里只处理主题设置、首页缓存和代理配置，不再触发 DataSourceManager 或播放器重依赖。
 */
class StartupInitializer(
    private val homeDataRepository: HomeDataRepository,
    private val proxyConfigServer: ProxyConfigServer,
    private val settingsManager: SettingsManager,
) {

    /**
     * 启动轻量初始化任务。
     *
     * @param coroutineScope 应用级协程作用域，任务会在 IO 调度器中执行。
     */
    fun start(coroutineScope: CoroutineScope) {

        coroutineScope.launch(Dispatchers.IO) {
            settingsManager.initSet()

            // 这些是主界面可用前需要恢复的轻量配置和首页缓存。
            val homeDataJob = launch {
                homeDataRepository.initData()
            }
            val proxyConfigJob = launch {
                proxyConfigServer.initConfig()
            }

            homeDataJob.join()
            proxyConfigJob.join()
        }
    }
}
