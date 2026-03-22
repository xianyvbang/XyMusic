package cn.xybbz

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import cn.xybbz.di.initKoin

fun main() = application {

    initKoin{}
    Window(
        onCloseRequest = ::exitApplication,
        title = "XyMusic-KMP",
    ) {
        App()
    }
}