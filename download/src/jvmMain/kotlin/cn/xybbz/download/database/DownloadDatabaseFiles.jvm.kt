package cn.xybbz.download.database

import cn.xybbz.database.getJvmDatabaseFiles
import java.io.File

fun getDownloadDatabaseFiles(): List<File> {
    return getJvmDatabaseFiles(DB_FILE_NAME)
}
