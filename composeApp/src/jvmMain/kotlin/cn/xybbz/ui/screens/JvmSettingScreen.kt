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

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.xybbz.common.enums.TranscodeAudioBitRateType
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.common.utils.copyTextToClipboard
import cn.xybbz.common.utils.formatBytes
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.localdata.enums.CacheUpperLimitEnum
import cn.xybbz.localdata.enums.LanguageType
import cn.xybbz.localdata.enums.ThemeTypeEnum
import cn.xybbz.music.cacheUpperLimitOptions
import cn.xybbz.router.SetBackgroundImage
import cn.xybbz.router.StreamingQuality
import cn.xybbz.ui.components.AlertDialogObject
import cn.xybbz.ui.components.JvmSettingFixedHeader
import cn.xybbz.ui.components.JvmSettingSegmentedControl
import cn.xybbz.ui.components.displayAudioBitRateText
import cn.xybbz.ui.components.displayMessage
import cn.xybbz.ui.components.rememberJvmFileKitDialogSettings
import cn.xybbz.ui.components.show
import cn.xybbz.ui.components.toExistingPlatformDirectoryOrNull
import cn.xybbz.ui.xy.LazyColumnNotComponent
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyText
import cn.xybbz.ui.xy.XyTextSub
import cn.xybbz.viewmodel.AboutViewModel
import cn.xybbz.viewmodel.CustomLyricsViewModel
import cn.xybbz.viewmodel.MemoryManagementViewModel
import cn.xybbz.viewmodel.SettingsViewModel
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.dialogs.openDirectoryPicker
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic.composeapp.generated.resources.*
import java.io.File

/** 设置页滚动内容左右内边距，与设计稿的 40px 保持一致。 */
private val JvmSettingContentHorizontalPadding = 40.dp

/** 设置页相邻分区之间的纵向间距，对应设计稿 .st-page 的 margin-top。 */
private val JvmSettingSectionTopGap = 34.dp

/** 设置页分区顶部内容间距，对应设计稿 .st-page 的 padding-top。 */
private val JvmSettingSectionInnerGap = 30.dp

/** 设置页滚动内容顶部内边距，对应设计稿 .st-scroll 的 padding-top。 */
private val JvmSettingContentTopPadding = 24.dp

/** 设置页滚动内容底部内边距，避免最后一行贴着播放栏。 */
private val JvmSettingContentBottomPadding = 56.dp

/** 设置页顶部标签索引总数，与分区数量一一对应。 */
private const val JvmSettingTabCount = 7

/** 设置行圆角，对应设计稿 .row 的 7px。 */
private val JvmSettingRowCorner = 7.dp

/** 设置行最小高度，对应设计稿 .row 的 min-height。 */
private val JvmSettingRowMinHeight = 62.dp

/** 设置行统一内边距，对应设计稿 .row 的 12px。 */
private val JvmSettingRowPadding = 12.dp

/** 设置行图标容器尺寸，对应设计稿 .ico 的 30px。 */
private val JvmSettingRowIconSize = 30.dp

/** 设置表单输入框高度，对应设计稿 .field input 的 34px。 */
private val JvmSettingFieldHeight = 34.dp

/** 设置页接口地址，用于「开源仓库」跳转。 */
private const val JvmSettingRepositoryUrl = "https://github.com/xianyvbang/XyMusic"

/**
 * 设置页面。
 *
 * 页面由一个固定不动的「设置」标题与 7 个横向标签，以及下方独立滚动的连续分区列表组成；
 * 标签会随滚动位置联动高亮，点击标签会平滑定位到对应分区。
 * 设计稿中的每个分组、行、表单和读数都对应真实的设置字段或原有的子页面逻辑。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JvmSettingScreen(
    settingsViewModel: SettingsViewModel = koinViewModel<SettingsViewModel>(),
    memoryManagementViewModel: MemoryManagementViewModel = koinViewModel<MemoryManagementViewModel>(),
    customLyricsViewModel: CustomLyricsViewModel = koinViewModel<CustomLyricsViewModel>(),
    aboutViewModel: AboutViewModel = koinViewModel<AboutViewModel>(),
) {
    val navigator = LocalNavigator.current
    val uriHandler = LocalUriHandler.current
    val coroutineScope = rememberCoroutineScope()

    // 设置数据是页面上所有开关、单选项和读数的唯一数据源。
    val settings = settingsViewModel.settingDataNow
    val imageFilePath by settingsViewModel.settingsManager.imageFilePath.collectAsState()
    val themeType by settingsViewModel.settingsManager.themeType.collectAsState()
    val songStoragePath = memoryManagementViewModel.musicCachePath
    val copySuccess = stringResource(Res.string.copy_success)
    val warningText = stringResource(Res.string.warning)
    // 关于分组弹窗所需文案，提前取好以便在非 Composable 的点击回调里使用。
    val checkUpdateTitle = stringResource(Res.string.settings_row_check_update)
    val notImplementedText = stringResource(Res.string.function_not_implemented)
    val licenseTitle = stringResource(Res.string.settings_row_license)
    val licenseName = stringResource(Res.string.settings_license_name)
    val authorTitle = stringResource(Res.string.settings_row_author)
    val authorName = stringResource(Res.string.settings_author_name)
    // 虚线分隔线颜色在 drawBehind 中不能用 Composable 读取，这里预先取好。
    val dashedLineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    val dataSourceLabel = settings.dataSourceType?.title
        ?: stringResource(Res.string.settings_data_source_none)
    val selectedQuality = TranscodeAudioBitRateType
        .getTranscodeAudioBitRate(settings.wifiNetworkAudioBitRate)
    val selectedQualityLabel = selectedQuality.displayAudioBitRateText()
    val mobileQualityLabel = TranscodeAudioBitRateType
        .getTranscodeAudioBitRate(settings.mobileNetworkAudioBitRate)
        .displayAudioBitRateText()
    // 原始音质没有数字码率，行尾标签改为「原始」。
    val losslessQualityTag = stringResource(Res.string.transcode_audio_bit_rate_type_text_01)

    // 语言搜索框的输入值，只影响语言列表的过滤结果，不写入设置。
    var languageQuery by remember { mutableStateOf("") }

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

    // 首次进入页面时刷新关于信息与存储占用，并把历史缓存上限迁移到桌面端可展示档位。
    LaunchedEffect(Unit) {
        aboutViewModel.getPlatformInfo()
        memoryManagementViewModel.logStorageInfo()
        if (cacheUpperLimitOptions().none { it.limit == settings.cacheUpperLimit }) {
            settingsViewModel.settingsManager.setCacheUpperLimit(CacheUpperLimitEnum.FourG)
        }
    }

    // 复制路径的通用行为：路径为空时不做任何处理。
    val copyPath: (String) -> Unit = { path ->
        if (path.isNotBlank()) {
            copyTextToClipboard(path)
            MessageUtils.sendPopTip(copySuccess)
        }
    }

    // 目录选择器配置，绑定主窗口后系统弹窗不会跑到应用窗口后面。
    val cacheDirectoryDialogSettings = rememberJvmFileKitDialogSettings(
        stringResource(Res.string.settings_group_song_dir)
    )

    // 选择新的歌曲缓存目录，取消选择时保持当前路径不变。
    fun chooseSongCacheDirectory() {
        coroutineScope.launch {
            val initialDirectory = memoryManagementViewModel.musicCachePath
                .takeIf { it.isNotBlank() }
                ?.let(::File)
                ?.toExistingPlatformDirectoryOrNull()
            val selectedDirectory = runCatching {
                FileKit.openDirectoryPicker(
                    directory = initialDirectory,
                    dialogSettings = cacheDirectoryDialogSettings,
                )?.absolutePath()
            }.getOrNull()
            selectedDirectory?.let(memoryManagementViewModel::changeMusicCacheDirectory)
        }
    }

    // 清理数据库属于高风险操作，先弹出确认框再交给 ViewModel 执行。
    fun confirmClearDatabase(onConfirmed: () -> Unit) {
        AlertDialogObject(
            title = warningText,
            content = {
                XyTextSub(
                    text = stringResource(Res.string.confirm_delete_database),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            ifWarning = true,
            onConfirmation = onConfirmed
        ).show()
    }

    // 清理全部可清理数据：音频缓存、临时缓存与数据库数据。
    fun confirmClearAllCache() {
        confirmClearDatabase {
            memoryManagementViewModel.clearMusicCache()
            memoryManagementViewModel.clearAllCache()
            memoryManagementViewModel.clearDatabaseData()
        }
    }

    // 普通提示弹窗，用于关于页的链接与未实现功能。
    fun showMessageDialog(title: String, message: String) {
        AlertDialogObject(
            title = title,
            content = {
                XyTextSub(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            onConfirmation = {}
        ).show()
    }

    // 存储占用按「音频缓存 / 临时缓存 / 数据库数据 / 必要数据」四段展示，与存储管理页口径一致。
    val accentColor = MaterialTheme.colorScheme.primary
    val storageSlices = listOf(
        JvmStorageSlice(
            label = stringResource(Res.string.audio_cache),
            bytes = jvmSizeLabelToBytes(memoryManagementViewModel.musicCacheSize),
            color = accentColor
        ),
        JvmStorageSlice(
            label = stringResource(Res.string.temporary_cache),
            bytes = jvmSizeLabelToBytes(memoryManagementViewModel.cacheSize),
            color = accentColor.copy(alpha = 0.55f)
        ),
        JvmStorageSlice(
            label = stringResource(Res.string.database_data),
            bytes = jvmSizeLabelToBytes(memoryManagementViewModel.databaseSize),
            color = accentColor.copy(alpha = 0.28f)
        ),
        JvmStorageSlice(
            label = stringResource(Res.string.essential_data),
            bytes = jvmSizeLabelToBytes(memoryManagementViewModel.appDataSize),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f)
        ),
    )
    // 可清理容量不包含必要数据，用于「清理全部」按钮的容量读数。
    val cleanableBytes = storageSlices.take(3).sumOf { it.bytes }
    val cleanableLabel = formatBytes(cleanableBytes, withSpace = true)

    // 关于页读数：运行时、界面框架与当前服务器类型。
    val aboutReadouts = listOf(
        stringResource(Res.string.settings_about_runtime) to "JVM ${System.getProperty("java.version") ?: "-"}",
        stringResource(Res.string.settings_about_framework) to "Compose Multiplatform",
        stringResource(Res.string.settings_about_server) to dataSourceLabel,
    )
    val versionLabel = stringResource(
        Res.string.settings_about_version,
        aboutViewModel.versionInfo.ifBlank { "JVM" }
    )

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
                    top = 0.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                item(key = "general") {
                    JvmSettingSectionPage(isFirst = true) {
                        JvmSettingGroupGrid {
                            JvmSettingPlainGroup(
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

                            JvmSettingPlainGroup(
                                title = stringResource(Res.string.settings_group_download),
                                modifier = Modifier.weight(1f)
                            ) {
                                JvmSettingRow(
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
                            }
                        }
                    }
                }

                item(key = "playback") {
                    JvmSettingSectionPage {
                        JvmSettingGroupGrid {
                            JvmSettingPlainGroup(
                                title = stringResource(Res.string.settings_group_quality),
                                modifier = Modifier.weight(1f)
                            ) {
                                // 设计稿 .pick 列表内部条目间距为 2px。
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    TranscodeAudioBitRateType.entries.forEach { quality ->
                                        JvmSettingPickRow(
                                            title = quality.displayAudioBitRateText(),
                                            description = quality.qualityDescription(),
                                            tag = quality.audioBitRateStr.ifBlank { losslessQualityTag },
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
                            }

                            JvmSettingPlainGroup(
                                title = stringResource(Res.string.settings_group_network),
                                modifier = Modifier.weight(1f)
                            ) {
                                JvmSettingNavigationRow(
                                    icon = Res.drawable.http_24px,
                                    title = stringResource(Res.string.settings_row_wifi_quality),
                                    description = stringResource(Res.string.settings_row_wifi_quality_desc),
                                    value = selectedQualityLabel,
                                    onClick = { navigator.navigate(StreamingQuality) }
                                )
                                JvmSettingNavigationRow(
                                    icon = Res.drawable.http_24px,
                                    title = stringResource(Res.string.settings_row_mobile_quality),
                                    description = stringResource(Res.string.settings_row_mobile_quality_desc),
                                    value = mobileQualityLabel,
                                    onClick = { navigator.navigate(StreamingQuality) }
                                )
                                JvmSettingSwitchRow(
                                    icon = Res.drawable.http_24px,
                                    title = stringResource(Res.string.settings_row_any_transcode),
                                    description = stringResource(Res.string.settings_row_any_transcode_desc),
                                    checked = settings.ifTranscoding,
                                    onCheckedChange = { checked ->
                                        coroutineScope.launch {
                                            settingsViewModel.settingsManager.setIfTranscoding(checked)
                                        }
                                    }
                                )
                                JvmSettingNavigationRow(
                                    icon = Res.drawable.music_note_24px,
                                    title = stringResource(Res.string.settings_row_transcode_format),
                                    description = stringResource(Res.string.settings_row_transcode_format_desc),
                                    value = settings.transcodeFormat.uppercase(),
                                    onClick = { navigator.navigate(StreamingQuality) }
                                )
                            }
                        }
                    }
                }

                item(key = "cache") {
                    JvmSettingSectionPage {
                        JvmSettingGroupGrid {
                            JvmSettingPlainGroup(
                                title = stringResource(Res.string.settings_group_cache_limit),
                                modifier = Modifier.weight(1f)
                            ) {
                                // 设计稿 .pick 列表内部条目间距为 2px。
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    cacheUpperLimitOptions().forEach { option ->
                                        JvmSettingPickRow(
                                            title = option.displayMessage(),
                                            description = option.limit.cacheLimitDescription(),
                                            tag = if (option.limit == CacheUpperLimitEnum.FourG) {
                                                stringResource(Res.string.settings_cache_recommended)
                                            } else {
                                                null
                                            },
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
                            }

                            JvmSettingPlainGroup(
                                title = stringResource(Res.string.settings_group_cache_behavior),
                                modifier = Modifier.weight(1f)
                            ) {
                                JvmSettingSwitchRow(
                                    icon = Res.drawable.download_24px,
                                    title = stringResource(Res.string.settings_row_edge_download),
                                    description = stringResource(Res.string.settings_row_edge_download_desc),
                                    checked = settings.ifEnableEdgeDownload,
                                    onCheckedChange = { checked ->
                                        coroutineScope.launch {
                                            settingsViewModel.settingsManager.setIfEnableEdgeDownload(checked)
                                        }
                                    }
                                )
                            }

                            // 歌曲缓存目录：路径可复制，浏览与恢复默认复用存储管理页的目录逻辑。
                            JvmSettingPlainGroup(
                                title = stringResource(Res.string.settings_group_song_dir),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // 设计稿表单字段直接对齐分组左边缘，不再额外内缩。
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    JvmSettingFieldLabel(
                                        text = stringResource(Res.string.settings_field_cache_dir)
                                    )
                                    Spacer(modifier = Modifier.height(7.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        JvmSettingPathField(
                                            path = songStoragePath,
                                            modifier = Modifier.weight(1f),
                                            onClick = { copyPath(songStoragePath) }
                                        )
                                        JvmSettingButton(
                                            text = stringResource(Res.string.settings_action_browse),
                                            onClick = { chooseSongCacheDirectory() }
                                        )
                                        JvmSettingButton(
                                            text = stringResource(Res.string.settings_action_restore),
                                            enabled = !memoryManagementViewModel.isDefaultMusicCachePath,
                                            onClick = {
                                                memoryManagementViewModel.restoreDefaultMusicCacheDirectory()
                                            }
                                        )
                                    }
                                    JvmSettingNoteText(
                                        text = stringResource(Res.string.settings_note_cache_path)
                                    )
                                }
                            }

                            // 存储占用：容量分布条 + 图例 + 四类数据行，数据来自存储管理 ViewModel。
                            JvmSettingPlainGroup(
                                title = stringResource(Res.string.settings_group_storage_usage),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // 设计稿在该分组把 .stack 的上边距内联覆盖为 2px，下边距仍为 14px。
                                Spacer(modifier = Modifier.height(2.dp))
                                JvmSettingStackBar(slices = storageSlices)
                                Spacer(modifier = Modifier.height(14.dp))
                                JvmSettingLegend(slices = storageSlices)
                                Spacer(modifier = Modifier.height(16.dp))
                                JvmSettingStorageRow(
                                    icon = Res.drawable.music_note_24px,
                                    title = stringResource(Res.string.audio_cache),
                                    description = stringResource(
                                        Res.string.settings_storage_audio_desc,
                                        memoryManagementViewModel.musicCacheSize
                                    ),
                                    action = stringResource(Res.string.settings_action_clean),
                                    onClick = { memoryManagementViewModel.clearMusicCache() }
                                )
                                JvmSettingStorageRow(
                                    icon = Res.drawable.av_timer_24px,
                                    title = stringResource(Res.string.temporary_cache),
                                    description = stringResource(
                                        Res.string.settings_storage_temp_desc,
                                        memoryManagementViewModel.cacheSize
                                    ),
                                    action = stringResource(Res.string.settings_action_clean),
                                    onClick = { memoryManagementViewModel.clearAllCache() }
                                )
                                JvmSettingStorageRow(
                                    icon = Res.drawable.folder_managed_24px,
                                    title = stringResource(Res.string.database_data),
                                    description = stringResource(
                                        Res.string.settings_storage_database_desc,
                                        memoryManagementViewModel.databaseSize
                                    ),
                                    action = stringResource(Res.string.settings_action_rebuild),
                                    onClick = {
                                        confirmClearDatabase {
                                            memoryManagementViewModel.clearDatabaseData()
                                        }
                                    }
                                )
                                JvmSettingStorageRow(
                                    icon = Res.drawable.queue_music_24px,
                                    title = stringResource(Res.string.essential_data),
                                    description = stringResource(
                                        Res.string.settings_storage_essential_desc,
                                        memoryManagementViewModel.appDataSize
                                    ),
                                    action = stringResource(Res.string.settings_action_keep),
                                    onClick = null
                                )
                            }

                            // 快速清理：高频动作集中成四张卡片，底部给出一键清理入口。
                            JvmSettingPlainGroup(
                                title = stringResource(Res.string.settings_group_quick_clean),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // 设计稿 .quick 直接铺满分组宽度，卡片自身负责内边距。
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        JvmSettingQuickTile(
                                            name = stringResource(Res.string.settings_quick_temp),
                                            sizeText = memoryManagementViewModel.cacheSize,
                                            onClick = { memoryManagementViewModel.clearAllCache() }
                                        )
                                        JvmSettingQuickTile(
                                            name = stringResource(Res.string.settings_quick_audio),
                                            sizeText = memoryManagementViewModel.musicCacheSize,
                                            onClick = { memoryManagementViewModel.clearMusicCache() }
                                        )
                                        JvmSettingQuickTile(
                                            name = stringResource(Res.string.settings_quick_database),
                                            sizeText = memoryManagementViewModel.databaseSize,
                                            onClick = {
                                                confirmClearDatabase {
                                                    memoryManagementViewModel.clearDatabaseData()
                                                }
                                            }
                                        )
                                        JvmSettingQuickTile(
                                            name = stringResource(Res.string.settings_quick_all),
                                            sizeText = cleanableLabel,
                                            onClick = { confirmClearAllCache() }
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(14.dp))
                                    // 设计稿的 .quick-foot 使用虚线分隔。
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .drawBehind {
                                                drawLine(
                                                    color = dashedLineColor,
                                                    start = Offset(0f, size.height / 2f),
                                                    end = Offset(size.width, size.height / 2f),
                                                    strokeWidth = 1.dp.toPx(),
                                                    pathEffect = PathEffect.dashPathEffect(
                                                        floatArrayOf(4.dp.toPx(), 4.dp.toPx())
                                                    )
                                                )
                                            }
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        XyTextSub(
                                            modifier = Modifier.weight(1f),
                                            text = stringResource(Res.string.settings_quick_note),
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2
                                        )
                                        JvmSettingButton(
                                            text = stringResource(Res.string.settings_action_clean_all, cleanableLabel),
                                            primary = true,
                                            onClick = { confirmClearAllCache() }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item(key = "interface") {
                    JvmSettingSectionPage {
                        JvmSettingGroupGrid {
                            JvmSettingPlainGroup(
                                title = stringResource(Res.string.settings_group_theme),
                                modifier = Modifier.weight(1f)
                            ) {
                                JvmSettingRow(
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

                            JvmSettingPlainGroup(
                                title = stringResource(Res.string.settings_group_background),
                                modifier = Modifier.weight(1f)
                            ) {
                                JvmSettingRow(
                                    icon = Res.drawable.album_24px,
                                    title = stringResource(Res.string.background_image_setting),
                                    description = imageFilePath
                                        ?.takeIf { it.isNotBlank() }
                                        ?.substringAfterLast(File.separator)
                                        ?: stringResource(Res.string.settings_background_desc),
                                    onClick = { navigator.navigate(SetBackgroundImage) },
                                    trailing = {
                                        JvmSettingChip(
                                            text = if (imageFilePath.isNullOrBlank()) {
                                                stringResource(Res.string.settings_value_unset)
                                            } else {
                                                stringResource(Res.string.settings_background_replace)
                                            }
                                        )
                                        JvmSettingChevron()
                                    }
                                )
                            }
                        }
                    }
                }

                item(key = "language") {
                    JvmSettingSectionPage {
                        JvmSettingGroupGrid {
                            JvmSettingPlainGroup(
                                title = stringResource(Res.string.settings_group_language),
                                modifier = Modifier.weight(1f)
                            ) {
                                // 设计稿搜索框直接对齐分组左边缘，列表项自身保留内缩。
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    JvmSettingSearchField(
                                        value = languageQuery,
                                        hint = stringResource(Res.string.settings_language_search_hint),
                                        onValueChange = { languageQuery = it }
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                }

                                // 跟随系统与语言列表同属设计稿的 .pick 列表，条目间距 2px。
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    // 跟随系统选项在搜索为空或命中关键词时展示。
                                    val followSystemTitle = stringResource(Res.string.system)
                                    if (languageQuery.isBlank() ||
                                        followSystemTitle.contains(languageQuery, ignoreCase = true)
                                    ) {
                                        JvmSettingPickRow(
                                            title = followSystemTitle,
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
                                    }

                                    // 语言列表按名称与语言代码过滤，未启用的语言保持禁用态。
                                    LanguageType.entries
                                        .filter { language ->
                                            languageQuery.isBlank() ||
                                                language.languageName.contains(languageQuery, ignoreCase = true) ||
                                                language.languageCode.contains(languageQuery, ignoreCase = true)
                                        }
                                        .forEach { language ->
                                            JvmSettingPickRow(
                                                title = language.languageName,
                                                description = language.languageNativeName(),
                                                // 设计稿把语言代码放在行尾小标签里。
                                                tag = language.languageCode,
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
                            // 设计稿语言分组只占左列，右列留白。
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                item(key = "custom") {
                    JvmSettingSectionPage {
                        JvmSettingGroupGrid {
                            JvmSettingPlainGroup(
                                title = stringResource(Res.string.settings_group_lyrics_api),
                                modifier = Modifier.weight(1f)
                            ) {
                                // 设计稿表单字段直接对齐分组左边缘。
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    JvmSettingFormField(
                                        label = stringResource(Res.string.settings_field_api_endpoint),
                                        value = customLyricsViewModel.customLrcSingleApiValue,
                                        placeholder = "https://lyrics.example.com/api",
                                        onValueChange = customLyricsViewModel::updateCustomLrcSingleApi
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    JvmSettingFormField(
                                        label = stringResource(Res.string.settings_field_api_token),
                                        value = customLyricsViewModel.customLrcApiAuthValue,
                                        placeholder = stringResource(Res.string.settings_field_api_token_hint),
                                        onValueChange = customLyricsViewModel::updateCustomLrcApiAuth
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        JvmSettingButton(
                                            text = stringResource(Res.string.settings_action_save),
                                            primary = true,
                                            onClick = { customLyricsViewModel.saveSettings() }
                                        )
                                        JvmSettingButton(
                                            text = stringResource(Res.string.settings_action_restore),
                                            onClick = {
                                                customLyricsViewModel.updateCustomLrcSingleApi("")
                                                customLyricsViewModel.updateCustomLrcApiAuth("")
                                                customLyricsViewModel.saveSettings()
                                            }
                                        )
                                    }
                                    JvmSettingNoteText(
                                        text = stringResource(Res.string.settings_note_lyrics_api)
                                    )
                                }
                            }

                            JvmSettingPlainGroup(
                                title = stringResource(Res.string.settings_group_cover_api),
                                modifier = Modifier.weight(1f)
                            ) {
                                // 设计稿表单字段直接对齐分组左边缘。
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    JvmSettingFormField(
                                        label = stringResource(Res.string.settings_field_api_endpoint),
                                        value = customLyricsViewModel.customCoverApiValue,
                                        placeholder = "https://cover.example.com/api",
                                        onValueChange = customLyricsViewModel::updateCustomCoverApi
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    JvmSettingFormField(
                                        label = stringResource(Res.string.settings_field_api_token),
                                        value = customLyricsViewModel.customLrcApiAuthValue,
                                        placeholder = stringResource(Res.string.settings_field_api_token_hint),
                                        onValueChange = customLyricsViewModel::updateCustomLrcApiAuth
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        JvmSettingButton(
                                            text = stringResource(Res.string.settings_action_save),
                                            primary = true,
                                            onClick = { customLyricsViewModel.saveSettings() }
                                        )
                                        JvmSettingButton(
                                            text = stringResource(Res.string.settings_action_restore),
                                            onClick = {
                                                customLyricsViewModel.updateCustomCoverApi("")
                                                customLyricsViewModel.saveSettings()
                                            }
                                        )
                                    }
                                    JvmSettingNoteText(
                                        text = stringResource(Res.string.settings_note_cover_api)
                                    )
                                }
                            }

                            JvmSettingPlainGroup(
                                title = stringResource(Res.string.settings_group_common),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                JvmSettingSwitchRow(
                                    icon = Res.drawable.settings_24px,
                                    title = stringResource(Res.string.prioritize_music_service_api),
                                    description = stringResource(Res.string.settings_prioritize_api_desc),
                                    checked = settings.ifPriorityMusicApi,
                                    onCheckedChange = { checked ->
                                        // 同步自定义接口页的本地状态，避免之后保存接口时覆盖这里的开关值。
                                        customLyricsViewModel.updateIfPriorityMusicApi(checked)
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
                    JvmSettingSectionPage {
                        JvmSettingAboutHero(versionText = versionLabel)
                        JvmSettingReadouts(items = aboutReadouts)
                        Spacer(modifier = Modifier.height(30.dp))
                        JvmSettingGroupGrid {
                            JvmSettingPlainGroup(
                                title = stringResource(Res.string.settings_group_update),
                                modifier = Modifier.weight(1f)
                            ) {
                                JvmSettingNavigationRow(
                                    icon = Res.drawable.download_24px,
                                    title = checkUpdateTitle,
                                    description = stringResource(Res.string.settings_row_check_update_desc),
                                    value = stringResource(Res.string.settings_value_latest),
                                    onClick = {
                                        showMessageDialog(checkUpdateTitle, notImplementedText)
                                    }
                                )
                            }

                            JvmSettingPlainGroup(
                                title = stringResource(Res.string.settings_group_links),
                                modifier = Modifier.weight(1f)
                            ) {
                                JvmSettingRow(
                                    icon = Res.drawable.http_24px,
                                    title = stringResource(Res.string.settings_row_repo),
                                    description = "github.com/xianyvbang/XyMusic",
                                    descriptionMono = true,
                                    onClick = { uriHandler.openUri(JvmSettingRepositoryUrl) },
                                    trailing = { JvmSettingChevron() }
                                )
                                JvmSettingRow(
                                    icon = Res.drawable.info_24px,
                                    title = licenseTitle,
                                    description = licenseName,
                                    onClick = {
                                        showMessageDialog(licenseTitle, licenseName)
                                    },
                                    trailing = { JvmSettingChevron() }
                                )
                                JvmSettingRow(
                                    icon = Res.drawable.settings_24px,
                                    title = authorTitle,
                                    description = authorName,
                                    onClick = {
                                        showMessageDialog(authorTitle, authorName)
                                    },
                                    trailing = { JvmSettingChevron() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** 设置页下载并发数可选项。 */
private val JvmDownloadConcurrencyOptions = listOf("1", "3", "5")

/**
 * 存储占用条中的一段数据。
 *
 * @param label 图例名称。
 * @param bytes 占用字节数。
 * @param color 分布条与图例使用的颜色。
 */
private data class JvmStorageSlice(
    val label: String,
    val bytes: Long,
    val color: Color,
)

/**
 * 设置页单个滚动分区。
 *
 * 设计稿中分区之间只有「34px 间距 + 细分隔线 + 30px 内间距」，没有标题、编号或说明，
 * 因此这里只负责分区间距与滚动容器，不额外渲染分区头。
 *
 * @param isFirst 是否为第一个分区，首个分区不绘制上分隔线。
 * @param content 分区内的设置分组。
 */
@Composable
private fun JvmSettingSectionPage(
    isFirst: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val lineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.09f)

    Column(modifier = Modifier.fillMaxWidth()) {
        if (isFirst) {
            Spacer(modifier = Modifier.height(JvmSettingContentTopPadding))
        } else {
            // 设计稿中相邻分区之间是「间距 + 细分隔线 + 内间距」的组合。
            Spacer(modifier = Modifier.height(JvmSettingSectionTopGap))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(lineColor)
            )
            Spacer(modifier = Modifier.height(JvmSettingSectionInnerGap))
        }
        content()
    }
}

/**
 * 设计稿中的无卡片分组：分组标题 + 直接排列的设置行。
 *
 * @param title 分组小标题。
 * @param modifier 分组修饰符，两列布局时传入权重。
 * @param content 分组内容。
 */
@Composable
private fun JvmSettingPlainGroup(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(11.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        RoundedCornerShape(2.dp)
                    )
            )
            XyTextSub(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
        Spacer(modifier = Modifier.height(9.dp))
        Column(content = content)
    }
}

/**
 * 设置页两列分组布局，列间距与行间距严格对齐设计稿的 40px / 30px。
 *
 * @param content 两列分组内容。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun JvmSettingGroupGrid(content: @Composable FlowRowScope.() -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(40.dp),
        verticalArrangement = Arrangement.spacedBy(30.dp),
        maxItemsInEachRow = 2,
        content = content
    )
}

/**
 * 设置行基础布局。
 *
 * @param title 行标题。
 * @param description 行说明。
 * @param icon 行首图标，可为空。
 * @param iconActive 图标是否使用强调色，设计稿中开关打开时图标会点亮。
 * @param descriptionMono 说明是否使用等宽字体，路径等读数使用。
 * @param enabled 是否启用。
 * @param onClick 点击回调，为空时不可点击。
 * @param trailing 行尾内容。
 */
@Composable
private fun JvmSettingRow(
    title: String,
    description: String,
    icon: DrawableResource? = null,
    iconActive: Boolean = false,
    descriptionMono: Boolean = false,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val contentAlpha = if (enabled) 1f else 0.45f
    val hoverColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = JvmSettingRowMinHeight)
            .clip(RoundedCornerShape(JvmSettingRowCorner))
            .background(if (hovered && enabled) hoverColor else Color.Transparent)
            .hoverable(interactionSource = interactionSource, enabled = enabled)
            .then(
                if (enabled && onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            )
    ) {
        // hover 时左侧出现强调色竖条，复刻设计稿的 .row:hover::before。
        if (hovered && enabled) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(2.dp)
                    .fillMaxHeight(0.64f)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(JvmSettingRowPadding),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(JvmSettingRowIconSize)
                        .clip(RoundedCornerShape(7.dp))
                        .background(
                            if (iconActive) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.045f)
                            }
                        )
                        .border(
                            width = 1.dp,
                            color = if (iconActive) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.09f)
                            },
                            shape = RoundedCornerShape(7.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        modifier = Modifier.size(15.dp),
                        painter = painterResource(icon),
                        contentDescription = null,
                        tint = if (iconActive) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha)
                        }
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                XyText(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (description.isNotBlank()) {
                    XyTextSub(
                        text = description,
                        style = if (descriptionMono) {
                            MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.5.sp
                            )
                        } else {
                            MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp)
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
                        maxLines = 1
                    )
                }
            }

            Row(
                // 设计稿 .row-tail 额外带有 8px 左边距，与行内容的 12px 间距叠加。
                modifier = Modifier.padding(start = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = trailing
            )
        }
    }
}

/**
 * 开关设置行。
 *
 * @param icon 行首图标。
 * @param title 行标题。
 * @param description 行说明。
 * @param checked 当前开关状态。
 * @param onCheckedChange 开关变化回调。
 */
@Composable
private fun JvmSettingSwitchRow(
    icon: DrawableResource,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    JvmSettingRow(
        icon = icon,
        iconActive = checked,
        title = title,
        description = description,
        onClick = { onCheckedChange(!checked) },
        trailing = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    uncheckedBorderColor = Color.Transparent,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                    uncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f)
                )
            )
        }
    )
}

/**
 * 跳转设置行，行尾展示当前值并带右箭头。
 *
 * @param icon 行首图标。
 * @param title 行标题。
 * @param description 行说明。
 * @param value 行尾展示值。
 * @param onClick 点击回调。
 */
@Composable
private fun JvmSettingNavigationRow(
    icon: DrawableResource,
    title: String,
    description: String,
    value: String,
    onClick: () -> Unit,
) {
    JvmSettingRow(
        icon = icon,
        title = title,
        description = description,
        onClick = onClick,
        trailing = {
            JvmSettingChip(text = value)
            JvmSettingChevron()
        }
    )
}

/**
 * 单选设置行，用于音质、缓存上限与语言列表。
 *
 * @param title 选项标题。
 * @param description 选项说明。
 * @param tag 行尾等宽小标签。
 * @param selected 是否为当前选中项。
 * @param enabled 是否允许点击。
 * @param onClick 点击回调。
 */
@Composable
private fun JvmSettingPickRow(
    title: String,
    description: String,
    tag: String? = null,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val accentColor = MaterialTheme.colorScheme.primary
    val contentAlpha = if (enabled) 1f else 0.45f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 54.dp)
            .clip(RoundedCornerShape(JvmSettingRowCorner))
            .background(
                if (hovered && enabled) {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                } else {
                    Color.Transparent
                }
            )
            .hoverable(interactionSource = interactionSource, enabled = enabled)
            .then(
                if (enabled) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 12.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.spacedBy(13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 单选圆点：选中时填充强调色内圆。
        Box(
            modifier = Modifier
                .size(15.dp)
                .border(
                    width = 1.dp,
                    color = if (selected) {
                        accentColor.copy(alpha = contentAlpha)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f * contentAlpha)
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .background(accentColor.copy(alpha = contentAlpha), CircleShape)
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            XyText(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (description.isNotBlank()) {
                XyTextSub(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
                    maxLines = 1
                )
            }
        }

        if (!tag.isNullOrBlank()) {
            JvmSettingChip(text = tag, compact = true)
        }
    }
}

/**
 * 存储占用中的单行数据。
 *
 * @param icon 行首图标。
 * @param title 数据名称。
 * @param description 占用与用途说明。
 * @param action 行尾动作文案。
 * @param onClick 点击动作，为空时表示不可清理。
 */
@Composable
private fun JvmSettingStorageRow(
    icon: DrawableResource,
    title: String,
    description: String,
    action: String,
    onClick: (() -> Unit)?,
) {
    JvmSettingRow(
        icon = icon,
        title = title,
        description = description,
        onClick = onClick,
        trailing = {
            // 设计稿中动作读数统一使用中性 chipval，可点击时再追加右箭头。
            JvmSettingChip(text = action)
            if (onClick != null) {
                JvmSettingChevron()
            }
        }
    )
}

/**
 * 行尾等宽数值标签。
 *
 * 设计稿里行尾读数使用 chipval（10sp、3/8 内边距、5px 圆角），
 * 单选行尾的标记使用更紧凑的 tag（9.5sp、2/6 内边距、4px 圆角）。
 *
 * @param text 标签文案。
 * @param accent 是否使用强调色。
 * @param compact 是否使用紧凑的 tag 样式。
 */
@Composable
private fun JvmSettingChip(text: String, accent: Boolean = false, compact: Boolean = false) {
    val accentColor = MaterialTheme.colorScheme.primary
    val corner = if (compact) 4.dp else 5.dp
    Box(
        modifier = Modifier
            // 设计稿 .chipval 的最大宽度为 190px，超长读数直接省略。
            .widthIn(max = 190.dp)
            .clip(RoundedCornerShape(corner))
            .background(
                if (accent) accentColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
            )
            .border(
                width = 1.dp,
                color = if (accent) {
                    accentColor.copy(alpha = 0.32f)
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
                },
                shape = RoundedCornerShape(corner)
            )
            .padding(
                horizontal = if (compact) 6.dp else 8.dp,
                vertical = if (compact) 2.dp else 3.dp
            )
    ) {
        XyTextSub(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = if (compact) 9.5.sp else 10.sp,
                letterSpacing = if (compact) 0.95.sp else 0.sp
            ),
            color = if (accent) accentColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.86f),
            maxLines = 1
        )
    }
}

/** 行尾右箭头。 */
@Composable
private fun JvmSettingChevron() {
    Icon(
        modifier = Modifier.size(14.dp),
        painter = painterResource(Res.drawable.chevron_right_24px),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/**
 * 分组内的说明文本，左侧带强调色短线。
 *
 * @param text 说明文案。
 */
@Composable
private fun JvmSettingNoteText(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // 设计稿 .note 上下各 8px 内边距，缓存目录分组额外把上边距提到 10px。
            .padding(top = 10.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 3.dp)
                .width(2.dp)
                .height(13.dp)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                    RoundedCornerShape(1.dp)
                )
        )
        XyTextSub(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, lineHeight = 19.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 3
        )
    }
}

/**
 * 表单字段标题，使用等宽小字。
 *
 * @param text 标题文案。
 */
@Composable
private fun JvmSettingFieldLabel(text: String) {
    XyTextSub(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(
            fontFamily = FontFamily.Monospace,
            fontSize = 9.5.sp,
            letterSpacing = 1.2.sp
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1
    )
}

/**
 * 只读路径输入框，点击可复制路径。
 *
 * @param path 路径文本。
 * @param modifier 外层修饰符。
 * @param onClick 点击回调。
 */
@Composable
private fun JvmSettingPathField(
    path: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .height(JvmSettingFieldHeight)
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.045f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        XyTextSub(
            text = path.ifBlank { "-" },
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}

/**
 * 可编辑表单字段。
 *
 * @param label 字段标题。
 * @param value 当前值。
 * @param onValueChange 输入变化回调。
 * @param placeholder 占位文案。
 */
@Composable
private fun JvmSettingFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String? = null,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        JvmSettingFieldLabel(text = label)
        Spacer(modifier = Modifier.height(7.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(JvmSettingFieldHeight)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.045f))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
                    shape = RoundedCornerShape(6.dp)
                )
        ) {
            JvmSettingTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = placeholder
            )
        }
    }
}

/**
 * 设置页单行输入框。
 *
 * 设计稿的输入框固定 34px 高、左右 10px 内边距，通用 XyEdit 自带较大的上下内边距，
 * 放进 34px 容器会把文字压扁，因此这里直接用 BasicTextField 还原设计稿尺寸。
 *
 * @param value 当前文本。
 * @param onValueChange 文本变化回调。
 * @param modifier 外层修饰符。
 * @param placeholder 占位文案。
 */
@Composable
private fun JvmSettingTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty() && placeholder != null) {
                    XyTextSub(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                        maxLines = 1
                    )
                }
                innerTextField()
            }
        }
    )
}

/**
 * 语言搜索输入框。
 *
 * @param value 当前输入值。
 * @param hint 占位文案。
 * @param onValueChange 输入变化回调。
 */
@Composable
private fun JvmSettingSearchField(
    value: String,
    hint: String,
    onValueChange: (String) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(JvmSettingFieldHeight)
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.045f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
                shape = RoundedCornerShape(6.dp)
            )
    ) {
        JvmSettingTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = hint
        )
    }
}

/**
 * 表单按钮。
 *
 * @param text 按钮文案。
 * @param primary 是否使用强调色实心样式。
 * @param enabled 是否启用。
 * @param onClick 点击回调。
 */
@Composable
private fun JvmSettingButton(
    text: String,
    primary: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val accentColor = MaterialTheme.colorScheme.primary
    val borderColor = if (primary) {
        accentColor
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    }
    Box(
        modifier = Modifier
            .height(JvmSettingFieldHeight)
            .clip(RoundedCornerShape(6.dp))
            .background(if (primary) accentColor else Color.Transparent)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(6.dp))
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        XyText(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = if (primary) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else 0.4f)
            },
            maxLines = 1
        )
    }
}

/**
 * 存储占用分布条。
 *
 * @param slices 各段数据。
 */
@Composable
private fun JvmSettingStackBar(slices: List<JvmStorageSlice>) {
    val totalBytes = slices.sumOf { it.bytes }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                shape = RoundedCornerShape(5.dp)
            )
    ) {
        if (totalBytes > 0L) {
            slices.filter { it.bytes > 0L }.forEach { slice ->
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(slice.bytes.toFloat() / totalBytes.toFloat())
                        .background(slice.color)
                )
            }
        }
    }
}

/**
 * 存储占用图例。
 *
 * @param slices 各段数据。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun JvmSettingLegend(slices: List<JvmStorageSlice>) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        slices.forEach { slice ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(slice.color, RoundedCornerShape(2.dp))
                )
                XyTextSub(
                    text = "${slice.label} ${formatBytes(slice.bytes, withSpace = true)}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * 快速清理卡片。
 *
 * @param name 动作名称。
 * @param sizeText 该动作可释放的容量读数。
 * @param onClick 点击回调。
 */
@Composable
private fun RowScope.JvmSettingQuickTile(
    name: String,
    sizeText: String,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val accentColor = MaterialTheme.colorScheme.primary
    // 设计稿 .quick-tile:hover 会上移 2px，这里用位移动画复刻。
    val hoverLift by animateDpAsState(if (hovered) (-2).dp else 0.dp)

    Column(
        modifier = Modifier
            .weight(1f)
            .offset(y = hoverLift)
            .clip(RoundedCornerShape(9.dp))
            .background(
                if (hovered) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f) else Color.Transparent
            )
            .border(
                width = 1.dp,
                color = if (hovered) accentColor.copy(alpha = 0.45f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
                shape = RoundedCornerShape(9.dp)
            )
            .hoverable(interactionSource = interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        XyText(
            text = name,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
        XyTextSub(
            text = sizeText,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                letterSpacing = 1.2.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

/**
 * 关于页品牌区。
 *
 * @param versionText 版本信息文案。
 */
@Composable
private fun JvmSettingAboutHero(versionText: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 26.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                // 设计稿 logo 带有强调色投影，桌面端用颜色阴影近似还原。
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                )
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            Color(0xFF7A4BFF)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            XyText(
                text = "X",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = MaterialTheme.colorScheme.onPrimary,
                maxLines = 1
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            XyText(
                text = "XyMusic",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            XyTextSub(
                text = versionText,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.5.sp,
                    letterSpacing = 1.2.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

/**
 * 关于页等宽读数行。
 *
 * @param items 标签与数值对。
 */
@Composable
private fun JvmSettingReadouts(items: List<Pair<String, String>>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, (label, value) ->
            if (index > 0) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(34.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.09f))
                )
            }
            Column(
                modifier = Modifier.padding(
                    start = if (index == 0) 0.dp else 16.dp,
                    end = if (index == items.lastIndex) 0.dp else 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                XyTextSub(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.5.sp,
                        letterSpacing = 1.4.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                XyText(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * 语言的英文名称，用于语言列表的说明行，与设计稿文案保持一致。
 */
@Composable
private fun LanguageType.languageNativeName(): String {
    return when (this) {
        LanguageType.ZH_CN -> stringResource(Res.string.settings_language_native_zh_cn)
        LanguageType.ZH_TW -> stringResource(Res.string.settings_language_native_zh_tw)
        LanguageType.EN -> stringResource(Res.string.settings_language_native_en)
    }
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

/**
 * 缓存上限档位的说明文案，与设计稿的容量说明一一对应。
 */
@Composable
private fun CacheUpperLimitEnum.cacheLimitDescription(): String {
    return when (this) {
        CacheUpperLimitEnum.ThreeG -> stringResource(Res.string.settings_cache_desc_2g)
        CacheUpperLimitEnum.FourG -> stringResource(Res.string.settings_cache_desc_4g)
        CacheUpperLimitEnum.EightG -> stringResource(Res.string.settings_cache_desc_8g)
        CacheUpperLimitEnum.SixteenG -> stringResource(Res.string.settings_cache_desc_16g)
        CacheUpperLimitEnum.ThirtyTwoG -> stringResource(Res.string.settings_cache_desc_32g)
        CacheUpperLimitEnum.SixtyFourG -> stringResource(Res.string.settings_cache_desc_64g)
        CacheUpperLimitEnum.OneHundredTwentyEightG -> stringResource(Res.string.settings_cache_desc_128g)
        else -> ""
    }
}

/**
 * 把 formatBytes 生成的容量文案还原成字节数。
 *
 * 存储占用条需要按真实比例绘制，而 ViewModel 只提供格式化后的容量文案，
 * 因此这里解析「数字 + 单位」的形式，解析失败时按 0 处理。
 *
 * @param label 形如 "1.23GB" 或 "512 KB" 的容量文案。
 * @return 解析出的字节数，失败时为 0。
 */
private fun jvmSizeLabelToBytes(label: String): Long {
    val normalized = label.trim().uppercase().replace(" ", "")
    if (normalized.isEmpty()) return 0L

    val numberPart = normalized.takeWhile { it.isDigit() || it == '.' }
    val unitPart = normalized.drop(numberPart.length)
    val number = numberPart.toDoubleOrNull() ?: return 0L
    val multiplier = when (unitPart) {
        "TB" -> 1024.0 * 1024 * 1024 * 1024
        "GB" -> 1024.0 * 1024 * 1024
        "MB" -> 1024.0 * 1024
        "KB" -> 1024.0
        else -> 1.0
    }
    return (number * multiplier).toLong().coerceAtLeast(0L)
}
