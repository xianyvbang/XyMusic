package cn.xybbz.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import cn.xybbz.ui.popup.MenuItemDefaultData
import cn.xybbz.ui.popup.XyDropdownMenu
import kotlinx.coroutines.launch

private val jvmRightClickDropdownMenuItemContentPadding = PaddingValues(
    horizontal = 10.dp,
    vertical = 0.dp,
)

private val jvmRightClickDropdownMenuItemHeight = 32.dp

val jvmRightClickDropdownMenuObjectList = mutableStateListOf<JvmRightClickDropdownMenuObject>()

/**
 * JVM 端右键菜单对象。
 */
@Immutable
data class JvmRightClickDropdownMenuObject(
    val offset: DpOffset = DpOffset(0.dp, 0.dp),
    val modifier: Modifier = Modifier,
    val containerColor: Color = Color.Transparent,
    val dismissOnItemClick: Boolean = true,
    val itemHeight: Dp? = jvmRightClickDropdownMenuItemHeight,
    val onCloseRequest: (() -> Unit)? = null,
    val itemDataList: List<MenuItemDefaultData>,
)

/**
 * JVM 端全局右键菜单组件。
 */
@Composable
fun JvmRightClickDropdownMenuComponent(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        jvmRightClickDropdownMenuObjectList.forEach { menuObject ->
            Box(
                modifier = Modifier
                    .offset(x = menuObject.offset.x, y = menuObject.offset.y)
                    .size(0.dp)
            ) {
                XyDropdownMenu(
                    modifier = menuObject.modifier,
                    onIfShowMenu = { true },
                    onSetIfShowMenu = { if (!it) menuObject.close() },
                    containerColor = menuObject.containerColor,
                    itemDataList = menuObject.itemDataList.map { item ->
                        if (menuObject.dismissOnItemClick) {
                            item.copy(
                                onClick = {
                                    item.onClick()
                                    menuObject.dismiss()
                                }
                            )
                        } else {
                            item
                        }
                    },
                    contentPadding = jvmRightClickDropdownMenuItemContentPadding,
                    itemHeight = menuObject.itemHeight,
                    ifShowHorizontalDivider = false,
                )
            }
        }
    }
}

/**
 * 带右键菜单能力的 JVM 端容器。
 */
@Composable
fun JvmRightClickDropdownMenuBox(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    menuModifier: Modifier = Modifier,
    containerColor: Color = Color.Transparent,
    dismissOnItemClick: Boolean = true,
    itemHeight: Dp? = jvmRightClickDropdownMenuItemHeight,
    onShowRequest: (() -> Unit)? = null,
    onCloseRequest: (() -> Unit)? = null,
    itemDataList: () -> List<MenuItemDefaultData>,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier.jvmRightClickDropdownMenu(
            enabled = enabled,
            menuModifier = menuModifier,
            containerColor = containerColor,
            dismissOnItemClick = dismissOnItemClick,
            itemHeight = itemHeight,
            onShowRequest = onShowRequest,
            onCloseRequest = onCloseRequest,
            itemDataList = itemDataList,
        ),
        content = content,
    )
}

/**
 * 给任意 JVM Compose 节点挂载右键菜单。
 */
fun Modifier.jvmRightClickDropdownMenu(
    enabled: Boolean = true,
    menuModifier: Modifier = Modifier,
    containerColor: Color = Color.Transparent,
    dismissOnItemClick: Boolean = true,
    itemHeight: Dp? = jvmRightClickDropdownMenuItemHeight,
    onShowRequest: (() -> Unit)? = null,
    onCloseRequest: (() -> Unit)? = null,
    itemDataList: () -> List<MenuItemDefaultData>,
): Modifier = composed {
    val density = LocalDensity.current
    var layoutCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val currentEnabled by rememberUpdatedState(enabled)
    val currentMenuModifier by rememberUpdatedState(menuModifier)
    val currentContainerColor by rememberUpdatedState(containerColor)
    val currentDismissOnItemClick by rememberUpdatedState(dismissOnItemClick)
    val currentItemHeight by rememberUpdatedState(itemHeight)
    val currentOnShowRequest by rememberUpdatedState(onShowRequest)
    val currentOnCloseRequest by rememberUpdatedState(onCloseRequest)
    val currentItemDataList by rememberUpdatedState(itemDataList)

    onGloballyPositioned { coordinates ->
        layoutCoordinates = coordinates
    }.pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent()
                val change = event.changes.firstOrNull() ?: continue
                if (
                    currentEnabled &&
                    event.type == PointerEventType.Press &&
                    event.buttons.isSecondaryPressed
                ) {
                    val coordinates = layoutCoordinates ?: continue
                    val rootPosition = coordinates.positionInRoot()
                    val clickPosition = rootPosition + change.position
                    val menuOffset = with(density) {
                        DpOffset(clickPosition.x.toDp(), clickPosition.y.toDp())
                    }
                    currentOnShowRequest?.invoke()
                    JvmRightClickDropdownMenuObject(
                        offset = menuOffset,
                        modifier = currentMenuModifier,
                        containerColor = currentContainerColor,
                        dismissOnItemClick = currentDismissOnItemClick,
                        itemHeight = currentItemHeight,
                        onCloseRequest = currentOnCloseRequest,
                        itemDataList = currentItemDataList(),
                    ).show()
                }
            }
        }
    }
}

/**
 * 关闭右键菜单，不触发关闭回调。
 */
fun JvmRightClickDropdownMenuObject.dismiss() = apply {
    mainMoeScope.launch {
        jvmRightClickDropdownMenuObjectList.remove(this@dismiss)
    }
}

/**
 * 关闭右键菜单，并触发关闭回调。
 */
fun JvmRightClickDropdownMenuObject.close() = apply {
    onCloseRequest?.invoke()
    dismiss()
}

/**
 * 显示右键菜单。
 */
fun JvmRightClickDropdownMenuObject.show() = apply {
    mainMoeScope.launch {
        jvmRightClickDropdownMenuObjectList.forEach { it.onCloseRequest?.invoke() }
        jvmRightClickDropdownMenuObjectList.clear()
        jvmRightClickDropdownMenuObjectList.add(this@show)
    }
}

/**
 * 关闭所有右键菜单，不触发关闭回调。
 */
fun dismissJvmRightClickDropdownMenus() {
    mainMoeScope.launch {
        jvmRightClickDropdownMenuObjectList.clear()
    }
}

/**
 * 关闭所有右键菜单，并触发关闭回调。
 */
fun closeJvmRightClickDropdownMenus() {
    mainMoeScope.launch {
        jvmRightClickDropdownMenuObjectList.forEach { it.onCloseRequest?.invoke() }
        jvmRightClickDropdownMenuObjectList.clear()
    }
}
