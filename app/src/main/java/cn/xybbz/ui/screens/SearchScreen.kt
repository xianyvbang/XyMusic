package cn.xybbz.ui.screens


import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import cn.xybbz.compositionLocal.LocalNavController
import cn.xybbz.config.favorite.FavoriteRepository
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.data.artist.XyArtist
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.data.search.SearchHistory
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import cn.xybbz.router.RouterConstants
import cn.xybbz.ui.components.LazyColumnNotComponent
import cn.xybbz.ui.components.LazyLoadingAndStatus
import cn.xybbz.ui.components.LazyRowComponent
import cn.xybbz.ui.components.MusicAlbumCardComponent
import cn.xybbz.ui.components.MusicArtistCardComponent
import cn.xybbz.ui.components.MusicItemComponent
import cn.xybbz.ui.components.SearchRecordComponent
import cn.xybbz.ui.components.show
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyColumnNotVerticalPadding
import cn.xybbz.ui.xy.XyItemBigTitle
import cn.xybbz.ui.xy.XyItemTextLarge
import cn.xybbz.viewmodel.SearchViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewSearchScreen(
    searchViewModel: SearchViewModel = hiltViewModel<SearchViewModel>()
) {
    val navHostController = LocalNavController.current

//    var query by remember { mutableStateOf("") }

    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(text = "", selection = TextRange("".length)))
    }

    SideEffect {
        Log.i("=====", "SearchScreen重载")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .brashColor()
    ) {
        // 顶部搜索栏
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            title = {
                TextField(
                    value = textFieldValue,
                    onValueChange = {
                        textFieldValue = it

                        if (textFieldValue.text.isBlank()) {
                            searchViewModel.updateIfShowSearchResult(false)
                        }
                    },
                    textStyle = MaterialTheme.typography.bodySmall,
                    placeholder = { XyItemTextLarge(text = "搜索音乐 / 专辑 / 艺术家") },
                    leadingIcon = {
                        Icon(
                            Icons.Rounded.Search,
                            contentDescription = "搜索框图标",
                            tint = Color.Gray
                        )
                    },
                    trailingIcon = if (textFieldValue.text.isNotBlank()) {
                        {
                            IconButton(onClick = {
                                textFieldValue = textFieldValue.copy("")
                                searchViewModel.updateIfShowSearchResult(false)
                            }) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "清除",
                                    tint = Color.Gray
                                )
                            }
                        }
                    } else null,
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        focusedLabelColor = Color.DarkGray,
                        unfocusedLabelColor = Color.DarkGray,
                        cursorColor = Color.White,
                        disabledContainerColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        searchViewModel.onSearch(
                            textFieldValue.text
                        )
                    }),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.onSurfaceVariant,
                            shape = RoundedCornerShape(XyTheme.dimens.corner)
                        )
                )
            },
            navigationIcon = {
                IconButton(onClick = {
                    navHostController.popBackStack()
                }) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "返回")
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        AnimatedContent(targetState = searchViewModel.ifShowSearchResult) { bool ->
            if (bool) {
                SearchResultScreen(
                    musicList = searchViewModel.musicList,
                    albumList = searchViewModel.albumList,
                    artistList = searchViewModel.artistList,
                    onAddMusic = {
                        searchViewModel.addMusic(it)
                    },
                    onLoadingState = {
                        searchViewModel.isSearchLoad
                    },
                    favoriteRepository = searchViewModel.favoriteRepository
                )
            } else {
                HistoryAndHintList(
                    onClick = { search ->
                        textFieldValue =
                            TextFieldValue(text = search, selection = TextRange(search.length))
                        searchViewModel.onSearch(search)
                    },
                    onClear = {
                        //清空搜索历史
                        searchViewModel.clearSearchHistory()
                    },
                    onHistoryList = { searchViewModel.searchHistory })
            }
        }
    }
}

/**
 * 列列表更改
 * @param [onSearchText] 搜索文本
 * @param [onClick] 点击方法
 * @param [onClear] 清除方法
 * @param [onList] 列表数据
 * @param [onIfSearchHintLoading] 搜索提示是否在加载中
 * @param [onErrorText] 错误文本
 * @param [onMusicSearchHintList] 搜索提示数据获取调用方法
 * @param [onHistoryList] 搜索历史列表
 */
@Composable
private fun HistoryAndHintList(
    onClick: (String) -> Unit,
    onClear: () -> Unit,
    onHistoryList: () -> List<SearchHistory>,
) {
    XyColumnNotVerticalPadding(
        horizontalAlignment = Alignment.Start,
        backgroundColor = Color.Transparent
    ) {
        SearchRecordComponent(
            onHistoryList(),
            "搜索历史",
            onClick = onClick,
            onClear = onClear
        )
    }
}


/**
 * 搜索结果页面
 */
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun SearchResultScreen(
    musicList: List<XyMusic>,
    albumList: List<XyAlbum>,
    artistList: List<XyArtist>,
    onAddMusic: (XyMusic) -> Unit,
    onLoadingState: () -> Boolean,
    favoriteRepository: FavoriteRepository
) {
    val navController = LocalNavController.current

    val favoriteList by favoriteRepository.favoriteMap.collectAsState()


    LazyColumnNotComponent(
        modifier = Modifier
            .padding(top = XyTheme.dimens.outerVerticalPadding)
            .fillMaxSize()
    ) {

        if (onLoadingState()) {
            item {
                LazyLoadingAndStatus(
                    text = "加载中...",
                    ifLoading = true
                )
            }
        } else {
            if (artistList.isNotEmpty()) {
                item {
                    XyItemBigTitle(text = "歌手")
                }
                item {
                    LazyRowComponent(modifier = Modifier.padding(start = XyTheme.dimens.innerHorizontalPadding)) {
                        items(artistList, key = { it.artistId }) { artist ->
                            MusicArtistCardComponent(
                                onItem = { artist },
                                onRouter = {
                                    navController.navigate(RouterConstants.ArtistInfo(it))
                                }
                            )
                        }
                    }
                }
            }

            if (albumList.isNotEmpty()) {
                item {
                    XyItemBigTitle(text = "专辑")
                }
                item {
                    LazyRowComponent(modifier = Modifier.padding(start = XyTheme.dimens.innerHorizontalPadding)) {
                        items(albumList, key = { it.itemId }) { album ->
                            MusicAlbumCardComponent(
                                onItem = { album },
                                onRouter = {
                                    navController.navigate(
                                        RouterConstants.AlbumInfo(
                                            it,
                                            MusicDataTypeEnum.ALBUM
                                        )
                                    )
                                })

                        }
                    }
                }
            }

            if (musicList.isNotEmpty()) {
                item {
                    XyItemBigTitle(text = "歌曲")
                }
                itemsIndexed(musicList, key = { _, music -> music.itemId }) { index, music ->
                    MusicItemComponent(
                        onMusicData = { music },
                        onIfFavorite = {
                            if (favoriteList.containsKey(music.itemId)) {
                                favoriteList.getOrDefault(music.itemId, false)
                            } else {
                                music.ifFavoriteStatus
                            }
                        },
                        onMusicPlay = {
                            onAddMusic(
                                music
                            )
                        },
                        trailingOnClick = {
                            music.show()
                        }
                    )
                }
            }

            item {
                LazyLoadingAndStatus(
                    text = "已经到底了",
                    ifLoading = false
                )
            }
        }


    }
}

