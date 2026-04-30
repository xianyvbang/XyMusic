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

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.ui.components.SettingItemComponent
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.TopAppBarTitle
import cn.xybbz.ui.xy.LazyColumnNotComponent
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyRow
import cn.xybbz.ui.xy.XyText
import cn.xybbz.viewmodel.AboutViewModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.about
import xymusic_kmp.composeapp.generated.resources.app_icon_info
import xymusic_kmp.composeapp.generated.resources.app_name
import xymusic_kmp.composeapp.generated.resources.arrow_back_24px
import xymusic_kmp.composeapp.generated.resources.current_version
import xymusic_kmp.composeapp.generated.resources.function_not_implemented
import xymusic_kmp.composeapp.generated.resources.logo_new
import xymusic_kmp.composeapp.generated.resources.no_official_website_yet
import xymusic_kmp.composeapp.generated.resources.official_website
import xymusic_kmp.composeapp.generated.resources.problem_feedback
import xymusic_kmp.composeapp.generated.resources.return_setting_screen
import cn.xybbz.ui.xy.XyIconButton as IconButton


@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class,
)
@Composable
fun AboutScreen(
    aboutViewModel: AboutViewModel = koinViewModel<AboutViewModel>()
) {
    val navigator = LocalNavigator.current

    val functionNotImplemented = stringResource(Res.string.function_not_implemented)
    val noOfficialWebsiteYet = stringResource(Res.string.no_official_website_yet)
    val logoSize = (128 * LocalDensity.current.fontScale).dp

    XyColumnScreen(
        modifier = Modifier
    ) {
        TopAppBarComponent(
            title = {
                TopAppBarTitle(
                    title = stringResource(Res.string.about)
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

        LazyColumnNotComponent(
            contentPadding = PaddingValues()
        ) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        modifier = Modifier.size(logoSize),
                        painter = painterResource(Res.drawable.logo_new),
                        contentScale = ContentScale.Fit,
                        contentDescription = stringResource(Res.string.app_icon_info)
                    )
                }
            }
            item {
                XyRow(
                    modifier = Modifier,
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    XyText(text = stringResource(Res.string.app_name))
                }

            }
            item {
                SettingItemComponent(
                    title = stringResource(Res.string.current_version),
                    info = aboutViewModel.versionInfo
                ) {

                }

            }
            item {
            }
            item {
                SettingItemComponent(title = stringResource(Res.string.problem_feedback)) {
                    MessageUtils.sendPopTip(functionNotImplemented)
                }
            }

            item {
                SettingItemComponent(title = stringResource(Res.string.official_website)) {
                    MessageUtils.sendPopTip(noOfficialWebsiteYet)
                }
            }
        }
    }
}

