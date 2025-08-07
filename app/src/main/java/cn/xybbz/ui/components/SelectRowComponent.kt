package cn.xybbz.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.ui.xy.XyItemBigTitle
import cn.xybbz.ui.xy.XyItemTabButton
import cn.xybbz.ui.xy.XyItemTextHorizontal

@Composable
fun SelectRowComponent(
    onIfOpenSelect: () -> Boolean,
    onSetIfOpenSelect: (Boolean) -> Unit,
    onSelectMusicDataList: () -> SnapshotStateList<XyMusic>,
    onRemoveSelectListResource: () -> Unit,
    onPlaySelect: () -> Unit,
    removePlaylistMusic: (@Composable () -> Unit)? = null
) {
    if (onIfOpenSelect()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                .height(54.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {

            XyItemTabButton(
                enabled = onSelectMusicDataList().isNotEmpty(),
                modifier = Modifier.weight(1f),
                onClick = {
                    AlertDialogObject(title = {
                        XyItemBigTitle(
                            text = "永久删除"
                        )
                    }, content = {
                        XyItemTextHorizontal(
                            text = "注意!!会同步删除服务端中本地文件"
                        )
                    }, onConfirmation = {
                        onRemoveSelectListResource()
                    }, onDismissRequest = {}).show()
                },
                text = "永久删除",
                imageVector = Icons.Rounded.Delete,
            )

            removePlaylistMusic?.invoke()

            XyItemTabButton(
                enabled = onSelectMusicDataList().isNotEmpty(),
                modifier = Modifier.weight(1f),
                onClick = {
                    onPlaySelect()
                },
                text = "播放选中",
                imageVector = Icons.AutoMirrored.Rounded.PlaylistAdd
            )
            XyItemTabButton(
                enabled = onSelectMusicDataList().isNotEmpty(),
                modifier = Modifier.weight(1f),
                onClick = {
                    AddPlaylistBottomData(
                        ifShow = true,
                        musicInfoList = onSelectMusicDataList().map { it },
                        onClose = {
                            onSetIfOpenSelect(false)
                            onSelectMusicDataList().clear()
                        }).show()
                },
                text = "增加到歌单",
                imageVector = Icons.Rounded.Add
            )
        }
    }
}