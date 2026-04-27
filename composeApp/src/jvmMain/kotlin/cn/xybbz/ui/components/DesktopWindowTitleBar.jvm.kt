package cn.xybbz.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.enums.LoginType
import cn.xybbz.common.enums.ConnectionUiType
import cn.xybbz.common.enums.img
import cn.xybbz.common.utils.DataSourceChangeUtils
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.localdata.config.LocalDatabaseClient
import cn.xybbz.localdata.data.connection.ConnectionConfig
import cn.xybbz.router.Connection
import cn.xybbz.router.Download
import cn.xybbz.router.Navigator
import cn.xybbz.router.Setting
import cn.xybbz.ui.popup.MenuItemDefaultData
import cn.xybbz.ui.popup.XyDropdownMenu
import cn.xybbz.ui.screens.jvmRouterMenuWidth
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.windows.DesktopInteractiveHitTestOwner
import cn.xybbz.ui.windows.DesktopWindowTitleBar as UiDesktopWindowTitleBar
import cn.xybbz.ui.xy.XyEdit
import cn.xybbz.ui.xy.XyText
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.getKoin
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.add_circle_24px
import xymusic_kmp.composeapp.generated.resources.add_connection
import xymusic_kmp.composeapp.generated.resources.app_icon_info
import xymusic_kmp.composeapp.generated.resources.app_name
import xymusic_kmp.composeapp.generated.resources.arrow_back_24px
import xymusic_kmp.composeapp.generated.resources.check_24px
import xymusic_kmp.composeapp.generated.resources.chevron_right_24px
import xymusic_kmp.composeapp.generated.resources.connection_link
import xymusic_kmp.composeapp.generated.resources.download_24px
import xymusic_kmp.composeapp.generated.resources.download_list
import xymusic_kmp.composeapp.generated.resources.icon
import xymusic_kmp.composeapp.generated.resources.keyboard_arrow_down_24px
import xymusic_kmp.composeapp.generated.resources.logo_new
import xymusic_kmp.composeapp.generated.resources.no_connection_selected
import xymusic_kmp.composeapp.generated.resources.open_settings_page_button
import xymusic_kmp.composeapp.generated.resources.open_add_or_switch_data_sources
import xymusic_kmp.composeapp.generated.resources.refresh_24px
import xymusic_kmp.composeapp.generated.resources.refresh_login
import xymusic_kmp.composeapp.generated.resources.search_24px
import xymusic_kmp.composeapp.generated.resources.search_music_album_artist
import xymusic_kmp.composeapp.generated.resources.settings_24px

/**
 * 桌面端主标题栏。
 * 负责组合品牌区、搜索区、数据源菜单以及窗口控制按钮。
 */
@Composable
fun DesktopWindowTitleBar(navigator: Navigator) {
    UiDesktopWindowTitleBar(
        backgroundColor = DesktopTitleBarColors.current.background,
        front = { _ ->
            DesktopTitleBrand()
        },
        middle = { hitTestOwner ->
            DesktopTitleCenter(
                navigator = navigator,
                hitTestOwner = hitTestOwner
            )
        },
        beforeWindowControls = { hitTestOwner ->
            DesktopTitleActions(
                navigator = navigator,
                hitTestOwner = hitTestOwner
            )
        }
    )
}

/**
 * 标题栏左侧品牌区，展示应用图标与名称。
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
 * 标题栏中间区域，承载返回、前进和搜索输入框。
 */
@Composable
private fun DesktopTitleCenter(
    navigator: Navigator,
    hitTestOwner: DesktopInteractiveHitTestOwner,
) {
    var keyword by remember { mutableStateOf("") }
    val currentStack = navigator.state.backStacks[navigator.state.topLevelRoute]
    val canGoBack = navigator.state.topLevelRoute != navigator.state.startRoute ||
        currentStack?.lastOrNull() != navigator.state.topLevelRoute

    Row(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .widthIn(max = 520.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding)
    ) {
        DesktopToolbarIconButton(
            resource = Res.drawable.arrow_back_24px,
            enabled = canGoBack,
            onClick = navigator::goBack,
            modifier = Modifier.desktopTitleBarHitTarget(hitTestOwner, "BackButton"),
        )
        DesktopSearchField(
            value = keyword,
            onValueChange = { keyword = it },
            modifier = Modifier
                .weight(1f)
                .desktopTitleBarHitTarget(hitTestOwner, "SearchField")
        )
    }
}

/**
 * 标题栏搜索框。
 * 目前沿用桌面原型交互，只维护本地输入态。
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
            Icon(
                painter = painterResource(Res.drawable.search_24px),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
    )
}

/**
 * 标题栏右侧操作区。
 * 包含当前数据源展示、切换/创建连接菜单和窗口控制按钮。
 */
@Composable
private fun DesktopTitleActions(
    navigator: Navigator,
    hitTestOwner: DesktopInteractiveHitTestOwner,
) {
    val koin = getKoin()
    val dataSourceManager: DataSourceManager = remember { koin.get() }
    val db: LocalDatabaseClient = remember { koin.get() }
    val musicController: MusicCommonController = remember { koin.get() }
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
                    .desktopTitleBarHitTarget(hitTestOwner, "ConnectionMenu")
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
                itemDataList = buildList {
                    // 将“创建连接”放在菜单首项，方便从桌面标题栏直接进入新增流程。
                    add(
                        MenuItemDefaultData(
                            title = stringResource(Res.string.add_connection),
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(Res.drawable.chevron_right_24px),
                                    contentDescription = stringResource(Res.string.add_connection),
                                    tint = colors.foreground,
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    painter = painterResource(Res.drawable.add_circle_24px),
                                    contentDescription = stringResource(Res.string.add_connection),
                                    tint = colors.foreground,
                                )
                            },
                            onClick = {
                                ifShowConnectionMenu = false
                                navigator.navigate(Connection(connectionUiType = ConnectionUiType.ADD_CONNECTION))
                            }
                        )
                    )

                    addAll(
                        connectionList.map { connection ->
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
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DesktopToolbarIconButton(
                resource = Res.drawable.download_24px,
                enabled = true,
                onClick = { navigator.navigate(Download) },
                modifier = Modifier.desktopTitleBarHitTarget(hitTestOwner, "DownloadButton"),
                contentDescription = stringResource(Res.string.download_list)
            )
            DesktopToolbarIconButton(
                resource = Res.drawable.refresh_24px,
                enabled = true,
                onClick = {
                    coroutineScope.launch {
                        dataSourceManager.serverLogin(
                            LoginType.API,
                            db.connectionConfigDao.selectConnectionConfig()
                        )
                    }
                },
                modifier = Modifier.desktopTitleBarHitTarget(hitTestOwner, "RefreshButton"),
                contentDescription = stringResource(Res.string.refresh_login)
            )
            DesktopToolbarIconButton(
                resource = Res.drawable.settings_24px,
                enabled = true,
                onClick = { navigator.navigate(Setting) },
                modifier = Modifier.desktopTitleBarHitTarget(hitTestOwner, "SettingsButton"),
                contentDescription = stringResource(Res.string.open_settings_page_button)
            )
        }
    }
}

/**
 * 标题栏中的通用小图标按钮，用于导航等轻量操作。
 */
@Composable
private fun DesktopToolbarIconButton(
    resource: DrawableResource,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    val colors = DesktopTitleBarColors.current
    val backgroundColor = if (enabled) {
        colors.iconButtonBackground
    } else {
        colors.iconButtonBackground.copy(alpha = 0.55f)
    }
    val borderColor = if (enabled) {
        colors.outline
    } else {
        colors.outline.copy(alpha = 0.45f)
    }
    val iconTint = if (enabled) {
        colors.foreground
    } else {
        colors.foreground.copy(alpha = 0.45f)
    }

    Box(
        modifier = modifier
            .size(36.dp)
            .clip(RoundedCornerShape(XyTheme.dimens.corner))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(XyTheme.dimens.corner)
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(resource),
            contentDescription = contentDescription,
            modifier = Modifier.size(18.dp),
            tint = iconTint
        )
    }
}

/**
 * 当前数据源在标题栏中的展示数据。
 */
private data class DataSourceTitleInfo(
    val title: String,
    val iconRes: DrawableResource,
)

/**
 * 标题栏使用的颜色集合，统一从当前 MaterialTheme 派生。
 */
private data class DesktopTitleBarColors(
    val background: Color,
    val searchBackground: Color,
    val iconButtonBackground: Color,
    val foreground: Color,
    val foregroundVariant: Color,
    val outline: Color,
) {
    companion object {
        val current: DesktopTitleBarColors
            @Composable
            get() = DesktopTitleBarColors(
                background = MaterialTheme.colorScheme.background,
                searchBackground = MaterialTheme.colorScheme.surfaceContainerLowest,
                iconButtonBackground = MaterialTheme.colorScheme.surfaceContainerLowest,
                foreground = MaterialTheme.colorScheme.onSurface,
                foregroundVariant = MaterialTheme.colorScheme.onSurfaceVariant,
                outline = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f),
            )
    }
}

/**
 * 读取标题栏需要展示的数据源标题和图标。
 */
private fun readCurrentDataSourceInfo(
    connectionList: List<ConnectionConfig>,
    currentConnectionId: Long,
    dataSourceManager: DataSourceManager,
    fallbackTitle: String,
): DataSourceTitleInfo {
    // 优先展示当前连接配置；若尚未选中连接，则退回到数据源类型或兜底标题。
    val currentConnection = connectionList.firstOrNull { it.id == currentConnectionId }
    val dataSource = dataSourceManager.dataSourceType
    return DataSourceTitleInfo(
        title = currentConnection?.name ?: dataSource?.title ?: fallbackTitle,
        iconRes = currentConnection?.type?.img ?: dataSource?.img ?: Res.drawable.logo_new
    )
}

private fun Modifier.desktopTitleBarHitTarget(
    owner: DesktopInteractiveHitTestOwner,
    targetId: String,
): Modifier = onGloballyPositioned { coordinates ->
    owner.updateBounds(targetId, coordinates.boundsInWindow())
}
