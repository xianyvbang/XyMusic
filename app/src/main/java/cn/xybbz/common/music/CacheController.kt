package cn.xybbz.common.music

import android.annotation.SuppressLint
import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.media3.datasource.DataSourceUtil
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
import cn.xybbz.config.SettingsConfig
import cn.xybbz.localdata.data.music.XyMusic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap


@SuppressLint("UnsafeOptInUsageError")
class CacheController(
    private val context: Context,
    private val settingsConfig: SettingsConfig,
    private val cacheApiClient: CacheApiClient
) {

    private val cache: Cache
    private var cacheDataSourceFactory: CacheDataSource.Factory
    private var cacheDataSource: CacheDataSource

    private val cacheTask: ConcurrentHashMap<String, CacheWriter> = ConcurrentHashMap()
    private val cacheCoroutineScope = CoroutineScopeUtils.getIo(this.javaClass.name)

    private val cacheSchedule = MutableStateFlow(0f)
    val _cacheSchedule = cacheSchedule.asStateFlow()

    init {
        //2025年1月20日 11:12:19 修改缓存数据目录到cache中,使其可以被系统的清除缓存功能删除
        val cacheParentDirectory =
            if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
                File(
//                    context.externalCacheDir,
                    context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                    context.packageName
                )
            } else {
                File(context.filesDir, context.packageName)
            }
        Log.i("catch", "缓存初始化 $cacheParentDirectory")
        // 设置缓存目录和缓存机制，如果不需要清除缓存可以使用NoOpCacheEvictor
        cache = SimpleCache(
            File(cacheParentDirectory, "example_media_cache"),
            //读取配置
            XyCacheEvictor(settingsConfig), ExampleDatabaseProvider(context)
//            NoOpCacheEvictor(), ExampleDatabaseProvider(context)
        )

        // 根据缓存目录创建缓存数据源
        cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(
                DefaultDataSource.Factory(
                    context,
                    OkHttpDataSource.Factory(cacheApiClient.okhttpClientFunction())
                )
            )
        // 设置上游数据源，缓存未命中时通过此获取数据
        /*.setUpstreamDataSourceFactory(
            DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(true)
        )*/
        cacheDataSource = cacheDataSourceFactory.createDataSource()
    }

    fun cacheMediaList(musicList: List<XyMusic>) {
        cacheCoroutineScope.launch {
            Log.i("=====", "创建缓存")
            withContext(Dispatchers.IO) {
                val jobs = musicList.map { music ->
                    async {
                        cacheSchedule.value = 0f
                        val dataSource = cacheDataSourceFactory.createDataSource()
                        try {
                            // 创建CacheWriter缓存数据
                            CacheWriter(
                                dataSource,
                                DataSpec.Builder()
                                    // 设置资源链接
                                    .setUri(music.musicUrl)
                                    // 设置需要缓存的大小（可以只缓存一部分）
//                                    .setLength(200000)
                                    .setLength(((music.size ?: 20000) * 0.1).toLong())
                                    .build(),
                                null
                            ) { requestLength, bytesCached, newBytesCached ->
                                // 缓冲进度变化时回调
                                Log.i(
                                    "=====",
                                    "缓冲数据变化 请求总大小${requestLength}, 已缓冲的字节数${bytesCached} 新缓冲的字节数${newBytesCached}"
                                )
                                cacheSchedule.value = bytesCached * 1.0f / requestLength
//                                 Log.i("cache","")
                                // requestLength 请求总大小
                                // bytesCached 已缓冲的字节数
                                // newBytesCached 新缓冲的字节数
                            }.let { cacheWriter ->
                                cacheWriter.cache()
                                cacheTask[music.musicUrl] = cacheWriter

                            }
                        } catch (e: Exception) {
                            Log.e("=====", "缓存数据报错: ${e.message} --- ${music.name}", e)
                        } finally {
                            DataSourceUtil.closeQuietly(dataSource)
                        }
                    }

                }

                // 等待所有缓存任务完成
//                jobs.awaitAll()
                Log.i("Cache", "所有缓存任务完成")
            }

        }
    }

    fun cacheMedia(music: XyMusic) {
        cacheCoroutineScope.launch {
            Log.i("=====", "创建缓存")
            withContext(Dispatchers.IO) {
                val job = async {
                    cacheSchedule.value = 0f
                    val dataSource = cacheDataSourceFactory.createDataSource()
                    try {
                        // 创建CacheWriter缓存数据
                        CacheWriter(
                            dataSource,
                            DataSpec.Builder()
                                // 设置资源链接
                                .setUri(music.musicUrl)
                                // 设置需要缓存的大小（可以只缓存一部分）
//                                    .setLength(200000)
                                .setLength(((music.size ?: 20000)))
                                .build(),
                            null
                        ) { requestLength, bytesCached, newBytesCached ->
                            // 缓冲进度变化时回调
                           /* Log.i(
                                "=====",
                                "缓冲数据变化 请求总大小${requestLength}, 已缓冲的字节数${bytesCached} 新缓冲的字节数${newBytesCached}, " +
                                        "当前进度: ${bytesCached * 1.0f / requestLength}"
                            )*/
                            cacheSchedule.value = bytesCached * 1.0f / requestLength
//                                 Log.i("cache","")
                            // requestLength 请求总大小
                            // bytesCached 已缓冲的字节数
                            // newBytesCached 新缓冲的字节数
                        }.let { cacheWriter ->
                            cacheTask[music.musicUrl] = cacheWriter
                            cacheWriter.cache()
                        }
                    } catch (e: Exception) {
                        Log.e("=====", "缓存数据报错: ${e.message} --- ${music.name}", e)
                    } finally {
                        DataSourceUtil.closeQuietly(dataSource)
                    }
                }

                // 等待所有缓存任务完成
//                job.await()
                Log.i("Cache", "${music.name} 缓存任务完成")
            }

        }
    }

    /**
     * 取消指定地址缓存
     */
    fun cancelCache(mediaUrl: String) {
        // 取消缓存
        cacheTask[mediaUrl]?.cancel()
    }

    /**
     * 取消所有缓存
     */
    fun cancelAllCache() {
        cacheTask.forEach { (_, u) ->
            u.cancel()
        }
        cacheTask.clear()
    }

    /**
     * 判断是否已经存在缓存
     */
    fun ifCache(mediaUrl: String, length: Long): Boolean {
        return cacheTask.containsKey(mediaUrl) && cacheDataSource.cache.isCached(
            mediaUrl,
            0,
            length
        )
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
        cacheDataSource.cache.keys.forEach {
            cacheDataSource.cache.removeResource(it)
        }
    }

    fun release() {
        cacheTask.values.forEach { it.cancel() }
        cache.release()
    }
}