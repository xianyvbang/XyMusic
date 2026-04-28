@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package cn.xybbz.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import cn.xybbz.router.Album
import cn.xybbz.router.AlbumInfo
import cn.xybbz.router.Artist
import cn.xybbz.router.ArtistInfo
import cn.xybbz.router.FavoriteList
import cn.xybbz.router.Home
import cn.xybbz.router.Music
import cn.xybbz.router.Search
import cn.xybbz.ui.components.TopAppBarComponent
import org.jetbrains.compose.resources.painterResource
import xymusic_kmp.composeapp.generated.resources.*

/**
 * JVM 桌面原型的总布局壳子。
 * 负责组合侧边栏、顶部栏、主内容区、播放条和右侧播放队列。
 */
@Composable
internal fun DesktopPrototypeScreen(destination: DesktopDestination) {
    val navigator = LocalNavigator.current
    var queueOpen by remember { mutableStateOf(false) }
    var playbackProgress by remember { mutableFloatStateOf(0.3f) }
    var volumeProgress by remember { mutableFloatStateOf(0.6f) }

    Surface(modifier = Modifier.fillMaxSize(), color = desktopColors.bgBase) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(desktopColors.bgBase, desktopColors.bgBase)))
                .padding(WindowInsets.systemBars.asPaddingValues())
        ) {
            val topHeight = (maxHeight - 90.dp).coerceAtLeast(0.dp)
            Column(modifier = Modifier.fillMaxSize()) {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(topHeight)) {
                    val contentWidth = (maxWidth - 240.dp).coerceAtLeast(0.dp)
                    Row(modifier = Modifier.fillMaxSize()) {
                        DesktopSidebar(
                            activeDestination = destination.toSidebarDestination(),
                            onMenuSelected = { target ->
                                when (target) {
                                    SidebarDestination.Home -> navigator.navigate(Home)
                                    SidebarDestination.Search -> navigator.navigate(Search())
                                    SidebarDestination.Library -> navigator.navigate(Music)
                                    SidebarDestination.Albums -> navigator.navigate(Album)
                                    SidebarDestination.Artists -> navigator.navigate(Artist)
                                }
                            },
                            onPlaylistSelected = { navigator.navigate(FavoriteList) },
                        )
                        Box(
                            modifier = Modifier
                                .width(contentWidth)
                                .fillMaxHeight()
                                .padding(top = 8.dp, end = 8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color(0xFF323232), desktopColors.bgElevated),
                                        endY = 640f,
                                    )
                                )
                        ) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                DesktopTopBar()
                                Box(modifier = Modifier.fillMaxSize()) {
                                    DesktopPageContent(destination)
                                    QueuePanel(visible = queueOpen, modifier = Modifier.align(Alignment.CenterEnd), onClose = { queueOpen = false })
                                }
                            }
                        }
                    }
                }
                PlayerBar(
                    progress = playbackProgress,
                    volume = volumeProgress,
                    queueOpen = queueOpen,
                    onProgressChange = { playbackProgress = it },
                    onVolumeChange = { volumeProgress = it },
                    onToggleQueue = { queueOpen = !queueOpen },
                    onOpenAlbum = { navigator.navigate(AlbumInfo("player-album", MusicDataTypeEnum.ALBUM)) },
                    onOpenArtist = { navigator.navigate(ArtistInfo("player-artist", "The Synth Band")) },
                )
            }
        }
    }
}

/**
 * 将详情页映射回左侧边栏的高亮项。
 */
private fun DesktopDestination.toSidebarDestination(): SidebarDestination = when (this) {
    DesktopDestination.Home -> SidebarDestination.Home
    DesktopDestination.Search -> SidebarDestination.Search
    DesktopDestination.Library -> SidebarDestination.Library
    DesktopDestination.Albums -> SidebarDestination.Albums
    DesktopDestination.Artists -> SidebarDestination.Artists
    DesktopDestination.PlaylistDetail -> SidebarDestination.Library
    is DesktopDestination.AlbumDetail -> SidebarDestination.Albums
    is DesktopDestination.ArtistDetail -> SidebarDestination.Artists
}

/**
 * 根据当前内部页面状态切换主内容区。
 */
@Composable
private fun DesktopPageContent(destination: DesktopDestination) {
    val navigator = LocalNavigator.current
    Crossfade(targetState = destination, modifier = Modifier.fillMaxSize()) { page ->
        when (page) {
            DesktopDestination.Home -> HomeDesktopContent()
            DesktopDestination.Search -> SearchDesktopContent()
            DesktopDestination.Library -> LibraryDesktopContent(onOpenPlaylist = { navigator.navigate(FavoriteList) })
            DesktopDestination.Albums -> AlbumsDesktopContent(onOpenAlbum = {
                navigator.navigate(AlbumInfo("desktop-album", MusicDataTypeEnum.ALBUM))
            })
            DesktopDestination.Artists -> ArtistsDesktopContent(onOpenArtist = {
                navigator.navigate(ArtistInfo("desktop-artist", "The Synth Band"))
            })
            DesktopDestination.PlaylistDetail -> PlaylistDetailDesktopContent(
                onOpenAlbum = { navigator.navigate(AlbumInfo("playlist-album", MusicDataTypeEnum.ALBUM)) },
                onOpenArtist = { navigator.navigate(ArtistInfo("playlist-artist", "The Synth Band")) },
            )
            is DesktopDestination.AlbumDetail -> AlbumDetailDesktopContent(
                title = page.title,
                onOpenArtist = { navigator.navigate(ArtistInfo("album-artist", "The Synth Band")) },
            )
            is DesktopDestination.ArtistDetail -> ArtistDetailDesktopContent(
                name = page.name,
                onOpenAlbum = { navigator.navigate(AlbumInfo("artist-album", MusicDataTypeEnum.ALBUM)) },
            )
        }
    }
}

/**
 * 左侧侧边栏，包含主导航和歌单列表。
 */
@Composable
private fun DesktopSidebar(
    activeDestination: SidebarDestination,
    onMenuSelected: (SidebarDestination) -> Unit,
    onPlaylistSelected: () -> Unit,
) {
    Column(
        modifier = Modifier.width(240.dp).fillMaxHeight().padding(horizontal = 12.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            CoverSwatch(Modifier.size(24.dp), desktopColors.theme, circular = true, glyph = painterResource(Res.drawable.album_24px))
            Text("XyMusic", color = desktopColors.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Column(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(desktopColors.bgElevated).padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            sidebarItems.forEach { item ->
                HoverMenuRow(
                    label = item.label,
                    icon = item.icon,
                    active = item.destination == activeDestination,
                    onClick = { onMenuSelected(item.destination) },
                )
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val libraryHeight = (maxHeight - 220.dp).coerceAtLeast(160.dp)
            Column(
                modifier = Modifier.width(216.dp).height(libraryHeight).clip(RoundedCornerShape(8.dp)).background(desktopColors.bgElevated).padding(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("我的歌单", color = desktopColors.textSecondary, fontWeight = FontWeight.SemiBold)
                    ResourceIcon(Res.drawable.add_24px, null, desktopColors.textSecondary)
                }
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    sidebarPlaylists.forEach { playlist ->
                        HoverPlaylistRow(playlist = playlist, onClick = onPlaylistSelected)
                    }
                }
            }
        }
    }
}

/**
 * 顶部栏，复用了项目现有的 TopAppBarComponent。
 */
@Composable
private fun DesktopTopBar() {
    TopAppBarComponent(
        modifier = Modifier.fillMaxWidth().background(Color(0xB2121212)).padding(horizontal = 24.dp),
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        title = {},
        navigationIcon = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                CircularIconButton(Res.drawable.arrow_back_24px)
                CircularIconButton(Res.drawable.chevron_right_24px)
            }
        },
        actions = {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(20.dp))
                    .background(Color(0x80000000))
                    .padding(start = 4.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CoverSwatch(Modifier.size(24.dp), Color(0xFF2E7D73), circular = true)
                Text("网易云音乐", color = desktopColors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                ResourceIcon(Res.drawable.keyboard_arrow_down_24px, null, desktopColors.textPrimary)
            }
        }
    )
}
