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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.ProgressIndicatorDefaults.drawStopIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.api.converter.jsonSerializer
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.common.utils.openFolderInSystemFileManager
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.config.image.rememberRawCoverUrls
import cn.xybbz.download.DownloaderManager
import cn.xybbz.download.database.data.XyDownload
import cn.xybbz.download.enums.DownloadStatus
import cn.xybbz.entity.data.music.OnMusicPlayParameter
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import cn.xybbz.localdata.enums.PlayerModeEnum
import cn.xybbz.platform.format.formatBytes
import cn.xybbz.router.AlbumInfo
import cn.xybbz.ui.components.AlertDialogObject
import cn.xybbz.ui.components.XyDialogIcon
import cn.xybbz.ui.components.XyDialogTone
import cn.xybbz.ui.components.JvmPageTitleBar
import cn.xybbz.ui.components.JvmSelectionAnimatedVisibility
import cn.xybbz.ui.components.JvmSelectionToggleButton
import cn.xybbz.ui.components.ScreenLazyColumn
import cn.xybbz.ui.components.SongTableColumns
import cn.xybbz.ui.components.rememberMusicArtistInfoLoader
import cn.xybbz.ui.components.rememberMusicArtistClickHandler
import cn.xybbz.ui.components.show
import cn.xybbz.ui.components.songTableItems
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.ext.jvmHoverDebounceClickable
import cn.xybbz.ui.icons.folder_open_24px
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyImage
import cn.xybbz.ui.xy.XyText
import cn.xybbz.ui.xy.XyTextSub
import cn.xybbz.ui.xy.rememberXyTextHoverColor
import cn.xybbz.viewmodel.DownloadViewModel
import cn.xybbz.viewmodel.LocalViewModel
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import xymusic.composeapp.generated.resources.*
import xymusic.composeapp.generated.resources.Res
import xymusic.composeapp.generated.resources.cancel_download
import xymusic.composeapp.generated.resources.confirm_delete_download
import xymusic.composeapp.generated.resources.delete_24px
import xymusic.composeapp.generated.resources.download_24px
import xymusic.composeapp.generated.resources.download_completed
import xymusic.composeapp.generated.resources.download_failed_with_reason
import xymusic.composeapp.generated.resources.download_status_queued
import xymusic.composeapp.generated.resources.local_and_download
import xymusic.composeapp.generated.resources.music_note_24px
import xymusic.composeapp.generated.resources.pause_24px
import xymusic.composeapp.generated.resources.remove_download_title
import xymusic.composeapp.generated.resources.tap_to_resume_download
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun JvmLocalDownloadScreen(
    initialTab: JvmLocalDownloadTab,
    downloadViewModel: DownloadViewModel = koinViewModel<DownloadViewModel>(),
    localViewModel: LocalViewModel = koinViewModel<LocalViewModel>(),
    dataSourceManager: DataSourceManager = koinInject(),
    downloaderManager: DownloaderManager = koinInject(),
) {
    val navigator = LocalNavigator.current
    val artistInfoLoader = rememberMusicArtistInfoLoader(dataSourceManager)
    val artistClickHandler = rememberMusicArtistClickHandler(
        artistList = artistInfoLoader.artistList,
        onLoadArtistInfos = artistInfoLoader.loadArtistInfos,
    )
    val allDownloadTasks by downloadViewModel.musicDownloadInfo.collectAsStateWithLifecycle()
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
    // 本地歌曲选择状态：选择与删除都以下载记录 id 为准，避免同名歌曲或重复记录串选
    var isSelectingLocal by remember { mutableStateOf(false) }
    var selectedLocalDownloadIds by remember { mutableStateOf(emptySet<Long>()) }
    // 本地已完成下载任务，按下载记录逐行绑定，同一首歌的多条记录也能单独选中
    val localDownloadTasks by localViewModel.downloadMusicTasks.collectAsStateWithLifecycle()
    // 本地歌曲行：下载记录 id 与还原出的歌曲信息一一对应，行下标与列表严格一致
    val localSongRows = remember(localDownloadTasks) {
        localDownloadTasks.mapNotNull { task ->
            task.toMusicOrNull()?.let { music -> LocalDownloadSongRow(task.id, music) }
        }
    }
    val localSongs = remember(localSongRows) { localSongRows.map { it.music } }
    // 当前本地列表全部下载 id，用于全选与全选状态判断
    val localDownloadIds = remember(localSongRows) { localSongRows.map { it.downloadId } }
    // 切换到下载列表时退出本地歌曲选择模式，避免选择状态残留
    LaunchedEffect(selectedTab) {
        if (selectedTab != JvmLocalDownloadTab.LocalSongs) {
            isSelectingLocal = false
            selectedLocalDownloadIds = emptySet()
        }
    }

    val removeDownloadTitle = stringResource(Res.string.remove_download_title)
    val confirmClearDownloads = stringResource(
        Res.string.confirm_delete_download,
        activeDownloadTasks.size
    )
    // 本地歌曲删除确认文案：明确提示会同步删除磁盘上的音乐文件
    val removeLocalMusicTitle = stringResource(Res.string.delete_local_music_title)
    val confirmDeleteLocalMusic = stringResource(
        Res.string.confirm_delete_local_music,
        selectedLocalDownloadIds.size
    )
    // 未选择歌曲时的提示文案，先在本层解析，避免在点击回调里调用组合函数
    val pleaseSelectText = stringResource(Res.string.please_select)
    // 按下载记录 id 切换选中状态，重复歌曲的不同记录互不影响
    val toggleLocalDownloadSelection: (Long) -> Unit = { downloadId ->
        selectedLocalDownloadIds = if (downloadId in selectedLocalDownloadIds) {
            selectedLocalDownloadIds - downloadId
        } else {
            selectedLocalDownloadIds + downloadId
        }
    }
    val isLocalSelectAll = localDownloadIds.isNotEmpty() &&
            selectedLocalDownloadIds.containsAll(localDownloadIds)
    val showDeleteLocalMusicDialog = {
        AlertDialogObject(
            title = removeLocalMusicTitle,
            body = confirmDeleteLocalMusic,
            tone = XyDialogTone.Danger,
            icon = XyDialogIcon.Trash,
            kicker = Res.string.dialog_kicker_danger,
            onConfirmation = {
                localViewModel.deleteLocalMusicByIds(selectedLocalDownloadIds.toList())
                isSelectingLocal = false
                selectedLocalDownloadIds = emptySet()
            },
            onDismissRequest = {},
        ).show()
    }

    // 打开文件所在位置：点击时再取实际生效的歌曲下载目录，保证设置变更后立即生效
    val openFolderFailedText = stringResource(Res.string.jvm_download_screen_text_10)
    val openDownloadFolder = {
        val folderPath = downloaderManager.config.usableFinalDirectory()
        if (!openFolderInSystemFileManager(folderPath)) {
            MessageUtils.sendPopTip(openFolderFailedText)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 标题、标签栏与下载工具栏固定在列表上方，滚动时始终可见
        JvmLocalDownloadHeader(
            modifier = Modifier.padding(horizontal = XyTheme.dimens.outerHorizontalPadding),
            selectedTab = selectedTab,
            localCount = localSongs.size,
            downloadingCount = activeDownloadTasks.size,
            isSelectingLocal = isSelectingLocal,
            onSelectTab = { selectedTab = it },
            onOpenFolder = openDownloadFolder,
            onToggleLocalSelection = {
                if (isSelectingLocal) {
                    isSelectingLocal = false
                    selectedLocalDownloadIds = emptySet()
                } else {
                    isSelectingLocal = true
                }
            },
            onResumeAll = { downloadViewModel.resumeDownloads(activeDownloadTasks) },
            onPauseAll = { downloadViewModel.pauseDownloads(activeDownloadTasks) },
            onClearAll = {
                if (activeDownloadTasks.isNotEmpty()) {
                    AlertDialogObject(
                        title = removeDownloadTitle,
                        body = confirmClearDownloads,
                        tone = XyDialogTone.Danger,
                        icon = XyDialogIcon.Trash,
                        kicker = Res.string.dialog_kicker_danger,
                        onConfirmation = {
                            downloadViewModel.deleteDownloads(activeDownloadTasks)
                        },
                        // 危险操作必须提供取消回调，动作区才会同时出现「取消」与「确定」
                        onDismissRequest = {},
                    ).show()
                }
            },
        )

        // 本地歌曲选择操作栏：展示已选数量，并提供全选、取消与删除本地文件；
        // 使用通用动画容器展开/收起，与艺术家/专辑详情页保持一致
        JvmSelectionAnimatedVisibility(
            visible = selectedTab == JvmLocalDownloadTab.LocalSongs && isSelectingLocal,
        ) {
            JvmLocalMusicSelectionBar(
                modifier = Modifier.padding(
                    start = XyTheme.dimens.outerHorizontalPadding,
                    end = XyTheme.dimens.outerHorizontalPadding,
                    top = 12.dp,
                ),
                selectedCount = selectedLocalDownloadIds.size,
                isSelectAll = isLocalSelectAll,
                canSelectAll = localDownloadIds.isNotEmpty(),
                onToggleSelectAll = {
                    selectedLocalDownloadIds = if (isLocalSelectAll) {
                        emptySet()
                    } else {
                        localDownloadIds.toSet()
                    }
                },
                onCancel = {
                    isSelectingLocal = false
                    selectedLocalDownloadIds = emptySet()
                },
                onDelete = {
                    if (selectedLocalDownloadIds.isEmpty()) {
                        MessageUtils.sendPopTip(pleaseSelectText)
                    } else {
                        showDeleteLocalMusicDialog()
                    }
                },
            )
        }

        ScreenLazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                horizontal = XyTheme.dimens.outerHorizontalPadding,
            ),
        ) {
            when (selectedTab) {
                JvmLocalDownloadTab.LocalSongs -> {
                    songTableItems(
                        tableKey = "local_download_local_music",
                        songs = localSongs,
                        // 选择模式下隐藏行内操作，改为展示选择列
                        columns = JvmLocalMusicTableColumns.copy(
                            showInlineActions = !isSelectingLocal,
                            showSelectionColumn = isSelectingLocal,
                        ),
                        ifFavorite = { music -> music.itemId in favoriteSet },
                        currentPlayingMusicIdFlow = currentPlayingMusicIdFlow,
                        // 行级选中状态：按行下标取对应下载记录 id，重复歌曲也不会串选
                        isSelectedAt = { index, _ ->
                            localSongRows.getOrNull(index)?.downloadId
                                ?.let { it in selectedLocalDownloadIds } == true
                        },
                        onSongClick = { index, music ->
                            if (isSelectingLocal) {
                                // 选择模式下点击整行等价于勾选/取消勾选当前下载记录
                                localSongRows.getOrNull(index)?.let { row ->
                                    toggleLocalDownloadSelection(row.downloadId)
                                }
                            } else {
                                localViewModel.musicList(
                                    OnMusicPlayParameter(
                                        musicId = music.itemId,
                                        albumId = music.album,
                                    ),
                                    downloadList = localSongs,
                                    playerModeEnum = PlayerModeEnum.SEQUENTIAL_PLAYBACK,
                                )
                            }
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
                        // 勾选列点击同样按行下标切换对应下载记录，支持批量选择删除
                        onSelectionClickAt = { index, _ ->
                            localSongRows.getOrNull(index)?.let { row ->
                                toggleLocalDownloadSelection(row.downloadId)
                            }
                        },
                        // 本地歌曲表头吸附固定在列表顶部
                        stickyTableHeader = true,
                    )
                }

                JvmLocalDownloadTab.Downloading -> {
                    // 下载列表表头同样吸附固定，自带页面背景避免下方行透出
                    stickyHeader(key = "download_table_header") {
                        JvmDownloadTableHeader(
                            modifier = Modifier.background(MaterialTheme.colorScheme.background),
                        )
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
}

@Composable
private fun JvmLocalDownloadHeader(
    modifier: Modifier = Modifier,
    selectedTab: JvmLocalDownloadTab,
    localCount: Int,
    downloadingCount: Int,
    isSelectingLocal: Boolean,
    onSelectTab: (JvmLocalDownloadTab) -> Unit,
    // 打开歌曲下载目录，本地歌曲与下载列表共用
    onOpenFolder: () -> Unit,
    onToggleLocalSelection: () -> Unit,
    onResumeAll: () -> Unit,
    onPauseAll: () -> Unit,
    onClearAll: () -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // 与设置页、连接管理页共用统一页头，左右边距由调用方提供，这里只补顶部间距
        JvmPageTitleBar(
            title = stringResource(Res.string.local_and_download),
            paddingValues = PaddingValues(top = XyTheme.dimens.outerVerticalPadding * 2),
        )
        Row(
            modifier = Modifier
                .padding(top = 18.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            JvmLocalDownloadTabButton(
                label = stringResource(Res.string.jvm_download_screen_text_01, localCount),
                selected = selectedTab == JvmLocalDownloadTab.LocalSongs,
                onClick = { onSelectTab(JvmLocalDownloadTab.LocalSongs) },
            )
            JvmLocalDownloadTabButton(
                label = stringResource(Res.string.jvm_download_screen_text_02, downloadingCount),
                selected = selectedTab == JvmLocalDownloadTab.Downloading,
                onClick = { onSelectTab(JvmLocalDownloadTab.Downloading) },
            )
            Spacer(modifier = Modifier.weight(1f))
            // 打开文件所在位置：两个标签页都直接定位到当前歌曲下载目录
            JvmDownloadToolbarButton(
                text = stringResource(Res.string.jvm_download_screen_text_09),
                onClick = onOpenFolder,
                imageVector = folder_open_24px,
            )
            // 本地歌曲标签提供选择入口，选择态切换为完成按钮
            if (selectedTab == JvmLocalDownloadTab.LocalSongs) {
                JvmSelectionToggleButton(
                    isSelecting = isSelectingLocal,
                    onClick = onToggleLocalSelection,
                )
            }
        }

        if (selectedTab == JvmLocalDownloadTab.Downloading) {
            Row(
                modifier = Modifier.padding(top = 22.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                JvmDownloadToolbarButton(
                    icon = Res.drawable.download_24px,
                    text = stringResource(Res.string.jvm_download_screen_text_03),
                    onClick = onResumeAll,
                )
                JvmDownloadToolbarButton(
                    icon = Res.drawable.pause_24px,
                    text = stringResource(Res.string.jvm_download_screen_text_04),
                    onClick = onPauseAll,
                )
                JvmDownloadToolbarButton(
                    icon = Res.drawable.delete_24px,
                    text = stringResource(Res.string.jvm_download_screen_text_05),
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
    val interactionSource = remember { MutableInteractionSource() }
    // 未选中标签悬停时文字切换为主题色，选中标签保持主题色
    val textColorState = rememberXyTextHoverColor(
        interactionSource = interactionSource,
        color = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface
        },
    )
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .jvmHoverDebounceClickable(
                interactionSource = interactionSource,
                onClick = onClick,
            )
            .padding(horizontal = 2.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            color = textColorState.color,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            maxLines = 1,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .width(26.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent),
        )
    }
}

@Composable
private fun JvmDownloadToolbarButton(
    text: String,
    onClick: () -> Unit,
    icon: DrawableResource? = null,
    // 资源图标不便复用时（如 ui 模块里的 ImageVector 图标）可直接传入矢量图标
    imageVector: ImageVector? = null,
) {
    Row(
        modifier = Modifier
            .height(JvmDownloadToolbarButtonHeight)
            .clip(RoundedCornerShape(JvmDownloadToolbarButtonHeight / 2))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.86f))
            .jvmHoverDebounceClickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when {
            icon != null -> Icon(
                painter = painterResource(icon),
                contentDescription = text,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(18.dp),
            )

            imageVector != null -> Icon(
                imageVector = imageVector,
                contentDescription = text,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(18.dp),
            )
        }
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp),
            maxLines = 1,
        )
    }
}

@Composable
private fun JvmDownloadTableHeader(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = XyTheme.dimens.innerHorizontalPadding,
                    vertical = XyTheme.dimens.outerVerticalPadding,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            JvmDownloadTableCell(stringResource(Res.string.jvm_download_screen_text_06), JvmDownloadSongColumnWidth, MaterialTheme.colorScheme.onSurfaceVariant)
            JvmDownloadTableSpacer(JvmDownloadActionsColumnWidth)
            JvmDownloadTableCell(stringResource(Res.string.jvm_download_screen_text_07), JvmDownloadProgressColumnWidth, MaterialTheme.colorScheme.onSurfaceVariant)
            JvmDownloadTableCell(
                stringResource(Res.string.size_num),
                JvmDownloadSizeColumnWidth,
                MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
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
    val rowBackgroundColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = if (hovered) 0.78f else 0.54f)

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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
            )
            XyTextSub(
                text = artistText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
                        MaterialTheme.colorScheme.surfaceContainerHighest,
                    )
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (coverUrl.isNullOrBlank()) {
            Icon(
                painter = painterResource(Res.drawable.music_note_24px),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
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
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.72f),
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
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
        DownloadStatus.DOWNLOADING -> stringResource(Res.string.jvm_download_screen_text_08)
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
        else -> MaterialTheme.colorScheme.onSurfaceVariant
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

/**
 * 本地歌曲行：绑定下载记录 id 与还原出的歌曲信息。
 *
 * 同一首歌可能对应多条下载记录，行级选择必须依赖下载 id，不能只按歌曲 id 匹配。
 */
private data class LocalDownloadSongRow(
    val downloadId: Long,
    val music: XyMusic,
)

/**
 * 本地歌曲选择操作栏。
 *
 * 展示已选数量，并提供全选、取消与批量删除本地文件入口。
 */
@Composable
private fun JvmLocalMusicSelectionBar(
    modifier: Modifier = Modifier,
    selectedCount: Int,
    isSelectAll: Boolean,
    canSelectAll: Boolean,
    onToggleSelectAll: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
) {
    val barShape = RoundedCornerShape(8.dp)
    val primaryColor = MaterialTheme.colorScheme.primary
    val countPrefix = stringResource(Res.string.selection_count_prefix)
    val countSuffix = stringResource(Res.string.selection_count_suffix)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(barShape)
            // 与列表选择操作栏保持同一套浅色主色底 + 主色描边样式
            .background(primaryColor.copy(alpha = 0.07f))
            .border(1.dp, primaryColor.copy(alpha = 0.30f), barShape)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "$countPrefix $selectedCount $countSuffix",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.weight(1f))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            JvmLocalSelectionTextButton(
                text = stringResource(
                    if (isSelectAll) Res.string.deselect_all else Res.string.select_all
                ),
                enabled = canSelectAll,
                onClick = onToggleSelectAll,
            )
            JvmLocalSelectionTextButton(
                text = stringResource(Res.string.cancel),
                onClick = onCancel,
            )
            // 删除按钮使用危险色，点击后还会二次确认提示会删除本地文件
            JvmLocalSelectionTextButton(
                text = stringResource(Res.string.delete_local_permanently),
                enabled = selectedCount > 0,
                danger = true,
                onClick = onDelete,
            )
        }
    }
}

/**
 * 本地歌曲选择操作栏里的文字按钮。
 *
 * @param text 按钮文案
 * @param enabled 是否可点击
 * @param danger 是否使用危险色，用于删除等不可逆操作
 * @param onClick 点击回调
 */
@Composable
private fun JvmLocalSelectionTextButton(
    text: String,
    enabled: Boolean = true,
    danger: Boolean = false,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.height(28.dp),
        enabled = enabled,
        shape = RoundedCornerShape(6.dp),
        contentPadding = PaddingValues(horizontal = 10.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (danger) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}
