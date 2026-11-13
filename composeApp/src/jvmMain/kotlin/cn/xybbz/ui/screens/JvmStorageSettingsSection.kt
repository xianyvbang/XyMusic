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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.utils.Log
import cn.xybbz.ui.components.AlertDialogObject
import cn.xybbz.ui.components.JvmSettingBaseRow
import cn.xybbz.ui.components.JvmSettingFlowRow
import cn.xybbz.ui.components.JvmSettingGroup
import cn.xybbz.ui.components.JvmSettingGroupVerticalGap
import cn.xybbz.ui.components.JvmSettingItemFlow
import cn.xybbz.ui.components.JvmSettingRowDescriptionStyle
import cn.xybbz.ui.components.JvmSettingWideGroup
import cn.xybbz.ui.components.rememberJvmFileKitDialogSettings
import cn.xybbz.ui.components.show
import cn.xybbz.ui.components.toExistingPlatformDirectoryOrNull
import cn.xybbz.ui.ext.jvmHoverDebounceClickable
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyTextSub
import cn.xybbz.viewmodel.MemoryManagementViewModel
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.dialogs.openDirectoryPicker
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.stringResource
import xymusic.composeapp.generated.resources.*
import xymusic.composeapp.generated.resources.Res
import xymusic.composeapp.generated.resources.adjust
import xymusic.composeapp.generated.resources.audio_cache
import xymusic.composeapp.generated.resources.audio_cache_description
import xymusic.composeapp.generated.resources.cache_path
import xymusic.composeapp.generated.resources.clear
import xymusic.composeapp.generated.resources.confirm_delete_database
import xymusic.composeapp.generated.resources.database_data
import xymusic.composeapp.generated.resources.database_data_description
import xymusic.composeapp.generated.resources.essential_data
import xymusic.composeapp.generated.resources.essential_data_description
import xymusic.composeapp.generated.resources.folder_managed_24px
import xymusic.composeapp.generated.resources.info_24px
import xymusic.composeapp.generated.resources.music_note_24px
import xymusic.composeapp.generated.resources.queue_music_24px
import xymusic.composeapp.generated.resources.restore_default
import xymusic.composeapp.generated.resources.settings_24px
import xymusic.composeapp.generated.resources.storage_management
import xymusic.composeapp.generated.resources.temporary_cache
import xymusic.composeapp.generated.resources.temporary_cache_description
import xymusic.composeapp.generated.resources.warning
import java.io.File
import java.util.Locale
import kotlin.math.roundToInt

/**
 * 设置页内嵌的存储管理分区。
 *
 * 承接原存储管理页的占用总览、分类清理与缓存目录管理能力，外观完全复用设置页的分组标题和设置行样式。
 *
 * @param memoryManagementViewModel 存储管理 ViewModel，提供占用数据与清理动作。
 * @param modifier 外层布局修饰符，通常由设置页分组网格下发宽度。
 */
@Composable
internal fun JvmStorageSettingsSection(
    memoryManagementViewModel: MemoryManagementViewModel,
    modifier: Modifier = Modifier,
) {
    // 进入存储分区时刷新一次存储信息，避免展示上次打开设置页时的旧数据。
    LaunchedEffect(memoryManagementViewModel) {
        memoryManagementViewModel.logStorageInfo()
    }
    // 数据库清理属于高风险操作，先弹出确认框，再交给 ViewModel 清理数据库与相关状态。
    val warning = stringResource(Res.string.warning)
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
    val storageItems = jvmStorageSettingsItems(
        memoryManagementViewModel = memoryManagementViewModel,
        onClearDatabase = ::confirmClearDatabase,
    )
    val totalBytes = storageItems.sumOf { it.sizeBytes }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(JvmSettingGroupVerticalGap),
    ) {
        JvmStorageUsageGroup(
            storageItems = storageItems,
            totalBytes = totalBytes,
        )
        JvmStorageLocationGroup(
            memoryManagementViewModel = memoryManagementViewModel,
        )
    }
}

/**
 * 存储占用分组：汇总本地占用，并用占比条、图例和分类行展示各类数据。
 *
 * @param storageItems 已整理好的存储展示条目。
 * @param totalBytes 总占用字节数，用于计算占比条比例。
 */
@Composable
private fun JvmStorageUsageGroup(
    storageItems: List<JvmStorageSettingsItem>,
    totalBytes: Double,
) {
    val totalSizeText = totalBytes.formatStorageLabel()
    val clearableSizeText = storageItems
        .filter { it.onClear != null }
        .sumOf { it.sizeBytes }
        .formatStorageLabel()
    val totalLabel = stringResource(Res.string.jvm_memory_management_screen_text_09)
    val clearableLabel = stringResource(Res.string.jvm_memory_management_screen_text_10)
    // 占比条与图例共用同一份主题同色系渐变，保证两者颜色一一对应。
    val gradientRamp = jvmStorageGradientRamp(
        primary = MaterialTheme.colorScheme.primary,
        isDarkTheme = XyTheme.configs.isDarkTheme,
    )
    // 每个分类取自己区间在渐变中的实际颜色，供图例圆点使用。
    val segmentColors = storageItems.segmentGradientColors(
        ramp = gradientRamp,
        totalBytes = totalBytes,
    )

    JvmSettingWideGroup(title = stringResource(Res.string.storage_management)) {
        // 顶部汇总总占用与可清理容量，方便直接判断磁盘压力。
        XyTextSub(
            text = "$totalLabel $totalSizeText · $clearableLabel $clearableSizeText",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
        JvmStorageStackedBar(
            storageItems = storageItems,
            totalBytes = totalBytes,
            gradientRamp = gradientRamp,
        )
        // 图例圆点使用对应分段在渐变中的实际颜色，便于与占比条区间对应。
        JvmSettingFlowRow(
            horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
            verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding),
        ) {
            storageItems.forEachIndexed { index, item ->
                JvmStorageLegendItem(
                    item = item,
                    dotColor = segmentColors.getOrElse(index) { gradientRamp.first() },
                )
            }
        }
        // 分类项与其它设置分组保持一致，一行最多三个横向排列，不绘制分割线。
        // 每个分类的清理入口直接跟在对应条目尾部，避免再单独占用一块快速清理区域。
        // 分类说明文案较长，必须完整换行展示，因此取消行数限制并允许内容超出省略处理。
        JvmSettingItemFlow {
            storageItems.forEach { item ->
                JvmSettingBaseRow(
                    icon = item.icon,
                    title = item.title,
                    description = item.description,
                    descriptionMaxLines = Int.MAX_VALUE,
                    descriptionOverflow = TextOverflow.Visible,
                    minHeight = 64.dp,
                    horizontalPadding = XyTheme.dimens.contentPadding,
                    verticalPadding = XyTheme.dimens.outerVerticalPadding,
                    iconSelected = true,
                    iconColor = item.color,
                    trailing = {
                        JvmStorageValuePill(text = item.sizeText)
                        val onClear = item.onClear
                        if (onClear != null) {
                            JvmStorageActionPill(
                                text = stringResource(Res.string.clear),
                                enabled = item.hasClearableData(),
                                onClick = onClear,
                            )
                        }
                    },
                )
            }
        }
    }
}

/**
 * 存储位置分组：展示当前播放缓存目录，并提供调整目录与恢复默认目录的操作。
 *
 * @param memoryManagementViewModel 存储管理 ViewModel，负责缓存路径读写。
 */
@Composable
private fun JvmStorageLocationGroup(
    memoryManagementViewModel: MemoryManagementViewModel,
) {
    val cachePathTitle = stringResource(Res.string.cache_path)
    // 目录选择器是 suspend API，点击调整后需要使用页面协程启动。
    val coroutineScope = rememberCoroutineScope()
    // 绑定 JVM 主窗口的 FileKit 设置，避免系统目录选择弹窗出现在应用窗口后方。
    val cacheDirectoryDialogSettings = rememberJvmFileKitDialogSettings(
        stringResource(Res.string.jvm_memory_management_screen_text_01)
    )
    val isDefaultPath = memoryManagementViewModel.isDefaultMusicCachePath
    val restoreDefaultEnabled = !isDefaultPath

    // 打开系统目录选择器：用户取消时返回 null，此时不修改当前缓存目录。
    fun chooseCacheDirectory() {
        coroutineScope.launch {
            chooseJvmStorageDirectory(
                currentPath = memoryManagementViewModel.musicCachePath,
                dialogSettings = cacheDirectoryDialogSettings,
            )?.let { selectedPath ->
                memoryManagementViewModel.changeMusicCacheDirectory(selectedPath)
            }
        }
    }

    JvmSettingGroup(title = stringResource(Res.string.jvm_memory_management_screen_text_25)) {
        // 目录行直接展示完整路径，点击行尾“调整”拉起系统目录选择器。
        // 两个操作项交由分组容器横向排列，窄窗口下自动换行。
        JvmSettingBaseRow(
            icon = Res.drawable.folder_managed_24px,
            title = cachePathTitle,
            description = memoryManagementViewModel.musicCachePath.ifBlank {
                stringResource(Res.string.jvm_memory_management_screen_text_29)
            },
            descriptionStyle = JvmSettingRowDescriptionStyle.Path,
            descriptionMaxLines = 2,
            descriptionOverflow = TextOverflow.Visible,
            minHeight = 72.dp,
            horizontalPadding = XyTheme.dimens.contentPadding,
            verticalPadding = XyTheme.dimens.outerVerticalPadding,
            iconSelected = true,
            trailing = {
                JvmStorageActionPill(
                    text = stringResource(Res.string.adjust),
                    onClick = ::chooseCacheDirectory,
                )
            },
        )
        // 已经是默认目录时禁用恢复操作，只保留状态提示。
        JvmSettingBaseRow(
            icon = Res.drawable.folder_managed_24px,
            title = stringResource(Res.string.restore_default),
            description = if (isDefaultPath) {
                stringResource(Res.string.jvm_memory_management_screen_text_30)
            } else {
                stringResource(Res.string.jvm_memory_management_screen_text_31)
            },
            enabled = restoreDefaultEnabled,
            minHeight = 72.dp,
            horizontalPadding = XyTheme.dimens.contentPadding,
            verticalPadding = XyTheme.dimens.outerVerticalPadding,
            iconSelected = true,
            trailing = {
                JvmStorageActionPill(
                    text = stringResource(Res.string.jvm_memory_management_screen_text_33),
                    enabled = restoreDefaultEnabled,
                    onClick = { memoryManagementViewModel.restoreDefaultMusicCacheDirectory() },
                )
            },
        )
    }
}

/**
 * 存储占比条：按分类占用比例绘制一条主题同色系的连续渐变色带。
 *
 * 整条色带使用同一色相从亮到暗的线性渐变，各分段按其在总占用中的位置从整条渐变取样，
 * 分段之间只保留 1px 分隔，因此视觉上是连续过渡而不是不同色块拼接。
 *
 * @param storageItems 存储展示条目。
 * @param totalBytes 总占用字节数，为 0 时只展示空轨道。
 * @param gradientRamp 主题同色系渐变的颜色节点，从亮色过渡到深色。
 */
@Composable
private fun JvmStorageStackedBar(
    storageItems: List<JvmStorageSettingsItem>,
    totalBytes: Double,
    gradientRamp: List<Color>,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            .drawWithCache {
                val trackWidth = size.width
                val trackHeight = size.height
                // 分段之间保留 1px 间隙用于区分类别，同时不破坏整体渐变连续性。
                val gap = 1.dp.toPx()
                // 零占用分类不参与绘制，避免出现零宽空段或错位。
                val visibleItems = if (totalBytes > 0.0) {
                    storageItems.filter { it.sizeBytes > 0.0 }
                } else {
                    emptyList()
                }
                val gapCount = (visibleItems.size - 1).coerceAtLeast(0)
                val drawableWidth = (trackWidth - gap * gapCount).coerceAtLeast(0f)
                // 渐变锚定在整条轨道上，分段只负责裁剪出自己那一段区间。
                val brush = Brush.horizontalGradient(
                    colors = gradientRamp,
                    startX = 0f,
                    endX = trackWidth,
                )
                var cursor = 0f
                val segmentRects = visibleItems.map { item ->
                    val segmentWidth = (drawableWidth * (item.sizeBytes / totalBytes)).toFloat()
                    val rect = Offset(cursor, 0f) to Size(segmentWidth, trackHeight)
                    cursor += segmentWidth + gap
                    rect
                }
                onDrawBehind {
                    segmentRects.forEach { (topLeft, segmentSize) ->
                        if (segmentSize.width > 0f) {
                            drawRect(
                                brush = brush,
                                topLeft = topLeft,
                                size = segmentSize,
                            )
                        }
                    }
                }
            }
    )
}

/**
 * 构建存储占比条与图例共用的主题同色系渐变。
 *
 * 只从主题 primary 派生出亮暗两端，避免使用其它色相导致偏粉偏橙的观感；
 * 深色主题下深色端不会过度压暗，保证在深色背景上依旧可读。
 *
 * @param primary 主题主色。
 * @param isDarkTheme 当前是否为深色主题。
 * @return 从亮到暗的渐变颜色节点。
 */
private fun jvmStorageGradientRamp(primary: Color, isDarkTheme: Boolean): List<Color> {
    return if (isDarkTheme) {
        listOf(
            lerp(primary, Color.White, 0.32f),
            primary,
            lerp(primary, Color.Black, 0.22f),
        )
    } else {
        // 浅色主题下亮端混白比例不宜过大，否则与浅色轨道对比度不足。
        listOf(
            lerp(primary, Color.White, 0.42f),
            primary,
            lerp(primary, Color.Black, 0.30f),
        )
    }
}

/**
 * 按比例从渐变节点中采样颜色。
 *
 * @param fraction 0 到 1 之间的取样位置，0 表示渐变起点。
 * @return 该位置对应的颜色。
 */
private fun List<Color>.sampleGradient(fraction: Float): Color {
    if (isEmpty()) return Color.Transparent
    if (size == 1) return first()
    val position = fraction.coerceIn(0f, 1f) * (size - 1)
    val index = position.toInt().coerceIn(0, size - 2)
    return lerp(this[index], this[index + 1], position - index)
}

/**
 * 计算每个分类在其占用区间中点的实际渐变颜色，供图例圆点与占比条对应。
 *
 * @param ramp 主题同色系渐变节点。
 * @param totalBytes 总占用字节数。
 * @return 与展示条目顺序一一对应的颜色列表。
 */
private fun List<JvmStorageSettingsItem>.segmentGradientColors(
    ramp: List<Color>,
    totalBytes: Double,
): List<Color> {
    if (totalBytes <= 0.0) return List(size) { ramp.first() }
    var cumulative = 0.0
    return map { item ->
        val weight = (item.sizeBytes / totalBytes).coerceIn(0.0, 1.0)
        // 取区间中点取样，使图例颜色更接近该段在进度条上的观感。
        val samplePosition = (cumulative + weight / 2.0).toFloat()
        cumulative += weight
        ramp.sampleGradient(samplePosition)
    }
}

/**
 * 存储占比图例：颜色方块加分类名称与容量。
 *
 * @param item 单个存储展示条目。
 * @param dotColor 该分类在占比条渐变中的实际颜色。
 */
@Composable
private fun JvmStorageLegendItem(item: JvmStorageSettingsItem, dotColor: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding / 2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                // 图例标记使用方形色块，与设计稿保持一致。
                .background(dotColor)
        )
        XyTextSub(
            text = "${item.title} ${item.sizeText}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

/**
 * 存储分类行尾部的容量胶囊。
 *
 * @param text 容量文本。
 */
@Composable
private fun JvmStorageValuePill(text: String) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        )
    ) {
        Text(
            modifier = Modifier.padding(
                horizontal = XyTheme.dimens.contentPadding,
                vertical = XyTheme.dimens.outerVerticalPadding / 2,
            ),
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * 存储分类行尾部的操作按钮。
 *
 * @param text 按钮文案。
 * @param enabled 是否可点击；为 false 时整体降低透明度且不绑定点击。
 * @param onClick 点击回调，调用方负责数据库等高风险操作的二次确认。
 */
@Composable
private fun JvmStorageActionPill(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val buttonHovered = enabled && hovered
    val contentAlpha = if (enabled) 1f else 0.42f

    Surface(
        modifier = Modifier.jvmHoverDebounceClickable(
            interactionSource = interactionSource,
            enabled = enabled,
            // 禁用态不挂点击回调，避免零占用或默认目录时仍触发操作。
            onClick = if (enabled) onClick else null,
        ),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary.copy(
            alpha = if (buttonHovered) 0.20f else 0.12f
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f * contentAlpha)
        )
    ) {
        Text(
            modifier = Modifier.padding(
                horizontal = XyTheme.dimens.contentPadding,
                vertical = XyTheme.dimens.outerVerticalPadding / 2,
            ),
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary.copy(alpha = contentAlpha),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * 存储展示条目。
 *
 * @property title 分类标题。
 * @property description 分类说明。
 * @property sizeText ViewModel 提供的容量文本。
 * @property sizeBytes 容量文本解析后的字节数，用于汇总和占比条占比。
 * @property icon 分类图标资源。
 * @property color 分类图标强调色；占比条与图例不再使用该色，统一走主题同色系渐变。
 * @property meta 分类辅助标签。
 * @property onClear 清理动作；为空时表示该分类不能在设置页清理。
 */
private data class JvmStorageSettingsItem(
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
private fun JvmStorageSettingsItem?.hasClearableData(): Boolean {
    return this?.sizeBytes?.let { it > 0.0 } == true
}

/**
 * 将 ViewModel 暴露的容量文本、清理动作、颜色和图标统一整理为设置页展示模型。
 *
 * @param memoryManagementViewModel 存储管理 ViewModel。
 * @param onClearDatabase 数据库清理回调，调用方负责确认逻辑。
 * @return 按音频缓存、临时缓存、数据库、必要数据顺序排列的展示条目。
 */
@Composable
private fun jvmStorageSettingsItems(
    memoryManagementViewModel: MemoryManagementViewModel,
    onClearDatabase: () -> Unit,
): List<JvmStorageSettingsItem> {
    return listOf(
        JvmStorageSettingsItem(
            title = stringResource(Res.string.audio_cache),
            description = stringResource(Res.string.audio_cache_description),
            sizeText = memoryManagementViewModel.musicCacheSize,
            sizeBytes = memoryManagementViewModel.musicCacheSize.parseStorageBytes(),
            icon = Res.drawable.music_note_24px,
            color = MaterialTheme.colorScheme.primary,
            meta = stringResource(Res.string.jvm_memory_management_screen_text_02),
            onClear = { memoryManagementViewModel.clearMusicCache() },
        ),
        JvmStorageSettingsItem(
            title = stringResource(Res.string.temporary_cache),
            description = stringResource(Res.string.temporary_cache_description),
            sizeText = memoryManagementViewModel.cacheSize,
            sizeBytes = memoryManagementViewModel.cacheSize.parseStorageBytes(),
            icon = Res.drawable.queue_music_24px,
            color = MaterialTheme.colorScheme.tertiary,
            meta = stringResource(Res.string.jvm_memory_management_screen_text_03),
            onClear = { memoryManagementViewModel.clearAllCache() },
        ),
        JvmStorageSettingsItem(
            title = stringResource(Res.string.database_data),
            description = stringResource(Res.string.database_data_description),
            sizeText = memoryManagementViewModel.databaseSize,
            sizeBytes = memoryManagementViewModel.databaseSize.parseStorageBytes(),
            icon = Res.drawable.settings_24px,
            color = MaterialTheme.colorScheme.error,
            meta = stringResource(Res.string.jvm_memory_management_screen_text_04),
            onClear = onClearDatabase,
        ),
        JvmStorageSettingsItem(
            title = stringResource(Res.string.essential_data),
            description = stringResource(Res.string.essential_data_description),
            sizeText = memoryManagementViewModel.appDataSize,
            sizeBytes = memoryManagementViewModel.appDataSize.parseStorageBytes(),
            icon = Res.drawable.info_24px,
            color = MaterialTheme.colorScheme.secondary,
            meta = stringResource(Res.string.jvm_memory_management_screen_text_05),
        ),
    )
}

/**
 * 打开 JVM 系统目录选择器选择播放缓存目录。
 *
 * @param currentPath 当前缓存目录路径，存在时作为系统选择器初始目录。
 * @param dialogSettings FileKit 弹窗配置，包含标题和主窗口 parent。
 * @return 用户选择的目录绝对路径；取消或打开失败时返回 null。
 */
private suspend fun chooseJvmStorageDirectory(
    currentPath: String,
    dialogSettings: io.github.vinceglb.filekit.dialogs.FileKitDialogSettings,
): String? {
    // 优先定位到当前缓存目录；当前路径无效时回退到系统音乐目录或用户主目录。
    val initialDirectory = currentPath
        .takeIf { it.isNotBlank() }
        ?.let(::File)
        ?.toExistingPlatformDirectoryOrNull()
        ?: defaultJvmStorageChooserDirectory()
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
private fun defaultJvmStorageChooserDirectory(): File {
    val userHome = File(System.getProperty("user.home") ?: ".")
    val musicDir = File(userHome, "Music")
    return if (musicDir.exists()) musicDir else userHome
}

/**
 * 将容量文本解析为字节数。
 *
 * ViewModel 暴露的是格式化后的文本，例如 12.4MB、1GB 或 0B；页面需要数值做汇总和占比条占比，
 * 因此在 UI 层进行轻量解析。
 *
 * @return 解析后的字节数；解析失败时返回 0。
 */
private fun String.parseStorageBytes(): Double {
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
private fun Double.formatStorageLabel(): String {
    if (this <= 0.0) return "0B"

    val units = listOf("B", "KB", "MB", "GB", "TB", "PB")
    var value = this
    var unitIndex = 0
    // 按 1024 进位找到最适合阅读的容量单位。
    while (value >= 1024.0 && unitIndex < units.lastIndex) {
        value /= 1024.0
        unitIndex++
    }

    // 10 以上或接近整数时不保留小数；小容量精确到一位小数，减少状态文本宽度压力。
    val numberText = if (value >= 10.0 || (value - value.roundToInt()).let { it < 0.05 && it > -0.05 }) {
        String.format(Locale.US, "%.0f", value)
    } else {
        String.format(Locale.US, "%.1f", value)
    }
    return "$numberText${units[unitIndex]}"
}

