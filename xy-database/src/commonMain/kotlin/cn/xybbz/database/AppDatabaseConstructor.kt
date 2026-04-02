package cn.xybbz.database

import androidx.room.RoomDatabaseConstructor

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<DatabaseClient> {

    override fun initialize(): DatabaseClient
}

