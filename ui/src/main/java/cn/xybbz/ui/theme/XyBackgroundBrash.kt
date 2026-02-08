package cn.xybbz.ui.theme

import android.net.Uri
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.graphics.Color

@Stable
class XyBackgroundBrash(
    ifEnabled: Boolean,
    ifChangeOneColor: Boolean,
    ifGlobalBrash: Boolean,
    backgroundImageUri: Uri?,
    globalBrash: List<Color>
) {
    /**
     * 是否启用渐变色
     */

    val ifEnabled by mutableStateOf(ifEnabled, structuralEqualityPolicy())

    /**
     * 是否切换为单一颜色背景
     */
    val ifChangeOneColor by mutableStateOf(ifChangeOneColor, structuralEqualityPolicy())

    /**
     * 是否切换为全局统一渐变色
     */
    val ifGlobalBrash by mutableStateOf(ifGlobalBrash, structuralEqualityPolicy())

    /**
     * 背景图片Uri
     */
    val backgroundImageUri by mutableStateOf(backgroundImageUri, structuralEqualityPolicy())

    /**
     * 全局统一渐变色
     */
    val globalBrash by mutableStateOf(globalBrash, structuralEqualityPolicy())
}

fun xyBackgroundBrash(
    ifEnabled: Boolean = false,
    ifChangeOneColor: Boolean = false,
    ifGlobalBrash: Boolean = false,
    backgroundImageUri: Uri? = null,
    globalBrash: List<Color> = listOf(Color(0xFF600015), Color(0xFF04727E))
): XyBackgroundBrash = XyBackgroundBrash(
    ifEnabled = ifEnabled,
    ifChangeOneColor = ifChangeOneColor,
    ifGlobalBrash = ifGlobalBrash,
    backgroundImageUri = backgroundImageUri,
    globalBrash = globalBrash
)