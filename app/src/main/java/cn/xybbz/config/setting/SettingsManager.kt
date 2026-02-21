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

package cn.xybbz.config.setting

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cn.xybbz.common.enums.AllDataEnum
import cn.xybbz.common.music.AudioFadeController
import cn.xybbz.config.network.NetWorkMonitor
import cn.xybbz.config.network.OnNetworkChangeListener
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.setting.XySettings
import cn.xybbz.localdata.enums.CacheUpperLimitEnum
import cn.xybbz.localdata.enums.DataSourceType
import cn.xybbz.localdata.enums.LanguageType
import cn.xybbz.localdata.enums.ThemeTypeEnum
import com.hjq.language.MultiLanguages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import java.util.Locale

class SettingsManager(
    private val db: DatabaseClient,
    private val applicationContext: Context,
    private val audioFadeController: AudioFadeController,
    private val netWorkMonitor: NetWorkMonitor
) {

    private var settings: XySettings? = null

    var languageType by mutableStateOf<LanguageType?>(null)
        private set

    //监听
    val onSettingsChangeListeners = mutableListOf<OnSettingsChangeListener>()

    //缓存设置
    var cacheUpperLimit by mutableStateOf(CacheUpperLimitEnum.Auto)
        private set

    //缓存文件所在位置
    var cacheFilePath by mutableStateOf("")
        private set

    //是否为非计费网络
    var isUnmeteredWifi: Boolean = false

    lateinit var packageInfo: PackageInfo
        private set

    //是否有连接配置
    var ifConnectionConfig by mutableStateOf(false)
        private set

    //是否显示SnackBar
    var ifShowSnackBar by mutableStateOf(false)
        private set

    /**
     * 主题类型
     */
    var themeType by mutableStateOf(ThemeTypeEnum.SYSTEM)
        private set

    /**
     * 是否动态颜色
     */
    var isDynamic by mutableStateOf(false)
        private set


    //是否设置转码音质
    private val _transcodingFlow = MutableSharedFlow<TranscodingState>(0, extraBufferCapacity = 1)
    val transcodingFlow = _transcodingFlow.asSharedFlow()

    /**
     * 音乐缓存上限
     * todo 这里赋值应该改为使用方法设置
     */
    var maxBytes by mutableLongStateOf(0L)

    fun get(): XySettings {
        return settings ?: XySettings()
    }

    suspend fun setSettingsData(): XySettings = withContext(Dispatchers.IO) {
        Log.i("=====", "开始存储设置")
        this@SettingsManager.settings = db.settingsDao.selectOneData() ?: XySettings()

        this@SettingsManager.themeType = this@SettingsManager.get().themeType
        this@SettingsManager.isDynamic = this@SettingsManager.get().isDynamic

        updateIfConnectionConfig(this@SettingsManager.get().connectionId != null)
        if (this@SettingsManager.get().languageType != null) {
            this@SettingsManager.languageType = this@SettingsManager.get().languageType
        } else {
            setDefaultLanguage(applicationContext)
        }
        this@SettingsManager.cacheUpperLimit = this@SettingsManager.get().cacheUpperLimit
        Log.i("api", "动态设置数据--读取配置")
        audioFadeController.updateFadeDurationMs(this@SettingsManager.get().fadeDurationMs)
        val packageManager = applicationContext.packageManager
        val packageName = applicationContext.packageName
        packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            packageManager.getPackageInfo(packageName, 0)
        }
        netWorkMonitor.addListener(object : OnNetworkChangeListener {
            override fun onNetworkChange(isUnmeteredWifi: Boolean) {
                this@SettingsManager.isUnmeteredWifi = isUnmeteredWifi
                sengTranscodingEvent(TranscodingState.NetWorkChange)
            }
        })
        netWorkMonitor.start()
        get()
    }

    /**
     * 设置是否开启边下边播
     */
    suspend fun setIfEnableEdgeDownload(ifEnableEdgeDownload: Boolean) {
        settings = get().copy(ifEnableEdgeDownload = ifEnableEdgeDownload)
        if (get().id != AllDataEnum.All.code) {
            db.settingsDao.update(
                get()
            )
        } else {
            val settingId =
                db.settingsDao.save(XySettings(ifEnableEdgeDownload = ifEnableEdgeDownload))
            settings = get().copy(id = settingId)
        }
    }

    /**
     * 设置缓存上限
     */
    suspend fun setCacheUpperLimit(cacheUpperLimit: CacheUpperLimitEnum) {
        val oldCacheUpperLimit = this.cacheUpperLimit
        this.cacheUpperLimit = cacheUpperLimit
        settings = get().copy(cacheUpperLimit = cacheUpperLimit)
        if (get().id != AllDataEnum.All.code) {
            db.settingsDao.updateCacheUpperLimit(cacheUpperLimit, get().id)
        } else {
            val settingId =
                db.settingsDao.save(XySettings(cacheUpperLimit = cacheUpperLimit))
            settings = get().copy(id = settingId)
        }
        for (listener in onSettingsChangeListeners.toList()) {
            listener.onCacheMaxBytesChanged(
                cacheUpperLimit,
                oldCacheUpperLimit
            )
        }
    }


    /**
     * 设置桌面歌词
     */
    suspend fun setIfDesktopLyrics(ifDesktopLyrics: Int) {

        settings = get().copy(ifDesktopLyrics = ifDesktopLyrics)
        if (get().id != AllDataEnum.All.code) {
            db.settingsDao.updateIfDesktopLyrics(ifDesktopLyrics, get().id)
        } else {
            val settingId =
                db.settingsDao.save(XySettings(ifDesktopLyrics = ifDesktopLyrics))
            settings = get().copy(id = settingId)
        }
    }

    /**
     * 新增或更新播放速度
     */
    suspend fun saveOrUpdateDoubleSpeed(doubleSpeed: Float) {
        settings = get().copy(doubleSpeed = doubleSpeed)
        if (get().id != AllDataEnum.All.code) {
            db.settingsDao.updateDoubleSpeed(
                doubleSpeed = doubleSpeed,
                id = get().id
            )
        } else {
            val settingId =
                db.settingsDao.save(XySettings(doubleSpeed = doubleSpeed))
            settings = get().copy(id = settingId)
        }
    }

    /**
     * 存储数据源类型
     */
    suspend fun saveConnectionId(connectionId: Long?, dataSourceType: DataSourceType?) {
        settings = get().copy(connectionId = connectionId, dataSourceType = dataSourceType)
        if (get().id != AllDataEnum.All.code) {
            db.settingsDao.updateConnectionId(connectionId, dataSourceType, get().id)
        } else {
            val settingId =
                db.settingsDao.save(
                    XySettings(
                        connectionId = connectionId,
                        dataSourceType = dataSourceType
                    )
                )
            settings = get().copy(id = settingId)
        }
        updateIfConnectionConfig(connectionId != null)
    }


    /**
     * 设置是否开启所有专辑的播放历史记录
     */
    suspend fun setIfEnableAlbumHistory(ifEnableAlbumHistory: Boolean) {
        settings = get().copy(ifEnableAlbumHistory = ifEnableAlbumHistory)
        if (get().id != AllDataEnum.All.code) {
            db.settingsDao.updateIfEnableAlbumHistory(
                ifEnableAlbumHistory = ifEnableAlbumHistory,
                get().id
            )

        } else {
            val settingId =
                db.settingsDao.save(XySettings(ifEnableAlbumHistory = ifEnableAlbumHistory))
            settings = get().copy(id = settingId)
        }
    }

    /**
     * 设置是否开启所有专辑的播放历史记录
     */
    suspend fun setIfHandleAudioFocus(ifHandleAudioFocus: Boolean) {
        settings = get().copy(ifHandleAudioFocus = ifHandleAudioFocus)
        if (get().id != AllDataEnum.All.code) {
            db.settingsDao.updateIfHandleAudioFocus(
                ifHandleAudioFocus = ifHandleAudioFocus,
                get().id
            )

        } else {
            val settingId =
                db.settingsDao.save(XySettings(ifHandleAudioFocus = ifHandleAudioFocus))
            settings = get().copy(id = settingId)
        }

        for (listener in onSettingsChangeListeners.toList()) {
            listener.onHandleAudioFocusChanged(
                ifHandleAudioFocus
            )
        }
    }

    /**
     * 设置是否开启所播放进度同步
     */
    suspend fun setIfEnableSyncPlayProgress(ifEnableSyncPlayProgress: Boolean) {
        settings = get().copy(ifEnableSyncPlayProgress = ifEnableSyncPlayProgress)
        if (get().id != AllDataEnum.All.code) {
            db.settingsDao.updateIfEnableSyncPlayProgress(
                ifEnableSyncPlayProgress = ifEnableSyncPlayProgress,
                get().id
            )

        } else {
            val settingId =
                db.settingsDao.save(XySettings(ifEnableSyncPlayProgress = ifEnableSyncPlayProgress))
            settings = get().copy(id = settingId)
        }
    }

    /**
     * 更新语言设置
     */
    suspend fun setLanguageTypeData(languageType: LanguageType, context: Context) {
        settings = get().copy(languageType = languageType)
        if (get().id != AllDataEnum.All.code) {
            db.settingsDao.updateLanguageType(
                languageType = languageType,
                get().id
            )

        } else {
            val settingId =
                db.settingsDao.save(XySettings(languageType = languageType))
            settings = get().copy(id = settingId)
        }
        updateLanguage(languageType, context)
    }


    /**
     * 全局更新语言设置
     */
    fun updateLanguage(languageType: LanguageType, context: Context) {
        val locale = Locale.forLanguageTag(languageType.languageCode)
        MultiLanguages.setAppLanguage(context, locale)
        this.languageType = languageType
    }

    /**
     * 设置当前默认语言
     */
    fun setDefaultLanguage(context: Context) {
        val systemLanguage = MultiLanguages.getSystemLanguage(context)
        this.languageType = LanguageType.getThis(systemLanguage.toLanguageTag())
    }

    /**
     * 设置缓存大小监听方法
     */
    fun setOnListener(onSettingsChangeListener: OnSettingsChangeListener) {
        this.onSettingsChangeListeners.add(onSettingsChangeListener)
    }

    /**
     * 设置最新版本号
     */
    suspend fun setLatestVersion(version: String) {
        settings = get().copy(latestVersion = version)
        if (get().id != AllDataEnum.All.code) {
            db.settingsDao.updateLatestVersion(
                latestVersion = version,
                get().id
            )

        } else {
            val settingId =
                db.settingsDao.save(XySettings(latestVersion = version))
            settings = get().copy(id = settingId)
        }
    }

    /**
     * 更新最新版本的下载地址
     */
    suspend fun setLastApkUrl(apkUrl: String) {
        settings = get().copy(lasestApkUrl = apkUrl)
        if (get().id != AllDataEnum.All.code) {
            db.settingsDao.updateLatestApkUrl(
                lasestApkUrl = apkUrl,
                get().id
            )

        } else {
            val settingId =
                db.settingsDao.save(XySettings(lasestApkUrl = apkUrl))
            settings = get().copy(id = settingId)
        }
    }

    /**
     * 更新最新版本的获取时间
     */
    suspend fun setLatestVersionTime(latestVersionTime: Long) {
        settings = get().copy(latestVersionTime = latestVersionTime)
        if (get().id != AllDataEnum.All.code) {
            db.settingsDao.updateLatestVersionTime(
                latestVersionTime = latestVersionTime,
                get().id
            )

        } else {
            val settingId =
                db.settingsDao.save(XySettings(latestVersionTime = latestVersionTime))
            settings = get().copy(id = settingId)
        }
    }

    /**
     * 更新最大同时下载数量
     */
    suspend fun setMaxConcurrentDownloads(maxConcurrentDownloads: Int) {
        settings = get().copy(maxConcurrentDownloads = maxConcurrentDownloads)
        if (get().id != AllDataEnum.All.code) {
            db.settingsDao.updateMaxConcurrentDownloads(
                maxConcurrentDownloads = maxConcurrentDownloads,
                get().id
            )

        } else {
            val settingId =
                db.settingsDao.save(XySettings(maxConcurrentDownloads = maxConcurrentDownloads))
            settings = get().copy(id = settingId)
        }
    }

    /**
     * 更新渐入渐出持续时间
     */
    suspend fun setFadeDurationMs(fadeDurationMs: Long) {
        audioFadeController.updateFadeDurationMs(fadeDurationMs)
        settings = get().copy(fadeDurationMs = fadeDurationMs)
        if (get().id != AllDataEnum.All.code) {
            db.settingsDao.updateFadeDurationMs(
                fadeDurationMs = fadeDurationMs,
                get().id
            )

        } else {
            val settingId =
                db.settingsDao.save(XySettings(fadeDurationMs = fadeDurationMs))
            settings = get().copy(id = settingId)
        }
    }

    /**
     * 更新移动网络转码比特率
     */
    suspend fun setMobileNetworkAudioBitRate(mobileNetworkAudioBitRate: Int) {
        settings = get().copy(mobileNetworkAudioBitRate = mobileNetworkAudioBitRate)
        if (get().id != AllDataEnum.All.code) {
            db.settingsDao.update(
                get()
            )
        } else {
            val settingId =
                db.settingsDao.save(XySettings(mobileNetworkAudioBitRate = mobileNetworkAudioBitRate))
            settings = get().copy(id = settingId)
        }
        if (!get().ifTranscoding)
            sengTranscodingEvent()
    }

    /**
     * 更新wifi网络转码比特率
     */
    suspend fun setWifiNetworkAudioBitRate(wifiNetworkAudioBitRate: Int) {
        settings = get().copy(wifiNetworkAudioBitRate = wifiNetworkAudioBitRate)
        if (get().id != AllDataEnum.All.code) {
            db.settingsDao.update(
                get()
            )
        } else {
            val settingId =
                db.settingsDao.save(XySettings(wifiNetworkAudioBitRate = wifiNetworkAudioBitRate))
            settings = get().copy(id = settingId)
        }
        if (!get().ifTranscoding)
            sengTranscodingEvent()
    }

    /**
     * 更新任意网络是否转码
     */
    suspend fun setIfTranscoding(ifTranscoding: Boolean) {
        settings = get().copy(ifTranscoding = ifTranscoding)
        if (get().id != AllDataEnum.All.code) {
            db.settingsDao.update(
                get()
            )
        } else {
            val settingId =
                db.settingsDao.save(XySettings(ifTranscoding = ifTranscoding))
            settings = get().copy(id = settingId)
        }

        sengTranscodingEvent()
    }

    /**
     * 更新编码格式
     */
    suspend fun setTranscodeFormat(transcodeFormat: String) {
        settings = get().copy(transcodeFormat = transcodeFormat)
        if (get().id != AllDataEnum.All.code) {
            db.settingsDao.update(
                get()
            )
        } else {
            val settingId =
                db.settingsDao.save(XySettings(transcodeFormat = transcodeFormat))
            settings = get().copy(id = settingId)
        }

        if (!get().ifTranscoding)
            sengTranscodingEvent()
    }


    /**
     * 更新主题颜色类型
     */
    suspend fun setThemeTypeData(themeType: ThemeTypeEnum) {
        this.themeType = themeType
        settings = get().copy(themeType = themeType)
        if (get().id != AllDataEnum.All.code) {
            db.settingsDao.update(
                get()
            )

        } else {
            val settingId =
                db.settingsDao.save(XySettings(themeType = themeType))
            settings = get().copy(id = settingId)
        }
    }

    /**
     * 更新缓存数据目录地址
     */
    fun updateCacheFilePath(path: String) {
        this.cacheFilePath = path
    }

    fun sengTranscodingEvent(transcodingState:TranscodingState = TranscodingState.Transcoding) {
        _transcodingFlow.tryEmit(transcodingState)
    }

    /**
     * 根据网络类型获得需要转码的比特率
     */
    fun getAudioBitRate(): Int {
        return if (!isUnmeteredWifi)
            get().mobileNetworkAudioBitRate
        else get().wifiNetworkAudioBitRate
    }

    /**
     * 获得是否静态资源不转码 true:不转码,false 转码
     */
    fun getStatic(): Boolean {
        val audioBitRate = getAudioBitRate()
        return if (get().ifTranscoding) true else if (audioBitRate == 0) true else false
    }

    /**
     * 更新是否存在连接设置
     */
    fun updateIfConnectionConfig(ifConnectionConfig: Boolean) {
        this.ifConnectionConfig = ifConnectionConfig
        updateIfShowSnackBar(ifConnectionConfig)
    }

    /**
     * 更新是否显示底部ShowSnackBar
     */
    fun updateIfShowSnackBar(ifShowSnackBar: Boolean) {
        this.ifShowSnackBar = ifShowSnackBar
    }

}