package cn.xybbz.entity.ui

import androidx.compose.runtime.Stable

/**
 * SnackbarHost通知对象
 */
@Stable
data class SnackbarHostObject(
    val message: String,
    val isError: Boolean = false,
    val actionLabel: String,
    val onChange: (suspend () -> Unit)? = null,
) {
    companion object {
        fun createConnected(): SnackbarHostObject {
            return SnackbarHostObject(message = "未连接网络", actionLabel = "")
        }

        fun createNo(): SnackbarHostObject {
            return SnackbarHostObject(message = "关闭", actionLabel = "")
        }
    }
}
