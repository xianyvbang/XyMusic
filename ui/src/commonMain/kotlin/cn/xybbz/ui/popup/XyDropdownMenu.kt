package cn.xybbz.ui.popup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.MenuItemColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
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
) {
    DropdownMenu(
        offset = offset,
        expanded = onIfShowMenu(),
        onDismissRequest = { onSetIfShowMenu(false) },
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        modifier = modifier.background(
            MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        containerColor = containerColor
    ) {
        itemDataList.filter { it.ifItemShow() }.forEachIndexed { _, data ->
            XyDropdownMenuItem(
                text = data.title,
                enabled = data.enabled,
                leadingIcon = data.leadingIcon,
                trailingIcon = data.trailingIcon,
                onClick = data.onClick,
                colors = data.colors(),
                contentPadding = contentPadding,
                itemHeight = itemHeight,
            )
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
    val ifItemShow: () -> Boolean = { true },
    val colors: @Composable () -> MenuItemColors = { MenuDefaults.itemColors() }
)
