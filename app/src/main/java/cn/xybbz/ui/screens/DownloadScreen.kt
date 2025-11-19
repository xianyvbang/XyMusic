package cn.xybbz.ui.screens

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.ProgressIndicatorDefaults.drawStopIndicator
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.xybbz.R
import cn.xybbz.compositionLocal.LocalNavController
import cn.xybbz.localdata.data.download.XyDownload
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.enums.DownloadStatus
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.show
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.LazyColumnNotComponent
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.viewmodel.DownloadViewModel
import kotlinx.coroutines.launch
import kotlin.math.log10
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadScreen(
    modifier: Modifier = Modifier,
    downloadViewModel: DownloadViewModel = hiltViewModel<DownloadViewModel>()
) {

    val coroutineScope = rememberCoroutineScope()
    val navController = LocalNavController.current
    val tasks by downloadViewModel.musicDownloadInfo.collectAsStateWithLifecycle()


    XyColumnScreen(
        modifier = Modifier
    ) {
        TopAppBarComponent(
            modifier = Modifier.statusBarsPadding(),
            title = {
                Text(
                    text = "下载列表",
                    fontWeight = FontWeight.W900
                )
            }, navigationIcon = {
                IconButton(onClick = composeClick { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.return_home)
                    )
                }
            })

        LazyColumnNotComponent(
            contentPadding = PaddingValues(
                XyTheme.dimens.outerHorizontalPadding
            ),
            verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.innerVerticalPadding),
        ) {
            items(tasks, key = { it.id }) { task ->
                DownloadItemTrailingContent(
                    task = task,
                    isMultiSelectMode = downloadViewModel.isMultiSelectMode,
                    isSelected = downloadViewModel.selectedTaskIds.contains(task.id),
                    onItemClick = {
                        if (downloadViewModel.isMultiSelectMode) {
                            downloadViewModel.toggleSelection(task.id)
                        }
                    },
                    onItemLongClick = {
                        downloadViewModel.enterMultiSelectMode()
                        downloadViewModel.toggleSelection(task.id)
                    },
                    onPause = { downloadViewModel.pauseDownload(task.id) },
                    onResume = { downloadViewModel.resumeDownload(task.id) },
                    onCancel = { downloadViewModel.cancelDownload(task.id) },
                    onDelete = { downloadViewModel.deleteDownload(task.id) },
                    onGetMusicInfo = { downloadViewModel.getMusicInfoById(it) }
                )
            }
        }
    }
}

@Composable
fun DownloadItemTrailingContent(
    modifier: Modifier = Modifier,
    task: XyDownload,
    enabled: Boolean = true,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    brush: Brush? = null,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    isMultiSelectMode: Boolean,
    isSelected: Boolean,
    onItemClick: () -> Unit,
    onItemLongClick: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    onGetMusicInfo: suspend (String) -> XyMusic?
) {

    val primary = MaterialTheme.colorScheme.primary
    val coroutineScope = rememberCoroutineScope()

    Card(
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (brush != null) Color.Transparent else backgroundColor
        ), modifier = Modifier
            .height(84.dp)
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (task.status == DownloadStatus.DOWNLOADING || task.status == DownloadStatus.QUEUED) {
                        onPause()
                    } else if (task.status == DownloadStatus.PAUSED) {
                        onResume()
                    }
                },
                onLongClick = onItemLongClick
            )
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.height(30.dp)) {
                    Text(
                        text = (task.title ?: task.fileName) + "Status: ${task.status.name}",
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodySmall,
                        overflow = TextOverflow.Ellipsis,
                        color = textColor
                    )
                }

                Column(
                    modifier
                        .fillMaxWidth()
                        .height(30.dp)
                ) {

                    when (task.status) {
                        DownloadStatus.QUEUED -> {
                            DownloadPrompt(
                                text = "排队中",
                                fontSize = 14.sp
                            )
                        }

                        DownloadStatus.DOWNLOADING -> {
                            Column {
                                LinearProgressIndicator(
                                    modifier = Modifier.fillMaxWidth(),
                                    progress = { if (task.totalBytes > 0) task.progress / 100f else 0f },
                                    drawStopIndicator = {
                                        drawStopIndicator(
                                            drawScope = this,
                                            stopSize = 0.dp,
                                            color = primary,
                                            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                                        )
                                    })
                                Text(
                                    "${formatBytes(task.downloadedBytes)} / ${formatBytes(task.totalBytes)}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.align(Alignment.End)
                                )
                                /*Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    if (task.status in listOf(
                                            DownloadStatus.QUEUED,
                                            DownloadStatus.DOWNLOADING,
                                            DownloadStatus.PAUSED,
                                            DownloadStatus.FAILED
                                        )
                                    ) {
                                        IconButton(onClick = onCancel) {
                                            Icon(Icons.Default.Close, contentDescription = "Cancel")
                                        }
                                        IconButton(onClick = onDelete) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                                        }
                                    }
                                    if (task.status == DownloadStatus.CANCEL || task.status == DownloadStatus.COMPLETED) {
                                        IconButton(onClick = onDelete) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                                        }
                                    }
                                }*/

                            }
                        }

                        DownloadStatus.PAUSED -> {
                            DownloadPrompt(
                                text = "点击继续下载",
                                fontSize = 14.sp,
                            )
                        }

                        DownloadStatus.COMPLETED -> {
                            DownloadPrompt(
                                text = "下载完成",
                                fontSize = 14.sp,
                            )
                        }

                        DownloadStatus.FAILED -> {
                            DownloadPrompt(
                                text = "下载失败: ${task.error}",
                                fontSize = 14.sp,
                            )
                        }

                        DownloadStatus.CANCEL -> {
                            DownloadPrompt(
                                text = "取消下载",
                                fontSize = 14.sp,
                            )
                        }
                    }
                }

            }

            if (isMultiSelectMode){
                IconButton(
                    modifier = Modifier.offset(x = (10).dp),
                    onClick = {
                        onItemClick()
                    },
                ) {
                    RadioButton(selected = isSelected, onClick = {
                        onItemClick()
                    })
                }
            }else {
                IconButton(
                    modifier = Modifier.offset(x = (10).dp),
                    onClick = {
                        coroutineScope.launch {
                            task.uid?.let {
                                onGetMusicInfo(it)?.show()
                            }
                        }
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "${task.title ?: task.fileName}${stringResource(R.string.other_operations_button_suffix)}"
                    )
                }
            }

        }
    }
}

@Composable
fun DownloadPrompt(
    text: String,
    fontSize: TextUnit = 14.sp,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Text(
        text = text,
        fontSize = fontSize,
        color = color
    )
}

//todo 这个考虑使用其他方式
private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt()
    if (digitGroups >= units.size) digitGroups = units.size - 1
    return String.format(
        "%.1f %s",
        bytes / 1024.0.pow(digitGroups.toDouble()),
        units[digitGroups]
    )
}