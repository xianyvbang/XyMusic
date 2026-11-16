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

import androidx.compose.runtime.Composable
import cn.xybbz.localdata.enums.CacheUpperLimitEnum
import cn.xybbz.music.cacheUpperLimitOptions
import cn.xybbz.ui.components.JvmSettingPickRow
import cn.xybbz.ui.components.displayMessage
import org.jetbrains.compose.resources.stringResource
import xymusic.composeapp.generated.resources.Res
import xymusic.composeapp.generated.resources.jvm_cache_limit_screen_text_18
import xymusic.composeapp.generated.resources.jvm_cache_limit_screen_text_21
import xymusic.composeapp.generated.resources.jvm_cache_limit_screen_text_22
import xymusic.composeapp.generated.resources.jvm_cache_limit_screen_text_25
import xymusic.composeapp.generated.resources.jvm_cache_limit_screen_text_26
import xymusic.composeapp.generated.resources.jvm_cache_limit_screen_text_29
import xymusic.composeapp.generated.resources.jvm_cache_limit_screen_text_30
import xymusic.composeapp.generated.resources.jvm_cache_limit_screen_text_33
import xymusic.composeapp.generated.resources.jvm_cache_limit_screen_text_34
import xymusic.composeapp.generated.resources.jvm_cache_limit_screen_text_36
import xymusic.composeapp.generated.resources.jvm_cache_limit_screen_text_37
import xymusic.composeapp.generated.resources.jvm_cache_limit_screen_text_40
import xymusic.composeapp.generated.resources.jvm_cache_limit_screen_text_41
import xymusic.composeapp.generated.resources.recommend

/**
 * 设置页内嵌的音乐缓存上限设置项。
 *
 * 承接原音乐缓存页的缓存上限档位选择能力，档位文案沿用缓存页同一套展示画像；
 * 真实占用、缓存目录与批量清理由存储管理分组统一负责，避免设置页出现重复入口。
 *
 * 需要在设置页分组的内容流中调用，各项会按分组宽度自动参与横向排布。
 *
 * @param selectedLimit 当前已保存的缓存上限档位，用于标记选中态。
 * @param onLimitSelected 用户选择新档位时的回调，设置写入由调用方负责。
 */
@Composable
internal fun JvmCacheLimitSettingItems(
    selectedLimit: CacheUpperLimitEnum,
    onLimitSelected: (CacheUpperLimitEnum) -> Unit,
) {
    // 档位选项直接作为分组内的设置项渲染，与其它设置项共用横向排布规则。
    cacheUpperLimitOptions().forEach { option ->
        val profile = option.limit.cacheLimitProfile()
        JvmSettingPickRow(
            title = option.displayMessage().addCapacitySpace(),
            description = profile.description,
            selected = selectedLimit == option.limit,
            // 右侧标签沿用缓存页的档位定位，便于快速区分轻量、常规和大容量策略。
            tag = profile.footLabel,
            onClick = {
                // 重复点击当前档位不触发写入，减少无意义的设置更新。
                if (selectedLimit != option.limit) {
                    onLimitSelected(option.limit)
                }
            },
        )
    }
}

/**
 * 缓存上限档位的展示画像。
 *
 * @property description 档位描述，说明该档位适合的使用场景。
 * @property footLabel 档位定位标签，设置页用作行尾标签。
 */
private data class JvmCacheLimitProfile(
    val description: String,
    val footLabel: String,
)

/**
 * 将缓存上限枚举映射为设置页单选行的展示画像。
 */
@Composable
private fun CacheUpperLimitEnum.cacheLimitProfile(): JvmCacheLimitProfile {
    return when (this) {
        // 自动档位主要用于兼容共享枚举，JVM 端会迁移到固定容量档位。
        CacheUpperLimitEnum.Auto -> JvmCacheLimitProfile(
            description = stringResource(Res.string.jvm_cache_limit_screen_text_18),
            footLabel = stringResource(Res.string.recommend),
        )
        // 不缓存档位同样用于兼容历史设置，说明关闭缓存后的最低占用行为。
        CacheUpperLimitEnum.No -> JvmCacheLimitProfile(
            description = stringResource(Res.string.jvm_cache_limit_screen_text_21),
            footLabel = stringResource(Res.string.jvm_cache_limit_screen_text_22),
        )
        // 小容量档位统一归为轻量策略，适合磁盘空间更紧张的桌面环境。
        CacheUpperLimitEnum.OneHundred,
        CacheUpperLimitEnum.FiveHundred,
        CacheUpperLimitEnum.EightHundred,
        CacheUpperLimitEnum.OneG,
        CacheUpperLimitEnum.ThreeG -> JvmCacheLimitProfile(
            description = stringResource(Res.string.jvm_cache_limit_screen_text_25),
            footLabel = stringResource(Res.string.jvm_cache_limit_screen_text_26),
        )
        // 4GB 是日常轻度使用档位，偏短期缓存和基础复听。
        CacheUpperLimitEnum.FourG -> JvmCacheLimitProfile(
            description = stringResource(Res.string.jvm_cache_limit_screen_text_29),
            footLabel = stringResource(Res.string.jvm_cache_limit_screen_text_30),
        )
        // 8GB 是桌面端默认推荐档位，兼顾缓存命中和磁盘占用。
        CacheUpperLimitEnum.EightG -> JvmCacheLimitProfile(
            description = stringResource(Res.string.jvm_cache_limit_screen_text_33),
            footLabel = stringResource(Res.string.jvm_cache_limit_screen_text_34),
        )
        // 16GB 适合无损音源或大曲库的高频试听场景。
        CacheUpperLimitEnum.SixteenG -> JvmCacheLimitProfile(
            description = stringResource(Res.string.jvm_cache_limit_screen_text_36),
            footLabel = stringResource(Res.string.jvm_cache_limit_screen_text_37),
        )
        // 更大的容量档位归为高级策略，通常需要用户明确预留独立缓存空间。
        CacheUpperLimitEnum.ThirtyTwoG,
        CacheUpperLimitEnum.SixtyFourG,
        CacheUpperLimitEnum.OneHundredTwentyEightG -> JvmCacheLimitProfile(
            description = stringResource(Res.string.jvm_cache_limit_screen_text_40),
            footLabel = stringResource(Res.string.jvm_cache_limit_screen_text_41),
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
