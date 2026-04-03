package cn.xybbz.database

import androidx.room.RoomDatabaseConstructor

abstract class AppDatabaseConstructor<T: DatabaseClient> : RoomDatabaseConstructor<T> {

    abstract override fun initialize(): T
}

