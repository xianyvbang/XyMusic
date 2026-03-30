package cn.xybbz

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.localdata.enums.ThemeTypeEnum
import cn.xybbz.ui.popup.XyPopTipHost
import cn.xybbz.ui.screens.MainScreen
import cn.xybbz.ui.theme.XyConfigs
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.theme.xyBackgroundBrash
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import com.github.panpf.sketch.SingletonSketch
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.fetch.KtorHttpUriFetcher
import com.github.panpf.sketch.fetch.internal.KtorHttpUriFetcherProvider
import com.github.panpf.sketch.http.KtorStack
import io.ktor.client.HttpClient
import org.koin.compose.getKoin

@OptIn(ExperimentalCoilApi::class)
@Composable
//@Preview
fun App() {

    val dataSourceManager: DataSourceManager = getKoin().get()
    val settingsManager: SettingsManager = getKoin().get()

    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .crossfade(true)
            .components {
                add(KtorNetworkFetcherFactory(dataSourceManager.getHttpClient()))
            }
            .build()
    }

    SingletonSketch.setSafe {
        Sketch.Builder(PlatformContext.INSTANCE).apply {
            logger(level = Logger.Level.Debug)
            addIgnoreFetcherProvider(KtorHttpUriFetcherProvider::class)
            addComponents {
                val httpStack = KtorStack(dataSourceManager.getHttpClient())
                addFetcher(KtorHttpUriFetcher.Factory(httpStack))
            }
        }.build()
    }

    val isDark = when (settingsManager.themeType) {
        ThemeTypeEnum.SYSTEM -> isSystemInDarkTheme()
        ThemeTypeEnum.DARK -> true
        ThemeTypeEnum.LIGHT -> false
    }

    XyTheme(
        xyConfigs = XyConfigs(
            isDarkTheme = isDark,
        ),
        brash = xyBackgroundBrash(
            backgroundImageUri = settingsManager.imageFilePath
        )
    ) {
        WindowInsets.systemBars.union(WindowInsets.displayCutout)
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                MainScreen()
                XyPopTipHost()
            }
        }
    }
}
