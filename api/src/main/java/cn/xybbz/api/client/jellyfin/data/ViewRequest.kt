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

package cn.xybbz.api.client.jellyfin.data

import cn.xybbz.api.client.data.Request
import cn.xybbz.api.enums.jellyfin.CollectionType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 媒体库请求实体类
 * @author Administrator
 * @date 2025/12/02
 * @constructor 创建[ViewRequest]
 */
@Serializable
@SerialName(value = "ViewRequest")
data class ViewRequest(
    /**
     * 是否包括外部视图，如频道或直播电视。
     */
    @SerialName(value = "IncludeExternalContent")
    val includeExternalContent: Boolean? = null,
    /**
     * 查询视图类型
     */
    @SerialName(value = "PresetViews")
    val presetViews: List<CollectionType>? = emptyList(),
    /**
     * 是否显示隐藏内容
     */
    @SerialName(value = "IncludeHidden")
    val includeHidden: Boolean? = null
) : Request()
