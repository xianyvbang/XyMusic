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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyEdit
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.cancel_24px
import xymusic_kmp.composeapp.generated.resources.clear
import xymusic_kmp.composeapp.generated.resources.connection_address_hint
import xymusic_kmp.composeapp.generated.resources.httpInput
import xymusic_kmp.composeapp.generated.resources.http_24px
import xymusic_kmp.composeapp.generated.resources.label_24px
import xymusic_kmp.composeapp.generated.resources.password
import xymusic_kmp.composeapp.generated.resources.password_24px
import xymusic_kmp.composeapp.generated.resources.person_24px
import xymusic_kmp.composeapp.generated.resources.set_alias
import xymusic_kmp.composeapp.generated.resources.username
import xymusic_kmp.composeapp.generated.resources.visibility_24px
import xymusic_kmp.composeapp.generated.resources.visibility_off_24px
import cn.xybbz.ui.xy.XyIconButton as IconButton

/**
 * JVM 连接地址输入框。
 *
 * @param address 当前连接地址。
 * @param updateAddress 地址变更回调。
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun JvmAddressInputEdit(
    address: String,
    updateAddress: (String) -> Unit,
) {
    JvmConnectionDataInfoInputEdit(
        text = address,
        onChange = {
            updateAddress(it)
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
        hint = stringResource(Res.string.connection_address_hint),
        icon = painterResource(Res.drawable.http_24px),
        iconContentDescription = stringResource(Res.string.httpInput),
        actionContent = if (address.isNotBlank()) {
            {
                IconButton(
                    onClick = composeClick {
                        updateAddress("")
                    },
                    modifier = Modifier.size(IconButtonDefaults.extraSmallIconSize)
                ) {
                    Icon(
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        painter = painterResource(Res.drawable.cancel_24px),
                        contentDescription = stringResource(Res.string.clear)
                    )
                }
            }
        } else null
    )
}

/**
 * JVM 连接用户名输入框。
 *
 * @param modifier 外部布局修饰符。
 * @param username 当前用户名。
 * @param updateUsername 用户名变更回调。
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun JvmUsernameInputEdit(
    modifier: Modifier = Modifier,
    username: String,
    updateUsername: (String) -> Unit,
) {
    JvmConnectionDataInfoInputEdit(
        text = username,
        modifier = modifier,
        onChange = {
            updateUsername(it)
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        hint = stringResource(Res.string.username),
        icon = painterResource(Res.drawable.person_24px),
        iconContentDescription = stringResource(Res.string.username),
        actionContent = if (username.isNotBlank()) {
            {
                IconButton(
                    onClick = composeClick {
                        updateUsername("")
                    },
                    modifier = Modifier.size(IconButtonDefaults.extraSmallIconSize)
                ) {
                    Icon(
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        painter = painterResource(Res.drawable.cancel_24px),
                        contentDescription = stringResource(Res.string.clear)
                    )
                }
            }
        } else null
    )
}

/**
 * JVM 连接密码输入框。
 *
 * @param modifier 外部布局修饰符。
 * @param password 当前密码。
 * @param updatePassword 密码变更回调。
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun JvmPasswordInputEdit(
    modifier: Modifier = Modifier,
    password: String,
    updatePassword: (String) -> Unit,
) {
    // 控制密码是否以明文展示。
    var showPassword by remember { mutableStateOf(false) }
    JvmConnectionDataInfoInputEdit(
        text = password,
        modifier = modifier,
        onChange = {
            updatePassword(it)
        },
        actionContent = {
            IconButton(
                onClick = composeClick {
                    showPassword = !showPassword
                },
                modifier = Modifier.size(IconButtonDefaults.extraSmallIconSize)
            ) {
                Icon(
                    painter = painterResource(if (showPassword) Res.drawable.visibility_24px else Res.drawable.visibility_off_24px),
                    contentDescription = null
                )
            }
        },
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        hint = stringResource(Res.string.password),
        icon = painterResource(Res.drawable.password_24px),
        iconContentDescription = stringResource(Res.string.password)
    )
}

/**
 * JVM 连接别名输入框。
 *
 * @param modifier 外部布局修饰符。
 * @param connectionName 当前连接别名。
 * @param updateConnectionName 别名变更回调。
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun JvmConnectionNameInputEdit(
    modifier: Modifier = Modifier,
    connectionName: String,
    updateConnectionName: (String) -> Unit,
) {
    JvmConnectionDataInfoInputEdit(
        text = connectionName,
        modifier = modifier,
        onChange = {
            updateConnectionName(it)
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        hint = stringResource(Res.string.set_alias),
        icon = painterResource(Res.drawable.label_24px),
        iconContentDescription = stringResource(Res.string.set_alias),
        actionContent = if (connectionName.isNotBlank()) {
            {
                IconButton(
                    onClick = composeClick {
                        updateConnectionName("")
                    },
                    modifier = Modifier.size(IconButtonDefaults.extraSmallIconSize)
                ) {
                    Icon(
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        painter = painterResource(Res.drawable.cancel_24px),
                        contentDescription = stringResource(Res.string.clear)
                    )
                }
            }
        } else null
    )
}

/**
 * JVM 连接表单通用输入框骨架。
 *
 * @param modifier 外部布局修饰符。
 * @param text 当前输入内容。
 * @param onChange 输入内容变更回调。
 * @param keyboardOptions 键盘类型配置。
 * @param visualTransformation 输入内容展示转换。
 * @param hint 占位提示文案。
 * @param icon 左侧图标。
 * @param iconContentDescription 左侧图标的无障碍描述。
 * @param actionContent 右侧操作内容。
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun JvmConnectionDataInfoInputEdit(
    modifier: Modifier = Modifier,
    text: String,
    onChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    hint: String? = null,
    icon: Painter,
    iconContentDescription: String,
    actionContent: (@Composable () -> Unit)? = null,
) {
    XyEdit(
        text = text,
        modifier = modifier
            .fillMaxWidth()
            .height(XyTheme.dimens.itemHeight),
        paddingValues = PaddingValues(
            vertical = XyTheme.dimens.outerVerticalPadding
        ),
        onChange = onChange,
        singleLine = true,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        hint = hint,
        leadingContent = {
            Icon(
                painter = icon,
                contentDescription = iconContentDescription
            )
        },
        actionContent = actionContent
    )
}
