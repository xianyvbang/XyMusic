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

import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cn.xybbz.R
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.TopAppBarTitle
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.xy.LazyColumnComponent
import cn.xybbz.ui.xy.XyButton
import cn.xybbz.ui.xy.XyColumnScreen
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
    val navigator = LocalNavigator.current
    val coroutineScope = rememberCoroutineScope()

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

        LazyColumnComponent {
            item {
                XyTextSubSmall(text = "${connectionConfigInfoViewModel.connectionConfig?.name} ${connectionConfigInfoViewModel.connectionConfig?.serverVersion}")
            }
            item {
                AddressInputEdit(
                    address = connectionConfigInfoViewModel.address
                ) {
                    connectionConfigInfoViewModel.updateAddress(it)
                }
            }

            item {
                UsernameInputEdit(username = connectionConfigInfoViewModel.username) {
                    connectionConfigInfoViewModel.updateUsername(it)
                }
            }
            item {
                PasswordInputEdit(password = connectionConfigInfoViewModel.password) {
                    connectionConfigInfoViewModel.updatePassword(it)
                }
            }

            item {
                ConnectionNameInputEdit(connectionName = connectionConfigInfoViewModel.connectionName){
                    connectionConfigInfoViewModel.updateConnectionName(it)
                }
            }
            item {
                XyButton(onClick = {
                    coroutineScope.launch {
                        //修改连接设置
                        connectionConfigInfoViewModel.updateConnectionConfig()
                    }.invokeOnCompletion {
                        connectionConfigInfoViewModel.restartLogin()
                    }
                }, text = "保存修改")
            }
        }
    }
}