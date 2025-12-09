package cn.xybbz.common.utils

import cn.xybbz.R
import cn.xybbz.api.client.IDataSourceManager
import cn.xybbz.common.constants.Constants
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
        dataSourceManager: IDataSourceManager,
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
                    title = it.artists + " - " + it.name,
                    duration = (it.runTimeTicks / 1000.0).toInt(),
                    path = it.path
                )
            } ?: emptyList()
            PlaylistParser.Playlist(musicTrackList, playlist.name)
        }
    }
}