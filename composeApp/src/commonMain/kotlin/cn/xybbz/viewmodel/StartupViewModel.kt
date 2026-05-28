package cn.xybbz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.xybbz.AppStartupContent
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.localdata.enums.ThemeTypeEnum
import cn.xybbz.startup.StartupInitializer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.shareIn
import org.koin.core.annotation.KoinViewModel

data class StartupState(
    //主体颜色
    val themeTypeEnum: ThemeTypeEnum,
    val mainSceneInitialPage: AppStartupContent,
    // 设置数据已读出后，App 才能判断当前应该显示连接页还是继续启动主壳。
    val settingsLoaded: Boolean = false,
    // 当前本地设置中是否存在连接配置，用于启动阶段选择首开连接页。
    val hasConnectionConfig: Boolean = false,
    // 主壳放行标记：应用级轻量启动加载完成后才放行，不等待登录全链路结束。
    val readyForContent: Boolean = false,
    //背景图片地址
    val imageFilePath: String? = null
)

/**
 * 根内容路由决策结果。
 */
internal data class StartupContentDecision(
    // 当前根页面应该显示的内容：启动页、连接页或主壳。
    val content: AppStartupContent,
    // 主壳是否已经展示过；展示过后刷新登录不能再把根页面拉回 STARTUP。
    val hasShownMainContent: Boolean
)

/**
 * 只根据启动门闩和首开状态决定外层页面。
 * 登录刷新属于主壳内的后台行为，不能作为 STARTUP 的触发条件。
 *
 * @param settingsLoaded 设置是否已读取完成；未完成时不能判断连接配置，只能停留 STARTUP。
 * @param ifEntryPage 是否允许进入主界面流程；false 表示当前没有连接配置，需要进入 CONNECTION。
 * @param readyForContent 应用级轻量启动任务是否完成；完成后才能首次进入 MAIN。
 * @param hasShownMainContent 当前 ViewModel 生命周期内主壳是否已经展示过，用于刷新登录时保持 MAIN。
 */
internal fun resolveStartupContent(
    ifEntryPage: Boolean,
    readyForContent: Boolean
): StartupContentDecision {

    // 已经显示过主壳后，即使刷新登录或切源出现短暂未就绪，也继续留在 MAIN。
    // 首次启动时只有 readyForContent=true 才会锁存主壳展示状态并进入 MAIN。
    val nextHasShownMainContent = readyForContent

    return if (ifEntryPage){
        StartupContentDecision(
            content = AppStartupContent.MAIN,
            hasShownMainContent = nextHasShownMainContent
        )
    }else {
        // 没有连接配置时进入首开连接页，并重置“主壳已展示”标记。
        StartupContentDecision(
            content = AppStartupContent.CONNECTION,
            hasShownMainContent = false
        )
    }
}

@KoinViewModel
class StartupViewModel(
    private val settingsManager: SettingsManager,
    private val startupInitializer: StartupInitializer,
) : ViewModel() {


    val appState: Flow<StartupState?> = combine(
        settingsManager.themeType,
        settingsManager.ifConnectionConfig,
        startupInitializer.readiness,
        settingsManager.imageFilePath,
        settingsManager.ifEntryPage
    ) { themeSettings, ifConnectionConfig, readiness, imageFilePath, ifEntryPage ->
        val startupContentDecision = resolveStartupContent(
            ifEntryPage = ifEntryPage,
            readyForContent = readiness.readyForContent
        )
        StartupState(
            themeTypeEnum = themeSettings,
            mainSceneInitialPage = startupContentDecision.content,
            settingsLoaded = readiness.settingsLoaded,
            hasConnectionConfig = ifConnectionConfig,
            readyForContent = readiness.readyForContent,
            imageFilePath = imageFilePath
        )
    }.shareIn(
        viewModelScope,
        started = SharingStarted.Eagerly,
        replay = 1,
    )
}
