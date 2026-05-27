package cn.xybbz.ui.screens

import androidx.compose.runtime.Composable

// 启动页标题栏按平台实现：桌面端需要窗口控制按钮，移动端保持空实现。
@Composable
expect fun StartupScreenTitleBar()
