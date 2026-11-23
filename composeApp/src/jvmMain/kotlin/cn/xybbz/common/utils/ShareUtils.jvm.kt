package cn.xybbz.common.utils

import cn.xybbz.config.image.isAbsoluteNetworkUrl
import cn.xybbz.platform.ContextWrapper
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.File
import java.net.URI

/**
 * 桌面端不展示分享歌曲入口，避免出现无效或体验不佳的分享操作。
 */
internal actual val platformSupportsSongShare: Boolean = false

actual fun shareMusicResource(
    contextWrapper: ContextWrapper,
    resource: String?
) {
    val value = resource?.trim()?.takeIf { it.isNotEmpty() } ?: return
    val desktop = if (Desktop.isDesktopSupported()) Desktop.getDesktop() else null

    runCatching {
        if (value.isAbsoluteNetworkUrl()) {
            if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(URI(value))
                return
            }
        } else {
            val file = File(value)
            if (file.exists() && desktop != null && desktop.isSupported(Desktop.Action.OPEN)) {
                desktop.open(file)
                return
            }
        }
    }

    Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(value), null)
}
