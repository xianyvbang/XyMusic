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

import android.content.ClipData
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cn.xybbz.R
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.router.SelectLibrary
import cn.xybbz.ui.components.AlertDialogObject
import cn.xybbz.ui.components.SettingItemComponent
import cn.xybbz.ui.components.SettingParentItemComponent
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.TopAppBarTitle
import cn.xybbz.ui.components.show
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.LazyColumnNotComponent
import cn.xybbz.ui.xy.RoundedSurfaceColumnPadding
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyEdit
import cn.xybbz.ui.xy.XyTextSubSmall
import cn.xybbz.viewmodel.ConnectionConfigInfoViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionConfigInfoScreen(
    connectionId: Long,
    connectionConfigInfoViewModel: ConnectionConfigInfoViewModel = hiltViewModel<ConnectionConfigInfoViewModel, ConnectionConfigInfoViewModel.Factory>(
        creationCallback = { factory ->
            factory.create(
                connectionId = connectionId,
            )
        }
    )
) {
    val clipboardManager = LocalClipboard.current
    val navigator = LocalNavigator.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val copySuccess = stringResource(R.string.copy_success)
    val cannotDeleteCurrentConnection = stringResource(R.string.cannot_delete_current_connection)
    val warning = stringResource(R.string.warning)

    XyColumnScreen(
        modifier =
            Modifier.brashColor(
                topVerticalColor = connectionConfigInfoViewModel.backgroundConfig.connectionInfoBrash[0],
                bottomVerticalColor = connectionConfigInfoViewModel.backgroundConfig.connectionInfoBrash[1]
            )
    ) {
        TopAppBarComponent(
            modifier = Modifier.statusBarsPadding(),
            navigationIcon = {
                IconButton(
                    onClick = {
                        navigator.goBack()
                    },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            }, title = {
                TopAppBarTitle(
                    title = stringResource(R.string.connection_info)
                )
            })

        LazyColumnNotComponent(modifier = Modifier) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = XyTheme.dimens.outerHorizontalPadding,
                            vertical = XyTheme.dimens.outerVerticalPadding
                        )
                        .height(20.dp),
                    content = {
                        XyTextSubSmall(text = "${connectionConfigInfoViewModel.connectionConfig?.name} ${connectionConfigInfoViewModel.connectionConfig?.serverVersion}")
                    }
                )
            }

            item {
                RoundedSurfaceColumnPadding(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF275454),
                            Color.Gray.copy(alpha = 0.1f)
                        ), tileMode = TileMode.Repeated
                    )
                ) {
                    ConnectionInfoTextItem(
                        title = stringResource(R.string.username),
                        info = connectionConfigInfoViewModel.username
                    ) {
                        coroutineScope.launch {
                            val clipData =
                                ClipData.newPlainText(
                                    "label",
                                    connectionConfigInfoViewModel.username
                                )
                            clipboardManager.setClipEntry(ClipEntry(clipData))
                        }.invokeOnCompletion {
                            MessageUtils.sendPopTip(copySuccess)
                        }
                    }
                    SettingItemComponent(
                        title = stringResource(R.string.password),
                        content = {
                            XyEdit(
                                text = connectionConfigInfoViewModel.password ?: "",
                                onChange = {
                                    connectionConfigInfoViewModel.updatePassword(it)
                                })
                        },
                        onConfirmation = {
                            connectionConfigInfoViewModel.savePasswordAndLogin()
                        },
                        onDismissRequest = { connectionConfigInfoViewModel.updatePassword("") },
                        onCloseRequest = { connectionConfigInfoViewModel.updatePassword("") })
                }
            }

            item {
                RoundedSurfaceColumnPadding(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF275454),
                            Color.Gray.copy(alpha = 0.1f)
                        ), tileMode = TileMode.Repeated
                    )
                ) {
                    SettingItemComponent(
                        title = stringResource(R.string.music_library),
                        info = if (connectionConfigInfoViewModel.library.id == Constants.MINUS_ONE_INT.toString())
                            stringResource(connectionConfigInfoViewModel.library.name.toInt())
                        else connectionConfigInfoViewModel.library.name,
                        enabled = connectionConfigInfoViewModel.getConnectionId() == connectionId,
                        onRouter = {
                            navigator.navigate(
                                SelectLibrary(
                                    connectionId,
                                    connectionConfigInfoViewModel.connectionConfig?.libraryId
                                )
                            )
                        }
                    )

                    SettingItemComponent(
                        title = stringResource(R.string.connection_address),
                        info = connectionConfigInfoViewModel.address,
                        onClick = {
                            connectionConfigInfoViewModel.updateTmpAddress(
                                connectionConfigInfoViewModel.address
                            )
                        },
                        content = {
                            XyEdit(
                                text = connectionConfigInfoViewModel.tmpAddress,
                                onChange = {
                                    connectionConfigInfoViewModel.updateTmpAddress(it)
                                })
                        },
                        onConfirmation = {
                            connectionConfigInfoViewModel.saveAddress()
                        },
                        onDismissRequest = { connectionConfigInfoViewModel.reductionAddress() },
                        onCloseRequest = { connectionConfigInfoViewModel.reductionAddress() })

                    SettingItemComponent(
                        title = stringResource(R.string.set_alias),
                        info = connectionConfigInfoViewModel.connectionName,
                        content = {
                            XyEdit(
                                text = connectionConfigInfoViewModel.connectionName,
                                onChange = {
                                    connectionConfigInfoViewModel.updateTmpName(it)
                                })
                        },
                        onConfirmation = {
                            connectionConfigInfoViewModel.saveName()
                        },
                        onDismissRequest = { connectionConfigInfoViewModel.reductionName() },
                        onCloseRequest = { connectionConfigInfoViewModel.reductionName() })

                    SettingParentItemComponent(
                        title = stringResource(R.string.delete_connection),
                        onClick = {
                            if (connectionConfigInfoViewModel.getConnectionId() == connectionId) {
                                MessageUtils.sendPopTipError(
                                    cannotDeleteCurrentConnection
                                )
                            } else {
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
                                            connectionConfigInfoViewModel.removeThisConnection()
                                        }.invokeOnCompletion {
                                            navigator.goBack()
                                        }
                                    }
                                ).show()
                            }

                        }, textColor = Color.Red
                    )
                }
            }
        }

    }
}

@Composable
private fun ConnectionInfoTextItem(title: String, info: String, onClick: (() -> Unit)? = null) {
    SettingParentItemComponent(
        modifier = Modifier,
        title = title,
        onClick = onClick,
        trailingContent = {
            XyTextSubSmall(text = info)
        })
}