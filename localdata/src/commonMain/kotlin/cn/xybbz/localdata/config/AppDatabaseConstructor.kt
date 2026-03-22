package cn.xybbz.localdata.config

import androidx.room.RoomDatabaseConstructor

@Suppress("KotlinNoActualForExpect")
expect class AppDatabaseConstructor : RoomDatabaseConstructor<DatabaseClient> {

    override fun initialize(): DatabaseClient
}

