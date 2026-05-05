package cn.xybbz.ui.screens


import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.ui.components.FavoriteIconButton
import cn.xybbz.ui.components.JvmIndexBar
import cn.xybbz.ui.components.JvmVerticalGridListComponent
import cn.xybbz.ui.components.MusicArtistCardComponent
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.TopAppBarTitle
import cn.xybbz.ui.components.rememberMusicArtistClickHandler
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.viewmodel.ArtistViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.artist
import xymusic_kmp.composeapp.generated.resources.get_all_artists
import xymusic_kmp.composeapp.generated.resources.get_favorite_artists

/**
 * 艺术家页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JvmArtistScreen(
    artistViewModel: ArtistViewModel = koinViewModel<ArtistViewModel>(),
) {
    val artistListPaging =
        artistViewModel.artistList.collectAsLazyPagingItems()

    val lazyGridState = rememberLazyGridState()

    val coroutineScope = rememberCoroutineScope()

    // 艺术家列表卡片点击也使用统一处理器，后续逻辑变化只需要改一处。
    val artistClickHandler = rememberMusicArtistClickHandler()

    XyColumnScreen {
        TopAppBarComponent(
            title = {
                TopAppBarTitle(
                    title = stringResource(Res.string.artist)
                )
            }, actions = {
                val isFavoriteFilter = artistViewModel.ifFavorite == true
                val favoriteFilterText = if (isFavoriteFilter) {
                    stringResource(Res.string.get_all_artists)
                } else {
                    stringResource(Res.string.get_favorite_artists)
                }
                FavoriteIconButton(
                    isFavorite = isFavoriteFilter,
                    contentDescription = favoriteFilterText,
                    tooltip = favoriteFilterText,
                    normalTint = Color.Red,
                    onClick = {
                        coroutineScope.launch {
                            if (artistViewModel.sortBy.value.isFavorite == true) {
                                artistViewModel.setFavoriteFilterData(null)
                            } else {
                                artistViewModel.setFavoriteFilterData(true)
                            }
                        }
                    })
            })

        JvmIndexBar(
            modifier = Modifier.fillMaxSize(),
            chars = artistViewModel.selectArtistChars,
            onSelect = { value ->
                coroutineScope.launch {
                    val index =
                        artistViewModel.selectIndexBySelectChat(value.toString().lowercase())
                    lazyGridState.scrollToItem((index - 1).coerceAtLeast(0))
                }
            }
        ) {
            JvmVerticalGridListComponent(
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
                            artist = artist,
                        ) { artistId ->
                            artistClickHandler.openArtist(artistId, artist.name)
                        }
                    }
                }
            }
        }
    }
}



