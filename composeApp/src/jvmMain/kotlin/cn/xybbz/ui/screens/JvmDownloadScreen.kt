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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.ProgressIndicatorDefaults.drawStopIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.xybbz.api.converter.jsonSerializer
import cn.xybbz.common.utils.formatBytes
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.config.image.rememberRawCoverUrls
import cn.xybbz.download.database.data.XyDownload
import cn.xybbz.download.enums.DownloadStatus
import cn.xybbz.entity.data.music.OnMusicPlayParameter
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import cn.xybbz.localdata.enums.PlayerModeEnum
import cn.xybbz.router.AlbumInfo
import cn.xybbz.ui.components.AlertDialogObject
import cn.xybbz.ui.components.ScreenLazyColumn
import cn.xybbz.ui.components.SongTableColumns
import cn.xybbz.ui.components.rememberMusicArtistClickHandler
import cn.xybbz.ui.components.show
import cn.xybbz.ui.components.songTableItems
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.ext.jvmHoverDebounceClickable
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyImage
import cn.xybbz.ui.xy.XyText
import cn.xybbz.ui.xy.XyTextLarge
import cn.xybbz.ui.xy.XyTextSub
import cn.xybbz.ui.xy.XyTextSubSmall
import cn.xybbz.viewmodel.DownloadViewModel
import cn.xybbz.viewmodel.LocalViewModel
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.cancel_download
import xymusic_kmp.composeapp.generated.resources.confirm_delete_download
import xymusic_kmp.composeapp.generated.resources.delete_24px
import xymusic_kmp.composeapp.generated.resources.download_24px
import xymusic_kmp.composeapp.generated.resources.download_completed
import xymusic_kmp.composeapp.generated.resources.download_failed_with_reason
import xymusic_kmp.composeapp.generated.resources.download_status_queued
import xymusic_kmp.composeapp.generated.resources.local_and_download
import xymusic_kmp.composeapp.generated.resources.music_note_24px
import xymusic_kmp.composeapp.generated.resources.pause_24px
import xymusic_kmp.composeapp.generated.resources.remove_download_title
import xymusic_kmp.composeapp.generated.resources.tap_to_resume_download
import cn.xybbz.ui.xy.XyIconButton as IconButton

private val JvmDownloadSongColumnWidth = 420.dp
private val JvmDownloadActionsColumnWidth = 112.dp
private val JvmDownloadProgressColumnWidth = 300.dp
private val JvmDownloadSizeColumnWidth = 120.dp
private val JvmDownloadRowHeight = 58.dp
private val JvmDownloadCoverSize = 38.dp
private val JvmDownloadActionButtonSize = 30.dp
private val JvmDownloadActionIconSize = 20.dp
private val JvmDownloadToolbarButtonHeight = 32.dp

private val JvmLocalMusicTableColumns = SongTableColumns(
    showFavoriteColumn = true,
    showInlineActions = true,
    showInlineDownloadButton = false,
    showAlbumColumn = true,
    showMetaColumn = false,
)

internal enum class JvmLocalDownloadTab {
    LocalSongs,
    Downloading,
}

@Composable
fun JvmDownloadScreen(
    downloadViewModel: DownloadViewModel = koinViewModel<DownloadViewModel>(),
    localViewModel: LocalViewModel = koinViewModel<LocalViewModel>(),
) {
    JvmLocalDownloadScreen(
        initialTab = JvmLocalDownloadTab.Downloading,
        downloadViewModel = downloadViewModel,
        localViewModel = localViewModel,
    )
}

@Composable
internal fun JvmLocalDownloadScreen(
    initialTab: JvmLocalDownloadTab,
    downloadViewModel: DownloadViewModel = koinViewModel<DownloadViewModel>(),
    localViewModel: LocalViewModel = koinViewModel<LocalViewModel>(),
) {
    val navigator = LocalNavigator.current
    val artistClickHandler = rememberMusicArtistClickHandler()
    val allDownloadTasks by downloadViewModel.musicDownloadInfo.collectAsStateWithLifecycle()
    val localSongs by localViewModel.musicDownloadInfo.collectAsStateWithLifecycle()
    val favoriteSet by localViewModel.favoriteSet.collectAsStateWithLifecycle(emptyList())
    val activeDownloadTasks = remember(allDownloadTasks) {
        allDownloadTasks.filter(::isActiveDownloadTask)
    }
    val currentPlayingMusicIdFlow = remember(localViewModel) {
        localViewModel.musicController.musicInfoFlow.map { musicInfo ->
            musicInfo?.itemId
        }
    }
    var selectedTab by remember(initialTab) {
        mutableStateOf(initialTab)
    }

    val removeDownloadTitle = stringResource(Res.string.remove_download_title)
    val confirmClearDownloads = stringResource(
        Res.string.confirm_delete_download,
        activeDownloadTasks.size
    )

    ScreenLazyColumn(
        contentPadding = PaddingValues(
            horizontal = XyTheme.dimens.outerHorizontalPadding,
        ),
    ) {
        item(key = "local_download_header") {
            JvmLocalDownloadHeader(
                selectedTab = selectedTab,
                localCount = localSongs.size,
                downloadingCount = activeDownloadTasks.size,
                onSelectTab = { selectedTab = it },
                onResumeAll = { downloadViewModel.resumeDownloads(activeDownloadTasks) },
                onPauseAll = { downloadViewModel.pauseDownloads(activeDownloadTasks) },
                onClearAll = {
                    if (activeDownloadTasks.isNotEmpty()) {
                        AlertDialogObject(
                            title = removeDownloadTitle,
                            content = {
                                XyTextSubSmall(text = confirmClearDownloads)
                            },
                            onConfirmation = {
                                downloadViewModel.deleteDownloads(activeDownloadTasks)
                            },
                            ifWarning = true,
                        ).show()
                    }
                },
            )
        }

        when (selectedTab) {
            JvmLocalDownloadTab.LocalSongs -> {
                songTableItems(
                    tableKey = "local_download_local_music",
                    songs = localSongs,
                    columns = JvmLocalMusicTableColumns,
                    ifFavorite = { music -> music.itemId in favoriteSet },
                    currentPlayingMusicIdFlow = currentPlayingMusicIdFlow,
                    onSongClick = { _, music ->
                        localViewModel.musicList(
                            OnMusicPlayParameter(
                                musicId = music.itemId,
                                albumId = music.album,
                            ),
                            downloadList = localSongs,
                            playerModeEnum = PlayerModeEnum.SEQUENTIAL_PLAYBACK,
                        )
                    },
                    onOpenArtist = artistClickHandler::openMusicArtists,
                    onOpenAlbum = { music ->
                        if (music.album.isNotBlank()) {
                            navigator.navigate(
                                AlbumInfo(
                                    music.album,
                                    MusicDataTypeEnum.ALBUM,
                                )
                            )
                        }
                    },
                    onFavoriteClick = { music ->
                        localViewModel.musicController.invokingOnFavorite(music.itemId)
                    },
                    onDownloadClick = {},
                    onMoreClick = { music ->
                        music.show()
                    },
                )
            }

            JvmLocalDownloadTab.Downloading -> {
                item(key = "download_table_header") {
                    JvmDownloadTableHeader()
                }
                items(activeDownloadTasks, key = { it.id }) { task ->
                    JvmDownloadTableRow(
                        task = task,
                        onPause = { downloadViewModel.pauseDownload(task.id) },
                        onResume = { downloadViewModel.resumeDownload(task.id) },
                        onDelete = { downloadViewModel.deleteDownload(task.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun JvmLocalDownloadHeader(
    selectedTab: JvmLocalDownloadTab,
    localCount: Int,
    downloadingCount: Int,
    onSelectTab: (JvmLocalDownloadTab) -> Unit,
    onResumeAll: () -> Unit,
    onPauseAll: () -> Unit,
    onClearAll: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = XyTheme.dimens.outerVerticalPadding),
    ) {
        XyTextLarge(
            text = stringResource(Res.string.local_and_download),
            color = desktopColors.textPrimary,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 30.sp),
        )
        Row(
            modifier = Modifier.padding(top = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            JvmLocalDownloadTabButton(
                label = "本地歌曲$localCount",
                selected = selectedTab == JvmLocalDownloadTab.LocalSongs,
                onClick = { onSelectTab(JvmLocalDownloadTab.LocalSongs) },
            )
            JvmLocalDownloadTabButton(
                label = "正在下载$downloadingCount",
                selected = selectedTab == JvmLocalDownloadTab.Downloading,
                onClick = { onSelectTab(JvmLocalDownloadTab.Downloading) },
            )
        }

        if (selectedTab == JvmLocalDownloadTab.Downloading) {
            Row(
                modifier = Modifier.padding(top = 22.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                JvmDownloadToolbarButton(
                    icon = Res.drawable.download_24px,
                    text = "开始",
                    onClick = onResumeAll,
                )
                JvmDownloadToolbarButton(
                    icon = Res.drawable.pause_24px,
                    text = "暂停",
                    onClick = onPauseAll,
                )
                JvmDownloadToolbarButton(
                    icon = Res.drawable.delete_24px,
                    text = "清空",
                    onClick = onClearAll,
                )
            }
        } else {
            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

@Composable
private fun JvmLocalDownloadTabButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .jvmHoverDebounceClickable(onClick = onClick)
            .padding(horizontal = 2.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            color = if (selected) desktopColors.theme else desktopColors.textPrimary,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            maxLines = 1,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .width(26.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (selected) desktopColors.theme else Color.Transparent),
        )
    }
}

@Composable
private fun JvmDownloadToolbarButton(
    icon: DrawableResource,
    text: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .height(JvmDownloadToolbarButtonHeight)
            .clip(RoundedCornerShape(JvmDownloadToolbarButtonHeight / 2))
            .background(desktopColors.bgHover.copy(alpha = 0.86f))
            .jvmHoverDebounceClickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = text,
            tint = desktopColors.textPrimary,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = text,
            color = desktopColors.textPrimary,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp),
            maxLines = 1,
        )
    }
}

@Composable
private fun JvmDownloadTableHeader() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = XyTheme.dimens.innerHorizontalPadding,
                    vertical = XyTheme.dimens.outerVerticalPadding,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            JvmDownloadTableCell("歌名/歌手", JvmDownloadSongColumnWidth, desktopColors.textSecondary)
            JvmDownloadTableSpacer(JvmDownloadActionsColumnWidth)
            JvmDownloadTableCell("进度", JvmDownloadProgressColumnWidth, desktopColors.textSecondary)
            JvmDownloadTableCell(
                "大小",
                JvmDownloadSizeColumnWidth,
                desktopColors.textSecondary,
                textAlign = TextAlign.End,
            )
        }
        HorizontalDivider(color = desktopColors.divider.copy(alpha = 0.45f))
    }
}

@Composable
private fun JvmDownloadTableRow(
    task: XyDownload,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onDelete: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val rowBackgroundColor = desktopColors.bgHover.copy(alpha = if (hovered) 0.78f else 0.54f)

    Row(
        modifier = Modifier
            .height(JvmDownloadRowHeight)
            .fillMaxWidth()
            .clip(RoundedCornerShape(XyTheme.dimens.outerVerticalPadding / 2))
            .background(rowBackgroundColor)
            .jvmHoverDebounceClickable(
                interactionSource = interactionSource,
                indication = null,
            )
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {},
                onLongClick = {},
            )
            .padding(
                horizontal = XyTheme.dimens.innerHorizontalPadding,
                vertical = XyTheme.dimens.outerVerticalPadding / 2,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        JvmDownloadTitleCell(task = task)
        JvmDownloadActionsCell(
            task = task,
            onPause = onPause,
            onResume = onResume,
            onDelete = onDelete,
        )
        JvmDownloadProgressCell(task = task)
        JvmDownloadTableCell(
            text = jvmDownloadSizeText(task),
            width = JvmDownloadSizeColumnWidth,
            color = desktopColors.textSecondary,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun JvmDownloadTitleCell(task: XyDownload) {
    val music = remember(task.data) { task.toMusicOrNull() }
    val artistText = music?.artists
        ?.joinToString()
        ?.takeIf { it.isNotBlank() }
        ?: task.fileName.substringBeforeLast('.', missingDelimiterValue = "")
    val title = task.title
        ?: music?.name
        ?: task.fileName.substringBeforeLast('.', missingDelimiterValue = task.fileName)
    val coverUrl = task.cover ?: music?.pic

    Row(
        modifier = Modifier.width(JvmDownloadSongColumnWidth),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        JvmDownloadCover(coverUrl = coverUrl)
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = title,
                color = desktopColors.textPrimary,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
            )
            XyTextSub(
                text = artistText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = desktopColors.textSecondary,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
            )
        }
    }
}

@Composable
private fun JvmDownloadCover(coverUrl: String?) {
    val coverUrls = rememberRawCoverUrls(coverUrl)

    Box(
        modifier = Modifier
            .size(JvmDownloadCoverSize)
            .clip(RoundedCornerShape(4.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        desktopColors.theme.copy(alpha = 0.75f),
                        desktopColors.bgHover,
                    )
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (coverUrl.isNullOrBlank()) {
            Icon(
                painter = painterResource(Res.drawable.music_note_24px),
                contentDescription = null,
                tint = desktopColors.textPrimary,
                modifier = Modifier.size(20.dp),
            )
        } else {
            XyImage(
                modifier = Modifier.matchParentSize(),
                model = coverUrls.primaryUrl,
                backModel = coverUrls.fallbackUrl,
                contentDescription = null,
            )
        }
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
            color = desktopColors.theme,
            trackColor = desktopColors.bgHover.copy(alpha = 0.72f),
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
            modifier = Modifier.padding(top = 7.dp),
            text = jvmDownloadStatusText(task),
            color = jvmDownloadStatusColor(task.status),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
        )
    }
}

@Composable
private fun JvmDownloadActionsCell(
    task: XyDownload,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier.width(JvmDownloadActionsColumnWidth),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
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
                    icon = Res.drawable.download_24px,
                    contentDescription = "Resume download",
                    onClick = onResume,
                )
            }

            DownloadStatus.COMPLETED,
            DownloadStatus.CANCEL -> {
                Spacer(modifier = Modifier.size(JvmDownloadActionButtonSize))
            }
        }

        JvmDownloadActionButton(
            icon = Res.drawable.delete_24px,
            contentDescription = "Delete download",
            onClick = onDelete,
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
            .size(JvmDownloadActionButtonSize),
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
    val total = task.totalBytes.takeIf { it > 0 } ?: task.fileSize
    return if (total > 0) {
        formatBytes(total)
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

private fun XyDownload.toMusicOrNull(): XyMusic? {
    return data?.let {
        runCatching {
            jsonSerializer.decodeFromString<XyMusic>(it)
        }.getOrNull()
    }
}

private fun isActiveDownloadTask(task: XyDownload): Boolean {
    return when (task.status) {
        DownloadStatus.COMPLETED,
        DownloadStatus.CANCEL -> false

        DownloadStatus.QUEUED,
        DownloadStatus.DOWNLOADING,
        DownloadStatus.PAUSED,
        DownloadStatus.FAILED -> true
    }
}
