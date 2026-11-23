package cn.xybbz.ui.xy

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import cn.xybbz.ui.ext.jvmHoverDebounceClickable

/**
 * JVM 桌面端可 hover/click 的 Text。
 */
@Composable
fun XyText(
    modifier: Modifier = Modifier,
    text: String,
    fontWeight: FontWeight? = FontWeight.Bold,
    maxLines: Int = 1,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    color: Color = MaterialTheme.colorScheme.onSurface,
    textAlign: TextAlign? = null,
    onClick: (() -> Unit)?,
) {
    // 悬停变色统一交给通用文字按钮状态，避免每个可点击文本重复维护交互源
    val colorState = rememberXyTextHoverColor(color = color)

    XyText(
        modifier = modifier.jvmHoverDebounceClickable(
            interactionSource = colorState.interactionSource,
            onClick = onClick,
        ),
        text = text,
        fontWeight = fontWeight,
        maxLines = maxLines,
        style = style.copy(textAlign = textAlign ?: style.textAlign),
        overflow = overflow,
        color = colorState.color,
    )
}

/**
 * JVM 桌面端可 hover/click 的次要信息 Text。
 */
@Composable
fun XyTextSub(
    modifier: Modifier = Modifier,
    text: String,
    style: TextStyle = MaterialTheme.typography.bodySmall,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onClick: (() -> Unit)?,
) {
    // 悬停变色统一交给通用文字按钮状态，避免每个可点击文本重复维护交互源
    val colorState = rememberXyTextHoverColor(color = color)

    XyTextSub(
        modifier = modifier.jvmHoverDebounceClickable(
            interactionSource = colorState.interactionSource,
            onClick = onClick,
        ),
        text = text,
        style = style,
        overflow = overflow,
        textAlign = textAlign,
        maxLines = maxLines,
        color = colorState.color,
    )
}
