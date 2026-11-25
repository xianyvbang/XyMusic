package cn.xybbz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
import cn.xybbz.ui.components.JvmPageTitleBar
import cn.xybbz.ui.components.MusicAlbumCardComponent
import cn.xybbz.ui.components.ScreenLazyColumn
import cn.xybbz.ui.components.SongTableColumns
import cn.xybbz.ui.components.rememberMusicArtistInfoLoader
import cn.xybbz.ui.components.rememberMusicArtistClickHandler
import cn.xybbz.ui.components.show
import cn.xybbz.ui.components.songTableItems
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyRow
import cn.xybbz.ui.xy.XyTextButton
import cn.xybbz.ui.xy.XyTextLarge
import cn.xybbz.viewmodel.HomeViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic.composeapp.generated.resources.Res
import xymusic.composeapp.generated.resources.daily_recommendations
import xymusic.composeapp.generated.resources.latest_albums
import xymusic.composeapp.generated.resources.most_played
import xymusic.composeapp.generated.resources.recently_played_albums
import xymusic.composeapp.generated.resources.recently_played_music
import xymusic.composeapp.generated.resources.view_more

private val HomeMusicTableColumns = SongTableColumns(
    showFavoriteColumn = true,
    showInlineActions = true,
    showAlbumColumn = true,
    showMetaColumn = false,
)

private const val HomeAlbumRowCount = 2

@Composable
fun JvmHomeScreen(
    // JVM 首页 ViewModel 只负责页面数据和交互，不在 init 阶段触发登录重依赖。
    // 桌面窗口首帧展示后再启动数据源链路，减少启动白屏和窗口卡顿。
    homeViewModel: HomeViewModel = koinViewModel<HomeViewModel>(),
) {
    val homeListState = rememberLazyListState()
    val mostPlayedMusicList by homeViewModel.homeDataRepository.mostPlayedMusic.collectAsStateWithLifecycle()
    val newestAlbumList by homeViewModel.homeDataRepository.newestAlbums.collectAsStateWithLifecycle()
    val recentMusicList by homeViewModel.homeDataRepository.recentMusic.collectAsStateWithLifecycle()
    val recentAlbumList by homeViewModel.homeDataRepository.recentAlbums.collectAsStateWithLifecycle()
    val mostPlayedAlbumList by homeViewModel.homeDataRepository.mostPlayedAlbums.collectAsStateWithLifecycle()
    val recommendedMusicList by homeViewModel.homeDataRepository.recommendedMusic.collectAsStateWithLifecycle()
    val favoriteSet by homeViewModel.favoriteSet.collectAsStateWithLifecycle(emptyList())
    // 已下载完成的音乐 ID 集合，用于行内展示已下载图标并禁止重复下载
    val downloadMusicIds by homeViewModel.downloadMusicIdsFlow.collectAsStateWithLifecycle(emptyList())
    val navigator = LocalNavigator.current
    val currentPlayingMusicIdFlow = remember(homeViewModel) {
        homeViewModel.musicController.musicInfoFlow.map { musicInfo ->
            musicInfo?.itemId
        }
    }

    // 首页歌曲表格里的艺术家文本和右键菜单共用同一套打开逻辑。
    val artistInfoLoader = rememberMusicArtistInfoLoader(homeViewModel.dataSourceManager)
    val artistClickHandler = rememberMusicArtistClickHandler(
        artistList = artistInfoLoader.artistList,
        onLoadArtistInfos = artistInfoLoader.loadArtistInfos,
    )
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
        currentPlayingMusicIdFlow = currentPlayingMusicIdFlow,
        favoriteMusicIds = favoriteSet,
        downloadedMusicIds = downloadMusicIds,
    )

    ScreenLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        state = homeListState,
        contentPadding = PaddingValues(),
    ) {
//        homeTopAnchor()
        // 让 LazyListScope 同时拥有 HomeMusicSectionScope 的上下文，下面可直接调用统一封装后的 homeMusicSection。
        with(homeMusicSectionScope) {
            // 所有分区都固定参与列表构建：数据未到达时由分区占位容器占住列表头部位置，
            // 数据到达后只在该分区内部追加内容，列表顶部的 item 结构不再发生插入/删除，
            // 避免 LazyColumn 按 item key 重新锚定、把滚动位置固定在后面的分区上。
            var hasVisibleSection = false

            // 推荐分区始终是第一个分区，页头样式固定，不会因为其他数据后到而切换高度。
            homeMusicSection(
                sectionKey = "recommended_music",
                title = dailyRecommendations,
                musicList = recommendedMusicList,
                usePageHeader = true,
                headerAction = {
                    XyTextButton(
                        text = viewMore,
                        fontWeight = FontWeight.Bold,
                        onClick = { navigator.navigate(DailyRecommend) },
                    )
                }
            )
            hasVisibleSection = recommendedMusicList.isNotEmpty()

            // 只有前面已经出现过有数据的分区时才插入间距，保持空分区不占视觉空间。
            if (newestAlbumList.isNotEmpty() && hasVisibleSection) {
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
            hasVisibleSection = hasVisibleSection || newestAlbumList.isNotEmpty()

            if (recentMusicList.isNotEmpty() && hasVisibleSection) {
                homeSectionSpacing(sectionKey = "recent_music")
            }
            homeMusicSection(
                sectionKey = "recent_music",
                title = recentlyPlayedMusic,
                musicList = recentMusicList,
            )
            hasVisibleSection = hasVisibleSection || recentMusicList.isNotEmpty()

            if (recentAlbumList.isNotEmpty() && hasVisibleSection) {
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
            hasVisibleSection = hasVisibleSection || recentAlbumList.isNotEmpty()

            if (mostPlayedAlbumList.isNotEmpty() && hasVisibleSection) {
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
            hasVisibleSection = hasVisibleSection || mostPlayedAlbumList.isNotEmpty()

            if (mostPlayedMusicList.isNotEmpty() && hasVisibleSection) {
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

private fun LazyListScope.homeTopAnchor() {
    item(key = "home_top_anchor") {
        Spacer(modifier = Modifier.height(1.dp))
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
 * @property currentPlayingMusicIdFlow 当前正在播放的歌曲 ID 流，交给 SongRow 按行监听播放状态。
 * @property favoriteMusicIds 已收藏歌曲 ID 列表，用于标记表格中的收藏状态。
 * @property downloadedMusicIds 已下载完成歌曲 ID 列表，用于标记表格中的已下载状态。
 */
private class HomeMusicSectionScope(
    private val homeViewModel: HomeViewModel,
    private val navigator: Navigator,
    private val onOpenArtist: (XyMusic) -> Unit,
    private val currentPlayingMusicIdFlow: Flow<String?>,
    private val favoriteMusicIds: List<String>,
    private val downloadedMusicIds: List<String>,
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
        usePageHeader: Boolean = false,
        headerAction: @Composable (() -> Unit)? = null,
    ) {
        // 分区占位容器：无论数据是否到达，都保留一个 key 固定的 item。
        // 数据未到达时只占 1dp 高度作为滚动锚点，数据到达后在同一 item 内渲染分区页头，
        // 这样列表头部不会因为数据后到而“凭空插入”item，滚动位置也就不会跑到后面的分区上。
        item(key = "${sectionKey}_placeholder") {
            if (musicList.isEmpty()) {
                Spacer(modifier = Modifier.height(1.dp))
            } else if (usePageHeader) {
                JvmHomePageSectionHeader(title = title, action = headerAction)
            } else {
                JvmHomeDesktopSectionHeader(title = title, action = headerAction)
            }
        }

        // 无数据时占位容器已经承担锚点职责，这里不再追加任何行 item。
        if (musicList.isEmpty()) {
            return
        }

        // 歌曲表格的通用行为集中在这里，调用处只需要传入当前分区自己的 musicList。
        songTableItems(
            tableKey = sectionKey,
            songs = musicList,
            columns = HomeMusicTableColumns,
            // 根据全局收藏列表判断当前行是否收藏。
            ifFavorite = { music -> music.itemId in favoriteMusicIds },
            // 根据全局已下载列表判断当前行是否已下载，已下载时展示完成图标且不可重复下载。
            ifDownloaded = { music -> music.itemId in downloadedMusicIds },
            // 播放状态下沉到 SongRow 内按行监听，避免 JvmHomeScreen 顶层跟随播放状态重组。
            currentPlayingMusicIdFlow = currentPlayingMusicIdFlow,
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
    val albumSnapshot = albumList.toList()

    // 分区占位容器（与歌曲分区保持一致）：空数据时保留 1dp 锚点，数据到达后渲染分区页头，
    // 保证专辑分区的 item 结构在数据到达前后保持稳定，不会顶掉滚动锚点。
    item(key = "${sectionKey}_placeholder") {
        if (albumSnapshot.isEmpty()) {
            Spacer(modifier = Modifier.height(1.dp))
        } else {
            JvmHomeDesktopSectionHeader(title = title)
        }
    }

    if (albumSnapshot.isEmpty()) {
        return
    }

    val rowCount = minOf(HomeAlbumRowCount, albumSnapshot.size)

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
            // 专辑网格左右保留与标题一致的页面外边距，纵向间距由外层 item 控制
            contentPadding = PaddingValues(
                horizontal = XyTheme.dimens.outerHorizontalPadding,
            ),
            horizontalArrangement = Arrangement.spacedBy(gridSpacing),
            verticalArrangement = Arrangement.spacedBy(gridSpacing),
        ) {
            items(
                count = albumSnapshot.size,
                key = { index -> "${sectionKey}_${albumSnapshot[index].itemId}_$index" },
            ) { index ->
                val album = albumSnapshot[index]
                MusicAlbumCardComponent(
                    album = album,
                    imageSize = MusicCardImageSize,
                    onRouter = { onOpenAlbum(album) },
                )
            }
        }
    }
}

/**
 * 首页首个分区标题。
 *
 * 复用设置页、连接管理页的 [JvmPageTitleBar]，让首页首屏标题与其他一级页面保持一致。
 */
@Composable
private fun JvmHomePageSectionHeader(
    title: String,
    action: @Composable (() -> Unit)? = null,
) {
    JvmPageTitleBar(
        title = title,
        trailing = {
            if (action != null) {
                action()
            }
        },
    )
}

@Composable
private fun JvmHomeDesktopSectionHeader(
    title: String,
    action: @Composable (() -> Unit)? = null,
) {
    // 分区标题只保留左右外边距，纵向间距由分区之间的 spacing 项控制
    XyRow(
        paddingValues = PaddingValues(horizontal = XyTheme.dimens.outerHorizontalPadding),
    ) {
        XyTextLarge(text = title)
        if (action != null) {
            action()
        }
    }
}
