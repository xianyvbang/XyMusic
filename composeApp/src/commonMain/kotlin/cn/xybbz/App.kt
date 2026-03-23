package cn.xybbz

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.localdata.enums.ThemeTypeEnum
import cn.xybbz.ui.popup.XyPopTipHost
import cn.xybbz.ui.screens.MainScreen
import cn.xybbz.ui.theme.XyConfigs
import cn.xybbz.ui.theme.XyTheme
import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import org.koin.compose.getKoin

@OptIn(ExperimentalCoilApi::class)
@Composable
//@Preview
fun App() {

    val dataSourceManager: DataSourceManager = getKoin().get()

    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .crossfade(true)
            .components {
                add(KtorNetworkFetcherFactory(dataSourceManager.getHttpClient()))
            }
            .build()
    }

    val isDark = when (settingsManager.themeType) {
        ThemeTypeEnum.SYSTEM -> isSystemInDarkTheme()
        ThemeTypeEnum.DARK -> true
        ThemeTypeEnum.LIGHT -> false
    }

    XyTheme(
        xyConfigs = XyConfigs(
            isDarkTheme = isDark,
            isDynamic = settingsManager.isDynamic
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
