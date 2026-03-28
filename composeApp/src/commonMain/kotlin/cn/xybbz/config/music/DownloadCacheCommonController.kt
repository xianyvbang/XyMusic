package cn.xybbz.config.music

import cn.xybbz.config.scope.IoScoped
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class DownloadCacheCommonController : IoScoped() {


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
}