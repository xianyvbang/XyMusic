package cn.xybbz.ui.components


import androidx.compose.runtime.Composable
import cn.xybbz.compositionLocal.LocalMainViewModel
import cn.xybbz.config.lrc.LrcServer
import cn.xybbz.ui.xy.XyItemText
import cn.xybbz.ui.xy.XyItemTextPadding
import cn.xybbz.ui.xy.XyRowNotHorizontalPadding

@Composable
fun LrcViewOneComponent() {
    XyItemText(text = LrcServer.lrcText)
}

