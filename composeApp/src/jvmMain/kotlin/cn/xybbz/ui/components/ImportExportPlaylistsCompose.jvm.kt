package cn.xybbz.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.utils.Log
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
internal actual fun rememberPlaylistFileHandler(
    onImportResult: (PlaylistImportData?) -> Unit,
    onExportResult: (Boolean) -> Unit
): PlaylistFileHandler {
    val currentImportResult by rememberUpdatedState(onImportResult)
    val currentExportResult by rememberUpdatedState(onExportResult)

    return remember {
        object : PlaylistFileHandler {
            override fun importPlaylist() {
                val chooser = JFileChooser(defaultDirectory()).apply {
                    dialogTitle = "导入歌单"
                    fileSelectionMode = JFileChooser.FILES_ONLY
                    isAcceptAllFileFilterUsed = false
                    addChoosableFileFilter(FileNameExtensionFilter("Playlist Files", "txt", "m3u8"))
                }

                if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
                    return
                }

                val selectedFile = chooser.selectedFile ?: return
                val importData = runCatching {
                    PlaylistImportData(
                        fileName = selectedFile.name,
                        lines = selectedFile.readLines().map(String::trim)
                    )
                }.onFailure { error ->
                    Log.e(Constants.LOG_ERROR_PREFIX, "读取导入歌单文件失败", error)
                }.getOrNull()

                currentImportResult(importData)
            }

            override fun exportPlaylist(request: PlaylistExportRequest) {
                val chooser = JFileChooser(defaultDirectory()).apply {
                    dialogTitle = "导出歌单"
                    fileSelectionMode = JFileChooser.FILES_ONLY
                    selectedFile = File(defaultDirectory(), request.fileName)
                    fileFilter = FileNameExtensionFilter(
                        request.fileType.code.removePrefix(".").uppercase(),
                        request.fileType.code.removePrefix(".")
                    )
                }

                if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) {
                    return
                }

                val chosenFile = chooser.selectedFile ?: return
                val targetFile = normalizeExtension(chosenFile, request.fileType)
                val success = runCatching {
                    targetFile.writeText(request.content)
                }.onFailure { error ->
                    Log.e(Constants.LOG_ERROR_PREFIX, "导出歌单失败", error)
                }.isSuccess

                currentExportResult(success)
            }
        }
    }
}

private fun defaultDirectory(): File {
    val userHome = File(System.getProperty("user.home") ?: ".")
    val downloadDir = File(userHome, "Downloads")
    return if (downloadDir.exists()) downloadDir else userHome
}

private fun normalizeExtension(file: File, exportType: ExportType): File {
    return if (file.name.endsWith(exportType.code, ignoreCase = true)) {
        file
    } else {
        File(file.parentFile ?: defaultDirectory(), file.name + exportType.code)
    }
}
