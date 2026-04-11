package cn.xybbz

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import cn.xybbz.di.initKoin
import cn.xybbz.proxy.JvmReverseProxyServer

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
        title = "XyMusic-KMP",
    ) {
        App()
    }
}
