package cn.xybbz.download.core

import cn.xybbz.config.download.state.DownloadState
import cn.xybbz.download.database.data.XyDownload
import cn.xybbz.download.database.enums.DownloadStatus
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.Flow

interface IDownloadCore {

    /**
     * 下载
     * @param [url] 网址
     * @param [fileName] 文件名
     * @param [filePath] 文件路径
     * @param [client] okhttp客户端
     * @param [statusChange] 状态变更信息
     */
    suspend fun download(
        client: HttpClient,
        statusChange: suspend () -> DownloadStatus?,
        xyDownload: XyDownload
    ): Flow<DownloadState>
}