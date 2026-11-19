/*
 *   XyMusic
 *   Copyright (C) 2023 xianyvbang
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package cn.xybbz.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.common.enums.PlayStateEnum
import cn.xybbz.common.enums.TabListEnum
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.config.image.rememberArtistCoverUrls
import cn.xybbz.entity.data.music.OnMusicPlayParameter
import cn.xybbz.extension.isSticking
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.data.artist.XyArtist
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import cn.xybbz.localdata.enums.MusicPlayTypeEnum
import cn.xybbz.localdata.enums.PlayerModeEnum
import cn.xybbz.router.AlbumInfo
import cn.xybbz.ui.components.FavoriteIconButton
import cn.xybbz.ui.components.JvmLazyListComponent
import cn.xybbz.ui.components.JvmMusicSelectionActionBar
import cn.xybbz.ui.components.LazyLoadingAndStatus
import cn.xybbz.ui.components.MusicAlbumCardComponent
import cn.xybbz.ui.components.MusicArtistCardComponent
import cn.xybbz.ui.components.SongTableColumns
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.jvmLazyColumnBottomComponent
import cn.xybbz.ui.components.rememberMusicArtistInfoLoader
import cn.xybbz.ui.components.rememberMusicArtistClickHandler
import cn.xybbz.ui.components.show
import cn.xybbz.ui.components.songTableItems
import cn.xybbz.ui.common.UiConstants.MusicCardImageSize
import cn.xybbz.ui.ext.debounceClickable
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.LazyColumnParentComponent
import cn.xybbz.ui.xy.ModalBottomSheetExtendComponent
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyImage
import cn.xybbz.viewmodel.ArtistInfoViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import xymusic.composeapp.generated.resources.Res
import xymusic.composeapp.generated.resources.album
import xymusic.composeapp.generated.resources.album_count_format
import xymusic.composeapp.generated.resources.artist
import xymusic.composeapp.generated.resources.artist_all_tracks
import xymusic.composeapp.generated.resources.artist_cover
import xymusic.composeapp.generated.resources.artist_count_format
import xymusic.composeapp.generated.resources.artrist_info
import xymusic.composeapp.generated.resources.check_24px
import xymusic.composeapp.generated.resources.expand_description
import xymusic.composeapp.generated.resources.no_description
import xymusic.composeapp.generated.resources.pause_circle_24px
import xymusic.composeapp.generated.resources.pause_playback
import xymusic.composeapp.generated.resources.play_all
import xymusic.composeapp.generated.resources.play_circle_24px
import xymusic.composeapp.generated.resources.playlist_add_check_24px
import xymusic.composeapp.generated.resources.random_play
import xymusic.composeapp.generated.resources.reached_bottom
import xymusic.composeapp.generated.resources.resemblance_artist
import xymusic.composeapp.generated.resources.resume_playback
import xymusic.composeapp.generated.resources.select
import xymusic.composeapp.generated.resources.selection_done
import xymusic.composeapp.generated.resources.shuffle_24px
import xymusic.composeapp.generated.resources.songs_count_suffix

/**
 * 艺术家主视觉整体尺寸（唱片外圈直径）。
 */
private val JvmArtistInfoDesktopCoverSize = 236.dp

/**
 * 唱片内圈艺术家封面直径，与设计稿的 26dp 内缩保持一致。
 */
private val JvmArtistInfoDesktopArtSize = 184.dp

/**
 * 艺术家详情页曲目表列配置。
 */
private val JvmArtistMusicTableColumns = SongTableColumns(
    showFavoriteColumn = true,
    showInlineActions = true,
    showAlbumColumn = true,
    showMetaColumn = false,
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun JvmArtistInfoScreen(
    artistId: String,
    artistName: String,
    artistInfoViewModel: ArtistInfoViewModel = koinViewModel<ArtistInfoViewModel> {
        parametersOf(
            artistId,
            artistName
        )
    },
) {
    val musicPage =
        artistInfoViewModel.musicList.collectAsLazyPagingItems()
    val albumPageList =
        artistInfoViewModel.albumList.collectAsLazyPagingItems()
    val favoriteSet by artistInfoViewModel.favoriteSet.collectAsStateWithLifecycle(emptyList())
    val selectUiState by artistInfoViewModel.selectControl.uiState.collectAsStateWithLifecycle()
    val playDataType by artistInfoViewModel.musicController.playDataTypeFlow.collectAsStateWithLifecycle()
    val playState by artistInfoViewModel.musicController.stateFlow.collectAsStateWithLifecycle()
    val currentPlayingMusicIdFlow = remember(artistInfoViewModel) {
        artistInfoViewModel.musicController.musicInfoFlow.map { musicInfo ->
            musicInfo?.itemId
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val navigator = LocalNavigator.current

    // 相似艺术家列表复用统一的艺术家打开逻辑。
    val artistInfoLoader = rememberMusicArtistInfoLoader(artistInfoViewModel.dataSourceManager)
    val artistClickHandler = rememberMusicArtistClickHandler(
        artistList = artistInfoLoader.artistList,
        onLoadArtistInfos = artistInfoLoader.loadArtistInfos,
    )
    val lazyListState = rememberLazyListState()
    val isSticking by remember(lazyListState) { lazyListState.isSticking(1) }
    var selectedTab by remember { mutableStateOf(TabListEnum.Music) }
    // 是否文本描述超过最大行数
    var isOverflow by remember { mutableStateOf(false) }
    // 是否打开描述信息页面
    var ifOpenDescribe by remember { mutableStateOf(false) }

    // 当前播放上下文是否为本艺术家，决定页头播放按钮的状态与文案
    val currentPlayingArtistId = artistInfoViewModel.musicPlayContext.musicPlayData
        ?.onMusicPlayParameter
        ?.artistId
    val isCurrentArtist =
        playDataType == MusicPlayTypeEnum.ARTIST && artistId == currentPlayingArtistId
    val isPlayingCurrentArtist = isCurrentArtist && playState == PlayStateEnum.Playing
    val artistPlayText = when {
        isPlayingCurrentArtist -> stringResource(Res.string.pause_playback)
        isCurrentArtist -> stringResource(Res.string.resume_playback)
        else -> stringResource(Res.string.play_all)
    }
    val artistPlayIcon = if (isPlayingCurrentArtist) {
        Res.drawable.pause_circle_24px
    } else {
        Res.drawable.play_circle_24px
    }
    // 艺术家收藏切换，页头与操作区共用同一份逻辑
    val toggleArtistFavorite: () -> Unit = {
        coroutineScope.launch {
            val ifFavorite = artistInfoViewModel.dataSourceManager.setFavoriteData(
                type = MusicTypeEnum.ARTIST,
                itemId = artistId,
                ifFavorite = artistInfoViewModel.ifFavorite
            )
            artistInfoViewModel.updateFavorite(ifFavorite)
        }
    }
    // 播放全部：正在播放本艺术家时切换暂停/继续，否则从首曲开始播放
    val playArtist: () -> Unit = {
        coroutineScope.launch {
            if (isCurrentArtist) {
                if (playState == PlayStateEnum.Pause) {
                    artistInfoViewModel.musicController.resume()
                } else {
                    artistInfoViewModel.musicController.pause()
                }
            } else {
                artistInfoViewModel.musicPlayContext.artist(
                    OnMusicPlayParameter(
                        musicId = "",
                        artistId = artistId,
                    ),
                    index = 0,
                    artistId = artistId,
                )
            }
        }
    }
    // 随机播放：先切换为随机模式，再以本艺术家为上下文开始播放
    val shuffleArtist: () -> Unit = {
        artistInfoViewModel.musicController.setPlayTypeData(PlayerModeEnum.RANDOM_PLAY)
        coroutineScope.launch {
            artistInfoViewModel.musicPlayContext.artist(
                OnMusicPlayParameter(
                    musicId = "",
                    artistId = artistId,
                ),
                index = 0,
                artistId = artistId,
            )
        }
    }

    ModalBottomSheetExtendComponent(
        modifier = Modifier.statusBarsPadding(),
        onIfDisplay = { ifOpenDescribe },
        onClose = { bool -> ifOpenDescribe = bool },
        titleText = artistName,
        dragHandle = { BottomSheetDefaults.DragHandle(height = 2.dp) }
    ) {
        LazyColumnParentComponent(
            verticalArrangement = Arrangement.Top,
            contentPadding = PaddingValues(
                horizontal = XyTheme.dimens.outerHorizontalPadding
            ),
        ) {
            item {
                BasicText(
                    text = artistInfoViewModel.artistDescribe ?: "",
                    modifier = Modifier,
                    style = MaterialTheme.typography.titleSmall.copy(lineHeight = 15.sp),
                    overflow = TextOverflow.Visible,
                )
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val gridSpacing = XyTheme.dimens.outerVerticalPadding
        val gridColumns = remember(maxWidth, gridSpacing) {
            (maxWidth.value / (MusicCardImageSize + gridSpacing).value)
                .toInt()
                .coerceAtLeast(1)
        }
        val artistCoverUrls = rememberArtistCoverUrls(artistInfoViewModel.artistInfoData)
        // 各分区数量：优先取艺术家档案，未加载完成时回退到已加载条目数
        val musicCount = artistInfoViewModel.artistInfoData?.musicCount ?: musicPage.itemCount
        val albumCount = artistInfoViewModel.artistInfoData?.albumCount ?: albumPageList.itemCount
        val similarCount = artistInfoViewModel.resemblanceArtistList.size
        val tabCounts = mapOf(
            TabListEnum.Music to musicCount,
            TabListEnum.Album to albumCount,
            TabListEnum.RESEMBLANCE_ARTIST to similarCount,
        )

        XyColumnScreen {
            TopAppBarComponent(
                title = {
                    AnimatedContent(
                        targetState = isSticking,
                        label = "artist_info_title",
                        transitionSpec = {
                            (fadeIn() togetherWith fadeOut()).using(
                                SizeTransform(clip = false)
                            )
                        }
                    ) { sticking ->
                        Text(
                            text = if (sticking) artistName else stringResource(Res.string.artist),
                            modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE)
                        )
                    }
                }
            )

            JvmLazyListComponent(
                lazyState = lazyListState,
                pagingItems = when (selectedTab) {
                    TabListEnum.Music -> musicPage
                    TabListEnum.Album -> albumPageList
                    TabListEnum.RESEMBLANCE_ARTIST -> null
                },
                lazyColumnBottom = if (selectedTab == TabListEnum.RESEMBLANCE_ARTIST) {
                    null
                } else {
                    { pagingUiState ->
                        jvmLazyColumnBottomComponent(
                            pagingUiState = pagingUiState,
                            retry = {
                                when (selectedTab) {
                                    TabListEnum.Music -> musicPage.retry()
                                    TabListEnum.Album -> albumPageList.retry()
                                    TabListEnum.RESEMBLANCE_ARTIST -> Unit
                                }
                            },
                        )
                    }
                }
            ) {
                item(key = "artist_hero") {
                    JvmArtistDesktopHero(
                        artistName = artistName,
                        description = artistInfoViewModel.artistDescribe,
                        coverUrl = artistCoverUrls.primaryUrl,
                        fallbackCoverUrl = artistCoverUrls.fallbackUrl,
                        ifFavorite = artistInfoViewModel.ifFavorite,
                        playText = artistPlayText,
                        playIcon = artistPlayIcon,
                        descriptionOverflow = isOverflow,
                        onOpenDescription = {
                            ifOpenDescribe = true
                        },
                        onDescriptionOverflow = { overflow ->
                            isOverflow = overflow
                        },
                        onPlayAll = playArtist,
                        onShuffle = shuffleArtist,
                        onToggleFavorite = toggleArtistFavorite,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = XyTheme.dimens.outerHorizontalPadding,
                                end = XyTheme.dimens.outerHorizontalPadding,
                                top = XyTheme.dimens.outerVerticalPadding,
                                bottom = XyTheme.dimens.innerVerticalPadding
                            )
                    )
                }

                // 吸顶章节导航：下划线选中态 + 当前分区操作
                stickyHeader(key = 1) {
                    JvmArtistSectionTabs(
                        selectedTab = selectedTab,
                        tabCounts = tabCounts,
                        onSelectTab = { selectedTab = it },
                    ) {
                        // 选择模式下列表内操作栏承载全选与批量操作，这里只保留入口/完成按钮
                        if (selectedTab == TabListEnum.Music) {
                            val isSelecting = selectUiState.isOpen
                            val actionText = if (isSelecting) {
                                stringResource(Res.string.selection_done)
                            } else {
                                stringResource(Res.string.select)
                            }
                            OutlinedButton(
                                onClick = {
                                    if (isSelecting) {
                                        artistInfoViewModel.selectControl.dismiss()
                                    } else {
                                        artistInfoViewModel.selectControl.show(true)
                                    }
                                },
                                modifier = Modifier.height(32.dp),
                                shape = RoundedCornerShape(XyTheme.dimens.corner),
                                contentPadding = PaddingValues(horizontal = 10.dp),
                            ) {
                                Icon(
                                    // 选择态切换为完成图标，明确当前点击会退出选择模式
                                    painter = painterResource(
                                        if (isSelecting) {
                                            Res.drawable.check_24px
                                        } else {
                                            Res.drawable.playlist_add_check_24px
                                        }
                                    ),
                                    contentDescription = actionText,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = actionText)
                            }
                        }
                    }
                }

                when (selectedTab) {
                    TabListEnum.Music -> {
                        item(key = "artist_music_head") {
                            JvmArtistSectionHead(
                                title = stringResource(Res.string.artist_all_tracks),
                                meta = "$musicCount${stringResource(Res.string.songs_count_suffix)}",
                            )
                        }
                        // 选择模式展开的批量操作栏，紧跟在“全部曲目”标题下方
                        item(key = "artist_selection_bar") {
                            AnimatedVisibility(
                                visible = selectUiState.isOpen,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut(),
                            ) {
                                JvmMusicSelectionActionBar(
                                    selectControl = artistInfoViewModel.selectControl,
                                    allMusicIds = {
                                        musicPage.itemSnapshotList.items.map { it.itemId }
                                    },
                                    dataSourceManager = artistInfoViewModel.dataSourceManager,
                                    musicPlayContext = artistInfoViewModel.musicPlayContext,
                                    db = artistInfoViewModel.db,
                                    downloadDb = artistInfoViewModel.downloadDb,
                                    musicController = artistInfoViewModel.musicController,
                                    downloaderManager = artistInfoViewModel.downloaderManager,
                                    modifier = Modifier.padding(
                                        start = XyTheme.dimens.outerHorizontalPadding,
                                        end = XyTheme.dimens.outerHorizontalPadding,
                                        bottom = XyTheme.dimens.innerVerticalPadding,
                                    ),
                                )
                            }
                        }
                        songTableItems(
                            tableKey = "artist_info_music",
                            pagingItems = musicPage,
                            columns = JvmArtistMusicTableColumns.copy(
                                showInlineActions = !selectUiState.isOpen,
                                showSelectionColumn = selectUiState.isOpen,
                            ),
                            ifFavorite = { music -> music.itemId in favoriteSet },
                            currentPlayingMusicIdFlow = currentPlayingMusicIdFlow,
                            isSelected = { music -> music.itemId in selectUiState.selectedMusicIds },
                            onSongClick = { index, music ->
                                if (selectUiState.isOpen) {
                                    artistInfoViewModel.selectControl.toggleSelection(
                                        music.itemId,
                                        onIsSelectAll = {
                                            selectUiState.selectedMusicIds.containsAll(
                                                musicPage.itemSnapshotList.items.map { it.itemId }
                                            )
                                        }
                                    )
                                } else {
                                    coroutineScope.launch {
                                        artistInfoViewModel.musicPlayContext.artist(
                                            onMusicPlayParameter = OnMusicPlayParameter(
                                                musicId = music.itemId,
                                                artistId = artistId,
                                            ),
                                            index = index,
                                            artistId = artistId,
                                        )
                                    }
                                }
                            },
                            onOpenArtist = artistClickHandler::openMusicArtists,
                            showViewArtistMenuItem = false,
                            onOpenAlbum = { music ->
                                if (music.album.isNotBlank()) {
                                    navigator.navigate(
                                        AlbumInfo(
                                            music.album,
                                            MusicDataTypeEnum.ALBUM,
                                        )
                                    )
                                }
                            },
                            onFavoriteClick = { music ->
                                artistInfoViewModel.musicController.invokingOnFavorite(music.itemId)
                            },
                            onDownloadClick = { music ->
                                artistInfoViewModel.downloadMusic(music)
                            },
                            onMoreClick = { music ->
                                coroutineScope.launch {
                                    music.show()
                                }
                            },
                            onSelectionClick = { musicId ->
                                artistInfoViewModel.selectControl.toggleSelection(
                                    musicId,
                                    onIsSelectAll = {
                                        selectUiState.selectedMusicIds.containsAll(
                                            musicPage.itemSnapshotList.items.map { it.itemId }
                                        )
                                    }
                                )
                            }
                        )
                    }

                    TabListEnum.Album -> {
                        item(key = "artist_album_head") {
                            JvmArtistSectionHead(
                                title = stringResource(Res.string.album),
                                meta = stringResource(Res.string.album_count_format, albumCount),
                            )
                        }
                        artistAlbumGridItems(
                            albumPageList = albumPageList,
                            gridColumns = gridColumns,
                            onOpenAlbum = { albumId ->
                                navigator.navigate(
                                    AlbumInfo(
                                        albumId,
                                        MusicDataTypeEnum.ALBUM
                                    )
                                )
                            }
                        )
                    }

                    TabListEnum.RESEMBLANCE_ARTIST -> {
                        item(key = "artist_similar_head") {
                            JvmArtistSectionHead(
                                title = stringResource(Res.string.resemblance_artist),
                                meta = stringResource(Res.string.artist_count_format, similarCount),
                            )
                        }
                        artistGridItems(
                            artistList = artistInfoViewModel.resemblanceArtistList,
                            gridColumns = gridColumns,
                            onOpenArtist = { artist ->
                                artistClickHandler.openArtist(artist.artistId, artist.name)
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 艺术家详情页头：唱片式主视觉 + 身份信息 + 简介 + 播放操作。
 */
@Composable
private fun JvmArtistDesktopHero(
    artistName: String,
    description: String?,
    coverUrl: String?,
    fallbackCoverUrl: String?,
    ifFavorite: Boolean,
    playText: String,
    playIcon: DrawableResource,
    descriptionOverflow: Boolean,
    onOpenDescription: () -> Unit,
    onDescriptionOverflow: (Boolean) -> Unit,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val descriptionText = description ?: stringResource(Res.string.no_description)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(34.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        JvmArtistDiscPortrait(
            coverUrl = coverUrl,
            fallbackCoverUrl = fallbackCoverUrl,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = artistName,
                modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = descriptionText,
                modifier = Modifier.debounceClickable(enabled = descriptionOverflow) {
                    onOpenDescription()
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                onTextLayout = { textLayoutResult ->
                    onDescriptionOverflow(textLayoutResult.hasVisualOverflow)
                },
            )
            if (descriptionOverflow) {
                TextButton(
                    onClick = onOpenDescription,
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.expand_description),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            // 主操作区：播放全部为主按钮，随机播放为次按钮，收藏为图标按钮
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                Button(
                    onClick = onPlayAll,
                    shape = RoundedCornerShape(XyTheme.dimens.corner),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Icon(
                        painter = painterResource(playIcon),
                        contentDescription = playText,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = playText,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                OutlinedButton(
                    onClick = onShuffle,
                    shape = RoundedCornerShape(XyTheme.dimens.corner),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.shuffle_24px),
                        contentDescription = stringResource(Res.string.random_play),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = stringResource(Res.string.random_play),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                FavoriteIconButton(
                    isFavorite = ifFavorite,
                    onClick = onToggleFavorite,
                    normalTint = XyTheme.extendedColors.favorite,
                )
            }
        }
    }
}

/**
 * 唱片式主视觉：外圈同心纹唱片 + 内圈艺术家封面。
 */
@Composable
private fun JvmArtistDiscPortrait(
    coverUrl: String?,
    fallbackCoverUrl: String?,
) {
    val discColor = MaterialTheme.colorScheme.surfaceContainerLowest
    val ringColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)

    Box(
        modifier = Modifier.size(JvmArtistInfoDesktopCoverSize),
        contentAlignment = Alignment.Center,
    ) {
        // 唱片纹路：从封面外缘向外逐圈绘制，保持设计稿的黑胶质感
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerRadius = size.minDimension / 2
            drawCircle(color = discColor, radius = centerRadius)
            var ringRadius = centerRadius - 1.dp.toPx()
            val step = 5.dp.toPx()
            while (ringRadius > JvmArtistInfoDesktopArtSize.toPx() / 2) {
                drawCircle(
                    color = ringColor,
                    radius = ringRadius,
                    style = Stroke(width = 1f)
                )
                ringRadius -= step
            }
        }
        XyImage(
            modifier = Modifier
                .size(JvmArtistInfoDesktopArtSize)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        // 艺术家封面占位渐变从主题扩展颜色读取
                        colors = listOf(
                            XyTheme.extendedColors.artistCoverGradientStart,
                            XyTheme.extendedColors.artistCoverGradientEnd,
                        ),
                        start = Offset(x = Float.POSITIVE_INFINITY, y = 0f),
                        end = Offset(x = 0f, y = Float.POSITIVE_INFINITY),
                    )
                ),
            model = coverUrl,
            backModel = fallbackCoverUrl,
            placeholder = Res.drawable.artrist_info,
            error = Res.drawable.artrist_info,
            fallback = Res.drawable.artrist_info,
            contentDescription = stringResource(Res.string.artist_cover),
        )
    }
}

/**
 * 艺术家详情章节导航：下划线选中态 + 分区数量 + 右侧当前分区操作。
 */
@Composable
private fun JvmArtistSectionTabs(
    selectedTab: TabListEnum,
    tabCounts: Map<TabListEnum, Int>,
    onSelectTab: (TabListEnum) -> Unit,
    trailing: @Composable RowScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = XyTheme.dimens.outerHorizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            TabListEnum.entries.forEach { tab ->
                JvmArtistSectionTab(
                    text = stringResource(tab.message),
                    count = tabCounts[tab] ?: 0,
                    selected = selectedTab == tab,
                    onClick = { onSelectTab(tab) },
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            trailing()
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

/**
 * 单个章节标签：名称 + 数量 + 选中下划线。
 */
@Composable
private fun JvmArtistSectionTab(
    text: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Column(
        modifier = Modifier
            .width(IntrinsicSize.Max)
            .clip(RoundedCornerShape(XyTheme.dimens.corner / 2))
            .clickable(onClick = onClick)
            .padding(top = 15.dp, bottom = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = text,
                color = contentColor,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (count > 0) {
                Text(
                    text = count.toString(),
                    modifier = Modifier.padding(start = 7.dp),
                    color = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 0.6.sp,
                    maxLines = 1,
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(
                    color = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        Color.Transparent
                    },
                    shape = RoundedCornerShape(2.dp)
                )
        )
    }
}

/**
 * 分区标题栏：标题 + 数量说明。
 */
@Composable
private fun JvmArtistSectionHead(
    title: String,
    meta: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = XyTheme.dimens.outerHorizontalPadding,
                vertical = XyTheme.dimens.innerVerticalPadding / 2,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = meta,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

/**
 * 专辑网格分页数据。
 */
private fun LazyListScope.artistAlbumGridItems(
    albumPageList: LazyPagingItems<XyAlbum>,
    gridColumns: Int,
    onOpenAlbum: (String) -> Unit,
) {
    val rowCount = (albumPageList.itemCount + gridColumns - 1) / gridColumns

    items(
        count = rowCount,
        key = { rowIndex -> "artist_album_row_$rowIndex" },
        contentType = { MusicTypeEnum.ALBUM.code },
    ) { rowIndex ->
        ArtistInfoCardGridRow {
            repeat(gridColumns) { columnIndex ->
                val itemIndex = rowIndex * gridColumns + columnIndex
                val album = if (itemIndex < albumPageList.itemCount) {
                    albumPageList[itemIndex]
                } else {
                    null
                }
                if (album != null) {
                    MusicAlbumCardComponent(
                        modifier = Modifier.width(MusicCardImageSize),
                        album = album,
                        imageSize = MusicCardImageSize,
                        onRouter = onOpenAlbum,
                    )
                } else {
                    Box(modifier = Modifier.width(MusicCardImageSize))
                }
            }
        }
    }
}

/**
 * 相似艺术家网格数据。
 */
private fun LazyListScope.artistGridItems(
    artistList: List<XyArtist>,
    gridColumns: Int,
    onOpenArtist: (XyArtist) -> Unit,
) {
    val rowCount = (artistList.size + gridColumns - 1) / gridColumns

    items(
        count = rowCount,
        key = { rowIndex -> "artist_resemblance_row_$rowIndex" },
        contentType = { MusicTypeEnum.ARTIST.code },
    ) { rowIndex ->
        ArtistInfoCardGridRow {
            repeat(gridColumns) { columnIndex ->
                val itemIndex = rowIndex * gridColumns + columnIndex
                val artist = artistList.getOrNull(itemIndex)
                if (artist != null) {
                    MusicArtistCardComponent(
                        modifier = Modifier.width(MusicCardImageSize),
                        artist = artist,
                        imageSize = MusicCardImageSize,
                        enabled = true,
                    ) {
                        onOpenArtist(artist)
                    }
                } else {
                    Box(modifier = Modifier.width(MusicCardImageSize))
                }
            }
        }
    }
    item {
        LazyLoadingAndStatus(
            text = stringResource(Res.string.reached_bottom),
            ifLoading = false
        )
    }
}

/**
 * 网格行容器：统一的水平内边距与间距。
 */
@Composable
private fun ArtistInfoCardGridRow(
    content: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = XyTheme.dimens.outerHorizontalPadding,
                vertical = XyTheme.dimens.innerVerticalPadding / 2,
            ),
        horizontalArrangement = Arrangement.spacedBy(
            XyTheme.dimens.outerVerticalPadding,
            Alignment.CenterHorizontally
        ),
        verticalAlignment = Alignment.Top,
    ) {
        content()
    }
}
