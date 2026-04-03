package cn.xybbz.localdata.config

import cn.xybbz.database.AppDatabaseConstructor

@Suppress("KotlinNoActualForExpect")
expect object LocalAppDatabaseConstructor : AppDatabaseConstructor<LocalDatabaseClient> {

    override fun initialize(): LocalDatabaseClient
}

