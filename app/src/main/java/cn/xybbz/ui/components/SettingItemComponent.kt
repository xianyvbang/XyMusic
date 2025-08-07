package cn.xybbz.ui.components


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.RoundedSurfaceColumn
import cn.xybbz.ui.xy.XyItemBigTitle
import cn.xybbz.ui.xy.XyItemSwitcherNotTextColor
import cn.xybbz.ui.xy.XyItemText
import cn.xybbz.ui.xy.XyItemTextAlignEnd
import cn.xybbz.ui.xy.XyItemTextPadding
import cn.xybbz.ui.xy.XyItemTitleNotHorizontalPadding
import cn.xybbz.ui.xy.XyRow
import kotlinx.coroutines.launch

@Composable
fun SettingItemComponent(
    title: String,
    modifier: Modifier = Modifier,
    info: String = "",
    maxLines: Int = 1,
    onClick: (() -> Unit)? = null,
    content: @Composable (() -> Unit)? = null,
    onCloseRequest: (() -> Unit)? = null,
    onDismissRequest: (() -> Unit)? = null,
    onConfirmation: (() -> Unit)? = null,
    onRouter: (() -> Unit)? = null
) {

    val coroutineScope = rememberCoroutineScope()

    SettingParentItemComponent(
        modifier = modifier,
        title = title,
        onClick = {
            onClick?.invoke()
            if (onRouter != null)
                onRouter()
            else
                AlertDialogObject(
                    title = {
                        XyItemBigTitle(
                            text = title
                        )
                    },
                    content = content,
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
                XyItemTextAlignEnd(text = info, maxLines = maxLines)
                Spacer(modifier = Modifier.width(5.dp))
                IconButton(onClick = composeClick {
                    if (onRouter != null)
                        onRouter()
                    else
                        AlertDialogObject(title = {
                            XyItemBigTitle(
                                text = title
                            )
                        }, content = content, onConfirmation = {
                            coroutineScope.launch {
                                onConfirmation?.invoke()
                            }.invokeOnCompletion {

                            }

                        }, onDismissRequest = {
                            onDismissRequest?.invoke()
                        }).show()
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = "进入设置"
                    )
                }
            }
        })
}

@Composable
fun SettingParentItemComponent(
    modifier: Modifier = Modifier,
    title: String,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    XyRow(
        modifier = modifier.height(XyTheme.dimens.itemHeight),
        onClick = composeClick {
            onClick?.invoke()
        }) {
        XyItemText(text = title, color = textColor)
        trailingContent?.invoke()

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


/**
 * 存储管理项
 * @param [modifier] 修饰语
 * @param [cacheSize] 缓存大小
 * @param [onClick] 点击时
 * @param [text] 文本
 * @param [describe] 描述
 */
@Composable
fun XySwitchItem(
    modifier: Modifier = Modifier,
    text: String,
    describe: String,
    enabled: Boolean = true,
    ifChecked: Boolean,
    onChange: (Boolean) -> Unit,
) {
    RoundedSurfaceColumn(horizontalAlignment = Alignment.Start) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .then(modifier)
                .fillMaxWidth()
                .padding(
                    horizontal = XyTheme.dimens.outerHorizontalPadding
                )
        ) {
            XyItemTitleNotHorizontalPadding(text = text)
            Switch(
                checked = ifChecked, onCheckedChange = onChange, enabled = enabled,
                colors = SwitchDefaults.colors(
                    uncheckedBorderColor = Color.Transparent,
                    checkedThumbColor = MaterialTheme.colorScheme.surface
                )
            )
        }
        XyItemTextPadding(
            text = describe,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            overflow = TextOverflow.Visible,
            vertical = 0.dp
        )
    }
}
