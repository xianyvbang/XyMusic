package cn.xybbz.common.utils

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.graphics.toColorInt
import cn.xybbz.R
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog


object MessageUtils {

    var popTip: PopTip? = null

    val errorColor = Color.argb(255, 171, 55, 47)
    val successColor = Color.argb(255, 65, 174, 60)
    const val RADIUS_PX = 50F

    fun sendPopTip(value: String, delay: Long = 2000) {
        popTip?.dismiss()
        val newPopTip = PopTip.show(value).setRadius(RADIUS_PX).autoDismiss(delay)
        popTip = newPopTip
    }

    fun sendPopTip(@StringRes resId: Int, delay: Long = 2000) {
        popTip?.dismiss()
        val newPopTip = PopTip.show(PopTip.getApplicationContext().getString(resId))
            .setRadius(RADIUS_PX)
            .autoDismiss(delay)
        popTip = newPopTip
    }

    fun sendPopTip(value: String, @StringRes vararg resIds: Int, delay: Long = 2000) {
        popTip?.dismiss()

        val newPopTip = PopTip.show(value + resIds.joinToString {
            PopTip.getApplicationContext().getString(it)
        })
            .setRadius(RADIUS_PX)
            .autoDismiss(delay)
        popTip = newPopTip
    }

    fun sendPopTip(
        @StringRes resId: Int,
        @ColorInt backgroundColor: Int,
        delay: Long = 2000
    ) {
        popTip?.dismiss()
        val newPopTip = PopTip.show(PopTip.getApplicationContext().getString(resId)).setRadius(RADIUS_PX)
            .autoDismiss(delay)
            .setBackgroundColor(backgroundColor)
        popTip = newPopTip
    }


    fun sendPopTipError(value: String, delay: Long = 2000): PopTip {
        popTip?.dismiss()
        return PopTip.show(value).setRadius(RADIUS_PX).autoDismiss(delay)
            .setBackgroundColor(errorColor)
    }

    fun sendPopTipError(@StringRes resId: Int, delay: Long = 2000): PopTip {
        popTip?.dismiss()
        return PopTip.show(PopTip.getApplicationContext().getString(resId))
            .setRadius(RADIUS_PX)
            .autoDismiss(delay)
            .setBackgroundColor(errorColor)
    }

    fun sendPopTipSuccess(@StringRes resId: Int, delay: Long = 2000): PopTip {
        popTip?.dismiss()
        return PopTip.show(PopTip.getApplicationContext().getString(resId))
            .setRadius(RADIUS_PX).autoDismiss(delay)
            .setBackgroundColor(successColor)
    }

    fun sendPopTipSuccess(message: String, delay: Long = 2000): PopTip {
        popTip?.dismiss()
        return PopTip.show(message)
            .setRadius(RADIUS_PX).autoDismiss(delay)
            .setBackgroundColor(successColor)
    }

    fun sendPopTipHint(@StringRes resId: Int, delay: Long = 2000) {
        popTip?.dismiss()
        val newPopTip = PopTip.show(PopTip.getApplicationContext().getString(resId))
            .setRadius(RADIUS_PX)
            .autoDismiss(delay)
            .setBackgroundColor("#E6A23C".toColorInt())
        popTip = newPopTip
    }

    fun sendPopTipIconError(
        @StringRes resId: Int,
        delay: Long = 2000,
        @DrawableRes icon: Int = R.drawable.icon_error
    ) {
        popTip?.dismiss()
        PopTip.show(icon, PopTip.getApplicationContext().getString(resId))
            .setRadius(RADIUS_PX).autoDismiss(delay)
            .setBackgroundColor(errorColor)
    }

    fun sendDismiss() {
        WaitDialog.dismiss()
        popTip?.dismiss()
        TipDialog.dismiss()
    }

    fun sendPopTipDismiss() {
        popTip?.dismiss()
    }

}