package cn.xybbz.localdata.data.setting

import androidx.room.Entity
import androidx.room.PrimaryKey
import cn.xybbz.localdata.common.Constants
import cn.xybbz.localdata.enums.CacheUpperLimitEnum
import cn.xybbz.localdata.enums.LanguageType

@Entity(tableName = "xy_settings")
data class XySettings(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /**
     * 缓存上限
     */
    val cacheUpperLimit: CacheUpperLimitEnum = CacheUpperLimitEnum.Auto,
    /**
     * 桌面歌词
     */
    val ifDesktopLyrics: Int = Constants.NO,
    /**
     * 倍速播放速度
     *  0.5f -> "0.5倍"
     *  1f -> "正常"
     *  1.5f -> "1.5倍"
     *  2f -> "2倍"
     */
    val doubleSpeed: Float = 1f,
    /**
     * 连接id
     */
    val connectionId: Long? = null,
    /**
     * 是否开启所有专辑的播放历史记录
     */
    val ifEnableAlbumHistory: Boolean = false,
    /**
     * 是否允许与其他应用同时播放
     */
    val ifHandleAudioFocus: Boolean = true,
    /**
     * 语言
     */
    val languageType: LanguageType? = null /*LanguageType.ZH_CN*/,
    /**
     * 最新版本获取时间 每次打开如果间隔不超过1小时的话,就不获取新数据
     */
    val latestVersionTime: Long = 0,
    /**
     * 最新版本版本号
     */
    val latestVersion: String = "",
    /**
     * 最新版本下载地址
     */
    val lasestApkUrl:String = "",
    /**
     * 最大同时下载数量
     */
    val maxConcurrentDownloads:Int = 3,
    /**
     * 背景图片地址
     */
    val imageFilePath: String? = null
)
