package cn.xybbz.common.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.graphics.createBitmap

/**
 * 资源工具类
 */
object ResourcesUtils {

    /**
     * 将字节数组转换成Bimap
     */
    fun bytes2Bimap(bytes: ByteArray?): Bitmap? {
        return if (bytes != null && bytes.isNotEmpty()) {
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } else {
            null
        }
    }

    fun drawableToBitmap(drawable: Drawable): Bitmap? {
        if (drawable is BitmapDrawable) {
            val bmp = drawable.bitmap
            // 如果是硬件位图，复制成 ARGB_8888 的软件位图
            if (bmp.config == Bitmap.Config.HARDWARE) {
                return bmp.copy(Bitmap.Config.ARGB_8888, false)
            }
            return bmp
        }
        val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 1
        val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 1
        return createBitmap(width, height).apply {
            val canvas = android.graphics.Canvas(this)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
        }
    }
}