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
import cn.xybbz.ui.components.JvmSettingProxyModeCardHeight
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
    val proxyModeLabel = if (enabled) "手动代理" else "关闭"
    val parsedProxy = addressText.toJvmProxyParsedAddress()

    JvmSettingPageScaffold(
        contentPadding = PaddingValues(
            horizontal = XyTheme.dimens.outerHorizontalPadding * 2,
            vertical = XyTheme.dimens.outerVerticalPadding * 3
        )
    ) {
        JvmSettingPageHeader(
            title = pageTitle,
            description = "把代理开关、服务器地址和连通性测试集中在一个页面内，便于桌面端排查服务访问失败。当前配置会应用到后续服务端请求。",
        ) {
            JvmSettingStatusCard(
                width = JvmSettingSummaryCardWidth,
                prominentValue = true,
                items = listOf(
                    JvmSettingStatusCardItem(label = "代理状态", value = proxyModeLabel),
                    JvmSettingStatusCardItem(label = "协议", value = parsedProxy.protocol),
                    JvmSettingStatusCardItem(label = "测试目标", value = connectionAddress.proxyTargetLabel()),
                )
            )
        }

        JvmSettingTwoPaneContent(
            leftContent = {
                JvmSettingSection(
                    title = "代理模式",
                    subtitle = "清楚区分关闭和手动代理；当前版本会保存这两种真实可用状态。",
                    badge = "当前：$proxyModeLabel",
                    contentContainerEnabled = false,
                    qualityNote = "代理只影响服务端访问和资源请求，不改变本地播放队列、缓存目录和数据库。"
                ) {
                    JvmProxyModeGrid(
                        enabled = enabled,
                        onEnabledChange = { checked ->
                            proxyConfigViewModel.updateEnabled(checked)
                        }
                    )
                }

                JvmSettingSection(
                    title = "手动代理",
                    subtitle = "桌面端使用一条代理地址字符串，支持 host:port 或带协议的 URL 写法。",
                    badge = "HTTP",
                    contentContainerEnabled = false,
                    qualityNote = "JVM 当前代理解析会自动为未带协议的地址补齐 ${ApiConstants.HTTP}，例如 ${Constants.DEFAULT_PROXY_ADDRESS} 会按 ${ApiConstants.HTTP_PROTOCOL_NAME} 代理使用。"
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
                    title = "例外规则",
                    subtitle = "这些地址通常属于本机或局域网场景，排查网络时优先确认是否需要直连。",
                    badge = "参考",
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
                title = "关闭",
                description = "清除当前代理选择，服务请求直接连接服务器。",
                status = if (!enabled) "当前模式" else "点击切换",
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
                title = "手动",
                description = "使用下方代理地址转发服务端请求。",
                status = if (enabled) "当前模式" else "点击切换",
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
        cardHeight = JvmSettingProxyModeCardHeight,
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
                        text = "示例：${Constants.DEFAULT_PROXY_ADDRESS} 或 ${ApiConstants.HTTP}${Constants.DEFAULT_PROXY_ADDRESS}",
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
                        label = "协议",
                        value = parsedProxy.protocol,
                        description = "未填写协议时按 HTTP"
                    ),
                    JvmProxyInfoTile(
                        label = "主机",
                        value = parsedProxy.host,
                        description = "代理服务监听地址"
                    ),
                    JvmProxyInfoTile(
                        label = "端口",
                        value = parsedProxy.port,
                        description = "代理服务监听端口"
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
                text = "使用当前代理向 ${connectionAddress.proxyTargetLabel()} 发起轻量请求，结果会通过页面提示反馈。",
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
        JvmProxyPill(text = if (enabled) "手动代理已启用" else "当前直连")
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
            JvmProxySummaryLine(label = "代理地址", value = addressText.ifBlank { "未填写" })
            JvmProxySummaryLine(label = "连接目标", value = connectionAddress.proxyTargetLabel())
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
                description = "本机服务与开发环境",
                value = "建议直连"
            ),
            JvmProxyRouteRule(
                icon = Res.drawable.signal_cellular_alt_24px,
                name = "192.168.*",
                description = "家庭局域网音乐服务器",
                value = "局域网"
            ),
            JvmProxyRouteRule(
                icon = Res.drawable.http_24px,
                name = "media.local",
                description = "本地域名或内网 DNS",
                value = "按需直连"
            ),
        ).forEach { rule ->
            JvmProxyRouteCard(rule = rule)
        }

        JvmSettingNote(text = "当前页面不会自动维护例外列表；如果服务器在局域网内，通常不需要开启代理。")
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
private fun String.toJvmProxyParsedAddress(): JvmProxyParsedAddress {
    if (isBlank()) {
        return JvmProxyParsedAddress(protocol = "HTTP", host = "未填写", port = "未填写")
    }

    val parsedUri = runCatching {
        URI(withDefaultHttpScheme())
    }.getOrNull()
    val protocol = parsedUri?.scheme?.uppercase()?.takeIf { it.isNotBlank() } ?: ApiConstants.HTTP_PROTOCOL_NAME
    val host = parsedUri?.host?.takeIf { it.isNotBlank() } ?: "未解析"
    val port = parsedUri?.port?.takeIf { it > 0 }?.toString() ?: "未解析"
    return JvmProxyParsedAddress(
        protocol = protocol,
        host = host,
        port = port
    )
}

/**
 * 将连接地址缩短成适合状态卡和测试卡展示的目标文案。
 */
private fun String.proxyTargetLabel(): String {
    return trim()
        .takeIf { it.isNotBlank() }
        ?.removePrefix(ApiConstants.HTTPS)
        ?.removePrefix(ApiConstants.HTTP)
        ?: "未连接"
}
