package cn.xybbz.common.utils

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.URLUtil
import cn.xybbz.platform.ContextWrapper
import java.io.File

actual fun shareMusicResource(
    contextWrapper: ContextWrapper,
    resource: String?
) {
    val value = resource?.trim()?.takeIf { it.isNotEmpty() } ?: return
    val context = contextWrapper.context

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        if (URLUtil.isNetworkUrl(value)) {
            putExtra(Intent.EXTRA_TEXT, value)
            type = "text/plain"
        } else {
            val uri = when {
                value.startsWith("content://") || value.startsWith("file://") -> Uri.parse(value)
                else -> Uri.fromFile(File(value))
            }
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            type = "audio/*"
        }
    }

    val chooserIntent = Intent.createChooser(sendIntent, null).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(chooserIntent, Bundle())
}
