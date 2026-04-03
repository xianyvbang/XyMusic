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

package cn.xybbz.di

import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.download.DownloaderManager
import cn.xybbz.download.core.DownloaderConfig
import cn.xybbz.download.core.HttpClientFactory
import cn.xybbz.download.database.DownloadDatabaseClient
import cn.xybbz.platform.ContextWrapper
import io.ktor.client.HttpClient
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
@Configuration
class DownloadModule {

    @Singleton
    fun downLoadManager(
        contextWrapper: ContextWrapper,
        db: DownloadDatabaseClient,
        settingsManager: SettingsManager,
        dataSourceManager: DataSourceManager
    ): DownloaderManager {
        val settings = settingsManager.get()
        val downLoadManager =
            DownloaderManager(
                contextWrapper,
                db,
                config = DownloaderConfig.Builder(contextWrapper)
                    .setMaxConcurrentDownloads(settings.maxConcurrentDownloads).build(),
                httpClientFactory = object : HttpClientFactory {
                    override fun createHttpClient(): HttpClient {
                        return dataSourceManager.getHttpClient()
                    }
                }
            )
        return downLoadManager;
    }
}