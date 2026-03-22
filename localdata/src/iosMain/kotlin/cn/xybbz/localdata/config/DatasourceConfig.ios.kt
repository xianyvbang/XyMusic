package cn.xybbz.localdata.config

import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual class DatasourceFactory {
    actual fun createDatabaseClientBuilder(): RoomDatabase.Builder<DatabaseClient> {
        val dbFilePath = documentDirectory() + "/${DB_FILE_NAME}"
        return Room.databaseBuilder<DatabaseClient>(
            name = dbFilePath,
        )
    }


}

@OptIn(ExperimentalForeignApi::class)
fun documentDirectory(): String {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    return requireNotNull(documentDirectory?.path)
}