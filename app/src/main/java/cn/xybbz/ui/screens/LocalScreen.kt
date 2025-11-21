package cn.xybbz.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.xybbz.R
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.compositionLocal.LocalNavController
import cn.xybbz.ui.components.MusicItemComponent
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.show
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.LazyColumnNotComponent
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.viewmodel.LocalViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalScreen(localViewModel: LocalViewModel = hiltViewModel<LocalViewModel>()) {

    val navController = LocalNavController.current
    val coroutineScope = rememberCoroutineScope()
    val downloadMusicList by localViewModel.musicDownloadInfo.collectAsStateWithLifecycle()
    val favoriteList by localViewModel.favoriteRepository.favoriteMap.collectAsState()

    XyColumnScreen(
        modifier = Modifier
    ) {
        TopAppBarComponent(
            modifier = Modifier.statusBarsPadding(),
            title = {
                Text(
                    text = stringResource(R.string.local_music),
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

        LazyColumnNotComponent(
            contentPadding = PaddingValues(
                XyTheme.dimens.outerHorizontalPadding
            ),
            verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.innerVerticalPadding),
        ) {
            itemsIndexed(
                downloadMusicList,
                key = { _, item -> item.id },
                contentType = { _, _ -> MusicTypeEnum.MUSIC }
            ) { index, download ->
                download.music?.let { music ->
                    MusicItemComponent(
                        onMusicData = { music },
                        onIfFavorite = {
                            if (favoriteList.containsKey(music.itemId)) {
                                favoriteList.getOrDefault(music.itemId, false)
                            } else {
                                music.ifFavoriteStatus
                            }
                        },
                        textColor = if (localViewModel.musicController.musicInfo?.itemId == music.itemId)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface,
                        backgroundColor = Color.Transparent,
                        onMusicPlay = {
                            localViewModel.musicPlayContext.favorite(
                                it,
                                index = index
                            )
                        },
                        trailingOnClick = {
                            coroutineScope.launch {
                                music.show()
                            }
                        }
                    )
                }
            }
        }
    }
}