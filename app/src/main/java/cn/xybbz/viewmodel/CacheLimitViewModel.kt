package cn.xybbz.viewmodel

import android.app.usage.StorageStatsManager
import android.content.Context
import android.os.Build
import android.os.storage.StorageManager
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.xybbz.config.BackgroundConfig
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.localdata.enums.CacheUpperLimitEnum
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CacheLimitViewModel @Inject constructor(
    private val settingsManager: SettingsManager,
    private val _backgroundConfig: BackgroundConfig
) : ViewModel() {

    val backgroundConfig = _backgroundConfig
    /**
     * 缓存上限
     */
    var cacheUpperLimit by mutableStateOf(settingsManager.cacheUpperLimit)
        private set

    /**
     * 缓存大小
     */
    var cacheSizeInfo by mutableStateOf("")
        private set


    fun setCacheUpperLimitData(data: CacheUpperLimitEnum) {
        cacheUpperLimit = data
        viewModelScope.launch {
            settingsManager.setCacheUpperLimit(
                cacheUpperLimit
            )
        }
    }

    fun getAutomaticCacheSize(context: Context) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                val storageStatsManager =
                    context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
                val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager

                val storageVolumes = storageManager.storageVolumes
                var maxBytes = 0L

                for (volume in storageVolumes) {
                    val uuid = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        // API 31+
                        volume.storageUuid ?: StorageManager.UUID_DEFAULT
                    } else {
                        // API 28~30
                        try {
                            volume.uuid?.let { UUID.fromString(it) } ?: StorageManager.UUID_DEFAULT
                        }catch (_: Exception){
                            StorageManager.UUID_DEFAULT
                        }
                    }
                    try {
                        val freeBytes = storageStatsManager.getFreeBytes(uuid) / 1024 / 1024 / 1024
                        maxBytes = when {
                            freeBytes > 100 -> 16L * 1024 * 1024 * 1024
                            freeBytes > 50 && freeBytes <= 100 -> 8L * 1024 * 1024 * 1024
                            freeBytes > 10 -> 4L * 1024 * 1024 * 1024
                            else -> 2L * 1024 * 1024 * 1024
                        }
                    } catch (e: IOException) {
                        Log.e("StorageInfo", "Error getting storage stats", e)
                    }
                }
                // 结果返回
                maxBytes
            }

            // 在主线程更新 State/MutableState
            cacheSizeInfo = formatSize(result) ?: ""
        }


    }

    fun formatSize(size: Long): String? {
        var suffix = "B"
        var value = size.toDouble()

        if (value > 1024) {
            suffix = "KB"
            value /= 1024.0
        }
        if (value > 1024) {
            suffix = "MB"
            value /= 1024.0
        }
        if (value > 1024) {
            suffix = "GB"
            value /= 1024.0
        }

        return String.format(Locale.getDefault(), "%.0f%s", value, suffix)
    }
}