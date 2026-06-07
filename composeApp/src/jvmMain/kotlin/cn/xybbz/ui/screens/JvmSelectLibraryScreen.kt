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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.constants.Constants
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.localdata.data.library.XyLibrary
import cn.xybbz.ui.components.JvmSettingFlowRow
import cn.xybbz.ui.components.JvmSettingOverviewTile
import cn.xybbz.ui.components.JvmSettingPageContentMaxWidth
import cn.xybbz.ui.components.JvmSettingPageHeader
import cn.xybbz.ui.components.JvmSettingPageScaffold
import cn.xybbz.ui.components.JvmSettingSection
import cn.xybbz.ui.components.JvmSettingStatusCard
import cn.xybbz.ui.components.JvmSettingStatusCardItem
import cn.xybbz.ui.components.JvmSettingTwoPaneContent
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.TopAppBarTitle
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyText
import cn.xybbz.ui.xy.XyTextLarge
import cn.xybbz.ui.xy.XyTextSub
import cn.xybbz.viewmodel.SelectLibraryViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.all_media_libraries
import xymusic_kmp.composeapp.generated.resources.arrow_back_24px
import xymusic_kmp.composeapp.generated.resources.back_to_connection_info
import xymusic_kmp.composeapp.generated.resources.check_24px
import xymusic_kmp.composeapp.generated.resources.folder_managed_24px
import xymusic_kmp.composeapp.generated.resources.media_library_selection
import xymusic_kmp.composeapp.generated.resources.refresh_24px
import cn.xybbz.ui.xy.XyIconButton as IconButton

/** JVM 媒体库选择页头部状态卡宽度。 */
private val JvmSelectLibrarySummaryWidth = 284.dp

/** JVM 媒体库选择页概览卡片三列断点。 */
private val JvmSelectLibraryOverviewThreeColumnWidth = 760.dp

/** JVM 媒体库列表单行最小高度。 */
private val JvmSelectLibraryRowMinHeight = 76.dp

/** JVM 媒体库头像尺寸。 */
private val JvmSelectLibraryAvatarSize = 46.dp

/**
 * JVM 桌面端媒体库选择页面。
 *
 * @param connectionId 当前连接 ID。
 * @param thisLibraryId 当前连接已保存的媒体库 ID 列表，为空表示全部媒体库。
 * @param showBackButton 是否显示独立返回栏，桌面主壳层内默认由外层导航处理。
 * @param selectLibraryViewModel 媒体库选择 ViewModel。
 * @param dataSourceManager 当前数据源管理器，用于读取数据源能力展示文案。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JvmSelectLibraryScreen(
    connectionId: Long,
    thisLibraryId: List<String>?,
    showBackButton: Boolean = true,
    selectLibraryViewModel: SelectLibraryViewModel = koinViewModel<SelectLibraryViewModel>() {
        parametersOf(connectionId, thisLibraryId)
    },
    dataSourceManager: DataSourceManager = koinInject(),
) {
    val navigator = LocalNavigator.current
    val coroutineScope = rememberCoroutineScope()
    val pageTitle = stringResource(Res.string.media_library_selection)
    val allLibraryName = stringResource(Res.string.all_media_libraries)
    val libraryList = selectLibraryViewModel.libraryList
    val selectedLibraryIds = selectLibraryViewModel.libraryIds
    val dataSourceType = dataSourceManager.dataSourceType
    val selectedLibraries = libraryList.filter { selectedLibraryIds.contains(it.id) }
    val allLibrarySelected = selectedLibraryIds.contains(Constants.MINUS_ONE_INT.toString())
    val selectedSummary = selectedLibraries.librarySelectionSummary(
        allLibrarySelected = allLibrarySelected,
        allLibraryName = allLibraryName
    )
    val selectedCountText = if (allLibrarySelected) {
        "全部"
    } else {
        "${selectedLibraries.size} 个"
    }
    val selectionModeText = if (dataSourceType?.ifMultiMediaLibrary == true) {
        "多媒体库"
    } else {
        "单媒体库"
    }

    JvmSettingPageScaffold(
        contentMaxWidth = JvmSettingPageContentMaxWidth,
        contentPadding = PaddingValues(
            horizontal = XyTheme.dimens.outerHorizontalPadding * 2,
            vertical = XyTheme.dimens.outerVerticalPadding * 3
        ),
        topBar = if (showBackButton) {
            {
                TopAppBarComponent(
                    title = {
                        TopAppBarTitle(title = pageTitle)
                    },
                    navigationIcon = {
                        IconButton(onClick = { navigator.goBack() }) {
                            Icon(
                                painter = painterResource(Res.drawable.arrow_back_24px),
                                contentDescription = stringResource(Res.string.back_to_connection_info)
                            )
                        }
                    }
                )
            }
        } else {
            null
        }
    ) {
        JvmSettingPageHeader(
            title = pageTitle,
            description = "为当前连接指定桌面端可见的媒体库范围；专辑、歌曲、搜索和播放队列都会按照这个范围加载。",
            contentMaxWidth = JvmSettingPageContentMaxWidth,
        ) {
            JvmSettingStatusCard(
                width = JvmSelectLibrarySummaryWidth,
                prominentValue = true,
                items = listOf(
                    JvmSettingStatusCardItem(label = "数据源", value = dataSourceType?.title ?: "未连接"),
                    JvmSettingStatusCardItem(label = "连接 ID", value = "#$connectionId"),
                    JvmSettingStatusCardItem(label = "当前范围", value = selectedCountText),
                )
            )
        }

        JvmSelectLibraryOverview(
            libraryCount = libraryList.size,
            selectedCountText = selectedCountText,
            selectedSummary = selectedSummary,
            selectionModeText = selectionModeText,
            allLibrarySelected = allLibrarySelected,
        )

        JvmSettingTwoPaneContent(
            leftContent = {
                JvmSettingSection(
                    title = "媒体库列表",
                    subtitle = "全部媒体库为互斥范围；具体媒体库的单选或多选由当前数据源能力决定。",
                    badge = selectionModeText,
                    contentContainerEnabled = true,
                ) {
                    JvmSelectLibraryList(
                        libraries = libraryList,
                        selectedLibraryIds = selectedLibraryIds,
                        allLibraryName = allLibraryName,
                        multiSelection = dataSourceType?.ifMultiMediaLibrary == true,
                        onLibraryClick = { library ->
                            coroutineScope.launch {
                                selectLibraryViewModel.updateLibraryId(library.id)
                            }
                        }
                    )
                }
            },
            rightContent = {
                JvmSettingSection(
                    title = "当前生效范围",
                    subtitle = "返回连接详情后，这个范围会用于后续数据加载。",
                    badge = selectedCountText,
                    contentContainerEnabled = false,
                ) {
                    JvmSelectLibrarySelectionCard(
                        selectedLibraries = selectedLibraries,
                        allLibraryName = allLibraryName,
                        allLibrarySelected = allLibrarySelected,
                    )
                }
            }
        )
    }
}

/**
 * JVM 媒体库选择页的概览卡片区域。
 *
 * @param libraryCount 当前可选媒体库数量。
 * @param selectedCountText 当前选中数量文案。
 * @param selectedSummary 当前选中范围摘要。
 * @param selectionModeText 当前数据源选择模式文案。
 * @param allLibrarySelected 当前是否选择全部媒体库。
 */
@Composable
private fun JvmSelectLibraryOverview(
    libraryCount: Int,
    selectedCountText: String,
    selectedSummary: String,
    selectionModeText: String,
    allLibrarySelected: Boolean,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val gap = XyTheme.dimens.contentPadding
        val useThreeColumns = maxWidth >= JvmSelectLibraryOverviewThreeColumnWidth
        val tileWidth = if (useThreeColumns) {
            (maxWidth - gap * 2) / 3
        } else {
            maxWidth
        }

        JvmSettingFlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(gap),
            verticalArrangement = Arrangement.spacedBy(gap),
        ) {
            JvmSettingOverviewTile(
                modifier = Modifier.width(tileWidth),
                icon = Res.drawable.folder_managed_24px,
                kicker = "可选媒体库",
                value = "${libraryCount} 个",
                sub = "来自当前服务连接",
            )
            JvmSettingOverviewTile(
                modifier = Modifier.width(tileWidth),
                icon = Res.drawable.check_24px,
                kicker = "已选择",
                value = selectedCountText,
                sub = if (allLibrarySelected) "不限制媒体库范围" else selectedSummary,
                subMaxLines = 1,
            )
            JvmSettingOverviewTile(
                modifier = Modifier.width(tileWidth),
                icon = Res.drawable.refresh_24px,
                kicker = "同步状态",
                value = "即时",
                sub = "当前模式：$selectionModeText",
            )
        }
    }
}

/**
 * JVM 媒体库列表内容。
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
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding)
    ) {
        libraries.forEach { library ->
            JvmSelectLibraryRow(
                library = library,
                selected = selectedLibraryIds.contains(library.id),
                allLibraryName = allLibraryName,
                multiSelection = multiSelection,
                onClick = { onLibraryClick(library) }
            )
        }
    }
}

/**
 * JVM 媒体库列表的单行。
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
    val isAllLibrary = library.isAllMediaLibrary()
    val shape = RoundedCornerShape(XyTheme.dimens.corner - XyTheme.dimens.outerVerticalPadding / 2)
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.42f)
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)
    }
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = if (XyTheme.configs.isDarkTheme) 0.18f else 0.10f)
    } else {
        Color.Transparent
    }
    val selectorRole = if (multiSelection) Role.Checkbox else Role.RadioButton
    val selectorDescription = if (isAllLibrary) allLibraryName else library.name

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = JvmSelectLibraryRowMinHeight)
            .selectable(
                selected = selected,
                role = selectorRole,
                onClick = onClick
            )
            .semantics {
                contentDescription = selectorDescription
            },
        shape = shape,
        color = containerColor,
        border = BorderStroke(width = 1.dp, color = borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = XyTheme.dimens.contentPadding,
                    vertical = XyTheme.dimens.outerVerticalPadding
                ),
            horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            JvmSelectLibraryAvatar(
                library = library,
                selected = selected,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding / 2)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    XyText(
                        modifier = Modifier.weight(1f, fill = false),
                        text = library.name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    JvmSelectLibraryPill(
                        text = if (isAllLibrary) "默认范围" else library.collectionType.ifBlank { "媒体库" },
                        selected = selected,
                        maxWidth = 120.dp,
                    )
                }
                XyTextSub(
                    text = library.libraryDescription(isAllLibrary = isAllLibrary),
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            JvmSelectLibrarySelector(
                selected = selected,
                multiSelection = multiSelection,
            )
        }
    }
}

/**
 * JVM 媒体库行左侧头像。
 *
 * @param library 当前媒体库。
 * @param selected 当前头像是否使用主色强调。
 */
@Composable
private fun JvmSelectLibraryAvatar(
    library: XyLibrary,
    selected: Boolean,
) {
    val isAllLibrary = library.isAllMediaLibrary()
    val accentColor = when {
        selected || isAllLibrary -> MaterialTheme.colorScheme.primary
        library.name.hashCode() % 3 == 0 -> MaterialTheme.colorScheme.tertiary
        library.name.hashCode() % 3 == 1 -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.primary
    }
    val avatarText = if (isAllLibrary) {
        "全"
    } else {
        library.name.firstOrNull()?.toString().orEmpty().ifBlank { "库" }
    }

    Box(
        modifier = Modifier
            .size(JvmSelectLibraryAvatarSize)
            .clip(RoundedCornerShape(XyTheme.dimens.corner))
            .background(accentColor.copy(alpha = if (selected || isAllLibrary) 0.22f else 0.14f))
            .border(
                BorderStroke(1.dp, accentColor.copy(alpha = if (selected) 0.42f else 0.22f)),
                RoundedCornerShape(XyTheme.dimens.corner)
            ),
        contentAlignment = Alignment.Center
    ) {
        XyText(
            text = avatarText,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = accentColor,
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
    }
}

/**
 * JVM 媒体库行尾选择控件。
 *
 * @param selected 当前是否选中。
 * @param multiSelection 是否展示多选样式。
 */
@Composable
private fun JvmSelectLibrarySelector(
    selected: Boolean,
    multiSelection: Boolean,
) {
    if (multiSelection) {
        Checkbox(
            checked = selected,
            onCheckedChange = null,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                checkmarkColor = MaterialTheme.colorScheme.onPrimary
            )
        )
    } else {
        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

/**
 * JVM 媒体库当前选择摘要卡片。
 *
 * @param selectedLibraries 当前已选媒体库。
 * @param allLibraryName 全部媒体库本地化名称。
 * @param allLibrarySelected 当前是否选择全部媒体库。
 */
@Composable
private fun JvmSelectLibrarySelectionCard(
    selectedLibraries: List<XyLibrary>,
    allLibraryName: String,
    allLibrarySelected: Boolean,
) {
    val selectedSummary = selectedLibraries.librarySelectionSummary(
        allLibrarySelected = allLibrarySelected,
        allLibraryName = allLibraryName
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        color = MaterialTheme.colorScheme.primary.copy(alpha = if (XyTheme.configs.isDarkTheme) 0.15f else 0.08f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
        )
    ) {
        Column(
            modifier = Modifier.padding(XyTheme.dimens.outerHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding)
        ) {
            XyText(
                text = "可见内容",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            XyTextLarge(
                text = selectedSummary,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            XyTextSub(
                text = if (allLibrarySelected) {
                    "使用服务端返回的完整媒体库范围。"
                } else {
                    "仅加载已选择媒体库中的音乐、专辑和播放内容。"
                },
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            JvmSelectLibraryTagList(
                selectedLibraries = selectedLibraries,
                allLibraryName = allLibraryName,
                allLibrarySelected = allLibrarySelected,
            )
        }
    }
}

/**
 * JVM 媒体库选择摘要标签列表。
 *
 * @param selectedLibraries 当前已选媒体库。
 * @param allLibraryName 全部媒体库本地化名称。
 * @param allLibrarySelected 当前是否选择全部媒体库。
 */
@Composable
private fun JvmSelectLibraryTagList(
    selectedLibraries: List<XyLibrary>,
    allLibraryName: String,
    allLibrarySelected: Boolean,
) {
    val tagNames = if (allLibrarySelected || selectedLibraries.isEmpty()) {
        listOf(allLibraryName)
    } else {
        selectedLibraries.map { it.name }
    }

    JvmSettingFlowRow(
        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding),
        verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding),
    ) {
        tagNames.forEach { tagName ->
            JvmSelectLibraryPill(
                text = tagName,
                selected = true,
                maxWidth = 180.dp,
            )
        }
    }
}

/**
 * JVM 媒体库选择页的空列表状态。
 */
@Composable
private fun JvmSelectLibraryEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(XyTheme.dimens.outerHorizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding)
    ) {
        Icon(
            painter = painterResource(Res.drawable.folder_managed_24px),
            contentDescription = null,
            modifier = Modifier.size(36.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        XyTextSub(
            text = "正在读取媒体库，或当前连接没有返回可选择的媒体库。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * JVM 媒体库选择页的小标签。
 *
 * @param text 标签文本。
 * @param selected 是否使用主色强调。
 * @param maxWidth 标签最大宽度。
 */
@Composable
private fun JvmSelectLibraryPill(
    text: String,
    selected: Boolean = false,
    maxWidth: Dp = 128.dp,
) {
    val color = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = CircleShape,
        color = color.copy(alpha = if (selected) 0.14f else 0.08f),
        contentColor = color,
        border = BorderStroke(
            width = 1.dp,
            color = color.copy(alpha = if (selected) 0.28f else 0.12f)
        )
    ) {
        XyText(
            modifier = Modifier
                .widthIn(max = maxWidth)
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
 * 判断当前媒体库是否为“全部媒体库”占位项。
 */
private fun XyLibrary.isAllMediaLibrary(): Boolean {
    return id == Constants.MINUS_ONE_INT.toString()
}

/**
 * 生成媒体库行说明文本。
 *
 * @param isAllLibrary 当前媒体库是否为全部媒体库占位项。
 */
private fun XyLibrary.libraryDescription(isAllLibrary: Boolean): String {
    return if (isAllLibrary) {
        "不过滤任何音乐库 · 服务端全部内容"
    } else {
        val typeText = collectionType.ifBlank { "未标记类型" }
        "$typeText · 连接 #$connectionId · ID $id"
    }
}

/**
 * 生成当前媒体库选择摘要。
 *
 * @param allLibrarySelected 当前是否选择全部媒体库。
 * @param allLibraryName 全部媒体库本地化名称。
 */
private fun List<XyLibrary>.librarySelectionSummary(
    allLibrarySelected: Boolean,
    allLibraryName: String,
): String {
    if (allLibrarySelected || isEmpty()) {
        return allLibraryName
    }
    return joinToString("、") { it.name }
}
