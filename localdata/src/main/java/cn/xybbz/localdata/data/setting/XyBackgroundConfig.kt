package cn.xybbz.localdata.data.setting

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 界面背景设置
 * 所有渐变色字段内容,都以 '/' 分割,并且只存储两个颜色
 */
@Entity(tableName = "xy_background_config")
data class XyBackgroundConfig(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /**
     * 图片地址
     * todo 暂未使用该字段
     */
    val imageFilePath: String? = null,
    /**
     * 是否切换为单一颜色背景
     */
    val ifChangeOneColor: Boolean = false,
    /**
     * 是否切换为全局统一渐变色
     */
    val ifGlobalBrash: Boolean = false,
    /**
     * 全局统一渐变色
     */
    val globalBrash: String = "#FF600015/#FF04727E",
    /**
     * 首页背景渐变色
     */
    val homeBrash: String = "#FF600015/#FF04727E",
    /**
     * 音乐列表页背景渐变色
     */
    val musicBrash: String = "#FF234B77/#FF04727E",
    /**
     * 专辑列表背景渐变色
     */
    val albumBrash: String = "#FF196473/#FF60318C",
    /**
     * 专辑详情背景渐变色
     */
    val albumInfoBrash: String = "#FF113460/#FF0B424B",
    /**
     * 艺术家列表背景渐变色
     */
    val artistBrash: String = "#FF036B41/#FF196473",
    /**
     * 艺术家详情背景渐变色
     */
    val artistInfoBrash: String = "#FF113460/#FF0B424B",
    /**
     * 收藏列表背景渐变色
     */
    val favoriteBrash: String = "#FF6C1577/#FFCC6877",
    /**
     * 流派列表背景渐变色
     */
    val genresBrash: String = "#FF5E0A69/#FF441172",
    /**
     * 流派详情背景渐变色
     */
    val genresInfoBrash: String = "#FF441172/#FF5E0A69",
    /**
     * 设置页面背景渐变色
     */
    val settingsBrash: String = "#FF503803/#FF04727E",
    /**
     * 关于页面背景渐变色
     */
    val aboutBrash: String = "#FF600015/#FF04727E",
    /**
     * 链接管理页面背景渐变色
     */
    val connectionManagerBrash: String = "#ff2b5876/#ff4e4376",
    /**
     * 链接详情页面背景渐变色
     */
    val connectionInfoBrash: String = "#FF228686/#ff330867",
    /**
     * 搜索页面背景渐变色
     */
    val searchBrash: String = "#FF600015/#FF04727E",
    /**
     * 缓存大小设置页面背景渐变色
     */
    val cacheLimitBrash: String = "#FF503803/#FF04727E",
    /**
     * 切换语言页面背景渐变色
     */
    val languageBrash: String = "#FF506464/#ffae8b9c",
    /**
     * 存储管理页面背景渐变色
     */
    val memoryManagementBrash: String = "#FF055934/#FF04727E",
    /**
     * 底部播放栏渐变色
     */
    val bottomPlayerBrash: String = "#ff3b82f6/#ff8b5cf6",
    /**
     * 底部弹出菜单渐变色
     */
    val bottomSheetBrash: String = "#FF600015/#FF04727E",
    /**
     * 弹窗渐变色
     */
    val alertDialogBrash: String = "#FF426770/#FF577C83",
    /**
     * 异常/告警弹窗渐变色
     */
    val errorAlertDialogBrash:String = "#FF814937/#FF8F6952",
    /**
     * 选择媒体库页面背景渐变色
     */
    val selectLibraryBrash:String = "#FF503803/#FF04727E",
    /**
     * 播放页渐变色
     */
    val playerBackground: String = "#FF0C0C0C",
    /**
     * 每日推荐页背景渐变色
     */
    val dailyRecommendBrash: String = "#FF6C1577/#FFCC6877",
    /**
     * 下载列表页面背景渐变色
     */
    val downloadListBrash: String = "#FF0D9488/#FF0EA5E9",
    /**
     * 本地音乐页面背景渐变色
     */
    val localMusicBrash: String = "#FF0A7B88/#FFFFBA6C",
    //todo 少了页面设置的背景颜色
)