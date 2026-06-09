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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.utils.Log
import cn.xybbz.ui.components.AlertDialogObject
import cn.xybbz.ui.components.JvmSettingActionEntry
import cn.xybbz.ui.components.JvmSettingActionGrid
import cn.xybbz.ui.components.JvmSettingBaseRow
import cn.xybbz.ui.components.JvmSettingFlowRow
import cn.xybbz.ui.components.JvmSettingPageHeader
import cn.xybbz.ui.components.JvmSettingPageScaffold
import cn.xybbz.ui.components.JvmSettingSection
import cn.xybbz.ui.components.JvmSettingStatusCard
import cn.xybbz.ui.components.JvmSettingStatusCardItem
import cn.xybbz.ui.components.JvmSettingTwoPaneContent
import cn.xybbz.ui.components.rememberJvmFileKitDialogSettings
import cn.xybbz.ui.components.show
import cn.xybbz.ui.components.toExistingPlatformDirectoryOrNull
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyTextSub
import cn.xybbz.viewmodel.MemoryManagementViewModel
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.dialogs.openDirectoryPicker
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.*
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.adjust
import xymusic_kmp.composeapp.generated.resources.audio_cache
import xymusic_kmp.composeapp.generated.resources.audio_cache_description
import xymusic_kmp.composeapp.generated.resources.cache_path
import xymusic_kmp.composeapp.generated.resources.check_24px
import xymusic_kmp.composeapp.generated.resources.clear
import xymusic_kmp.composeapp.generated.resources.confirm_delete_database
import xymusic_kmp.composeapp.generated.resources.database_data
import xymusic_kmp.composeapp.generated.resources.database_data_description
import xymusic_kmp.composeapp.generated.resources.delete_24px
import xymusic_kmp.composeapp.generated.resources.download_24px
import xymusic_kmp.composeapp.generated.resources.essential_data
import xymusic_kmp.composeapp.generated.resources.essential_data_description
import xymusic_kmp.composeapp.generated.resources.folder_managed_24px
import xymusic_kmp.composeapp.generated.resources.info_24px
import xymusic_kmp.composeapp.generated.resources.music_note_24px
import xymusic_kmp.composeapp.generated.resources.queue_music_24px
import xymusic_kmp.composeapp.generated.resources.restore_default
import xymusic_kmp.composeapp.generated.resources.settings_24px
import xymusic_kmp.composeapp.generated.resources.storage_management
import xymusic_kmp.composeapp.generated.resources.temporary_cache
import xymusic_kmp.composeapp.generated.resources.temporary_cache_description
import xymusic_kmp.composeapp.generated.resources.warning
import java.io.File
import java.util.Locale
import kotlin.math.roundToInt

/**
 * JVM 桌面端存储管理页面。
 *
 * 页面负责把 [MemoryManagementViewModel] 中的缓存、数据库和应用数据占用转换成可扫读的卡片、
 * 进度条和操作入口；清理、恢复默认路径、选择缓存目录等实际行为仍由 ViewModel 处理。
 *
 * @param memoryManagementViewModel 存储管理 ViewModel，提供存储占用、缓存路径和清理操作。
 */
@Composable
fun JvmMemoryManagementScreen(
    memoryManagementViewModel: MemoryManagementViewModel = koinViewModel<MemoryManagementViewModel>()
) {
    // 页面用到的资源文案集中读取，避免同一资源在多个子组件中重复解析。
    val warning = stringResource(Res.string.warning)
    val storageManagementTitle = stringResource(Res.string.storage_management)
    val audioCacheTitle = stringResource(Res.string.audio_cache)
    val temporaryCacheTitle = stringResource(Res.string.temporary_cache)
    val databaseDataTitle = stringResource(Res.string.database_data)
    val essentialDataTitle = stringResource(Res.string.essential_data)
    val cachePathTitle = stringResource(Res.string.cache_path)
    val clearTitle = stringResource(Res.string.clear)
    val adjustTitle = stringResource(Res.string.adjust)
    val restoreDefaultTitle = stringResource(Res.string.restore_default)

    // 目录选择器是 suspend API，点击路径操作后需要使用页面协程启动。
    val coroutineScope = rememberCoroutineScope()
    // 绑定 JVM 主窗口的 FileKit 设置，避免系统目录选择弹窗出现在应用窗口后方。
    val cacheDirectoryDialogSettings = rememberJvmFileKitDialogSettings(stringResource(Res.string.jvm_memory_management_screen_text_01))

    // 打开缓存路径弹窗：弹窗内的“调整”会继续拉起系统目录选择器，“恢复默认”直接调用 ViewModel。
    fun showCachePathDialog() {
        showJvmCachePathDialog(
            title = cachePathTitle,
            cachePath = memoryManagementViewModel.musicCachePath,
            isDefaultPath = memoryManagementViewModel.isDefaultMusicCachePath,
            onChoosePath = {
                coroutineScope.launch {
                    // 用户取消选择时返回 null，此时不修改当前缓存路径。
                    chooseJvmCacheDirectory(
                        currentPath = memoryManagementViewModel.musicCachePath,
                        dialogSettings = cacheDirectoryDialogSettings,
                    )?.let { selectedPath ->
                        memoryManagementViewModel.changeMusicCacheDirectory(selectedPath)
                    }
                }
            },
            onRestoreDefault = {
                memoryManagementViewModel.restoreDefaultMusicCacheDirectory()
            }
        )
    }

    // 数据库清理属于高风险操作，必须先弹出确认框，再交给 ViewModel 清理数据库与相关状态。
    fun confirmClearDatabase() {
        AlertDialogObject(
            title = warning,
            content = {
                XyTextSub(
                    text = stringResource(Res.string.confirm_delete_database),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            ifWarning = true,
            onConfirmation = {
                memoryManagementViewModel.clearDatabaseData()
            }
        ).show()
    }

    // 将 ViewModel 暴露的字符串尺寸、可清理动作、颜色和图标统一整理为页面展示模型。
    val storageItems = listOf(
        JvmStorageDisplayItem(
            title = audioCacheTitle,
            description = stringResource(Res.string.audio_cache_description),
            sizeText = memoryManagementViewModel.musicCacheSize,
            sizeBytes = memoryManagementViewModel.musicCacheSize.toStorageBytes(),
            icon = Res.drawable.music_note_24px,
            color = MaterialTheme.colorScheme.primary,
            meta = stringResource(Res.string.jvm_memory_management_screen_text_02),
            onClear = { memoryManagementViewModel.clearMusicCache() },
        ),
        JvmStorageDisplayItem(
            title = temporaryCacheTitle,
            description = stringResource(Res.string.temporary_cache_description),
            sizeText = memoryManagementViewModel.cacheSize,
            sizeBytes = memoryManagementViewModel.cacheSize.toStorageBytes(),
            icon = Res.drawable.queue_music_24px,
            color = MaterialTheme.colorScheme.tertiary,
            meta = stringResource(Res.string.jvm_memory_management_screen_text_03),
            onClear = { memoryManagementViewModel.clearAllCache() },
        ),
        JvmStorageDisplayItem(
            title = databaseDataTitle,
            description = stringResource(Res.string.database_data_description),
            sizeText = memoryManagementViewModel.databaseSize,
            sizeBytes = memoryManagementViewModel.databaseSize.toStorageBytes(),
            icon = Res.drawable.settings_24px,
            color = MaterialTheme.colorScheme.error,
            meta = stringResource(Res.string.jvm_memory_management_screen_text_04),
            onClear = ::confirmClearDatabase,
        ),
        JvmStorageDisplayItem(
            title = essentialDataTitle,
            description = stringResource(Res.string.essential_data_description),
            sizeText = memoryManagementViewModel.appDataSize,
            sizeBytes = memoryManagementViewModel.appDataSize.toStorageBytes(),
            icon = Res.drawable.info_24px,
            color = MaterialTheme.colorScheme.secondary,
            meta = stringResource(Res.string.jvm_memory_management_screen_text_05),
        ),
    )
    // 总占用用于头部状态和空间分布进度条的比例计算。
    val totalBytes = storageItems.sumOf { it.sizeBytes }
    // 可清理占用只统计带清理动作的条目，核心应用数据不计入可清理容量。
    val clearableBytes = storageItems
        .filter { it.onClear != null }
        .sumOf { it.sizeBytes }
    // 将字节数重新格式化为更适合顶部状态卡展示的容量文本。
    val totalSizeText = totalBytes.toStorageLabel()
    val clearableSizeText = clearableBytes.toStorageLabel()
    // 路径状态用于提示当前是默认缓存目录还是用户自定义目录。
    val cachePathMode = if (memoryManagementViewModel.isDefaultMusicCachePath) stringResource(Res.string.jvm_memory_management_screen_text_06) else stringResource(Res.string.jvm_memory_management_screen_text_07)

    LaunchedEffect(Unit) {
        // 进入页面时刷新一次平台存储信息，避免显示上一次页面打开时的旧数据。
        memoryManagementViewModel.logStorageInfo()
    }

    JvmSettingPageScaffold(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = XyTheme.dimens.outerHorizontalPadding * 2,
            vertical = XyTheme.dimens.outerVerticalPadding * 3,
        )
    ) {
        JvmMemoryContent(
            title = storageManagementTitle,
            storageItems = storageItems,
            totalBytes = totalBytes,
            totalSizeText = totalSizeText,
            clearableSizeText = clearableSizeText,
            cachePath = memoryManagementViewModel.musicCachePath,
            cachePathMode = cachePathMode,
            isDefaultCachePath = memoryManagementViewModel.isDefaultMusicCachePath,
            clearTitle = clearTitle,
            adjustTitle = adjustTitle,
            restoreDefaultTitle = restoreDefaultTitle,
            onClearMusicCache = { memoryManagementViewModel.clearMusicCache() },
            onClearTemporaryCache = { memoryManagementViewModel.clearAllCache() },
            onClearDatabase = ::confirmClearDatabase,
            onOpenPath = ::showCachePathDialog,
            onRestoreDefaultPath = { memoryManagementViewModel.restoreDefaultMusicCacheDirectory() },
        )
    }
}

/**
 * 存储管理页主体内容。
 *
 * 这里只负责组织页面信息架构：顶部说明、概览卡片、空间分布、快速清理和存储位置。
 *
 * @param title 页面标题。
 * @param storageItems 已整理好的存储展示条目。
 * @param totalBytes 总占用字节数，用于计算各分类占比。
 * @param totalSizeText 总占用容量文本。
 * @param clearableSizeText 可清理容量文本。
 * @param cachePath 当前歌曲缓存目录。
 * @param cachePathMode 缓存路径状态，默认路径或自定义路径。
 * @param isDefaultCachePath 当前缓存路径是否为默认路径。
 * @param clearTitle 清理按钮文案。
 * @param adjustTitle 调整路径文案。
 * @param restoreDefaultTitle 恢复默认路径文案。
 * @param onClearMusicCache 清理歌曲缓存回调。
 * @param onClearTemporaryCache 清理临时缓存回调。
 * @param onClearDatabase 清理数据库回调，调用方负责确认逻辑。
 * @param onOpenPath 打开缓存路径弹窗回调。
 * @param onRestoreDefaultPath 恢复默认缓存路径回调。
 */
@Composable
private fun JvmMemoryContent(
    title: String,
    storageItems: List<JvmStorageDisplayItem>,
    totalBytes: Double,
    totalSizeText: String,
    clearableSizeText: String,
    cachePath: String,
    cachePathMode: String,
    isDefaultCachePath: Boolean,
    clearTitle: String,
    adjustTitle: String,
    restoreDefaultTitle: String,
    onClearMusicCache: () -> Unit,
    onClearTemporaryCache: () -> Unit,
    onClearDatabase: () -> Unit,
    onOpenPath: () -> Unit,
    onRestoreDefaultPath: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding * 2)
    ) {
        JvmMemoryHeader(
            title = title,
            totalSize = totalSizeText,
            clearableSize = clearableSizeText,
            cachePathMode = cachePathMode,
        )

        // 四个概览卡片横向铺开，窄屏时由通用 FlowRow 自动换行。
        JvmMemoryOverview(storageItems = storageItems)

        // 主体区域统一采用设置页的左主栏 + 右侧栏比例，右侧快速清理获得同等宽度。
        JvmSettingTwoPaneContent(
            leftContent = {
                JvmMemoryDistributionSection(
                    storageItems = storageItems,
                    totalBytes = totalBytes,
                    totalSizeText = totalSizeText,
                )
            },
            rightContent = {
                JvmMemoryQuickCleanSection(
                    storageItems = storageItems,
                    clearTitle = clearTitle,
                    onClearMusicCache = onClearMusicCache,
                    onClearTemporaryCache = onClearTemporaryCache,
                    onClearDatabase = onClearDatabase,
                    onOpenPath = onOpenPath,
                )
                JvmMemoryPathSection(
                    cachePath = cachePath,
                    cachePathMode = cachePathMode,
                    isDefaultCachePath = isDefaultCachePath,
                    adjustTitle = adjustTitle,
                    restoreDefaultTitle = restoreDefaultTitle,
                    onOpenPath = onOpenPath,
                    onRestoreDefaultPath = onRestoreDefaultPath,
                )
            }
        )
    }
}

/**
 * 页面头部区域。
 *
 * 左侧显示页面标题和说明，右侧状态卡汇总本地占用、可清理容量和缓存路径模式。
 *
 * @param title 页面标题。
 * @param totalSize 总占用容量文本。
 * @param clearableSize 可清理容量文本。
 * @param cachePathMode 缓存路径模式。
 */
@Composable
private fun JvmMemoryHeader(
    title: String,
    totalSize: String,
    clearableSize: String,
    cachePathMode: String,
) {
    JvmSettingPageHeader(
        title = title,
        description = stringResource(Res.string.jvm_memory_management_screen_text_08),
    ) {
        JvmSettingStatusCard(
            width = 278.dp,
            prominentValue = true,
            items = listOf(
                JvmSettingStatusCardItem(label = stringResource(Res.string.jvm_memory_management_screen_text_09), value = totalSize),
                JvmSettingStatusCardItem(label = stringResource(Res.string.jvm_memory_management_screen_text_10), value = clearableSize),
                JvmSettingStatusCardItem(label = stringResource(Res.string.cache_path), value = cachePathMode),
            )
        )
    }
}

/**
 * 存储概览卡片区域。
 *
 * @param storageItems 存储展示条目列表，每个条目渲染为一个概览卡片。
 */
@Composable
private fun JvmMemoryOverview(storageItems: List<JvmStorageDisplayItem>) {
    JvmSettingFlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
        verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
    ) {
        storageItems.forEach { item ->
            JvmMemoryMetricTile(
                modifier = Modifier
                    .widthIn(min = 220.dp)
                    .weight(1f),
                item = item,
            )
        }
    }
}

/**
 * 单个存储概览卡片。
 *
 * @param modifier 外部布局修饰符。
 * @param item 存储展示条目。
 */
@Composable
private fun JvmMemoryMetricTile(
    modifier: Modifier = Modifier,
    item: JvmStorageDisplayItem,
) {
    Surface(
        modifier = modifier.heightIn(min = 108.dp),
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier
                // 使用分类色做非常轻的顶部氛围，保持与预览稿的层次感一致。
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            item.color.copy(alpha = 0.10f),
                            Color.Transparent
                        )
                    )
                )
                .padding(XyTheme.dimens.outerHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding)
        ) {
            JvmMemoryKicker(
                icon = item.icon,
                text = item.title,
                color = item.color,
            )
            Spacer(modifier = Modifier.height(XyTheme.dimens.outerVerticalPadding))
            Text(
                text = item.sizeText,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.meta,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 空间分布区域。
 *
 * @param storageItems 存储展示条目列表。
 * @param totalBytes 总占用字节数，用于每一行进度条占比。
 * @param totalSizeText 总占用容量文本，用于标题徽标。
 */
@Composable
private fun JvmMemoryDistributionSection(
    storageItems: List<JvmStorageDisplayItem>,
    totalBytes: Double,
    totalSizeText: String,
) {
    JvmSettingSection(
        title = stringResource(Res.string.jvm_memory_management_screen_text_11),
        subtitle = stringResource(Res.string.jvm_memory_management_screen_text_12),
        badge = stringResource(Res.string.jvm_memory_management_screen_text_13, totalSizeText),
        contentContainerEnabled = false,
        qualityNote = stringResource(Res.string.jvm_memory_management_screen_text_14),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(XyTheme.dimens.corner))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
                .border(
                    BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)),
                    RoundedCornerShape(XyTheme.dimens.corner)
                )
        ) {
            storageItems.forEachIndexed { index, item ->
                // 每个分类都用同一套行组件展示名称、说明、进度条和容量。
                JvmMemoryStorageRow(
                    item = item,
                    totalBytes = totalBytes,
                )
                if (index != storageItems.lastIndex) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f))
                    )
                }
            }
        }
    }
}

/**
 * 空间分布列表中的一行。
 *
 * @param item 存储展示条目。
 * @param totalBytes 总占用字节数，用于计算当前条目的百分比。
 */
@Composable
private fun JvmMemoryStorageRow(
    item: JvmStorageDisplayItem,
    totalBytes: Double,
) {
    // 总占用为 0 时直接显示空进度，避免除以 0。
    val progress = if (totalBytes <= 0.0) {
        0f
    } else {
        (item.sizeBytes / totalBytes).toFloat().coerceIn(0f, 1f)
    }
    // 非 0 的小占用保留最小可见进度，避免在 UI 上看起来完全没有数据。
    val visibleProgress = if (progress > 0f) progress.coerceAtLeast(0.03f) else 0f

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(XyTheme.dimens.contentPadding)
    ) {
        if (maxWidth < 620.dp) {
            // 窄宽度下改为纵向排布，避免名称、进度条和容量互相挤压。
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding)
            ) {
                JvmMemoryStorageRowTitle(item = item)
                JvmMemoryProgressBar(
                    progress = visibleProgress,
                    color = item.color,
                )
                Text(
                    text = item.sizeText,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        } else {
            // 宽屏下保持预览稿的横向表格感，便于快速比较不同分类。
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                JvmMemoryStorageRowTitle(
                    modifier = Modifier.width(180.dp),
                    item = item,
                )
                JvmMemoryProgressBar(
                    modifier = Modifier.weight(1f),
                    progress = visibleProgress,
                    color = item.color,
                )
                Text(
                    modifier = Modifier.width(84.dp),
                    text = item.sizeText,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}

/**
 * 空间分布行的标题区域。
 *
 * @param modifier 外部布局修饰符。
 * @param item 存储展示条目。
 */
@Composable
private fun JvmMemoryStorageRowTitle(
    modifier: Modifier = Modifier,
    item: JvmStorageDisplayItem,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        JvmMemorySmallIcon(
            icon = item.icon,
            color = item.color,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding / 2)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 存储占比进度条。
 *
 * @param modifier 外部布局修饰符。
 * @param progress 当前占比，范围 0f 到 1f。
 * @param color 进度条颜色。
 */
@Composable
private fun JvmMemoryProgressBar(
    modifier: Modifier = Modifier,
    progress: Float,
    color: Color,
) {
    LinearProgressIndicator(
        progress = { progress },
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(999.dp)),
        color = color,
        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
    )
}

/**
 * 快速清理区域。
 *
 * @param storageItems 存储展示条目列表，按页面固定顺序读取歌曲缓存、临时缓存和数据库。
 * @param clearTitle 清理按钮文案。
 * @param onClearMusicCache 清理歌曲缓存回调。
 * @param onClearTemporaryCache 清理临时缓存回调。
 * @param onClearDatabase 清理数据库回调。
 * @param onOpenPath 打开缓存路径弹窗回调。
 */
@Composable
private fun JvmMemoryQuickCleanSection(
    storageItems: List<JvmStorageDisplayItem>,
    clearTitle: String,
    onClearMusicCache: () -> Unit,
    onClearTemporaryCache: () -> Unit,
    onClearDatabase: () -> Unit,
    onOpenPath: () -> Unit,
) {
    // storageItems 的顺序在入口处固定，这里只提取快速清理需要的前三类。
    val musicCache = storageItems.getOrNull(0)
    val temporaryCache = storageItems.getOrNull(1)
    val database = storageItems.getOrNull(2)
    // 快速清理复用设置页“通用”入口卡片，保证高度、宽度计算和 hover 动效完全一致。
    val quickCleanEntries = listOf(
        JvmSettingActionEntry(
            icon = Res.drawable.delete_24px,
            kicker = stringResource(Res.string.jvm_memory_management_screen_text_15),
            title = "${clearTitle}${musicCache?.title.orEmpty()}",
            description = musicCache.clearDescription(stringResource(Res.string.jvm_memory_management_screen_text_16)),
            enabled = musicCache.hasStorage(),
            color = MaterialTheme.colorScheme.primary,
            onClick = onClearMusicCache,
        ),
        JvmSettingActionEntry(
            icon = Res.drawable.download_24px,
            kicker = stringResource(Res.string.jvm_memory_management_screen_text_15),
            title = "${clearTitle}${temporaryCache?.title.orEmpty()}",
            description = temporaryCache.clearDescription(stringResource(Res.string.jvm_memory_management_screen_text_17)),
            enabled = temporaryCache.hasStorage(),
            color = MaterialTheme.colorScheme.tertiary,
            onClick = onClearTemporaryCache,
        ),
        JvmSettingActionEntry(
            icon = Res.drawable.delete_24px,
            kicker = stringResource(Res.string.jvm_memory_management_screen_text_18),
            title = "${clearTitle}${database?.title.orEmpty()}",
            description = database.clearDescription(stringResource(Res.string.jvm_memory_management_screen_text_19)),
            enabled = database.hasStorage(),
            color = MaterialTheme.colorScheme.error,
            onClick = onClearDatabase,
        ),
        JvmSettingActionEntry(
            icon = Res.drawable.folder_managed_24px,
            kicker = stringResource(Res.string.jvm_memory_management_screen_text_20),
            title = stringResource(Res.string.jvm_memory_management_screen_text_21),
            description = stringResource(Res.string.jvm_memory_management_screen_text_22),
            color = MaterialTheme.colorScheme.secondary,
            onClick = onOpenPath,
        ),
    )

    JvmSettingSection(
        title = stringResource(Res.string.jvm_memory_management_screen_text_23),
        subtitle = stringResource(Res.string.jvm_memory_management_screen_text_24),
        badge = clearTitle,
        contentContainerEnabled = false,
    ) {
        JvmSettingActionGrid(actionEntries = quickCleanEntries)
    }
}

/**
 * 存储位置区域。
 *
 * @param cachePath 当前歌曲缓存目录。
 * @param cachePathMode 缓存路径状态，默认路径或自定义路径。
 * @param isDefaultCachePath 当前缓存路径是否为默认路径。
 * @param adjustTitle 调整路径文案。
 * @param restoreDefaultTitle 恢复默认路径文案。
 * @param onOpenPath 打开缓存路径弹窗回调。
 * @param onRestoreDefaultPath 恢复默认缓存路径回调。
 */
@Composable
private fun JvmMemoryPathSection(
    cachePath: String,
    cachePathMode: String,
    isDefaultCachePath: Boolean,
    adjustTitle: String,
    restoreDefaultTitle: String,
    onOpenPath: () -> Unit,
    onRestoreDefaultPath: () -> Unit,
) {
    JvmSettingSection(
        title = stringResource(Res.string.jvm_memory_management_screen_text_25),
        subtitle = stringResource(Res.string.jvm_memory_management_screen_text_26),
        badge = cachePathMode,
        contentContainerEnabled = false,
        qualityNote = stringResource(Res.string.jvm_memory_management_screen_text_27),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(XyTheme.dimens.corner))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
                .border(
                    BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)),
                    RoundedCornerShape(XyTheme.dimens.corner)
                )
        ) {
            JvmSettingBaseRow(
                icon = Res.drawable.folder_managed_24px,
                title = stringResource(Res.string.jvm_memory_management_screen_text_28),
                description = cachePath.ifBlank { stringResource(Res.string.jvm_memory_management_screen_text_29) },
                descriptionMaxLines = 2,
                descriptionOverflow = TextOverflow.Visible,
                minHeight = 72.dp,
                horizontalPadding = XyTheme.dimens.contentPadding,
                verticalPadding = XyTheme.dimens.outerVerticalPadding,
                iconSelected = true,
                onClick = onOpenPath,
                trailing = {
                    JvmMemorySettingValueBadge(text = adjustTitle)
                }
            )
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f))
            )
            val restorePathEnabled = !isDefaultCachePath
            val restorePathContentAlpha = if (restorePathEnabled) 1f else 0.48f
            JvmSettingBaseRow(
                icon = Res.drawable.check_24px,
                title = restoreDefaultTitle,
                // 已经是默认目录时禁用恢复操作，只保留状态提示。
                description = if (isDefaultCachePath) stringResource(Res.string.jvm_memory_management_screen_text_30) else stringResource(Res.string.jvm_memory_management_screen_text_31),
                enabled = restorePathEnabled,
                minHeight = 72.dp,
                horizontalPadding = XyTheme.dimens.contentPadding,
                verticalPadding = XyTheme.dimens.outerVerticalPadding,
                iconSelected = true,
                contentAlpha = restorePathContentAlpha,
                onClick = onRestoreDefaultPath,
                trailing = {
                    JvmMemorySettingValueBadge(
                        text = if (isDefaultCachePath) stringResource(Res.string.jvm_memory_management_screen_text_32) else stringResource(Res.string.jvm_memory_management_screen_text_33),
                        alpha = restorePathContentAlpha,
                    )
                }
            )
        }
    }
}

/**
 * 存储位置设置行右侧值标签。
 *
 * @param text 标签文本。
 * @param alpha 标签透明度。
 */
@Composable
private fun JvmMemorySettingValueBadge(
    text: String,
    alpha: Float = 1f,
) {
    JvmMemoryBadge(
        text = text,
        alpha = alpha,
    )
}

/**
 * 带小图标的短标签。
 *
 * @param icon 图标资源。
 * @param text 标签文本。
 * @param color 图标强调色。
 */
@Composable
private fun JvmMemoryKicker(
    icon: DrawableResource,
    text: String,
    color: Color,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        JvmMemorySmallIcon(
            icon = icon,
            color = color,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * 小尺寸图标容器。
 *
 * @param icon 图标资源。
 * @param color 图标和容器强调色。
 */
@Composable
private fun JvmMemorySmallIcon(
    icon: DrawableResource,
    color: Color,
) {
    Surface(
        modifier = Modifier.size(32.dp),
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        color = color.copy(alpha = 0.16f),
        border = BorderStroke(
            width = 1.dp,
            color = color.copy(alpha = 0.26f)
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                modifier = Modifier.size(18.dp),
                painter = painterResource(icon),
                contentDescription = null,
                tint = color
            )
        }
    }
}

/**
 * 胶囊徽标。
 *
 * @param text 徽标文本。
 * @param alpha 整体透明度，用于禁用态。
 */
@Composable
private fun JvmMemoryBadge(
    text: String,
    alpha: Float = 1f,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f * alpha),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f * alpha)
        )
    ) {
        Text(
            modifier = Modifier.padding(
                horizontal = XyTheme.dimens.contentPadding,
                vertical = XyTheme.dimens.outerVerticalPadding / 2,
            ),
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * 存储展示条目。
 *
 * @property title 分类标题。
 * @property description 分类说明。
 * @property sizeText ViewModel 提供的容量文本。
 * @property sizeBytes 容量文本解析后的字节数，用于排序、汇总和进度条占比。
 * @property icon 分类图标资源。
 * @property color 分类强调色。
 * @property meta 概览卡片的辅助标签。
 * @property onClear 清理动作；为空时表示该分类不能在页面中清理。
 */
private data class JvmStorageDisplayItem(
    val title: String,
    val description: String,
    val sizeText: String,
    val sizeBytes: Double,
    val icon: DrawableResource,
    val color: Color,
    val meta: String,
    val onClear: (() -> Unit)? = null,
)

/**
 * 判断存储条目是否有可清理或可展示的非零占用。
 *
 * @return 占用大于 0 时返回 true。
 */
private fun JvmStorageDisplayItem?.hasStorage(): Boolean {
    return this?.sizeBytes?.let { it > 0.0 } == true
}

/**
 * 生成快速清理卡片说明。
 *
 * @param fallback 当前分类的默认清理说明。
 * @return 根据是否有占用生成“当前占用”或“当前无需清理”的说明。
 */
@Composable
private fun JvmStorageDisplayItem?.clearDescription(fallback: String): String {
    val item = this ?: return fallback
    return if (item.sizeBytes > 0.0) {
        stringResource(Res.string.jvm_memory_management_screen_text_34, item.sizeText, fallback.replaceFirstChar { it.lowercase(Locale.getDefault()) })
    } else {
        stringResource(Res.string.jvm_memory_management_screen_text_35, fallback.replaceFirstChar { it.lowercase(Locale.getDefault()) })
    }
}

/**
 * 显示 JVM 缓存路径管理弹窗。
 *
 * @param title 弹窗标题。
 * @param cachePath 当前缓存目录路径。
 * @param isDefaultPath 当前路径是否为默认路径。
 * @param onChoosePath 点击调整路径时执行的动作。
 * @param onRestoreDefault 点击恢复默认路径时执行的动作。
 */
private fun showJvmCachePathDialog(
    title: String,
    cachePath: String,
    isDefaultPath: Boolean,
    onChoosePath: () -> Unit,
    onRestoreDefault: () -> Unit,
) {
    AlertDialogObject(
        title = title,
        content = {
            // 路径可能很长，允许完整换行显示，避免省略后用户无法确认当前目录。
            XyTextSub(
                modifier = Modifier.widthIn(max = 420.dp),
                text = cachePath,
                style = MaterialTheme.typography.bodyMedium,
                overflow = TextOverflow.Visible,
            )
        },
        onDismissRequest = {
            // 该弹窗的 dismiss 按钮被用作“调整”，点击后再打开系统目录选择器。
            onChoosePath()
        },
        onConfirmation = onRestoreDefault,
        dismissText = Res.string.adjust,
        confirmText = Res.string.restore_default,
        confirmEnabled = !isDefaultPath,
    ).show()
}

/**
 * 打开 JVM 系统目录选择器选择播放缓存目录。
 *
 * @param currentPath 当前缓存目录路径，存在时作为系统选择器初始目录。
 * @param dialogSettings FileKit 弹窗配置，包含标题和主窗口 parent。
 * @return 用户选择的目录绝对路径；取消或打开失败时返回 null。
 */
private suspend fun chooseJvmCacheDirectory(
    currentPath: String,
    dialogSettings: io.github.vinceglb.filekit.dialogs.FileKitDialogSettings,
): String? {
    // 优先定位到当前缓存目录；当前路径无效时回退到系统音乐目录或用户主目录。
    val initialDirectory = currentPath
        .takeIf { it.isNotBlank() }
        ?.let(::File)
        ?.toExistingPlatformDirectoryOrNull()
        ?: defaultJvmCacheDirectoryChooserDirectory()
            .toExistingPlatformDirectoryOrNull()

    // 平台目录选择器异常只记录日志，避免设置页因为系统弹窗失败而崩溃。
    return runCatching {
        FileKit.openDirectoryPicker(
            directory = initialDirectory,
            dialogSettings = dialogSettings,
        )?.absolutePath()
    }.onFailure { error ->
        Log.e(Constants.LOG_ERROR_PREFIX, "打开缓存目录选择器失败", error)
    }.getOrNull()
}

/**
 * 获取缓存目录选择器的默认目录。
 *
 * @return 优先返回用户音乐目录，不存在时回退到用户主目录。
 */
private fun defaultJvmCacheDirectoryChooserDirectory(): File {
    val userHome = File(System.getProperty("user.home") ?: ".")
    val musicDir = File(userHome, "Music")
    return if (musicDir.exists()) musicDir else userHome
}

/**
 * 将容量文本解析为字节数。
 *
 * ViewModel 当前暴露的是格式化后的文本，例如 12.4MB、1GB 或 0B；页面需要数值做汇总和进度条占比，
 * 因此在 UI 层进行轻量解析。
 *
 * @return 解析后的字节数；解析失败时返回 0。
 */
private fun String.toStorageBytes(): Double {
    val text = trim()
    if (text.isEmpty()) return 0.0

    // 容量文本由数字和单位组成，先截取数字部分，再根据单位换算。
    val numberText = text.takeWhile { it.isDigit() || it == '.' }
    val value = numberText.toDoubleOrNull() ?: return 0.0
    val unit = text.drop(numberText.length).trim().uppercase()
    val multiplier = when (unit) {
        "PB" -> 1024.0 * 1024.0 * 1024.0 * 1024.0 * 1024.0
        "TB" -> 1024.0 * 1024.0 * 1024.0 * 1024.0
        "GB" -> 1024.0 * 1024.0 * 1024.0
        "MB" -> 1024.0 * 1024.0
        "KB" -> 1024.0
        else -> 1.0
    }
    return value * multiplier
}

/**
 * 将字节数格式化为页面展示容量。
 *
 * @return 带单位的容量文本，例如 0B、42MB、1.5GB。
 */
private fun Double.toStorageLabel(): String {
    if (this <= 0.0) return "0B"

    val units = listOf("B", "KB", "MB", "GB", "TB", "PB")
    var value = this
    var unitIndex = 0
    // 按 1024 进位找到最适合阅读的容量单位。
    while (value >= 1024.0 && unitIndex < units.lastIndex) {
        value /= 1024.0
        unitIndex++
    }

    // 10 以上或接近整数时不保留小数；小容量精确到一位小数，减少状态卡宽度压力。
    val numberText = if (value >= 10.0 || (value - value.roundToInt()).let { it < 0.05 && it > -0.05 }) {
        String.format(Locale.US, "%.0f", value)
    } else {
        String.format(Locale.US, "%.1f", value)
    }
    return "$numberText${units[unitIndex]}"
}
