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
import cn.xybbz.common.music.AudioFadeController
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
class SettingsModule {

    @Singleton
    @Provides
    fun settingsManager(
        db: DatabaseClient,
        @ApplicationContext applicationContext: Context,
        audioFadeController: AudioFadeController,
        netWorkMonitor: NetWorkMonitor
    ): SettingsManager {
        val settingsManager =
            SettingsManager(db, applicationContext, audioFadeController, netWorkMonitor)
        settingsManager.setSettingsData()
        return settingsManager;
    }
}