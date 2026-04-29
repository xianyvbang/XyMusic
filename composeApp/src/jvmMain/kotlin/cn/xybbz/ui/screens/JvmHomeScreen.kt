package cn.xybbz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.entity.data.music.OnMusicPlayParameter
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import cn.xybbz.router.AlbumInfo
import cn.xybbz.router.DailyRecommend
import cn.xybbz.router.Navigator
import cn.xybbz.ui.common.UiConstants.MusicCardImageSize
import cn.xybbz.ui.components.JvmHorizontalScrollbarBottomPadding
import cn.xybbz.ui.components.JvmLazyHorizontalGridComponent
import cn.xybbz.ui.components.MusicAlbumCardComponent
import cn.xybbz.ui.components.ScreenLazyColumn
import cn.xybbz.ui.components.SidebarVerticalScrollbar
import cn.xybbz.ui.components.SongTableColumns
import cn.xybbz.ui.components.songTableItems
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyRow
import cn.xybbz.ui.xy.XyText
import cn.xybbz.ui.xy.XyTextLarge
import cn.xybbz.viewmodel.HomeViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.daily_recommendations
import xymusic_kmp.composeapp.generated.resources.latest_albums
import xymusic_kmp.composeapp.generated.resources.most_played
import xymusic_kmp.composeapp.generated.resources.recently_played_albums
import xymusic_kmp.composeapp.generated.resources.recently_played_music
import xymusic_kmp.composeapp.generated.resources.view_more

private val HomeMusicTableColumns = SongTableColumns(
    showFavoriteColumn = true,
    showInlineActions = true,
    showAlbumColumn = true,
    showMetaColumn = false,
)

private const val HomeAlbumRowCount = 2

@Composable
fun JvmHomeScreen(
    homeViewModel: HomeViewModel = koinViewModel<HomeViewModel>()
) {
    val homeListState = rememberLazyListState()
    val scrollbarInteractionSource = remember { MutableInteractionSource() }
    val scrollbarHovered by scrollbarInteractionSource.collectIsHoveredAsState()
    val showScrollbar = scrollbarHovered || homeListState.isScrollInProgress
    val mostPlayedMusicList by homeViewModel.homeDataRepository.mostPlayedMusic.collectAsStateWithLifecycle()
    val newestAlbumList by homeViewModel.homeDataRepository.newestAlbums.collectAsStateWithLifecycle()
    val recentMusicList by homeViewModel.homeDataRepository.recentMusic.collectAsStateWithLifecycle()
    val recentAlbumList by homeViewModel.homeDataRepository.recentAlbums.collectAsStateWithLifecycle()
    val mostPlayedAlbumList by homeViewModel.homeDataRepository.mostPlayedAlbums.collectAsStateWithLifecycle()
    val recommendedMusicList by homeViewModel.homeDataRepository.recommendedMusic.collectAsStateWithLifecycle()
    val navigator = LocalNavigator.current
    val dailyRecommendations = stringResource(Res.string.daily_recommendations)
    val latestAlbums = stringResource(Res.string.latest_albums)
    val recentlyPlayedMusic = stringResource(Res.string.recently_played_music)
    val recentlyPlayedAlbums = stringResource(Res.string.recently_played_albums)
    val mostPlayed = stringResource(Res.string.most_played)
    val viewMore = stringResource(Res.string.view_more)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .hoverable(interactionSource = scrollbarInteractionSource)
    ) {
        ScreenLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            state = homeListState,
            contentPadding = PaddingValues(horizontal = XyTheme.dimens.outerHorizontalPadding),
        ) {
            var hasPreviousSection = false

            if (recommendedMusicList.isNotEmpty()) {
                homeMusicSection(
                    sectionKey = "recommended_music",
                    title = dailyRecommendations,
                    musicList = recommendedMusicList,
                    navigator = navigator,
                    onSongClick = { music ->
                        homeViewModel.musicList(
                            onMusicPlayParameter = OnMusicPlayParameter(musicId = music.itemId),
                            musicList = recommendedMusicList
                        )
                    },
                    headerAction = {
                        TextButton(
                            modifier = Modifier.padding(XyTheme.dimens.outerHorizontalPadding / 2),
                            onClick = { navigator.navigate(DailyRecommend) },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            XyText(
                                text = viewMore,
                            )
                        }
                    }
                )
                hasPreviousSection = true
            }

            if (newestAlbumList.isNotEmpty()) {
                if (hasPreviousSection) {
                    item(key = "latest_album_spacing") {
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
                homeAlbumSection(
                    sectionKey = "latest_album",
                    title = latestAlbums,
                    albumList = newestAlbumList,
                    onOpenAlbum = { album ->
                        navigator.navigate(AlbumInfo(album.itemId, MusicDataTypeEnum.ALBUM))
                    }
                )
                hasPreviousSection = true
            }

            if (recentMusicList.isNotEmpty()) {
                if (hasPreviousSection) {
                    item(key = "recent_music_spacing") {
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
                homeMusicSection(
                    sectionKey = "recent_music",
                    title = recentlyPlayedMusic,
                    musicList = recentMusicList,
                    navigator = navigator,
                    onSongClick = { music ->
                        homeViewModel.musicList(
                            onMusicPlayParameter = OnMusicPlayParameter(musicId = music.itemId),
                            musicList = recentMusicList
                        )
                    }
                )
                hasPreviousSection = true
            }

            if (recentAlbumList.isNotEmpty()) {
                if (hasPreviousSection) {
                    item(key = "recent_album_spacing") {
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
                homeAlbumSection(
                    sectionKey = "recent_album",
                    title = recentlyPlayedAlbums,
                    albumList = recentAlbumList,
                    onOpenAlbum = { album ->
                        navigator.navigate(AlbumInfo(album.itemId, MusicDataTypeEnum.ALBUM))
                    }
                )
                hasPreviousSection = true
            }

            if (mostPlayedAlbumList.isNotEmpty()) {
                if (hasPreviousSection) {
                    item(key = "most_played_album_spacing") {
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
                homeAlbumSection(
                    sectionKey = "most_played_album",
                    title = mostPlayed,
                    albumList = mostPlayedAlbumList,
                    onOpenAlbum = { album ->
                        navigator.navigate(AlbumInfo(album.itemId, MusicDataTypeEnum.ALBUM))
                    }
                )
                hasPreviousSection = true
            }

            if (mostPlayedMusicList.isNotEmpty()) {
                if (hasPreviousSection) {
                    item(key = "most_played_music_spacing") {
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
                homeMusicSection(
                    sectionKey = "most_played_music",
                    title = mostPlayed,
                    musicList = mostPlayedMusicList,
                    navigator = navigator,
                    onSongClick = { music ->
                        homeViewModel.musicList(
                            onMusicPlayParameter = OnMusicPlayParameter(musicId = music.itemId),
                            musicList = mostPlayedMusicList
                        )
                    }
                )
            }
        }

        SidebarVerticalScrollbar(
            visible = showScrollbar,
            modifier = Modifier.align(Alignment.CenterEnd),
            adapter = rememberScrollbarAdapter(scrollState = homeListState),
        )
    }
}

private fun LazyListScope.homeMusicSection(
    sectionKey: String,
    title: String,
    musicList: List<XyMusic>,
    onSongClick: (XyMusic) -> Unit,
    navigator: Navigator,
    headerAction: @Composable (() -> Unit)? = null,
) {
    item(key = "${sectionKey}_header") {
        JvmHomeDesktopSectionHeader(title = title, action = headerAction)
    }
    item(key = "${sectionKey}_header_spacing") {
        Spacer(modifier = Modifier.height(6.dp))
    }
    songTableItems(
        tableKey = sectionKey,
        songs = musicList,
        columns = HomeMusicTableColumns,
        onSongClick = onSongClick,
        onOpenAlbum = { music ->
            if (music.album.isNotBlank()) {
                navigator.navigate(AlbumInfo(music.album, MusicDataTypeEnum.ALBUM))
            }
        },
    )
}

private fun LazyListScope.homeAlbumSection(
    sectionKey: String,
    title: String,
    albumList: List<XyAlbum>,
    onOpenAlbum: (XyAlbum) -> Unit,
) {
    if (albumList.isEmpty()) {
        return
    }

    val rowCount = minOf(HomeAlbumRowCount, albumList.size)

    item(key = "${sectionKey}_header") {
        JvmHomeDesktopSectionHeader(title = title)
    }
    item(key = "${sectionKey}_grid_spacing") {
        Spacer(modifier = Modifier.height(XyTheme.dimens.outerVerticalPadding + XyTheme.dimens.contentPadding))
    }
    item(key = "${sectionKey}_grid") {
        val gridSpacing = XyTheme.dimens.innerVerticalPadding * 2
        JvmLazyHorizontalGridComponent(
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    (MusicCardImageSize + 50.dp) * rowCount +
                            gridSpacing * (rowCount - 1) +
                            JvmHorizontalScrollbarBottomPadding()
                ),
            rows = GridCells.Fixed(rowCount),
            userScrollEnabled = false,
            contentPadding = PaddingValues(0.dp),
            horizontalArrangement = Arrangement.spacedBy(gridSpacing),
            verticalArrangement = Arrangement.spacedBy(gridSpacing),
        ) {
            items(
                items = albumList,
                key = { album -> "${sectionKey}_${album.itemId}" },
            ) { album ->
                MusicAlbumCardComponent(
                    album = album,
                    imageSize = MusicCardImageSize,
                    onRouter = { onOpenAlbum(album) },
                )
            }
        }
    }
}

@Composable
private fun JvmHomeDesktopSectionHeader(
    title: String,
    action: @Composable (() -> Unit)? = null,
) {
    XyRow(paddingValues = PaddingValues(vertical = XyTheme.dimens.outerVerticalPadding)) {
        XyTextLarge(text = title)
        if (action != null) {
            action()
        }
    }
}

