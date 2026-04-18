package cn.xybbz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import cn.xybbz.ui.common.UiConstants.MusicCardImageSize
import cn.xybbz.ui.components.MusicAlbumCardComponent
import cn.xybbz.ui.components.MusicItemComponent
import cn.xybbz.ui.components.ScreenLazyColumn
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyRow
import cn.xybbz.ui.xy.XyText
import cn.xybbz.ui.xy.XyTextLarge
import cn.xybbz.viewmodel.HomeViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.jetbrains.compose.resources.stringResource
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.daily_recommendations
import xymusic_kmp.composeapp.generated.resources.latest_albums
import xymusic_kmp.composeapp.generated.resources.most_played
import xymusic_kmp.composeapp.generated.resources.recently_played_albums
import xymusic_kmp.composeapp.generated.resources.recently_played_music
import xymusic_kmp.composeapp.generated.resources.view_more

@Composable
fun JvmHomeScreenV2(
    homeViewModel: HomeViewModel = koinViewModel<HomeViewModel>()
) {
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

    ScreenLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.surface),
        contentPadding = PaddingValues(horizontal = XyTheme.dimens.innerHorizontalPadding),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
    ) {
        var hasPreviousSection = false

        if (recommendedMusicList.isNotEmpty()) {
            homeMusicSection(
                sectionKey = "recommended_music",
                title = dailyRecommendations,
                musicList = recommendedMusicList,
                onSongClick = { music ->
                    homeViewModel.musicList(
                        onMusicPlayParameter = OnMusicPlayParameter(musicId = music.itemId),
                        musicList = recommendedMusicList
                    )
                },
                headerAction = {
                    TextButton(
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
                onSongClick = { music ->
                    homeViewModel.musicList(
                        onMusicPlayParameter = OnMusicPlayParameter(musicId = music.itemId),
                        musicList = mostPlayedMusicList
                    )
                }
            )
        }
    }
}

private fun LazyListScope.homeMusicSection(
    sectionKey: String,
    title: String,
    musicList: List<XyMusic>,
    onSongClick: (XyMusic) -> Unit,
    headerAction: @Composable (() -> Unit)? = null,
) {
    item(key = "${sectionKey}_header") {
        // TODO: 这里暂时仍是页面内自定义 section header；ui module / components package
        // 里还没有桌面首页可直接复用的 section header 组件，后续可由你统一抽取。
        JvmHomeDesktopSectionHeader(title = title, action = headerAction)
    }
    item(key = "${sectionKey}_header_spacing") {
        Spacer(modifier = Modifier.height(6.dp))
    }
    // TODO: MusicItemComponent 能复用，但它目前不支持桌面首页原型里“单独点击专辑/艺人”的交互入口。
    // 这里先退回为“整行点击播放 + trailing action 预留”，后续由你决定是否抽一个桌面专用行组件。
    itemsIndexed(
        items = musicList,
        key = { _, music -> music.itemId }
    ) { index, music ->
        MusicItemComponent(
            modifier = if (index > 0) Modifier.padding(top = 6.dp) else Modifier,
            music = music,
            onIfFavorite = { music.ifFavoriteStatus },
            ifDownload = false,
            ifPlay = false,
            backgroundColor = androidx.compose.ui.graphics.Color.Transparent,
            onMusicPlay = {
                onSongClick(music)
            },
            trailingOnClick = {
                // TODO: components package 里目前没有适合桌面首页复用的轻量更多操作组件；
                // 暂时留空，等你统一处理桌面首页的 trailing action。
            }
        )
    }
}

private fun LazyListScope.homeAlbumSection(
    sectionKey: String,
    title: String,
    albumList: List<XyAlbum>,
    onOpenAlbum: (XyAlbum) -> Unit,
) {
    item(key = "${sectionKey}_header") {
        // TODO: components package 里还没有“固定五列桌面专辑网格”组件，
        // 这里先保留页面内布局骨架，后续可抽到通用组件。
        JvmHomeDesktopSectionHeader(title = title)
    }
    item(key = "${sectionKey}_grid_spacing") {
        Spacer(modifier = Modifier.height(20.dp))
    }
    item(key = "${sectionKey}_grid") {
        val rows = ((albumList.size + 4) / 5).coerceAtLeast(1)
        val gridHeight = 252.dp * rows + 24.dp * (rows - 1)

        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            userScrollEnabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .height(gridHeight),
        ) {
            gridItems(albumList, key = { album -> album.itemId }) { album ->
                MusicAlbumCardComponent(
                    onItem = { album },
                    imageSize = MusicCardImageSize,
                    onRouter = {
                        onOpenAlbum(album)
                    }
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
    XyRow {
        XyTextLarge(text = title)
        if (action != null) {
            action()
        }
    }
}
