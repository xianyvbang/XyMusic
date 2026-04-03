package cn.xybbz.download.core

import cn.xybbz.config.download.state.DownloadState
import cn.xybbz.download.database.data.XyDownload
import cn.xybbz.download.enums.DownloadStatus
import cn.xybbz.platform.ContextWrapper
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.Flow

interface IDownloadCore {

    /**
     * 下载
     * @param [client] Ktor HttpClient
     * @param [statusChange] 状态变更信息
     */
    suspend fun download(
        client: HttpClient,
        contextWrapper: ContextWrapper? = null,
        statusChange: suspend () -> DownloadStatus?,
        xyDownload: XyDownload
    ): Flow<DownloadState>
}
