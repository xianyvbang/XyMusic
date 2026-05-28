package cn.xybbz.di

import cn.xybbz.common.utils.Log
import cn.xybbz.startup.StartupInitializer
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.KoinApplication

/**
 * Koin 启动完成后调用，用来拉起应用级后台初始化。
 */
fun KoinApplication.startCommonKoinModule(
    coroutineScope: CoroutineScope = createAppRootCoroutineScope(),
): KoinApplication {
    koin.get<StartupInitializer>().start(coroutineScope)
    return this
}

fun createAppRootCoroutineScope(): CoroutineScope {
    return CoroutineScope(
        CoroutineExceptionHandler { coroutineContext, throwable ->
            Log.e(
                "AppRootCoroutineScope",
                "未捕获的应用级协程异常: $coroutineContext",
                throwable
            )
        } + SupervisorJob() + Dispatchers.Default
    )
}
