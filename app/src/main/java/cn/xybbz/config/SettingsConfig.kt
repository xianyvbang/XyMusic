package cn.xybbz.config

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cn.xybbz.common.enums.AllDataEnum
import cn.xybbz.common.utils.CoroutineScopeUtils
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.setting.XySettings
import cn.xybbz.localdata.enums.CacheUpperLimitEnum
import cn.xybbz.localdata.enums.LanguageType
import com.hjq.language.MultiLanguages
import kotlinx.coroutines.launch
import java.util.Locale


class SettingsConfig(
    private val db: DatabaseClient,
    private val applicationContext: Context
) {

    private val coroutineScope = CoroutineScopeUtils.getIo("settings")

    private var settings: XySettings? = null

    var languageType by mutableStateOf<LanguageType?>(null)
        private set

    //监听
    var onCacheUpperLimitChange: ((CacheUpperLimitEnum) -> Unit)? = null
        private set

    //缓存设置
    var cacheUpperLimit by mutableStateOf(CacheUpperLimitEnum.Auto)
        private set

    //缓存文件所在位置
    var cacheFilePath by mutableStateOf("")
        private set

    lateinit var packageInfo: PackageInfo
        private set

    /**
     * 音乐缓存上限
     */
    var maxBytes by mutableStateOf(0L)

    fun get(): XySettings {
        return settings ?: XySettings()
    }

    fun setSettingsData() {
        coroutineScope.launch {
            Log.i("=====", "开始存储设置")
            this@SettingsConfig.settings = db.settingsDao.selectOneData() ?: XySettings()
            this@SettingsConfig.languageType = this@SettingsConfig.get().languageType
            this@SettingsConfig.cacheUpperLimit = this@SettingsConfig.get().cacheUpperLimit
            Log.i("api", "动态设置数据--读取配置")
            setDefaultLanguage(this@SettingsConfig.languageType, applicationContext)
        }
        val packageManager = applicationContext.packageManager
        val packageName = applicationContext.packageName
        packageInfo =
            packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
    }

    fun setSettingsData(settings: XySettings) {
        this.settings = settings
        Log.i("api", "动态设置数据--设置配置")
    }


    /**
     * 设置缓存上限
     */
    suspend fun setCacheUpperLimit(cacheUpperLimit: CacheUpperLimitEnum) {
        this.cacheUpperLimit = cacheUpperLimit
        settings = get().copy(cacheUpperLimit = cacheUpperLimit)
        if (get().id != AllDataEnum.All.code) {
            db.settingsDao.updateCacheUpperLimit(cacheUpperLimit, get().id)
        } else {
            val settingId =
                db.settingsDao.save(XySettings(cacheUpperLimit = cacheUpperLimit))
            settings = get().copy(id = settingId)
        }
        onCacheUpperLimitChange?.invoke(cacheUpperLimit)
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
    suspend fun saveConnectionId(connectionId: Long?) {
        if (connectionId != null) {
            settings = get().copy(connectionId = connectionId)
            if (get().id != AllDataEnum.All.code) {
                db.settingsDao.updateConnectionId(connectionId, get().id)
            } else {
                val settingId =
                    db.settingsDao.save(XySettings(connectionId = connectionId))
                settings = get().copy(id = settingId)
            }
        }
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
    fun setDefaultLanguage(languageType: LanguageType?, context: Context) {
        if (languageType == null) {
            val systemLanguage = MultiLanguages.getSystemLanguage(context)
            Log.i("======", "当前语言 ${systemLanguage.displayLanguage}  ${systemLanguage.toLanguageTag()}")
            MultiLanguages.clearAppLanguage(context)
            this.languageType = LanguageType.getThis(systemLanguage.toLanguageTag())
        } else {
            val locale = Locale.forLanguageTag(languageType.languageCode)
            MultiLanguages.setAppLanguage(context, locale);
            this.languageType = languageType
        }
    }

    /**
     * 设置缓存大小监听方法
     */
    fun setOnCacheUpperLimitListener(onChange: (CacheUpperLimitEnum) -> Unit) {
        this.onCacheUpperLimitChange = onChange
    }

    /**
     * 更新设置是否关闭本地数据优先获取播放
     */
    suspend fun setIsLocalData(isLocal: Boolean) {
        settings = get().copy(isLocal = isLocal)
        if (get().id != AllDataEnum.All.code) {
            db.settingsDao.updateIsLocal(
                isLocal = isLocal,
                get().id
            )

        } else {
            val settingId =
                db.settingsDao.save(XySettings(isLocal = isLocal))
            settings = get().copy(id = settingId)
        }
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
     * 更新缓存数据目录地址
     */
    fun updateCacheFilePath(path: String) {
        this.cacheFilePath = path
    }

}