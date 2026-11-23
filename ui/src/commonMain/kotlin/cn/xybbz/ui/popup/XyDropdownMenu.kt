package cn.xybbz.ui.popup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.MenuItemColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import cn.xybbz.ui.theme.XyTheme

@Composable
fun XyDropdownMenu(
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    onIfShowMenu: () -> Boolean,
    onSetIfShowMenu: (Boolean) -> Unit,
    containerColor: Color = Color.Transparent,
    itemDataList: List<MenuItemDefaultData>,
    contentPadding: PaddingValues = MenuDefaults.DropdownMenuItemContentPadding,
    itemHeight: Dp? = null,
    // 桌面端信号台样式：描边、悬停导轨与读数条；默认关闭，移动端视觉不受影响
    signalStyle: Boolean = false,
    // 菜单头部插槽，用于展示曲目轨道等信息；为空时不占位
    header: (@Composable () -> Unit)? = null,
    properties: PopupProperties = PopupProperties(focusable = true),
) {
    val shape = RoundedCornerShape(XyTheme.dimens.corner)
    DropdownMenu(
        offset = offset,
        expanded = onIfShowMenu(),
        onDismissRequest = { onSetIfShowMenu(false) },
        shape = shape,
        modifier = modifier
            .then(
                // 桌面端补一圈 1dp 细描边，深色背景下菜单与页面内容才能分层
                if (signalStyle) {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
                        shape = shape,
                    )
                } else {
                    Modifier
                },
            )
            .background(MaterialTheme.colorScheme.surfaceContainerLowest),
        containerColor = containerColor,
        properties = properties,
    ) {
        val visibleItems = itemDataList.filter { it.ifItemShow() }
        var expandedSubMenuIndex by remember(visibleItems) { mutableStateOf<Int?>(null) }
        // 条目圆角取主题圆角的一半，34dp 行高下不会显得过圆
        val itemCorner = if (signalStyle) XyTheme.dimens.corner / 2 else XyTheme.dimens.corner

        // 头部插槽：曲目轨道等信息，底部用 1dp 细线与条目区分
        if (header != null) {
            Box(modifier = Modifier.padding(horizontal = 5.dp)) {
                header()
            }
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 5.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.09f),
            )
        }

        visibleItems.forEachIndexed { index, data ->
            // 只读读数条：占据一行高度，用描边块把数值从条目中独立出来
            if (data.isReadout) {
                XyMenuReadout(data = data, corner = itemCorner)
                return@forEachIndexed
            }
            val visibleSubItems = data.subItems.filter { it.ifItemShow() }
            val hasSubMenu = visibleSubItems.isNotEmpty()
            val subMenuExpanded = expandedSubMenuIndex == index
            val interactionSource = remember { MutableInteractionSource() }
            val hovered by interactionSource.collectIsHoveredAsState()

            LaunchedEffect(hovered, hasSubMenu, index) {
                if (hovered) {
                    expandedSubMenuIndex = if (hasSubMenu) index else null
                }
            }

            // 强调色：主色只作用于文字与图标，不改变整行底色
            val emphasisColor = when (data.emphasis) {
                MenuItemEmphasis.Primary -> MaterialTheme.colorScheme.primary
                MenuItemEmphasis.Normal -> null
            }
            val textColor = when {
                !data.enabled -> MaterialTheme.colorScheme.onSurfaceVariant
                emphasisColor != null -> emphasisColor
                else -> MaterialTheme.colorScheme.onSurface
            }
            // 桌面端按悬停状态重算图标色：悬停转主色，强调条目常驻强调色
            val iconColor = when {
                !data.enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                emphasisColor != null -> emphasisColor
                hovered -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
            val itemColors = if (signalStyle) {
                MenuDefaults.itemColors(
                    textColor = textColor,
                    leadingIconColor = iconColor,
                    trailingIconColor = if (hovered || emphasisColor != null) {
                        emphasisColor ?: MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                )
            } else {
                data.colors()
            }
            val backgroundColor = when {
                subMenuExpanded -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                signalStyle && hovered -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                else -> data.backgroundColor()
            }

            Box {
                XyDropdownMenuItem(
                    text = data.title,
                    enabled = data.enabled,
                    leadingIcon = if (data.showLeadingIcon) data.leadingIcon else null,
                    trailingIcon = data.trailingIcon,
                    trailingText = data.trailingText,
                    textColor = textColor,
                    textWeight = if (emphasisColor != null) FontWeight.SemiBold else FontWeight.Normal,
                    onClick = {
                        if (hasSubMenu) {
                            expandedSubMenuIndex = index
                        } else {
                            data.onClick()
                        }
                    },
                    modifier = Modifier.hoverable(interactionSource),
                    colors = itemColors,
                    backgroundColor = backgroundColor,
                    contentPadding = contentPadding,
                    itemHeight = itemHeight,
                    corner = itemCorner,
                    interactionSource = interactionSource,
                )
                // 悬停导轨：贴条目左缘的 2dp 主色竖条，与设置页行悬停保持一致
                if (signalStyle && hovered && data.enabled) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 5.dp)
                            .width(2.dp)
                            .height((itemHeight ?: 34.dp) - 14.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(2.dp),
                            ),
                    )
                }
                if (hasSubMenu) {
                    val subMenuOffset = DpOffset(
                        x = data.subMenuOffset.x,
                        y = data.subMenuOffset.y - (itemHeight ?: 0.dp),
                    )
                    XyDropdownMenu(
                        modifier = data.subMenuModifier,
                        offset = subMenuOffset,
                        onIfShowMenu = { subMenuExpanded },
                        onSetIfShowMenu = {
                            if (!it && expandedSubMenuIndex == index) {
                                expandedSubMenuIndex = null
                            }
                        },
                        containerColor = containerColor,
                        itemDataList = visibleSubItems,
                        contentPadding = contentPadding,
                        itemHeight = itemHeight,
                        signalStyle = signalStyle,
                        properties = PopupProperties(focusable = false),
                    )
                }
            }
        }
    }
}

/**
 * 只读读数条：用于歌词偏移这类需要常驻展示当前值的菜单。
 *
 * @param data 条目数据，title 为左侧说明，trailingText 为右侧数值
 * @param corner 圆角，与条目保持一致
 */
@Composable
private fun XyMenuReadout(
    data: MenuItemDefaultData,
    corner: Dp,
) {
    val valueColor = if (data.emphasis == MenuItemEmphasis.Primary) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 5.dp)
            .padding(bottom = 4.dp)
            .height(34.dp)
            .background(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                shape = RoundedCornerShape(corner),
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.09f),
                shape = RoundedCornerShape(corner),
            )
            .padding(horizontal = 9.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = data.title,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            letterSpacing = 1.2.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = data.trailingText.orEmpty(),
            fontFamily = FontFamily.Monospace,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            maxLines = 1,
        )
    }
}

/**
 * 菜单条目强调级别。
 *
 * Normal 为普通正文色；Primary 用于提交类动作。
 */
enum class MenuItemEmphasis {
    Normal,
    Primary,
}

@Immutable
data class MenuItemDefaultData(
    val title: String,
    val enabled: Boolean = true,
    val leadingIcon: @Composable (() -> Unit)? = null,
    val trailingIcon: @Composable (() -> Unit)? = null,
    val onClick: () -> Unit,
    val dismissOnClick: Boolean = true,
    val ifItemShow: () -> Boolean = { true },
    val subItems: List<MenuItemDefaultData> = emptyList(),
    val subMenuModifier: Modifier = Modifier,
    val subMenuOffset: DpOffset = DpOffset(180.dp, 0.dp),
    val colors: @Composable () -> MenuItemColors = { MenuDefaults.itemColors() },
    val backgroundColor: @Composable () -> Color = { Color.Transparent },
    val showLeadingIcon: Boolean = true,
    // 右侧等宽读数，如步进量「+0.5s」；为空时不占位
    val trailingText: String? = null,
    // 条目强调级别，默认普通
    val emphasis: MenuItemEmphasis = MenuItemEmphasis.Normal,
    // 只读读数条，不响应点击
    val isReadout: Boolean = false,
)
