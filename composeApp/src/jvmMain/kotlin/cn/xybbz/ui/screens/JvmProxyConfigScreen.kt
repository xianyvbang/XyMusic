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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.xybbz.api.constants.ApiConstants
import cn.xybbz.api.utils.withDefaultHttpScheme
import cn.xybbz.common.constants.Constants
import cn.xybbz.ui.components.JvmSettingActionEntry
import cn.xybbz.ui.components.JvmSettingNote
import cn.xybbz.ui.components.JvmSettingPageHeader
import cn.xybbz.ui.components.JvmSettingPageScaffold
import cn.xybbz.ui.components.JvmSettingSection
import cn.xybbz.ui.components.JvmSettingStatusCard
import cn.xybbz.ui.components.JvmSettingStatusCardItem
import cn.xybbz.ui.components.JvmSettingSummaryCardWidth
import cn.xybbz.ui.components.JvmSettingTwoPaneContent
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyButton
import cn.xybbz.ui.xy.XyEdit
import cn.xybbz.ui.xy.XyIconTextButton
import cn.xybbz.ui.xy.XyText
import cn.xybbz.ui.xy.XyTextSub
import cn.xybbz.viewmodel.ProxyConfigViewModel
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.*
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.check_24px
import xymusic_kmp.composeapp.generated.resources.close_24px
import xymusic_kmp.composeapp.generated.resources.http_24px
import xymusic_kmp.composeapp.generated.resources.info_24px
import xymusic_kmp.composeapp.generated.resources.poxy_config
import xymusic_kmp.composeapp.generated.resources.proxy_address
import xymusic_kmp.composeapp.generated.resources.save
import xymusic_kmp.composeapp.generated.resources.signal_cellular_alt_24px
import xymusic_kmp.composeapp.generated.resources.test_connection
import java.net.URI
import cn.xybbz.ui.components.JvmSettingActionGrid as JvmSettingActionEntryGrid

/**
 * JVM 代理路由规则展示数据。
 *
 * @param icon 规则图标资源。
 * @param name 规则名称。
 * @param description 规则说明。
 * @param value 规则状态标签。
 */
private data class JvmProxyRouteRule(
    val icon: DrawableResource,
    val name: String,
    val description: String,
    val value: String,
)

/**
 * JVM 代理只读配置摘要展示数据。
 *
 * @param label 摘要标签。
 * @param value 摘要当前值。
 * @param description 摘要辅助说明。
 */
private data class JvmProxyInfoTile(
    val label: String,
    val value: String,
    val description: String,
)

/**
 * JVM 桌面端代理设置页面。
 */
@Composable
fun JvmProxyConfigScreen(
    proxyConfigViewModel: ProxyConfigViewModel = koinViewModel<ProxyConfigViewModel>()
) {
    val pageTitle = stringResource(Res.string.poxy_config)
    val proxyAddressTitle = stringResource(Res.string.proxy_address)
    val testConnectionTitle = stringResource(Res.string.test_connection)
    val saveTitle = stringResource(Res.string.save)
    val enabled = proxyConfigViewModel.enabled
    val addressValue = proxyConfigViewModel.addressValue
    val addressText = addressValue.text.trim()
    val connectionAddress = proxyConfigViewModel.getConnectionAddress()
    val proxyModeLabel = if (enabled) stringResource(Res.string.jvm_proxy_config_screen_text_01) else stringResource(Res.string.close)
    val parsedProxy = addressText.toJvmProxyParsedAddress()

    JvmSettingPageScaffold(
        contentPadding = PaddingValues(
            horizontal = XyTheme.dimens.outerHorizontalPadding * 2,
            vertical = XyTheme.dimens.outerVerticalPadding * 3
        )
    ) {
        JvmSettingPageHeader(
            title = pageTitle,
            description = stringResource(Res.string.jvm_proxy_config_screen_text_02),
        ) {
            JvmSettingStatusCard(
                width = JvmSettingSummaryCardWidth,
                prominentValue = true,
                items = listOf(
                    JvmSettingStatusCardItem(label = stringResource(Res.string.jvm_proxy_config_screen_text_03), value = proxyModeLabel),
                    JvmSettingStatusCardItem(label = stringResource(Res.string.jvm_connection_new_screen_text_13), value = parsedProxy.protocol),
                    JvmSettingStatusCardItem(label = stringResource(Res.string.jvm_proxy_config_screen_text_04), value = connectionAddress.proxyTargetLabel()),
                )
            )
        }

        JvmSettingTwoPaneContent(
            leftContent = {
                JvmSettingSection(
                    title = stringResource(Res.string.jvm_proxy_config_screen_text_05),
                    subtitle = stringResource(Res.string.jvm_proxy_config_screen_text_06),
                    badge = stringResource(Res.string.jvm_cache_limit_screen_text_07, proxyModeLabel),
                    contentContainerEnabled = false,
                    qualityNote = stringResource(Res.string.jvm_proxy_config_screen_text_07)
                ) {
                    JvmProxyModeGrid(
                        enabled = enabled,
                        onEnabledChange = { checked ->
                            proxyConfigViewModel.updateEnabled(checked)
                        }
                    )
                }

                JvmSettingSection(
                    title = stringResource(Res.string.jvm_proxy_config_screen_text_01),
                    subtitle = stringResource(Res.string.jvm_proxy_config_screen_text_08),
                    badge = "HTTP",
                    contentContainerEnabled = false,
                    qualityNote = stringResource(Res.string.jvm_proxy_config_screen_text_09, ApiConstants.HTTP, Constants.DEFAULT_PROXY_ADDRESS, ApiConstants.HTTP_PROTOCOL_NAME)
                ) {
                    JvmProxyAddressPanel(
                        title = proxyAddressTitle,
                        addressValue = addressValue,
                        parsedProxy = parsedProxy,
                        onAddressChange = { newAddress ->
                            proxyConfigViewModel.updateAddress(newAddress)
                        }
                    )
                }
            },
            rightContent = {
                JvmProxyActionPanel(
                    enabled = enabled,
                    addressText = addressText,
                    connectionAddress = connectionAddress,
                    testConnectionTitle = testConnectionTitle,
                    saveTitle = saveTitle,
                    onTestConnection = {
                        proxyConfigViewModel.testProxyConfig()
                    },
                    onSave = {
                        proxyConfigViewModel.saveConfig()
                    }
                )

                JvmSettingSection(
                    title = stringResource(Res.string.jvm_proxy_config_screen_text_10),
                    subtitle = stringResource(Res.string.jvm_proxy_config_screen_text_11),
                    badge = stringResource(Res.string.jvm_proxy_config_screen_text_12),
                    contentContainerEnabled = false,
                ) {
                    JvmProxyRouteRules()
                }
            }
        )
    }
}

/**
 * JVM 代理模式卡片网格。
 */
@Composable
private fun JvmProxyModeGrid(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
) {
    JvmSettingActionEntryGrid(
        actionEntries = listOf(
            JvmSettingActionEntry(
                icon = Res.drawable.close_24px,
                kicker = "OFF",
                title = stringResource(Res.string.close),
                description = stringResource(Res.string.jvm_proxy_config_screen_text_13),
                status = if (!enabled) stringResource(Res.string.jvm_proxy_config_screen_text_14) else stringResource(Res.string.jvm_interface_setting_screen_text_12),
                selected = !enabled,
                role = Role.RadioButton,
                onClick = {
                    if (enabled) {
                        onEnabledChange(false)
                    }
                },
            ),
            JvmSettingActionEntry(
                icon = Res.drawable.signal_cellular_alt_24px,
                kicker = "MANUAL",
                title = stringResource(Res.string.jvm_cache_limit_screen_text_42),
                description = stringResource(Res.string.jvm_proxy_config_screen_text_15),
                status = if (enabled) stringResource(Res.string.jvm_proxy_config_screen_text_14) else stringResource(Res.string.jvm_interface_setting_screen_text_12),
                selected = enabled,
                role = Role.RadioButton,
                onClick = {
                    if (!enabled) {
                        onEnabledChange(true)
                    }
                },
            ),
        ),
        fillTwoColumnWidth = true,
    )
}

/**
 * JVM 代理地址编辑面板。
 */
@Composable
private fun JvmProxyAddressPanel(
    title: String,
    addressValue: TextFieldValue,
    parsedProxy: JvmProxyParsedAddress,
    onAddressChange: (TextFieldValue) -> Unit,
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
            verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                JvmProxyIconBox(icon = Res.drawable.http_24px, selected = true)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding / 2)
                ) {
                    XyText(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    XyTextSub(
                        text = stringResource(Res.string.jvm_proxy_config_screen_text_16, Constants.DEFAULT_PROXY_ADDRESS, ApiConstants.HTTP, Constants.DEFAULT_PROXY_ADDRESS),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            JvmProxyConfigInput(
                modifier = Modifier.fillMaxWidth(),
                text = addressValue,
                onChange = { newValue ->
                    onAddressChange(newValue)
                },
                hint = Constants.DEFAULT_PROXY_ADDRESS
            )

            JvmProxyInfoGrid(
                tiles = listOf(
                    JvmProxyInfoTile(
                        label = stringResource(Res.string.jvm_connection_new_screen_text_13),
                        value = parsedProxy.protocol,
                        description = stringResource(Res.string.jvm_proxy_config_screen_text_17)
                    ),
                    JvmProxyInfoTile(
                        label = stringResource(Res.string.jvm_proxy_config_screen_text_18),
                        value = parsedProxy.host,
                        description = stringResource(Res.string.jvm_proxy_config_screen_text_19)
                    ),
                    JvmProxyInfoTile(
                        label = stringResource(Res.string.jvm_proxy_config_screen_text_20),
                        value = parsedProxy.port,
                        description = stringResource(Res.string.jvm_proxy_config_screen_text_21)
                    ),
                )
            )
        }
    }
}

/**
 * JVM 代理地址输入框。
 */
@Composable
private fun JvmProxyConfigInput(
    text: TextFieldValue,
    onChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    hint: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    XyEdit(
        modifier = modifier.heightIn(min = 44.dp),
        text = text,
        onChange = onChange,
        hint = hint,
        paddingValues = PaddingValues(),
        backgroundColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
        textStyle = MaterialTheme.typography.bodySmall.copy(
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Start
        ),
        textContentAlignment = Alignment.CenterStart,
        singleLine = true,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
    )
}

/**
 * JVM 代理只读信息网格。
 */
@Composable
private fun JvmProxyInfoGrid(tiles: List<JvmProxyInfoTile>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
    ) {
        tiles.forEach { tile ->
            JvmProxyInfoTileCard(
                modifier = Modifier.weight(1f),
                tile = tile
            )
        }
    }
}

/**
 * JVM 代理只读信息卡。
 */
@Composable
private fun JvmProxyInfoTileCard(
    modifier: Modifier,
    tile: JvmProxyInfoTile,
) {
    Surface(
        modifier = modifier.heightIn(min = 88.dp),
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)
        )
    ) {
        Column(
            modifier = Modifier.padding(XyTheme.dimens.contentPadding),
            verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding / 2)
        ) {
            XyTextSub(
                text = tile.label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            XyText(
                text = tile.value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            XyTextSub(
                text = tile.description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * JVM 代理测试和保存操作面板。
 */
@Composable
private fun JvmProxyActionPanel(
    enabled: Boolean,
    addressText: String,
    connectionAddress: String,
    testConnectionTitle: String,
    saveTitle: String,
    onTestConnection: () -> Unit,
    onSave: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        color = MaterialTheme.colorScheme.primary.copy(alpha = if (XyTheme.configs.isDarkTheme) 0.18f else 0.10f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
        )
    ) {
        Column(
            modifier = Modifier.padding(XyTheme.dimens.outerHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding)
        ) {
            JvmProxyActionHeader(enabled = enabled)
            XyText(
                text = testConnectionTitle,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            XyTextSub(
                text = stringResource(Res.string.jvm_proxy_config_screen_text_22, connectionAddress.proxyTargetLabel()),
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            JvmProxyConnectionSummary(
                addressText = addressText,
                connectionAddress = connectionAddress
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                XyIconTextButton(
                    modifier = Modifier.weight(1f),
                    onClick = onTestConnection,
                    text = testConnectionTitle,
                    icon = Res.drawable.signal_cellular_alt_24px,
                    enabled = addressText.isNotBlank(),
                )
                XyButton(
                    modifier = Modifier.widthIn(min = 96.dp),
                    onClick = onSave,
                    text = saveTitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    backgroundColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                )
            }
        }
    }
}

/**
 * JVM 代理操作面板头部状态。
 */
@Composable
private fun JvmProxyActionHeader(enabled: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        JvmProxyPill(text = if (enabled) stringResource(Res.string.jvm_proxy_config_screen_text_23) else stringResource(Res.string.jvm_proxy_config_screen_text_24))
        JvmProxyIconBox(
            icon = if (enabled) Res.drawable.check_24px else Res.drawable.info_24px,
            selected = enabled
        )
    }
}

/**
 * JVM 代理测试面板的连接摘要。
 */
@Composable
private fun JvmProxyConnectionSummary(
    addressText: String,
    connectionAddress: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(XyTheme.dimens.contentPadding),
            verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding)
        ) {
            JvmProxySummaryLine(label = stringResource(Res.string.proxy_address), value = addressText.ifBlank { stringResource(Res.string.jvm_connection_config_info_screen_text_37) })
            JvmProxySummaryLine(label = stringResource(Res.string.jvm_proxy_config_screen_text_25), value = connectionAddress.proxyTargetLabel())
        }
    }
}

/**
 * JVM 代理摘要行。
 */
@Composable
private fun JvmProxySummaryLine(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        XyTextSub(
            modifier = Modifier.width(68.dp),
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        XyText(
            modifier = Modifier.weight(1f),
            text = value,
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * JVM 代理例外规则列表。
 */
@Composable
private fun JvmProxyRouteRules() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding)
    ) {
        listOf(
            JvmProxyRouteRule(
                icon = Res.drawable.info_24px,
                name = "localhost",
                description = stringResource(Res.string.jvm_proxy_config_screen_text_26),
                value = stringResource(Res.string.jvm_proxy_config_screen_text_27)
            ),
            JvmProxyRouteRule(
                icon = Res.drawable.signal_cellular_alt_24px,
                name = "192.168.*",
                description = stringResource(Res.string.jvm_proxy_config_screen_text_28),
                value = stringResource(Res.string.jvm_proxy_config_screen_text_29)
            ),
            JvmProxyRouteRule(
                icon = Res.drawable.http_24px,
                name = "media.local",
                description = stringResource(Res.string.jvm_proxy_config_screen_text_30),
                value = stringResource(Res.string.jvm_proxy_config_screen_text_31)
            ),
        ).forEach { rule ->
            JvmProxyRouteCard(rule = rule)
        }

        JvmSettingNote(text = stringResource(Res.string.jvm_proxy_config_screen_text_32))
    }
}

/**
 * JVM 代理例外规则卡片。
 */
@Composable
private fun JvmProxyRouteCard(rule: JvmProxyRouteRule) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(XyTheme.dimens.contentPadding),
            horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            JvmProxyIconBox(icon = rule.icon, selected = true)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding / 2)
            ) {
                XyText(
                    text = rule.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                XyTextSub(
                    text = rule.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            JvmProxyPill(text = rule.value)
        }
    }
}

/**
 * JVM 代理图标容器。
 */
@Composable
private fun JvmProxyIconBox(
    icon: DrawableResource,
    selected: Boolean,
    size: Dp = 34.dp,
) {
    val accentColor = MaterialTheme.colorScheme.primary
    val shape = RoundedCornerShape(XyTheme.dimens.corner - XyTheme.dimens.outerVerticalPadding / 2)
    Box(
        modifier = Modifier
            .size(size)
            .clip(shape)
            .background(
                color = if (selected) {
                    accentColor.copy(alpha = 0.18f)
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)
                }
            )
            .border(
                BorderStroke(
                    width = 1.dp,
                    color = if (selected) {
                        accentColor.copy(alpha = 0.26f)
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
            modifier = Modifier.size(size * 0.58f),
            tint = if (selected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * JVM 代理状态标签。
 */
@Composable
private fun JvmProxyPill(
    text: String,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Surface(
        shape = CircleShape,
        color = color.copy(alpha = 0.12f),
        contentColor = color,
        border = BorderStroke(
            width = 1.dp,
            color = color.copy(alpha = 0.28f)
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
            style = MaterialTheme.typography.labelSmall,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * JVM 代理地址解析后的展示值。
 *
 * @param protocol 代理协议显示值。
 * @param host 代理主机显示值。
 * @param port 代理端口显示值。
 */
private data class JvmProxyParsedAddress(
    val protocol: String,
    val host: String,
    val port: String,
)

/**
 * 将代理地址解析成页面可读的协议、主机和端口。
 */
@Composable
private fun String.toJvmProxyParsedAddress(): JvmProxyParsedAddress {
    if (isBlank()) {
        return JvmProxyParsedAddress(protocol = "HTTP", host = stringResource(Res.string.jvm_connection_config_info_screen_text_37), port = stringResource(Res.string.jvm_connection_config_info_screen_text_37))
    }

    val parsedUri = runCatching {
        URI(withDefaultHttpScheme())
    }.getOrNull()
    val protocol = parsedUri?.scheme?.uppercase()?.takeIf { it.isNotBlank() } ?: ApiConstants.HTTP_PROTOCOL_NAME
    val host = parsedUri?.host?.takeIf { it.isNotBlank() } ?: stringResource(Res.string.jvm_proxy_config_screen_text_33)
    val port = parsedUri?.port?.takeIf { it > 0 }?.toString() ?: stringResource(Res.string.jvm_proxy_config_screen_text_33)
    return JvmProxyParsedAddress(
        protocol = protocol,
        host = host,
        port = port
    )
}

/**
 * 将连接地址缩短成适合状态卡和测试卡展示的目标文案。
 */
@Composable
private fun String.proxyTargetLabel(): String {
    return trim()
        .takeIf { it.isNotBlank() }
        ?.removePrefix(ApiConstants.HTTPS)
        ?.removePrefix(ApiConstants.HTTP)
        ?: stringResource(Res.string.jvm_connection_config_info_screen_text_01)
}
