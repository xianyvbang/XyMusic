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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cn.xybbz.common.enums.TranscodeAudioBitRateType
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.common.utils.copyTextToClipboard
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.localdata.data.setting.XySettings
import cn.xybbz.music.cacheUpperLimitOptions
import cn.xybbz.router.About
import cn.xybbz.router.CacheLimit
import cn.xybbz.router.ConnectionManagement
import cn.xybbz.router.CustomApi
import cn.xybbz.router.InterfaceSetting
import cn.xybbz.router.LanguageConfig
import cn.xybbz.router.MemoryManagement
import cn.xybbz.router.ProxyConfig
import cn.xybbz.router.StreamingQuality
import cn.xybbz.ui.components.JvmSettingActionEntry
import cn.xybbz.ui.components.JvmSettingActionGrid as JvmSettingActionEntryGrid
import cn.xybbz.ui.components.JvmSettingDownloadRow
import cn.xybbz.ui.components.JvmSettingFlowRow
import cn.xybbz.ui.components.JvmSettingNavigationRow
import cn.xybbz.ui.components.JvmSettingOverviewTile
import cn.xybbz.ui.components.JvmSettingOverviewThreeColumnWidth
import cn.xybbz.ui.components.JvmSettingPageHeader
import cn.xybbz.ui.components.JvmSettingPageScaffold
import cn.xybbz.ui.components.JvmSettingPathRow
import cn.xybbz.ui.components.JvmSettingSection
import cn.xybbz.ui.components.JvmSettingStatusCard
import cn.xybbz.ui.components.JvmSettingStatusCardItem
import cn.xybbz.ui.components.JvmSettingSwitchRow
import cn.xybbz.ui.components.JvmSettingTwoPaneContent
import cn.xybbz.ui.components.displayAudioBitRateText
import cn.xybbz.ui.components.displayMessage
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyRow
import cn.xybbz.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.*
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.about
import xymusic_kmp.composeapp.generated.resources.album_24px
import xymusic_kmp.composeapp.generated.resources.album_playback_history
import xymusic_kmp.composeapp.generated.resources.allow_simultaneous_playback
import xymusic_kmp.composeapp.generated.resources.av_timer_24px
import xymusic_kmp.composeapp.generated.resources.broadcast_while_down
import xymusic_kmp.composeapp.generated.resources.cache_limit
import xymusic_kmp.composeapp.generated.resources.cache_location
import xymusic_kmp.composeapp.generated.resources.connection_management
import xymusic_kmp.composeapp.generated.resources.copy_success
import xymusic_kmp.composeapp.generated.resources.customize_lyric_settings
import xymusic_kmp.composeapp.generated.resources.download_24px
import xymusic_kmp.composeapp.generated.resources.enabled_sync_play_progress
import xymusic_kmp.composeapp.generated.resources.folder_managed_24px
import xymusic_kmp.composeapp.generated.resources.http_24px
import xymusic_kmp.composeapp.generated.resources.info_24px
import xymusic_kmp.composeapp.generated.resources.interface_settings
import xymusic_kmp.composeapp.generated.resources.language
import xymusic_kmp.composeapp.generated.resources.music_note_24px
import xymusic_kmp.composeapp.generated.resources.online_music_quality
import xymusic_kmp.composeapp.generated.resources.poxy_config
import xymusic_kmp.composeapp.generated.resources.queue_music_24px
import xymusic_kmp.composeapp.generated.resources.settings
import xymusic_kmp.composeapp.generated.resources.settings_24px
import xymusic_kmp.composeapp.generated.resources.signal_cellular_alt_24px
import xymusic_kmp.composeapp.generated.resources.song_cache_location
import xymusic_kmp.composeapp.generated.resources.storage_management
import xymusic_kmp.composeapp.generated.resources.volume_up_24px

/**
 * 设置页面
 */
@Composable
fun JvmSettingScreen(
    settingsViewModel: SettingsViewModel = koinViewModel<SettingsViewModel>()
) {
    val navigator = LocalNavigator.current
    val coroutineScope = rememberCoroutineScope()
    val settings = settingsViewModel.settingDataNow
    val cacheFilePath by settingsViewModel.settingsManager.cacheFilePath.collectAsState()
    val songStoragePath = settingsViewModel.songStoragePath
    val copySuccess = stringResource(Res.string.copy_success)
    val cacheLimitLabel = cacheUpperLimitOptions()
        .firstOrNull { it.limit == settings.cacheUpperLimit }
        ?.displayMessage()
        ?: settings.cacheUpperLimit.name
    val selectedQuality = TranscodeAudioBitRateType
        .getTranscodeAudioBitRate(settings.wifiNetworkAudioBitRate)
        .displayAudioBitRateText()
    val dataSourceLabel = settings.dataSourceType?.title ?: stringResource(Res.string.jvm_connection_config_info_screen_text_01)

    JvmSettingPageScaffold() {
        JvmSettingPageHeader(
            title = stringResource(Res.string.settings),
            description = stringResource(Res.string.jvm_setting_screen_text_01),
        ) {
            JvmSettingStatusCard(
                items = listOf(
                    JvmSettingStatusCardItem(label = stringResource(Res.string.jvm_setting_screen_text_02), value = dataSourceLabel),
                    JvmSettingStatusCardItem(label = stringResource(Res.string.jvm_setting_screen_text_03), value = selectedQuality),
                    JvmSettingStatusCardItem(label = stringResource(Res.string.jvm_setting_screen_text_04), value = settings.maxConcurrentDownloads.toString()),
                )
            )
        }

        JvmSettingOverview(
            settings = settings,
            cacheLimitLabel = cacheLimitLabel,
            onStorageClick = {
                navigator.navigate(MemoryManagement)
            },
        )

        JvmSettingTwoPaneContent(
            leftContent = {
                JvmSettingSection(
                    title = stringResource(Res.string.jvm_setting_screen_text_05),
                    subtitle = stringResource(Res.string.jvm_setting_screen_text_06),
                    badge = stringResource(Res.string.jvm_setting_screen_text_07),
                ) {
                    JvmSettingSwitchRow(
                        icon = Res.drawable.download_24px,
                        title = stringResource(Res.string.broadcast_while_down),
                        description = stringResource(Res.string.jvm_setting_screen_text_08),
                        checked = settings.ifEnableEdgeDownload,
                        onCheckedChange = { checked ->
                            coroutineScope.launch {
                                settingsViewModel.settingsManager.setIfEnableEdgeDownload(checked)
                            }
                        }
                    )

                    AnimatedVisibility(visible = settings.ifEnableEdgeDownload) {
                        JvmSettingNavigationRow(
                            icon = Res.drawable.folder_managed_24px,
                            title = stringResource(Res.string.cache_limit),
                            description = stringResource(Res.string.jvm_setting_screen_text_09),
                            value = cacheLimitLabel,
                            onClick = {
                                navigator.navigate(CacheLimit)
                            }
                        )
                    }

                    JvmSettingNavigationRow(
                        icon = Res.drawable.music_note_24px,
                        title = stringResource(Res.string.online_music_quality),
                        description = stringResource(Res.string.jvm_setting_screen_text_10),
                        value = "$selectedQuality · ${settings.transcodeFormat.uppercase()}",
                        onClick = {
                            navigator.navigate(StreamingQuality)
                        }
                    )

                    JvmSettingSwitchRow(
                        icon = Res.drawable.album_24px,
                        title = stringResource(Res.string.album_playback_history),
                        description = stringResource(Res.string.jvm_setting_screen_text_11),
                        checked = settings.ifEnableAlbumHistory,
                        onCheckedChange = { checked ->
                            coroutineScope.launch {
                                settingsViewModel.settingsManager.setIfEnableAlbumHistory(checked)
                            }
                        }
                    )

                    JvmSettingSwitchRow(
                        icon = Res.drawable.volume_up_24px,
                        title = stringResource(Res.string.allow_simultaneous_playback),
                        description = stringResource(Res.string.jvm_setting_screen_text_12),
                        checked = settings.ifHandleAudioFocus,
                        onCheckedChange = { checked ->
                            coroutineScope.launch {
                                settingsViewModel.settingsManager.setIfHandleAudioFocus(checked)
                            }
                        }
                    )

                    JvmSettingSwitchRow(
                        icon = Res.drawable.av_timer_24px,
                        title = stringResource(Res.string.enabled_sync_play_progress),
                        description = stringResource(Res.string.jvm_setting_screen_text_13),
                        checked = settings.ifEnableSyncPlayProgress,
                        onCheckedChange = { checked ->
                            coroutineScope.launch {
                                settingsViewModel.setSyncPlayProgressEnabled(checked)
                            }
                        }
                    )

                    JvmSettingPathRow(
                        icon = Res.drawable.folder_managed_24px,
                        title = stringResource(Res.string.cache_location),
                        path = cacheFilePath,
                        onClick = {
                            if (cacheFilePath.isNotBlank()) {
                                copyTextToClipboard(cacheFilePath)
                                MessageUtils.sendPopTip(copySuccess)
                            }
                        }
                    )
                }

                JvmSettingSection(
                    title = stringResource(Res.string.jvm_setting_screen_text_14),
                    subtitle = stringResource(Res.string.jvm_setting_screen_text_15),
                    badge = stringResource(Res.string.jvm_setting_screen_text_16),
                ) {
                    JvmSettingDownloadRow(
                        selected = settings.maxConcurrentDownloads,
                        onSelected = { maxConcurrentDownloads ->
                            coroutineScope.launch {
                                settingsViewModel.setMaxConcurrentDownloads(maxConcurrentDownloads)
                            }
                        }
                    )

                    JvmSettingPathRow(
                        icon = Res.drawable.queue_music_24px,
                        title = stringResource(Res.string.song_cache_location),
                        path = songStoragePath,
                        onClick = {
                            if (songStoragePath.isNotBlank()) {
                                copyTextToClipboard(songStoragePath)
                                MessageUtils.sendPopTip(copySuccess)
                            }
                        }
                    )

                    JvmSettingNavigationRow(
                        icon = Res.drawable.folder_managed_24px,
                        title = stringResource(Res.string.storage_management),
                        description = stringResource(Res.string.jvm_setting_screen_text_17),
                        value = stringResource(Res.string.jvm_setting_screen_text_18),
                        onClick = {
                            navigator.navigate(MemoryManagement)
                        }
                    )
                }
            },
            rightContent = {
                JvmSettingSection(
                    title = stringResource(Res.string.connect),
                    subtitle = stringResource(Res.string.jvm_setting_screen_text_19),
                    badge = stringResource(Res.string.jvm_connection_config_info_screen_text_10),
                ) {
                    JvmSettingNavigationRow(
                        icon = Res.drawable.http_24px,
                        title = stringResource(Res.string.connection_management),
                        description = stringResource(Res.string.jvm_setting_screen_text_20),
                        value = dataSourceLabel,
                        onClick = {
                            navigator.navigate(ConnectionManagement)
                        }
                    )

                    JvmSettingNavigationRow(
                        icon = Res.drawable.signal_cellular_alt_24px,
                        title = stringResource(Res.string.poxy_config),
                        description = stringResource(Res.string.jvm_setting_screen_text_21),
                        value = stringResource(Res.string.jvm_setting_screen_text_22),
                        onClick = {
                            navigator.navigate(ProxyConfig)
                        }
                    )
                }

                JvmSettingSection(
                    title = stringResource(Res.string.jvm_setting_screen_text_23),
                    subtitle = stringResource(Res.string.jvm_setting_screen_text_24),
                    badge = stringResource(Res.string.jvm_interface_setting_screen_text_17),
                    contentContainerColor = Color.Transparent,
                    contentContainerBorderColor = Color.Transparent,
                    qualityNote = stringResource(Res.string.jvm_setting_screen_text_25),
                ) {
                    JvmSettingActionGrid(
                        onInterfaceClick = {
                            navigator.navigate(InterfaceSetting)
                        },
                        onLanguageClick = {
                            navigator.navigate(LanguageConfig)
                        },
                        onCustomApiClick = {
                            navigator.navigate(CustomApi)
                        },
                        onAboutClick = {
                            navigator.navigate(About)
                        },
                    )
                }
            }
        )
    }
}

/**
 * 设置页通用入口卡片网格。
 *
 * @param onInterfaceClick 界面设置入口点击事件。
 * @param onLanguageClick 语言设置入口点击事件。
 * @param onCustomApiClick 自定义资源入口点击事件。
 * @param onAboutClick 关于页面入口点击事件。
 */
@Composable
private fun JvmSettingActionGrid(
    onInterfaceClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onCustomApiClick: () -> Unit,
    onAboutClick: () -> Unit,
) {
    // 设置页只关心四个固定入口，通用网格负责真正的宽度计算和卡片渲染。
    JvmSettingActionEntryGrid(
        actionEntries = jvmSettingActionEntries(
            onInterfaceClick = onInterfaceClick,
            onLanguageClick = onLanguageClick,
            onCustomApiClick = onCustomApiClick,
            onAboutClick = onAboutClick,
        )
    )
}

/**
 * 组装设置页右栏“通用”分组中的四个入口卡片。
 */
@Composable
private fun jvmSettingActionEntries(
    onInterfaceClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onCustomApiClick: () -> Unit,
    onAboutClick: () -> Unit,
): List<JvmSettingActionEntry> {
    return listOf(
        JvmSettingActionEntry(
            icon = Res.drawable.settings_24px,
            kicker = stringResource(Res.string.jvm_setting_screen_text_26),
            title = stringResource(Res.string.interface_settings),
            description = stringResource(Res.string.jvm_setting_screen_text_27),
            onClick = onInterfaceClick,
        ),
        JvmSettingActionEntry(
            icon = Res.drawable.info_24px,
            kicker = stringResource(Res.string.jvm_language_config_screen_text_02),
            title = stringResource(Res.string.language),
            description = stringResource(Res.string.jvm_setting_screen_text_28),
            onClick = onLanguageClick,
        ),
        JvmSettingActionEntry(
            icon = Res.drawable.music_note_24px,
            kicker = stringResource(Res.string.jvm_setting_screen_text_29),
            title = stringResource(Res.string.customize_lyric_settings),
            description = stringResource(Res.string.jvm_setting_screen_text_30),
            onClick = onCustomApiClick,
        ),
        JvmSettingActionEntry(
            icon = Res.drawable.info_24px,
            kicker = stringResource(Res.string.jvm_setting_screen_text_31),
            title = stringResource(Res.string.about),
            description = stringResource(Res.string.jvm_setting_screen_text_32),
            onClick = onAboutClick,
        ),
    )
}

/**
 * 设置概览卡片区。
 *
 * 宽屏时保持三张卡片一行，窄屏时每张卡片独占一行。
 */
@Composable
private fun JvmSettingOverview(
    settings: XySettings,
    cacheLimitLabel: String,
    onStorageClick: () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val gap = XyTheme.dimens.contentPadding


        XyRow(
            paddingValues = PaddingValues(),
            // 卡片不再铺满整行时居中摆放，视觉上更接近预览稿。
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            JvmSettingOverviewTile(
                modifier = Modifier.fillMaxWidth().weight(1f),
                icon = Res.drawable.download_24px,
                kicker = stringResource(Res.string.jvm_setting_screen_text_33),
                value = if (settings.ifEnableEdgeDownload) stringResource(Res.string.jvm_setting_screen_text_34) else stringResource(Res.string.jvm_setting_screen_text_35),
                sub = stringResource(Res.string.jvm_setting_screen_text_36, cacheLimitLabel)
            )
            Spacer(modifier = Modifier.width(gap))
            JvmSettingOverviewTile(
                modifier = Modifier.fillMaxWidth().weight(1f),
                icon = Res.drawable.av_timer_24px,
                kicker = stringResource(Res.string.jvm_setting_screen_text_37),
                value = if (settings.ifEnableSyncPlayProgress) stringResource(Res.string.jvm_setting_screen_text_38) else stringResource(Res.string.jvm_setting_screen_text_39),
                sub = if (settings.ifEnableAlbumHistory) stringResource(Res.string.jvm_setting_screen_text_40) else stringResource(Res.string.jvm_setting_screen_text_41)
            )
            Spacer(modifier = Modifier.width(gap))
            JvmSettingOverviewTile(
                modifier = Modifier.fillMaxWidth().weight(1f),
                icon = Res.drawable.folder_managed_24px,
                kicker = stringResource(Res.string.storage_management),
                value = stringResource(Res.string.jvm_setting_screen_text_18),
                sub = stringResource(Res.string.jvm_setting_screen_text_42),
                onClick = onStorageClick,
            )
        }
    }
}
