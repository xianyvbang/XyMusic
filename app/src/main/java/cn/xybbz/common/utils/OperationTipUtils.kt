package cn.xybbz.common.utils

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import cn.xybbz.R
import cn.xybbz.common.constants.Constants
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
            MessageUtils.sendPopTipError(errorMessage)
        }
        return operationStatus
    }

    suspend fun operationTipNotToBlockAndThrow(
        @StringRes loadingMessage: Int = R.string.deleting,
        @StringRes successMessage: Int = R.string.delete_success,
        @StringRes failMessage: Int = R.string.delete_success,
        @StringRes errorMessage: Int = R.string.delete_failed,
        operation: suspend () -> Boolean
    ): Boolean {
        MessageUtils.sendPopTip(loadingMessage)
        var isError = false
        val operationStatus = try {
            operation()
        }catch (e: Exception){
            Log.e(Constants.LOG_ERROR_PREFIX,"操作异常",e)
            isError = true
            false
        }
        if (isError){
            MessageUtils.sendPopTipError(errorMessage)
        }else {
            if (operationStatus) {
                MessageUtils.sendPopTipSuccess(successMessage)
            } else {
                MessageUtils.sendPopTipSuccess(failMessage)
            }
        }

        return operationStatus
    }

    suspend fun operationTipProgress(
        loadingMessage: Int = 0,
        operation: suspend (LoadingObject) -> Unit
    ) {
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
    }


}