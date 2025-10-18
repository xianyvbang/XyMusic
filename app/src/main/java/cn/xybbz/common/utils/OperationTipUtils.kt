package cn.xybbz.common.utils

import androidx.annotation.StringRes
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import cn.xybbz.R
import cn.xybbz.ui.components.LoadingObject
import cn.xybbz.ui.components.dismiss
import cn.xybbz.ui.components.show
import kotlinx.coroutines.delay
import java.util.UUID

/**
 * 统一操作提示
 * @author 刘梦龙
 * @date 2025/07/18
 */
object OperationTipUtils {

    /**
     * 统一操作提示方法,不阻断操作
     */
    suspend fun operationTipNotToBlock(
        @StringRes loadingMessage: Int = R.string.deleting,
        @StringRes successMessage: Int = R.string.delete_success,
        @StringRes errorMessage: Int = R.string.delete_failed,
        operation: suspend () -> Boolean
    ): Boolean {
        MessageUtils.sendPopTip(loadingMessage)

        val operationStatus = operation()
        if (operationStatus) {
            MessageUtils.sendPopTipSuccess(successMessage)
        } else {
            MessageUtils.sendPopTipIconError(errorMessage)
        }
        return operationStatus
    }

    suspend fun operationTipProgress(
        loadingMessage: Int = 0,
        operation: suspend (LoadingObject) -> Unit
    ): Boolean {
        val loadingObject =
            LoadingObject(
                id = UUID.randomUUID().toString(),
                messageIsStringRes = false,
                loadingCompose = { progress ->
                    CircularProgressIndicator(
                        modifier = Modifier
                            .clip(CircleShape),
                        progress = {
                            progress ?: 0.0f
                        },
                    )
                })
        loadingObject.updateMessageProgress(loadingMessage)
        loadingObject
            .show()
        operation(loadingObject)
        delay(10)
        loadingObject.dismiss()
        return true
    }


}