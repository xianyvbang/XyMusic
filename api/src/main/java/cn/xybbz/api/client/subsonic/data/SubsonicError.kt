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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * SubsonicError
 */
@Serializable
data class SubsonicError (
    /**
     * The error code.
     * * 0: A generic error.
     * * 10: Required parameter is missing.
     * * 20: Incompatible Subsonic REST protocol version. Client must upgrade.
     * * 30: Incompatible Subsonic REST protocol version. Server must upgrade.
     * * 40: Wrong username or password.
     * * 41: Token authentication not supported for LDAP users.
     * * 42: Provided authentication mechanism not supported.
     * * 43: Multiple conflicting authentication mechanisms provided.
     * * 44: Invalid API key.
     * * 50: User is not authorized for the given operation.
     * * 60: The trial period for the Subsonic server is over. Please upgrade to Subsonic
     * Premium. Visit subsonic.org for details.
     * * 70: The requested data was not found.
     */
    @SerialName(value = "code")
    val code: Int,

    /**
     * A URL (documentation, configuration, etc) which may provide additional context for the
     * error)
     */
    @SerialName(value = "helpUrl")
    val helpURL: String? = null,

    /**
     * The optional error message
     */
    @SerialName(value = "message")
    val message: String? = null
)