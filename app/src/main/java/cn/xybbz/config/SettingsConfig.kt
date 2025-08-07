package cn.xybbz.config

import android.app.LocaleManager
import android.content.Context
import android.os.LocaleList
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cn.xybbz.common.enums.AllDataEnum
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.setting.Settings
import cn.xybbz.localdata.enums.CacheUpperLimitEnum
import cn.xybbz.localdata.enums.LanguageType
import cn.xybbz.localdata.enums.ThemeTypeEnum
import kotlinx.coroutines.runBlocking
import java.util.Locale


class SettingsConfig(
    private val db: DatabaseClient,
) {

    private lateinit var settings: Settings

    var themeType by mutableStateOf(ThemeTypeEnum.SYSTEM)
        private set

    var isDynamic by mutableStateOf(false)
        private set

    var languageType by mutableStateOf(LanguageType.ZH_CN)
        private set

    //监听
    var onCacheUpperLimitChange:((CacheUpperLimitEnum) -> Unit)? = null
        private set

    //缓存设置
    var cacheUpperLimit  by mutableStateOf(CacheUpperLimitEnum.Auto)
        private set

    /**
     * 音乐缓存上限
     */
    var maxBytes by mutableStateOf(0L)



    fun setSettingsData() {
        Log.i("=====", "开始存储设置")
        this.settings = runBlocking { db.settingsDao.selectOneData() ?: Settings() }
        this.themeType = this.settings.themeType
        this.isDynamic = this.settings.isDynamic
        this.languageType = this.settings.languageType
        this.cacheUpperLimit = this.settings.cacheUpperLimit
        Log.i("api", "动态设置数据--读取配置")

    }

    fun setSettingsData(settings: Settings) {
        this.settings = settings
        Log.i("api", "动态设置数据--设置配置")
    }

    fun getSettingsData(): Settings {
        return settings
    }


    /**
     * 设置缓存上限
     */
    suspend fun setCacheUpperLimit(cacheUpperLimit: CacheUpperLimitEnum) {
        this.cacheUpperLimit = cacheUpperLimit
        settings = settings.copy(cacheUpperLimit = cacheUpperLimit)
        if (settings.id != AllDataEnum.All.code) {
            db.settingsDao.updateCacheUpperLimit(cacheUpperLimit, settings.id)
        } else {
            val settingId =
                db.settingsDao.save(Settings(cacheUpperLimit = cacheUpperLimit))
            settings = settings.copy(id = settingId)
        }
        onCacheUpperLimitChange?.invoke(cacheUpperLimit)
    }


    /**
     * 设置桌面歌词
     */
    suspend fun setIfDesktopLyrics(ifDesktopLyrics: Int) {

        settings = settings.copy(ifDesktopLyrics = ifDesktopLyrics)
        if (settings.id != AllDataEnum.All.code) {
            db.settingsDao.updateIfDesktopLyrics(ifDesktopLyrics, settings.id)
        } else {
            val settingId =
                db.settingsDao.save(Settings(ifDesktopLyrics = ifDesktopLyrics))
            settings = settings.copy(id = settingId)
        }
    }

    /**
     * 新增或更新播放速度
     */
    suspend fun saveOrUpdateDoubleSpeed(doubleSpeed: Float) {
        settings = settings.copy(doubleSpeed = doubleSpeed)
        if (settings.id != AllDataEnum.All.code) {
            db.settingsDao.updateDoubleSpeed(
                doubleSpeed = doubleSpeed,
                id = settings.id
            )
        } else {
            val settingId =
                db.settingsDao.save(Settings(doubleSpeed = doubleSpeed))
            settings = settings.copy(id = settingId)
        }
    }

    /**
     * 存储数据源类型
     */
    suspend fun saveConnectionId(connectionId: Long?) {
        if (connectionId != null) {
            settings = settings.copy(connectionId = connectionId)
            if (settings.id != AllDataEnum.All.code) {
                db.settingsDao.updateConnectionId(connectionId, settings.id)
            } else {
                val settingId =
                    db.settingsDao.save(Settings(connectionId = connectionId))
                settings = settings.copy(id = settingId)
            }
        }
    }


    /**
     * 设置是否开启所有专辑的播放历史记录
     */
    suspend fun setIfEnableAlbumHistory(ifEnableAlbumHistory: Boolean) {
        settings = settings.copy(ifEnableAlbumHistory = ifEnableAlbumHistory)
        if (settings.id != AllDataEnum.All.code) {
            db.settingsDao.updateIfEnableAlbumHistory(
                ifEnableAlbumHistory = ifEnableAlbumHistory,
                settings.id
            )

        } else {
            val settingId =
                db.settingsDao.save(Settings(ifEnableAlbumHistory = ifEnableAlbumHistory))
            settings = settings.copy(id = settingId)
        }
    }

    /**
     * 设置是否自动备份
     */

    suspend fun setAutoBackups(autoBackups: Boolean) {
        settings = settings.copy(autoBackups = autoBackups)
        if (settings.id != AllDataEnum.All.code) {
            db.settingsDao.updateAutoBackups(
                autoBackups = autoBackups,
                settings.id
            )

        } else {
            val settingId =
                db.settingsDao.save(Settings(autoBackups = autoBackups))
            settings = settings.copy(id = settingId)
        }
    }

    /**
     * 设置是否开启所有专辑的播放历史记录
     */
    suspend fun setIfHandleAudioFocus(ifHandleAudioFocus: Boolean) {
        settings = settings.copy(ifHandleAudioFocus = ifHandleAudioFocus)
        if (settings.id != AllDataEnum.All.code) {
            db.settingsDao.updateIfHandleAudioFocus(
                ifHandleAudioFocus = ifHandleAudioFocus,
                settings.id
            )

        } else {
            val settingId =
                db.settingsDao.save(Settings(ifHandleAudioFocus = ifHandleAudioFocus))
            settings = settings.copy(id = settingId)
        }
    }

    /**
     * 更新主题颜色类型
     */
    suspend fun setThemeTypeData(themeType: ThemeTypeEnum) {
        this.themeType = themeType
        settings = settings.copy(themeType = themeType)
        if (settings.id != AllDataEnum.All.code) {
            db.settingsDao.updateThemeType(
                themeType = themeType,
                settings.id
            )

        } else {
            val settingId =
                db.settingsDao.save(Settings(themeType = themeType))
            settings = settings.copy(id = settingId)
        }
    }

    /**
     * 设置是否动态颜色
     */
    suspend fun setIsDynamic(isDynamic: Boolean) {
        this.isDynamic = isDynamic
        settings = settings.copy(isDynamic = isDynamic)
        if (settings.id != AllDataEnum.All.code) {
            db.settingsDao.updateIsDynamic(
                isDynamic = isDynamic,
                settings.id
            )

        } else {
            val settingId =
                db.settingsDao.save(Settings(isDynamic = isDynamic))
            settings = settings.copy(id = settingId)
        }
    }

    /**
     * 更新语言设置
     */
    suspend fun setLanguageTypeData(languageType: LanguageType, context: Context) {
        this.languageType = languageType
        settings = settings.copy(languageType = languageType)
        if (settings.id != AllDataEnum.All.code) {
            db.settingsDao.updateLanguageType(
                languageType = languageType,
                settings.id
            )

        } else {
            val settingId =
                db.settingsDao.save(Settings(languageType = languageType))
            settings = settings.copy(id = settingId)
        }
        updateLanguage(languageType, context)
    }


    /**
     * 全局更新语言设置
     */
    fun updateLanguage(languageType: LanguageType, context: Context) {
        context.getSystemService(
            LocaleManager::class.java
        ).applicationLocales = LocaleList(Locale.forLanguageTag(languageType.languageCode))
    }

    /**
     * 设置缓存大小监听方法
     */
    fun setOnCacheUpperLimitListener(onChange:(CacheUpperLimitEnum) ->Unit){
        this.onCacheUpperLimitChange = onChange
    }

}