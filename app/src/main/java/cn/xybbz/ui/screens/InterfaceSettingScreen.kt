package cn.xybbz.ui.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cn.xybbz.R
import cn.xybbz.compositionLocal.LocalNavController
import cn.xybbz.ui.components.AlertDialogObject
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.dismiss
import cn.xybbz.ui.components.show
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.LazyColumnNotComponent
import cn.xybbz.ui.xy.RoundedSurfaceColumnPadding
import cn.xybbz.ui.xy.XyButtonNotPadding
import cn.xybbz.ui.xy.XyColumn
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyItemSwitcherNotTextColor
import cn.xybbz.ui.xy.XyItemTextPadding
import cn.xybbz.ui.xy.XyItemTitlePadding
import cn.xybbz.ui.xy.XyRow
import cn.xybbz.ui.xy.XyRowButton
import cn.xybbz.viewmodel.InterfaceSettingViewModel
import com.github.skydoves.colorpicker.compose.AlphaSlider
import com.github.skydoves.colorpicker.compose.AlphaTile
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.ColorPickerController
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import kotlinx.coroutines.launch

/**
 * 界面设置
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterfaceSettingScreen(
    interfaceSettingViewModel: InterfaceSettingViewModel = hiltViewModel<InterfaceSettingViewModel>()
) {

    val navHostController = LocalNavController.current
    val coroutineScope = rememberCoroutineScope()

    XyColumnScreen(
        modifier = Modifier.brashColor(
            topVerticalColor = interfaceSettingViewModel.backgroundConfig.homeBrash[0],
            bottomVerticalColor = interfaceSettingViewModel.backgroundConfig.homeBrash[1]
        )
    ) {
        TopAppBarComponent(
            modifier = Modifier.statusBarsPadding(),
            title = {
                Text(
                    text = stringResource(R.string.interface_settings),
                    fontWeight = FontWeight.W900
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = {
                        navHostController.popBackStack()
                    },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.return_setting_screen)
                    )
                }
            }
        )

        LazyColumnNotComponent {
            item {
                XyRow {
                    XyItemTextPadding(
                        text = stringResource(R.string.interface_screen),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    TextButton(onClick = {
                        coroutineScope.launch {
                            interfaceSettingViewModel.backgroundConfig.reset()
                        }
                    }) {
                        Text(stringResource(R.string.reset))
                    }
                }

            }
            item {
                RoundedSurfaceColumnPadding(color = Color.Black.copy(alpha = 0.3f)) {
                    XyItemSwitcherNotTextColor(
                        state = interfaceSettingViewModel.backgroundConfig.ifChangeOneColor,
                        text = stringResource(R.string.single_color_setting),
                        onChange = {
                            coroutineScope.launch {
                                interfaceSettingViewModel.backgroundConfig.updateIfChangeOneColor(it)
                            }
                        })

                    XyItemSwitcherNotTextColor(
                        state = interfaceSettingViewModel.backgroundConfig.ifGlobalBrash,
                        text = stringResource(R.string.global_gradient_setting),
                        onChange = {
                            coroutineScope.launch {
                                interfaceSettingViewModel.backgroundConfig.updateIfGlobalBrash(it)
                            }
                        })

                    if (interfaceSettingViewModel.backgroundConfig.ifGlobalBrash)
                        BrashColorConfigItem(
                            title = R.string.global_gradient_colors,
                            onIfChangeOneColor = { interfaceSettingViewModel.backgroundConfig.ifChangeOneColor },
                            onTopColor = { interfaceSettingViewModel.backgroundConfig.globalBrash[0] },
                            onBottomColor = { interfaceSettingViewModel.backgroundConfig.globalBrash[1] },
                            onDefaultColors = {
                                interfaceSettingViewModel.backgroundConfig.stringToColors(
                                    interfaceSettingViewModel.backgroundConfig.defaultBackgroundConfig.globalBrash
                                )
                            },
                            onSetColors = { colorStrList, colors ->
                                coroutineScope.launch {
                                    interfaceSettingViewModel.backgroundConfig.updateGlobalBrash(
                                        colorStrList,
                                        colors
                                    )
                                }
                            }
                        )
                    else {

                        BrashColorConfigItem(
                            title = R.string.home_background_gradient,
                            onIfChangeOneColor = { interfaceSettingViewModel.backgroundConfig.ifChangeOneColor },
                            onTopColor = { interfaceSettingViewModel.backgroundConfig.homeBrash[0] },
                            onBottomColor = { interfaceSettingViewModel.backgroundConfig.homeBrash[1] },
                            onDefaultColors = {
                                interfaceSettingViewModel.backgroundConfig.stringToColors(
                                    interfaceSettingViewModel.backgroundConfig.defaultBackgroundConfig.homeBrash
                                )
                            },
                            onSetColors = { colorStrList, colors ->
                                coroutineScope.launch {
                                    interfaceSettingViewModel.backgroundConfig.updateHomeBrash(
                                        colorStrList,
                                        colors
                                    )
                                }
                            }
                        )

                        BrashColorConfigItem(
                            title = R.string.music_list_background_gradient,
                            onIfChangeOneColor = { interfaceSettingViewModel.backgroundConfig.ifChangeOneColor },
                            onTopColor = { interfaceSettingViewModel.backgroundConfig.musicBrash[0] },
                            onBottomColor = { interfaceSettingViewModel.backgroundConfig.musicBrash[1] },
                            onDefaultColors = {
                                interfaceSettingViewModel.backgroundConfig.stringToColors(
                                    interfaceSettingViewModel.backgroundConfig.defaultBackgroundConfig.musicBrash
                                )
                            },
                            onSetColors = { colorStrList, colors ->
                                coroutineScope.launch {
                                    interfaceSettingViewModel.backgroundConfig.updateMusicBrash(
                                        colorStrList,
                                        colors
                                    )
                                }
                            }
                        )

                        BrashColorConfigItem(
                            title = R.string.album_list_background_gradient,
                            onIfChangeOneColor = { interfaceSettingViewModel.backgroundConfig.ifChangeOneColor },
                            onTopColor = { interfaceSettingViewModel.backgroundConfig.albumBrash[0] },
                            onBottomColor = { interfaceSettingViewModel.backgroundConfig.albumBrash[1] },
                            onDefaultColors = {
                                interfaceSettingViewModel.backgroundConfig.stringToColors(
                                    interfaceSettingViewModel.backgroundConfig.defaultBackgroundConfig.albumBrash
                                )
                            },
                            onSetColors = { colorStrList, colors ->
                                coroutineScope.launch {
                                    interfaceSettingViewModel.backgroundConfig.updateAlbumBrash(
                                        colorStrList,
                                        colors
                                    )
                                }
                            }
                        )

                        BrashColorConfigItem(
                            title = R.string.album_detail_background_gradient,
                            onIfChangeOneColor = { interfaceSettingViewModel.backgroundConfig.ifChangeOneColor },
                            onTopColor = { interfaceSettingViewModel.backgroundConfig.albumInfoBrash[0] },
                            onBottomColor = { interfaceSettingViewModel.backgroundConfig.albumInfoBrash[1] },
                            onDefaultColors = {
                                interfaceSettingViewModel.backgroundConfig.stringToColors(
                                    interfaceSettingViewModel.backgroundConfig.defaultBackgroundConfig.albumInfoBrash
                                )
                            },
                            onSetColors = { colorStrList, colors ->
                                coroutineScope.launch {
                                    interfaceSettingViewModel.backgroundConfig.updateAlbumInfoBrash(
                                        colorStrList,
                                        colors
                                    )
                                }
                            }
                        )

                        BrashColorConfigItem(
                            title = R.string.artist_list_background_gradient,
                            onIfChangeOneColor = { interfaceSettingViewModel.backgroundConfig.ifChangeOneColor },
                            onTopColor = { interfaceSettingViewModel.backgroundConfig.artistBrash[0] },
                            onBottomColor = { interfaceSettingViewModel.backgroundConfig.artistBrash[1] },
                            onDefaultColors = {
                                interfaceSettingViewModel.backgroundConfig.stringToColors(
                                    interfaceSettingViewModel.backgroundConfig.defaultBackgroundConfig.artistBrash
                                )
                            },
                            onSetColors = { colorStrList, colors ->
                                coroutineScope.launch {
                                    interfaceSettingViewModel.backgroundConfig.updateArtistBrash(
                                        colorStrList,
                                        colors
                                    )
                                }
                            }
                        )

                        BrashColorConfigItem(
                            title = R.string.artist_detail_background_gradient,
                            onIfChangeOneColor = { interfaceSettingViewModel.backgroundConfig.ifChangeOneColor },
                            onTopColor = { interfaceSettingViewModel.backgroundConfig.artistInfoBrash[0] },
                            onBottomColor = { interfaceSettingViewModel.backgroundConfig.artistInfoBrash[1] },
                            onDefaultColors = {
                                interfaceSettingViewModel.backgroundConfig.stringToColors(
                                    interfaceSettingViewModel.backgroundConfig.defaultBackgroundConfig.artistInfoBrash
                                )
                            },
                            onSetColors = { colorStrList, colors ->
                                coroutineScope.launch {
                                    interfaceSettingViewModel.backgroundConfig.updateArtistInfoBrash(
                                        colorStrList,
                                        colors
                                    )
                                }
                            }
                        )

                        BrashColorConfigItem(
                            title = R.string.favorite_list_background_gradient,
                            onIfChangeOneColor = { interfaceSettingViewModel.backgroundConfig.ifChangeOneColor },
                            onTopColor = { interfaceSettingViewModel.backgroundConfig.favoriteBrash[0] },
                            onBottomColor = { interfaceSettingViewModel.backgroundConfig.favoriteBrash[1] },
                            onDefaultColors = {
                                interfaceSettingViewModel.backgroundConfig.stringToColors(
                                    interfaceSettingViewModel.backgroundConfig.defaultBackgroundConfig.favoriteBrash
                                )
                            },
                            onSetColors = { colorStrList, colors ->
                                coroutineScope.launch {
                                    interfaceSettingViewModel.backgroundConfig.updateFavoriteBrash(
                                        colorStrList,
                                        colors
                                    )
                                }
                            }
                        )

                        BrashColorConfigItem(
                            title = R.string.genre_list_background_gradient,
                            onIfChangeOneColor = { interfaceSettingViewModel.backgroundConfig.ifChangeOneColor },
                            onTopColor = { interfaceSettingViewModel.backgroundConfig.genresBrash[0] },
                            onBottomColor = { interfaceSettingViewModel.backgroundConfig.genresBrash[1] },
                            onDefaultColors = {
                                interfaceSettingViewModel.backgroundConfig.stringToColors(
                                    interfaceSettingViewModel.backgroundConfig.defaultBackgroundConfig.genresBrash
                                )
                            },
                            onSetColors = { colorStrList, colors ->
                                coroutineScope.launch {
                                    interfaceSettingViewModel.backgroundConfig.updateGenresBrash(
                                        colorStrList,
                                        colors
                                    )
                                }
                            }
                        )

                        BrashColorConfigItem(
                            title = R.string.genre_detail_background_gradient,
                            onIfChangeOneColor = { interfaceSettingViewModel.backgroundConfig.ifChangeOneColor },
                            onTopColor = { interfaceSettingViewModel.backgroundConfig.genresInfoBrash[0] },
                            onBottomColor = { interfaceSettingViewModel.backgroundConfig.genresInfoBrash[1] },
                            onDefaultColors = {
                                interfaceSettingViewModel.backgroundConfig.stringToColors(
                                    interfaceSettingViewModel.backgroundConfig.defaultBackgroundConfig.genresInfoBrash
                                )
                            },
                            onSetColors = { colorStrList, colors ->
                                coroutineScope.launch {
                                    interfaceSettingViewModel.backgroundConfig.updateGenresInfoBrash(
                                        colorStrList,
                                        colors
                                    )
                                }
                            }
                        )

                        BrashColorConfigItem(
                            title = R.string.settings_page_background_gradient,
                            onIfChangeOneColor = { interfaceSettingViewModel.backgroundConfig.ifChangeOneColor },
                            onTopColor = { interfaceSettingViewModel.backgroundConfig.settingsBrash[0] },
                            onBottomColor = { interfaceSettingViewModel.backgroundConfig.settingsBrash[1] },
                            onDefaultColors = {
                                interfaceSettingViewModel.backgroundConfig.stringToColors(
                                    interfaceSettingViewModel.backgroundConfig.defaultBackgroundConfig.settingsBrash
                                )
                            },
                            onSetColors = { colorStrList, colors ->
                                coroutineScope.launch {
                                    interfaceSettingViewModel.backgroundConfig.updateSettingsBrash(
                                        colorStrList,
                                        colors
                                    )
                                }
                            }
                        )

                        BrashColorConfigItem(
                            title = R.string.about_page_background_gradient,
                            onIfChangeOneColor = { interfaceSettingViewModel.backgroundConfig.ifChangeOneColor },
                            onTopColor = { interfaceSettingViewModel.backgroundConfig.aboutBrash[0] },
                            onBottomColor = { interfaceSettingViewModel.backgroundConfig.aboutBrash[1] },
                            onDefaultColors = {
                                interfaceSettingViewModel.backgroundConfig.stringToColors(
                                    interfaceSettingViewModel.backgroundConfig.defaultBackgroundConfig.aboutBrash
                                )
                            },
                            onSetColors = { colorStrList, colors ->
                                coroutineScope.launch {
                                    interfaceSettingViewModel.backgroundConfig.updateAboutBrash(
                                        colorStrList,
                                        colors
                                    )
                                }
                            }
                        )

                        BrashColorConfigItem(
                            title = R.string.connection_management_background_gradient,
                            onIfChangeOneColor = { interfaceSettingViewModel.backgroundConfig.ifChangeOneColor },
                            onTopColor = { interfaceSettingViewModel.backgroundConfig.connectionManagerBrash[0] },
                            onBottomColor = { interfaceSettingViewModel.backgroundConfig.connectionManagerBrash[1] },
                            onDefaultColors = {
                                interfaceSettingViewModel.backgroundConfig.stringToColors(
                                    interfaceSettingViewModel.backgroundConfig.defaultBackgroundConfig.connectionManagerBrash
                                )
                            },
                            onSetColors = { colorStrList, colors ->
                                coroutineScope.launch {
                                    interfaceSettingViewModel.backgroundConfig.updateConnectionManagerBrash(
                                        colorStrList,
                                        colors
                                    )
                                }
                            }
                        )


                        BrashColorConfigItem(
                            title = R.string.connection_detail_background_gradient,
                            onIfChangeOneColor = { interfaceSettingViewModel.backgroundConfig.ifChangeOneColor },
                            onTopColor = { interfaceSettingViewModel.backgroundConfig.connectionInfoBrash[0] },
                            onBottomColor = { interfaceSettingViewModel.backgroundConfig.connectionInfoBrash[1] },
                            onDefaultColors = {
                                interfaceSettingViewModel.backgroundConfig.stringToColors(
                                    interfaceSettingViewModel.backgroundConfig.defaultBackgroundConfig.connectionInfoBrash
                                )
                            },
                            onSetColors = { colorStrList, colors ->
                                coroutineScope.launch {
                                    interfaceSettingViewModel.backgroundConfig.updateConnectionInfoBrash(
                                        colorStrList,
                                        colors
                                    )
                                }
                            }
                        )


                        BrashColorConfigItem(
                            title = R.string.search_page_background_gradient,
                            onIfChangeOneColor = { interfaceSettingViewModel.backgroundConfig.ifChangeOneColor },
                            onTopColor = { interfaceSettingViewModel.backgroundConfig.searchBrash[0] },
                            onBottomColor = { interfaceSettingViewModel.backgroundConfig.searchBrash[1] },
                            onDefaultColors = {
                                interfaceSettingViewModel.backgroundConfig.stringToColors(
                                    interfaceSettingViewModel.backgroundConfig.defaultBackgroundConfig.searchBrash
                                )
                            },
                            onSetColors = { colorStrList, colors ->
                                coroutineScope.launch {
                                    interfaceSettingViewModel.backgroundConfig.updateSearchBrash(
                                        colorStrList,
                                        colors
                                    )
                                }
                            }
                        )


                        BrashColorConfigItem(
                            title = R.string.cache_limit_setting_background_gradient,
                            onIfChangeOneColor = { interfaceSettingViewModel.backgroundConfig.ifChangeOneColor },
                            onTopColor = { interfaceSettingViewModel.backgroundConfig.cacheLimitBrash[0] },
                            onBottomColor = { interfaceSettingViewModel.backgroundConfig.cacheLimitBrash[1] },
                            onDefaultColors = {
                                interfaceSettingViewModel.backgroundConfig.stringToColors(
                                    interfaceSettingViewModel.backgroundConfig.defaultBackgroundConfig.cacheLimitBrash
                                )
                            },
                            onSetColors = { colorStrList, colors ->
                                coroutineScope.launch {
                                    interfaceSettingViewModel.backgroundConfig.updateCacheLimitBrash(
                                        colorStrList,
                                        colors
                                    )
                                }
                            }
                        )


                        BrashColorConfigItem(
                            title = R.string.language_switching_background_gradient,
                            onIfChangeOneColor = { interfaceSettingViewModel.backgroundConfig.ifChangeOneColor },
                            onTopColor = { interfaceSettingViewModel.backgroundConfig.languageBrash[0] },
                            onBottomColor = { interfaceSettingViewModel.backgroundConfig.languageBrash[1] },
                            onDefaultColors = {
                                interfaceSettingViewModel.backgroundConfig.stringToColors(
                                    interfaceSettingViewModel.backgroundConfig.defaultBackgroundConfig.languageBrash
                                )
                            },
                            onSetColors = { colorStrList, colors ->
                                coroutineScope.launch {
                                    interfaceSettingViewModel.backgroundConfig.updateLanguageBrash(
                                        colorStrList,
                                        colors
                                    )
                                }
                            }
                        )


                        BrashColorConfigItem(
                            title = R.string.storage_management_background_gradient,
                            onIfChangeOneColor = { interfaceSettingViewModel.backgroundConfig.ifChangeOneColor },
                            onTopColor = { interfaceSettingViewModel.backgroundConfig.memoryManagementBrash[0] },
                            onBottomColor = { interfaceSettingViewModel.backgroundConfig.memoryManagementBrash[1] },
                            onDefaultColors = {
                                interfaceSettingViewModel.backgroundConfig.stringToColors(
                                    interfaceSettingViewModel.backgroundConfig.defaultBackgroundConfig.memoryManagementBrash
                                )
                            },
                            onSetColors = { colorStrList, colors ->
                                coroutineScope.launch {
                                    interfaceSettingViewModel.backgroundConfig.updateMemoryManagementBrash(
                                        colorStrList,
                                        colors
                                    )
                                }
                            }
                        )


                        BrashColorConfigItem(
                            title = R.string.bottom_player_gradient,
                            onIfChangeOneColor = { interfaceSettingViewModel.backgroundConfig.ifChangeOneColor },
                            onTopColor = { interfaceSettingViewModel.backgroundConfig.bottomPlayerBrash[0] },
                            onBottomColor = { interfaceSettingViewModel.backgroundConfig.bottomPlayerBrash[1] },
                            onDefaultColors = {
                                interfaceSettingViewModel.backgroundConfig.stringToColors(
                                    interfaceSettingViewModel.backgroundConfig.defaultBackgroundConfig.bottomPlayerBrash
                                )
                            },
                            onSetColors = { colorStrList, colors ->
                                coroutineScope.launch {
                                    interfaceSettingViewModel.backgroundConfig.updateBottomPlayerBrash(
                                        colorStrList,
                                        colors
                                    )
                                }
                            }
                        )


                        BrashColorConfigItem(
                            title = R.string.bottom_sheet_gradient,
                            onIfChangeOneColor = { interfaceSettingViewModel.backgroundConfig.ifChangeOneColor },
                            onTopColor = { interfaceSettingViewModel.backgroundConfig.bottomSheetBrash[0] },
                            onBottomColor = { interfaceSettingViewModel.backgroundConfig.bottomSheetBrash[1] },
                            onDefaultColors = {
                                interfaceSettingViewModel.backgroundConfig.stringToColors(
                                    interfaceSettingViewModel.backgroundConfig.defaultBackgroundConfig.bottomSheetBrash
                                )
                            },
                            onSetColors = { colorStrList, colors ->
                                coroutineScope.launch {
                                    interfaceSettingViewModel.backgroundConfig.updateBottomSheetBrash(
                                        colorStrList,
                                        colors
                                    )
                                }
                            }
                        )


                        BrashColorConfigItem(
                            title = R.string.dialog_gradient,
                            onIfChangeOneColor = { interfaceSettingViewModel.backgroundConfig.ifChangeOneColor },
                            onTopColor = { interfaceSettingViewModel.backgroundConfig.alertDialogBrash[0] },
                            onBottomColor = { interfaceSettingViewModel.backgroundConfig.alertDialogBrash[1] },
                            onDefaultColors = {
                                interfaceSettingViewModel.backgroundConfig.stringToColors(
                                    interfaceSettingViewModel.backgroundConfig.defaultBackgroundConfig.alertDialogBrash
                                )
                            },
                            onSetColors = { colorStrList, colors ->
                                coroutineScope.launch {
                                    interfaceSettingViewModel.backgroundConfig.updateAlertDialogBrash(
                                        colorStrList,
                                        colors
                                    )
                                }
                            }
                        )


                        BrashColorConfigItem(
                            title = R.string.error_dialog_gradient,
                            onIfChangeOneColor = { interfaceSettingViewModel.backgroundConfig.ifChangeOneColor },
                            onTopColor = { interfaceSettingViewModel.backgroundConfig.errorAlertDialogBrash[0] },
                            onBottomColor = { interfaceSettingViewModel.backgroundConfig.errorAlertDialogBrash[1] },
                            onDefaultColors = {
                                interfaceSettingViewModel.backgroundConfig.stringToColors(
                                    interfaceSettingViewModel.backgroundConfig.defaultBackgroundConfig.errorAlertDialogBrash
                                )
                            },
                            onSetColors = { colorStrList, colors ->
                                coroutineScope.launch {
                                    interfaceSettingViewModel.backgroundConfig.updateErrorAlertDialogBrash(
                                        colorStrList,
                                        colors
                                    )
                                }
                            }
                        )

                        BrashColorConfigItem(
                            title = R.string.select_library_background_gradient,
                            onIfChangeOneColor = { interfaceSettingViewModel.backgroundConfig.ifChangeOneColor },
                            onTopColor = { interfaceSettingViewModel.backgroundConfig.selectLibraryBrash[0] },
                            onBottomColor = { interfaceSettingViewModel.backgroundConfig.selectLibraryBrash[1] },
                            onDefaultColors = {
                                interfaceSettingViewModel.backgroundConfig.stringToColors(
                                    interfaceSettingViewModel.backgroundConfig.defaultBackgroundConfig.selectLibraryBrash
                                )
                            },
                            onSetColors = { colorStrList, colors ->
                                coroutineScope.launch {
                                    interfaceSettingViewModel.backgroundConfig.updateSelectLibraryBrash(
                                        colorStrList,
                                        colors
                                    )
                                }
                            }
                        )

                    }

                    ColorConfigItem(
                        title = R.string.player_default_background_color,
                        onColor = { interfaceSettingViewModel.backgroundConfig.playerBackground },
                        onDefaultColor = {
                            interfaceSettingViewModel.backgroundConfig.stringToColor(
                                interfaceSettingViewModel.backgroundConfig.defaultBackgroundConfig.playerBackground
                            )
                        },
                        onSetColor = { colorStr, color ->
                            coroutineScope.launch {
                                interfaceSettingViewModel.backgroundConfig.updatePlayerBackground(
                                    colorStr,
                                    color
                                )
                            }
                        })
                }
            }
            /*item {
                XyItemTextPadding(
                    text = "背景图片选择",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }*/
        }
    }
}


@Composable
private fun SelectColorItem(
    modifier: Modifier = Modifier,
    controller: ColorPickerController,
    initialColor: Color? = null,
    onColorChanged: (colorEnvelope: ColorEnvelope) -> Unit = {}
) {
    XyColumn(
        modifier = modifier,
        backgroundColor = Color.Transparent,
        paddingValues = PaddingValues()
    ) {
        HsvColorPicker(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            controller = controller,
            onColorChanged = onColorChanged,
            initialColor = initialColor
        )

        AlphaSlider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .height(35.dp),
            controller = controller,
            initialColor = initialColor
        )

        BrightnessSlider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .height(35.dp),
            controller = controller,
            initialColor = initialColor
        )
    }
}

@Composable
private fun PreviewColorsItem(
    modifier: Modifier = Modifier,
    onTopColor: () -> Color,
    onBottomColor: () -> Color
) {
    Box(
        modifier = modifier
            .brashColor(
                onTopColor(),
                onBottomColor()
            )
    )
}


@Composable
private fun PreviewColorItem(
    modifier: Modifier = Modifier,
    onTopColor: () -> Color
) {
    Box(
        modifier = modifier
            .size(XyTheme.dimens.itemHeight)
            .clip(RoundedCornerShape(XyTheme.dimens.corner))
            .drawBehind {
                drawRect(onTopColor())
            }
    )
}


@Composable
private fun BrashColorConfigItem(
    @StringRes title: Int,
    modifier: Modifier = Modifier,
    onIfChangeOneColor: () -> Boolean = { false },
    onTopColor: () -> Color,
    onBottomColor: () -> Color,
    onDefaultColors: () -> List<Color>,
    onSetColors: (List<String>, List<Color>) -> Unit,
) {

    val topColorController = rememberColorPickerController()
    val bottomColorController = rememberColorPickerController()
    var topColor by remember {
        mutableStateOf(onTopColor())
    }

    var topColorStr by remember {
        mutableStateOf("#%08X".format(onTopColor().toArgb()))
    }

    var bottomColor by remember {
        mutableStateOf(onBottomColor())
    }

    var bottomColorStr by remember {
        mutableStateOf("#%08X".format(onBottomColor().toArgb()))
    }

    XyRowButton(modifier = modifier, onClick = {
        AlertDialogObject(
            title = title,
            content = { alertObject ->
                XyColumn(
                    paddingValues = PaddingValues(),
                    backgroundColor = Color.Transparent
                ) {
                    XyRow(
                        paddingValues = PaddingValues(),
                    ) {
                        SelectColorItem(
                            modifier = Modifier.weight(1f),
                            controller = topColorController,
                            onColorChanged = { colorEnvelope: ColorEnvelope ->
                                val color: Color =
                                    colorEnvelope.color // ARGB color value.
                                topColor = color
                                val hexCode: String =
                                    colorEnvelope.hexCode // Color hex code, which represents color value.
                                topColorStr = "#${hexCode}"
                            },
                            initialColor = onTopColor()
                        )
                        if (!onIfChangeOneColor())
                            SelectColorItem(
                                modifier = Modifier.weight(1f),
                                controller = bottomColorController,
                                onColorChanged = { colorEnvelope: ColorEnvelope ->
                                    val color: Color =
                                        colorEnvelope.color // ARGB color value.
                                    bottomColor = color
                                    val hexCode: String =
                                        colorEnvelope.hexCode // Color hex code, which represents color value.
                                    bottomColorStr = "#${hexCode}"
                                },
                                initialColor = onBottomColor()
                            )
                    }
                    if (onIfChangeOneColor())
                        PreviewColorItem(onTopColor = { topColorController.selectedColor.value })
                    else
                        PreviewColorsItem(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(XyTheme.dimens.corner)),
                            onTopColor = { topColorController.selectedColor.value },
                            onBottomColor = { bottomColorController.selectedColor.value }
                        )

                    XyButtonNotPadding(onClick = {
                        onSetColors(
                            onDefaultColors().map {
                                "#%08X".format(it.toArgb())
                            },
                            onDefaultColors()
                        )
                        alertObject.dismiss()
                    }, text = stringResource(R.string.reset))
                }


            }, onConfirmation = {
                onSetColors(listOf(topColorStr, bottomColorStr), listOf(topColor, bottomColor))
            }, onDismissRequest = {}).show()

    }) {
        XyItemTitlePadding(
            modifier = Modifier, text = stringResource(title), paddingValues = PaddingValues(
                vertical = XyTheme.dimens.innerVerticalPadding
            )
        )

        if (onIfChangeOneColor())
            PreviewColorItem(onTopColor = onTopColor)
        else
            PreviewColorsItem(
                modifier = Modifier
                    .size(XyTheme.dimens.itemHeight)
                    .clip(RoundedCornerShape(XyTheme.dimens.corner)),
                onTopColor = onTopColor,
                onBottomColor = onBottomColor
            )
    }
}


@Composable
private fun ColorConfigItem(
    @StringRes title: Int,
    modifier: Modifier = Modifier,
    onColor: () -> Color,
    onDefaultColor: () -> Color,
    onSetColor: (String, Color) -> Unit,
) {

    val colorController = rememberColorPickerController()
    var tmpColor by remember {
        mutableStateOf(onColor())
    }

    var tmpColorStr by remember {
        mutableStateOf("#%08X".format(onColor().toArgb()))
    }

    XyRowButton(modifier = modifier, onClick = {
        AlertDialogObject(
            title = title,
            content = { alertObject ->
                XyColumn(
                    paddingValues = PaddingValues(),
                    backgroundColor = Color.Transparent
                ) {
                    SelectColorItem(
                        modifier = Modifier,
                        controller = colorController,
                        onColorChanged = { colorEnvelope: ColorEnvelope ->
                            val color: Color =
                                colorEnvelope.color // ARGB color value.
                            tmpColor = color
                            val hexCode: String =
                                colorEnvelope.hexCode // Color hex code, which represents color value.
                            tmpColorStr = "#${hexCode}"
                        },
                        initialColor = onColor()
                    )
                    AlphaTile(
                        modifier = Modifier
                            .size(XyTheme.dimens.itemHeight)
                            .clip(RoundedCornerShape(XyTheme.dimens.corner)),
                        controller = colorController,
                    )
                }
                XyButtonNotPadding(onClick = {
                    onSetColor(
                        "#%08X".format(onDefaultColor().toArgb()),
                        onDefaultColor()
                    )
                    alertObject.dismiss()
                }, text = stringResource(R.string.reset))

            }, onConfirmation = {
                onSetColor(tmpColorStr, tmpColor)
            }, onDismissRequest = {}).show()

    }) {
        XyItemTitlePadding(
            modifier = Modifier, text = stringResource(title), paddingValues = PaddingValues(
                vertical = XyTheme.dimens.innerVerticalPadding
            )
        )

        Box(
            modifier = Modifier
                .size(XyTheme.dimens.itemHeight)
                .clip(RoundedCornerShape(XyTheme.dimens.corner))
                .drawBehind {
                    drawRect(onColor())
                }
        )
    }
}