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

package cn.xybbz.ui.components


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.xybbz.R
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyColumnButton
import cn.xybbz.ui.xy.XyItemSwitcherNotTextColor
import cn.xybbz.ui.xy.XyRow
import cn.xybbz.ui.xy.XyTextSubSmall
import kotlinx.coroutines.launch

@Composable
fun SettingItemComponent(
    title: String,
    modifier: Modifier = Modifier,
    info: String? = null,
    bottomInfo: String? = null,
    imageVector: ImageVector? = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
    maxLines: Int = 1,
    ifOpenBadge: Boolean = false,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    onCloseRequest: (() -> Unit)? = null,
    onDismissRequest: (() -> Unit)? = null,
    onConfirmation: (() -> Unit)? = null,
    onRouter: (() -> Unit)? = null
) {

    val coroutineScope = rememberCoroutineScope()

    SettingParentItemComponent(
        modifier = modifier,
        title = title,
        bottomInfo = bottomInfo,
        enabled = enabled,
        onClick = {
            onClick?.invoke()
            if (onRouter != null)
                onRouter()
            else
                AlertDialogObject(
                    title = title,
                    content = if (content != null) {
                        { content.invoke() }
                    } else null,
                    onConfirmation = {
                        coroutineScope.launch {
                            onConfirmation?.invoke()
                        }.invokeOnCompletion {

                        }

                    },
                    onDismissRequest = {
                        onDismissRequest?.invoke()
                    },
                    onCloseRequest = onCloseRequest
                ).show()
        }, trailingContent = {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                info?.let {
                    XyTextSubSmall(
                        text = info,
                        maxLines = maxLines
                    )
                }
                trailingContent?.invoke()
                imageVector?.let {
                    Spacer(modifier = Modifier.width(5.dp))
                    IconButton(onClick = composeClick {
                        if (onRouter != null)
                            onRouter()
                        else
                            AlertDialogObject(
                                title = title,
                                content = if (content != null) {
                                    { content.invoke() }
                                } else null,
                                onConfirmation = {
                                    coroutineScope.launch {
                                        onConfirmation?.invoke()
                                    }.invokeOnCompletion {

                                    }

                                }, onDismissRequest = {
                                    onDismissRequest?.invoke()
                                }).show()
                    }) {
                        BadgedBox(
                            badge = {
                                if (ifOpenBadge)
                                    Badge()
                            }
                        ) {
                            Icon(
                                imageVector = imageVector,
                                contentDescription = stringResource(
                                    R.string.enter_settings,
                                    title
                                )
                            )
                        }

                    }
                }
            }
        })
}

@Composable
fun SettingParentItemComponent(
    modifier: Modifier = Modifier,
    title: String,
    bottomInfo: String? = null,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    enabled: Boolean = true,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    XyColumnButton(
        modifier = modifier.height(XyTheme.dimens.itemHeight),
        enabled = enabled,
        onClick = {
            onClick?.invoke()
        }
    ) {
        XyRow(paddingValues = PaddingValues(),modifier = Modifier.weight(1f),) {
            XyTextSubSmall(
                text = title,
                color = if (enabled) textColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
            trailingContent?.invoke()

        }
        bottomInfo?.let {
            Text(
                modifier = Modifier.weight(1f),
                text = bottomInfo,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
                color = if (enabled) textColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

}

@Composable
fun MusicSettingSwitchItemComponent(
    title: String,
    ifChecked: Boolean,
    onRouter: (Boolean) -> Unit,
) {
    XyItemSwitcherNotTextColor(state = ifChecked, onChange = { onRouter(it) }, text = title)
}