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

import cn.xybbz.localdata.enums.CacheUpperLimitEnum

private const val MIB = 1024L * 1024L
private const val GIB = 1024L * MIB

internal fun CacheUpperLimitEnum.jvmCacheLimitBytes(): Long {
    return when (this) {
        CacheUpperLimitEnum.Auto,
        CacheUpperLimitEnum.No,
        CacheUpperLimitEnum.OneHundred,
        CacheUpperLimitEnum.FiveHundred,
        CacheUpperLimitEnum.EightHundred,
        CacheUpperLimitEnum.OneG -> 2L * GIB
        CacheUpperLimitEnum.ThreeG -> 2L * GIB
        CacheUpperLimitEnum.FourG -> 4L * GIB
        CacheUpperLimitEnum.EightG -> 8L * GIB
        CacheUpperLimitEnum.SixteenG -> 16L * GIB
        CacheUpperLimitEnum.ThirtyTwoG -> 32L * GIB
        CacheUpperLimitEnum.SixtyFourG -> 64L * GIB
        CacheUpperLimitEnum.OneHundredTwentyEightG -> 128L * GIB
    }
}
