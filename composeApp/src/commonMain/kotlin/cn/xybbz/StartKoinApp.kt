package cn.xybbz

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module

@org.koin.core.annotation.KoinApplication(modules = [ViewModelModule::class])
class StartKoinApp

@Module
@Configuration
@ComponentScan("cn.xybbz")
class ViewModelModule