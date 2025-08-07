package cn.xybbz.ui.components


import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cn.xybbz.ui.ItemOutSpacer
import cn.xybbz.ui.SaltTheme
import cn.xybbz.ui.ext.debounceClickable
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyButton
import cn.xybbz.ui.xy.XyColumn
import cn.xybbz.ui.xy.XyItemOutSpacer
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
 * @param [onCloseRequest] 关闭方法
 * @param [onDismissRequest] 取消方法
 * @param [onConfirmation] 确认方法
 * @param [properties] 属性
 * @param [dismissText] 取消问题
 * @param [confirmText] 确认文字
 */
data class AlertDialogObject(
    val title: @Composable (() -> Unit)? = null,
    val content: @Composable (() -> Unit)? = null,
    val modifier: Modifier = Modifier,
//    val onIfAlertDialog: () -> Boolean,
//    val onSetIfAlertDialog: (Boolean) -> Unit,
    val onCloseRequest: (() -> Unit)? = null,
    val onDismissRequest: (() -> Unit)? = null,
    val onConfirmation: (() -> Unit)? = null,
    val properties: DialogProperties = DialogProperties(),
    val dismissText: String = "取消",
    val confirmText: String = "确认",
)

/**
 * 警报对话框组件
 */
@Composable
fun AlertDialogComponent() {
    alertDialogObjectList.forEach {
        Dialog(onDismissRequest = {
            it.onCloseRequest?.invoke()
            it.dismiss()
        }) {
            XyColumn {
                Spacer(modifier = Modifier.height(XyTheme.dimens.innerVerticalPadding))
                it.title?.invoke()
                XyItemOutSpacer()
                it.content?.invoke()
                XyItemOutSpacer()
                Row(modifier = Modifier.padding(horizontal = SaltTheme.dimens.outerHorizontalPadding)) {
                    if (it.onConfirmation != null) {
                        XyButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                it.onConfirmation.invoke()
                                it.dismiss()
                            },
                            text = it.confirmText,

                            )
                    }
                    Spacer(modifier = Modifier.width(XyTheme.dimens.outerHorizontalPadding))
                    it.onDismissRequest?.run {
                        XyButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                it.onDismissRequest.invoke()
                                it.dismiss()
                            },
                            text = it.dismissText,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            backgroundColor = MaterialTheme.colorScheme.surfaceContainerLowest
                        )
                    }
                }
                ItemOutSpacer()
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


/**
 * 警报对话框组件
 * @param [title] 标题
 * @param [onIfAlertDialog] 打开if警报对话框
 * @param [content] 所容纳之物
 * @param [onDismissRequest] 应驳回请求
 * @param [onConfirmation] 确认时
 */
@Composable
fun AlertDialogComponent(
    title: String,
    onIfAlertDialog: () -> Boolean,
    onSetIfAlertDialog: (Boolean) -> Unit,
    content: @Composable (() -> Unit)? = null,
    onDismissRequest: (() -> Unit)? = null,
    onConfirmation: (() -> Unit)? = null,
) {


    if (onIfAlertDialog()) {
        AlertDialog(
            /*icon = {
                Icon(imageVector = Icons.Default.Info, contentDescription = "Example Icon")
            },*/
            title = {
                Text(text = title)
            },
            text = {
//                Text(text = "Alert dialog example")
                content?.let { it() }
            },
            onDismissRequest = {
                onDismissRequest?.invoke()
                onSetIfAlertDialog.invoke(false)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        /*onConfirmation?.invoke()
                        onSetIfAlertDialog.invoke(false)*/
                    },

                    ) {
                    Text("确定", modifier = Modifier.debounceClickable {
                        onConfirmation?.invoke()
                        onSetIfAlertDialog.invoke(false)
                    })
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {

                    }
                ) {
                    Text("取消", modifier = Modifier.debounceClickable {
                        onDismissRequest?.invoke()
                        onSetIfAlertDialog.invoke(false)
                    })
                }
            }
        )
    }
}
