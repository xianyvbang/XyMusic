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
import cn.xybbz.ui.components.JvmSnackBarPlayerComponentV2
import cn.xybbz.ui.theme.XyTheme

@Composable
internal actual fun MainScreenSnackBarHost(modifier: Modifier) {
    val mainViewModel = LocalMainViewModel.current
    val ifShowSnackBar by mainViewModel.settingsManager.ifShowSnackBar.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                bottom = XyTheme.dimens.outerVerticalPadding
            )
    ) {
        if (ifShowSnackBar) {
            Column {
                JvmSnackBarPlayerComponentV2(
                    onClick = {
                        mainViewModel.putSheetState(true)
                    }
                )
            }
        }
    }
}
