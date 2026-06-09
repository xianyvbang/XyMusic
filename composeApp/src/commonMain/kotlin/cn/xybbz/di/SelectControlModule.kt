package cn.xybbz.di

import cn.xybbz.config.select.SelectControl
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
@Configuration
class SelectControlModule {

    /**
     * 提供选择控制器。
     * DataSourceManager 改为动作执行时传入，避免选择控制器单例创建时提前拉起数据源。
     */
    @Singleton
    fun selectListData(): SelectControl {
        return SelectControl()
    }
}
