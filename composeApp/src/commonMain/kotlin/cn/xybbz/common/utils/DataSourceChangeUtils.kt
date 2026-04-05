package cn.xybbz.common.utils

import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.config.music.MusicPlayContext
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.download.database.DownloadDatabaseClient
import cn.xybbz.localdata.config.LocalDatabaseClient
import cn.xybbz.localdata.data.connection.ConnectionConfig

/**
 * 切换数据源方法
 */
object DataSourceChangeUtils {

    suspend fun changeDataSource(
        connectionConfig: ConnectionConfig,
        dataSourceManager: DataSourceManager,
        musicController: MusicCommonController,
        db: LocalDatabaseClient,
        downloadDb: DownloadDatabaseClient,
        musicPlayContext: MusicPlayContext
    ) {
        if (connectionConfig.id != dataSourceManager.getConnectionId()) {
            //清空所有下载
            musicController.clearPlayerList()
            dataSourceManager.changeDataSource(connectionConfig)
            PlayerListRestoreUtils.restoreCurrentDataSourcePlayerList(db, downloadDb, musicPlayContext)
        }
    }

    /**
     * 清空数据源之后
     */
    suspend fun clearDataSourceAfter(
        musicController: MusicCommonController,
        dataSourceManager: DataSourceManager,
        settingsManager: SettingsManager
    ) {
        settingsManager.saveConnectionId(null, null)
        settingsManager.updateIfConnectionConfig(false)
        musicController.clearPlayerList()
        dataSourceManager.release()
    }
}
