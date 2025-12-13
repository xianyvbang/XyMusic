package cn.xybbz.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import cn.xybbz.R
import cn.xybbz.compositionLocal.LocalNavController
import cn.xybbz.router.RouterConstants
import cn.xybbz.ui.components.MusicGenreCardComponent
import cn.xybbz.ui.components.SwipeRefreshVerticalGridListComponent
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.TopAppBarTitle
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.viewmodel.GenresViewModel

/**
 * 流派页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenresScreen(
    genresViewModel: GenresViewModel = hiltViewModel<GenresViewModel>()
) {
    val navController = LocalNavController.current
    val genreLazyPagingItems = genresViewModel.genresPage.collectAsLazyPagingItems()


    XyColumnScreen(
        modifier = Modifier.brashColor(
            topVerticalColor = genresViewModel.backgroundConfig.genresBrash[0],
            bottomVerticalColor = genresViewModel.backgroundConfig.genresBrash[1]
        )
    ) {
        TopAppBarComponent(
            modifier = Modifier.statusBarsPadding(),
            title = {
                TopAppBarTitle(
                    title = stringResource(R.string.genres)
                )
            }, navigationIcon = {
                IconButton(onClick = composeClick { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.return_home)
                    )
                }
            })


        SwipeRefreshVerticalGridListComponent(
            modifier = Modifier.fillMaxSize(),
            collectAsLazyPagingItems = genreLazyPagingItems,
        ) {
            items(
                genreLazyPagingItems.itemCount,
                key = genreLazyPagingItems.itemKey { item -> item.itemId },
                contentType = genreLazyPagingItems.itemContentType { it.name }
            ) { index ->
                genreLazyPagingItems[index]?.let { genre ->
                    MusicGenreCardComponent(
                        onItem = { genre },
                        enabled = !it,
                        onRouter = {
                            navController.navigate(RouterConstants.GenreInfo(genre.itemId))
                        }
                    )
                }
            }
        }
    }
}