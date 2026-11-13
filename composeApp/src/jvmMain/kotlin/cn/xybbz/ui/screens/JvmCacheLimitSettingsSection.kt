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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cn.xybbz.localdata.enums.CacheUpperLimitEnum
import cn.xybbz.music.cacheUpperLimitOptions
import cn.xybbz.ui.components.JvmSettingBaseRow
import cn.xybbz.ui.components.JvmSettingNote
import cn.xybbz.ui.components.JvmSettingPickRow
import cn.xybbz.ui.components.displayMessage
import cn.xybbz.ui.theme.XyTheme
import org.jetbrains.compose.resources.stringResource
import xymusic.composeapp.generated.resources.Res
import xymusic.composeapp.generated.resources.jvm_cache_limit_screen_text_12
import xymusic.composeapp.generated.resources.jvm_cache_limit_screen_text_13
import xymusic.composeapp.generated.resources.jvm_cache_limit_screen_text_14
import xymusic.composeapp.generated.resources.music_note_24px

/**
 * 设置页内嵌的音乐缓存设置项。
 *
 * 承接原音乐缓存页的缓存上限档位选择与真实占用展示能力：
 * 占用信息用设置页只读设置行呈现，档位选择用设置页通用单选行呈现，
 * 批量清理与缓存目录仍由存储管理分组统一负责，避免设置页出现重复入口。
 *
 * 需要在设置页分组的内容流中调用，各项会按分组宽度自动参与横向排布。
 *
 * @param selectedLimit 当前已保存的缓存上限档位，用于标记选中态。
 * @param usedCacheSizeLabel 当前音乐播放缓存真实占用文本。
 * @param onLimitSelected 用户选择新档位时的回调，设置写入由调用方负责。
 */
@Composable
internal fun JvmCacheLimitSettingItems(
    selectedLimit: CacheUpperLimitEnum,
    usedCacheSizeLabel: String,
    onLimitSelected: (CacheUpperLimitEnum) -> Unit,
) {
    // 真实占用沿用缓存页的展示方式，只读展示，清理入口统一放在存储管理分组。
    JvmSettingBaseRow(
        icon = Res.drawable.music_note_24px,
        title = stringResource(Res.string.jvm_cache_limit_screen_text_13),
        description = stringResource(Res.string.jvm_cache_limit_screen_text_14),
        descriptionMaxLines = 2,
        descriptionOverflow = TextOverflow.Visible,
        minHeight = 64.dp,
        horizontalPadding = XyTheme.dimens.contentPadding,
        verticalPadding = XyTheme.dimens.outerVerticalPadding,
        iconSelected = true,
        trailing = {
            JvmStorageValuePill(text = usedCacheSizeLabel)
        },
    )

    // 档位选择与缓存页共用同一套展示画像，保证两处文案与选中态完全一致。
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

    // 迁移缓存页的清理策略说明，并指向设置页存储分组中已有的清理与路径入口。
    JvmSettingNote(text = stringResource(Res.string.jvm_cache_limit_screen_text_12))
}
