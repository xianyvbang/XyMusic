package cn.xybbz.config.music

import cn.xybbz.config.scope.IoScoped
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class DownloadCacheCommonController : IoScoped() {

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
}