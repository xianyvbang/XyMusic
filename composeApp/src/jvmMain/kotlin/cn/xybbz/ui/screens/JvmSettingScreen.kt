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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import cn.xybbz.ui.components.JvmSettingActionGrid
import cn.xybbz.ui.components.JvmSettingDownloadRow
import cn.xybbz.ui.components.JvmSettingNavigationRow
import cn.xybbz.ui.components.JvmSettingNote
import cn.xybbz.ui.components.JvmSettingOverviewTile
import cn.xybbz.ui.components.JvmSettingPageHeader
import cn.xybbz.ui.components.JvmSettingPageScaffold
import cn.xybbz.ui.components.JvmSettingPathRow
import cn.xybbz.ui.components.JvmSettingSection
import cn.xybbz.ui.components.JvmSettingSwitchRow
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.album_24px
import xymusic_kmp.composeapp.generated.resources.album_playback_history
import xymusic_kmp.composeapp.generated.resources.allow_simultaneous_playback
import xymusic_kmp.composeapp.generated.resources.av_timer_24px
import xymusic_kmp.composeapp.generated.resources.broadcast_while_down
import xymusic_kmp.composeapp.generated.resources.cache_limit
import xymusic_kmp.composeapp.generated.resources.cache_location
import xymusic_kmp.composeapp.generated.resources.connection_management
import xymusic_kmp.composeapp.generated.resources.copy_success
import xymusic_kmp.composeapp.generated.resources.download_24px
import xymusic_kmp.composeapp.generated.resources.enabled_sync_play_progress
import xymusic_kmp.composeapp.generated.resources.folder_managed_24px
import xymusic_kmp.composeapp.generated.resources.http_24px
import xymusic_kmp.composeapp.generated.resources.music_note_24px
import xymusic_kmp.composeapp.generated.resources.online_music_quality
import xymusic_kmp.composeapp.generated.resources.poxy_config
import xymusic_kmp.composeapp.generated.resources.queue_music_24px
import xymusic_kmp.composeapp.generated.resources.settings
import xymusic_kmp.composeapp.generated.resources.signal_cellular_alt_24px
import xymusic_kmp.composeapp.generated.resources.song_cache_location
import xymusic_kmp.composeapp.generated.resources.storage_management
import xymusic_kmp.composeapp.generated.resources.volume_up_24px

// 设置页整体内容的最大宽度，避免桌面宽屏上阅读线过长。
private val JvmSettingContentMaxWidth = 1080.dp
// 概览区从单列切换为三列卡片的最小宽度。
private val JvmSettingOverviewGridMinWidth = 760.dp
// 主设置区从单列切换为左右两栏的最小宽度。
private val JvmSettingLayoutGridMinWidth = 860.dp
// 主设置区左右两栏的紧凑宽度上限，用来避免 item 横向过宽。
private val JvmSettingMainContentMaxWidth = 960.dp

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
        ?.message
        ?: settings.cacheUpperLimit.name
    val selectedQuality = TranscodeAudioBitRateType
        .getTranscodeAudioBitRate(settings.wifiNetworkAudioBitRate)
        .audioBitRateStr
    val dataSourceLabel = settings.dataSourceType?.title ?: "未连接"

    JvmSettingPageScaffold(contentMaxWidth = JvmSettingContentMaxWidth) {
        JvmSettingPageHeader(
            title = stringResource(Res.string.settings),
            description = "把桌面端常用配置集中为更可扫读的设置中心：播放缓存、连接管理、下载队列、界面语言和扩展能力都保留当前入口。",
            contentMaxWidth = JvmSettingMainContentMaxWidth,
        ) {
            JvmSettingStatusCard(
                // 状态卡保持紧凑宽度，避免挤占标题说明的阅读空间。
                modifier = Modifier.widthIn(min = 248.dp),
                dataSourceLabel = dataSourceLabel,
                selectedQuality = selectedQuality,
                maxConcurrentDownloads = settings.maxConcurrentDownloads,
            )
        }

        JvmSettingOverview(
            settings = settings,
            cacheLimitLabel = cacheLimitLabel,
            onStorageClick = {
                navigator.navigate(MemoryManagement)
            },
        )

        JvmSettingMainLayout(
            leftContent = {
                JvmSettingSection(
                    title = "播放与缓存",
                    subtitle = "控制在线播放策略、缓存位置和跨设备播放行为。",
                    badge = "核心",
                ) {
                    JvmSettingSwitchRow(
                        icon = Res.drawable.download_24px,
                        title = stringResource(Res.string.broadcast_while_down),
                        description = "播放时缓存音频资源，弱网重播更稳定。",
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
                            description = "设置播放缓存最大占用空间。",
                            value = cacheLimitLabel,
                            onClick = {
                                navigator.navigate(CacheLimit)
                            }
                        )
                    }

                    JvmSettingNavigationRow(
                        icon = Res.drawable.music_note_24px,
                        title = stringResource(Res.string.online_music_quality),
                        description = "选择桌面端在线音频品质与转码格式。",
                        value = "$selectedQuality · ${settings.transcodeFormat.uppercase()}",
                        onClick = {
                            navigator.navigate(StreamingQuality)
                        }
                    )

                    JvmSettingSwitchRow(
                        icon = Res.drawable.album_24px,
                        title = stringResource(Res.string.album_playback_history),
                        description = "记录专辑播放进度，便于下次继续。",
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
                        description = "保留系统音频焦点，不主动打断其他声音。",
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
                        description = "向服务端同步当前播放位置。",
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
                    title = "下载与存储",
                    subtitle = "下载并发、歌曲缓存路径与本地空间管理。",
                    badge = "本机",
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
                        description = "查看缓存占用并清理本地文件。",
                        value = "打开存储管理",
                        onClick = {
                            navigator.navigate(MemoryManagement)
                        }
                    )
                }
            },
            rightContent = {
                JvmSettingSection(
                    title = "连接",
                    subtitle = "管理音乐服务地址和当前连接。",
                    badge = "在线",
                ) {
                    JvmSettingNavigationRow(
                        icon = Res.drawable.http_24px,
                        title = stringResource(Res.string.connection_management),
                        description = "切换或编辑 Jellyfin、Navidrome 等数据源。",
                        value = dataSourceLabel,
                        onClick = {
                            navigator.navigate(ConnectionManagement)
                        }
                    )

                    JvmSettingNavigationRow(
                        icon = Res.drawable.signal_cellular_alt_24px,
                        title = stringResource(Res.string.poxy_config),
                        description = "配置服务访问代理和网络转发。",
                        value = "网络",
                        onClick = {
                            navigator.navigate(ProxyConfig)
                        }
                    )
                }

                JvmSettingSection(
                    title = "通用",
                    subtitle = "界面、语言、自定义资源和应用信息。",
                    badge = "偏好",
                    contentContainerColor = Color.Transparent,
                    contentContainerBorderColor = Color.Transparent,
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

                    JvmSettingNote(
                        text = "设置项保持原有路由和数据写入行为，桌面端只调整信息架构和视觉密度。"
                    )
                }
            }
        )
    }
}

/**
 * 设置主体的响应式布局。
 *
 * 宽屏时保持预览稿的左侧主栏 + 右侧侧栏结构，窄屏时交给 FlowRow 自动换成单列。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun JvmSettingMainLayout(
    leftContent: @Composable ColumnScope.() -> Unit,
    rightContent: @Composable ColumnScope.() -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val gap = XyTheme.dimens.outerHorizontalPadding
        // 可用宽度足够时才允许左右双栏，否则两个区域都按整行宽度排布。
        val useTwoColumns = maxWidth >= JvmSettingLayoutGridMinWidth
        // 双栏整体不铺满所有可用空间，让每个设置分区保持更紧凑的阅读宽度。
        val layoutWidth = minOf(maxWidth, JvmSettingMainContentMaxWidth)
        val contentWidth = if (useTwoColumns) {
            // 双栏时需要预留两栏之间的间距，否则两栏总宽会超过 FlowRow 一行容量。
            layoutWidth - gap
        } else {
            layoutWidth
        }
        // 沿用预览稿左栏更宽、右栏更窄的信息层级比例。
        val leftWeight = 1.45f
        val rightWeight = 0.95f
        // 左栏承载播放、下载等高频设置，双栏时按更大比例分配宽度。
        val leftWidth = if (useTwoColumns) {
            contentWidth * (leftWeight / (leftWeight + rightWeight))
        } else {
            // 单列时让左侧内容独占整行，交由 FlowRow 放在右侧内容之前。
            maxWidth
        }
        // 右栏承载连接、通用入口，宽度使用剩余空间保证两栏总宽精确。
        val rightWidth = if (useTwoColumns) {
            contentWidth - leftWidth
        } else {
            // 单列时右侧内容也独占整行，形成纵向阅读顺序。
            maxWidth
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            // 子项宽度被收紧后居中排列，避免在宽屏上贴左显得失衡。
            horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding * 2),
            itemVerticalAlignment = Alignment.Top,
        ) {
            JvmSettingStack(
                modifier = Modifier.width(leftWidth),
                content = leftContent
            )
            JvmSettingStack(
                modifier = Modifier.width(rightWidth),
                content = rightContent
            )
        }
    }
}

/**
 * 主设置区中的纵向分组容器，用于承载左栏或右栏的多个 section。
 */
@Composable
private fun JvmSettingStack(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding * 2),
        content = content
    )
}

@Composable
private fun JvmSettingStatusCard(
    modifier: Modifier = Modifier,
    dataSourceLabel: String,
    selectedQuality: String,
    maxConcurrentDownloads: Int,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
        )
    ) {
        Column(
            modifier = Modifier.padding(XyTheme.dimens.outerHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding)
        ) {
            JvmSettingStatusRow(label = "当前数据源", value = dataSourceLabel)
            JvmSettingStatusRow(label = "在线品质", value = selectedQuality)
            JvmSettingStatusRow(label = "下载并发", value = maxConcurrentDownloads.toString())
        }
    }
}

@Composable
private fun JvmSettingStatusRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            modifier = Modifier.padding(start = XyTheme.dimens.contentPadding),
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * 设置概览卡片区。
 *
 * 宽屏时保持三张卡片一行，窄屏时每张卡片独占一行。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun JvmSettingOverview(
    settings: XySettings,
    cacheLimitLabel: String,
    onStorageClick: () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val gap = XyTheme.dimens.contentPadding
        // 三张概览卡只有在足够宽时才同排，避免卡片文字被挤压。
        val useThreeColumns = maxWidth >= JvmSettingOverviewGridMinWidth
        // 概览区和下方主体使用同一个宽度基准，保证三张 item 总宽与下面内容对齐。
        val contentWidth = minOf(maxWidth, JvmSettingMainContentMaxWidth)
        val tileWidth = if (useThreeColumns) {
            // 三列时扣除两个横向间距，再平均分配每张卡片宽度。
            (contentWidth - gap * 2) / 3f
        } else {
            // 单列时铺满可用宽度，避免窄屏出现过窄卡片。
            maxWidth
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            // 卡片不再铺满整行时居中摆放，视觉上更接近预览稿。
            horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(gap),
        ) {
            JvmSettingOverviewTile(
                modifier = Modifier.width(tileWidth),
                icon = Res.drawable.download_24px,
                kicker = "播放缓存",
                value = if (settings.ifEnableEdgeDownload) "边下边播已开启" else "边下边播已关闭",
                sub = "缓存上限 · $cacheLimitLabel"
            )
            JvmSettingOverviewTile(
                modifier = Modifier.width(tileWidth),
                icon = Res.drawable.av_timer_24px,
                kicker = "播放同步",
                value = if (settings.ifEnableSyncPlayProgress) "进度同步已开启" else "进度同步已关闭",
                sub = if (settings.ifEnableAlbumHistory) "播放历史 · 专辑启用" else "播放历史 · 专辑关闭"
            )
            JvmSettingOverviewTile(
                modifier = Modifier.width(tileWidth),
                icon = Res.drawable.folder_managed_24px,
                kicker = "存储管理",
                value = "打开存储管理",
                sub = "真实占用在存储管理页查看",
                onClick = onStorageClick,
            )
        }
    }
}
