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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * 收藏和取消收藏接口返回实体类
 * @author xybbz
 * @date 2025/02/11
 * @constructor 创建[FavoriteResponse]
 * @param [itemId] 项目ID
 * @param [isFavorite] 是否收藏
 */
@Serializable
data class FavoriteResponse(
    @SerialName(value = "ItemId")
    val itemId: String? = null,
    @SerialName(value = "IsFavorite")
    val isFavorite: Boolean,
)
