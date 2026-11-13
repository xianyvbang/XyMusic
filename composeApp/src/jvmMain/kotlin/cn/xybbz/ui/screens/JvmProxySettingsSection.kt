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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cn.xybbz.common.constants.Constants
import cn.xybbz.ui.components.JvmSettingFlowRow
import cn.xybbz.ui.components.JvmSettingNote
import cn.xybbz.ui.components.JvmSettingSwitchRow
import cn.xybbz.ui.components.JvmSettingWideGroup
import cn.xybbz.ui.ext.jvmHoverDebounceClickable
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyEdit
import cn.xybbz.ui.xy.XyText
import cn.xybbz.ui.xy.XyTextSub
import cn.xybbz.viewmodel.ProxyConfigViewModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import xymusic.composeapp.generated.resources.Res
import xymusic.composeapp.generated.resources.http_24px
import xymusic.composeapp.generated.resources.jvm_connection_config_info_screen_text_01
import xymusic.composeapp.generated.resources.jvm_proxy_config_screen_text_03
import xymusic.composeapp.generated.resources.jvm_proxy_config_screen_text_04
import xymusic.composeapp.generated.resources.jvm_proxy_config_screen_text_08
import xymusic.composeapp.generated.resources.jvm_proxy_config_screen_text_15
import xymusic.composeapp.generated.resources.jvm_proxy_config_screen_text_23
import xymusic.composeapp.generated.resources.jvm_proxy_config_screen_text_24
import xymusic.composeapp.generated.resources.jvm_proxy_settings_section_text_01
import xymusic.composeapp.generated.resources.jvm_setting_screen_text_22
import xymusic.composeapp.generated.resources.open_proxy
import xymusic.composeapp.generated.resources.proxy_address
import xymusic.composeapp.generated.resources.save
import xymusic.composeapp.generated.resources.signal_cellular_alt_24px
import xymusic.composeapp.generated.resources.test_connection

/**
 * 设置页内嵌的代理设置分区。
 *
 * 提供代理开关、地址编辑、连通性测试与保存能力，
 * 外观完全复用设置页的分组标题与设置行样式，内容统一收在设置页的网络分组内。
 *
 * @param proxyConfigViewModel 代理设置 ViewModel，提供开关、地址以及测试和保存动作。
 * @param modifier 外层布局修饰符，通常由设置页分组网格下发宽度。
 */
@Composable
internal fun JvmProxySettingsSection(
    proxyConfigViewModel: ProxyConfigViewModel,
    modifier: Modifier = Modifier,
) {
    // 网络分组使用宽内容布局，代理地址输入与操作按钮才能整行展示。
    JvmSettingWideGroup(
        modifier = modifier,
        title = stringResource(Res.string.jvm_setting_screen_text_22),
    ) {
        // 代理开关：直接使用设置页统一的开关行，开关状态写入仍由 ViewModel 负责。
        JvmSettingSwitchRow(
            icon = Res.drawable.signal_cellular_alt_24px,
            title = stringResource(Res.string.open_proxy),
            description = stringResource(Res.string.jvm_proxy_config_screen_text_15),
            checked = proxyConfigViewModel.enabled,
            onCheckedChange = { checked ->
                proxyConfigViewModel.updateEnabled(checked)
            },
        )

        // 代理地址小类的内容整体并入网络分组：地址输入、状态摘要与测试保存操作。
        XyTextSub(
            text = stringResource(Res.string.proxy_address),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
        JvmProxyAddressEditor(proxyConfigViewModel = proxyConfigViewModel)
        JvmProxyStatusRow(proxyConfigViewModel = proxyConfigViewModel)
        JvmProxyActionRow(proxyConfigViewModel = proxyConfigViewModel)
        // 地址写法说明沿用代理设置页的同一套文案，避免两处配置口径不一致。
        JvmSettingNote(text = stringResource(Res.string.jvm_proxy_config_screen_text_08))
    }
}

/**
 * 代理地址输入框。
 *
 * 复用连接表单的输入框样式，行首保留 HTTP 图标，内容使用等宽字体便于核对地址。
 *
 * @param proxyConfigViewModel 代理设置 ViewModel，负责地址输入状态读写。
 */
@Composable
private fun JvmProxyAddressEditor(proxyConfigViewModel: ProxyConfigViewModel) {
    XyEdit(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp),
        text = proxyConfigViewModel.addressValue,
        onChange = { value ->
            proxyConfigViewModel.updateAddress(value)
        },
        hint = Constants.DEFAULT_PROXY_ADDRESS,
        // 输入框自带圆角底色，外层不再叠加内边距，保持与设置行一致的高度观感。
        paddingValues = PaddingValues(),
        backgroundColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
        textStyle = MaterialTheme.typography.bodySmall.copy(
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Start,
        ),
        textContentAlignment = Alignment.CenterStart,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
        leadingContent = {
            Icon(
                painter = painterResource(Res.drawable.http_24px),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
    )
}

/**
 * 代理状态与测试目标摘要。
 *
 * 把原代理设置页头部状态卡里的关键信息压缩成行内胶囊，适配设置页的紧凑排版。
 *
 * @param proxyConfigViewModel 代理设置 ViewModel，提供开关状态与当前连接地址。
 */
@Composable
private fun JvmProxyStatusRow(proxyConfigViewModel: ProxyConfigViewModel) {
    val enabled = proxyConfigViewModel.enabled
    // 开关状态文案沿用代理设置页的启用/直连表述，保证两处口径一致。
    val statusLabel = if (enabled) {
        stringResource(Res.string.jvm_proxy_config_screen_text_23)
    } else {
        stringResource(Res.string.jvm_proxy_config_screen_text_24)
    }
    val targetLabel = proxyConfigViewModel.getConnectionAddress()
        .takeIf { it.isNotBlank() }
        ?: stringResource(Res.string.jvm_connection_config_info_screen_text_01)

    JvmSettingFlowRow(
        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
        verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding),
    ) {
        JvmProxyInfoPill(
            label = stringResource(Res.string.jvm_proxy_config_screen_text_03),
            value = statusLabel,
            highlighted = enabled,
        )
        JvmProxyInfoPill(
            label = stringResource(Res.string.jvm_proxy_config_screen_text_04),
            value = targetLabel,
            highlighted = false,
        )
    }
}

/**
 * 代理测试与保存操作行。
 *
 * @param proxyConfigViewModel 代理设置 ViewModel，负责连通性测试与配置保存。
 */
@Composable
private fun JvmProxyActionRow(proxyConfigViewModel: ProxyConfigViewModel) {
    // 地址为空时禁用测试，避免发出必然失败的请求。
    val addressText = proxyConfigViewModel.addressValue.text.trim()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        JvmProxyActionPill(
            text = stringResource(Res.string.test_connection),
            enabled = addressText.isNotBlank(),
            onClick = {
                proxyConfigViewModel.testProxyConfig()
            },
        )
        JvmProxyActionPill(
            text = stringResource(Res.string.save),
            onClick = {
                proxyConfigViewModel.saveConfig()
            },
        )
        // 保存说明跟在操作按钮后，提示开关与地址需要保存后才会应用到后续请求。
        XyTextSub(
            modifier = Modifier.weight(1f),
            text = stringResource(Res.string.jvm_proxy_settings_section_text_01),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * 代理信息胶囊。
 *
 * @param label 信息标签。
 * @param value 信息当前值。
 * @param highlighted 是否使用强调色，用于区分已启用状态。
 */
@Composable
private fun JvmProxyInfoPill(
    label: String,
    value: String,
    highlighted: Boolean,
) {
    val colorScheme = MaterialTheme.colorScheme
    val accentColor = if (highlighted) colorScheme.primary else colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(accentColor.copy(alpha = 0.08f))
            .border(
                BorderStroke(1.dp, accentColor.copy(alpha = 0.18f)),
                CircleShape,
            )
            .padding(
                horizontal = XyTheme.dimens.contentPadding,
                vertical = XyTheme.dimens.outerVerticalPadding / 2,
            ),
        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding / 2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        XyTextSub(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
        XyText(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            color = accentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * 代理操作胶囊按钮。
 *
 * 与存储管理的行尾操作保持同一套视觉样式，禁用时降低透明度并移除点击。
 *
 * @param text 按钮文案。
 * @param enabled 是否可点击。
 * @param onClick 点击回调。
 */
@Composable
private fun JvmProxyActionPill(
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
            // 禁用态不挂点击回调，避免地址为空时仍发起测试请求。
            onClick = if (enabled) onClick else null,
        ),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary.copy(
            alpha = if (buttonHovered) 0.20f else 0.12f
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f * contentAlpha),
        ),
    ) {
        XyText(
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
