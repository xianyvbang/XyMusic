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

import cn.xybbz.api.TokenServer
import cn.xybbz.common.enums.AllDataEnum
import cn.xybbz.common.utils.CoroutineScopeUtils
import cn.xybbz.common.utils.Log
import cn.xybbz.config.music.AudioFadeController
import cn.xybbz.config.network.NetWorkMonitor
import cn.xybbz.config.network.OnNetworkChangeListener
import cn.xybbz.localdata.config.LocalDatabaseClient
import cn.xybbz.localdata.data.setting.XySettings
import cn.xybbz.localdata.enums.CacheUpperLimitEnum
import cn.xybbz.localdata.enums.DataSourceType
import cn.xybbz.localdata.enums.LanguageType
import cn.xybbz.localdata.enums.ThemeTypeEnum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext

class SettingsManager(
    private val db: LocalDatabaseClient,
    private val audioFadeController: AudioFadeController,
    private val netWorkMonitor: NetWorkMonitor,
    private val languagePlatformManager: LanguagePlatformManager
) {
    private val scope = CoroutineScopeUtils.getIo("settings-manager")

    private val settingsSource =
        db.settingsDao.selectOneDataFlow()
            .map { it ?: XySettings() }
            .distinctUntilChanged()

    val settings: StateFlow<XySettings> =
        settingsSource.stateIn(scope, SharingStarted.Eagerly, XySettings())

    //i18n多语言
    val languageType = settings.map {
        it.languageType ?: languagePlatformManager.getSystemLanguageType()
    }.distinctUntilChanged()
        .stateIn(scope, SharingStarted.Eagerly, languagePlatformManager.getSystemLanguageType())

    //监听
    val onSettingsChangeListeners = mutableListOf<OnSettingsChangeListener>()

    //缓存设置
    val cacheUpperLimit = settings.map { it.cacheUpperLimit }.distinctUntilChanged()
        .stateIn(scope, SharingStarted.Eagerly, settings.value.cacheUpperLimit)

    //缓存文件所在位置
    val cacheFilePath = settings.map { it.cacheFilePath }.distinctUntilChanged()
        .stateIn(scope, SharingStarted.Eagerly, settings.value.cacheFilePath)

    //是否为非计费网络
    var isUnmeteredWifi: Boolean = false

    //是否有连接配置
    val ifConnectionConfig = settings.map {
        it.connectionId != null && it.dataSourceType != null
    }.distinctUntilChanged()
        .stateIn(scope, SharingStarted.Eagerly, false)

    //连接前缀
    val baseUrl = settings.map {
        it.connectionId?.let { db.connectionConfigDao.selectById(it).address }
    }.distinctUntilChanged()
        .stateIn(scope, SharingStarted.Eagerly, null)

    //是否显示SnackBar
    private val _ifShowSnackBar = MutableStateFlow(false)
    val ifShowSnackBar: StateFlow<Boolean> = _ifShowSnackBar.asStateFlow()

    /**
     * 主题类型
     */
    val themeType = settings.map { it.themeType }.distinctUntilChanged()
        .stateIn(scope, SharingStarted.Eagerly, settings.value.themeType)

    /**
     * 背景图片地址
     */
    val imageFilePath = settings.map { it.imageFilePath }.distinctUntilChanged()
        .stateIn(scope, SharingStarted.Eagerly, settings.value.imageFilePath)

    //音频编码
    val audioBitRate = combine(
        settings,
        netWorkMonitor.isUnmeteredWifi
    ) { settings, isUnmeteredWifi ->
        if (!isUnmeteredWifi) settings.mobileNetworkAudioBitRate
        else settings.wifiNetworkAudioBitRate
    }.stateIn(scope, SharingStarted.Eagerly, settings.value.mobileNetworkAudioBitRate)

    //是否设置转码音质
    private val _transcodingFlow = MutableSharedFlow<TranscodingState>(0, extraBufferCapacity = 1)
    val transcodingFlow = _transcodingFlow.asSharedFlow()

    /**
     * 音乐缓存上限
     * todo 这里赋值应该改为使用方法设置
     */
    private val _maxBytesFlow = MutableStateFlow(0L)

    // 音乐缓存上限的唯一响应式来源
    val maxBytesFlow = _maxBytesFlow.asStateFlow()


    /**
     * 更新当前缓存上限字节数。
     */
    fun updateMaxBytes(maxBytes: Long) {
        _maxBytesFlow.value = maxBytes
    }

    fun get(): XySettings {
        return settings.value
    }

    suspend fun getLatest(): XySettings {
        return withContext(Dispatchers.IO) {
            db.settingsDao.selectOneData() ?: XySettings()
        }
    }

    suspend fun initSet() {
        Log.i("=====", "开始存储设置")

        val currentSettings = getLatest()
        val connectionId = currentSettings.connectionId
        val ifConnectionId = connectionId != null
        updateIfConnectionConfig(ifConnectionId)
        if (connectionId != null) {
            TokenServer.updateBaseUrl(db.connectionConfigDao.selectById(connectionId).address)
        }

        Log.i("api", "动态设置数据--读取配置")
        audioFadeController.updateFadeDurationMs(currentSettings.fadeDurationMs)
        netWorkMonitor.addListener(object : OnNetworkChangeListener {
            override fun onNetworkChange(isUnmeteredWifi: Boolean) {
                this@SettingsManager.isUnmeteredWifi = isUnmeteredWifi
                sengTranscodingEvent(TranscodingState.NetWorkChange)
            }
        })
        netWorkMonitor.start()
        this@SettingsManager.isUnmeteredWifi = netWorkMonitor.isUnmeteredWifi.value
    }

    /**
     * 设置是否开启边下边播
     */
    suspend fun setIfEnableEdgeDownload(ifEnableEdgeDownload: Boolean) {
        updateSettings { it.copy(ifEnableEdgeDownload = ifEnableEdgeDownload) }
    }

    /**
     * 设置缓存上限
     */
    suspend fun setCacheUpperLimit(cacheUpperLimit: CacheUpperLimitEnum) {
        val oldCacheUpperLimit = settings.first().cacheUpperLimit
        updateSettings { it.copy(cacheUpperLimit = cacheUpperLimit) }
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
        updateSettings { it.copy(ifDesktopLyrics = ifDesktopLyrics) }
    }

    /**
     * 新增或更新播放速度
     */
    suspend fun saveOrUpdateDoubleSpeed(doubleSpeed: Float) {
        updateSettings { it.copy(doubleSpeed = doubleSpeed) }
    }

    /**
     * 存储数据源类型
     */
    suspend fun saveConnectionId(connectionId: Long?, dataSourceType: DataSourceType?) {
        updateSettings { it.copy(connectionId = connectionId, dataSourceType = dataSourceType) }
    }


    /**
     * 设置是否开启所有专辑的播放历史记录
     */
    suspend fun setIfEnableAlbumHistory(ifEnableAlbumHistory: Boolean) {
        updateSettings { it.copy(ifEnableAlbumHistory = ifEnableAlbumHistory) }
    }

    /**
     * 设置是否开启所有专辑的播放历史记录
     */
    suspend fun setIfHandleAudioFocus(ifHandleAudioFocus: Boolean) {
        updateSettings { it.copy(ifHandleAudioFocus = ifHandleAudioFocus) }

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
        updateSettings { it.copy(ifEnableSyncPlayProgress = ifEnableSyncPlayProgress) }
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
        updateSettings { it.copy(latestVersion = version) }
    }

    /**
     * 更新语言设置
     */
    suspend fun setLanguageTypeData(languageType: LanguageType) {
        updateSettings { it.copy(languageType = languageType) }
        updateLanguage(languageType)
    }


    /**
     * 全局更新语言设置
     */
    fun updateLanguage(languageType: LanguageType) {
        languagePlatformManager.applyLanguage(languageType)
    }

    /**
     * 更新最新版本的下载地址
     */
    suspend fun setLastApkUrl(apkUrl: String) {
        updateSettings { it.copy(lasestApkUrl = apkUrl) }
    }

    /**
     * 更新最新版本的获取时间
     */
    suspend fun setLatestVersionTime(latestVersionTime: Long) {
        updateSettings { it.copy(latestVersionTime = latestVersionTime) }
    }

    /**
     * 更新最大同时下载数量
     */
    suspend fun setMaxConcurrentDownloads(maxConcurrentDownloads: Int) {
        updateSettings { it.copy(maxConcurrentDownloads = maxConcurrentDownloads) }
    }

    /**
     * 更新渐入渐出持续时间
     */
    suspend fun setFadeDurationMs(fadeDurationMs: Long) {
        audioFadeController.updateFadeDurationMs(fadeDurationMs)
        updateSettings { it.copy(fadeDurationMs = fadeDurationMs) }
    }

    /**
     * 更新资源优先使用音乐服务接口（歌词/封面）
     */
    suspend fun setIfPriorityMusicApi(ifPriorityMusicApi: Boolean) {
        updateSettings { it.copy(ifPriorityMusicApi = ifPriorityMusicApi) }
        for (listener in onSettingsChangeListeners.toList()) {
            listener.onMusicResourceConfigChanged()
        }
    }

    /**
     * 更新自定义歌词单曲接口地址
     */
    suspend fun setCustomLrcSingleApi(customLrcSingleApi: String) {
        updateSettings { it.copy(customLrcSingleApi = customLrcSingleApi) }
    }

    /**
     * 更新自定义歌词接口鉴权
     */
    suspend fun setCustomLrcApiAuth(customLrcApiAuth: String) {
        updateSettings { it.copy(customLrcApiAuth = customLrcApiAuth) }
    }

    /**
     * 更新自定义封面接口地址
     */
    suspend fun setCustomCoverApi(customCoverApi: String) {
        updateSettings { it.copy(customCoverApi = customCoverApi) }
        for (listener in onSettingsChangeListeners.toList()) {
            listener.onMusicResourceConfigChanged()
        }
    }

    /**
     * 更新移动网络转码比特率
     */
    suspend fun setMobileNetworkAudioBitRate(mobileNetworkAudioBitRate: Int) {
        val next = updateSettings { it.copy(mobileNetworkAudioBitRate = mobileNetworkAudioBitRate) }
        if (!next.ifTranscoding)
            sengTranscodingEvent()
    }

    /**
     * 更新wifi网络转码比特率
     */
    suspend fun setWifiNetworkAudioBitRate(wifiNetworkAudioBitRate: Int) {
        val next = updateSettings { it.copy(wifiNetworkAudioBitRate = wifiNetworkAudioBitRate) }
        if (!next.ifTranscoding)
            sengTranscodingEvent()
    }

    /**
     * 更新任意网络是否转码
     */
    suspend fun setIfTranscoding(ifTranscoding: Boolean) {
        updateSettings { it.copy(ifTranscoding = ifTranscoding) }

        sengTranscodingEvent()
    }

    /**
     * 更新编码格式
     */
    suspend fun setTranscodeFormat(transcodeFormat: String) {
        val next = updateSettings { it.copy(transcodeFormat = transcodeFormat) }
        if (!next.ifTranscoding)
            sengTranscodingEvent()
    }


    /**
     * 更新主题颜色类型
     */
    suspend fun setThemeTypeData(themeType: ThemeTypeEnum) {
        updateSettings { it.copy(themeType = themeType) }
    }

    /**
     * 更新背景图片地址
     */
    suspend fun setImageFilePath(imageFilePath: String?) {
        updateSettings { it.copy(imageFilePath = imageFilePath) }
    }

    /**
     * 更新 JVM 播放器音量百分比
     */
    suspend fun setJvmVolume(jvmVolume: Int) {
        val value = jvmVolume.coerceIn(0, 100)
        updateSettings { it.copy(jvmVolume = value) }
    }

    //更新设置
    private suspend fun updateSettings(
        transform: (XySettings) -> XySettings
    ): XySettings = withContext(Dispatchers.IO) {
        val old = db.settingsDao.selectOneData() ?: XySettings()
        val next = transform(old)

        if (old.id != AllDataEnum.All.code) {
            db.settingsDao.update(next)
            next
        } else {
            val id = db.settingsDao.save(next)
            next.copy(id = id)
        }
    }

    /**
     * 更新缓存数据目录地址
     */
    fun updateCacheFilePath(path: String) {
/*        this._cacheFilePath.value = path
        db.settingsDao.updateCacheFilePath*/
    }

    /**
     * 保存播放缓存目录。空字符串表示使用平台默认目录。
     */
    suspend fun setCacheFilePath(cacheFilePath: String) {
        updateSettings { it.copy(cacheFilePath = cacheFilePath) }
    }

    fun sengTranscodingEvent(transcodingState: TranscodingState = TranscodingState.Transcoding) {
        _transcodingFlow.tryEmit(transcodingState)
    }

    /**
     * 获得是否静态资源不转码 true:不转码,false 转码
     */
    fun getStatic(): Boolean {
        val settings = settings.value
        return settings.ifTranscoding || audioBitRate.value == 0
    }

    /**
     * 更新是否存在连接设置
     */
    fun updateIfConnectionConfig(ifConnectionConfig: Boolean) {
        updateIfShowSnackBar(ifConnectionConfig)
    }

    /**
     * 更新是否显示底部ShowSnackBar
     */
    fun updateIfShowSnackBar(ifShowSnackBar: Boolean) {
        this._ifShowSnackBar.value = ifShowSnackBar
    }

}
