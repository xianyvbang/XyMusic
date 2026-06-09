package cn.xybbz.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.xybbz.compositionLocal.LocalMainViewModel
import cn.xybbz.compositionLocal.LocalPlayerChromeState
import cn.xybbz.ui.components.JvmSnackBarPlayerComponent
import cn.xybbz.ui.theme.XyTheme

@Composable
internal actual fun MainScreenSnackBarHost(modifier: Modifier) {
    val mainViewModel = LocalMainViewModel.current
    // 控制桌面端完整播放器页的打开状态。
    val playerChromeState = LocalPlayerChromeState.current
    val ifShowSnackBar by mainViewModel.settingsManager.ifShowSnackBar.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(
//                bottom = XyTheme.dimens.outerVerticalPadding
            )
    ) {
        if (ifShowSnackBar) {
            Column {
                JvmSnackBarPlayerComponent(
                    onClick = {
                        // 点击桌面迷你播放条时显示完整播放器页面。
                        playerChromeState.showPlayerSheet()
                    }
                )
            }
        }
    }
}
