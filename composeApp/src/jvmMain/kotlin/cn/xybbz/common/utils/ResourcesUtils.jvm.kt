package cn.xybbz.common.utils

import coil3.Bitmap

internal actual fun bitmapWidth(bitmap: Bitmap): Int = bitmap.width

internal actual fun bitmapHeight(bitmap: Bitmap): Int = bitmap.height

internal actual fun bitmapColor(bitmap: Bitmap, x: Int, y: Int): Int = bitmap.getColor(x, y)
