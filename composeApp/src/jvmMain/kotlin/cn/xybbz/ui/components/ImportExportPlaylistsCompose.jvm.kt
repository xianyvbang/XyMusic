package cn.xybbz.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.utils.Log
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
internal actual fun rememberPlaylistFileHandler(
    onImportResult: (PlaylistImportData?) -> Unit,
    onExportResult: (Boolean) -> Unit
): PlaylistFileHandler {
    // 保存最新回调，避免 remember 的 handler 对象持有旧闭包。
    val currentImportResult by rememberUpdatedState(onImportResult)
    val currentExportResult by rememberUpdatedState(onExportResult)
    // FileKit 选择/保存弹窗是 suspend API，现有接口保持非 suspend，由协程桥接。
    val coroutineScope = rememberCoroutineScope()
    // 导入和导出分别使用不同标题，系统原生弹窗会显示这些标题。
    val importDialogSettings = rememberJvmFileKitDialogSettings("导入歌单")
    val exportDialogSettings = rememberJvmFileKitDialogSettings("导出歌单")

    return remember(coroutineScope, importDialogSettings, exportDialogSettings) {
        object : PlaylistFileHandler {
            override fun importPlaylist() {
                coroutineScope.launch {
                    // 只允许选择当前解析器支持的歌单文件；用户取消时不触发失败提示。
                    val selectedFile = runCatching {
                        FileKit.openFilePicker(
                            type = FileKitType.File("txt", "m3u8"),
                            directory = defaultDirectory().toExistingPlatformDirectoryOrNull(),
                            dialogSettings = importDialogSettings,
                        )
                    }.onFailure { error ->
                        Log.e(Constants.LOG_ERROR_PREFIX, "打开导入歌单文件选择器失败", error)
                    }.getOrNull() ?: return@launch

                    // 文件读取放到 IO 线程，回调仍在当前协程恢复后触发。
                    val importData = runCatching {
                        withContext(Dispatchers.IO) {
                            PlaylistImportData(
                                fileName = selectedFile.name,
                                lines = selectedFile.readString().lineSequence().map(String::trim).toList()
                            )
                        }
                    }.onFailure { error ->
                        Log.e(Constants.LOG_ERROR_PREFIX, "读取导入歌单文件失败", error)
                    }.getOrNull()

                    currentImportResult(importData)
                }
            }

            override fun exportPlaylist(request: PlaylistExportRequest) {
                coroutineScope.launch {
                    // FileKit 的扩展名参数是保存弹窗提示，真正落盘前仍要做扩展名兜底。
                    val extension = request.fileType.code.removePrefix(".")
                    val chosenFile = runCatching {
                        FileKit.openFileSaver(
                            suggestedName = request.fileName,
                            defaultExtension = extension,
                            allowedExtensions = setOf(extension),
                            directory = defaultDirectory().toExistingPlatformDirectoryOrNull(),
                            dialogSettings = exportDialogSettings,
                        )
                    }.onFailure { error ->
                        Log.e(Constants.LOG_ERROR_PREFIX, "打开导出歌单文件选择器失败", error)
                    }.getOrNull() ?: return@launch

                    // 平台保存弹窗可能返回不带扩展名的路径，这里保持旧实现的自动补后缀行为。
                    val targetFile = normalizeExtension(chosenFile, request.fileType)
                    val success = runCatching {
                        withContext(Dispatchers.IO) {
                            targetFile.writeText(request.content)
                        }
                    }.onFailure { error ->
                        Log.e(Constants.LOG_ERROR_PREFIX, "导出歌单失败", error)
                    }.isSuccess

                    currentExportResult(success)
                }
            }
        }
    }
}

/**
 * 获取歌单导入/导出的默认目录。
 *
 * @return 优先返回用户下载目录，不存在时回退到用户主目录。
 */
private fun defaultDirectory(): File {
    val userHome = File(System.getProperty("user.home") ?: ".")
    val downloadDir = File(userHome, "Downloads")
    return if (downloadDir.exists()) downloadDir else userHome
}

/**
 * 确保导出的歌单文件带有目标格式扩展名。
 *
 * @param file 用户在系统保存弹窗中选择的目标文件。
 * @param exportType 导出格式。
 * @return 已补齐扩展名的目标文件。
 */
private fun normalizeExtension(file: File, exportType: ExportType): File {
    return if (file.name.endsWith(exportType.code, ignoreCase = true)) {
        file
    } else {
        File(file.parentFile ?: defaultDirectory(), file.name + exportType.code)
    }
}

/**
 * 将 FileKit 返回的平台文件转换为 JVM File，并复用扩展名规范化逻辑。
 *
 * @param file FileKit 保存弹窗返回的平台文件。
 * @param exportType 导出格式。
 * @return 已补齐扩展名的 JVM 文件。
 */
private fun normalizeExtension(file: PlatformFile, exportType: ExportType): File {
    return normalizeExtension(File(file.absolutePath()), exportType)
}
