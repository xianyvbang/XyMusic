package cn.xybbz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.WideNavigationRailDefaults
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.material3.WideNavigationRailValue
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.xybbz.startup.AppSessionState
import cn.xybbz.startup.SessionCoordinator
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import cn.xybbz.router.AlbumInfo
import cn.xybbz.router.NavigationState
import cn.xybbz.router.Navigator
import cn.xybbz.router.PlatformNavigationConfig
import cn.xybbz.router.jvmTopRouterDataList
import cn.xybbz.ui.components.AlertDialogObject
import cn.xybbz.ui.components.DesktopWindowTitleBar
import cn.xybbz.ui.components.JvmRightClickDropdownMenuComponent
import cn.xybbz.ui.components.MusicPlaylistItemComponent
import cn.xybbz.ui.components.show
import cn.xybbz.ui.ext.jvmHoverDebounceClickable
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.windows.DesktopTooltipBox
import cn.xybbz.ui.xy.LazyColumnNotComponent
import cn.xybbz.ui.xy.ModalSideSheetExtendComponent
import cn.xybbz.ui.xy.XyEdit
import cn.xybbz.ui.xy.XyIconButton
import cn.xybbz.ui.xy.XyImage
import cn.xybbz.ui.xy.XyRow
import cn.xybbz.ui.xy.XyText
import cn.xybbz.ui.xy.XyTextSubSmall
import cn.xybbz.viewmodel.SidebarPlaylistViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.getKoin
import org.koin.compose.viewmodel.koinViewModel
import xymusic.composeapp.generated.resources.Res
import xymusic.composeapp.generated.resources.add_24px
import xymusic.composeapp.generated.resources.background_image
import xymusic.composeapp.generated.resources.create_playlist
import xymusic.composeapp.generated.resources.empty_info
import xymusic.composeapp.generated.resources.logging_in
import xymusic.composeapp.generated.resources.login_exception_info
import xymusic.composeapp.generated.resources.login_failed
import xymusic.composeapp.generated.resources.menu_open_24px
import xymusic.composeapp.generated.resources.new_playlist
import xymusic.composeapp.generated.resources.no_playlists
import xymusic.composeapp.generated.resources.playlist
import xymusic.composeapp.generated.resources.songs_count_suffix
import xymusic.composeapp.generated.resources.warning_24px

val jvmRouterMenuWidth = 220.dp

private val DesktopLoginStatusActionHeight = 72.dp
private val DesktopLoginStatusErrorButtonSize = 44.dp
private val DesktopLoginStatusSheetMaxHeight = 400.dp
private const val DesktopLoginStatusSheetAnimationMillis = 220

@Composable
actual fun MainScreenScaffold(
    modifier: Modifier,
    navigationConfig: PlatformNavigationConfig,
    navigationState: NavigationState,
    navigator: Navigator,
    snackbarHost: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val koin = getKoin()
    val sessionCoordinator: SessionCoordinator = remember { koin.get() }
    val sidebarPlaylistViewModel = koinViewModel<SidebarPlaylistViewModel>()
    val coroutineScope = rememberCoroutineScope()
    val playlists by sidebarPlaylistViewModel.playlists.collectAsStateWithLifecycle()
    val sessionState by sessionCoordinator.state.collectAsStateWithLifecycle()
    val playlistTitle = stringResource(Res.string.playlist)
    val createPlaylist = stringResource(Res.string.create_playlist)
    val newPlaylist = stringResource(Res.string.new_playlist)
    val noPlaylistsText = stringResource(Res.string.no_playlists)
    val songsCountSuffix = stringResource(Res.string.songs_count_suffix)
    val loginFailedState = sessionState as? AppSessionState.LoginFailed
    val loginErrorHint = stringResource(loginFailedState?.errorHint ?: Res.string.empty_info)
    val loginErrorMessage = loginFailedState?.errorMessage.orEmpty()
    var playlistName by remember { mutableStateOf("") }
    var showLoginErrorSheet by remember { mutableStateOf(false) }
    val sidebarListState = rememberLazyListState()
    val loginLoading = sessionState is AppSessionState.Restoring || sessionState is AppSessionState.LoggingIn
    val loginFailed = loginFailedState != null
    val sidebarColors = DesktopSidebarColors.current
    val sidebarPanelShape = RoundedCornerShape(XyTheme.dimens.corner)
    // 一级导航栏状态：默认展开为带文字的宽导航栏，可切换为 96dp 图标栏
    val navigationRailState = rememberWideNavigationRailState(
        initialValue = WideNavigationRailValue.Expanded
    )
    // 目标展开状态驱动导航项图标位置，点击后立即切换
    val navigationRailExpanded =
        navigationRailState.targetValue == WideNavigationRailValue.Expanded
    // 当前一级路由，用于导航栏与歌单面板的选中态判断
    val currentRoute = navigator.state.backStacks[navigator.state.topLevelRoute]
        ?.lastOrNull()
        ?: navigator.state.topLevelRoute

    LaunchedEffect(loginLoading, loginFailed) {
        if (loginLoading || !loginFailed) {
            showLoginErrorSheet = false
        }
    }

    Box(modifier = modifier) {
        XyImage(
            modifier = Modifier.fillMaxSize(),
            model = XyTheme.brash.backgroundImageUri,
            contentDescription = stringResource(Res.string.background_image),
        )
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = snackbarHost,
            containerColor = if (XyTheme.brash.backgroundImageUri.isNullOrBlank()) {
                MaterialTheme.colorScheme.background
            } else {
                Color.Transparent
            },
        ) { paddingValues ->
            Column(modifier = Modifier.fillMaxSize()) {
                DesktopWindowTitleBar(navigator = navigator)

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
                    verticalAlignment = Alignment.Top,
                ) {
                    // 一级导航栏：Material3 宽导航栏，仅承载顶部导航
                    WideNavigationRail(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(
                                start = XyTheme.dimens.outerVerticalPadding,
                                top = XyTheme.dimens.outerVerticalPadding,
                                bottom = XyTheme.dimens.snackBarPlayerHeight +
                                        XyTheme.dimens.outerVerticalPadding
                            ),
                        state = navigationRailState,
                        shape = sidebarPanelShape,
                        colors = WideNavigationRailDefaults.colors(
                            containerColor = sidebarColors.panelBackground,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                        windowInsets = WindowInsets(0, 0, 0, 0),
                        header = {
                            // 展开 / 收起一级导航栏
                            XyIconButton(
                                onClick = {
                                    coroutineScope.launch { navigationRailState.toggle() }
                                }
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.menu_open_24px),
                                    contentDescription = null,
                                )
                            }
                        },
                    ) {
                        jvmTopRouterDataList.forEach { item ->
                            WideNavigationRailItem(
                                selected = currentRoute == item.route,
                                onClick = {
                                    if (item.route == navigationConfig.startRoute) {
                                        navigator.navigateToRoot(item.route)
                                    } else {
                                        navigator.navigate(route = item.route)
                                    }
                                },
                                icon = {
                                    Icon(
                                        painter = painterResource(item.icon),
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                    )
                                },
                                label = {
                                    Text(
                                        text = stringResource(item.title),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                },
                                railExpanded = navigationRailExpanded,
                            )
                        }
                    }

                    // 歌单独立面板：与一级导航栏并排，保留原有滚动与登录状态浮层
                    Box(
                        modifier = Modifier
                            .width(jvmRouterMenuWidth)
                            .fillMaxHeight()
                            .padding(end = XyTheme.dimens.contentPadding)
                    ) {
                        LazyColumnNotComponent(
                            modifier = Modifier
                                .fillMaxSize(),
                            state = sidebarListState,
                            contentPadding = PaddingValues(
                                start = XyTheme.dimens.outerVerticalPadding,
                                top = XyTheme.dimens.outerVerticalPadding,
                                end = XyTheme.dimens.outerVerticalPadding,
                                bottom = XyTheme.dimens.snackBarPlayerHeight +
                                        DesktopLoginStatusActionHeight +
                                        XyTheme.dimens.outerVerticalPadding * 2
                            ),
                            verticalArrangement = Arrangement.spacedBy(
                                XyTheme.dimens.contentPadding
                            ),
                        ) {
                            item(key = "playlist_panel") {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(sidebarPanelShape)
                                        .background(sidebarColors.panelBackground)
                                        .padding(vertical = XyTheme.dimens.outerVerticalPadding),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    XyRow {
                                        XyText(
                                            text = playlistTitle,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                        XyIconButton(
                                            onClick = {
                                                playlistName = newPlaylist + playlists.size
                                                AlertDialogObject(
                                                    title = createPlaylist,
                                                    content = {
                                                        XyEdit(
                                                            text = playlistName,
                                                            onChange = { playlistName = it }
                                                        )
                                                    },
                                                    onDismissRequest = {},
                                                    onConfirmation = {
                                                        coroutineScope.launch {
                                                            sidebarPlaylistViewModel.savePlaylist(
                                                                playlistName
                                                            )
                                                        }
                                                    }
                                                ).show()
                                            }
                                        ) {
                                            Icon(
                                                painter = painterResource(Res.drawable.add_24px),
                                                contentDescription = createPlaylist,
                                                tint = MaterialTheme.colorScheme.onSurface,
                                            )
                                        }
                                    }

                                    if (playlists.isEmpty()) {
                                        XyRow {
                                            XyTextSubSmall(
                                                text = noPlaylistsText,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    } else {
                                        playlists.forEach { playlist ->
                                            val selectedPlaylist = currentRoute is AlbumInfo &&
                                                    currentRoute.itemId == playlist.itemId &&
                                                    currentRoute.dataType == MusicDataTypeEnum.PLAYLIST
                                            MusicPlaylistItemComponent(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .pointerHoverIcon(PointerIcon.Hand),
                                                name = playlist.name,
                                                subordination = "${playlist.musicCount}${songsCountSuffix}",
                                                imgUrl = playlist.pic,
                                                backgroundColor = if (selectedPlaylist) {
                                                    sidebarColors.playlistSelectedBackground
                                                } else {
                                                    Color.Transparent
                                                },
                                                brush = null,
                                                onClick = {
                                                    navigator.navigate(
                                                        route = AlbumInfo(
                                                            itemId = playlist.itemId,
                                                            dataType = MusicDataTypeEnum.PLAYLIST
                                                        )
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        DesktopLoginStatusFloatingAction(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(
                                    start = XyTheme.dimens.outerVerticalPadding,
                                    end = XyTheme.dimens.outerVerticalPadding,
                                    bottom = XyTheme.dimens.snackBarPlayerHeight +
                                            XyTheme.dimens.outerVerticalPadding
                            ),
                            loginLoading = loginLoading,
                            loginFailed = loginFailed,
                            errorHint = loginErrorHint,
                            onErrorClick = {
                                showLoginErrorSheet = true
                            }
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        content(PaddingValues())
                    }
                }
            }
        }
        DesktopLoginErrorSideSheet(
            show = showLoginErrorSheet,
            errorHint = loginErrorHint,
            errorMessage = loginErrorMessage,
            onClose = { showLoginErrorSheet = false }
        )
        JvmRightClickDropdownMenuComponent()
    }
}

@Composable
private fun DesktopLoginStatusFloatingAction(
    modifier: Modifier = Modifier,
    loginLoading: Boolean,
    loginFailed: Boolean,
    errorHint: String,
    onErrorClick: () -> Unit,
) {
    if (!loginLoading && !loginFailed) {
        return
    }

    val loggingInText = stringResource(Res.string.logging_in)
    val loginFailedText = stringResource(Res.string.login_failed)
    val errorInteractionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = jvmRouterMenuWidth),
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
    ) {
        if (loginLoading) {
            Column(
                modifier = Modifier
                    .height(DesktopLoginStatusActionHeight)
                    .fillMaxWidth()
                    .padding(
                        horizontal = XyTheme.dimens.contentPadding,
                        vertical = XyTheme.dimens.outerVerticalPadding
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                XyTextSubSmall(
                    text = loggingInText,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = XyTheme.dimens.innerVerticalPadding)
                        .height(4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                )
            }
        } else {
            DesktopTooltipBox(tooltip = loginFailedText) {
                Row(
                    modifier = Modifier
                        .height(DesktopLoginStatusActionHeight)
                        .fillMaxWidth()
                        .jvmHoverDebounceClickable(
                            interactionSource = errorInteractionSource,
                            indication = null,
                            onClick = onErrorClick
                        )
                        .padding(horizontal = XyTheme.dimens.contentPadding),
                    horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.innerVerticalPadding),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.warning_24px),
                        contentDescription = loginFailedText,
                        modifier = Modifier.size(DesktopLoginStatusErrorButtonSize),
                        tint = Color.Red,
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        XyTextSubSmall(
                            text = loginFailedText,
                            color = Color.Red,
                            maxLines = 1,
                        )
                        if (errorHint.isNotBlank()) {
                            XyTextSubSmall(
                                text = errorHint,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DesktopLoginErrorSideSheet(
    show: Boolean,
    errorHint: String,
    errorMessage: String,
    onClose: () -> Unit,
) {
    ModalSideSheetExtendComponent(
        modifier = Modifier.fillMaxSize(),
        sheetWidth = 380.dp,
        sheetMaxWidth = 480.dp,
        containerColor = MaterialTheme.colorScheme.surface,
        animationDurationMillis = DesktopLoginStatusSheetAnimationMillis,
        useDialog = false,
        onIfDisplay = { show },
        onClose = { onClose() },
        titleText = stringResource(Res.string.login_exception_info),
        titleSub = null,
        titleTailContent = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = DesktopLoginStatusSheetMaxHeight)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = XyTheme.dimens.outerHorizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding),
        ) {
            XyText(
                text = errorHint,
                overflow = TextOverflow.Visible,
                maxLines = Int.MAX_VALUE,
            )

            if (errorMessage.isNotBlank()) {
                XyTextSubSmall(
                    text = errorMessage,
                    overflow = TextOverflow.Visible,
                    maxLines = Int.MAX_VALUE,
                )
            }
        }
    }
}

/**
 * JVM 左侧栏使用的颜色集合。
 *
 * 暗色主题保持桌面 HTML 原型的侧栏层级；浅色主题从 MaterialTheme 派生，
 * 避免为了一个桌面专属区域改动全局 ColorScheme。
 */
private data class DesktopSidebarColors(
    val background: Color,
    val panelBackground: Color,
    val playlistSelectedBackground: Color,
) {
    companion object {
        val current: DesktopSidebarColors
            @Composable
            get() {
                val colorScheme = MaterialTheme.colorScheme
                return if (XyTheme.configs.isDarkTheme) {
                    DesktopSidebarColors(
                        background = Color(0xFF111111),
                        panelBackground = Color(0xFF242424),
                        playlistSelectedBackground = Color.White.copy(alpha = 0.05f),
                    )
                } else {
                    DesktopSidebarColors(
                        background = colorScheme.background,
                        panelBackground = colorScheme.surfaceContainerLowest,
                        playlistSelectedBackground = colorScheme.primary.copy(alpha = 0.08f),
                    )
                }
            }
    }
}
