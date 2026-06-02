package cn.xybbz.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.utils.Log
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.common.utils.PlaylistParser
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyButton
import cn.xybbz.ui.xy.XyColumn
import cn.xybbz.ui.xy.XyTextSubSmall
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readString
import io.github.vinceglb.filekit.writeString
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.export_playlist
import xymusic_kmp.composeapp.generated.resources.please_select_export_format

/**
 * 文件名中各平台都不适合作为真实文件名的字符。
 */
private val ILLEGAL_FILE_CHARS_REGEX = Regex("""[\\/:*?"<>|]""")

/**
 * 从用户选择的歌单文件中读取出的导入数据。
 *
 * @param fileName 原始文件名,用于推断导入格式和默认歌单名
 * @param lines 文件内容按行裁剪后的结果
 * @param fileType 根据文件名和内容推断出的歌单格式
 */
internal data class PlaylistImportData(
    val fileName: String?,
    val lines: List<String>,
    val fileType: ExportType
)

/**
 * 交给文件保存器写出的歌单导出请求。
 *
 * @param fileName 建议保存的文件名
 * @param fileType 用户选择的导出格式
 * @param content 已序列化好的歌单内容
 */
internal data class PlaylistExportRequest(
    val fileName: String,
    val fileType: ExportType,
    val content: String
)

/**
 * 文件保存器完成一次导出后的结果。
 *
 * @param request 本次导出的文件信息
 * @param success 是否写入成功
 */
internal data class PlaylistExportResult(
    val request: PlaylistExportRequest,
    val success: Boolean
)

/**
 * 屏蔽 FileKit 具体实现的歌单文件操作入口。
 */
internal interface PlaylistFileHandler {
    /**
     * 打开歌单导入文件选择器。
     */
    fun importPlaylist()

    /**
     * 打开歌单导出保存选择器。
     */
    fun exportPlaylist(request: PlaylistExportRequest)
}

/**
 * 创建跨平台歌单导入/导出的 FileKit 处理器。
 *
 * 这里负责文件选择/保存弹窗、文件读写和异常日志,业务层只需要关心
 * 导入结果或导出是否成功。
 */
@Composable
internal fun rememberPlaylistFileHandler(
    onImportResult: (PlaylistImportData?) -> Unit,
    onExportResult: (PlaylistExportResult) -> Unit
): PlaylistFileHandler {
    // 保持回调引用最新,避免 launcher 回调中捕获旧的 Composable 参数。
    val currentImportResult by rememberUpdatedState(onImportResult)
    val currentExportResult by rememberUpdatedState(onExportResult)
    val coroutineScope = rememberCoroutineScope()
    val importDialogSettings = rememberFileKitDialogSettings("导入歌单")
    val exportDialogSettings = rememberFileKitDialogSettings("导出歌单")
    // FileKit 的保存回调只返回用户选择的文件,导出内容需要临时挂起保存。
    var pendingExportRequest by remember { mutableStateOf<PlaylistExportRequest?>(null) }

    val importLauncher = rememberFilePickerLauncher(
        type = FileKitType.File("txt", "m3u8"),
        dialogSettings = importDialogSettings
    ) { selectedFile ->
        // 用户取消选择时不提示错误。
        if (selectedFile == null) {
            return@rememberFilePickerLauncher
        }

        val selectedFileName = selectedFile.name
        MessageUtils.sendPopTip(buildImportReadingMessage(selectedFileName))
        coroutineScope.launch {
            val importData = runCatching {
                // 文件读取放到 IO 线程,避免阻塞 Compose 主线程。
                withContext(Dispatchers.IO) {
                    val lines = selectedFile.readString().lineSequence().map(String::trim).toList()
                    PlaylistImportData(
                        fileName = selectedFileName,
                        lines = lines,
                        fileType = detectExportType(selectedFileName, lines)
                    )
                }
            }.onFailure { error ->
                Log.e(Constants.LOG_ERROR_PREFIX, "读取导入歌单文件失败", error)
                MessageUtils.sendPopTipError(buildImportReadFailedMessage(selectedFileName))
            }.getOrNull()

            currentImportResult(importData)
        }
    }
    val exportLauncher = rememberFileSaverLauncher(
        dialogSettings = exportDialogSettings
    ) { chosenFile ->
        // 取出并清空待导出请求,防止重复写入上一轮导出内容。
        val request = pendingExportRequest
        pendingExportRequest = null

        // request 为空属于内部状态异常,chosenFile 为空表示用户取消保存;二者都不继续写文件。
        if (request == null || chosenFile == null) {
            return@rememberFileSaverLauncher
        }

        coroutineScope.launch {
            MessageUtils.sendPopTip(buildExportWritingMessage(request))
            // 保存文件失败时只返回失败状态,由上层统一弹出导出失败提示。
            val success = runCatching {
                chosenFile.writeString(request.content)
            }.onFailure { error ->
                Log.e(Constants.LOG_ERROR_PREFIX, "导出歌单失败", error)
            }.isSuccess

            currentExportResult(PlaylistExportResult(request, success))
        }
    }

    return remember(importLauncher, exportLauncher) {
        object : PlaylistFileHandler {
            override fun importPlaylist() {
                // 打开原生文件选择器本身也可能失败,需要兜底防止桌面端弹异常窗口。
                runCatching {
                    importLauncher.launch()
                }.onFailure { error ->
                    Log.e(Constants.LOG_ERROR_PREFIX, "打开导入歌单文件选择器失败", error)
                }
            }

            override fun exportPlaylist(request: PlaylistExportRequest) {
                val extension = request.fileType.code.removePrefix(".")
                pendingExportRequest = request

                // 保存器弹出后会异步回调 chosenFile,所以先保存 request 再 launch。
                runCatching {
                    exportLauncher.launch(
                        suggestedName = request.fileName.removeSuffixIgnoreCase(request.fileType.code),
                        defaultExtension = extension,
                        allowedExtensions = setOf(extension)
                    )
                }.onFailure { error ->
                    pendingExportRequest = null
                    Log.e(Constants.LOG_ERROR_PREFIX, "打开导出歌单文件选择器失败", error)
                    currentExportResult(PlaylistExportResult(request, false))
                }
            }
        }
    }
}

/**
 * 获得导出文件弹窗对象
 *
 * @param coroutineScope 页面级协程作用域,用于让导出生成任务跨过菜单和弹窗关闭
 * @param playlistName 当前歌单名,用于在生成导出数据前展示预计文件名
 * @param onPlayTrackList 拉取并组装当前歌单数据,参数为带文件名和格式的失败提示
 * @param onClick 打开弹窗前的 UI 收尾动作,例如关闭菜单
 */
@Composable
fun getExportPlaylistsAlertDialogObject(
    coroutineScope: CoroutineScope,
    playlistName: String? = null,
    onPlayTrackList: suspend (exportFailedTip: String) -> PlaylistParser.Playlist?,
    onClick: () -> Unit
): () -> Unit {
    val exportPlaylist = stringResource(Res.string.export_playlist)
    val playlistFileHandler = rememberPlaylistFileHandler(
        onImportResult = {},
        onExportResult = { result ->
            // 文件真正写入完成后再提示成功,失败则统一显示导出失败。
            if (result.success) {
                MessageUtils.sendPopTipSuccess(buildExportSuccessMessage(result.request))
            } else {
                MessageUtils.sendPopTipError(buildExportFailedMessage(result.request))
            }
        }
    )

    return {
        onClick()
        // 弹窗只负责选择导出格式,真正的保存路径由 FileKit 保存器继续处理。
        AlertDialogObject(
            title = exportPlaylist,
            content = {
                ExportPlaylistsCompose(
                    onClose = { it.dismiss() },
                    onExportTypeSelected = { type ->
                        coroutineScope.launch {
                            val pendingFileName = buildExportFileName(playlistName.orEmpty(), type)
                            val exportFailedTip = buildExportFailedMessage(pendingFileName, type)
                            MessageUtils.sendPopTip(buildExportPreparingMessage(pendingFileName, type))
                            // 拉取远端歌单数据可能超时,这里兜底避免异常冒出导致闪退。
                            val playlist = runCatching {
                                onPlayTrackList(exportFailedTip)
                            }.onFailure { error ->
                                if (error is CancellationException) {
                                    throw error
                                }
                                Log.e(Constants.LOG_ERROR_PREFIX, "准备导出歌单数据失败", error)
                                MessageUtils.sendPopTipError(exportFailedTip)
                            }.getOrNull()
                            if (playlist == null) {
                                return@launch
                            }

                            playlistFileHandler.exportPlaylist(playlist.toExportRequest(type))
                        }
                    }
                )
            }
        ).show()
    }
}

/**
 * 支持的歌单导出/导入格式。
 *
 * @param code 文件扩展名
 * @param mimeType 文件 MIME 类型,保留给需要 MIME 的平台或后续扩展使用
 */
enum class ExportType(val code: String, val mimeType: String) {
    TXT(".txt", "text/plain"),
    M3U8(".m3u8", "application/octet-stream");

    override fun toString(): String = code
}

/**
 * 导出歌单组件
 *
 * @param onClose 导出格式选择完成后关闭当前弹窗
 * @param onExportTypeSelected 将选择的格式交给弹窗外层继续导出流程
 */
@Composable
private fun ExportPlaylistsCompose(
    onClose: () -> Unit,
    onExportTypeSelected: (ExportType) -> Unit
) {
    XyColumn(
        backgroundColor = Color.Transparent,
        paddingValues = PaddingValues(horizontal = XyTheme.dimens.outerHorizontalPadding)
    ) {
        XyTextSubSmall(
            text = stringResource(Res.string.please_select_export_format)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ExportType.entries.forEachIndexed { index, type ->
                XyButton(
                    onClick = {
                        // 点击格式后立即关闭选择弹窗,后续生成和保存流程只通过提示反馈状态。
                        onClose()
                        onExportTypeSelected(type)
                    },
                    text = type.code,
                    modifier = Modifier.weight(1f)
                )
                if (index != ExportType.entries.size - 1) {
                    Spacer(modifier = Modifier.width(XyTheme.dimens.outerHorizontalPadding))
                }
            }
        }
    }
}

/**
 * 导入歌单数据
 *
 * @param onCreatePlaylist 解析成功后创建或导入歌单的业务回调
 * @param onClick 打开导入选择器前的 UI 收尾动作,例如关闭菜单
 */
@Composable
fun importPlaylistsCompose(
    onCreatePlaylist: (PlaylistParser.Playlist) -> Unit,
    onClick: () -> Unit
): () -> Unit {
    val unknownPlaylist = stringResource(Constants.UNKNOWN_PLAYLIST)
    val playlistFileHandler = rememberPlaylistFileHandler(
        onImportResult = importHandler@{ importData ->
            if (importData == null) {
                return@importHandler
            }

            runCatching {
                MessageUtils.sendPopTip(buildImportParsingMessage(importData))
                // 根据文件名和内容自动识别格式,再交给对应解析器。
                val tracks = when (importData.fileType) {
                    ExportType.M3U8 -> PlaylistParser.parseM3U(importData.lines)
                    ExportType.TXT -> PlaylistParser.parseTxt(importData.lines)
                }
                MessageUtils.sendPopTipSuccess(buildImportParseSuccessMessage(importData))
                onCreatePlaylist(
                    PlaylistParser.Playlist(
                        musicList = tracks,
                        playlistName = resolvePlaylistName(importData.fileName, unknownPlaylist)
                    )
                )
            }.onFailure { error ->
                Log.e(Constants.LOG_ERROR_PREFIX, "导入歌单失败", error)
                MessageUtils.sendPopTipError(buildImportParseFailedMessage(importData))
            }
        },
        onExportResult = {}
    )

    return {
        onClick()
        playlistFileHandler.importPlaylist()
    }
}

/**
 * 将内存中的歌单转换为文件保存请求。
 */
private fun PlaylistParser.Playlist.toExportRequest(fileType: ExportType): PlaylistExportRequest {
    val content = when (fileType) {
        ExportType.M3U8 -> PlaylistParser.exportM3U(musicList)
        ExportType.TXT -> PlaylistParser.exportTxt(musicList)
    }

    return PlaylistExportRequest(
        fileName = buildExportFileName(playlistName, fileType),
        fileType = fileType,
        content = content
    )
}

/**
 * 构造导出文件名,同时清理系统不允许的文件名字符。
 */
private fun buildExportFileName(playlistName: String, fileType: ExportType): String {
    val baseName = playlistName.trim()
        .ifBlank { "playlist" }
        .replace(ILLEGAL_FILE_CHARS_REGEX, "_")

    return if (baseName.endsWith(fileType.code, ignoreCase = true)) {
        baseName
    } else {
        baseName + fileType.code
    }
}

/**
 * 导出格式展示名,用于用户提示。
 */
private fun ExportType.displayName(): String {
    return code.removePrefix(".").uppercase()
}

/**
 * 导出数据生成阶段的提示。
 */
private fun buildExportPreparingMessage(fileName: String, fileType: ExportType): String {
    return "正在生成 $fileName 的 ${fileType.displayName()} 歌单文件…"
}

/**
 * 导出文件写入阶段的提示。
 */
private fun buildExportWritingMessage(request: PlaylistExportRequest): String {
    return "正在导出 ${request.fileName} 的 ${request.fileType.displayName()} 歌单文件…"
}

/**
 * 导出成功提示。
 */
private fun buildExportSuccessMessage(request: PlaylistExportRequest): String {
    return "${request.fileName} 的 ${request.fileType.displayName()} 歌单文件导出成功"
}

/**
 * 导出失败提示。
 */
private fun buildExportFailedMessage(request: PlaylistExportRequest): String {
    return buildExportFailedMessage(request.fileName, request.fileType)
}

/**
 * 导出失败提示。
 */
private fun buildExportFailedMessage(fileName: String, fileType: ExportType): String {
    return "$fileName 的 ${fileType.displayName()} 歌单文件导出失败"
}

/**
 * 导入文件读取阶段的提示。
 */
private fun buildImportReadingMessage(fileName: String?): String {
    return "正在读取 ${fileName.toDisplayFileName()} 歌单文件…"
}

/**
 * 导入文件读取失败提示。
 */
private fun buildImportReadFailedMessage(fileName: String?): String {
    return "${fileName.toDisplayFileName()} 歌单文件读取失败"
}

/**
 * 导入文件解析阶段的提示。
 */
private fun buildImportParsingMessage(importData: PlaylistImportData): String {
    return "正在解析 ${importData.displayFileName()} 的 ${importData.fileType.displayName()} 歌单文件…"
}

/**
 * 导入文件解析成功提示。
 */
private fun buildImportParseSuccessMessage(importData: PlaylistImportData): String {
    return "${importData.displayFileName()} 的 ${importData.fileType.displayName()} 歌单文件解析成功"
}

/**
 * 导入文件解析失败提示。
 */
private fun buildImportParseFailedMessage(importData: PlaylistImportData): String {
    return "${importData.displayFileName()} 的 ${importData.fileType.displayName()} 歌单文件解析失败"
}

/**
 * 导入数据展示用文件名。
 */
private fun PlaylistImportData.displayFileName(): String {
    return fileName.toDisplayFileName()
}

/**
 * 用户未提供文件名时的展示兜底。
 */
private fun String?.toDisplayFileName(): String {
    return this?.ifBlank { null } ?: "未知"
}

/**
 * 根据扩展名和首个有效内容行推断导入文件格式。
 */
private fun detectExportType(fileName: String?, lines: List<String>): ExportType {
    if (fileName?.endsWith(ExportType.M3U8.code, ignoreCase = true) == true) {
        return ExportType.M3U8
    }

    return if (lines.firstOrNull { it.isNotBlank() }?.trim()?.equals("#EXTM3U", ignoreCase = true) == true) {
        ExportType.M3U8
    } else {
        ExportType.TXT
    }
}

/**
 * 从导入文件名推断歌单名,文件名为空时使用兜底名称。
 */
private fun resolvePlaylistName(fileName: String?, fallback: String): String {
    val rawName = fileName
        ?.substringBeforeLast(".")
        ?.trim()
        .orEmpty()

    return rawName.ifBlank { fallback }
}

/**
 * 忽略大小写移除指定后缀,避免 FileKit 建议文件名出现重复扩展名。
 */
private fun String.removeSuffixIgnoreCase(suffix: String): String {
    return if (endsWith(suffix, ignoreCase = true)) {
        dropLast(suffix.length)
    } else {
        this
    }
}
