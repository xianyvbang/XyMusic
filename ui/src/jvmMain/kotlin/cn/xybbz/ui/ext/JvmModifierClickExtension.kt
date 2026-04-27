package cn.xybbz.ui.ext

import androidx.compose.foundation.Indication
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role

/**
 * JVM 桌面端文字/控件点击交互。
 *
 * 聚合 hover 状态、小手光标和防抖点击；传入同一个 interactionSource
 * 可以让调用方继续监听 hover 状态来切换颜色等视觉效果。
 */
fun Modifier.jvmHoverDebounceClickable(
    interactionSource: MutableInteractionSource ,
    debounceInterval: Long = 500L,
    indication: Indication? = null,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: (() -> Unit)? = null,
): Modifier {
    val hoverModifier = this.hoverable(interactionSource, enabled)
        .pointerHoverIcon(PointerIcon.Hand)

    return if (onClick == null) {
        hoverModifier
    } else {
        hoverModifier.debounceClickable(
            debounceInterval = debounceInterval,
            interactionSource = interactionSource,
            indication = indication,
            enabled = enabled,
            onClickLabel = onClickLabel,
            role = role,
            onClick = onClick,
        )
    }
}
