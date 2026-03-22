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
            db.eraItemDao.removeByConnectionId()
            db.connectionConfigDao.removeById(connectionId)
        }
    }


}