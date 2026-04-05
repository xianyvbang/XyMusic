package cn.xybbz.di

import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.config.select.SelectControl
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
@Configuration
class SelectControlModule {

    @Singleton
    fun selectListData(
        dataSourceManager: DataSourceManager
    ): SelectControl {
        return SelectControl(dataSourceManager)
    }
}
