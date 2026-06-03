package cn.xybbz.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.xybbz.compositionLocal.LocalMainViewModel
import cn.xybbz.compositionLocal.LocalPlayerChromeState
import cn.xybbz.ui.components.SnackBarPlayerComponent

@Composable
internal expect fun MainScreenSnackBarHost(modifier: Modifier = Modifier)

@Composable
internal fun SharedMainScreenSnackBarHost(modifier: Modifier = Modifier) {
    val mainViewModel = LocalMainViewModel.current
    // 控制完整播放器页的打开状态。
    val playerChromeState = LocalPlayerChromeState.current
    val ifShowSnackBar by mainViewModel.settingsManager.ifShowSnackBar.collectAsStateWithLifecycle()

    if (ifShowSnackBar) {
        Column(modifier = modifier) {
            SnackBarPlayerComponent(
                onClick = {
                    playerChromeState.showPlayerSheet()
                }
            )
        }
    }
}
