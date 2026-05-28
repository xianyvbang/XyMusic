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

/**
 * App 根 Composable 需要的启动展示状态。
 */
data class StartupState(
    // 主体颜色。
    val themeTypeEnum: ThemeTypeEnum,
    // null 表示仍在启动加载阶段；非空时才交给根路由切到连接页或主壳。
    val mainSceneInitialPage: AppStartupContent?,
    // 设置数据已读出后，App 才能判断当前应该显示连接页还是继续启动主壳。
    val settingsLoaded: Boolean = false,
    // 当前本地设置中是否存在连接配置，用于启动阶段选择首开连接页。
    val hasConnectionConfig: Boolean = false,
    // 主壳放行标记：应用级轻量启动加载完成后才放行，不等待登录全链路结束。
    val readyForContent: Boolean = false,
    // 背景图片地址。
    val imageFilePath: String? = null
)

/**
 * 根内容路由决策结果。
 */
internal data class StartupContentDecision(
    // 当前根页面应该显示的内容；null 表示初始化未完成，UI 层继续显示启动加载页。
    val content: AppStartupContent?,
    // 主壳是否已经展示过；展示过后刷新登录不能再把根页面拉回启动加载页。
    val hasShownMainContent: Boolean
)

/**
 * 只根据启动门闩和首开状态决定外层页面。
 * 登录刷新属于主壳内的后台行为，不能作为启动加载页的触发条件。
 *
 * @param settingsLoaded 设置是否已读取完成；未完成时不能判断连接配置，只能继续显示启动加载页。
 * @param ifEntryPage 是否允许进入主界面流程；false 表示当前没有连接配置，需要进入 CONNECTION。
 * @param readyForContent 应用级轻量启动任务是否完成；完成后才能首次进入 MAIN。
 * @param hasShownMainContent 当前 ViewModel 生命周期内主壳是否已经展示过，用于刷新登录时保持 MAIN。
 */
internal fun resolveStartupContent(
    settingsLoaded: Boolean,
    ifEntryPage: Boolean,
    readyForContent: Boolean,
    hasShownMainContent: Boolean
): StartupContentDecision {

    // 设置还没读完时，只能老老实实留在启动页，避免过早判断首开状态。
    if (!settingsLoaded) {
        return StartupContentDecision(
            content = null,
            hasShownMainContent = false
        )
    }

    // 没有连接配置时直接进入连接页，和后续的自动登录阶段彻底解耦。
    if (!ifEntryPage) {
        return StartupContentDecision(
            content = AppStartupContent.CONNECTION,
            hasShownMainContent = false
        )
    }

    // 主壳已经展示过后，刷新登录/切源期间继续保持主壳，不要把根页面拉回去。
    if (hasShownMainContent) {
        return StartupContentDecision(
            content = AppStartupContent.MAIN,
            hasShownMainContent = true
        )
    }

    // 首次进入主壳前，仍然要等轻量启动任务完成，避免主壳过早创建。
    if (!readyForContent) {
        return StartupContentDecision(
            content = null,
            hasShownMainContent = false
        )
    }

    // 已经显示过主壳后，即使刷新登录或切源出现短暂未就绪，也继续留在 MAIN。
    // 首次启动时只有 readyForContent=true 才会锁存主壳展示状态并进入 MAIN。
    return StartupContentDecision(
        content = AppStartupContent.MAIN,
        hasShownMainContent = true
    )
}

@KoinViewModel
class StartupViewModel(
    private val settingsManager: SettingsManager,
    private val startupInitializer: StartupInitializer,
) : ViewModel() {

    /**
     * 当前 ViewModel 生命周期内主壳是否已经展示过。
     * 一旦进入过 MAIN，刷新登录或切源的短暂 loading 不再把根页面拉回启动加载页。
     */
    private var hasShownMainContent = false

    /**
     * App 根层订阅的启动状态。
     * 合并主题、连接配置、启动门闩和背景图，避免 UI 层直接拼多个数据源。
     */
    val appState: Flow<StartupState?> = combine(
        settingsManager.themeType,
        settingsManager.ifConnectionConfig,
        startupInitializer.readiness,
        settingsManager.imageFilePath,
        settingsManager.ifEntryPage
    ) { themeSettings, ifConnectionConfig, readiness, imageFilePath, ifEntryPage ->
        // 这里把设置状态和启动门闩合并成根路由决策，避免 UI 层自己拼状态。
        val startupContentDecision = resolveStartupContent(
            settingsLoaded = readiness.settingsLoaded,
            ifEntryPage = ifEntryPage,
            readyForContent = readiness.readyForContent,
            hasShownMainContent = hasShownMainContent
        )
        hasShownMainContent = startupContentDecision.hasShownMainContent
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
