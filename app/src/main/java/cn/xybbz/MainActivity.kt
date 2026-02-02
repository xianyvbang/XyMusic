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

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.config.BackgroundConfig
import cn.xybbz.config.network.NetWorkMonitor
import cn.xybbz.ui.screens.MainScreen
import cn.xybbz.ui.theme.XyTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.hjq.language.MultiLanguages
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@ExperimentalPermissionsApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var backgroundConfig: BackgroundConfig
    @Inject
    lateinit var netWorkMonitor: NetWorkMonitor
    @Inject
    lateinit var dataSourceManager: DataSourceManager

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        lifecycleScope
        //启动页面
        installSplashScreen()
        super.onCreate(savedInstanceState)

        /* Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
             // 在这里处理异常，可以进行日志记录或其他操作
             throwable.printStackTrace()
             Log.e("=====", "有异常" + throwable.message.toString())
             // 退出应用程序
 //            exitProcess(1)
         }*/
        enableEdgeToEdge()

        setContent {
            XyTheme(
                brash = backgroundConfig.xyBackgroundBrash
//                xyBackground = backgroundConfig.xyBackground
            ) {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }

    }


    override fun attachBaseContext(newBase: Context?) {
        // 绑定语种
        super.attachBaseContext(MultiLanguages.attach(newBase));
    }

    override fun onDestroy() {
/*        netWorkMonitor.stop()
        dataSourceManager.close()*/
        super.onDestroy()
    }
}