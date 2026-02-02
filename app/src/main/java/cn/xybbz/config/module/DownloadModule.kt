/*
 *   XyMusic
 *   Copyright (C) 2023 xianyvbang
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

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