package cn.xybbz.ui.screens

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.ProgressIndicatorDefaults.drawStopIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.xybbz.R
import cn.xybbz.compositionLocal.LocalNavController
import cn.xybbz.localdata.data.download.XyDownload
import cn.xybbz.localdata.enums.DownloadStatus
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.LazyColumnNotComponent
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.viewmodel.DownloadViewModel
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
    val favoriteList by downloadViewModel.favoriteRepository.favoriteMap.collectAsState()
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

        LazyColumnNotComponent() {
            items(tasks, key = { it.id }) { task ->

                DownloadItem(
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
                    onDelete = { downloadViewModel.deleteDownload(task.id) })
            }
        }
    }
}

@Composable
fun DownloadItem(
    task: XyDownload,
    isMultiSelectMode: Boolean,
    isSelected: Boolean,
    onItemClick: () -> Unit,
    onItemLongClick: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary

    ListItem(
        modifier = Modifier.combinedClickable(
            onClick = onItemClick,
            onLongClick = onItemLongClick
        ),
        colors = ListItemDefaults.colors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        ),
        headlineContent = {
            Text(
                text = task.title ?: task.fileName,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                style = MaterialTheme.typography.bodySmall,
                overflow = TextOverflow.Ellipsis
            )
        }, supportingContent = {
            Column() {
                Text("Status: ${task.status.name}", fontSize = 12.sp)
                Spacer(modifier = Modifier.height(XyTheme.dimens.outerVerticalPadding))
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
                Spacer(modifier = Modifier.height(XyTheme.dimens.outerVerticalPadding))
                Text(
                    "${formatBytes(task.downloadedBytes)} / ${formatBytes(task.totalBytes)}",
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.End)
                )
                Spacer(modifier = Modifier.height(XyTheme.dimens.outerVerticalPadding))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (!isMultiSelectMode) {
                        if (task.status == DownloadStatus.DOWNLOADING || task.status == DownloadStatus.QUEUED) {
                            IconButton(onClick = composeClick { onPause() }) {
                                Icon(
                                    painterResource(R.drawable.svg_notification_pause),
                                    contentDescription = "Pause"
                                )
                            }
                        }
                        if (task.status == DownloadStatus.PAUSED) {
                            IconButton(onClick = onResume) {
                                Icon(
                                    painterResource(R.drawable.svg_notification_play),
                                    contentDescription = "Resume"
                                )
                            }
                        }
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
                    }
                }

            }

        })
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