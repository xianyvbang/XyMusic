package cn.xybbz.config.download

import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.api.converter.jsonSerializer
import cn.xybbz.common.enums.getDownloadType
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.download.DownloaderManager
import cn.xybbz.download.core.DownloadRequest
import cn.xybbz.localdata.data.music.XyMusic
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.add_download_list

suspend fun DownloaderManager.enqueueMusicDownload(
    musicData: XyMusic,
    dataSourceManager: DataSourceManager,
) {
    val downloadTypes = getDownloadType(dataSourceManager.dataSourceType)
    enqueue(
        DownloadRequest(
            url = musicData.downloadUrl,
            fileName = musicData.name + "." + musicData.container,
            fileSize = musicData.size ?: 0,
            uid = musicData.itemId,
            title = musicData.name,
            type = downloadTypes.toString(),
            cover = musicData.pic,
            duration = musicData.runTimeTicks,
            mediaLibraryId = dataSourceManager.getConnectionId().toString(),
            data = jsonSerializer.encodeToString(musicData),
        )
    )
    MessageUtils.sendPopTip(Res.string.add_download_list)
}
