package cn.xybbz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import cn.xybbz.ui.components.SongTableColumns
import cn.xybbz.ui.components.rememberMusicArtistClickHandler
import cn.xybbz.ui.components.show
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
    val mostPlayedMusicList by homeViewModel.homeDataRepository.mostPlayedMusic.collectAsStateWithLifecycle()
    val newestAlbumList by homeViewModel.homeDataRepository.newestAlbums.collectAsStateWithLifecycle()
    val recentMusicList by homeViewModel.homeDataRepository.recentMusic.collectAsStateWithLifecycle()
    val recentAlbumList by homeViewModel.homeDataRepository.recentAlbums.collectAsStateWithLifecycle()
    val mostPlayedAlbumList by homeViewModel.homeDataRepository.mostPlayedAlbums.collectAsStateWithLifecycle()
    val recommendedMusicList by homeViewModel.homeDataRepository.recommendedMusic.collectAsStateWithLifecycle()
    val playbackState by homeViewModel.musicController.playbackStateFlow.collectAsStateWithLifecycle()
    val favoriteSet by homeViewModel.favoriteSet.collectAsStateWithLifecycle(emptyList())
    val navigator = LocalNavigator.current

    // 首页歌曲表格里的艺术家文本和右键菜单共用同一套打开逻辑。
    val artistClickHandler = rememberMusicArtistClickHandler()
    val dailyRecommendations = stringResource(Res.string.daily_recommendations)
    val latestAlbums = stringResource(Res.string.latest_albums)
    val recentlyPlayedMusic = stringResource(Res.string.recently_played_music)
    val recentlyPlayedAlbums = stringResource(Res.string.recently_played_albums)
    val mostPlayed = stringResource(Res.string.most_played)
    val viewMore = stringResource(Res.string.view_more)
    // 将首页音乐分区共用的状态和事件收束到同一个作用域，避免每个 homeMusicSection 重复传参。
    val homeMusicSectionScope = HomeMusicSectionScope(
        homeViewModel = homeViewModel,
        navigator = navigator,
        onOpenArtist = artistClickHandler::openMusicArtists,
        currentPlayingMusicId = playbackState.musicInfo?.itemId,
        favoriteMusicIds = favoriteSet,
    )

    ScreenLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        state = homeListState,
        contentPadding = PaddingValues(),
    ) {
        // 让 LazyListScope 同时拥有 HomeMusicSectionScope 的上下文，下面可直接调用统一封装后的 homeMusicSection。
        with(homeMusicSectionScope) {
            var hasPreviousSection = false

            if (recommendedMusicList.isNotEmpty()) {
                homeMusicSection(
                    sectionKey = "recommended_music",
                    title = dailyRecommendations,
                    musicList = recommendedMusicList,
                    headerAction = {
                        TextButton(
                            modifier = Modifier.padding(XyTheme.dimens.outerHorizontalPadding),
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
                    homeSectionSpacing(sectionKey = "latest_album")
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
                    homeSectionSpacing(sectionKey = "recent_music")
                }
                homeMusicSection(
                    sectionKey = "recent_music",
                    title = recentlyPlayedMusic,
                    musicList = recentMusicList,
                )
                hasPreviousSection = true
            }

            if (recentAlbumList.isNotEmpty()) {
                if (hasPreviousSection) {
                    homeSectionSpacing(sectionKey = "recent_album")
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
                    homeSectionSpacing(sectionKey = "most_played_album")
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
                    homeSectionSpacing(sectionKey = "most_played_music")
                }
                homeMusicSection(
                    sectionKey = "most_played_music",
                    title = mostPlayed,
                    musicList = mostPlayedMusicList,
                )
            }
        }
    }
}

private fun LazyListScope.homeSectionSpacing(sectionKey: String) {
    item(key = "${sectionKey}_spacing") {
        HomeSectionSpacing()
    }
}

@Composable
private fun HomeSectionSpacing() {
    Spacer(modifier = Modifier.height(20.dp))
}

/**
 * 首页音乐分区构建作用域。
 *
 * 这里集中保存各个 homeMusicSection 都会用到的页面级依赖和状态，
 * 调用处只需要关心当前分区的 key、标题、音乐列表和少量自定义头部动作。
 *
 * @property homeViewModel 首页 ViewModel，统一处理播放列表、收藏切换和下载。
 * @property navigator 页面导航器，统一处理专辑详情等页面跳转。
 * @property onOpenArtist 艺人打开回调，统一复用艺术家文本和右键菜单的打开逻辑。
 * @property currentPlayingMusicId 当前正在播放的歌曲 ID，用于标记表格中的播放行。
 * @property favoriteMusicIds 已收藏歌曲 ID 列表，用于标记表格中的收藏状态。
 */
private class HomeMusicSectionScope(
    private val homeViewModel: HomeViewModel,
    private val navigator: Navigator,
    private val onOpenArtist: (XyMusic) -> Unit,
    private val currentPlayingMusicId: String?,
    private val favoriteMusicIds: List<String>,
) {
    /**
     * 构建首页中的歌曲分区。
     *
     * 该方法统一封装歌曲表格的播放、收藏、下载、专辑跳转、艺人打开和更多菜单逻辑，
     * 避免每个首页音乐分区重复实现同一套 songTableItems 参数。
     */
    fun LazyListScope.homeMusicSection(
        sectionKey: String,
        title: String,
        musicList: List<XyMusic>,
        headerAction: @Composable (() -> Unit)? = null,
    ) {
        // 每个分区先添加独立 header，headerAction 用于推荐歌曲的“查看更多”等差异化按钮。
        item(key = "${sectionKey}_header") {
            JvmHomeDesktopSectionHeader(title = title, action = headerAction)
        }

        // 歌曲表格的通用行为集中在这里，调用处只需要传入当前分区自己的 musicList。
        songTableItems(
            tableKey = sectionKey,
            songs = musicList,
            columns = HomeMusicTableColumns,
            // 根据全局收藏列表判断当前行是否收藏。
            ifFavorite = { music -> music.itemId in favoriteMusicIds },
            // 根据播放器状态判断当前行是否正在播放。
            ifPlay = { music -> music.itemId == currentPlayingMusicId },
            onSongClick = { _, music ->
                // 点击任意歌曲时，使用当前分区的完整列表作为播放队列。
                homeViewModel.musicList(
                    onMusicPlayParameter = OnMusicPlayParameter(musicId = music.itemId),
                    musicList = musicList,
                )
            },
            onOpenAlbum = { music ->
                // 只有存在专辑 ID 时才打开专辑详情，避免空 ID 触发无效导航。
                if (music.album.isNotBlank()) {
                    navigator.navigate(AlbumInfo(music.album, MusicDataTypeEnum.ALBUM))
                }
            },
            // 艺人打开逻辑由外部统一传入，保证文本点击和菜单点击行为一致。
            onOpenArtist = onOpenArtist,
            onFavoriteClick = { music ->
                // 收藏按钮统一调用首页 ViewModel，避免各分区重复实现。
                homeViewModel.toggleFavorite(music.itemId)
            },
            onDownloadClick = { music ->
                // 下载按钮统一调用首页 ViewModel，下载参数由 ViewModel 内部补齐。
                homeViewModel.downloadMusic(music)
            },
            // 更多菜单沿用歌曲实体已有的弹出菜单能力。
            onMoreClick = { music -> music.show() },
        )
    }
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
            itemsIndexed(
                items = albumList,
                key = { index, album -> "${sectionKey}_${album.itemId}_$index" },
            ) { _, album ->
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
    XyRow(paddingValues = PaddingValues(/*vertical = XyTheme.dimens.outerVerticalPadding*/)) {
        XyTextLarge(text = title)
        if (action != null) {
            action()
        }
    }
}

