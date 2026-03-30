package cn.xybbz.common.utils

import androidx.compose.ui.graphics.Color
import com.github.panpf.sketch.Bitmap
import com.github.panpf.sketch.Image
import com.github.panpf.sketch.asBitmapOrNull
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.math.abs
import kotlin.math.max

/**
 * 资源工具类
 */
object ResourcesUtils {

    private val logger = KotlinLogging.logger("ResourcesUtils")

    /**
     * Sketch 的成功结果返回的是 `Image`，这里统一转成 `Bitmap` 供跨平台取色使用。
     */
    fun drawableToBitmap(drawable: Image?): Bitmap? {
        return drawable?.asBitmapOrNull()
    }

    /**
     * 从 Sketch 的图片结果中提取主色调。
     */
    fun readPaletteColor(
        image: Image?,
        onColorReady: (Color) -> Unit
    ) {
        Log.i("=====","加载图片成功1")
        logger.info { "加载图片成功" }
        val bitmap = drawableToBitmap(image)
        if (bitmap == null) {
            onColorReady(Color.Transparent)
            return
        }
        Color(0.99607843f, 0.99607843f, 0.99607843f, 1.0f,)
        val dominantColor = extractDominantColor(bitmap)
        logger.info { "加载图片成功: $dominantColor" }
        onColorReady(dominantColor)
    }
}

internal expect fun bitmapWidth(bitmap: Bitmap): Int

internal expect fun bitmapHeight(bitmap: Bitmap): Int

internal expect fun bitmapColor(bitmap: Bitmap, x: Int, y: Int): Int

private data class ColorBucket(
    var count: Int = 0,
    var red: Int = 0,
    var green: Int = 0,
    var blue: Int = 0,
    var saturation: Float = 0f,
    var value: Float = 0f
)

internal fun extractDominantColor(bitmap: Bitmap): Color {
    return sampleDominantColor(
        width = bitmapWidth(bitmap),
        height = bitmapHeight(bitmap),
        colorAt = { x, y -> bitmapColor(bitmap, x, y) }
    )
}

private fun sampleDominantColor(
    width: Int,
    height: Int,
    colorAt: (Int, Int) -> Int
): Color {
    if (width <= 0 || height <= 0) {
        return Color.Transparent
    }

    val stepX = max(1, width / 24)
    val stepY = max(1, height / 24)
    val buckets = LinkedHashMap<Int, ColorBucket>()

    for (y in 0 until height step stepY) {
        for (x in 0 until width step stepX) {
            val argb = colorAt(x, y)
            val alpha = (argb ushr 24) and 0xFF
            if (alpha < 128) {
                continue
            }

            val red = (argb ushr 16) and 0xFF
            val green = (argb ushr 8) and 0xFF
            val blue = argb and 0xFF
            val (_, saturation, value) = rgbToHsv(red, green, blue)

            if (value < 0.08f) {
                continue
            }

            val bucketKey = ((red shr 4) shl 8) or ((green shr 4) shl 4) or (blue shr 4)
            val bucket = buckets.getOrPut(bucketKey) { ColorBucket() }
            bucket.count++
            bucket.red += red
            bucket.green += green
            bucket.blue += blue
            bucket.saturation += saturation
            bucket.value += value
        }
    }

    val bestBucket = buckets.values.maxByOrNull { bucket ->
        val averageSaturation = bucket.saturation / bucket.count
        val averageValue = bucket.value / bucket.count
        val darknessFit = (1f - abs(averageValue - 0.35f) / 0.35f).coerceIn(0f, 1f)
        bucket.count * (0.65f + averageSaturation * 0.35f + darknessFit * 0.25f)
    } ?: return Color.Transparent

    val averageRed = bestBucket.red / bestBucket.count
    val averageGreen = bestBucket.green / bestBucket.count
    val averageBlue = bestBucket.blue / bestBucket.count
    return Color(
        red = averageRed,
        green = averageGreen,
        blue = averageBlue,
        alpha = 255
    )
}

private fun rgbToHsv(red: Int, green: Int, blue: Int): Triple<Float, Float, Float> {
    val r = red / 255f
    val g = green / 255f
    val b = blue / 255f
    val maxColor = maxOf(r, g, b)
    val minColor = minOf(r, g, b)
    val delta = maxColor - minColor
    val saturation = if (maxColor == 0f) 0f else delta / maxColor

    return Triple(0f, saturation, maxColor)
}
