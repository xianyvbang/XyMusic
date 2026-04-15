package cn.xybbz

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import cn.xybbz.di.initKoin
import cn.xybbz.proxy.JvmReverseProxyServer
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyColumn
import cn.xybbz.ui.xy.XyRow

fun main() = application {
    initKoin {}
    // 应用启动后立即拉起本地代理服务，供封面、音频与视频流转发使用。
    JvmReverseProxyServer.start()
    Window(
        onCloseRequest = {
            // 应用退出前主动关闭代理服务，避免残留端口占用与连接资源泄漏。
            JvmReverseProxyServer.stop()
            exitApplication()
        },
        undecorated = true,
        resizable = true,
        title = "XyMusic-KMP",
    ) {
        XyColumn(
            modifier = Modifier.fillMaxSize(),
            paddingValues = PaddingValues()
        ) {
            WindowDraggableArea {
                XyRow(
                    modifier = Modifier.height(XyTheme.dimens.itemHeight)
                        .background(Color.Green),
                    paddingValues = PaddingValues()
                ) {

                }
            }
            App()
        }
    }
}
