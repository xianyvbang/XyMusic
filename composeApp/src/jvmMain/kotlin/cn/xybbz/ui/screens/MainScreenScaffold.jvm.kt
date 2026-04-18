package cn.xybbz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import cn.xybbz.ui.components.DesktopWindowTitleBar
import cn.xybbz.ui.components.MusicPlaylistItemComponent
import cn.xybbz.ui.xy.XyText
import cn.xybbz.ui.xy.XyTextSubSmall
import cn.xybbz.viewmodel.HomeViewModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.no_playlists
import xymusic_kmp.composeapp.generated.resources.playlist
import xymusic_kmp.composeapp.generated.resources.songs_count_suffix

val jvmRouterMenuWidth = 180.dp

@Composable
actual fun MainScreenScaffold(
    modifier: Modifier,
    navigationConfig: PlatformNavigationConfig,
    navigationState: NavigationState,
    navigator: Navigator,
    snackbarHost: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val homeViewModel = koinViewModel<HomeViewModel>()
    val playlists by homeViewModel.homeDataRepository.playlists.collectAsStateWithLifecycle()
    val playlistTitle = stringResource(Res.string.playlist)
    val noPlaylistsText = stringResource(Res.string.no_playlists)
    val songsCountSuffix = stringResource(Res.string.songs_count_suffix)

    LaunchedEffect(playlists.isEmpty()) {
        if (playlists.isEmpty()) {
            homeViewModel.getServerPlaylists()
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
                LazyColumn(
                    modifier = Modifier.width(jvmRouterMenuWidth),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(jvmTopRouterDataList) { item ->
                        DesktopNavigationItem(
                            item = item,
                            selected = navigator.state.topLevelRoute == item.route,
                            onClick = { navigator.navigate(route = item.route) },
                        )
                    }
                    item(key = "playlist_spacer") {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                    item(key = "playlist_header") {
                        XyText(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            text = playlistTitle,
                            fontWeight = null,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (playlists.isEmpty()) {
                        item(key = "playlist_empty") {
                            XyTextSubSmall(
                                text = noPlaylistsText,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
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

                Box(modifier = Modifier.weight(1f)) {
                    content(PaddingValues())
                }
            }
        }
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .hoverable(interactionSource = interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
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
        )
    }
}
