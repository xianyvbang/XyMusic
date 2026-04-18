package cn.xybbz.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cn.xybbz.ui.theme.XyTheme

@Composable
internal actual fun MainScreenSnackBarHost(modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = jvmRouterMenuWidth + XyTheme.dimens.innerHorizontalPadding,
                end = XyTheme.dimens.innerHorizontalPadding,
                bottom = XyTheme.dimens.outerVerticalPadding
            )
    ) {
        SharedMainScreenSnackBarHost()
    }
}
