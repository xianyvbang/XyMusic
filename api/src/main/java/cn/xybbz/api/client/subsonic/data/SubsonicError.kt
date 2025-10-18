package cn.xybbz.api.client.subsonic.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * SubsonicError
 */
@JsonClass(generateAdapter = true)
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
    @param:Json(name = "code")
    val code: Int,

    /**
     * A URL (documentation, configuration, etc) which may provide additional context for the
     * error)
     */
    @param:Json(name = "helpUrl")
    val helpURL: String? = null,

    /**
     * The optional error message
     */
    @param:Json(name = "message")
    val message: String? = null
)