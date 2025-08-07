package cn.xybbz.common.utils

import android.graphics.Color
import android.os.Handler
import android.os.Looper
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import cn.xybbz.R
import cn.xybbz.common.constants.Constants
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.kongzue.dialogx.interfaces.OnBackPressedListener


object MessageUtils {

    var popTip: PopTip? = null

    val errorColor = Color.argb(255, 171, 55, 47)
    val successColor = Color.argb(255, 65, 174, 60)


    fun sendPopTip(value: String, delay: Long = 2000): PopTip {
        popTip?.dismiss()
        val newPopTip = PopTip.show(value).setRadius(200F).autoDismiss(delay)
        popTip = newPopTip
        return newPopTip
    }

    fun sendPopTip(value: String, @ColorInt backgroundColor: Int, delay: Long = 2000): PopTip {
        popTip?.dismiss()
        val newPopTip = PopTip.show(value).setRadius(200F).autoDismiss(delay)
            .setBackgroundColor(backgroundColor)
        popTip = newPopTip
        return newPopTip
    }


    fun sendPopTipError(value: String, delay: Long = 2000): PopTip {
        popTip?.dismiss()
        return PopTip.show(value).setRadius(200F).autoDismiss(delay)
            .setBackgroundColor(errorColor)
    }

    fun sendPopTipSuccess(value: String, delay: Long = 2000): PopTip {
        popTip?.dismiss()
        return PopTip.show(value).setRadius(200F).autoDismiss(delay)
            .setBackgroundColor(successColor)
    }

    fun sendPopTipIconError(
        value: String,
        delay: Long = 2000,
        @DrawableRes icon: Int = R.drawable.icon_error
    ) {
        popTip?.dismiss()
        PopTip.show(icon, value).setRadius(200F).autoDismiss(delay)
            .setBackgroundColor(errorColor)
    }

    /*fun PopTip.sendPopTipIconSuccess(
        value: String,
        delay: Long = 2000,
        @DrawableRes icon: Int? = null
    ) {
        this.dismiss()
        PopTip.show(icon, value).setRadius(200F).autoDismiss(delay)
            .setBackgroundColor(errorColor)
    }*/


    /*fun sendPopTipIcon(
        value: String,
        @ColorInt backgroundColor: Int,
        delay: Long = 2000,
        @DrawableRes icon: Int = R.drawable.icon_error
    ) {
        popTip?.dismiss()
        val textInfo = TextInfo()
        popTip = PopTip.show(icon, value).setRadius(200F).autoDismiss(delay)
            .setBackgroundColor(backgroundColor)
            .setMessageTextInfo(
                textInfo
            )
    }*/

    fun sendWaitDialog(
        value: String,
        onBackPressedListener: OnBackPressedListener<WaitDialog> = OnBackPressedListener<WaitDialog> {
            WaitDialog.dismiss()
            true
        }
    ) {
        WaitDialog.dismiss()
        WaitDialog.show(value).setOnBackPressedListener(onBackPressedListener)
        Handler(Looper.getMainLooper()).postDelayed(
            { WaitDialog.dismiss() },
            Constants.DEFAULT_TIMEOUT_MILLISECONDS
        )
    }

    fun sendWaitDialog(value: String, delayMillis: Long) {
        WaitDialog.dismiss()
        WaitDialog.show(value).setOnBackPressedListener {
            WaitDialog.dismiss()
            true
        }
        Handler(Looper.getMainLooper()).postDelayed({ WaitDialog.dismiss() }, delayMillis)

    }

    fun sendTipDialog(value: String, type: WaitDialog.TYPE, timeout: Long) {
        TipDialog.dismiss()
        TipDialog.show(value, type, timeout).setOnBackPressedListener {
            TipDialog.dismiss()
            true
        }
    }

    fun sendDismiss() {
        WaitDialog.dismiss()
        popTip?.dismiss()
        TipDialog.dismiss()
    }

    fun sendPopTipDismiss() {
        popTip?.dismiss()
    }


    /**
     * 设置消息提示
     */
    /*fun setErrorData(value: String) {
        sendDismiss()
        sendPopTip(value, Color.RED)
    }*/

    fun topDialog(value: String, type: WaitDialog.TYPE) {
        sendDismiss()
        TipDialog.show(value, type, Constants.DEFAULT_TIMEOUT_MILLISECONDS)
            .setOnBackPressedListener {
                TipDialog.dismiss()
                true
            }
    }


}