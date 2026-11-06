package cn.xybbz.common.utils

import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.localdata.data.connection.ConnectionConfig
import cn.xybbz.startup.SessionCoordinator

/**
 * 切换数据源方法
 */
object DataSourceChangeUtils {

    /** 清空旧播放队列后，通过应用会话协调器切换连接。 */
    suspend fun changeDataSource(
        connectionConfig: ConnectionConfig,
        sessionCoordinator: SessionCoordinator,
        musicController: MusicCommonController
    ) {
        if (connectionConfig.id != sessionCoordinator.state.value.connection?.id) {
            //清空所有下载
            musicController.clearPlayerList()
            sessionCoordinator.switchConnection(connectionConfig)

        }
    }

    /**
     * 清空数据源之后
     */
    /** 清理连接配置与播放队列，并清除当前应用会话。 */
    suspend fun clearDataSourceAfter(
        musicController: MusicCommonController,
        sessionCoordinator: SessionCoordinator,
        settingsManager: SettingsManager
    ) {
        settingsManager.saveConnectionId(null, null)
        settingsManager.updateIfConnectionConfig(false)
        musicController.clearPlayerList()
        sessionCoordinator.clear()
    }
}
