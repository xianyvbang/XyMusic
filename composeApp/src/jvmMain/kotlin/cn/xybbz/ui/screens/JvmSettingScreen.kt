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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cn.xybbz.common.enums.TranscodeAudioBitRateType
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.common.utils.copyTextToClipboard
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.localdata.enums.LanguageType
import cn.xybbz.localdata.enums.ThemeTypeEnum
import cn.xybbz.music.cacheUpperLimitOptions
import cn.xybbz.router.About
import cn.xybbz.router.CustomApi
import cn.xybbz.router.InterfaceSetting
import cn.xybbz.router.MemoryManagement
import cn.xybbz.router.SetBackgroundImage
import cn.xybbz.router.StreamingQuality
import cn.xybbz.ui.components.JvmSettingBaseRow
import cn.xybbz.ui.components.JvmSettingFixedHeader
import cn.xybbz.ui.components.JvmSettingGroup
import cn.xybbz.ui.components.JvmSettingGroupFlow
import cn.xybbz.ui.components.JvmLazyListComponent
import cn.xybbz.ui.components.JvmSettingNavigationRow
import cn.xybbz.ui.components.JvmSettingNote
import cn.xybbz.ui.components.JvmSettingPathRow
import cn.xybbz.ui.components.JvmSettingPickRow
import cn.xybbz.ui.components.JvmSettingSectionHeader
import cn.xybbz.ui.components.JvmSettingSegmentedControl
import cn.xybbz.ui.components.JvmSettingSwitchRow
import cn.xybbz.ui.components.displayAudioBitRateText
import cn.xybbz.ui.components.displayMessage
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.LazyColumnComponent
import cn.xybbz.ui.xy.LazyColumnNotComponent
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic.composeapp.generated.resources.Res
import xymusic.composeapp.generated.resources.about
import xymusic.composeapp.generated.resources.album_24px
import xymusic.composeapp.generated.resources.album_playback_history
import xymusic.composeapp.generated.resources.allow_simultaneous_playback
import xymusic.composeapp.generated.resources.av_timer_24px
import xymusic.composeapp.generated.resources.background_image_setting
import xymusic.composeapp.generated.resources.broadcast_while_down
import xymusic.composeapp.generated.resources.cache_location
import xymusic.composeapp.generated.resources.copy_success
import xymusic.composeapp.generated.resources.customize_lyric_settings
import xymusic.composeapp.generated.resources.dark
import xymusic.composeapp.generated.resources.download_24px
import xymusic.composeapp.generated.resources.download_max_list
import xymusic.composeapp.generated.resources.enabled_sync_play_progress
import xymusic.composeapp.generated.resources.folder_managed_24px
import xymusic.composeapp.generated.resources.http_24px
import xymusic.composeapp.generated.resources.info_24px
import xymusic.composeapp.generated.resources.interface_settings
import xymusic.composeapp.generated.resources.jvm_setting_screen_text_08
import xymusic.composeapp.generated.resources.jvm_setting_screen_text_11
import xymusic.composeapp.generated.resources.jvm_setting_screen_text_12
import xymusic.composeapp.generated.resources.jvm_setting_screen_text_13
import xymusic.composeapp.generated.resources.jvm_setting_screen_text_17
import xymusic.composeapp.generated.resources.jvm_setting_screen_text_30
import xymusic.composeapp.generated.resources.jvm_setting_screen_text_32
import xymusic.composeapp.generated.resources.jvm_streaming_quality_screen_text_15
import xymusic.composeapp.generated.resources.jvm_streaming_quality_screen_text_16
import xymusic.composeapp.generated.resources.jvm_streaming_quality_screen_text_17
import xymusic.composeapp.generated.resources.jvm_streaming_quality_screen_text_18
import xymusic.composeapp.generated.resources.jvm_streaming_quality_screen_text_19
import xymusic.composeapp.generated.resources.jvm_streaming_quality_screen_text_23
import xymusic.composeapp.generated.resources.light
import xymusic.composeapp.generated.resources.music_note_24px
import xymusic.composeapp.generated.resources.online_music_quality
import xymusic.composeapp.generated.resources.prioritize_music_service_api
import xymusic.composeapp.generated.resources.queue_music_24px
import xymusic.composeapp.generated.resources.settings
import xymusic.composeapp.generated.resources.settings_24px
import xymusic.composeapp.generated.resources.settings_data_source_none
import xymusic.composeapp.generated.resources.settings_download_desc
import xymusic.composeapp.generated.resources.settings_group_about
import xymusic.composeapp.generated.resources.settings_group_api
import xymusic.composeapp.generated.resources.settings_group_cache_dir
import xymusic.composeapp.generated.resources.settings_group_cache_limit
import xymusic.composeapp.generated.resources.settings_group_download
import xymusic.composeapp.generated.resources.settings_group_interface
import xymusic.composeapp.generated.resources.settings_group_language
import xymusic.composeapp.generated.resources.settings_group_play
import xymusic.composeapp.generated.resources.settings_group_quality
import xymusic.composeapp.generated.resources.settings_group_storage
import xymusic.composeapp.generated.resources.settings_group_theme
import xymusic.composeapp.generated.resources.settings_group_transcode
import xymusic.composeapp.generated.resources.settings_interface_desc
import xymusic.composeapp.generated.resources.settings_language_disabled
import xymusic.composeapp.generated.resources.settings_language_system_desc
import xymusic.composeapp.generated.resources.settings_meta_data_source
import xymusic.composeapp.generated.resources.settings_meta_quality
import xymusic.composeapp.generated.resources.settings_note_about
import xymusic.composeapp.generated.resources.settings_note_cache_dir
import xymusic.composeapp.generated.resources.settings_prioritize_api_desc
import xymusic.composeapp.generated.resources.settings_section_about_desc
import xymusic.composeapp.generated.resources.settings_section_cache_desc
import xymusic.composeapp.generated.resources.settings_section_custom_desc
import xymusic.composeapp.generated.resources.settings_section_general_desc
import xymusic.composeapp.generated.resources.settings_section_interface_desc
import xymusic.composeapp.generated.resources.settings_section_language_desc
import xymusic.composeapp.generated.resources.settings_section_playback_desc
import xymusic.composeapp.generated.resources.settings_tab_about
import xymusic.composeapp.generated.resources.settings_tab_cache
import xymusic.composeapp.generated.resources.settings_tab_custom
import xymusic.composeapp.generated.resources.settings_tab_general
import xymusic.composeapp.generated.resources.settings_tab_interface
import xymusic.composeapp.generated.resources.settings_tab_language
import xymusic.composeapp.generated.resources.settings_tab_playback
import xymusic.composeapp.generated.resources.settings_theme_desc
import xymusic.composeapp.generated.resources.settings_value_edit
import xymusic.composeapp.generated.resources.settings_value_manage
import xymusic.composeapp.generated.resources.settings_value_set
import xymusic.composeapp.generated.resources.settings_value_unset
import xymusic.composeapp.generated.resources.settings_value_view
import xymusic.composeapp.generated.resources.song_cache_location
import xymusic.composeapp.generated.resources.storage_management
import xymusic.composeapp.generated.resources.system
import xymusic.composeapp.generated.resources.theme_mode
import xymusic.composeapp.generated.resources.transcode_audio_bit_rate_type_text_01
import xymusic.composeapp.generated.resources.transcoding_format
import xymusic.composeapp.generated.resources.volume_up_24px

/** 设置页滚动内容左右内边距，与固定头部保持一致。 */
private val JvmSettingContentHorizontalPadding = 40.dp

/** 设置页相邻分区之间的纵向间距。 */
private val JvmSettingSectionGap = 40.dp

/** 设置页滚动内容顶部内边距。 */
private val JvmSettingContentTopPadding = 24.dp

/** 设置页滚动内容底部内边距，避免最后一行贴着播放栏。 */
private val JvmSettingContentBottomPadding = 56.dp

/** 设置页顶部标签索引总数，与分区数量一一对应。 */
private const val JvmSettingTabCount = 7

/**
 * 设置页面。
 *
 * 页面由一个固定不动的标题与标签栏，以及下方独立滚动的连续分区列表组成；
 * 标签会随滚动位置联动高亮，点击标签会平滑定位到对应分区。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JvmSettingScreen(
    settingsViewModel: SettingsViewModel = koinViewModel<SettingsViewModel>()
) {
    val navigator = LocalNavigator.current
    val coroutineScope = rememberCoroutineScope()
    val settings = settingsViewModel.settingDataNow
    val cacheFilePath by settingsViewModel.settingsManager.cacheFilePath.collectAsState()
    val themeType by settingsViewModel.settingsManager.themeType.collectAsState()
    val imageFilePath by settingsViewModel.settingsManager.imageFilePath.collectAsState()
    val songStoragePath = settingsViewModel.songStoragePath
    val copySuccess = stringResource(Res.string.copy_success)
    val dataSourceLabel = settings.dataSourceType?.title
        ?: stringResource(Res.string.settings_data_source_none)
    val selectedQuality = TranscodeAudioBitRateType
        .getTranscodeAudioBitRate(settings.wifiNetworkAudioBitRate)
    val selectedQualityLabel = selectedQuality.displayAudioBitRateText()

    // 顶部标签文案，顺序与下方分区顺序严格一致。
    val tabs = listOf(
        stringResource(Res.string.settings_tab_general),
        stringResource(Res.string.settings_tab_playback),
        stringResource(Res.string.settings_tab_cache),
        stringResource(Res.string.settings_tab_interface),
        stringResource(Res.string.settings_tab_language),
        stringResource(Res.string.settings_tab_custom),
        stringResource(Res.string.settings_tab_about),
    )

    // 当前高亮的标签下标，由滚动位置和点击行为共同驱动。
    var selectedTab by remember { mutableIntStateOf(0) }
    val listState = rememberLazyListState()

    // 监听滚动位置，把第一个可见分区同步为标签高亮；滚到底部时强制高亮最后一个标签。
    LaunchedEffect(listState) {
        snapshotFlow {
            listState.firstVisibleItemIndex to listState.canScrollForward
        }.collect { (firstVisibleIndex, canScrollForward) ->
            selectedTab = if (canScrollForward) {
                firstVisibleIndex.coerceIn(0, JvmSettingTabCount - 1)
            } else {
                JvmSettingTabCount - 1
            }
        }
    }

    // 复制路径的通用行为：路径为空时不做任何处理。
    val copyPath: (String) -> Unit = { path ->
        if (path.isNotBlank()) {
            copyTextToClipboard(path)
            MessageUtils.sendPopTip(copySuccess)
        }
    }

    XyColumnScreen {
        Column(modifier = Modifier.fillMaxSize()) {
            JvmSettingFixedHeader(
                title = stringResource(Res.string.settings),
                meta = "${stringResource(Res.string.settings_meta_data_source)} $dataSourceLabel · " +
                        "${stringResource(Res.string.settings_meta_quality)} $selectedQualityLabel",
                tabs = tabs,
                selectedIndex = selectedTab,
                onTabSelected = { index ->
                    selectedTab = index
                    coroutineScope.launch {
                        listState.animateScrollToItem(index)
                    }
                }
            )

            // 复用 JVM 通用懒加载列表组件，保持滚动容器与其它桌面页面一致。
            LazyColumnNotComponent(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                state = listState,
                contentPadding = PaddingValues(
                    start = JvmSettingContentHorizontalPadding,
                    end = JvmSettingContentHorizontalPadding,
                    top = JvmSettingContentTopPadding,
                ),
                verticalArrangement = Arrangement.spacedBy(JvmSettingSectionGap),
            ) {
                item(key = "general") {
                    JvmSettingSectionPage(
                        index = "01",
                        title = tabs[0],
                        subtitle = stringResource(Res.string.settings_section_general_desc),
                        badge = "GENERAL"
                    ) {
                    JvmSettingGroupFlow {
                        JvmSettingGroup(
                            title = stringResource(Res.string.settings_group_play),
                            modifier = Modifier.weight(1f)
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
                        }

                        JvmSettingGroup(
                            title = stringResource(Res.string.settings_group_download),
                            modifier = Modifier.weight(1f)
                        ) {
                            JvmSettingBaseRow(
                                icon = Res.drawable.download_24px,
                                title = stringResource(Res.string.download_max_list),
                                description = stringResource(Res.string.settings_download_desc),
                                trailing = {
                                    JvmSettingSegmentedControl(
                                        options = JvmDownloadConcurrencyOptions,
                                        selectedIndex = JvmDownloadConcurrencyOptions
                                            .indexOf(settings.maxConcurrentDownloads.toString())
                                            .coerceAtLeast(0),
                                        onSelected = { index ->
                                            val concurrentDownloads =
                                                JvmDownloadConcurrencyOptions[index].toIntOrNull() ?: 3
                                            coroutineScope.launch {
                                                settingsViewModel.setMaxConcurrentDownloads(concurrentDownloads)
                                            }
                                        }
                                    )
                                }
                            )
                            JvmSettingPathRow(
                                icon = Res.drawable.queue_music_24px,
                                title = stringResource(Res.string.song_cache_location),
                                path = songStoragePath,
                                onClick = { copyPath(songStoragePath) }
                            )
                        }
                    }
                    }
                }

                item(key = "playback") {
                    JvmSettingSectionPage(
                        index = "02",
                        title = tabs[1],
                        subtitle = stringResource(Res.string.settings_section_playback_desc),
                        badge = "AUDIO"
                    ) {
                    JvmSettingGroupFlow {
                        JvmSettingGroup(
                            title = stringResource(Res.string.settings_group_quality),
                            modifier = Modifier.weight(1f)
                        ) {
                            TranscodeAudioBitRateType.entries.forEach { quality ->
                                JvmSettingPickRow(
                                    title = quality.displayAudioBitRateText(),
                                    description = quality.qualityDescription(),
                                    tag = quality.audioBitRateStr.ifBlank {
                                        stringResource(Res.string.transcode_audio_bit_rate_type_text_01)
                                    },
                                    selected = selectedQuality == quality,
                                    onClick = {
                                        if (selectedQuality != quality) {
                                            coroutineScope.launch {
                                                settingsViewModel.settingsManager
                                                    .setWifiNetworkAudioBitRate(quality.audioBitRate)
                                                settingsViewModel.settingsManager
                                                    .setMobileNetworkAudioBitRate(quality.audioBitRate)
                                            }
                                        }
                                    }
                                )
                            }
                        }

                        JvmSettingGroup(
                            title = stringResource(Res.string.settings_group_transcode),
                            modifier = Modifier.weight(1f)
                        ) {
                            JvmSettingNavigationRow(
                                icon = Res.drawable.music_note_24px,
                                title = stringResource(Res.string.online_music_quality),
                                description = stringResource(Res.string.jvm_streaming_quality_screen_text_15),
                                value = selectedQualityLabel,
                                onClick = { navigator.navigate(StreamingQuality) }
                            )
                            JvmSettingNavigationRow(
                                icon = Res.drawable.settings_24px,
                                title = stringResource(Res.string.transcoding_format),
                                description = stringResource(Res.string.jvm_streaming_quality_screen_text_23),
                                value = settings.transcodeFormat.uppercase(),
                                onClick = { navigator.navigate(StreamingQuality) }
                            )
                        }
                    }
                    }
                }

                item(key = "cache") {
                    JvmSettingSectionPage(
                        index = "03",
                        title = tabs[2],
                        subtitle = stringResource(Res.string.settings_section_cache_desc),
                        badge = "STORAGE"
                    ) {
                    JvmSettingGroupFlow {
                        JvmSettingGroup(
                            title = stringResource(Res.string.settings_group_cache_limit),
                            modifier = Modifier.weight(1f)
                        ) {
                            cacheUpperLimitOptions().forEach { option ->
                                JvmSettingPickRow(
                                    title = option.displayMessage(),
                                    description = "",
                                    selected = settings.cacheUpperLimit == option.limit,
                                    onClick = {
                                        if (settings.cacheUpperLimit != option.limit) {
                                            coroutineScope.launch {
                                                settingsViewModel.settingsManager.setCacheUpperLimit(option.limit)
                                            }
                                        }
                                    }
                                )
                            }
                        }

                        JvmSettingGroup(
                            title = stringResource(Res.string.settings_group_cache_dir),
                            modifier = Modifier.weight(1f)
                        ) {
                            JvmSettingPathRow(
                                icon = Res.drawable.folder_managed_24px,
                                title = stringResource(Res.string.cache_location),
                                path = cacheFilePath,
                                onClick = { copyPath(cacheFilePath) }
                            )
                            JvmSettingPathRow(
                                icon = Res.drawable.queue_music_24px,
                                title = stringResource(Res.string.song_cache_location),
                                path = songStoragePath,
                                onClick = { copyPath(songStoragePath) }
                            )
                            JvmSettingNote(text = stringResource(Res.string.settings_note_cache_dir))
                        }

                        JvmSettingGroup(
                            title = stringResource(Res.string.settings_group_storage),
                            modifier = Modifier.weight(1f)
                        ) {
                            JvmSettingNavigationRow(
                                icon = Res.drawable.folder_managed_24px,
                                title = stringResource(Res.string.storage_management),
                                description = stringResource(Res.string.jvm_setting_screen_text_17),
                                value = stringResource(Res.string.settings_value_manage),
                                onClick = { navigator.navigate(MemoryManagement) }
                            )
                        }
                    }
                    }
                }

                item(key = "interface") {
                    JvmSettingSectionPage(
                        index = "04",
                        title = tabs[3],
                        subtitle = stringResource(Res.string.settings_section_interface_desc),
                        badge = "UI"
                    ) {
                    JvmSettingGroupFlow {
                        JvmSettingGroup(
                            title = stringResource(Res.string.settings_group_theme),
                            modifier = Modifier.weight(1f)
                        ) {
                            JvmSettingBaseRow(
                                icon = Res.drawable.settings_24px,
                                title = stringResource(Res.string.theme_mode),
                                description = stringResource(Res.string.settings_theme_desc),
                                trailing = {
                                    JvmSettingSegmentedControl(
                                        options = listOf(
                                            stringResource(Res.string.system),
                                            stringResource(Res.string.light),
                                            stringResource(Res.string.dark)
                                        ),
                                        selectedIndex = themeType.themeOptionIndex(),
                                        onSelected = { index ->
                                            val targetTheme = index.themeType()
                                            if (targetTheme != themeType) {
                                                coroutineScope.launch {
                                                    settingsViewModel.settingsManager.setThemeTypeData(targetTheme)
                                                }
                                            }
                                        }
                                    )
                                }
                            )
                        }

                        JvmSettingGroup(
                            title = stringResource(Res.string.settings_group_interface),
                            modifier = Modifier.weight(1f)
                        ) {
                            JvmSettingNavigationRow(
                                icon = Res.drawable.settings_24px,
                                title = stringResource(Res.string.interface_settings),
                                description = stringResource(Res.string.settings_interface_desc),
                                value = stringResource(Res.string.settings_value_edit),
                                onClick = { navigator.navigate(InterfaceSetting) }
                            )
                            JvmSettingNavigationRow(
                                icon = Res.drawable.http_24px,
                                title = stringResource(Res.string.background_image_setting),
                                description = stringResource(Res.string.settings_section_interface_desc),
                                value = if (imageFilePath.isNullOrBlank()) {
                                    stringResource(Res.string.settings_value_unset)
                                } else {
                                    stringResource(Res.string.settings_value_set)
                                },
                                onClick = { navigator.navigate(SetBackgroundImage) }
                            )
                        }
                    }
                    }
                }

                item(key = "language") {
                    JvmSettingSectionPage(
                        index = "05",
                        title = tabs[4],
                        subtitle = stringResource(Res.string.settings_section_language_desc),
                        badge = "LANG"
                    ) {
                    JvmSettingGroupFlow {
                        JvmSettingGroup(
                            title = stringResource(Res.string.settings_group_language),
                            modifier = Modifier.weight(1f)
                        ) {
                            JvmSettingPickRow(
                                title = stringResource(Res.string.system),
                                description = stringResource(Res.string.settings_language_system_desc),
                                selected = settings.languageType == null,
                                onClick = {
                                    if (settings.languageType != null) {
                                        coroutineScope.launch {
                                            settingsViewModel.settingsManager.setLanguageTypeData(null)
                                        }
                                    }
                                }
                            )
                            LanguageType.entries.forEach { language ->
                                JvmSettingPickRow(
                                    title = language.languageName,
                                    description = language.languageCode,
                                    tag = if (language.enabled) {
                                        null
                                    } else {
                                        stringResource(Res.string.settings_language_disabled)
                                    },
                                    selected = settings.languageType == language,
                                    enabled = language.enabled,
                                    onClick = {
                                        if (settings.languageType != language) {
                                            coroutineScope.launch {
                                                settingsViewModel.settingsManager.setLanguageTypeData(language)
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                    }
                }

                item(key = "custom") {
                    JvmSettingSectionPage(
                        index = "06",
                        title = tabs[5],
                        subtitle = stringResource(Res.string.settings_section_custom_desc),
                        badge = "API"
                    ) {
                    JvmSettingGroupFlow {
                        JvmSettingGroup(
                            title = stringResource(Res.string.settings_group_api),
                            modifier = Modifier.weight(1f)
                        ) {
                            JvmSettingNavigationRow(
                                icon = Res.drawable.music_note_24px,
                                title = stringResource(Res.string.customize_lyric_settings),
                                description = stringResource(Res.string.jvm_setting_screen_text_30),
                                value = stringResource(Res.string.settings_value_edit),
                                onClick = { navigator.navigate(CustomApi) }
                            )
                            JvmSettingSwitchRow(
                                icon = Res.drawable.settings_24px,
                                title = stringResource(Res.string.prioritize_music_service_api),
                                description = stringResource(Res.string.settings_prioritize_api_desc),
                                checked = settings.ifPriorityMusicApi,
                                onCheckedChange = { checked ->
                                    coroutineScope.launch {
                                        settingsViewModel.settingsManager.setIfPriorityMusicApi(checked)
                                    }
                                }
                            )
                        }
                    }
                    }
                }

                item(key = "about") {
                    JvmSettingSectionPage(
                        index = "07",
                        title = tabs[6],
                        subtitle = stringResource(Res.string.settings_section_about_desc),
                        badge = "ABOUT"
                    ) {
                    JvmSettingGroupFlow {
                        JvmSettingGroup(
                            title = stringResource(Res.string.settings_group_about),
                            modifier = Modifier.weight(1f)
                        ) {
                            JvmSettingNavigationRow(
                                icon = Res.drawable.info_24px,
                                title = stringResource(Res.string.about),
                                description = stringResource(Res.string.jvm_setting_screen_text_32),
                                value = stringResource(Res.string.settings_value_view),
                                onClick = { navigator.navigate(About) }
                            )
                        }
                    }
                    JvmSettingNote(text = stringResource(Res.string.settings_note_about))
                    }
                }
            }
        }
    }
}

/** 设置页下载并发数可选项。 */
private val JvmDownloadConcurrencyOptions = listOf("1", "3", "5")

/**
 * 设置页单个滚动分区。
 *
 * LazyColumn 的每个 item 只应包含一个布局容器，因此分区标题与分组内容统一收在 Column 内，
 * 避免多个子项在同一个 item 中重叠。
 *
 * @param index 两位分区编号。
 * @param title 分区标题。
 * @param subtitle 分区说明。
 * @param badge 右侧等宽标记。
 * @param content 分区内的设置分组。
 */
@Composable
private fun JvmSettingSectionPage(
    index: String,
    title: String,
    subtitle: String,
    badge: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        content = {
            JvmSettingSectionHeader(
                index = index,
                title = title,
                subtitle = subtitle,
                badge = badge
            )
            content()
        }
    )
}

/**
 * 把主题枚举映射到分段控件的下标，顺序为跟随系统、浅色、深色。
 */
private fun ThemeTypeEnum.themeOptionIndex(): Int {
    return when (this) {
        ThemeTypeEnum.SYSTEM -> 0
        ThemeTypeEnum.LIGHT -> 1
        ThemeTypeEnum.DARK -> 2
    }
}

/**
 * 把分段控件下标映射回主题枚举。
 */
private fun Int.themeType(): ThemeTypeEnum {
    return when (this) {
        1 -> ThemeTypeEnum.LIGHT
        2 -> ThemeTypeEnum.DARK
        else -> ThemeTypeEnum.SYSTEM
    }
}

/**
 * 音质档位的说明文案，与在线音质子页保持同一套描述。
 */
@Composable
private fun TranscodeAudioBitRateType.qualityDescription(): String {
    return when (this) {
        TranscodeAudioBitRateType.LOSSLESS -> stringResource(Res.string.jvm_streaming_quality_screen_text_15)
        TranscodeAudioBitRateType.HIGHEST -> stringResource(Res.string.jvm_streaming_quality_screen_text_16)
        TranscodeAudioBitRateType.HIGH -> stringResource(Res.string.jvm_streaming_quality_screen_text_17)
        TranscodeAudioBitRateType.MEDIUM -> stringResource(Res.string.jvm_streaming_quality_screen_text_18)
        TranscodeAudioBitRateType.LOW -> stringResource(Res.string.jvm_streaming_quality_screen_text_19)
    }
}
