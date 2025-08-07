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
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.compositionLocal.LocalNavController
import cn.xybbz.ui.components.MusicItemComponent
import cn.xybbz.ui.components.SwipeRefreshListComponent
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.show
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.xy.XyColumnNotPaddingScreen
import cn.xybbz.viewmodel.MusicFavoriteViewModel
import kotlinx.coroutines.launch

/**
 * 个人中心收藏界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicProfileFavoriteScreen(
    musicFavoriteViewModel: MusicFavoriteViewModel = hiltViewModel<MusicFavoriteViewModel>(),
) {
    val coroutineScope = rememberCoroutineScope()
    val navController = LocalNavController.current
    val favoriteMusicListPage =
        musicFavoriteViewModel.favoriteMusicList.collectAsLazyPagingItems()
    val favoriteList by musicFavoriteViewModel.favoriteRepository.favoriteMap.collectAsState()

    XyColumnNotPaddingScreen(
        modifier = Modifier
            .brashColor(
                Color(0xFF6C1577),
                Color(0xFFCC6877)
            )
    ) {
        TopAppBarComponent(
            modifier = Modifier.statusBarsPadding(),
            title = {
                Text(
                    text = "我的收藏",
                    fontWeight = FontWeight.W900
                )
            }, navigationIcon = {
                IconButton(onClick = composeClick { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回数据中心"
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
                            textColor = if (musicFavoriteViewModel.musicController.musicInfo?.itemId == music.itemId)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface,
                            backgroundColor = Color.Transparent,
                            onMusicPlay = {
                                coroutineScope.launch {
                                    musicFavoriteViewModel.musicPlayContext.favorite(
                                        it,
                                        index = index
                                    )
                                }
                            },
                            trailingOnClick = {
                                coroutineScope.launch {
                                    musicFavoriteViewModel.getMusicInfo(music.itemId)
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

