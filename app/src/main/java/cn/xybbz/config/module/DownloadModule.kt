package cn.xybbz.config.module

import android.content.Context
import android.os.Environment
import androidx.work.WorkManager
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.constants.Constants
import cn.xybbz.config.download.DownLoadManager
import cn.xybbz.config.download.core.DownloadDispatcherImpl
import cn.xybbz.config.download.core.DownloaderConfig
import cn.xybbz.config.download.notification.NotificationController
import cn.xybbz.config.network.NetWorkMonitor
import cn.xybbz.config.setting.SettingsManager
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
        dataSourceManager: DataSourceManager,
        notificationController: NotificationController,
        settingsManager: SettingsManager,
        workManager: WorkManager
    ): DownloadDispatcherImpl {
        val settings = settingsManager.get()
        val downloadDispatcherImpl = DownloadDispatcherImpl(
            db,
            workManager,
            DownloaderConfig.Builder(applicationContext)
                .setFinalDirectory("${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath}/${Constants.APP_NAME}")
                .setMaxConcurrentDownloads(settings.maxConcurrentDownloads).build(),
            dataSourceManager,
            notificationController
        )
        return downloadDispatcherImpl
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

    @Singleton
    @Provides
    fun netWorkMonitor(@ApplicationContext applicationContext: Context): NetWorkMonitor {
        return NetWorkMonitor(applicationContext)
    }
}