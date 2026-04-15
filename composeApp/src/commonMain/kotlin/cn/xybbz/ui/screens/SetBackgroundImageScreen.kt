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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.TopAppBarTitle
import cn.xybbz.ui.components.rememberBackgroundImagePicker
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyNoData
import cn.xybbz.viewmodel.SetBackgroundImageViewModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.arrow_back_24px
import xymusic_kmp.composeapp.generated.resources.background_image_setting
import xymusic_kmp.composeapp.generated.resources.clear_image
import xymusic_kmp.composeapp.generated.resources.return_interface_settings
import xymusic_kmp.composeapp.generated.resources.select_image
import cn.xybbz.ui.xy.XyIconButton as IconButton

/**
 * 设置背景图片界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetBackgroundImageScreen(setBackgroundImageViewModel: SetBackgroundImageViewModel = koinViewModel<SetBackgroundImageViewModel>()) {

    val ifSelectImage by remember {
        derivedStateOf {
            !setBackgroundImageViewModel.settingsManager.imageFilePath.isNullOrBlank()
        }
    }
    val navigator = LocalNavigator.current
    val imagePicker = rememberBackgroundImagePicker(
        onImagePicked = setBackgroundImageViewModel::updateBackgroundImagePath
    )
    key(XyTheme.brash.backgroundImageUri) {
        XyColumnScreen {

            TopAppBarComponent(
                title = {
                    TopAppBarTitle(
                        title = stringResource(Res.string.background_image_setting)
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
                            contentDescription = stringResource(Res.string.return_interface_settings)
                        )
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = composeClick() {
                            imagePicker.pickImage()
                        }) {
                            Text(text = stringResource(Res.string.select_image))
                        }

                        TextButton(onClick = composeClick() {
                            setBackgroundImageViewModel.updateBackgroundImagePath(null)
                        }, enabled = ifSelectImage) {
                            Text(text = stringResource(Res.string.clear_image))
                        }
                    }
                }
            )

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (!ifSelectImage) {
                    XyNoData()
                } else {
                    Text(text = setBackgroundImageViewModel.settingsManager.imageFilePath.orEmpty())
                }
                Spacer(
                    modifier = Modifier.height(
                        XyTheme.dimens.snackBarPlayerHeight + WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding()
                    )
                )
            }
        }
    }


}

