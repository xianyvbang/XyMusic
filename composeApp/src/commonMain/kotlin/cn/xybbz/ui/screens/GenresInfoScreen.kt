package cn.xybbz.ui.screens

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.common.utils.Log
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import cn.xybbz.router.AlbumInfo
import cn.xybbz.ui.components.MusicAlbumCardComponent
import cn.xybbz.ui.components.SwipeRefreshVerticalGridListComponent
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.viewmodel.GenresInfoViewModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.arrow_back_24px
import xymusic_kmp.composeapp.generated.resources.back_to_genres_list
import cn.xybbz.ui.xy.XyIconButton as IconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenresInfoScreen(
    genreId: String,
    genresInfoViewModel: GenresInfoViewModel = koinViewModel<GenresInfoViewModel> {
        parametersOf(genreId)
    }
) {
    SideEffect {
        Log.d("=====", "MusicAudiobookInfoScreen重组一次")
    }
    val navigator = LocalNavigator.current
    val albumListPage =
        genresInfoViewModel.albumList.collectAsLazyPagingItems()

    XyColumnScreen {
        TopAppBarComponent(
            modifier = Modifier.statusBarsPadding(),
            title = {
                Text(
                    text = genresInfoViewModel.genreInfo?.name ?: "",
                    modifier = Modifier.basicMarquee(
                        iterations = Int.MAX_VALUE
                    )
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = {
                        navigator.goBack()
                    },
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.arrow_back_24px),
                        contentDescription = stringResource(Res.string.back_to_genres_list)
                    )
                }
            }
        )

        SwipeRefreshVerticalGridListComponent(
            modifier = Modifier,
            collectAsLazyPagingItems = albumListPage,
        ) {
            items(
                count = albumListPage.itemCount,
                key = albumListPage.itemKey { it.itemId },
                contentType = albumListPage.itemContentType { MusicTypeEnum.ALBUM.code }) { index ->
                albumListPage[index]?.let { album ->
                    MusicAlbumCardComponent(
                        modifier = Modifier,
                        onItem = { album },
                        onRouter = {
                            navigator.navigate(
                                AlbumInfo(it, MusicDataTypeEnum.ALBUM)
                            )
                        }
                    )

                }
            }
        }
    }
}

