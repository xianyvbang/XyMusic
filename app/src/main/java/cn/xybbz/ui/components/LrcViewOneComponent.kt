package cn.xybbz.ui.components


import androidx.compose.runtime.Composable
import cn.xybbz.ui.xy.XyItemText

@Composable
fun LrcViewOneComponent(lrcText:String?) {
    lrcText?.let {
        XyItemText(text = lrcText)
    }
}

