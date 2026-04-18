package cn.xybbz.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import cn.xybbz.router.AlbumInfo
import cn.xybbz.router.JvmTopRouterData
import cn.xybbz.router.NavigationState
import cn.xybbz.router.Navigator
import cn.xybbz.router.PlatformNavigationConfig
import cn.xybbz.router.jvmTopRouterDataList
import cn.xybbz.ui.components.AlertDialogObject
import cn.xybbz.ui.components.DesktopWindowTitleBar
import cn.xybbz.ui.components.MusicPlaylistItemComponent
import cn.xybbz.ui.components.show
import cn.xybbz.ui.ext.debounceClickable
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyEdit
import cn.xybbz.ui.xy.XyIconButton
import cn.xybbz.ui.xy.XyRow
import cn.xybbz.ui.xy.XyText
import cn.xybbz.ui.xy.XyTextSubSmall
import cn.xybbz.viewmodel.SidebarPlaylistViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.add_24px
import xymusic_kmp.composeapp.generated.resources.create_playlist
import xymusic_kmp.composeapp.generated.resources.new_playlist
import xymusic_kmp.composeapp.generated.resources.no_playlists
import xymusic_kmp.composeapp.generated.resources.playlist
import xymusic_kmp.composeapp.generated.resources.songs_count_suffix

val jvmRouterMenuWidth = 220.dp

@Composable
actual fun MainScreenScaffold(
    modifier: Modifier,
    navigationConfig: PlatformNavigationConfig,
    navigationState: NavigationState,
    navigator: Navigator,
    snackbarHost: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val sidebarPlaylistViewModel = koinViewModel<SidebarPlaylistViewModel>()
    val coroutineScope = rememberCoroutineScope()
    val playlists by sidebarPlaylistViewModel.playlists.collectAsStateWithLifecycle()
    val playlistTitle = stringResource(Res.string.playlist)
    val createPlaylist = stringResource(Res.string.create_playlist)
    val newPlaylist = stringResource(Res.string.new_playlist)
    val noPlaylistsText = stringResource(Res.string.no_playlists)
    val songsCountSuffix = stringResource(Res.string.songs_count_suffix)
    var playlistName by remember { mutableStateOf("") }
    val sidebarListState = rememberLazyListState()
    val sidebarInteractionSource = remember { MutableInteractionSource() }
    val sidebarHovered by sidebarInteractionSource.collectIsHoveredAsState()
    val showScrollbar = sidebarHovered || sidebarListState.isScrollInProgress

    LaunchedEffect(playlists.isEmpty()) {
        if (playlists.isEmpty()) {
            sidebarPlaylistViewModel.refreshPlaylists()
        }
    }

    Scaffold(
        modifier = modifier,
//        snackbarHost = snackbarHost
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
                        .hoverable(interactionSource = sidebarInteractionSource)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(end = XyTheme.dimens.contentPadding),
                        state = sidebarListState,
                        contentPadding = PaddingValues(XyTheme.dimens.outerVerticalPadding),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        items(jvmTopRouterDataList) { item ->
                            DesktopNavigationItem(
                                item = item,
                                selected = navigator.state.topLevelRoute == item.route,
                                onClick = { navigator.navigate(route = item.route) },
                            )
                        }

                        item(key = "playlist_header") {
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
                        }
                        if (playlists.isEmpty()) {
                            item(key = "playlist_empty") {
                                XyRow {
                                    XyTextSubSmall(
                                        text = noPlaylistsText,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }

                            }
                        } else {
                            items(playlists, key = { item -> item.itemId }) { playlist ->
                                MusicPlaylistItemComponent(
                                    modifier = Modifier.fillMaxWidth(),
                                    name = playlist.name,
                                    subordination = "${playlist.musicCount}${songsCountSuffix}",
                                    imgUrl = playlist.pic,
                                    backgroundColor = Color.Transparent,
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
                    SidebarVerticalScrollbar(
                        visible = showScrollbar,
                        modifier = Modifier.align(Alignment.CenterEnd),
                        adapter = rememberScrollbarAdapter(scrollState = sidebarListState),
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    content(PaddingValues())
                }
            }
        }
    }
}

@Composable
private fun SidebarVerticalScrollbar(
    visible: Boolean,
    modifier: Modifier = Modifier,
    adapter: androidx.compose.foundation.v2.ScrollbarAdapter,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier,
    ) {
        VerticalScrollbar(
            modifier = Modifier
                .fillMaxHeight()
                .width(6.dp)
                .padding(vertical = XyTheme.dimens.outerVerticalPadding),
            adapter = adapter,
        )
    }
}

@Composable
private fun DesktopNavigationItem(
    item: JvmTopRouterData,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val backgroundColor = if (selected || hovered) {
        MaterialTheme.colorScheme.surfaceContainerLowest
    } else {
        Color.Transparent
    }
    val contentColor = MaterialTheme.colorScheme.onSurface

    XyRow(
        modifier = Modifier
            .clip(RoundedCornerShape(XyTheme.dimens.corner))
            .background(backgroundColor)
            .hoverable(interactionSource = interactionSource)
            .debounceClickable(
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
