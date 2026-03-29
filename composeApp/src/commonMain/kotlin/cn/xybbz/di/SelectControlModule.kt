package cn.xybbz.di

import cn.xybbz.config.select.SelectControl
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
@Configuration
class SelectControlModule {

    @Singleton
    fun selectListData(): SelectControl {
        return SelectControl()
    }
}
