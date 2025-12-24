package cn.xybbz.router

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import cn.xybbz.R
import cn.xybbz.ui.screens.AboutScreen
import cn.xybbz.ui.screens.AlbumInfoScreen
import cn.xybbz.ui.screens.AlbumScreen
import cn.xybbz.ui.screens.ArtistInfoScreen
import cn.xybbz.ui.screens.ArtistScreen
import cn.xybbz.ui.screens.CacheLimitScreen
import cn.xybbz.ui.screens.ConnectionConfigInfoScreen
import cn.xybbz.ui.screens.ConnectionManagement
import cn.xybbz.ui.screens.ConnectionScreen
import cn.xybbz.ui.screens.DailyRecommendScreen
import cn.xybbz.ui.screens.DownloadScreen
import cn.xybbz.ui.screens.FavoriteScreen
import cn.xybbz.ui.screens.GenresInfoScreen
import cn.xybbz.ui.screens.GenresScreen
import cn.xybbz.ui.screens.HomeScreen
import cn.xybbz.ui.screens.InterfaceSettingScreen
import cn.xybbz.ui.screens.LanguageConfigScreen
import cn.xybbz.ui.screens.LocalScreen
import cn.xybbz.ui.screens.MemoryManagementScreen
import cn.xybbz.ui.screens.MusicScreen
import cn.xybbz.ui.screens.ProxyConfigScreen
import cn.xybbz.ui.screens.SearchScreen
import cn.xybbz.ui.screens.SelectLibraryScreen
import cn.xybbz.ui.screens.SetBackgroundImageScreen
import cn.xybbz.ui.screens.SettingScreen
import cn.xybbz.ui.theme.XyTheme
import coil.compose.AsyncImage

/**
 * 首页使用
 */
inline fun <reified T: NavKey> EntryProviderScope<NavKey>.homeComposable(noinline content: @Composable (T) -> Unit,) {
    val homeRouterAnimate = RouterAnimate.HomeRouterAnimate
    entry<T>(
        metadata = NavDisplay.transitionSpec(homeRouterAnimate.enter) + NavDisplay.transitionSpec(
            homeRouterAnimate.popExit
        )
    ) {
        Box {
            AsyncImage(
                model = XyTheme.brash.backgroundImageUri,
                contentDescription = stringResource(R.string.background_image),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            content(it)
        }

    }
}

/**
 * 节点页面
 */
inline fun <reified T : NavKey> EntryProviderScope<NavKey>.nodeComposable(noinline content: @Composable (T) -> Unit) {
    val nodeRouterAnimate = RouterAnimate.NodeRouterAnimate
    entry<T>(
        /*metadata = NavDisplay.transitionSpec(nodeRouterAnimate.enter) + NavDisplay.transitionSpec(
            nodeRouterAnimate.popExit
        )*/
    ) {
        Box {
            AsyncImage(
                model = XyTheme.brash.backgroundImageUri,
                contentDescription = stringResource(R.string.background_image),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            content(it)
        }
    }
}

/**
 * 最内层页面
 */
inline fun <reified T : NavKey> EntryProviderScope<NavKey>.extremityComposable(noinline content: @Composable (T) -> Unit) {
    val extremityRouterAnimate = RouterAnimate.ExtremityRouterAnimate
    entry<T>(
        /*metadata = NavDisplay.transitionSpec(extremityRouterAnimate.enter) + NavDisplay.transitionSpec(
            extremityRouterAnimate.popExit
        )*/
    ) {
        Box {
            AsyncImage(
                model = XyTheme.brash.backgroundImageUri,
                contentDescription = stringResource(R.string.background_image),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            content(it)
        }
    }
}

val entryProvider = entryProvider{
    nodeComposable<Connection> {
        ConnectionScreen(it.connectionUiType)
    }

    nodeComposable<Home> {
        HomeScreen(modifier = Modifier)
    }

    nodeComposable<Music> {
        MusicScreen()
    }

    nodeComposable<Album> {
        AlbumScreen()
    }

    nodeComposable<Artist> {
        ArtistScreen()
    }

    nodeComposable<AlbumInfo> {albumInfo ->
        AlbumInfoScreen(
            itemId = albumInfo.itemId,
            dataType = albumInfo.dataType,
        )
    }

    nodeComposable<Setting> {
        SettingScreen()
    }

    nodeComposable<Search> {
        SearchScreen()
    }

    nodeComposable<FavoriteList> {
        FavoriteScreen()
    }

    nodeComposable<ConnectionManagement> {
        ConnectionManagement()
    }

    nodeComposable<ConnectionInfo> {connectionInfo->
        ConnectionConfigInfoScreen(
            connectionId = connectionInfo.connectionId
        )

    }

    nodeComposable<MemoryManagement> {
        MemoryManagementScreen()
    }

    extremityComposable<ArtistInfo> {artistInfo->
        ArtistInfoScreen(
            artistId = { artistInfo.artistId })
    }

    nodeComposable<InterfaceSetting> {
        InterfaceSettingScreen()
    }


    nodeComposable<LanguageConfig> {
        LanguageConfigScreen()
    }

    nodeComposable<Genres> {
        GenresScreen()
    }

    extremityComposable<GenreInfo> {genreInfo->
        GenresInfoScreen(
            genreId = genreInfo.genreId
        )
    }

    nodeComposable<About> {
        AboutScreen()
    }

    extremityComposable<CacheLimit> {
        CacheLimitScreen()
    }

    extremityComposable<SelectLibrary> {selectLibrary->
        SelectLibraryScreen(
            connectionId = selectLibrary.connectionId,
            thisLibraryId = selectLibrary.libraryId
        )
    }

    extremityComposable<DailyRecommend> {
        DailyRecommendScreen()
    }

    nodeComposable<Download> {
        DownloadScreen()
    }

    nodeComposable<Local> {
        LocalScreen()
    }

    extremityComposable<SetBackgroundImage> {
        SetBackgroundImageScreen()
    }

    extremityComposable<ProxyConfig> {
        ProxyConfigScreen()
    }
}