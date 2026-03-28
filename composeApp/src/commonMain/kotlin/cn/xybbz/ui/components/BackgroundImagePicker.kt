package cn.xybbz.ui.components

import androidx.compose.runtime.Composable

internal interface BackgroundImagePicker {
    fun pickImage()
}

@Composable
internal expect fun rememberBackgroundImagePicker(
    onImagePicked: (String?) -> Unit
): BackgroundImagePicker
