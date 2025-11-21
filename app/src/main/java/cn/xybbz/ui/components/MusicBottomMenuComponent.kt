package cn.xybbz.ui.components


import android.content.Intent
import android.icu.math.BigDecimal
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.webkit.URLUtil
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.PlaylistAdd
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.AvTimer
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.KeyboardDoubleArrowRight
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction.Companion.Done
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import cn.xybbz.R
import cn.xybbz.common.utils.DateUtil.millisecondsToTime
import cn.xybbz.common.utils.DateUtil.toDateStr
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.compositionLocal.LocalMainViewModel
import cn.xybbz.compositionLocal.LocalNavController
import cn.xybbz.entity.data.MusicItemMenuData
import cn.xybbz.localdata.data.artist.XyArtist
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.data.setting.SkipTime
import cn.xybbz.localdata.enums.DataSourceType
import cn.xybbz.router.RouterConstants.ArtistInfo
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.LazyColumnBottomSheetComponent
import cn.xybbz.ui.xy.ModalBottomSheetExtendComponent
import cn.xybbz.ui.xy.RoundedSurfaceColumnPadding
import cn.xybbz.ui.xy.XyButtonHorizontalPadding
import cn.xybbz.ui.xy.XyColumn
import cn.xybbz.ui.xy.XyColumnNotHorizontalPadding
import cn.xybbz.ui.xy.XyEdit
import cn.xybbz.ui.xy.XyItemSlider
import cn.xybbz.ui.xy.XyItemSwitcherNotTextColor
import cn.xybbz.ui.xy.XyItemTabButton
import cn.xybbz.ui.xy.XyItemText
import cn.xybbz.ui.xy.XyItemTextHorizontal
import cn.xybbz.ui.xy.XyItemTextIconCheckSelectHeightSmall
import cn.xybbz.ui.xy.XyItemTextPadding
import cn.xybbz.ui.xy.XyRow
import cn.xybbz.viewmodel.MusicBottomMenuViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

var bottomMenuMusicInfo = mutableStateListOf<XyMusic>()

/**
 * 底部弹出菜单
 * todo 这里要限制一下弹出的高度为最大高度的百分之55
 */
@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicBottomMenuComponent(
    musicBottomMenuViewModel: MusicBottomMenuViewModel = hiltViewModel<MusicBottomMenuViewModel>(),
    onAlbumRouter: (String) -> Unit,
) {
    val mainViewModel = LocalMainViewModel.current

    val navHostController = LocalNavController.current
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )


    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var ifShowArtistList by remember {
        mutableStateOf(false)
    }

    SideEffect {
        Log.d("=====", "MusicBottomMenuComponent重组一次")
    }

    var ifCanScheduleExactAlarms by remember {
        mutableStateOf(musicBottomMenuViewModel.alarmConfig.returnAm().canScheduleExactAlarms())
    }


    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        val canScheduleExactAlarms =
            musicBottomMenuViewModel.alarmConfig.returnAm().canScheduleExactAlarms()
        ifCanScheduleExactAlarms = canScheduleExactAlarms
        Toast.makeText(
            context,
            if (canScheduleExactAlarms)
                context.getString(R.string.exact_alarm_permission_granted)
            else context.getString(R.string.exact_alarm_permission_not_granted),
            Toast.LENGTH_SHORT
        ).show()
    }

    ArtistItemListBottomSheet(
        artistList = musicBottomMenuViewModel.xyArtists,
        onIfShowArtistList = { ifShowArtistList },
        onSetShowArtistList = { ifShowArtistList = it },
    )

    bottomMenuMusicInfo.forEach { music ->

        val favoriteMusicMap by musicBottomMenuViewModel.favoriteRepository.favoriteMap.collectAsState()
        //收藏信息
        val favoriteState by remember {
            derivedStateOf {
                if (favoriteMusicMap.containsKey(music.itemId)) {
                    favoriteMusicMap.getOrDefault(music.itemId, false)
                } else {
                    music.ifFavoriteStatus
                }
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
            mutableStateOf(true)
        }

        /**
         * 是否可以删除数据
         */
        val ifDelete by remember {
            derivedStateOf {
                musicBottomMenuViewModel.dataSourceManager.dataSourceType?.ifDelete == true
            }
        }

        MusicInfoBottomComponent(
            musicInfo = music,
            onIfShowMusicInfo = { ifShowMusicInfo },
            onSetShowMusicInfo = { ifShowMusicInfo = it },
            dataSourceType = musicBottomMenuViewModel.connectionConfigServer.connectionConfig?.type
        )



        ModalBottomSheetExtendComponent(
            modifier = Modifier
                .statusBarsPadding(),
            bottomSheetState = sheetState,
            onIfDisplay = { ifShowBottom },
            onClose = {
                coroutineScope.launch {
                    ifShowBottom = false
                    music.dismiss()
                }.invokeOnCompletion {
                    mainViewModel.putIterations(1)
                }
            }
        ) {

            XyColumn(
                verticalArrangement = Arrangement.Top,
                paddingValues = PaddingValues(
                    vertical = XyTheme.dimens.outerVerticalPadding
                ),
                backgroundColor = Color.Transparent,
            ) {
                MusicItemNotClickComponent(
                    modifier = Modifier.padding(
                        horizontal = XyTheme.dimens.outerHorizontalPadding
                    ),
                    onMusicData = {
                        music
                    },
                    backgroundColor = Color.Transparent,
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF5A524C), Color(0xFF726B66)),
                        tileMode = TileMode.Repeated
                    )
                )
                XyRow(paddingValues = PaddingValues(horizontal = XyTheme.dimens.outerHorizontalPadding)) {
                    IconButtonComponent(
                        MusicItemMenuData(
                            imageVector = if (favoriteState)
                                Icons.Rounded.Favorite
                            else
                                Icons.Rounded.FavoriteBorder,
                            name = stringResource(R.string.favorite)
                        ),
                        iconColor = if (favoriteState) Color.Red else MaterialTheme.colorScheme.onSurface,
                        onClick = {
                            coroutineScope.launch {
                                sheetState.hide()
                                musicBottomMenuViewModel.setFavoriteMusic(
                                    itemId = music.itemId,
                                    ifFavorite = favoriteState
                                )
                            }.invokeOnCompletion {
                                ifShowBottom = false

                            }

                        }
                    )


                    IconButtonComponent(
                        MusicItemMenuData(
                            imageVector = Icons.Outlined.Add,
                            name = stringResource(R.string.add_to_playlist)
                        ),
                        onClick = {
                            coroutineScope.launch {
                                sheetState.hide()
                                AddPlaylistBottomData(
                                    ifShow = true,
                                    musicInfoList = listOf(music)
                                ).show()
                            }.invokeOnCompletion {
                                ifShowBottom = false

                            }
                        }
                    )
                    IconButtonComponent(
                        MusicItemMenuData(
                            imageVector = Icons.Outlined.KeyboardDoubleArrowRight,
                            name = stringResource(R.string.skip_head_tail)
                        ),
                        onClick = {
                            coroutineScope.launch {
                                sheetState.hide()
                                ifShowHeadAndTail = true
                            }.invokeOnCompletion {
                                ifShowBottom = false

                            }
                        }
                    )

                    IconButtonComponent(
                        MusicItemMenuData(
                            imageVector = Icons.Outlined.AvTimer,
                            name = stringResource(R.string.timer_close)
                        ),
                        onClick = {
                            coroutineScope.launch {
                                sheetState.hide()
                                ifTimer = true
                            }.invokeOnCompletion {
                                ifShowBottom = false
                            }
                        }
                    )
                    IconButtonComponent(
                        MusicItemMenuData(
                            imageVector = Icons.Outlined.Speed,
                            name = stringResource(R.string.double_speed)
                        ),
                        onClick = {
                            coroutineScope.launch {
                                sheetState.hide()
                                ifDoubleSpeed = true
                            }.invokeOnCompletion {
                                ifShowBottom = false
                            }
                        }
                    )

                    IconButtonComponent(
                        MusicItemMenuData(
                            imageVector = Icons.Outlined.Download,
                            name = "下载"
                        ),
                        onClick = {
                            coroutineScope.launch {
                                musicBottomMenuViewModel.downloadMusic(music)
                            }.invokeOnCompletion {


                            }
                        }
                    )
                }

                RoundedSurfaceColumnPadding(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF275454),
                            Color.Gray.copy(alpha = 0.1f)
                        ), tileMode = TileMode.Repeated
                    )
                ) {
                    IconBottomMenuHor(
                        imageVector = Icons.AutoMirrored.Outlined.PlaylistAdd,
                        text = stringResource(R.string.play_next),
                        onClick = {
                            coroutineScope.launch {
                                sheetState.hide()
                                musicBottomMenuViewModel.musicController.addNextPlayer(
                                    music
                                )
                                MessageUtils.sendPopTip(context.getString(R.string.add_to_next_play_success))
                            }.invokeOnCompletion {
                                ifShowBottom = false
                                music.dismiss()
                            }

                        })

                    IconBottomMenuHor(
                        imageVector = Icons.Outlined.Person,
                        text = "${stringResource(R.string.artist)}: ${music.artists}",
                        onClick = {
                            //获得歌手信息
                            music.artistIds?.let { artistIds ->
                                if (artistIds.isNotBlank()) {
                                    coroutineScope.launch {
                                        sheetState.hide()
                                        if (artistIds.contains(",")) {
                                            ifShowArtistList = true
                                            musicBottomMenuViewModel.getArtistInfos(artistIds)
                                        } else {
                                            navHostController.navigate(ArtistInfo(artistIds))
                                        }
                                    }.invokeOnCompletion {
                                        ifShowBottom = false
                                        music.dismiss()
                                    }
                                }

                            }


                        })

                    IconBottomMenuHor(
                        imageVector = Icons.Outlined.Album,
                        text = "${stringResource(R.string.album)}: ${music.albumName ?: ""}",
                        onClick = {
                            coroutineScope.launch {
                                sheetState.hide()
                                onAlbumRouter(
                                    music.album
                                )
                            }.invokeOnCompletion {
                                ifShowBottom = false
                                music.dismiss()
                            }
                        })

                    IconBottomMenuHor(
                        imageVector = Icons.Outlined.Share,
                        text = stringResource(R.string.share_song),
                        onClick = {
                            if (URLUtil.isNetworkUrl(music.musicUrl)) {
                                val sendIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        music.musicUrl
                                    )
//                                putExtra(Intent.EXTRA_TEXT, "<a href = 'https://www.baidu.com'>百度</a>")
                                    type = "text/plain"
                                }
                                context.startActivity(
                                    sendIntent,
                                    Bundle()
                                )
                            } else {
                                val sendIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(
                                        Intent.EXTRA_STREAM,
                                        music.musicUrl
                                    )
                                    type = "video/*"
                                }
                                context.startActivity(
                                    sendIntent,
                                    Bundle()
                                )
                            }
                            coroutineScope.launch {
                                sheetState.hide()
                            }.invokeOnCompletion {
                                ifShowBottom = false
                                music.dismiss()
                            }

                        })

                    IconBottomMenuHor(
                        imageVector = Icons.Outlined.Info,
                        text = stringResource(R.string.song_info),
                        onClick = {
                            coroutineScope.launch {
                                sheetState.hide()
                                ifShowMusicInfo = true
                            }.invokeOnCompletion {
                                ifShowBottom = false
                            }
                        }
                    )
                    if (ifDelete)
                        IconBottomMenuHor(
                            imageVector = Icons.Outlined.DeleteForever,
                            text = stringResource(R.string.delete_permanently),
                            onClick = {
                                AlertDialogObject(title = R.string.delete_permanently, content = {
                                    XyItemTextHorizontal(
                                        text = stringResource(R.string.delete_warning)
                                    )
                                }, ifWarning = true, onConfirmation = {
                                    coroutineScope.launch {
                                        sheetState.hide()
                                        musicBottomMenuViewModel.removeMusicResource(music)
                                    }.invokeOnCompletion {
                                        ifShowBottom = false
                                        music.dismiss()
                                    }

                                }, onDismissRequest = {}).show()
                            })

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
            onSetTimerInfo = { num, ifApplyRight ->
                musicBottomMenuViewModel.setTimerInfoData(num)
                if (ifCanScheduleExactAlarms && ifApplyRight) {
                    coroutineScope
                        .launch {
                            if (musicBottomMenuViewModel.sliderTimerEndData == 0f || musicBottomMenuViewModel.timerInfo == 0L) {
                                musicBottomMenuViewModel.cancelAlarm()
                            } else {
                                musicBottomMenuViewModel.createMusicStop()
                            }
                        }
                        .invokeOnCompletion {
                        }
                }
            },
            onSliderTimerEndData = { musicBottomMenuViewModel.sliderTimerEndData },
            onSetSliderTimerEndData = { musicBottomMenuViewModel.setSliderTimerEndDataValue(it) },
            onIfPlayEndClose = { musicBottomMenuViewModel.ifPlayEndClose },
            onSetIfPlayEndClose = { musicBottomMenuViewModel.setPlayEndCloseData(it) },
            ifCanScheduleExactAlarms = ifCanScheduleExactAlarms,
            onApplyPermission = {
                val canScheduleExactAlarms =
                    musicBottomMenuViewModel.alarmConfig.returnAm().canScheduleExactAlarms()
                if (!canScheduleExactAlarms) {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = "package:${context.packageName}".toUri()
                    }
                    launcher.launch(intent)
                }
            }

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


    }

}

/**
 * 定时关闭
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun TimerComponent(
    onIfTimer: () -> Boolean,
    onSetIfTimer: (Boolean) -> Unit,
    onTimerInfo: () -> Long,
    onSetTimerInfo: (Long, Boolean) -> Unit,
    onSliderTimerEndData: () -> Float,
    onSetSliderTimerEndData: (Float) -> Unit,
    onIfPlayEndClose: () -> Boolean,
    onSetIfPlayEndClose: (Boolean) -> Unit,
    ifCanScheduleExactAlarms: Boolean,
    onApplyPermission: () -> Unit
) {
    val context = LocalContext.current
    val mainViewModel = LocalMainViewModel.current
    val sheetTimer = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val coroutineScope = rememberCoroutineScope()

    //输入内容
    var customInputValue by remember {
        mutableStateOf("")
    }
    var isError by remember { mutableStateOf(false) }

    var number by remember {
        mutableStateOf("")
    }
    LaunchedEffect(onTimerInfo()) {
        val sdf = SimpleDateFormat.getTimeInstance()
        val calendar = Calendar.getInstance().apply { time = Date() } // 创建Calendar对象并设置为当前时间
        calendar.add(Calendar.MINUTE, onTimerInfo().toInt())
        number = sdf.format(calendar.time)
    }

    ModalBottomSheetExtendComponent(
        bottomSheetState = sheetTimer,
        modifier = Modifier.statusBarsPadding(),
        onIfDisplay = onIfTimer,
        onClose = {
            onSetTimerInfo(0, false)
            onSetSliderTimerEndData(0f)
            mainViewModel.putIterations(1)
            onSetIfTimer(it)
        },
        titleText = stringResource(R.string.timer_close_title),
        titleSub = stringResource(R.string.timer_close_subtitle),
        titleTailContent = if (!ifCanScheduleExactAlarms) {
            {
                OutlinedButton(
                    modifier = Modifier.size(height = 25.dp, width = 50.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp),
                    onClick = {
                        onApplyPermission()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.apply_permission),
                        fontSize = 10.sp,
                    )
                }
                Spacer(modifier = Modifier.width(XyTheme.dimens.outerHorizontalPadding))

                OutlinedButton(
                    modifier = Modifier.size(height = 25.dp, width = 50.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp),
                    enabled = ifCanScheduleExactAlarms,
                    onClick = {
                        coroutineScope
                            .launch {
                                onSetTimerInfo(0, true)
                                onSetSliderTimerEndData(0f)
                                onSetIfPlayEndClose(false)
                            }
                    },
                ) {
                    Text(
                        text = stringResource(R.string.reset),
                        fontSize = 10.sp,
                    )
                }
            }
        } else null
    ) {
        XyItemSlider(
            value = onSliderTimerEndData(),
            enabled = ifCanScheduleExactAlarms,
            onValueChange = {
                if (it >= 75f) {
                    AlertDialogObject(title = R.string.custom_timer_close, content = {
                        XyEdit(
                            text = customInputValue,
                            onChange = { input ->
                                val pattern = Regex("[^0-9]") // 定义正则表达式，匹配至少1位数字
                                var replace = input.replace(pattern, "")
                                if (replace.length >= 4) {
                                    MessageUtils.sendPopTipError(R.string.max_24_hours)
                                    replace = replace.substring(0, 4)
                                } else {
                                    isError = false
                                }
                                customInputValue = replace
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = Done
                            ),
                            singleLine = true,
                            modifier = Modifier.semantics {
                                if (isError) error(context.getString(R.string.max_24_hours))
                            }
                        )
                    }, onDismissRequest = {
                        customInputValue = ""
                        onSetSliderTimerEndData(0F)
                    }, onConfirmation = {
                        if (!isError) {
                            Log.i("=====", "调用设置${customInputValue}")
                            onSetTimerInfo(customInputValue.toLong(), true)
                        }
                        customInputValue = ""
                    }).show()
                } else {
                    onSetTimerInfo(it.toLong(), true)
                }
                onSetSliderTimerEndData(it)
                //判断是否为自定义
            }, valueRange = 0f..75f, steps = 4,
            text = "${stringResource(R.string.countdown_prefix)}${
                when (onSliderTimerEndData()) {
                    0f -> stringResource(R.string.timer_close_disabled)
                    75f -> "${stringResource(R.string.timer_close_custom)} ${onTimerInfo()}${
                        stringResource(
                            R.string.custom_timer_suffix
                        )
                    } $number ${stringResource(R.string.timer_close_title)}"

                    else -> "${onSliderTimerEndData().toInt()}${stringResource(R.string.custom_timer_suffix)} $number ${
                        stringResource(
                            R.string.timer_close_title
                        )
                    }"
                }
            }"
        )

        XyRow {
            XyItemText(text = stringResource(R.string.timer_close_disabled))
            XyItemText(text = stringResource(R.string.fifteen_minutes))
            XyItemText(text = stringResource(R.string.thirty_minutes))
            XyItemText(text = stringResource(R.string.forty_five_minutes))
            XyItemText(text = stringResource(R.string.sixty_minutes))
            XyItemText(text = stringResource(R.string.timer_close_custom))
        }

        XyItemSwitcherNotTextColor(
            state = onIfPlayEndClose(),
            enabled = ifCanScheduleExactAlarms,
            onChange = {
                coroutineScope.launch {
                    onSetIfPlayEndClose(it)
                }
            },
            text = stringResource(R.string.close_after_playback)
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


    ModalBottomSheetExtendComponent(
        bottomSheetState = sheetDoubleSpeed,
        modifier = Modifier.statusBarsPadding(),
        onIfDisplay = onIfDoubleSpeed,
        onClose = {
            onSetIfDoubleSpeed(false)
            mainViewModel.putIterations(1)
        },
        titleText = stringResource(R.string.double_speed_title),
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
                    text = stringResource(R.string.reset),
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
            text = "${stringResource(R.string.playback_speed)}: ${
                when (doubleSpeedTmp) {
                    0.5f -> "0.5"
                    1f -> stringResource(R.string.normal)
                    1.5f -> "1.5"
                    2f -> "2"
                    else -> ""
                }
            }"
        )
        XyRow {
            XyItemText(text = "0.5")
            XyItemText(text = "0")
            XyItemText(text = "1.5")
            XyItemText(text = "2.0")
        }

        XyButtonHorizontalPadding(text = stringResource(R.string.confirm), onClick = {
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
    ModalBottomSheetExtendComponent(
        bottomSheetState = sheetSkip,
        modifier = Modifier.statusBarsPadding(),
        onIfDisplay = onIfShowHeadAndTail,
        onClose = {
            onSetIfShowHeadAndTail(it)
            mainViewModel.putIterations(1)
        },
        titleText = stringResource(R.string.skip_head_tail),
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
                    text = stringResource(R.string.reset),
                    fontSize = 10.sp,
                )
            }
        }
    ) {
        XyColumnNotHorizontalPadding(backgroundColor = Color.Transparent) {
            XyItemSlider(
                value = startTime,
                onValueChange = {
                    startTime = it.toLong().toFloat()
                },
                valueRange = 0f..60f,
                text = "${stringResource(R.string.skip_head_prefix)} ${startTime.toLong()}s"
            )
            XyRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                XyItemText(text = "0s")
                XyItemText(text = "60s")
            }
        }

        XyColumnNotHorizontalPadding(backgroundColor = Color.Transparent) {
            XyItemSlider(
                value = endTime,
                onValueChange = {
                    endTime = it.toLong().toFloat()
                },
                valueRange = 0f..60f,
                text = "${stringResource(R.string.skip_tail_prefix)} ${endTime.toLong()}s"
            )
            XyRow {
                XyItemText(text = "0s")
                XyItemText(text = "60s")
            }
        }

        XyButtonHorizontalPadding(text = stringResource(R.string.confirm), onClick = {
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
 * 设置的单个按钮
 */
@Composable
private fun IconButtonComponent(
    musicItemMenuData: MusicItemMenuData,
    iconColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit,
) {
    XyItemTabButton(
        onClick = onClick,
        imageVector = musicItemMenuData.imageVector,
        text = musicItemMenuData.name,
        iconColor = iconColor,
        color = Color.Transparent
    )
}

/**
 * bottom的hor按钮
 */
@Composable
fun IconBottomMenuHor(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    text: String,
    onClick: () -> Unit,
) {
    XyItemTextIconCheckSelectHeightSmall(
        modifier = modifier,
        text = text,
        icon = imageVector,
        onClick = onClick
    )

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

    ModalBottomSheetExtendComponent(
        modifier = modifier.statusBarsPadding(),
        bottomSheetState = sheetState,
        onIfDisplay = onIfShowMusicInfo,
        onClose = {
            onSetShowMusicInfo(false)
            mainViewModel.putIterations(1)
        },
        titleText = stringResource(R.string.song_info_title)
    ) {

        LazyColumnBottomSheetComponent {
            item {
                RoundedSurfaceColumnPadding(
                    color = Color.Transparent,
                    contentPaddingValues = PaddingValues(vertical = XyTheme.dimens.outerVerticalPadding)
                ) {
                    XyItemTextPadding(text = stringResource(R.string.title), sub = musicInfo.name)
                    XyItemTextPadding(
                        text = stringResource(R.string.artist),
                        sub = musicInfo.artists ?: ""
                    )
                    XyItemTextPadding(
                        text = stringResource(R.string.album),
                        sub = musicInfo.albumName ?: ""
                    )
                    XyItemTextPadding(
                        text = stringResource(R.string.album_artist),
                        sub = musicInfo.albumArtist ?: ""
                    )


                    XyItemTextPadding(
                        text = stringResource(R.string.media_source),
                        sub = dataSourceType?.title ?: ""
                    )
                    XyItemTextPadding(
                        text = stringResource(R.string.duration),
                        sub = millisecondsToTime(
                            BigDecimal(
                                musicInfo.runTimeTicks
                            ).toLong()
                        )
                    )
                    XyItemTextPadding(
                        text = stringResource(R.string.bitrate),
                        sub = "${(musicInfo.bitRate ?: 0) / 1000}kbps"
                    )
                    XyItemTextPadding(
                        text = stringResource(R.string.sample_rate),
                        sub = "${musicInfo.sampleRate ?: 0}Hz"
                    )
                    XyItemTextPadding(
                        text = stringResource(R.string.bit_depth),
                        sub = "${musicInfo.bitDepth ?: 0}bit"
                    )
                    XyItemTextPadding(
                        text = stringResource(R.string.size),
                        sub = "${
                            BigDecimal(musicInfo.size ?: 0).divide(BigDecimal(1024))
                                .divide(
                                    BigDecimal(1024), BigDecimal.ROUND_UP
                                ).toInt()
                        }MB"
                    )
                    XyItemTextPadding(
                        text = stringResource(R.string.format),
                        sub = musicInfo.container ?: ""
                    )
                    XyItemTextPadding(
                        text = stringResource(R.string.play_path),
                        sub = musicInfo.musicUrl
                    )
                    XyItemTextPadding(
                        text = stringResource(R.string.actual_path),
                        sub = musicInfo.path
                    )
                    XyItemTextPadding(
                        text = stringResource(R.string.add_time),
                        sub = musicInfo.createTime.toDateStr(
                            "yyyy/MM/dd HH:mm"
                        )
                    )

                }
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
    val navHostController = LocalNavController.current
    val coroutineScope = rememberCoroutineScope()

    ModalBottomSheetExtendComponent(
        modifier = modifier.statusBarsPadding(),
        bottomSheetState = sheetState,
        onIfDisplay = onIfShowArtistList,
        onClose = {
            onSetShowArtistList(false)
            mainViewModel.putIterations(1)
        },
        titleText = stringResource(R.string.artist_list_title)
    ) {
        LazyVerticalGridComponent {
            items(artistList, key = { it.artistId }) { artist ->
                MusicArtistCardComponent(
                    onItem = { artist },
                    onRouter = {
                        navHostController.navigate(ArtistInfo(it))
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

fun XyMusic.show() = apply {
    bottomMenuMusicInfo.add(this@show)
}

fun XyMusic.dismiss() = apply {
    bottomMenuMusicInfo.remove(this@dismiss)
}