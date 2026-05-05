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


import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction.Companion.Done
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.xybbz.common.utils.DateUtil.millisecondsToTime
import cn.xybbz.common.utils.DateUtil.toDateStr
import cn.xybbz.common.utils.DateUtil.toSecondMsString
import cn.xybbz.common.utils.Log
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.common.utils.shareMusicResource
import cn.xybbz.compositionLocal.LocalMainViewModel
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.entity.data.ext.joinToString
import cn.xybbz.localdata.data.artist.XyArtist
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.data.setting.SkipTime
import cn.xybbz.localdata.enums.DataSourceType
import cn.xybbz.platform.ContextWrapper
import cn.xybbz.router.ArtistInfo
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.LazyColumnBottomSheetComponent
import cn.xybbz.ui.xy.XyButton
import cn.xybbz.ui.xy.XyEdit
import cn.xybbz.ui.xy.XyItemIcon
import cn.xybbz.ui.xy.XyItemReversal
import cn.xybbz.ui.xy.XyItemSlider
import cn.xybbz.ui.xy.XyItemSwitcher
import cn.xybbz.ui.xy.XyRow
import cn.xybbz.ui.xy.XySmallSlider
import cn.xybbz.ui.xy.XyTextSubSmall
import cn.xybbz.viewmodel.MusicBottomMenuViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.actual_path
import xymusic_kmp.composeapp.generated.resources.add_24px
import xymusic_kmp.composeapp.generated.resources.add_time
import xymusic_kmp.composeapp.generated.resources.add_to_next_play_success
import xymusic_kmp.composeapp.generated.resources.add_to_playlist
import xymusic_kmp.composeapp.generated.resources.album
import xymusic_kmp.composeapp.generated.resources.album_24px
import xymusic_kmp.composeapp.generated.resources.album_artist
import xymusic_kmp.composeapp.generated.resources.artist
import xymusic_kmp.composeapp.generated.resources.artist_list_title
import xymusic_kmp.composeapp.generated.resources.av_timer_24px
import xymusic_kmp.composeapp.generated.resources.bit_depth
import xymusic_kmp.composeapp.generated.resources.bitrate
import xymusic_kmp.composeapp.generated.resources.close_after_playback
import xymusic_kmp.composeapp.generated.resources.confirm
import xymusic_kmp.composeapp.generated.resources.countdown_prefix
import xymusic_kmp.composeapp.generated.resources.custom_timer_close
import xymusic_kmp.composeapp.generated.resources.custom_timer_suffix
import xymusic_kmp.composeapp.generated.resources.delete_forever_24px
import xymusic_kmp.composeapp.generated.resources.delete_permanently
import xymusic_kmp.composeapp.generated.resources.delete_warning
import xymusic_kmp.composeapp.generated.resources.double_speed
import xymusic_kmp.composeapp.generated.resources.download
import xymusic_kmp.composeapp.generated.resources.download_24px
import xymusic_kmp.composeapp.generated.resources.duration
import xymusic_kmp.composeapp.generated.resources.format
import xymusic_kmp.composeapp.generated.resources.info_24px
import xymusic_kmp.composeapp.generated.resources.keyboard_arrow_right_24px
import xymusic_kmp.composeapp.generated.resources.keyboard_double_arrow_right_24px
import xymusic_kmp.composeapp.generated.resources.max_24_hours
import xymusic_kmp.composeapp.generated.resources.media_source
import xymusic_kmp.composeapp.generated.resources.minutes
import xymusic_kmp.composeapp.generated.resources.normal
import xymusic_kmp.composeapp.generated.resources.person_24px
import xymusic_kmp.composeapp.generated.resources.play_next
import xymusic_kmp.composeapp.generated.resources.play_settings
import xymusic_kmp.composeapp.generated.resources.play_settings_time
import xymusic_kmp.composeapp.generated.resources.playback_speed
import xymusic_kmp.composeapp.generated.resources.playlist_add_24px
import xymusic_kmp.composeapp.generated.resources.reset
import xymusic_kmp.composeapp.generated.resources.sample_rate
import xymusic_kmp.composeapp.generated.resources.settings_voice_24px
import xymusic_kmp.composeapp.generated.resources.share_24px
import xymusic_kmp.composeapp.generated.resources.share_song
import xymusic_kmp.composeapp.generated.resources.size_num
import xymusic_kmp.composeapp.generated.resources.skip_head_prefix
import xymusic_kmp.composeapp.generated.resources.skip_head_tail
import xymusic_kmp.composeapp.generated.resources.skip_tail_prefix
import xymusic_kmp.composeapp.generated.resources.song_info
import xymusic_kmp.composeapp.generated.resources.speed_24px
import xymusic_kmp.composeapp.generated.resources.timer_close
import xymusic_kmp.composeapp.generated.resources.timer_close_custom
import xymusic_kmp.composeapp.generated.resources.timer_close_disabled
import xymusic_kmp.composeapp.generated.resources.title
import xymusic_kmp.composeapp.generated.resources.volume_up_24px
import xymusic_kmp.composeapp.generated.resources.volume_value_setting
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

var bottomMenuMusicInfo = mutableStateListOf<XyMusic>()
private var bottomMenuMusicInitialActions =
    mutableStateMapOf<String, MusicBottomMenuInitialAction>()

enum class MusicBottomMenuInitialAction {
    Menu,
    SongInfo,
    DoubleSpeed,
    SkipBeginningAndEnd,
    Timer,
}

/**
 * 底部弹出菜单
 * todo 这里要限制一下弹出的高度为最大高度的百分之55
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicBottomMenuComponent(
    musicBottomMenuViewModel: MusicBottomMenuViewModel = koinViewModel<MusicBottomMenuViewModel>(),
    onAlbumRouter: (String) -> Unit,
    onPlayerSheetClose: () -> Unit
) {
    val mainViewModel = LocalMainViewModel.current

    val navigator = LocalNavigator.current
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )


    val coroutineScope = rememberCoroutineScope()
    val contextWrapper = koinInject<ContextWrapper>()

    SideEffect {
        Log.i("=====", "MusicBottomMenuComponent重组一次")
    }

    val addToNextPlaySuccess = stringResource(Res.string.add_to_next_play_success)
    val deletePermanently = stringResource(Res.string.delete_permanently)
    val timerClose = stringResource(Res.string.timer_close)


    val favoriteMusicMap by musicBottomMenuViewModel.favoriteSet.collectAsStateWithLifecycle(
        emptyList()
    )
    val downloadMusicIds by musicBottomMenuViewModel.downloadMusicIdsFlow.collectAsStateWithLifecycle(
        emptyList()
    )

    bottomMenuMusicInfo.forEach { music ->

        LaunchedEffect(Unit) {
            musicBottomMenuViewModel.refreshVolume()
        }

        val initialAction =
            bottomMenuMusicInitialActions[music.itemId] ?: MusicBottomMenuInitialAction.Menu

        //收藏信息
        val favoriteState by remember {
            derivedStateOf {
                favoriteMusicMap.contains(music.itemId)
            }
        }

        //是否显示头尾跳过时间
        var ifShowHeadAndTail by remember {
            mutableStateOf(false)
        }
        //是否显示倍速设置
        var ifDoubleSpeed by remember {
            mutableStateOf(false)
        }
        //是否显示定时关闭
        var ifTimer by remember {
            mutableStateOf(false)
        }

        /**
         * 是否打开音乐详情
         */
        var ifShowMusicInfo by remember {
            mutableStateOf(false)
        }

        /**
         * 是否打开底部菜单
         */
        var ifShowBottom by remember {
            mutableStateOf(initialAction == MusicBottomMenuInitialAction.Menu)
        }

        /**
         * 是否打开播放设置-淡入淡出
         */
        var ifShowFadeInOut by remember {
            mutableStateOf(false)
        }

        // 统一处理艺术家点击：多艺术家弹选择列表，单艺术家直接进入详情页。
        val artistClickHandler = rememberMusicArtistClickHandler(
            musicBottomMenuViewModel = musicBottomMenuViewModel,
            onBeforeOpen = {
                sheetState.hide()
            },
            onBeforeSingleArtistNavigate = onPlayerSheetClose,
            onAfterOpen = {
                ifShowBottom = false
                music.dismiss()
            }
        )

        /**
         * 是否可以删除数据
         */
        val ifDelete by remember {
            derivedStateOf {
                musicBottomMenuViewModel.dataSourceManager.getCanDelete()
            }
        }

        LaunchedEffect(music.itemId, initialAction) {
            when (initialAction) {
                MusicBottomMenuInitialAction.Menu -> ifShowBottom = true
                MusicBottomMenuInitialAction.SongInfo -> ifShowMusicInfo = true
                MusicBottomMenuInitialAction.DoubleSpeed -> ifDoubleSpeed = true
                MusicBottomMenuInitialAction.SkipBeginningAndEnd -> ifShowHeadAndTail = true
                MusicBottomMenuInitialAction.Timer -> ifTimer = true
            }
        }


        val permissionState = downloadPermission {
            musicBottomMenuViewModel.downloadMusic(music)
            ifShowBottom = false
            music.dismiss()
        }

        MusicInfoBottomComponent(
            musicInfo = music,
            onIfShowMusicInfo = { ifShowMusicInfo },
            onSetShowMusicInfo = { ifShowMusicInfo = it },
            dataSourceType = musicBottomMenuViewModel.dataSourceManager.dataSourceType
        )



        MusicBottomMenuPlatformSheet(
            modifier = Modifier,
            bottomSheetState = sheetState,
            onIfDisplay = { ifShowBottom },
            dragHandle = null,
            onClose = {
                coroutineScope.launch {
                    ifShowBottom = false
                    music.dismiss()
                }.invokeOnCompletion {
                    mainViewModel.putIterations(1)
                }
            }
        ) {

            LazyColumnBottomSheetComponent {
                item {
                    MusicItemNotClickComponent(
                        music = music,
                        ifDownload = music.itemId in downloadMusicIds,
                        backgroundColor = Color.Transparent,
                        favoriteState = favoriteState,
                        trailingOnClick = {
                            coroutineScope.launch {
                                sheetState.hide()
                                musicBottomMenuViewModel.setFavoriteMusic(
                                    itemId = music.itemId,
                                    ifFavorite = favoriteState
                                )
                            }.invokeOnCompletion {
                                ifShowBottom = false

                            }
                        },
                    )
                }

                item {
                    XyItemIcon(
                        painter = painterResource(Res.drawable.download_24px),
                        enabled = musicBottomMenuViewModel.dataSourceManager.getCanDownload(),
                        text = stringResource(Res.string.download),
                        onClick = {
                            permissionState.launchMultiplePermissionRequest()
                        })
                }


                item {
                    XyItemIcon(
                        painter = painterResource(Res.drawable.person_24px),
                        text = "${stringResource(Res.string.artist)}: ${music.artists}",
                        onClick = {
                            artistClickHandler.openMusicArtists(music)
                        })
                }

                item {
                    XyItemIcon(
                        painter = painterResource(Res.drawable.album_24px),
                        text = "${stringResource(Res.string.album)}: ${music.albumName ?: ""}",
                        onClick = {
                            coroutineScope.launch {
                                sheetState.hide()
                                onPlayerSheetClose()
                                onAlbumRouter(
                                    music.album
                                )
                            }.invokeOnCompletion {
                                ifShowBottom = false
                                music.dismiss()
                            }
                        })
                }

                item {
                    XyItemIcon(
                        painter = painterResource(Res.drawable.keyboard_double_arrow_right_24px),
                        text = stringResource(Res.string.skip_head_tail),
                        onClick = {
                            coroutineScope.launch {
                                sheetState.hide()
                                ifShowHeadAndTail = true
                            }.invokeOnCompletion {
                                ifShowBottom = false

                            }
                        }
                    )
                }


                item {
                    XyItemIcon(
                        painter = painterResource(Res.drawable.av_timer_24px),
                        text = stringResource(Res.string.timer_close),
                        onClick = {
                            coroutineScope.launch {
                                sheetState.hide()
                                ifTimer = true
                            }.invokeOnCompletion {
                                ifShowBottom = false
                            }
                        }
                    )
                }

                item {
                    XyItemIcon(
                        painter = painterResource(Res.drawable.speed_24px),
                        text = stringResource(Res.string.double_speed),
                        onClick = {
                            coroutineScope.launch {
                                sheetState.hide()
                                ifDoubleSpeed = true
                            }.invokeOnCompletion {
                                ifShowBottom = false
                            }
                        }
                    )
                }


                item {
                    XyItemIcon(
                        painter = painterResource(Res.drawable.add_24px),
                        text = stringResource(Res.string.add_to_playlist),
                        onClick = {
                            coroutineScope.launch {
                                sheetState.hide()
                                AddPlaylistBottomData(
                                    ifShow = true,
                                    musicInfoList = listOf(music.itemId)
                                ).show()
                            }.invokeOnCompletion {
                                ifShowBottom = false

                            }
                        }
                    )
                }

                item {
                    XyItemIcon(
                        text = stringResource(Res.string.volume_value_setting),
                        sub = (musicBottomMenuViewModel.volumeValue * 100).toInt().toString(),
                        painter = painterResource(Res.drawable.volume_up_24px),
                        middleContent = {
                            Spacer(modifier = Modifier.width(XyTheme.dimens.contentPadding))
                            XySmallSlider(
                                modifier = Modifier.weight(7f),
                                progress = musicBottomMenuViewModel.volumeValue,
                                onProgressChanged = { musicBottomMenuViewModel.updateVolume(it) },
                                cacheProgressBarColor = Color.Transparent,
                            )
                        }
                    )
                }

                item {
                    XyItemIcon(
                        painter = painterResource(Res.drawable.settings_voice_24px),
                        text = "${stringResource(Res.string.play_settings)}: ${
                            musicBottomMenuViewModel.getFadeDurationMs().toSecondMsString()
                        }",
                        onClick = {
                            coroutineScope.launch {
                                sheetState.hide()
                                ifShowFadeInOut = true
                            }.invokeOnCompletion {
                                ifShowBottom = false
                            }
                        },
                    )
                }

                item {
                    XyItemIcon(
                        painter = painterResource(Res.drawable.playlist_add_24px),
                        text = stringResource(Res.string.play_next),
                        onClick = {
                            coroutineScope.launch {
                                sheetState.hide()
                                musicBottomMenuViewModel.addNextPlayer(
                                    music.itemId
                                )
                                MessageUtils.sendPopTip(addToNextPlaySuccess)
                            }.invokeOnCompletion {
                                ifShowBottom = false
                                music.dismiss()
                            }

                        })
                }

                item {
                    XyItemIcon(
                        painter = painterResource(Res.drawable.share_24px),
                        text = stringResource(Res.string.share_song),
                        onClick = {
                            shareMusicResource(
                                contextWrapper = contextWrapper,
                                resource = music.downloadUrl
                            )
                            coroutineScope.launch {
                                sheetState.hide()
                            }.invokeOnCompletion {
                                ifShowBottom = false
                                music.dismiss()
                            }

                        })
                }

                item {
                    XyItemIcon(
                        painter = painterResource(Res.drawable.info_24px),
                        text = stringResource(Res.string.song_info),
                        onClick = {
                            coroutineScope.launch {
                                sheetState.hide()
                                ifShowMusicInfo = true
                            }.invokeOnCompletion {
                                ifShowBottom = false
                            }
                        }
                    )
                }
                if (ifDelete) {
                    item {
                        XyItemIcon(
                            painter = painterResource(Res.drawable.delete_forever_24px),
                            text = deletePermanently,
                            onClick = {
                                AlertDialogObject(
                                    title = deletePermanently,
                                    content = {
                                        XyTextSubSmall(
                                            text = stringResource(Res.string.delete_warning)
                                        )
                                    },
                                    ifWarning = true,
                                    onConfirmation = {
                                        coroutineScope.launch {
                                            sheetState.hide()
                                            musicBottomMenuViewModel.removeMusicResource(music)
                                        }.invokeOnCompletion {
                                            ifShowBottom = false
                                            music.dismiss()
                                        }

                                    },
                                    onDismissRequest = {}).show()
                            })
                    }
                }


            }

        }

        SkipBeginningAndEndComponent(
            onItemId = { music.itemId },
            onAlbumId = { music.album },
            onIfShowHeadAndTail = { ifShowHeadAndTail },
            onSetIfShowHeadAndTail = {
                ifShowHeadAndTail = it
                music.dismiss()
            },
            onGetSkipTimeData = {

                musicBottomMenuViewModel.getSkipTimeData(
                    music.album
                )
            },
            onSaveOrUpdateSkipTimeData = {
                musicBottomMenuViewModel.saveOrUpdateSkipTimeData(it)

            })

        TimerComponent(
            onIfTimer = { ifTimer },
            onSetIfTimer = {
                ifTimer = it
                music.dismiss()
            },
            onTimerInfo = { musicBottomMenuViewModel.timerInfo },
            onSetTimerInfo = { num, applyTimer ->
                musicBottomMenuViewModel.setTimerInfoData(num)
                if (applyTimer) {
                    coroutineScope
                        .launch {
                            if (musicBottomMenuViewModel.sliderTimerEndData == 0f || musicBottomMenuViewModel.timerInfo == 0L) {
                                musicBottomMenuViewModel.cancelAlarm()
                            } else {
                                musicBottomMenuViewModel.createMusicStop(timerClose)
                            }
                        }
                        .invokeOnCompletion {
                        }
                }
            },
            onSliderTimerEndData = { musicBottomMenuViewModel.sliderTimerEndData },
            onSetSliderTimerEndData = { musicBottomMenuViewModel.setSliderTimerEndDataValue(it) },
            onIfPlayEndClose = { musicBottomMenuViewModel.ifPlayEndClose },
            onSetIfPlayEndClose = { musicBottomMenuViewModel.setPlayEndCloseData(it) }
        )
        DoubleSpeedComponent(
            onIfDoubleSpeed = { ifDoubleSpeed },
            onSetIfDoubleSpeed = {
                ifDoubleSpeed = it
                music.dismiss()
            },
            onDoubleSpeed = { musicBottomMenuViewModel.doubleSpeed },
            onSetDoubleSpeed = {
                musicBottomMenuViewModel.setDoubleSpeed(it)
                musicBottomMenuViewModel.musicController.setDoubleSpeed(it)
            })


        FadeInOutBottomSheet(
            onIfShowFadeInOut = { ifShowFadeInOut },
            onSetShowFadeInOut = { ifShowFadeInOut = it },
            onFadeDurationMs = { musicBottomMenuViewModel.getFadeDurationMs() },
            onSetFadeDurationMs = { musicBottomMenuViewModel.setFadeDurationMs(it) })
    }

}

/**
 * 定时关闭
 */
@OptIn(FormatStringsInDatetimeFormats::class, ExperimentalMaterial3Api::class)
@Composable
fun TimerComponent(
    onIfTimer: () -> Boolean,
    onSetIfTimer: (Boolean) -> Unit,
    onTimerInfo: () -> Long,
    onSetTimerInfo: (Long, Boolean) -> Unit,
    onSliderTimerEndData: () -> Float,
    onSetSliderTimerEndData: (Float) -> Unit,
    onIfPlayEndClose: () -> Boolean,
    onSetIfPlayEndClose: (Boolean) -> Unit
) {
    val mainViewModel = LocalMainViewModel.current
    val sheetTimer = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val coroutineScope = rememberCoroutineScope()
    val maxCustomTimerMinutes = 24 * 60L

    val customTimerClose = stringResource(Res.string.custom_timer_close)
    val max24Hours = stringResource(Res.string.max_24_hours)

    //输入内容
    var customInputValue by remember {
        mutableStateOf("")
    }
    var isError by remember { mutableStateOf(false) }

    var number by remember {
        mutableStateOf("")
    }
    var draftSliderTimerEndData by remember {
        mutableFloatStateOf(onSliderTimerEndData())
    }
    var draftTimerInfo by remember {
        mutableLongStateOf(onTimerInfo())
    }
    var draftPlayEndClose by remember {
        mutableStateOf(onIfPlayEndClose())
    }
    val systemTimeZone = remember { TimeZone.currentSystemDefault() }
    val timerTimeFormat = remember {
        LocalDateTime.Format {
            byUnicodePattern("HH:mm:ss")
        }
    }

    LaunchedEffect(onIfTimer()) {
        if (onIfTimer()) {
            draftSliderTimerEndData = onSliderTimerEndData()
            draftTimerInfo = onTimerInfo()
            draftPlayEndClose = onIfPlayEndClose()
        }
    }

    LaunchedEffect(draftTimerInfo) {
        number = if (draftTimerInfo > 0) {
            timerTimeFormat.format(
                (Clock.System.now() + draftTimerInfo.toInt().minutes)
                    .toLocalDateTime(systemTimeZone)
            )
        } else {
            ""
        }
    }

    val timerClose = stringResource(Res.string.timer_close)
    MusicBottomMenuPlatformSheet(
        bottomSheetState = sheetTimer,
        modifier = Modifier,
        dragHandle = null,
        onIfDisplay = onIfTimer,
        onClose = {
            mainViewModel.putIterations(1)
            onSetIfTimer(it)
        },
        titleText = timerClose,
        titleTailContent = {
            OutlinedButton(
                modifier = Modifier.size(height = 25.dp, width = 50.dp),
                contentPadding = PaddingValues(horizontal = 2.dp),
                onClick = {
                    draftSliderTimerEndData = 0f
                    draftTimerInfo = 0L
                    draftPlayEndClose = false
                    customInputValue = ""
                    isError = false
                    coroutineScope.launch {
                        onSetSliderTimerEndData(0f)
                        onSetIfPlayEndClose(false)
                        onSetTimerInfo(0, true)
                    }
                },
            ) {
                XyTextSubSmall(
                    text = stringResource(Res.string.reset)
                )
            }
        }
    ) {
        XyItemSlider(
            value = draftSliderTimerEndData,
            onValueChange = {
                if (it >= 75f) {
                    customInputValue = draftTimerInfo
                        .takeIf { minutes -> minutes > 0 }
                        ?.toString()
                        .orEmpty()
                    isError = false
                    AlertDialogObject(
                        title = customTimerClose,
                        content = {
                            XyEdit(
                                text = customInputValue,
                                onChange = { input ->
                                    val pattern = Regex("[^0-9]") // 定义正则表达式，匹配至少1位数字
                                    var replace = input.replace(pattern, "")
                                    if (replace.length >= 4) {
                                        MessageUtils.sendPopTipError(Res.string.max_24_hours)
                                        replace = replace.take(4)
                                    }
                                    isError = replace.toLongOrNull()?.let { value ->
                                        value !in 1..maxCustomTimerMinutes
                                    } ?: false
                                    customInputValue = replace
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = Done
                                ),
                                singleLine = true,
                                modifier = Modifier.semantics {
                                    if (isError) error(max24Hours)
                                }
                            )
                        },
                        onDismissRequest = {
                            customInputValue = ""
                            isError = false
                        },
                        onConfirmation = {
                            val customMinutes = customInputValue.toLongOrNull()
                            if (customMinutes != null && customMinutes in 1..maxCustomTimerMinutes) {
                                draftSliderTimerEndData = 75f
                                draftTimerInfo = customMinutes
                            } else {
                                isError = true
                                MessageUtils.sendPopTipError(Res.string.max_24_hours)
                            }
                            customInputValue = ""
                        }
                    ).show()
                } else {
                    draftSliderTimerEndData = it
                    draftTimerInfo = it.toLong()
                }
            }, valueRange = 0f..75f, steps = 4,
            text = "${stringResource(Res.string.countdown_prefix)}${
                when (draftSliderTimerEndData) {
                    0f -> stringResource(Res.string.timer_close_disabled)
                    75f -> "${stringResource(Res.string.timer_close_custom)} ${draftTimerInfo}${
                        stringResource(
                            Res.string.custom_timer_suffix
                        )
                    } $number $timerClose"

                    else -> "${draftSliderTimerEndData.toInt()}${stringResource(Res.string.custom_timer_suffix)} $number $timerClose"
                }
            }"
        )

        XyRow {
            XyTextSubSmall(
                text = stringResource(Res.string.timer_close_disabled),
                color = MaterialTheme.colorScheme.onSurface
            )
            XyTextSubSmall(
                text = "15${stringResource(Res.string.minutes)}",
                color = MaterialTheme.colorScheme.onSurface
            )
            XyTextSubSmall(
                text = "30${stringResource(Res.string.minutes)}",
                color = MaterialTheme.colorScheme.onSurface
            )
            XyTextSubSmall(
                text = "45${stringResource(Res.string.minutes)}",
                color = MaterialTheme.colorScheme.onSurface
            )
            XyTextSubSmall(
                text = "60${stringResource(Res.string.minutes)}",
                color = MaterialTheme.colorScheme.onSurface
            )
            XyTextSubSmall(
                text = stringResource(Res.string.timer_close_custom),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        XyItemSwitcher(
            state = draftPlayEndClose,
            onChange = {
                draftPlayEndClose = it
            },
            text = stringResource(Res.string.close_after_playback)
        )

        XyButtonHorizontalPadding(
            text = stringResource(Res.string.confirm),
            onClick = {
                coroutineScope
                    .launch {
                        onSetSliderTimerEndData(draftSliderTimerEndData)
                        onSetIfPlayEndClose(draftPlayEndClose)
                        onSetTimerInfo(draftTimerInfo, true)
                        sheetTimer.hide()
                    }
                    .invokeOnCompletion {
                        onSetIfTimer(false)
                    }
            }
        )

    }
}

/**
 * 倍速配置
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DoubleSpeedComponent(
    onIfDoubleSpeed: () -> Boolean,
    onSetIfDoubleSpeed: (Boolean) -> Unit,
    onDoubleSpeed: () -> Float,
    onSetDoubleSpeed: suspend (Float) -> Unit,
) {
    val mainViewModel = LocalMainViewModel.current
    val coroutineScope = rememberCoroutineScope()
    val sheetDoubleSpeed = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    var doubleSpeedTmp by remember {
        mutableFloatStateOf(onDoubleSpeed())
    }


    MusicBottomMenuPlatformSheet(
        bottomSheetState = sheetDoubleSpeed,
        modifier = Modifier,
        onIfDisplay = onIfDoubleSpeed,
        dragHandle = null,
        onClose = {
            onSetIfDoubleSpeed(false)
            mainViewModel.putIterations(1)
        },
        titleText = stringResource(Res.string.double_speed),
        titleTailContent = {
            OutlinedButton(
                modifier = Modifier.size(height = 25.dp, width = 50.dp),
                contentPadding = PaddingValues(horizontal = 2.dp),
                onClick = {
                    coroutineScope.launch {
                        doubleSpeedTmp = 1f
                    }
                },
            ) {
                Text(
                    text = stringResource(Res.string.reset),
                    fontSize = 10.sp
                )
            }
        }
    ) {
        XyItemSlider(
            value = doubleSpeedTmp,
            onValueChange = {
                doubleSpeedTmp = it
            },
            valueRange = 0.5f..2f,
            steps = 2,
            text = "${stringResource(Res.string.playback_speed)}: ${
                when (doubleSpeedTmp) {
                    0.5f -> "0.5"
                    1f -> stringResource(Res.string.normal)
                    1.5f -> "1.5"
                    2f -> "2"
                    else -> ""
                }
            }"
        )
        XyRow {
            XyTextSubSmall(text = "0.5")
            XyTextSubSmall(text = "0")
            XyTextSubSmall(text = "1.5")
            XyTextSubSmall(text = "2.0")
        }

        XyButtonHorizontalPadding(text = stringResource(Res.string.confirm), onClick = {
            coroutineScope
                .launch {
                    sheetDoubleSpeed.hide()
                    onSetDoubleSpeed(doubleSpeedTmp)
                }
                .invokeOnCompletion {
                    onSetIfDoubleSpeed(false)
                }
        })
    }
}

/**
 * 跳过头尾
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SkipBeginningAndEndComponent(
    onItemId: () -> String,
    onAlbumId: () -> String,
    onIfShowHeadAndTail: () -> Boolean,
    onSetIfShowHeadAndTail: (Boolean) -> Unit,
    onGetSkipTimeData: suspend () -> SkipTime,
    onSaveOrUpdateSkipTimeData: suspend (SkipTime) -> Unit
) {
    val sheetSkip = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val coroutineScope = rememberCoroutineScope()

    val mainViewModel = LocalMainViewModel.current
    var skipTime by remember {
        mutableStateOf(SkipTime(connectionId = 0))
    }

    var startTime by remember {
        mutableFloatStateOf(0f)
    }

    var endTime by remember {
        mutableFloatStateOf(0f)
    }

    LaunchedEffect(Unit) {
        if (onItemId().isNotBlank())
            skipTime = onGetSkipTimeData()

        startTime = skipTime.headTime.toFloat()
        endTime = skipTime.endTime.toFloat()
    }
    MusicBottomMenuPlatformSheet(
        bottomSheetState = sheetSkip,
        modifier = Modifier,
        onIfDisplay = onIfShowHeadAndTail,
        onClose = {
            onSetIfShowHeadAndTail(it)
            mainViewModel.putIterations(1)
        },
        dragHandle = null,
        titleText = stringResource(Res.string.skip_head_tail),
        titleTailContent = {
            OutlinedButton(
                modifier = Modifier.size(height = 25.dp, width = 50.dp),
                contentPadding = PaddingValues(horizontal = 2.dp),
                onClick = {
                    if (onAlbumId().isNotBlank()) {
                        startTime = 0f
                        endTime = 0f
                        skipTime.endTime = 0L
                        skipTime.headTime = 0L
                        skipTime.albumId = onAlbumId()
                        coroutineScope.launch {
                            onSaveOrUpdateSkipTimeData(skipTime)
                        }
                    }

                },
            ) {
                Text(
                    text = stringResource(Res.string.reset),
                    fontSize = 10.sp,
                )
            }
        }
    ) {
        XyItemSlider(
            value = startTime,
            onValueChange = {
                startTime = it.toLong().toFloat()
            },
            valueRange = 0f..60f,
            text = "${stringResource(Res.string.skip_head_prefix)} ${startTime.toLong()}s"
        )
        XyRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            XyTextSubSmall(text = "0s")
            XyTextSubSmall(text = "60s")
        }

        XyItemSlider(
            value = endTime,
            onValueChange = {
                endTime = it.toLong().toFloat()
            },
            valueRange = 0f..60f,
            text = "${stringResource(Res.string.skip_tail_prefix)} ${endTime.toLong()}s"
        )
        XyRow {
            XyTextSubSmall(text = "0s")
            XyTextSubSmall(text = "60s")
        }

        XyButtonHorizontalPadding(text = stringResource(Res.string.confirm), onClick = {
            if (onAlbumId().isNotBlank()) {
                skipTime.endTime = endTime.toLong()
                skipTime.headTime = startTime.toLong()
                skipTime.albumId = onAlbumId()
                coroutineScope
                    .launch {
                        sheetSkip.hide()
                        onSaveOrUpdateSkipTimeData(skipTime)
                    }
                    .invokeOnCompletion {
                        onSetIfShowHeadAndTail(false)
                    }
            }

        })
    }
}

/**
 * 音乐信息底部弹出
 * 音乐信息底部组件
 * @param [modifier] 样式
 * @param [musicInfo] 音乐信息
 * @param [onIfShowMusicInfo] 是否显示音乐信息
 * @param [onSetShowMusicInfo] 设置是否显示音乐信息
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicInfoBottomComponent(
    modifier: Modifier = Modifier,
    musicInfo: XyMusic,
    onIfShowMusicInfo: () -> Boolean,
    onSetShowMusicInfo: (Boolean) -> Unit,
    dataSourceType: DataSourceType?
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )
    val mainViewModel = LocalMainViewModel.current

    MusicBottomMenuPlatformSheet(
        modifier = modifier,
        bottomSheetState = sheetState,
        onIfDisplay = onIfShowMusicInfo,
        dragHandle = null,
        onClose = {
            onSetShowMusicInfo(false)
            mainViewModel.putIterations(1)
        },
        titleText = stringResource(Res.string.song_info)
    ) {

        LazyColumnBottomSheetComponent(horizontal = XyTheme.dimens.outerHorizontalPadding) {
            item { XyItemReversal(text = stringResource(Res.string.title), sub = musicInfo.name) }
            item {
                XyItemReversal(
                    text = stringResource(Res.string.artist),
                    sub = musicInfo.artists?.joinToString() ?: ""
                )
            }
            item {
                XyItemReversal(
                    text = stringResource(Res.string.album),
                    sub = musicInfo.albumName ?: ""
                )
            }
            item {
                XyItemReversal(
                    text = stringResource(Res.string.album_artist),
                    sub = musicInfo.albumArtist?.joinToString() ?: ""
                )
            }
            item {
                XyItemReversal(
                    text = stringResource(Res.string.media_source),
                    sub = dataSourceType?.title ?: ""
                )
            }
            item {
                XyItemReversal(
                    text = stringResource(Res.string.duration),
                    sub = millisecondsToTime(
                        musicInfo.runTimeTicks
                    )
                )
            }
            item {
                XyItemReversal(
                    text = stringResource(Res.string.bitrate),
                    sub = "${(musicInfo.bitRate ?: 0) / 1000}kbps"
                )
            }
            item {
                XyItemReversal(
                    text = stringResource(Res.string.sample_rate),
                    sub = "${musicInfo.sampleRate ?: 0}Hz"
                )
            }
            item {
                XyItemReversal(
                    text = stringResource(Res.string.bit_depth),
                    sub = "${musicInfo.bitDepth ?: 0}bit"
                )
            }
            item {
                XyItemReversal(
                    text = stringResource(Res.string.size_num),
                    sub = "${
                        ((musicInfo.size ?: 0L) + 1024 * 1024 - 1) / (1024 * 1024)
                    }MB"
                )
            }
            item {
                XyItemReversal(
                    text = stringResource(Res.string.format),
                    sub = musicInfo.container ?: ""
                )
            }
            item {
                XyItemReversal(
                    text = stringResource(Res.string.actual_path),
                    sub = musicInfo.path,
                    subMaxLines = Int.MAX_VALUE,
                )
            }
            item {
                XyItemReversal(
                    text = stringResource(Res.string.add_time),
                    sub = musicInfo.createTime.toDateStr(
                        "yyyy/MM/dd HH:mm"
                    )
                )
            }
        }
    }
}

/**
 * 艺术家底部弹窗列表
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistItemListBottomSheet(
    modifier: Modifier = Modifier,
    artistList: List<XyArtist>,
    onIfShowArtistList: () -> Boolean,
    onSetShowArtistList: (Boolean) -> Unit,
) {

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )
    val mainViewModel = LocalMainViewModel.current
    val navHostController = LocalNavigator.current
    val coroutineScope = rememberCoroutineScope()

    MusicBottomMenuPlatformSheet(
        modifier = modifier,
        bottomSheetState = sheetState,
        onIfDisplay = onIfShowArtistList,
        dragHandle = null,
        onClose = {
            onSetShowArtistList(false)
            mainViewModel.putIterations(1)
        },
        titleText = stringResource(Res.string.artist_list_title)
    ) {
        LazyVerticalGridComponent {
            items(artistList, key = { it.artistId }) { artist ->
                MusicArtistCardComponent(
                    onItem = { artist },
                    onRouter = {
                        navHostController.navigate(ArtistInfo(it, artist.name ?: ""))
                        coroutineScope.launch {
                            sheetState.hide()
                        }.invokeOnCompletion {
                            onSetShowArtistList(false)
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FadeInOutBottomSheet(
    modifier: Modifier = Modifier,
    onIfShowFadeInOut: () -> Boolean,
    onSetShowFadeInOut: (Boolean) -> Unit,
    onFadeDurationMs: () -> Long,
    onSetFadeDurationMs: (Long) -> Unit
) {

    var fadeDurationMs by remember {
        mutableLongStateOf(onFadeDurationMs())
    }

    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val coroutineScope = rememberCoroutineScope()

    MusicBottomMenuPlatformSheet(
        bottomSheetState = bottomSheetState,
        modifier = modifier,
        onIfDisplay = onIfShowFadeInOut,
        onClose = {
            onSetShowFadeInOut(it)
        },
        dragHandle = null,
        titleText = stringResource(Res.string.play_settings),
        titleTailContent = {
            OutlinedButton(
                modifier = Modifier.size(height = 25.dp, width = 50.dp),
                contentPadding = PaddingValues(horizontal = 2.dp),
                onClick = {
                    fadeDurationMs = 300L
                    onSetFadeDurationMs(fadeDurationMs)
                },
            ) {
                Text(
                    text = stringResource(Res.string.reset),
                    fontSize = 10.sp,
                )
            }
        }
    ) {
        XyItemSlider(
            value = fadeDurationMs.toFloat(),
            onValueChange = {
                fadeDurationMs = it.toLong()
            },
            valueRange = 0f..15000f,
            text = "${stringResource(Res.string.play_settings_time)}: ${fadeDurationMs.toSecondMsString()}"
        )
        XyRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            XyTextSubSmall(text = "0s")
            XyTextSubSmall(text = "15s")
        }

        XyButtonHorizontalPadding(text = stringResource(Res.string.confirm), onClick = {
            coroutineScope
                .launch {
                    bottomSheetState.hide()
                    onSetFadeDurationMs(fadeDurationMs)
                }
                .invokeOnCompletion {
                    onSetShowFadeInOut(false)
                }

        })
    }
}


@Composable
fun XyButtonHorizontalPadding(
    onClick: () -> Unit,
    enabled: Boolean = true,
    text: String,
) {
    XyButton(
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        onClick = onClick,
        text = text,
        paddingValues = PaddingValues(
            horizontal = XyTheme.dimens.outerHorizontalPadding
        )
    )
}

fun XyMusic.show(
    initialAction: MusicBottomMenuInitialAction = MusicBottomMenuInitialAction.Menu,
) = apply {
    bottomMenuMusicInitialActions[itemId] = initialAction
    bottomMenuMusicInfo.add(this@show)
}

fun XyMusic.dismiss() = apply {
    bottomMenuMusicInitialActions.remove(itemId)
    bottomMenuMusicInfo.remove(this@dismiss)
}
