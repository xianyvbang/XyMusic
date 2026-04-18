package cn.xybbz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import cn.xybbz.ui.xy.XyText
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

    ScreenLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.surface),
        contentPadding = PaddingValues(horizontal = XyTheme.dimens.innerHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(40.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        if (recommendedMusicList.isNotEmpty()) {
            item {
                JvmHomeDesktopMusicSection(
                    title = stringResource(Res.string.daily_recommendations),
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
                                text = stringResource(Res.string.view_more),
                            )
                        }
                    }
                )
            }
        }

        if (newestAlbumList.isNotEmpty()) {
            item {
                JvmHomeDesktopAlbumSection(
                    title = stringResource(Res.string.latest_albums),
                    albumList = newestAlbumList,
                    onOpenAlbum = { album ->
                        navigator.navigate(AlbumInfo(album.itemId, MusicDataTypeEnum.ALBUM))
                    }
                )
            }
        }

        if (recentMusicList.isNotEmpty()) {
            item {
                JvmHomeDesktopMusicSection(
                    title = stringResource(Res.string.recently_played_music),
                    musicList = recentMusicList,
                    onSongClick = { music ->
                        homeViewModel.musicList(
                            onMusicPlayParameter = OnMusicPlayParameter(musicId = music.itemId),
                            musicList = recentMusicList
                        )
                    }
                )
            }
        }

        if (recentAlbumList.isNotEmpty()) {
            item {
                JvmHomeDesktopAlbumSection(
                    title = stringResource(Res.string.recently_played_albums),
                    albumList = recentAlbumList,
                    onOpenAlbum = { album ->
                        navigator.navigate(AlbumInfo(album.itemId, MusicDataTypeEnum.ALBUM))
                    }
                )
            }
        }

        if (mostPlayedAlbumList.isNotEmpty()) {
            item {
                JvmHomeDesktopAlbumSection(
                    title = stringResource(Res.string.most_played),
                    albumList = mostPlayedAlbumList,
                    onOpenAlbum = { album ->
                        navigator.navigate(AlbumInfo(album.itemId, MusicDataTypeEnum.ALBUM))
                    }
                )
            }
        }

        if (mostPlayedMusicList.isNotEmpty()) {
            item {
                JvmHomeDesktopMusicSection(
                    title = stringResource(Res.string.most_played),
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
}

@Composable
private fun JvmHomeDesktopMusicSection(
    title: String,
    musicList: List<XyMusic>,
    onSongClick: (XyMusic) -> Unit,
    headerAction: @Composable (() -> Unit)? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        // TODO: 这里暂时仍是页面内自定义 section header；ui module / components package
        // 里还没有桌面首页可直接复用的 section header 组件，后续可由你统一抽取。
        JvmHomeDesktopSectionHeader(title = title, action = headerAction)
        // TODO: MusicItemComponent 能复用，但它目前不支持桌面首页原型里“单独点击专辑/艺人”的交互入口。
        // 这里先退回为“整行点击播放 + trailing action 预留”，后续由你决定是否抽一个桌面专用行组件。
        musicList.forEach { music ->
            MusicItemComponent(
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
}

@Composable
private fun JvmHomeDesktopAlbumSection(
    title: String,
    albumList: List<XyAlbum>,
    onOpenAlbum: (XyAlbum) -> Unit,
) {
    val rows = ((albumList.size + 4) / 5).coerceAtLeast(1)
    val gridHeight = 252.dp * rows + 24.dp * (rows - 1)

    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        // TODO: components package 里还没有“固定五列桌面专辑网格”组件，
        // 这里先保留页面内布局骨架，后续可抽到通用组件。
        JvmHomeDesktopSectionHeader(title = title)
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            userScrollEnabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .height(gridHeight),
        ) {
            items(albumList, key = { album -> album.itemId }) { album ->
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        XyText(text = title)
        if (action != null) {
            action()
        }
    }
}
