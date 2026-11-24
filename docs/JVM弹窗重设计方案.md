# AlertDialogComponent 改造方案（单文件重构）

> 配套设计稿：`dialog-redesign.html`（结构解剖 / 令牌表 / 10 个变体 / 状态与交互）
>
> **改动范围**：只改 `composeApp/src/commonMain/kotlin/cn/xybbz/ui/components/AlertDialogComponent.kt` 一个文件。
> 不新增文件、不改 `strings.xml`、不改 `XyButton` / `XyDimens` / `XyColor*`、不改任何调用点（阶段 1）。
>
> **执行约束**：新增的类 / 方法 / 属性必须写中文 KDoc；不做 `git commit` / `git push`；AI 不主动执行 Gradle 编译。

---

## 1. 边界

### 做

1. 语义色由 `tone`（Info / Danger / Success）统一驱动，替换"只把标题染红"的 `ifWarning`。
2. 外壳重排为七段：语义导轨 → 图标 + 引导语 + 标题 → 正文 → 插槽 → 动作区；缺省段落自动收起。
3. 动作按钮单独实现（36dp / r7 / 主次分明 / 描边次按钮），不再复用 `XyButton`。
4. 修掉双重内边距、`dialogCorner` 被覆盖、`XyItemOutSpacer` 硬间距、标题误用 `titleLarge` 四个既有缺陷。
5. 全部代码内聚在本文件：枚举、令牌、外壳、动作区、按钮都写成 `private` / `internal`，不新增文件。

### 不做（都因为这轮只碰一个文件）

| 不做 | 原因 | 替代 |
|---|---|---|
| 动作区左侧 `Esc / Enter` 键位提示 | `XyDialogKeyHint` 只能在 jvmMain 写真实实现，commonMain 需要 expect/actual，会多出三个平台文件 | 本轮省略；设计稿里的键位提示留作二期 |
| `strings.xml` 追加 kicker 文案 | 要改资源文件 | `kicker` 默认 `null`，不传就不显示；迁移时由调用点直接传中文字符串 |
| 显式设置遮罩色 | `DialogProperties.scrimColor` 在 commonMain 是否可用需验证 | 完全沿用调用方传入的 `properties`，行为与现在一致 |
| 弹窗队列 / 只渲染最后一个 | 属于行为变化 | 保持 `forEach` |
| 按钮 loading 态 | 设计稿有，但没有调用点需要 | 不实现 |

> 渲染入口在 [MainScreen.kt L153](../composeApp/src/commonMain/kotlin/cn/xybbz/ui/screens/MainScreen.kt#L153)，JVM 与 Android 共用，因此**这次改造会同时改到移动端弹窗外观**。因为代码只有一份、没有平台分支，移动端会跟着一起变好看，这也正是单文件方案的必然结果。

---

## 2. 现状问题（都在这一个文件里）

[AlertDialogComponent.kt](../composeApp/src/commonMain/kotlin/cn/xybbz/ui/components/AlertDialogComponent.kt) 共 179 行。

| # | 问题 | 位置 | 后果 |
|---|---|---|---|
| 1 | 语义只有两档，且只作用于标题文字 | L95-L99 `ifWarning ? errorContainer : onSurface` | `errorContainer` 是容器色，深色主题下对比度极低；"删除"和"取消"权重相同 |
| 2 | 圆角双套控制 | L107-L109 `clipSize = dialogCorner` 被外层 `clip(RoundedCornerShape(corner))` 覆盖 | 实际渲染 12dp，`dialogCorner` 是死参数 |
| 3 | 内容被塞进 `XyRow(Center)` | L120-L125 | `XyRow` 内部 `fillMaxWidth()` 再补一层 `outerHorizontalPadding`，输入框 / 列表变成居中短条 + 双重内边距 |
| 4 | 间距靠 `XyItemOutSpacer()` 堆 | L112 / L119 / L126 / L155 | 固定 16dp 大间距，段落节奏不可控 |
| 5 | 标题复用页面标题组件 | L114-L117 `XyScreenTitle` → `titleLarge` | 弹窗用了页面级字号 |
| 6 | 动作区只有等宽双键 | L128-L155 | 无法做危险主按钮、禁用态、三按钮；`XyButton` 圆角锁死且不能加描边 |

---

## 3. 目标结构

```
┌──────────────────────────────────────────┐
│ ▔▔▔▔ 2dp 语义导轨（tone → 12% → 透明）    │
│  ┌────┐  KICKER 9.5sp 等宽 字距 1.7sp    │
│  │icon│  标题 17sp/700/行高 21          │
│  └────┘                                  │
│  （15dp）                                 │
│  正文 13sp / 行高 22sp / onSurfaceVariant │
│  （15dp）                                 │
│  插槽：输入 / 选择 / 进度 / 结果 / 概要     │
│  ─── 1dp 分隔线（上方 16dp，色 9%）──────  │
│  （14dp）                                  │
│                    [取消] [主按钮] 36dp   │
└──────────────────────────────────────────┘
```

| 项 | 值 |
|---|---|
| 卡片宽度 | `widthIn(max = 420.dp).fillMaxWidth()` |
| 圆角 | 14dp |
| 内边距 | 22 / 22 / 22 / 18 |
| 导轨 | 高 2dp，`tone → tone@12% → 透明` |
| 图标方块 | 34dp，r9，底 `tone@13%`，描边 1dp `tone@40%` |
| 标题 | 17sp / Bold / 行高 21sp / `onSurface`（**不染色**） |
| 正文 | 13sp / 行高 22sp / `onSurfaceVariant` |
| 分隔线 | 1dp / `onSurface@9%` |
| 按钮 | 高 36dp / r7 / 横排右对齐 / 间距 10dp / 内距 16dp / 12.5sp·600 |
| 卡片底 | `surfaceContainerLowest` |

### 语义色

| tone | 色源 | 浅色 | 深色 |
|---|---|---|---|
| Info | `colorScheme.primary` | #2F70B1 | #4D93CB |
| Danger | `XyTheme.extendedColors.danger` | #A84A44 | #CF6B62 |
| Success | `XyTheme.extendedColors.success` | #2E7A57 | #4FA37A |

> **坑**：不要用 `colorScheme.onPrimary` 当主按钮文字色。[XyTheme.kt L45-L64](../ui/src/commonMain/kotlin/cn/xybbz/ui/theme/XyTheme.kt#L45-L64) 只覆盖了 `primary / onSurface / onSurfaceVariant / surface / background / surfaceContainerLowest`，`onPrimary` 仍是 M3 默认值——深色主题下是深紫 `#381E72`，落在 #4D93CB 上对比度不合格。用 `luminance()` 判定。

---

## 4. 完整改造代码

整份文件按下面顺序组织：包声明与 import → 全局列表 → 枚举 → 动作配置 → 数据类 → 令牌 → 取色 → 外壳 → 动作区 → 按钮 → 渲染入口 → `show()` / `dismiss()`。

### 4.1 追加 import

```kotlin
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
import cn.xybbz.ui.ext.debounceClickable
import cn.xybbz.ui.ext.platformHoverClickable
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
```

移除不再使用的：`PaddingValues`、`XyColumn`、`XyRow`、`XyItemOutSpacer`、`XyScreenTitle`、`XyButton`、`RoundedCornerShape`（仍需要，保留）。

### 4.2 枚举与动作配置

```kotlin
/**
 * 弹窗语义色：决定顶部导轨、图标方块、引导语与主按钮的颜色。
 */
enum class XyDialogTone { Info, Danger, Success }

/**
 * 弹窗外壳图标，映射到 composeResources 下已有的线性图标。
 *
 * 项目没有 material-icons 依赖，因此这里只能枚举现有资源；缺图标时回落到 [Info]。
 */
enum class XyDialogIcon { Ask, Warn, Info, Success, Edit, List, Box, Gear, Download, Trash, None }

/**
 * 枚举到实际矢量资源的映射。
 *
 * @return 对应 drawable；[Ask] / [None] 回落为信息图标
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
 * 这样旧调用点只要把 ifWarning 换成 tone，就能自动拿到合适的图标，不必逐个补 icon 参数。
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
 */
data class XyDialogActionSpec(
    val text: String,
    val onClick: () -> Unit,
    val primary: Boolean = false,
    val enabled: Boolean = true,
)
```

> `drawable()` 用到的 9 个资源全部已存在于 `composeApp/src/commonMain/composeResources/drawable/`：`warning_24px`、`check_24px`、`edit_24px`、`queue_music_24px`、`folder_managed_24px`、`settings_24px`、`download_24px`、`delete_24px`、`info_24px`。需要补 `Res.drawable.*` 的 import。

### 4.3 数据类

```kotlin
/**
 * 警报对话框对象
 * @author 刘梦龙
 * @date 2025/05/07
 * @constructor 创建[AlertDialogObject]
 * @param [title] 标题
 * @param [content] 内容插槽
 * @param [modifier] 修饰符
 * @param [ifWarning] 是否是告警弹窗（已废弃，改用 [tone]）
 * @param [onCloseRequest] 关闭方法
 * @param [onDismissRequest] 取消方法
 * @param [onConfirmation] 确认方法
 * @param [properties] 属性
 * @param [dismissText] 取消文字
 * @param [confirmText] 确认文字
 * @param [dismissEnabled] 取消按钮是否可用
 * @param [confirmEnabled] 确认按钮是否可用
 * @param [tone] 语义色
 * @param [icon] 外壳图标
 * @param [kicker] 引导语，等宽小字；不传则不显示该行
 * @param [body] 正文；提供后不必再在 content 里手写 XyTextSubSmall
 * @param [actions] 自定义动作区；为空时按 dismiss / confirm 生成默认两键
 */
data class AlertDialogObject(
    // ---------- 原有字段（签名一律不变） ----------
    val title: String? = null,
    val content: (@Composable (AlertDialogObject) -> Unit)? = null,
    val modifier: Modifier = Modifier,
    @Deprecated("改用 tone = XyDialogTone.Danger", ReplaceWith("tone = XyDialogTone.Danger"))
    val ifWarning: Boolean = false,
    val onCloseRequest: (() -> Unit)? = null,
    val onDismissRequest: (() -> Unit)? = null,
    val onConfirmation: (() -> Unit)? = null,
    val properties: DialogProperties = DialogProperties(),
    val dismissText: StringResource = Res.string.cancel,
    val confirmText: StringResource = Res.string.confirm,
    val dismissEnabled: Boolean = true,
    val confirmEnabled: Boolean = true,
    // ---------- 新增字段（全部有默认值，调用点无需改动） ----------
    /** 语义色，默认信息态 */
    val tone: XyDialogTone = XyDialogTone.Info,
    /** 外壳图标；不传时按 tone 自动派生（危险→警告、成功→对勾、信息→询问），显式传 None 则不显示 */
    val icon: XyDialogIcon? = null,
    /** 引导语，等宽小字，如「危险操作 / 不可撤销」 */
    val kicker: String? = null,
    /** 正文；提供后不必再在 content 里手写 XyTextSubSmall */
    val body: String? = null,
    /** 自定义动作区；为空时按 dismiss / confirm 生成默认两键 */
    val actions: List<XyDialogActionSpec>? = null,
)
```

**关键：`content` 的签名保持 `(@Composable (AlertDialogObject) -> Unit)?` 不变**，刻意不改成 `ColumnScope` 接收者——避免 `HomeScreen` / `AlbumInfoScreen` 里 `{ it.dismiss() }` 这类调用出现 lambda 解析歧义。插槽撑满由外壳内的 `Column(fillMaxWidth)` 保证。

### 4.4 令牌与取色

```kotlin
/**
 * 弹窗专属尺寸令牌，全部为整数 dp，便于与设计稿 1:1 对照；两端共用同一套数值。
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
    /** 顶部语义导轨高度 */
    val railHeight = 2.dp
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
 * 项目的 `colorScheme.onPrimary` 未被主题覆盖，深色主题下仍是 M3 默认深紫，
 * 直接当主按钮文字会与语义色冲突，因此按亮度自行判定。
 *
 * @param background 按钮背景色
 * @return 背景偏亮时返回近黑色，否则返回白色
 */
private fun contrastOn(background: Color): Color =
    if (background.luminance() > 0.55f) Color(0xFF14161A) else Color.White
```

### 4.5 外壳

```kotlin
/**
 * 弹窗统一外壳。
 *
 * 七段结构：语义导轨 → 图标 → 引导语 → 标题 → 正文 → 自定义插槽 → 动作区。
 * 标题、正文、插槽、动作区任一段为空时自动收起，不留空白。
 *
 * @param title 标题，为空时头部整段隐藏
 * @param kicker 引导语，为空时只显示标题
 * @param body 正文
 * @param tone 语义色
 * @param icon 外壳图标
 * @param content 自定义插槽：输入 / 单选 / 进度 / 结果
 * @param footer 动作区；为空时连分隔线一起隐藏
 * @param modifier 外部修饰符
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
    val fg = tone.foreground()
    val shape = RoundedCornerShape(XyDialogTokens.corner)
    Column(
        modifier = modifier
            // 先限宽再撑满：宽屏固定 420dp，窄屏按可用宽度撑满
            .widthIn(max = XyDialogTokens.maxWidth)
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest),
    ) {
        // 顶部语义导轨：替代旧实现里整句被染红的标题
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(XyDialogTokens.railHeight)
                .background(
                    Brush.horizontalGradient(
                        listOf(fg, fg.copy(alpha = 0.12f), Color.Transparent),
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
            if (title != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (icon != XyDialogIcon.None) {
                        // 34dp 图标方块：底色 13%、描边 40%，与导轨、主按钮同色
                        Box(
                            modifier = Modifier
                                .size(XyDialogTokens.iconBox)
                                .background(
                                    fg.copy(alpha = 0.13f),
                                    RoundedCornerShape(XyDialogTokens.iconCorner),
                                )
                                .border(
                                    width = 1.dp,
                                    color = fg.copy(alpha = 0.40f),
                                    shape = RoundedCornerShape(XyDialogTokens.iconCorner),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(icon.drawable()),
                                contentDescription = null,
                                tint = fg,
                                modifier = Modifier.size(17.dp),
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Column {
                        if (kicker != null) {
                            Text(
                                text = kicker,
                                fontSize = 9.5.sp,
                                lineHeight = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.7.sp,
                                color = fg,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        Text(
                            text = title,
                            fontSize = 17.sp,
                            lineHeight = 21.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
            if (body != null) {
                Text(
                    text = body,
                    modifier = Modifier.padding(top = 15.dp),
                    fontSize = 13.sp,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (content != null) {
                // 插槽一律撑满内容宽：修掉旧实现里 XyRow 造成的居中短条与双重内边距
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 15.dp),
                ) { content() }
            }
            if (footer != null) {
                HorizontalDivider(
                    modifier = Modifier.padding(top = 16.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.09f),
                )
                Box(modifier = Modifier.padding(top = 14.dp)) { footer() }
            }
        }
    }
}
```

### 4.6 动作区与按钮

```kotlin
/**
 * 动作区：一律右对齐横排，主按钮在最右；两端共用同一排布。
 *
 * @param tone 语义色
 * @param actions 动作项，按传入顺序从左到右排列
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
        actions.forEach { spec ->
            XyDialogButton(spec = spec, tone = tone)
            if (spec != actions.last()) {
                Spacer(modifier = Modifier.width(XyDialogTokens.btnGap))
            }
        }
    }
}

/**
 * 弹窗专用按钮。
 *
 * 不复用 XyButton：它把圆角锁在 `dimens.corner`、默认填充主色，
 * 加参数会波及其它页面，因此按设计稿单独实现。
 *
 * @param spec 按钮配置
 * @param tone 语义色
 */
@Composable
private fun XyDialogButton(
    spec: XyDialogActionSpec,
    tone: XyDialogTone,
) {
    val fg = tone.foreground()
    val shape = RoundedCornerShape(XyDialogTokens.btnCorner)
    // 悬停源：桌面端给出淡底色反馈，不做缩放以免连排按钮抖动
    val hoverSource = remember { MutableInteractionSource() }
    val hovered by hoverSource.collectIsHoveredAsState()
    // 点击源：只负责防抖点击，不绘制默认 ripple
    val clickSource = remember { MutableInteractionSource() }
    val container = when {
        spec.primary -> fg
        hovered -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
        else -> Color.Transparent
    }
    // 禁用态不换色，整体降到 40% 不透明度，与设计稿「DISABLED = 40%」一致
    val contentColor = if (spec.primary) {
        contrastOn(fg)
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
            .background(container)
            .then(
                if (spec.primary) {
                    Modifier
                } else {
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
```

### 4.7 渲染入口

```kotlin
/**
 * 警报对话框组件。
 *
 * 遍历列表 → 生成动作项 → 交给 [XyDialogShell]。所有弹窗统一用居中的 Dialog 承载。
 */
@Composable
fun AlertDialogComponent() {
    alertDialogObjectList.forEach { dlg ->
        // 兼容旧字段：ifWarning 映射为危险语义
        @Suppress("DEPRECATION")
        val tone = if (dlg.ifWarning) XyDialogTone.Danger else dlg.tone
        // 文案必须先解析，不能放进非 composable 的 buildList 里
        val dismissLabel = stringResource(dlg.dismissText)
        val confirmLabel = stringResource(dlg.confirmText)
        // 自定义 actions 优先，否则由 dismiss / confirm 生成默认两键
        val actions = dlg.actions ?: buildList {
            dlg.onDismissRequest?.let { onDismiss ->
                add(
                    XyDialogActionSpec(
                        text = dismissLabel,
                        onClick = { onDismiss(); dlg.dismiss() },
                        enabled = dlg.dismissEnabled,
                    ),
                )
            }
            dlg.onConfirmation?.let { onConfirm ->
                add(
                    XyDialogActionSpec(
                        text = confirmLabel,
                        onClick = { onConfirm(); dlg.dismiss() },
                        primary = true,
                        enabled = dlg.confirmEnabled,
                    ),
                )
            }
        }
        val contentBlock: (@Composable () -> Unit)? = dlg.content?.let { block ->
            { block(dlg) }
        }
        val footerBlock: (@Composable () -> Unit)? = if (actions.isEmpty()) {
            null
        } else {
            { XyDialogFooter(tone = tone, actions = actions) }
        }
        Dialog(
            onDismissRequest = {
                dlg.onCloseRequest?.invoke()
                dlg.dismiss()
            },
            properties = dlg.properties,
        ) {
            XyDialogShell(
                title = dlg.title,
                kicker = dlg.kicker,
                body = dlg.body,
                tone = tone,
                // 未显式指定图标时按语义派生，旧调用点无需补参
                icon = dlg.icon ?: tone.defaultIcon(),
                content = contentBlock,
                footer = footerBlock,
                modifier = dlg.modifier,
            )
        }
    }
}
```

要点：

- 顺序必须是 **先执行回调、再 `dismiss()`**；`DownloadScreen`、`MusicBottomMenuComponent` 这类"点按钮同时做事并关闭"的调用点依赖这个顺序。
- `actions` 为空时 `footer = null`，外壳不画分隔线（对应旧实现里只有 `content`、没有任何按钮的弹窗）。
- `properties` 原样透传，不额外改遮罩，保证关闭行为（遮罩点击 / 返回键）与现在完全一致。
- `show()` / `dismiss()` 两个扩展函数与 `mainMoeScope` / `alertDialogObjectList` 不做改动。

---

## 5. 调用点处理

### 阶段 1：零改动

改完第 4 节，`jvmMain` 7 处 + `commonMain` 16 处共 23 个调用点**一行都不用改**，直接获得新外观：`ifWarning = true` 自动映射成 Danger，`content` 里的文本会自动落到插槽（上间距 15dp、正文色），输入框自动撑满。

### 阶段 2（可选）：补语义与图标

不补也能用，只是图标全是 `Ask`、没有 kicker。建议按下面优先级补：

| 文件:行 | 形态 | 建议参数 |
|---|---|---|
| `JvmStorageSettingsSection.kt:122` | 清空数据库 | `tone = Danger`、`icon = Trash`、`kicker = "危险操作 / 不可撤销"`、文案改 `body` |
| `JvmDownloadScreen.kt:198` | 清空下载任务 | 同上 |
| `JvmConnectionManagement.kt:960` | 删除连接 | 同上（改 `JvmConnectionDeleteDialog` 函数体） |
| `JvmAlbumInfoScreen.kt:381` | 删除歌单 | 同上 |
| `JvmAlbumInfoScreen.kt:347` | 重命名歌单 | `icon = Edit`、`kicker = "输入 / 重命名"`、`XyEdit` 加 `fillMaxWidth()` |
| `JvmAlbumInfoScreen.kt:301` | 导入歌单提示 | `icon = Download` |
| `MainScreenScaffold.jvm.kt:207` | 新建歌单 | `icon = Edit`、`kicker = "输入 / 创建"` |
| `ImportExportPlaylistsCompose.kt:244` | 导出格式选择 | `icon = List` |
| `SelectControl.kt:104` | 永久删除所选资源 | `tone = Danger`、`icon = Trash` |
| `SettingItemComponent.kt:89 / 127` | 通用设置项弹窗 | `tone = Info`、`icon = Ask` |
| 其余 Android 侧 | —— | 与上表同模板；想控风险可只改 JVM + 共享层 |

改完全部调用点后，`ifWarning` 传参消失，可进入阶段 3 把字段和兼容分支一起删掉。

---

## 6. 验收

### 视觉（对照 `dialog-redesign.html` 第 02 节）

1. 顶部 2dp 导轨存在且为语义色向右渐隐。
2. 危险弹窗的图标底、kicker、主按钮三处同色；**标题是正文色，不是红色**。
3. 正文 13sp、行高 22sp，与标题间距 15dp。
4. 动作区上方 1dp 分隔线，上面留 16dp。
5. 按钮 36dp 横排右对齐；宽屏卡片不超过 420dp、窄屏撑满，不切纵向堆叠。
6. 输入框、选择列表撑满内容宽，两侧只有 22dp 内边距（不再有双重内边距）。
7. 卡片圆角 14dp，不是旧的 12dp。
8. 没有按钮的弹窗（`onDismissRequest` 与 `onConfirmation` 都为 null）不出现分隔线和空白动作区。

### 功能回归

- 确认 / 取消均能关闭，回调只触发一次（注意 `debounceClickable` 的 500ms 防抖）。
- `confirmEnabled = false` 时主按钮整体降到 40% 不透明度且不可点。
- 点击遮罩 / 返回键 / 关闭按钮的行为与改造前一致。
- 新建 / 重命名歌单：输入框能正常输入，确认后歌单名正确写入。
- 导出格式选择：选择后仍能继续走到 FileKit 保存流程。
- 清空数据库 / 清空下载 / 删除连接 / 删除歌单：确认后动作真实执行，取消后无副作用。

### 编译（由你手动执行，AI 不主动跑）

```powershell
.\gradlew :composeApp:compileKotlinJvm
.\gradlew :composeApp:compileDebugKotlinAndroid
```

---

## 7. 风险

| 风险 | 说明 | 处理 |
|---|---|---|
| 移动端同步变化 | 渲染入口共享，无平台分支 | 接受；窄屏由 `Dialog` 默认宽度收窄，不会撑破 |
| `scrimColor` 兼容性 | `DialogProperties.scrimColor` 在 commonMain 是否可用未确认 | 本轮不设，原样透传 `properties`；想统一遮罩再单独验证 |
| `XyEdit` 高度偏高 | 默认内距 `innerVerticalPadding` 12dp | 需要时给调用点的 `XyEdit` 传 `paddingValues`，不要改 `XyEdit` 默认值 |
| 长列表插槽溢出 | 选择型内容过高会撑破窗口 | 插槽外层加 `heightIn(max = 320.dp)` 并允许内部滚动（等出现真实场景再加） |
| 图标语义偏差 | 没有专用图标，`List` 借用了 `queue_music_24px` | 可接受；后续补图标只需改 `drawable()` 一行 |
| 旧调用点文案重复 | 部分调用点仍在 `content` 里手写 `XyTextSubSmall` | 视觉上与 `body` 一致，不必强求阶段 1 迁移 |
