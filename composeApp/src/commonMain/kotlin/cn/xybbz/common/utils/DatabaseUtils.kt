package cn.xybbz.common.utils

import cn.xybbz.database.withTransaction
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

    suspend fun clearAllDatabaseData(db: LocalDatabaseClient) {
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
    }

}
