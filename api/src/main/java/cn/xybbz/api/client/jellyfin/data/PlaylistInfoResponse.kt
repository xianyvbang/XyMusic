package cn.xybbz.api.client.jellyfin.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlaylistInfoResponse(
    /**
     * Gets or sets a value indicating whether the playlist is publicly readable.
     */
    @param:Json(name = "OpenAccess")
    val openAccess: Boolean,
    @param:Json(name = "Shares")
    val shares: List<UserShare>,
    @param:Json(name = "ItemIds")
    val itemIds : List<String>
)

data class UserShare(
    @param:Json(name = "UserId")
    val userId: String,
    @param:Json(name = "CanEdit")
    val canEdit: Boolean
)
