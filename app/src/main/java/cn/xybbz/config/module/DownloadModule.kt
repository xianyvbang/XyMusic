package cn.xybbz.config.module

import android.content.Context
import androidx.work.WorkManager
import cn.xybbz.config.ConnectionConfigServer
import cn.xybbz.config.download.DownLoadManager
import cn.xybbz.config.download.core.DownloadDispatcherImpl
import cn.xybbz.config.download.core.DownloaderConfig
import cn.xybbz.config.download.notification.NotificationController
import cn.xybbz.localdata.config.DatabaseClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DownloadModule {


    @Singleton
    @Provides
    fun downloadDispatcher(
        db: DatabaseClient,
        @ApplicationContext applicationContext: Context,
        connectionConfigServer: ConnectionConfigServer,
        notificationController:NotificationController
    ): DownloadDispatcherImpl {
        val downloadDispatcherImpl = DownloadDispatcherImpl(
            db,
            WorkManager.getInstance(applicationContext),
            DownloaderConfig.Builder(applicationContext).build(),
            connectionConfigServer,
            notificationController
        )
        return downloadDispatcherImpl;
    }


    @Singleton
    @Provides
    fun downLoadManager(
        db: DatabaseClient,
        @ApplicationContext applicationContext: Context,
        downloadDispatcher: DownloadDispatcherImpl
    ): DownLoadManager {
        val downLoadManager =
            DownLoadManager(
                applicationContext,
                db,
                downloadDispatcher
            )
        return downLoadManager;
    }

    @Singleton
    @Provides
    fun notificationController(
        @ApplicationContext applicationContext: Context
    ): NotificationController {
        val notificationController =
            NotificationController(
                applicationContext,
            )
        return notificationController;
    }
}