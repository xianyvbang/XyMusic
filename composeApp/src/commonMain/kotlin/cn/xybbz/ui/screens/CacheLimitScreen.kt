/*
 *   XyMusic
 *   Copyright (C) 2023 xianyvbang
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package cn.xybbz.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.localdata.enums.CacheUpperLimitEnum
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.TopAppBarTitle
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.LazyColumnNotComponent
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyItemRadioButton
import cn.xybbz.ui.xy.XyTextSubSmall
import cn.xybbz.viewmodel.CacheLimitViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.arrow_back_24px
import xymusic_kmp.composeapp.generated.resources.auto_cache_limit_description
import xymusic_kmp.composeapp.generated.resources.current_auto_cache_limit
import xymusic_kmp.composeapp.generated.resources.music_cache_limit_title
import xymusic_kmp.composeapp.generated.resources.return_setting_screen
import cn.xybbz.ui.xy.XyIconButton as IconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CacheLimitScreen(
    cacheLimitViewModel: CacheLimitViewModel = koinViewModel<CacheLimitViewModel>()
) {

    val navigator = LocalNavigator.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        cacheLimitViewModel.getAutomaticCacheSize()
    }

    XyColumnScreen{
        TopAppBarComponent(
            modifier = Modifier.statusBarsPadding(),
            title = {
                TopAppBarTitle(
                    title = stringResource(Res.string.music_cache_limit_title)
                )
            }, navigationIcon = {
                IconButton(
                    onClick = {
                        navigator.goBack()
                    },
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.arrow_back_24px),
                        contentDescription = stringResource(Res.string.return_setting_screen)
                    )
                }
            })

        LazyColumnNotComponent(modifier = Modifier) {
            items(CacheUpperLimitEnum.entries) {
                Column {
                    XyItemRadioButton(
                        text = it.message,
                        sub = if (it == CacheUpperLimitEnum.Auto)
                            stringResource(
                                Res.string.current_auto_cache_limit,
                                cacheLimitViewModel.cacheSizeInfo
                            )
                        else null,
                        selected = cacheLimitViewModel.cacheUpperLimit == it,
                        onClick = {
                            coroutineScope.launch {
                                cacheLimitViewModel.setCacheUpperLimitData(it)

                            }
                        })
                    HorizontalDivider(modifier = Modifier.padding(horizontal = XyTheme.dimens.outerHorizontalPadding))
                }
            }
            item {
                XyTextSubSmall(
                    text = stringResource(Res.string.auto_cache_limit_description).trimIndent(),
                    modifier = Modifier.padding(
                        top = XyTheme.dimens.innerVerticalPadding,
                        start = XyTheme.dimens.innerHorizontalPadding,
                        end = XyTheme.dimens.innerHorizontalPadding
                    )
                )
            }
        }
    }
}

