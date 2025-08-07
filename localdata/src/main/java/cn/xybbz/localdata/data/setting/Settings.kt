package cn.xybbz.localdata.data.setting

import androidx.room.Entity
import androidx.room.PrimaryKey
import cn.xybbz.localdata.common.Constants
import cn.xybbz.localdata.enums.CacheUpperLimitEnum
import cn.xybbz.localdata.enums.LanguageType
import cn.xybbz.localdata.enums.ThemeTypeEnum

@Entity(tableName = "xy_settings")
data class Settings(
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
    //todo 默认播放音乐品质
    /**
     * 是否默认自动导出设置的配置
     */
    val autoBackups: Boolean = false,

    /**
     *  主题类型
     */
    val themeType: ThemeTypeEnum = ThemeTypeEnum.SYSTEM,
    /**
     * 是否动态颜色
     */
    val isDynamic: Boolean = false,
    /**
     * 是否允许与其他应用同时播放
     */
    val ifHandleAudioFocus: Boolean = true,
    /**
     * 语言
     * todo 到时候改成默认中文
     */
    val languageType: LanguageType = LanguageType.ZH_CN
)
