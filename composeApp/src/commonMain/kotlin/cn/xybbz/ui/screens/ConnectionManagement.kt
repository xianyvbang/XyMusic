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


import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cn.xybbz.common.enums.ConnectionUiType
import cn.xybbz.common.enums.img
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.router.Connection
import cn.xybbz.router.ConnectionInfo
import cn.xybbz.router.SelectLibrary
import cn.xybbz.ui.components.AlertDialogObject
import cn.xybbz.ui.components.BottomSheetObject
import cn.xybbz.ui.components.ScreenLazyColumn
import cn.xybbz.ui.components.SettingItemComponent
import cn.xybbz.ui.components.SettingParentItemComponent
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.TopAppBarTitle
import cn.xybbz.ui.components.dismiss
import cn.xybbz.ui.components.show
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.ItemTrailingArrowRight
import cn.xybbz.ui.xy.RoundedSurfaceColumn
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyTextSubSmall
import cn.xybbz.viewmodel.ConnectionManagementViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.add_card_24px
import xymusic_kmp.composeapp.generated.resources.arrow_back_24px
import xymusic_kmp.composeapp.generated.resources.confirm_delete_connection
import xymusic_kmp.composeapp.generated.resources.connection_settings_list
import xymusic_kmp.composeapp.generated.resources.delete_connection
import xymusic_kmp.composeapp.generated.resources.modify_connection
import xymusic_kmp.composeapp.generated.resources.more_vert_24px
import xymusic_kmp.composeapp.generated.resources.music_library
import xymusic_kmp.composeapp.generated.resources.return_setting_screen
import xymusic_kmp.composeapp.generated.resources.view_connection_info
import xymusic_kmp.composeapp.generated.resources.warning
import cn.xybbz.ui.xy.XyIconButton as IconButton

/**
 * 连接设置列表
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionManagement(
    connectionManagementViewModel: ConnectionManagementViewModel = koinViewModel<ConnectionManagementViewModel>()
) {
    val navigator = LocalNavigator.current
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val warning = stringResource(Res.string.warning)

    XyColumnScreen {

        TopAppBarComponent(
            modifier = Modifier.statusBarsPadding(),
            title = {
                TopAppBarTitle(
                    title = stringResource(Res.string.connection_settings_list)
                )
            }, actions = {
                IconButton(onClick = {
                    navigator.navigate(Connection(connectionUiType = ConnectionUiType.ADD_CONNECTION))
                }) {
                    Icon(painter = painterResource(Res.drawable.add_card_24px), contentDescription = "")
                }
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
        ScreenLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = lazyListState,
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                connectionManagementViewModel.connectionList,
                key = { it.id }) { connectionConfig ->
                ItemTrailingArrowRight(
                    modifier = Modifier
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.onSurfaceVariant,
                            shape = RoundedCornerShape(XyTheme.dimens.corner)
                        ),
                    backgroundColor = Color.Transparent,
                    name = connectionConfig.type.title + "-" + connectionConfig.username,
                    subordination = connectionConfig.address,
                    img = connectionConfig.type.img.let { img ->
                        painterResource(
                            img
                        )
                    },
                    onClick = {
                        navigator.navigate(ConnectionInfo(connectionConfig.id))
                    },
                    trailingContent = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            Switch(
                                checked = connectionManagementViewModel.connectionId == connectionConfig.id,
                                onCheckedChange = {
                                    if (it)
                                        connectionManagementViewModel.changeDataSource(
                                            connectionConfig
                                        )
                                },
                                enabled = true,
                                colors = SwitchDefaults.colors(
                                    uncheckedBorderColor = Color.Transparent,
                                    checkedThumbColor = MaterialTheme.colorScheme.surface
                                )
                            )
                            IconButton(
//                                colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Red),
                                onClick = composeClick {
                                    BottomSheetObject(
                                        sheetState = sheetState,
                                        state = true,
                                        titleText = connectionConfig.name,
                                        dragHandle = null,
                                        content = { sheetObject ->
                                            RoundedSurfaceColumn {
                                                SettingItemComponent(
                                                    title = stringResource(Res.string.modify_connection)
                                                ) {
                                                    coroutineScope.launch {
                                                        sheetState.hide()
                                                    }.invokeOnCompletion {
                                                        sheetObject.dismiss()
                                                        navigator.navigate(
                                                            ConnectionInfo(connectionConfig.id)
                                                        )
                                                    }

                                                }
                                                SettingItemComponent(
                                                    title = stringResource(Res.string.music_library),
                                                    onRouter = {
                                                        coroutineScope.launch {
                                                            sheetState.hide()
                                                        }.invokeOnCompletion {
                                                            sheetObject.dismiss()
                                                            navigator.navigate(
                                                                SelectLibrary(
                                                                    connectionConfig.id,
                                                                    connectionConfig.libraryIds
                                                                )
                                                            )
                                                        }
                                                    }
                                                )

                                                SettingParentItemComponent(
                                                    title = stringResource(Res.string.delete_connection),
                                                    onClick = {
                                                        AlertDialogObject(
                                                            title = warning,
                                                            content = {
                                                                XyTextSubSmall(
                                                                    text = stringResource(Res.string.confirm_delete_connection)
                                                                )
                                                            },
                                                            ifWarning = true,
                                                            onConfirmation = {
                                                                coroutineScope.launch {
                                                                    connectionManagementViewModel.removeConnection(
                                                                        connectionConfig.id
                                                                    )
                                                                    sheetState.hide()
                                                                }.invokeOnCompletion {
                                                                    sheetObject.dismiss()
                                                                }
                                                            }
                                                        ).show()

                                                    }, textColor = Color.Red
                                                )
                                            }
                                        },
                                    ).show()
                                }) {
                                Icon(
                                    painter = painterResource(Res.drawable.more_vert_24px),
                                    contentDescription =
                                        stringResource(
                                            Res.string.view_connection_info,
                                            connectionConfig.type.title + "-" + connectionConfig.username
                                        )
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}


