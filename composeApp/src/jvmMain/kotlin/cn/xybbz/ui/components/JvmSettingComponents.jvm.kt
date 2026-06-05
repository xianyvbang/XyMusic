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

package cn.xybbz.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.xybbz.ui.ext.jvmHoverDebounceClickable
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyColumnScreen
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.about
import xymusic_kmp.composeapp.generated.resources.chevron_right_24px
import xymusic_kmp.composeapp.generated.resources.customize_lyric_settings
import xymusic_kmp.composeapp.generated.resources.download_24px
import xymusic_kmp.composeapp.generated.resources.download_max_list
import xymusic_kmp.composeapp.generated.resources.info_24px
import xymusic_kmp.composeapp.generated.resources.interface_settings
import xymusic_kmp.composeapp.generated.resources.language
import xymusic_kmp.composeapp.generated.resources.music_note_24px
import xymusic_kmp.composeapp.generated.resources.settings_24px

/** 通用设置入口区域的最小宽度，低于该宽度时退化为单列卡片。 */
private val JvmSettingActionGridMinWidth = 320.dp

/** 通用入口卡片在右侧栏双列场景下使用的最小紧凑宽度。 */
private val JvmSettingActionCardCompactWidth = 154.dp

/** 通用入口卡片在双列场景下允许的最大宽度，避免右栏加宽后卡片被拉得过宽。 */
private val JvmSettingActionCardMaxWidth = 196.dp

/** 通用入口卡片固定高度，保证四个入口在 FlowRow 中等高排列。 */
private val JvmSettingActionCardHeight = 148.dp

/** 通用入口卡片移入时的上移距离，保持与专辑卡片 hover 反馈一致。 */
private val JvmSettingActionCardLiftOffset = (-6).dp

/** 设置页统一图标容器尺寸。 */
private val JvmSettingIconSize = 32.dp

/**
 * 通用入口卡片的渲染数据。
 *
 * @param icon 卡片小标题图标。
 * @param kicker 卡片小标题。
 * @param title 卡片主标题。
 * @param description 卡片说明。
 * @param onClick 卡片点击事件。
 */
private data class JvmSettingActionEntry(
    val icon: DrawableResource,
    val kicker: String,
    val title: String,
    val description: String,
    val onClick: () -> Unit,
)

/**
 * JVM 设置页统一换行容器。
 *
 * @param modifier 外层修饰符，默认铺满父级宽度。
 * @param horizontalArrangement 横向间距和对齐方式。
 * @param verticalArrangement 纵向间距。
 * @param itemVerticalAlignment 每个 item 在所在行内的垂直对齐方式。
 * @param content 容器内的可换行内容。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun JvmSettingFlowRow(
    modifier: Modifier = Modifier.fillMaxWidth(),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
    itemVerticalAlignment: Alignment.Vertical = Alignment.Top,
    content: @Composable FlowRowScope.() -> Unit,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        itemVerticalAlignment = itemVerticalAlignment,
        content = content
    )
}

/**
 * JVM 设置类页面的通用列表外壳。
 *
 * @param modifier 传给内部列表的修饰符。
 * @param contentMaxWidth 页面主体最大宽度，用来控制桌面宽屏阅读线。
 * @param contentPadding 列表内容内边距。
 * @param verticalArrangement 列表条目之间的纵向间距。
 * @param topBar 页面顶部栏；为空时不显示。
 * @param content 页面主体内容，默认按设置页统一纵向间距排列。
 */
@Composable
internal fun JvmSettingPageScaffold(
    modifier: Modifier = Modifier,
    contentMaxWidth: Dp,
    contentPadding: PaddingValues = PaddingValues(
        horizontal = XyTheme.dimens.outerHorizontalPadding * 2,
    ),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(
        XyTheme.dimens.outerVerticalPadding * 2
    ),
    topBar: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    XyColumnScreen {
        topBar?.invoke()
        JvmLazyListComponent(
            modifier = modifier.fillMaxSize(),
            pagingItems = null,
            contentPadding = contentPadding,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = verticalArrangement,
            lazyColumnBottom = null
        ) {
            item {
                Column(
                    modifier = Modifier
                        .widthIn(max = contentMaxWidth)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding * 2),
                    content = content
                )
            }
        }
    }
}

/**
 * JVM 设置类页面的通用左右响应式布局。
 *
 * @param modifier 外层修饰符。
 * @param breakpoint 小于该宽度时改为上下堆叠。
 * @param leftWeight 宽屏时左侧权重。
 * @param rightWeight 宽屏时右侧权重。
 * @param horizontalGap 宽屏左右栏间距。
 * @param verticalGap 窄屏上下区块间距。
 * @param left 左侧内容。
 * @param right 右侧内容。
 */
@Composable
internal fun JvmSettingResponsiveRow(
    modifier: Modifier = Modifier,
    breakpoint: Dp,
    leftWeight: Float = 1f,
    rightWeight: Float = 1f,
    horizontalGap: Dp = XyTheme.dimens.outerHorizontalPadding,
    verticalGap: Dp = XyTheme.dimens.outerVerticalPadding * 2,
    left: @Composable () -> Unit,
    right: @Composable () -> Unit,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        if (maxWidth < breakpoint) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(verticalGap)
            ) {
                left()
                right()
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(horizontalGap),
                verticalAlignment = Alignment.Top
            ) {
                Box(modifier = Modifier.weight(leftWeight)) {
                    left()
                }
                Box(modifier = Modifier.weight(rightWeight)) {
                    right()
                }
            }
        }
    }
}

/**
 * JVM 设置类页面的通用头部。
 *
 * @param modifier 头部外层修饰符。
 * @param title 页面标题。
 * @param description 页面说明文案。
 * @param contentMaxWidth 可选的头部宽度上限；为空时铺满当前内容宽度。
 * @param statusContent 右侧状态摘要卡片。
 */
@Composable
internal fun JvmSettingPageHeader(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    contentMaxWidth: Dp? = null,
    statusContent: @Composable () -> Unit,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val gap = XyTheme.dimens.contentPadding * 2
        // 设置首页需要和下方主体同宽，在线音乐品质页则沿用外层内容宽度。
        val headerWidth = contentMaxWidth?.let { maxWidth -> minOf(this.maxWidth, maxWidth) }
        val flowModifier = if (headerWidth == null) {
            Modifier.fillMaxWidth()
        } else {
            Modifier
                .width(headerWidth)
                .align(Alignment.Center)
        }

        JvmSettingFlowRow(
            modifier = flowModifier,
            horizontalArrangement = Arrangement.spacedBy(gap),
            verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding * 2),
            itemVerticalAlignment = Alignment.Bottom
        ) {
            Column(
                modifier = Modifier
                    // 标题说明保留最小可读宽度，再吃掉同一行剩余空间。
                    .widthIn(min = 320.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
            }

            statusContent()
        }
    }
}

/**
 * 设置概览卡片。
 *
 * @param modifier 外层布局传入的宽度和位置修饰。
 * @param icon 概览项左上角的资源图标。
 * @param kicker 概览项的小标题，用来提示信息类型。
 * @param value 概览项的主状态文案。
 * @param sub 概览项的辅助说明。
 * @param onClick 可选点击事件；为空时卡片只展示信息。
 */
@Composable
internal fun JvmSettingOverviewTile(
    modifier: Modifier = Modifier,
    icon: DrawableResource,
    kicker: String,
    value: String,
    sub: String,
    onClick: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(XyTheme.dimens.corner)
    // 只有业务传入点击事件时才附加 clickable，避免纯展示卡片产生误导性的点击语义。
    val clickableModifier = if (onClick == null) {
        Modifier
    } else {
        Modifier.clickable(onClick = onClick)
    }

    Surface(
        modifier = modifier
            .heightIn(min = 106.dp)
            .then(clickableModifier),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(XyTheme.dimens.outerHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding)
        ) {
            JvmSettingKicker(icon = icon, text = kicker)
            Spacer(modifier = Modifier.height(XyTheme.dimens.outerVerticalPadding))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = sub,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 设置分组容器。
 *
 * @param title 分组标题。
 * @param subtitle 分组说明，用来解释该组设置的范围。
 * @param badge 分组右侧标签。
 * @param titleMinWidth 标题说明区域的最小宽度，不同页面可按排版密度调整。
 * @param contentContainerEnabled 是否为内容区包一层容器；卡片网格类内容可关闭。
 * @param contentContainerColor 分组内容区背景色，默认保留设置行的浅色容器。
 * @param contentContainerBorderColor 分组内容区边框色，透明内容区可同步传透明避免出现嵌套边框。
 * @param qualityNote 分组底部提示文案；为空或空白时不显示。
 * @param content 分组内部的设置行内容。
 */
@Composable
internal fun JvmSettingSection(
    title: String,
    subtitle: String,
    badge: String,
    titleMinWidth: Dp = 220.dp,
    contentContainerEnabled: Boolean = true,
    contentContainerColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
    contentContainerBorderColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f),
    qualityNote: String? = null,
    content: @Composable ColumnScope.() -> Unit,
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
            modifier = Modifier.padding(
                XyTheme.dimens.innerHorizontalPadding + XyTheme.dimens.outerVerticalPadding / 2
            ),
            verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding * 2)
        ) {
            JvmSettingFlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerHorizontalPadding),
                verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding),
                itemVerticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(min = titleMinWidth)
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding / 2)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }

                JvmSettingBadge(text = badge)
            }

            // 设置页普通行需要统一浅色容器，卡片网格类内容则直接使用 section 的留白。
            if (contentContainerEnabled) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(XyTheme.dimens.corner))
                        .background(contentContainerColor)
                        .border(
                            BorderStroke(1.dp, contentContainerBorderColor),
                            RoundedCornerShape(XyTheme.dimens.corner)
                        )
                ) {
                    content()
                }
            } else {
                content()
            }

            if (!qualityNote.isNullOrBlank()) {
                JvmSettingNote(text = qualityNote)
            }
        }
    }
}

/**
 * 带开关的设置行。
 *
 * @param icon 行首图标资源。
 * @param title 设置项标题。
 * @param description 设置项说明。
 * @param checked 当前开关状态。
 * @param onCheckedChange 状态变化回调。
 */
@Composable
internal fun JvmSettingSwitchRow(
    icon: DrawableResource,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    JvmSettingBaseRow(
        icon = icon,
        title = title,
        description = description,
        onClick = {
            onCheckedChange(!checked)
        },
        trailing = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    uncheckedBorderColor = Color.Transparent,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                    uncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f)
                )
            )
        }
    )
}

/**
 * 跳转型设置行。
 *
 * @param icon 行首图标资源。
 * @param title 设置项标题。
 * @param description 设置项说明。
 * @param value 行尾展示的当前值。
 * @param onClick 点击跳转回调。
 */
@Composable
internal fun JvmSettingNavigationRow(
    icon: DrawableResource,
    title: String,
    description: String,
    value: String,
    onClick: () -> Unit,
) {
    JvmSettingBaseRow(
        icon = icon,
        title = title,
        description = description,
        onClick = onClick,
        trailing = {
            JvmSettingValuePill(value = value)
            JvmSettingChevron()
        }
    )
}

/**
 * 路径展示设置行。
 *
 * @param icon 行首图标资源。
 * @param title 设置项标题。
 * @param path 当前路径内容，空值时展示占位文案。
 * @param onClick 点击复制路径的回调。
 */
@Composable
internal fun JvmSettingPathRow(
    icon: DrawableResource,
    title: String,
    path: String,
    onClick: () -> Unit,
) {
    JvmSettingBaseRow(
        icon = icon,
        title = title,
        description = path.ifBlank { "路径尚未生成" },
        descriptionStyle = JvmSettingRowDescriptionStyle.Path,
        onClick = onClick,
        trailing = {
            JvmSettingValuePill(value = "点击复制")
        }
    )
}

/**
 * 下载并发数量选择行。
 *
 * @param selected 当前选择的并发数量。
 * @param onSelected 选择新并发数量时触发。
 */
@Composable
internal fun JvmSettingDownloadRow(
    selected: Int,
    onSelected: (Int) -> Unit,
) {
    JvmSettingBaseRow(
        icon = Res.drawable.download_24px,
        title = stringResource(Res.string.download_max_list),
        description = "限制并行下载任务数量，避免占满带宽。",
        trailing = {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(XyTheme.dimens.corner))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                    .border(
                        BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                        RoundedCornerShape(XyTheme.dimens.corner)
                    )
                    .padding(XyTheme.dimens.outerVerticalPadding / 2),
                horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding / 2),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                listOf(1, 3, 5).forEach { count ->
                    JvmSettingDownloadSegment(
                        value = count,
                        selected = selected == count,
                        onClick = {
                            // 重复点击当前项不写入设置，减少无意义的数据更新。
                            if (selected != count) {
                                onSelected(count)
                            }
                        }
                    )
                }
            }
        }
    )
}

/**
 * 下载并发数量的单个分段按钮。
 *
 * @param value 分段代表的并发数量。
 * @param selected 是否为当前选中项。
 * @param onClick 点击该分段时触发。
 */
@Composable
private fun JvmSettingDownloadSegment(
    value: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(XyTheme.dimens.corner - XyTheme.dimens.outerVerticalPadding / 2)
    Box(
        modifier = Modifier
            .height(34.dp)
            .width(48.dp)
            .clip(shape)
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.26f)
                } else {
                    Color.Transparent
                }
            )
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

/**
 * 通用设置入口卡片网格。
 *
 * @param onInterfaceClick 界面设置入口点击事件。
 * @param onLanguageClick 语言设置入口点击事件。
 * @param onCustomApiClick 自定义资源入口点击事件。
 * @param onAboutClick 关于页面入口点击事件。
 */
@Composable
internal fun JvmSettingActionGrid(
    onInterfaceClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onCustomApiClick: () -> Unit,
    onAboutClick: () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val gap = XyTheme.dimens.contentPadding
        val actionEntries = jvmSettingActionEntries(
            onInterfaceClick = onInterfaceClick,
            onLanguageClick = onLanguageClick,
            onCustomApiClick = onCustomApiClick,
            onAboutClick = onAboutClick,
        )
        // 两列布局至少满足卡片自身宽度，也不能低于设置页约定的通用入口最小宽度。
        val twoColumnMinWidth = maxOf(
            JvmSettingActionGridMinWidth,
            JvmSettingActionCardCompactWidth * 2f + gap
        )
        // 根据紧凑卡片宽度判断能放几列，避免卡片被强制拉伸。
        val columnCount = if (maxWidth >= twoColumnMinWidth) {
            // 通用入口固定最多两列，四个入口在宽度足够时保持 2x2 排布。
            2
        } else {
            // 极窄窗口保留单列，优先保证文字和点击区域完整。
            1
        }
        val cardWidth = if (columnCount == 2) {
            // 双列时随可用宽度收缩，并用上限避免右栏加宽后卡片过宽。
            minOf((maxWidth - gap) / 2f, JvmSettingActionCardMaxWidth)
        } else {
            maxWidth
        }

        JvmSettingFlowRow(
            modifier = Modifier.fillMaxWidth(),
            // 卡片宽度确定后居中排列，避免最后一行靠左显得松散。
            horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(gap),
        ) {
            actionEntries.forEach { actionEntry ->
                JvmSettingActionCard(
                    modifier = Modifier.width(cardWidth),
                    actionEntry = actionEntry
                )
            }
        }
    }
}

/**
 * 组装通用入口的四个卡片配置，避免每个卡片在网格里重复声明文案和点击逻辑。
 */
@Composable
private fun jvmSettingActionEntries(
    onInterfaceClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onCustomApiClick: () -> Unit,
    onAboutClick: () -> Unit,
): List<JvmSettingActionEntry> {
    return listOf(
        JvmSettingActionEntry(
            icon = Res.drawable.settings_24px,
            kicker = "显示",
            title = stringResource(Res.string.interface_settings),
            description = "主题、背景图片与桌面显示偏好。",
            onClick = onInterfaceClick,
        ),
        JvmSettingActionEntry(
            icon = Res.drawable.info_24px,
            kicker = "本地化",
            title = stringResource(Res.string.language),
            description = "切换跟随系统或固定语言。",
            onClick = onLanguageClick,
        ),
        JvmSettingActionEntry(
            icon = Res.drawable.music_note_24px,
            kicker = "资源",
            title = stringResource(Res.string.customize_lyric_settings),
            description = "自定义歌词与封面服务地址。",
            onClick = onCustomApiClick,
        ),
        JvmSettingActionEntry(
            icon = Res.drawable.info_24px,
            kicker = "应用",
            title = stringResource(Res.string.about),
            description = "版本信息、检查更新与项目说明。",
            onClick = onAboutClick,
        ),
    )
}

/**
 * 设置行基础布局。
 *
 * @param icon 行首图标资源。
 * @param title 设置项标题。
 * @param description 设置项说明。
 * @param descriptionStyle 说明文本样式，路径类说明允许显示两行。
 * @param onClick 可选整行点击事件。
 * @param trailing 行尾控件内容。
 */
@Composable
private fun JvmSettingBaseRow(
    icon: DrawableResource,
    title: String,
    description: String,
    descriptionStyle: JvmSettingRowDescriptionStyle = JvmSettingRowDescriptionStyle.Normal,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit,
) {
    // 可点击行才附加 clickable，纯控件行保持默认语义。
    val clickableModifier = if (onClick == null) {
        Modifier
    } else {
        Modifier.clickable(onClick = onClick)
    }

    JvmSettingFlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = XyTheme.dimens.itemHeight)
            .then(clickableModifier)
            .padding(
                horizontal = XyTheme.dimens.outerHorizontalPadding,
                vertical = XyTheme.dimens.contentPadding
            ),
        horizontalArrangement = Arrangement.spacedBy(
            space = XyTheme.dimens.contentPadding,
            alignment = Alignment.Start
        ),
        verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding),
        itemVerticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .widthIn(min = 220.dp)
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            JvmSettingIcon(icon = icon)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding / 2)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = description,
                    style = if (descriptionStyle == JvmSettingRowDescriptionStyle.Path) {
                        MaterialTheme.typography.labelSmall
                    } else {
                        MaterialTheme.typography.bodySmall
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (descriptionStyle == JvmSettingRowDescriptionStyle.Path) 2 else 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            trailing()
        }
    }
}

/**
 * 通用设置入口卡片。
 *
 * @param modifier 外层网格传入的宽度修饰。
 * @param actionEntry 单个入口卡片的展示文案、图标和点击事件。
 */
@Composable
private fun JvmSettingActionCard(
    modifier: Modifier = Modifier,
    actionEntry: JvmSettingActionEntry,
) {
    val shape = RoundedCornerShape(XyTheme.dimens.corner)
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    // 复用专辑卡片的 hover 上移动效，只做视觉偏移，不改变 FlowRow 的布局尺寸。
    val liftOffset by animateDpAsState(
        targetValue = if (hovered) JvmSettingActionCardLiftOffset else 0.dp,
        animationSpec = tween(durationMillis = 160),
        label = "setting_action_card_lift_offset",
    )

    Box(
        modifier = modifier
            .heightIn(
                min = JvmSettingActionCardHeight,
                max = JvmSettingActionCardHeight
            )
            .jvmHoverDebounceClickable(
                interactionSource = interactionSource,
                onClick = actionEntry.onClick
            )
    ) {
        Column(
            modifier = Modifier
                .offset(y = liftOffset)
                .fillMaxSize()
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                .border(
                    BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)),
                    shape
                )
                .padding(XyTheme.dimens.outerHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding)
        ) {
            JvmSettingKicker(icon = actionEntry.icon, text = actionEntry.kicker)
            Spacer(modifier = Modifier.height(XyTheme.dimens.outerVerticalPadding))
            Text(
                text = actionEntry.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = actionEntry.description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 17.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 设置页提示块。
 *
 * @param text 提示内容。
 */
@Composable
internal fun JvmSettingNote(text: String) {
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
            modifier = Modifier.padding(
                horizontal = XyTheme.dimens.outerHorizontalPadding,
                vertical = XyTheme.dimens.contentPadding
            ),
            horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "i",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                modifier = Modifier.weight(1f),
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        }
    }
}

/**
 * 设置卡片中的小标题。
 *
 * @param icon 小标题图标。
 * @param text 小标题文本。
 */
@Composable
private fun JvmSettingKicker(icon: DrawableResource, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        JvmSettingIcon(icon = icon, size = 24.dp, selected = true)
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * 设置页统一图标容器。
 *
 * @param icon 图标资源。
 * @param size 外层容器尺寸。
 * @param selected 是否使用主色强调样式。
 */
@Composable
private fun JvmSettingIcon(
    icon: DrawableResource,
    size: Dp = JvmSettingIconSize,
    selected: Boolean = false,
) {
    Box(
        modifier = Modifier
            .size(size)
            .background(
                color = if (selected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)
                },
                shape = RoundedCornerShape(XyTheme.dimens.corner - XyTheme.dimens.outerVerticalPadding / 2)
            )
            .border(
                BorderStroke(
                    width = 1.dp,
                    color = if (selected) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.26f)
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.09f)
                    }
                ),
                shape = RoundedCornerShape(XyTheme.dimens.corner - XyTheme.dimens.outerVerticalPadding / 2)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(size * 0.58f),
            tint = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

/**
 * 设置分组右侧的小标签。
 *
 * @param text 标签文本。
 */
@Composable
private fun JvmSettingBadge(text: String) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
        contentColor = MaterialTheme.colorScheme.primary,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
        )
    ) {
        Text(
            modifier = Modifier.padding(
                horizontal = XyTheme.dimens.contentPadding,
                vertical = XyTheme.dimens.outerVerticalPadding
            ),
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
        )
    }
}

/**
 * 设置行右侧的值标签。
 *
 * @param value 当前值文案。
 */
@Composable
private fun JvmSettingValuePill(value: String) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
        )
    ) {
        Text(
            modifier = Modifier
                .widthIn(max = 180.dp)
                .padding(
                    horizontal = XyTheme.dimens.contentPadding,
                    vertical = XyTheme.dimens.outerVerticalPadding / 2
                ),
            text = value,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * 设置行右侧的进入箭头。
 */
@Composable
private fun JvmSettingChevron() {
    Box(
        modifier = Modifier
            .size(30.dp)
            .background(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                shape = RoundedCornerShape(XyTheme.dimens.corner - XyTheme.dimens.outerVerticalPadding / 2)
            )
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                RoundedCornerShape(XyTheme.dimens.corner - XyTheme.dimens.outerVerticalPadding / 2)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(Res.drawable.chevron_right_24px),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 设置行说明文本的样式类型。
 */
private enum class JvmSettingRowDescriptionStyle {
    /** 普通说明文本，单行展示。 */
    Normal,

    /** 路径说明文本，允许两行展示以保留更多路径信息。 */
    Path,
}
