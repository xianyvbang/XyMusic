/*
 *   XyMusic
 *   Copyright (C) 2023 xianyvbang
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package cn.xybbz.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.ProgressIndicatorDefaults.drawStopIndicator
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.xybbz.common.utils.formatBytes
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.download.database.data.XyDownload
import cn.xybbz.download.enums.DownloadStatus
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.router.Setting
import cn.xybbz.ui.components.AlertDialogObject
import cn.xybbz.ui.components.ScreenLazyColumn
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.TopAppBarTitle
import cn.xybbz.ui.components.XySelectAllComponent
import cn.xybbz.ui.components.show
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyRow
import cn.xybbz.ui.xy.XyText
import cn.xybbz.ui.xy.XyTextSubSmall
import cn.xybbz.viewmodel.DownloadViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.cancel_24px
import xymusic_kmp.composeapp.generated.resources.cancel_download
import xymusic_kmp.composeapp.generated.resources.close
import xymusic_kmp.composeapp.generated.resources.close_24px
import xymusic_kmp.composeapp.generated.resources.confirm_delete_download
import xymusic_kmp.composeapp.generated.resources.delete_24px
import xymusic_kmp.composeapp.generated.resources.download_completed
import xymusic_kmp.composeapp.generated.resources.download_failed_with_reason
import xymusic_kmp.composeapp.generated.resources.download_list
import xymusic_kmp.composeapp.generated.resources.download_status_queued
import xymusic_kmp.composeapp.generated.resources.more_vert_24px
import xymusic_kmp.composeapp.generated.resources.open_selection_function
import xymusic_kmp.composeapp.generated.resources.open_settings_page_button
import xymusic_kmp.composeapp.generated.resources.other_operations_button_suffix
import xymusic_kmp.composeapp.generated.resources.pause_24px
import xymusic_kmp.composeapp.generated.resources.play_circle_24px
import xymusic_kmp.composeapp.generated.resources.playlist_add_check_24px
import xymusic_kmp.composeapp.generated.resources.remove_download_title
import xymusic_kmp.composeapp.generated.resources.settings_24px
import xymusic_kmp.composeapp.generated.resources.tap_to_resume_download
import cn.xybbz.ui.xy.XyIconButton as IconButton

private val JvmDownloadTitleColumnWidth = 320.dp
private val JvmDownloadStatusColumnWidth = 116.dp
private val JvmDownloadProgressColumnWidth = 220.dp
private val JvmDownloadSizeColumnWidth = 180.dp
private val JvmDownloadActionsColumnWidth = 176.dp
private val JvmDownloadSelectionColumnWidth = 56.dp
private val JvmDownloadRowHeight = 62.dp
private val JvmDownloadActionButtonSize = 32.dp
private val JvmDownloadActionIconSize = 20.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JvmDownloadScreen(
    downloadViewModel: DownloadViewModel = koinViewModel<DownloadViewModel>()
) {

    val removeDownloadTitle = stringResource(Res.string.remove_download_title)

    val tasks by downloadViewModel.musicDownloadInfo.collectAsStateWithLifecycle()
    XyColumnScreen {
        JvmMultiSelectTopAppEnd(
            isMultiSelectMode = downloadViewModel.isMultiSelectMode,
            isSelectAll = downloadViewModel.isSelectAll,
            onExitMultiSelectMode = { downloadViewModel.exitMultiSelectMode() },
            onEnterMultiSelectMode = { downloadViewModel.enterMultiSelectMode() },
            onPause = { downloadViewModel.performBatchPause() },
            onResume = { downloadViewModel.performBatchResume() },
            onCancel = { downloadViewModel.performBatchCancel() },
            onDelete = {
                if (downloadViewModel.selectedTaskIds.isNotEmpty())
                    AlertDialogObject(
                        title = removeDownloadTitle,
                        content = {
                            XyTextSubSmall(
                                text = stringResource(
                                    Res.string.confirm_delete_download,
                                    downloadViewModel.selectedTaskIds.size
                                )
                            )
                        }, onConfirmation = {
                            downloadViewModel.performBatchDelete()
                        }, onDismissRequest = {
                            downloadViewModel.enterMultiSelectMode()
                        }, ifWarning = true
                    ).show()
            },
            onSelectAll = {
                downloadViewModel.toggleSelectionAll()
            }
        )

        ScreenLazyColumn(
            contentPadding = PaddingValues(
                horizontal = XyTheme.dimens.outerHorizontalPadding
            ),
        ) {
            item(key = "download_table_header") {
                JvmDownloadTableHeader(
                    isMultiSelectMode = downloadViewModel.isMultiSelectMode,
                )
            }
            items(tasks, key = { it.id }) { task ->
                JvmDownloadTableRow(
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
private fun JvmDownloadTableHeader(
    isMultiSelectMode: Boolean,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        XyRow(
            paddingValues = PaddingValues(
                horizontal = XyTheme.dimens.innerHorizontalPadding,
                vertical = XyTheme.dimens.outerVerticalPadding,
            )
        ) {
            JvmDownloadTableCell("标题", JvmDownloadTitleColumnWidth, desktopColors.textSecondary)
            JvmDownloadTableCell("状态", JvmDownloadStatusColumnWidth, desktopColors.textSecondary)
            JvmDownloadTableCell("进度", JvmDownloadProgressColumnWidth, desktopColors.textSecondary)
            JvmDownloadTableCell("大小", JvmDownloadSizeColumnWidth, desktopColors.textSecondary)
            if (isMultiSelectMode) {
                JvmDownloadTableSpacer(JvmDownloadSelectionColumnWidth)
            } else {
                JvmDownloadTableCell(
                    "操作",
                    JvmDownloadActionsColumnWidth,
                    desktopColors.textSecondary,
                    textAlign = TextAlign.Center,
                )
            }
        }
        HorizontalDivider(color = desktopColors.divider)
    }
}

@Composable
private fun JvmDownloadTableRow(
    task: XyDownload,
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
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val coroutineScope = rememberCoroutineScope()
    val rowBackgroundColor = if (hovered || isSelected) {
        desktopColors.bgHover
    } else {
        Color.Transparent
    }

    Row(
        modifier = Modifier
            .height(JvmDownloadRowHeight)
            .fillMaxWidth()
            .background(rowBackgroundColor, RoundedCornerShape(XyTheme.dimens.outerVerticalPadding / 2))
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    if (isMultiSelectMode) {
                        onItemClick()
                    }
                },
                onLongClick = onItemLongClick
            )
            .pointerHoverIcon(PointerIcon.Hand)
            .padding(
                horizontal = XyTheme.dimens.innerHorizontalPadding,
                vertical = XyTheme.dimens.outerVerticalPadding,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        JvmDownloadTitleCell(task = task)
        JvmDownloadStatusCell(task = task)
        JvmDownloadProgressCell(task = task)
        JvmDownloadTableCell(
            text = jvmDownloadSizeText(task),
            width = JvmDownloadSizeColumnWidth,
            color = desktopColors.textSecondary,
        )
        if (isMultiSelectMode) {
            Box(
                modifier = Modifier.width(JvmDownloadSelectionColumnWidth),
                contentAlignment = Alignment.Center,
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = onItemClick,
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
                )
            }
        } else {
            JvmDownloadActionsCell(
                task = task,
                onPause = onPause,
                onResume = onResume,
                onCancel = onCancel,
                onDelete = onDelete,
                onMore = {
                    coroutineScope.launch {
                        task.uid?.let {
                            onGetMusicInfo(it)?.show()
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun JvmDownloadTitleCell(task: XyDownload) {
    Column(
        modifier = Modifier.width(JvmDownloadTitleColumnWidth),
        verticalArrangement = Arrangement.Center,
    ) {
        XyText(
            text = task.title ?: task.fileName,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
            color = desktopColors.textPrimary,
        )
        XyTextSubSmall(
            text = task.fileName,
            maxLines = 1,
            color = desktopColors.textSecondary,
        )
    }
}

@Composable
private fun JvmDownloadStatusCell(task: XyDownload) {
    val statusColor = jvmDownloadStatusColor(task.status)
    Box(
        modifier = Modifier.width(JvmDownloadStatusColumnWidth),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = jvmDownloadStatusText(task),
            color = statusColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
            ),
        )
    }
}

@Composable
private fun JvmDownloadProgressCell(task: XyDownload) {
    val primary = MaterialTheme.colorScheme.primary
    val progress = task.progressFraction()

    Column(
        modifier = Modifier.width(JvmDownloadProgressColumnWidth),
        verticalArrangement = Arrangement.Center,
    ) {
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            progress = { progress },
            drawStopIndicator = {
                drawStopIndicator(
                    drawScope = this,
                    stopSize = 0.dp,
                    color = primary,
                    strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                )
            },
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${(progress * 100).toInt()}%",
                color = desktopColors.textSecondary,
                maxLines = 1,
                style = MaterialTheme.typography.labelSmall,
            )
            if (task.status == DownloadStatus.FAILED && !task.error.isNullOrBlank()) {
                Text(
                    text = task.error.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Composable
private fun JvmDownloadActionsCell(
    task: XyDownload,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    onMore: () -> Unit,
) {
    Row(
        modifier = Modifier.width(JvmDownloadActionsColumnWidth),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when (task.status) {
            DownloadStatus.DOWNLOADING,
            DownloadStatus.QUEUED -> {
                JvmDownloadActionButton(
                    icon = Res.drawable.pause_24px,
                    contentDescription = "Pause download",
                    onClick = onPause,
                )
            }

            DownloadStatus.PAUSED,
            DownloadStatus.FAILED -> {
                JvmDownloadActionButton(
                    icon = Res.drawable.play_circle_24px,
                    contentDescription = "Resume download",
                    onClick = onResume,
                )
            }

            DownloadStatus.COMPLETED,
            DownloadStatus.CANCEL -> {
                Spacer(modifier = Modifier.size(JvmDownloadActionButtonSize))
            }
        }

        if (task.status != DownloadStatus.COMPLETED && task.status != DownloadStatus.CANCEL) {
            JvmDownloadActionButton(
                icon = Res.drawable.cancel_24px,
                contentDescription = "Cancel download",
                onClick = onCancel,
            )
        } else {
            Spacer(modifier = Modifier.size(JvmDownloadActionButtonSize))
        }

        if (task.status == DownloadStatus.COMPLETED ||
            task.status == DownloadStatus.CANCEL ||
            task.status == DownloadStatus.FAILED
        ) {
            JvmDownloadActionButton(
                icon = Res.drawable.delete_24px,
                contentDescription = "Delete download",
                onClick = onDelete,
            )
        } else {
            Spacer(modifier = Modifier.size(JvmDownloadActionButtonSize))
        }

        JvmDownloadActionButton(
            icon = Res.drawable.more_vert_24px,
            contentDescription = "${task.title ?: task.fileName}${stringResource(Res.string.other_operations_button_suffix)}",
            onClick = onMore,
        )
    }
}

@Composable
private fun JvmDownloadActionButton(
    icon: DrawableResource,
    contentDescription: String,
    onClick: () -> Unit,
) {
    IconButton(
        modifier = Modifier
            .size(JvmDownloadActionButtonSize)
            .pointerHoverIcon(PointerIcon.Hand),
        onClick = composeClick { onClick() },
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = contentDescription,
            modifier = Modifier.size(JvmDownloadActionIconSize),
            tint = desktopColors.textSecondary,
        )
    }
}

@Composable
private fun JvmDownloadTableCell(
    text: String,
    width: Dp,
    color: Color,
    textAlign: TextAlign = TextAlign.Start,
) {
    Box(
        modifier = Modifier
            .width(width)
            .fillMaxHeight(),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = text,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = textAlign,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
        )
    }
}

@Composable
private fun JvmDownloadTableSpacer(width: Dp) {
    Spacer(modifier = Modifier.width(width))
}

@Composable
private fun jvmDownloadStatusText(task: XyDownload): String {
    return when (task.status) {
        DownloadStatus.QUEUED -> stringResource(Res.string.download_status_queued)
        DownloadStatus.DOWNLOADING -> "下载中"
        DownloadStatus.PAUSED -> stringResource(Res.string.tap_to_resume_download)
        DownloadStatus.COMPLETED -> stringResource(Res.string.download_completed)
        DownloadStatus.FAILED -> stringResource(Res.string.download_failed_with_reason, task.error ?: "")
        DownloadStatus.CANCEL -> stringResource(Res.string.cancel_download)
    }
}

@Composable
private fun jvmDownloadStatusColor(status: DownloadStatus): Color {
    return when (status) {
        DownloadStatus.DOWNLOADING -> MaterialTheme.colorScheme.primary
        DownloadStatus.FAILED -> MaterialTheme.colorScheme.error
        DownloadStatus.COMPLETED -> MaterialTheme.colorScheme.tertiary
        else -> desktopColors.textSecondary
    }
}

private fun jvmDownloadSizeText(task: XyDownload): String {
    return if (task.totalBytes > 0) {
        "${formatBytes(task.downloadedBytes)} / ${formatBytes(task.totalBytes)}"
    } else {
        formatBytes(task.downloadedBytes)
    }
}

private fun XyDownload.progressFraction(): Float {
    return when {
        totalBytes > 0 -> progress.coerceIn(0f, 100f) / 100f
        status == DownloadStatus.COMPLETED -> 1f
        else -> 0f
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JvmMultiSelectTopAppEnd(
    isMultiSelectMode: Boolean,
    isSelectAll: Boolean,
    onExitMultiSelectMode: () -> Unit,
    onEnterMultiSelectMode: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    onSelectAll: () -> Unit
) {
    val navigator = LocalNavigator.current

    AnimatedContent(isMultiSelectMode, label = "animated content") {
        if (it) {
            TopAppBarComponent(
                title = {},
                navigationIcon = {
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = composeClick {
                            onExitMultiSelectMode()
                        }) {
                            Icon(
                                painter = painterResource(Res.drawable.close_24px),
                                contentDescription = stringResource(Res.string.close)
                            )
                        }
                        XySelectAllComponent(
                            isSelectAll = isSelectAll,
                            horizontalArrangement = Arrangement.End,
                            onSelectAll = onSelectAll
                        )
                    }


                }, actions = {
                    AnimatedVisibility(isMultiSelectMode) {
                        Row() {
                            IconButton(onClick = composeClick(onClick = onPause)) {
                                Icon(
                                    painter = painterResource(Res.drawable.pause_24px),
                                    contentDescription = "Pause selected"
                                )
                            }
                            IconButton(onClick = composeClick(onClick = onResume)) {
                                Icon(
                                    painter = painterResource(Res.drawable.play_circle_24px),
                                    contentDescription = "Resume selected"
                                )
                            }
                            IconButton(onClick = composeClick(onClick = onCancel)) {
                                Icon(
                                    painter = painterResource(Res.drawable.cancel_24px),
                                    contentDescription = "Cancel selected"
                                )
                            }

                            IconButton(onClick = composeClick(onClick = onDelete)) {
                                Icon(
                                    painter = painterResource(Res.drawable.delete_24px),
                                    contentDescription = "Cancel selected"
                                )
                            }
                        }
                    }
                })

        } else {
            TopAppBarComponent(
                title = {
                    TopAppBarTitle(
                        title = stringResource(Res.string.download_list)
                    )
                }, actions = {

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = {
                            onEnterMultiSelectMode()
                        }) {
                            Icon(
                                painter = painterResource(Res.drawable.playlist_add_check_24px),
                                contentDescription = stringResource(Res.string.open_selection_function)
                            )
                        }

                        IconButton(onClick = {
                            navigator.navigate(Setting)
                        }) {
                            Icon(
                                painter = painterResource(Res.drawable.settings_24px),
                                contentDescription = stringResource(Res.string.open_settings_page_button)
                            )
                        }
                    }

                })
        }
    }
}



