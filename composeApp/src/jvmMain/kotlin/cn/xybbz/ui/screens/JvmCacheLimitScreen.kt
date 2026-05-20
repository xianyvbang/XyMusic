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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import cn.xybbz.localdata.enums.CacheUpperLimitEnum
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.TopAppBarTitle
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.LazyColumnNotComponent
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyItemRadioButton
import cn.xybbz.viewmodel.CacheLimitViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.music_cache_limit_title

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JvmCacheLimitScreen(
    cacheLimitViewModel: CacheLimitViewModel = koinViewModel<CacheLimitViewModel>()
) {

    val coroutineScope = rememberCoroutineScope()
    val cacheLimitOptions = CacheUpperLimitEnum.entries.filterNot { it == CacheUpperLimitEnum.Auto }

    LaunchedEffect(Unit) {
        if (cacheLimitViewModel.cacheUpperLimit == CacheUpperLimitEnum.Auto) {
            cacheLimitViewModel.setCacheUpperLimitData(CacheUpperLimitEnum.ThreeG)
        }
    }

    XyColumnScreen{
        TopAppBarComponent(
            title = {
                TopAppBarTitle(
                    title = stringResource(Res.string.music_cache_limit_title)
                )
            })

        LazyColumnNotComponent(modifier = Modifier) {
            items(cacheLimitOptions) {
                Column {
                    XyItemRadioButton(
                        text = it.message,
                        sub = null,
                        selected = cacheLimitViewModel.cacheUpperLimit == it,
                        onClick = {
                            coroutineScope.launch {
                                cacheLimitViewModel.setCacheUpperLimitData(it)

                            }
                        })
                    HorizontalDivider(modifier = Modifier.padding(horizontal = XyTheme.dimens.outerHorizontalPadding))
                }
            }
        }
    }
}



