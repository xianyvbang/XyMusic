package cn.xybbz.localdata.config

import cn.xybbz.database.getJvmDatabaseFiles
import java.io.File

fun getLocalDatabaseFiles(): List<File> {
    return getJvmDatabaseFiles(DB_FILE_NAME)
}
