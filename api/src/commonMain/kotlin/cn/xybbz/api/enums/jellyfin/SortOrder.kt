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

package cn.xybbz.api.enums.jellyfin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * An enum representing the sorting order.
 */
@Serializable
enum class SortOrder(
    private val serialName: String,
) {
    @SerialName(value = "Ascending")
    ASCENDING("Ascending"),

    @SerialName(value = "Descending")
    DESCENDING("Descending"), ;

    /**
     * Get the serial name of the enum member.
     */
    override fun toString(): String = serialName
}