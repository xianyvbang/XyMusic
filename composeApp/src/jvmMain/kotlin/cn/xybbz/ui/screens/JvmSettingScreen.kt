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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cn.xybbz.common.enums.TranscodeAudioBitRateType
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.common.utils.copyTextToClipboard
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.config.info.supportsICloudPasswordSync
import cn.xybbz.config.setting.JvmAutoStartManager
import cn.xybbz.localdata.enums.CacheUpperLimitEnum
import cn.xybbz.music.cacheUpperLimitOptions
import cn.xybbz.router.About
import cn.xybbz.ui.components.JvmSettingActionEntry
import cn.xybbz.ui.components.JvmSettingActionGrid as JvmSettingActionEntryGrid
import cn.xybbz.ui.components.JvmSettingBaseRow
import cn.xybbz.ui.components.JvmSettingDownloadRow
import cn.xybbz.ui.components.JvmSettingGroup
import cn.xybbz.ui.components.JvmSettingGroupGrid
import cn.xybbz.ui.components.JvmSettingHeadline
import cn.xybbz.ui.components.JvmSettingLazyColumn
import cn.xybbz.ui.components.JvmSettingNote
import cn.xybbz.ui.components.JvmSettingPathRow
import cn.xybbz.ui.components.JvmSettingPickRow
import cn.xybbz.ui.components.JvmSettingRowDescriptionStyle
import cn.xybbz.ui.components.JvmSettingSwitchRow
import cn.xybbz.ui.components.JvmSettingTabBar
import cn.xybbz.ui.components.JvmSettingTabDivider
import cn.xybbz.ui.components.JvmSettingTabItem
import cn.xybbz.ui.components.JvmSettingThemeRow
import cn.xybbz.ui.components.JvmSettingWideGroup
import cn.xybbz.ui.components.displayAudioBitRateText
import cn.xybbz.ui.components.rememberBackgroundImagePicker
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyIconTextButton
import cn.xybbz.viewmodel.CustomLyricsViewModel
import cn.xybbz.viewmodel.InterfaceSettingViewModel
import cn.xybbz.viewmodel.LanguageConfigViewModel
import cn.xybbz.viewmodel.MemoryManagementViewModel
import cn.xybbz.viewmodel.ProxyConfigViewModel
import cn.xybbz.viewmodel.SettingsViewModel
import cn.xybbz.viewmodel.StreamingQualityViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import java.awt.SystemTray
import xymusic.composeapp.generated.resources.*
import xymusic.composeapp.generated.resources.Res
import xymusic.composeapp.generated.resources.about
import xymusic.composeapp.generated.resources.album_24px
import xymusic.composeapp.generated.resources.album_playback_history
import xymusic.composeapp.generated.resources.allow_simultaneous_playback
import xymusic.composeapp.generated.resources.av_timer_24px
import xymusic.composeapp.generated.resources.background_image_setting
import xymusic.composeapp.generated.resources.broadcast_while_down
import xymusic.composeapp.generated.resources.clear_image
import xymusic.composeapp.generated.resources.close_24px
import xymusic.composeapp.generated.resources.connect
import xymusic.composeapp.generated.resources.copy_success
import xymusic.composeapp.generated.resources.download
import xymusic.composeapp.generated.resources.download_24px
import xymusic.composeapp.generated.resources.enabled_sync_play_progress
import xymusic.composeapp.generated.resources.folder_managed_24px
import xymusic.composeapp.generated.resources.info_24px
import xymusic.composeapp.generated.resources.interface_settings
import xymusic.composeapp.generated.resources.jvm_interface_setting_screen_text_04
import xymusic.composeapp.generated.resources.jvm_set_background_image_screen_text_01
import xymusic.composeapp.generated.resources.jvm_set_background_image_screen_text_20
import xymusic.composeapp.generated.resources.jvm_setting_screen_text_60
import xymusic.composeapp.generated.resources.language
import xymusic.composeapp.generated.resources.password_24px
import xymusic.composeapp.generated.resources.queue_music_24px
import xymusic.composeapp.generated.resources.restart_alt_24px
import xymusic.composeapp.generated.resources.select_image
import xymusic.composeapp.generated.resources.settings
import xymusic.composeapp.generated.resources.settings_24px
import xymusic.composeapp.generated.resources.song_cache_location
import xymusic.composeapp.generated.resources.volume_up_24px

/**
 * 设置页面
 */
@Composable
fun JvmSettingScreen(
    settingsViewModel: SettingsViewModel = koinViewModel<SettingsViewModel>(),
    // 音频音质分组直接内嵌在线音质设置，复用其视图模型读写品质档位与转码格式。
    streamingQualityViewModel: StreamingQualityViewModel = koinViewModel<StreamingQualityViewModel>(),
    // 存储分组直接内嵌原存储管理页的占用与清理能力，复用其视图模型。
    memoryManagementViewModel: MemoryManagementViewModel = koinViewModel<MemoryManagementViewModel>(),
    // 网络分组内嵌原代理设置页的开关、地址与连通性测试能力，复用其视图模型。
    proxyConfigViewModel: ProxyConfigViewModel = koinViewModel<ProxyConfigViewModel>(),
    // 界面分组内嵌原界面设置页的主题外观与背景图片入口，复用其视图模型读写主题。
    interfaceSettingViewModel: InterfaceSettingViewModel = koinViewModel<InterfaceSettingViewModel>(),
    // 语言分组内嵌原本地化页的语言选择与预览能力，复用其视图模型读写当前语言。
    languageConfigViewModel: LanguageConfigViewModel = koinViewModel<LanguageConfigViewModel>(),
    // 自定义资源分组内嵌原自定义 API 设置页的接口配置能力，复用其视图模型读写歌词与封面接口。
    customLyricsViewModel: CustomLyricsViewModel = koinViewModel<CustomLyricsViewModel>(),
) {
    val navigator = LocalNavigator.current
    val coroutineScope = rememberCoroutineScope()
    val settingsManager = settingsViewModel.settingsManager
    val settings = settingsViewModel.settingDataNow
    val ifSyncPasswordsByICloud by settingsManager.ifSyncPasswordsByICloud.collectAsState()
    val songStoragePath = settingsViewModel.songStoragePath
    val copySuccess = stringResource(Res.string.copy_success)
    val canShowICloudPasswordSync = supportsICloudPasswordSync()
    // 当前主题用于界面分组回显，跟随设置流实时刷新。
    val themeType by interfaceSettingViewModel.settingsManager.themeType.collectAsState()
    // 背景图片路径用于界面分组内嵌的预览与文件信息展示。
    val backgroundImagePath by settingsManager.imageFilePath.collectAsState()
    // 是否已选择背景图片，驱动预览、清除按钮与状态标签。
    val backgroundImageConfigured = !backgroundImagePath.isNullOrBlank()
    // 从路径中提取文件名，作为文件信息行的标题。
    val backgroundImageFileName = backgroundImagePath?.toBackgroundImageFileName()
    // 背景图片选择器：选中后直接写入设置，主界面背景与内嵌预览同步刷新。
    val backgroundImagePicker = rememberBackgroundImagePicker(
        onImagePicked = { path ->
            coroutineScope.launch {
                settingsManager.setImageFilePath(path)
            }
        }
    )
    val selectedQuality = TranscodeAudioBitRateType
        .getTranscodeAudioBitRate(settings.wifiNetworkAudioBitRate)
        .displayAudioBitRateText()
    val dataSourceLabel = settings.dataSourceType?.title
        ?: stringResource(Res.string.jvm_connection_config_info_screen_text_01)

    // 迁移音乐缓存页的兜底逻辑：历史不可展示档位（自动/不缓存）在开启缓存后统一修正为默认 2GB 档位。
    // 以开启状态与当前档位作为 key，用户在本页切换边下边播开关后也能立即完成迁移。
    LaunchedEffect(settings.ifEnableEdgeDownload, settings.cacheUpperLimit) {
        if (settings.ifEnableEdgeDownload &&
            cacheUpperLimitOptions().none { it.limit == settings.cacheUpperLimit }
        ) {
            settingsManager.setCacheUpperLimit(CacheUpperLimitEnum.ThreeG)
        }
    }

    // 顶部标签：顺序必须与下方 LazyColumn 中分组 item 的顺序保持一致
    val settingTabs = listOf(
        // 常规放在最左侧，集中启动、播放与音质缓存等桌面端基础设置。
        JvmSettingTabItem(key = "general", title = stringResource(Res.string.jvm_setting_screen_text_57)),
        JvmSettingTabItem(key = "storage", title = stringResource(Res.string.jvm_setting_screen_text_14)),
        JvmSettingTabItem(key = "connect", title = stringResource(Res.string.connect)),
        // 界面放在通用之前，集中主题外观与背景图片等桌面端视觉设置。
        JvmSettingTabItem(key = "interface", title = stringResource(Res.string.jvm_setting_screen_text_60)),
        // 语言紧跟在界面之后，聚合语言选择、文本预览与缺失翻译说明。
        JvmSettingTabItem(key = "language", title = stringResource(Res.string.language)),
        // 自定义资源紧跟在语言之后，承载原自定义 API 设置页的歌词与封面接口配置。
        JvmSettingTabItem(key = "custom", title = stringResource(Res.string.jvm_setting_screen_text_61)),
        JvmSettingTabItem(key = "common", title = stringResource(Res.string.jvm_setting_screen_text_23)),
    )
    // 设置页使用独立滚动区：头部与标签固定，只有下方内容滚动
    val lazyState = rememberLazyListState()
    // 标签与滚动列表一一对应，首项索引即当前分组索引
    val selectedTabIndex by remember(settingTabs.size) {
        derivedStateOf {
            lazyState.firstVisibleItemIndex.coerceIn(0, settingTabs.lastIndex)
        }
    }

    XyColumnScreen {
        Column(modifier = Modifier.fillMaxSize()) {
            // 固定头部：紧凑标题、数据源读数与标签栏不随内容滚动
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = XyTheme.dimens.outerHorizontalPadding,
                        end = XyTheme.dimens.outerHorizontalPadding,
                        top = XyTheme.dimens.outerVerticalPadding * 2,
                    ),
                verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding * 2),
            ) {
                JvmSettingHeadline(
                    title = stringResource(Res.string.settings),
                    meta = stringResource(
                        Res.string.jvm_setting_screen_text_43,
                        dataSourceLabel,
                        selectedQuality
                    ),
                )

                // 顶部标签栏：点击标签滚动到对应分组，滚动时自动高亮当前分组
                JvmSettingTabBar(
                    tabs = settingTabs,
                    selectedIndex = selectedTabIndex,
                    onSelected = { index ->
                        coroutineScope.launch {
                            lazyState.animateScrollToItem(index)
                        }
                    },
                )
            }

            // 设置页专用 LazyColumn：每个标签对应一个双列分组页面
            JvmSettingLazyColumn(
                lazyState = lazyState,
                modifier = Modifier.weight(1f),
                // 底部留出更大滚动余量，避免最后一组设置贴住窗口底边。
                contentPadding = PaddingValues(
                    start = XyTheme.dimens.outerHorizontalPadding,
                    end = XyTheme.dimens.outerHorizontalPadding,
                    top = XyTheme.dimens.outerVerticalPadding * 2,
                    bottom = XyTheme.dimens.outerVerticalPadding * 6,
                ),
            ) {
                item(key = "general") {
                    JvmSettingGroupGrid { groupWidth ->
                        JvmSettingGroup(
                            modifier = Modifier.width(groupWidth),
                            title = stringResource(Res.string.jvm_setting_screen_text_44),
                        ) {
                            JvmSettingSwitchRow(
                                icon = Res.drawable.restart_alt_24px,
                                title = stringResource(Res.string.jvm_setting_screen_text_45),
                                description = stringResource(Res.string.jvm_setting_screen_text_46),
                                checked = settings.ifAutoStart,
                                // 非 Windows 平台无法写入 Run 注册表，直接禁用开关。
                                enabled = JvmAutoStartManager.isSupported,
                                onCheckedChange = { checked ->
                                    coroutineScope.launch {
                                        // 注册表写入成功后再持久化设置，避免开关状态与实际启动项不一致。
                                        val success = withContext(Dispatchers.IO) {
                                            JvmAutoStartManager.setEnabled(checked)
                                        }
                                        if (success) {
                                            settingsManager.setIfAutoStart(checked)
                                        } else {
                                            MessageUtils.sendPopTipError(Res.string.jvm_setting_screen_text_56)
                                        }
                                    }
                                }
                            )

                            JvmSettingSwitchRow(
                                icon = Res.drawable.queue_music_24px,
                                title = stringResource(Res.string.jvm_setting_screen_text_47),
                                description = stringResource(Res.string.jvm_setting_screen_text_48),
                                checked = settings.ifRestorePlayQueue,
                                onCheckedChange = { checked ->
                                    coroutineScope.launch {
                                        settingsManager.setIfRestorePlayQueue(checked)
                                    }
                                }
                            )

                            JvmSettingSwitchRow(
                                icon = Res.drawable.close_24px,
                                title = stringResource(Res.string.jvm_setting_screen_text_49),
                                description = stringResource(Res.string.jvm_setting_screen_text_50),
                                checked = settings.ifMinimizeToTray,
                                // 系统不支持托盘时禁用，避免关闭窗口后无法恢复界面。
                                enabled = SystemTray.isSupported(),
                                onCheckedChange = { checked ->
                                    coroutineScope.launch {
                                        settingsManager.setIfMinimizeToTray(checked)
                                    }
                                }
                            )
                        }

                        // 播放相关开关并入常规设置，与启动项同处一个分组页。
                        JvmSettingGroup(
                            modifier = Modifier.width(groupWidth),
                            title = stringResource(Res.string.jvm_setting_screen_text_51),
                        ) {
                            JvmSettingSwitchRow(
                                icon = Res.drawable.download_24px,
                                title = stringResource(Res.string.broadcast_while_down),
                                description = stringResource(Res.string.jvm_setting_screen_text_08),
                                checked = settings.ifEnableEdgeDownload,
                                onCheckedChange = { checked ->
                                    coroutineScope.launch {
                                        settingsManager.setIfEnableEdgeDownload(checked)
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
                                        settingsManager.setIfEnableAlbumHistory(checked)
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
                                        settingsManager.setIfHandleAudioFocus(checked)
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

                            if (canShowICloudPasswordSync) {
                                JvmSettingSwitchRow(
                                    icon = Res.drawable.password_24px,
                                    title = stringResource(Res.string.icloud_keychain_password_sync),
                                    description = stringResource(Res.string.icloud_keychain_password_sync_desc),
                                    checked = ifSyncPasswordsByICloud,
                                    onCheckedChange = { checked ->
                                        coroutineScope.launch {
                                            settingsViewModel.setIfSyncPasswordsByICloud(checked)
                                        }
                                    }
                                )
                            }
                        }

                        // 播放品质分组：把原在线音乐品质页的品质档位独立成组，与其他小类共用同一套分组样式。
                        JvmSettingGroup(
                            modifier = Modifier.width(groupWidth),
                            title = stringResource(Res.string.jvm_streaming_quality_screen_text_02),
                        ) {
                            TranscodeAudioBitRateType.entries.forEach { quality ->
                                JvmSettingPickRow(
                                    title = quality.kickerText(),
                                    description = quality.descriptionText(),
                                    // 桌面端只维护一组品质设置，回显以 Wi-Fi 码率为准。
                                    selected = streamingQualityViewModel.wifiNetworkAudioBitRate == quality,
                                    tag = quality.bitRateTagText(),
                                    onClick = {
                                        coroutineScope.launch {
                                            // 桌面端只暴露一组品质设置，选择后同步写入 Wi-Fi 与移动网络两套码率。
                                            streamingQualityViewModel.updateWifiNetworkAudioBitRate(quality)
                                            streamingQualityViewModel.updateMobileNetworkAudioBitRate(quality)
                                        }
                                    },
                                )
                            }
                        }

                        // 转码格式分组：服务端支持的格式列表，同样使用统一的分组与单选行样式。
                        JvmSettingGroup(
                            modifier = Modifier.width(groupWidth),
                            title = stringResource(Res.string.transcoding_format),
                        ) {
                            if (streamingQualityViewModel.transcodeAudioBitRateType.isEmpty()) {
                                JvmSettingNote(
                                    text = stringResource(Res.string.jvm_streaming_quality_screen_text_10)
                                )
                            } else {
                                streamingQualityViewModel.transcodeAudioBitRateType.forEach { format ->
                                    val targetFormat = format.targetFormat
                                    JvmSettingPickRow(
                                        title = format.name.ifBlank { targetFormat.uppercase() },
                                        description = targetFormat.formatDescriptionText(),
                                        selected = streamingQualityViewModel.transcodeFormat == targetFormat,
                                        onClick = {
                                            coroutineScope.launch {
                                                streamingQualityViewModel.updateTranscodeFormat(targetFormat)
                                            }
                                        },
                                    )
                                }
                            }
                        }
                    }
                }

                item(key = "storage") {
                    Column {
                        // 标签页之间用横线分隔，避免不同标签的内容视觉上连成一片。
                        JvmSettingTabDivider()
                        JvmSettingGroupGrid { groupWidth ->
                            // 下载分组：下载并发与歌曲缓存位置。
                            JvmSettingGroup(
                                modifier = Modifier.width(groupWidth),
                                title = stringResource(Res.string.download),
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
                            }

                            // 缓存分组：内嵌音乐缓存页迁移过来的缓存上限档位选择。
                            JvmSettingGroup(
                                modifier = Modifier.width(groupWidth),
                                title = stringResource(Res.string.jvm_setting_screen_text_59),
                            ) {
                                // 仅开启边下边播时才有缓存上限；直接条件渲染，避免隐藏项在横向排布中留下空位。
                                if (settings.ifEnableEdgeDownload) {
                                    // 迁移自音乐缓存页：内嵌缓存上限档位选择，样式适配为设置页单选行。
                                    JvmCacheLimitSettingItems(
                                        selectedLimit = settings.cacheUpperLimit,
                                        onLimitSelected = { limit ->
                                            coroutineScope.launch {
                                                settingsManager.setCacheUpperLimit(limit)
                                            }
                                        },
                                    )
                                }
                            }

                            // 存储管理相关功能整体内嵌到设置页，不再跳转独立存储管理页。
                            JvmStorageSettingsSection(
                                memoryManagementViewModel = memoryManagementViewModel,
                                modifier = Modifier.width(groupWidth),
                            )
                        }
                    }
                }

                item(key = "connect") {
                    Column {
                        // 标签页之间用横线分隔，避免不同标签的内容视觉上连成一片。
                        JvmSettingTabDivider()
                        JvmSettingGroupGrid { groupWidth ->
                            // 代理设置内容整体收进网络分组：开关、地址与测试保存能力都在这里。
                            JvmProxySettingsSection(
                                proxyConfigViewModel = proxyConfigViewModel,
                                modifier = Modifier.width(groupWidth),
                            )
                        }
                    }
                }

                item(key = "interface") {
                    Column {
                        // 标签页之间用横线分隔，避免不同标签的内容视觉上连成一片。
                        JvmSettingTabDivider()
                        JvmSettingGroupGrid { groupWidth ->
                            // 外观分组：迁移原界面设置页的主题外观，改为单行加右侧分段选择的外观模式设置。
                            // 使用普通分组让设置行只占一列宽度，避免横向上铺满整个内容区。
                            JvmSettingGroup(
                                modifier = Modifier.width(groupWidth),
                                title = stringResource(Res.string.jvm_interface_setting_screen_text_04),
                            ) {
                                JvmSettingThemeRow(
                                    selected = themeType,
                                    // 主题写入由视图模型内部切换到协程，这里直接透传选择结果。
                                    onSelected = interfaceSettingViewModel::setThemeTypeData,
                                )
                            }

                            // 背景图片分组：原独立页的选择/清除与文件信息整体内嵌，避免二次跳转。
                            // 设置行需要独立控制宽度，不参与普通分组的横向等分，因此改用宽内容分组。
                            JvmSettingWideGroup(
                                modifier = Modifier.width(groupWidth),
                                title = stringResource(Res.string.background_image_setting),
                            ) {
                                // 限制最大宽度并左对齐，避免单个设置行横向铺满整个内容区。
                                Box(modifier = Modifier.widthIn(max = JvmSettingBackgroundItemMaxWidth)) {
                                    // 文件信息行：文件名加完整路径，点击复制；选择/清除按钮放在行尾，按内容宽度排布不横向铺满。
                                    JvmSettingBaseRow(
                                        icon = Res.drawable.album_24px,
                                        title = backgroundImageFileName
                                            ?: stringResource(Res.string.jvm_set_background_image_screen_text_01),
                                        description = backgroundImagePath?.takeIf { it.isNotBlank() }
                                            ?: stringResource(Res.string.jvm_set_background_image_screen_text_20),
                                        descriptionStyle = JvmSettingRowDescriptionStyle.Path,
                                        descriptionMaxLines = 2,
                                        iconSelected = backgroundImageConfigured,
                                        onClick = if (backgroundImageConfigured) {
                                            {
                                                copyTextToClipboard(backgroundImagePath.orEmpty())
                                                MessageUtils.sendPopTip(copySuccess)
                                            }
                                        } else {
                                            null
                                        },
                                        trailing = {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding),
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                XyIconTextButton(
                                                    onClick = composeClick(onClick = backgroundImagePicker::pickImage),
                                                    text = stringResource(Res.string.select_image),
                                                    icon = Res.drawable.folder_managed_24px,
                                                    // 收紧内边距，让按钮只占自身内容宽度。
                                                    paddingValues = PaddingValues(
                                                        horizontal = XyTheme.dimens.contentPadding,
                                                        vertical = XyTheme.dimens.outerVerticalPadding / 2,
                                                    ),
                                                )
                                                XyIconTextButton(
                                                    onClick = composeClick(onClick = {
                                                        coroutineScope.launch {
                                                            settingsManager.setImageFilePath(null)
                                                        }
                                                    }),
                                                    text = stringResource(Res.string.clear_image),
                                                    icon = Res.drawable.close_24px,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    backgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                                    borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
                                                    enabled = backgroundImageConfigured,
                                                    // 与选择按钮保持一致的内边距，两个按钮都按内容宽度排布。
                                                    paddingValues = PaddingValues(
                                                        horizontal = XyTheme.dimens.contentPadding,
                                                        vertical = XyTheme.dimens.outerVerticalPadding / 2,
                                                    ),
                                                )
                                            }
                                        },
                                    )
                                }
                            }
                        }
                    }
                }

                item(key = "language") {
                    Column {
                        // 标签页之间用横线分隔，避免不同标签的内容视觉上连成一片。
                        JvmSettingTabDivider()
                        // 语言分区整块内嵌原本地化页能力，分组间距由分区内部统一控制。
                        JvmSettingGroupGrid { groupWidth ->
                            JvmLanguageSettingsSection(
                                languageConfigViewModel = languageConfigViewModel,
                                modifier = Modifier.width(groupWidth),
                            )
                        }
                    }
                }

                item(key = "custom") {
                    Column {
                        // 标签页之间用横线分隔，避免不同标签的内容视觉上连成一片。
                        JvmSettingTabDivider()
                        // 自定义资源分区：歌词与封面接口的地址、共用访问令牌与保存操作。
                        JvmCustomResourceSettingsSection(
                            customLyricsViewModel = customLyricsViewModel,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                item(key = "common") {
                    Column {
                        // 标签页之间用横线分隔，避免不同标签的内容视觉上连成一片。
                        JvmSettingTabDivider()
                        JvmSettingGroupGrid { groupWidth ->
                            JvmSettingGroup(
                                modifier = Modifier.width(groupWidth),
                                title = stringResource(Res.string.jvm_setting_screen_text_55),
                            ) {
                                JvmSettingActionGrid(
                                    onAboutClick = {
                                        navigator.navigate(About)
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 设置页通用入口卡片网格。
 *
 * @param onAboutClick 关于页面入口点击事件。
 */
@Composable
private fun JvmSettingActionGrid(
    onAboutClick: () -> Unit,
) {
    // 设置页只关心关于入口，通用网格负责真正的宽度计算和卡片渲染。
    JvmSettingActionEntryGrid(
        actionEntries = jvmSettingActionEntries(onAboutClick = onAboutClick),
        // 通用入口放在双列分组内，卡片平分分组宽度，保持设计稿的两列卡片排布。
        fillTwoColumnWidth = true,
    )
}

/**
 * 组装设置页右栏“通用”分组中的入口卡片。
 */
@Composable
private fun jvmSettingActionEntries(
    onAboutClick: () -> Unit,
): List<JvmSettingActionEntry> {
    return listOf(
        JvmSettingActionEntry(
            icon = Res.drawable.info_24px,
            kicker = stringResource(Res.string.jvm_setting_screen_text_31),
            title = stringResource(Res.string.about),
            description = stringResource(Res.string.jvm_setting_screen_text_32),
            onClick = onAboutClick,
        ),
    )
}

/** 品质档位的名称文案。 */
@Composable
private fun TranscodeAudioBitRateType.kickerText(): String {
    return when (this) {
        TranscodeAudioBitRateType.LOSSLESS -> stringResource(Res.string.jvm_streaming_quality_screen_text_11)
        TranscodeAudioBitRateType.HIGHEST -> stringResource(Res.string.jvm_streaming_quality_screen_text_12)
        TranscodeAudioBitRateType.HIGH -> stringResource(Res.string.jvm_cache_limit_screen_text_32)
        TranscodeAudioBitRateType.MEDIUM -> stringResource(Res.string.jvm_streaming_quality_screen_text_13)
        TranscodeAudioBitRateType.LOW -> stringResource(Res.string.jvm_streaming_quality_screen_text_14)
    }
}

/** 品质档位卡片的主说明文案。 */
@Composable
private fun TranscodeAudioBitRateType.descriptionText(): String {
    return when (this) {
        TranscodeAudioBitRateType.LOSSLESS -> stringResource(Res.string.jvm_streaming_quality_screen_text_15)
        TranscodeAudioBitRateType.HIGHEST -> stringResource(Res.string.jvm_streaming_quality_screen_text_16)
        TranscodeAudioBitRateType.HIGH -> stringResource(Res.string.jvm_streaming_quality_screen_text_17)
        TranscodeAudioBitRateType.MEDIUM -> stringResource(Res.string.jvm_streaming_quality_screen_text_18)
        TranscodeAudioBitRateType.LOW -> stringResource(Res.string.jvm_streaming_quality_screen_text_19)
    }
}

/** 档位右侧标签文案；不转码档位无需标注码率，其余档位展示实际码率。 */
@Composable
private fun TranscodeAudioBitRateType.bitRateTagText(): String? {
    return if (this == TranscodeAudioBitRateType.LOSSLESS) {
        null
    } else {
        displayAudioBitRateText()
    }
}

/** 转码格式的主说明文案。 */
@Composable
private fun String.formatDescriptionText(): String {
    return when (lowercase()) {
        "mp3" -> stringResource(Res.string.jvm_streaming_quality_screen_text_23)
        "aac" -> stringResource(Res.string.jvm_streaming_quality_screen_text_24)
        else -> stringResource(Res.string.jvm_streaming_quality_screen_text_25)
    }
}

/** 从背景图片路径中提取文件名，用于文件信息行标题展示。 */
private fun String.toBackgroundImageFileName(): String {
    return substringAfterLast('\\')
        .substringAfterLast('/')
        .ifBlank { this }
}

/** 背景图片设置行的最大宽度：避免单个设置行横向铺满整个内容区。 */
private val JvmSettingBackgroundItemMaxWidth = 560.dp
