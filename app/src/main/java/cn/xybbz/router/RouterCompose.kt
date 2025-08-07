package cn.xybbz.router

import android.util.Log
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
import cn.xybbz.ui.screens.GenresInfoScreen
import cn.xybbz.ui.screens.GenresScreen
import cn.xybbz.ui.screens.HomeScreen
import cn.xybbz.ui.screens.InterfaceSettingScreen
import cn.xybbz.ui.screens.LanguageConfigScreen
import cn.xybbz.ui.screens.MemoryManagementScreen
import cn.xybbz.ui.screens.MusicProfileFavoriteScreen
import cn.xybbz.ui.screens.MusicScreen
import cn.xybbz.ui.screens.NewSearchScreen
import cn.xybbz.ui.screens.SettingScreen
import cn.xybbz.viewmodel.AlbumInfoViewModel

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
        startDestination = if (mainViewModel.dataSourceManager.dataSourceType == null) RouterConstants.Connection() else RouterConstants.Screen
//        startDestination = RouterConstants.Connection()
    ) {

        /* dialog("test") {
             Box(modifier = Modifier.size(500.dp)) {
                 Text(text = "test")
             }
         }*/
        composable<RouterConstants.Connection> {
            val connection = it.toRoute<RouterConstants.Connection>()
            ConnectionScreen(connection.connectionUiType)
        }


        navigation<RouterConstants.Screen>(
            startDestination = RouterConstants.Home
        ) {
            composable<RouterConstants.Home>(
                enterTransition = {
                    EnterTransition.None
                },
                exitTransition = {
                    ExitTransition.None
                }
            ) {
                HomeScreen(modifier = Modifier/*.padding(bottom = paddingValues.calculateBottomPadding())*/)
            }


            composable<RouterConstants.Music>(
                popEnterTransition = {
                    EnterTransition.None
                },
                popExitTransition = {
                    fadeOut(
                        animationSpec = tween(
                            300, easing = LinearEasing
                        )
                    ) + slideOutOfContainer(
                        animationSpec = tween(300, easing = EaseOut),
                        towards = AnimatedContentTransitionScope.SlideDirection.End
                    )
                },
                enterTransition = {
                    fadeIn(
                        animationSpec = tween(
                            300, easing = LinearEasing
                        )
                    ) + slideIntoContainer(
                        animationSpec = tween(300, easing = EaseIn),
                        towards = AnimatedContentTransitionScope.SlideDirection.Start
                    )
                },
                exitTransition = {
                    ExitTransition.None
                }
            ) {
                MusicScreen(paddingValues)
            }

            composable<RouterConstants.Album>(
                popEnterTransition = {
                    EnterTransition.None
                },
                popExitTransition = {
                    fadeOut(
                        animationSpec = tween(
                            300, easing = LinearEasing
                        )
                    ) + slideOutOfContainer(
                        animationSpec = tween(300, easing = EaseOut),
                        towards = AnimatedContentTransitionScope.SlideDirection.End
                    )
                },
                enterTransition = {
                    fadeIn(
                        animationSpec = tween(
                            300, easing = LinearEasing
                        )
                    ) + slideIntoContainer(
                        animationSpec = tween(300, easing = EaseIn),
                        towards = AnimatedContentTransitionScope.SlideDirection.Start
                    )
                },
                exitTransition = {
                    ExitTransition.None
                }
            ) {
                AlbumScreen(modifier = Modifier.padding(paddingValues))
            }

            composable<RouterConstants.Artist>(
                popEnterTransition = {
                    EnterTransition.None
                },
                popExitTransition = {
                    fadeOut(
                        animationSpec = tween(
                            300, easing = LinearEasing
                        )
                    ) + slideOutOfContainer(
                        animationSpec = tween(300, easing = EaseOut),
                        towards = AnimatedContentTransitionScope.SlideDirection.End
                    )
                },
                enterTransition = {
                    fadeIn(
                        animationSpec = tween(
                            300, easing = LinearEasing
                        )
                    ) + slideIntoContainer(
                        animationSpec = tween(300, easing = EaseIn),
                        towards = AnimatedContentTransitionScope.SlideDirection.Start
                    )
                },
                exitTransition = {
                    ExitTransition.None
                }
            ) {
                ArtistScreen()
            }

            composable<RouterConstants.AlbumInfo>(
                enterTransition = {

                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Left
                    )
                },
                exitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right
                    )
                }

            ) {
                val albumInfo = it.toRoute<RouterConstants.AlbumInfo>()
                val viewModel =
                    hiltViewModel<AlbumInfoViewModel, AlbumInfoViewModel.Factory>(
                        creationCallback = { factory ->
                            factory.create(
                                itemId = albumInfo.itemId,
                                dataType = albumInfo.dataType
                            )
                        })
                AlbumInfoScreen(
                    modifier = Modifier.padding(top = paddingValues.calculateTopPadding()),
                    itemId = albumInfo.itemId,
                    dataType = albumInfo.dataType,
                    albumInfoViewModel = viewModel
                )
            }
            composable<RouterConstants.Setting>(
                popEnterTransition = {
                    EnterTransition.None
                },
                popExitTransition = {
                    fadeOut(
                        animationSpec = tween(
                            300, easing = LinearEasing
                        )
                    ) + slideOutOfContainer(
                        animationSpec = tween(300, easing = EaseOut),
                        towards = AnimatedContentTransitionScope.SlideDirection.End
                    )
                },
                enterTransition = {
                    fadeIn(
                        animationSpec = tween(
                            300, easing = LinearEasing
                        )
                    ) + slideIntoContainer(
                        animationSpec = tween(300, easing = EaseIn),
                        towards = AnimatedContentTransitionScope.SlideDirection.Start
                    )
                },
                exitTransition = {
                    ExitTransition.None
                }
            ) {
                SettingScreen(
                )
            }

            composable<RouterConstants.Search>(
                popEnterTransition = {
                    EnterTransition.None
                },
                popExitTransition = {
                    fadeOut(
                        animationSpec = tween(
                            300, easing = LinearEasing
                        )
                    ) + slideOutOfContainer(
                        animationSpec = tween(300, easing = EaseOut),
                        towards = AnimatedContentTransitionScope.SlideDirection.End
                    )
                },
                enterTransition = {
                    fadeIn(
                        animationSpec = tween(
                            300, easing = LinearEasing
                        )
                    ) + slideIntoContainer(
                        animationSpec = tween(300, easing = EaseIn),
                        towards = AnimatedContentTransitionScope.SlideDirection.Start
                    )
                },
                exitTransition = {
                    ExitTransition.None
                }
            ) {
//                SearchScreen(modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding()))
                NewSearchScreen()
            }

            composable<RouterConstants.FavoriteList>(
                popEnterTransition = {
                    EnterTransition.None
                },
                popExitTransition = {
                    fadeOut(
                        animationSpec = tween(
                            300, easing = LinearEasing
                        )
                    ) + slideOutOfContainer(
                        animationSpec = tween(300, easing = EaseOut),
                        towards = AnimatedContentTransitionScope.SlideDirection.End
                    )
                },
                enterTransition = {
                    fadeIn(
                        animationSpec = tween(
                            300, easing = LinearEasing
                        )
                    ) + slideIntoContainer(
                        animationSpec = tween(300, easing = EaseIn),
                        towards = AnimatedContentTransitionScope.SlideDirection.Start
                    )
                },
                exitTransition = {
                    ExitTransition.None
                }
            ) {
                MusicProfileFavoriteScreen()
            }

            composable<RouterConstants.ConnectionManagement>(
                popEnterTransition = {
                    EnterTransition.None
                },
                popExitTransition = {
                    fadeOut(
                        animationSpec = tween(
                            300, easing = LinearEasing
                        )
                    ) + slideOutOfContainer(
                        animationSpec = tween(300, easing = EaseOut),
                        towards = AnimatedContentTransitionScope.SlideDirection.End
                    )
                },
                enterTransition = {
                    fadeIn(
                        animationSpec = tween(
                            300, easing = LinearEasing
                        )
                    ) + slideIntoContainer(
                        animationSpec = tween(300, easing = EaseIn),
                        towards = AnimatedContentTransitionScope.SlideDirection.Start
                    )
                },
                exitTransition = {
                    ExitTransition.None
                }) {

                ConnectionManagement()

            }

            composable<RouterConstants.ConnectionInfo>(
                popExitTransition = {
                    fadeOut(
                        animationSpec = tween(
                            300, easing = LinearEasing
                        )
                    ) + slideOutOfContainer(
                        animationSpec = tween(300, easing = EaseOut),
                        towards = AnimatedContentTransitionScope.SlideDirection.End
                    )
                },
                popEnterTransition = {
                    EnterTransition.None
                },
                enterTransition = {
                    fadeIn(
                        animationSpec = tween(
                            300, easing = LinearEasing
                        )
                    ) + slideIntoContainer(
                        animationSpec = tween(300, easing = EaseIn),
                        towards = AnimatedContentTransitionScope.SlideDirection.Start
                    )
                },
                exitTransition = {

                    ExitTransition.None
                }) {
                val connectionInfo = it.toRoute<RouterConstants.ConnectionInfo>()
                ConnectionConfigInfoScreen(
                    modifier = Modifier.padding(paddingValues),
                    connectionId = connectionInfo.connectionId
                )

            }

            composable<RouterConstants.MemoryManagement>(
                popEnterTransition = {
                    EnterTransition.None
                },
                popExitTransition = {
                    fadeOut(
                        animationSpec = tween(
                            300, easing = LinearEasing
                        )
                    ) + slideOutOfContainer(
                        animationSpec = tween(300, easing = EaseOut),
                        towards = AnimatedContentTransitionScope.SlideDirection.End
                    )
                },
                enterTransition = {
                    fadeIn(
                        animationSpec = tween(
                            300, easing = LinearEasing
                        )
                    ) + slideIntoContainer(
                        animationSpec = tween(300, easing = EaseIn),
                        towards = AnimatedContentTransitionScope.SlideDirection.Start
                    )
                },
                exitTransition = {
                    ExitTransition.None
                }) {

                MemoryManagementScreen()

            }

            composable<RouterConstants.ArtistInfo>(
                popEnterTransition = {
                    EnterTransition.None
                },
                popExitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right
                    )
                },
                enterTransition = {

                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Left
                    )
                },
                exitTransition = {
                    ExitTransition.None

                }

            ) {
                val artistInfo = it.toRoute<RouterConstants.ArtistInfo>()
                ArtistInfoScreen(
                    paddingValues = paddingValues,
                    artistId = { artistInfo.artistId })
            }

            composable<RouterConstants.InterfaceSetting>(
                enterTransition = {
                    fadeIn(
                        animationSpec = tween(
                            300, easing = LinearEasing
                        )
                    ) + slideIntoContainer(
                        animationSpec = tween(300, easing = EaseIn),
                        towards = AnimatedContentTransitionScope.SlideDirection.Start
                    )
                },
                popExitTransition = {
                    fadeOut(
                        animationSpec = tween(
                            300, easing = LinearEasing
                        )
                    ) + slideOutOfContainer(
                        animationSpec = tween(300, easing = EaseOut),
                        towards = AnimatedContentTransitionScope.SlideDirection.End
                    )
                },
                exitTransition = {
                    ExitTransition.None
                }
            ) {

                InterfaceSettingScreen()
            }


            composable<RouterConstants.LanguageConfig>(
                enterTransition = {
                    fadeIn(
                        animationSpec = tween(
                            300, easing = LinearEasing
                        )
                    ) + slideIntoContainer(
                        animationSpec = tween(300, easing = EaseIn),
                        towards = AnimatedContentTransitionScope.SlideDirection.Start
                    )
                },
                popExitTransition = {
                    fadeOut(
                        animationSpec = tween(
                            300, easing = LinearEasing
                        )
                    ) + slideOutOfContainer(
                        animationSpec = tween(300, easing = EaseOut),
                        towards = AnimatedContentTransitionScope.SlideDirection.End
                    )
                },
                exitTransition = {
                    ExitTransition.None
                }
            ) {

                LanguageConfigScreen()
            }

            composable<RouterConstants.Genres>(
                popEnterTransition = {
                    EnterTransition.None
                },
                popExitTransition = {
                    fadeOut(
                        animationSpec = tween(
                            300, easing = LinearEasing
                        )
                    ) + slideOutOfContainer(
                        animationSpec = tween(300, easing = EaseOut),
                        towards = AnimatedContentTransitionScope.SlideDirection.End
                    )
                },
                enterTransition = {
                    fadeIn(
                        animationSpec = tween(
                            300, easing = LinearEasing
                        )
                    ) + slideIntoContainer(
                        animationSpec = tween(300, easing = EaseIn),
                        towards = AnimatedContentTransitionScope.SlideDirection.Start
                    )
                },
                exitTransition = {
                    ExitTransition.None
                }
            ) {

                GenresScreen(modifier = Modifier.padding(paddingValues))
            }

            composable<RouterConstants.GenreInfo>(
                enterTransition = {

                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Left
                    )
                },
                exitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right
                    )
                }
            ) {
                val genreInfo =
                    it.toRoute<RouterConstants.GenreInfo>()
                GenresInfoScreen(
                    modifier = Modifier.padding(paddingValues),
                    genreId = genreInfo.genreId
                )
            }

            composable<RouterConstants.About>(
                popEnterTransition = {
                    EnterTransition.None
                },
                popExitTransition = {
                    fadeOut(
                        animationSpec = tween(
                            300, easing = LinearEasing
                        )
                    ) + slideOutOfContainer(
                        animationSpec = tween(300, easing = EaseOut),
                        towards = AnimatedContentTransitionScope.SlideDirection.End
                    )
                },
                enterTransition = {
                    fadeIn(
                        animationSpec = tween(
                            300, easing = LinearEasing
                        )
                    ) + slideIntoContainer(
                        animationSpec = tween(300, easing = EaseIn),
                        towards = AnimatedContentTransitionScope.SlideDirection.Start
                    )
                },
                exitTransition = {
                    ExitTransition.None
                }
            ) {

                AboutScreen()
            }

            composable<RouterConstants.CacheLimit>(
                enterTransition = {

                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Left
                    )
                },
                exitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right
                    )
                }

            ) {
                CacheLimitScreen()
            }
        }
    }

}