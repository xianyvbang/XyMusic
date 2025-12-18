package cn.xybbz.router

import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.navigation
import androidx.navigation.toRoute
import cn.xybbz.compositionLocal.LocalMainViewModel
import cn.xybbz.compositionLocal.LocalNavController
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

@Composable
fun RouterCompose(
    paddingValues: PaddingValues
) {
    val navHostController = LocalNavController.current
    SideEffect {
        Log.d("=====", "RouterCompose重组一次")
    }

    val mainViewModel = LocalMainViewModel.current
    NavHost(
        navController = navHostController,
        startDestination = if (mainViewModel.connectionIsLogIn) RouterConstants.Screen else RouterConstants.Connection()
    ) {

        /* dialog("test") {
             Box(modifier = Modifier.size(500.dp)) {
                 Text(text = "test")
             }
         }*/

        nodeComposable<RouterConstants.Connection> {
            val connection = it.toRoute<RouterConstants.Connection>()
            ConnectionScreen(connection.connectionUiType)
        }


        navigation<RouterConstants.Screen>(
            startDestination = RouterConstants.Home
        ) {
            nodeComposable<RouterConstants.Home> {
                HomeScreen(modifier = Modifier)
            }


            nodeComposable<RouterConstants.Music> {
                MusicScreen()
            }

            nodeComposable<RouterConstants.Album> {
                AlbumScreen()
            }

            nodeComposable<RouterConstants.Artist> {
                ArtistScreen()
            }

            nodeComposable<RouterConstants.AlbumInfo> {
                val albumInfo = it.toRoute<RouterConstants.AlbumInfo>()
                AlbumInfoScreen(
                    itemId = albumInfo.itemId,
                    dataType = albumInfo.dataType,
                )
            }

            nodeComposable<RouterConstants.Setting> {
                SettingScreen()
            }

            nodeComposable<RouterConstants.Search> {
                SearchScreen()
            }

            nodeComposable<RouterConstants.FavoriteList> {
                FavoriteScreen()
            }

            nodeComposable<RouterConstants.ConnectionManagement> {
                ConnectionManagement()
            }

            nodeComposable<RouterConstants.ConnectionInfo> {
                val connectionInfo = it.toRoute<RouterConstants.ConnectionInfo>()
                ConnectionConfigInfoScreen(
                    connectionId = connectionInfo.connectionId
                )

            }

            nodeComposable<RouterConstants.MemoryManagement> {
                MemoryManagementScreen()
            }

            extremityComposable<RouterConstants.ArtistInfo> {
                val artistInfo = it.toRoute<RouterConstants.ArtistInfo>()
                ArtistInfoScreen(
                    artistId = { artistInfo.artistId })
            }

            nodeComposable<RouterConstants.InterfaceSetting> {
                InterfaceSettingScreen()
            }


            nodeComposable<RouterConstants.LanguageConfig> {
                LanguageConfigScreen()
            }

            nodeComposable<RouterConstants.Genres> {
                GenresScreen()
            }

            extremityComposable<RouterConstants.GenreInfo> {
                val genreInfo =
                    it.toRoute<RouterConstants.GenreInfo>()
                GenresInfoScreen(
                    genreId = genreInfo.genreId
                )
            }

            nodeComposable<RouterConstants.About> {
                AboutScreen()
            }

            extremityComposable<RouterConstants.CacheLimit> {
                CacheLimitScreen()
            }

            extremityComposable<RouterConstants.SelectLibrary> {
                val selectLibrary =
                    it.toRoute<RouterConstants.SelectLibrary>()
                SelectLibraryScreen(
                    connectionId = selectLibrary.connectionId,
                    thisLibraryId = selectLibrary.libraryId
                )
            }

            extremityComposable<RouterConstants.DailyRecommend> {
                DailyRecommendScreen()
            }

            nodeComposable<RouterConstants.Download> {
                DownloadScreen()
            }

            nodeComposable<RouterConstants.Local> {
                LocalScreen()
            }

            extremityComposable<RouterConstants.SetBackgroundImage> {
                SetBackgroundImageScreen()
            }

            extremityComposable<RouterConstants.ProxyConfig> {
                ProxyConfigScreen()
            }
        }
    }

}