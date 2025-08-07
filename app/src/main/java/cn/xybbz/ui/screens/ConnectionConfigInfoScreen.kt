package cn.xybbz.ui.screens

import android.content.ClipData
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.compositionLocal.LocalNavController
import cn.xybbz.localdata.data.library.XyLibrary
import cn.xybbz.ui.components.AlertDialogObject
import cn.xybbz.ui.components.LazyColumnNotComponent
import cn.xybbz.ui.components.SettingItemComponent
import cn.xybbz.ui.components.SettingParentItemComponent
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.show
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.ext.debounceClickable
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.ModalBottomSheetExtendComponent
import cn.xybbz.ui.xy.RoundedSurfaceColumnPadding
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyItemBigTitle
import cn.xybbz.ui.xy.XyItemEdit
import cn.xybbz.ui.xy.XyItemText
import cn.xybbz.ui.xy.XyItemTextAlignEnd
import cn.xybbz.ui.xy.XyItemTextCheckSelectHeightSmall
import cn.xybbz.ui.xy.XyItemTextHorizontal
import cn.xybbz.ui.xy.XyItemTextPadding
import cn.xybbz.viewmodel.ConnectionConfigInfoViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionConfigInfoScreen(
    modifier: Modifier = Modifier,
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
    val coroutineScope = rememberCoroutineScope()

    var ifShowSelectLibrary by remember { mutableStateOf(false) }

    val library by remember {
        derivedStateOf {
            connectionConfigInfoViewModel.libraryList.find { it.id == connectionConfigInfoViewModel.libraryId }
        }
    }

    SelectLibraryBottomSheet(
        libraryList = connectionConfigInfoViewModel.libraryList,
        selectLibraryId = connectionConfigInfoViewModel.libraryId,
        onSelectLibraryId = {
            connectionConfigInfoViewModel.updateLibraryId(it.id)
        },
        onIfDisplay = { ifShowSelectLibrary },
        onSetState = {
            ifShowSelectLibrary = it
        }
    )

    XyColumnScreen(
        modifier =
            Modifier.brashColor(
                topVerticalColor = Color(0xFF228686),
                bottomVerticalColor = Color(0xff330867)
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
                        contentDescription = "返回"
                    )
                }
            }, title = {
                Text(
                    text = "连接信息",
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
                        XyItemText(text = "${connectionConfigInfoViewModel.connectionConfig?.type?.name} ${connectionConfigInfoViewModel.connectionConfig?.serverVersion}")
                    }
                )
            }

            item {
                XyItemTextPadding(
                    text = "用户设置",
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
                        title = "用户名",
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
                            MessageUtils.sendPopTip("复制成功")
                        }
                    }
                    SettingItemComponent(
                        title = "密码",
                        content = {
                            XyItemEdit(
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
                    text = "媒体库管理",
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
                    SettingParentItemComponent(
                        title = "音乐库",
                        onClick = {
                            ifShowSelectLibrary = true
                        },
                        trailingContent = {
                            Row(
                                modifier = Modifier.debounceClickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    ifShowSelectLibrary = true
                                }, horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                XyItemTextAlignEnd(
                                    text = library?.name ?: "",
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Icon(
                                    Icons.Filled.ArrowDropDown,
                                    contentDescription = "点击切换媒体库"
                                )
                            }
                        }
                    )

                    SettingItemComponent(
                        title = "链接地址",
                        info = connectionConfigInfoViewModel.address,
                        onClick = {
                            connectionConfigInfoViewModel.updateTmpAddress(
                                connectionConfigInfoViewModel.address
                            )
                        },
                        content = {
                            XyItemEdit(
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
                        title = "设置别名",
                        info = connectionConfigInfoViewModel.connectionName,
                        content = {
                            XyItemEdit(
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
                        title = "删除连接",
                        onClick = {
                            if (connectionConfigInfoViewModel._connectionConfigServer.connectionConfig?.id == connectionId) {
                                MessageUtils.sendPopTip(
                                    "当前连接无法删除",
                                    backgroundColor = android.graphics.Color.RED
                                )
                            } else {
                                AlertDialogObject(
                                    title = {
                                        XyItemBigTitle(text = "注意", color = Color.Red)
                                    },
                                    content = {
                                        XyItemTextHorizontal(
                                            text = "确定要删除当前连接信息吗?"
                                        )
                                    },
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun SelectLibraryBottomSheet(
    modifier: Modifier = Modifier,
    libraryList: List<XyLibrary>,
    selectLibraryId: String?,
    onSelectLibraryId: (XyLibrary) -> Unit,
    onIfDisplay: () -> Boolean,
    onSetState: (Boolean) -> Unit,
) {

    val modalBottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )
    val coroutineScope = rememberCoroutineScope()

    ModalBottomSheetExtendComponent(
        modifier = modifier.statusBarsPadding(),
        bottomSheetState = modalBottomSheetState,
        onIfDisplay = onIfDisplay,
        onClose = onSetState
    ) {
        LazyColumnNotComponent {
            item {
                XyItemBigTitle(text = "选择媒体库")
            }
            item {
                RoundedSurfaceColumnPadding(horizontalAlignment = Alignment.Start) {
                    FlowRow(
                        maxItemsInEachRow = 2,
                    ) {
                        libraryList.forEach {
                            XyItemTextCheckSelectHeightSmall(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(XyTheme.dimens.itemHeight),
                                text = it.name,
                                select = selectLibraryId == it.id,
                                onClick = {
                                    coroutineScope.launch {
                                        onSelectLibraryId(it)
                                        modalBottomSheetState.hide()
                                    }.invokeOnCompletion {
                                        onSetState(false)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}