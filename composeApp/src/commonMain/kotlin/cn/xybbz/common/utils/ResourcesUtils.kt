package cn.xybbz.common.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.github.panpf.sketch.Bitmap
import com.github.panpf.sketch.Image
import com.github.panpf.sketch.asBitmapOrNull
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

/**
 * 资源工具类
 */
object ResourcesUtils {


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
        isDarkTheme: Boolean,
        onColorReady: (Color) -> Unit
    ) {
        val bitmap = drawableToBitmap(image)
        if (bitmap == null) {
            onColorReady(Color.Transparent)
            return
        }
        val dominantColor = sanitizePaletteColorForTheme(
            color = extractDominantColor(bitmap),
            isDarkTheme = isDarkTheme
        )
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

internal fun sanitizePaletteColorForTheme(color: Color, isDarkTheme: Boolean): Color {
    if (color.alpha == 0f) {
        return color
    }

    val luminance = color.luminance()
    val shouldUseDefaultColor =
        (!isDarkTheme && luminance < 0.2f) ||
                (isDarkTheme && luminance > 0.88f)

    return if (shouldUseDefaultColor) Color.Transparent else color
}

internal fun sampleDominantColor(
    width: Int,
    height: Int,
    colorAt: (Int, Int) -> Int
): Color {
    if (width <= 0 || height <= 0) {
        return Color.Transparent
    }

    val stepX = max(1, width / 24)
    val stepY = max(1, height / 24)
    val visibleBuckets = LinkedHashMap<Int, ColorBucket>()
    val accentBuckets = LinkedHashMap<Int, ColorBucket>()

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

            addToBucket(
                buckets = visibleBuckets,
                red = red,
                green = green,
                blue = blue,
                saturation = saturation,
                value = value
            )

            val isAccentCandidate = saturation >= 0.18f && value in 0.12f..0.92f
            if (isAccentCandidate) {
                addToBucket(
                    buckets = accentBuckets,
                    red = red,
                    green = green,
                    blue = blue,
                    saturation = saturation,
                    value = value
                )
            }
        }
    }

    val bestBucket = pickBestBucket(accentBuckets.values, preferAccent = true)
        ?: pickBestBucket(visibleBuckets.values, preferAccent = false)
        ?: return Color.Transparent

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

private fun addToBucket(
    buckets: MutableMap<Int, ColorBucket>,
    red: Int,
    green: Int,
    blue: Int,
    saturation: Float,
    value: Float
) {
    val bucketKey = ((red shr 4) shl 8) or ((green shr 4) shl 4) or (blue shr 4)
    val bucket = buckets.getOrPut(bucketKey) { ColorBucket() }
    bucket.count++
    bucket.red += red
    bucket.green += green
    bucket.blue += blue
    bucket.saturation += saturation
    bucket.value += value
}

private fun pickBestBucket(
    buckets: Collection<ColorBucket>,
    preferAccent: Boolean
): ColorBucket? {
    return buckets.maxByOrNull { bucket ->
        val averageSaturation = bucket.saturation / bucket.count
        val averageValue = bucket.value / bucket.count
        val countWeight = sqrt(bucket.count.toFloat())
        val saturationWeight = if (preferAccent) {
            ((averageSaturation - 0.12f) / 0.88f).coerceIn(0f, 1f)
        } else {
            (0.2f + averageSaturation * 0.8f).coerceIn(0f, 1f)
        }
        val midtoneFit = (1f - abs(averageValue - 0.55f) / 0.55f).coerceIn(0f, 1f)
        val whitePenalty = if (averageValue > 0.94f && averageSaturation < 0.12f) 0.15f else 1f
        val blackPenalty = if (averageValue < 0.14f && averageSaturation < 0.18f) 0.15f else 1f

        countWeight * (0.55f + saturationWeight * 0.75f + midtoneFit * 0.45f) * whitePenalty * blackPenalty
    }
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
