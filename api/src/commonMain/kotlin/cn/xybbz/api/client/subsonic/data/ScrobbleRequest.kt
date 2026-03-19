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

package cn.xybbz.api.client.subsonic.data

import cn.xybbz.api.client.data.Request
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 上报播放记录请求实体类
 */
@Serializable
@SerialName(value = "ScrobbleRequest")
data class ScrobbleRequest(
    /**
     * 歌曲id
     */
    val id: String,
    /**
     * 播放时间，自1970以来的毫秒数
     */
    val time: Int? = null,
    /**
     * 是否提交，不提交则表示正在播放
     */
    val submission: Boolean? = false
): Request()
