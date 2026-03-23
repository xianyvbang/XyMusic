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


import android.content.ClipData
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.router.About
import cn.xybbz.router.CacheLimit
import cn.xybbz.router.ConnectionManagement
import cn.xybbz.router.CustomApi
import cn.xybbz.router.InterfaceSetting
import cn.xybbz.router.LanguageConfig
import cn.xybbz.router.MemoryManagement
import cn.xybbz.router.ProxyConfig
import cn.xybbz.router.StreamingQuality
import cn.xybbz.ui.components.MusicSettingSwitchItemComponent
import cn.xybbz.ui.components.SettingItemComponent
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.TopAppBarTitle
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.popup.MenuItemDefaultData
import cn.xybbz.ui.popup.XyDropdownMenu
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.LazyColumnNotComponent
import cn.xybbz.ui.xy.RoundedSurfaceColumn
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.viewmodel.SettingsViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.launch
import cn.xybbz.ui.xy.XyIconButton as IconButton

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

    val navigator = LocalNavigator.current
    val coroutineScope = rememberCoroutineScope()

    var ifShowMaxConcurrentDownloads by remember {
        mutableStateOf(false)
    }

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        Log.i("=====", "MusicSettingScreen: ")
    }

    val clipboardManager = LocalClipboard.current

    val copySuccess = stringResource(Res.string.copy_success)

    XyColumnScreen(
        modifier = Modifier.brashColor(
            topVerticalColor = settingsViewModel.backgroundConfig.settingsBrash[0],
            bottomVerticalColor = settingsViewModel.backgroundConfig.settingsBrash[1]
        )
    ) {
        TopAppBarComponent(
            modifier = Modifier.statusBarsPadding(),
            title = {
                TopAppBarTitle(
                    title = stringResource(Res.string.settings)
                )
            }, navigationIcon = {
                IconButton(
                    onClick = {
                        navigator.goBack()
                    },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(Res.string.return_home)
                    )
                }
            })

        LazyColumnNotComponent(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding),
            contentPadding = PaddingValues()
        ) {
            item {
                SettingRoundedSurfaceColumn {
                    MusicSettingSwitchItemComponent(
                        title = stringResource(Res.string.broadcast_while_down),
                        ifChecked = settingsViewModel.settingDataNow.ifEnableEdgeDownload
                    ) { bol ->
                        coroutineScope.launch {
                            settingsViewModel.settingsManager.setIfEnableEdgeDownload(
                                bol
                            )
                        }
                    }

                    AnimatedVisibility(visible = settingsViewModel.settingDataNow.ifEnableEdgeDownload) {
                        SettingItemComponent(title = stringResource(Res.string.cache_limit)) {
                            navigator.navigate(CacheLimit)
                        }
                    }

                    SettingItemComponent(title = stringResource(Res.string.online_music_quality)) {
                        navigator.navigate(StreamingQuality)
                    }

                    MusicSettingSwitchItemComponent(
                        title = stringResource(Res.string.album_playback_history),
                        ifChecked = settingsViewModel.settingDataNow.ifEnableAlbumHistory
                    ) { bol ->
                        coroutineScope.launch {
                            settingsViewModel.settingsManager.setIfEnableAlbumHistory(
                                bol
                            )
                        }
                    }

                    MusicSettingSwitchItemComponent(
                        title = stringResource(Res.string.allow_simultaneous_playback),
                        ifChecked = settingsViewModel.settingDataNow.ifHandleAudioFocus
                    ) { bol ->
                        coroutineScope.launch {
                            settingsViewModel.settingsManager.setIfHandleAudioFocus(
                                bol
                            )
                        }
                    }

                    MusicSettingSwitchItemComponent(
                        title = stringResource(Res.string.enabled_sync_play_progress),
                        ifChecked = settingsViewModel.settingDataNow.ifEnableSyncPlayProgress
                    ) { bol ->
                        coroutineScope.launch {
                            settingsViewModel.setSyncPlayProgressEnabled(
                                bol
                            )
                        }
                    }

                    SettingItemComponent(
                        title = stringResource(Res.string.cache_location),
                        bottomInfo = settingsViewModel.settingsManager.cacheFilePath,
                        maxLines = Int.MAX_VALUE,
                        imageVector = null
                    ) {
                        if (settingsViewModel.settingsManager.cacheFilePath.isNotBlank()) {
                            val clipData =
                                ClipData.newPlainText(
                                    "label",
                                    settingsViewModel.settingsManager.cacheFilePath
                                )
                            coroutineScope.launch {
                                clipboardManager.setClipEntry(ClipEntry(clipData))
                            }.invokeOnCompletion {
                                MessageUtils.sendPopTip(copySuccess)
                            }
                        }

                    }

                }
            }
            item {
                SettingRoundedSurfaceColumn {
                    SettingItemComponent(title = stringResource(Res.string.connection_management)) {
                        navigator.navigate(ConnectionManagement)
                    }
                }
            }

            item {
                SettingRoundedSurfaceColumn {
                    SettingItemComponent(
                        title = stringResource(Res.string.download_max_list),
                        info = settingsViewModel.settingDataNow.maxConcurrentDownloads.toString(),
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        trailingContent = {
                            XyDropdownMenu(
                                onIfShowMenu = { ifShowMaxConcurrentDownloads },
                                onSetIfShowMenu = { ifShowMaxConcurrentDownloads = it },
                                modifier = Modifier
                                    .width(200.dp),
                                itemDataList = listOf(
                                    MenuItemDefaultData(
                                        title = "1", leadingIcon = {
                                            if (settingsViewModel.settingDataNow.maxConcurrentDownloads == 1)
                                                Icon(
                                                    Icons.Rounded.Check,
                                                    contentDescription = stringResource(
                                                        Res.string.download_max_list
                                                    ) + "1"
                                                )
                                        },
                                        onClick = {
                                            coroutineScope.launch {
                                                ifShowMaxConcurrentDownloads = false
                                                settingsViewModel.setMaxConcurrentDownloads(
                                                    1,
                                                    context
                                                )
                                            }.invokeOnCompletion {

                                            }

                                        }),
                                    MenuItemDefaultData(
                                        title = "3", leadingIcon = {
                                            if (settingsViewModel.settingDataNow.maxConcurrentDownloads == 3)
                                                Icon(
                                                    Icons.Rounded.Check,
                                                    contentDescription = stringResource(
                                                        Res.string.download_max_list
                                                    ) + "3"
                                                )
                                        },
                                        onClick = {
                                            coroutineScope.launch {
                                                ifShowMaxConcurrentDownloads = false
                                                settingsViewModel.setMaxConcurrentDownloads(
                                                    3,
                                                    context
                                                )
                                            }.invokeOnCompletion {

                                            }

                                        }),
                                    MenuItemDefaultData(
                                        title = "5", leadingIcon = {
                                            if (settingsViewModel.settingDataNow.maxConcurrentDownloads == 5)
                                                Icon(
                                                    Icons.Rounded.Check,
                                                    contentDescription = stringResource(
                                                        Res.string.download_max_list
                                                    ) + "5"
                                                )
                                        },
                                        onClick = {
                                            coroutineScope.launch {
                                                ifShowMaxConcurrentDownloads = false
                                                settingsViewModel.setMaxConcurrentDownloads(
                                                    5,
                                                    context
                                                )
                                            }.invokeOnCompletion {

                                            }

                                        })
                                )
                            )
                        }
                    ) {
                        ifShowMaxConcurrentDownloads = true
                    }

                    SettingItemComponent(
                        title = stringResource(Res.string.song_cache_location),
                        bottomInfo = settingsViewModel.downLoadManager.getConfig().finalDirectory,
                        maxLines = Int.MAX_VALUE,
                        imageVector = null
                    ) {
                        if (settingsViewModel.settingsManager.cacheFilePath.isNotBlank()) {
                            val clipData =
                                ClipData.newPlainText(
                                    "label",
                                    settingsViewModel.downLoadManager.getConfig().finalDirectory
                                )
                            coroutineScope.launch {
                                clipboardManager.setClipEntry(ClipEntry(clipData))
                            }.invokeOnCompletion {
                                MessageUtils.sendPopTip(copySuccess)
                            }
                        }

                    }

                }
            }

            item {
                SettingRoundedSurfaceColumn {

                    SettingItemComponent(title = stringResource(Res.string.storage_management)) {
                        navigator.navigate(MemoryManagement)
                    }

                    SettingItemComponent(
                        title = stringResource(Res.string.customize_lyric_settings)
                    ) {
                        navigator.navigate(CustomApi)
                    }

                    SettingItemComponent(title = stringResource(Res.string.poxy_config)) {
                        navigator.navigate(ProxyConfig)
                    }

                    SettingItemComponent(title = stringResource(Res.string.interface_settings)) {
                        navigator.navigate(InterfaceSetting)
                    }

                    SettingItemComponent(title = stringResource(Res.string.language)) {
                        navigator.navigate(LanguageConfig)
                    }

                    SettingItemComponent(title = stringResource(Res.string.about)) {
                        //版本信息,检查更新
                        navigator.navigate(About)
                    }

                }
            }
        }
    }

}

@Composable
fun SettingRoundedSurfaceColumn(content: @Composable ColumnScope.() -> Unit) {
    RoundedSurfaceColumn(
        contentPaddingValues = PaddingValues(
            horizontal = XyTheme.dimens.outerHorizontalPadding
        ),
        content = content
    )
}

