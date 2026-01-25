/*
 *   XyMusic
 *   Copyright (C) 2023 xianyvbang
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package cn.xybbz.common.music

import android.annotation.SuppressLint
import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSink
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadHelper
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.upstream.DefaultLoadErrorHandlingPolicy
import cn.xybbz.api.client.CacheApiClient
import cn.xybbz.config.scope.IoScoped
import cn.xybbz.config.setting.OnSettingsChangeListener
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.localdata.data.music.XyPlayMusic
import cn.xybbz.localdata.enums.CacheUpperLimitEnum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.util.concurrent.Executors


@SuppressLint("UnsafeOptInUsageError")
class DownloadCacheController(
    private val context: Context,
    private val settingsManager: SettingsManager,
    cacheApiClient: CacheApiClient
) : IoScoped() {

    val cache: Cache
    var cacheDataSourceFactory: CacheDataSource.Factory
    var downloadCacheDataSourceFactory: CacheDataSource.Factory
    private var cacheDataSource: CacheDataSource
    private var upstreamDataSourceFactory: DefaultDataSource.Factory
    var downloadManager: DownloadManager
    private val downloadCacheProgressTicker: DownloadCacheProgressTicker

//    private val cacheTask: ConcurrentHashMap<String, CacheTask> = ConcurrentHashMap()

    /** 只允许一个任务 */
    private var download: Download? = null
    private var currentTaskId: String? = null

    private val childPath = "cache"

    private val _cacheSchedule = MutableStateFlow(0f)
    val cacheSchedule = _cacheSchedule.asStateFlow()

    init {
        //2025年1月20日 11:12:19 修改缓存数据目录到cache中,使其可以被系统的清除缓存功能删除
        val cacheParentDirectory =
            if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
                File(
//                    context.externalCacheDir,
                    context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                    childPath
                )
            } else {
                File(context.filesDir, childPath)
            }
        Log.i("music", "缓存初始化 $cacheParentDirectory")
        // 设置缓存目录和缓存机制，如果不需要清除缓存可以使用NoOpCacheEvictor

        val cacheDir = File(cacheParentDirectory, "example_media_cache")
        settingsManager.updateCacheFilePath(cacheDir.path)
        cache = SimpleCache(
            cacheDir,
            //读取配置
            LeastRecentlyUsedCacheEvictor(100 * 1024 * 1024)
            /*XyCacheEvictor(settingsManager)*/, ExampleDatabaseProvider(context)
        )

        // 根据缓存目录创建缓存数据源
        upstreamDataSourceFactory = DefaultDataSource.Factory(
            context,
            OkHttpDataSource.Factory(cacheApiClient.okhttpClientFunction())
                .setDefaultRequestProperties(mapOf("11111" to "22222"))
        )
        cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(
                upstreamDataSourceFactory
            ).setCacheWriteDataSinkFactory(null)
            .setCacheWriteDataSinkFactory(
                CacheDataSink.Factory()
                    .setCache(cache)
                    .setFragmentSize(2 * 1024 * 1024) // 2MB 分片写入
            )
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        downloadCacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(
                upstreamDataSourceFactory
            ).setCacheWriteDataSinkFactory(null)
            .setCacheWriteDataSinkFactory(
                CacheDataSink.Factory()
                    .setCache(cache)
                    .setFragmentSize(2 * 1024 * 1024) // 2MB 分片写入
            )
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        cacheDataSource = cacheDataSourceFactory.createDataSource()


        downloadManager =
            DownloadManager(
                context,
                StandaloneDatabaseProvider(context),
                cache,
                downloadCacheDataSourceFactory,
                Executors.newFixedThreadPool( /* nThreads= */6)
            )

        downloadCacheProgressTicker = DownloadCacheProgressTicker(
            this,
            1000L,
            {
                Log.i("music", "缓存进度 $it")
//                切换当前数据源?
                _cacheSchedule.value = it / 100.0f
            }
        )

        settingsManager.setOnListener(object : OnSettingsChangeListener {
            override fun onCacheMaxBytesChanged(
                cacheUpperLimit: CacheUpperLimitEnum,
                oldCacheUpperLimit: CacheUpperLimitEnum
            ) {
                if (cacheUpperLimit == CacheUpperLimitEnum.No) {
                    clearCache()
                }
            }

        })
    }

    fun cacheMedia(
        music: XyPlayMusic,
        ifStatic: Boolean
    ) {
        if (settingsManager.get().cacheUpperLimit == CacheUpperLimitEnum.No) return

        val itemId = music.itemId
        val url = music.getMusicUrl()
        scope.launch(Dispatchers.IO) {
            Log.i("music", "开始缓存1")
            /** 切换缓存 → 取消旧任务 */
            if (currentTaskId != null && currentTaskId != itemId) {
                cancelCurrentCache()
            }
            startNewCacheLocked(itemId, url, ifStatic)
        }
    }

    private fun startNewCacheLocked(
        itemId: String,
        url: String,
        ifStatic: Boolean
    ) {
        currentTaskId = itemId
        val oldDownload = downloadManager.downloadIndex.getDownload(itemId)
        if (oldDownload != null) {
            download = oldDownload
            _cacheSchedule.value = oldDownload.percentDownloaded / 100.0f
            if (oldDownload.percentDownloaded == 100f){
                return
            }
            resumeCache()
            return
        }
        if (ifStatic) {
            val downloadRequest = DownloadRequest.Builder(itemId, url.toUri()).build()
            DownloadService.sendAddDownload(
                context,
                ExoPlayerDownloadService::class.java,
                downloadRequest,
                /* foreground= */ false
            )
        } else {
            val downloadHelper =
                DownloadHelper.Factory()
                    .setRenderersFactory(DefaultRenderersFactory(context))
                    .create(getDownloadMediaSource(url))
            downloadHelper.prepare(object : DownloadHelper.Callback {
                override fun onPrepared(
                    helper: DownloadHelper,
                    tracksInfoAvailable: Boolean
                ) {
                    Log.i("music", "缓存准备完成: $itemId")
                    val data = ByteArray(8 * 1024)

                    val downloadRequest = helper.getDownloadRequest(itemId, data)
                    DownloadService.sendAddDownload(
                        context,
                        ExoPlayerDownloadService::class.java,
                        downloadRequest,
                        false
                    )
                    Log.i("music", "开始缓存: $itemId")

                    download = downloadManager.downloadIndex.getDownload(itemId)
                    downloadCacheProgressTicker.start(itemId)
                }

                override fun onPrepareError(
                    helper: DownloadHelper,
                    e: IOException
                ) {
                    Log.e("music", "缓存报错: $itemId", e)
                }
            })
        }


    }

    /**
     * 取消当前缓存
     */
    private fun cancelCurrentCache() {
        pauseCache()
        _cacheSchedule.value = 0f
        download = null
        currentTaskId = null
        Log.i("music", "已取消当前缓存")
    }

    /**
     * 暂停当前缓存
     */
    fun pauseCache() {
        DownloadService.sendSetStopReason(
            context,
            ExoPlayerDownloadService::class.java,
            currentTaskId,
            1,
            /* foreground= */ false
        )
        downloadCacheProgressTicker.stop()
    }

    /**
     * 恢复开始缓存
     */
    fun resumeCache() {
        currentTaskId?.let {
            DownloadService.sendSetStopReason(
                context,
                ExoPlayerDownloadService::class.java,
                it,
                Download.STATE_QUEUED,
                /* foreground= */ false
            )
            downloadCacheProgressTicker.start(it)
        }

    }

    /**
     * 取消所有缓存
     */
    fun cancelAllCache() {
        DownloadService.sendSetStopReason(
            context,
            ExoPlayerDownloadService::class.java,
            null,
            1,
            /* foreground= */ false
        )
    }

    fun getMediaSourceFactory(): MediaSource.Factory {
        // 创建逐步加载数据的数据源

//        val mediaSourceFactory = ProgressiveMediaSource.Factory(cacheDataSourceFactory)
        val mediaSourceFactory =
            DefaultMediaSourceFactory(context).setDataSourceFactory(cacheDataSourceFactory)
//        val mediaSourceFactory = HlsMediaSource.Factory(cacheDataSourceFactory)
                .setLoadErrorHandlingPolicy(DefaultLoadErrorHandlingPolicy(2))
        return mediaSourceFactory
    }

    fun getDownloadMediaSource(url: String): MediaSource {
        return DefaultMediaSourceFactory(context)
            .setDataSourceFactory(
                downloadCacheDataSourceFactory
            )
            .setLoadErrorHandlingPolicy(DefaultLoadErrorHandlingPolicy(2))
            .createMediaSource(
                MediaItem.Builder().setUri(url)
                    .setMimeType(MimeTypes.APPLICATION_M3U8).build()
            )
    }

    /**
     * 获得所有缓存大小
     */
    fun getCacheSize(): Long {
        //缓存大小
        return cacheDataSource.cache.cacheSpace
    }

    /**
     * 清空缓存
     */
    fun clearCache() {
        DownloadService.sendRemoveAllDownloads(
            context,
            ExoPlayerDownloadService::class.java,
            /* foreground= */ false
        )

    }

    override fun close() {
        release()
        super.close()
    }

    private fun release() {
        pauseCache()
        cache.release()
    }
}