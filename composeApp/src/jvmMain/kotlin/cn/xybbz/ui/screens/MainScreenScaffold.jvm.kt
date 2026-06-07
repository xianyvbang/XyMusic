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
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.xybbz.api.client.DataSourceManager
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
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.add_24px
import xymusic_kmp.composeapp.generated.resources.background_image
import xymusic_kmp.composeapp.generated.resources.create_playlist
import xymusic_kmp.composeapp.generated.resources.logging_in
import xymusic_kmp.composeapp.generated.resources.login_exception_info
import xymusic_kmp.composeapp.generated.resources.login_failed
import xymusic_kmp.composeapp.generated.resources.new_playlist
import xymusic_kmp.composeapp.generated.resources.no_playlists
import xymusic_kmp.composeapp.generated.resources.playlist
import xymusic_kmp.composeapp.generated.resources.songs_count_suffix
import xymusic_kmp.composeapp.generated.resources.warning_24px

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
    val dataSourceManager: DataSourceManager = remember { koin.get() }
    val sidebarPlaylistViewModel = koinViewModel<SidebarPlaylistViewModel>()
    val coroutineScope = rememberCoroutineScope()
    val playlists by sidebarPlaylistViewModel.playlists.collectAsStateWithLifecycle()
    val autoLoginRunning by dataSourceManager.autoLoginRunning.collectAsStateWithLifecycle()
    val playlistTitle = stringResource(Res.string.playlist)
    val createPlaylist = stringResource(Res.string.create_playlist)
    val newPlaylist = stringResource(Res.string.new_playlist)
    val noPlaylistsText = stringResource(Res.string.no_playlists)
    val songsCountSuffix = stringResource(Res.string.songs_count_suffix)
    val loginErrorHint = stringResource(dataSourceManager.errorHint)
    var playlistName by remember { mutableStateOf("") }
    var showLoginErrorSheet by remember { mutableStateOf(false) }
    val sidebarListState = rememberLazyListState()
    val loginLoading = dataSourceManager.loading || autoLoginRunning
    val loginFailed = dataSourceManager.ifLoginError
    val sidebarColors = DesktopSidebarColors.current
    val sidebarPanelShape = RoundedCornerShape(XyTheme.dimens.corner)

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
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(
                        modifier = Modifier
                            .width(jvmRouterMenuWidth)
                            .fillMaxSize()
//                            .background(sidebarColors.background)
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
                            item(key = "navigation_panel") {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(sidebarPanelShape)
                                        .background(sidebarColors.panelBackground)
                                        .padding(vertical = XyTheme.dimens.outerVerticalPadding),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    jvmTopRouterDataList.forEach { item ->
                                        DesktopNavigationItem(
                                            item = item,
                                            selected = (navigator.state.backStacks[navigator.state.topLevelRoute]
                                                ?.lastOrNull()
                                                ?: navigator.state.topLevelRoute) == item.route,
                                            colors = sidebarColors,
                                            onClick = {
                                                if (item.route == navigationConfig.startRoute) {
                                                    navigator.navigateToRoot(item.route)
                                                } else {
                                                    navigator.navigate(route = item.route)
                                                }
                                            },
                                        )
                                    }
                                }
                            }

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
                                            val currentRoute = navigator.state.backStacks[navigator.state.topLevelRoute]
                                                ?.lastOrNull()
                                                ?: navigator.state.topLevelRoute
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
            dataSourceManager = dataSourceManager,
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
    dataSourceManager: DataSourceManager,
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
                text = stringResource(dataSourceManager.errorHint),
                overflow = TextOverflow.Visible,
                maxLines = Int.MAX_VALUE,
            )

            if (dataSourceManager.errorMessage.isNotBlank()) {
                XyTextSubSmall(
                    text = dataSourceManager.errorMessage,
                    overflow = TextOverflow.Visible,
                    maxLines = Int.MAX_VALUE,
                )
            }
        }
    }
}

@Composable
private fun DesktopNavigationItem(
    item: JvmTopRouterData,
    selected: Boolean,
    colors: DesktopSidebarColors,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val backgroundColor = if (selected || hovered) {
        colors.navigationSelectedBackground
    } else {
        Color.Transparent
    }
    val contentColor = MaterialTheme.colorScheme.onSurface

    XyRow(
        modifier = Modifier
            .clip(RoundedCornerShape(XyTheme.dimens.corner))
            .background(backgroundColor)
            .jvmHoverDebounceClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.innerVerticalPadding),
    ) {
        Icon(
            painter = painterResource(item.icon),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = contentColor,
        )
        XyText(
            text = stringResource(item.title),
            color = contentColor,
            style = MaterialTheme.typography.bodyLarge,
        )
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
    val navigationSelectedBackground: Color,
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
                        navigationSelectedBackground = Color(0xFF363636),
                        playlistSelectedBackground = Color.White.copy(alpha = 0.05f),
                    )
                } else {
                    DesktopSidebarColors(
                        background = colorScheme.background,
                        panelBackground = colorScheme.surfaceContainerLowest,
                        navigationSelectedBackground = colorScheme.primary.copy(alpha = 0.10f),
                        playlistSelectedBackground = colorScheme.primary.copy(alpha = 0.08f),
                    )
                }
            }
    }
}
