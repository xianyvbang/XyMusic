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

package cn.xybbz.ui.components


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.api.client.FavoriteCoordinator
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.common.enums.PlayStateEnum
import cn.xybbz.compositionLocal.LocalMainViewModel
import cn.xybbz.config.image.rememberPlayMusicCoverUrls
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.entity.data.ext.joinToString
import cn.xybbz.localdata.data.music.XyPlayMusic
import cn.xybbz.localdata.enums.PlayerTypeEnum
import cn.xybbz.ui.components.lrc.LrcViewNewCompose
import cn.xybbz.ui.ext.debounceClickable
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.ModalBottomSheetExtendFillMaxSizeComponent
import cn.xybbz.ui.xy.XyColumn
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyImage
import cn.xybbz.ui.xy.XyRow
import cn.xybbz.viewmodel.MusicPlayerViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.album_cover
import xymusic_kmp.composeapp.generated.resources.close_player_screen
import xymusic_kmp.composeapp.generated.resources.disc_placeholder
import xymusic_kmp.composeapp.generated.resources.favorite_24px
import xymusic_kmp.composeapp.generated.resources.favorite_border_24px
import xymusic_kmp.composeapp.generated.resources.favorite_button
import xymusic_kmp.composeapp.generated.resources.keyboard_arrow_down_24px
import xymusic_kmp.composeapp.generated.resources.more_vert_24px
import xymusic_kmp.composeapp.generated.resources.music_list
import xymusic_kmp.composeapp.generated.resources.next_track
import xymusic_kmp.composeapp.generated.resources.other_operations_button_suffix
import xymusic_kmp.composeapp.generated.resources.pause
import xymusic_kmp.composeapp.generated.resources.pause_24px
import xymusic_kmp.composeapp.generated.resources.play_arrow_24px
import xymusic_kmp.composeapp.generated.resources.playing
import xymusic_kmp.composeapp.generated.resources.previous_track
import xymusic_kmp.composeapp.generated.resources.queue_music_24px
import xymusic_kmp.composeapp.generated.resources.skip_next_24px
import xymusic_kmp.composeapp.generated.resources.skip_previous_24px
import xymusic_kmp.composeapp.generated.resources.unknown_artist
import kotlin.math.roundToInt
import cn.xybbz.ui.xy.XyIconButton as IconButton


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicPlayerComponent(
    music: XyPlayMusic,
    picByte: ByteArray? = null,
    sheetStateR: SheetState,
    toNext: () -> Unit,
    backNext: () -> Unit,
    onSetState: (Boolean) -> Unit
) {
    val mainViewModel = LocalMainViewModel.current
    val coroutineScope = rememberCoroutineScope()
    /*  val sheetStateR = rememberModalBottomSheetState(
          skipPartiallyExpanded = true
      )*/

    val horPagerState =
        rememberPagerState {
            3
        }
    val listState = rememberLazyListState()
    val similarPopularListState = rememberLazyListState()

    val bottomSheetScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // 这里你可以根据需要自定义滚动逻辑
                if (horPagerState.currentPage == 1)
                    listState.dispatchRawDelta(-available.y)
                else if (horPagerState.currentPage == 2)
                    similarPopularListState.dispatchRawDelta(-available.y)
                return Offset(0f, available.y)  // 表示不消费滚动事件
            }
        }
    }

    ModalBottomSheetExtendFillMaxSizeComponent(
        modifier = Modifier.nestedScroll(bottomSheetScrollConnection),
        bottomSheetState = sheetStateR,
        onIfDisplay = { mainViewModel.sheetState },
        onClose = {
            mainViewModel.putIterations(1)
            mainViewModel.putSheetState(false)
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = { WindowInsets.captionBar },
    ) {
        MusicPlayerScreen(
            musicDetail = music,
            picByte = picByte,
            onCloseSheet = {
                coroutineScope.launch {
                    sheetStateR.hide()
                }.invokeOnCompletion {
                    mainViewModel.putIterations(1)
                    mainViewModel.putSheetState(false)
                }
            },
            onSeekToNext = toNext,
            onSeekBack = backNext,
            onSetState = onSetState,
            lrcListState = listState,
            similarPopularListState = similarPopularListState,
            horPagerState = horPagerState
        )
    }
}


/**
 * 播放页面弹窗
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicPlayerScreen(
    musicDetail: XyPlayMusic,
    picByte: ByteArray? = null,
    musicPlayerViewModel: MusicPlayerViewModel = koinViewModel<MusicPlayerViewModel>(),
    onCloseSheet: () -> Unit,
    onSeekToNext: () -> Unit,
    onSeekBack: () -> Unit,
    onSetState: (Boolean) -> Unit,
    lrcListState: LazyListState = rememberLazyListState(),
    similarPopularListState: LazyListState = rememberLazyListState(),
    horPagerState: PagerState
) {


    val cacheScheduleData by musicPlayerViewModel.downloadCacheController.cacheSchedule.collectAsStateWithLifecycle()

    val favoriteList by musicPlayerViewModel.favoriteSet.collectAsStateWithLifecycle(emptyList())
    val coverUrls = rememberPlayMusicCoverUrls(
        musicDetail,
        musicPlayerViewModel.musicController.coverRefreshVersion
    )

    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        XyImage(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize(),
            model = coverUrls.primaryUrl ?: picByte,
            backModel = coverUrls.fallbackUrl ?: picByte,
            alpha = 0.2f,
            contentDescription = stringResource(Res.string.album_cover),
        )
        XyColumnScreen(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(
                    top = WindowInsets.statusBars.asPaddingValues()
                        .calculateTopPadding()
                ),
            background = Color.Transparent
        ) {
            TopAppBarComponent(
                modifier = Modifier,
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        musicPlayerViewModel.dataList.forEachIndexed { index, item ->

                            TextButton(onClick = {
                                coroutineScope
                                    .launch {
                                        horPagerState.animateScrollToPage(index)
                                    }
                            }) {
                                Text(
                                    text = stringResource(item),
                                    maxLines = 2,
                                    color = if (horPagerState.currentPage == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            onCloseSheet()
                        },
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.keyboard_arrow_down_24px),
                            contentDescription = stringResource(Res.string.close_player_screen)
                        )
                    }
                }, actions = {
                    IconButton(
                        onClick = {
                        },
                    ) {
                        // todo 加个隐藏按钮操作,打开开发者页面
                    }
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                HorizontalPager(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(4f),
                    state = horPagerState,
                    // 在底部弹窗里关闭 Pager 的边缘回弹，避免翻页落位后再次左右抖动
                    overscrollEffect = null
                ) { page ->
                    when (page) {
                        0 -> {
                            Box(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                XyImage(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(300.dp)
                                        .aspectRatio(1F)
                                        .clip(
                                            CircleShape
                                        ),
                                    model = coverUrls.primaryUrl ?: picByte,
                                    backModel = coverUrls.fallbackUrl ?: picByte,
                                    placeholder = painterResource(Res.drawable.disc_placeholder),
                                    /*.graphicsLayer(rotationZ = rotationState)*/
                                    error = painterResource(Res.drawable.disc_placeholder),
                                    fallback = painterResource(Res.drawable.disc_placeholder),
                                    contentDescription = stringResource(Res.string.album_cover),
                                )
                            }

                        }

                        1 -> {
                            LrcViewNewCompose(
                                listState = lrcListState,
                                onSetLrcOffset = { offsetMs ->
                                    coroutineScope.launch {
                                        musicPlayerViewModel.lrcServer.updateLrcConfig(offsetMs)
                                    }
                                }
                            )
                        }

                        else -> {
                            MusicPlayerSimilarPopularComponent(
                                listState = similarPopularListState,
                                onFavoriteSet = { emptySet() },
                                onDownloadMusicIds = { emptyList() },
                                playMusicList = musicPlayerViewModel.musicController.originMusicList,
                                onAddPlayMusic = { musicPlayerViewModel.addNextPlayer(it) }
                            )
                        }
                    }
                }


                XyColumn(
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier
                    /*.weight(2.8f)*/,
                    paddingValues = PaddingValues(0.dp),
                    clipSize = 0.dp,
                    backgroundColor = Color.Transparent
                ) {
//                    Spacer(modifier = Modifier.height(XyTheme.dimens.corner))
                    XyRow {
                        Column(
                            modifier = Modifier
                                .width(200.dp)
                        ) {
                            Text(
                                text = musicDetail.name,
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.W700,
                                lineHeight = 31.86.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Visible,
                                modifier = Modifier
                                    .basicMarquee(iterations = Int.MAX_VALUE)
                            )

                            Text(
                                text = if (musicDetail.artists.isNullOrEmpty()) stringResource(Res.string.unknown_artist) else musicDetail.artists?.joinToString()
                                    ?: "",
                                color = Color(0xff7B7B8B),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.W400,
                                lineHeight = 15.93.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.basicMarquee(
                                    iterations = 0
                                )
                            )
                        }

                        FavoriteMusicIconComponent(
                            musicDetail = musicDetail,
                            dataSourceManager = musicPlayerViewModel.dataSourceManager,
                            musicController = musicPlayerViewModel.musicController,
                            onFavoriteMusicIdSet = { favoriteList }
                        )
                        IconButton(
                            modifier = Modifier.offset(x = (10).dp),
                            onClick = {
                                coroutineScope.launch {
                                    val itemId =
                                        musicPlayerViewModel.musicController.musicInfo?.itemId
                                    itemId?.let {
                                        musicPlayerViewModel.dataSourceManager.selectMusicInfoById(
                                            it
                                        )?.show()
                                    }
                                }
                            },
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.more_vert_24px),
                                contentDescription = "${musicPlayerViewModel.musicController.musicInfo?.name}${
                                    stringResource(
                                        Res.string.other_operations_button_suffix
                                    )
                                }"
                            )
                        }
                    }
                    AnimatedVisibility(
                        visible = !musicPlayerViewModel.lrcServer.lrcText.isNullOrBlank() && horPagerState.currentPage == 0
                    ) {
                        XyRow(modifier = Modifier/*.height(30.dp)*/) {
                            LrcViewOneComponent(lrcText = musicPlayerViewModel.lrcServer.lrcText)
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        PlayerCurrentPosition(
                            musicController = musicPlayerViewModel.musicController,
                            onCacheProgress = {
                                cacheScheduleData
                            })
                        Spacer(modifier = Modifier.height(XyTheme.dimens.outerVerticalPadding))
                        XyRow(modifier = Modifier/*.weight(1f)*/) {
                            PlayerTypeComponent(musicController = musicPlayerViewModel.musicController)
                            Icon(
                                painter = painterResource(Res.drawable.skip_previous_24px),
                                contentDescription = stringResource(Res.string.previous_track),
                                modifier = Modifier
                                    .size(30.dp, 35.dp)
                                    .debounceClickable {
                                        coroutineScope.launch {
                                            onSeekBack()
                                        }
                                    },
                                tint = Color.White
                            )

                            PlayerStateComponent(musicController = musicPlayerViewModel.musicController)

                            Icon(
                                painter = painterResource(Res.drawable.skip_next_24px),
                                contentDescription = stringResource(Res.string.next_track),
                                modifier = Modifier
                                    .size(30.dp, 35.dp)
                                    .debounceClickable {
                                        coroutineScope.launch {
                                            onSeekToNext()
                                        }
                                    },
                                tint = Color.White,
                            )

                            IconButton(
                                onClick = {
                                    if (musicPlayerViewModel.musicController.originMusicList.isNotEmpty()) {
                                        onSetState(true)
                                    }
                                },
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.queue_music_24px),
                                    contentDescription = stringResource(Res.string.music_list)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(XyTheme.dimens.outerVerticalPadding * 4))
                    }
                }

            }

        }
    }

}

/**
 * 播放类型
 */
@Composable
private fun PlayerTypeComponent(
    musicController: MusicCommonController
) {
    val mainViewModel = LocalMainViewModel.current
    IconButton(
        onClick = {
            mainViewModel.setNowPlayerTypeData()
        },
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (musicController.playType.code == PlayerTypeEnum.SINGLE_LOOP.code) {
                Text(text = "1", fontSize = 10.sp)
            }
            Icon(
                painter = mainViewModel.iconList[musicController.playType.code].icon,
                contentDescription = stringResource(mainViewModel.iconList[musicController.playType.code].message),
            )
        }

    }
}

/**
 * 音乐进度
 */
@Composable
private fun PlayerCurrentPosition(
    musicController: MusicCommonController,
    onCacheProgress: () -> Float
) {

    val currentPosition by musicController.progressStateFlow.collectAsStateWithLifecycle()

    XyColumn(
        backgroundColor = Color.Transparent,
        clipSize = 0.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {

            MusicProgressBar(
                currentTime = currentPosition,
                progressStateFlow = musicController.progressStateFlow,
                totalTime = musicController.duration,
                cacheProgress = onCacheProgress(),
                onProgressChanged = { newProgress ->
                    musicController.seekTo(
                        (musicController.duration * newProgress).roundToInt().toLong()
                    )
                })
        }
    }
}

/**
 * 音乐状态
 */
@Composable
fun PlayerStateComponent(
    size: Dp = 60.dp,
    musicController: MusicCommonController
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
    ) {

        Icon(
            painter = painterResource(if (musicController.state == PlayStateEnum.Playing
                || musicController.state == PlayStateEnum.Loading
            ) Res.drawable.pause_24px else Res.drawable.play_arrow_24px) ,
            contentDescription = if (musicController.state == PlayStateEnum.Playing) stringResource(
                Res.string.playing
            ) else stringResource(Res.string.pause),
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .clickable {
                    if (musicController.state != PlayStateEnum.Pause) {
                        musicController.pause()
                    } else {
                        musicController.resume()
                    }

                }
        )
        if (musicController.state == PlayStateEnum.Loading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .clip(CircleShape),
                strokeWidth = size
            )
        }
    }
}

/**
 * 音乐收藏
 */
@Composable
private fun FavoriteMusicIconComponent(
    musicDetail: XyPlayMusic,
    dataSourceManager: DataSourceManager,
    musicController: MusicCommonController,
    onFavoriteMusicIdSet: () -> List<String>
) {
    val coroutineScope = rememberCoroutineScope()

    IconButton(
        onClick = {
            coroutineScope.launch {
                FavoriteCoordinator.setFavoriteData(
                    dataSourceManager = dataSourceManager,
                    type = MusicTypeEnum.MUSIC,
                    itemId = musicDetail.itemId,
                    ifFavorite = musicDetail.itemId in onFavoriteMusicIdSet(),
                    musicController = musicController
                )
            }
        },
    ) {
        Icon(
            modifier = Modifier
                .size(60.dp),
            painter =
               painterResource( if (musicDetail.itemId in onFavoriteMusicIdSet())
                   Res.drawable.favorite_24px
               else
                   Res.drawable.favorite_border_24px),
            contentDescription = stringResource(Res.string.favorite_button),
            tint = if (musicDetail.itemId in onFavoriteMusicIdSet()) Color.Red else LocalContentColor.current
        )
    }

}


