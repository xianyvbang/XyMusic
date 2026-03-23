package cn.xybbz.common.utils

import cn.xybbz.ui.popup.XyPopTipHandle
import cn.xybbz.ui.popup.XyPopTipManager
import cn.xybbz.ui.popup.XyPopTipStyle
import org.jetbrains.compose.resources.StringResource


object MessageUtils {

    fun sendPopTip(value: String, delay: Long = 1500) {
        XyPopTipManager.show(value, durationMillis = delay)
    }

    fun sendPopTip(value: StringResource, delay: Long = 1500) {
        XyPopTipManager.show(value, durationMillis = delay)
    }

    fun sendPopTipError(value: String, delay: Long = 1500) {
        XyPopTipManager.show(
            text = value,
            style = XyPopTipStyle.Error,
            durationMillis = delay
        )
    }

    fun sendPopTipError(resId: StringResource, delay: Long = 1500) {
        XyPopTipManager.show(
            textRes = resId,
            style = XyPopTipStyle.Error,
            durationMillis = delay
        )
    }

    fun sendPopTipSuccess(resId: StringResource, delay: Long = 1500) {
        XyPopTipManager.show(
            textRes = resId,
            style = XyPopTipStyle.Success,
            durationMillis = delay
        )
    }

    fun sendPopTipSuccess(message: String, delay: Long = 1500) {
        XyPopTipManager.show(
            text = message,
            style = XyPopTipStyle.Success,
            durationMillis = delay
        )
    }

    fun sendPopTipHint(resId: StringResource, delay: Long = 1500): XyPopTipHandle {
        return XyPopTipManager.show(
            textRes = resId,
            style = XyPopTipStyle.Hint,
            durationMillis = delay
        )
    }

    fun sendDismiss() {
        XyPopTipManager.dismissCurrent()
    }

}
