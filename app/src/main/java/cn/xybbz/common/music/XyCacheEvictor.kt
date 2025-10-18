package cn.xybbz.common.music

import android.os.Environment
import android.os.StatFs
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheEvictor
import androidx.media3.datasource.cache.CacheSpan
import cn.xybbz.config.SettingsConfig
import cn.xybbz.localdata.enums.CacheUpperLimitEnum
import java.util.TreeSet


@UnstableApi
class XyCacheEvictor(private val settingsConfig: SettingsConfig) : CacheEvictor {


    private var leastRecentlyUsed: TreeSet<CacheSpan>? = null

    private var currentSize: Long = 0

    init {
        this.leastRecentlyUsed =
            TreeSet<CacheSpan>(XyCacheEvictor::compare)
        onChangeMaxSize(settingsConfig.get().cacheUpperLimit)
        settingsConfig.setOnCacheUpperLimitListener {
            onChangeMaxSize(it)
        }
    }

    override fun requiresCacheSpanTouches(): Boolean {
        return true
    }

    override fun onCacheInitialized() {
        // Do nothing.
    }

    override fun onStartFile(cache: Cache, key: String, position: Long, length: Long) {
        if (length != C.LENGTH_UNSET.toLong()) {
            evictCache(cache, length)
        }
    }

    override fun onSpanAdded(cache: Cache, span: CacheSpan) {
        leastRecentlyUsed!!.add(span)
        currentSize += span.length
        evictCache(cache, 0)
    }

    override fun onSpanRemoved(cache: Cache, span: CacheSpan) {
        leastRecentlyUsed!!.remove(span)
        currentSize -= span.length
    }

    override fun onSpanTouched(cache: Cache, oldSpan: CacheSpan, newSpan: CacheSpan) {
        onSpanRemoved(cache, oldSpan)
        onSpanAdded(cache, newSpan)
    }

    private fun evictCache(cache: Cache, requiredSpace: Long) {
        while (currentSize + requiredSpace > settingsConfig.maxBytes && !leastRecentlyUsed!!.isEmpty()) {
            cache.removeSpan(leastRecentlyUsed!!.first())
        }
    }

    private fun onChangeMaxSize(cacheUpperLimit: CacheUpperLimitEnum) {
        if (cacheUpperLimit == CacheUpperLimitEnum.Auto) {
            val statFs = StatFs(Environment.getDataDirectory().path)
            val size = statFs.blockSizeLong //每格所占的大小，一般是4KB==

            val availableCounts = statFs.availableBlocksLong //获取可用的block数
            val availROMSize = (availableCounts * size).toDouble() //可用内部存储大小

            val gigabytes = availROMSize / (1024.0 * 1024.0 * 1024.0)

            settingsConfig.maxBytes = when {
                gigabytes > 100 -> 16L * 1024 * 1024 * 1024
                gigabytes > 50 && gigabytes <= 100 -> 8L * 1024 * 1024 * 1024
                gigabytes > 10 -> 4L * 1024 * 1024 * 1024
                else -> 2L * 1024 * 1024 * 1024
            }

        } else {
            settingsConfig.maxBytes = cacheUpperLimit.value * 1024 * 1024L
        }
    }

    companion object {
        private fun compare(lhs: CacheSpan, rhs: CacheSpan): Int {
            val lastTouchTimestampDelta = lhs.lastTouchTimestamp - rhs.lastTouchTimestamp
            if (lastTouchTimestampDelta == 0L) {
                // Use the standard compareTo method as a tie-break.
                return lhs.compareTo(rhs)
            }
            return if (lhs.lastTouchTimestamp < rhs.lastTouchTimestamp) -1 else 1
        }


    }

}