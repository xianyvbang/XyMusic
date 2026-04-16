package cn.xybbz

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material3.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.enums.img
import cn.xybbz.common.utils.DataSourceChangeUtils
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.di.initKoin
import cn.xybbz.localdata.config.LocalDatabaseClient
import cn.xybbz.localdata.data.connection.ConnectionConfig
import cn.xybbz.localdata.enums.ThemeTypeEnum
import cn.xybbz.proxy.JvmReverseProxyServer
import cn.xybbz.ui.popup.MenuItemDefaultData
import cn.xybbz.ui.popup.XyDropdownMenu
import cn.xybbz.ui.screens.jvmRouterMenuWidth
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyColumn
import cn.xybbz.ui.xy.XyEdit
import cn.xybbz.ui.xy.XyRow
import cn.xybbz.ui.xy.XyText
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.mp.KoinPlatform
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.app_icon_info
import xymusic_kmp.composeapp.generated.resources.app_name
import xymusic_kmp.composeapp.generated.resources.arrow_back_24px
import xymusic_kmp.composeapp.generated.resources.chevron_right_24px
import xymusic_kmp.composeapp.generated.resources.check_24px
import xymusic_kmp.composeapp.generated.resources.connection_link
import xymusic_kmp.composeapp.generated.resources.icon
import xymusic_kmp.composeapp.generated.resources.keyboard_arrow_down_24px
import xymusic_kmp.composeapp.generated.resources.logo_new
import xymusic_kmp.composeapp.generated.resources.no_connection_selected
import xymusic_kmp.composeapp.generated.resources.open_add_or_switch_data_sources
import xymusic_kmp.composeapp.generated.resources.search_24px
import xymusic_kmp.composeapp.generated.resources.search_music_album_artist
import kotlinx.coroutines.launch

fun main() = application {
    initKoin {}
    // 应用启动后立即拉起本地代理服务，供封面、音频与视频流转发使用。
    JvmReverseProxyServer.start()

    val handleCloseRequest = {
        // 应用退出前主动关闭代理服务，避免残留端口占用与连接资源泄漏。
        JvmReverseProxyServer.stop()
        exitApplication()
    }
    val windowState = rememberWindowState()

    Window(
        onCloseRequest = handleCloseRequest,
        undecorated = true,
        resizable = true,
        title = "XyMusic-KMP",
        state = windowState,
    ) {
        DesktopWindowTheme {
            XyColumn(
                modifier = Modifier.fillMaxSize(),
                paddingValues = PaddingValues()
            ) {
                WindowDraggableArea {
                    XyRow(
                        modifier = Modifier
                            .height(XyTheme.dimens.itemHeight * 1.3f)
                            .background(DesktopTitleBarColors.current.background),
                        paddingValues = PaddingValues(
//                            horizontal = XyTheme.dimens.outerHorizontalPadding,
                            vertical = XyTheme.dimens.outerVerticalPadding
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        DesktopTitleBrand()

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            DesktopTitleCenter()
                        }

                        DesktopTitleActions(
                            isMaximized = windowState.placement == WindowPlacement.Maximized,
                            onMinimize = { windowState.isMinimized = true },
                            onToggleMaximize = {
                                windowState.placement =
                                    if (windowState.placement == WindowPlacement.Maximized) {
                                        WindowPlacement.Floating
                                    } else {
                                        WindowPlacement.Maximized
                                    }
                            },
                            onClose = handleCloseRequest
                        )
                    }
                }
                App()
            }
        }
    }
}

/**
 * 为 desktopApp 入口补一层 Material 风格主题色，
 * 供桌面端自定义标题栏直接读取统一的颜色体系。
 */
@Composable
private fun DesktopWindowTheme(content: @Composable () -> Unit) {
    val settingsManager: SettingsManager = remember { KoinPlatform.getKoin().get() }
    val themeType by settingsManager.themeType.collectAsState()
    val isDark = when (themeType) {
        ThemeTypeEnum.SYSTEM -> isSystemInDarkTheme()
        ThemeTypeEnum.DARK -> true
        ThemeTypeEnum.LIGHT -> false
    }

    MaterialTheme(
        colors = if (isDark) {
            darkColors(
                primary = cn.xybbz.ui.theme.darkPrimary,
                background = cn.xybbz.ui.theme.darkSurface,
                surface = cn.xybbz.ui.theme.darkSurfaceContainerLowest,
                onSurface = cn.xybbz.ui.theme.darkOnSurface,
            )
        } else {
            lightColors(
                primary = cn.xybbz.ui.theme.lightPrimary,
                background = cn.xybbz.ui.theme.lightSurface,
                surface = cn.xybbz.ui.theme.lightSurfaceContainerLowest,
                onSurface = cn.xybbz.ui.theme.lightOnSurface,
            )
        },
        content = content
    )
}

/**
 * 顶部栏左侧的应用品牌区，展示应用图标与名称。
 */
@Composable
private fun DesktopTitleBrand() {
    val colors = DesktopTitleBarColors.current

    Row(
        modifier = Modifier.widthIn(min = jvmRouterMenuWidth),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(Res.drawable.logo_new),
            contentDescription = stringResource(Res.string.app_icon_info),
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(XyTheme.dimens.corner))
        )
        Spacer(modifier = Modifier.width(XyTheme.dimens.contentPadding))
        XyText(
            text = stringResource(Res.string.app_name),
            color = colors.foreground,
        )
    }
}

/**
 * 顶部栏中间区域，包含返回、前进按钮和搜索框。
 */
@Composable
private fun DesktopTitleCenter() {
    var keyword by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .widthIn(max = 520.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding)
    ) {
        DesktopToolbarIconButton(resource = Res.drawable.arrow_back_24px)
        DesktopToolbarIconButton(resource = Res.drawable.chevron_right_24px)
        DesktopSearchField(
            value = keyword,
            onValueChange = { keyword = it },
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * 顶部栏搜索输入框，复用项目内的 XyEdit 组件。
 */
@Composable
private fun DesktopSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = DesktopTitleBarColors.current

    XyEdit(
        text = value,
        onChange = onValueChange,
        modifier = modifier
            .height(XyTheme.dimens.itemHeight * 0.8f)
            .border(
                width = 1.dp,
                color = colors.outline,
                shape = RoundedCornerShape(XyTheme.dimens.corner)
            )
            .clip(RoundedCornerShape(XyTheme.dimens.corner)),
        backgroundColor = colors.searchBackground,
        hint = stringResource(Res.string.search_music_album_artist),
        hintColor = colors.foregroundVariant,
        paddingValues = PaddingValues(),
        singleLine = true,
        leadingContent = {
            Image(
                painter = painterResource(Res.drawable.search_24px),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
    )
}

/**
 * 顶部栏右侧操作区，展示当前数据源信息和窗口控制按钮。
 */
@Composable
private fun DesktopTitleActions(
    isMaximized: Boolean,
    onMinimize: () -> Unit,
    onToggleMaximize: () -> Unit,
    onClose: () -> Unit
) {
    val dataSourceManager: DataSourceManager = remember { KoinPlatform.getKoin().get() }
    val db: LocalDatabaseClient = remember { KoinPlatform.getKoin().get() }
    val musicController: MusicCommonController = remember { KoinPlatform.getKoin().get() }
    val coroutineScope = rememberCoroutineScope()
    var ifShowConnectionMenu by remember { mutableStateOf(false) }
    val connectionList by db.connectionConfigDao.selectAllDataFlow().collectAsState(initial = emptyList())
    val noConnectionSelected = stringResource(Res.string.no_connection_selected)
    val currentConnectionId = dataSourceManager.getConnectionId()
    val currentDataSource = remember(connectionList, currentConnectionId, dataSourceManager.dataSourceType) {
        readCurrentDataSourceInfo(
            connectionList = connectionList,
            currentConnectionId = currentConnectionId,
            dataSourceManager = dataSourceManager,
            fallbackTitle = noConnectionSelected
        )
    }
    val colors = DesktopTitleBarColors.current

    Row(
        modifier = Modifier.padding(end = XyTheme.dimens.outerHorizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding)
    ) {
        Box {
            Row(
                modifier = Modifier
                    .height(XyTheme.dimens.itemHeight * .8f)
                    .clip(RoundedCornerShape(XyTheme.dimens.corner))
                    .clickable { ifShowConnectionMenu = true }
                    .padding(
                        horizontal = XyTheme.dimens.innerHorizontalPadding,
                        vertical = XyTheme.dimens.innerVerticalPadding
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding)
            ) {
                Image(
                    painter = painterResource(currentDataSource.iconRes),
                    contentDescription = null,
                )
                XyText(
                    text = currentDataSource.title,
                    color = colors.foreground,
                )
                Icon(
                    painter = painterResource(Res.drawable.keyboard_arrow_down_24px),
                    contentDescription = stringResource(Res.string.open_add_or_switch_data_sources),
                    tint = colors.foreground,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { ifShowConnectionMenu = true }
                )
            }
            XyDropdownMenu(
                offset = DpOffset(0.dp, XyTheme.dimens.outerVerticalPadding / 2),
                onIfShowMenu = { ifShowConnectionMenu },
                onSetIfShowMenu = { ifShowConnectionMenu = it },
                modifier = Modifier.width(220.dp),
                itemDataList = connectionList.map { connection ->
                    MenuItemDefaultData(
                        title = connection.name,
                        leadingIcon = {
                            if (currentConnectionId == connection.id) {
                                Icon(
                                    painter = painterResource(Res.drawable.check_24px),
                                    contentDescription = connection.name + stringResource(Res.string.connection_link),
                                    tint = colors.foreground,
                                )
                            }
                        },
                        trailingIcon = {
                            Image(
                                painter = painterResource(connection.type.img),
                                contentDescription = connection.name + stringResource(Res.string.icon),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        onClick = {
                            coroutineScope.launch {
                                ifShowConnectionMenu = false
                                DataSourceChangeUtils.changeDataSource(
                                    connectionConfig = connection,
                                    dataSourceManager = dataSourceManager,
                                    musicController = musicController
                                )
                            }
                        }
                    )
                }
            )
        }

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(XyTheme.dimens.corner))
                .background(Color.Transparent),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DesktopWindowControlButton(
                controlType = WindowControlType.Minimize,
                onClick = onMinimize
            )
            DesktopWindowControlButton(
                controlType = if (isMaximized) WindowControlType.Restore else WindowControlType.Maximize,
                onClick = onToggleMaximize
            )
            DesktopWindowControlButton(
                controlType = WindowControlType.Close,
                onClick = onClose,
                backgroundColor = colors.primary.copy(alpha = 0.16f),
                contentColor = colors.foreground
            )
        }
    }
}

/**
 * 标题栏中的通用小图标按钮，用于返回和前进操作。
 */
@Composable
private fun DesktopToolbarIconButton(resource: DrawableResource) {
    val colors = DesktopTitleBarColors.current

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(XyTheme.dimens.corner))
            .background(colors.iconButtonBackground)
            .border(
                width = 1.dp,
                color = colors.outline,
                shape = RoundedCornerShape(XyTheme.dimens.corner)
            )
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(resource),
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
    }
}

/**
 * 桌面端窗口控制按钮容器，负责承载最小化、最大化和关闭按钮。
 */
@Composable
private fun DesktopWindowControlButton(
    controlType: WindowControlType,
    onClick: () -> Unit,
    backgroundColor: Color = DesktopTitleBarColors.current.windowButtonBackground,
    contentColor: Color = DesktopTitleBarColors.current.foreground
) {
    Box(
        modifier = Modifier
            .size(width = 42.dp, height = 34.dp)
            .clip(RoundedCornerShape(XyTheme.dimens.corner))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        WindowControlGlyph(
            controlType = controlType,
            tint = contentColor,
            modifier = Modifier.size(12.dp)
        )
    }
}

/**
 * 使用 Canvas 绘制窗口控制按钮的几何图形。
 */
@Composable
private fun WindowControlGlyph(
    controlType: WindowControlType,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 1.4.dp.toPx()
        val inset = 1.5.dp.toPx()
        val left = inset
        val top = inset
        val right = size.width - inset
        val bottom = size.height - inset

        when (controlType) {
            WindowControlType.Minimize -> {
                drawLine(
                    color = tint,
                    start = Offset(left, bottom - inset),
                    end = Offset(right, bottom - inset),
                    strokeWidth = strokeWidth
                )
            }

            WindowControlType.Maximize -> {
                drawRect(
                    color = tint,
                    topLeft = Offset(left, top),
                    size = Size(right - left, bottom - top),
                    style = Stroke(width = strokeWidth)
                )
            }

            WindowControlType.Restore -> {
                val shift = 2.dp.toPx()
                drawRect(
                    color = tint,
                    topLeft = Offset(left + shift, top),
                    size = Size(right - left - shift, bottom - top - shift),
                    style = Stroke(width = strokeWidth)
                )
                drawRect(
                    color = tint,
                    topLeft = Offset(left, top + shift),
                    size = Size(right - left - shift, bottom - top - shift),
                    style = Stroke(width = strokeWidth)
                )
            }

            WindowControlType.Close -> {
                drawLine(
                    color = tint,
                    start = Offset(left, top),
                    end = Offset(right, bottom),
                    strokeWidth = strokeWidth
                )
                drawLine(
                    color = tint,
                    start = Offset(right, top),
                    end = Offset(left, bottom),
                    strokeWidth = strokeWidth
                )
            }
        }
    }
}

/**
 * 桌面端标题栏使用的窗口控制类型。
 */
private enum class WindowControlType {
    Minimize,
    Maximize,
    Restore,
    Close,
}

/**
 * 当前数据源在标题栏中的展示数据。
 */
private data class DataSourceTitleInfo(
    val title: String,
    val iconRes: DrawableResource,
)

/**
 * 标题栏组件使用的颜色集合，统一从 MaterialTheme.colors 派生。
 */
private data class DesktopTitleBarColors(
    val primary: Color,
    val background: Color,
    val searchBackground: Color,
    val chipBackground: Color,
    val iconButtonBackground: Color,
    val windowButtonBackground: Color,
    val foreground: Color,
    val foregroundVariant: Color,
    val outline: Color,
) {
    companion object {
        val current: DesktopTitleBarColors
            @Composable
            get() {
                val colors = MaterialTheme.colors
                return DesktopTitleBarColors(
                    primary = colors.primary,
                    background = colors.background,
                    searchBackground = colors.surface.copy(alpha = 0.9f),
                    chipBackground = colors.surface.copy(alpha = 0.92f),
                    iconButtonBackground = colors.surface,
                    windowButtonBackground = colors.onSurface.copy(alpha = 0.12f),
                    foreground = colors.onSurface,
                    foregroundVariant = colors.onSurface.copy(alpha = 0.68f),
                    outline = colors.onSurface.copy(alpha = 0.18f),
                )
            }
    }
}

/**
 * 从当前 DataSourceManager 中读取标题栏展示所需的数据源名称与图标。
 */
private fun readCurrentDataSourceInfo(
    connectionList: List<ConnectionConfig>,
    currentConnectionId: Long,
    dataSourceManager: DataSourceManager,
    fallbackTitle: String,
): DataSourceTitleInfo {
    val currentConnection = connectionList.firstOrNull { it.id == currentConnectionId }
    val dataSource = dataSourceManager.dataSourceType
    return DataSourceTitleInfo(
        title = currentConnection?.name ?: dataSource?.title ?: fallbackTitle,
        iconRes = currentConnection?.type?.img ?: dataSource?.img ?: Res.drawable.logo_new
    )
}
