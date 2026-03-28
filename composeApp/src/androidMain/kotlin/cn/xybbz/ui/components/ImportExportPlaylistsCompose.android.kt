package cn.xybbz.ui.components

import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.documentfile.provider.DocumentFile
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.utils.Log

@Composable
internal actual fun rememberPlaylistFileHandler(
    onImportResult: (PlaylistImportData?) -> Unit,
    onExportResult: (Boolean) -> Unit
): PlaylistFileHandler {
    val context = LocalContext.current
    val currentImportResult by rememberUpdatedState(onImportResult)
    val currentExportResult by rememberUpdatedState(onExportResult)
    var pendingExportRequest by remember { mutableStateOf<PlaylistExportRequest?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { directoryUri ->
        val request = pendingExportRequest
        pendingExportRequest = null
        if (request == null) {
            return@rememberLauncherForActivityResult
        }
        if (directoryUri == null) {
            currentExportResult(false)
            return@rememberLauncherForActivityResult
        }

        val success = runCatching {
            val pickedDir = DocumentFile.fromTreeUri(context, directoryUri)
                ?.takeIf { it.canWrite() }
                ?: error("无法写入目标目录")
            val newFile = pickedDir.createFile(request.fileType.mimeType, request.fileName)
                ?: error("创建导出文件失败")
            context.contentResolver.openOutputStream(newFile.uri)?.use { output ->
                output.write(request.content.encodeToByteArray())
            } ?: error("打开输出流失败")
        }.onFailure { error ->
            Log.e(Constants.LOG_ERROR_PREFIX, "导出歌单失败", error)
        }.isSuccess

        currentExportResult(success)
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { fileUri ->
        if (fileUri == null) {
            return@rememberLauncherForActivityResult
        }

        val importData = runCatching {
            val lines = context.contentResolver.openInputStream(fileUri)?.bufferedReader()?.use { reader ->
                reader.readLines().map(String::trim)
            } ?: error("读取导入文件失败")

            PlaylistImportData(
                fileName = resolveDisplayName(context, fileUri),
                lines = lines
            )
        }.onFailure { error ->
            Log.e(Constants.LOG_ERROR_PREFIX, "读取导入歌单文件失败", error)
        }.getOrNull()

        currentImportResult(importData)
    }

    return remember(exportLauncher, importLauncher) {
        object : PlaylistFileHandler {
            override fun importPlaylist() {
                importLauncher.launch(
                    arrayOf(
                        "text/plain",
                        "application/vnd.apple.mpegurl",
                        "application/octet-stream"
                    )
                )
            }

            override fun exportPlaylist(request: PlaylistExportRequest) {
                pendingExportRequest = request
                exportLauncher.launch(null)
            }
        }
    }
}

private fun resolveDisplayName(
    context: android.content.Context,
    fileUri: android.net.Uri
): String? {
    val cursor = context.contentResolver.query(fileUri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0) {
                return it.getString(nameIndex)
            }
        }
    }
    return null
}
