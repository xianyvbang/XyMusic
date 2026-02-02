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

package cn.xybbz.viewmodel

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.icu.math.BigDecimal
import android.os.Environment
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import androidx.room.withTransaction
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.music.DownloadCacheController
import cn.xybbz.common.music.MusicController
import cn.xybbz.config.BackgroundConfig
import cn.xybbz.config.download.DownLoadManager
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.localdata.config.DatabaseClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@OptIn(UnstableApi::class)
@HiltViewModel
class MemoryManagementViewModel @Inject constructor(
    private val downloadCacheController: DownloadCacheController,
    private val db: DatabaseClient,
    private val settingsManager: SettingsManager,
    private val dataSourceManager: DataSourceManager,
    private val musicController: MusicController,
    val backgroundConfig: BackgroundConfig,
    private val downLoadManager: DownLoadManager
) : ViewModel() {


    var cacheSize by mutableStateOf("0B")
        private set

    var appDataSize by mutableStateOf("0B")
        private set

    var musicCacheSize by mutableStateOf("0B")
        private set

    var databaseSize by mutableStateOf("0B")
        private set

    init {
        viewModelScope.launch {
            downloadCacheController.getCacheSize()
            downloadCacheController.allCacheSizeFlow.collect {
                musicCacheSize = getFormatSize(it)
            }
        }
    }

    fun logStorageInfo(context: Context) {
        appDataSize = getFormatSize(getAppDataSize(context))
        cacheSize = getTotalCacheSize(context)
        databaseSize = getAppDatabaseSize()
    }


    /**
     * 获取整体缓存大小
     * @param context
     * @return
     * @throws Exception
     */
    //todo 获取和清除缓存可以通过settingConfig.cacheFilePath进行操作
    @Throws(java.lang.Exception::class)
    fun getTotalCacheSize(context: Context): String {
        var cacheSize = getFolderSize(context.cacheDir)
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            val externalCacheDir = context.externalCacheDir
            if (externalCacheDir != null)
                cacheSize += getFolderSize(externalCacheDir)
        }
        return getFormatSize(cacheSize)
    }

    /**
     * 获取应用程序数据大小
     * @param [context] android上下文
     * @return [Long]
     */
    private fun getAppDataSize(context: Context): Long {
        try {
            val packageManager = context.packageManager
            val packageName = context.packageName
            val packageInfo: PackageInfo = packageManager.getPackageInfo(packageName, 0)
            val applicationInfo = packageInfo.applicationInfo
            if (applicationInfo != null) {
                val dataDir = File(applicationInfo.dataDir)
                return getFolderOrNULLSize(dataDir, context)
            }

        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return 0
    }

    /**
     * 获取应用程序数据库大小
     */
    private fun getAppDatabaseSize(): String {
        val databasePath = db.openHelper.writableDatabase.path
        if (!databasePath.isNullOrBlank()) {
            return getFormatSize(File(databasePath).length())
        }
        return "0B"
    }

    /**
     * 获取文件夹大小
     * @param [folder] 文件夹
     * @param [context] android上下文
     * @return [Long]
     */
    private fun getFolderOrNULLSize(folder: File?, context: Context): Long {
        val databasePath = db.openHelper.writableDatabase.path
        var size: Long = 0
        if (folder != null && folder.isDirectory) {
            val files = folder.listFiles()
            if (!files.isNullOrEmpty()) {
                for (file in files) {
                    size += if (file.isDirectory) {
                        if (file.path.startsWith(context.cacheDir.path) || (!databasePath.isNullOrBlank() && File(
                                databasePath
                            ).parent == file.path)
                        ) {
                            0
                        } else {
                            getFolderSize(file)

                        }
                    } else {
                        file.length()
                    }
                }
            }

        }
        return size
    }


    /**
     * 获取文件
     * Context.getExternalFilesDir() --> SDCard/Android/data/你的应用的包名/files/ 目录，一般放一些长时间保存的数据
     * Context.getExternalCacheDir() --> SDCard/Android/data/你的应用包名/cache/目录，一般存放临时缓存数据
     * @param file
     * @return
     * @throws Exception
     */
    @Throws(java.lang.Exception::class)
    fun getFolderSize(file: File): Long {
        var size: Long = 0
        try {
            val fileList = file.listFiles()
            if (fileList != null) {
                for (i in fileList.indices) {
                    // 如果下面还有文件
                    size = if (fileList[i].isDirectory) {
                        Log.d("Storage", "文件路径${file.path}")
                        size + getFolderSize(fileList[i])
                    } else {
                        size + fileList[i].length()
                    }
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return size
    }

    /**
     * 格式化单位
     * @param size
     */
    private fun getFormatSize(size: Long): String {
        if (size < 1024) {
            return "${size}B"
        }
        val kb = BigDecimal(size).divide(BigDecimal(1024), 1, BigDecimal.ROUND_HALF_UP).toFloat()
        if (kb < 1024) {
            return "${kb}KB"
        }
        val m = BigDecimal(size).divide(BigDecimal(1024))
            .divide(BigDecimal(1024), 2, BigDecimal.ROUND_HALF_UP).toFloat()
        if (m < 1024) {
            return "${m}MB"
        }

        val g = BigDecimal(size).divide(BigDecimal(1024)).divide(BigDecimal(1024))
            .divide(BigDecimal(1024), 2, BigDecimal.ROUND_HALF_UP).toFloat()

        if (g < 1024) {
            return "${m}GB"
        }
        return "0B"
    }

    /**
     * 全部缓存清空方法
     * @param context
     */
    fun clearAllCache(context: Context) {
        deleteDir(context.cacheDir)
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            deleteDir(context.externalCacheDir)
        }
        cacheSize = getTotalCacheSize(context)
    }

    /**
     * 清除音乐缓存
     */
    fun clearMusicCache() {
        viewModelScope.launch {
            downloadCacheController.clearCache()
        }
    }

    /**
     * 清空数据库数据
     */
    fun clearDatabaseData() {
        viewModelScope.launch {
            musicController.clearPlayerList()
            dataSourceManager.release()
            db.withTransaction {
                //清空缓存
                db.clearAllTables()
            }
            databaseSize = "0B"
            settingsManager.setSettingsData()
        }
    }

    private fun deleteDir(dir: File?): Boolean {
        if (dir != null && dir.isDirectory) {
            val children = dir.list()
            if (children != null) {
                for (i in children.indices) {
                    val success = deleteDir(File(dir, children[i]))
                    if (!success) {
                        return false
                    }
                }
            }
        }
        return dir!!.delete()
    }
}