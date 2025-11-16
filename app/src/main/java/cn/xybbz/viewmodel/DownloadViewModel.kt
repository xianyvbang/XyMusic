package cn.xybbz.viewmodel

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import cn.xybbz.config.download.core.DownloadListener
import cn.xybbz.config.favorite.FavoriteRepository
import cn.xybbz.localdata.data.download.XyDownload
import cn.xybbz.localdata.enums.DownloadStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.lang.Exception
import javax.inject.Inject

@SuppressLint("UnsafeOptInUsageError")
@HiltViewModel
class DownloadViewModel @Inject constructor(
    val favoriteRepository: FavoriteRepository,
): ViewModel() {


    private val downloadListener = object : DownloadManager.Listener {

        override fun onDownloadsPausedChanged(
            downloadManager: DownloadManager,
            downloadsPaused: Boolean
        ) {

            downloadManager.getCurrentDownloads()
            super.onDownloadsPausedChanged(downloadManager, downloadsPaused)
        }

        override fun onDownloadRemoved(
            downloadManager: DownloadManager,
            download: Download
        ) {
            super.onDownloadRemoved(downloadManager, download)
        }

        override fun onDownloadChanged(
            downloadManager: DownloadManager,
            download: Download,
            finalException: Exception?
        ) {
            super.onDownloadChanged(downloadManager, download, finalException)
        }
    }

    init {

    }
}