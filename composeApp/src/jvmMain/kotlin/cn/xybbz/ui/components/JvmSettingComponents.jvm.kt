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
import cn.xybbz.ui.xy.XyText
import cn.xybbz.ui.xy.XyTextLarge
import cn.xybbz.ui.xy.XyTextSub
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import xymusic.composeapp.generated.resources.*
import xymusic.composeapp.generated.resources.Res
import xymusic.composeapp.generated.resources.chevron_right_24px
import xymusic.composeapp.generated.resources.download_24px
import xymusic.composeapp.generated.resources.download_max_list

/** JVM 设置页头部摘要卡统一宽度，按连接、代理和媒体库等信息密度更高的页面取值。 */
internal val JvmSettingSummaryCardWidth = 284.dp

/** JVM 设置页概览区切换为三列卡片的宽度断点。 */
internal val JvmSettingOverviewThreeColumnWidth = 760.dp

/** JVM 设置页小型概览卡的最小宽度。 */
internal val JvmSettingOverviewTileMinWidth = 140.dp

/** JVM 设置页连接列表卡片双列布局的单卡最小宽度。 */
internal val JvmSettingConnectionCardMinWidth = 240.dp

/** JVM 设置页头像统一尺寸，连接和媒体库等头像类入口共用。 */
internal val JvmSettingAvatarSize = 56.dp

/** JVM 设置页连接表单卡片的最小高度。 */
internal val JvmSettingConnectionFormCardMinHeight = 118.dp

/** JVM 设置页内容卡片切换为双列的统一宽度断点。 */
internal val JvmSettingContentGridTwoColumnBreakpoint = 640.dp

/** JVM 设置页背景预览叠加层在窄宽度下隐藏侧栏的断点。 */
internal val JvmSettingBackgroundPreviewCompactBreakpoint = 560.dp

/** JVM 设置页背景预览中模拟桌面播放器的底栏高度。 */
internal val JvmSettingBackgroundPreviewPlayerHeight = 58.dp

/** JVM 设置页背景预览中模拟桌面顶栏的高度。 */
internal val JvmSettingBackgroundPreviewTopBarHeight = 44.dp

/** JVM 设置页背景预览中模拟桌面侧栏的宽度。 */
internal val JvmSettingBackgroundPreviewSideWidth = 124.dp

/** JVM 设置页媒体库列表单行最小高度。 */
internal val JvmSettingLibraryRowMinHeight = 76.dp

/** JVM 设置页界面预览卡片的最小高度。 */
internal val JvmSettingInterfaceMiniPreviewHeight = 230.dp

/** JVM 设置类页面从单列切换为左右两栏的最小宽度。 */
private val JvmSettingTwoPaneBreakpoint = 860.dp

/** JVM 设置类页面宽屏时左侧主栏权重，以设置页主体布局为基准。 */
private const val JvmSettingTwoPaneLeftWeight = 1.30f

/** JVM 设置类页面宽屏时右侧侧栏权重，以设置页主体布局为基准。 */
private const val JvmSettingTwoPaneRightWeight = 1.10f

/** 通用设置入口区域的最小宽度，低于该宽度时退化为单列卡片。 */
private val JvmSettingActionGridMinWidth = 320.dp

/** 通用入口卡片在右侧栏双列场景下使用的最小紧凑宽度。 */
private val JvmSettingActionCardCompactWidth = 154.dp

/** 通用入口卡片在双列场景下允许的最大宽度，避免右栏加宽后卡片被拉得过宽。 */
private val JvmSettingActionCardMaxWidth = 196.dp

/** 通用入口卡片统一高度，保证设置、档位和分段选择卡片等高排列。 */
private val JvmSettingActionCardHeight = 160.dp

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
 * @param enabled 是否允许点击和 hover 上移动效，禁用时保持占位尺寸不变。
 * @param color 卡片强调色；为空时使用主题主色，设置页默认保持统一主色样式。
 * @param selected 是否展示选中态背景和边框。
 * @param status 可选底部状态文本，适合入口卡片承载当前状态。
 * @param role 可选语义角色，单选类入口可传 [Role.RadioButton]。
 * @param onClick 卡片点击事件。
 */
internal data class JvmSettingActionEntry(
    val icon: DrawableResource,
    val kicker: String,
    val title: String,
    val description: String,
    val enabled: Boolean = true,
    val color: Color? = null,
    val selected: Boolean = false,
    val status: String? = null,
    val role: Role? = null,
    val onClick: () -> Unit,
)

/**
 * 通用设置入口卡片网格的排列方式。
 */
internal enum class JvmSettingActionGridArrangement {
    /** 默认设置页网格，宽度足够时最多两列。 */
    Grid,

    /** 横向紧凑排列，宽度足够时尽量保持同一行。 */
    Horizontal,
}

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
 * @param contentPadding 列表内容内边距。
 * @param verticalArrangement 列表条目之间的纵向间距。
 * @param topBar 页面顶部栏；为空时不显示。
 * @param content 页面主体内容，默认按设置页统一纵向间距排列。
 */
@Composable
internal fun JvmSettingPageScaffold(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(
        horizontal = XyTheme.dimens.outerHorizontalPadding,
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
                    modifier = Modifier.fillMaxWidth(),
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
    breakpoint: Dp = JvmSettingTwoPaneBreakpoint,
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
 * JVM 设置类页面的统一双栏内容布局。
 *
 * 宽屏时使用设置页的左主栏 + 右侧栏比例；窄屏时左右内容按上下顺序铺满整行。
 *
 * @param modifier 外层修饰符。
 * @param breakpoint 小于该宽度时改为上下堆叠。
 * @param leftContent 左侧主栏内容。
 * @param rightContent 右侧侧栏内容。
 */
@Composable
internal fun JvmSettingTwoPaneContent(
    modifier: Modifier = Modifier,
    breakpoint: Dp = JvmSettingTwoPaneBreakpoint,
    leftContent: @Composable ColumnScope.() -> Unit,
    rightContent: @Composable ColumnScope.() -> Unit,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val gap = XyTheme.dimens.outerHorizontalPadding
        val useTwoColumns = maxWidth >= breakpoint
        val totalWeight = JvmSettingTwoPaneLeftWeight + JvmSettingTwoPaneRightWeight
        val contentWidth = if (useTwoColumns) {
            maxWidth - gap
        } else {
            maxWidth
        }
        val leftWidth = if (useTwoColumns) {
            contentWidth * (JvmSettingTwoPaneLeftWeight / totalWeight)
        } else {
            maxWidth
        }
        val rightWidth = if (useTwoColumns) {
            contentWidth - leftWidth
        } else {
            maxWidth
        }

        JvmSettingFlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding * 2),
            itemVerticalAlignment = Alignment.Top,
        ) {
            JvmSettingPaneStack(
                modifier = Modifier.width(leftWidth),
                content = leftContent
            )
            JvmSettingPaneStack(
                modifier = Modifier.width(rightWidth),
                content = rightContent
            )
        }
    }
}

/**
 * 统一双栏布局中的纵向分组容器。
 */
@Composable
private fun JvmSettingPaneStack(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding * 2),
        content = content
    )
}

/**
 * JVM 设置类页面的通用头部。
 *
 * @param modifier 头部外层修饰符。
 * @param title 页面标题。
 * @param description 页面说明文案。
 * @param statusContent 右侧状态摘要卡片。
 */
@Composable
internal fun JvmSettingPageHeader(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    statusContent: @Composable () -> Unit,
) {
    val gap = XyTheme.dimens.contentPadding * 2

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(gap),
        verticalAlignment = Alignment.Bottom
    ) {
        Column(
            modifier = Modifier
                // 标题说明只吃状态卡之外的剩余空间，保证头部摘要始终在同一行。
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding)
        ) {
            XyTextLarge(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            XyTextSub(
                text = description,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        statusContent()
    }
}

/**
 * JVM 设置类页面头部右侧状态摘要项。
 */
internal data class JvmSettingStatusCardItem(
    val label: String,
    val value: String,
)

/**
 * JVM 设置类页面头部右侧状态摘要卡。
 *
 * 卡片使用固定宽度，避免子项的 fillMaxWidth 在 Row 测量时把头部右侧内容撑满整行。
 */
@Composable
internal fun JvmSettingStatusCard(
    modifier: Modifier = Modifier,
    width: Dp = JvmSettingSummaryCardWidth,
    items: List<JvmSettingStatusCardItem>,
    prominentValue: Boolean = false,
) {
    Surface(
        modifier = modifier.width(width),
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
        )
    ) {
        Column(
            modifier = Modifier.padding(XyTheme.dimens.outerHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding)
        ) {
            items.forEach { item ->
                JvmSettingStatusCardRow(
                    label = item.label,
                    value = item.value,
                    prominentValue = prominentValue,
                )
            }
        }
    }
}

@Composable
private fun JvmSettingStatusCardRow(
    label: String,
    value: String,
    prominentValue: Boolean,
) {
    val valueStyle = if (prominentValue) {
        MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
    } else {
        MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
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
            text = value,
            style = valueStyle,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
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
 * @param subMaxLines 辅助说明最多展示的行数。
 * @param onClick 可选点击事件；为空时卡片只展示信息。
 */
@Composable
internal fun JvmSettingOverviewTile(
    modifier: Modifier = Modifier,
    icon: DrawableResource,
    kicker: String,
    value: String,
    sub: String,
    subMaxLines: Int = 1,
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
            XyTextLarge(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            XyTextSub(
                text = sub,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = subMaxLines,
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
 * @param headerAction 分组标题右侧追加操作，用于放置页面局部按钮。
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
    headerAction: (@Composable () -> Unit)? = null,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerHorizontalPadding),
                verticalAlignment = Alignment.Top
            ) {
                // 宽度足够时保留标题区倾向宽度，窄屏时优先保证右侧操作不换行。
                val effectiveTitleMinWidth = if (headerAction == null) {
                    titleMinWidth
                } else {
                    0.dp
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .widthIn(min = effectiveTitleMinWidth),
                    verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding / 2)
                ) {
                    XyText(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    XyTextSub(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    JvmSettingBadge(text = badge)
                    headerAction?.invoke()
                }
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
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
) {
    JvmSettingBaseRow(
        icon = icon,
        title = title,
        description = description,
        enabled = enabled,
        onClick = {
            if (enabled) {
                onCheckedChange(!checked)
            }
        },
        trailing = {
            Switch(
                checked = checked,
                enabled = enabled,
                onCheckedChange = {
                    if (enabled) {
                        onCheckedChange(it)
                    }
                },
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
        description = path.ifBlank { stringResource(Res.string.jvm_setting_components_text_01) },
        descriptionStyle = JvmSettingRowDescriptionStyle.Path,
        descriptionMaxLines = 2,
        onClick = onClick,
        trailing = {
            JvmSettingValuePill(value = stringResource(Res.string.jvm_setting_components_text_02))
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
        description = stringResource(Res.string.jvm_setting_components_text_03),
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
        XyText(
            text = value.toString(),
            style = MaterialTheme.typography.labelLarge,
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
 * @param actionEntries 需要渲染的入口卡片数据，调用方只负责提供文案、颜色和点击动作。
 * @param modifier 外层布局修饰符，用来承接不同页面的宽度约束。
 * @param fillTwoColumnWidth 双列时是否让两张卡片平分整行宽度，默认沿用紧凑入口卡宽。
 * @param arrangement 卡片排列方式，默认保持设置页的网格布局。
 * @param cardHeight 卡片固定高度，默认沿用设置入口卡高度；带底部状态的卡片会自动抬高最小高度。
 */
@Composable
internal fun JvmSettingActionGrid(
    actionEntries: List<JvmSettingActionEntry>,
    modifier: Modifier = Modifier,
    fillTwoColumnWidth: Boolean = false,
    arrangement: JvmSettingActionGridArrangement = JvmSettingActionGridArrangement.Grid,
    cardHeight: Dp = JvmSettingActionCardHeight,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val gap = XyTheme.dimens.contentPadding
        val resolvedCardHeight = cardHeight
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
            val twoColumnCardWidth = (maxWidth - gap) / 2f
            if (fillTwoColumnWidth) {
                twoColumnCardWidth
            } else {
                minOf(twoColumnCardWidth, JvmSettingActionCardMaxWidth)
            }
        } else {
            maxWidth
        }

        if (arrangement == JvmSettingActionGridArrangement.Horizontal) {
            val horizontalCardWidth = minOf(maxWidth, JvmSettingActionCardCompactWidth)
            JvmSettingFlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(gap),
                verticalArrangement = Arrangement.spacedBy(gap),
            ) {
                actionEntries.forEach { actionEntry ->
                    JvmSettingActionCard(
                        modifier = Modifier.width(horizontalCardWidth),
                        actionEntry = actionEntry,
                        cardHeight = resolvedCardHeight,
                    )
                }
            }
        } else if (columnCount == 2 && fillTwoColumnWidth) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(gap),
            ) {
                actionEntries.chunked(2).forEach { rowEntries ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(gap),
                    ) {
                        rowEntries.forEach { actionEntry ->
                            JvmSettingActionCard(
                                modifier = Modifier.weight(1f),
                                actionEntry = actionEntry,
                                cardHeight = resolvedCardHeight,
                            )
                        }
                        if (rowEntries.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        } else {
            JvmSettingFlowRow(
                modifier = Modifier.fillMaxWidth(),
                // 卡片宽度确定后居中排列，避免最后一行靠左显得松散。
                horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(gap),
            ) {
                actionEntries.forEach { actionEntry ->
                    JvmSettingActionCard(
                        modifier = Modifier.width(cardWidth),
                        actionEntry = actionEntry,
                        cardHeight = resolvedCardHeight,
                    )
                }
            }
        }
    }
}

/**
 * 设置行基础布局。
 *
 * @param icon 行首图标资源。
 * @param title 设置项标题。
 * @param description 设置项说明。
 * @param descriptionStyle 说明文本样式。
 * @param descriptionMaxLines 说明文本最大行数。
 * @param descriptionOverflow 说明文本超出时的处理方式，可按场景选择换行展示或缩略点。
 * @param enabled 是否启用点击和正常内容透明度。
 * @param minHeight 行最小高度。
 * @param horizontalPadding 行横向内边距。
 * @param verticalPadding 行纵向内边距。
 * @param iconSelected 是否使用强调色图标样式。
 * @param iconColor 图标强调色；为空时使用默认主色。
 * @param contentAlpha 内容透明度，禁用态默认降低透明度。
 * @param onClick 可选整行点击事件。
 * @param trailing 行尾控件内容。
 */
@Composable
internal fun JvmSettingBaseRow(
    icon: DrawableResource,
    title: String,
    description: String,
    descriptionStyle: JvmSettingRowDescriptionStyle = JvmSettingRowDescriptionStyle.Normal,
    descriptionMaxLines: Int = 1,
    descriptionOverflow: TextOverflow = TextOverflow.Ellipsis,
    enabled: Boolean = true,
    minHeight: Dp = XyTheme.dimens.itemHeight,
    horizontalPadding: Dp = XyTheme.dimens.outerHorizontalPadding,
    verticalPadding: Dp = XyTheme.dimens.contentPadding,
    iconSelected: Boolean = false,
    iconColor: Color? = null,
    contentAlpha: Float = if (enabled) 1f else 0.48f,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit,
) {
    // 可点击且启用的行才附加 clickable，禁用行保持占位尺寸不变。
    val clickableModifier = if (enabled && onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .then(clickableModifier)
            .padding(
                horizontal = horizontalPadding,
                vertical = verticalPadding
            ),
        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            JvmSettingIcon(
                icon = icon,
                selected = iconSelected,
                color = iconColor,
                contentAlpha = contentAlpha,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding / 2)
            ) {
                XyText(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                XyTextSub(
                    text = description,
                    style = if (descriptionStyle == JvmSettingRowDescriptionStyle.Path) {
                        MaterialTheme.typography.labelSmall
                    } else {
                        MaterialTheme.typography.bodySmall
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
                    maxLines = descriptionMaxLines,
                    overflow = descriptionOverflow
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
    cardHeight: Dp = JvmSettingActionCardHeight,
) {
    val shape = RoundedCornerShape(XyTheme.dimens.corner)
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val cardHovered = actionEntry.enabled && hovered
    val contentAlpha = if (actionEntry.enabled) 1f else 0.44f
    val colorScheme = MaterialTheme.colorScheme
    val clickableModifier = if (actionEntry.enabled) {
        Modifier.jvmHoverDebounceClickable(
            interactionSource = interactionSource,
            role = actionEntry.role,
            onClick = actionEntry.onClick
        )
    } else {
        Modifier
    }
    // 复用专辑卡片的 hover 上移动效，只做视觉偏移，不改变 FlowRow 的布局尺寸。
    val liftOffset by animateDpAsState(
        targetValue = if (cardHovered) JvmSettingActionCardLiftOffset else 0.dp,
        animationSpec = tween(durationMillis = 160),
        label = "setting_action_card_lift_offset",
    )
    val containerColor = if (actionEntry.selected) {
        colorScheme.primary.copy(alpha = if (XyTheme.configs.isDarkTheme) 0.18f else 0.10f)
    } else {
        colorScheme.surfaceContainerLowest
    }
    val borderColor = if (actionEntry.selected) {
        colorScheme.primary.copy(alpha = 0.72f)
    } else {
        colorScheme.onSurface.copy(alpha = 0.10f)
    }

    Box(
        modifier = modifier
            .heightIn(
                min = cardHeight,
                max = cardHeight
            )
            .then(clickableModifier)
    ) {
        Column(
            modifier = Modifier
                .offset(y = liftOffset)
                .fillMaxSize()
                .clip(shape)
                .background(containerColor)
                .border(BorderStroke(1.dp, borderColor), shape)
                .padding(XyTheme.dimens.outerHorizontalPadding),
        ) {
            JvmSettingKicker(
                icon = actionEntry.icon,
                text = actionEntry.kicker,
                color = actionEntry.color,
                contentAlpha = contentAlpha,
            )
            Spacer(modifier = Modifier.height(XyTheme.dimens.outerVerticalPadding))
            XyText(
                text = actionEntry.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(XyTheme.dimens.outerVerticalPadding / 2))
            XyTextSub(
                text = actionEntry.description,
                style = MaterialTheme.typography.labelSmall.copy(lineHeight = 17.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            actionEntry.status?.let { status ->
                Spacer(modifier = Modifier.weight(1f))
                XyTextSub(
                    text = status,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = colorScheme.onSurfaceVariant.copy(alpha = contentAlpha * 0.78f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
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
                XyText(
                    text = "i",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            XyTextSub(
                modifier = Modifier.weight(1f),
                text = text,
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 设置卡片中的小标题。
 *
 * @param icon 小标题图标。
 * @param text 小标题文本。
 * @param color 小标题图标强调色；为空时使用主题主色。
 * @param contentAlpha 禁用态内容透明度，避免额外包裹透明层影响布局。
 */
@Composable
private fun JvmSettingKicker(
    icon: DrawableResource,
    text: String,
    color: Color? = null,
    contentAlpha: Float = 1f,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        JvmSettingIcon(
            icon = icon,
            size = 24.dp,
            selected = true,
            color = color,
            contentAlpha = contentAlpha,
        )
        XyTextSub(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
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
 * @param color 选中态强调色；为空时使用主题主色。
 * @param contentAlpha 图标整体内容透明度，用于禁用态。
 */
@Composable
private fun JvmSettingIcon(
    icon: DrawableResource,
    size: Dp = JvmSettingIconSize,
    selected: Boolean = false,
    color: Color? = null,
    contentAlpha: Float = 1f,
) {
    // 统一计算强调色，设置页不传色值时仍然沿用 Material 主题主色。
    val accentColor = color ?: MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .size(size)
            .background(
                color = if (selected) {
                    accentColor.copy(alpha = 0.18f * contentAlpha)
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f * contentAlpha)
                },
                shape = RoundedCornerShape(XyTheme.dimens.corner - XyTheme.dimens.outerVerticalPadding / 2)
            )
            .border(
                BorderStroke(
                    width = 1.dp,
                    color = if (selected) {
                        accentColor.copy(alpha = 0.26f * contentAlpha)
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.09f * contentAlpha)
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
                accentColor.copy(alpha = contentAlpha)
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha)
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
        XyText(
            modifier = Modifier.padding(
                horizontal = XyTheme.dimens.contentPadding,
                vertical = XyTheme.dimens.outerVerticalPadding
            ),
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
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
        XyText(
            modifier = Modifier
                .widthIn(max = 180.dp)
                .padding(
                    horizontal = XyTheme.dimens.contentPadding,
                    vertical = XyTheme.dimens.outerVerticalPadding / 2
            ),
            text = value,
            style = MaterialTheme.typography.labelSmall,
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
internal enum class JvmSettingRowDescriptionStyle {
    /** 普通说明文本，单行展示。 */
    Normal,

    /** 路径说明文本，允许两行展示以保留更多路径信息。 */
    Path,
}
