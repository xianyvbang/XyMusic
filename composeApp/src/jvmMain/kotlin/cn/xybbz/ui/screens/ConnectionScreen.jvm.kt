package cn.xybbz.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cn.xybbz.common.enums.ConnectionUiType

@Composable
actual fun ConnectionScreen(
    connectionUiType: ConnectionUiType?,
    modifier: Modifier,
) {
    /*JvmConnectionNewScreen(
        connectionUiType = connectionUiType,
        modifier = modifier,
    )*/
    JvmConnectionScreen(
        connectionUiType = connectionUiType,
        modifier = modifier,
    )
}
