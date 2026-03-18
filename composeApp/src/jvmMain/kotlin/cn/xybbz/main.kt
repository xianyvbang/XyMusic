package cn.xybbz

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "XyMusic-KMP",
    ) {
        App()
    }
}