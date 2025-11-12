package cn.xybbz.download.core

import cn.xybbz.api.client.version.VersionApiClient
import cn.xybbz.localdata.enums.DownloadStatus
import cn.xybbz.download.state.DownloadState
import cn.xybbz.localdata.data.download.XyDownload
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
        client: VersionApiClient,
        statusChange: suspend () -> DownloadStatus?,
        xyDownload: XyDownload
    ):Flow<DownloadState>
}