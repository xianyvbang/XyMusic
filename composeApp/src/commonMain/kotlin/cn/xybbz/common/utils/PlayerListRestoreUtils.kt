package cn.xybbz.common.utils

import cn.xybbz.assembler.MusicPlayAssembler
import cn.xybbz.download.database.DownloadDatabaseClient
import cn.xybbz.config.music.MusicPlayContext
import cn.xybbz.localdata.config.LocalDatabaseClient

object PlayerListRestoreUtils {

    /**
     * 按当前数据源恢复播放队列和播放器状态。
     * 这个方法必须在播放器控制器已经完成初始化后调用，否则恢复出来的数据可能不完整。
     */
    suspend fun restoreCurrentDataSourcePlayerList(
        db: LocalDatabaseClient,
        downloadDb: DownloadDatabaseClient,
        musicPlayContext: MusicPlayContext
    ) {
        val musicList = MusicPlayAssembler.attachFilePath(
            playMusicList = db.musicDao.selectPlayQueuePlayMusicList(),
            downloadDb = downloadDb
        ) ?: emptyList()
        if (musicList.isEmpty()) {
            return
        }

        val player = db.playerDao.selectPlayerByDataSource()
        musicPlayContext.initPlayList(
            musicList = musicList,
            player = player
        )
    }
}
