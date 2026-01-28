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

package cn.xybbz

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.music.MusicController
import cn.xybbz.common.utils.CoroutineScopeUtils
import cn.xybbz.config.BackgroundConfig
import cn.xybbz.config.HomeDataRepository
import cn.xybbz.config.download.DownLoadManager
import cn.xybbz.config.proxy.ProxyConfigServer
import cn.xybbz.config.setting.SettingsManager
import com.hjq.language.MultiLanguages
import com.kongzue.dialogx.DialogX
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class XyApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var dataSourceManager: DataSourceManager

    @Inject
    lateinit var proxyConfigServer: ProxyConfigServer

    @Inject
    lateinit var settingsManager: SettingsManager

    @Inject
    lateinit var backgroundConfig: BackgroundConfig

    @Inject
    lateinit var downloadManager: DownLoadManager

    @Inject
    lateinit var homeDataRepository: HomeDataRepository

    @Inject
    lateinit var musicController: MusicController

    override fun onCreate() {
        // 初始化语种切换框架
        super.onCreate()
        MultiLanguages.init(this)
        DialogX.init(this)
        DialogX.DEBUGMODE = true
        DialogX.onlyOnePopTip = false
        //是否默认可以关闭
        DialogX.cancelableTipDialog = false
        DialogX.globalTheme = DialogX.THEME.DARK

        val scope = CoroutineScopeUtils.getDefault("XyApplication")
        scope.launch {
            val settings = settingsManager.setSettingsData()
            dataSourceManager.initDataSource(settings.dataSourceType)
        }

        scope.launch {
            backgroundConfig.load()
        }
        scope.launch {
            proxyConfigServer.initConfig()
        }

        scope.launch {
            homeDataRepository.initData()
        }

        Log.i("init","musicController加载")
        musicController.initController {  }

    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}