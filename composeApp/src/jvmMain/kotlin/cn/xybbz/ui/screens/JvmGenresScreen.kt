package cn.xybbz.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.router.GenreInfo
import cn.xybbz.ui.components.JvmPageTitleBar
import cn.xybbz.ui.components.JvmVerticalGridListComponent
import cn.xybbz.ui.components.MusicGenreCardComponent
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.viewmodel.GenresViewModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic.composeapp.generated.resources.Res
import xymusic.composeapp.generated.resources.arrow_back_24px
import xymusic.composeapp.generated.resources.genres
import xymusic.composeapp.generated.resources.return_home
import cn.xybbz.ui.xy.XyIconButton as IconButton

/**
 * 流派页面
 */
@Composable
fun JvmGenresScreen(
    genresViewModel: GenresViewModel = koinViewModel<GenresViewModel>()
) {
    val navigator = LocalNavigator.current
    val genreLazyPagingItems = genresViewModel.genresPage.collectAsLazyPagingItems()


    XyColumnScreen {
        // 与设置页、连接管理页共用统一页头
        JvmPageTitleBar(title = stringResource(Res.string.genres))

        JvmVerticalGridListComponent(
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
                        genre = genre,
                        onRouter = {
                            navigator.navigate(GenreInfo(genre.itemId))
                        }
                    )
                }
            }
        }
    }
}



