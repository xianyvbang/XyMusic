package cn.xybbz.ui.screens


import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import cn.xybbz.R
import cn.xybbz.ui.components.MusicPageListComponent
import cn.xybbz.viewmodel.FavoriteViewModel

/**
 * 收藏界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen(
    favoriteViewModel: FavoriteViewModel = hiltViewModel<FavoriteViewModel>(),
) {
    val favoriteMusicListPage =
        favoriteViewModel.favoriteMusicList.collectAsLazyPagingItems()

    MusicPageListComponent(
        brashList = favoriteViewModel.backgroundConfig.favoriteBrash,
        title = stringResource(R.string.my_favorites),
        musicListPage = favoriteMusicListPage,
        favoriteRepository = favoriteViewModel.favoriteRepository,
        downloadRepository = favoriteViewModel.downloadRepository,
        musicController = favoriteViewModel.musicController,
        getMusicInfo = { favoriteViewModel.getMusicInfo(it) },
        onMusicPlay = { playInfo, index ->
            favoriteViewModel.musicPlayContext.favorite(
                playInfo,
                index = index
            )
        })
}

