package cn.xybbz.ui.screens


import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.compositionLocal.LocalNavController
import cn.xybbz.router.RouterConstants
import cn.xybbz.ui.components.IndexBar
import cn.xybbz.ui.components.MusicArtistCardComponent
import cn.xybbz.ui.components.SwipeRefreshVerticalGridListComponent
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.xy.XyColumnNotPaddingScreen
import cn.xybbz.viewmodel.ArtistViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 艺术家页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistScreen(
    artistViewModel: ArtistViewModel = hiltViewModel<ArtistViewModel>(),
) {
    val artistListPaging =
        artistViewModel.artistList.collectAsLazyPagingItems()

    val lazyGridState = rememberLazyGridState()

    val coroutineScope = rememberCoroutineScope()

    val navController = LocalNavController.current

    XyColumnNotPaddingScreen(
        modifier = Modifier.brashColor(
            topVerticalColor = Color(0xFF036B41),
            bottomVerticalColor = Color(0xFF196473)
        )
    ) {
        TopAppBarComponent(
            modifier = Modifier.statusBarsPadding(),
            title = {
                Text(
                    text = "艺术家",
                    fontWeight = FontWeight.W900
                )
            }, navigationIcon = {
                IconButton(
                    onClick = {
                        navController.popBackStack()
                    },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回首页"
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
                        artistListPaging.refresh()
                    }
                }) {
                    Icon(
                        imageVector = if (artistViewModel.ifFavorite == true) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = if (artistViewModel.ifFavorite == true) "获取所有艺术家" else "只获取收藏艺术家",
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
                    key = artistListPaging.itemKey { it.artistId },
                    contentType = artistListPaging.itemContentType { MusicTypeEnum.ARTIST.code },
                ) { index ->

                    artistListPaging[index]?.let { artist ->
                        MusicArtistCardComponent(
                            modifier = Modifier,
                            onItem = { artist }
                        ) {
                            navController.navigate(RouterConstants.ArtistInfo(it))
                        }
                    }
                }
            }
        }
    }
}
