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
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
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
import cn.xybbz.ui.components.XyDialogIcon
import cn.xybbz.ui.components.XyDialogTone
import cn.xybbz.ui.components.JvmPageHeader
import cn.xybbz.ui.components.JvmSettingConnectionCardMinWidth
import cn.xybbz.ui.components.JvmSettingPageScaffold
import cn.xybbz.ui.components.show
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.ext.jvmHoverDebounceClickable
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.windows.DesktopTooltipIconButton
import cn.xybbz.ui.xy.XySmallImage
import cn.xybbz.ui.xy.XyText
import cn.xybbz.ui.xy.XyTextSub
import cn.xybbz.ui.xy.XyTextSubSmall
import cn.xybbz.viewmodel.ConnectionManagementViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic.composeapp.generated.resources.*
import xymusic.composeapp.generated.resources.Res
import xymusic.composeapp.generated.resources.add_24px
import xymusic.composeapp.generated.resources.add_connection
import xymusic.composeapp.generated.resources.check_24px
import xymusic.composeapp.generated.resources.confirm_delete_connection
import xymusic.composeapp.generated.resources.connection_management
import xymusic.composeapp.generated.resources.connection_permission_read_only
import xymusic.composeapp.generated.resources.current_connection
import xymusic.composeapp.generated.resources.delete_24px
import xymusic.composeapp.generated.resources.delete_connection
import xymusic.composeapp.generated.resources.delete_prefix
import xymusic.composeapp.generated.resources.download
import xymusic.composeapp.generated.resources.edit_24px
import xymusic.composeapp.generated.resources.folder_managed_24px
import xymusic.composeapp.generated.resources.library_add_24px
import xymusic.composeapp.generated.resources.modify_connection
import xymusic.composeapp.generated.resources.music_library
import xymusic.composeapp.generated.resources.unknown
import xymusic.composeapp.generated.resources.warning

/** 卡片左侧强调条宽度。 */
private val JvmConnectionCardAccentWidth = 2.dp

/** 连接卡片的最小高度。 */
private val JvmConnectionCardMinHeight = 168.dp

/** 连接卡片头像尺寸。 */
private val JvmConnectionCardAvatarSize = 32.dp

/** 连线矩阵卡片圆角，比主题圆角略小以贴合设计稿。 */
private val JvmConnectionChipCorner = 4.dp

/**
 * 连接管理一级页面。
 *
 * 排版对齐设计稿「Connection Matrix」：顶部为标题与读数，左侧是连接卡片矩阵
 * （末尾带虚线添加卡），右侧是当前连接的媒体库范围。
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
    val inactiveConnectionText = stringResource(Res.string.jvm_connection_config_info_screen_text_05)
    val disconnectedText = stringResource(Res.string.jvm_connection_config_info_screen_text_01)
    val readOnlyText = stringResource(Res.string.connection_permission_read_only)
    val warningText = stringResource(Res.string.warning)
    val deleteText = stringResource(Res.string.delete_prefix)
    val downloadText = stringResource(Res.string.download)
    val allLibrariesText = stringResource(Res.string.jvm_connection_management_text_23)
    val unknownText = stringResource(Res.string.unknown)
    val confirmDeleteText = stringResource(Res.string.confirm_delete_connection)

    val displayItems = connections.map { connectionConfig ->
        // 媒体库为空表示不限制范围，与服务端「全部媒体库」语义保持一致。
        val libraryNames = connectionManagementViewModel.selectedLibraryNames(connectionConfig)
            ?.filter { it.isNotBlank() }
            .orEmpty()
        val capabilityValue = buildList {
            if (connectionConfig.ifEnabledDownload) add(downloadText)
            if (connectionConfig.ifEnabledDelete) add(deleteText)
        }.takeIf { it.isNotEmpty() }?.joinToString(" / ") ?: readOnlyText

        JvmConnectionDisplayItem(
            config = connectionConfig,
            selected = currentConnectionId == connectionConfig.id,
            title = connectionConfig.jvmConnectionDisplayTitle(),
            address = connectionConfig.address,
            status = if (currentConnectionId == connectionConfig.id) {
                currentConnectionText
            } else {
                inactiveConnectionText
            },
            aliasText = connectionConfig.name.ifBlank { connectionConfig.type.title },
            versionText = connectionConfig.serverVersion
                .takeIf { it.isNotBlank() }
                ?.let { version -> "v$version" }
                ?: unknownText,
            libraryText = if (libraryNames.isEmpty()) {
                allLibrariesText
            } else {
                stringResource(Res.string.jvm_connection_management_text_21, libraryNames.size)
            },
            capabilityText = capabilityValue,
        )
    }
    val currentDisplayItem = displayItems.firstOrNull { it.selected } ?: displayItems.firstOrNull()

    // 与设置页保持一致：内容内边距使用 JvmSettingPageScaffold 的统一默认值。
    JvmSettingPageScaffold {
        // 统一页面头部：左侧大标题，右侧当前连接读数
        JvmPageHeader(
            title = stringResource(Res.string.connection_management),
            trailing = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    JvmConnectionReadout(
                        label = stringResource(Res.string.jvm_connection_config_info_screen_text_08),
                        value = currentDisplayItem?.aliasText ?: disconnectedText,
                        emphasized = true,
                    )
                    JvmConnectionReadoutDivider()
                    JvmConnectionReadout(
                        label = stringResource(Res.string.jvm_connection_management_text_19),
                        value = connections.size.toString(),
                    )
                    JvmConnectionReadoutDivider()
                    JvmConnectionReadout(
                        label = stringResource(Res.string.jvm_connection_management_text_20),
                        value = currentDisplayItem?.config?.type?.title ?: disconnectedText,
                    )
                }
            },
        )

        JvmConnectionListSection(
            displayItems = displayItems,
            onAddConnection = {
                navigator.navigate(Connection(connectionUiType = ConnectionUiType.ADD_CONNECTION))
            },
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
    }
}

/**
 * 头部读数单元格。
 *
 * @param label 读数标签。
 * @param value 读数内容。
 * @param emphasized 是否使用更大的强调字号。
 */
@Composable
private fun JvmConnectionReadout(
    label: String,
    value: String,
    emphasized: Boolean = false,
) {
    Column(
        modifier = Modifier.widthIn(max = 180.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        XyTextSub(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.4.sp,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
        XyText(
            text = value,
            style = MaterialTheme.typography.labelLarge.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = if (emphasized) 15.sp else 12.5.sp,
                fontWeight = FontWeight.Bold,
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )
    }
}

/**
 * 头部读数之间的竖直分隔线。
 */
@Composable
private fun JvmConnectionReadoutDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(30.dp)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f))
    )
}

/**
 * 连接设置列表分区。
 *
 * @param displayItems 连接展示项列表。
 * @param onAddConnection 添加连接回调。
 * @param onSelectConnection 切换连接回调。
 * @param onEditConnection 编辑连接回调。
 * @param onSelectLibrary 选择媒体库回调。
 * @param onDeleteConnection 删除连接回调。
 */
@Composable
private fun JvmConnectionListSection(
    displayItems: List<JvmConnectionDisplayItem>,
    onAddConnection: () -> Unit,
    onSelectConnection: (ConnectionConfig) -> Unit,
    onEditConnection: (ConnectionConfig) -> Unit,
    onSelectLibrary: (ConnectionConfig) -> Unit,
    onDeleteConnection: (ConnectionConfig) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (displayItems.isEmpty()) {
            JvmConnectionEmptyState()
        } else {
            JvmConnectionCardGrid(
                displayItems = displayItems,
                onAddConnection = onAddConnection,
                onSelectConnection = onSelectConnection,
                onEditConnection = onEditConnection,
                onSelectLibrary = onSelectLibrary,
                onDeleteConnection = onDeleteConnection,
            )
        }
    }
}

/**
 * 连接卡片矩阵，末尾固定追加「添加连接」占位卡。
 *
 * @param displayItems 连接展示项列表。
 * @param onAddConnection 添加连接回调。
 * @param onSelectConnection 切换连接回调。
 * @param onEditConnection 编辑连接回调。
 * @param onSelectLibrary 选择媒体库回调。
 * @param onDeleteConnection 删除连接回调。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun JvmConnectionCardGrid(
    displayItems: List<JvmConnectionDisplayItem>,
    onAddConnection: () -> Unit,
    onSelectConnection: (ConnectionConfig) -> Unit,
    onEditConnection: (ConnectionConfig) -> Unit,
    onSelectLibrary: (ConnectionConfig) -> Unit,
    onDeleteConnection: (ConnectionConfig) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val gap = XyTheme.dimens.contentPadding
        val useTwoColumns = maxWidth >= JvmSettingConnectionCardMinWidth * 2 + gap
        val columns = if (useTwoColumns) 2 else 1
        // 最后一个槽位留给「添加连接」占位卡。
        val slotCount = displayItems.size + 1

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(gap),
        ) {
            var start = 0
            while (start < slotCount) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(gap),
                ) {
                    for (offset in 0 until columns) {
                        val slot = start + offset
                        when {
                            slot < displayItems.size -> {
                                val displayItem = displayItems[slot]
                                JvmConnectionCard(
                                    modifier = Modifier.weight(1f),
                                    displayItem = displayItem,
                                    onSelectConnection = onSelectConnection,
                                    onEditConnection = onEditConnection,
                                    onSelectLibrary = onSelectLibrary,
                                    onDeleteConnection = onDeleteConnection,
                                )
                            }

                            slot == displayItems.size -> {
                                JvmConnectionAddCard(
                                    modifier = Modifier.weight(1f),
                                    onClick = onAddConnection,
                                )
                            }

                            else -> Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
                start += columns
            }
        }
    }
}

/**
 * 单个连接卡片。
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
    // 复用桌面卡片的悬停上移动效，让连接卡片也有一致的鼠标反馈。
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val liftOffset by animateDpAsState(
        targetValue = if (hovered) (-4).dp else 0.dp,
        animationSpec = tween(durationMillis = 160),
        label = "connection_card_lift_offset",
    )
    // 当前连接使用主色浅底与主色描边，其余连接保持中性底色。
    val containerColor = if (selected) {
        colorScheme.primary.copy(alpha = if (XyTheme.configs.isDarkTheme) 0.10f else 0.06f)
    } else {
        Color.Transparent
    }
    val borderColor = if (selected) {
        colorScheme.primary.copy(alpha = 0.52f)
    } else {
        colorScheme.onSurface.copy(alpha = 0.10f)
    }
    val clickModifier = if (selected) {
        Modifier.hoverable(interactionSource = interactionSource)
    } else {
        Modifier.jvmHoverDebounceClickable(interactionSource = interactionSource) {
            onSelectConnection(displayItem.config)
        }
    }

    Surface(
        modifier = modifier
            .offset(y = liftOffset)
            .heightIn(min = JvmConnectionCardMinHeight)
            .then(clickModifier),
        shape = shape,
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Box {
            // 当前连接左侧的强调竖条。
            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxHeight()
                        .width(JvmConnectionCardAccentWidth)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
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
                        size = JvmConnectionCardAvatarSize,
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        XyText(
                            text = displayItem.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        XyTextSub(
                            text = displayItem.address,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 16.sp
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

                // 标签行：别名、版本、媒体库数量、权限能力。
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding),
                    verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding),
                ) {
                    JvmConnectionInfoChip(text = displayItem.aliasText, accent = true)
                    JvmConnectionInfoChip(text = displayItem.versionText)
                    JvmConnectionInfoChip(text = displayItem.libraryText)
                    JvmConnectionInfoChip(text = displayItem.capabilityText)
                }

                JvmConnectionCardFooter(
                    statusText = if (selected) {
                        stringResource(Res.string.jvm_connection_management_text_30)
                    } else {
                        displayItem.status
                    },
                    active = selected,
                    onEditConnection = { onEditConnection(displayItem.config) },
                    onSelectLibrary = { onSelectLibrary(displayItem.config) },
                    onDeleteConnection = { onDeleteConnection(displayItem.config) },
                )
            }
        }
    }
}

/**
 * 连接卡片底部：状态读数与维护按钮。
 *
 * @param statusText 状态文案。
 * @param active 是否为当前连接。
 * @param onEditConnection 编辑连接回调。
 * @param onSelectLibrary 选择媒体库回调。
 * @param onDeleteConnection 删除连接回调。
 */
@Composable
private fun JvmConnectionCardFooter(
    statusText: String,
    active: Boolean,
    onEditConnection: () -> Unit,
    onSelectLibrary: () -> Unit,
    onDeleteConnection: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            JvmConnectionStatusTag(text = statusText, active = active)
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DesktopTooltipIconButton(
                    tooltip = stringResource(Res.string.modify_connection),
                    onClick = composeClick { onEditConnection() },
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.edit_24px),
                        contentDescription = stringResource(Res.string.modify_connection),
                    )
                }
                DesktopTooltipIconButton(
                    tooltip = stringResource(Res.string.music_library),
                    onClick = composeClick { onSelectLibrary() },
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.folder_managed_24px),
                        contentDescription = stringResource(Res.string.music_library),
                    )
                }
                DesktopTooltipIconButton(
                    tooltip = stringResource(Res.string.delete_connection),
                    onClick = composeClick { onDeleteConnection() },
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.delete_24px),
                        contentDescription = stringResource(Res.string.delete_connection),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

/**
 * 「添加连接」虚线占位卡。
 *
 * @param modifier 外层布局修饰符。
 * @param onClick 添加连接回调。
 */
@Composable
private fun JvmConnectionAddCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val shape = RoundedCornerShape(XyTheme.dimens.corner)
    val accentColor = if (hovered) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .heightIn(min = JvmConnectionCardMinHeight)
            .clip(shape)
            .background(
                if (hovered) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                } else {
                    Color.Transparent
                }
            )
            .jvmDashedRoundedBorder(
                color = if (hovered) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.50f)
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f)
                },
                cornerRadius = XyTheme.dimens.corner,
            )
            .hoverable(interactionSource)
            .jvmHoverDebounceClickable(interactionSource = interactionSource) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                painter = painterResource(Res.drawable.add_24px),
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp),
            )
            XyText(
                text = stringResource(Res.string.add_connection),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            XyTextSub(
                text = stringResource(Res.string.jvm_connection_management_text_28),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}

/**
 * 给「添加连接」占位卡绘制虚线圆角描边。
 *
 * @param color 描边颜色。
 * @param cornerRadius 圆角半径。
 * @param strokeWidth 描边宽度。
 * @param dashLength 虚线段长度。
 */
private fun Modifier.jvmDashedRoundedBorder(
    color: Color,
    cornerRadius: Dp,
    strokeWidth: Dp = 1.dp,
    dashLength: Dp = 5.dp,
): Modifier = drawBehind {
    val stroke = strokeWidth.toPx()
    val dash = dashLength.toPx()
    drawRoundRect(
        color = color,
        topLeft = Offset(stroke / 2f, stroke / 2f),
        size = Size(size.width - stroke, size.height - stroke),
        cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
        style = Stroke(
            width = stroke,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash, dash))
        )
    )
}

/**
 * 连接状态读数：圆点 + 文案。
 *
 * @param text 状态文案。
 * @param active 是否为当前连接。
 */
@Composable
private fun JvmConnectionStatusTag(
    text: String,
    active: Boolean,
) {
    val color = if (active) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .background(
                    color = if (active) color else color.copy(alpha = 0.55f),
                    shape = CircleShape,
                )
        )
        XyTextSub(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.9.sp,
            ),
            color = color,
            maxLines = 1,
        )
    }
}

/**
 * 连接服务头像。
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
    val shape = RoundedCornerShape(XyTheme.dimens.corner / 1.5f)
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
 * 当前连接状态标签。
 *
 * @param text 标签文案。
 */
@Composable
private fun JvmConnectionCurrentPill(text: String) {
    Surface(
        shape = RoundedCornerShape(JvmConnectionChipCorner),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
        contentColor = MaterialTheme.colorScheme.primary,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(Res.drawable.check_24px),
                contentDescription = null,
                modifier = Modifier.size(12.dp),
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
 * 连接信息标签。
 *
 * @param text 标签文案。
 * @param accent 是否使用强调色。
 */
@Composable
private fun JvmConnectionInfoChip(
    text: String,
    accent: Boolean = false,
) {
    val color = if (accent) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(JvmConnectionChipCorner),
        color = if (accent) color.copy(alpha = 0.10f) else Color.Transparent,
        contentColor = color,
        border = BorderStroke(
            width = 1.dp,
            color = color.copy(alpha = if (accent) 0.42f else 0.14f)
        )
    ) {
        XyTextSub(
            modifier = Modifier
                .widthIn(max = 220.dp)
                .padding(horizontal = 6.dp, vertical = 2.dp),
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.4.sp,
            ),
            color = color,
            maxLines = 1,
        )
    }
}

/**
 * 连接列表空状态。
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
                text = stringResource(Res.string.jvm_connection_management_text_14),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            XyTextSub(
                text = stringResource(Res.string.jvm_connection_management_text_15),
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * 连接空状态图标。
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
 * 连接删除确认弹窗。
 *
 * @param warning 警告标题文案。
 * @param content 删除说明文案。
 * @param onDelete 确认删除回调。
 */
private fun JvmConnectionDeleteDialog(
    warning: String,
    content: String,
    onDelete: () -> Unit,
) {
    AlertDialogObject(
        title = warning,
        body = content,
        tone = XyDialogTone.Danger,
        icon = XyDialogIcon.Trash,
        kicker = Res.string.dialog_kicker_danger,
        onConfirmation = onDelete,
    ).show()
}

/**
 * 连接管理页内部展示数据。
 *
 * @param config 原始连接配置。
 * @param selected 是否为当前连接。
 * @param title 卡片标题。
 * @param address 服务器地址。
 * @param status 连接状态文案。
 * @param aliasText 本地别名标签。
 * @param versionText 服务端版本标签。
 * @param libraryText 媒体库数量标签。
 * @param capabilityText 权限能力标签。
 */
private data class JvmConnectionDisplayItem(
    val config: ConnectionConfig,
    val selected: Boolean,
    val title: String,
    val address: String,
    val status: String,
    val aliasText: String,
    val versionText: String,
    val libraryText: String,
    val capabilityText: String,
)

/**
 * 生成连接卡片标题。
 */
private fun ConnectionConfig.jvmConnectionDisplayTitle(): String {
    val userPart = username
        .ifBlank { name }
        .ifBlank { "#$id" }

    return "${type.title} - $userPart"
}
