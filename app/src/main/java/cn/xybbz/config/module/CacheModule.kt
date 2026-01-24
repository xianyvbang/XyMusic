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
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import cn.xybbz.api.client.CacheApiClient
import cn.xybbz.common.music.DownloadCacheController
import cn.xybbz.config.setting.SettingsManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class CacheModule {

    @OptIn(UnstableApi::class)
    @Singleton
    @Provides
    fun downloadCacheController(
        @ApplicationContext context: Context,
        settingsManager: SettingsManager,
        cacheApiClient: CacheApiClient
    ): DownloadCacheController {
        return DownloadCacheController(context, settingsManager,cacheApiClient)
    }
}