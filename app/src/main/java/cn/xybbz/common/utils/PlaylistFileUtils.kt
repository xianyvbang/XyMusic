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

import cn.xybbz.R
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.constants.Constants
import cn.xybbz.entity.data.ext.joinToString
import cn.xybbz.localdata.enums.MusicDataTypeEnum

/**
 * 歌单备份文件工具类
 */
object PlaylistFileUtils {

    /**
     * 创建json数据,导出歌单
     * @param [applicationContext] 应用程序上下文
     * @param [db] DB
     * @param [key] 钥匙
     * @param [playlistId] 播放列表ID
     */
    suspend fun createTrackList(
        dataSourceManager: DataSourceManager,
        playlistId: String
    ): PlaylistParser.Playlist? {

        val playlist =
            dataSourceManager.selectAlbumInfoById(playlistId, MusicDataTypeEnum.PLAYLIST)
        val musicListResponse =
            dataSourceManager.getRemoteServerMusicListByAlbumOrPlaylist(
                startIndex = 0,
                pageSize = Constants.PAGE_SIZE_ALL,
                parentId = playlistId,
                dataType = MusicDataTypeEnum.PLAYLIST
            )
        //获取数据并组装数据
        return if (playlist == null) {
            MessageUtils.sendPopTipError(R.string.playlist_export_failed)
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
}