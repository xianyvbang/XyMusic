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

package cn.xybbz.music

import xymusic_kmp.composeapp.generated.resources.*

import cn.xybbz.localdata.enums.CacheUpperLimitEnum

actual fun cacheUpperLimitOptions(): List<CacheUpperLimitOption> {
    return listOf(
        CacheUpperLimitOption(CacheUpperLimitEnum.Auto, "", Res.string.cache_upper_limit_options_text_01),
        CacheUpperLimitOption(CacheUpperLimitEnum.No, "", Res.string.jvm_cache_limit_screen_text_20),
        CacheUpperLimitOption(CacheUpperLimitEnum.OneHundred, "100MB"),
        CacheUpperLimitOption(CacheUpperLimitEnum.FiveHundred, "500MB"),
        CacheUpperLimitOption(CacheUpperLimitEnum.EightHundred, "800MB"),
        CacheUpperLimitOption(CacheUpperLimitEnum.OneG, "1GB"),
        CacheUpperLimitOption(CacheUpperLimitEnum.ThreeG, "2GB"),
        CacheUpperLimitOption(CacheUpperLimitEnum.FourG, "4GB"),
        CacheUpperLimitOption(CacheUpperLimitEnum.EightG, "8GB"),
    )
}
