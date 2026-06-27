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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.localdata.enums.CacheUpperLimitEnum
import cn.xybbz.music.CacheUpperLimitOption
import cn.xybbz.music.cacheUpperLimitOptions
import cn.xybbz.router.MemoryManagement
import cn.xybbz.ui.components.JvmSettingActionEntry
import cn.xybbz.ui.components.JvmSettingActionGrid as JvmSettingActionEntryGrid
import cn.xybbz.ui.components.JvmSettingBaseRow
import cn.xybbz.ui.components.JvmSettingNavigationRow
import cn.xybbz.ui.components.JvmSettingPageHeader
import cn.xybbz.ui.components.JvmSettingPageScaffold
import cn.xybbz.ui.components.JvmSettingSection
import cn.xybbz.ui.components.JvmSettingStatusCard
import cn.xybbz.ui.components.JvmSettingStatusCardItem
import cn.xybbz.ui.components.JvmSettingSummaryCardWidth
import cn.xybbz.ui.components.JvmSettingTwoPaneContent
import cn.xybbz.ui.components.displayMessage
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.viewmodel.CacheLimitViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic.composeapp.generated.resources.*
import xymusic.composeapp.generated.resources.Res
import xymusic.composeapp.generated.resources.av_timer_24px
import xymusic.composeapp.generated.resources.folder_managed_24px
import xymusic.composeapp.generated.resources.info_24px
import xymusic.composeapp.generated.resources.music_cache_limit_title
import xymusic.composeapp.generated.resources.music_note_24px
import xymusic.composeapp.generated.resources.speed_24px

/**
 * JVM 桌面端音乐缓存上限设置页面。
 */
@Composable
fun JvmCacheLimitScreen(
    cacheLimitViewModel: CacheLimitViewModel = koinViewModel<CacheLimitViewModel>()
) {
    // 桌面端设置页使用同一套路由对象，右侧概览卡可跳转到存储管理页。
    val navigator = LocalNavigator.current

    // 点击档位后需要调用 suspend 写入方法，复用当前组合范围内的协程作用域。
    val coroutineScope = rememberCoroutineScope()

    // 读取当前平台可展示的缓存上限选项，JVM 端会过滤掉不适合桌面的历史档位。
    val cacheLimitOptions = cacheUpperLimitOptions()

    // 从 ViewModel 读取已保存的上限枚举，作为页面所有选中态和摘要信息的单一来源。
    val selectedLimit = cacheLimitViewModel.cacheUpperLimit

    // 在可展示选项中匹配当前保存值；历史值可能不存在，因此后面需要兜底迁移。
    val selectedOption = cacheLimitOptions.firstOrNull { it.limit == selectedLimit }

    // 优先使用选项里的展示文案，兜底时用枚举名，避免摘要区域出现空文本。
    val selectedLimitLabel = selectedOption?.displayMessage() ?: selectedLimit.name

    // 将枚举映射为桌面卡片需要的策略、描述和脚注文案。
    val selectedLimitProfile = selectedLimit.cacheLimitProfile()

    // 如果当前值是历史不可展示档位，先提示已自动迁移，实际写入在 LaunchedEffect 中执行。
    val displayedLimitLabel = if (selectedOption == null) {
        stringResource(Res.string.jvm_cache_limit_screen_text_01, CacheLimitFallbackOption.message)
    } else {
        selectedLimitLabel
    }

    // 已使用缓存容量由 ViewModel 统一计算，页面只负责格式化和展示。
    val musicCacheSizeLabel = cacheLimitViewModel.musicCacheSizeLabel

    // 首次进入页面时修正历史不可展示配置，并刷新真实音乐缓存占用。
    LaunchedEffect(Unit) {
        if (cacheLimitOptions.none { it.limit == cacheLimitViewModel.cacheUpperLimit }) {
            cacheLimitViewModel.setCacheUpperLimitData(CacheLimitFallbackOption.limit)
        }
        cacheLimitViewModel.refreshMusicCacheSize()
    }

    JvmSettingPageScaffold(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = XyTheme.dimens.outerHorizontalPadding * 2,
            vertical = XyTheme.dimens.outerVerticalPadding * 3,
        )
    ) {
        JvmSettingPageHeader(
            title = stringResource(Res.string.music_cache_limit_title),
            description = stringResource(Res.string.jvm_cache_limit_screen_text_02),
        ) {
            JvmSettingStatusCard(
                width = JvmSettingSummaryCardWidth,
                prominentValue = true,
                items = listOf(
                    JvmSettingStatusCardItem(label = stringResource(Res.string.cache_limit), value = displayedLimitLabel),
                    JvmSettingStatusCardItem(label = stringResource(Res.string.jvm_cache_limit_screen_text_03), value = selectedLimitProfile.strategy),
                    JvmSettingStatusCardItem(label = stringResource(Res.string.jvm_cache_limit_screen_text_04), value = musicCacheSizeLabel.addCapacitySpace()),
                )
            )
        }

        JvmSettingTwoPaneContent(
            leftContent = {
                JvmSettingSection(
                    title = stringResource(Res.string.jvm_cache_limit_screen_text_05),
                    subtitle = stringResource(Res.string.jvm_cache_limit_screen_text_06),
                    badge = stringResource(Res.string.jvm_cache_limit_screen_text_07, displayedLimitLabel),
                    contentContainerEnabled = false,
                    qualityNote = stringResource(Res.string.jvm_cache_limit_screen_text_08),
                ) {
                    JvmSettingActionEntryGrid(
                        // 将缓存上限选项转换成桌面设置页通用的操作卡片模型。
                        actionEntries = jvmCacheLimitActionEntries(
                            options = cacheLimitOptions,
                            selectedLimit = selectedLimit,
                            onSelected = { option ->
                                // 重复点击当前档位不触发写入，减少无意义的设置更新。
                                if (selectedLimit != option.limit) {
                                    coroutineScope.launch {
                                        cacheLimitViewModel.setCacheUpperLimitData(option.limit)
                                    }
                                }
                            },
                        ),
                        fillTwoColumnWidth = true,
                    )
                }
            },
            rightContent = {
                JvmCacheUsageOverviewSection(
                    selectedLimitLabel = displayedLimitLabel,
                    musicCacheSizeLabel = musicCacheSizeLabel,
                    onStorageClick = {
                        // 存储管理页负责更细的缓存路径、分类占用和批量清理操作。
                        navigator.navigate(MemoryManagement)
                    },
                )
            }
        )
    }
}

/**
 * 将业务层缓存上限选项转换为 JVM 设置页网格可以直接渲染的卡片条目。
 *
 * @param options 当前平台允许展示的缓存上限选项列表。
 * @param selectedLimit 当前已保存的缓存上限枚举，用于标记选中态。
 * @param onSelected 用户选择某个档位时触发的回调，由外层负责写入设置。
 */
@Composable
private fun jvmCacheLimitActionEntries(
    options: List<CacheUpperLimitOption>,
    selectedLimit: CacheUpperLimitEnum,
    onSelected: (CacheUpperLimitOption) -> Unit,
): List<JvmSettingActionEntry> {
    return options.map { option ->
        // 每个枚举档位都有一套桌面展示画像，用于补充容量之外的策略说明。
        val profile = option.limit.cacheLimitProfile()
        JvmSettingActionEntry(
            icon = option.limit.cacheLimitIcon(),
            kicker = profile.kicker,
            title = option.displayMessage().addCapacitySpace(),
            description = profile.description,
            color = option.limit.cacheLimitColor(),
            selected = selectedLimit == option.limit,
            status = "${profile.footLabel} · ${profile.footValue}",
            role = Role.RadioButton,
            onClick = {
                // 只向外暴露被选中的原始选项，避免组件层承担设置写入逻辑。
                onSelected(option)
            },
        )
    }
}

/**
 * 根据缓存上限档位返回对应的主题色，帮助用户快速区分轻量、常规和大容量策略。
 */
@Composable
private fun CacheUpperLimitEnum.cacheLimitColor(): Color {
    return when (this) {
        // 自动策略使用主色，强调这是系统推荐/默认方向。
        CacheUpperLimitEnum.Auto -> MaterialTheme.colorScheme.primary

        // 不缓存是风险提示类选择，使用错误色提醒用户会失去缓存收益。
        CacheUpperLimitEnum.No -> MaterialTheme.colorScheme.error

        // 小容量档位使用次要色，表示更保守的磁盘占用策略。
        CacheUpperLimitEnum.OneHundred,
        CacheUpperLimitEnum.FiveHundred,
        CacheUpperLimitEnum.EightHundred,
        CacheUpperLimitEnum.OneG,
        CacheUpperLimitEnum.ThreeG,
        CacheUpperLimitEnum.FourG -> MaterialTheme.colorScheme.secondary

        // 中等容量档位使用主色，和推荐的日常使用场景保持一致。
        CacheUpperLimitEnum.EightG,
        CacheUpperLimitEnum.SixteenG -> MaterialTheme.colorScheme.primary

        // 超大容量档位使用第三主题色，和普通日常档位拉开视觉层级。
        CacheUpperLimitEnum.ThirtyTwoG,
        CacheUpperLimitEnum.SixtyFourG,
        CacheUpperLimitEnum.OneHundredTwentyEightG -> MaterialTheme.colorScheme.tertiary
    }
}

/**
 * 根据缓存上限档位返回卡片图标资源，让不同容量策略在网格中更容易扫读。
 */
private fun CacheUpperLimitEnum.cacheLimitIcon(): DrawableResource {
    return when (this) {
        // 自动档位强调速度/策略自动选择。
        CacheUpperLimitEnum.Auto -> Res.drawable.speed_24px

        // 不缓存档位使用信息图标，提示这是特殊行为。
        CacheUpperLimitEnum.No -> Res.drawable.info_24px

        // 轻量容量档位用文件夹图标，表达受控的缓存空间。
        CacheUpperLimitEnum.OneHundred,
        CacheUpperLimitEnum.FiveHundred,
        CacheUpperLimitEnum.EightHundred,
        CacheUpperLimitEnum.OneG,
        CacheUpperLimitEnum.ThreeG,
        CacheUpperLimitEnum.FourG -> Res.drawable.folder_managed_24px

        // 中等容量更偏音乐播放场景，使用音符图标。
        CacheUpperLimitEnum.EightG,
        CacheUpperLimitEnum.SixteenG -> Res.drawable.music_note_24px

        // 大容量档位偏长期保留，使用时间图标表达更长缓存周期。
        CacheUpperLimitEnum.ThirtyTwoG,
        CacheUpperLimitEnum.SixtyFourG,
        CacheUpperLimitEnum.OneHundredTwentyEightG -> Res.drawable.av_timer_24px
    }
}

/**
 * 右侧缓存占用概览区域，集中展示当前真实占用和当前缓存上限。
 *
 * @param selectedLimitLabel 当前缓存上限的展示文案。
 * @param musicCacheSizeLabel 当前音乐缓存真实占用的展示文案。
 * @param onStorageClick 点击真实占用行时跳转到存储管理页。
 */
@Composable
private fun JvmCacheUsageOverviewSection(
    selectedLimitLabel: String,
    musicCacheSizeLabel: String,
    onStorageClick: () -> Unit,
) {
    JvmSettingSection(
        title = stringResource(Res.string.jvm_cache_limit_screen_text_09),
        subtitle = stringResource(Res.string.jvm_cache_limit_screen_text_10),
        badge = stringResource(Res.string.jvm_cache_limit_screen_text_11),
        contentContainerEnabled = false,
        qualityNote = stringResource(Res.string.jvm_cache_limit_screen_text_12),
    ) {
        Surface(
            // 外层 Surface 承载圆形仪表和两行信息列表，形成独立的概览块。
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(XyTheme.dimens.corner),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            )
        ) {
            Column(
                modifier = Modifier.padding(XyTheme.dimens.outerHorizontalPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.contentPadding)
            ) {
                JvmCacheGauge(
                    value = musicCacheSizeLabel.addCapacitySpace(),
                    label = stringResource(Res.string.jvm_cache_limit_screen_text_13),
                )
                JvmCacheInfoList(
                    // 第一行可跳转到存储管理页，第二行只展示当前上限。
                    rows = listOf(
                        JvmCacheInfoRow(
                            icon = Res.drawable.music_note_24px,
                            title = stringResource(Res.string.jvm_cache_limit_screen_text_11),
                            description = stringResource(Res.string.jvm_cache_limit_screen_text_14),
                            value = musicCacheSizeLabel.addCapacitySpace(),
                            onClick = onStorageClick,
                        ),
                        JvmCacheInfoRow(
                            icon = Res.drawable.folder_managed_24px,
                            title = stringResource(Res.string.jvm_cache_limit_screen_text_15),
                            description = stringResource(Res.string.jvm_cache_limit_screen_text_16),
                            value = selectedLimitLabel.addCapacitySpace(),
                        ),
                    )
                )
            }
        }
    }
}

/**
 * 圆形缓存占用仪表，只展示当前容量文本，不承担进度计算。
 *
 * @param value 仪表中心展示的容量值。
 * @param label 仪表下方说明文案。
 */
@Composable
private fun JvmCacheGauge(
    value: String,
    label: String,
) {
    Box(
        // 使用半透明填充和粗边框模拟仪表盘，让容量数字成为视觉中心。
        modifier = Modifier
            .size(188.dp)
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                shape = CircleShape,
            )
            .border(
                BorderStroke(12.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)),
                CircleShape,
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            // 数值和标签垂直居中，文本均限制为单行以避免撑破圆形区域。
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding / 2)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * 缓存概览的信息列表容器，负责统一背景、边框和行间分隔线。
 *
 * @param rows 需要展示的缓存信息行。
 */
@Composable
private fun JvmCacheInfoList(rows: List<JvmCacheInfoRow>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(XyTheme.dimens.corner))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)),
                RoundedCornerShape(XyTheme.dimens.corner)
            )
    ) {
        rows.forEachIndexed { index, row ->
            JvmCacheInfoListRow(row = row)
            if (index != rows.lastIndex) {
                // 非最后一行后添加细分隔线，保持列表结构清晰。
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f))
                )
            }
        }
    }
}

/**
 * 渲染单行缓存信息；有点击行为时使用导航行，没有点击行为时使用基础展示行。
 *
 * @param row 当前行的展示数据和可选点击回调。
 */
@Composable
private fun JvmCacheInfoListRow(row: JvmCacheInfoRow) {
    // 将可空点击回调先取出，便于 Kotlin 智能类型判断两个 UI 分支。
    val onClick = row.onClick
    if (onClick == null) {
        // 纯展示行只在右侧放置胶囊标签，不提供导航交互。
        JvmSettingBaseRow(
            icon = row.icon,
            title = row.title,
            description = row.description,
            minHeight = 68.dp,
            horizontalPadding = XyTheme.dimens.contentPadding,
            verticalPadding = XyTheme.dimens.outerVerticalPadding,
            iconSelected = true,
            trailing = {
                JvmCachePill(text = row.value)
            }
        )
    } else {
        // 可点击行复用桌面设置页的导航样式，保持与其它设置入口一致。
        JvmSettingNavigationRow(
            icon = row.icon,
            title = row.title,
            description = row.description,
            value = row.value,
            onClick = onClick,
        )
    }
}

/**
 * 缓存信息行右侧的胶囊标签，用于展示容量或状态短文本。
 *
 * @param text 胶囊内展示的文本。
 * @param selected 是否使用选中态配色。
 */
@Composable
private fun JvmCachePill(
    text: String,
    selected: Boolean = false,
) {
    // 选中态使用主色弱背景，普通态使用 onSurface 弱背景。
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    }
    // 文本颜色跟随容器状态变化，保证普通态和选中态都具备足够辨识度。
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    // 胶囊边框进一步区分状态，同时避免在浅色背景上丢失轮廓。
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
    }

    Surface(
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor,
        border = BorderStroke(width = 1.dp, color = borderColor)
    ) {
        Text(
            modifier = Modifier
                .widthIn(max = 132.dp)
                .padding(
                    horizontal = XyTheme.dimens.contentPadding,
                    vertical = XyTheme.dimens.outerVerticalPadding / 2,
                ),
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * 缓存上限档位的桌面展示画像。
 *
 * @property kicker 卡片左上角的短标签，例如 LOW、8GB、MAX。
 * @property strategy 摘要卡展示的容量策略名称。
 * @property description 卡片主体描述，说明该档位适合的使用场景。
 * @property footLabel 卡片底部状态文案左侧标签。
 * @property footValue 卡片底部状态文案右侧值。
 */
private data class JvmCacheLimitProfile(
    val kicker: String,
    val strategy: String,
    val description: String,
    val footLabel: String,
    val footValue: String,
)

/**
 * 缓存占用概览列表的单行数据模型。
 *
 * @property icon 行首图标资源。
 * @property title 行标题。
 * @property description 行描述文案。
 * @property value 行尾展示值。
 * @property onClick 可选点击回调，为空时渲染为纯展示行。
 */
private data class JvmCacheInfoRow(
    val icon: DrawableResource,
    val title: String,
    val description: String,
    val value: String,
    val onClick: (() -> Unit)? = null,
)

/**
 * 历史自动/不缓存等 JVM 页面不再展示的档位进入页面后的兜底目标。
 */
private val CacheLimitFallbackOption = CacheUpperLimitOption(
    limit = CacheUpperLimitEnum.ThreeG,
    message = "2GB",
)

/**
 * 将缓存上限枚举映射为 JVM 桌面端页面的展示画像。
 */
@Composable
private fun CacheUpperLimitEnum.cacheLimitProfile(): JvmCacheLimitProfile {
    return when (this) {
        // 自动档位主要用于兼容共享枚举，JVM 页面会迁移到固定容量档位。
        CacheUpperLimitEnum.Auto -> JvmCacheLimitProfile(
            kicker = "AUTO",
            strategy = stringResource(Res.string.jvm_cache_limit_screen_text_17),
            description = stringResource(Res.string.jvm_cache_limit_screen_text_18),
            footLabel = stringResource(Res.string.recommend),
            footValue = stringResource(Res.string.jvm_cache_limit_screen_text_19),
        )
        // 不缓存档位同样用于兼容历史设置，说明关闭缓存后的最低占用行为。
        CacheUpperLimitEnum.No -> JvmCacheLimitProfile(
            kicker = "OFF",
            strategy = stringResource(Res.string.jvm_cache_limit_screen_text_20),
            description = stringResource(Res.string.jvm_cache_limit_screen_text_21),
            footLabel = stringResource(Res.string.jvm_cache_limit_screen_text_22),
            footValue = stringResource(Res.string.jvm_cache_limit_screen_text_23),
        )
        // 小容量档位统一归为轻量策略，适合磁盘空间更紧张的桌面环境。
        CacheUpperLimitEnum.OneHundred,
        CacheUpperLimitEnum.FiveHundred,
        CacheUpperLimitEnum.EightHundred,
        CacheUpperLimitEnum.OneG,
        CacheUpperLimitEnum.ThreeG -> JvmCacheLimitProfile(
            kicker = "LOW",
            strategy = stringResource(Res.string.jvm_cache_limit_screen_text_24),
            description = stringResource(Res.string.jvm_cache_limit_screen_text_25),
            footLabel = stringResource(Res.string.jvm_cache_limit_screen_text_26),
            footValue = stringResource(Res.string.jvm_cache_limit_screen_text_27),
        )
        // 4GB 是日常轻度使用档位，偏短期缓存和基础复听。
        CacheUpperLimitEnum.FourG -> JvmCacheLimitProfile(
            kicker = "4GB",
            strategy = stringResource(Res.string.jvm_cache_limit_screen_text_28),
            description = stringResource(Res.string.jvm_cache_limit_screen_text_29),
            footLabel = stringResource(Res.string.jvm_cache_limit_screen_text_30),
            footValue = stringResource(Res.string.jvm_cache_limit_screen_text_31),
        )
        // 8GB 是桌面端默认推荐档位，兼顾缓存命中和磁盘占用。
        CacheUpperLimitEnum.EightG -> JvmCacheLimitProfile(
            kicker = "8GB",
            strategy = stringResource(Res.string.jvm_cache_limit_screen_text_32),
            description = stringResource(Res.string.jvm_cache_limit_screen_text_33),
            footLabel = stringResource(Res.string.jvm_cache_limit_screen_text_34),
            footValue = stringResource(Res.string.jvm_cache_limit_screen_text_32),
        )
        // 16GB 适合无损音源或大曲库的高频试听场景。
        CacheUpperLimitEnum.SixteenG -> JvmCacheLimitProfile(
            kicker = "16GB",
            strategy = stringResource(Res.string.jvm_cache_limit_screen_text_35),
            description = stringResource(Res.string.jvm_cache_limit_screen_text_36),
            footLabel = stringResource(Res.string.jvm_cache_limit_screen_text_37),
            footValue = stringResource(Res.string.jvm_cache_limit_screen_text_38),
        )
        // 更大的容量档位归为高级策略，通常需要用户明确预留独立缓存空间。
        CacheUpperLimitEnum.ThirtyTwoG,
        CacheUpperLimitEnum.SixtyFourG,
        CacheUpperLimitEnum.OneHundredTwentyEightG -> JvmCacheLimitProfile(
            kicker = "MAX",
            strategy = stringResource(Res.string.jvm_cache_limit_screen_text_39),
            description = stringResource(Res.string.jvm_cache_limit_screen_text_40),
            footLabel = stringResource(Res.string.jvm_cache_limit_screen_text_41),
            footValue = stringResource(Res.string.jvm_cache_limit_screen_text_42),
        )
    }
}

/**
 * 给容量单位前补空格，把 "2GB" 统一展示为 "2 GB"，提升桌面端扫读性。
 */
private fun String.addCapacitySpace(): String {
    // 只在数字后紧跟容量单位时插入空格，避免影响其它普通文案。
    return trim().replace(
        Regex("""(?<=\d)(?=(PB|TB|GB|MB|KB|B)(\s|$))"""),
        " ",
    )
}
