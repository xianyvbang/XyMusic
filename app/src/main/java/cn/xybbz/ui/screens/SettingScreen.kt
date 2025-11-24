package cn.xybbz.ui.screens


import android.content.ClipData
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cn.xybbz.R
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.compositionLocal.LocalNavController
import cn.xybbz.router.RouterConstants
import cn.xybbz.ui.components.MusicSettingSwitchItemComponent
import cn.xybbz.ui.components.SettingItemComponent
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.LazyColumnNotComponent
import cn.xybbz.ui.xy.RoundedSurfaceColumnPadding
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyItemTitle
import cn.xybbz.ui.xy.XyRow
import cn.xybbz.viewmodel.SettingsViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.launch

/**
 * 设置页面
 */
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class, ExperimentalPermissionsApi::class
)
@Composable
fun SettingScreen(
    settingsViewModel: SettingsViewModel = hiltViewModel<SettingsViewModel>()
) {

    val navController = LocalNavController.current
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        Log.i("=====", "MusicSettingScreen: ")
    }

    val clipboardManager = LocalClipboard.current

    XyColumnScreen(
        modifier = Modifier.brashColor(
            topVerticalColor = settingsViewModel.backgroundConfig.settingsBrash[0],
            bottomVerticalColor = settingsViewModel.backgroundConfig.settingsBrash[1]
        )
    ) {
        TopAppBarComponent(
            modifier = Modifier.statusBarsPadding(),
            title = {
                Text(
                    text = stringResource(R.string.settings)
                )
            }, navigationIcon = {
                IconButton(
                    onClick = {
                        navController.popBackStack()
                    },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.return_home)
                    )
                }
            })

        LazyColumnNotComponent(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding),
            contentPadding = PaddingValues()
        ) {
            item {
                XyRow(
                    paddingValues = PaddingValues(
                        start = XyTheme.dimens.outerHorizontalPadding,
                        end = XyTheme.dimens.outerHorizontalPadding,
                        top = XyTheme.dimens.outerVerticalPadding
                    ),
                    horizontalArrangement = Arrangement.Start
                ) {
                    XyItemTitle(
                        text = stringResource(R.string.playback)
                    )
                }

            }
            item {
                SettingRoundedSurfaceColumn {

                    MusicSettingSwitchItemComponent(
                        title = stringResource(R.string.prioritize_local_data),
                        ifChecked = settingsViewModel.settingDataNow.isLocal
                    ) { bol ->
                        coroutineScope.launch {
                            settingsViewModel.settingsConfig.setIsLocalData(
                                bol
                            )
                        }
                    }

                    SettingItemComponent(title = R.string.cache_limit) {
                        navController.navigate(RouterConstants.CacheLimit)
                    }

                    MusicSettingSwitchItemComponent(
                        title = stringResource(R.string.album_playback_history),
                        ifChecked = settingsViewModel.settingDataNow.ifEnableAlbumHistory
                    ) { bol ->
                        coroutineScope.launch {
                            settingsViewModel.settingsConfig.setIfEnableAlbumHistory(
                                bol
                            )
                        }
                    }

                    MusicSettingSwitchItemComponent(
                        title = stringResource(R.string.allow_simultaneous_playback),
                        ifChecked = settingsViewModel.settingDataNow.ifHandleAudioFocus
                    ) { bol ->
                        coroutineScope.launch {
                            settingsViewModel.settingsConfig.setIfHandleAudioFocus(
                                bol
                            )
                        }
                    }

                    SettingItemComponent(
                        title = R.string.cache_location,
                        bottomInfo = settingsViewModel.settingsConfig.cacheFilePath,
                        maxLines = Int.MAX_VALUE
                    ) {
                        if (settingsViewModel.settingsConfig.cacheFilePath.isNotBlank()) {
                            val clipData =
                                ClipData.newPlainText(
                                    "label",
                                    settingsViewModel.settingsConfig.cacheFilePath
                                )
                            coroutineScope.launch {
                                clipboardManager.setClipEntry(ClipEntry(clipData))
                            }.invokeOnCompletion {
                                MessageUtils.sendPopTip(context.getString(R.string.copy_success))
                            }
                        }

                    }

                }
            }
            item {
                XyRow(
                    paddingValues = PaddingValues(
                        start = XyTheme.dimens.outerHorizontalPadding,
                        end = XyTheme.dimens.outerHorizontalPadding,
                        top = XyTheme.dimens.outerVerticalPadding
                    ),
                    horizontalArrangement = Arrangement.Start
                ) {
                    XyItemTitle(
                        text = stringResource(R.string.connection_management)
                    )
                }

            }
            item {
                SettingRoundedSurfaceColumn {
                    SettingItemComponent(title = R.string.connection_management) {
                        navController.navigate(RouterConstants.ConnectionManagement)
                    }
                }
            }

            item {
                XyRow(
                    paddingValues = PaddingValues(
                        start = XyTheme.dimens.outerHorizontalPadding,
                        end = XyTheme.dimens.outerHorizontalPadding,
                        top = XyTheme.dimens.outerVerticalPadding
                    ),
                    horizontalArrangement = Arrangement.Start
                ) {
                    XyItemTitle(
                        text = stringResource(R.string.download_management)
                    )
                }

            }
            item {
                SettingRoundedSurfaceColumn {
                    SettingItemComponent(
                        title = R.string.download_max_list,
                        info = settingsViewModel.settingDataNow.maxConcurrentDownloads.toString(),
                        imageVector = Icons.Rounded.KeyboardArrowDown
                    ) {
                        //进行最大下载数量设置
                    }

                    MusicSettingSwitchItemComponent(
                        title = stringResource(R.string.only_wifi),
                        ifChecked = settingsViewModel.settingDataNow.ifOnlyWifiDownload
                    ) { bol ->
                        coroutineScope.launch {

                        }
                    }

                    SettingItemComponent(
                        title = R.string.song_cache_location,
                        bottomInfo = settingsViewModel.downLoadManager.getConfig().finalDirectory,
                        maxLines = Int.MAX_VALUE
                    ) {
                        if (settingsViewModel.settingsConfig.cacheFilePath.isNotBlank()) {
                            val clipData =
                                ClipData.newPlainText(
                                    "label",
                                    settingsViewModel.settingsConfig.cacheFilePath
                                )
                            coroutineScope.launch {
                                clipboardManager.setClipEntry(ClipEntry(clipData))
                            }.invokeOnCompletion {
                                MessageUtils.sendPopTip(context.getString(R.string.copy_success))
                            }
                        }

                    }

                }
            }

            item {
                XyRow(
                    paddingValues = PaddingValues(
                        start = XyTheme.dimens.outerHorizontalPadding,
                        end = XyTheme.dimens.outerHorizontalPadding,
                        top = XyTheme.dimens.outerVerticalPadding
                    ),
                    horizontalArrangement = Arrangement.Start
                ) {
                    XyItemTitle(
                        text = stringResource(R.string.more)
                    )
                }

            }
            item {
                SettingRoundedSurfaceColumn {

                    SettingItemComponent(title = R.string.storage_management) {
                        navController.navigate(RouterConstants.MemoryManagement)
                    }

                    SettingItemComponent(title = R.string.interface_settings) {
                        navController.navigate(RouterConstants.InterfaceSetting)
                    }

                    SettingItemComponent(title = R.string.language) {
                        navController.navigate(RouterConstants.LanguageConfig)
                    }

                    SettingItemComponent(title = R.string.about) {
                        //版本信息,检查更新
                        navController.navigate(RouterConstants.About)
                    }

                }
            }
        }
    }

}

@Composable
fun SettingRoundedSurfaceColumn(content: @Composable ColumnScope.() -> Unit) {
    RoundedSurfaceColumnPadding(
        brush = Brush.horizontalGradient(
            colors = listOf(
                Color(0x4D503803),
                Color.Gray.copy(alpha = 0.1f)
            ), tileMode = TileMode.Repeated
        ),
        paddingValues = PaddingValues(
            horizontal = XyTheme.dimens.outerHorizontalPadding
        ),
        content = content
    )
}