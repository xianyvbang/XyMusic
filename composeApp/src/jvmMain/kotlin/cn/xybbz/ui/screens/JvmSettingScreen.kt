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


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cn.xybbz.common.utils.Log
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.common.utils.copyTextToClipboard
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
import cn.xybbz.ui.popup.MenuItemDefaultData
import cn.xybbz.ui.popup.XyDropdownMenu
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.LazyColumnNotComponent
import cn.xybbz.ui.xy.RoundedSurfaceColumn
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.about
import xymusic_kmp.composeapp.generated.resources.album_playback_history
import xymusic_kmp.composeapp.generated.resources.allow_simultaneous_playback
import xymusic_kmp.composeapp.generated.resources.broadcast_while_down
import xymusic_kmp.composeapp.generated.resources.cache_limit
import xymusic_kmp.composeapp.generated.resources.cache_location
import xymusic_kmp.composeapp.generated.resources.check_24px
import xymusic_kmp.composeapp.generated.resources.connection_management
import xymusic_kmp.composeapp.generated.resources.copy_success
import xymusic_kmp.composeapp.generated.resources.customize_lyric_settings
import xymusic_kmp.composeapp.generated.resources.download_max_list
import xymusic_kmp.composeapp.generated.resources.enabled_sync_play_progress
import xymusic_kmp.composeapp.generated.resources.interface_settings
import xymusic_kmp.composeapp.generated.resources.keyboard_arrow_down_24px
import xymusic_kmp.composeapp.generated.resources.language
import xymusic_kmp.composeapp.generated.resources.online_music_quality
import xymusic_kmp.composeapp.generated.resources.poxy_config
import xymusic_kmp.composeapp.generated.resources.settings
import xymusic_kmp.composeapp.generated.resources.song_cache_location
import xymusic_kmp.composeapp.generated.resources.storage_management

/**
 * 设置页面
 */
@OptIn(
    ExperimentalMaterial3Api::class,
)
@Composable
fun JvmSettingScreen(
    settingsViewModel: SettingsViewModel = koinViewModel<SettingsViewModel>()
) {

    val navigator = LocalNavigator.current
    val coroutineScope = rememberCoroutineScope()

    var ifShowMaxConcurrentDownloads by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        Log.i("=====", "MusicSettingScreen: ")
    }

    val copySuccess = stringResource(Res.string.copy_success)
    val cacheFilePath by settingsViewModel.settingsManager.cacheFilePath.collectAsState()
    val songStoragePath = settingsViewModel.songStoragePath

    XyColumnScreen {
        TopAppBarComponent(
            title = {
                TopAppBarTitle(
                    title = stringResource(Res.string.settings)
                )
            })

        LazyColumnNotComponent(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding),
            contentPadding = PaddingValues()
        ) {
            item {
                JvmSettingRoundedSurfaceColumn {
                    MusicSettingSwitchItemComponent(
                        title = stringResource(Res.string.broadcast_while_down),
                        ifChecked = settingsViewModel.settingDataNow.ifEnableEdgeDownload,
                        false,
                        { bol ->
                            coroutineScope.launch {
                                settingsViewModel.settingsManager.setIfEnableEdgeDownload(
                                    bol
                                )
                            }
                        }
                    )

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
                        bottomInfo = cacheFilePath,
                        maxLines = Int.MAX_VALUE,
                        painter = null
                    ) {
                        if (cacheFilePath.isNotBlank()) {
                            copyTextToClipboard(cacheFilePath)
                            MessageUtils.sendPopTip(copySuccess)
                        }

                    }

                }
            }
            item {
                JvmSettingRoundedSurfaceColumn {
                    SettingItemComponent(title = stringResource(Res.string.connection_management)) {
                        navigator.navigate(ConnectionManagement)
                    }
                }
            }

            item {
                JvmSettingRoundedSurfaceColumn {
                    SettingItemComponent(
                        title = stringResource(Res.string.download_max_list),
                        info = settingsViewModel.settingDataNow.maxConcurrentDownloads.toString(),
                        painter = Res.drawable.keyboard_arrow_down_24px,
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
                                                    painter = painterResource(Res.drawable.check_24px),
                                                    contentDescription = stringResource(
                                                        Res.string.download_max_list
                                                    ) + "1"
                                                )
                                        },
                                        onClick = {
                                            coroutineScope.launch {
                                                ifShowMaxConcurrentDownloads = false
                                                settingsViewModel.setMaxConcurrentDownloads(
                                                    1
                                                )
                                            }.invokeOnCompletion {

                                            }

                                        }),
                                    MenuItemDefaultData(
                                        title = "3", leadingIcon = {
                                            if (settingsViewModel.settingDataNow.maxConcurrentDownloads == 3)
                                                Icon(
                                                    painter = painterResource(Res.drawable.check_24px),
                                                    contentDescription = stringResource(
                                                        Res.string.download_max_list
                                                    ) + "3"
                                                )
                                        },
                                        onClick = {
                                            coroutineScope.launch {
                                                ifShowMaxConcurrentDownloads = false
                                                settingsViewModel.setMaxConcurrentDownloads(
                                                    3
                                                )
                                            }.invokeOnCompletion {

                                            }

                                        }),
                                    MenuItemDefaultData(
                                        title = "5", leadingIcon = {
                                            if (settingsViewModel.settingDataNow.maxConcurrentDownloads == 5)
                                                Icon(
                                                    painter = painterResource(Res.drawable.check_24px),
                                                    contentDescription = stringResource(
                                                        Res.string.download_max_list
                                                    ) + "5"
                                                )
                                        },
                                        onClick = {
                                            coroutineScope.launch {
                                                ifShowMaxConcurrentDownloads = false
                                                settingsViewModel.setMaxConcurrentDownloads(
                                                    5
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
                        bottomInfo = songStoragePath,
                        maxLines = Int.MAX_VALUE,
                        painter = null
                    ) {
                        if (songStoragePath.isNotBlank()) {
                            copyTextToClipboard(songStoragePath)
                            MessageUtils.sendPopTip(copySuccess)
                        }
                    }

                }
            }

            item {
                JvmSettingRoundedSurfaceColumn {

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
fun JvmSettingRoundedSurfaceColumn(content: @Composable ColumnScope.() -> Unit) {
    RoundedSurfaceColumn(
        contentPaddingValues = PaddingValues(
            horizontal = XyTheme.dimens.outerHorizontalPadding
        ),
        content = content
    )
}



