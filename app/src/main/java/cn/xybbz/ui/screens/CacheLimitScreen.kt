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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cn.xybbz.R
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.localdata.enums.CacheUpperLimitEnum
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.TopAppBarTitle
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.LazyColumnNotComponent
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyItemRadioButton
import cn.xybbz.ui.xy.XyItemTextHorizontal
import cn.xybbz.viewmodel.CacheLimitViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CacheLimitScreen(
    cacheLimitViewModel: CacheLimitViewModel = hiltViewModel<CacheLimitViewModel>()
) {

    val context = LocalContext.current
    val navigator = LocalNavigator.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        cacheLimitViewModel.getAutomaticCacheSize()
    }

    XyColumnScreen(
        modifier = Modifier.brashColor(
            topVerticalColor = cacheLimitViewModel.backgroundConfig.cacheLimitBrash[0],
            bottomVerticalColor = cacheLimitViewModel.backgroundConfig.cacheLimitBrash[1]
        )
    ) {
        TopAppBarComponent(
            modifier = Modifier.statusBarsPadding(),
            title = {
                TopAppBarTitle(
                    title = stringResource(R.string.music_cache_limit_title)
                )
            }, navigationIcon = {
                IconButton(
                    onClick = {
                        navigator.goBack()
                    },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.return_setting_screen)
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
                                R.string.current_auto_cache_limit,
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
                XyItemTextHorizontal(
                    text = stringResource(R.string.auto_cache_limit_description).trimIndent(),
                    modifier = Modifier.padding(top = XyTheme.dimens.outerVerticalPadding)
                )
            }
        }
    }
}