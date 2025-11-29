package cn.xybbz.ui.screens

import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cn.xybbz.R
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.compositionLocal.LocalNavController
import cn.xybbz.ui.components.LazyLoadingAndStatus
import cn.xybbz.ui.components.MusicItemComponent
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.show
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.xy.LazyColumnNotComponent
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.viewmodel.DailyRecommendViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyRecommendScreen(
    dailyRecommendViewModel: DailyRecommendViewModel = hiltViewModel<DailyRecommendViewModel>()
) {

    val coroutineScope = rememberCoroutineScope()
    val navController = LocalNavController.current
    val favoriteList by dailyRecommendViewModel.favoriteRepository.favoriteSet.collectAsState()
    val downloadMusicIds by dailyRecommendViewModel.downloadRepository.musicIdsFlow.collectAsState()
    val state = rememberPullToRefreshState()

    var isRefreshing by remember {
        mutableStateOf(false)
    }

    XyColumnScreen(
        modifier = Modifier
            .brashColor(
                topVerticalColor = dailyRecommendViewModel.backgroundConfig.favoriteBrash[0],
                bottomVerticalColor = dailyRecommendViewModel.backgroundConfig.favoriteBrash[0]
            )
    ) {
        TopAppBarComponent(
            modifier = Modifier.statusBarsPadding(),
            title = {
                Text(
                    text = stringResource(R.string.daily_recommendations),
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


        PullToRefreshBox(
            state = state,
            isRefreshing = isRefreshing,
            onRefresh = {
                coroutineScope.launch {
                    isRefreshing = true
                    dailyRecommendViewModel.generateRecommendedMusicList()
                }.invokeOnCompletion {
                    isRefreshing = false
                }
            }
        ) {
            LazyColumnNotComponent() {
                items(
                    dailyRecommendViewModel.recommendedMusicList,
                    key = { extend -> extend.music.itemId },
                    contentType = { _-> MusicTypeEnum.MUSIC }
                ) { musicExtend ->
                    MusicItemComponent(
                        itemId = musicExtend.music.itemId,
                        name = musicExtend.music.name,
                        album = musicExtend.music.album,
                        artists = musicExtend.music.artists,
                        pic = musicExtend.music.pic,
                        codec = musicExtend.music.codec,
                        bitRate = musicExtend.music.bitRate,
                        onIfFavorite = {
                            musicExtend.music.itemId in favoriteList
                        },
                        ifDownload = musicExtend.music.itemId in downloadMusicIds,
                        textColor = if (dailyRecommendViewModel.musicController.musicInfo?.itemId == musicExtend.music.itemId)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface,
                        backgroundColor = Color.Transparent,
                        onMusicPlay = {
                            coroutineScope.launch {
                                dailyRecommendViewModel.musicList(
                                    it
                                )
                            }
                        },
                        trailingOnClick = {
                            musicExtend.music.show()
                        },
                        trailingOnSelectClick = {
                            coroutineScope.launch {
                                dailyRecommendViewModel.getMusicInfo(musicExtend.music.itemId)
                                    ?.show()
                            }
                        }
                    )
                }
                item {
                    LazyLoadingAndStatus(
                        text = stringResource(R.string.reached_bottom),
                        ifLoading = false
                    )
                }
            }
        }

    }
}

