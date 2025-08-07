package cn.xybbz.ui.ext

import androidx.compose.foundation.Indication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.semantics.Role

/**
 * 点击防抖
 */
inline fun Modifier.debounceClickable(
    debounceInterval: Long = 500L,
    interactionSource: MutableInteractionSource,
    indication: Indication?,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    crossinline onClick: () -> Unit,
): Modifier = then(
    Modifier.composed {
        var lastClickTime by remember { mutableLongStateOf(0L) }
        clickable(
            interactionSource, indication, enabled, onClickLabel, role
        ) {
            val currentTime = System.currentTimeMillis()
            if ((currentTime - lastClickTime) < debounceInterval) return@clickable
            lastClickTime = currentTime
            onClick()
        }
    }
)

inline fun Modifier.debounceClickable(
    debounceInterval: Long = 500L,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    crossinline onClick: () -> Unit,
): Modifier = then(Modifier.composed {
    var lastClickTime by remember { mutableLongStateOf(0L) }
    clickable(enabled, onClickLabel, role) {
        val currentTime = System.currentTimeMillis()
        if ((currentTime - lastClickTime) < debounceInterval) return@clickable
        lastClickTime = currentTime
        onClick()
    }
})


/**
 * 防止重复点击,比如用在Button时直接传入onClick函数
 */
@Composable
inline fun composeClick(
    debounceInterval: Long = 500L,
    crossinline onClick: () -> Unit,
): () -> Unit {
    var lastClickTime by remember { mutableLongStateOf(value = 0L) }//使用remember函数记录上次点击的时间
    return {
        val currentTimeMillis = System.currentTimeMillis()
        if (currentTimeMillis - debounceInterval >= lastClickTime) {//判断点击间隔,如果在间隔内则不回调
            onClick()
            lastClickTime = currentTimeMillis
        }
    }
}