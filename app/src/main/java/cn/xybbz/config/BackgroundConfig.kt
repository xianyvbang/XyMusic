package cn.xybbz.config

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.enums.AllDataEnum
import cn.xybbz.common.utils.CoroutineScopeUtils
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.setting.XyBackgroundConfig
import cn.xybbz.ui.theme.xyBackgroundBrash
import kotlinx.coroutines.launch

class BackgroundConfig(
    private val db: DatabaseClient,
    private val applicationContext: Context
) {

    private var backgroundConfig: XyBackgroundConfig? = null
    fun get(): XyBackgroundConfig {
        return backgroundConfig ?: XyBackgroundConfig()
    }

    val defaultBackgroundConfig: XyBackgroundConfig = XyBackgroundConfig()

    var xyBackgroundBrash by mutableStateOf(xyBackgroundBrash())
        private set

    private val coroutineScope = CoroutineScopeUtils.getIo("background")


    /**
     * 图片地址
     * todo 暂未使用该字段
     */
    var imageFilePath by mutableStateOf<String?>("", structuralEqualityPolicy())
        private set

    /**
     * 是否切换为单一颜色背景
     */
    var ifChangeOneColor by mutableStateOf(false, structuralEqualityPolicy())
        private set

    /**
     * 是否切换为全局统一渐变色
     */
    var ifGlobalBrash by mutableStateOf(false, structuralEqualityPolicy())
        private set

    /**
     * 全局统一渐变色
     */
    var globalBrash by mutableStateOf<List<Color>>(emptyList(), structuralEqualityPolicy())
        private set

    /**
     * 首页背景渐变色
     */
    var homeBrash by mutableStateOf<List<Color>>(emptyList(), structuralEqualityPolicy())
        private set

    /**
     * 音乐列表页背景渐变色
     */
    var musicBrash by mutableStateOf<List<Color>>(emptyList(), structuralEqualityPolicy())
        private set

    /**
     * 专辑列表背景渐变色
     */
    var albumBrash by mutableStateOf<List<Color>>(emptyList(), structuralEqualityPolicy())
        private set

    /**
     * 专辑详情背景渐变色
     */
    var albumInfoBrash by mutableStateOf<List<Color>>(emptyList(), structuralEqualityPolicy())
        private set

    /**
     * 艺术家列表背景渐变色
     */
    var artistBrash by mutableStateOf<List<Color>>(emptyList(), structuralEqualityPolicy())
        private set

    /**
     * 艺术家详情背景渐变色
     */
    var artistInfoBrash by mutableStateOf<List<Color>>(emptyList(), structuralEqualityPolicy())
        private set

    /**
     * 收藏列表背景渐变色
     */
    var favoriteBrash by mutableStateOf<List<Color>>(emptyList(), structuralEqualityPolicy())
        private set

    /**
     * 流派列表背景渐变色
     */
    var genresBrash by mutableStateOf<List<Color>>(emptyList(), structuralEqualityPolicy())
        private set

    /**
     * 流派详情背景渐变色
     */
    var genresInfoBrash by mutableStateOf<List<Color>>(emptyList(), structuralEqualityPolicy())
        private set

    /**
     * 设置页面背景渐变色
     */
    var settingsBrash by mutableStateOf<List<Color>>(emptyList(), structuralEqualityPolicy())
        private set

    /**
     * 关于页面背景渐变色
     */
    var aboutBrash by mutableStateOf<List<Color>>(emptyList(), structuralEqualityPolicy())
        private set

    /**
     * 链接管理页面背景渐变色
     */
    var connectionManagerBrash by mutableStateOf<List<Color>>(
        emptyList(),
        structuralEqualityPolicy()
    )
        private set

    /**
     * 链接详情页面背景渐变色
     */
    var connectionInfoBrash by mutableStateOf<List<Color>>(emptyList(), structuralEqualityPolicy())
        private set

    /**
     * 搜索页面背景渐变色
     */
    var searchBrash by mutableStateOf<List<Color>>(emptyList(), structuralEqualityPolicy())
        private set

    /**
     * 缓存大小设置页面背景渐变色
     */
    var cacheLimitBrash by mutableStateOf<List<Color>>(emptyList(), structuralEqualityPolicy())
        private set

    /**
     * 切换语言页面背景渐变色
     */
    var languageBrash by mutableStateOf<List<Color>>(emptyList(), structuralEqualityPolicy())
        private set

    /**
     * 存储管理页面背景渐变色
     */
    var memoryManagementBrash by mutableStateOf<List<Color>>(
        emptyList(),
        structuralEqualityPolicy()
    )
        private set

    /**
     * 底部播放栏渐变色
     */
    var bottomPlayerBrash by mutableStateOf<List<Color>>(emptyList(), structuralEqualityPolicy())
        private set

    /**
     * 底部弹出菜单渐变色
     */
    var bottomSheetBrash by mutableStateOf<List<Color>>(emptyList(), structuralEqualityPolicy())
        private set

    /**
     * 弹窗渐变色
     */
    var alertDialogBrash by mutableStateOf<List<Color>>(emptyList(), structuralEqualityPolicy())
        private set

    /**
     * 异常/告警弹窗渐变色
     */
    var errorAlertDialogBrash by mutableStateOf<List<Color>>(
        emptyList(),
        structuralEqualityPolicy()
    )
        private set

    /**
     * 选择媒体库页面背景渐变色
     */
    var selectLibraryBrash by mutableStateOf<List<Color>>(emptyList(), structuralEqualityPolicy())
        private set

    /**
     * 播放页渐变色
     */
    var playerBackground by mutableStateOf<Color>(Color(0xFF0C0C0C), structuralEqualityPolicy())
        private set

    fun load() {
        coroutineScope.launch {
            backgroundConfig = db.backgroundConfigDao.selectOne()
            imageFilePath = get().imageFilePath
            ifChangeOneColor = get().ifChangeOneColor
            ifGlobalBrash = get().ifGlobalBrash
            globalBrash = stringToColors(get().globalBrash)
            homeBrash = stringToColors(get().homeBrash)
            musicBrash = stringToColors(get().musicBrash)
            albumBrash = stringToColors(get().albumBrash)
            albumInfoBrash = stringToColors(get().albumInfoBrash)
            artistBrash = stringToColors(get().artistBrash)
            artistInfoBrash = stringToColors(get().artistInfoBrash)
            favoriteBrash = stringToColors(get().favoriteBrash)
            genresBrash = stringToColors(get().genresBrash)
            genresInfoBrash = stringToColors(get().genresInfoBrash)
            settingsBrash = stringToColors(get().settingsBrash)
            aboutBrash = stringToColors(get().aboutBrash)
            connectionManagerBrash = stringToColors(get().connectionManagerBrash)
            connectionInfoBrash = stringToColors(get().connectionInfoBrash)
            searchBrash = stringToColors(get().searchBrash)
            cacheLimitBrash = stringToColors(get().cacheLimitBrash)
            languageBrash = stringToColors(get().languageBrash)
            memoryManagementBrash = stringToColors(get().memoryManagementBrash)
            bottomPlayerBrash = stringToColors(get().bottomPlayerBrash)
            bottomSheetBrash = stringToColors(get().bottomSheetBrash)
            alertDialogBrash = stringToColors(get().alertDialogBrash)
            errorAlertDialogBrash = stringToColors(get().errorAlertDialogBrash)
            selectLibraryBrash = stringToColors(get().selectLibraryBrash)
            playerBackground = stringToColor(get().playerBackground)

        }
    }

    /**
     * 重置设置
     */
    suspend fun reset() {
        if (get().id != AllDataEnum.All.code)
            db.backgroundConfigDao.deleteById(get())
        load()
    }

    fun updateXyBackgroundBrash() {
        xyBackgroundBrash = xyBackgroundBrash(
            ifChangeOneColor = ifChangeOneColor,
            ifGlobalBrash = ifGlobalBrash,
            globalBrash = globalBrash
        )
    }


    /**
     * 是否切换为单一颜色背景
     */
    suspend fun updateIfChangeOneColor(ifChangeOneColor: Boolean) {
        this.ifChangeOneColor = ifChangeOneColor
        backgroundConfig =
            get().copy(ifChangeOneColor = ifChangeOneColor)
        saveOrUpdate()
        updateXyBackgroundBrash()
    }

    /**
     * 是否切换为单一颜色背景
     */
    suspend fun updateIfGlobalBrash(ifGlobalBrash: Boolean) {
        this.ifGlobalBrash = ifGlobalBrash
        backgroundConfig =
            get().copy(ifGlobalBrash = ifGlobalBrash)
        saveOrUpdate()
        updateXyBackgroundBrash()
    }

    /**
     * 是否切换为单一颜色背景
     */
    suspend fun updateGlobalBrash(colorStrings: List<String>, colors: List<Color>) {
        this.globalBrash = colors
        val globalBrashStr = colorStrings.joinToString(Constants.SLASH_DELIMITER) { it }
        backgroundConfig =
            get().copy(globalBrash = globalBrashStr)
        saveOrUpdate()
        updateXyBackgroundBrash()
    }


    /**
     * 设置首页渐变色
     */
    suspend fun updateHomeBrash(colorStrings: List<String>, homeBrash: List<Color>) {
        this.homeBrash = homeBrash
        val homeBrashStr = colorStrings.joinToString(Constants.SLASH_DELIMITER) { it }
        backgroundConfig =
            get().copy(homeBrash = homeBrashStr)
        saveOrUpdate()
    }


    /**
     * 设置音乐列表页背景渐变色
     */
    suspend fun updateMusicBrash(colorStrings: List<String>, musicBrash: List<Color>) {
        this.musicBrash = musicBrash
        val brashStr = colorStrings.joinToString(Constants.SLASH_DELIMITER) { it }
        backgroundConfig =
            get().copy(musicBrash = brashStr)
        saveOrUpdate()
    }


    /**
     * 设置专辑列表背景渐变色
     */
    suspend fun updateAlbumBrash(colorStrings: List<String>, colors: List<Color>) {
        this.albumBrash = colors
        val brashStr = colorStrings.joinToString(Constants.SLASH_DELIMITER) { it }
        backgroundConfig =
            get().copy(albumBrash = brashStr)
        saveOrUpdate()
    }

    /**
     * 设置专辑详情背景渐变色
     */
    suspend fun updateAlbumInfoBrash(colorStrings: List<String>, colors: List<Color>) {
        this.albumInfoBrash = colors
        val brashStr = colorStrings.joinToString(Constants.SLASH_DELIMITER) { it }
        backgroundConfig =
            get().copy(albumInfoBrash = brashStr)
        saveOrUpdate()
    }

    /**
     * 设置艺术家列表背景渐变色
     */
    suspend fun updateArtistBrash(colorStrings: List<String>, colors: List<Color>) {
        this.artistBrash = colors
        val brashStr = colorStrings.joinToString(Constants.SLASH_DELIMITER) { it }
        backgroundConfig =
            get().copy(artistBrash = brashStr)
        saveOrUpdate()
    }

    /**
     * 设置艺术家详情背景渐变色
     */
    suspend fun updateArtistInfoBrash(colorStrings: List<String>, colors: List<Color>) {
        this.artistInfoBrash = colors
        val brashStr = colorStrings.joinToString(Constants.SLASH_DELIMITER) { it }
        backgroundConfig =
            get().copy(artistInfoBrash = brashStr)
        saveOrUpdate()
    }

    /**
     * 设置收藏列表背景渐变色
     */
    suspend fun updateFavoriteBrash(colorStrings: List<String>, colors: List<Color>) {
        this.favoriteBrash = colors
        val brashStr = colorStrings.joinToString(Constants.SLASH_DELIMITER) { it }
        backgroundConfig =
            get().copy(favoriteBrash = brashStr)
        saveOrUpdate()
    }

    /**
     * 设置流派列表背景渐变色
     */
    suspend fun updateGenresBrash(colorStrings: List<String>, colors: List<Color>) {
        this.genresBrash = colors
        val brashStr = colorStrings.joinToString(Constants.SLASH_DELIMITER) { it }
        backgroundConfig =
            get().copy(genresBrash = brashStr)
        saveOrUpdate()
    }

    /**
     * 设置流派详情背景渐变色
     */
    suspend fun updateGenresInfoBrash(colorStrings: List<String>, colors: List<Color>) {
        this.genresInfoBrash = colors
        val brashStr = colorStrings.joinToString(Constants.SLASH_DELIMITER) { it }
        backgroundConfig =
            get().copy(genresInfoBrash = brashStr)
        saveOrUpdate()
    }

    /**
     * 设置设置页面背景渐变色
     */
    suspend fun updateSettingsBrash(colorStrings: List<String>, colors: List<Color>) {
        this.settingsBrash = colors
        val brashStr = colorStrings.joinToString(Constants.SLASH_DELIMITER) { it }
        backgroundConfig =
            get().copy(settingsBrash = brashStr)
        saveOrUpdate()
    }

    /**
     * 设置关于页面背景渐变色
     */
    suspend fun updateAboutBrash(colorStrings: List<String>, colors: List<Color>) {
        this.aboutBrash = colors
        val brashStr = colorStrings.joinToString(Constants.SLASH_DELIMITER) { it }
        backgroundConfig =
            get().copy(aboutBrash = brashStr)
        saveOrUpdate()
    }

    /**
     * 设置链接管理页面背景渐变色
     */
    suspend fun updateConnectionManagerBrash(colorStrings: List<String>, colors: List<Color>) {
        this.connectionManagerBrash = colors
        val brashStr = colorStrings.joinToString(Constants.SLASH_DELIMITER) { it }
        backgroundConfig =
            get().copy(connectionManagerBrash = brashStr)
        saveOrUpdate()
    }

    /**
     * 设置链接详情页面背景渐变色
     */
    suspend fun updateConnectionInfoBrash(colorStrings: List<String>, colors: List<Color>) {
        this.connectionInfoBrash = colors
        val brashStr = colorStrings.joinToString(Constants.SLASH_DELIMITER) { it }
        backgroundConfig =
            get().copy(connectionInfoBrash = brashStr)
        saveOrUpdate()
    }

    /**
     * 设置搜索页面背景渐变色
     */
    suspend fun updateSearchBrash(colorStrings: List<String>, colors: List<Color>) {
        this.searchBrash = colors
        val brashStr = colorStrings.joinToString(Constants.SLASH_DELIMITER) { it }
        backgroundConfig =
            get().copy(searchBrash = brashStr)
        saveOrUpdate()
    }

    /**
     * 设置缓存大小设置页面背景渐变色
     */
    suspend fun updateCacheLimitBrash(colorStrings: List<String>, colors: List<Color>) {
        this.cacheLimitBrash = colors
        val brashStr = colorStrings.joinToString(Constants.SLASH_DELIMITER) { it }
        backgroundConfig =
            get().copy(cacheLimitBrash = brashStr)
        saveOrUpdate()
    }

    /**
     * 设置切换语言页面背景渐变色
     */
    suspend fun updateLanguageBrash(colorStrings: List<String>, colors: List<Color>) {
        this.languageBrash = colors
        val brashStr = colorStrings.joinToString(Constants.SLASH_DELIMITER) { it }
        backgroundConfig =
            get().copy(languageBrash = brashStr)
        saveOrUpdate()
    }

    /**
     * 设置存储管理页面背景渐变色
     */
    suspend fun updateMemoryManagementBrash(colorStrings: List<String>, colors: List<Color>) {
        this.memoryManagementBrash = colors
        val brashStr = colorStrings.joinToString(Constants.SLASH_DELIMITER) { it }
        backgroundConfig =
            get().copy(memoryManagementBrash = brashStr)
        saveOrUpdate()
    }

    /**
     * 设置底部播放栏渐变色
     */
    suspend fun updateBottomPlayerBrash(colorStrings: List<String>, colors: List<Color>) {
        this.bottomPlayerBrash = colors
        val brashStr = colorStrings.joinToString(Constants.SLASH_DELIMITER) { it }
        backgroundConfig =
            get().copy(bottomPlayerBrash = brashStr)
        saveOrUpdate()
    }

    /**
     * 设置底部弹出菜单渐变色
     */
    suspend fun updateBottomSheetBrash(colorStrings: List<String>, colors: List<Color>) {
        this.bottomSheetBrash = colors
        val brashStr = colorStrings.joinToString(Constants.SLASH_DELIMITER) { it }
        backgroundConfig =
            get().copy(bottomSheetBrash = brashStr)
        saveOrUpdate()
    }

    /**
     * 设置弹窗渐变色
     */
    suspend fun updateAlertDialogBrash(colorStrings: List<String>, colors: List<Color>) {
        this.alertDialogBrash = colors
        val brashStr = colorStrings.joinToString(Constants.SLASH_DELIMITER) { it }
        backgroundConfig =
            get().copy(alertDialogBrash = brashStr)
        saveOrUpdate()
    }


    /**
     * 设置异常弹窗渐变色
     */
    suspend fun updateErrorAlertDialogBrash(colorStrings: List<String>, colors: List<Color>) {
        this.errorAlertDialogBrash = colors
        val brashStr = colorStrings.joinToString(Constants.SLASH_DELIMITER) { it }
        backgroundConfig =
            get().copy(errorAlertDialogBrash = brashStr)
        saveOrUpdate()
    }

    /**
     * 设置选择媒体库页面背景渐变色
     */
    suspend fun updateSelectLibraryBrash(colorStrings: List<String>, colors: List<Color>) {
        this.selectLibraryBrash = colors
        val brashStr = colorStrings.joinToString(Constants.SLASH_DELIMITER) { it }
        backgroundConfig =
            get().copy(selectLibraryBrash = brashStr)
        saveOrUpdate()
    }

    /**
     * 设置播放页渐变色
     */
    suspend fun updatePlayerBackground(colorString: String, color: Color) {
        this.playerBackground = color
        backgroundConfig =
            get().copy(playerBackground = colorString)
        saveOrUpdate()
    }


    /**
     * 将字符串转换成颜色集合
     */
    fun stringToColors(color: String): List<Color> {
        return color.split(Constants.SLASH_DELIMITER).map { Color(it.toColorInt()) }
    }

    /**
     * 将字符串转换成颜色
     */
    fun stringToColor(color: String): Color {
        return Color(color.toColorInt())
    }

    suspend fun saveOrUpdate() {
        if (get().id != AllDataEnum.All.code) {
            db.backgroundConfigDao.updateById(get())
        } else {
            val configId =
                db.backgroundConfigDao.save(get())
            backgroundConfig = get().copy(id = configId)
        }
    }
}