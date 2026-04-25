package cn.xybbz.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import cn.xybbz.compositionLocal.LocalDesktopWindowFrameState
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyTextSub
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.close_window
import xymusic_kmp.composeapp.generated.resources.maximize_window
import xymusic_kmp.composeapp.generated.resources.minimize_window
import xymusic_kmp.composeapp.generated.resources.restore_window

/**
 * 桌面端右上角窗口控制按钮组。
 */
@Composable
fun DesktopWindowControlButtons(modifier: Modifier = Modifier) {
    val frameState = LocalDesktopWindowFrameState.current

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(XyTheme.dimens.corner))
            .background(Color.Transparent),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DesktopWindowControlTooltipBox(
            tooltip = stringResource(Res.string.minimize_window)
        ) {
            DesktopWindowControlButton(
                controlType = WindowControlType.Minimize,
                onClick = frameState.onMinimize,
            )
        }
        DesktopWindowControlTooltipBox(
            tooltip = stringResource(
                if (frameState.isMaximized) {
                    Res.string.restore_window
                } else {
                    Res.string.maximize_window
                }
            )
        ) {
            DesktopWindowControlButton(
                controlType = if (frameState.isMaximized) {
                    WindowControlType.Restore
                } else {
                    WindowControlType.Maximize
                },
                onClick = frameState.onToggleMaximize,
            )
        }
        DesktopWindowControlTooltipBox(
            tooltip = stringResource(Res.string.close_window)
        ) {
            DesktopWindowControlButton(
                controlType = WindowControlType.Close,
                onClick = frameState.onClose,
            )
        }
    }
}

/**
 * 单个窗口控制按钮容器，负责承载最小化、最大化和关闭图标。
 */
@Composable
private fun DesktopWindowControlButton(
    controlType: WindowControlType,
    onClick: () -> Unit,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(width = 42.dp, height = 34.dp)
            .clip(RoundedCornerShape(XyTheme.dimens.corner))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        WindowControlGlyph(
            controlType = controlType,
            tint = contentColor,
            modifier = Modifier.size(12.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DesktopWindowControlTooltipBox(
    tooltip: String,
    content: @Composable () -> Unit,
) {
    val tooltipState = rememberTooltipState(isPersistent = true)
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    LaunchedEffect(isHovered) {
        if (isHovered) {
            delay(WindowControlTooltipDelayMillis)
            tooltipState.show()
        } else {
            tooltipState.dismiss()
        }
    }

    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
        state = tooltipState,
        enableUserInput = false,
        tooltip = {
            PlainTooltip(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                contentColor = MaterialTheme.colorScheme.onBackground
            ) {
                XyTextSub(
                    text = tooltip,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        content = {
            Box(modifier = Modifier.hoverable(interactionSource = interactionSource)) {
                content()
            }
        },
    )
}

private const val WindowControlTooltipDelayMillis = 500L

/**
 * 使用 Canvas 绘制窗口控制按钮的几何图形。
 */
@Composable
private fun WindowControlGlyph(
    controlType: WindowControlType,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 1.4.dp.toPx()
        val inset = 1.5.dp.toPx()
        val left = inset
        val top = inset
        val right = size.width - inset
        val bottom = size.height - inset

        when (controlType) {
            WindowControlType.Minimize -> {
                drawLine(
                    color = tint,
                    start = Offset(left, bottom - inset),
                    end = Offset(right, bottom - inset),
                    strokeWidth = strokeWidth
                )
            }

            WindowControlType.Maximize -> {
                drawRect(
                    color = tint,
                    topLeft = Offset(left, top),
                    size = Size(right - left, bottom - top),
                    style = Stroke(width = strokeWidth)
                )
            }

            WindowControlType.Restore -> {
                val shift = 2.dp.toPx()
                drawRect(
                    color = tint,
                    topLeft = Offset(left + shift, top),
                    size = Size(right - left - shift, bottom - top - shift),
                    style = Stroke(width = strokeWidth)
                )
                drawRect(
                    color = tint,
                    topLeft = Offset(left, top + shift),
                    size = Size(right - left - shift, bottom - top - shift),
                    style = Stroke(width = strokeWidth)
                )
            }

            WindowControlType.Close -> {
                drawLine(
                    color = tint,
                    start = Offset(left, top),
                    end = Offset(right, bottom),
                    strokeWidth = strokeWidth
                )
                drawLine(
                    color = tint,
                    start = Offset(right, top),
                    end = Offset(left, bottom),
                    strokeWidth = strokeWidth
                )
            }
        }
    }
}

/**
 * 窗口控制按钮类型。
 */
private enum class WindowControlType {
    Minimize,
    Maximize,
    Restore,
    Close,
}
