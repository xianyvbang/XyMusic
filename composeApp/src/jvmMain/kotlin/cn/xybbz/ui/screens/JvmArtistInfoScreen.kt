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
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.common.enums.TabListEnum
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.config.image.rememberArtistCoverUrls
import cn.xybbz.config.select.SelectControl
import cn.xybbz.extension.isSticking
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.data.artist.XyArtist
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import cn.xybbz.router.AlbumInfo
import cn.xybbz.ui.components.FavoriteIconButton
import cn.xybbz.ui.components.JvmLazyListComponent
import cn.xybbz.ui.components.LazyLoadingAndStatus
import cn.xybbz.ui.components.MusicAlbumCardComponent
import cn.xybbz.ui.components.MusicArtistCardComponent
import cn.xybbz.ui.components.MusicItemComponent
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.XySelectAllComponent
import cn.xybbz.ui.components.jvmLazyColumnBottomComponent
import cn.xybbz.ui.components.rememberMusicArtistClickHandler
import cn.xybbz.ui.components.show
import cn.xybbz.ui.common.UiConstants.MusicCardImageSize
import cn.xybbz.ui.ext.debounceClickable
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.windows.DesktopTooltipIconButton
import cn.xybbz.ui.xy.LazyColumnParentComponent
import cn.xybbz.ui.xy.ModalBottomSheetExtendComponent
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyImage
import cn.xybbz.ui.xy.XyRow
import cn.xybbz.viewmodel.ArtistInfoViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.album
import xymusic_kmp.composeapp.generated.resources.arrow_back_24px
import xymusic_kmp.composeapp.generated.resources.artist
import xymusic_kmp.composeapp.generated.resources.artist_cover
import xymusic_kmp.composeapp.generated.resources.artrist_info
import xymusic_kmp.composeapp.generated.resources.close_24px
import xymusic_kmp.composeapp.generated.resources.close_selection
import xymusic_kmp.composeapp.generated.resources.no_description
import xymusic_kmp.composeapp.generated.resources.playlist_add_check_24px
import xymusic_kmp.composeapp.generated.resources.reached_bottom
import xymusic_kmp.composeapp.generated.resources.return_home
import xymusic_kmp.composeapp.generated.resources.select
import xymusic_kmp.composeapp.generated.resources.songs_count_suffix
import cn.xybbz.ui.xy.XyIconButton as IconButton

private val JvmArtistInfoDesktopCoverSize = 232.dp

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
    }
) {
    val musicPage =
        artistInfoViewModel.musicList.collectAsLazyPagingItems()
    val albumPageList =
        artistInfoViewModel.albumList.collectAsLazyPagingItems()
    val favoriteSet by artistInfoViewModel.favoriteSet.collectAsStateWithLifecycle(emptyList())
    val downloadMusicIds by artistInfoViewModel.downloadMusicIdsFlow.collectAsStateWithLifecycle(
        emptyList()
    )
    val selectUiState by artistInfoViewModel.selectControl.uiState.collectAsStateWithLifecycle()
    val playbackState by artistInfoViewModel.musicController.playbackStateFlow.collectAsStateWithLifecycle()

    val coroutineScope = rememberCoroutineScope()
    val navigator = LocalNavigator.current

    // 相似艺术家列表复用统一的艺术家打开逻辑。
    val artistClickHandler = rememberMusicArtistClickHandler()
    val lazyListState = rememberLazyListState()
    val isSticking by remember(lazyListState) { lazyListState.isSticking(1) }
    var selectedTab by remember { mutableStateOf(TabListEnum.Music) }
    //是否文本描述超过最大行数
    var isOverflow by remember { mutableStateOf(false) }
    //是否打开描述信息页面
    var ifOpenDescribe by remember { mutableStateOf(false) }

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
                },
                actions = {
                    FavoriteIconButton(
                        isFavorite = artistInfoViewModel.ifFavorite,
                        onClick = {
                            coroutineScope.launch {
                                val ifFavorite =
                                    artistInfoViewModel.dataSourceManager.setFavoriteData(
                                        type = MusicTypeEnum.ARTIST,
                                        itemId = artistId,
                                        ifFavorite = artistInfoViewModel.ifFavorite
                                    )
                                artistInfoViewModel.updateFavorite(ifFavorite)
                            }
                        },
                        normalTint = Color.Red,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navigator.goBack()
                        },
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.arrow_back_24px),
                            contentDescription = stringResource(Res.string.return_home)
                        )
                    }
                },
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
                item {
                    JvmArtistDesktopHeader(
                        artistName = artistName,
                        description = artistInfoViewModel.artistDescribe,
                        musicCount = artistInfoViewModel.artistInfoData?.musicCount,
                        albumCount = artistInfoViewModel.artistInfoData?.albumCount,
                        coverUrl = artistCoverUrls.primaryUrl,
                        fallbackCoverUrl = artistCoverUrls.fallbackUrl,
                        descriptionOverflow = isOverflow,
                        onOpenDescription = {
                            ifOpenDescribe = true
                        },
                        onDescriptionOverflow = { overflow ->
                            isOverflow = overflow
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = XyTheme.dimens.outerHorizontalPadding,
                                end = XyTheme.dimens.outerHorizontalPadding,
                                top = XyTheme.dimens.outerVerticalPadding,
                                bottom = XyTheme.dimens.outerVerticalPadding
                            )
                    )
                }
                stickyHeader(key = 1) {
                    PrimaryTabRow(
                        containerColor = MaterialTheme.colorScheme.background,
                        selectedTabIndex = TabListEnum.entries.indexOf(selectedTab),
                        divider = {},
                    ) {
                        TabListEnum.entries.forEach { tab ->
                            Tab(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(XyTheme.dimens.corner)),
                                selected = selectedTab == tab,
                                onClick = {
                                    selectedTab = tab
                                },
                                text = {
                                    Text(
                                        text = stringResource(tab.message),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            )
                        }
                    }
                }

                when (selectedTab) {
                    TabListEnum.Music -> {
                        item {
                            JvmArtistMusicListOperation(
                                selectControl = artistInfoViewModel.selectControl,
                                onSelectAll = {
                                    artistInfoViewModel.selectControl.toggleSelectionAll(
                                        musicPage.itemSnapshotList.items.map { it.itemId })
                                },
                                ifOpenSelect = selectUiState.isOpen,
                                isSelectAll = selectUiState.isSelectAll,
                                sortContent = {}
                            )
                        }
                        items(
                            musicPage.itemCount,
                            key = musicPage.itemKey { item -> item.itemId },
                            contentType = musicPage.itemContentType { MusicTypeEnum.MUSIC }
                        ) { index ->
                            musicPage[index]?.let { music ->
                                MusicItemComponent(
                                    music = music,
                                    onIfFavorite = {
                                        music.itemId in favoriteSet
                                    },
                                    ifDownload = music.itemId in downloadMusicIds,
                                    ifPlay = playbackState.musicInfo?.itemId == music.itemId,
                                    backgroundColor = Color.Transparent,
                                    trailingOnClick = {
                                        music.show()
                                    },
                                    onMusicPlay = {
                                        coroutineScope.launch {
                                            artistInfoViewModel.musicPlayContext.artist(
                                                onMusicPlayParameter = it.copy(
                                                    artistId = artistId
                                                ),
                                                index = index,
                                                artistId = artistId
                                            )
                                        }
                                    },
                                    ifSelect = selectUiState.isOpen,
                                    ifSelectCheckBox = { music.itemId in selectUiState.selectedMusicIds },
                                    trailingOnSelectClick = { _ ->
                                        artistInfoViewModel.selectControl.toggleSelection(
                                            music.itemId,
                                            onIsSelectAll = {
                                                selectUiState.selectedMusicIds.containsAll(
                                                    musicPage.itemSnapshotList.items.map { it.itemId }
                                                )
                                            }
                                        )
                                    }
                                )
                            }
                        }
                    }

                    TabListEnum.Album -> {
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


@Composable
private fun JvmArtistDesktopHeader(
    artistName: String,
    description: String?,
    musicCount: Int?,
    albumCount: Int?,
    coverUrl: String?,
    fallbackCoverUrl: String?,
    descriptionOverflow: Boolean,
    onOpenDescription: () -> Unit,
    onDescriptionOverflow: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val songsCountSuffix = stringResource(Res.string.songs_count_suffix)
    val albumText = stringResource(Res.string.album)
    val descriptionText = description ?: stringResource(Res.string.no_description)
    val subText = buildList {
        musicCount?.takeIf { it > 0 }?.let {
            add("$it$songsCountSuffix")
        }
        albumCount?.takeIf { it > 0 }?.let {
            add("$it$albumText")
        }
    }.joinToString(" • ")

    Row(
        modifier = modifier.height(JvmArtistInfoDesktopCoverSize),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        XyImage(
            modifier = Modifier
                .size(JvmArtistInfoDesktopCoverSize)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xff10b981), Color(0xff06b6d4)),
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
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
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
            if (subText.isNotBlank()) {
                Text(
                    text = subText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
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
        }
    }
}


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


/**
 * 音乐列表操作栏
 */
@Composable
private fun JvmArtistMusicListOperation(
    selectControl: SelectControl,
    ifOpenSelect: Boolean,
    isSelectAll: Boolean,
    onSelectAll: () -> Unit,
    sortContent: @Composable () -> Unit
) {
    XyRow(
        modifier = Modifier
            .debounceClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = ifOpenSelect,
            ) {
                onSelectAll()
            }
            .height(XyTheme.dimens.itemHeight),
        paddingValues = PaddingValues(
            horizontal = XyTheme.dimens.outerHorizontalPadding
        ),
        horizontalArrangement = if (ifOpenSelect) Arrangement.SpaceBetween else Arrangement.End
    ) {


        if (ifOpenSelect) {
            XySelectAllComponent(
                isSelectAll = isSelectAll,
                onSelectAll = onSelectAll
            )
            IconButton(onClick = {
                selectControl.dismiss()
            }) {
                Icon(
                    painter = painterResource(Res.drawable.close_24px),
                    contentDescription = stringResource(Res.string.close_selection)
                )
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                sortContent()

                val selectText = stringResource(Res.string.select)
                DesktopTooltipIconButton(
                    tooltip = selectText,
                    onClick = {
                        selectControl.show(true)
                    },
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.playlist_add_check_24px),
                        contentDescription = selectText
                    )
                }
            }
        }

    }
}
