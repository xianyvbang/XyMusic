package cn.xybbz.router

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import cn.xybbz.ui.screens.AlbumInfoDesktopScreen
import cn.xybbz.ui.screens.ArtistDesktopScreen
import cn.xybbz.ui.screens.ArtistInfoDesktopScreen
import cn.xybbz.ui.screens.CacheLimitScreen
import cn.xybbz.ui.screens.ConnectionConfigInfoScreen
import cn.xybbz.ui.screens.ConnectionManagement
import cn.xybbz.ui.screens.ConnectionScreen
import cn.xybbz.ui.screens.CustomApiScreen
import cn.xybbz.ui.screens.DailyRecommendScreen
import cn.xybbz.ui.screens.DownloadScreen
import cn.xybbz.ui.screens.FavoriteDesktopScreen
import cn.xybbz.ui.screens.GenresInfoScreen
import cn.xybbz.ui.screens.GenresScreen
import cn.xybbz.ui.screens.HomeScreen
import cn.xybbz.ui.screens.InterfaceSettingScreen
import cn.xybbz.ui.screens.JvmAboutScreen
import cn.xybbz.ui.screens.JvmAlbumInfoScreen
import cn.xybbz.ui.screens.JvmAlbumScreen
import cn.xybbz.ui.screens.JvmMusicScreen
import cn.xybbz.ui.screens.JvmSearchScreen
import cn.xybbz.ui.screens.JvmSettingScreen
import cn.xybbz.ui.screens.LanguageConfigScreen
import cn.xybbz.ui.screens.LocalScreen
import cn.xybbz.ui.screens.MemoryManagementScreen
import cn.xybbz.ui.screens.ProxyConfigScreen
import cn.xybbz.ui.screens.SelectLibraryScreen
import cn.xybbz.ui.screens.SetBackgroundImageScreen
import cn.xybbz.ui.screens.StreamingQualityScreen
import cn.xybbz.ui.xy.XyImage
import org.jetbrains.compose.resources.stringResource
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.background_image

private inline fun <reified T : NavKey> EntryProviderScope<NavKey>.desktopNode(noinline content: @Composable (T) -> Unit) {
    entry<T> {
        Box {
            XyImage(
                modifier = Modifier.fillMaxSize(),
                model = null,
                contentDescription = stringResource(Res.string.background_image),
            )
            content(it)
        }
    }
}

private val jvmDesktopEntryProvider = buildDefaultRouteEntryProvider()


private val jvmDesktopEntryProvider2 = entryProvider<NavKey> {
    desktopNode<Connection> { ConnectionScreen(it.connectionUiType) }
    desktopNode<Home> { HomeScreen() }
    desktopNode<Search> { JvmSearchScreen(searchQuery = it.searchQuery) }
    desktopNode<Music> { JvmMusicScreen() }
    desktopNode<Album> { JvmAlbumScreen() }
    desktopNode<Artist> { ArtistDesktopScreen() }
    desktopNode<FavoriteList> { FavoriteDesktopScreen() }
//    desktopNode<AlbumInfo> { AlbumInfoDesktopScreen(it) }
    desktopNode<AlbumInfo> { JvmAlbumInfoScreen(it.itemId, it.dataType) }
    desktopNode<ArtistInfo> { ArtistInfoDesktopScreen(it) }

    desktopNode<Setting> { JvmSettingScreen() }
    desktopNode<ConnectionManagement> { ConnectionManagement() }
    desktopNode<ConnectionInfo> { ConnectionConfigInfoScreen(connectionId = it.connectionId) }
    desktopNode<MemoryManagement> { MemoryManagementScreen() }
    desktopNode<InterfaceSetting> { InterfaceSettingScreen() }
    desktopNode<LanguageConfig> { LanguageConfigScreen() }
    desktopNode<Genres> { GenresScreen() }
    desktopNode<GenreInfo> { GenresInfoScreen(genreId = it.genreId) }
    desktopNode<About> { JvmAboutScreen() }
    desktopNode<CacheLimit> { CacheLimitScreen() }
    desktopNode<SelectLibrary> {
        SelectLibraryScreen(connectionId = it.connectionId, thisLibraryId = it.libraryIds)
    }
    desktopNode<DailyRecommend> { DailyRecommendScreen() }
    desktopNode<Download> { DownloadScreen() }
    desktopNode<Local> { LocalScreen() }
    desktopNode<SetBackgroundImage> { SetBackgroundImageScreen() }
    desktopNode<ProxyConfig> { ProxyConfigScreen() }
    desktopNode<StreamingQuality> { StreamingQualityScreen() }
    desktopNode<CustomApi> { CustomApiScreen() }
}

actual val platformEntryProvider: (NavKey) -> NavEntry<NavKey> = jvmDesktopEntryProvider2
