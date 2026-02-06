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
 * Enum ImageType.
 */
@Serializable
enum class ImageType(
    private val serialName: String,
) {
    @SerialName(value = "Primary")
    PRIMARY("Primary"),
    @SerialName(value = "Art")
    ART("Art"),
    @SerialName(value = "Backdrop")
    BACKDROP("Backdrop"),
    @SerialName(value = "Banner")
    BANNER("Banner"),
    @SerialName(value = "Logo")
    LOGO("Logo"),
    @SerialName(value = "Thumb")
    THUMB("Thumb"),
    @SerialName(value = "Disc")
    DISC("Disc"),
    @SerialName(value = "Box")
    BOX("Box"),
    @SerialName(value = "Screenshot")
    SCREENSHOT("Screenshot"),
    @SerialName(value = "Menu")
    MENU("Menu"),
    @SerialName(value = "Chapter")
    CHAPTER("Chapter"),
    @SerialName(value = "BoxRear")
    BOX_REAR("BoxRear"),
    @SerialName(value = "Profile")
    PROFILE("Profile"),
    ;

    /**
     * Get the serial name of the enum member.
     */
    override fun toString(): String = serialName
}
