package cn.xybbz.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.export_playlist
import xymusic_kmp.composeapp.generated.resources.import_playlist_failed
import xymusic_kmp.composeapp.generated.resources.playlist_export_failed
import xymusic_kmp.composeapp.generated.resources.playlist_export_success
import xymusic_kmp.composeapp.generated.resources.please_select_export_format

private val ILLEGAL_FILE_CHARS_REGEX = Regex("""[\\/:*?"<>|]""")

internal data class PlaylistImportData(
    val fileName: String?,
    val lines: List<String>
)

internal data class PlaylistExportRequest(
    val fileName: String,
    val fileType: ExportType,
    val content: String
)

internal interface PlaylistFileHandler {
    fun importPlaylist()

    fun exportPlaylist(request: PlaylistExportRequest)
}

@Composable
internal expect fun rememberPlaylistFileHandler(
    onImportResult: (PlaylistImportData?) -> Unit,
    onExportResult: (Boolean) -> Unit
): PlaylistFileHandler

/**
 * 获得导出文件弹窗对象
 */
@Composable
fun getExportPlaylistsAlertDialogObject(
    onPlayTrackList: suspend () -> PlaylistParser.Playlist?,
    onClick: () -> Unit
): () -> Unit {
    val exportPlaylist = stringResource(Res.string.export_playlist)
    val playlistFileHandler = rememberPlaylistFileHandler(
        onImportResult = {},
        onExportResult = { success ->
            if (success) {
                MessageUtils.sendPopTipSuccess(Res.string.playlist_export_success)
            } else {
                MessageUtils.sendPopTipError(Res.string.playlist_export_failed)
            }
        }
    )

    return {
        onClick()
        AlertDialogObject(
            title = exportPlaylist,
            content = {
                ExportPlaylistsCompose(
                    onPlayTrackList = onPlayTrackList,
                    onClose = { it.dismiss() },
                    onExport = { request ->
                        playlistFileHandler.exportPlaylist(request)
                    }
                )
            }
        ).show()
    }
}

enum class ExportType(val code: String, val mimeType: String) {
    TXT(".txt", "text/plain"),
    M3U8(".m3u8", "application/octet-stream");

    override fun toString(): String = code
}

/**
 * 导出歌单组件
 */
@Composable
private fun ExportPlaylistsCompose(
    onPlayTrackList: suspend () -> PlaylistParser.Playlist?,
    onClose: () -> Unit,
    onExport: (PlaylistExportRequest) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

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
                        coroutineScope.launch {
                            val playlist = onPlayTrackList()
                            if (playlist == null) {
                                MessageUtils.sendPopTipError(Res.string.playlist_export_failed)
                                return@launch
                            }
                            onClose()
                            onExport(playlist.toExportRequest(type))
                        }
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
                val exportType = detectExportType(importData.fileName, importData.lines)
                val tracks = when (exportType) {
                    ExportType.M3U8 -> PlaylistParser.parseM3U(importData.lines)
                    ExportType.TXT -> PlaylistParser.parseTxt(importData.lines)
                }
                onCreatePlaylist(
                    PlaylistParser.Playlist(
                        musicList = tracks,
                        playlistName = resolvePlaylistName(importData.fileName, unknownPlaylist)
                    )
                )
            }.onFailure { error ->
                Log.e(Constants.LOG_ERROR_PREFIX, "导入歌单失败", error)
                MessageUtils.sendPopTipError(Res.string.import_playlist_failed)
            }
        },
        onExportResult = {}
    )

    return {
        onClick()
        playlistFileHandler.importPlaylist()
    }
}

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

private fun resolvePlaylistName(fileName: String?, fallback: String): String {
    val rawName = fileName
        ?.substringBeforeLast(".")
        ?.trim()
        .orEmpty()

    return rawName.ifBlank { fallback }
}
