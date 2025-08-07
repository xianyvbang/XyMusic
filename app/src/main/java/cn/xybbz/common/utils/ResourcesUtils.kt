package cn.xybbz.common.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory

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
}