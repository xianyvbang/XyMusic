package cn.xybbz.download.database

import androidx.room.RoomDatabaseConstructor

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<DownloadDatabaseClient> {

    override fun initialize(): DownloadDatabaseClient
}

