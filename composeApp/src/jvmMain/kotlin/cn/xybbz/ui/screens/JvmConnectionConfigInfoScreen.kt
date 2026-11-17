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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.xybbz.api.constants.ApiConstants
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.ui.components.JvmPageHeader
import cn.xybbz.ui.components.JvmSettingPageScaffold
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyButton
import cn.xybbz.ui.xy.XyText
import cn.xybbz.ui.xy.XyTextSub
import cn.xybbz.viewmodel.ConnectionConfigInfoViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import xymusic.composeapp.generated.resources.*
import xymusic.composeapp.generated.resources.Res
import xymusic.composeapp.generated.resources.cancel
import xymusic.composeapp.generated.resources.connection_address
import xymusic.composeapp.generated.resources.connection_info
import xymusic.composeapp.generated.resources.connection_server_version_unknown_label
import xymusic.composeapp.generated.resources.password
import xymusic.composeapp.generated.resources.save
import xymusic.composeapp.generated.resources.set_alias
import xymusic.composeapp.generated.resources.username

/** 表单字段标签字号，对应设计稿的等宽小标签。 */
private val JvmConnectionFieldLabelSize = 9.5.sp

/** 表单字段标签字距。 */
private val JvmConnectionFieldLabelLetterSpacing = 1.3.sp

/** 表单字段双列布局的切换宽度，低于该宽度时改为上下堆叠。 */
private val JvmConnectionFieldGridBreakpoint = 520.dp

/**
 * JVM 桌面端连接信息页面。
 *
 * 排版对齐设计稿「连接信息」：顶部是页面标题与右侧读数，主体为连接凭据表单；
 * 设计稿中的「保存影响」和「连接状态」分区不再展示。
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
    val navigator = LocalNavigator.current
    val coroutineScope = rememberCoroutineScope()
    val pageTitle = stringResource(Res.string.connection_info)
    val saveText = stringResource(Res.string.save)
    val cancelText = stringResource(Res.string.cancel)
    val connectionConfig = connectionConfigInfoViewModel.connectionConfig
    val serverVersion = connectionConfig?.serverVersion.orEmpty()
    // 页头读数使用的版本号，有版本时补 v 前缀，与服务端展示习惯一致。
    val serverVersionValue = if (serverVersion.isNotBlank()) {
        "v$serverVersion"
    } else {
        stringResource(Res.string.connection_server_version_unknown_label)
    }
    val dataSourceLabel = connectionConfig?.type?.title
        ?: stringResource(Res.string.jvm_connection_config_info_screen_text_01)
    val address = connectionConfigInfoViewModel.address
    val username = connectionConfigInfoViewModel.username
    val canSave = address.isNotBlank() && connectionConfigInfoViewModel.connectionName.isNotBlank()
    val saveConnection = {
        coroutineScope.launch {
            // 修改连接设置，成功后沿用原有重新登录逻辑。
            if (connectionConfigInfoViewModel.updateConnectionConfig()) {
                connectionConfigInfoViewModel.restartLogin()
            }
        }
        Unit
    }

    // 与设置页、连接管理页保持一致：内容内边距使用 JvmSettingPageScaffold 的统一默认值。
    JvmSettingPageScaffold {
        // 顶部统一页头：标题在左侧，右侧是服务端版本与服务类型读数。
        JvmPageHeader(
            title = pageTitle,
            trailing = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    JvmConnectionInfoReadout(
                        label = stringResource(Res.string.jvm_connection_config_info_screen_text_06),
                        value = serverVersionValue,
                        emphasized = true,
                    )
                    JvmConnectionInfoReadoutDivider()
                    JvmConnectionInfoReadout(
                        label = stringResource(Res.string.jvm_connection_config_info_screen_text_07),
                        value = dataSourceLabel,
                    )
                }
            },
        )

        JvmConnectionCredentialFormCard(
            address = address,
            username = username,
            password = connectionConfigInfoViewModel.password,
            connectionName = connectionConfigInfoViewModel.connectionName,
            canSave = canSave,
            saveText = saveText,
            cancelText = cancelText,
            onAddressChange = connectionConfigInfoViewModel::updateAddress,
            onUsernameChange = connectionConfigInfoViewModel::updateUsername,
            onPasswordChange = connectionConfigInfoViewModel::updatePassword,
            onConnectionNameChange = connectionConfigInfoViewModel::updateConnectionName,
            onSave = saveConnection,
            onCancel = { navigator.goBack() },
        )
    }
}

/**
 * 页头读数单元格。
 *
 * @param label 读数标签。
 * @param value 读数内容。
 * @param emphasized 是否使用更大的强调字号。
 */
@Composable
private fun JvmConnectionInfoReadout(
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
 * 页头读数之间的竖直分隔线。
 */
@Composable
private fun JvmConnectionInfoReadoutDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(30.dp)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f))
    )
}

/**
 * 连接凭据表单卡片。
 *
 * @param address 当前连接地址。
 * @param username 当前用户名。
 * @param password 当前密码。
 * @param connectionName 当前连接别名。
 * @param canSave 是否允许保存。
 * @param saveText 保存按钮文案。
 * @param cancelText 取消按钮文案。
 * @param onAddressChange 地址变更回调。
 * @param onUsernameChange 用户名变更回调。
 * @param onPasswordChange 密码变更回调。
 * @param onConnectionNameChange 别名变更回调。
 * @param onSave 保存回调。
 * @param onCancel 取消回调。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun JvmConnectionCredentialFormCard(
    address: String,
    username: String,
    password: String,
    connectionName: String,
    canSave: Boolean,
    saveText: String,
    cancelText: String,
    onAddressChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConnectionNameChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(XyTheme.dimens.outerHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
        ) {
            // 链接地址
            JvmConnectionFormField(
                label = stringResource(Res.string.connection_address),
                tag = stringResource(Res.string.jvm_connection_config_info_screen_text_43),
            ) {
                JvmAddressInputEdit(
                    address = address,
                    updateAddress = onAddressChange
                )
            }

            // 地址示例与协议支持提示
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                JvmConnectionHintChip(
                    text = stringResource(
                        Res.string.jvm_connection_config_info_screen_text_53,
                        ApiConstants.HTTP
                    ),
                    accent = true,
                )
                JvmConnectionHintChip(
                    text = stringResource(Res.string.jvm_connection_new_screen_text_11)
                )
            }

            // 用户名与密码双列排布，窄窗口下自动上下堆叠。
            JvmConnectionCredentialGrid(
                username = username,
                password = password,
                onUsernameChange = onUsernameChange,
                onPasswordChange = onPasswordChange,
            )

            // 设置别名
            JvmConnectionFormField(
                label = stringResource(Res.string.set_alias),
                tag = stringResource(Res.string.jvm_connection_config_info_screen_text_43),
            ) {
                JvmConnectionNameInputEdit(
                    connectionName = connectionName,
                    updateConnectionName = onConnectionNameChange
                )
            }

            // 保存与取消
            Row(
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                XyButton(
                    enabled = canSave,
                    onClick = onSave,
                    text = saveText,
                )
                OutlinedButton(
                    modifier = Modifier.widthIn(min = 72.dp),
                    onClick = onCancel,
                    shape = RoundedCornerShape(XyTheme.dimens.corner),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    XyText(
                        text = cancelText,
                        fontWeight = FontWeight.Normal,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

/**
 * 表单字段容器：等宽小标签 + 必填等标签 + 输入控件。
 *
 * @param label 字段标签。
 * @param tag 字段标签右侧的提示标签；为空时不展示。
 * @param content 字段输入控件。
 */
@Composable
private fun JvmConnectionFormField(
    label: String,
    tag: String? = null,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            XyTextSub(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = JvmConnectionFieldLabelSize,
                    letterSpacing = JvmConnectionFieldLabelLetterSpacing,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
            if (!tag.isNullOrBlank()) {
                JvmConnectionFieldTag(text = tag)
            }
        }
        content()
    }
}

/**
 * 字段标签旁的小标签。
 *
 * @param text 标签文案。
 */
@Composable
private fun JvmConnectionFieldTag(text: String) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = Color.Transparent,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.14f)
        )
    ) {
        XyTextSub(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = JvmConnectionFieldLabelSize,
                letterSpacing = 0.4.sp,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

/**
 * 用户名与密码字段的双列布局。
 *
 * @param username 当前用户名。
 * @param password 当前密码。
 * @param onUsernameChange 用户名变更回调。
 * @param onPasswordChange 密码变更回调。
 */
@Composable
private fun JvmConnectionCredentialGrid(
    username: String,
    password: String,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
) {
    val usernameField: @Composable () -> Unit = {
        JvmConnectionFormField(
            label = stringResource(Res.string.username),
            tag = stringResource(Res.string.jvm_connection_config_info_screen_text_45),
        ) {
            JvmUsernameInputEdit(
                username = username,
                updateUsername = onUsernameChange
            )
        }
    }
    val passwordField: @Composable () -> Unit = {
        JvmConnectionFormField(
            label = stringResource(Res.string.password),
            tag = stringResource(Res.string.jvm_connection_config_info_screen_text_47),
        ) {
            JvmPasswordInputEdit(
                password = password,
                updatePassword = onPasswordChange
            )
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val gap = XyTheme.dimens.contentPadding
        if (maxWidth < JvmConnectionFieldGridBreakpoint) {
            Column(verticalArrangement = Arrangement.spacedBy(gap)) {
                usernameField()
                passwordField()
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                Box(modifier = Modifier.weight(1f)) { usernameField() }
                Box(modifier = Modifier.weight(1f)) { passwordField() }
            }
        }
    }
}

/**
 * 表单提示标签。
 *
 * @param text 标签文案。
 * @param accent 是否使用主色强调。
 */
@Composable
private fun JvmConnectionHintChip(
    text: String,
    accent: Boolean = false,
) {
    val color = if (accent) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = if (accent) color.copy(alpha = 0.10f) else Color.Transparent,
        border = BorderStroke(
            width = 1.dp,
            color = color.copy(alpha = if (accent) 0.34f else 0.14f)
        )
    ) {
        XyTextSub(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
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
