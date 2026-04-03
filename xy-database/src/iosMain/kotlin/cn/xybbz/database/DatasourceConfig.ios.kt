package cn.xybbz.database

import androidx.room.Room
import androidx.room.RoomDatabase
import cn.xybbz.platform.ContextWrapper
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual inline fun <reified T : DatabaseClient> createDatabaseClientBuilder(
    dbFileName: String,
    contextWrapper: ContextWrapper
): RoomDatabase.Builder<T> {
    val dbFilePath = documentDirectory() + "/${dbFileName}"
    return Room.databaseBuilder<T>(
        name = dbFilePath,
    )
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