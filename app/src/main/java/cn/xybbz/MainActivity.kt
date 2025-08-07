package cn.xybbz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.media3.common.util.UnstableApi
import cn.xybbz.common.utils.NetWorkUtils
import cn.xybbz.config.SettingsConfig
import cn.xybbz.localdata.enums.ThemeTypeEnum
import cn.xybbz.ui.XyConfigs
import cn.xybbz.ui.screens.MainScreen
import cn.xybbz.ui.theme.XyTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.kongzue.dialogx.DialogX
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@ExperimentalPermissionsApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsConfig: SettingsConfig

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {

        DialogX.init(this);
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
            val isDark = when (settingsConfig.themeType) {
                ThemeTypeEnum.SYSTEM -> isSystemInDarkTheme()
                ThemeTypeEnum.DARK -> true
                ThemeTypeEnum.LIGHT -> false
            }
            DialogX.globalTheme = if (isDark) DialogX.THEME.DARK else DialogX.THEME.LIGHT
            //todo i18n语言设置 https://developer.android.google.cn/guide/topics/resources/app-languages?hl=nb#app-language-settings
          settingsConfig.updateLanguage(settingsConfig.languageType, this)
            XyTheme(
                xyConfigs = XyConfigs(
                    isDarkTheme = isDark,
                    isDynamic = settingsConfig.isDynamic
                )
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

}