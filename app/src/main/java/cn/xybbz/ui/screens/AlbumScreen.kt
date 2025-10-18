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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import cn.xybbz.R
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
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.viewmodel.AlbumViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumScreen(
    albumViewModel: AlbumViewModel = hiltViewModel<AlbumViewModel>()
) {

    val navController = LocalNavController.current

    val albumPageListItems =
        albumViewModel.albumPageList.collectAsLazyPagingItems()

    val sortBy by albumViewModel.sortBy.collectAsState()

    val mainViewModel = LocalMainViewModel.current



    XyColumnScreen(
        modifier = Modifier.brashColor(
            topVerticalColor = albumViewModel.backgroundConfig.albumBrash[0],
            bottomVerticalColor = albumViewModel.backgroundConfig.albumBrash[1]
        )
    ) {
        TopAppBarComponent(
            modifier = Modifier.statusBarsPadding(),
            title = {
                Text(
                    text = stringResource(R.string.album),
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
                        contentDescription = stringResource(R.string.return_home)
                    )
                }
            }, actions = {
                SelectSortBottomSheetComponent(
                    onSortTypeClick = {
                        albumViewModel.setSortedData(it, { albumPageListItems.refresh() })
                    },
                    onSortType = { sortBy.sortType },
                    onFilterEraTypeList = { mainViewModel.eraItemList },
                    onFilterEraTypeClick = {
                        albumViewModel.setFilterEraType(
                            it,
                            { albumPageListItems.refresh() })
                    },
                    onIfFavorite = { sortBy.isFavorite == true },
                    setFavorite = {
                        albumViewModel.setFavorite(
                            it,
                            { albumPageListItems.refresh() })
                    },
                    sortTypeList = listOf(
                        SortTypeEnum.CREATE_TIME_ASC,
                        SortTypeEnum.CREATE_TIME_DESC,
                        SortTypeEnum.ALBUM_NAME_ASC,
                        SortTypeEnum.ALBUM_NAME_DESC,
                        SortTypeEnum.ARTIST_NAME_ASC,
                        SortTypeEnum.ARTIST_NAME_DESC,
                        SortTypeEnum.PREMIERE_DATE_ASC,
                        SortTypeEnum.PREMIERE_DATE_DESC
                    ),
                    onIfSelectOneYear = { albumViewModel.dataSourceManager.dataSourceType?.ifAlbumSelectOneYear },
                    onIfStartEndYear = { albumViewModel.dataSourceManager.dataSourceType?.ifStartEndYear },
                    onIfSort = { albumViewModel.dataSourceManager.dataSourceType?.ifAlbumSort },
                    onIfFavoriteFilter = { albumViewModel.dataSourceManager.dataSourceType?.ifAlbumFavoriteFilter },
                    onYearSet = { mainViewModel.yearSet },
                    onSelectYear = { sortBy.yearList?.get(0) },
                    onSetSelectYear = { year ->
                        year?.let {
                            albumViewModel.setFilterYear(
                                listOf(it),
                                { albumPageListItems.refresh() })
                        }
                    },
                    onSelectRangeYear = { sortBy.yearList },
                    onIfYearFilter = { albumViewModel.dataSourceManager.dataSourceType?.ifYearFilter },
                    onSetSelectRangeYear = { years ->
                        albumViewModel.setFilterYear(years.mapNotNull { it },
                            { albumPageListItems.refresh() })
                    }
                )
            })
        SwipeRefreshVerticalGridListComponent(
            modifier = Modifier.fillMaxSize(),
            collectAsLazyPagingItems = albumPageListItems
        ) {
            items(
                count = albumPageListItems.itemCount,
                key = albumPageListItems.itemKey {item ->  item.itemId },
                contentType = albumPageListItems.itemContentType { MusicTypeEnum.ALBUM.code }) { index ->
                MusicAlbumCardComponent(
                    modifier = Modifier,
                    onItem = { albumPageListItems[index] },
                    enabled = !it,
                    onRouter = {itemId->
                        //取消刷新
                        navController.navigate(
                            RouterConstants.AlbumInfo(
                                itemId,
                                MusicDataTypeEnum.ALBUM
                            )
                        )
                    }
                )
            }
        }
    }
}