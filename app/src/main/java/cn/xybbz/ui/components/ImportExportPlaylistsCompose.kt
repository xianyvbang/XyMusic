package cn.xybbz.ui.components

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.documentfile.provider.DocumentFile
import cn.xybbz.R
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.common.utils.PlaylistParser
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyButton
import cn.xybbz.ui.xy.XyColumn
import cn.xybbz.ui.xy.XyItemTextHorizontal
import kotlinx.coroutines.launch

/**
 * 获得导出文件弹窗对象
 */
@Composable
inline fun getExportPlaylistsAlertDialogObject(
    crossinline onPlayTrackList: suspend () -> PlaylistParser.Playlist?,
    crossinline onClick: () -> Unit
): () -> Unit {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var playlist by remember {
        mutableStateOf<PlaylistParser.Playlist?>(null)
    }
    //导出
    val directoryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = { selectImageUri ->
            coroutineScope.launch {

                val playTrackList = playlist
                if (playTrackList == null) {
                    MessageUtils.sendPopTipError(R.string.playlist_export_failed)
                    Log.e(Constants.LOG_ERROR_PREFIX, "歌单信息为空")
                    return@launch
                }
                selectImageUri?.let {
                    //todo 这里保存文件并且生成文件
                    //创建file文件
                    // 拿到目录的 DocumentFile
                    val pickedDir = DocumentFile.fromTreeUri(context, it)
                    if (pickedDir != null && pickedDir.canWrite()) {
                        // 在目录下创建一个新文件，例如 test.txt
                        val newFile = pickedDir.createFile(
                            fileType?.mimeType ?: "text/plain",
                            "${playTrackList.playlistName}${fileType?.code}"
                        )
                        newFile?.uri?.let { fileUri ->
                            if (playTrackList.musicList.isNotEmpty()) {
                                val exportM3U =
                                    if (fileType == ExportType.M3U8) PlaylistParser.exportM3U(
                                        playTrackList.musicList
                                    ) else PlaylistParser.exportTxt(playTrackList.musicList)
                                // 往文件里写数据
                                context.contentResolver.openOutputStream(fileUri)?.use { output ->
                                    output.write(exportM3U.toByteArray())
                                }
                            }
                            MessageUtils.sendPopTipSuccess(R.string.playlist_export_success)
                        }
                    }
                }
            }
        }
    )
    return {
        onClick()
        AlertDialogObject(title = R.string.export_playlist, content = {
            ExportPlaylistsCompose(
                onPlayTrackList = {
                    playlist = onPlayTrackList()
                    playlist
                },
                onClose = { it.dismiss() },
                directoryLauncher = directoryLauncher
            )
        }).show()
    }

}

//导出文件类型
var fileType by mutableStateOf<ExportType?>(null)

enum class ExportType(val code: String, val mimeType: String) {
    TXT(".txt", "text/plain"),
    M3U8(".m3u8", "application/octet-stream");

    override fun toString(): String {
        return code
    }
}

/**
 * 导出歌单组件
 */
@Composable
fun ExportPlaylistsCompose(
    onPlayTrackList: suspend () -> PlaylistParser.Playlist?,
    onClose: () -> Unit,
    directoryLauncher: ManagedActivityResultLauncher<Uri?, Uri?>
) {
    val coroutineScope = rememberCoroutineScope()


    XyColumn(
        backgroundColor = Color.Transparent,
        paddingValues = PaddingValues(horizontal = XyTheme.dimens.outerHorizontalPadding)
    ) {
        XyItemTextHorizontal(
            text = stringResource(R.string.please_select_export_format)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ExportType.entries.forEachIndexed { index, type ->

                XyButton(onClick = {
                    fileType = type
                    coroutineScope.launch {
                        if (onPlayTrackList() == null) {
                            MessageUtils.sendPopTipError(R.string.playlist_import_failed)
                            return@launch
                        }
                        /*val initUri = DocumentsContract.buildTreeDocumentUri(
                       "com.android.externalstorage.documents",
                       "primary:Download" // 指向 /storage/emulated/0/Download
                   )*/
                        onClose()
                        val downloadDir =
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        val initUri = Uri.fromFile(downloadDir)
                        directoryLauncher.launch(initUri)
                    }
                }, text = type.code, modifier = Modifier.weight(1f))
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
inline fun importPlaylistsCompose(
    crossinline onCreatePlaylist: (PlaylistParser.Playlist) -> Unit,
    crossinline onClick: () -> Unit
): () -> Unit {
    val context = LocalContext.current
    //导入
    val openSelectPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { selectImageUri ->
            selectImageUri?.let {
                try {
                    context.contentResolver.openInputStream(it)?.use { fis ->
                        val configInfo =
                            fis.bufferedReader().use { reader ->
                                reader.readLines().map { line -> line.trim() }
                            }

                        val fileName = getFileName(context, it)
                        val tracks =
                            if (fileType == ExportType.M3U8) {
                                PlaylistParser.parseM3U(configInfo)
                            } else {
                                PlaylistParser.parseTxt(configInfo)
                            }
                        //开始对象转换
                        //读取
                        onCreatePlaylist(
                            PlaylistParser.Playlist(
                                tracks,
                                fileName ?: context.getString(Constants.UNKNOWN_PLAYLIST)
                            )
                        )
                    }
                } catch (_: Exception) {

                }

            }
        }
    )
    return {
        onClick()
        openSelectPhotoLauncher.launch(
            arrayOf(
                "text/plain",
                "application/vnd.apple.mpegurl",
                "application/octet-stream"
            )
        )
    }
}

fun getFileName(context: Context, uri: Uri): String? {
    var name: String? = null
    val cursor = context.contentResolver.query(
        uri, null, null, null, null
    )
    cursor?.use {
        if (it.moveToFirst()) {
            val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0) {
                name = it.getString(index)
            }
        }
    }
    return name
}