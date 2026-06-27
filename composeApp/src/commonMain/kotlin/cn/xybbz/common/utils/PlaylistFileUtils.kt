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

package cn.xybbz.common.utils

import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.constants.Constants
import cn.xybbz.entity.data.ext.joinToString
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import kotlinx.coroutines.CancellationException
import xymusic.composeapp.generated.resources.Res
import xymusic.composeapp.generated.resources.playlist_export_failed

/**
 * 歌单备份文件工具类
 */
object PlaylistFileUtils {

    /**
     * 创建json数据,导出歌单
     * @param [playlistId] 播放列表ID
     * @param [exportFailedTip] 业务层已知文件名和格式时传入的失败提示
     */
    suspend fun createTrackList(
        dataSourceManager: DataSourceManager,
        playlistId: String,
        exportFailedTip: String? = null
    ): PlaylistParser.Playlist? {

        val exportData = runCatching {
            val playlist =
                dataSourceManager.selectAlbumInfoById(playlistId, MusicDataTypeEnum.PLAYLIST)
            val musicListResponse =
                dataSourceManager.getRemoteServerMusicListByAlbumOrPlaylist(
                    startIndex = 0,
                    pageSize = Constants.PAGE_SIZE_ALL,
                    parentId = playlistId,
                    dataType = MusicDataTypeEnum.PLAYLIST
                )
            playlist to musicListResponse
        }.onFailure { error ->
            if (error is CancellationException) {
                throw error
            }
            Log.e(Constants.LOG_ERROR_PREFIX, "获取导出歌单数据失败", error)
            sendExportFailedTip(exportFailedTip)
        }.getOrNull() ?: return null

        val (playlist, musicListResponse) = exportData
        //获取数据并组装数据
        return if (playlist == null) {
            sendExportFailedTip(exportFailedTip)
            null
        } else {
            val musicTrackList = musicListResponse.items?.map {
                PlaylistParser.Track(
                    title = it.artists?.joinToString() + " - " + it.name,
                    duration = (it.runTimeTicks / 1000.0).toInt(),
                    path = it.path
                )
            } ?: emptyList()
            PlaylistParser.Playlist(musicTrackList, playlist.name)
        }
    }

    /**
     * 发送导出失败提示,优先使用带文件名和格式的动态文案。
     */
    private fun sendExportFailedTip(exportFailedTip: String?) {
        if (exportFailedTip.isNullOrBlank()) {
            MessageUtils.sendPopTipError(Res.string.playlist_export_failed)
        } else {
            MessageUtils.sendPopTipError(exportFailedTip)
        }
    }
}
