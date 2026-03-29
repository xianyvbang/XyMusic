package cn.xybbz.config.music

import cn.xybbz.config.scope.IoScoped
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.localdata.data.music.XyPlayMusic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

abstract class DownloadCacheCommonController : IoScoped(), KoinComponent {


    val settingsManager = get<SettingsManager>()

    /**
     * 所有缓存大小
     */
    private val _allCacheSizeFlow = MutableStateFlow(0L)
    val allCacheSizeFlow = _allCacheSizeFlow.asStateFlow()
    /**
     * 当前缓存进度
     */
    private val _cacheSchedule = MutableStateFlow(0f)
    val cacheSchedule = _cacheSchedule.asStateFlow()

    fun updateCacheSchedule(schedule: Float) {
        _cacheSchedule.value = schedule
    }

    /**
     * 清空缓存
     */
    abstract fun clearCache()

    /**
     * 获得所有缓存大小
     */
    abstract fun getCacheSize()

    /**
     * 缓存音乐
     */
    abstract fun cacheMedia(
        music: XyPlayMusic,
        ifStatic: Boolean
    )

    /**
     * 取消所有缓存
     */
    abstract fun cancelAllCache()

    /**
     * 根据缓存key获得mediaItem
     */
    abstract fun getMediaItem(cacheKey: String): Any?

    /**
     * 获得缓存key
     */
    fun getCacheKey(musicId: String): String {
        return musicId + settingsManager.getStatic() + settingsManager.get().transcodeFormat + settingsManager.getAudioBitRate()
    }

    /**
     * 更新当前缓存大小
     */
    fun updateCacheSizeFlow(size: Long){
        _allCacheSizeFlow.value = size
    }
}