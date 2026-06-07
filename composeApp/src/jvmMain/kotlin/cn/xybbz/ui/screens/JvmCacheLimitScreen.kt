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
import cn.xybbz.ui.components.JvmSettingPageContentMaxWidth
import cn.xybbz.ui.components.JvmSettingPageHeader
import cn.xybbz.ui.components.JvmSettingPageScaffold
import cn.xybbz.ui.components.JvmSettingSection
import cn.xybbz.ui.components.JvmSettingStatusCard
import cn.xybbz.ui.components.JvmSettingStatusCardItem
import cn.xybbz.ui.components.JvmSettingTwoPaneContent
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.viewmodel.CacheLimitViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.av_timer_24px
import xymusic_kmp.composeapp.generated.resources.folder_managed_24px
import xymusic_kmp.composeapp.generated.resources.info_24px
import xymusic_kmp.composeapp.generated.resources.music_cache_limit_title
import xymusic_kmp.composeapp.generated.resources.music_note_24px
import xymusic_kmp.composeapp.generated.resources.speed_24px

private val JvmCacheSummaryWidth = 278.dp
private val JvmCacheLimitActionCardHeight = 168.dp

/**
 * JVM 桌面端音乐缓存上限设置页面。
 */
@Composable
fun JvmCacheLimitScreen(
    cacheLimitViewModel: CacheLimitViewModel = koinViewModel<CacheLimitViewModel>()
) {
    val navigator = LocalNavigator.current
    val coroutineScope = rememberCoroutineScope()
    val cacheLimitOptions = cacheUpperLimitOptions()
    val selectedLimit = cacheLimitViewModel.cacheUpperLimit
    val selectedOption = cacheLimitOptions.firstOrNull { it.limit == selectedLimit }
    val selectedLimitLabel = selectedOption?.message ?: selectedLimit.name
    val selectedLimitProfile = selectedLimit.cacheLimitProfile()
    val displayedLimitLabel = if (selectedOption == null) {
        "${CacheLimitFallbackOption.message} · 已自动迁移"
    } else {
        selectedLimitLabel
    }
    val musicCacheSizeLabel = cacheLimitViewModel.musicCacheSizeLabel

    LaunchedEffect(Unit) {
        if (cacheLimitOptions.none { it.limit == cacheLimitViewModel.cacheUpperLimit }) {
            cacheLimitViewModel.setCacheUpperLimitData(CacheLimitFallbackOption.limit)
        }
        cacheLimitViewModel.refreshMusicCacheSize()
    }

    JvmSettingPageScaffold(
        modifier = Modifier.fillMaxSize(),
        contentMaxWidth = JvmSettingPageContentMaxWidth,
        contentPadding = PaddingValues(
            horizontal = XyTheme.dimens.outerHorizontalPadding * 2,
            vertical = XyTheme.dimens.outerVerticalPadding * 3,
        )
    ) {
        JvmSettingPageHeader(
            title = stringResource(Res.string.music_cache_limit_title),
            description = "把边下边播产生的播放缓存上限做成可扫读的容量面板。固定档位适合给桌面磁盘预留明确空间预算。",
            contentMaxWidth = JvmSettingPageContentMaxWidth,
        ) {
            JvmSettingStatusCard(
                width = JvmCacheSummaryWidth,
                prominentValue = true,
                items = listOf(
                    JvmSettingStatusCardItem(label = "缓存上限", value = displayedLimitLabel),
                    JvmSettingStatusCardItem(label = "容量策略", value = selectedLimitProfile.strategy),
                    JvmSettingStatusCardItem(label = "已使用", value = musicCacheSizeLabel.addCapacitySpace()),
                )
            )
        }

        JvmSettingTwoPaneContent(
            leftContent = {
                JvmSettingSection(
                    title = "上限档位",
                    subtitle = "卡片直接呈现容量语义，减少在列表和说明之间来回确认。",
                    badge = "当前：$displayedLimitLabel",
                    contentContainerEnabled = false,
                    qualityNote = "JVM 端当前提供固定容量档位；历史自动或不缓存配置进入页面后会迁移到默认 2GB 档位。",
                ) {
                    JvmSettingActionEntryGrid(
                        actionEntries = jvmCacheLimitActionEntries(
                            options = cacheLimitOptions,
                            selectedLimit = selectedLimit,
                            onSelected = { option ->
                                if (selectedLimit != option.limit) {
                                    coroutineScope.launch {
                                        cacheLimitViewModel.setCacheUpperLimitData(option.limit)
                                    }
                                }
                            },
                        ),
                        fillTwoColumnWidth = true,
                        cardHeight = JvmCacheLimitActionCardHeight,
                    )
                }
            },
            rightContent = {
                JvmCacheUsageOverviewSection(
                    selectedLimitLabel = displayedLimitLabel,
                    musicCacheSizeLabel = musicCacheSizeLabel,
                    onStorageClick = {
                        navigator.navigate(MemoryManagement)
                    },
                )
            }
        )
    }
}

@Composable
private fun jvmCacheLimitActionEntries(
    options: List<CacheUpperLimitOption>,
    selectedLimit: CacheUpperLimitEnum,
    onSelected: (CacheUpperLimitOption) -> Unit,
): List<JvmSettingActionEntry> {
    return options.map { option ->
        val profile = option.limit.cacheLimitProfile()
        JvmSettingActionEntry(
            icon = option.limit.cacheLimitIcon(),
            kicker = profile.kicker,
            title = option.message.addCapacitySpace(),
            description = profile.description,
            color = option.limit.cacheLimitColor(),
            selected = selectedLimit == option.limit,
            status = "${profile.footLabel} · ${profile.footValue}",
            role = Role.RadioButton,
            onClick = {
                onSelected(option)
            },
        )
    }
}

@Composable
private fun CacheUpperLimitEnum.cacheLimitColor(): Color {
    return when (this) {
        CacheUpperLimitEnum.Auto -> MaterialTheme.colorScheme.primary
        CacheUpperLimitEnum.No -> MaterialTheme.colorScheme.error
        CacheUpperLimitEnum.OneHundred,
        CacheUpperLimitEnum.FiveHundred,
        CacheUpperLimitEnum.EightHundred,
        CacheUpperLimitEnum.OneG,
        CacheUpperLimitEnum.ThreeG,
        CacheUpperLimitEnum.FourG -> MaterialTheme.colorScheme.secondary
        CacheUpperLimitEnum.EightG,
        CacheUpperLimitEnum.SixteenG -> MaterialTheme.colorScheme.primary
        CacheUpperLimitEnum.ThirtyTwoG,
        CacheUpperLimitEnum.SixtyFourG,
        CacheUpperLimitEnum.OneHundredTwentyEightG -> MaterialTheme.colorScheme.tertiary
    }
}

private fun CacheUpperLimitEnum.cacheLimitIcon(): DrawableResource {
    return when (this) {
        CacheUpperLimitEnum.Auto -> Res.drawable.speed_24px
        CacheUpperLimitEnum.No -> Res.drawable.info_24px
        CacheUpperLimitEnum.OneHundred,
        CacheUpperLimitEnum.FiveHundred,
        CacheUpperLimitEnum.EightHundred,
        CacheUpperLimitEnum.OneG,
        CacheUpperLimitEnum.ThreeG,
        CacheUpperLimitEnum.FourG -> Res.drawable.folder_managed_24px
        CacheUpperLimitEnum.EightG,
        CacheUpperLimitEnum.SixteenG -> Res.drawable.music_note_24px
        CacheUpperLimitEnum.ThirtyTwoG,
        CacheUpperLimitEnum.SixtyFourG,
        CacheUpperLimitEnum.OneHundredTwentyEightG -> Res.drawable.av_timer_24px
    }
}

@Composable
private fun JvmCacheUsageOverviewSection(
    selectedLimitLabel: String,
    musicCacheSizeLabel: String,
    onStorageClick: () -> Unit,
) {
    JvmSettingSection(
        title = "占用概览",
        subtitle = "真实占用与存储管理页的音频缓存使用同一套计算方式。",
        badge = "真实占用",
        contentContainerEnabled = false,
        qualityNote = "缓存命中和当前播放条目会优先保留，超过上限时后台清理更早访问的缓存文件。缓存路径、真实占用和批量清理继续归入存储管理页。",
    ) {
        Surface(
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
                    label = "音频缓存占用",
                )
                JvmCacheInfoList(
                    rows = listOf(
                        JvmCacheInfoRow(
                            icon = Res.drawable.music_note_24px,
                            title = "真实占用",
                            description = "点击查看歌曲缓存路径和其它存储分类。",
                            value = musicCacheSizeLabel.addCapacitySpace(),
                            onClick = onStorageClick,
                        ),
                        JvmCacheInfoRow(
                            icon = Res.drawable.folder_managed_24px,
                            title = "当前上限",
                            description = "达到容量上限后自动触发 LRU 清理。",
                            value = selectedLimitLabel.addCapacitySpace(),
                        ),
                    )
                )
            }
        }
    }
}

@Composable
private fun JvmCacheGauge(
    value: String,
    label: String,
) {
    Box(
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

@Composable
private fun JvmCacheInfoListRow(row: JvmCacheInfoRow) {
    val onClick = row.onClick
    if (onClick == null) {
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
        JvmSettingNavigationRow(
            icon = row.icon,
            title = row.title,
            description = row.description,
            value = row.value,
            onClick = onClick,
        )
    }
}

@Composable
private fun JvmCachePill(
    text: String,
    selected: Boolean = false,
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
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

private data class JvmCacheLimitProfile(
    val kicker: String,
    val strategy: String,
    val description: String,
    val footLabel: String,
    val footValue: String,
)

private data class JvmCacheInfoRow(
    val icon: DrawableResource,
    val title: String,
    val description: String,
    val value: String,
    val onClick: (() -> Unit)? = null,
)

private val CacheLimitFallbackOption = CacheUpperLimitOption(
    limit = CacheUpperLimitEnum.ThreeG,
    message = "2GB",
)

private fun CacheUpperLimitEnum.cacheLimitProfile(): JvmCacheLimitProfile {
    return when (this) {
        CacheUpperLimitEnum.Auto -> JvmCacheLimitProfile(
            kicker = "AUTO",
            strategy = "系统建议",
            description = "按平台策略动态限制播放缓存。",
            footLabel = "推荐",
            footValue = "弹性",
        )
        CacheUpperLimitEnum.No -> JvmCacheLimitProfile(
            kicker = "OFF",
            strategy = "不缓存",
            description = "关闭播放缓存，仅保留当前流式播放。",
            footLabel = "占用",
            footValue = "最低",
        )
        CacheUpperLimitEnum.OneHundred,
        CacheUpperLimitEnum.FiveHundred,
        CacheUpperLimitEnum.EightHundred,
        CacheUpperLimitEnum.OneG,
        CacheUpperLimitEnum.ThreeG -> JvmCacheLimitProfile(
            kicker = "LOW",
            strategy = "轻量",
            description = "适合系统盘空间紧张的桌面环境。",
            footLabel = "保守",
            footValue = "低占用",
        )
        CacheUpperLimitEnum.FourG -> JvmCacheLimitProfile(
            kicker = "4GB",
            strategy = "日常",
            description = "适合普通在线播放和少量重复收听。",
            footLabel = "基础",
            footValue = "短期",
        )
        CacheUpperLimitEnum.EightG -> JvmCacheLimitProfile(
            kicker = "8GB",
            strategy = "均衡",
            description = "覆盖日常播放和短期重复收听。",
            footLabel = "默认",
            footValue = "均衡",
        )
        CacheUpperLimitEnum.SixteenG -> JvmCacheLimitProfile(
            kicker = "16GB",
            strategy = "高频",
            description = "适合大曲库和无损音源频繁试听。",
            footLabel = "宽松",
            footValue = "长期",
        )
        CacheUpperLimitEnum.ThirtyTwoG,
        CacheUpperLimitEnum.SixtyFourG,
        CacheUpperLimitEnum.OneHundredTwentyEightG -> JvmCacheLimitProfile(
            kicker = "MAX",
            strategy = "大容量",
            description = "适合独立缓存盘或长时间离线重播。",
            footLabel = "高级",
            footValue = "手动",
        )
    }
}

private fun String.addCapacitySpace(): String {
    return trim().replace(
        Regex("""(?<=\d)(?=(PB|TB|GB|MB|KB|B)(\s|$))"""),
        " ",
    )
}
