package cn.xybbz.ui.screens


import android.util.Log
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.common.enums.SortTypeEnum
import cn.xybbz.compositionLocal.LocalMainViewModel
import cn.xybbz.compositionLocal.LocalNavController
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import cn.xybbz.router.RouterConstants
import cn.xybbz.ui.components.MusicAlbumCardComponent
import cn.xybbz.ui.components.SelectSortBottomSheetComponent
import cn.xybbz.ui.components.SwipeRefreshVerticalGridListComponent
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.xy.XyColumnNotPaddingScreen
import cn.xybbz.viewmodel.AlbumViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumScreen(
    modifier: Modifier = Modifier,
    albumViewModel: AlbumViewModel = hiltViewModel<AlbumViewModel>()
) {

    val navController = LocalNavController.current

    val albumPageListItems =
        albumViewModel.albumPageList.collectAsLazyPagingItems()

    var isSortBottomSheet by remember {
        mutableStateOf(false)
    }
    val mainViewModel = LocalMainViewModel.current

    SelectSortBottomSheetComponent(
        ifDisplay = { isSortBottomSheet },
        onSetDisplay = { isSortBottomSheet = it },
        onSortTypeClick = {
            albumViewModel.setSortedData(it)
        },
        onRefresh = { albumPageListItems.refresh() },
        onSortType = { albumViewModel.sortBy.value.sortType },
        onFilterEraTypeList = { mainViewModel.eraItemList },
        onFilterEraType = { albumViewModel.sortBy.value.eraItem?.id ?: 0 },
        onFilterEraTypeClick = { albumViewModel.setFilterEraType(it) },
        onIfFavorite = { albumViewModel.sortBy.value.isFavorite == true },
        setFavorite = { albumViewModel.setFavorite(it) },
        sortTypeList = listOf(
            SortTypeEnum.CREATE_TIME_ASC,
            SortTypeEnum.CREATE_TIME_DESC,
            SortTypeEnum.ALBUM_NAME_ASC,
            SortTypeEnum.ALBUM_NAME_DESC,
            SortTypeEnum.ARTIST_NAME_ASC,
            SortTypeEnum.ARTIST_NAME_DESC,
            SortTypeEnum.PREMIERE_DATE_ASC,
            SortTypeEnum.PREMIERE_DATE_DESC
        )
    )

    XyColumnNotPaddingScreen(
        modifier = Modifier.brashColor(
            topVerticalColor = Color(0xFF196473),
            bottomVerticalColor = Color(0xFF60318C)
        )
    ) {
        TopAppBarComponent(
            modifier = Modifier.statusBarsPadding(),
            title = {
                Text(
                    text = "专辑",
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
                    isSortBottomSheet = true
                }) {
                    Icon(imageVector = Icons.AutoMirrored.Rounded.Sort, contentDescription = "排序")
                }
            })
        SwipeRefreshVerticalGridListComponent(
            modifier = Modifier,
            collectAsLazyPagingItems = albumPageListItems
        ) {
            items(
                count = albumPageListItems.itemCount,
                key = albumPageListItems.itemKey { it.itemId },
                contentType = albumPageListItems.itemContentType { MusicTypeEnum.ALBUM.code }) { index ->
                MusicAlbumCardComponent(
                    modifier = Modifier,
                    onItem = { albumPageListItems[index] },
                    onRouter = {
                        navController.navigate(
                            RouterConstants.AlbumInfo(
                                it,
                                MusicDataTypeEnum.ALBUM
                            )
                        )
                    }
                )
            }
        }
    }
}