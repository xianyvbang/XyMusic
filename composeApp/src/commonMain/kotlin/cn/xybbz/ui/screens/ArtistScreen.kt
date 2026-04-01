package cn.xybbz.ui.screens


import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.router.ArtistInfo
import cn.xybbz.ui.components.IndexBar
import cn.xybbz.ui.components.MusicArtistCardComponent
import cn.xybbz.ui.components.SwipeRefreshVerticalGridListComponent
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.TopAppBarTitle
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.viewmodel.ArtistViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.arrow_back_24px
import xymusic_kmp.composeapp.generated.resources.artist
import xymusic_kmp.composeapp.generated.resources.favorite_24px
import xymusic_kmp.composeapp.generated.resources.favorite_border_24px
import xymusic_kmp.composeapp.generated.resources.get_all_artists
import xymusic_kmp.composeapp.generated.resources.get_favorite_artists
import xymusic_kmp.composeapp.generated.resources.return_home
import cn.xybbz.ui.xy.XyIconButton as IconButton

/**
 * 艺术家页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistScreen(
    artistViewModel: ArtistViewModel = koinViewModel<ArtistViewModel>(),
) {
    val artistListPaging =
        artistViewModel.artistList.collectAsLazyPagingItems()

    val lazyGridState = rememberLazyGridState()

    val coroutineScope = rememberCoroutineScope()

    val navigator = LocalNavigator.current

    XyColumnScreen {
        TopAppBarComponent(
            modifier = Modifier.statusBarsPadding(),
            title = {
                TopAppBarTitle(
                    title = stringResource(Res.string.artist)
                )
            }, navigationIcon = {
                IconButton(
                    onClick = {
                        navigator.goBack()
                    },
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.arrow_back_24px),
                        contentDescription = stringResource(Res.string.return_home)
                    )
                }
            }, actions = {
                IconButton(onClick = {
                    coroutineScope.launch {
                        if (artistViewModel.sortBy.value.isFavorite == true) {
                            artistViewModel.setFavoriteFilterData(null)
                        } else {
                            artistViewModel.setFavoriteFilterData(true)
                        }
                        delay(500)
                    }.invokeOnCompletion {
//                        artistListPaging.refresh()
                    }
                }) {
                    Icon(
                        painter = painterResource(if (artistViewModel.ifFavorite == true) Res.drawable.favorite_border_24px else Res.drawable.favorite_24px),
                        contentDescription = if (artistViewModel.ifFavorite == true) stringResource(
                            Res.string.get_all_artists
                        ) else stringResource(Res.string.get_favorite_artists),
                        tint = Color.Red
                    )
                }
            })

        IndexBar(
            modifier = Modifier.fillMaxSize(),
            chars = artistViewModel.selectArtistChars,
            onSelect = { value ->
                coroutineScope.launch {
                    val index =
                        artistViewModel.selectIndexBySelectChat(value.toString().lowercase())
                    lazyGridState.scrollToItem(index)
                }
            }
        ) {
            SwipeRefreshVerticalGridListComponent(
                modifier = Modifier,
                lazyGridState = lazyGridState,
                collectAsLazyPagingItems = artistListPaging,
            ) {
                items(
                    count = artistListPaging.itemCount,
                    key = artistListPaging.itemKey { (artist, _) -> artist.artistId },
                    contentType = artistListPaging.itemContentType { MusicTypeEnum.ARTIST.code },
                ) { index ->

                    artistListPaging[index]?.let { (artist, _) ->
                        MusicArtistCardComponent(
                            modifier = Modifier,
                            onItem = { artist },
                            enabled = !it
                        ) { artistId ->
                            navigator.navigate(ArtistInfo(artistId, artist.name ?: ""))
                        }
                    }
                }
            }
        }
    }
}

