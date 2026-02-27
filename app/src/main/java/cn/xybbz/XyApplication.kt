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
import cn.xybbz.config.dialog.XyMaterialYouStyle
import cn.xybbz.config.download.DownLoadManager
import cn.xybbz.config.proxy.ProxyConfigServer
import cn.xybbz.config.setting.SettingsManager
import com.hjq.language.MultiLanguages
import com.kongzue.dialogx.DialogX
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.system.exitProcess

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
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            // 在这里处理异常，可以进行日志记录或其他操作
            throwable.printStackTrace()
            Log.e("=====", "有异常" + throwable.message.toString())
            // 退出应用程序
            exitProcess(1)
        }
        // 初始化语种切换框架
        super.onCreate()
        MultiLanguages.init(this)
        DialogX.init(this)
        DialogX.globalStyle = XyMaterialYouStyle()
        DialogX.DEBUGMODE = true

        val scope = CoroutineScopeUtils.getDefault("XyApplication")

        scope.launch {
            val settings = settingsManager.setSettingsData()
            dataSourceManager.initDataSource(settings.dataSourceType)
        }

        scope.launch {
            homeDataRepository.initData()
        }

        scope.launch {
            backgroundConfig.load()
        }
        scope.launch {
            proxyConfigServer.initConfig()
        }



        Log.i("init", "musicController加载")
        musicController.initController { }

    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}