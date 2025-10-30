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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cn.xybbz.R
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.compositionLocal.LocalNavController
import cn.xybbz.router.RouterConstants
import cn.xybbz.ui.components.AlertDialogObject
import cn.xybbz.ui.components.SettingItemComponent
import cn.xybbz.ui.components.SettingParentItemComponent
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.show
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.LazyColumnNotComponent
import cn.xybbz.ui.xy.RoundedSurfaceColumnPadding
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyEdit
import cn.xybbz.ui.xy.XyItemText
import cn.xybbz.ui.xy.XyItemTextHorizontal
import cn.xybbz.ui.xy.XyItemTextPadding
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
    val navHostController = LocalNavController.current
    val context = LocalContext.current
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
                        navHostController.popBackStack()
                    },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            }, title = {
                Text(
                    text = stringResource(R.string.connection_info),
                    fontWeight = FontWeight.W900
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
                        XyItemText(text = "${connectionConfigInfoViewModel.connectionConfig?.name} ${connectionConfigInfoViewModel.connectionConfig?.serverVersion}")
                    }
                )
            }

            item {
                XyItemTextPadding(
                    text = stringResource(R.string.user_settings),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            MessageUtils.sendPopTip(context.getString(R.string.copy_success))
                        }
                    }
                    SettingItemComponent(
                        title = R.string.password,
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
                XyItemTextPadding(
                    text = stringResource(R.string.media_library_management),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    SettingItemComponent(
                        title = R.string.music_library,
                        info = if (connectionConfigInfoViewModel.library.id == Constants.MINUS_ONE_INT.toString())
                            stringResource(connectionConfigInfoViewModel.library.name.toInt())
                        else connectionConfigInfoViewModel.library.name,
                        enabled = connectionConfigInfoViewModel.connectionConfigServer.getConnectionId() == connectionId,
                        onRouter = {
                            navHostController.navigate(
                                RouterConstants.SelectLibrary(
                                    connectionId,
                                    connectionConfigInfoViewModel.library.id
                                )
                            )
                        }
                    )

                    SettingItemComponent(
                        title = R.string.connection_address,
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
                        title = R.string.set_alias,
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
                            if (connectionConfigInfoViewModel.connectionConfigServer.getConnectionId() == connectionId) {
                                MessageUtils.sendPopTipError(
                                    context.getString(R.string.cannot_delete_current_connection),
                                )
                            } else {
                                AlertDialogObject(
                                    title = R.string.warning,
                                    content = {
                                        XyItemTextHorizontal(
                                            text = stringResource(R.string.confirm_delete_connection)
                                        )
                                    },
                                    ifWarning = true,
                                    onConfirmation = {
                                        coroutineScope.launch {
                                            connectionConfigInfoViewModel.removeThisConnection()
                                        }.invokeOnCompletion {
                                            navHostController.popBackStack()
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
            XyItemText(text = info)
        })
}