package cn.xybbz.localdata.config

import cn.xybbz.database.getJvmDatabaseFiles
import cn.xybbz.platform.ContextWrapper
import java.io.File

fun getLocalDatabaseFiles(contextWrapper: ContextWrapper): List<File> {
    // 主库文件列表从 JVM 持久数据库目录派生，包含 SQLite 伴生文件。
    return getJvmDatabaseFiles(DB_FILE_NAME, contextWrapper)
}
