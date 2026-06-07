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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.xybbz.ui.components.JvmSettingBaseRow
import cn.xybbz.ui.components.JvmSettingNote
import cn.xybbz.ui.components.JvmSettingPageContentMaxWidth
import cn.xybbz.ui.components.JvmSettingPageHeader
import cn.xybbz.ui.components.JvmSettingPageScaffold
import cn.xybbz.ui.components.JvmSettingRowDescriptionStyle
import cn.xybbz.ui.components.JvmSettingSection
import cn.xybbz.ui.components.JvmSettingStatusCard
import cn.xybbz.ui.components.JvmSettingStatusCardItem
import cn.xybbz.ui.components.JvmSettingTwoPaneContent
import cn.xybbz.ui.components.rememberBackgroundImagePicker
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyIconTextButton
import cn.xybbz.ui.xy.XyImage
import cn.xybbz.ui.xy.XyText
import cn.xybbz.ui.xy.XyTextSub
import cn.xybbz.viewmodel.SetBackgroundImageViewModel
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.album_24px
import xymusic_kmp.composeapp.generated.resources.background_image
import xymusic_kmp.composeapp.generated.resources.background_image_setting
import xymusic_kmp.composeapp.generated.resources.clear_image
import xymusic_kmp.composeapp.generated.resources.close_24px
import xymusic_kmp.composeapp.generated.resources.folder_managed_24px
import xymusic_kmp.composeapp.generated.resources.select_image
import xymusic_kmp.composeapp.generated.resources.settings_24px
import xymusic_kmp.composeapp.generated.resources.visibility_24px

/** JVM 背景设置页头部摘要卡宽度。 */
private val JvmBackgroundSummaryWidth = 278.dp

/** JVM 背景预览叠加层在窄宽度下隐藏侧栏的断点。 */
private val JvmBackgroundPreviewCompactBreakpoint = 560.dp

/** JVM 背景预览中模拟桌面播放器的底栏高度。 */
private val JvmBackgroundPreviewPlayerHeight = 58.dp

/** JVM 背景预览中模拟桌面顶栏的高度。 */
private val JvmBackgroundPreviewTopBarHeight = 44.dp

/** JVM 背景预览中模拟桌面侧栏的宽度。 */
private val JvmBackgroundPreviewSideWidth = 124.dp

/** JVM 背景信息行展示数据。 */
private data class JvmBackgroundInfoRow(
    /** 信息行图标资源。 */
    val icon: DrawableResource,
    /** 信息行标题。 */
    val title: String,
    /** 信息行说明。 */
    val description: String,
    /** 信息行尾部状态。 */
    val value: String,
    /** 信息行图标是否使用强调色。 */
    val selected: Boolean = true,
    /** 信息行说明样式。 */
    val descriptionStyle: JvmSettingRowDescriptionStyle = JvmSettingRowDescriptionStyle.Normal,
)

/**
 * JVM 背景图片设置界面。
 */
@Composable
fun JvmSetBackgroundImageScreen(
    setBackgroundImageViewModel: SetBackgroundImageViewModel = koinViewModel<SetBackgroundImageViewModel>()
) {
    val imageFilePath by setBackgroundImageViewModel.settingsManager.imageFilePath.collectAsState()
    val imageSelected = !imageFilePath.isNullOrBlank()
    val imagePicker = rememberBackgroundImagePicker(
        onImagePicked = setBackgroundImageViewModel::updateBackgroundImagePath
    )
    val pageTitle = stringResource(Res.string.background_image_setting)
    val selectImageTitle = stringResource(Res.string.select_image)
    val clearImageTitle = stringResource(Res.string.clear_image)
    val fileName = imageFilePath?.toJvmBackgroundFileName()?.takeIf { it.isNotBlank() }
        ?: "未选择背景图片"
    val filePath = imageFilePath?.takeIf { it.isNotBlank() } ?: "未设置"

    key(XyTheme.brash.backgroundImageUri) {
        JvmSettingPageScaffold(
            contentMaxWidth = JvmSettingPageContentMaxWidth,
            contentPadding = PaddingValues(
                horizontal = XyTheme.dimens.outerHorizontalPadding * 2,
                vertical = XyTheme.dimens.outerVerticalPadding * 3
            ),
        ) {
            JvmSettingPageHeader(
                title = pageTitle,
                description = "将背景图选择、清除和裁剪预览放在同一个桌面工作区，保存前即可看到它与侧栏、内容区和播放栏叠加后的状态。",
                contentMaxWidth = JvmSettingPageContentMaxWidth,
            ) {
                JvmSettingStatusCard(
                    width = JvmBackgroundSummaryWidth,
                    prominentValue = true,
                    items = listOf(
                        JvmSettingStatusCardItem(label = "当前状态", value = if (imageSelected) "已选择" else "未选择"),
                        JvmSettingStatusCardItem(label = "显示比例", value = "16:9"),
                        JvmSettingStatusCardItem(label = "填充方式", value = "居中裁剪"),
                    )
                )
            }

            JvmSettingTwoPaneContent(
                leftContent = {
                    JvmSettingSection(
                        title = "背景预览",
                        subtitle = "按照当前桌面端背景图逻辑展示，图片会铺满 16:9 区域并保留中心视觉。",
                        badge = if (imageSelected) "当前图片" else "默认背景",
                        contentContainerEnabled = false,
                        qualityNote = "背景图片会使用 ContentScale.Crop 填充预览区域；窗口比例变化时边缘可能被裁剪，中心内容会优先保留。"
                    ) {
                        JvmBackgroundPreviewWorkbench(
                            imageFilePath = imageFilePath,
                            imageSelected = imageSelected,
                        )
                    }
                },
                rightContent = {
                    JvmBackgroundActionPanel(
                        imageSelected = imageSelected,
                        fileName = fileName,
                        filePath = filePath,
                        selectImageTitle = selectImageTitle,
                        clearImageTitle = clearImageTitle,
                        onSelectImage = {
                            imagePicker.pickImage()
                        },
                        onClearImage = {
                            setBackgroundImageViewModel.updateBackgroundImagePath(null)
                        },
                    )

                    JvmSettingSection(
                        title = "显示信息",
                        subtitle = "桌面端只展示与当前背景相关的少量关键信息。",
                        badge = "JVM",
                    ) {
                        JvmBackgroundInfoRows(
                            rows = listOf(
                                JvmBackgroundInfoRow(
                                    icon = Res.drawable.visibility_24px,
                                    title = "生效范围",
                                    description = "桌面端主窗口背景。",
                                    value = "JVM"
                                ),
                                JvmBackgroundInfoRow(
                                    icon = Res.drawable.settings_24px,
                                    title = "裁剪策略",
                                    description = "铺满预览区域，超出部分自动裁剪。",
                                    value = "Crop"
                                ),
                                JvmBackgroundInfoRow(
                                    icon = Res.drawable.folder_managed_24px,
                                    title = "文件路径",
                                    description = filePath,
                                    value = if (imageSelected) "本地" else "空",
                                    selected = imageSelected,
                                    descriptionStyle = JvmSettingRowDescriptionStyle.Path,
                                ),
                            )
                        )
                    }

                    JvmSettingNote(text = "清除背景后会回到默认主题背景，侧栏和播放栏保持当前 Material 主题样式。")
                }
            )
        }
    }
}

/**
 * JVM 背景图片预览工作台。
 */
@Composable
private fun JvmBackgroundPreviewWorkbench(
    imageFilePath: String?,
    imageSelected: Boolean,
) {
    val colorScheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(XyTheme.dimens.corner)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(shape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        colorScheme.primary.copy(alpha = if (XyTheme.configs.isDarkTheme) 0.30f else 0.16f),
                        colorScheme.tertiary.copy(alpha = if (XyTheme.configs.isDarkTheme) 0.18f else 0.10f),
                        colorScheme.surfaceContainerLowest,
                    )
                )
            )
            .border(
                BorderStroke(1.dp, colorScheme.onSurface.copy(alpha = 0.10f)),
                shape
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (imageSelected) {
            XyImage(
                modifier = Modifier.matchParentSize(),
                model = imageFilePath,
                contentScale = ContentScale.Crop,
                contentDescription = stringResource(Res.string.background_image),
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(colorScheme.scrim.copy(alpha = 0.18f))
            )
            JvmBackgroundMockDesktopOverlay(
                modifier = Modifier
                    .matchParentSize()
                    .padding(XyTheme.dimens.outerHorizontalPadding + XyTheme.dimens.contentPadding)
            )
        } else {
            JvmBackgroundEmptyPreview()
        }
    }
}

/**
 * JVM 背景未选择时的空状态预览。
 */
@Composable
private fun JvmBackgroundEmptyPreview() {
    Column(
        modifier = Modifier
            .widthIn(max = 320.dp)
            .padding(XyTheme.dimens.outerHorizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding)
    ) {
        JvmBackgroundIconBox(
            icon = Res.drawable.album_24px,
            selected = false,
            size = 72.dp,
        )
        XyText(
            text = "未选择背景图片",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        XyTextSub(
            text = "页面会回到默认深色背景，侧栏和播放栏保持当前主题。",
            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * JVM 背景预览里的桌面界面叠加层。
 */
@Composable
private fun JvmBackgroundMockDesktopOverlay(modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(XyTheme.dimens.corner)

    Surface(
        modifier = modifier,
        shape = shape,
        color = colorScheme.surface.copy(alpha = 0.42f),
        border = BorderStroke(
            width = 1.dp,
            color = colorScheme.onSurface.copy(alpha = 0.14f)
        ),
        shadowElevation = XyTheme.dimens.outerVerticalPadding,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            JvmBackgroundMockTopBar()
            JvmBackgroundMockMainArea(modifier = Modifier.weight(1f))
            JvmBackgroundMockPlayerBar()
        }
    }
}

/**
 * JVM 背景预览里的模拟桌面顶栏。
 */
@Composable
private fun JvmBackgroundMockTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(JvmBackgroundPreviewTopBarHeight)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.54f))
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)),
                RoundedCornerShape(0.dp)
            )
            .padding(horizontal = XyTheme.dimens.contentPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding / 2),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(3) {
                JvmBackgroundMockDot()
            }
        }
        XyText(
            text = "XyMusic",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * JVM 背景预览里的模拟桌面主区域。
 */
@Composable
private fun JvmBackgroundMockMainArea(modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val showSideBar = maxWidth >= JvmBackgroundPreviewCompactBreakpoint

        Row(modifier = Modifier.fillMaxSize()) {
            if (showSideBar) {
                Column(
                    modifier = Modifier
                        .width(JvmBackgroundPreviewSideWidth)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.42f))
                        .border(
                            BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)),
                            RoundedCornerShape(0.dp)
                        )
                        .padding(XyTheme.dimens.contentPadding),
                    verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding)
                ) {
                    repeat(3) { index ->
                        JvmBackgroundMockLine(
                            selected = index == 0,
                            widthFraction = if (index == 2) 0.72f else 1f,
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(XyTheme.dimens.contentPadding),
                verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding)
            ) {
                JvmBackgroundMockCard(modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding)
                ) {
                    JvmBackgroundMockCard(modifier = Modifier.weight(1f))
                    if (showSideBar) {
                        JvmBackgroundMockCard(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/**
 * JVM 背景预览里的模拟播放栏。
 */
@Composable
private fun JvmBackgroundMockPlayerBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(JvmBackgroundPreviewPlayerHeight)
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.72f))
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)),
                RoundedCornerShape(0.dp)
            )
            .padding(horizontal = XyTheme.dimens.contentPadding),
        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.tertiary,
                            MaterialTheme.colorScheme.primary,
                        )
                    ),
                    shape = RoundedCornerShape(XyTheme.dimens.outerVerticalPadding)
                )
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding / 2)
        ) {
            JvmBackgroundMockLine(widthFraction = 0.72f)
            JvmBackgroundMockLine(widthFraction = 0.42f)
        }
        Box(
            modifier = Modifier
                .width(96.dp)
                .height(XyTheme.dimens.outerVerticalPadding)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.60f),
                    CircleShape
                )
        )
    }
}

/**
 * JVM 背景预览里的模拟窗口圆点。
 */
@Composable
private fun JvmBackgroundMockDot() {
    Box(
        modifier = Modifier
            .size(9.dp)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f), CircleShape)
    )
}

/**
 * JVM 背景预览里的模拟内容卡片。
 */
@Composable
private fun JvmBackgroundMockCard(modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(XyTheme.dimens.outerVerticalPadding + XyTheme.dimens.innerVerticalPadding / 4)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 42.dp)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), shape)
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
                shape
            )
    )
}

/**
 * JVM 背景预览里的模拟文本线条。
 */
@Composable
private fun JvmBackgroundMockLine(
    widthFraction: Float = 1f,
    selected: Boolean = false,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .height(if (selected) 28.dp else XyTheme.dimens.outerVerticalPadding)
            .background(
                color = if (selected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.36f)
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.24f)
                },
                shape = RoundedCornerShape(XyTheme.dimens.outerVerticalPadding)
            )
    )
}

/**
 * JVM 背景图片操作面板。
 */
@Composable
private fun JvmBackgroundActionPanel(
    imageSelected: Boolean,
    fileName: String,
    filePath: String,
    selectImageTitle: String,
    clearImageTitle: String,
    onSelectImage: () -> Unit,
    onClearImage: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
        )
    ) {
        Column(
            modifier = Modifier.padding(XyTheme.dimens.outerHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding / 2)
                ) {
                    XyText(
                        text = "当前图片",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    XyTextSub(
                        text = "保留原页面的选择和清除动作，并把文件状态放到操作旁边。",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                JvmBackgroundPill(text = if (imageSelected) "已选择" else "未选择", selected = imageSelected)
            }

            JvmBackgroundFileCard(
                imageSelected = imageSelected,
                fileName = fileName,
                filePath = filePath,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                XyIconTextButton(
                    modifier = Modifier.weight(1f),
                    onClick = composeClick(onClick = onSelectImage),
                    text = selectImageTitle,
                    icon = Res.drawable.folder_managed_24px,
                    color = MaterialTheme.colorScheme.onPrimary,
                    backgroundColor = MaterialTheme.colorScheme.primary,
                )
                XyIconTextButton(
                    modifier = Modifier.weight(1f),
                    onClick = composeClick(onClick = onClearImage),
                    text = clearImageTitle,
                    icon = Res.drawable.close_24px,
                    color = if (imageSelected) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    backgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
                    enabled = imageSelected,
                )
            }
        }
    }
}

/**
 * JVM 背景图片当前文件卡片。
 */
@Composable
private fun JvmBackgroundFileCard(
    imageSelected: Boolean,
    fileName: String,
    filePath: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier.padding(XyTheme.dimens.contentPadding),
            horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            JvmBackgroundIconBox(
                icon = Res.drawable.album_24px,
                selected = imageSelected,
                size = 46.dp,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding / 2)
            ) {
                XyText(
                    text = fileName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                XyTextSub(
                    text = filePath,
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * JVM 背景图片显示信息行列表。
 */
@Composable
private fun JvmBackgroundInfoRows(rows: List<JvmBackgroundInfoRow>) {
    rows.forEach { row ->
        JvmSettingBaseRow(
            icon = row.icon,
            title = row.title,
            description = row.description,
            descriptionStyle = row.descriptionStyle,
            descriptionMaxLines = if (row.descriptionStyle == JvmSettingRowDescriptionStyle.Path) 2 else 1,
            iconSelected = row.selected,
            trailing = {
                JvmBackgroundPill(
                    text = row.value,
                    selected = row.selected,
                )
            }
        )
    }
}

/**
 * JVM 背景图片图标容器。
 */
@Composable
private fun JvmBackgroundIconBox(
    icon: DrawableResource,
    selected: Boolean,
    size: Dp = 34.dp,
) {
    val color = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val shape = RoundedCornerShape(XyTheme.dimens.corner - XyTheme.dimens.outerVerticalPadding / 2)

    Box(
        modifier = Modifier
            .size(size)
            .clip(shape)
            .background(
                color = if (selected) {
                    color.copy(alpha = 0.18f)
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)
                }
            )
            .border(
                BorderStroke(
                    width = 1.dp,
                    color = if (selected) {
                        color.copy(alpha = 0.26f)
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.09f)
                    }
                ),
                shape = shape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(size * 0.56f),
            tint = color
        )
    }
}

/**
 * JVM 背景图片状态标签。
 */
@Composable
private fun JvmBackgroundPill(
    text: String,
    selected: Boolean = true,
    color: Color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Surface(
        shape = CircleShape,
        color = color.copy(alpha = if (selected) 0.12f else 0.08f),
        contentColor = color,
        border = BorderStroke(
            width = 1.dp,
            color = color.copy(alpha = if (selected) 0.28f else 0.12f)
        )
    ) {
        XyText(
            modifier = Modifier
                .widthIn(max = 128.dp)
                .padding(
                    horizontal = XyTheme.dimens.contentPadding,
                    vertical = XyTheme.dimens.outerVerticalPadding / 2
                ),
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * 将 JVM 背景图片路径转换为适合页面展示的文件名。
 */
private fun String.toJvmBackgroundFileName(): String {
    return substringAfterLast('\\')
        .substringAfterLast('/')
        .ifBlank { this }
}
