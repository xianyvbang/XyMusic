package cn.xybbz.common.utils

import kotlin.math.pow
import kotlin.math.round

fun formatBytes(bytes: Long, withSpace: Boolean = false): String {
    if (bytes <= 0L) return if (withSpace) "0 B" else "0B"

    val units = listOf("B", "KB", "MB", "GB", "TB", "PB")
    var value = bytes.toDouble()
    var unitIndex = 0

    while (value >= 1024 && unitIndex < units.lastIndex) {
        value /= 1024
        unitIndex++
    }

    val decimals = when {
        unitIndex == 0 -> 0
        value >= 100 -> 0
        value >= 10 -> 1
        else -> 2
    }

    val scale = 10.0.pow(decimals)
    val roundedValue = round(value * scale) / scale
    val number = if (decimals == 0) {
        roundedValue.toLong().toString()
    } else {
        roundedValue.toString()
    }

    return if (withSpace) "$number ${units[unitIndex]}" else number + units[unitIndex]
}
