package cn.xybbz.di

import cn.xybbz.StartKoinApp
import org.koin.core.KoinApplication
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.includes
import org.koin.plugin.module.dsl.startKoin

/**
 * 只负责启动 Koin 和注册依赖。
 * 应用数据初始化、播放器初始化、自动登录等有耗时风险的流程统一交给 StartupViewModel 后台推进。
 */
fun initKoin(config: KoinAppDeclaration? = null): KoinApplication {
    return startKoin<StartKoinApp> {
        includes(config)
        modules(
        )
    }
}

