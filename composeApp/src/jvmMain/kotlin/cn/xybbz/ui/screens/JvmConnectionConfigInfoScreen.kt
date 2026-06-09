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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.xybbz.api.constants.ApiConstants
import cn.xybbz.api.utils.withDefaultHttpScheme
import cn.xybbz.common.enums.img
import cn.xybbz.localdata.data.connection.ConnectionConfig
import cn.xybbz.ui.components.JvmSettingBaseRow
import cn.xybbz.ui.components.JvmSettingConnectionFormCardMinHeight
import cn.xybbz.ui.components.JvmSettingNote
import cn.xybbz.ui.components.JvmSettingOverviewTile
import cn.xybbz.ui.components.JvmSettingPageHeader
import cn.xybbz.ui.components.JvmSettingPageScaffold
import cn.xybbz.ui.components.JvmSettingSection
import cn.xybbz.ui.components.JvmSettingStatusCard
import cn.xybbz.ui.components.JvmSettingStatusCardItem
import cn.xybbz.ui.components.JvmSettingSummaryCardWidth
import cn.xybbz.ui.components.JvmSettingTwoPaneContent
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyButton
import cn.xybbz.ui.xy.XySmallImage
import cn.xybbz.ui.xy.XyText
import cn.xybbz.ui.xy.XyTextLarge
import cn.xybbz.ui.xy.XyTextSub
import cn.xybbz.viewmodel.ConnectionConfigInfoViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.connection_address
import xymusic_kmp.composeapp.generated.resources.connection_info
import xymusic_kmp.composeapp.generated.resources.connection_media_library_all_label
import xymusic_kmp.composeapp.generated.resources.connection_permissions_label
import xymusic_kmp.composeapp.generated.resources.connection_server_version_label
import xymusic_kmp.composeapp.generated.resources.connection_server_version_unknown_label
import xymusic_kmp.composeapp.generated.resources.current_connection
import xymusic_kmp.composeapp.generated.resources.delete_prefix
import xymusic_kmp.composeapp.generated.resources.download
import xymusic_kmp.composeapp.generated.resources.http_24px
import xymusic_kmp.composeapp.generated.resources.info_24px
import xymusic_kmp.composeapp.generated.resources.label_24px
import xymusic_kmp.composeapp.generated.resources.password
import xymusic_kmp.composeapp.generated.resources.person_24px
import xymusic_kmp.composeapp.generated.resources.refresh_24px
import xymusic_kmp.composeapp.generated.resources.save
import xymusic_kmp.composeapp.generated.resources.set_alias
import xymusic_kmp.composeapp.generated.resources.warning_24px
import xymusic_kmp.composeapp.generated.resources.username
import java.net.URI

/**
 * JVM 桌面端连接信息页面。
 *
 * @param connectionId 当前要编辑的连接 ID。
 * @param connectionConfigInfoViewModel 连接信息编辑 ViewModel。
 */
@Composable
fun JvmConnectionConfigInfoScreen(
    connectionId: Long,
    connectionConfigInfoViewModel: ConnectionConfigInfoViewModel = koinViewModel<ConnectionConfigInfoViewModel> {
        parametersOf(connectionId)
    }
) {
    val coroutineScope = rememberCoroutineScope()
    val pageTitle = stringResource(Res.string.connection_info)
    val saveText = stringResource(Res.string.save)
    val currentConnectionText = stringResource(Res.string.current_connection)
    val allLibraryText = stringResource(Res.string.connection_media_library_all_label)
    val downloadText = stringResource(Res.string.download)
    val deleteText = stringResource(Res.string.delete_prefix)
    val connectionConfig = connectionConfigInfoViewModel.connectionConfig
    val connectionName = connectionConfig?.name
        ?.takeIf { it.isNotBlank() }
        ?: connectionConfigInfoViewModel.connectionName.ifBlank { pageTitle }
    val serverVersion = connectionConfig?.serverVersion.orEmpty()
    val serverVersionText = if (serverVersion.isNotBlank()) {
        stringResource(Res.string.connection_server_version_label, serverVersion)
    } else {
        stringResource(Res.string.connection_server_version_unknown_label)
    }
    val dataSourceLabel = connectionConfig?.type?.title ?: "未连接"
    val address = connectionConfigInfoViewModel.address
    val username = connectionConfigInfoViewModel.username
    val currentConnection = connectionConfigInfoViewModel.getConnectionId() == connectionId
    val canSave = address.isNotBlank() && connectionConfigInfoViewModel.connectionName.isNotBlank()
    val capabilityValue = buildList {
        if (connectionConfig?.ifEnabledDownload == true) add(downloadText)
        if (connectionConfig?.ifEnabledDelete == true) add(deleteText)
    }.takeIf { it.isNotEmpty() }?.joinToString(" / ") ?: "只读"
    val capabilityText = stringResource(Res.string.connection_permissions_label, capabilityValue)
    val libraryText = connectionConfig?.libraryIds
        ?.takeIf { it.isNotEmpty() }
        ?.let { "已选 ${it.size} 个媒体库" }
        ?: allLibraryText
    val saveConnection = {
        coroutineScope.launch {
            // 修改连接设置，成功后沿用原有重新登录逻辑。
            if (connectionConfigInfoViewModel.updateConnectionConfig()) {
                connectionConfigInfoViewModel.restartLogin()
            }
        }
        Unit
    }

    JvmSettingPageScaffold(
        contentPadding = PaddingValues(
            horizontal = XyTheme.dimens.outerHorizontalPadding * 2,
            vertical = XyTheme.dimens.outerVerticalPadding * 3
        )
    ) {
        JvmSettingPageHeader(
            title = pageTitle,
            description = "维护当前音乐服务地址、账号凭据和显示别名；地址、用户名或密码变更后，保存会触发当前连接重新登录。",
        ) {
            JvmSettingStatusCard(
                width = JvmSettingSummaryCardWidth,
                prominentValue = true,
                items = listOf(
                    JvmSettingStatusCardItem(
                        label = "连接状态",
                        value = if (currentConnection) currentConnectionText else "备用连接"
                    ),
                    JvmSettingStatusCardItem(label = "服务端版本", value = serverVersionText),
                    JvmSettingStatusCardItem(label = "服务类型", value = dataSourceLabel),
                )
            )
        }

        JvmSettingTwoPaneContent(
            leftContent = {
                JvmSettingSection(
                    title = "当前连接",
                    subtitle = "用于桌面端播放、媒体库浏览和数据同步的数据源。",
                    badge = if (currentConnection) "在线" else "未选中",
                    contentContainerEnabled = false,
                ) {
                    JvmConnectionHeroCard(
                        connectionConfig = connectionConfig,
                        name = connectionName,
                        address = address,
                        status = if (currentConnection) currentConnectionText else "备用",
                    )
                }

                JvmConnectionOverview(
                    address = address,
                    username = username,
                    serverVersionText = serverVersionText,
                    connectionConfig = connectionConfig,
                )

                JvmSettingSection(
                    title = "连接设置",
                    subtitle = "修改服务地址、登录账号、密码和本地显示别名。",
                    badge = if (canSave) "可保存" else "需填写",
                    contentContainerEnabled = false,
                    headerAction = {
                        XyButton(
                            modifier = Modifier.widthIn(min = 96.dp),
                            enabled = canSave,
                            onClick = saveConnection,
                            text = saveText,
                        )
                    },
                ) {
                    JvmConnectionFormFields(
                        address = address,
                        username = username,
                        password = connectionConfigInfoViewModel.password,
                        connectionName = connectionConfigInfoViewModel.connectionName,
                        onAddressChange = connectionConfigInfoViewModel::updateAddress,
                        onUsernameChange = connectionConfigInfoViewModel::updateUsername,
                        onPasswordChange = connectionConfigInfoViewModel::updatePassword,
                        onConnectionNameChange = connectionConfigInfoViewModel::updateConnectionName,
                    )
                }
            },
            rightContent = {
                JvmSettingSection(
                    title = "连接详情",
                    subtitle = "来自当前 ConnectionConfig 的关键状态。",
                    badge = "详情",
                ) {
                    JvmConnectionInfoRow(label = "连接 ID", value = "#$connectionId")
                    JvmConnectionInfoRow(label = "服务类型", value = dataSourceLabel)
                    JvmConnectionInfoRow(label = "媒体库", value = libraryText)
                    JvmConnectionInfoRow(label = "权限", value = capabilityText)
                }

                JvmSettingSection(
                    title = "保存影响",
                    subtitle = "保存按钮会更新本地配置，并按当前连接状态处理登录。",
                    badge = "保存",
                ) {
                    JvmSettingBaseRow(
                        icon = Res.drawable.refresh_24px,
                        title = "地址、用户名、密码变更",
                        description = "如果正在使用这个连接，保存后会立即重新登录。",
                        descriptionMaxLines = 2,
                        iconSelected = true,
                        trailing = {
                            JvmConnectionStatePill(text = if (currentConnection) "会重登" else "下次生效")
                        }
                    )
                    JvmSettingBaseRow(
                        icon = Res.drawable.label_24px,
                        title = "别名变更",
                        description = "只更新本地显示名，不额外触发服务端请求。",
                        descriptionMaxLines = 2,
                        trailing = {
                            JvmConnectionStatePill(text = "本地")
                        }
                    )
                    JvmSettingBaseRow(
                        icon = Res.drawable.warning_24px,
                        title = "空值校验",
                        description = "链接地址和别名为空时不允许保存。",
                        descriptionMaxLines = 2,
                        iconColor = MaterialTheme.colorScheme.error,
                        trailing = {
                            JvmConnectionStatePill(
                                text = if (canSave) "通过" else "待填写",
                                selected = canSave,
                            )
                        }
                    )
                    JvmSettingNote(text = "密码字段沿用当前连接输入框和本地加密保存流程；页面只重排信息架构，不改变原有保存与重新登录语义。")
                }
            }
        )
    }
}

/**
 * JVM 连接页左侧的连接摘要卡。
 *
 * @param connectionConfig 当前连接配置，为空时展示占位图标。
 * @param name 当前连接显示名称。
 * @param address 当前连接地址。
 * @param status 当前连接状态标签。
 */
@Composable
private fun JvmConnectionHeroCard(
    connectionConfig: ConnectionConfig?,
    name: String,
    address: String,
    status: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        color = MaterialTheme.colorScheme.primary.copy(alpha = if (XyTheme.configs.isDarkTheme) 0.18f else 0.10f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.26f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(XyTheme.dimens.outerHorizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            JvmConnectionServiceAvatar(connectionConfig = connectionConfig)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding / 2)
            ) {
                XyTextLarge(
                    text = name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                XyTextSub(
                    text = address.ifBlank { "未填写链接地址" },
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            JvmConnectionStatePill(text = status, selected = true)
        }
    }
}

/**
 * JVM 连接页的服务类型头像。
 *
 * @param connectionConfig 当前连接配置。
 */
@Composable
private fun JvmConnectionServiceAvatar(connectionConfig: ConnectionConfig?) {
    val shape = RoundedCornerShape(XyTheme.dimens.corner + XyTheme.dimens.outerVerticalPadding / 2)
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)),
                shape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (connectionConfig == null) {
            Icon(
                painter = painterResource(Res.drawable.http_24px),
                contentDescription = null,
                modifier = Modifier.size(34.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        } else {
            XySmallImage(
                modifier = Modifier.size(42.dp),
                model = painterResource(connectionConfig.type.img),
                contentDescription = connectionConfig.type.title
            )
        }
    }
}

/**
 * JVM 连接页的三张概览卡。
 *
 * @param address 当前连接地址。
 * @param username 当前用户名。
 * @param serverVersionText 服务端版本文案。
 * @param connectionConfig 当前连接配置。
 */
@Composable
private fun JvmConnectionOverview(
    address: String,
    username: String,
    serverVersionText: String,
    connectionConfig: ConnectionConfig?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
        verticalAlignment = Alignment.Top
    ) {
        JvmSettingOverviewTile(
            modifier = Modifier.weight(1f),
            icon = Res.drawable.http_24px,
            kicker = "服务器地址",
            value = address.jvmConnectionHostLabel(),
            sub = address.jvmConnectionProtocolPortLabel(connectionConfig),
        )
        JvmSettingOverviewTile(
            modifier = Modifier.weight(1f),
            icon = Res.drawable.person_24px,
            kicker = stringResource(Res.string.username),
            value = username.ifBlank { "未填写" },
            sub = "保存后用于重新登录",
        )
        JvmSettingOverviewTile(
            modifier = Modifier.weight(1f),
            icon = Res.drawable.info_24px,
            kicker = "服务版本",
            value = serverVersionText.removePrefix("版本："),
            sub = connectionConfig?.type?.title ?: "等待连接配置",
        )
    }
}

/**
 * JVM 连接页的表单字段集合。
 *
 * @param address 当前连接地址。
 * @param username 当前用户名。
 * @param password 当前密码。
 * @param connectionName 当前连接别名。
 * @param onAddressChange 地址变更回调。
 * @param onUsernameChange 用户名变更回调。
 * @param onPasswordChange 密码变更回调。
 * @param onConnectionNameChange 别名变更回调。
 */
@Composable
private fun JvmConnectionFormFields(
    address: String,
    username: String,
    password: String,
    connectionName: String,
    onAddressChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConnectionNameChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding)
    ) {
        JvmConnectionFormCard(
            title = stringResource(Res.string.connection_address),
            description = "示例：${ApiConstants.HTTP}192.168.1.2:8096 或 ${ApiConstants.HTTPS}music.example.com",
            state = "必填",
            selected = true,
        ) {
            JvmAddressInputEdit(
                address = address,
                updateAddress = onAddressChange
            )
        }

        JvmConnectionFormCard(
            title = stringResource(Res.string.username),
            description = "保存后作为服务端 API 登录账号。",
            state = "账号",
        ) {
            JvmUsernameInputEdit(
                username = username,
                updateUsername = onUsernameChange
            )
        }

        JvmConnectionFormCard(
            title = stringResource(Res.string.password),
            description = "密码会在本地加密保存。",
            state = "加密",
        ) {
            JvmPasswordInputEdit(
                password = password,
                updatePassword = onPasswordChange
            )
        }

        JvmConnectionFormCard(
            title = stringResource(Res.string.set_alias),
            description = "别名只影响本地列表和页面标题展示。",
            state = "必填",
            selected = true,
        ) {
            JvmConnectionNameInputEdit(
                connectionName = connectionName,
                updateConnectionName = onConnectionNameChange
            )
        }
    }
}

/**
 * JVM 连接页的单个表单卡片。
 *
 * @param title 字段标题。
 * @param description 字段说明。
 * @param state 字段状态标签。
 * @param selected 是否使用主色强调状态。
 * @param content 字段输入控件。
 */
@Composable
private fun JvmConnectionFormCard(
    title: String,
    description: String,
    state: String,
    selected: Boolean = false,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = JvmSettingConnectionFormCardMinHeight),
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.045f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.075f)
        )
    ) {
        Column(
            modifier = Modifier.padding(XyTheme.dimens.contentPadding),
            verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding / 2)
                ) {
                    XyText(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    XyTextSub(
                        text = description,
                        style = MaterialTheme.typography.labelSmall.copy(lineHeight = 17.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                JvmConnectionStatePill(text = state, selected = selected)
            }
            content()
        }
    }
}

/**
 * JVM 连接页右侧详情列表的单行。
 *
 * @param label 行标签。
 * @param value 行内容。
 */
@Composable
private fun JvmConnectionInfoRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = XyTheme.dimens.itemHeight)
            .padding(
                horizontal = XyTheme.dimens.outerHorizontalPadding,
                vertical = XyTheme.dimens.contentPadding
            ),
        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        XyTextSub(
            modifier = Modifier.weight(1f),
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        XyText(
            modifier = Modifier.widthIn(max = 180.dp),
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * JVM 连接页通用状态标签。
 *
 * @param text 标签文案。
 * @param selected 是否使用主色强调。
 * @param width 标签最大宽度。
 */
@Composable
private fun JvmConnectionStatePill(
    text: String,
    selected: Boolean = false,
    width: Dp = 128.dp,
) {
    val color = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
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
                .widthIn(max = width)
                .padding(
                    horizontal = XyTheme.dimens.contentPadding,
                    vertical = XyTheme.dimens.outerVerticalPadding / 2
                ),
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * 提取连接地址中的主机名。
 */
private fun String.jvmConnectionHostLabel(): String {
    if (isBlank()) {
        return "未填写"
    }
    return runCatching {
        URI(withDefaultHttpScheme()).host
    }.getOrNull()?.takeIf { it.isNotBlank() } ?: this
}

/**
 * 提取连接地址中的协议和端口展示文案。
 *
 * @param connectionConfig 当前连接配置，用于补充默认端口。
 */
private fun String.jvmConnectionProtocolPortLabel(connectionConfig: ConnectionConfig?): String {
    if (isBlank()) {
        return "等待填写服务地址"
    }
    val uri = runCatching {
        URI(withDefaultHttpScheme())
    }.getOrNull()
    val scheme = uri?.scheme?.uppercase()?.takeIf { it.isNotBlank() } ?: ApiConstants.HTTP_PROTOCOL_NAME
    val port = uri?.port
        ?.takeIf { it > 0 }
        ?: if (scheme == ApiConstants.HTTPS_PROTOCOL_NAME) {
            connectionConfig?.type?.httpsPort
        } else {
            connectionConfig?.type?.httpPort
        }
    return if (port == null) {
        scheme
    } else {
        "$scheme · $port"
    }
}
