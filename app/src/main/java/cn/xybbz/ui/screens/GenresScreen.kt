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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import cn.xybbz.compositionLocal.LocalNavController
import cn.xybbz.router.RouterConstants
import cn.xybbz.ui.components.MusicGenreCardComponent
import cn.xybbz.ui.components.SwipeRefreshVerticalGridListComponent
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.xy.XyColumnNotPaddingScreen
import cn.xybbz.viewmodel.GenresViewModel

/**
 * 流派页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenresScreen(
    modifier: Modifier = Modifier,
    genresViewModel: GenresViewModel = hiltViewModel<GenresViewModel>()
) {


    val navController = LocalNavController.current

    val genreLazyPagingItems = genresViewModel.genresPage.collectAsLazyPagingItems()


    XyColumnNotPaddingScreen(
        modifier = Modifier.brashColor(
            Color(0xFF5E0A69),
            Color(0xFF441172)
        )
    ) {
        TopAppBarComponent(
            modifier = Modifier.statusBarsPadding(),
            title = {
                Text(
                    text = "流派",
                    fontWeight = FontWeight.W900
                )
            }, navigationIcon = {
                IconButton(onClick = composeClick { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回数据中心"
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
                        enabled = it,
                        onRouter = {
                            navController.navigate(RouterConstants.GenreInfo(genre.itemId))
                        }
                    )
                }
            }
        }
    }
}