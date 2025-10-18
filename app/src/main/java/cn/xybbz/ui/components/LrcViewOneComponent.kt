package cn.xybbz.ui.components


import androidx.compose.runtime.Composable
import cn.xybbz.config.lrc.LrcServer
import cn.xybbz.ui.xy.XyItemText

@Composable
fun LrcViewOneComponent() {
    XyItemText(text = LrcServer.lrcText)
}

