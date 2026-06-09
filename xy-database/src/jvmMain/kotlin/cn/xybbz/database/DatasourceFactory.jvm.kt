package cn.xybbz.database

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import cn.xybbz.platform.ContextWrapper
import kotlinx.coroutines.Dispatchers
import java.io.File

actual inline fun <reified T : DatabaseClient> createDatabaseClientBuilder(
    dbFileName: String,
    contextWrapper: ContextWrapper
): RoomDatabase.Builder<T> {
    val dbFile = getJvmDatabaseFile(dbFileName)
    return Room.databaseBuilder<T>(
        name = dbFile.absolutePath,
    )
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
}

fun getJvmDatabaseFile(dbFileName: String): File {
    return File(System.getProperty("java.io.tmpdir"), dbFileName)
}

fun getJvmDatabaseFiles(dbFileName: String): List<File> {
    val databaseFile = getJvmDatabaseFile(dbFileName)
    return listOf(
        databaseFile,
        File(databaseFile.absolutePath + "-wal"),
        File(databaseFile.absolutePath + "-shm"),
        File(databaseFile.absolutePath + "-journal"),
    )
}
