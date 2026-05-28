package cn.xybbz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.xybbz.AppStartupContent
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.localdata.enums.ThemeTypeEnum
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.koin.core.annotation.KoinViewModel

/**
 * App 根 Composable 需要的启动展示状态。
 */
data class StartupState(
    // 主体颜色。
    val themeTypeEnum: ThemeTypeEnum,
    // null 表示仍在启动加载阶段；非空时才交给根路由切到连接页或主壳。
    val mainSceneInitialPage: AppStartupContent?,
    // 当前本地设置中是否存在连接配置，用于启动阶段选择首开连接页。
    val hasConnectionConfig: Boolean = false,
    // 背景图片地址。
    val imageFilePath: String? = null
)

/**
 * 根内容路由决策结果。
 */
internal data class StartupContentDecision(
    // 当前根页面应该显示的内容；null 表示初始化未完成，UI 层继续显示启动加载页。
    val content: AppStartupContent?
)

/**
 * 只根据启动门闩和首开状态决定外层页面。
 * 登录刷新属于主壳内的后台行为，不能作为启动加载页的触发条件。
 *
 * @param ifEntryPage 是否允许进入主界面流程；false 表示当前没有连接配置，需要进入 CONNECTION。
 */
internal fun resolveStartupContent(
    ifEntryPage: Boolean,
): StartupContentDecision {

    // 已经显示过主壳后，即使刷新登录或切源出现短暂未就绪，也继续留在 MAIN。
    // 首次启动时只有 readyForContent=true 才会锁存主壳展示状态并进入 MAIN。

    return if (ifEntryPage) {
        StartupContentDecision(
            content = AppStartupContent.MAIN,
        )
    } else {
        // 没有连接配置时进入首开连接页，并重置“主壳已展示”标记。
        StartupContentDecision(
            content = AppStartupContent.CONNECTION,
        )
    }
}

@KoinViewModel
class StartupViewModel(
    private val settingsManager: SettingsManager
) : ViewModel() {


    /**
     * App 根层订阅的启动状态。
     * 合并主题、连接配置、启动门闩和背景图，避免 UI 层直接拼多个数据源。
     */
    val appState: StateFlow<StartupState> = combine(
        settingsManager.themeType,
        settingsManager.ifConnectionConfig,
        settingsManager.imageFilePath,
        settingsManager.ifEntryPage
    ) { themeSettings, ifConnectionConfig, imageFilePath, ifEntryPage ->
        // 这里把设置状态和启动门闩合并成根路由决策，避免 UI 层自己拼状态。
        val startupContentDecision = resolveStartupContent(
            ifEntryPage = ifEntryPage,
        )
        StartupState(
            themeTypeEnum = themeSettings,
            mainSceneInitialPage = startupContentDecision.content,
            hasConnectionConfig = ifConnectionConfig,
            imageFilePath = imageFilePath
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        StartupState(
            settingsManager.get().themeType,
            if (settingsManager.ifEntryPage.value) {
                AppStartupContent.MAIN
            } else {
                AppStartupContent.CONNECTION
            }, settingsManager.ifConnectionConfig.value, settingsManager.imageFilePath.value
        )
    )
}
