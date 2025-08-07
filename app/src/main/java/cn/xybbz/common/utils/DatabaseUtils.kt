package cn.xybbz.common.utils

import androidx.room.withTransaction
import cn.xybbz.localdata.config.DatabaseClient

/**
 * 通用操作数据库方法
 */
object DatabaseUtils {

    /**
     * 清空指定连接的数据
     * todo 清除新增的几个关联表
     */
    suspend fun clearDatabaseByConnectionConfig(
        db: DatabaseClient,
        connectionId: Long
    ) {
        db.withTransaction {
            db.albumDao.removeByConnectionId(connectionId)
            db.artistDao.removeByConnectionId(connectionId)
            db.eraItemDao.removeByConnectionId()
            db.genreDao.removeByConnectionId(connectionId)
            db.libraryDao.removeByConnectionId(connectionId)
            db.enableProgressDao.removeByConnectionId(connectionId)
            db.progressDao.removeByConnectionId(connectionId)
            db.remoteCurrentDao.removeByConnectionId(connectionId)
            db.skipTimeDao.removeByConnectionId(connectionId)
            db.connectionConfigDao.removeById(connectionId)
        }
    }


}