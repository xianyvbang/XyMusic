package cn.xybbz.localdata.config

import androidx.room.RoomDatabase
import cn.xybbz.database.DatasourceFactory
import cn.xybbz.platform.ContextWrapper

actual fun getLocalRoomDatabaseBuilder(
    dbFileName: String,
    contextWrapper: ContextWrapper
): RoomDatabase.Builder<LocalDatabaseClient> {
    return DatasourceFactory(contextWrapper.context).createDatabaseClientBuilder(
        dbFileName
    )
}