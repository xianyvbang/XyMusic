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

    /**
     * 发送带格式化参数的普通资源提示。
     */
    fun sendPopTip(value: StringResource, formatArgs: List<Any>, delay: Long = 1500) {
        XyPopTipManager.show(
            textRes = value,
            durationMillis = delay,
            formatArgs = formatArgs
        )
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

    /**
     * 发送带格式化参数的错误资源提示。
     */
    fun sendPopTipError(resId: StringResource, formatArgs: List<Any>, delay: Long = 1500) {
        XyPopTipManager.show(
            textRes = resId,
            style = XyPopTipStyle.Error,
            durationMillis = delay,
            formatArgs = formatArgs
        )
    }

    fun sendPopTipSuccess(resId: StringResource, delay: Long = 1500) {
        XyPopTipManager.show(
            textRes = resId,
            style = XyPopTipStyle.Success,
            durationMillis = delay
        )
    }

    /**
     * 发送带格式化参数的成功资源提示。
     */
    fun sendPopTipSuccess(resId: StringResource, formatArgs: List<Any>, delay: Long = 1500) {
        XyPopTipManager.show(
            textRes = resId,
            style = XyPopTipStyle.Success,
            durationMillis = delay,
            formatArgs = formatArgs
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
