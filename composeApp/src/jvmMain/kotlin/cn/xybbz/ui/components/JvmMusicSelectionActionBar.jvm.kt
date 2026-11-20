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

package cn.xybbz.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.api.converter.jsonSerializer
import cn.xybbz.common.enums.getDownloadType
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.config.music.MusicPlayContext
import cn.xybbz.config.select.SelectControl
import cn.xybbz.download.DownloaderManager
import cn.xybbz.download.core.DownloadRequest
import cn.xybbz.download.database.DownloadDatabaseClient
import cn.xybbz.download.enums.DownloadStatus
import cn.xybbz.localdata.config.LocalDatabaseClient
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import xymusic.composeapp.generated.resources.Res
import xymusic.composeapp.generated.resources.add_download_list
import xymusic.composeapp.generated.resources.add_to_playlist
import xymusic.composeapp.generated.resources.cancel
import xymusic.composeapp.generated.resources.delete_local_permanently
import xymusic.composeapp.generated.resources.delete_permanently
import xymusic.composeapp.generated.resources.deselect_all
import xymusic.composeapp.generated.resources.download
import xymusic.composeapp.generated.resources.downloaded
import xymusic.composeapp.generated.resources.music_remove_from_playlist
import xymusic.composeapp.generated.resources.play_selected
import xymusic.composeapp.generated.resources.please_select
import xymusic.composeapp.generated.resources.select_all
import xymusic.composeapp.generated.resources.selection_count_prefix
import xymusic.composeapp.generated.resources.selection_count_suffix
import xymusic.composeapp.generated.resources.unfavorite

/**
 * 音乐列表选择操作栏。
 *
 * 列表进入选择模式后展开在列表标题下方，左侧展示已选数量，右侧以文字按钮提供
 * 全选、取消以及批量操作。批量操作项与底部播放栏原有的选择操作保持一致，
 * 各音乐列表页面可直接复用该组件，避免批量操作长期分散在播放栏里。
 *
 * @param selectControl 选择状态控制器，提供选中数量与批量操作方法
 * @param allMusicIds 当前列表全部音乐 id，用于全选
 * @param dataSourceManager 数据源管理器，用于判断删除、下载能力
 * @param musicPlayContext 播放上下文，用于播放选中音乐
 * @param db 本地音乐数据库
 * @param downloadDb 下载数据库
 * @param musicController 播放控制器，用于取消收藏后同步收藏状态
 * @param downloaderManager 下载管理器，用于批量下载选中音乐
 */
@Composable
fun JvmMusicSelectionActionBar(
    selectControl: SelectControl,
    allMusicIds: () -> List<String>,
    dataSourceManager: DataSourceManager,
    musicPlayContext: MusicPlayContext,
    db: LocalDatabaseClient,
    downloadDb: DownloadDatabaseClient,
    musicController: MusicCommonController,
    downloaderManager: DownloaderManager,
    modifier: Modifier = Modifier,
) {
    // 选择状态：选中数量、全选标记以及批量操作是否可用都由它驱动
    val selectUiState by selectControl.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val pleaseSelect = stringResource(Res.string.please_select)

    // 批量下载选中音乐：沿用底部播放栏原有的入队逻辑，下载完成后退出选择模式
    val downloadSelected: () -> Unit = {
        coroutineScope.launch {
            val downloadTypes = getDownloadType(dataSourceManager.dataSourceType)
            val itemIds = selectControl.selectMusicIdList.toList()
            val mediaLibraryId = dataSourceManager.getConnectionId().toString()
            // 已下载完成的歌曲不再重复入队，只有从本地歌曲中删除后才允许重新下载
            val downloadedIds = downloadDb.downloadDao
                .getDataByUids(
                    musicIds = itemIds,
                    status = DownloadStatus.COMPLETED,
                    mediaLibraryId = mediaLibraryId,
                )
                .map { it.uid }
                .toSet()
            val musicList = db.musicDao.selectByIds(itemIds).filterNot { it.itemId in downloadedIds }
            val requests = musicList.map { musicData ->
                DownloadRequest(
                    url = musicData.downloadUrl,
                    fileName = musicData.name + "." + musicData.container,
                    fileSize = musicData.size ?: 0,
                    uid = musicData.itemId,
                    title = musicData.name,
                    type = downloadTypes.toString(),
                    cover = musicData.pic,
                    duration = musicData.runTimeTicks,
                    mediaLibraryId = mediaLibraryId,
                    data = jsonSerializer.encodeToString(musicData)
                )
            }
            if (requests.isNotEmpty()) {
                downloaderManager.enqueue(*requests.toTypedArray())
                MessageUtils.sendPopTip(Res.string.add_download_list)
            } else if (itemIds.isNotEmpty()) {
                // 选中歌曲全部已下载时给出提示，避免用户误以为点击无效
                MessageUtils.sendPopTip(Res.string.downloaded)
            }
        }.invokeOnCompletion { selectControl.dismiss() }
    }
    // 桌面端下载权限由平台实现决定，JVM 端会直接执行下载回调
    val downloadPermissionState = downloadPermission { downloadSelected() }

    val barShape = RoundedCornerShape(8.dp)
    val primaryColor = MaterialTheme.colorScheme.primary
    // 已选数量文案在本层先解析，组合字符串时只做样式拼接
    val countPrefix = stringResource(Res.string.selection_count_prefix)
    val countSuffix = stringResource(Res.string.selection_count_suffix)
    val countLabelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(barShape)
            // 浅色主色底 + 主色描边，与列表内容区分开，保持轻量不抢焦点
            .background(primaryColor.copy(alpha = 0.07f))
            .border(1.dp, primaryColor.copy(alpha = 0.30f), barShape)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = buildAnnotatedString {
                // 已选数量：数量本身使用主色加粗，便于快速识别
                withStyle(SpanStyle(color = countLabelColor)) {
                    append(countPrefix)
                    append(" ")
                }
                withStyle(SpanStyle(color = primaryColor, fontWeight = FontWeight.Bold)) {
                    append(selectUiState.selectedMusicIds.size.toString())
                }
                withStyle(SpanStyle(color = countLabelColor)) {
                    append(" ")
                    append(countSuffix)
                }
            },
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // 全选 / 全不选：列表存在数据时才可用
            JvmSelectionTextButton(
                text = if (selectUiState.isSelectAll) {
                    stringResource(Res.string.deselect_all)
                } else {
                    stringResource(Res.string.select_all)
                },
                enabled = allMusicIds().isNotEmpty(),
                onClick = {
                    selectControl.toggleSelectionAll(allMusicIds())
                },
            )

            // 取消：退出选择模式并清空选中
            JvmSelectionTextButton(
                text = stringResource(Res.string.cancel),
                onClick = { selectControl.dismiss() },
            )

            // 删除：仅数据源支持删除时展示，本地文件与远程资源文案不同
            if (dataSourceManager.getCanDelete()) {
                JvmSelectionTextButton(
                    text = if (selectUiState.ifLocal) {
                        stringResource(Res.string.delete_local_permanently)
                    } else {
                        stringResource(Res.string.delete_permanently)
                    },
                    enabled = selectUiState.ifEnableButton,
                    onClick = {
                        if (selectControl.ifSelectEmpty()) {
                            MessageUtils.sendPopTip(pleaseSelect)
                        } else {
                            coroutineScope.launch {
                                selectControl.onRemoveSelectListResource.invoke(
                                    dataSourceManager,
                                    coroutineScope
                                )
                            }
                        }
                    },
                )
            }

            // 播放选中：把选中音乐加入播放队列并开始播放
            JvmSelectionTextButton(
                text = stringResource(Res.string.play_selected),
                enabled = selectUiState.ifEnableButton,
                onClick = {
                    if (selectControl.ifSelectEmpty()) {
                        MessageUtils.sendPopTip(pleaseSelect)
                    } else {
                        coroutineScope.launch {
                            selectControl.onAddPlaySelect(
                                musicPlayContext,
                                db,
                                downloadDb,
                                dataSourceManager
                            )
                        }
                    }
                },
            )

            // 加入歌单：弹出歌单选择面板
            JvmSelectionTextButton(
                text = stringResource(Res.string.add_to_playlist),
                enabled = selectUiState.ifEnableButton,
                onClick = {
                    if (selectControl.ifSelectEmpty()) {
                        MessageUtils.sendPopTip(pleaseSelect)
                    } else {
                        selectControl.onAddPlaylistSelect()
                    }
                },
            )

            // 歌单场景下额外提供移除选中音乐
            if (selectUiState.ifPlaylist) {
                JvmSelectionTextButton(
                    text = stringResource(Res.string.music_remove_from_playlist),
                    enabled = selectUiState.ifEnableButton,
                    onClick = {
                        if (selectControl.ifSelectEmpty()) {
                            MessageUtils.sendPopTip(pleaseSelect)
                        } else {
                            selectControl.onRemovePlaylistMusic(
                                dataSourceManager,
                                coroutineScope
                            )
                        }
                    },
                )
            }

            // 取消收藏：批量取消选中音乐的收藏状态
            JvmSelectionTextButton(
                text = stringResource(Res.string.unfavorite),
                enabled = selectUiState.ifEnableButton,
                onClick = {
                    if (selectControl.ifSelectEmpty()) {
                        MessageUtils.sendPopTip(pleaseSelect)
                    } else {
                        selectControl.onRemoveFavorite(
                            dataSourceManager,
                            coroutineScope,
                            musicController
                        )
                    }
                },
            )

            // 下载：远程资源且数据源支持下载时展示，作为主操作按钮
            if (!selectUiState.ifLocal && dataSourceManager.getCanDownload()) {
                JvmSelectionTextButton(
                    text = stringResource(Res.string.download),
                    primary = true,
                    enabled = selectUiState.ifEnableButton,
                    onClick = {
                        if (selectControl.ifSelectEmpty()) {
                            MessageUtils.sendPopTip(pleaseSelect)
                        } else {
                            downloadPermissionState.launchMultiplePermissionRequest()
                        }
                    },
                )
            }
        }
    }
}

/**
 * 选择操作栏里的文字按钮。
 *
 * @param text 按钮文案
 * @param primary 是否为主操作按钮，主操作使用主色填充
 * @param enabled 是否可点击
 * @param onClick 点击回调
 */
@Composable
private fun JvmSelectionTextButton(
    text: String,
    primary: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val buttonShape = RoundedCornerShape(6.dp)
    val contentPadding = PaddingValues(horizontal = 10.dp)
    if (primary) {
        Button(
            onClick = onClick,
            modifier = Modifier.height(28.dp),
            enabled = enabled,
            shape = buttonShape,
            contentPadding = contentPadding,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier.height(28.dp),
            enabled = enabled,
            shape = buttonShape,
            contentPadding = contentPadding,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
        }
    }
}
