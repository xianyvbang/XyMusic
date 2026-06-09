package cn.xybbz.di

import cn.xybbz.StartKoinApp
import org.koin.core.KoinApplication
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.includes
import org.koin.plugin.module.dsl.startKoin

/**
 * 启动 Koin、注册依赖，并在 Koin 可用后拉起应用级后台初始化。
 */
fun initKoin(config: KoinAppDeclaration? = null): KoinApplication {
    return startKoin<StartKoinApp> {
        includes(config)
        modules(
        )
    }.startCommonKoinModule()
}

