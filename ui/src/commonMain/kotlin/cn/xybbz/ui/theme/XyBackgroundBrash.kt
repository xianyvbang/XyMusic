package cn.xybbz.ui.theme

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.graphics.Color

@Stable
class XyBackgroundBrash(
    backgroundImageUri: String?,
) {

    /**
     * 背景图片Uri
     */
    val backgroundImageUri by mutableStateOf(backgroundImageUri, structuralEqualityPolicy())

}

fun xyBackgroundBrash(
    backgroundImageUri: String? = null,
): XyBackgroundBrash = XyBackgroundBrash(
    backgroundImageUri = backgroundImageUri,
)
