package cn.xybbz.ui.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import cn.xybbz.ui.windows.DesktopWindowTitleBar

@Composable
actual fun StartupScreenTitleBar() {
    // 桌面端启动页也需要真实窗口控制按钮，避免启动阶段看起来像黑屏或无边框空窗。
    DesktopWindowTitleBar(
        backgroundColor = MaterialTheme.colorScheme.background
    )
}
