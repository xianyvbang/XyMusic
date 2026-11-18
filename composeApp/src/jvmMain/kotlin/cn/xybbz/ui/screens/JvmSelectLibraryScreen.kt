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
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.constants.Constants
import cn.xybbz.localdata.data.library.XyLibrary
import cn.xybbz.ui.components.JvmPageHeader
import cn.xybbz.ui.components.JvmSettingPageScaffold
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyIconTextButton
import cn.xybbz.ui.xy.XyText
import cn.xybbz.ui.xy.XyTextSub
import cn.xybbz.viewmodel.SelectLibraryViewModel
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import xymusic.composeapp.generated.resources.Res
import xymusic.composeapp.generated.resources.all_media_libraries
import xymusic.composeapp.generated.resources.check_24px
import xymusic.composeapp.generated.resources.current_connection
import xymusic.composeapp.generated.resources.folder_managed_24px
import xymusic.composeapp.generated.resources.jvm_connection_config_info_screen_text_01
import xymusic.composeapp.generated.resources.jvm_select_library_screen_text_01
import xymusic.composeapp.generated.resources.jvm_select_library_screen_text_02
import xymusic.composeapp.generated.resources.jvm_select_library_screen_text_06
import xymusic.composeapp.generated.resources.jvm_select_library_screen_text_23
import xymusic.composeapp.generated.resources.jvm_select_library_screen_text_27
import xymusic.composeapp.generated.resources.jvm_select_library_screen_text_31
import xymusic.composeapp.generated.resources.jvm_select_library_screen_text_32
import xymusic.composeapp.generated.resources.jvm_select_library_screen_text_33
import xymusic.composeapp.generated.resources.jvm_select_library_screen_text_34
import xymusic.composeapp.generated.resources.music_library
import xymusic.composeapp.generated.resources.refresh_24px
import xymusic.composeapp.generated.resources.restart_alt_24px

/** 媒体库选择器内容区最大宽度，对齐设计稿的单栏选择器宽度。 */
private val JvmSelectLibraryMaxContentWidth = 820.dp

/** 媒体库列表行的最小高度。 */
private val JvmSelectLibraryRowMinHeight = 62.dp

/** 媒体库列表行左侧选择指示器尺寸。 */
private val JvmSelectLibrarySelectorSize = 15.dp

/** 单选指示器内部圆点尺寸。 */
private val JvmSelectLibrarySelectorDotSize = 7.dp

/** 媒体库列表行右侧确认圆点尺寸。 */
private val JvmSelectLibraryCheckSize = 20.dp

/** 媒体库列表行选中时左侧强调条的宽度。 */
private val JvmSelectLibraryAccentWidth = 2.dp

/** 页面头部读数使用的等宽小标签字距。 */
private val JvmSelectLibraryKickerLetterSpacing = 1.4.sp

/**
 * JVM 桌面端媒体库选择页面。
 *
 * 排版对齐设计稿：顶部沿用连接管理与设置页的通用标题组件，
 * 标题右侧展示当前可见媒体库读数与刷新入口，下方是单栏媒体库选择列表。
 *
 * @param connectionId 当前连接 ID。
 * @param thisLibraryId 当前连接已保存的媒体库 ID 列表，为空表示全部媒体库。
 * @param selectLibraryViewModel 媒体库选择 ViewModel。
 * @param dataSourceManager 当前数据源管理器，用于读取数据源能力展示文案。
 */
@Composable
fun JvmSelectLibraryScreen(
    connectionId: Long,
    thisLibraryId: List<String>?,
    selectLibraryViewModel: SelectLibraryViewModel = koinViewModel<SelectLibraryViewModel>() {
        parametersOf(connectionId, thisLibraryId)
    },
    dataSourceManager: DataSourceManager = koinInject(),
) {
    val pageTitle = stringResource(Res.string.music_library)
    val allLibraryName = stringResource(Res.string.all_media_libraries)
    val libraryList = selectLibraryViewModel.libraryList
    val selectedLibraryIds = selectLibraryViewModel.libraryIds
    val dataSourceType = dataSourceManager.dataSourceType
    val dataSourceLabel = dataSourceType?.title
        ?: stringResource(Res.string.jvm_connection_config_info_screen_text_01)
    val multiSelection = dataSourceType?.ifMultiMediaLibrary == true
    val selectionModeText = if (multiSelection) {
        stringResource(Res.string.jvm_select_library_screen_text_01)
    } else {
        stringResource(Res.string.jvm_select_library_screen_text_02)
    }
    val allLibrarySelected = selectedLibraryIds.contains(Constants.MINUS_ONE_INT.toString())
    // 全部媒体库占位项不计入可切换数量，读数与设计稿保持「可见 / 可选」的比例。
    val selectableLibraryCount = libraryList.count { !it.isAllMediaLibrary() }
    val visibleLibraryCount = if (allLibrarySelected) {
        selectableLibraryCount
    } else {
        libraryList.count { !it.isAllMediaLibrary() && selectedLibraryIds.contains(it.id) }
    }
    val selectedLibraries = libraryList.filter {
        !it.isAllMediaLibrary() && selectedLibraryIds.contains(it.id)
    }
    val selectionSummary = if (allLibrarySelected || selectedLibraries.isEmpty()) {
        allLibraryName
    } else {
        selectedLibraries.joinToString("、") { it.name }
    }
    val hasInitialChanges = selectLibraryViewModel.hasInitialLibraryChanges

    JvmSettingPageScaffold {
        JvmPageHeader(
            title = pageTitle,
            trailing = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    JvmSelectLibraryReadout(
                        label = stringResource(Res.string.jvm_select_library_screen_text_31),
                        value = "$visibleLibraryCount / $selectableLibraryCount",
                    )
                    JvmSelectLibraryActionButton(
                        onClick = selectLibraryViewModel::refreshLibraryList,
                        text = stringResource(Res.string.jvm_select_library_screen_text_23),
                        icon = Res.drawable.refresh_24px,
                    )
                }
            },
        )

        // 选择器限制最大宽度并左对齐，宽窗口下保持设计稿的阅读节奏。
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.widthIn(max = JvmSelectLibraryMaxContentWidth),
                verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding * 2),
            ) {
                JvmSelectLibraryPickerHeader(
                    countText = stringResource(
                        Res.string.jvm_select_library_screen_text_34,
                        selectableLibraryCount,
                    ),
                    modeText = "$selectionModeText · $dataSourceLabel",
                )

                JvmSelectLibraryList(
                    libraries = libraryList,
                    selectedLibraryIds = selectedLibraryIds,
                    allLibraryName = allLibraryName,
                    multiSelection = multiSelection,
                    onLibraryClick = { library ->
                        selectLibraryViewModel.updateLibraryId(library.id)
                    },
                )

                JvmSelectLibraryFooter(
                    summary = stringResource(
                        Res.string.jvm_select_library_screen_text_33,
                        selectionSummary,
                    ),
                    restoreEnabled = hasInitialChanges,
                    onRestore = selectLibraryViewModel::restoreInitialLibraryIds,
                )
            }
        }
    }
}

/**
 * 页面头部读数单元格：等宽小标签加等宽数值。
 *
 * @param label 读数标签。
 * @param value 读数内容。
 */
@Composable
private fun JvmSelectLibraryReadout(
    label: String,
    value: String,
) {
    Column(
        modifier = Modifier.widthIn(max = 180.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalAlignment = Alignment.End,
    ) {
        XyTextSub(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                letterSpacing = JvmSelectLibraryKickerLetterSpacing,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
        XyText(
            text = value,
            style = MaterialTheme.typography.labelLarge.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )
    }
}

/**
 * 页面内紧凑操作按钮，对齐设计稿的小号描边按钮。
 *
 * @param onClick 点击回调。
 * @param text 按钮文案。
 * @param icon 左侧图标资源。
 * @param enabled 是否启用按钮。
 */
@Composable
private fun JvmSelectLibraryActionButton(
    onClick: () -> Unit,
    text: String,
    icon: DrawableResource,
    enabled: Boolean = true,
) {
    XyIconTextButton(
        modifier = Modifier.height(32.dp),
        onClick = onClick,
        text = text,
        icon = icon,
        enabled = enabled,
        color = MaterialTheme.colorScheme.onSurface,
        backgroundColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
        borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        paddingValues = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
        textStyle = MaterialTheme.typography.labelSmall,
    )
}

/**
 * 媒体库选择器头部：左侧可见数量，右侧当前选择模式。
 *
 * @param countText 可见媒体库数量文案。
 * @param modeText 当前单选或多选模式文案。
 */
@Composable
private fun JvmSelectLibraryPickerHeader(
    countText: String,
    modeText: String,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = XyTheme.dimens.contentPadding),
            horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            XyTextSub(
                modifier = Modifier.weight(1f),
                text = countText,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = JvmSelectLibraryKickerLetterSpacing,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
            JvmSelectLibraryTag(text = modeText)
        }
        // 头部下边线，与设计稿选择器标题的分隔线保持一致。
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.09f))
        )
    }
}

/**
 * 媒体库列表内容。
 *
 * @param libraries 当前可选媒体库列表。
 * @param selectedLibraryIds 当前选中媒体库 ID 集合。
 * @param allLibraryName 全部媒体库本地化名称。
 * @param multiSelection 当前数据源是否支持多选。
 * @param onLibraryClick 点击媒体库回调。
 */
@Composable
private fun JvmSelectLibraryList(
    libraries: List<XyLibrary>,
    selectedLibraryIds: Set<String>,
    allLibraryName: String,
    multiSelection: Boolean,
    onLibraryClick: (XyLibrary) -> Unit,
) {
    if (libraries.isEmpty()) {
        JvmSelectLibraryEmptyState()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = XyTheme.dimens.outerVerticalPadding),
        verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding),
    ) {
        libraries.forEach { library ->
            JvmSelectLibraryRow(
                library = library,
                selected = selectedLibraryIds.contains(library.id),
                allLibraryName = allLibraryName,
                multiSelection = multiSelection,
                onClick = { onLibraryClick(library) },
            )
        }
    }
}

/**
 * 媒体库列表的单行。
 *
 * 左侧是单选/多选指示器，中间是媒体库名称与类型标签，右侧是选中确认圆点；
 * 选中行使用主色描边、浅色底和左侧强调条，悬停时提升描边与底色对比。
 *
 * @param library 当前媒体库。
 * @param selected 当前行是否选中。
 * @param allLibraryName 全部媒体库本地化名称。
 * @param multiSelection 当前数据源是否支持多选。
 * @param onClick 点击当前行回调。
 */
@Composable
private fun JvmSelectLibraryRow(
    library: XyLibrary,
    selected: Boolean,
    allLibraryName: String,
    multiSelection: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val colorScheme = MaterialTheme.colorScheme
    val accentColor = colorScheme.primary
    val isAllLibrary = library.isAllMediaLibrary()
    val shape = RoundedCornerShape(XyTheme.dimens.corner - XyTheme.dimens.outerVerticalPadding / 2)
    val borderColor = when {
        selected -> accentColor.copy(alpha = 0.55f)
        hovered -> colorScheme.onSurface.copy(alpha = 0.16f)
        else -> colorScheme.onSurface.copy(alpha = 0.09f)
    }
    val containerColor = when {
        selected -> accentColor.copy(alpha = if (XyTheme.configs.isDarkTheme) 0.10f else 0.07f)
        hovered -> colorScheme.onSurface.copy(alpha = 0.05f)
        else -> Color.Transparent
    }
    val selectorRole = if (multiSelection) Role.Checkbox else Role.RadioButton

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = JvmSelectLibraryRowMinHeight)
            .clip(shape)
            .background(containerColor)
            .border(BorderStroke(1.dp, borderColor), shape)
            .hoverable(interactionSource = interactionSource)
            .pointerHoverIcon(PointerIcon.Hand)
            .selectable(
                selected = selected,
                interactionSource = interactionSource,
                indication = null,
                role = selectorRole,
                onClick = onClick,
            ),
    ) {
        // 选中行左侧的强调竖条，上下留出内缩避免贴合圆角。
        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxHeight()
                    .padding(vertical = 14.dp)
                    .width(JvmSelectLibraryAccentWidth)
                    .background(accentColor, RoundedCornerShape(2.dp))
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = XyTheme.dimens.outerHorizontalPadding, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            JvmSelectLibrarySelector(
                selected = selected,
                multiSelection = multiSelection,
            )
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                XyText(
                    modifier = Modifier.weight(1f, fill = false),
                    text = if (isAllLibrary) allLibraryName else library.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                JvmSelectLibraryTag(
                    text = if (isAllLibrary) {
                        stringResource(Res.string.jvm_select_library_screen_text_32)
                    } else {
                        library.collectionType.ifBlank { stringResource(Res.string.music_library) }
                    },
                )
                if (selected) {
                    JvmSelectLibraryTag(
                        text = stringResource(Res.string.current_connection),
                        accent = true,
                    )
                }
            }
            JvmSelectLibraryCheckBadge(selected = selected)
        }
    }
}

/**
 * 媒体库行首选择指示器。
 *
 * 单选使用圆形点选样式，多选使用圆角方形勾选样式，尺寸与设计稿一致。
 *
 * @param selected 当前是否选中。
 * @param multiSelection 是否展示多选样式。
 */
@Composable
private fun JvmSelectLibrarySelector(
    selected: Boolean,
    multiSelection: Boolean,
) {
    val accentColor = MaterialTheme.colorScheme.primary
    val strokeColor = if (selected) {
        accentColor
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f)
    }

    if (multiSelection) {
        val shape = RoundedCornerShape(4.dp)
        Box(
            modifier = Modifier
                .size(JvmSelectLibrarySelectorSize)
                .clip(shape)
                .background(if (selected) accentColor else Color.Transparent)
                .border(BorderStroke(1.dp, strokeColor), shape),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Icon(
                    painter = painterResource(Res.drawable.check_24px),
                    contentDescription = null,
                    modifier = Modifier.size(10.dp),
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    } else {
        Box(
            modifier = Modifier
                .size(JvmSelectLibrarySelectorSize)
                .clip(CircleShape)
                .border(BorderStroke(1.dp, strokeColor), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            // 未选中时圆点保持透明，选中时以主色填充形成实心圆。
            Box(
                modifier = Modifier
                    .size(JvmSelectLibrarySelectorDotSize)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = if (selected) 1f else 0f))
            )
        }
    }
}

/**
 * 媒体库行尾的选中确认圆点。
 *
 * @param selected 当前是否选中。
 */
@Composable
private fun JvmSelectLibraryCheckBadge(selected: Boolean) {
    val accentColor = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .size(JvmSelectLibraryCheckSize)
            .clip(CircleShape)
            .background(if (selected) accentColor else Color.Transparent)
            .border(
                BorderStroke(
                    width = 1.dp,
                    color = if (selected) accentColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.09f)
                ),
                CircleShape
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(Res.drawable.check_24px),
            contentDescription = null,
            modifier = Modifier.size(11.dp),
            tint = if (selected) MaterialTheme.colorScheme.onPrimary else Color.Transparent,
        )
    }
}

/**
 * 媒体库行内的小标签。
 *
 * @param text 标签文本。
 * @param accent 是否使用主色强调。
 * @param maxWidth 标签最大宽度。
 */
@Composable
private fun JvmSelectLibraryTag(
    text: String,
    accent: Boolean = false,
    maxWidth: Dp = 180.dp,
) {
    val color = if (accent) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = if (accent) color.copy(alpha = 0.12f) else Color.Transparent,
        contentColor = color,
        border = BorderStroke(
            width = 1.dp,
            color = color.copy(alpha = if (accent) 0.45f else 0.14f)
        )
    ) {
        XyTextSub(
            modifier = Modifier
                .widthIn(max = maxWidth)
                .padding(horizontal = 6.dp, vertical = 2.dp),
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                letterSpacing = 0.6.sp,
            ),
            color = color,
            maxLines = 1,
        )
    }
}

/**
 * 媒体库列表底部的范围摘要与恢复操作。
 *
 * @param summary 当前生效范围摘要文案。
 * @param restoreEnabled 是否允许恢复到进入页面时的范围。
 * @param onRestore 恢复初始范围回调。
 */
@Composable
private fun JvmSelectLibraryFooter(
    summary: String,
    restoreEnabled: Boolean,
    onRestore: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        XyTextSub(
            modifier = Modifier.weight(1f),
            text = summary,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
        )
        JvmSelectLibraryActionButton(
            onClick = onRestore,
            text = stringResource(Res.string.jvm_select_library_screen_text_06),
            icon = Res.drawable.restart_alt_24px,
            enabled = restoreEnabled,
        )
    }
}

/**
 * 媒体库选择页的空列表状态。
 */
@Composable
private fun JvmSelectLibraryEmptyState() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 180.dp),
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)
        )
    ) {
        Column(
            modifier = Modifier.padding(XyTheme.dimens.outerHorizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter = painterResource(Res.drawable.folder_managed_24px),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            XyTextSub(
                modifier = Modifier.padding(top = XyTheme.dimens.contentPadding),
                text = stringResource(Res.string.jvm_select_library_screen_text_27),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * 判断当前媒体库是否为“全部媒体库”占位项。
 */
private fun XyLibrary.isAllMediaLibrary(): Boolean {
    return id == Constants.MINUS_ONE_INT.toString()
}
