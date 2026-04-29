package cn.xybbz.ui.popup

import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.MenuItemColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
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
    properties: PopupProperties = PopupProperties(focusable = true),
) {
    DropdownMenu(
        offset = offset,
        expanded = onIfShowMenu(),
        onDismissRequest = { onSetIfShowMenu(false) },
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        modifier = modifier.background(
            MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        containerColor = containerColor,
        properties = properties,
    ) {
        val visibleItems = itemDataList.filter { it.ifItemShow() }
        var expandedSubMenuIndex by remember(visibleItems) { mutableStateOf<Int?>(null) }

        visibleItems.forEachIndexed { index, data ->
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

            Box {
                XyDropdownMenuItem(
                    text = data.title,
                    enabled = data.enabled,
                    leadingIcon = data.leadingIcon,
                    trailingIcon = data.trailingIcon,
                    onClick = {
                        if (hasSubMenu) {
                            expandedSubMenuIndex = index
                        } else {
                            data.onClick()
                        }
                    },
                    modifier = Modifier.hoverable(interactionSource),
                    colors = data.colors(),
                    backgroundColor = if (subMenuExpanded) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        data.backgroundColor()
                    },
                    contentPadding = contentPadding,
                    itemHeight = itemHeight,
                    interactionSource = interactionSource,
                )
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
                        properties = PopupProperties(focusable = false),
                    )
                }
            }
        }
    }
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
)
