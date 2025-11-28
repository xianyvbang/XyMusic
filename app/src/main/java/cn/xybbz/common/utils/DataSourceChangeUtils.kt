package cn.xybbz.common.utils

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import cn.xybbz.api.client.IDataSourceManager
import cn.xybbz.common.music.MusicController
import cn.xybbz.localdata.data.connection.ConnectionConfig

/**
 * 切换数据源方法
 */
object DataSourceChangeUtils {

    @OptIn(UnstableApi::class)
    suspend fun changeDataSource(
        connectionConfig: ConnectionConfig,
        dataSourceManager: IDataSourceManager,
        musicController: MusicController
    ) {
        //清空所有下载
        musicController.clearPlayerList()
        dataSourceManager.changeDataSource(connectionConfig)
    }
}