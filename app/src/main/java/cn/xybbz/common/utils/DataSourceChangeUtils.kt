package cn.xybbz.common.utils

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.music.MusicController
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.localdata.data.connection.ConnectionConfig

/**
 * 切换数据源方法
 */
object DataSourceChangeUtils {

    @OptIn(UnstableApi::class)
    suspend fun changeDataSource(
        connectionConfig: ConnectionConfig,
        dataSourceManager: DataSourceManager,
        musicController: MusicController
    ) {
        //清空所有下载
        musicController.clearPlayerList()
        dataSourceManager.changeDataSource(connectionConfig)
    }

    /**
     * 清空数据源之后
     */
    suspend fun clearDataSourceAfter(
        musicController: MusicController,
        dataSourceManager: DataSourceManager,
        settingsManager: SettingsManager
    ) {
        musicController.clearPlayerList()
        dataSourceManager.release()
        settingsManager.setSettingsData()
    }
}