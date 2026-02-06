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
 * 响应实体包装类
 * @author Administrator
 * @date 2025/12/02
 * @constructor 创建[Response]
 */
@Serializable
class Response<T>(
    @SerialName(value = "Items")
    val items: List<T>,
    /**
     * The total number of records available.
     */
    @SerialName(value = "TotalRecordCount")
    val totalRecordCount: Int,
    /**
     * The index of the first record in Items.
     */
    @SerialName(value = "StartIndex")
    val startIndex: Int? = null,
)