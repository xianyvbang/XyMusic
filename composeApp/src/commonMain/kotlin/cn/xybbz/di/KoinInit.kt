package cn.xybbz.di

import cn.xybbz.config.proxy.ProxyConfigServer
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
        koin.koin.get<ProxyConfigServer>().initConfig()
    }
    return koin
}

@org.koin.core.annotation.KoinApplication
class StartKoinApp