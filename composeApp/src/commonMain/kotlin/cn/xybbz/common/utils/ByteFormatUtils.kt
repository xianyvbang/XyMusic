package cn.xybbz.common.utils

import kotlin.math.pow
import kotlin.math.round
import kotlin.math.roundToInt

/** 容量单位，按 1024 进位依次递增。 */
private val BYTE_UNITS = listOf("B", "KB", "MB", "GB", "TB", "PB")

/**
 * 将字节数按 1024 进位拆分为展示数值与单位下标。
 *
 * @return 换算后的数值与对应的单位下标；超出范围时停留在最后一个单位。
 */
private fun splitBytesToUnit(bytes: Double): Pair<Double, Int> {
    var value = bytes
    var unitIndex = 0
    while (value >= 1024.0 && unitIndex < BYTE_UNITS.lastIndex) {
        value /= 1024.0
        unitIndex++
    }
    return value to unitIndex
}

fun formatBytes(bytes: Long, withSpace: Boolean = false): String {
    if (bytes <= 0L) return if (withSpace) "0 B" else "0B"

    val (value, unitIndex) = splitBytesToUnit(bytes.toDouble())

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

    return if (withSpace) "$number ${BYTE_UNITS[unitIndex]}" else number + BYTE_UNITS[unitIndex]
}

/**
 * 将容量文本解析为字节数。
 *
 * ViewModel 暴露的是格式化后的文本，例如 12.4MB、1GB 或 0B；页面需要数值做汇总和占比条占比，
 * 因此在 UI 层进行轻量解析。
 *
 * @return 解析后的字节数；解析失败时返回 0。
 */
fun String.parseStorageBytes(): Double {
    val text = trim()
    if (text.isEmpty()) return 0.0

    // 容量文本由数字和单位组成，先截取数字部分，再根据单位换算。
    val numberText = text.takeWhile { it.isDigit() || it == '.' }
    val value = numberText.toDoubleOrNull() ?: return 0.0
    val unit = text.drop(numberText.length).trim().uppercase()
    val multiplier = when (unit) {
        "PB" -> 1024.0 * 1024.0 * 1024.0 * 1024.0 * 1024.0
        "TB" -> 1024.0 * 1024.0 * 1024.0 * 1024.0
        "GB" -> 1024.0 * 1024.0 * 1024.0
        "MB" -> 1024.0 * 1024.0
        "KB" -> 1024.0
        else -> 1.0
    }
    return value * multiplier
}

/**
 * 将字节数格式化为页面展示容量。
 *
 * @return 带单位的容量文本，例如 0B、42MB、1.5GB。
 */
fun Double.formatStorageLabel(): String {
    if (this <= 0.0) return "0B"

    val (value, unitIndex) = splitBytesToUnit(this)

    // 10 以上或接近整数时不保留小数；小容量精确到一位小数，减少状态文本宽度压力。
    val decimals = if (value >= 10.0 || (value - value.roundToInt()).let { it < 0.05 && it > -0.05 }) {
        0
    } else {
        1
    }
    val scale = 10.0.pow(decimals)
    val roundedValue = round(value * scale) / scale
    val numberText = if (decimals == 0) roundedValue.toLong().toString() else roundedValue.toString()
    return numberText + BYTE_UNITS[unitIndex]
}
