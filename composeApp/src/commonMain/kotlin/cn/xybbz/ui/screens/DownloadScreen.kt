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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import cn.xybbz.ui.xy.XyTextSubSmall
import cn.xybbz.viewmodel.DownloadViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic.composeapp.generated.resources.*
import xymusic.composeapp.generated.resources.Res
import xymusic.composeapp.generated.resources.arrow_back_24px
import xymusic.composeapp.generated.resources.cancel_24px
import xymusic.composeapp.generated.resources.cancel_download
import xymusic.composeapp.generated.resources.close
import xymusic.composeapp.generated.resources.close_24px
import xymusic.composeapp.generated.resources.confirm_delete_download
import xymusic.composeapp.generated.resources.delete_24px
import xymusic.composeapp.generated.resources.download_completed
import xymusic.composeapp.generated.resources.download_failed
import xymusic.composeapp.generated.resources.download_failed_with_reason
import xymusic.composeapp.generated.resources.download_list
import xymusic.composeapp.generated.resources.download_status_queued
import xymusic.composeapp.generated.resources.more_vert_24px
import xymusic.composeapp.generated.resources.open_selection_function
import xymusic.composeapp.generated.resources.open_settings_page_button
import xymusic.composeapp.generated.resources.other_operations_button_suffix
import xymusic.composeapp.generated.resources.pause_24px
import xymusic.composeapp.generated.resources.play_circle_24px
import xymusic.composeapp.generated.resources.playlist_add_check_24px
import xymusic.composeapp.generated.resources.remove_download_title
import xymusic.composeapp.generated.resources.return_home
import xymusic.composeapp.generated.resources.settings_24px
import xymusic.composeapp.generated.resources.tap_to_resume_download
import cn.xybbz.ui.xy.XyIconButton as IconButton

private val DownloadCardMinHeight = 112.dp
private val DownloadCardActionSize = 48.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadScreen(
    downloadViewModel: DownloadViewModel = koinViewModel<DownloadViewModel>()
) {

    val removeDownloadTitle = stringResource(Res.string.remove_download_title)

    val tasks by downloadViewModel.musicDownloadInfo.collectAsStateWithLifecycle()
    XyColumnScreen {
        MultiSelectTopAppEnd(
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
    val title = task.title ?: task.fileName
    val progress = task.downloadProgressFraction()
    val statusColor = downloadStatusColor(task.status)
    val statusContainerColor = downloadStatusContainerColor(task.status)
    val supportingText = downloadSupportingText(task)

    Card(
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (brush != null) Color.Transparent else backgroundColor
        ), modifier = Modifier
            .heightIn(min = DownloadCardMinHeight)
            .fillMaxWidth()
            .then(modifier)
            .combinedClickable(
                enabled = enabled,
                onClick = {
                    if (isMultiSelectMode) {
                        onItemClick()
                    } else {
                        if (task.status == DownloadStatus.DOWNLOADING || task.status == DownloadStatus.QUEUED) {
                            onPause()
                        } else if (task.status == DownloadStatus.PAUSED || task.status == DownloadStatus.FAILED) {
                            onResume()
                        }
                    }

                },
                onLongClick = onItemLongClick
            )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = XyTheme.dimens.innerHorizontalPadding,
                    vertical = XyTheme.dimens.outerVerticalPadding + 2.dp,
                )
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = title,
                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodyMedium,
                        overflow = TextOverflow.Ellipsis,
                        color = textColor
                    )
                    DownloadStatusPill(
                        text = downloadStatusText(task),
                        contentColor = statusColor,
                        containerColor = statusContainerColor,
                    )
                }

                Column(
                    Modifier
                        .fillMaxWidth()
                        .height(26.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp),
                        progress = { progress },
                        color = if (task.status == DownloadStatus.FAILED) {
                            MaterialTheme.colorScheme.error
                        } else {
                            primary
                        },
                        drawStopIndicator = {
                            drawStopIndicator(
                                drawScope = this,
                                stopSize = 0.dp,
                                color = primary,
                                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                            )
                        },
                    )
                    Text(
                        text = "${(progress * 100).toInt()}% · ${downloadSizeText(task)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (supportingText.isNotBlank()) {
                    Text(
                        text = supportingText,
                        fontSize = 12.sp,
                        color = if (task.status == DownloadStatus.FAILED) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            if (isMultiSelectMode) {
                IconButton(
                    modifier = Modifier.size(DownloadCardActionSize),
                    onClick = {
                        onItemClick()
                    },
                ) {
                    RadioButton(selected = isSelected, onClick = {
                        onItemClick()
                    })
                }
            } else {
                IconButton(
                    modifier = Modifier.size(DownloadCardActionSize),
                    onClick = {
                        coroutineScope.launch {
                            task.uid?.let {
                                onGetMusicInfo(it)?.show()
                            }
                        }
                    },
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.more_vert_24px),
                        contentDescription = "${title}${stringResource(Res.string.other_operations_button_suffix)}"
                    )
                }
            }

        }
    }
}

@Composable
private fun DownloadStatusPill(
    text: String,
    contentColor: Color,
    containerColor: Color,
) {
    Box(
        modifier = Modifier
            .background(
                color = containerColor,
                shape = RoundedCornerShape(999.dp),
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = contentColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
        )
    }
}

@Composable
private fun downloadStatusText(task: XyDownload): String {
    return when (task.status) {
        DownloadStatus.QUEUED -> stringResource(Res.string.download_status_queued)
        DownloadStatus.DOWNLOADING -> stringResource(Res.string.jvm_download_screen_text_08)
        DownloadStatus.PAUSED -> stringResource(Res.string.tap_to_resume_download)
        DownloadStatus.COMPLETED -> stringResource(Res.string.download_completed)
        DownloadStatus.FAILED -> stringResource(Res.string.download_failed)
        DownloadStatus.CANCEL -> stringResource(Res.string.cancel_download)
    }
}

@Composable
private fun downloadSupportingText(task: XyDownload): String {
    return when (task.status) {
        DownloadStatus.QUEUED -> stringResource(Res.string.download_status_queued)
        DownloadStatus.DOWNLOADING -> ""
        DownloadStatus.PAUSED -> stringResource(Res.string.tap_to_resume_download)
        DownloadStatus.COMPLETED -> stringResource(Res.string.download_completed)
        DownloadStatus.FAILED -> stringResource(
            Res.string.download_failed_with_reason,
            task.error ?: ""
        )
        DownloadStatus.CANCEL -> stringResource(Res.string.cancel_download)
    }
}

@Composable
private fun downloadStatusColor(status: DownloadStatus): Color {
    return when (status) {
        DownloadStatus.DOWNLOADING -> MaterialTheme.colorScheme.primary
        DownloadStatus.FAILED -> MaterialTheme.colorScheme.error
        DownloadStatus.COMPLETED -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

@Composable
private fun downloadStatusContainerColor(status: DownloadStatus): Color {
    return downloadStatusColor(status).copy(alpha = 0.12f)
}

private fun downloadSizeText(task: XyDownload): String {
    return if (task.totalBytes > 0) {
        "${formatBytes(task.downloadedBytes)} / ${formatBytes(task.totalBytes)}"
    } else {
        formatBytes(task.downloadedBytes)
    }
}

private fun XyDownload.downloadProgressFraction(): Float {
    return when {
        totalBytes > 0 -> progress.coerceIn(0f, 100f) / 100f
        status == DownloadStatus.COMPLETED -> 1f
        else -> 0f
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiSelectTopAppEnd(
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
                }, navigationIcon = {

                    IconButton(onClick = composeClick { navigator.goBack() }) {
                        Icon(
                            painter = painterResource(Res.drawable.arrow_back_24px),
                            contentDescription = stringResource(Res.string.return_home)
                        )
                    }
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

