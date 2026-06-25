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
    val dbFile = getJvmDatabaseFile(dbFileName, contextWrapper)
    // 数据库目录必须在 Room 打开前创建，避免 bundled SQLite 因父目录缺失而建库失败。
    dbFile.parentFile?.mkdirs()
    return Room.databaseBuilder<T>(
        name = dbFile.absolutePath,
    )
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
}

fun getJvmDatabaseFile(dbFileName: String, contextWrapper: ContextWrapper): File {
    // JVM 数据库落在持久数据库目录，不再使用会被系统清理的 java.io.tmpdir。
    return File(contextWrapper.databaseDirectory, dbFileName)
}

fun getJvmDatabaseFiles(dbFileName: String, contextWrapper: ContextWrapper): List<File> {
    val databaseFile = getJvmDatabaseFile(dbFileName, contextWrapper)
    // Room/SQLite 可能在主库旁生成 WAL、SHM 或 journal 文件，统计和清理都要覆盖。
    return listOf(
        databaseFile,
        File(databaseFile.absolutePath + "-wal"),
        File(databaseFile.absolutePath + "-shm"),
        File(databaseFile.absolutePath + "-journal"),
    )
}
