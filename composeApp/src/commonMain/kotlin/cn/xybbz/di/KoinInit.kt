package cn.xybbz.di

import org.koin.core.KoinApplication
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.includes
import org.koin.plugin.module.dsl.startKoin

fun initKoin(config: KoinAppDeclaration? = null): KoinApplication {
    return startKoin<StartKoinApp> {
        includes(config)
        modules(

        )
    }
}

@org.koin.core.annotation.KoinApplication
class StartKoinApp