package cn.xybbz.ui.screens


import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import cn.xybbz.R
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.compositionLocal.LocalNavController
import cn.xybbz.ui.components.MusicItemComponent
import cn.xybbz.ui.components.SwipeRefreshListComponent
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.show
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.viewmodel.FavoriteViewModel
import kotlinx.coroutines.launch

/**
 * 收藏界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen(
    favoriteViewModel: FavoriteViewModel = hiltViewModel<FavoriteViewModel>(),
) {
    val coroutineScope = rememberCoroutineScope()
    val navController = LocalNavController.current
    val favoriteMusicListPage =
        favoriteViewModel.favoriteMusicList.collectAsLazyPagingItems()
    val favoriteList by favoriteViewModel.favoriteRepository.favoriteMap.collectAsState()

    XyColumnScreen(
        modifier = Modifier
            .brashColor(
                topVerticalColor = favoriteViewModel.backgroundConfig.favoriteBrash[0],
                bottomVerticalColor = favoriteViewModel.backgroundConfig.favoriteBrash[0]
            )
    ) {
        TopAppBarComponent(
            modifier = Modifier.statusBarsPadding(),
            title = {
                Text(
                    text = stringResource(R.string.my_favorites),
                    fontWeight = FontWeight.W900
                )
            }, navigationIcon = {
                IconButton(onClick = composeClick { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.return_home)
                    )
                }
            })
        SwipeRefreshListComponent(
            collectAsLazyPagingItems = favoriteMusicListPage,
            listContent = { collectAsLazyPagingItems ->
                items(
                    collectAsLazyPagingItems.itemCount,
                    key = collectAsLazyPagingItems.itemKey { item -> item.itemId },
                    contentType = collectAsLazyPagingItems.itemContentType { MusicTypeEnum.MUSIC }
                ) { index ->
                    collectAsLazyPagingItems[index]?.let { music ->
                        MusicItemComponent(
                            onMusicData = { music },
                            onIfFavorite = {
                                if (favoriteList.containsKey(music.itemId)) {
                                    favoriteList.getOrDefault(music.itemId, false)
                                } else {
                                    music.ifFavoriteStatus
                                }
                            },
                            textColor = if (favoriteViewModel.musicController.musicInfo?.itemId == music.itemId)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface,
                            backgroundColor = Color.Transparent,
                            onMusicPlay = {
                                coroutineScope.launch {
                                    favoriteViewModel.musicPlayContext.favorite(
                                        it,
                                        index = index
                                    )
                                }
                            },
                            trailingOnClick = {
                                coroutineScope.launch {
                                    favoriteViewModel.getMusicInfo(music.itemId)
                                        ?.show()
                                }
                            }
                        )
                    }
                }
            }
        )
    }

}

