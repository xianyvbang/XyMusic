package cn.xybbz

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.media3.common.util.UnstableApi
import cn.xybbz.config.BackgroundConfig
import cn.xybbz.ui.screens.MainScreen
import cn.xybbz.ui.theme.XyTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.hjq.language.MultiLanguages
import com.kongzue.dialogx.DialogX
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@ExperimentalPermissionsApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var backgroundConfig: BackgroundConfig

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("MainActivity", "调用两次")
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
        DialogX.onlyOnePopTip = false
        //是否默认可以关闭
        DialogX.cancelableTipDialog = false
        enableEdgeToEdge()

        setContent {

            DialogX.globalTheme = DialogX.THEME.DARK

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
}