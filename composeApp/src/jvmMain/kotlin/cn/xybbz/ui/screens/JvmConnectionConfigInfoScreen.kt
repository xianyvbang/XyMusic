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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cn.xybbz.ui.components.JvmLazyListComponent
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.TopAppBarTitle
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyText
import cn.xybbz.ui.xy.XyTextSub
import cn.xybbz.viewmodel.ConnectionConfigInfoViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.connection_address
import xymusic_kmp.composeapp.generated.resources.connection_info
import xymusic_kmp.composeapp.generated.resources.connection_server_version_label
import xymusic_kmp.composeapp.generated.resources.connection_server_version_unknown_label
import xymusic_kmp.composeapp.generated.resources.password
import xymusic_kmp.composeapp.generated.resources.save
import xymusic_kmp.composeapp.generated.resources.set_alias
import xymusic_kmp.composeapp.generated.resources.username

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JvmConnectionConfigInfoScreen(
    connectionId: Long,
    connectionConfigInfoViewModel: ConnectionConfigInfoViewModel = koinViewModel<ConnectionConfigInfoViewModel> {
        parametersOf(connectionId)
    }
) {
    val coroutineScope = rememberCoroutineScope()
    val saveConnection = {
        coroutineScope.launch {
            //修改连接设置
            connectionConfigInfoViewModel.updateConnectionConfig()
        }.invokeOnCompletion {
            connectionConfigInfoViewModel.restartLogin()
        }
        Unit
    }
    val connectionName = connectionConfigInfoViewModel.connectionConfig?.name
        ?.takeIf { it.isNotBlank() }
        ?: connectionConfigInfoViewModel.connectionName
    val serverVersion = connectionConfigInfoViewModel.connectionConfig?.serverVersion.orEmpty()
    val serverVersionText = if (serverVersion.isNotBlank()) {
        stringResource(Res.string.connection_server_version_label, serverVersion)
    } else {
        stringResource(Res.string.connection_server_version_unknown_label)
    }

    XyColumnScreen {
        TopAppBarComponent(
            title = {
                TopAppBarTitle(
                    title = stringResource(Res.string.connection_info)
                )
            },
            actions = {
                TextButton(onClick = saveConnection) {
                    Text(text = stringResource(Res.string.save))
                }
            }
        )

        JvmLazyListComponent(
            modifier = Modifier.fillMaxSize(),
            pagingItems = null,
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            lazyColumnBottom = null
        ) {
            item {
                Column(
                    modifier = Modifier
                        .widthIn(max = 760.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    JvmConnectionSummaryHeader(
                        name = connectionName,
                        version = serverVersionText,
                        address = connectionConfigInfoViewModel.address
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(XyTheme.dimens.corner),
                        color = MaterialTheme.colorScheme.surfaceContainerLowest
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            JvmConnectionFormRow(label = stringResource(Res.string.connection_address)) {
                                AddressInputEdit(
                                    address = connectionConfigInfoViewModel.address
                                ) {
                                    connectionConfigInfoViewModel.updateAddress(it)
                                }
                            }

                            JvmConnectionFormRow(label = stringResource(Res.string.username)) {
                                UsernameInputEdit(username = connectionConfigInfoViewModel.username) {
                                    connectionConfigInfoViewModel.updateUsername(it)
                                }
                            }

                            JvmConnectionFormRow(label = stringResource(Res.string.password)) {
                                PasswordInputEdit(password = connectionConfigInfoViewModel.password) {
                                    connectionConfigInfoViewModel.updatePassword(it)
                                }
                            }

                            JvmConnectionFormRow(label = stringResource(Res.string.set_alias)) {
                                ConnectionNameInputEdit(connectionName = connectionConfigInfoViewModel.connectionName) {
                                    connectionConfigInfoViewModel.updateConnectionName(it)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun JvmConnectionSummaryHeader(
    name: String,
    version: String,
    address: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.48f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                XyText(
                    text = name,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1
                )
                XyTextSub(
                    text = address,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1
                )
            }
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                XyText(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    text = version,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun JvmConnectionFormRow(
    label: String,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        XyTextSub(
            modifier = Modifier.width(96.dp),
            text = label,
            maxLines = 1,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
        )
        Box(modifier = Modifier.weight(1f)) {
            content()
        }
    }
}

