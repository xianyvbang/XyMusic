package cn.xybbz.common.utils

import androidx.compose.ui.graphics.Color
import coil3.Bitmap
import coil3.Image
import coil3.toBitmap

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

    /**
     * Coil 3 的成功结果返回的是 `Image`，这里统一转成 `Bitmap` 供 Palette 使用。
     */
    fun drawableToBitmap(drawable: Image?): Bitmap? {
        return drawable?.toBitmap()
    }

    /**
     * 从 Coil 3 的图片结果中提取主色调。
     */
    fun readPaletteColor(
        image: Image?,
        onColorReady: (Color) -> Unit
    ) {
        val bitmap = drawableToBitmap(image)
        if (bitmap == null) {
            onColorReady(Color.Transparent)
            return
        }
        val scaledBitmap = bitmap.scale(200, 200, false)
        Palette.from(scaledBitmap).generate { palette ->
            val colorValue = palette?.darkMutedSwatch?.rgb
                ?: palette?.mutedSwatch?.rgb
                ?: palette?.darkVibrantSwatch?.rgb
                ?: palette?.vibrantSwatch?.rgb
                ?: palette?.dominantSwatch?.rgb
            onColorReady(colorValue?.let(::Color) ?: Color.Transparent)
        }
    }
}
