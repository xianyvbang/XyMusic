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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.localdata.enums.LanguageType
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.TopAppBarTitle
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.LazyColumnNotComponent
import cn.xybbz.ui.xy.RoundedSurfaceColumn
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyItemRadioButton
import cn.xybbz.viewmodel.LanguageConfigViewModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.arrow_back_24px
import xymusic_kmp.composeapp.generated.resources.language
import xymusic_kmp.composeapp.generated.resources.return_setting_screen
import cn.xybbz.ui.xy.XyIconButton as IconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageConfigScreen(
    languageConfigViewModel: LanguageConfigViewModel = koinViewModel<LanguageConfigViewModel>()
) {

    val navigator = LocalNavigator.current

    val languageType by remember {
        derivedStateOf {
            languageConfigViewModel.settingsManager.languageType
        }
    }

    key(languageType) {
        XyColumnScreen {
            TopAppBarComponent(
                modifier = Modifier.statusBarsPadding(),
                title = {
                    TopAppBarTitle(
                        title = stringResource(Res.string.language)
                    )
                },
                navigationIcon = {
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
                }
            )

            LazyColumnNotComponent {
                item {
                    RoundedSurfaceColumn {
                        LanguageType.entries
                            .forEach {
                                XyItemRadioButton(
                                    text = it.languageName,
                                    sub = it.languageCode,
                                    selected = it == languageConfigViewModel.settingsManager.languageType,
                                    enabled = it.enabled,
                                    paddingValue = PaddingValues(
                                        horizontal = XyTheme.dimens.innerHorizontalPadding,
                                        vertical = XyTheme.dimens.outerVerticalPadding
                                    ),
                                    onClick = {
                                        languageConfigViewModel.updateLanguageType(
                                            it
                                        )
                                    }
                                )
                            }
                    }
                }
            }
        }
    }
}

