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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cn.xybbz.common.enums.ConnectionUiType
import cn.xybbz.common.enums.img
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.router.Connection
import cn.xybbz.router.ConnectionInfo
import cn.xybbz.router.SelectLibrary
import cn.xybbz.ui.components.AlertDialogObject
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.TopAppBarTitle
import cn.xybbz.ui.components.show
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.ext.debounceClickable
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.windows.DesktopTooltipIconButton
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XySmallImage
import cn.xybbz.ui.xy.XyText
import cn.xybbz.ui.xy.XyTextSub
import cn.xybbz.ui.xy.XyTextSubSmall
import cn.xybbz.viewmodel.ConnectionManagementViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.add_card_24px
import xymusic_kmp.composeapp.generated.resources.check_24px
import xymusic_kmp.composeapp.generated.resources.confirm_delete_connection
import xymusic_kmp.composeapp.generated.resources.connection_settings_list
import xymusic_kmp.composeapp.generated.resources.delete_24px
import xymusic_kmp.composeapp.generated.resources.delete_connection
import xymusic_kmp.composeapp.generated.resources.edit_24px
import xymusic_kmp.composeapp.generated.resources.modify_connection
import xymusic_kmp.composeapp.generated.resources.music_library
import xymusic_kmp.composeapp.generated.resources.queue_music_24px
import xymusic_kmp.composeapp.generated.resources.warning
import cn.xybbz.ui.xy.XyIconButton as IconButton

/**
 * 连接设置列表
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun JvmConnectionManagement(
    connectionManagementViewModel: ConnectionManagementViewModel = koinViewModel<ConnectionManagementViewModel>()
) {
    val navigator = LocalNavigator.current
    val lazyGridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()

    val warning = stringResource(Res.string.warning)

    XyColumnScreen {

        TopAppBarComponent(
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
            })
        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            state = lazyGridState,
            columns = GridCells.Adaptive(minSize = 420.dp),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                connectionManagementViewModel.connectionList,
                key = { it.id }) { connectionConfig ->
                val isCurrentConnection = connectionManagementViewModel.connectionId == connectionConfig.id
                val cardShape = RoundedCornerShape(XyTheme.dimens.corner)
                val libraryText = connectionManagementViewModel.selectedLibraryNames(connectionConfig)
                    ?.joinToString("、", prefix = "媒体库：")
                    ?: "媒体库：全部"
                val versionText = connectionConfig.serverVersion.takeIf { it.isNotBlank() }?.let {
                    "版本：$it"
                } ?: "版本：未知"
                val capabilityText = "权限：" + (buildList {
                    if (connectionConfig.ifEnabledDownload) add("下载")
                    if (connectionConfig.ifEnabledDelete) add("删除")
                }.takeIf { it.isNotEmpty() }?.joinToString(" / ") ?: "只读")
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .debounceClickable {
                            if (!isCurrentConnection) {
                                connectionManagementViewModel.changeDataSource(connectionConfig)
                            }
                        },
                    shape = cardShape,
                    color = if (isCurrentConnection) {
                        MaterialTheme.colorScheme.surfaceContainerLowest
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.48f)
                    },
                    tonalElevation = if (isCurrentConnection) 1.dp else 0.dp,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                        ) {
                            XySmallImage(
                                modifier = Modifier
                                    .size(50.dp)
                                    .alpha(if (isCurrentConnection) 1f else 0.45f),
                                model = painterResource(connectionConfig.type.img),
                                contentDescription = connectionConfig.type.title + "-" + connectionConfig.username,
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.Center,
                            ) {
                                XyText(
                                    text = connectionConfig.type.title + "-" + connectionConfig.username,
                                )
                                XyTextSub(
                                    text = connectionConfig.address,
                                    maxLines = 1,
                                )
                            }
                            if (isCurrentConnection) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                                    contentColor = MaterialTheme.colorScheme.primary,
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(
                                            painter = painterResource(Res.drawable.check_24px),
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        XyTextSubSmall(
                                            text = "当前",
                                            color = MaterialTheme.colorScheme.primary,
                                            maxLines = 1,
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            FlowRow(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                JvmConnectionInfoChip(libraryText)
                                JvmConnectionInfoChip(versionText)
                                JvmConnectionInfoChip(capabilityText)
                            }
                            DesktopTooltipIconButton(
                                tooltip = stringResource(Res.string.modify_connection),
                                onClick = composeClick {
                                    navigator.navigate(ConnectionInfo(connectionConfig.id))
                                },
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.edit_24px),
                                    contentDescription = stringResource(Res.string.modify_connection),
                                )
                            }
                            DesktopTooltipIconButton(
                                tooltip = stringResource(Res.string.music_library),
                                onClick = composeClick {
                                    navigator.navigate(
                                        SelectLibrary(
                                            connectionConfig.id,
                                            connectionConfig.libraryIds
                                        )
                                    )
                                },
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.queue_music_24px),
                                    contentDescription = stringResource(Res.string.music_library),
                                )
                            }
                            DesktopTooltipIconButton(
                                tooltip = stringResource(Res.string.delete_connection),
                                onClick = composeClick {
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
                                            }
                                        }
                                    ).show()
                                },
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.delete_24px),
                                    contentDescription = stringResource(Res.string.delete_connection),
                                    tint = Color.Red,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun JvmConnectionInfoChip(text: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f),
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        XyTextSubSmall(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}
