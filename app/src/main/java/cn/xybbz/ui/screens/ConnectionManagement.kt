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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.AddCard
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cn.xybbz.R
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
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.ItemTrailingArrowRight
import cn.xybbz.ui.xy.RoundedSurfaceColumn
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyTextSubSmall
import cn.xybbz.viewmodel.ConnectionManagementViewModel
import kotlinx.coroutines.launch

/**
 * 连接设置列表
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionManagement(
    connectionManagementViewModel: ConnectionManagementViewModel = hiltViewModel<ConnectionManagementViewModel>()
) {
    val navigator = LocalNavigator.current
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val warning = stringResource(R.string.warning)

    XyColumnScreen(
        modifier =
            Modifier.brashColor(
                topVerticalColor = connectionManagementViewModel.backgroundConfig.connectionManagerBrash[0],
                bottomVerticalColor = connectionManagementViewModel.backgroundConfig.connectionManagerBrash[1]
            )
    ) {

        TopAppBarComponent(
            modifier = Modifier.statusBarsPadding(),
            title = {
                TopAppBarTitle(
                    title = stringResource(R.string.connection_settings_list)
                )
            }, actions = {
                IconButton(onClick = {
                    navigator.navigate(Connection(connectionUiType = ConnectionUiType.ADD_CONNECTION))
                }) {
                    Icon(imageVector = Icons.Rounded.AddCard, contentDescription = "")
                }
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
        ScreenLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = lazyListState,
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                connectionManagementViewModel.connectionList,
                key = { it.connectionConfig.id }) { connectionConfigExt ->
                ItemTrailingArrowRight(
                    modifier = Modifier
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.onSurfaceVariant,
                            shape = RoundedCornerShape(XyTheme.dimens.corner)
                        ),
                    backgroundColor = Color.Transparent,
                    name = connectionConfigExt.connectionConfig.type.title + "-" + connectionConfigExt.connectionConfig.username,
                    subordination = connectionConfigExt.connectionConfig.address,
                    img = connectionConfigExt.connectionConfig.type.img.let { img ->
                        painterResource(
                            img
                        )
                    },
                    onClick = {
                        navigator.navigate(ConnectionInfo(connectionConfigExt.connectionConfig.id))
                    },
                    trailingContent = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            Switch(
                                checked = connectionManagementViewModel.connectionId == connectionConfigExt.connectionConfig.id,
                                onCheckedChange = {
                                    if (it)
                                        connectionManagementViewModel.changeDataSource(
                                            connectionConfigExt.connectionConfig
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
                                        titleText = connectionConfigExt.connectionConfig.name,
                                        dragHandle = null,
                                        content = { sheetObject ->
                                            RoundedSurfaceColumn {
                                                SettingItemComponent(
                                                    title = "修改连接"
                                                ) {
                                                    coroutineScope.launch {
                                                        sheetState.hide()
                                                    }.invokeOnCompletion {
                                                        sheetObject.dismiss()
                                                        navigator.navigate(
                                                            ConnectionInfo(connectionConfigExt.connectionConfig.id)
                                                        )
                                                    }

                                                }
                                                SettingItemComponent(
                                                    title = stringResource(R.string.music_library),
                                                    info = if (connectionConfigExt.connectionConfig.libraryId.isNullOrBlank())
                                                        stringResource(R.string.all_media_libraries)
                                                    else connectionConfigExt.libraryName,
                                                    onRouter = {
                                                        coroutineScope.launch {
                                                            sheetState.hide()
                                                        }.invokeOnCompletion {
                                                            sheetObject.dismiss()
                                                            navigator.navigate(
                                                                SelectLibrary(
                                                                    connectionConfigExt.connectionConfig.id,
                                                                    connectionConfigExt.connectionConfig.libraryId
                                                                )
                                                            )
                                                        }
                                                    }
                                                )

                                                SettingParentItemComponent(
                                                    title = stringResource(R.string.delete_connection),
                                                    onClick = {
                                                        AlertDialogObject(
                                                            title = warning,
                                                            content = {
                                                                XyTextSubSmall(
                                                                    text = stringResource(R.string.confirm_delete_connection)
                                                                )
                                                            },
                                                            ifWarning = true,
                                                            onConfirmation = {
                                                                coroutineScope.launch {
                                                                    connectionManagementViewModel.removeConnection(
                                                                        connectionConfigExt.connectionConfig.id
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
                                    imageVector = Icons.Rounded.MoreVert,
                                    contentDescription =
                                        stringResource(
                                            R.string.view_connection_info,
                                            connectionConfigExt.connectionConfig.type.title + "-" + connectionConfigExt.connectionConfig.username
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

