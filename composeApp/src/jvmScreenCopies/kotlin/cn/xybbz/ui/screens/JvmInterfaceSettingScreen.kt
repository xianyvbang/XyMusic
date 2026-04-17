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

import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cn.xybbz.common.enums.toResStringInt
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.localdata.enums.ThemeTypeEnum
import cn.xybbz.router.SetBackgroundImage
import cn.xybbz.ui.components.SettingItemComponent
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.TopAppBarTitle
import cn.xybbz.ui.popup.MenuItemDefaultData
import cn.xybbz.ui.popup.XyDropdownMenu
import cn.xybbz.ui.xy.LazyColumnNotComponent
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.viewmodel.InterfaceSettingViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.arrow_back_24px
import xymusic_kmp.composeapp.generated.resources.background_image_setting
import xymusic_kmp.composeapp.generated.resources.check_24px
import xymusic_kmp.composeapp.generated.resources.interface_settings
import xymusic_kmp.composeapp.generated.resources.keyboard_arrow_down_24px
import xymusic_kmp.composeapp.generated.resources.return_setting_screen
import xymusic_kmp.composeapp.generated.resources.theme_mode
import cn.xybbz.ui.xy.XyIconButton as IconButton

/**
 * 界面设置
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JvmInterfaceSettingScreen(
    interfaceSettingViewModel: InterfaceSettingViewModel = koinViewModel<InterfaceSettingViewModel>()
) {

    val navigator = LocalNavigator.current
    val coroutineScope = rememberCoroutineScope()
    val themeType by interfaceSettingViewModel.settingsManager.themeType.collectAsState()
    var ifShowThemeMenu by remember {
        mutableStateOf(false)
    }


    XyColumnScreen {
        TopAppBarComponent(
            title = {
                TopAppBarTitle(
                    title = stringResource(Res.string.interface_settings)
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
                SettingRoundedSurfaceColumn {
                    SettingItemComponent(
                        title = stringResource(Res.string.background_image_setting),
                        trailingContent = {

                        }) {
                        navigator.navigate(SetBackgroundImage)
                    }


                    SettingItemComponent(
                        title = stringResource(Res.string.theme_mode),
                        info = stringResource(themeType.toResStringInt()),
                        painter = Res.drawable.keyboard_arrow_down_24px,
                        trailingContent = {
                            XyDropdownMenu(
                                onIfShowMenu = { ifShowThemeMenu },
                                onSetIfShowMenu = { ifShowThemeMenu = it },
                                modifier = Modifier
                                    .width(200.dp),
                                itemDataList = ThemeTypeEnum.entries.map {
                                    MenuItemDefaultData(
                                        title = stringResource(it.toResStringInt()),
                                        leadingIcon = {
                                            if (themeType == it)
                                                Icon(
                                                    painter = painterResource(Res.drawable.check_24px),
                                                    contentDescription = stringResource(
                                                        it.toResStringInt()
                                                    )
                                                )
                                        },
                                        onClick = {
                                            coroutineScope.launch {
                                                ifShowThemeMenu = false
                                                interfaceSettingViewModel.setThemeTypeData(
                                                    it
                                                )
                                            }.invokeOnCompletion {

                                            }

                                        })
                                }
                            )
                        }
                    ) {
                        ifShowThemeMenu = true
                    }
                }
            }
        }
    }
}


