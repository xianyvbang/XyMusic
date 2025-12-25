package cn.xybbz.ui.screens

import android.util.Log
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import cn.xybbz.R
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import cn.xybbz.router.AlbumInfo
import cn.xybbz.ui.components.MusicAlbumCardComponent
import cn.xybbz.ui.components.SwipeRefreshVerticalGridListComponent
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.viewmodel.GenresInfoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenresInfoScreen(
    genreId: String,
    genresInfoViewModel: GenresInfoViewModel = hiltViewModel<GenresInfoViewModel, GenresInfoViewModel.Factory>(
        creationCallback = { factory ->
            factory.create(
                genreId = genreId,
            )
        }
    )
) {
    SideEffect {
        Log.d("=====", "MusicAudiobookInfoScreen重组一次")
    }
    val navigator = LocalNavigator.current
    val albumListPage =
        genresInfoViewModel.albumList.collectAsLazyPagingItems()

    XyColumnScreen(
        modifier = Modifier.brashColor(
            topVerticalColor = genresInfoViewModel.backgroundConfig.genresInfoBrash[0],
            bottomVerticalColor = genresInfoViewModel.backgroundConfig.genresInfoBrash[1],
        )
    ) {
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
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_to_genres_list)
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