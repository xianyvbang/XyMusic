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

import android.os.Environment
import android.os.StatFs
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheEvictor
import androidx.media3.datasource.cache.CacheSpan
import cn.xybbz.config.setting.OnSettingsChangeListener
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.localdata.enums.CacheUpperLimitEnum
import java.util.TreeSet


@UnstableApi
class XyCacheEvictor(private val settingsManager: SettingsManager) : CacheEvictor {


    private var leastRecentlyUsed: TreeSet<CacheSpan> =
        TreeSet<CacheSpan>(Comparator { lhs: CacheSpan, rhs: CacheSpan ->
            compare(
                lhs,
                rhs
            )
        })

    private var currentSize: Long = 0

    init {
        this.leastRecentlyUsed =
            TreeSet<CacheSpan>(XyCacheEvictor::compare)
        onChangeMaxSize(settingsManager.get().cacheUpperLimit)
        settingsManager.setOnListener(object : OnSettingsChangeListener{
            override fun onCacheMaxBytesChanged(
                cacheUpperLimit: CacheUpperLimitEnum,
                oldCacheUpperLimit: CacheUpperLimitEnum
            ) {
                onChangeMaxSize(cacheUpperLimit)
            }

        })
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
        leastRecentlyUsed.add(span)
        currentSize += span.length
        evictCache(cache, 0)
    }

    override fun onSpanRemoved(cache: Cache, span: CacheSpan) {
        leastRecentlyUsed.remove(span)
        currentSize -= span.length
    }

    override fun onSpanTouched(cache: Cache, oldSpan: CacheSpan, newSpan: CacheSpan) {
        onSpanRemoved(cache, oldSpan)
        onSpanAdded(cache, newSpan)
    }

    private fun evictCache(cache: Cache, requiredSpace: Long) {
        while (currentSize + requiredSpace > settingsManager.maxBytes && !leastRecentlyUsed.isEmpty()) {
            cache.removeSpan(leastRecentlyUsed.first())
        }
    }

    private fun onChangeMaxSize(cacheUpperLimit: CacheUpperLimitEnum) {
        if (cacheUpperLimit == CacheUpperLimitEnum.Auto) {
            val statFs = StatFs(Environment.getDataDirectory().path)
            val size = statFs.blockSizeLong //每格所占的大小，一般是4KB==

            val availableCounts = statFs.availableBlocksLong //获取可用的block数
            val availROMSize = (availableCounts * size).toDouble() //可用内部存储大小

            val gigabytes = availROMSize / (1024.0 * 1024.0 * 1024.0)

            settingsManager.maxBytes = when {
                gigabytes > 100 -> 16L * 1024 * 1024 * 1024
                gigabytes > 50 && gigabytes <= 100 -> 8L * 1024 * 1024 * 1024
                gigabytes > 10 -> 4L * 1024 * 1024 * 1024
                else -> 2L * 1024 * 1024 * 1024
            }

        } else {
            settingsManager.maxBytes = cacheUpperLimit.value * 1024 * 1024L
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