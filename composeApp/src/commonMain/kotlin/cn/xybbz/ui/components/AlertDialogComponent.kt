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


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cn.xybbz.ui.ext.debounceClickable
import cn.xybbz.ui.ext.platformHoverClickable
import cn.xybbz.ui.theme.XyTheme
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import xymusic.composeapp.generated.resources.Res
import xymusic.composeapp.generated.resources.cancel
import xymusic.composeapp.generated.resources.check_24px
import xymusic.composeapp.generated.resources.confirm
import xymusic.composeapp.generated.resources.delete_24px
import xymusic.composeapp.generated.resources.download_24px
import xymusic.composeapp.generated.resources.edit_24px
import xymusic.composeapp.generated.resources.folder_managed_24px
import xymusic.composeapp.generated.resources.info_24px
import xymusic.composeapp.generated.resources.queue_music_24px
import xymusic.composeapp.generated.resources.settings_24px
import xymusic.composeapp.generated.resources.warning_24px

val mainMoeScope = MainScope()

val alertDialogObjectList = mutableStateListOf<AlertDialogObject>()

/**
 * 弹窗语义色：决定顶部导轨、图标方块、引导语与主按钮的颜色。
 *
 * @author 刘梦龙
 * @date 2026/11/16
 */
enum class XyDialogTone {
    /** 信息态，沿用项目主色，用于输入、选择、普通提示 */
    Info,

    /** 危险态，用于删除、覆盖、断开等不可撤销操作 */
    Danger,

    /** 成功态，用于导入完成、已保存等结果确认 */
    Success,
}

/**
 * 弹窗外壳图标，映射到 composeResources 下已有的线性图标。
 *
 * 项目没有引入 material-icons 依赖，因此只能在这里枚举现有资源；缺图标时回落到信息图标。
 *
 * @author 刘梦龙
 * @date 2026/11/16
 */
enum class XyDialogIcon {
    /** 询问 / 默认 */
    Ask,

    /** 警告三角，用于危险但不致命 */
    Warn,

    /** 圆形信息，用于普通提示 */
    Info,

    /** 对勾，用于结果成功 */
    Success,

    /** 铅笔，用于重命名 / 输入 */
    Edit,

    /** 列表，用于格式或条目选择 */
    List,

    /** 归档盒，用于缓存 / 存储 */
    Box,

    /** 齿轮，用于设置与后台任务 */
    Gear,

    /** 下载箭头，用于导入 / 下载 */
    Download,

    /** 垃圾桶，用于删除 */
    Trash,

    /** 不显示图标 */
    None,
}

/**
 * 图标枚举到实际矢量资源的映射。
 *
 * @return 对应 drawable；[XyDialogIcon.Ask] 与 [XyDialogIcon.None] 回落为信息图标
 * @author 刘梦龙
 * @date 2026/11/16
 */
private fun XyDialogIcon.drawable(): DrawableResource = when (this) {
    XyDialogIcon.Warn -> Res.drawable.warning_24px
    XyDialogIcon.Success -> Res.drawable.check_24px
    XyDialogIcon.Edit -> Res.drawable.edit_24px
    XyDialogIcon.List -> Res.drawable.queue_music_24px
    XyDialogIcon.Box -> Res.drawable.folder_managed_24px
    XyDialogIcon.Gear -> Res.drawable.settings_24px
    XyDialogIcon.Download -> Res.drawable.download_24px
    XyDialogIcon.Trash -> Res.drawable.delete_24px
    else -> Res.drawable.info_24px
}

/**
 * 未显式指定图标时，按语义色派生一个默认图标。
 *
 * 这样调用点只需要给出 tone，就能自动拿到合适的图标，不必逐个补 icon 参数。
 *
 * @return 语义对应的默认图标
 * @author 刘梦龙
 * @date 2026/11/16
 */
private fun XyDialogTone.defaultIcon(): XyDialogIcon = when (this) {
    XyDialogTone.Info -> XyDialogIcon.Ask
    XyDialogTone.Danger -> XyDialogIcon.Warn
    XyDialogTone.Success -> XyDialogIcon.Success
}

/**
 * 单个动作按钮配置，用于替换固定的「取消 + 确定」两键。
 *
 * @param text 按钮文案，调用点已解析好的字符串
 * @param onClick 点击回调
 * @param primary 是否主按钮（填充语义色）
 * @param enabled 是否可用
 * @author 刘梦龙
 * @date 2026/11/16
 */
data class XyDialogActionSpec(
    val text: String,
    val onClick: () -> Unit,
    val primary: Boolean = false,
    val enabled: Boolean = true,
)

/**
 * 警报对话框对象
 * @author 刘梦龙
 * @date 2025/05/07
 * @constructor 创建[AlertDialogObject]
 * @param [title] 标题
 * @param [content] 内容插槽
 * @param [modifier] 修饰符
 * @param [onCloseRequest] 关闭方法
 * @param [onDismissRequest] 取消方法
 * @param [onConfirmation] 确认方法
 * @param [properties] 属性
 * @param [dismissText] 取消文字
 * @param [confirmText] 确认文字
 * @param [dismissEnabled] 取消按钮是否可用
 * @param [confirmEnabled] 确认按钮是否可用
 * @param [tone] 语义色，默认信息态
 * @param [icon] 外壳图标
 * @param [kicker] 引导语资源，等宽小字，如「危险操作 / 不可撤销」；不传则不显示该行
 * @param [body] 正文，提供后不必再在 content 里手写 XyTextSubSmall
 * @param [actions] 自定义动作区；为空时按 dismiss / confirm 生成默认两键
 */
data class AlertDialogObject(
    // ---------- 原有字段（签名保持不变） ----------
    val title: String? = null,
    val content: @Composable ((AlertDialogObject) -> Unit)? = null,
    val modifier: Modifier = Modifier,
    val onCloseRequest: (() -> Unit)? = null,
    val onDismissRequest: (() -> Unit)? = null,
    val onConfirmation: (() -> Unit)? = null,
    val properties: DialogProperties = DialogProperties(),
    val dismissText: StringResource = Res.string.cancel,
    val confirmText: StringResource = Res.string.confirm,
    val dismissEnabled: Boolean = true,
    val confirmEnabled: Boolean = true,
    // ---------- 新增字段（全部有默认值，旧调用点无需改动） ----------
    /** 语义色，默认信息态 */
    val tone: XyDialogTone = XyDialogTone.Info,
    /** 外壳图标；不传时按 [tone] 自动派生（危险→警告、成功→对勾、信息→询问） */
    val icon: XyDialogIcon? = null,
    /** 引导语资源，等宽小字，如「危险操作 / 不可撤销」；不传则不显示该行 */
    val kicker: StringResource? = null,
    /** 正文；提供后不必再在 content 里手写 XyTextSubSmall */
    val body: String? = null,
    /** 自定义动作区；为空时按 dismiss / confirm 生成默认两键 */
    val actions: List<XyDialogActionSpec>? = null,
)

/**
 * 弹窗专属尺寸令牌，全部为整数 dp/sp，便于与设计稿逐项对照；两端共用同一套数值。
 *
 * @author 刘梦龙
 * @date 2026/11/16
 */
private object XyDialogTokens {
    /** 卡片最大宽度，窄屏时按可用宽度撑满 */
    val maxWidth = 420.dp

    /** 卡片圆角 */
    val corner = 14.dp

    /** 卡片左右内边距 */
    val padHorizontal = 22.dp

    /** 卡片顶部内边距 */
    val padTop = 22.dp

    /** 卡片底部内边距 */
    val padBottom = 18.dp

    /** 图标方块尺寸 */
    val iconBox = 34.dp

    /** 图标方块圆角 */
    val iconCorner = 9.dp

    /** 图标方块内图标尺寸 */
    val iconGlyph = 17.dp

    /** 顶部语义导轨高度 */
    val railHeight = 2.dp

    /** 图标与标题区之间的间距 */
    val iconGap = 12.dp

    /** 标题与正文 / 插槽之间的间距 */
    val sectionGap = 15.dp

    /** 分隔线上方间距 */
    val dividerTop = 16.dp

    /** 分隔线与动作区之间的间距 */
    val footerTop = 14.dp

    /** 动作按钮高度 */
    val btnHeight = 36.dp

    /** 动作按钮圆角 */
    val btnCorner = 7.dp

    /** 动作按钮之间的间距 */
    val btnGap = 10.dp

    /** 动作按钮左右内边距 */
    val btnPadHorizontal = 16.dp
}

/**
 * 取语义色对应的前景色（导轨 / 图标 / 引导语 / 主按钮）。
 *
 * @return 语义色；Info 沿用项目主色，Danger / Success 取扩展色
 * @author 刘梦龙
 * @date 2026/11/16
 */
@Composable
private fun XyDialogTone.foreground(): Color = when (this) {
    XyDialogTone.Info -> MaterialTheme.colorScheme.primary
    XyDialogTone.Danger -> XyTheme.extendedColors.danger
    XyDialogTone.Success -> XyTheme.extendedColors.success
}

/**
 * 按背景亮度挑一个可读的文字色。
 *
 * 项目的 colorScheme.onPrimary 未被主题覆盖，深色主题下仍是 Material3 默认深紫，
 * 直接拿来当主按钮文字会与语义色冲突，因此按亮度自行判定。
 *
 * @param background 按钮 / 方块的背景色
 * @return 背景偏亮时返回近黑色，否则返回白色
 * @author 刘梦龙
 * @date 2026/11/16
 */
private fun contrastOn(background: Color): Color =
    if (background.luminance() > 0.55f) Color(0xFF14161A) else Color.White

/**
 * 弹窗统一外壳。
 *
 * 七段结构：语义导轨 → 图标 → 引导语 → 标题 → 正文 → 自定义插槽 → 动作区。
 * 标题、正文、插槽、动作区任一段为空时自动收起，不留空白。
 *
 * @param title 标题，为空时头部整段隐藏
 * @param kicker 引导语，为空时只显示标题
 * @param body 正文，调用点直接传已解析好的字符串
 * @param tone 语义色
 * @param icon 外壳图标
 * @param content 自定义插槽：输入 / 单选 / 进度 / 结果
 * @param footer 动作区；为空时连分隔线一起隐藏
 * @param modifier 外部修饰符
 * @author 刘梦龙
 * @date 2026/11/16
 */
@Composable
private fun XyDialogShell(
    title: String?,
    kicker: String?,
    body: String?,
    tone: XyDialogTone,
    icon: XyDialogIcon,
    content: (@Composable () -> Unit)?,
    footer: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
) {
    // 语义前景色：导轨、图标、引导语、主按钮三处同色，标题不使用它
    val foreground = tone.foreground()
    val shape = RoundedCornerShape(XyDialogTokens.corner)
    // 可空参数先落到局部变量，保证在下方 lambda 内可以安全智能转换
    val titleText = title
    val kickerText = kicker
    val bodyText = body
    val contentSlot = content
    val footerSlot = footer
    // 没有标题时头部整段不渲染，正文与插槽也就不需要顶部间距
    val hasHeader = titleText != null
    val sectionTop = if (hasHeader) XyDialogTokens.sectionGap else 0.dp
    Column(
        modifier = modifier
            // 先限宽再撑满：宽屏固定 420dp，窄屏按可用宽度撑满
            .widthIn(max = XyDialogTokens.maxWidth)
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest),
    ) {
        // 顶部语义导轨：替代旧实现里整句被染红的标题，语义只占 2dp 高
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(XyDialogTokens.railHeight)
                .background(
                    Brush.horizontalGradient(
                        listOf(foreground, foreground.copy(alpha = 0.12f), Color.Transparent),
                    ),
                ),
        )
        Column(
            modifier = Modifier.padding(
                start = XyDialogTokens.padHorizontal,
                end = XyDialogTokens.padHorizontal,
                top = XyDialogTokens.padTop,
                bottom = XyDialogTokens.padBottom,
            ),
        ) {
            if (titleText != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (icon != XyDialogIcon.None) {
                        // 34dp 图标方块：底色 13%、描边 40%，与导轨、主按钮同色
                        Box(
                            modifier = Modifier
                                .size(XyDialogTokens.iconBox)
                                .background(
                                    color = foreground.copy(alpha = 0.13f),
                                    shape = RoundedCornerShape(XyDialogTokens.iconCorner),
                                )
                                .border(
                                    width = 1.dp,
                                    color = foreground.copy(alpha = 0.40f),
                                    shape = RoundedCornerShape(XyDialogTokens.iconCorner),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(icon.drawable()),
                                contentDescription = null,
                                tint = foreground,
                                modifier = Modifier.size(XyDialogTokens.iconGlyph),
                            )
                        }
                        Spacer(modifier = Modifier.width(XyDialogTokens.iconGap))
                    }
                    // 标题列占满剩余宽度，长标题自动换行而不是把图标挤出卡片
                    Column(modifier = Modifier.weight(1f)) {
                        if (kickerText != null) {
                            Text(
                                text = kickerText,
                                fontSize = 9.5.sp,
                                lineHeight = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.7.sp,
                                color = foreground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        // 标题一律用正文色，语义只由导轨和图标承担
                        Text(
                            text = titleText,
                            fontSize = 17.sp,
                            lineHeight = 21.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
            if (bodyText != null) {
                Text(
                    text = bodyText,
                    modifier = Modifier.padding(top = sectionTop),
                    fontSize = 13.sp,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (contentSlot != null) {
                // 插槽一律撑满内容宽：修掉旧实现里 XyRow 造成的居中短条与双重内边距
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = if (bodyText != null) XyDialogTokens.sectionGap else sectionTop,
                        ),
                ) {
                    contentSlot()
                }
            }
            if (footerSlot != null) {
                // 上方没有任何内容时不需要分隔线，否则会在卡片顶部留出一条孤立细线
                if (hasHeader || bodyText != null || contentSlot != null) {
                    HorizontalDivider(
                        modifier = Modifier.padding(top = XyDialogTokens.dividerTop),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.09f),
                    )
                }
                Box(modifier = Modifier.padding(top = XyDialogTokens.footerTop)) {
                    footerSlot()
                }
            }
        }
    }
}

/**
 * 动作区：一律右对齐横排，主按钮在最右；两端共用同一排布。
 *
 * @param tone 语义色
 * @param actions 动作项，按传入顺序从左到右排列
 * @author 刘梦龙
 * @date 2026/11/16
 */
@Composable
private fun XyDialogFooter(
    tone: XyDialogTone,
    actions: List<XyDialogActionSpec>,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        actions.forEachIndexed { index, spec ->
            XyDialogButton(spec = spec, tone = tone)
            // 最后一个按钮后面不补间距
            if (index != actions.lastIndex) {
                Spacer(modifier = Modifier.width(XyDialogTokens.btnGap))
            }
        }
    }
}

/**
 * 弹窗专用按钮。
 *
 * 不复用 XyButton：它把圆角锁在 dimens.corner、默认填充主色，加参数会波及其它页面，
 * 因此这里按设计稿单独实现：36dp 高、7dp 圆角、主按钮填充语义色、次按钮描边。
 *
 * @param spec 按钮配置
 * @param tone 语义色
 * @author 刘梦龙
 * @date 2026/11/16
 */
@Composable
private fun XyDialogButton(
    spec: XyDialogActionSpec,
    tone: XyDialogTone,
) {
    val foreground = tone.foreground()
    val shape = RoundedCornerShape(XyDialogTokens.btnCorner)
    // 悬停源：桌面端给出淡底色反馈，不做缩放以免连排按钮抖动
    val hoverSource = remember { MutableInteractionSource() }
    val hovered by hoverSource.collectIsHoveredAsState()
    // 点击源：只负责防抖点击，不绘制默认 ripple
    val clickSource = remember { MutableInteractionSource() }
    val containerColor = when {
        spec.primary -> foreground
        hovered -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
        else -> Color.Transparent
    }
    // 禁用态不换色，整体降到 40% 不透明度，与设计稿「DISABLED = 40%」一致
    val contentColor = if (spec.primary) {
        contrastOn(foreground)
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    Box(
        modifier = Modifier
            .height(XyDialogTokens.btnHeight)
            .platformHoverClickable(interactionSource = hoverSource, enabled = spec.enabled)
            .alpha(if (spec.enabled) 1f else 0.4f)
            .clip(shape)
            // 背景先画、描边后画，否则次按钮的边框会被背景盖住
            .background(containerColor)
            .then(
                if (spec.primary) {
                    Modifier
                } else {
                    // 次按钮走 1dp 描边，与填充式主按钮形成主次层级
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f),
                        shape = shape,
                    )
                },
            )
            .debounceClickable(
                interactionSource = clickSource,
                indication = null,
                enabled = spec.enabled,
                role = Role.Button,
                onClick = spec.onClick,
            )
            .padding(horizontal = XyDialogTokens.btnPadHorizontal),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = spec.text,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = contentColor,
            maxLines = 1,
        )
    }
}

/**
 * 警报对话框组件。
 *
 * 职责收敛为：遍历列表 → 生成动作项 → 交给 [XyDialogShell]。所有弹窗统一用居中的 Dialog 承载。
 */
@Composable
fun AlertDialogComponent() {
    alertDialogObjectList.forEach { dialogObject ->
        val tone = dialogObject.tone
        // 文案必须先在这里解析，不能放进非 composable 的 buildList 里
        val dismissLabel = stringResource(dialogObject.dismissText)
        val confirmLabel = stringResource(dialogObject.confirmText)
        // 引导语同样在这里解析，调用点只负责传资源，避免在非 composable 回调里取值
        val kickerText = dialogObject.kicker?.let { stringResource(it) }
        // 自定义 actions 优先，否则由 dismiss / confirm 生成默认两键
        val actions = dialogObject.actions ?: buildList {
            dialogObject.onDismissRequest?.let { onDismiss ->
                add(
                    XyDialogActionSpec(
                        text = dismissLabel,
                        onClick = {
                            onDismiss()
                            dialogObject.dismiss()
                        },
                        enabled = dialogObject.dismissEnabled,
                    ),
                )
            }
            dialogObject.onConfirmation?.let { onConfirm ->
                add(
                    XyDialogActionSpec(
                        text = confirmLabel,
                        onClick = {
                            onConfirm()
                            dialogObject.dismiss()
                        },
                        primary = true,
                        enabled = dialogObject.confirmEnabled,
                    ),
                )
            }
        }
        // 内容区与动作区都是 composable lambda，用局部变量收拢后再传给外壳
        val contentSlot = dialogObject.content
        val contentBlock: (@Composable () -> Unit)? = if (contentSlot != null) {
            { contentSlot(dialogObject) }
        } else {
            null
        }
        val footerBlock: (@Composable () -> Unit)? = if (actions.isEmpty()) {
            null
        } else {
            { XyDialogFooter(tone = tone, actions = actions) }
        }
        Dialog(
            onDismissRequest = {
                dialogObject.onCloseRequest?.invoke()
                dialogObject.dismiss()
            },
            properties = dialogObject.properties,
        ) {
            XyDialogShell(
                title = dialogObject.title,
                kicker = kickerText,
                body = dialogObject.body,
                tone = tone,
                icon = dialogObject.icon ?: tone.defaultIcon(),
                content = contentBlock,
                footer = footerBlock,
                modifier = dialogObject.modifier,
            )
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
