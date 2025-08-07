package cn.xybbz.api.client.jellyfin.data

import cn.xybbz.api.enums.jellyfin.MediaType
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


/**
 * Create new playlist dto.
 */
@JsonClass(generateAdapter = true)
data class CreatePlaylistRequest(
    /**
     * The name of the new playlist.
     */
    @param:Json(name = "Name")
    val name: String? = null,
    /**
     * Item ids to add to the playlist.
     */
    @param:Json(name = "Ids")
    val ids: List<String>? = null,
    /**
     * The user id.
     */
    @param:Json(name = "UserId")
    val userId: String? = null,
    /**
     * The media type.
     */
    @param:Json(name = "MediaType")
    val mediaType: MediaType? = null,
    /**
     * The playlist users.
     */
    @param:Json(name = "Users")
    val users: List<PlaylistUserPermissions>? = null,
    /**
     * A value indicating whether the playlist is .
     */
    @param:Json(name = "IsPublic")
    val isPublic: Boolean? = false,
)

/**
 * Class to hold data on user permissions for playlists.
 */
@JsonClass(generateAdapter = true)
data class PlaylistUserPermissions(
    /**
     * The user id.
     */
    @param:Json(name = "UserId")
    val userId: String,
    /**
     * A value indicating whether the user has edit permissions.
     */
    @param:Json(name = "CanEdit")
    val canEdit: Boolean,
)
