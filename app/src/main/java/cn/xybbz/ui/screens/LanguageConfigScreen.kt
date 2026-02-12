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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cn.xybbz.R
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.localdata.enums.LanguageType
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.TopAppBarTitle
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.ext.debounceClickable
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.LazyColumnNotComponent
import cn.xybbz.ui.xy.RoundedSurfaceColumn
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyItemRadioButton
import cn.xybbz.viewmodel.LanguageConfigViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageConfigScreen(
    languageConfigViewModel: LanguageConfigViewModel = hiltViewModel<LanguageConfigViewModel>()
) {

    val navigator = LocalNavigator.current
    val context = LocalContext.current

    val languageType by remember {
        derivedStateOf {
            languageConfigViewModel.settingsConfig.languageType
        }
    }

    key(languageType) {
        XyColumnScreen(
            modifier = Modifier.brashColor(
                topVerticalColor = languageConfigViewModel.backgroundConfig.languageBrash[0],
                bottomVerticalColor = languageConfigViewModel.backgroundConfig.languageBrash[1]
            )
        ) {
            TopAppBarComponent(
                modifier = Modifier.statusBarsPadding(),
                title = {
                    TopAppBarTitle(
                        title = stringResource(R.string.language)
                    )
                },
                navigationIcon = {
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
                                    selected = it == languageConfigViewModel.settingsConfig.languageType,
                                    enabled = it.enabled,
                                    paddingValue = PaddingValues(
                                        horizontal = XyTheme.dimens.innerHorizontalPadding,
                                        vertical = XyTheme.dimens.outerVerticalPadding
                                    ),
                                    onClick = {
                                        languageConfigViewModel.updateLanguageType(
                                            it,
                                            context = context
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

@Composable
private fun LanguageTextItem(
    modifier: Modifier = Modifier,
    languageType: LanguageType?,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth()
            .debounceClickable(enabled = enabled) {
                onClick.invoke()
            }
            .padding(
                horizontal = XyTheme.dimens.innerHorizontalPadding,
                vertical = XyTheme.dimens.innerVerticalPadding
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = languageType?.languageName ?: "",
            maxLines = 1,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodySmall
        )

        Text(
            text = languageType?.languageCode ?: "",
            color = MaterialTheme.colorScheme.onSurface,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleSmall
        )
    }

}