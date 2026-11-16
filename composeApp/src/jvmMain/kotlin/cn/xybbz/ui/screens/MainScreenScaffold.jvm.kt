package cn.xybbz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.xybbz.startup.AppSessionState
import cn.xybbz.startup.SessionCoordinator
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import cn.xybbz.router.AlbumInfo
import cn.xybbz.router.JvmTopRouterData
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
import cn.xybbz.ui.xy.ModalSideSheetExtendComponent
import cn.xybbz.ui.xy.XyEdit
import cn.xybbz.ui.xy.XyIconButton
import cn.xybbz.ui.xy.XyRow
import cn.xybbz.ui.xy.XyText
import cn.xybbz.ui.xy.XyTextSub
import cn.xybbz.ui.xy.XyTextSubSmall
import cn.xybbz.viewmodel.SidebarPlaylistViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.getKoin
import org.koin.compose.viewmodel.koinViewModel
import xymusic.composeapp.generated.resources.Res
import xymusic.composeapp.generated.resources.add_24px
import xymusic.composeapp.generated.resources.create_playlist
import xymusic.composeapp.generated.resources.empty_info
import xymusic.composeapp.generated.resources.logging_in
import xymusic.composeapp.generated.resources.login_exception_info
import xymusic.composeapp.generated.resources.login_failed
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
    val loginLoading = sessionState is AppSessionState.Restoring || sessionState is AppSessionState.LoggingIn
    val loginFailed = loginFailedState != null
    val sidebarColors = XyTheme.extendedColors
    val sidebarPanelShape = RoundedCornerShape(XyTheme.dimens.corner)

    // 侧栏当前路由，用于一级导航判断选中态
    val currentRoute = navigator.state.backStacks[navigator.state.topLevelRoute]
        ?.lastOrNull() ?: navigator.state.topLevelRoute

    LaunchedEffect(loginLoading, loginFailed) {
        if (loginLoading || !loginFailed) {
            showLoginErrorSheet = false
        }
    }

    Box(modifier = modifier) {
        MainScreenBackgroundLayer(modifier = Modifier.fillMaxSize())
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
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(
                        modifier = Modifier
                            .width(jvmRouterMenuWidth)
                            .fillMaxHeight()
                            // 与顶部标题栏留出间距，避免侧边栏紧贴标题栏
                            .padding(
                                top = XyTheme.dimens.contentPadding,
                                end = XyTheme.dimens.contentPadding
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(sidebarPanelShape)
                                .background(sidebarColors.sidebarBackground)
                                .padding(bottom = DesktopLoginStatusActionHeight)
                                .verticalScroll(rememberScrollState()),
                        ) {
                            // 一级导航：图标与文字横排，选中项左侧带细指示条并填充背景
                            // 链接管理与设置在 jvmTopRouterDataList 末尾，与这里同列排列
                            jvmTopRouterDataList.forEach { item ->
                                DesktopSidebarNavigationItem(
                                    item = item,
                                    selected = currentRoute == item.route,
                                    selectedBackground = sidebarColors.sidebarNavigationSelectedBackground,
                                    onClick = {
                                        if (item.route == navigationConfig.startRoute) {
                                            navigator.navigateToRoot(item.route)
                                        } else {
                                            navigator.navigate(route = item.route)
                                        }
                                    },
                                )
                            }

                            // 歌单分组标题
                            XyRow(
                                modifier = Modifier.padding(
                                    start = 12.dp,
                                    top = XyTheme.dimens.contentPadding,
                                    bottom = XyTheme.dimens.innerVerticalPadding,
                                ),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                XyTextSubSmall(
                                    text = playlistTitle,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f),
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
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }

                            if (playlists.isEmpty()) {
                                XyRow(
                                    modifier = Modifier.padding(
                                        start = 12.dp
                                    ),
                                ) {
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
                                            .padding(horizontal = XyTheme.dimens.innerVerticalPadding)
                                            .pointerHoverIcon(PointerIcon.Hand),
                                        name = playlist.name,
                                        subordination = "${playlist.musicCount}${songsCountSuffix}",
                                        imgUrl = playlist.pic,
                                        backgroundColor = if (selectedPlaylist) {
                                            sidebarColors.sidebarPlaylistSelectedBackground
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
                        tint = XyTheme.extendedColors.danger,
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        XyTextSubSmall(
                            text = loginFailedText,
                            color = XyTheme.extendedColors.danger,
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
 * JVM 左侧栏一级导航项。
 *
 * 图标与文字横排，选中时整行填充背景并在左侧显示细指示条。
 */
@Composable
private fun DesktopSidebarNavigationItem(
    item: JvmTopRouterData,
    selected: Boolean,
    selectedBackground: Color,
    onClick: () -> Unit,
) {
    // 统一的交互源，同时用于悬停检测与点击
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val backgroundColor = when {
        selected -> selectedBackground
        hovered -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
        else -> Color.Transparent
    }
    val itemShape = RoundedCornerShape(XyTheme.dimens.corner)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = XyTheme.dimens.innerVerticalPadding, vertical = 2.dp)
            .clip(itemShape)
            .background(backgroundColor)
            .jvmHoverDebounceClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 12.dp,
                    top = XyTheme.dimens.outerVerticalPadding,
                    end = XyTheme.dimens.innerVerticalPadding,
                    bottom = XyTheme.dimens.outerVerticalPadding,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
        ) {
            Icon(
                painter = painterResource(item.icon),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurface,
            )
            XyText(
                text = stringResource(item.title),
                // 选中时加粗并加深文字颜色，未选中保持常规字重与次要颜色
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // 选中状态左侧指示条
        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(3.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp))
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
    }
}
