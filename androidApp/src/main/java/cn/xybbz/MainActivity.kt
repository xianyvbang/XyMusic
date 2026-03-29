package cn.xybbz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import cn.xybbz.di.initKoin
import org.koin.android.ext.koin.androidContext

// this part should be configured only once in the app to use native android logging
object Static {
    init {
        System.setProperty("kotlin-logging-to-android-native", "true")
    }
}
private val static = Static

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        initKoin {
            androidContext(applicationContext)
        }
        enableEdgeToEdge()
        setContent {
            App()
        }
    }
}
