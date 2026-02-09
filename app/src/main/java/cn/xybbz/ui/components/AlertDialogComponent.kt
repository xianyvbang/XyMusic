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


import androidx.annotation.StringRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cn.xybbz.R
import cn.xybbz.ui.ext.brashColor
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyButton
import cn.xybbz.ui.xy.XyColumn
import cn.xybbz.ui.xy.XyItemOutSpacer
import cn.xybbz.ui.xy.XyScreenTitle
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

val mainMoeScope = MainScope()

val alertDialogObjectList = mutableStateListOf<AlertDialogObject>()

/**
 * 警报对话框对象
 * @author 刘梦龙
 * @date 2025/05/07
 * @constructor 创建[AlertDialogObject]
 * @param [title] 标题
 * @param [content] 内容
 * @param [modifier] 修饰符
 * @param [ifWarning] 是否是告警弹窗
 * @param [onCloseRequest] 关闭方法
 * @param [onDismissRequest] 取消方法
 * @param [onConfirmation] 确认方法
 * @param [properties] 属性
 * @param [dismissText] 取消问题
 * @param [confirmText] 确认文字
 */
data class AlertDialogObject(
    val title: String? = null,
    val content: @Composable ((AlertDialogObject) -> Unit)? = null,
    val modifier: Modifier = Modifier,
    val ifWarning: Boolean = false,
    val brashColors: List<Color> = if (ifWarning) listOf(
        Color(0xFF814937),
        Color(0xFF8F6952)
    ) else listOf(
        Color(0xFF426770),
        Color(0xFF577C83)
    ),
    val onCloseRequest: (() -> Unit)? = null,
    val onDismissRequest: (() -> Unit)? = null,
    val onConfirmation: (() -> Unit)? = null,
    val properties: DialogProperties = DialogProperties(),
    @param:StringRes val dismissText: Int = R.string.cancel,
    @param:StringRes val confirmText: Int = R.string.confirm,
)

/**
 * 警报对话框组件
 */
@Composable
fun AlertDialogComponent() {
    alertDialogObjectList.forEach {
        Dialog(
            onDismissRequest = {
                it.onCloseRequest?.invoke()
                it.dismiss()
            }) {
            XyColumn(
                paddingValues = PaddingValues(0.dp),
                clipSize = XyTheme.dimens.dialogCorner,
                backgroundColor = Color.Transparent,
                modifier = Modifier
                    .clip(RoundedCornerShape(XyTheme.dimens.corner))
                    .brashColor(
                        it.brashColors[0],
                        it.brashColors[1]
                    )
            ) {
                XyItemOutSpacer()
                it.title?.let { title ->
                    XyScreenTitle(
                        text = title,
                        color = if (it.ifWarning) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface
                    )
                }
                XyItemOutSpacer()
                it.content?.invoke(it)
                XyItemOutSpacer()
                if (it.onDismissRequest != null || it.onConfirmation != null) {
                    Row(modifier = Modifier.padding(horizontal = XyTheme.dimens.outerHorizontalPadding)) {
                        it.onDismissRequest?.run {
                            XyButton(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    it.onDismissRequest.invoke()
                                    it.dismiss()
                                },
                                text = stringResource(it.dismissText),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                backgroundColor = MaterialTheme.colorScheme.surfaceContainerLowest
                            )
                            Spacer(modifier = Modifier.width(XyTheme.dimens.outerHorizontalPadding))
                        }
                        if (it.onConfirmation != null) {
                            XyButton(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    it.onConfirmation.invoke()
                                    it.dismiss()
                                },
                                text = stringResource(it.confirmText)
                            )
                        }
                    }
                    XyItemOutSpacer()
                }
            }
        }
    }
}

/**
 * 关闭弹窗
 */
fun AlertDialogObject.dismiss() = apply {
    mainMoeScope.launch {
        alertDialogObjectList.remove(this@dismiss)
    }
}

/**
 * 显示弹窗
 */
fun AlertDialogObject.show() = apply {
    mainMoeScope.launch {
        alertDialogObjectList.add(this@show)
    }
}