package cn.xybbz.localdata.config

import androidx.room.RoomDatabaseConstructor
import cn.xybbz.database.DatabaseClient

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<LocalDatabaseClient> {

    override fun initialize(): LocalDatabaseClient
}

