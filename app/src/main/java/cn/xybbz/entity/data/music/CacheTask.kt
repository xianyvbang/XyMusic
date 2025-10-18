package cn.xybbz.entity.data.music

import android.annotation.SuppressLint
import androidx.media3.datasource.cache.CacheWriter

data class CacheTask(
    @param:SuppressLint("UnsafeOptInUsageError") var cacheWriter: CacheWriter,
    var isPaused: Boolean = false
)