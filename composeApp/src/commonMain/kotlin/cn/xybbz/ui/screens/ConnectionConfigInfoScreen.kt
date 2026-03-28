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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.TopAppBarTitle
import cn.xybbz.ui.xy.LazyColumnComponent
import cn.xybbz.ui.xy.XyButton
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyTextSubSmall
import cn.xybbz.viewmodel.ConnectionConfigInfoViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.arrow_back_24px
import xymusic_kmp.composeapp.generated.resources.back
import xymusic_kmp.composeapp.generated.resources.connection_info
import xymusic_kmp.composeapp.generated.resources.save
import cn.xybbz.ui.xy.XyIconButton as IconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionConfigInfoScreen(
    connectionId: Long,
    connectionConfigInfoViewModel: ConnectionConfigInfoViewModel = koinViewModel<ConnectionConfigInfoViewModel> {
        parametersOf(connectionId)
    }
) {
    val navigator = LocalNavigator.current
    val coroutineScope = rememberCoroutineScope()

    XyColumnScreen {
        TopAppBarComponent(
            modifier = Modifier.statusBarsPadding(),
            navigationIcon = {
                IconButton(
                    onClick = {
                        navigator.goBack()
                    },
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.arrow_back_24px),
                        contentDescription = stringResource(Res.string.back)
                    )
                }
            }, title = {
                TopAppBarTitle(
                    title = stringResource(Res.string.connection_info)
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
                ConnectionNameInputEdit(connectionName = connectionConfigInfoViewModel.connectionName) {
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
                }, text = stringResource(Res.string.save))
            }
        }
    }
}

