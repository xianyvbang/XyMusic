package cn.xybbz.common.utils

import androidx.annotation.StringRes
import androidx.core.graphics.toColorInt
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import org.jetbrains.compose.resources.StringResource


object MessageUtils {

    fun sendPopTip(value: String, delay: Long = 1500) {
        PopTip.show(value).autoDismiss(delay).bringToFront()
    }

    fun sendPopTip(value: StringResource, delay: Long = 1500) {
        PopTip.show(PopTip.getApplicationContext().getString(value))
            .autoDismiss(delay).bringToFront()
    }

    fun sendPopTipError(value: String, delay: Long = 1500) {
        PopTip
            .show(value)
            .iconError()
            .autoDismiss(delay)
            .bringToFront()
    }

    fun sendPopTipError(resId: StringResource, delay: Long = 1500) {
        PopTip
            .show(PopTip.getApplicationContext().getString(resId))
            .iconError()
            .autoDismiss(delay)
            .bringToFront()
    }

    fun sendPopTipSuccess(resId: StringResource, delay: Long = 1500) {
        PopTip.show(PopTip.getApplicationContext().getString(resId))
            .iconSuccess()
            .autoDismiss(delay)
            .bringToFront()
    }

    fun sendPopTipSuccess(message: String, delay: Long = 1500) {
        PopTip.show(message)
            .iconSuccess()
            .autoDismiss(delay)
            .bringToFront()
    }

    fun sendPopTipHint(resId: StringResource, delay: Long = 1500):PopTip {
       return PopTip.show(PopTip.getApplicationContext().getString(resId))
            .autoDismiss(delay)
            .setBackgroundColor("#E6A23C".toColorInt()).bringToFront()
    }

    fun sendDismiss() {
        WaitDialog.dismiss()
        TipDialog.dismiss()
    }

}