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
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.CacheWriter
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.upstream.DefaultLoadErrorHandlingPolicy
import cn.xybbz.api.client.CacheApiClient
import cn.xybbz.common.utils.CoroutineScopeUtils
import cn.xybbz.config.setting.OnSettingsChangeListener
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.entity.data.music.CacheTask
import cn.xybbz.localdata.data.music.XyPlayMusic
import cn.xybbz.localdata.enums.CacheUpperLimitEnum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ConcurrentHashMap


@SuppressLint("UnsafeOptInUsageError")
class CacheController(
    private val context: Context,
    private val settingsManager: SettingsManager,
    private val cacheApiClient: CacheApiClient
) {

    private val cache: Cache
    private var cacheDataSourceFactory: CacheDataSource.Factory
    private var cacheDataSource: CacheDataSource
    private var upstreamDataSourceFactory: DefaultDataSource.Factory

    private val cacheTask: ConcurrentHashMap<String, CacheTask> = ConcurrentHashMap()
    private val cacheCoroutineScope = CoroutineScopeUtils.getIo(this.javaClass.name)

    private val childPath = "cache"

    private val cacheSchedule = MutableStateFlow(0f)
    val _cacheSchedule = cacheSchedule.asStateFlow()

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
        Log.i("catch", "缓存初始化 $cacheParentDirectory")
        // 设置缓存目录和缓存机制，如果不需要清除缓存可以使用NoOpCacheEvictor

        val cacheDir = File(cacheParentDirectory, "example_media_cache")
        settingsManager.updateCacheFilePath(cacheDir.path)
        cache = SimpleCache(
            cacheDir,
            //读取配置
            XyCacheEvictor(settingsManager), ExampleDatabaseProvider(context)
        )

        // 根据缓存目录创建缓存数据源
        upstreamDataSourceFactory = DefaultDataSource.Factory(
            context,
            OkHttpDataSource.Factory(cacheApiClient.okhttpClientFunction())
        )
        cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(
                upstreamDataSourceFactory
            ).setCacheWriteDataSinkFactory(null)
        // 设置上游数据源，缓存未命中时通过此获取数据
        /*.setUpstreamDataSourceFactory(
            DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(true)
        )*/
        cacheDataSource = cacheDataSourceFactory.createDataSource()

        settingsManager.setOnListener(object :OnSettingsChangeListener {
            override fun onCacheMaxBytesChanged(
                cacheUpperLimit: CacheUpperLimitEnum,
                oldCacheUpperLimit: CacheUpperLimitEnum
            ) {
                if (cacheUpperLimit == CacheUpperLimitEnum.No){
                    clearCache()
                }
            }

        })
    }

    fun cacheMedia(music: XyPlayMusic) {
        if (settingsManager.get().cacheUpperLimit != CacheUpperLimitEnum.No){
            val url = music.getMusicUrl()
            val itemId = music.itemId
            cacheCoroutineScope.launch(Dispatchers.IO) {
                val dataSpec = DataSpec.Builder()
                    .setKey(itemId)
                    .setUri(url)
                    .setLength(music.size ?: 20000)
                    .build()

                val existingTask = cacheTask[itemId]

                try {
                    if (existingTask != null) {
                        if (existingTask.isPaused) {
                            Log.i("=====", "继续缓存: $url --- $itemId")
                            // 重新创建 CacheWriter（因为 cancel() 后不能恢复）
                            val newWriter = createCacheWriter(dataSpec)
                            cacheTask[itemId] = CacheTask(newWriter, isPaused = false)
                            newWriter.cache()
                        } else {
                            Log.i("=====", "任务已在进行中: $url --- $itemId")
                        }
                    } else {
                        Log.i("=====", "新建缓存: $url")
                        val writer = createCacheWriter(dataSpec)
                        cacheTask[itemId] = CacheTask(writer, isPaused = false)
                        writer.cache()
                    }
                } catch (e: Exception) {
                    Log.e("=====", "缓存数据报错: ${e.message} --- ${music.name}", e)
                }

            }
        }

    }

    private fun createCacheWriter(dataSpec: DataSpec): CacheWriter {
        val cacheDataSource = CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(
                upstreamDataSourceFactory
            )
            .createDataSource()
        return CacheWriter(
            cacheDataSource, // 这里用你初始化好的 CacheDataSource
            dataSpec,
            null
        ) { requestLength, bytesCached, newBytesCached ->
            val progress = bytesCached * 1f / requestLength
            cacheSchedule.value = progress
//            Log.i("=====", "进度: $progress url=${music.musicUrl}")
        }
    }

    /**
     * 暂停当前缓存
     */
    fun pauseCache(itemId: String) {
        val task = cacheTask[itemId]
        if (task != null && !task.isPaused) {
            task.cacheWriter.cancel() // 停止当前写入
            task.isPaused = true
            Log.i("=====", "已暂停缓存: $itemId")
        }
    }

    /**
     * 取消所有缓存
     */
    fun cancelAllCache() {
        cacheTask.forEach { (_, u) ->
            u.cacheWriter.cancel()
            u.isPaused = true
        }
        cacheTask.clear()
    }

    fun getMediaSourceFactory(): MediaSource.Factory? {
        // 创建逐步加载数据的数据源

//        val mediaSourceFactory = ProgressiveMediaSource.Factory(cacheDataSourceFactory)
        val mediaSourceFactory =
            DefaultMediaSourceFactory(context).setDataSourceFactory(cacheDataSourceFactory)
//        val mediaSourceFactory = HlsMediaSource.Factory(cacheDataSourceFactory)
                .setLoadErrorHandlingPolicy(DefaultLoadErrorHandlingPolicy(2))
        return mediaSourceFactory
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
        cacheTask.forEach { (_, u) ->
            u.cacheWriter.cancel()
            u.isPaused = true
        }
        cacheTask.clear()
        cacheDataSource.cache.keys.forEach {
            cacheDataSource.cache.removeResource(it)
        }
    }

    fun release() {
        cacheTask.values.forEach {
            it.cacheWriter.cancel()
            it.isPaused = true
        }
        cacheTask.clear()
        cache.release()
    }
}