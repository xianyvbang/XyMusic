package cn.xybbz.di

import cn.xybbz.download.database.DownloadDatabaseClient
import cn.xybbz.download.database.getDownloadRoomDatabase
import cn.xybbz.localdata.config.LocalDatabaseClient
import cn.xybbz.localdata.config.getLocalRoomDatabase
import cn.xybbz.platform.ContextWrapper
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
class DatabaseModule {

    @Single
    fun db(contextWrapper: ContextWrapper): LocalDatabaseClient {
        return getLocalRoomDatabase(contextWrapper)
    }
    
    @Single
    fun downloadDb(contextWrapper: ContextWrapper): DownloadDatabaseClient {
        return getDownloadRoomDatabase(contextWrapper)
    }
}