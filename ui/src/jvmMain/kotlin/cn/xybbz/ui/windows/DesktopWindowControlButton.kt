package cn.xybbz.ui.windows

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import cn.xybbz.ui.ext.jvmHoverDebounceClickable
import cn.xybbz.ui.theme.XyTheme

@Composable
internal fun DesktopWindowControlButton(
    controlType: WindowControlType,
    icon: WindowControlIcon,
    onClick: () -> Unit,
    onBoundsChanged: (WindowControlType, Rect) -> Unit,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier,
) {
    // 交互状态源，用于监听鼠标悬停并切换按钮底色
    val interactionSource = remember { MutableInteractionSource() }
    // 当前是否处于悬停状态，与刷新登录、主题切换按钮保持一致
    val hovered by interactionSource.collectIsHoveredAsState()
    // 悬停底色，取与标题栏其他图标按钮一致的浅色容器色
    val hoverBackgroundColor = MaterialTheme.colorScheme.surfaceContainerLowest

    Box(
        modifier = modifier
            .size(
                width = DesktopWindowTitleBarDefaults.WindowControlButtonWidth,
                height = DesktopWindowTitleBarDefaults.WindowControlButtonHeight
            )
            .onGloballyPositioned { coordinates ->
                onBoundsChanged(controlType, coordinates.boundsInWindow())
            }
            .clip(RoundedCornerShape(XyTheme.dimens.corner))
            .background(if (hovered) hoverBackgroundColor else Color.Transparent)
            .jvmHoverDebounceClickable(
                interactionSource = interactionSource,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center
    ) {
        icon(
            contentColor,
            Modifier.size(DesktopWindowTitleBarDefaults.WindowControlIconSize)
        )
    }
}
