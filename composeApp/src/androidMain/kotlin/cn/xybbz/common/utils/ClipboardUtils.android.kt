package cn.xybbz.common.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import cn.xybbz.di.ContextWrapper
import org.koin.mp.KoinPlatform

actual fun copyTextToClipboard(text: String) {
    val context = KoinPlatform.getKoin().get<ContextWrapper>().context
    val clipboardManager =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboardManager.setPrimaryClip(ClipData.newPlainText("text", text))
}
