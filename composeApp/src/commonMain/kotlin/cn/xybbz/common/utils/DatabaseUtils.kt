package cn.xybbz.common.utils

import cn.xybbz.database.withTransaction
import cn.xybbz.download.DownloaderManager
import cn.xybbz.download.database.DownloadDatabaseClient
import cn.xybbz.localdata.config.LocalDatabaseClient

/**
 * 通用操作数据库方法
 */
object DatabaseUtils {

    /**
     * 清空指定连接的数据
     * todo 清除新增的几个关联表
     */
    suspend fun clearDatabaseByConnectionConfig(
        db: LocalDatabaseClient,
        connectionId: Long
    ) {
        db.withTransaction {
            db.musicDao.removeByConnectionId(connectionId)
            db.albumDao.removeByConnectionIdWithReferences(connectionId)
            db.artistDao.removeByConnectionIdWithReferences(connectionId)
            db.progressDao.removeByConnectionId(connectionId)
            db.enableProgressDao.removeByConnectionId(connectionId)
            db.playerDao.removeByConnectionId(connectionId)
            db.libraryDao.removeByConnectionId(connectionId)
            db.genreDao.removeByConnectionId(connectionId)
            db.dataCountDao.removeByConnectionId(connectionId)
            db.recentHistoryDao.removeByConnectionId(connectionId)
            db.searchHistoryDao.removeByConnectionId(connectionId)
            db.skipTimeDao.removeByConnectionId(connectionId)
            db.lrcConfigDao.removeByConnectionId(connectionId)
            db.eraItemDao.removeAll()
            db.remoteCurrentDao.removeByConnectionId(connectionId)
            db.connectionConfigDao.removeById(connectionId)
        }
    }

    /**
     * 清空全部数据库数据，清库前先删除下载任务关联的磁盘文件。
     */
    suspend fun clearAllDatabaseData(
        db: LocalDatabaseClient,
        downloadDb: DownloadDatabaseClient,
        downloaderManager: DownloaderManager,
    ) {
        // 清库会移除下载记录中的文件路径，必须先拿到任务 ID 并交给下载管理器清理文件。
        val downloadIds = downloadDb.downloadDao.getAllTasksSuspend(
            mediaLibraryId = null,
            typeData = null
        ).map { it.id }.toLongArray()
        if (downloadIds.isNotEmpty()) {
            // 等待下载文件删除完成后再清主库，避免异步删除和 removeAll 之间发生路径丢失。
            downloaderManager.deleteAndAwait(*downloadIds)
        }
        db.withTransaction {
            db.musicDao.removeAllWithReferences()
            db.albumDao.removeAllWithReferences()
            db.artistDao.removeAllWithReferences()
            db.playerDao.removeAll()
            db.progressDao.removeAll()
            db.enableProgressDao.removeAll()
            db.connectionConfigDao.removeAll()
            db.libraryDao.removeAll()
            db.genreDao.removeAll()
            db.eraItemDao.removeAll()
            db.remoteCurrentDao.removeAll()
            db.searchHistoryDao.deleteAll()
            db.skipTimeDao.removeAll()
            db.dataCountDao.removeAll()
            db.recentHistoryDao.removeAll()
            db.lrcConfigDao.removeAll()
            db.proxyConfigDao.removeAll()
            db.settingsDao.remove()
        }
        downloadDb.withTransaction {
            // 兜底清空下载表，覆盖删除流程中已无对应文件或记录状态异常的残留记录。
            downloadDb.downloadDao.removeAll()
        }
    }

}
