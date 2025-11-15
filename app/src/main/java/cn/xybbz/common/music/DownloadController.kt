package cn.xybbz.common.music

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import cn.xybbz.api.client.CacheApiClient
import cn.xybbz.common.utils.CoroutineScopeUtils
import cn.xybbz.entity.data.music.CacheTask
import cn.xybbz.localdata.data.music.XyMusic
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

class DownloadController(
    private val context: Context,
    private val cacheApiClient: CacheApiClient
) {

    private val cache: Cache

    private val cacheTask: ConcurrentHashMap<String, CacheTask> = ConcurrentHashMap()
    private val cacheCoroutineScope = CoroutineScopeUtils.getIo(this.javaClass.name)

    private var downloadManager: DownloadManager
    private var downloadNotificationHelper: DownloadNotificationHelper

    private val childPath = "download"

    companion object {
        const val DOWNLOAD_NOTIFICATION_CHANNEL_ID: String = "download_channel";
    }

    init {
        val cacheParentDirectory =
            if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
                File(
                    context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                    childPath
                )
            } else {
                File(context.filesDir, childPath)
            }
        Log.i("download", "下载缓存初始化 $cacheParentDirectory")
        // 设置缓存目录和缓存机制，如果不需要清除缓存可以使用NoOpCacheEvictor

        val cacheDir = File(cacheParentDirectory, "example_media_download")
        val databaseProvider = ExampleDatabaseProvider(context)
        cache = SimpleCache(
            cacheDir,
            //读取配置
            NoOpCacheEvictor(), databaseProvider
        )

        val dataSourceFactory = OkHttpDataSource.Factory(cacheApiClient.okhttpClientFunction())
        val downloadExecutor = Executors.newFixedThreadPool(/* nThreads= */ 6)

        downloadManager =
            DownloadManager(
                context,
                databaseProvider,
                cache,
                dataSourceFactory,
                downloadExecutor
            )
        downloadNotificationHelper =
            DownloadNotificationHelper(context, DOWNLOAD_NOTIFICATION_CHANNEL_ID);
        // Optionally, properties can be assigned to configure the download manager.
//        downloadManager.requirements = requirements
        downloadManager.maxParallelDownloads = 3
    }

    fun getDownloadManagerData(): DownloadManager {
        return downloadManager
    }

    fun getDownloadNotificationHelper(): DownloadNotificationHelper {
        return downloadNotificationHelper
    }

    fun downLoadMusic(music: XyMusic) {
        val downloadRequest =
            DownloadRequest.Builder(music.itemId, Uri.parse(music.musicUrl)).build()

        DownloadService.sendAddDownload(
            context,
            DemoDownloadService::class.java,
            downloadRequest,
            /* foreground= */ true
        )
    }
}