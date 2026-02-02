package cn.xybbz.ui.popup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.MenuItemColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
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
    itemDataList: List<MenuItemDefaultData>
) {
    DropdownMenu(
        offset = offset,
        expanded = onIfShowMenu(),
        onDismissRequest = { onSetIfShowMenu(false) },
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        modifier = modifier.background(
            Brush.horizontalGradient(
                colors = listOf(Color(0xFF5A524C), Color(0xFF726B66)),
                tileMode = TileMode.Repeated
            )
        ),
        containerColor = containerColor
    ) {
        itemDataList.filter { it.ifItemShow() }.forEachIndexed { index, data ->
            if (index != 0)
                HorizontalDivider(modifier = Modifier.fillMaxWidth())
            XyDropdownMenuItem(
                text = data.title,
                leadingIcon = data.leadingIcon,
                trailingIcon = data.trailingIcon,
                onClick = data.onClick,
                colors = data.colors()
            )
        }
    }
}

@Immutable
data class MenuItemDefaultData(
    val title: String,
    val leadingIcon: @Composable (() -> Unit)? = null,
    val trailingIcon: @Composable (() -> Unit)? = null,
    val onClick: () -> Unit,
    val ifItemShow: () -> Boolean = { true },
    val colors: @Composable () -> MenuItemColors = { MenuDefaults.itemColors() }
)