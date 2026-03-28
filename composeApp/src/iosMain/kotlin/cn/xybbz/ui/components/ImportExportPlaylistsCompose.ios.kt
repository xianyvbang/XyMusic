package cn.xybbz.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.utils.Log
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerMode
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.darwin.NSObject
import platform.posix.SEEK_END
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fread
import platform.posix.fseek
import platform.posix.ftell
import platform.posix.fwrite
import platform.posix.rewind

@Composable
internal actual fun rememberPlaylistFileHandler(
    onImportResult: (PlaylistImportData?) -> Unit,
    onExportResult: (Boolean) -> Unit
): PlaylistFileHandler {
    val currentImportResult by rememberUpdatedState(onImportResult)
    val currentExportResult by rememberUpdatedState(onExportResult)
    var importDelegate by remember { mutableStateOf<PlaylistImportDelegate?>(null) }

    return remember {
        object : PlaylistFileHandler {
            override fun importPlaylist() {
                val picker = UIDocumentPickerViewController(
                    documentTypes = listOf(
                        "public.text",
                        "public.plain-text",
                        "public.m3u-playlist",
                        "public.data"
                    ),
                    inMode = UIDocumentPickerMode.UIDocumentPickerModeImport
                )
                val delegate = PlaylistImportDelegate { importData ->
                    currentImportResult(importData)
                }
                importDelegate = delegate
                picker.delegate = delegate
                topViewController()?.presentViewController(
                    viewControllerToPresent = picker,
                    animated = true,
                    completion = null
                )
            }

            override fun exportPlaylist(request: PlaylistExportRequest) {
                val tempFilePath = NSTemporaryDirectory() + request.fileName
                val success = runCatching {
                    if (!writeTextFile(tempFilePath, request.content)) {
                        error("写入临时导出文件失败")
                    }
                    val fileUrl = NSURL.fileURLWithPath(tempFilePath)
                    val controller = UIActivityViewController(
                        activityItems = listOf(fileUrl),
                        applicationActivities = null
                    )
                    topViewController()?.presentViewController(
                        viewControllerToPresent = controller,
                        animated = true,
                        completion = null
                    ) ?: error("无法获取当前页面")
                }.onFailure { error ->
                    Log.e(Constants.LOG_ERROR_PREFIX, "导出歌单失败", error)
                }.isSuccess

                currentExportResult(success)
            }
        }
    }
}

private class PlaylistImportDelegate(
    private val onPicked: (PlaylistImportData?) -> Unit
) : NSObject(), UIDocumentPickerDelegateProtocol {

    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>
    ) {
        val fileUrl = didPickDocumentsAtURLs.firstOrNull() as? NSURL
        if (fileUrl == null) {
            onPicked(null)
            return
        }

        val importData = runCatching {
            val accessing = fileUrl.startAccessingSecurityScopedResource()
            try {
                val filePath = fileUrl.path ?: error("文件路径为空")
                val content = readTextFile(filePath) ?: error("读取导入文件失败")
                PlaylistImportData(
                    fileName = fileUrl.lastPathComponent,
                    lines = content.lines().map(String::trim)
                )
            } finally {
                if (accessing) {
                    fileUrl.stopAccessingSecurityScopedResource()
                }
            }
        }.onFailure { error ->
            Log.e(Constants.LOG_ERROR_PREFIX, "读取导入歌单文件失败", error)
        }.getOrNull()

        onPicked(importData)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun writeTextFile(path: String, content: String): Boolean {
    val file = fopen(path, "wb") ?: return false
    return try {
        val bytes = content.encodeToByteArray()
        val written = if (bytes.isEmpty()) {
            0UL
        } else {
            bytes.usePinned { pinned ->
                fwrite(pinned.addressOf(0), 1.convert(), bytes.size.convert(), file)
            }
        }
        written.toLong() == bytes.size.toLong()
    } finally {
        fclose(file)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun readTextFile(path: String): String? {
    val file = fopen(path, "rb") ?: return null
    return try {
        fseek(file, 0, SEEK_END)
        val fileSize = ftell(file)
        if (fileSize < 0) {
            return null
        }
        rewind(file)

        val size = fileSize.toInt()
        if (size == 0) {
            return ""
        }

        val bytes = ByteArray(size)
        val read = bytes.usePinned { pinned ->
            fread(pinned.addressOf(0), 1.convert(), size.convert(), file)
        }
        if (read.toLong() != size.toLong()) {
            return null
        }

        bytes.decodeToString()
    } finally {
        fclose(file)
    }
}

private fun topViewController(): UIViewController? {
    var controller = UIApplication.sharedApplication.keyWindow?.rootViewController
    while (controller?.presentedViewController != null) {
        controller = controller.presentedViewController
    }
    return controller
}
