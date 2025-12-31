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
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.QueueMusic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cn.xybbz.R
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.common.enums.PlayStateEnum
import cn.xybbz.common.music.MusicController
import cn.xybbz.compositionLocal.LocalMainViewModel
import cn.xybbz.config.favorite.FavoriteRepository
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
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicPlayerComponent(
    music: XyPlayMusic,
    picByte: ByteArray? = null,
    toNext: () -> Unit,
    backNext: () -> Unit,
    onSetState: (Boolean) -> Unit
) {
    val mainViewModel = LocalMainViewModel.current
    val coroutineScope = rememberCoroutineScope()
    val sheetStateR = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val listState = rememberLazyListState()

    val bottomSheetScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // 这里你可以根据需要自定义滚动逻辑
                listState.dispatchRawDelta(-available.y)
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
        contentWindowInsets = { WindowInsets.Companion.captionBar },
    ) {
        MusicPlayerScreen(
            musicDetail = music,
            picByte = picByte,
            onCloseSheet = {
                coroutineScope.launch {
                    sheetStateR.hide()
                    mainViewModel.putIterations(1)
                    mainViewModel.putSheetState(false)
                }
            },
            onSeekToNext = toNext,
            onSeekBack = backNext,
            onSetState = onSetState,
            listState = listState
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
    musicPlayerViewModel: MusicPlayerViewModel = hiltViewModel<MusicPlayerViewModel>(),
    onCloseSheet: () -> Unit,
    onSeekToNext: () -> Unit,
    onSeekBack: () -> Unit,
    onSetState: (Boolean) -> Unit,
    listState: LazyListState = rememberLazyListState()
) {

    val horPagerState =
        rememberPagerState {
            2
        }
    val lcrEntryList by musicPlayerViewModel.lrcServer.lcrEntryListFlow.collectAsState(emptyList())

    val cacheScheduleData by musicPlayerViewModel.cacheController._cacheSchedule.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = musicDetail.pic ?: picByte,
            contentDescription = stringResource(R.string.album_cover),
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize(),
            alpha = 0.2f,
            contentScale = ContentScale.Crop
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
                            imageVector = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = stringResource(R.string.close_player_screen)
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
                    state = horPagerState
                ) { page ->
                    if (page == 0) {
                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            XyImage(
                                model = musicDetail.pic ?: picByte,
                                contentDescription = stringResource(R.string.album_cover),
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(300.dp)
                                    .aspectRatio(1F)
                                    .clip(
                                        CircleShape
                                    ),
                                /*.graphicsLayer(rotationZ = rotationState)*/
                                placeholder = painterResource(id = R.drawable.disc_placeholder),
                                error = painterResource(id = R.drawable.disc_placeholder),
                                fallback = painterResource(id = R.drawable.disc_placeholder),
                                contentScale = ContentScale.Crop
                            )
                        }

                    } else {
                        LrcViewNewCompose(
                            listState = listState,
                            lcrEntryList = lcrEntryList
                        )
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
                                text = (if (musicDetail.artists.isNullOrBlank()) stringResource(R.string.unknown_artist) else musicDetail.artists).toString(),
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
                            favoriteRepository = musicPlayerViewModel.favoriteRepository
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
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "${musicPlayerViewModel.musicController.musicInfo?.name}${
                                    stringResource(
                                        R.string.other_operations_button_suffix
                                    )
                                }"
                            )
                        }
                    }
                    AnimatedVisibility(
                        visible = !musicPlayerViewModel.lrcServer.lrcText.isNullOrBlank() && horPagerState.currentPage == 0
                    ) {
                        XyRow(modifier = Modifier.height(30.dp)) {
                            LrcViewOneComponent(lrcText = musicPlayerViewModel.lrcServer.lrcText)
                        }
                    }

                    Column(modifier = Modifier
                        .fillMaxWidth()) {
                        PlayerCurrentPosition(
                            musicController = musicPlayerViewModel.musicController,
                            onCacheProgress = {
                                cacheScheduleData
                            })
                        Spacer(modifier = Modifier.height(XyTheme.dimens.outerVerticalPadding))
                        XyRow(modifier = Modifier/*.weight(1f)*/) {
                            PlayerTypeComponent(musicController = musicPlayerViewModel.musicController)
                            Icon(
                                imageVector = Icons.Rounded.SkipPrevious,
                                contentDescription = stringResource(R.string.previous_track),
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
                                imageVector = Icons.Rounded.SkipNext,
                                contentDescription = stringResource(R.string.next_track),
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
                                    imageVector = Icons.AutoMirrored.Sharp.QueueMusic,
                                    contentDescription = stringResource(R.string.music_list)
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
    musicController: MusicController
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
                imageVector = mainViewModel.iconList[musicController.playType.code].icon,
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
    musicController: MusicController,
    onCacheProgress: () -> Float
) {

    val progress by remember {
        derivedStateOf {
            musicController.currentPosition.toFloat() / musicController.duration.toFloat()
        }
    }

    XyColumn(
        backgroundColor = Color.Transparent,
        clipSize = 0.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {

            MusicProgressBar(
                currentTime = musicController.currentPosition,
                totalTime = musicController.duration,
                progress = progress,
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
    musicController: MusicController
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
    ) {

        Icon(
            imageVector = if (musicController.state == PlayStateEnum.Playing
                || musicController.state == PlayStateEnum.Loading
            ) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
            contentDescription = if (musicController.state == PlayStateEnum.Playing) stringResource(
                R.string.playing
            ) else stringResource(R.string.pause),
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
    musicController: MusicController,
    favoriteRepository: FavoriteRepository
) {
    val coroutineScope = rememberCoroutineScope()
    val favoriteSet by favoriteRepository.favoriteSet.collectAsState()

    IconButton(
        onClick = {
            coroutineScope.launch {
                val ifFavoriteData = dataSourceManager.setFavoriteData(
                    MusicTypeEnum.MUSIC,
                    itemId = musicDetail.itemId,
                    musicController = musicController,
                    ifFavorite = musicDetail.itemId in favoriteSet
                )
//                ifFavorite = ifFavoriteData
            }
        },
    ) {
        Icon(
            modifier = Modifier
                .size(60.dp),
            imageVector =
                if (musicDetail.itemId in favoriteSet)
                    Icons.Rounded.Favorite
                else
                    Icons.Rounded.FavoriteBorder,
            contentDescription = stringResource(R.string.favorite_button),
            tint = if (musicDetail.itemId in favoriteSet) Color.Red else LocalContentColor.current
        )
    }

}

