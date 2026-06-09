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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.xybbz.common.enums.ConnectionUiType
import cn.xybbz.common.enums.img
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.localdata.data.connection.ConnectionConfig
import cn.xybbz.router.Connection
import cn.xybbz.router.ConnectionInfo
import cn.xybbz.router.SelectLibrary
import cn.xybbz.ui.components.AlertDialogObject
import cn.xybbz.ui.components.JvmSettingBaseRow
import cn.xybbz.ui.components.JvmSettingConnectionAvatarSize
import cn.xybbz.ui.components.JvmSettingConnectionBoardTwoPaneBreakpoint
import cn.xybbz.ui.components.JvmSettingConnectionCardMinWidth
import cn.xybbz.ui.components.JvmSettingFlowRow
import cn.xybbz.ui.components.JvmSettingOverviewTile
import cn.xybbz.ui.components.JvmSettingOverviewThreeColumnWidth
import cn.xybbz.ui.components.JvmSettingPageContentMaxWidth
import cn.xybbz.ui.components.JvmSettingPageHeader
import cn.xybbz.ui.components.JvmSettingPageScaffold
import cn.xybbz.ui.components.JvmSettingResponsiveRow
import cn.xybbz.ui.components.JvmSettingSection
import cn.xybbz.ui.components.show
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.ext.jvmHoverDebounceClickable
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.windows.DesktopTooltipIconButton
import cn.xybbz.ui.xy.XyIconTextButton
import cn.xybbz.ui.xy.XySmallImage
import cn.xybbz.ui.xy.XyText
import cn.xybbz.ui.xy.XyTextLarge
import cn.xybbz.ui.xy.XyTextSub
import cn.xybbz.ui.xy.XyTextSubSmall
import cn.xybbz.viewmodel.ConnectionManagementViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.add_connection
import xymusic_kmp.composeapp.generated.resources.check_24px
import xymusic_kmp.composeapp.generated.resources.chinese_list_separator
import xymusic_kmp.composeapp.generated.resources.confirm_delete_connection
import xymusic_kmp.composeapp.generated.resources.connection_media_library_all_label
import xymusic_kmp.composeapp.generated.resources.connection_media_library_label
import xymusic_kmp.composeapp.generated.resources.connection_permission_read_only
import xymusic_kmp.composeapp.generated.resources.connection_permissions_label
import xymusic_kmp.composeapp.generated.resources.connection_server_version_label
import xymusic_kmp.composeapp.generated.resources.connection_server_version_unknown_label
import xymusic_kmp.composeapp.generated.resources.connection_settings_list
import xymusic_kmp.composeapp.generated.resources.current_connection
import xymusic_kmp.composeapp.generated.resources.delete_24px
import xymusic_kmp.composeapp.generated.resources.delete_connection
import xymusic_kmp.composeapp.generated.resources.delete_prefix
import xymusic_kmp.composeapp.generated.resources.download
import xymusic_kmp.composeapp.generated.resources.download_24px
import xymusic_kmp.composeapp.generated.resources.edit_24px
import xymusic_kmp.composeapp.generated.resources.folder_managed_24px
import xymusic_kmp.composeapp.generated.resources.http_24px
import xymusic_kmp.composeapp.generated.resources.info_24px
import xymusic_kmp.composeapp.generated.resources.library_add_24px
import xymusic_kmp.composeapp.generated.resources.modify_connection
import xymusic_kmp.composeapp.generated.resources.music_library
import xymusic_kmp.composeapp.generated.resources.warning

/**
 * 连接设置列表。
 *
 * @param connectionManagementViewModel 连接管理 ViewModel。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JvmConnectionManagement(
    connectionManagementViewModel: ConnectionManagementViewModel = koinViewModel<ConnectionManagementViewModel>()
) {
    val navigator = LocalNavigator.current
    val coroutineScope = rememberCoroutineScope()
    val connections = connectionManagementViewModel.connectionList
    val currentConnectionId = connectionManagementViewModel.connectionId
    val currentConnectionText = stringResource(Res.string.current_connection)
    val readOnlyText = stringResource(Res.string.connection_permission_read_only)
    val warningText = stringResource(Res.string.warning)
    val deleteText = stringResource(Res.string.delete_prefix)
    val downloadText = stringResource(Res.string.download)
    val separatorText = stringResource(Res.string.chinese_list_separator)
    val addConnectionText = stringResource(Res.string.add_connection)
    val allLibraryText = stringResource(Res.string.connection_media_library_all_label)
    val unknownVersionText = stringResource(Res.string.connection_server_version_unknown_label)
    val confirmDeleteText = stringResource(Res.string.confirm_delete_connection)
    val displayItems = connections.map { connectionConfig ->
        val selectedLibraryText = connectionManagementViewModel.selectedLibraryNames(connectionConfig)
            ?.joinToString(separatorText)
            ?.takeIf { it.isNotBlank() }
        val serverVersionText = connectionConfig.serverVersion.takeIf { it.isNotBlank() }
        val capabilityValue = buildList {
            if (connectionConfig.ifEnabledDownload) add(downloadText)
            if (connectionConfig.ifEnabledDelete) add(deleteText)
        }.takeIf { it.isNotEmpty() }?.joinToString(" / ") ?: readOnlyText

        connectionConfig.toJvmConnectionDisplayItem(
            selected = currentConnectionId == connectionConfig.id,
            libraryLabel = selectedLibraryText
                ?.let { stringResource(Res.string.connection_media_library_label, it) }
                ?: allLibraryText,
            libraryValue = selectedLibraryText ?: "全部",
            serverVersionLabel = serverVersionText
                ?.let { stringResource(Res.string.connection_server_version_label, it) }
                ?: unknownVersionText,
            serverVersionValue = serverVersionText ?: "未知",
            capabilityLabel = stringResource(Res.string.connection_permissions_label, capabilityValue),
            capabilityValue = capabilityValue,
            currentConnectionText = currentConnectionText,
        )
    }
    val currentDisplayItem = displayItems.firstOrNull { it.selected } ?: displayItems.firstOrNull()

    JvmSettingPageScaffold(
        contentMaxWidth = JvmSettingPageContentMaxWidth,
        contentPadding = PaddingValues(
            horizontal = XyTheme.dimens.outerHorizontalPadding * 2,
            vertical = XyTheme.dimens.outerVerticalPadding * 3
        )
    ) {
        JvmSettingPageHeader(
            title = stringResource(Res.string.connection_settings_list),
            description = "集中管理桌面端音乐服务连接：查看当前数据源、切换连接、编辑账号地址，并为每个连接维护可见媒体库范围。",
            contentMaxWidth = JvmSettingPageContentMaxWidth,
        ) {
            XyIconTextButton(
                modifier = Modifier.widthIn(min = 128.dp),
                onClick = {
                    navigator.navigate(Connection(connectionUiType = ConnectionUiType.ADD_CONNECTION))
                },
                text = addConnectionText,
                icon = Res.drawable.library_add_24px,
            )
        }

        JvmConnectionOverview(
            currentDisplayItem = currentDisplayItem,
            connectionCount = connections.size,
        )

        JvmSettingSection(
            title = "连接管理",
            subtitle = "卡片承载连接身份与状态，右侧保留当前连接的详细信息和常用操作。",
            badge = "${connections.size} 个连接",
            contentContainerEnabled = false,
        ) {
            JvmSettingResponsiveRow(
                breakpoint = JvmSettingConnectionBoardTwoPaneBreakpoint,
                leftWeight = 2.0f,
                rightWeight = 0.8f,
                horizontalGap = XyTheme.dimens.outerHorizontalPadding,
                verticalGap = XyTheme.dimens.outerVerticalPadding * 2,
                left = {
                    JvmConnectionCardGrid(
                        displayItems = displayItems,
                        onSelectConnection = connectionManagementViewModel::changeDataSource,
                        onEditConnection = { connectionConfig ->
                            navigator.navigate(ConnectionInfo(connectionConfig.id))
                        },
                        onSelectLibrary = { connectionConfig ->
                            navigator.navigate(
                                SelectLibrary(
                                    connectionConfig.id,
                                    connectionConfig.libraryIds
                                )
                            )
                        },
                        onDeleteConnection = { connectionConfig ->
                            JvmConnectionDeleteDialog(
                                warning = warningText,
                                content = confirmDeleteText,
                                onDelete = {
                                    coroutineScope.launch {
                                        connectionManagementViewModel.removeConnection(connectionConfig.id)
                                    }
                                },
                            )
                        },
                    )
                },
                right = {
                    JvmConnectionDetailPane(
                        displayItem = currentDisplayItem,
                        onEditConnection = { connectionConfig ->
                            navigator.navigate(ConnectionInfo(connectionConfig.id))
                        },
                        onSelectLibrary = { connectionConfig ->
                            navigator.navigate(
                                SelectLibrary(
                                    connectionConfig.id,
                                    connectionConfig.libraryIds
                                )
                            )
                        },
                    )
                }
            )
        }
    }
}

/**
 * JVM 连接管理页顶部概览卡片。
 *
 * @param currentDisplayItem 当前连接展示项。
 * @param connectionCount 当前连接数量。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun JvmConnectionOverview(
    currentDisplayItem: JvmConnectionDisplayItem?,
    connectionCount: Int,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val gap = XyTheme.dimens.contentPadding
        val useThreeColumns = maxWidth >= JvmSettingOverviewThreeColumnWidth
        val contentWidth = minOf(maxWidth, JvmSettingPageContentMaxWidth)
        val tileWidth = if (useThreeColumns) {
            (contentWidth - gap * 2) / 3f
        } else {
            maxWidth
        }

        JvmSettingFlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(gap),
        ) {
            JvmSettingOverviewTile(
                modifier = Modifier.width(tileWidth),
                icon = Res.drawable.http_24px,
                kicker = "当前连接",
                value = currentDisplayItem?.config?.type?.title ?: "未连接",
                sub = currentDisplayItem?.config?.address ?: "添加连接后可切换数据源",
            )
            JvmSettingOverviewTile(
                modifier = Modifier.width(tileWidth),
                icon = Res.drawable.folder_managed_24px,
                kicker = "媒体库范围",
                value = currentDisplayItem?.libraryValue ?: "暂无",
                sub = currentDisplayItem?.title ?: "${connectionCount} 个连接",
            )
            JvmSettingOverviewTile(
                modifier = Modifier.width(tileWidth),
                icon = Res.drawable.download_24px,
                kicker = "权限能力",
                value = currentDisplayItem?.capabilityValue ?: "只读",
                sub = currentDisplayItem?.serverVersionLabel ?: "服务端未知",
            )
        }
    }
}

/**
 * JVM 连接管理页卡片网格。
 *
 * @param displayItems 连接展示项列表。
 * @param onSelectConnection 切换连接回调。
 * @param onEditConnection 编辑连接回调。
 * @param onSelectLibrary 选择媒体库回调。
 * @param onDeleteConnection 删除连接回调。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun JvmConnectionCardGrid(
    displayItems: List<JvmConnectionDisplayItem>,
    onSelectConnection: (ConnectionConfig) -> Unit,
    onEditConnection: (ConnectionConfig) -> Unit,
    onSelectLibrary: (ConnectionConfig) -> Unit,
    onDeleteConnection: (ConnectionConfig) -> Unit,
) {
    if (displayItems.isEmpty()) {
        JvmConnectionEmptyState()
        return
    }

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val gap = XyTheme.dimens.contentPadding
        val useTwoColumns = maxWidth >= JvmSettingConnectionCardMinWidth * 2 + gap

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(gap),
        ) {
            if (useTwoColumns) {
                displayItems.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(gap),
                    ) {
                        rowItems.forEach { displayItem ->
                            JvmConnectionCard(
                                modifier = Modifier.weight(1f),
                                displayItem = displayItem,
                                onSelectConnection = onSelectConnection,
                                onEditConnection = onEditConnection,
                                onSelectLibrary = onSelectLibrary,
                                onDeleteConnection = onDeleteConnection,
                            )
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            } else {
                displayItems.forEach { displayItem ->
                    JvmConnectionCard(
                        modifier = Modifier.fillMaxWidth(),
                        displayItem = displayItem,
                        onSelectConnection = onSelectConnection,
                        onEditConnection = onEditConnection,
                        onSelectLibrary = onSelectLibrary,
                        onDeleteConnection = onDeleteConnection,
                    )
                }
            }
        }
    }
}

/**
 * JVM 连接管理页单个连接卡片。
 *
 * @param modifier 外层布局修饰符。
 * @param displayItem 连接展示项。
 * @param onSelectConnection 切换连接回调。
 * @param onEditConnection 编辑连接回调。
 * @param onSelectLibrary 选择媒体库回调。
 * @param onDeleteConnection 删除连接回调。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun JvmConnectionCard(
    modifier: Modifier = Modifier,
    displayItem: JvmConnectionDisplayItem,
    onSelectConnection: (ConnectionConfig) -> Unit,
    onEditConnection: (ConnectionConfig) -> Unit,
    onSelectLibrary: (ConnectionConfig) -> Unit,
    onDeleteConnection: (ConnectionConfig) -> Unit,
) {
    val shape = RoundedCornerShape(XyTheme.dimens.corner)
    val colorScheme = MaterialTheme.colorScheme
    val selected = displayItem.selected
    val containerColor = if (selected) {
        colorScheme.primary.copy(alpha = if (XyTheme.configs.isDarkTheme) 0.18f else 0.10f)
    } else {
        colorScheme.onSurface.copy(alpha = 0.05f)
    }
    val borderColor = if (selected) {
        colorScheme.primary.copy(alpha = 0.36f)
    } else {
        colorScheme.onSurface.copy(alpha = 0.08f)
    }
    val clickModifier = if (selected) {
        Modifier
    } else {
        Modifier.jvmHoverDebounceClickable {
            onSelectConnection(displayItem.config)
        }
    }

    Surface(
        modifier = modifier
            .heightIn(min = 174.dp)
            .then(clickModifier),
        shape = shape,
        color = containerColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(XyTheme.dimens.outerHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                JvmConnectionAvatar(
                    connectionConfig = displayItem.config,
                    selected = selected,
                    size = JvmSettingConnectionAvatarSize,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding / 2)
                ) {
                    XyText(
                        text = displayItem.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    XyTextSub(
                        text = displayItem.config.address,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 18.sp
                        ),
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (selected) {
                    JvmConnectionCurrentPill(text = displayItem.status)
                }
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding),
                verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding),
            ) {
                JvmConnectionInfoChip(text = displayItem.libraryLabel, selected = true)
                JvmConnectionInfoChip(text = displayItem.serverVersionLabel)
                JvmConnectionInfoChip(text = displayItem.capabilityLabel)
            }

            Spacer(modifier = Modifier.weight(1f))

            JvmConnectionCardActions(
                displayItem = displayItem,
                onEditConnection = onEditConnection,
                onSelectLibrary = onSelectLibrary,
                onDeleteConnection = onDeleteConnection,
            )
        }
    }
}

/**
 * JVM 连接卡片底部操作按钮。
 *
 * @param displayItem 连接展示项。
 * @param onEditConnection 编辑连接回调。
 * @param onSelectLibrary 选择媒体库回调。
 * @param onDeleteConnection 删除连接回调。
 */
@Composable
private fun JvmConnectionCardActions(
    displayItem: JvmConnectionDisplayItem,
    onEditConnection: (ConnectionConfig) -> Unit,
    onSelectLibrary: (ConnectionConfig) -> Unit,
    onDeleteConnection: (ConnectionConfig) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        DesktopTooltipIconButton(
            tooltip = stringResource(Res.string.modify_connection),
            onClick = composeClick {
                onEditConnection(displayItem.config)
            },
        ) {
            Icon(
                painter = painterResource(Res.drawable.edit_24px),
                contentDescription = stringResource(Res.string.modify_connection),
            )
        }
        DesktopTooltipIconButton(
            tooltip = stringResource(Res.string.music_library),
            onClick = composeClick {
                onSelectLibrary(displayItem.config)
            },
        ) {
            Icon(
                painter = painterResource(Res.drawable.folder_managed_24px),
                contentDescription = stringResource(Res.string.music_library),
            )
        }
        DesktopTooltipIconButton(
            tooltip = stringResource(Res.string.delete_connection),
            onClick = composeClick {
                onDeleteConnection(displayItem.config)
            },
        ) {
            Icon(
                painter = painterResource(Res.drawable.delete_24px),
                contentDescription = stringResource(Res.string.delete_connection),
                tint = MaterialTheme.colorScheme.error,
            )
        }
    }
}

/**
 * JVM 连接详情侧栏。
 *
 * @param displayItem 当前连接展示项。
 * @param onEditConnection 编辑连接回调。
 * @param onSelectLibrary 选择媒体库回调。
 */
@Composable
private fun JvmConnectionDetailPane(
    displayItem: JvmConnectionDisplayItem?,
    onEditConnection: (ConnectionConfig) -> Unit,
    onSelectLibrary: (ConnectionConfig) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        )
    ) {
        if (displayItem == null) {
            JvmConnectionDetailEmptyState()
        } else {
            Column(
                modifier = Modifier.padding(XyTheme.dimens.outerHorizontalPadding),
                verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding)
            ) {
                JvmConnectionDetailHeader(displayItem = displayItem)
                JvmConnectionDetailRows(displayItem = displayItem)
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
                        icon = Res.drawable.edit_24px,
                        title = stringResource(Res.string.modify_connection),
                        description = "地址、账号和服务类型",
                        onClick = {
                            onEditConnection(displayItem.config)
                        },
                        trailing = {}
                    )
                    JvmSettingBaseRow(
                        icon = Res.drawable.folder_managed_24px,
                        title = stringResource(Res.string.music_library),
                        description = "选择可见媒体库",
                        onClick = {
                            onSelectLibrary(displayItem.config)
                        },
                        trailing = {}
                    )
                }
            }
        }
    }
}

/**
 * JVM 连接详情头部。
 *
 * @param displayItem 当前连接展示项。
 */
@Composable
private fun JvmConnectionDetailHeader(displayItem: JvmConnectionDisplayItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        JvmConnectionAvatar(
            connectionConfig = displayItem.config,
            selected = true,
            size = JvmSettingConnectionAvatarSize,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding / 2)
        ) {
            XyTextLarge(
                text = displayItem.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            XyTextSub(
                text = displayItem.config.address,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 18.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * JVM 连接详情信息行集合。
 *
 * @param displayItem 当前连接展示项。
 */
@Composable
private fun JvmConnectionDetailRows(displayItem: JvmConnectionDisplayItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(XyTheme.dimens.corner))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)),
                RoundedCornerShape(XyTheme.dimens.corner)
            )
            .padding(XyTheme.dimens.contentPadding),
        verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding)
    ) {
        JvmConnectionDetailRow(label = "媒体库", value = displayItem.libraryValue)
        JvmConnectionDetailRow(label = "服务端", value = displayItem.serverVersionValue)
        JvmConnectionDetailRow(label = "权限", value = displayItem.capabilityValue)
        JvmConnectionDetailRow(label = "状态", value = displayItem.status)
    }
}

/**
 * JVM 连接详情单行。
 *
 * @param label 行标题。
 * @param value 行值。
 */
@Composable
private fun JvmConnectionDetailRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        XyTextSub(
            modifier = Modifier.weight(1f),
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        XyText(
            modifier = Modifier.widthIn(max = 220.dp),
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * JVM 连接服务头像。
 *
 * @param connectionConfig 连接配置。
 * @param selected 是否为当前连接。
 * @param size 头像尺寸。
 */
@Composable
private fun JvmConnectionAvatar(
    connectionConfig: ConnectionConfig,
    selected: Boolean,
    size: Dp,
) {
    val shape = RoundedCornerShape(XyTheme.dimens.corner)
    val accentColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(shape)
            .background(accentColor.copy(alpha = if (selected) 0.18f else 0.10f))
            .border(
                BorderStroke(1.dp, accentColor.copy(alpha = if (selected) 0.30f else 0.12f)),
                shape
            ),
        contentAlignment = Alignment.Center
    ) {
        XySmallImage(
            modifier = Modifier
                .size(size * 0.68f)
                .alpha(if (selected) 1f else 0.72f),
            model = painterResource(connectionConfig.type.img),
            contentDescription = connectionConfig.type.title,
        )
    }
}

/**
 * JVM 当前连接状态标签。
 *
 * @param text 标签文案。
 */
@Composable
private fun JvmConnectionCurrentPill(text: String) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
        contentColor = MaterialTheme.colorScheme.primary,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
        )
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = XyTheme.dimens.contentPadding,
                vertical = XyTheme.dimens.outerVerticalPadding / 2
            ),
            horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding / 2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(Res.drawable.check_24px),
                contentDescription = null,
                modifier = Modifier.size(14.dp),
            )
            XyTextSubSmall(
                text = text,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
            )
        }
    }
}

/**
 * JVM 连接信息标签。
 *
 * @param text 标签文案。
 * @param selected 是否使用强调色。
 */
@Composable
private fun JvmConnectionInfoChip(
    text: String,
    selected: Boolean = false,
) {
    val color = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = CircleShape,
        color = color.copy(alpha = if (selected) 0.12f else 0.07f),
        contentColor = color,
        border = BorderStroke(
            width = 1.dp,
            color = color.copy(alpha = if (selected) 0.24f else 0.08f)
        )
    ) {
        XyTextSubSmall(
            modifier = Modifier
                .widthIn(max = 240.dp)
                .padding(
                    horizontal = XyTheme.dimens.contentPadding,
                    vertical = XyTheme.dimens.outerVerticalPadding / 2
                ),
            text = text,
            color = color,
            maxLines = 1,
        )
    }
}

/**
 * JVM 连接列表空状态。
 */
@Composable
private fun JvmConnectionEmptyState() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 220.dp),
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
            verticalArrangement = Arrangement.Center
        ) {
            JvmConnectionEmptyIcon(icon = Res.drawable.library_add_24px)
            Spacer(modifier = Modifier.height(XyTheme.dimens.contentPadding))
            XyText(
                text = "还没有连接",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            XyTextSub(
                text = "添加连接后可以在这里切换数据源并管理媒体库范围。",
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * JVM 连接详情空状态。
 */
@Composable
private fun JvmConnectionDetailEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 220.dp)
            .padding(XyTheme.dimens.outerHorizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        JvmConnectionEmptyIcon(icon = Res.drawable.info_24px)
        Spacer(modifier = Modifier.height(XyTheme.dimens.contentPadding))
        XyText(
            text = "暂无当前连接",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        XyTextSub(
            text = "选择或添加连接后，这里会展示服务端、权限和媒体库信息。",
            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * JVM 连接空状态图标。
 *
 * @param icon 图标资源。
 */
@Composable
private fun JvmConnectionEmptyIcon(icon: DrawableResource) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                shape = RoundedCornerShape(XyTheme.dimens.corner)
            )
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.26f)),
                RoundedCornerShape(XyTheme.dimens.corner)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
        )
    }
}

/**
 * JVM 连接删除确认弹窗。
 *
 * @param warning 警告标题文案。
 * @param connectionConfig 待删除连接配置。
 * @param onDelete 确认删除回调。
 */
private fun JvmConnectionDeleteDialog(
    warning: String,
    content: String,
    onDelete: () -> Unit,
) {
    AlertDialogObject(
        title = warning,
        content = {
            XyTextSubSmall(
                text = content
            )
        },
        ifWarning = true,
        onConfirmation = onDelete,
    ).show()
}

/**
 * JVM 连接管理页内部展示数据。
 *
 * @param config 原始连接配置。
 * @param selected 是否为当前连接。
 * @param title 卡片标题。
 * @param libraryLabel 卡片媒体库标签。
 * @param libraryValue 详情媒体库值。
 * @param serverVersionLabel 卡片服务端版本标签。
 * @param serverVersionValue 详情服务端版本值。
 * @param capabilityLabel 卡片权限标签。
 * @param capabilityValue 详情权限值。
 * @param status 当前状态文案。
 */
private data class JvmConnectionDisplayItem(
    val config: ConnectionConfig,
    val selected: Boolean,
    val title: String,
    val libraryLabel: String,
    val libraryValue: String,
    val serverVersionLabel: String,
    val serverVersionValue: String,
    val capabilityLabel: String,
    val capabilityValue: String,
    val status: String,
)

/**
 * 将连接配置转换为页面展示数据。
 *
 * @param selected 是否为当前连接。
 * @param libraryLabel 媒体库卡片标签。
 * @param libraryValue 媒体库详情值。
 * @param serverVersionLabel 服务端版本卡片标签。
 * @param serverVersionValue 服务端版本详情值。
 * @param capabilityLabel 权限卡片标签。
 * @param capabilityValue 权限详情值。
 * @param currentConnectionText 当前连接文案。
 */
private fun ConnectionConfig.toJvmConnectionDisplayItem(
    selected: Boolean,
    libraryLabel: String,
    libraryValue: String,
    serverVersionLabel: String,
    serverVersionValue: String,
    capabilityLabel: String,
    capabilityValue: String,
    currentConnectionText: String,
): JvmConnectionDisplayItem {
    return JvmConnectionDisplayItem(
        config = this,
        selected = selected,
        title = jvmConnectionDisplayTitle(),
        libraryLabel = libraryLabel,
        libraryValue = libraryValue,
        serverVersionLabel = serverVersionLabel,
        serverVersionValue = serverVersionValue,
        capabilityLabel = capabilityLabel,
        capabilityValue = capabilityValue,
        status = if (selected) currentConnectionText else "备用连接",
    )
}

/**
 * 生成连接卡片标题。
 */
private fun ConnectionConfig.jvmConnectionDisplayTitle(): String {
    val userPart = username
        .ifBlank { name }
        .ifBlank { "#$id" }

    return "${type.title} - $userPart"
}
